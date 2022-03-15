package com.xiongmaokuaishou.myapplication.http;

import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Response;

/**
 * 处理服务器端回调
 */
public abstract class Api {

    private ApiListener apiListener = null; // 将成功与否通过该接口回调

    public JSONObject jsonObject; // 响应结果
    public  Response  httpResponse;  //added

    private OkHttpCallback okHttpCallback = new OkHttpCallback(false) {

        @Override
        protected boolean isRunOnUiThread() {
            return isBackToUiThread();
        }

        @Override
        public void onSuccess(Call call, JSONObject jsonObject) { // 成功收到响应结果
            Api.this.jsonObject = jsonObject;
            if (isSuccess()) { // 根据状态码判断调用成功与否
                try {
                  // parseData(jsonObject);
                    Log.d("logcat_",jsonObject.toString());
                    apiListener.success(Api.this); // 回调成功
                } catch (Exception e) {
                    e.printStackTrace();
                    apiListener.failure(Api.this); // 回调失败，解析响应结果中的data错误
                }
            } else {
                try {
                    parseCode(jsonObject);
                    apiListener.failure(Api.this); // 回调失败，状态码非0
                } catch (Exception e) {
                    e.printStackTrace();
                    apiListener.failure(Api.this); // 回调失败，解析响应结果中的data错误
                }
            }
        }

        @Override
        public void onFailure(Call call) {
            apiListener.failure(Api.this);
        }

        @Override
        public void onData(Response response) {
            apiListener.onData(response);
        }
    };

    private OkHttpCallback okHttpCallbackData = new OkHttpCallback(true) {

        @Override
        protected boolean isRunOnUiThread() {
            return isBackToUiThread();
        }

        @Override
        public void onSuccess(Call call, JSONObject jsonObject) { // 成功收到响应结果

        }

        @Override
        public void onFailure(Call call) {
            apiListener.failure(Api.this);
        }

        @Override
        public void onData(Response response) {
            apiListener.onData(response);
        }
    };

    private boolean isSuccess() {
        return "0".equals(jsonObject.optString("e"))
                || "200".equals(jsonObject.optString("e"));
    }

    protected boolean isBackToUiThread() {
        return false;
    }

    private HashMap<String,String> paramsMap = new HashMap<>();
    void setParamsMap(HashMap<String,String> paramsMap) {
        this.paramsMap = paramsMap;
    }

    public void get(ApiListener apiListener) {
        this.apiListener = apiListener;
        OkHttpUtil.get(getUrl(), okHttpCallback, paramsMap);
    }

    public void get(String address,ApiListener apiListener) {
        this.apiListener = apiListener;
        OkHttpUtil.get(address, okHttpCallbackData, paramsMap);  //added
    }

    public void post(ApiListener apiListener) {
        this.apiListener = apiListener;
        okHttpCallback = new OkHttpCallback(false) {

            @Override
            protected boolean isRunOnUiThread() {
                return isBackToUiThread();
            }

            @Override
            public void onSuccess(Call call, JSONObject jsonObject) { // 成功收到响应结果
                Api.this.jsonObject = jsonObject;
                if (isSuccess()) { // 根据状态码判断调用成功与否
                    try {
                        // parseData(jsonObject);
                        Log.d("qxj-------------Success",jsonObject.toString());
                        apiListener.success(Api.this); // 回调成功
                    } catch (Exception e) {
                        e.printStackTrace();
                        apiListener.failure(Api.this); // 回调失败，解析响应结果中的data错误
                    }
                } else {
                    try {
                        parseCode(jsonObject);
                        apiListener.failure(Api.this); // 回调失败，状态码非0
                    } catch (Exception e) {
                        e.printStackTrace();
                        apiListener.failure(Api.this); // 回调失败，解析响应结果中的data错误
                    }
                }
            }

            @Override
            public void onFailure(Call call) {
                apiListener.failure(Api.this);
            }

            @Override
            public void onData(Response response) {
                apiListener.onData(response);
            }
        };

        OkHttpUtil.post(getUrl(), okHttpCallback, paramsMap);
    }

    public void upload(String filePath,ApiListener apiListener) {
        this.apiListener = apiListener;
        okHttpCallback = new OkHttpCallback(false) {

            @Override
            protected boolean isRunOnUiThread() {
                return isBackToUiThread();
            }

            @Override
            public void onSuccess(Call call, JSONObject jsonObject) { // 成功收到响应结果
                Api.this.jsonObject = jsonObject;
                if (isSuccess()) { // 根据状态码判断调用成功与否
                    try {
                        // parseData(jsonObject);
                        Log.d("qxj_Success",jsonObject.toString());
                        apiListener.success(Api.this); // 回调成功
                    } catch (Exception e) {
                        e.printStackTrace();
                        apiListener.failure(Api.this); // 回调失败，解析响应结果中的data错误
                    }
                } else {
                    try {
                        parseCode(jsonObject);
                        apiListener.failure(Api.this); // 回调失败，状态码非0
                    } catch (Exception e) {
                        e.printStackTrace();
                        apiListener.failure(Api.this); // 回调失败，解析响应结果中的data错误
                    }
                }
            }

            @Override
            public void onFailure(Call call) {
                apiListener.failure(Api.this);
            }

            @Override
            public void onData(Response response) {
                apiListener.onData(response);
            }
        };
        OkHttpUtil.upload(getUrl(), okHttpCallback, paramsMap,filePath);
    }


    protected abstract void parseCode(JSONObject jsonObject) throws Exception; //解析响应状态
    protected abstract void parseData(JSONObject jsonObject) throws Exception; //解析响应结果

    protected abstract String getUrl();
}
