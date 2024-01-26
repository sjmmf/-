package com.example.yamigo.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Outline;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.TextView;

import com.example.yamigo.InfoCenterActivity;
import com.example.yamigo.MainActivity;
import com.example.yamigo.R;
import com.example.yamigo.adapter.DeviceFragmentRecyclerViewAdapter;
import com.example.yamigo.bean.DeviceBlockData;
import com.example.yamigo.callback.DeviceDragItemTouchHelperCallback;
import com.example.yamigo.decoration.BottomOffsetDecoration;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeviceFragment extends Fragment {
    private static final String TAG = "DeviceFragment";

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private Context context = getContext();
    private MainActivity mainActivity;

    private ViewPager2 viewPager2;

    private MaterialToolbar toolbar;

    private RecyclerView recyclerView;

    private LinearLayoutManager layoutManager;

    private DeviceFragmentRecyclerViewAdapter adapter;
    private ItemTouchHelper.Callback callback;
    private ItemTouchHelper touchHelper;

    private ArrayList<Entry> temp_line_values;
    private ArrayList<BarEntry> temp_bar_values;
    private ArrayList<Entry> humi_line_values;
    private ArrayList<BarEntry> humi_bar_values;
    private ArrayList<Entry> smok_line_values;
    private ArrayList<BarEntry> smok_bar_values;

    private DeviceBlockData tempdata;
    private DeviceBlockData humidata;
    private DeviceBlockData smokdata;
    private ArrayList<DeviceBlockData> deviceBlockData;

    private ExecutorService service;
//    private CountDownLatch countDownLatch;

    private boolean temp_status;
    private boolean humi_status;
    private boolean smok_status;

    // 逻辑处理器数量
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));


    public static DeviceFragment newInstance(String param1, String param2) {
        DeviceFragment fragment = new DeviceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public DeviceFragment() {
        // Required empty public constructor
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
        return inflater.inflate(R.layout.fragment_device, container, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        delete_fragment_listener();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 监听HomeFragment发来的设备状态信息
        listen_HomeFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainActivity = (MainActivity) getActivity();

//        // 线程池
//        service = Executors.newFixedThreadPool(CORE_POOL_SIZE);
//        countDownLatch = new CountDownLatch(1);

        // 获得折线图的坐标点数据
        get_line_chart_values();

        // 获得柱状图的数据
        get_bar_chart_values();

        // 获得数据集合
        get_data_list();

        // 初始化组件
        init_view(view);
    }

    private void get_data_list() {
        tempdata = new DeviceBlockData("temp", temp_line_values, temp_bar_values);
        humidata = new DeviceBlockData("humi", humi_line_values, humi_bar_values);
        smokdata = new DeviceBlockData("smok", smok_line_values, smok_bar_values);

        deviceBlockData = new ArrayList<>();
        deviceBlockData.add(tempdata);
        deviceBlockData.add(humidata);
        deviceBlockData.add(smokdata);
    }

    private void listen_HomeFragment() {
        getParentFragmentManager().setFragmentResultListener("DeviceStatus", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                Log.d(TAG, "收到消息");
                temp_status = result.getBoolean("temp_status");
                humi_status = result.getBoolean("humi_status");
                smok_status = result.getBoolean("smok_status");
            }
        });
    }

    private void delete_fragment_listener() {
        getParentFragmentManager().clearFragmentResultListener("DeviceStatus");
    }

    private void init_view(View view) {
        viewPager2 = mainActivity.findViewById(R.id.main_page_viewpage2);

        toolbar = view.findViewById(R.id.device_fragment_toolbar);
        init_toolbar();

        recyclerView = view.findViewById(R.id.device_recycler_view);
        init_recycler_view();
    }

    private void init_toolbar() {
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.device_fragment_home_item:
                        viewPager2.setCurrentItem(0);
                        break;
                    case R.id.device_fragment_setting_item:
                        viewPager2.setCurrentItem(2);
                        break;
                    case R.id.device_fragment_info_center:
                        Intent intent = new Intent(getActivity(), InfoCenterActivity.class);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });
    }

    private void init_recycler_view() {
        adapter = new DeviceFragmentRecyclerViewAdapter(getContext(), deviceBlockData);
        // 设置布局方式
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        // 设置装饰器
        BottomOffsetDecoration bottomOffsetDecoration = new BottomOffsetDecoration((int)100);
        recyclerView.addItemDecoration(bottomOffsetDecoration);
        // 设置adapter
        recyclerView.setAdapter(adapter);
        // 创建Callback
        callback = new DeviceDragItemTouchHelperCallback(adapter);
        // 用Callback构造ItemTouchHelper
        touchHelper = new ItemTouchHelper(callback);
        // 调用ItemTouchHelper的attachToRecyclerView建立联系
        touchHelper.attachToRecyclerView(recyclerView);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int firstItemPosition = layoutManager.findFirstVisibleItemPosition();
                int lastItemPosition = layoutManager.findLastVisibleItemPosition();
                Log.d(TAG, "first:" + firstItemPosition + " last:" + lastItemPosition);
                if (lastItemPosition == 2) {
                    String last_device_name = adapter.getContentList().get(lastItemPosition).getName();
                    View last_itemView = recyclerView.getChildAt(lastItemPosition);
                    if (last_itemView != null) {
                        switch (last_device_name) {
                            case "temp":
                                if (temp_status) {
                                    last_itemView.findViewById(R.id.device_status).setBackground(mainActivity.getResources().getDrawable(R.drawable.device_status_circle_green));
                                } else {
                                    last_itemView.findViewById(R.id.device_status).setBackground(mainActivity.getResources().getDrawable(R.drawable.device_status_circle_red));
                                }
                                break;
                            case "humi":
                                if (humi_status) {
                                    last_itemView.findViewById(R.id.device_status).setBackground(mainActivity.getResources().getDrawable(R.drawable.device_status_circle_green));
                                } else {
                                    last_itemView.findViewById(R.id.device_status).setBackground(mainActivity.getResources().getDrawable(R.drawable.device_status_circle_red));
                                }
                                break;
                            case "smok":
                                if (smok_status) {
                                    last_itemView.findViewById(R.id.device_status).setBackground(mainActivity.getResources().getDrawable(R.drawable.device_status_circle_green));
                                } else {
                                    last_itemView.findViewById(R.id.device_status).setBackground(mainActivity.getResources().getDrawable(R.drawable.device_status_circle_red));
                                }
                                break;
                        }
                    }
                }
                for (int i = 0; i < lastItemPosition - firstItemPosition; i++) {
                    String device_name = adapter.getContentList().get(i).getName();
                    View itemView = recyclerView.getChildAt(i);
                    if (itemView != null) {
                        Log.d(TAG, ((TextView)itemView.findViewById(R.id.device_line_chart_title)).getText().toString() + i);
                        switch (device_name) {
                            case "temp":
                                if (temp_status) {
                                    itemView.findViewById(R.id.device_status).setBackground(mainActivity.getResources().getDrawable(R.drawable.device_status_circle_green));
                                } else {
                                    itemView.findViewById(R.id.device_status).setBackground(mainActivity.getResources().getDrawable(R.drawable.device_status_circle_red));
                                }
                                break;
                            case "humi":
                                if (humi_status) {
                                    itemView.findViewById(R.id.device_status).setBackground(mainActivity.getResources().getDrawable(R.drawable.device_status_circle_green));
                                } else {
                                    itemView.findViewById(R.id.device_status).setBackground(mainActivity.getResources().getDrawable(R.drawable.device_status_circle_red));
                                }
                                break;
                            case "smok":
                                if (smok_status) {
                                    itemView.findViewById(R.id.device_status).setBackground(mainActivity.getResources().getDrawable(R.drawable.device_status_circle_green));
                                } else {
                                    itemView.findViewById(R.id.device_status).setBackground(mainActivity.getResources().getDrawable(R.drawable.device_status_circle_red));
                                }
                                break;
                        }
                    }
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    private void get_line_chart_values() {
        temp_line_values = new ArrayList<>();
        humi_line_values = new ArrayList<>();
        smok_line_values = new ArrayList<>();

        // 测试使用随机数添加数据
        Random random = new Random();
        for (int i = 0; i < 24; i++) {
            temp_line_values.add(new Entry(i % 24, random.nextInt(40 - 10 + 1) + 10));
            humi_line_values.add(new Entry(i % 24, random.nextInt(40 - 10 + 1) + 10));
            smok_line_values.add(new Entry(i % 24, random.nextInt(40 - 10 + 1) + 10));
        }
    }

    private void get_bar_chart_values() {
        temp_bar_values = new ArrayList<>();
        humi_bar_values = new ArrayList<>();
        smok_bar_values = new ArrayList<>();

        // 测试使用随机数添加数据
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            temp_bar_values.add(new BarEntry(i, random.nextInt(100)));
            humi_bar_values.add(new BarEntry(i, random.nextInt(100)));
            smok_bar_values.add(new BarEntry(i, random.nextInt(100)));
        }
    }
}