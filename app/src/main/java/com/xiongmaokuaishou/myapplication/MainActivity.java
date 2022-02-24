package com.xiongmaokuaishou.myapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.donkingliang.banner.CustomBanner;

import java.util.ArrayList;

import com.bumptech.glide.Glide;
import com.dou361.dialogui.DialogUIUtils;
import com.rq.misc.MiscUtil;
import com.xiongmaokuaishou.myapplication.common.AppManager;
import com.xiongmaokuaishou.myapplication.http.Api;
import com.xiongmaokuaishou.myapplication.http.ApiListener;
import com.xiongmaokuaishou.myapplication.http.httpNetApi;
import com.xiongmaokuaishou.myapplication.utils.AdroidUtil;
import com.xiongmaokuaishou.myapplication.utils.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    public ArrayList<String> images;
    public ArrayList<String> urls;
    private RelativeLayout scanLinear;
    private RelativeLayout queryLinear;
    private TextView quitTextView;
    private TextView stationNameTextview;
   private ImageView settingsImage;
    private CustomBanner<String> mBanner;
    public static String stationName;
    private httpNetApi httpApi;
    private SharedPreferences sharedPreferences;
    private final int STATION_NAME = 0x21;
    private final int DEVICE_BANNER = 0x22;
    private long lastonclickTime = 0;
    private long lastonclickTime1 = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DialogUIUtils.init(this);
        AdroidUtil.setStatusBarMode(this, true, R.color.white);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        sharedPreferences =SharedPreferences.getInstance();
        initView();
        initListener();
    }

    private void initView() {
        scanLinear = findViewById(R.id.main_scan);
        queryLinear = findViewById(R.id.main_query);
        quitTextView = findViewById(R.id.main_quit);
        settingsImage= findViewById(R.id.main_settings);
        stationNameTextview = findViewById(R.id.main_stationName);
        mBanner = (CustomBanner) findViewById(R.id.banner);
        images = new ArrayList<>();
        urls = new ArrayList<>();
        httpApi = httpNetApi.getInstance();
        httpApi.Init();
        httpApi.bmxSaveToken(sharedPreferences.getString("taken",""));

        httpApi.bmxGetStationInfo(new ApiListener() {
            @Override
            public void success(Api api) {
                Log.d("logcat_qxj", "--bmxGetStationInfo-success"+api.jsonObject);

                try {
                    JSONObject value = api.jsonObject.getJSONObject("d");
                    stationName = value.optString("stationName");
                    stationNameTextview.setText(stationName);
                    Log.d("wwwwwqxj", "---" + (Thread.currentThread() == Looper.getMainLooper().getThread()));
                } catch (Exception e) {
                }
            }

            @Override
            public void failure(Api api) {
                Log.d("wwwwwqxj", "bmxGetStationInfo---failure-" + api.jsonObject);
            }

            @Override
            public void onData(Response response) {

            }
        });

        httpApi.bmxGetBanner(new ApiListener() {
            @Override
            public void success(Api api) {
                Log.d("logcat_qxj", "--bmxGetBanner-success"+api.jsonObject);
                try {

                    JSONObject value = api.jsonObject.getJSONObject("d");
                    JSONArray jsonArray = value.getJSONArray("data");
                    Log.d("qxj", "1---2-" + jsonArray.length());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String bannerPicUrl = jsonObject.optString("bannerPicUrl", null);
                        String url = jsonObject.optString("url", null);
                        urls.add(url);
                        images.add(bannerPicUrl);
                        Log.d("qxj", "---" + bannerPicUrl + "--" + url);
                    }
                    Message msg = new Message();
                    msg.what = DEVICE_BANNER;  //消息(一个整型值)
                    msg.obj = urls;
                    handler.sendMessage(msg);// 每隔100毫秒发送一个msg给mHandler

                } catch (Exception e) {
                }
            }

            @Override
            public void failure(Api api) {
                Log.d("logcat_qxj", "bmxGetBanner---failure-" + api.jsonObject);
            }

            @Override
            public void onData(Response response) {

            }
        });

    }

    private void initListener() {
        scanLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long time = SystemClock.uptimeMillis();
                if (time - lastonclickTime >= 2000) {
                    Log.d("time___","----456\n-----0-");
                    Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                    startActivity(intent);
                    lastonclickTime = time;
                }
            }
        });
        queryLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long time = SystemClock.uptimeMillis();
                if (time - lastonclickTime1 >= 1000) {
                    Intent intent = new Intent(MainActivity.this, QueryActivity.class);
                    startActivity(intent);
                    lastonclickTime1 = time;
                }
            }
        });
        quitTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_quit, null);
                TextView sure_text;    //确定按钮
                TextView cancal_text;    //取消按钮
                sure_text = (TextView) view.findViewById(R.id.dialog_sure);
                cancal_text = (TextView) view.findViewById(R.id.dialog_cancal);
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setContentView(view);
                dialog.show();
                sure_text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        httpApi.bmxLogout(new ApiListener() {
                            @Override
                            public void success(Api api) {
                                SharedPreferences sharedPreferences = SharedPreferences.getInstance();
                                sharedPreferences.putBoolean("isLogin",false);
                                Log.d("qxj", "bmxLogout---success-" + api.jsonObject);
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }

                            @Override
                            public void failure(Api api) {
                                Log.d("qxj", "bmxLogout---failure-" + api.jsonObject);
                                if (!AdroidUtil.isConnectedToInternet(MainActivity.this)) {
                                    DialogUIUtils.showToastCenter("网络不可用，无法退出");
                                }
                            }

                            @Override
                            public void onData(Response response) {

                            }
                        });

                    }
                });
                cancal_text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });
        settingsImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_settings_main = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent_settings_main);
            }
        });
    }

    private void setBean(final ArrayList<String> beans) {
        mBanner.setOnPageClickListener(new CustomBanner.OnPageClickListener() {
            @Override
            public void onPageClick(int i, Object o) {

                Uri uri = Uri.parse(urls.get(mBanner.getCurrentItem()));
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setData(uri);
                startActivity(intent);

            }
        });
        mBanner.setPages(new CustomBanner.ViewCreator<String>() {
            @Override
            public View createView(Context context, int position) {
                ImageView imageView = new ImageView(context);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                return imageView;
            }

            @Override
            public void updateUI(Context context, View view, int position, String entity) {
                Glide.with(context).load(entity).into((ImageView) view);
            }
        }, beans)
                //设置指示器为普通指示器
                .setIndicatorStyle(CustomBanner.IndicatorStyle.ORDINARY)
                //设置两个点图片作为翻页指示器，不设置则没有指示器，可以根据自己需求自行配合自己的指示器,不需要圆点指示器可用不设
                .setIndicatorRes(R.drawable.shape_point_select, R.drawable.shape_point_unselect)
                //设置指示器的方向
                .setIndicatorGravity(CustomBanner.IndicatorGravity.CENTER)
                //设置指示器的指示点间隔
                .setIndicatorInterval(10)
                //设置自动翻页
                .startTurning(5000);
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @SuppressLint({"ResourceAsColor", "SetTextI18n", "HandlerLeak"})
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STATION_NAME:
                    break;
                case DEVICE_BANNER:
                    ArrayList<String> result = (ArrayList<String>) msg.obj;
                    setBean(images);
                    break;
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                httpApi.bmxCheckToken(new ApiListener() {
                    @Override
                    public void success(Api api) {
                        Log.d("logcat_qxj", "bmxCheckToken---success-" + api.jsonObject);
                    }

                    @Override
                    public void failure(Api api) {
                        Log.d("logcat_qxj", "bmxCheckToken---failure-" + api.jsonObject);
                        httpApi.bmxGetToken(sharedPreferences.getString("account",""),sharedPreferences.getString("password",""),LoginActivity.ANDROID_ID,new ApiListener() {
                            @Override
                            public void success(Api api) {
                                Log.d("logcat_qxj----------","bmxGetToken---success- token:= "+api.jsonObject);

                                try {
                                    JSONObject value = api.jsonObject.getJSONObject("d");
                                    httpApi.bmxSaveToken(value.optString("token"));
                                    sharedPreferences.putString("taken",value.optString("token"));
                                    sharedPreferences.putBoolean("isLogin",true);
                                }catch (Exception e){}
                            }

                            @Override
                            public void failure(Api api) {
                                Log.d("qxj","bmxGetToken---failure-"+api.jsonObject);

                            }

                            @Override
                            public void onData(Response response) {
                                Log.d("qxj", "bmxAPI ---onData-");
                            }
                        });
                    }

                    @Override
                    public void onData(Response response) {

                    }
                });
            }
        },1000);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!AdroidUtil.isConnectedToInternet(MainActivity.this)) {
            DialogUIUtils.showToastCenter("请检查网络连接");
        }
    }
}