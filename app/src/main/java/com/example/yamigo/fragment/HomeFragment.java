package com.example.yamigo.fragment;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yamigo.InfoCenterActivity;
import com.example.yamigo.MainActivity;
import com.example.yamigo.R;
import com.example.yamigo.message.DeviceStatusMessage;
import com.example.yamigo.message.RealDataMessage;
import com.example.yamigo.websocketclient.RealDataWebSocketClient;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
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

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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

    private ImageView video_player_btn;

    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private LibVLC libVLC;
    private MediaPlayer mediaPlayer = null;
    private MediaPlayer.EventListener mPlayerListener = new MyPlayerListener(this);

    private LineChart lineChart;
    private ArrayList<Entry> temp_values;
    private ArrayList<Entry> humi_values;
    private LineDataSet temp_data_set;
    private LineDataSet humi_data_set;
    private ArrayList<ILineDataSet> dataSets;
    private LineData lineData;
    private Legend legend;
    private LegendEntry[] legendEntrys = new LegendEntry[2];

    private RealDataWebSocketClient client;

    private EventBus eventBus_self;

    private ExecutorService service;
    private CountDownLatch countDownLatch;

    private String video_url = null;
    private String video_token;
    private static final String web_socket_url = "ws://113.54.231.165:12098/ws/get_sensor_data";

    private static int[] colorClassArray = new int[] {Color.RED, Color.BLUE};
    private static String[] legendName = new String[] {"温度", "湿度"};

    // 逻辑处理器数量
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    private static int message_count = 0;

    // EventBus 接收者实时接收数据并更新组件
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onReceiveRealDataMessage(RealDataMessage message) {
        double temp = message.getTemp();
        double humi = message.getHumi();
        boolean smok = message.isSmok();

        // 处理数据（小数点后两位）
        BigDecimal bigDecimal = new BigDecimal(temp);
        bigDecimal = bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP);
        temp = bigDecimal.doubleValue();
        bigDecimal = new BigDecimal(humi);
        bigDecimal = bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP);
        humi = bigDecimal.doubleValue();

        // 规定安全范围
        if (judge_temp_danger(temp)) {
            temp_textView.setTextColor(mainActivity.getResources().getColor(R.color.red));
            message_count += 1;
            NotificationChannel channel = new NotificationChannel("default", "name", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = (NotificationManager) mainActivity.getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
            Notification notification = new NotificationCompat.Builder(mainActivity, "default")
                    .setContentTitle(message_count + " 条新消息")
                    .setContentText("温度异常")
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .build();
            manager.notify(message_count, notification);
        } else {
            temp_textView.setTextColor(mainActivity.getResources().getColor(R.color.teal_700));
        }
        temp_textView.setText(temp + "℃");

        if (judge_humi_danger(humi)) {
            humi_textView.setTextColor(mainActivity.getResources().getColor(R.color.red));
            message_count += 1;
            NotificationChannel channel = new NotificationChannel("default", "name", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = (NotificationManager) mainActivity.getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
            Notification notification = new NotificationCompat.Builder(mainActivity, "default")
                    .setContentTitle(message_count + " 条新消息")
                    .setContentText("湿度异常")
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .build();
            manager.notify(message_count, notification);
        } else {
            humi_textView.setTextColor(mainActivity.getResources().getColor(R.color.teal_700));
        }
        humi_textView.setText(humi + "%");

        if (judge_smok_danger(smok)) {
            smok_textView.setTextColor(mainActivity.getResources().getColor(R.color.red));
            smok_textView.setText("异常");
            message_count += 1;
            NotificationChannel channel = new NotificationChannel("default", "name", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = (NotificationManager) mainActivity.getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
            Notification notification = new NotificationCompat.Builder(mainActivity, "default")
                    .setContentTitle(message_count + " 条新消息")
                    .setContentText("烟雾异常")
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .build();
            manager.notify(message_count, notification);
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
    public void onResume() {
        super.onResume();
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainActivity = (MainActivity) getActivity();

        // 线程池
        service = Executors.newFixedThreadPool(CORE_POOL_SIZE);
        // 设置要阻塞的线程数
        countDownLatch = new CountDownLatch(1);

        service.submit(new Runnable() {
            @Override
            public void run() {
                // 初始化组件
                init_view(view);
            }
        });

        service.submit(new Runnable() {
            @Override
            public void run() {
                // EventBus 订阅者注册
                register_event_bus();
                countDownLatch.countDown();
            }
        });

        // 阻塞线程，直到EventBus被注册
        try {
            countDownLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 获取传感器数据
        update_real_data();

        // 获得温度和湿度的坐标点数据
        get_line_chart_values();

        // 绘制折线图
        draw_line_chart();
    }

    private void get_video_url() {
        SharedPreferences sp = mainActivity.getSharedPreferences("token", Context.MODE_PRIVATE);
        String login_token = sp.getString("token", "");

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://113.54.231.165:12098/api/get_rtmp_info")
                .header("Authorization", "Bearer " + login_token)
                .build();

        try {
            Response response = client.newCall(request).execute();
            String responseData = response.body().string();
            Log.d(TAG, responseData);
            JSONObject jsonObject = new JSONObject(responseData);
            int code = jsonObject.getInt("code");
            if (code == 200) {
                String data = jsonObject.getString("data");
                JSONObject url_token_jsonObject = new JSONObject(data);
                video_url = "rtmp://113.54.231.165:12091" + url_token_jsonObject.getString("url");
                video_token = url_token_jsonObject.getString("token");
                video_url = video_url + "?token=" + video_token;
            } else {
                Log.d(TAG, "获取视频流url和token失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void update_real_data() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                URI uri = URI.create(web_socket_url);
                client = new RealDataWebSocketClient(uri) {
                    // 每一秒收到传感器数据后更新界面
                    @Override
                    public void onMessage(String message) {
                        super.onMessage(message);

                        // 提取后端发来的传感器数据
                        try {
                            JSONObject response = new JSONObject(message);
                            int code = response.getInt("code");
                            if (code == 200) {
                                JSONObject real_data = response.getJSONObject("data");
                                double temp = real_data.getDouble("temp");
                                double humi = real_data.getDouble("humi");
                                boolean smok = real_data.getBoolean("smok");
                                Log.d(TAG, "temp:" + temp + " humi:" + humi + " smok:" + (smok ? "异常" : "无"));

                                RealDataMessage realDataMessage = new RealDataMessage(temp, humi, smok);
                                pushRealData(realDataMessage);
                                DeviceStatusMessage test_deviceStatusMessage = new DeviceStatusMessage(true, true, true);
                                pushDeviceStatusData(test_deviceStatusMessage);
                            } else {
                                Log.d(TAG, "传感器数据接收错误");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };

                try {
                    client.connectBlocking();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

//                // 随机数测试
//                Random random = new Random();
//                double test_temp = random.nextDouble() + 20;
//                double test_humi = random.nextDouble() + 20;
//                boolean test_smok = random.nextBoolean();
//                RealDataMessage test_realDataMessage = new RealDataMessage(test_temp, test_humi, test_smok);
//                pushRealData(test_realDataMessage);
//                DeviceStatusMessage test_deviceStatusMessage = new DeviceStatusMessage(true, false, true);
//                pushDeviceStatusData(test_deviceStatusMessage);
            }
        }).start();
    }



    private void init_view(View view) {
        viewPager2 = mainActivity.findViewById(R.id.main_page_viewpage2);

        temp_textView = view.findViewById(R.id.home_fragment_real_data_temperature);
        humi_textView = view.findViewById(R.id.home_fragment_real_data_humidity);
        smok_textView = view.findViewById(R.id.home_fragment_real_data_smoke);

        video_player_btn = view.findViewById(R.id.video_player_btn);
        init_player_btn();

        toolbar = view.findViewById(R.id.home_fragment_toolbar);
        init_toolbar();

        surfaceView = view.findViewById(R.id.home_fragment_surface);
        init_surface_view();

        lineChart = view.findViewById(R.id.home_fragment_line_chart);
        init_line_chart();
    }

    private void init_player_btn() {
        video_player_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.video_player_btn:
                        CountDownLatch count = new CountDownLatch(1);
                        service.submit(new Runnable() {
                            @Override
                            public void run() {
                                get_video_url();
                                count.countDown();
                            }
                        });

                        try {
                            count.await();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (video_url != null) {
                            Log.d(TAG, video_url);
                            video_player_btn.setVisibility(View.GONE);
                            createPlayer(video_url);
                            mediaPlayer.play();
                        }
                        break;
                }
            }
        });
    }

    private void init_toolbar() {
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home_fragment_info_center:
                        Intent intent = new Intent(getActivity(), InfoCenterActivity.class);
                        startActivity(intent);
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
    }

    private void init_surface_view(){
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
    }

    private void init_line_chart() {
        lineChart.setTouchEnabled(true);    // 允许触摸事件
        lineChart.setPinchZoom(true);
        lineChart.setDescription(null);     // 去掉图标注释
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);   // 设置x轴的位置
        lineChart.getXAxis().setDrawGridLines(false);       // 去掉竖网格线
        lineChart.getAxisLeft().setDrawGridLines(false);    // 和下一行一起去掉横网格线
        lineChart.getAxisRight().setDrawGridLines(false);

        // 设置图例
        init_legend();

        // 设置坐标轴
        init_Axis();
    }

    private void draw_line_chart() {
        // 绘制
        lineChart.setData(lineData);
        lineChart.invalidate();
    }

    private void get_line_chart_values() {
        temp_values = new ArrayList<>();
        humi_values = new ArrayList<>();
        dataSets = new ArrayList<>();

        // 测试使用随机数添加数据
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            temp_values.add(new Entry(i, random.nextInt(40 - 10 + 1) + 10));
            humi_values.add(new Entry(i, random.nextInt(40 - 10 + 1) + 10));
        }

        temp_data_set = new LineDataSet(temp_values, legendName[0]);
        temp_data_set.setColor(colorClassArray[0]);

        humi_data_set = new LineDataSet(humi_values, legendName[1]);
        humi_data_set.setColor(colorClassArray[1]);

        dataSets.add(temp_data_set);
        dataSets.add(humi_data_set);

        lineData = new LineData(dataSets);
    }

    private void init_legend() {
        legend = lineChart.getLegend();
        // 图例:线段
        legend.setForm(Legend.LegendForm.LINE);
        legend.setXEntrySpace(10);
        legend.setFormToTextSpace(10);

        for (int i = 0; i < legendEntrys.length; i++) {
            LegendEntry entry = new LegendEntry();
            entry.formColor = colorClassArray[i];
            entry.label = String.valueOf(legendName[i]);
            legendEntrys[i] = entry;
        }

        legend.setCustom(legendEntrys);
    }

    private void init_Axis() {
        YAxis yAxisLeft = lineChart.getAxisLeft();
        YAxis yAxisRight = lineChart.getAxisRight();
        yAxisLeft.setAxisMinimum(0f);
        yAxisRight.setAxisMinimum(0f);
        yAxisLeft.setValueFormatter(new TempAxisValueFormatter());
        yAxisRight.setValueFormatter(new HumiAxisValueFormatter());
    }

    private void register_event_bus(){
        eventBus_self = new EventBus();
        eventBus_self.register(HomeFragment.this);
    }

    // EventBus 发布RealData
    private void pushRealData(RealDataMessage message) {
        eventBus_self.postSticky(message);
    }

    private void pushDeviceStatusData(DeviceStatusMessage message) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("temp_status", message.isTemp_status());
        bundle.putBoolean("humi_status", message.isHumi_status());
        bundle.putBoolean("smok_status", message.isSmok_status());
        getParentFragmentManager().setFragmentResult("DeviceStatus", bundle);
        Log.d(TAG, "发送设备状态");
    }

    // 销毁EventBus
    private void deleteEventBus() {
        eventBus_self.unregister(HomeFragment.this);
    }

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releasePlayer() {
        if (libVLC == null) {
            return;
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            final IVLCVout vout = mediaPlayer.getVLCVout();
            vout.removeCallback(this);
            vout.detachViews();
            holder = null;
            libVLC.release();
            libVLC = null;
            return;
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

    private boolean judge_temp_danger(double temp) {
        if (temp < 10 || temp > 35) {
            return true;
        } else {
            return false;
        }
    }

    private boolean judge_humi_danger(double humi) {
        if (humi < 20 || humi > 70) {
            return true;
        } else {
            return false;
        }
    }

    private boolean judge_smok_danger(boolean smok) {
        if (smok) {
            return true;
        } else {
            return false;
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

    private class TempAxisValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return value + "℃";
        }
    }

    private class HumiAxisValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return value + "%";
        }
    }
}