package com.xiongmaokuaishou.myapplication.http;

import org.json.JSONObject;

import java.util.HashMap;

import android.os.Looper;
import android.util.Log;


public class httpNetApi extends Api {

    private String jsonCode;
    private JSONObject jsonData;

    private String bmxToken = "openbmx";

    private String url;

    private static httpNetApi netApi;

    public httpNetApi() {
        this.url = ApiUtil.IP_PORT;
    }

    public static httpNetApi getInstance() {
        if (netApi == null) {
            synchronized (httpNetApi.class) {
                if (netApi == null) {
                    netApi = new httpNetApi();
                }
            }
        }
        return netApi;
    }

    public void Init() {
        OkHttpUtil.init(); //for httpTest
    }

    public String getJsonCode() {
        return jsonCode;
    }

    public JSONObject getJsonData() {
        return jsonData;
    }

    @Override
    protected void parseCode(JSONObject jsonObject) {
        this.jsonCode = jsonObject.optString("e");
        Log.d("qxj", "parseCode e:= " + jsonCode);
    }

    @Override
    protected void parseData(JSONObject jsonObject) throws Exception {

        //this.jsonData = jsonObject.getJSONObject("d"); //for bmx platform
        Object value = jsonObject.get("d");
        if (value instanceof JSONObject) { //      value == JSONObject.NULL
            this.jsonData = jsonObject.getJSONObject("d"); //for bmx platform
        }

        Log.d("qxj111", "parseData : " + jsonData);
    }

    @Override
    protected String getUrl() {
        return url;
    }

    @Override
    protected boolean isBackToUiThread() {
        return true;
    }

    //for bmx platform
    public void bmxSaveToken(String token) {
        Log.d("qxj", "bmxSaveToken : token = " + token);
        this.bmxToken = token;
    }

    public void bmxGetToken(String user, String pwd, String deviceId, ApiListener apiListener) {
        Log.d("qxj", "bmxGetToken : ");
        Log.d("wwwwwwwwww22--", "---" + (Thread.currentThread() == Looper.getMainLooper().getThread()));
      /*  long timecurrentTimeMillis = System.currentTimeMillis();
        String currTime=String.valueOf(timecurrentTimeMillis);

        String biz = "{\"userName\":"+"\""+user+"\""+",\"userPwd\":"+"\""+pwd+"\""+",\"deviceNum\":"+"\""+deviceId+"\""+"}";

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("accountName", ApiUtil.ACCOUNT_NAME);
        //hashMap.put("accountPassword", ApiUtil.ACCOUNT_PASSWORD);
        hashMap.put("apiName",ApiUtil.BMX_API_GET_TOKEN);
        hashMap.put("bizData", biz);
        hashMap.put("token", bmxToken);
        hashMap.put("deviceNum", deviceId);
        hashMap.put("userPwd", pwd);
        hashMap.put("userName",user );
        hashMap.put("timestamp", currTime);
        hashMap.put("sign", ApiUtil.md5(biz+ApiUtil.ACCOUNT_NAME+ApiUtil.BMX_API_GET_TOKEN+"openbmx"+currTime+ApiUtil.ACCOUNT_PASSWORD));
      */

        String biz = "{\"userName\":" + "\"" + user + "\"" + ",\"userPwd\":" + "\"" + pwd + "\"" + ",\"deviceNum\":" + "\"" + deviceId + "\"" + "}";

        HashMap<String, String> hashMap = getMsgBody(biz, ApiUtil.BMX_API_GET_TOKEN);
        setParamsMap(hashMap);
        post(apiListener);


    }

    public void bmxGetQRToken(String deviceId, ApiListener apiListener) {
        Log.d("qxj", "bmxGetQRToken : ");

        String biz = "{\"deviceNum\":" + "\"" + deviceId + "\"" + "}";
        HashMap<String, String> hashMap = getMsgBody(biz, ApiUtil.BMX_API_GET_QRTOKEN);

        setParamsMap(hashMap);
        post(apiListener);

    }

    public void bmxLogout(ApiListener apiListener) {

        Log.d("qxj", "bmxLogout : ");

        String biz = "{}";
        HashMap<String, String> hashMap = getMsgBody(biz, ApiUtil.BMX_API_LOGOUT);

        setParamsMap(hashMap);
        post(apiListener);

        bmxSaveToken("openbmx"); //added for restore the token

    }

    public void bmxParcelOut(String mailNum, ApiListener apiListener) {


        Log.d("qxj", "bmxParcelOut : ");

        String biz = "{\"mailNo\":" + "\"" + mailNum + "\"" + "}";
        HashMap<String, String> hashMap = getMsgBody(biz, ApiUtil.BMX_API_OUTPUT_PARCEL);

        setParamsMap(hashMap);
        post(apiListener);


    }

    public void bmxGetMyParcelList(String number, ApiListener apiListener) {


        Log.d("qxj", "bmxGetMyParcelList : ");

        String biz = "{\"searchName\":" + "\"" + number + "\"" + "}";
        HashMap<String, String> hashMap = getMsgBody(biz, ApiUtil.BMX_API_GET_MY_PARCEL);

        setParamsMap(hashMap);
        post(apiListener);

    }

    public void bmxGetParcelInfo(String number, ApiListener apiListener) {


        Log.d("qxj", "bmxGetParcelInfo : ");

        String biz = "{\"mailNo\":" + "\"" + number + "\"}";
        HashMap<String, String> hashMap = getMsgBody(biz, ApiUtil.BMX_API_GET_PARCEL_INFO);

        setParamsMap(hashMap);
        post(apiListener);

    }

    public void bmxCheckToken(ApiListener apiListener) {

        Log.d("qxj", "bmxCheckToken : ");

        String biz = "{}";
        HashMap<String, String> hashMap = getMsgBody(biz, ApiUtil.BMX_API_CHECK_TOKEN);

        setParamsMap(hashMap);
        post(apiListener);

    }


    public void bmxGetMyParcelListNew(String number, ApiListener apiListener) {


        Log.d("qxj", "bmxGetMyParcelListNew : ");

        String biz = "{\"searchName\":" + "\"" + number + "\"" + "}";
        HashMap<String, String> hashMap = getMsgBody(biz, ApiUtil.BMX_API_GET_PARCEL_NEW);

        setParamsMap(hashMap);
        post(apiListener);

    }

    public void bmxGetBanner(ApiListener apiListener) {


        Log.d("qxj", "bmxGetBanner : ");

        String biz = "{}";
        HashMap<String, String> hashMap = getMsgBody(biz, ApiUtil.BMX_API_GET_BANNER);

        setParamsMap(hashMap);
        post(apiListener);

    }

    public void bmxGetStationInfo(ApiListener apiListener) {


        Log.d("qxj", "bmxGetStationInfo : ");

        String biz = "{}";
        HashMap<String, String> hashMap = getMsgBody(biz, ApiUtil.BMX_API_GET_STATION);

        setParamsMap(hashMap);
        post(apiListener);

    }

    public void bmxGetParcelPic(String parcelID, String arrivetime, ApiListener apiListener) {

        Log.d("qxj", "bmxParcelPic : ");
        String biz = "{\"parcelId\":" + "\"" + parcelID + "\"" + ",\"arriveTime\":" + "\"" + arrivetime + "\"" + "}";
        HashMap<String, String> hashMap = getMsgBody(biz, ApiUtil.BMX_API_GET_PARCEL_PIC);
        setParamsMap(hashMap);
        post(apiListener);

    }

    public void bmxCheckAccount(String deviceNumber, ApiListener apiListener) {


        Log.d("qxj", "bmxCheckAccount :bmxToken:=  " + bmxToken);

        String biz = "{\"deviceNum\":" + "\"" + deviceNumber + "\"}";
        HashMap<String, String> hashMap = getMsgBody(biz, ApiUtil.BMX_API_CHECK_ACCOUNT);

        setParamsMap(hashMap);
        post(apiListener);

    }


    public void bmxUploadParcel(String parcelID, String arrivetime, String filePath, ApiListener apiListener) {


        Log.d("qxj", "bmxUploadParcel : ");

        String biz = "{\"parcelId\":" + "\"" + parcelID + "\"" + ",\"arriveTime\":" + "\"" + arrivetime + "\"" + "}";
        HashMap<String, String> hashMap = getMsgBody(biz, ApiUtil.BMX_API_UPLOAD_PIC);

        setParamsMap(hashMap);
        upload(filePath, apiListener);  //NOTED

    }

    public void bmxDownloadImg(String url, ApiListener apiListener) {


        Log.d("qxj", "bmxDownloadImg : ");

        setParamsMap(null);
        get(url, apiListener);  //NOTED
    }


    private HashMap<String, String> getMsgBody(String bizData, String apiName) {

        long timecurrentTimeMillis = System.currentTimeMillis();
        String currTime = String.valueOf(timecurrentTimeMillis);

        HashMap<String, String> body = new HashMap<>();
        body.put("accountName", ApiUtil.ACCOUNT_NAME);
        //hashMap.put("accountPassword", ApiUtil.ACCOUNT_PASSWORD);
        body.put("apiName", apiName);
        body.put("bizData", bizData);
        Log.d("qxj", "---token " + bmxToken);
        body.put("token", bmxToken);
        body.put("timestamp", currTime);
        body.put("sign", ApiUtil.md5(bizData + ApiUtil.ACCOUNT_NAME + apiName + bmxToken + currTime + ApiUtil.ACCOUNT_PASSWORD));

        return body;
    }


}
