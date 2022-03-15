package com.rq.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.rq.barcode.RqEngineer;
import com.rq.misc.MiscUtil;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.RequiresApi;

/**
 * 保存桢图像工具类
 */
public class FrameSaver {
    private static final String TAG = MiscUtil.getTag(FrameSaver.class);

    /**
     * 图像输出模式
     */
    public final static int SAVE_NONE = 0;
    //仅输出解码成功的图像
    public final static int SAVE_WHEN_DECODE_SUCCESS = 1;
    //仅输出解码失败的图像
    public final static int SAVE_WHEN_DECODE_FAILED = 2;
    //解码成功和失败的都输出
    public final static int SAVE_ALL = 3;
    //回调的图像都输出
    public final static int SAVE_DEBUG = 4;

    /**
     * 文件路径名称
     */
    private final static String DEFAULT_FOLDER_PATH = "/Android/data/com.xiongmaokuaishou.myapplication/files/";
    private final static String DEFAULT_FAILED_PREFIX = "FAILED_";
    private final static String DEFAULT_SUCCESS_PREFIX = "SUCCESS_";
    private final static String DEFAULT_DEBUG_PREFIX = "DEBUG_";
    /**
     * debug因为是一直输出的，所以要限制最大存图量
     */
    public final static int DEFAULT_MAX_NUM_FOR_SAVE = 50;
    /**
     * 图像输出格式
     */
    public final static int FORMAT_YUV = 0;
    public final static int FORMAT_PNG = 1;
    public final static int FORMAT_JPEG = 2;
    /**
     * png格式输出图像质量
     */
    public final static int QUALITY = 50;

    /**
     * 存图数量上限
     */
    private int saveNumMax = DEFAULT_MAX_NUM_FOR_SAVE;

    /**
     * 获取 存图数量上限
     *
     * @return 最大存图数量
     */
    public int getSaveNumMax() {
        return saveNumMax;
    }

    /**
     * 设置 存图数量上限
     *
     * @param saveNumMax
     */
    public void setSaveNumMax(int saveNumMax) {
        this.saveNumMax = saveNumMax;
    }

    /**
     * 存图像的关键边量
     */
    //存图方案
    private int frameSaveType = FrameSaver.SAVE_NONE;
    //存图格式
    private int frameSaveFormat = FrameSaver.FORMAT_PNG;
    /**
     * 数据(UI)和存图(解码)是不同线程，做一个任务属性
     * 数据输出任务（要存储的文件名）
     * 存图进程监听接收任务并置空任务
     */
    private List<Boolean> saveTaskCache = new ArrayList<Boolean>();


    private Context mContext;

    public FrameSaver(Context context, int frameSaveType, int frameSaveFormat) {
        this.mContext = context;
        this.frameSaveType = frameSaveType;
        this.frameSaveFormat = frameSaveFormat;
    }

    public int getFrameSaveType() {
        return frameSaveType;
    }

    public int getFrameSaveFormat() {
        return frameSaveFormat;
    }

    public void setFrameSaveType(int frameSaveType) {
        this.frameSaveType = frameSaveType;
    }


    public void setFrameSaveFormat(int frameSaveFormat) {
        this.frameSaveFormat = frameSaveFormat;
    }

    /**
     * 获取存图任务缓存List
     *
     * @return 存图任务List
     */
    public List<Boolean> getSaveTaskCache() {
        return saveTaskCache;
    }

    /**
     * 按照顺序获取计划生成的文件名称
     *
     * @param isDecodeSuccess 是否是解码成功的帧
     * @return 计划生成的文件名称
     */
    public String getPlanSaveFileName(boolean isDecodeSuccess) {

        String prefix = getPrefix(isDecodeSuccess);
        String format = MiscUtil.getSuffix(frameSaveFormat);
        if (prefix == null || format == null) {
            Log.i(TAG, "getPlanSaveFilePath failed, wrong prefix or format");
            return null;
        }

        return MiscUtil.getNewFilePath(saveNumMax, DEFAULT_FOLDER_PATH, prefix,
                MiscUtil.getSuffix(frameSaveFormat));

    }

    /**
     * 按照条码获取计划生成的文件名称
     *
     * @param barcode 条码信息，后来新加字段，如果存在则以条码名称为文件名称存图
     * @return 计划生成的文件名称
     */
    public String getPlanSaveFileName(String barcode) {
        if(!TextUtils.isEmpty(barcode)) {
            String format = MiscUtil.getSuffix(frameSaveFormat);
            if (format == null) {
                Log.i(TAG, "getPlanSaveFilePath failed, wrong prefix or format");
                return null;
            }
            return MiscUtil.getNewFilePathByBarcode(DEFAULT_FOLDER_PATH, barcode,
                    MiscUtil.getSuffix(frameSaveFormat));
        }
        Log.i(TAG, "getPlanSaveFilePath failed, null  barcode");
        return null;
    }

    /**
     * 把桢中数据存为有序名称和特定数据格式的图像数据到sdcard
     *
     * @param frame
     * @param isDecodeSuccess
     * @return 文件路径
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public synchronized String saveFrame(Frame frame, boolean isDecodeSuccess) {
        String name = getPlanSaveFileName(isDecodeSuccess);
        if (frame == null || TextUtils.isEmpty(name)) {
            if (MiscUtil.DEBUG)
                Log.d(TAG, "saveFrame error: args frame or name is null.");
            return null;
        }

        byte[] data = MiscUtil.getFrameData(frame);
        if (data == null) {
            if (MiscUtil.DEBUG)
                Log.d(TAG, "frame may been recycled.");
            return null;
        }
        if (MiscUtil.DEBUG)
            Log.d(TAG, "imageSave  name:=" + name + " data.length=" + data.length + " format=" + frameSaveFormat);

        return MiscUtil.doSave(mContext,data,name,frameSaveFormat);
    }

    /**
     * 把桢中数据存为条码名称和特定数据格式的图像数据到sdcard
     *
     * @param frame
     * @return 文件路径
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public synchronized String saveFrame(Frame frame) {
        if (frame == null) {
            if (MiscUtil.DEBUG)
                Log.d(TAG, "saveFrame error:  frame null.");
            return null;
        }

        String barcode = frame.getBarcode();
        if (TextUtils.isEmpty(barcode)) {
            if (MiscUtil.DEBUG)
                Log.d(TAG, "saveFrame error: frame barcode is null.");
            return null;
        }
Log.d("wwwwww1","----"+barcode);
        String name = getPlanSaveFileName(barcode);
        if (TextUtils.isEmpty(name)) {
            if (MiscUtil.DEBUG)
                Log.d(TAG, "saveFrame error: planfilename is null.");
            return null;
        }

        byte[] data = MiscUtil.getFrameData(frame);
        if (data == null) {
            if (MiscUtil.DEBUG)
                Log.d(TAG, "frame may been recycled.");
            return null;
        }

        if (MiscUtil.DEBUG)
            Log.d(TAG, "imageSave  name:=" + name + " data.length=" + data.length + " format=" + frameSaveFormat);
        return MiscUtil.doSave(mContext,data,barcode,frameSaveFormat);
    }

    /**
     * 判断是否开启存图模式
     * @return 是否开启存图模式
     */
    public boolean isSavingMode(){
        return frameSaveType != SAVE_NONE;
    }

    private String getPrefix(boolean isDecodeSuccess) {
        if (frameSaveType == SAVE_NONE) {
            return null;
        }
        String prefix = "";
        switch (frameSaveType) {
            case SAVE_WHEN_DECODE_SUCCESS:
                prefix += DEFAULT_SUCCESS_PREFIX;
                break;
            case SAVE_WHEN_DECODE_FAILED:
                prefix += DEFAULT_FAILED_PREFIX;
                break;
            case SAVE_ALL:
                if (isDecodeSuccess)
                    prefix += DEFAULT_SUCCESS_PREFIX;
                else
                    prefix += DEFAULT_FAILED_PREFIX;
                break;
            case SAVE_DEBUG:
                prefix += DEFAULT_DEBUG_PREFIX;
                break;
        }
        return prefix;
    }

}
