package com.example.yamigo.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.example.yamigo.MainActivity;
import com.example.yamigo.R;
import com.google.android.material.appbar.MaterialToolbar;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class HomeFragment extends Fragment implements IVLCVout.Callback {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private Context context = getContext();
    private MainActivity mainActivity;
    private ViewPager2 viewPager2;

    private MaterialToolbar toolbar;

    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private LibVLC libVLC;
    private MediaPlayer mediaPlayer = null;
    private MediaPlayer.EventListener mPlayerListener = new MyPlayerListener(this);

    private static String url = "rtmp://113.54.231.165:12091/hls/test?token=123456";

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

        toolbar = view.findViewById(R.id.home_fragment_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home_item:
                        break;
                    case R.id.device_item:
                        viewPager2.setCurrentItem(1);
                        break;
                    case R.id.setting_item:
                        viewPager2.setCurrentItem(2);
                        break;
                }
                return true;
            }
        });

        surfaceView = view.findViewById(R.id.home_fragment_surface);
        holder = surfaceView.getHolder();
    }

    @Override
    public void onResume() {
        super.onResume();
        createPlayer(url);
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
}