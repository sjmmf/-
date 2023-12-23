package com.example.yamigo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    // 权限申请码
    private static final int INTERNET_REQUESTCODE = 1;

    private ImageButton return_btn;
    private EditText username;
    private EditText phone;
    private EditText verification;
    private Button ver_btn;
    private EditText pwd;
    private ImageButton next_btn;

    // 动态申请权限
    private void checkPermission() {
        int checkInternetPermission = ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.INTERNET);
        if (checkInternetPermission == PackageManager.PERMISSION_GRANTED) {
            // 有网络权限后执行的功能
            register();
        } else {
            // 无网络权限则申请权限
            ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest.permission.INTERNET}, INTERNET_REQUESTCODE);
        }
    }

    // 动态权限申请回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case INTERNET_REQUESTCODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    register();
                    Toast.makeText(RegisterActivity.this, "网络权限申请成功", Toast.LENGTH_SHORT).show();
                } else {
                    // 拒绝授权
                    Toast.makeText(RegisterActivity.this, "拒绝网络权限申请", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        return_btn = findViewById(R.id.register_return_btn);
        username = findViewById(R.id.register_username_edit_text);
        phone = findViewById(R.id.register_account_edit_text);
        verification = findViewById(R.id.register_verification_code_edit_text);
        ver_btn = findViewById(R.id.register_get_verification_code_btn);
        pwd = findViewById(R.id.register_pwd_edit_text);
        next_btn = findViewById(R.id.register_next_page_btn);

        return_btn.setOnClickListener(this);
        ver_btn.setOnClickListener(this);
        next_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.register_return_btn:
                Intent intent_1 = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent_1);
                finish();
                break;
            case R.id.register_get_verification_code_btn:
                // TODO：获取验证码
                break;
            case R.id.register_next_page_btn:
                checkPermission();
                break;
        }
    }

    private void register() {
        new Thread(() -> {
            try {
                // 获取输入框内容
                String username_str = username.getText().toString();
                String phone_str = phone.getText().toString();
                String verify_code = verification.getText().toString();
                String pwd_str = pwd.getText().toString();

                if (!username_str.isEmpty() && !phone_str.isEmpty() && !verify_code.isEmpty() && !pwd_str.isEmpty()) {
                    // 构造params
                    FormBody.Builder params = new FormBody.Builder();
                    params.add("username", username_str);
                    params.add("password", pwd_str);
                    params.add("phone", phone_str);
                    params.add("veri_code", verify_code);

                    // 创建http客户端
                    OkHttpClient client = new OkHttpClient();
                    // 创建http请求
                    Request request = new Request.Builder()
                            .url("http://113.54.232.23:12098/user/register")
                            .post(params.build())
                            .build();
                    Response response = client.newCall(request).execute();

                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this, "发送成功", Toast.LENGTH_LONG).show();
                    });

                    if (checkRegister(response)) {
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this, "输入不能为空", Toast.LENGTH_LONG).show();
                    });
                }
            } catch (Exception e) {
                // post请求失败提示
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(RegisterActivity.this, "网络连接失败", Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private boolean checkRegister(Response response) throws IOException, JSONException {
        if (response.isSuccessful()){
            String responseData = response.body().string();
            Log.d("Register_Response", responseData);
            JSONObject jsonObject = new JSONObject(responseData);
            int code = jsonObject.getInt("code");

            if (code == 200){
                runOnUiThread(() -> {
                    Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_LONG).show();
                });

                return true;
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(RegisterActivity.this, "注册失败", Toast.LENGTH_LONG).show();
                });

                return false;
            }
        } else {
            runOnUiThread(() -> {
                Toast.makeText(RegisterActivity.this, "注册失败", Toast.LENGTH_LONG).show();
            });

            return false;
        }
    }
}