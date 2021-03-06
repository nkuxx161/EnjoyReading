package com.awslab.bookuitemplate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class LoginActivity extends AppCompatActivity {

    private Button buttonCode,buttonLogin;
    private EditText editTextPhoneNum,editTextCode;
    private String phoneNum,code;
    private EventHandler eh;
    private TimeCount mTimeCount;//计时器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_login);
        buttonCode = findViewById(R.id.buttonCode);
        buttonLogin = findViewById(R.id.buttonLogin);
        editTextCode = findViewById(R.id.editTextCode);
        editTextPhoneNum = findViewById(R.id.editTextPhoneNum);
        mTimeCount = new TimeCount(60000, 1000);

        eh = new EventHandler() {
            @Override
            public void afterEvent(int event, int result, Object data) {
                if (result == SMSSDK.RESULT_COMPLETE){
                    //回调完成
                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        //提交验证码成功
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this,"登录成功",Toast.LENGTH_SHORT).show();
                                //如果需要跳转可在这里进行跳转页面
                                //登录成功跳转到软件首页页面
                                //用sharedPreferences保存登录状态
                                SharedPreferences preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("userPhone", editTextPhoneNum.getText().toString());//username为用户登录的用户名
                                editor.commit();

                                Intent intent=new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        });
                    }else if (event == SMSSDK.EVENT_GET_VOICE_VERIFICATION_CODE){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this,"语音验证发送",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE){
                        //获取验证码成功
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this,"验证码已发送",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES){
                        Log.i("登录log1","test");
                    }
                }else{
                    ((Throwable)data).printStackTrace();
                    Throwable throwable = (Throwable) data;
                    throwable.printStackTrace();
                    Log.i("登录log2",throwable.toString());
                    try {
                        JSONObject obj = new JSONObject(throwable.getMessage());
                        final String des = obj.optString("detail");
                        if (!TextUtils.isEmpty(des)){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LoginActivity.this,des,Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        //注册一个事件回调监听，用于处理SMSSDK接口请求的结果
        SMSSDK.registerEventHandler(eh);
        buttonCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTimeCount.start();
                phoneNum = editTextPhoneNum.getText().toString();
                if(!phoneNum.isEmpty()){
                    if(Utils.checkTel(phoneNum)){ //利用正则表达式获取检验手机号
                        // 获取验证码
                        SMSSDK.getVerificationCode("86", phoneNum);
                    }else{
                        Toast.makeText(getApplicationContext(),"请输入有效的手机号",Toast.LENGTH_LONG).show();
                    }
                }else {
                    Toast.makeText(getApplicationContext(),"请输入手机号",Toast.LENGTH_LONG).show();
                    return;
                }
                phoneNum = editTextPhoneNum.getText().toString();
            }
        });
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code = editTextCode.getText().toString();
                if(!code.isEmpty()){
                    //提交验证码
                    SMSSDK.submitVerificationCode("86", phoneNum, code);
                }else{
                    Toast.makeText(getApplicationContext(),"请输入验证码",Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });
    }
    // 使用完EventHandler需注销，否则可能出现内存泄漏
    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterEventHandler(eh);
    }


    class TimeCount extends CountDownTimer {

        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
            buttonCode.setClickable(false);
            buttonCode.setText(l/1000 + "秒后重新获取");
        }

        @Override
        public void onFinish() {
            buttonCode.setClickable(true);
            buttonCode.setText("获取验证码");
        }
    }

}

