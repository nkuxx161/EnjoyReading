package com.awslab.bookuitemplate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.net.URL;


public class AdminManageActivity extends AppCompatActivity {
    TextView userName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_manage);
        userName = findViewById(R.id.admin_text);
        SharedPreferences preferences=getSharedPreferences("user", Context.MODE_PRIVATE);
        String userPhone = preferences.getString("userPhone",null);
        userName.setText("欢迎您！" + userPhone);
    }

    public void exitLogin(View view) {
        SharedPreferences preferences=getSharedPreferences("user", Context.MODE_PRIVATE);
        preferences.edit().clear().commit();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void connectGithub(View view) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("https://github.com/nkuxx161/EnjoyReading"));//Url 就是你要打开的网址
        intent.setAction(Intent.ACTION_VIEW);
        this.startActivity(intent); //启动浏览器
    }
}
