package com.xiongmaokuaishou.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dou361.dialogui.DialogUIUtils;
import com.xiongmaokuaishou.myapplication.http.Api;
import com.xiongmaokuaishou.myapplication.http.ApiListener;
import com.xiongmaokuaishou.myapplication.http.httpNetApi;
import com.xiongmaokuaishou.myapplication.model.Express;
import com.xiongmaokuaishou.myapplication.model.ExpressAdapter;
import com.xiongmaokuaishou.myapplication.utils.AdroidUtil;
import com.xiongmaokuaishou.myapplication.utils.MulitPointTouchListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.Response;

public class QueryActivity extends AppCompatActivity {
    private LinearLayout quitLinear;
    private List<Express> ExpressList;
    private ListView listView;
    private EditText quitphonoEdit;
    private TextView queryButton;
    private ExpressAdapter adapter;
    private LinearLayout noExpress;
    private LinearLayout expressListview;
    private TextView stationNameTextview;
    private TextView queryResult;
    private httpNetApi httpApi;
    public static RelativeLayout queryCardView;
    public static ImageView queryimageButton;
    public static ImageView imageClose;
    public static final int UPDATE_DATE = 0x10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);
        AdroidUtil.setStatusBarMode(this, true, R.color.white);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        initView();
        initListener();
    }

    private void initView() {
        quitLinear = findViewById(R.id.query_quit);
        listView = findViewById(R.id.list_view);
        quitphonoEdit = findViewById(R.id.query_edit_phone);
        queryButton = findViewById(R.id.query_express_button);
        noExpress = findViewById(R.id.no_express);
        expressListview = findViewById(R.id.express_listview);
        stationNameTextview = findViewById(R.id.query_stationName);
        queryimageButton = findViewById(R.id.query_imageView);
        queryCardView = findViewById(R.id.query_cardView);
        imageClose = findViewById(R.id.query_close);
        queryResult = findViewById(R.id.query_result);
        ExpressList = new ArrayList<>();

        adapter = new ExpressAdapter(QueryActivity.this, R.layout.quit_listview_item, ExpressList);
        listView.setAdapter(adapter);
        httpApi = httpNetApi.getInstance();
        httpApi.Init();
        stationNameTextview.setText(MainActivity.stationName);

    }

    private void initListener() {
        quitLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(QueryActivity.this, MainActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(intent);
                finish();
            }
        });
        quitphonoEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        queryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s = quitphonoEdit.getText().toString();
                if (s == null || s.isEmpty()) {
                    expressListview.setVisibility(View.GONE);
                    noExpress.setVisibility(View.VISIBLE);
                    queryResult.setText("未查询到包裹");
                } else {
                    httpApi.bmxGetMyParcelListNew(s, new ApiListener() {
                        @Override
                        public void success(Api api) {
                            Log.d("logcat_qxj", "bmxGetMyParcelListNew:success= " + api.jsonObject);
                            try {
                                JSONArray jsonArray = api.jsonObject.getJSONArray("d");
                                Log.d("qxj--", jsonArray.length() + "--");
                                if (jsonArray.length() == 0) {
                                    expressListview.setVisibility(View.GONE);
                                    noExpress.setVisibility(View.VISIBLE);
                                    queryResult.setText("未查询到包裹");
                                } else {
                                    ExpressList.clear();
                                    for (int i = 0; i < jsonArray.length(); i++) {

                                        // JSON数组里面的具体-JSON对象
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        String takeCode = jsonObject.optString("frameCode", null);
                                        int outState = jsonObject.optInt("parcelStatus", 0);
                                        String phoneNum = jsonObject.optString("recipientMobile", null);
                                        String name = jsonObject.optString("recipientName", null);
                                        String orderId = jsonObject.optString("mailNo", null);
                                        int uploadState = jsonObject.optInt("isHavePic", 0);
                                        String time = jsonObject.optString("lastTimestamp", null);
                                        String parcelId = jsonObject.optString("parcelId", null);
                                        String arriveTime = jsonObject.optString("arriveTime", null);
                                        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                                        cal.setTimeInMillis(Long.valueOf(time)* 1000L);
                                        CharSequence sysTimeStr = DateFormat.format("MM-dd  HH:mm",cal);
                                        String outString = "已出库";
                                        boolean isHavaPic = true;
                                        if (outState == 1) {
                                            outString = "已出库";
                                        } else if (outState == 3) {
                                            outString = "拒收";

                                        } else if (outState == 4) {
                                            outString = "异常";
                                        } else if (outState == 6) {
                                            outString = "拒收出库";
                                        } else if (outState == 7) {
                                            outString = "异常出库";
                                        } else {
                                            outString = "已到站";
                                        }
                                        if (uploadState == 3) {
                                            isHavaPic = true;
                                        } else {
                                            isHavaPic = false;
                                        }
                                        // 日志打印结果：
                                        Log.d("qxj", "analyzeJSONArray1 解析的结果：takeCode" + takeCode + " outState:" + outState + " phoneNum:" + phoneNum
                                                + " name:" + name + " orderId:" + orderId + " uploadState:" + uploadState + " time:" + time);

                                        Express c = new Express(takeCode, outString, phoneNum, name, orderId, isHavaPic, (String) sysTimeStr,arriveTime,parcelId);
                                        ExpressList.add(c);
                                    }
                                    Log.d("qxj222","---"+ExpressList.size());
                                    noExpress.setVisibility(View.GONE);
                                    expressListview.setVisibility(View.VISIBLE);
                                    adapter.updateList(ExpressList);
                                }

                            } catch (Exception e) {
                            }
                        }

                        @Override
                        public void failure(Api api) {
                            Log.d("qxj--", "-----bmxGetMyParcelListNew-failure"+api.jsonObject);
                            if (!AdroidUtil.isConnectedToInternet(QueryActivity.this)) {
                                queryResult.setText("请检查网络连接");
                            } else {
                                queryResult.setText("未查询到包裹");
                            }

                        }

                        @Override
                        public void onData(Response response) {

                        }
                    });
                }
            }
        });
        imageClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                queryCardView.setVisibility(View.GONE);
            }
        });
        queryimageButton.setOnTouchListener(new MulitPointTouchListener());
    }
    @SuppressLint("HandlerLeak")
    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                queryimageButton.setImageBitmap((Bitmap) msg.obj);
                queryCardView.setVisibility(View.VISIBLE);
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        queryResult.setText("输入相关信息查询");
    }
}