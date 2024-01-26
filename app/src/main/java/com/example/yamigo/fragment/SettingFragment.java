package com.example.yamigo.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.yamigo.LoginActivity;
import com.example.yamigo.MainActivity;
import com.example.yamigo.R;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingFragment extends Fragment {
    private static final String TAG = "SettingFragment";

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private Context context = getContext();
    private MainActivity mainActivity;

    private ViewPager2 viewPager2;

    private MaterialToolbar toolbar;

    private ImageButton photo;
    private TextView username;
    private TextView userphone;

    private ImageButton account_and_safe_btn;
    private ImageButton privacy_btn;
    private ImageButton callback_btn;
    private ImageButton about_btn;

    private AppCompatButton login_another_btn;
    private AppCompatButton logout_btn;

    private ExecutorService service;
//    private CountDownLatch countDownLatch;

    private String mParam1;
    private String mParam2;

    // 逻辑处理器数量
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));

    public SettingFragment() {
        // Required empty public constructor
    }

    public static SettingFragment newInstance(String param1, String param2) {
        SettingFragment fragment = new SettingFragment();
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
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivity = (MainActivity) getActivity();

        service = Executors.newFixedThreadPool(CORE_POOL_SIZE);

        service.submit(new Runnable() {
            @Override
            public void run() {
                init_view(view);
            }
        });
    }

    private void init_view(View view) {
        viewPager2 = mainActivity.findViewById(R.id.main_page_viewpage2);

        toolbar = view.findViewById(R.id.setting_fragment_toolbar);
        init_toolbar();

        photo = view.findViewById(R.id.setting_fragment_photo);
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO：上传照片
            }
        });

        username = view.findViewById(R.id.setting_user_name);
        userphone = view.findViewById(R.id.setting_user_phone);

        account_and_safe_btn = view.findViewById(R.id.account_and_safe_btn);
        account_and_safe_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });
        privacy_btn = view.findViewById(R.id.privacy_btn);
        privacy_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });
        callback_btn = view.findViewById(R.id.callback_btn);
        callback_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });
        about_btn = view.findViewById(R.id.about_btn);
        about_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });

        login_another_btn = view.findViewById(R.id.login_another_btn);
        login_another_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        logout_btn = view.findViewById(R.id.logout_btn);
        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
    }

    private void init_toolbar() {
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.setting_fragment_home_item:
                        viewPager2.setCurrentItem(0);
                        break;
                    case R.id.setting_fragment_device_item:
                        viewPager2.setCurrentItem(1);
                        break;
                }
                return true;
            }
        });
    }
}