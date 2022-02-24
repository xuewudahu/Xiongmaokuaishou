package com.xiongmaokuaishou.myapplication;

import android.content.Intent;
import com.xiongmaokuaishou.myapplication.utils.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.dou361.dialogui.DialogUIUtils;
import com.xiongmaokuaishou.myapplication.common.AppManager;


public class WelcomeActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        DialogUIUtils.init(this);

        init();
    }

    private void init() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sharedPreferences = SharedPreferences.getInstance();
                        Boolean isLogin=sharedPreferences.getBoolean("isLogin",false);
                        Intent intent=null;
                        if (isLogin) {
                            intent = new Intent(WelcomeActivity.this, MainActivity.class);

                        } else {
                            intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                        }
                        startActivity(intent);
                        AppManager.getAppManager().finishActivity(WelcomeActivity.this);
                    }
                });
            }
        }).start();
    }

}

