package com.example.yamigo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.yamigo.adapter.ViewPager2FragmentAdapter;
import com.example.yamigo.fragment.SettingFragment;
import com.example.yamigo.fragment.HomeFragment;
import com.example.yamigo.fragment.DeviceFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private HomeFragment homeFragment;
    private DeviceFragment deviceFragment;
    private SettingFragment settingFragment;

    private ViewPager2 main_page_viewpage2;
    private BottomNavigationView main_page_bottom_nav;

    private ViewPager2FragmentAdapter adapter;
    private List<Fragment> fragmentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main_page_bottom_nav = findViewById(R.id.main_page_bottom_nav);
        main_page_viewpage2 = findViewById(R.id.main_page_viewpage2);

        // 初始化FragmentList
        fragmentList = initFragmentList();

        // 初始化ViewPager2
        initViewPager2(main_page_viewpage2);

        // 监听滑动事件
        main_page_viewpage2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        main_page_bottom_nav.setSelectedItemId(R.id.home);
                        break;
                    case 1:
                        main_page_bottom_nav.setSelectedItemId(R.id.device);
                        break;
                    case 2:
                        main_page_bottom_nav.setSelectedItemId(R.id.setting);
                        break;

                }
            }
        });

        // 监听底部栏点击事件
        main_page_bottom_nav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        main_page_viewpage2.setCurrentItem(0);
                        break;
                    case R.id.device:
                        main_page_viewpage2.setCurrentItem(1);
                        break;
                    case R.id.setting:
                        main_page_viewpage2.setCurrentItem(2);
                        break;
                }
                return true;
            }
        });
    }

    private List<Fragment> initFragmentList() {
        List<Fragment> fragmentList = new ArrayList<>();
        homeFragment = HomeFragment.newInstance("主页","");
        deviceFragment = DeviceFragment.newInstance("设备", "");
        settingFragment = SettingFragment.newInstance("设置", "");

        fragmentList.add(homeFragment);
        fragmentList.add(deviceFragment);
        fragmentList.add(settingFragment);

        return fragmentList;
    }

    private void initViewPager2(ViewPager2 viewpage2) {
        adapter = new ViewPager2FragmentAdapter(this, fragmentList);
        viewpage2.setAdapter(adapter);
    }
}