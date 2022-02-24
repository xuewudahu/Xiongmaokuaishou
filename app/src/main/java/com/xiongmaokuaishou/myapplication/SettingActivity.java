package com.xiongmaokuaishou.myapplication;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.xiongmaokuaishou.myapplication.utils.SharedPreferences;

@SuppressLint("UseSwitchCompatOrMaterialCode")
public class SettingActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private Switch switchFacecamera;
    private Switch switchScancamera;
    private RelativeLayout settingCode;
    private TextView settingButton;
    private EditText settingEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        RelativeLayout relativeViewBackmain = findViewById(R.id.back_main);
        if (sharedPreferences == null) {
            sharedPreferences = SharedPreferences.getInstance();
        }
        settingCode=findViewById(R.id.settings_code);
        switchFacecamera = findViewById(R.id.face_camera);
        switchScancamera = findViewById(R.id.scan_camera_switch);
        settingButton = findViewById(R.id.setting_button);
        settingEdit = findViewById(R.id.setting_edit);
        relativeViewBackmain.setOnClickListener(new listener());
        settingButton.setOnClickListener(new listener());

        /**
         * 保持屏幕常亮
         */
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);     // for keeping screen on


        switchFacecamera.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                sharedPreferences.putBoolean("face_camera_out", isChecked);
            }
        });


        switchScancamera.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                sharedPreferences.putBoolean("gamma_on", isChecked);
            }
        });
        settingCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_back_main = new Intent(SettingActivity.this, SymbologiesActivity.class);
                startActivity(intent_back_main);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sharedPreferences.getInt("MultipleCode", 1) != 1) {
            settingEdit.setText(String.valueOf(sharedPreferences.getInt("MultipleCode",1)));
        }
    }

    /**
     * 单击事件监听
     */
    private class listener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.back_main:
                    Intent intent_back_main = new Intent(SettingActivity.this, MainActivity.class);
                    intent_back_main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent_back_main);
                    break;
                case R.id.setting_button:
                    if (!settingEdit.getText().toString().isEmpty()) {
                        sharedPreferences.putInt("MultipleCode",Integer.valueOf(settingEdit.getText().toString()));
                        Toast.makeText(SettingActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
                    }

                    break;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        switchFacecamera.setChecked(sharedPreferences.getBoolean("face_camera_out", true));
        switchScancamera.setChecked(sharedPreferences.getBoolean("gamma_on", true));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}

