package com.xiongmaokuaishou.myapplication.http;


import android.text.TextUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ApiUtil {
    /**
     * 用户ID
     */
    public static String ACCOUNT_ID = "";

    /**
     * 用户TOKEN
     */
    public static String ACCOUNT_TOKEN = "";

    /**
     * 用户头像
     */
    public static String ACCOUNT_AVATAR = "";

    /**
     * 用户昵称
     */
   // public static String ACCOUNT_NAME = "";


    public final static String IP_PORT = "http://api.java.105.100baimi.cn:10099/v3/openBmx"; //for BMX platform
    public final static String LOGIN = IP_PORT;
    public final static String TEMPORARY_TOKEN = IP_PORT + "auth/temporaryToken/";
    public final static String INFO_TOKEN = IP_PORT + "auth/infoToken/";
    public final static String IMAGE_URL_BMX_TEST = "http://xiaoniu.tp.pandabg.cn/c29b9121-decf-4dc6-8f54-0ec372f01a0e.jpg?watermark/3/text/5Ye65bqT5pe26Ze0OjIwMjItMDEtMTAgMTY6MzI6MzI=/fill/I0ZGMDAwMA==/gravity/NorthWest/dx/30/dy/50/fontsize/700/text/5pON5L2c5Lq6OjEyMzQxMjM0NDQw/fill/I0ZGMDAwMA==/gravity/NorthWest/dx/30/dy/100/fontsize/700/text/54aK54yr5b-r5pS25YWo55CD5bqX/fill/I0ZGMDAwMA==/gravity/NorthWest/dx/30/dy/150/fontsize/700";

    //for bmx open platform
    public final static String ACCOUNT_NAME =  "HYD10001";
    public final static String ACCOUNT_PASSWORD =  "4eAAAW04bZH6h2143";

    public final static String BMX_API_GET_TOKEN =  "GET_TOKEN";
    public final static String BMX_API_OUTPUT_PARCEL =  "OUTPUT_PARCEL";
    public final static String BMX_API_UPLOAD_PIC =  "UPLOAD_PARCEL_PIC";
    public final static String BMX_API_GET_MY_PARCEL =  "GET_MY_PARCEL";
    public final static String BMX_API_GET_PARCEL_INFO =  "GET_PARCEL_INFO";
    public final static String BMX_API_GET_QRTOKEN =  "GET_QR_TOKEN";
    public final static String BMX_API_CHECK_TOKEN =  "GET_TOKEN_EXPIRATION";
    public final static String BMX_API_GET_PARCEL_NEW =  "GET_MY_PARCEL_NEW";
    public final static String BMX_API_GET_STATION =  "GET_STATION_DETAIL";
    public final static String BMX_API_GET_BANNER =  "GET_DEVICE_BANNER";
    public final static String BMX_API_GET_PARCEL_PIC =  "GET_PARCEL_PIC";
    public final static String BMX_API_CHECK_ACCOUNT =  "VERIFY_DEVICE_AND_ACCOUNT_RELATION";
    public final static String BMX_API_LOGOUT =  "LOG_OUT_XM";




    public static String md5(String string){
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result.append(temp);
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
