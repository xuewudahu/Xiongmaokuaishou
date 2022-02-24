package com.rq.barcode;

import android.content.Context;
import android.content.SharedPreferences;

import com.rq.camera.Frame;

import java.util.List;

/**
 * 解码类：barcode解析，配置
 */
public abstract class RqDecoder {

    public final static int DECODE_ERROR_TIMEOUT = -1;
    public final static int DECODE_ERROR_CANCEL = -2;
    public final static int DECODE_ERROR_OVERHEAT = -3;
    public final static int DECODE_ERROR_NOT_INIT = -4;
    public final static int DECODE_ERROR_OPEN_CAMERA_FAILED = -5;
    public final static int DECODE_ERROR_NOT_OPEN_CAMERA = -6;
    //设置条码开关等参数，引用了不支持的类型
    public final static int DECODE_ERROR_NOT_SUPPORT_TYPE = -7;
    public final static int DECODE_ERROR_FAILED = -8;
    public final static int DECODE_WARNING_REPEAT_CALL = -21;


    //多条码最大条码数
    public static final int MAX_BARCODE_NUMBERS = 5;//20

    /**
     * 构造函数
     * @param context
     */
    public RqDecoder(Context context){}


    /**
     * 扫描参数初始化
     * @return 执行反馈
     */
    public abstract int initialize();
    //开始解码
    /**
     * 开始解码
     * @return 执行反馈
     */
    public abstract int startDecode(Frame frame);
    //停止解码
    /**
     * 停止解码
     * @return 执行反馈
     */
    public abstract int stopDecode();
    //销毁
    /**
     * 销毁
     * @return 执行反馈
     */
    public abstract int destroy();

    /**
     * 获取注册工具类，用于临时注册
     * @return 执行反馈
     */
    public abstract ActivateTool getActivateTool();


    /**
     * 扫描结果回调
     * @param resultCallback
     */
    public abstract void addResultCallback(ResultCallback resultCallback);
    public abstract void removeResultCallback(ResultCallback resultCallback);

    /**
     * 设置同时能够扫到的条码数量（默认5）
     * @param number
     */
    public abstract void setNumberOfBarcodesToDecode(int number);

    /**
     * 获取同时能够扫到的条码数量最大值
     * @return 多条码数量最大值
     */
    public abstract int getNumberOfBarcodesToDecode();

    /**
     * 设置条码开关，checksum等属性
     * @param key
     * @param sharedPreferences
     * @return  执行反馈
     */
    public abstract int pushBarcodePreference(String key, SharedPreferences sharedPreferences);
    /**
     * 从条码库中获取当前状态的条码配置
     * @param key
     * @return SharedPreferences
     */
    public abstract SharedPreferences pullBarcodePreference(String key);
    /**
     * 常用二维码开关,涉及如下条码
     * Aztec,PDF417,MicroPDF417,QR,MicroQR,MaxiCode,GridMatrix,DataMatrix,HanXin
     * @param enable
     */
    public abstract void set2DBarcodesEnable(boolean enable);

    public abstract void addResultCallback();

    /**
     * 扫描结果回调
     */
    public interface ResultCallback{
        /**
         * 扫到单个条码
         * @param s
         * @param symbologyType
         * @param corner
         */
        void onResultCallback(String s, RqSymbologyType symbologyType, int[] corner);

        /**
         * 扫到多个条码
         * @param strings
         * @param symbologyTypes
         * @param corners
         */
        void onMultipleResultCallback(String[] strings, RqSymbologyType[] symbologyTypes, List<int[]> corners);

        /**
         * 一桢解码失败
         * @param errorCode
         */
        void onDecodeError(int errorCode);
    }

}
