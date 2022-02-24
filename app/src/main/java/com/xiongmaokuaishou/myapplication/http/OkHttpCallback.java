package com.xiongmaokuaishou.myapplication.http;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 封装原始Callback，对响应事件进行监听，下层判断成功与否
 */
public abstract class OkHttpCallback implements Callback {

    public abstract void onSuccess(Call call, JSONObject jsonObject);
    public abstract void onFailure(Call call);
    public abstract void onData(Response response); //added
    private Handler handler = new Handler(Looper.getMainLooper());
    private JSONObject jsonData;
    private boolean isPassed  = false;


    public OkHttpCallback(boolean flag) {
         this.isPassed = flag;
    }

    @Override
    public synchronized void onResponse( final Call call,  final Response response) {

       // synchronized(this) {
            if (response.isSuccessful()) {
                if (!isPassed) {
                    try {
                        String string = response.body().string().trim();
                        jsonData = (JSONObject) new JSONTokener(string).nextValue();
                        System.out.println(jsonData);
                        Log.d("qxj", "-------------------" + jsonData);
                    } catch (Exception e) {
                        e.printStackTrace();
                        onFailure(call, null); // 数据解析错误
                    }
                    if (jsonData != null) {
                        Log.d("qxj", "-------------------" + isRunOnUiThread());
                        if (isRunOnUiThread()) {
                            Log.d("qxj", "-------------------" + isRunOnUiThread());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("qxj", "----OkHttpCallback onRespons2e-");
                                    Log.d("qxj121----------","----OkHttpCallback onResponse- isPassed := "+jsonData);
                                    onSuccess(call, jsonData);

                                }
                            });
                        } else {
                            onSuccess(call, jsonData);
                        }
                    } else {
                        onFailure(call, null); // 响应数据为空
                    }
                } else {

                    onData(response);  //added
                }

            } else {
                Log.d("qxj", "----OkHttpCallback onRespons onFailure-");
                onFailure(call, null); // 响应不成功
            }
      //  }
    }

    @Override
    public void onFailure( final Call call,  IOException e) {
        Log.d("qxj ","----OkHttpCallback onFailure-");
        Log.d("qxj","-----"+e.toString());
        if (isRunOnUiThread()) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onFailure(call);

                }
            });
        } else {
            onFailure(call);
        }
    }

    protected boolean isRunOnUiThread() {
        return true;
    }

    public static String JSONTokener(String in){
         if(in != null&& in.startsWith("\ufeff")){
             in= in.substring(1);
         }
         return in;
    }
}
