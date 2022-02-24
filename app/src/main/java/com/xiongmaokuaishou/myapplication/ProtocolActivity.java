package com.xiongmaokuaishou.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.xiongmaokuaishou.myapplication.utils.AdroidUtil;

public class ProtocolActivity extends AppCompatActivity {
   private LinearLayout quitLinear;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protocol);
        AdroidUtil.setStatusBarMode(this,true, R.color.white);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        quitLinear =findViewById(R.id.protocol_quit);
        quitLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent =new Intent(ProtocolActivity.this,LoginActivity.class);
//                startActivity(intent);
                finish();
            }
        });
    }
}