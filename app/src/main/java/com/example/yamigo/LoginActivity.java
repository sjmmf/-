package com.example.yamigo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    // 权限申请码
    private static final int INTERNET_REQUESTCODE = 1;

    private EditText account;
    private EditText pwd;
    private android.widget.Button register_btn;
    private android.widget.Button forget_pwd_btn;
    private android.widget.Button login_btn;

    // 动态申请权限
    private void checkPermission() {
        int checkInternetPermission = ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.INTERNET);
        if (checkInternetPermission == PackageManager.PERMISSION_GRANTED) {
            // 有网络权限后执行的功能
            login();
        } else {
            // 无网络权限则申请权限
            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.INTERNET}, INTERNET_REQUESTCODE);
        }
    }

    // 动态权限申请回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case INTERNET_REQUESTCODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    login();
                    Toast.makeText(LoginActivity.this, "网络权限申请成功", Toast.LENGTH_SHORT).show();
                } else {
                    // 拒绝授权
                    Toast.makeText(LoginActivity.this, "拒绝网络权限申请", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 获取登录界面的组件
        init_view();

        // 按钮监听器
        register_btn.setOnClickListener(this);
        forget_pwd_btn.setOnClickListener(this);
        login_btn.setOnClickListener(this);
    }

    private void init_view() {
        account = findViewById(R.id.login_account_edit_text);
        pwd = findViewById(R.id.login_pwd_edit_text);
        register_btn = findViewById(R.id.register_reminder);
        forget_pwd_btn = findViewById(R.id.forget_pwd_reminder);
        login_btn = findViewById(R.id.login_btn);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login_btn:
                checkPermission();
                finish();
                break;
            case R.id.forget_pwd_reminder:
                // TODO：忘记密码
                break;
            case R.id.register_reminder:
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void login(){
        // post -> account + pwd
        new Thread(() -> {
            try {
                // 获取输入框内容
                String account_str = account.getText().toString();
                String pwd_str = pwd.getText().toString();

                // 判断输入框内容是否为空
                if (!account_str.isEmpty() && !pwd_str.isEmpty()) {
//                    Log.d("Login_Edit", account_str + pwd_str);

                    // 构造params
                    FormBody.Builder params = new FormBody.Builder();
                    params.add("phone", account_str);
                    params.add("password", pwd_str);

                    // 创建http客户端
                    OkHttpClient client = new OkHttpClient();
                    // 创建http请求
                    Request request = new Request.Builder()
                            .url("http://113.54.231.165:12098/user/login")
                            .post(params.build())
                            .build();
                    Response response = client.newCall(request).execute();

                    if (checkLogin(response)) {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, "手机号或密码输入为空", Toast.LENGTH_LONG).show();
                    });
                }
            } catch (Exception e) {
                // post请求失败提示
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "网络连接失败", Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private boolean checkLogin(Response response) throws IOException, JSONException {
        if (response.isSuccessful()) {
            String responseData = response.body().string();
            Log.d("Login_Response", responseData);
            JSONObject jsonObject = new JSONObject(responseData);
            int code = jsonObject.getInt("code");

            if (code == 200) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_LONG).show();
                });

                String data = jsonObject.getString("data");
                JSONObject token_jsonObject = new JSONObject(data);
                String token = token_jsonObject.getString("token");
                Log.d("Login_Token", token);

                saveToken(token);

                return true;
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_LONG).show();
                });

                return false;
            }
        } else {
            runOnUiThread(() -> {
                Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_LONG).show();
            });

            return false;
        }
    }

    private void saveToken(String token) {
        SharedPreferences sp = getSharedPreferences("token", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("token", token);
        editor.commit();
    }
}