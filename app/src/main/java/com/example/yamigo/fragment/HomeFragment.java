package com.example.yamigo.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Outline;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.TextView;

import com.example.yamigo.InfoCenterActivity;
import com.example.yamigo.MainActivity;
import com.example.yamigo.R;
import com.example.yamigo.message.RealDataMessage;
import com.example.yamigo.websocketclient.RealDataWebSocketClient;
import com.google.android.material.appbar.MaterialToolbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.java_websocket.client.WebSocketClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Random;

public class HomeFragment extends Fragment implements IVLCVout.Callback {
    private static final String TAG = "HomeFragment";

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private Context context = getContext();
    private MainActivity mainActivity;

    private ViewPager2 viewPager2;

    private MaterialToolbar toolbar;

    private TextView temp_textView;
    private TextView humi_textView;
    private TextView smok_textView;

    private RealDataWebSocketClient client;

    private EventBus eventBus_self;

    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private LibVLC libVLC;
    private MediaPlayer mediaPlayer = null;
    private MediaPlayer.EventListener mPlayerListener = new MyPlayerListener(this);

    private static String video_url = "rtmp://113.54.231.165:12091/hls/test?token=123456";
    private static String web_socket_url = "ws://113.54.232.23:12098/ws/sensor";

    // EventBus 接收者实时接收数据并更新组件
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onReceiveRealDataMessage(RealDataMessage message) {
        double temp = message.getTemp();
        double humi = message.getHumi();
        boolean smok = message.isSmok();

        // 处理数据（小数点后两位）
        BigDecimal bigDecimal = new BigDecimal(temp);
        bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
        temp = bigDecimal.doubleValue();
        bigDecimal = new BigDecimal(humi);
        bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
        humi = bigDecimal.doubleValue();

        // 规定安全范围
        if (temp < 10.0 || temp > 35.0) {
            temp_textView.setTextColor(mainActivity.getResources().getColor(R.color.red));
        } else {
            temp_textView.setTextColor(mainActivity.getResources().getColor(R.color.teal_700));
        }
        temp_textView.setText(String.valueOf(temp));

        if (humi < 20 || humi > 70) {
            humi_textView.setTextColor(mainActivity.getResources().getColor(R.color.red));
        } else {
            humi_textView.setTextColor(mainActivity.getResources().getColor(R.color.teal_700));
        }
        humi_textView.setText(String.valueOf(humi));

        if (smok) {
            smok_textView.setTextColor(mainActivity.getResources().getColor(R.color.red));
            smok_textView.setText("异常");
        } else {
            smok_textView.setTextColor(mainActivity.getResources().getColor(R.color.teal_700));
            smok_textView.setText("无");
        }
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainActivity = (MainActivity) getActivity();

        viewPager2 = mainActivity.findViewById(R.id.main_page_viewpage2);

        temp_textView = view.findViewById(R.id.home_fragment_real_data_temperature);
        humi_textView = view.findViewById(R.id.home_fragment_real_data_humidity);
        smok_textView = view.findViewById(R.id.home_fragment_real_data_smoke);

        toolbar = view.findViewById(R.id.home_fragment_toolbar);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home_fragment_info_center:
                        Intent intent = new Intent(getActivity(), InfoCenterActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.home_fragment_home_item:
                        break;
                    case R.id.home_fragment_device_item:
                        viewPager2.setCurrentItem(1);
                        break;
                    case R.id.home_fragment_setting_item:
                        viewPager2.setCurrentItem(2);
                        break;
                }
                return true;
            }
        });

        surfaceView = view.findViewById(R.id.home_fragment_surface);
        surfaceView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                Rect rect = new Rect();
                view.getGlobalVisibleRect(rect);
                Rect selfRect = new Rect(0, 0, rect.right - rect.left, rect.bottom - rect.top);
                outline.setRoundRect(selfRect, 60);
            }
        });
        surfaceView.setClipToOutline(true);
        holder = surfaceView.getHolder();

        // EventBus 订阅者注册
        eventBus_self = new EventBus();
        eventBus_self.register(this);

        // 接收传感器数据
        new Thread(new Runnable() {
            @Override
            public void run() {
//                URI uri = URI.create(web_socket_url);
//                client = new RealDataWebSocketClient(uri) {
//                    // 每一秒收到传感器数据后更新界面
//                    @Override
//                    public void onMessage(String message) {
//                        super.onMessage(message);
//
//                        // 提取后端发来的传感器数据
//                        try {
//                            JSONObject real_data = new JSONObject(message);
//                            double temp = real_data.getDouble("temp");
//                            double humi = real_data.getDouble("humi");
//                            boolean smok = real_data.getBoolean("smok");
//                            Log.d(TAG, "temp:" + temp + " humi:" + humi + " smok:" + (smok ? "异常" : "无"));
//
//                            RealDataMessage realDataMessage = new RealDataMessage(temp, humi, smok);
//                            pushRealData(realDataMessage);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                };
//
//                try {
//                    client.connectBlocking();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

                // 随机数测试
                Random random = new Random();
                double test_temp = random.nextDouble();
                double test_humi = random.nextDouble();
                boolean test_smok = random.nextBoolean();
                RealDataMessage test_realDataMessage = new RealDataMessage(test_temp, test_humi, test_smok);
                pushRealData(test_realDataMessage);
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        createPlayer(video_url);
    }

    @Override
    public void onPause() {
        super.onPause();
        releasePlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
        closeConnect(client);
        deleteEventBus();
    }

    // EventBus 发布RealData
    public void pushRealData(RealDataMessage message) {
        eventBus_self.postSticky(message);
    }

    // 销毁EventBus
    public void deleteEventBus() {
        eventBus_self.unregister(this);
    }

    // 判断温度是否安全，并修改组件


    private void createPlayer(String url) {
        try {
            ArrayList<String> options = new ArrayList<String>();
            options.add("--aout=opensles");
            options.add("--audio-time-stretch");
            options.add("-vvv");
            libVLC = new LibVLC(requireContext(), options);
            holder.setKeepScreenOn(true);

            mediaPlayer = new MediaPlayer(libVLC);
            mediaPlayer.setEventListener(mPlayerListener);

            final IVLCVout vout = mediaPlayer.getVLCVout();
            vout.setVideoView(surfaceView);
            vout.addCallback(this);
            vout.attachViews();

            Media media = new Media(libVLC, Uri.parse(url));
            mediaPlayer.setMedia(media);
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releasePlayer() {
        if (libVLC == null) {
            mediaPlayer.stop();
            final IVLCVout vout = mediaPlayer.getVLCVout();
            vout.removeCallback(this);
            vout.detachViews();
            holder = null;
            libVLC.release();
            libVLC = null;
        }
    }

    private static class MyPlayerListener implements MediaPlayer.EventListener {
        private WeakReference<HomeFragment> mOwner;

        public MyPlayerListener(HomeFragment owner) {
            mOwner = new WeakReference<HomeFragment>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            HomeFragment player = mOwner.get();

            switch (event.type) {
                case MediaPlayer.Event.EndReached:
                    player.releasePlayer();
                    break;
                case MediaPlayer.Event.Playing:
                case MediaPlayer.Event.Paused:
                case MediaPlayer.Event.Stopped:
                default:
                    break;
            }
        }
    }

    private void closeConnect(WebSocketClient client) {
        try {
            if (null != client){
                client.closeBlocking();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client = null;
        }
    }

    @Override
    public void onNewLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {

    }

    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {

    }

    @Override
    public void onHardwareAccelerationError(IVLCVout vlcVout) {

    }
}