package com.rq.barcode.cortex;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.codecorp.licensing.LicenseCallback;
import com.codecorp.licensing.LicenseStatusCode;
import com.rq.barcode.ActivateTool;
import com.rq.barcode.RqDecoder;
import com.rq.misc.MiscUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import androidx.annotation.RequiresApi;
/**
 * @deprecated
 */
public class CortexActivateTool extends ActivateTool implements LicenseCallback {
    private static final String TAG = MiscUtil.getTag(CortexActivateTool.class);
    /**
     * 状态机
     */
    private int actatveState = UNACTIVATIED;

    /**
     * 关键属性
     */
    private Handler mHandler = new Handler();
    private ActivateCallback mActivateCallback;
    private CortexDecoder mDecoder;

    /**
     * 注册工具类
     * @param mContext
     * @param rqDecoder
     */
    protected CortexActivateTool(Context mContext, RqDecoder rqDecoder){
        super(rqDecoder);
        mDecoder = (CortexDecoder)rqDecoder;
        mDecoder.getCortexDecoderLibrary().setLicenseCallback(this);
    }
    /**
     * 注册状态回调
     * @param activateCallback
     */
    public void setActivateCallback(ActivateCallback activateCallback) {
        this.mActivateCallback = activateCallback;
    }

    /**
     * 生成返回设备信息文件C2V,用于PC注册，Cortex特有方法
     */
    public void generateDeviceID(){
        if(mDecoder.getCortexDecoderLibrary() != null)
            mDecoder.getCortexDecoderLibrary().generateDeviceID();
    }

    /**
     * 激活注册
     * @param actCode
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void activateLicense(String actCode) {
        if(mDecoder.getCortexDecoderLibrary() == null) {
            if (MiscUtil.DEBUG)
                Log.d(TAG, "activateLicense DecoderLibrary is not inited.");
            return;
        }
        /**
         * 如果已经注册过，不再进行注册
         */
        if (mDecoder.getCortexDecoderLibrary().isLicenseActivated()
                && !mDecoder.getCortexDecoderLibrary().isLicenseExpired()) {
            if (MiscUtil.DEBUG)
                Log.d(TAG, "initCortexBarcode activate license is Activated， no need active again.");
            actatveState = ACTIVATIED;
            return;
        }
        /**
         * register need wait 10 second
         */
        if (actatveState == ACTIVATING) {
            if (MiscUtil.DEBUG)
                Log.d(TAG, "initCortexBarcode activate license is activating... ");
            return;
        }
        /**
         * 注册优先顺序 :
         * 使用传入码在线注册
         * 》如果码为空
         * 》进行本地注册文件加载
         * 》如果本机文件注册加载失败
         * 》使用本地settings.global存储的license字段进行在线住处
         * 》如果settings.global存储的license字段为空
         * 》使用固定测试码进行注册
         */
        final boolean isNetworkConneted = MiscUtil.isConnectedToInternet(mDecoder.getContext());
        if(!TextUtils.isEmpty(actCode)) {
            if (isNetworkConneted) {
                mDecoder.getCortexDecoderLibrary().activateLicense(actCode);
                if (MiscUtil.DEBUG)
                    Log.d(TAG, "initCortexBarcode activate license start");
                actatveState = ACTIVATING;
            } else {
                if (MiscUtil.DEBUG)
                    Log.d(TAG, "initCortexBarcode Please enable internet to activate the license.");
                actatveState = UNACTIVATIED;
            }
        } else if (!loadLicenseFile() && isNetworkConneted) {//优先使用本地文件注册,如果不成功使用缓存或临时码进行注册
            actCode = Settings.Global.getString(mDecoder.getContext().getContentResolver(), "license");
            if (MiscUtil.DEBUG)
                Log.d(TAG, "initCortexBarcode activate license.actCode=" + actCode);
            if (TextUtils.isEmpty(actCode)) {
                actCode = "70c5f6ca-f863-4deb-a9f8-a4cad16e6ed5";
            }
            mDecoder.getCortexDecoderLibrary().activateLicense(actCode);
            if (MiscUtil.DEBUG)
                Log.d(TAG, "initCortexBarcode activate license start");
            actatveState = ACTIVATING;
        }

        /**
         * activie state change
         */
        if(mActivateCallback != null)
            mActivateCallback.onActivateStateChange(actatveState);//状态回调
        /**
         * 防止网络状态不佳，ACTIVATING一直没有复位，
         */
        mHandler.removeCallbacks(activateCortexBarcodeRunnable);
        if(actatveState == ACTIVATING) {
            mHandler.postDelayed(activateCortexBarcodeRunnable, 10000);
        }
    }

    /**
     * 本地加载注册码
     * @return
     */
    private boolean loadLicenseFile() {
        if (MiscUtil.DEBUG)
            Log.d(TAG, "loadLicenseFile.");
        try {
            File file = new File(Environment.getExternalStorageDirectory().getPath() + "/LicenseFile.V2C");

            if (!file.exists()) {
                //may changed name by system
                file = new File(Environment.getExternalStorageDirectory().getPath() + "/LicenseFi");
                if (!file.exists()) {
                    if (MiscUtil.DEBUG)
                        Log.d(TAG, "loadLicenseFile LicenseFile.v2c is not exists.");
                    return false;
                }
            }
            InputStream is = new FileInputStream(file);
            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer v2C = new StringBuffer();
            while ((line = r.readLine()) != null) {
                v2C.append(line).append('\n');
            }
            line = v2C.toString();
            Log.d(TAG, line);
            if(mDecoder.getCortexDecoderLibrary() != null)
                mDecoder.getCortexDecoderLibrary().loadLicenseFile(line);
            return true;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return false;
    }

    /**
     * 注册返回状态
     * @param statusCode
     */
    @Override
    public void onActivationResult(LicenseStatusCode statusCode) {
        if (MiscUtil.DEBUG)
            Log.d(TAG, "onActivationResult:" + statusCode);
        mHandler.removeCallbacks(activateCortexBarcodeRunnable);
        switch (statusCode) {
            case LicenseStatus_LicenseValid:
                actatveState = ACTIVATIED;
                if (MiscUtil.DEBUG)
                    Log.d(TAG, "onActivationResult License Activated.");
                break;
            default:
                actatveState = UNACTIVATIED;
                if (MiscUtil.DEBUG)
                    Log.d(TAG, "onActivationResult License Invalid.");
                break;
        }
        if(mActivateCallback != null)
            mActivateCallback.onActivateStateChange(actatveState);//状态回调
    }

    /**
     * generateDeviceID()调用返回结果，用于生成设备信息C2V
     * @param resultCode
     * @param data
     */
    @Override
    public void onDeviceIDResult(int resultCode, String data) {
        final String path = Environment.getExternalStorageDirectory().getPath() + "/DeviceIDlatest.c2v";
        if (resultCode == 0) {
            try {
                File file = new File(path);
                if (file.exists()) {
                    if (MiscUtil.DEBUG)
                        Log.d(TAG, "onDeviceIDResult DeviceIDlatest.v2c is exists.");
                } else {
                    FileOutputStream out = new FileOutputStream(file);
                    out.write(data.getBytes());
                    out.close();
                }
            } catch (IOException e) {
                if(mActivateCallback != null)
                    mActivateCallback.onDeviceIDFileGenerated(resultCode,"");//状态回调
                Log.e(TAG, e.getMessage());
                return;
            }
        }
        if(mActivateCallback != null)
            mActivateCallback.onDeviceIDFileGenerated(resultCode,path);//状态回调
    }
    /**
     * 返回注册状态
     * @return
     */
    public int getActivateState(){
        return actatveState;
    }

    /**
     * 注册状态超时复位
     */
    private Runnable activateCortexBarcodeRunnable = new Runnable() {
        public void run() {
            if (MiscUtil.DEBUG)
                Log.d(TAG, "activateCortexBarcodeRunnable.");
            if (actatveState == ACTIVATING) {
                actatveState = UNACTIVATIED;
            }
        }
    };
}
