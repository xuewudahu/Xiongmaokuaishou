package com.xiongmaokuaishou.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dou361.dialogui.DialogUIUtils;
import com.xiongmaokuaishou.myapplication.common.AppManager;
import com.xiongmaokuaishou.myapplication.http.Api;
import com.xiongmaokuaishou.myapplication.http.ApiListener;
import com.xiongmaokuaishou.myapplication.http.httpNetApi;
import com.xiongmaokuaishou.myapplication.utils.AdroidUtil;
import com.xiongmaokuaishou.myapplication.utils.SharedPreferences;

import org.json.JSONObject;

import java.util.HashMap;

import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private TextView loginButton;
 private TextView loginProtocol;
 private TextView loginqrText;
 private TextView loginpasswordText;
 private LinearLayout loginpasswordLinearLayout;
 private LinearLayout loginqrLinearLayout;
 private ImageView loginpasswordImageView;
 private ImageView loginqrImageView;
 private ImageView loginprotocolImage;
 private ImageView loginprotocolNoselectImage;
 private ImageView loginAccoutDelectImage;
 private ImageView loginPasswordDelectImage;
 private EditText loginAccountEdit;
 private EditText loginPasswordEdit;
    private SharedPreferences sharedPreferences;
    private Boolean accountFlag = false;
    private Boolean passwordFlag = false;
    private Dialog dialog;
    private httpNetApi httpApi;
    public static String ANDROID_ID;
    private Thread loginthread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AdroidUtil.setStatusBarColor(this, R.color.orange);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        sharedPreferences = SharedPreferences.getInstance();
        Boolean isLogin=sharedPreferences.getBoolean("isLogin",false);
        if (isLogin) {
            Intent intent =new Intent(LoginActivity.this,MainActivity.class);
            startActivity(intent);
            AppManager.getAppManager().finishActivity(this);
        }
        AppManager.getAppManager().addActivity(this);
        DialogUIUtils.init(this);
        initView();
        initListener();
        initThread();
    }
   private void  initView() {
       loginButton=findViewById(R.id.login_go);
       loginButton.setEnabled(false);
       loginProtocol=findViewById(R.id.login_protocol);
       loginqrText=findViewById(R.id.login_qr);
       loginpasswordText=findViewById(R.id.login_password);
       loginpasswordLinearLayout =findViewById(R.id.login_password_linear);
       loginqrLinearLayout =findViewById(R.id.login_qr_linear);
       loginpasswordImageView=findViewById(R.id.login_password_image);
       loginqrImageView=findViewById(R.id.login_qr_image);
       loginprotocolImage=findViewById(R.id.login_protocol_image);
       loginprotocolNoselectImage=findViewById(R.id.login_protocol_noselect_image);
       loginAccountEdit = findViewById(R.id.loginAccount);
       loginAccoutDelectImage = findViewById(R.id.account_delect);
       loginPasswordDelectImage = findViewById(R.id.password_delect);
       SpannableString s = new SpannableString("请输入账号");
       AbsoluteSizeSpan textSize = new AbsoluteSizeSpan(24,true);
       s.setSpan(textSize,0,s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
       loginAccountEdit.setHint(s);
       loginPasswordEdit = findViewById(R.id.loginPassword);
       SpannableString s2 = new SpannableString("请输入密码");
       AbsoluteSizeSpan textSize2 = new AbsoluteSizeSpan(24,true);
       s2.setSpan(textSize2,0,s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
       loginPasswordEdit.setHint(s2);
       httpApi = httpNetApi.getInstance();
       httpApi.Init();
    }
    private void initListener() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loginprotocolImage.getVisibility() == View.INVISIBLE) {
                    DialogUIUtils.showToastCenter("请先勾选协议");
                } else {
                     ANDROID_ID = Settings.System.getString(getContentResolver(), Settings.System.ANDROID_ID);
                    Log.d("qxjuu","---"+ANDROID_ID);

                    httpApi.bmxGetToken(loginAccountEdit.getText().toString(),loginPasswordEdit.getText().toString(),ANDROID_ID,new ApiListener() {
                        @Override
                        public void success(Api api) {
                            Log.d("logcat_qxj","bmxGetToken---success= "+api.jsonObject);
                            sharedPreferences.putString("account",loginAccountEdit.getText().toString());
                            sharedPreferences.putString("password",loginPasswordEdit.getText().toString());
                            try {
                                JSONObject value = api.jsonObject.getJSONObject("d");
                                httpApi.bmxSaveToken(value.optString("token"));
                                sharedPreferences.putString("taken",value.optString("token"));
                                sharedPreferences.putBoolean("isLogin",true);
                                Intent intent =new Intent(LoginActivity.this,MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                new Handler().postDelayed(loginthread,23*60*60*1000);
                                // new Handler().postDelayed(loginthread,20*1000);
                            }catch (Exception e){}
                        }

                        @Override
                        public void failure(Api api) {
                            Log.d("logcat_qxj","bmxGetToken---failure-"+api.jsonObject);
                            try {
                                if (isConnectedToInternet()) {
                                    String value = api.jsonObject.optString("m");
                                   // if (value.equals("接口异常")) {

                                        DialogUIUtils.showToastCenter(value);
                                 //   } else {
                                    //        Log.d("qxj", "bmxGetToken---failure111-");
                                     //       DialogUIUtils.showToastCenter("账号或密码有错误");
                                   // }
                                } else {
                                    Log.d("qxj", "bmxGetToken---failure222-");
                                    DialogUIUtils.showToastCenter("请检查网络连接");
                                }
                            }catch (Exception e){
                                DialogUIUtils.showToastCenter("请检查网络连接");
                            }
                        }

                        @Override
                        public void onData(Response response) {
                            Log.d("qxj", "bmxAPI ---onData-");
                        }
                    });

                   // dialog.dismiss();
                }

            }
        });
        loginProtocol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(LoginActivity.this,ProtocolActivity.class);
                startActivity(intent);
            }
        });
        loginqrText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginqrImageView.setVisibility(View.VISIBLE);
                loginpasswordImageView.setVisibility(View.GONE);
                loginpasswordLinearLayout.setVisibility(View.GONE);
                loginqrLinearLayout.setVisibility(View.VISIBLE);
            }
        });
        loginpasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginqrImageView.setVisibility(View.GONE);
                loginpasswordImageView.setVisibility(View.VISIBLE);
                loginpasswordLinearLayout.setVisibility(View.VISIBLE);
                loginqrLinearLayout.setVisibility(View.GONE);
            }
        });
        loginprotocolNoselectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginprotocolNoselectImage.setVisibility(View.INVISIBLE);
                loginprotocolImage.setVisibility(View.VISIBLE);
            }
        });
        loginprotocolImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginprotocolImage.setVisibility(View.INVISIBLE);
                loginprotocolNoselectImage.setVisibility(View.VISIBLE);
            }
        });

        loginAccountEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                accountFlag = s.length() > 0;
                if (accountFlag && passwordFlag) {
                    loginButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.login_button_orange));
                    loginButton.setEnabled(true);
                } else {
                    loginButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.login_button_grey));
                    loginButton.setEnabled(false);
                }
                if (accountFlag) {
                    loginAccoutDelectImage.setVisibility(View.VISIBLE);
                } else {
                    loginAccoutDelectImage.setVisibility(View.GONE);
                }
            }
        });
        loginAccountEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    if (!loginAccountEdit.getText().toString().isEmpty()) {
                        loginAccoutDelectImage.setVisibility(View.VISIBLE);
                    }
                } else {
                    loginAccoutDelectImage.setVisibility(View.GONE);
                }
            }
        });

        loginPasswordEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
Log.e("wxw1","--"+s);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.e("wxw2","--"+s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.e("wxw3","--"+s);
                passwordFlag = s.length() > 0;
                if (accountFlag && passwordFlag) {
                   loginButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.login_button_orange));
                    loginButton.setEnabled(true);
                } else {
                    loginButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.login_button_grey));
                    loginButton.setEnabled(false);
                }
                if (passwordFlag) {
                    loginPasswordDelectImage.setVisibility(View.VISIBLE);
                } else {
                    loginPasswordDelectImage.setVisibility(View.GONE);
                }
            }
        });
        loginPasswordEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    if (!loginPasswordEdit.getText().toString().isEmpty()) {
                        loginPasswordDelectImage.setVisibility(View.VISIBLE);
                    }
                } else {
                    loginPasswordDelectImage.setVisibility(View.GONE);
                }
            }
        });
        loginAccoutDelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginAccountEdit.setText("");
            }
        });
        loginPasswordDelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginPasswordEdit.setText("");
            }
        });
    }
    private void initThread() {
        loginthread = new Thread(){
            @Override
            public void run() {

                httpApi.bmxGetToken(sharedPreferences.getString("account",""),sharedPreferences.getString("password",""),ANDROID_ID,new ApiListener() {
                    @Override
                    public void success(Api api) {
                        Log.d("logcat_qxj","bmxGetToken---success- token:= "+api.jsonObject);

                        try {
                            JSONObject value = api.jsonObject.getJSONObject("d");
                            httpApi.bmxSaveToken(value.optString("token"));
                            sharedPreferences.putString("taken",value.optString("token"));
                            sharedPreferences.putBoolean("isLogin",true);
                            new Handler().postDelayed(loginthread,23*60*60*1000);
                            //new Handler().postDelayed(loginthread,20*1000);
                        }catch (Exception e){}
                    }

                    @Override
                    public void failure(Api api) {
                        Log.d("logcat_qxj","bmxGetToken---failure-"+api.jsonObject);

                    }

                    @Override
                    public void onData(Response response) {
                        Log.d("qxj", "bmxAPI ---onData-");
                    }
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    }
    private boolean isConnectedToInternet() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isAvailable();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}