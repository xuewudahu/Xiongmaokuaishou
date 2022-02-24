package com.xiongmaokuaishou.myapplication.model;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.xiongmaokuaishou.myapplication.QueryActivity;
import com.xiongmaokuaishou.myapplication.R;
import com.xiongmaokuaishou.myapplication.http.Api;
import com.xiongmaokuaishou.myapplication.http.ApiListener;
import com.xiongmaokuaishou.myapplication.http.httpNetApi;
import com.xiongmaokuaishou.myapplication.utils.AdroidUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Response;

public class ExpressAdapter extends ArrayAdapter<Express> {
    private int resourceId;
    private List<Express> mlist=new ArrayList<>();
    private Context mcontext;
    private  ViewHolder viewHolder;
    private httpNetApi httpApi;
    // 适配器的构造函数，把要适配的数据传入这里
    public ExpressAdapter(Context context, int textViewResourceId, List<Express> objects) {
        super(context, textViewResourceId, objects);
        mlist.addAll(objects);
        Log.d("qxj1","---"+mlist.size());
        this.mcontext = context;
        resourceId = textViewResourceId;
        httpApi = httpNetApi.getInstance();
        httpApi.Init();
    }
    public void updateList(List<Express> listInfo){
        Log.d("qxj21","---"+listInfo.size());
        mlist.clear();
        mlist.addAll(listInfo);
        Log.d("qxj2","---"+mlist.size());
        Log.d("qxj2","---"+listInfo.size());
        notifyDataSetChanged();
    }
    @Nullable
    @Override
    public Express getItem(int position) {
        return mlist.get(position);
    }

    // convertView 参数用于将之前加载好的布局进行缓存
    @SuppressLint("ResourceAsColor")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Express express = getItem(position); //获取当前项的Express实例
        // 加个判断，以免ListView每次滚动时都要重新加载布局，以提高运行效率
        View view;

        if (convertView == null) {

            // 避免ListView每次滚动时都要重新加载布局，以提高运行效率
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);

            // 避免每次调用getView()时都要重新获取控件实例
            viewHolder = new ViewHolder();
            viewHolder.takeCode = view.findViewById(R.id.take_code);
            viewHolder.outState = view.findViewById(R.id.out_state);
            viewHolder.phoneNum = view.findViewById(R.id.phone_num);
            viewHolder.name = view.findViewById(R.id.quit_name);
            viewHolder.orderID = view.findViewById(R.id.order_ID);
            viewHolder.uploadState = view.findViewById(R.id.upload_state);
            viewHolder.time = view.findViewById(R.id.quit_time);
            viewHolder.relativeLayout=view.findViewById(R.id.upload_fail_relative);

            // 将ViewHolder存储在View中（即将控件的实例存储在其中）
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        // 获取控件实例，并调用set...方法使其显示出来
        viewHolder.takeCode.setText(express.getTakeCode());
        viewHolder.outState.setText(express.getOutState());
        if (express.getOutState().equals("已出库")) {
            viewHolder.uploadState.setVisibility(View.VISIBLE);
        } else {
            viewHolder.uploadState.setVisibility(View.GONE);
        }
        viewHolder.phoneNum.setText(express.getPhoneNum());
        viewHolder.name.setText(express.getName());
        viewHolder.orderID.setText(express.getOrderID());
        if (!express.isUploadState()&&express.getOutState().equals("已出库")) {
            viewHolder.relativeLayout.setVisibility(View.VISIBLE);
            viewHolder.uploadState.setTextColor(Color.RED);
            viewHolder.uploadState.setOnClickListener(null);
        } else {
            viewHolder.relativeLayout.setVisibility(View.GONE);
            viewHolder.uploadState.setTextColor(Color.BLACK);
            initListener(position);
        }
        viewHolder.uploadState.setText(express.isUploadState()?"查看底单":"上传失败");
        viewHolder.time.setText(express.getTime());
        return view;
    }
    private  void initListener(int position) {
    viewHolder.uploadState.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Express express = getItem(position);
            httpApi.bmxGetParcelPic(express.getParcelId(), express.getArriveTime(), new ApiListener() {
                @Override
                public void success(Api api) {
                    Log.d("logcat_qxj----------", "bmxGetParcelPic:success= " + api.jsonObject);
                    JSONObject value = null;
                    try {
                        value = api.jsonObject.getJSONObject("d");
                        String url= value.optString("outPicUrl");
                        Log.d("qxj","---bmxGetParcelPic-success"+api.jsonObject);
                        Log.d("qxj","---bmxGetParcelPic-success"+url);
                        new Thread(){
                            @SuppressLint("WrongConstant")
                            @Override
                            public void run() {
                                // String picturepath = "http://vn9693.bvimg.com/14751/f159cbc72b3b3485.png";

                                String picturepath = url;
                                byte[] data = null;
                                try {
                                    data = AdroidUtil.getImage(picturepath);
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);// BitmapFactory：图片工厂！
                                    Looper.prepare();// 必须调用此方法，要不然会报错
                                    Message msg = new Message();
                                    msg.what = 0;
                                    msg.obj = bitmap;
                                    QueryActivity.handler.sendMessage(msg);
                                } catch (Exception e) {
                                    //Toast.makeText(mcontext, "获取图片错误", 1).show();
                                    Log.d("wxwUU","cuowu");
                                }
                            }
                        }.start();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void failure(Api api) {
                    Log.d("qxj","---bmxGetParcelPic-failure"+api.jsonObject);
                }

                @Override
                public void onData(Response response) {

                }
            });

        }
    });
}

    // 定义一个内部类，用于对控件的实例进行缓存
    class ViewHolder {
        TextView takeCode;
        TextView outState;
        TextView phoneNum;
        TextView name;
        TextView orderID;
        TextView uploadState;
        TextView time;
        RelativeLayout relativeLayout;
    }
}