package com.xiongmaokuaishou.myapplication.http;

import okhttp3.Response;

/**
 * API调结果用监听接口
 */
public interface ApiListener {

    void success(Api api);

    void failure(Api api);

    void onData(Response response);
}
