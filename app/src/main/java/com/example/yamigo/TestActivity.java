package com.example.yamigo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.yamigo.websocketclient.TestWebSocketClient;

import org.java_websocket.client.WebSocketClient;

import java.net.URI;

public class TestActivity extends AppCompatActivity implements View.OnClickListener {
    // 权限申请码
    private static final int INTERNET_REQUESTCODE = 1;

    private TestWebSocketClient client;

    // 动态申请权限
    private void checkPermission() throws InterruptedException {
        int checkInternetPermission = ContextCompat.checkSelfPermission(TestActivity.this, Manifest.permission.INTERNET);
        if (checkInternetPermission == PackageManager.PERMISSION_GRANTED) {
            // 有网络权限后执行的功能
            test();
        } else {
            // 无网络权限则申请权限
            ActivityCompat.requestPermissions(TestActivity.this, new String[]{Manifest.permission.INTERNET}, INTERNET_REQUESTCODE);
        }
    }

    // 动态权限申请回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case INTERNET_REQUESTCODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        test();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(TestActivity.this, "网络权限申请成功", Toast.LENGTH_SHORT).show();
                } else {
                    // 拒绝授权
                    Toast.makeText(TestActivity.this, "拒绝网络权限申请", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        Button test_btn = findViewById(R.id.test);
        Button send_btn = findViewById(R.id.nihao);
        test_btn.setOnClickListener(this);
        send_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.test:
                try {
                    checkPermission();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.nihao:
                sendInfo(client);
                break;
            case R.id.close:
                closeConnect(client);
                break;
        }
    }

    private void sendInfo(TestWebSocketClient client) {
        client.send("你好");
    }

    private void test() throws InterruptedException {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                SharedPreferences sp = getSharedPreferences("token", Context.MODE_PRIVATE);
//                String token = sp.getString("token", "");
//
//                // 创建http客户端
//                OkHttpClient client = new OkHttpClient();
//                // 创建http请求
//                Request request = new Request.Builder()
//                        .url("http://113.54.232.23:12098/ping")
//                        .header("Authorization", "Bearer " + token)
//                        .build();
//                try {
//                    Response response = client.newCall(request).execute();
//                    String data = response.body().string();
//                    JSONObject object = new JSONObject(data);
//                    Log.d("Test", "" + object.getInt("code"));
//                } catch (IOException | JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                URI uri = URI.create("ws://113.54.232.23:12098/ws/sensor");
                client = new TestWebSocketClient(uri) {
                    @Override
                    public void onMessage(String message) {
                        Log.d("Test", message);
                    }
                };

                try {
                    client.connectBlocking();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
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
}