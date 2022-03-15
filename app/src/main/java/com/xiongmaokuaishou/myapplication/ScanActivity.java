package com.xiongmaokuaishou.myapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.params.Face;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StatFs;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dou361.dialogui.DialogUIUtils;
import com.rq.barcode.RqDecoder;
import com.rq.barcode.RqEngineer;
import com.rq.barcode.RqSymbologyType;
import com.rq.camera.FrameSaver;
import com.rq.misc.MiscUtil;
import com.rq.view.RqDecodeComponent;
import com.xiongmaokuaishou.myapplication.http.Api;
import com.xiongmaokuaishou.myapplication.http.ApiListener;
import com.xiongmaokuaishou.myapplication.http.httpNetApi;
import com.xiongmaokuaishou.myapplication.model.Express;
import com.xiongmaokuaishou.myapplication.model.Take;
import com.xiongmaokuaishou.myapplication.model.TakeAdapter;
import com.xiongmaokuaishou.myapplication.ui.CameraTexture2;
import com.xiongmaokuaishou.myapplication.utils.AdroidUtil;
import com.xiongmaokuaishou.myapplication.utils.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Response;

public class ScanActivity extends AppCompatActivity implements RqDecoder.ResultCallback {
    private static TextureView rView;//前摄人脸显示框
    private RqEngineer mRqEngineer;//扫码类
    private RqDecodeComponent rqDecodeComponent;
    public CameraTexture2 cameraView;
    private LinearLayout quitScanLinera;//返回首页
    private LinearLayout visibilityScanSuccessLinear;//出库成功的显示和隐藏
    private LinearLayout visibilityScanFailLinear;//出库失败的显示和隐藏
    private LinearLayout visibilityScanTakeLinear;//未取包裹的显示和隐藏
    private LinearLayout visibilityScanNullLinear;//初始化的显示和隐藏
    private TextView spacePercentage;//空间剩余百分比
    private TextView scanFailOrderId;//出库失败的运单号
    private TextView scanFailCause;//出库失败的原因
    private TextView scanTakeNum;//还剩多少包裹未取的数量
    private TextView scanSuccessOrderId;//出库成功的运单号
    private TextView scanSuccessNum;//今日取件的数量
    private TextView scanTakeMobile;//取件人姓名和手机号
    private TextView scanTakeName;//取件人手机号
    private TextView noTakeButton;//取件人手机号
    private ImageView imagePercentage;
    private ImageView imagePercentageGrey;
    private List<Take> takeList;//未取包裹的列表
    private List<Take> takeList1;//未取包裹的列表
    private Paint pb;
    public static boolean isFace;
    public static final int SCANSUCCESS = 0x31;
    public static final int SCANRESULT = 0x32;
    public static final int NOEDIT = 0x33;

    public static final int UPDATE_TIME = 0x34;
    public static final int SCANFAIL = 0x35;
    public static final int DEFAULT = 0x36;
    public static final int UPDATE_SPACE = 0x37;
    private TextView OrderIDTextview;//运单号的显示
    private TextView updateTime;//时间显示
    private ListView listView;
    private TextView stationNameTextview;//站点的名称
    private TakeAdapter adapter;
    private httpNetApi httpApi;
    private String orderName;
    private long time;
    private SharedPreferences sharedPreferences;
    MediaPlayer mMediaPlayer;
    private boolean isFaceVisibility;
    Runnable runnable_scanner;
    Runnable runnable_decode;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        AdroidUtil.setStatusBarMode(this, true, R.color.white);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        sharedPreferences = SharedPreferences.getInstance();
        initView();
        initListener();
        httpApi = httpNetApi.getInstance();
        httpApi.Init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRqEngineer.getRqDecoder().setNumberOfBarcodesToDecode(sharedPreferences.getInt("MultipleCode", 1));
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void initView() {

        quitScanLinera = findViewById(R.id.scan_quit);
        rqDecodeComponent = (RqDecodeComponent) findViewById(R.id.scan_scanner_view);
        cameraView = (CameraTexture2) findViewById(R.id.scan_face_camera);
        spacePercentage = findViewById(R.id.scan_space_Percentage);
        imagePercentage = findViewById(R.id.scan_space_image);
        imagePercentageGrey = findViewById(R.id.scan_space_image_grey);
        rView = findViewById(R.id.texture_view);
        OrderIDTextview = findViewById(R.id.scan_orderID);
        updateTime = findViewById(R.id.update_time);
        listView = findViewById(R.id.list_view_take);
        visibilityScanSuccessLinear = findViewById(R.id.scan_sucess);
        visibilityScanFailLinear = findViewById(R.id.scan_fail);
        visibilityScanTakeLinear = findViewById(R.id.scan_take);
        visibilityScanNullLinear = findViewById(R.id.scan_null);
        scanFailOrderId = findViewById(R.id.scan_fail_orderid);
        scanFailCause = findViewById(R.id.scan_fail_cause);
        scanTakeNum = findViewById(R.id.scan_take_num);
        scanTakeMobile = findViewById(R.id.scan_take_name);
        scanSuccessOrderId = findViewById(R.id.scan_success_orderid);
        scanSuccessNum = findViewById(R.id.scan_success_num);

        noTakeButton = findViewById(R.id.noTakeButton);

        rqDecodeComponent.transformRotation(Surface.ROTATION_270);


        mRqEngineer = RqEngineer.getInstence(this);

        mRqEngineer.setContinueScanMode(true);

        mRqEngineer.getRqDecoder().addResultCallback(ScanActivity.this);

        mRqEngineer.getRqDecoder().getNumberOfBarcodesToDecode();


        FrameSaver f = mRqEngineer.getFrameSaver();
        f.setFrameSaveType(1);
        f.setFrameSaveFormat(2);


        stationNameTextview = findViewById(R.id.scan_stationName);
        stationNameTextview.setText(MainActivity.stationName);
        takeList = new ArrayList<>();
        takeList1 = new ArrayList<>();
        adapter = new TakeAdapter(ScanActivity.this, R.layout.scan_listview_item, takeList);
        listView.setAdapter(adapter);

        new Thread() {
            @Override
            public void run() {
                int i = 0;
                do {
                    try {
                        Thread.sleep(100);
                        Message msg = new Message();
                        msg.what = UPDATE_TIME;  //消息(一个整型值)
                        handler.sendMessage(msg);// 每隔100毫秒发送一个msg给mHandler
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } while (true);
            }
        }.start();
    }

    private void initListener() {
        quitScanLinera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent =new Intent(ScanActivity.this,MainActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(intent);
                finish();
            }
        });
        noTakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setVisibility(true, false, false, false);
            }
        });


    }


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @SuppressLint({"ResourceAsColor", "SetTextI18n", "HandlerLeak"})
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SCANFAIL:
                    String cause = msg.getData().getString("text");
                    if (cause.equals("出库失败，未检测到人脸，请正对摄像头")) {
                        isFaceVisibility = true;
                    } else {
                        isFaceVisibility = false;
                    }
                    String resultfail = msg.obj.toString();
                    scanFailOrderId.setText(resultfail);
                    scanFailCause.setText(cause);
                    setVisibility(false, false, true, false);
                    break;
                case SCANRESULT:
                    String result = msg.obj.toString();
                    OrderIDTextview.setText(result);
                    break;
                case NOEDIT:
                    OrderIDTextview.setText(" ");
                    break;
                case DEFAULT:
                    setVisibility(true, false, false, false);
                    break;
                case UPDATE_TIME:
                    long sysTime = System.currentTimeMillis();//获取系统时间
                    CharSequence sysTimeStr = DateFormat.format("yyyy/MM/dd HH:mm:ss", sysTime);//时间显示格式
                    updateTime.setText(sysTimeStr);
                    break;
                case UPDATE_SPACE:
                    spacePercentage.setText(String.valueOf(AdroidUtil.getSpace()) + "%");
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imagePercentage.getLayoutParams();
                    RelativeLayout.LayoutParams layoutParamsGrey = (RelativeLayout.LayoutParams) imagePercentageGrey.getLayoutParams();
                    layoutParams.width = (int) ((100 - AdroidUtil.getSpace()) * layoutParamsGrey.width / 100);
                    imagePercentage.setLayoutParams(layoutParams);
                    break;
            }
        }
    };

    @Override
    public void onResultCallback(String s, RqSymbologyType symbologyType, int[] corner) {
        parceOut(s, System.currentTimeMillis());

    }

    @Override
    public void onMultipleResultCallback(String[] strings, RqSymbologyType[] symbologyTypes, List<int[]> corners) {

        List list = Arrays.asList(strings);
        Set set = new HashSet(list);
        String[] rid = (String[]) set.toArray(new String[0]);
        for (String s : rid) {
            parceOut(s, System.currentTimeMillis());
        }
    }

    @Override
    public void onDecodeError(int errorCode) {

    }


    @Override
    protected void onStart() {
        super.onStart();

        //开始时获取今日取件的个数
        long sysTime = System.currentTimeMillis();//获取系统时间
        int sysTimeSun = Integer.valueOf((String) DateFormat.format("dd", sysTime));//时间显示格式
        int finallySun = sharedPreferences.getInt("scanSuccessTime", 32);
        if (finallySun != sysTimeSun) {
            sharedPreferences.putInt("scanSuccessTime", sysTimeSun);
            scanSuccessNum.setText("0");
            sharedPreferences.putInt("scanSuccessNum", 0);
        } else {
            int scanNum = sharedPreferences.getInt("scanSuccessNum", 0);
            scanSuccessNum.setText(String.valueOf(scanNum));
        }

        //开始时获取储存空间的余量
        new Thread() {
            @Override
            public void run() {
                do {
                    try {

                        Message msg = new Message();
                        msg.what = UPDATE_SPACE;
                        handler.sendMessage(msg);
                        Thread.sleep(1 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } while (true);
            }
        }.start();

//        new Thread() {
//            @Override
//            public void run() {
//                cameraView.onResume(ScanActivity.this);
//            }
//        }.start();

        runnable_scanner = new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void run() {
                mRqEngineer.setIllState(10);
                if (sharedPreferences.getBoolean("gamma_on", true)) {
                    mRqEngineer.setGammaOn(true);
                }
                cameraView.onResume(ScanActivity.this);
                if (mRqEngineer.openScannner() != MiscUtil.NO_ERROR) {
                    Toast.makeText(ScanActivity.this, "扫码相机无法打开,请重新启动", Toast.LENGTH_SHORT).show();
                }
            }
        };
        runnable_decode = new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void run() {
                if (mRqEngineer.startDecode() == MiscUtil.NO_ERROR) {
                    // Toast.makeText(MainActivity.this, "无法打开扫码,请重新启动", Toast.LENGTH_SHORT).show();
                }
            }
        };
        handler.postDelayed(runnable_scanner, 1000);
        handler.postDelayed(runnable_decode, 1500);

    }

    private void setVisibility(boolean scannull, boolean scansuccess, boolean scanfail, boolean scantake) {
        visibilityScanNullLinear.setVisibility(scannull ? View.VISIBLE : View.GONE);
        visibilityScanSuccessLinear.setVisibility(scansuccess ? View.VISIBLE : View.GONE);
        visibilityScanFailLinear.setVisibility(scanfail ? View.VISIBLE : View.GONE);
        visibilityScanTakeLinear.setVisibility(scantake ? View.VISIBLE : View.GONE);

    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable_scanner);
        handler.removeCallbacks(runnable_decode);
        if (mRqEngineer != null) {
            mRqEngineer.setIllState(0);
            mRqEngineer.setGammaOn(false);
            if (mRqEngineer.stopDecode() == MiscUtil.NO_ERROR) {
            }
        }
        //退出时关闭前摄
        if (cameraView != null) {


            cameraView.onStop();
        }
        //退出时关闭扫码相机、扫光灯
        if (mRqEngineer != null) {
            mRqEngineer.closeScanner();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRqEngineer != null)
            mRqEngineer.destory();
    }

    public void onCameraImagePreviewed(CaptureResult result) {
        Face faces[] = result.get(CaptureResult.STATISTICS_FACES);
        Canvas canvas = rView.lockCanvas();
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//旧画面清理覆盖

        if (faces.length > 0) {

            isFace = true;
            for (int i = 0; i < faces.length; i++) {
                Rect fRect = faces[i].getBounds();
                //人脸检测坐标基于相机成像画面尺寸以及坐标原点。此处进行比例换算
                //成像画面与方框绘制画布长宽比比例（同画面角度情况下的长宽比例（此处前后摄像头成像画面相对预览画面倒置（±90°），计算比例时长宽互换））
                float scaleHeight = canvas.getWidth() * 1.0f / 1477;
                float scaleWidth = canvas.getHeight() * 1.0f / 2192;
//                float scaleHeight = canvas.getWidth() * 1.0f / 3872;
//                float scaleWidth = canvas.getHeight() * 1.0f / 2192;
                //坐标缩放
                int l = (int) (fRect.left * scaleWidth);
                int t = (int) (fRect.top * scaleHeight);
                int r = (int) (fRect.right * scaleWidth);
                int b = (int) (fRect.bottom * scaleHeight);

                canvas.drawRect(canvas.getWidth() - t, canvas.getHeight() - r, canvas.getWidth() - b, canvas.getHeight() - l, getPaint());
                //canvas.drawRect(t,l,b,r,getPaint());
                //canvas.drawRect(l,canvas.getHeight()-b,r,canvas.getHeight()-t,getPaint());
            }
        } else {

            isFace = false;
        }
        rView.unlockCanvasAndPost(canvas);
    }

    private Paint getPaint() {
        if (pb == null) {
            pb = new Paint();
            pb.setColor(Color.GREEN);
            pb.setStrokeWidth(1);
            pb.setStyle(Paint.Style.STROKE);//使绘制的矩形中空
        }
        return pb;
    }


    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {

            releaseMediaPlayer();
        }
    };

    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    Map<String, Long> mMap = new ConcurrentHashMap<String, Long>();

    private void parceOut(String s, long nowTime) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (String s1 : mMap.keySet()) {
                    if (System.currentTimeMillis() - mMap.get(s1) > 5000) {
                        mMap.remove(s1);
                    }
                }

                if (!mMap.keySet().contains(s)) {
                    mMap.put(s, nowTime);
                } else {
                    return;
                }


                Message msg = new Message();
                msg.what = SCANRESULT;
                msg.obj = s;
                handler.removeMessages(NOEDIT);
                handler.sendMessage(msg);
                handler.sendEmptyMessageDelayed(NOEDIT, 2000);
                if (!AdroidUtil.isConnectedToInternet(ScanActivity.this)) {
                    Message msgnet = new Message();
                    msgnet.what = SCANFAIL;
                    msgnet.obj = s;
                    Bundle bundle = new Bundle();
                    bundle.putString("text", "出库失败，请检查您的网络连接");
                    msgnet.setData(bundle);
                    handler.removeMessages(DEFAULT);
                    handler.sendMessage(msgnet);
                    handler.sendEmptyMessageDelayed(DEFAULT, 2000);
                    return;
                }

                if (ScanActivity.isFace || !sharedPreferences.getBoolean("face_camera_out", true)) {
//            if (orderName != null && !orderName.isEmpty() && orderName.equals(s) && System.currentTimeMillis() - time < 5000) {
//                return;
//            }
                    orderName = s;
                    time = System.currentTimeMillis();
                    httpApi.bmxParcelOut(s, new ApiListener() {
                        @Override
                        public void success(Api api) {
                            Log.d("logcat_qxj", "bmxParcelOut:success= " + api.jsonObject + "  " + s);
                            long sysTime = System.currentTimeMillis();//获取系统时间
                            int sysTimeSun = Integer.valueOf((String) DateFormat.format("dd", sysTime));//时间显示格式
                            int finallySun = sharedPreferences.getInt("scanSuccessTime", 32);
                            if (finallySun != sysTimeSun) {
                                sharedPreferences.putInt("scanSuccessTime", sysTimeSun);
                                scanSuccessNum.setText("1");
                                sharedPreferences.putInt("scanSuccessNum", 1);
                            } else {
                                int scanNum = sharedPreferences.getInt("scanSuccessNum", 0);
                                scanSuccessNum.setText(String.valueOf(scanNum + 1));
                                sharedPreferences.putInt("scanSuccessNum", scanNum + 1);
                            }

                            try {
                                JSONObject value = api.jsonObject.getJSONObject("d");
                                JSONObject currentParcel = value.getJSONObject("currentParcel");
                                Intent intent = new Intent();
                                intent.putExtra("arriveTime", currentParcel.optString("arriveTime"));
                                intent.putExtra("parcelId", currentParcel.optString("parcelId"));
                                intent.setAction("BROADCAST_ACTION");
                                ScanActivity.this.sendBroadcast(intent);
                                mMediaPlayer = MediaPlayer.create(ScanActivity.this, R.raw.success);
                                mMediaPlayer.start();
                                int i = Integer.valueOf(value.optString("todoParcel"));
                                if (i >= 1) {
                                    scanTakeNum.setText(String.valueOf(i) + "件");
                                    JSONArray jsonArray = value.getJSONArray("data");

                                    String recipientMobile = "";
                                    String recipientName = "";
                                    takeList1.clear();
                                    for (int j = 0; j < jsonArray.length(); j++) {
                                        JSONObject jsonObject = jsonArray.getJSONObject(j);
                                        String mailNo = jsonObject.optString("mailNo", null);
                                        String frameCode = jsonObject.optString("frameCode", null);
                                        recipientMobile = jsonObject.optString("recipientMobile", null);
                                        recipientName = jsonObject.optString("recipientName", null);
                                        Take take = new Take(frameCode, mailNo);
                                        takeList1.add(take);
                                    }
                                    adapter.updateList(takeList1);
                                    scanTakeMobile.setText(recipientName + " **" + recipientMobile.substring(recipientMobile.length() - 4));
                                    setVisibility(false, false, false, true);
                                    handler.removeMessages(DEFAULT);
                                } else {
                                    scanSuccessOrderId.setText(s);
                                    setVisibility(false, true, false, false);
                                    handler.removeMessages(DEFAULT);
                                    handler.sendEmptyMessageDelayed(DEFAULT, 2000);
                                }
                            } catch (Exception e) {

                            }
                        }

                        @Override
                        public void failure(Api api) {
                            Log.d("logcat_qxj", "bmxParcelOut:failure= " + api.jsonObject);
                            Message msg = new Message();
                            msg.what = SCANFAIL;
                            msg.obj = s;
                            Bundle bundle = new Bundle();
                            String errorCode = api.jsonObject.optString("e");
                            switch (errorCode) {
                                case "E125":
                                    mMediaPlayer = MediaPlayer.create(ScanActivity.this, R.raw.notstorage);
                                    mMediaPlayer.start();
                                    mMediaPlayer.setOnCompletionListener(mCompletionListener);
                                    bundle.putString("text", "包裹未入库，请先入库后再出库");
                                    break;
                                case "E127":
                                    mMediaPlayer = MediaPlayer.create(ScanActivity.this, R.raw.already);
                                    mMediaPlayer.start();
                                    mMediaPlayer.setOnCompletionListener(mCompletionListener);
                                    bundle.putString("text", "包裹已出库，请勿重复出库");
                                    break;
                                case "E126":
                                    mMediaPlayer = MediaPlayer.create(ScanActivity.this, R.raw.problem);
                                    mMediaPlayer.start();
                                    mMediaPlayer.setOnCompletionListener(mCompletionListener);
                                    bundle.putString("text", "问题件，请退回给快递员");
                                    break;
                                case "E999":
                                    mMediaPlayer = MediaPlayer.create(ScanActivity.this, R.raw.problem);
                                    mMediaPlayer.start();
                                    mMediaPlayer.setOnCompletionListener(mCompletionListener);
                                    bundle.putString("text", "接口异常");
                                    break;
                                default:
                                    bundle.putString("text", "请重试");
                                    relogin();
                                    break;
                            }
                            msg.setData(bundle);
                            handler.removeMessages(DEFAULT);
                            handler.sendMessage(msg);
                            handler.sendEmptyMessageDelayed(DEFAULT, 2000);
                        }

                        @Override
                        public void onData(Response response) {

                        }
                    });

                } else {
                    mMap.remove(s);
                    if (visibilityScanNullLinear.getVisibility() == View.VISIBLE || isFaceVisibility) {
                        if (isFaceVisibility) {
                            isFaceVisibility = false;
                        }
                        if (mMediaPlayer == null) {
                            mMediaPlayer = MediaPlayer.create(ScanActivity.this, R.raw.face);
                        }

                        if (!mMediaPlayer.isPlaying()) {
                            mMediaPlayer.start();
                            mMediaPlayer.setOnCompletionListener(mCompletionListener);
                        }
                        Message msgfail = new Message();
                        msgfail.what = SCANFAIL;
                        msgfail.obj = s;
                        Bundle bundle = new Bundle();
                        bundle.putString("text", "出库失败，未检测到人脸，请正对摄像头");
                        msgfail.setData(bundle);
                        handler.removeMessages(DEFAULT);
                        handler.sendMessage(msgfail);
                        handler.sendEmptyMessageDelayed(DEFAULT, 2000);
                    }

                }
            }
        });
    }

    private void relogin() {
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
                                Log.d("logcat_qxj","bmxGetToken---success= "+api.jsonObject);

                                try {
                                    JSONObject value = api.jsonObject.getJSONObject("d");
                                    httpApi.bmxSaveToken(value.optString("token"));
                                    sharedPreferences.putString("taken",value.optString("token"));
                                    sharedPreferences.putBoolean("isLogin",true);
                                }catch (Exception e){}
                            }

                            @Override
                            public void failure(Api api) {
                                Log.d("logcat_qxj","bmxGetToken---failure-"+api.jsonObject);

                            }

                            @Override
                            public void onData(Response response) {
                            }
                        });
                    }

                    @Override
                    public void onData(Response response) {

                    }
                });
            }
        },1);
    }

}