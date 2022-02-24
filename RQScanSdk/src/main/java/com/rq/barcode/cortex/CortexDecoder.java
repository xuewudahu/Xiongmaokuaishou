package com.rq.barcode.cortex;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.codecorp.camera.Resolution;
import com.codecorp.decoder.CortexDecoderLibrary;
import com.codecorp.decoder.CortexDecoderLibraryCallback;
import com.codecorp.symbology.Symbologies;
import com.codecorp.symbology.SymbologyType;
import com.codecorp.util.Codewords;
import com.rq.barcode.ActivateTool;
import com.rq.barcode.RqDecoder;
import com.rq.barcode.RqEngineer;
import com.rq.barcode.RqSymbologyConfig;
import com.rq.barcode.RqSymbologyConfigValue;
import com.rq.barcode.RqSymbologyType;
import com.rq.camera.Frame;
import com.rq.camera.RqCameraManager;
import com.rq.misc.MiscUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.RequiresApi;

import static com.codecorp.symbology.Symbologies.Code11PropertiesChecksum.Code11PropertiesChecksum_Disabled;

/**
 * @deprecated
 */
public class CortexDecoder extends RqDecoder
        implements ActivateTool.ActivateCallback, CortexDecoderLibraryCallback {

    private static final String TAG = MiscUtil.getTag(CortexDecoder.class);

    /**
     * 关键属性
     */
    private CortexDecoderLibrary mCortexDecoderLibrary;
    private ActivateTool mActiviteTool;
    private List<ResultCallback> mResultCallbacks = new ArrayList<ResultCallback>();
    private Context mContext;

    /**
     * 扫描相关参数
     */
    //默认多条码数量
    private int numberOfCodes = 1;

    /*
     * 稳定图像方案
     */
    private volatile String lastResult = "";


    /**
     * 构造函数
     * @param context
     */
    public CortexDecoder(Context context){
        super(context);
        mContext = context;
        /**
         * 初始化解码库核心类
         */
        mCortexDecoderLibrary = CortexDecoderLibrary.sharedObject(mContext, "nocamera");
        /**
         * 注册工具类初始化并注册
         */
        mActiviteTool = new CortexActivateTool(mContext, this);
        /**
         * 因为注册需要时间，并且只需要注册一次，初始化时直接进行注册
         */
        mActiviteTool.setActivateCallback(this);
        mActiviteTool.activateLicense(null);
    }

    /**
     * 初始化参数配置
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public int initialize() {
        if(mActiviteTool == null) {
            if(MiscUtil.DEBUG)
                Log.i(TAG,"initialize failed, unActivited");
            return MiscUtil.ERROR_NULL_POINT;
        }

        if(mActiviteTool.getActivateState() != ActivateTool.ACTIVATIED) {
            if(MiscUtil.DEBUG)
                Log.i(TAG,"initialize failed, unActivited");
            return MiscUtil.ERROR_UNACTIVATED;
        }
        /**
         * 如果被销毁过，需要重新初始化
         */
        if(mCortexDecoderLibrary == null) {
            mCortexDecoderLibrary = CortexDecoderLibrary.sharedObject(mContext, "nocamera");
        }
        /**
         * 参数配置
         */
        configCortexBarcode();

        return MiscUtil.NO_ERROR;
    }

    /**
     * 开始解码
     * @param frame
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public int startDecode(Frame frame) {
        if(frame == null
                || mCortexDecoderLibrary == null) {
            if(MiscUtil.DEBUG)
                Log.e(TAG, "startDecode error: frame is null.");
            return MiscUtil.ERROR_NULL_POINT;
        }

        /**
         * yuv图像缓存
         */
        ByteBuffer pixBuf = ByteBuffer.allocateDirect(
                RqCameraManager.DecodePreviewWidth * RqCameraManager.DecodePreviewHeight * 3 / 2);
        byte[] data = MiscUtil.getFrameData(frame);
        if(data == null) {
            return MiscUtil.ERROR_NULL_DATA;
        }
        pixBuf.put(data);

        /**
         * 解码
         */
        mCortexDecoderLibrary.doDecode(pixBuf, RqCameraManager.DecodePreviewWidth,
                RqCameraManager.DecodePreviewHeight, RqCameraManager.DecodePreviewWidth);
        return MiscUtil.NO_ERROR;
    }

    /**
     * 停止解码
     * @return
     */
    @Override
    public int stopDecode() {
        if(mCortexDecoderLibrary == null) {
            if(MiscUtil.DEBUG)
                Log.e(TAG, "stopDecode error: mCortexDecoderLibrary is null.");
            return MiscUtil.ERROR_NULL_POINT;
        }
        //stopDecode just wait last frame decode finish, nothing to do.
        return MiscUtil.NO_ERROR;
    }

    @Override
    public int destroy() {
        if(mCortexDecoderLibrary == null) {
            if(MiscUtil.DEBUG)
                Log.e(TAG, "destroy error: mCortexDecoderLibrary is null.");
            return MiscUtil.ERROR_NULL_POINT;
        }
        mCortexDecoderLibrary.closeSharedObject();
        mCortexDecoderLibrary = null;
        mResultCallbacks.clear();

        return MiscUtil.NO_ERROR;
    }

    /**
     * 获取注册工具类
     * @return
     */
    @Override
    public ActivateTool getActivateTool() {
        return mActiviteTool;
    }

    /**
     * 添加结果回调
     * @param resultCallback
     */
    @Override
    public void addResultCallback(ResultCallback resultCallback) {
        synchronized (mResultCallbacks) {
            if (!mResultCallbacks.contains(resultCallback))
                mResultCallbacks.add(resultCallback);
        }
    }

    /**
     * 注销结果回调
     * @param resultCallback
     */
    @Override
    public void removeResultCallback(ResultCallback resultCallback){
        synchronized (mResultCallbacks) {
            if (mResultCallbacks.contains(resultCallback))
                mResultCallbacks.remove(resultCallback);
        }
    }

    /**
     * 设置多条码最大解码数
     * @param number
     */
    @Override
    public void setNumberOfBarcodesToDecode(int number) {
        if(mCortexDecoderLibrary == null) {
            if(MiscUtil.DEBUG)
                Log.e(TAG, "setNumberOfBarcodesToDecode error: mCortexDecoderLibrary is null.");
        }
        numberOfCodes = number;
        mCortexDecoderLibrary.setNumberOfBarcodesToDecode(number);
    }

    /**
     * 获取多条码最大解码数
     * @return
     */
    @Override
    public int getNumberOfBarcodesToDecode() {
        return numberOfCodes;
    }

    /**
     * 注册状态反馈
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onActivateStateChange(int newState) {
        if(newState == ActivateTool.ACTIVATIED) {
            initialize();
        }
    }


    /**
     * 设备信息获取回调
     */
    @Override
    public void onDeviceIDFileGenerated(int resultCode, String filePath) {
        if(resultCode == 0 && !TextUtils.isEmpty(filePath)) {
            if(MiscUtil.DEBUG)
                Log.i(TAG,"DeviceID File  Generated SUCCESS! path:"+filePath);
        }
    }

    /**
     * 扫描结果回调
     * @param string
     * @param symbologyType
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void receivedDecodedData(String string, SymbologyType symbologyType) {
        /**
         * 稳定图像方案，只针对存图模式，并且单解码
         */
        if(RqEngineer.getInstence(mContext).getFrameSaver().isSavingMode()
                && !lastResult.equals(string)) {
            lastResult = string;
            synchronized (mResultCallbacks) {
                for (ResultCallback resultCallback : mResultCallbacks) {
                    resultCallback.onDecodeError(RqDecoder.DECODE_ERROR_FAILED);
                }
            }
            return;
        }

        List<int[]> corners = mCortexDecoderLibrary.getBarcodeCornersArray();
        if(MiscUtil.DEBUG)
            Log.i(TAG,"receivedMultipleDecodedData string="+string + " corners.size="+corners.size());

        synchronized (mResultCallbacks) {
            for (ResultCallback resultCallback : mResultCallbacks) {
                resultCallback.onResultCallback(string, RqSymbologyType.getOrderStatusEnum(
                        RqEngineer.getInstence(mContext).getDecodeProgram(),
                        symbologyType.toString()), corners.size()>0?corners.get(0):null);
            }
        }

    }

    /**
     * 回调处理
     * @param strings
     * @param symbologyTypes
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void receivedMultipleDecodedData(String[] strings, SymbologyType[] symbologyTypes) {
        List<int[]> corners = mCortexDecoderLibrary.getBarcodeCornersArray();
        if(MiscUtil.DEBUG)
            Log.i(TAG,"receivedMultipleDecodedData length="+strings.length+" corners.size="+corners.size());
        RqSymbologyType[] rqSymbologyTypes = new RqSymbologyType[symbologyTypes.length];
        for (int i = 0; i < symbologyTypes.length; i++) {
            rqSymbologyTypes[i] = RqSymbologyType.getOrderStatusEnum(
                    RqEngineer.getInstence(mContext).getDecodeProgram(),
                    symbologyTypes[i].toString());
        }
        synchronized (mResultCallbacks) {
            for (ResultCallback resultCallback : mResultCallbacks) {
                resultCallback.onMultipleResultCallback(strings, rqSymbologyTypes, corners);
            }
        }
    }

    @Override
    public void receiveBarcodeCorners(int[] ints) {
        if(MiscUtil.DEBUG)
            Log.i(TAG,"receiveBarcodeCorners.");
    }

    @Override
    public void receiveMultipleBarcodeCorners(List<int[]> list) {
        if(MiscUtil.DEBUG)
            Log.i(TAG,"receiveMultipleBarcodeCorners size="+list.size());

    }

    @Override
    public void receivedDecodedCodewordsData(Codewords codewords) {
        if(MiscUtil.DEBUG)
            Log.i(TAG,"receivedDecodedCodewordsData.");
    }

    @Override
    public void barcodeDecodeFailed(boolean b) {
        if(MiscUtil.DEBUG)
            Log.i(TAG,"barcodeDecodeFailed b="+b);
        if(b) {
            synchronized (mResultCallbacks) {
                for (ResultCallback resultCallback : mResultCallbacks) {
                    resultCallback.onDecodeError(RqDecoder.DECODE_ERROR_FAILED);
                }
            }
            //稳定图像方案
            lastResult = "";
        }
    }

    @Override
    public void multiFrameDecodeCount(int i) {
        if(MiscUtil.DEBUG)
            Log.i(TAG,"multiFrameDecodeCount i="+i);
    }


    /**
     * 配置扫描库参数
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void configCortexBarcode() {
        Log.d(TAG, " configCortexBarcode");
        if(mCortexDecoderLibrary == null) {
            Log.d(TAG, " mCortexDecoderLibrary not init");
            return;
        }
        mCortexDecoderLibrary.enableVibrateOnScan(false);
        mCortexDecoderLibrary.enableBeepPlayer(false);
        mCortexDecoderLibrary.lowContrastDecodingEnabled(true);
        //mCortexDecoderLibrary.ensureRegionOfInterest(true);  //TODO
        //mCortexDecoderLibrary.regionOfInterestHeight(1000/*RqCameraManager.DecodePreviewHeight*/);   //800  720
        //mCortexDecoderLibrary.regionOfInterestWidth(1000/*RqCameraManager.DecodePreviewWidth*/);  //1280   960
        //mCortexDecoderLibrary.regionOfInterestLeft(0);
        //mCortexDecoderLibrary.regionOfInterestTop(0);
        //mCortexDecoderLibrary.ensureRegionOfInterest(true);
        //mCortexDecoderLibrary.enableROIDecoding(true);
        //mCortexDecoderLibrary.setRegionOfInterest(0,0,200,200,false);
//        mCortexDecoderLibrary.CRD_Set(306, 200);//left
//        mCortexDecoderLibrary.CRD_Set(307, 200);//right
//        mCortexDecoderLibrary.CRD_Set(308, 1000);//width
//        mCortexDecoderLibrary.CRD_Set(309, 1000);//height
        mCortexDecoderLibrary.decoderTimeLimitInMilliseconds(150);   //超时时间 30
        mCortexDecoderLibrary.setNumberOfBarcodesToDecode(numberOfCodes);//added by eric-zhao for multi-barcodes
        mCortexDecoderLibrary.setExactlyNBarcodes(false);  //for multibar
        mCortexDecoderLibrary.setCallback(this);
    }


    /**
     * 提供其他对象复用核心属性
     * @return
     */
    public CortexDecoderLibrary getCortexDecoderLibrary() {
        return mCortexDecoderLibrary;
    }

    /**
     * 提供其他对象复用核心属性
     * @return
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * 常用二维码开关,涉及如下条码
     * Aztec,PDF417,MicroPDF417,QR,MicroQR,MaxiCode,GridMatrix,DataMatrix,HanXin
     * @param enable
     */
    public void set2DBarcodesEnable(boolean enable) {
        Symbologies.AztecProperties aztec = new Symbologies.AztecProperties();
        aztec.setEnabled(mContext, enable);
        aztec.setMirrorDecodingEnabled(mContext, enable);
        aztec.setPolarity(mContext, Symbologies.AztecPropertiesPolarity.AztecPropertiesPolarity_Either);

        Symbologies.PDF417Properties pdf417 = new Symbologies.PDF417Properties();
        pdf417.setEnabled(mContext, enable);

        Symbologies.MicroPDF417Properties microPdf = new Symbologies.MicroPDF417Properties();
        microPdf.setEnabled(mContext, enable);

        Symbologies.QRProperties qr = new Symbologies.QRProperties();
        qr.setEnabled(mContext, enable);
        qr.setModel1DecodingEnabled(mContext, enable);
        qr.setMirrorDecodingEnabled(mContext, enable);
        qr.setPolarity(mContext, Symbologies.QRPropertiesPolarity.QRPropertiesPolarity_Either);

        Symbologies.MicroQRProperties microQR = new Symbologies.MicroQRProperties();
        microQR.setEnabled(mContext, enable);

        Symbologies.MaxiCodeProperties maxi = new Symbologies.MaxiCodeProperties();
        maxi.setEnabled(mContext, enable);

        Symbologies.GridMatrixProperties gridMatrix = new Symbologies.GridMatrixProperties();
        gridMatrix.setEnabled(mContext, enable);
        gridMatrix.setMirrorDecodingEnabled(mContext, enable);
        gridMatrix.setPolarity(mContext, Symbologies.GridMatrixPropertiesPolarity.GridMatrixPropertiesPolarity_Either);

        Symbologies.DataMatrixProperties dataMatrix = new Symbologies.DataMatrixProperties();
        dataMatrix.setEnabled(mContext, enable);
        dataMatrix.setExtendedRectEnabled(mContext, enable);
        dataMatrix.setMirrorDecodingEnabled(mContext, enable);
        dataMatrix.setPolarity(mContext, Symbologies.DataMatrixPropertiesPolarity.DataMatrixPropertiesPolarity_Either);

        Symbologies.HanXinCodeProperties hanxin = new Symbologies.HanXinCodeProperties();
        hanxin.setEnabled(mContext, enable);
    }

    @Override
    public void addResultCallback() {

    }

    /**
     * 从条码库中获取默认的条码配置
     * @param key
     * @return SharedPreferences
     */
    public SharedPreferences pullBarcodePreference(String key) {
        if(TextUtils.isEmpty(key)) {
            if(MiscUtil.DEBUG)
                Log.e(TAG, "pushBarcodePreference error: key or sharedPreferences is null. ");
            return null;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        /**
         * 默认赋值为false的开光状态获取
         */
        Log.d("wxwLL","---"+key);
        boolean enabled = sharedPreferences.getBoolean(key, false);
        if (key.equals(RqSymbologyType.SymbologyType_Aztec.getSampleName())) {
            Symbologies.AztecProperties p = new Symbologies.AztecProperties();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.apply();

        } else if (key.equals(RqSymbologyType.SymbologyType_Code11.getSampleName())) {
            Symbologies.Code11Properties p = new Symbologies.Code11Properties();
            String checkSumSaveValue = RqSymbologyConfigValue
                    .Code11.Code11PropertiesChecksum_Disabled;
            String checkSum = p.getChecksumProperties(mContext).name();
            if(checkSum.equals(
                    Symbologies.Code11PropertiesChecksum.Code11PropertiesChecksum_Disabled.name())) {
                if (!p.isStripChecksumEnabled(mContext)) {
                    checkSumSaveValue =  RqSymbologyConfigValue
                            .Code11.Code11PropertiesChecksum_Disabled;
                }
            } else if(checkSum.equals(
                    Symbologies.Code11PropertiesChecksum.Code11PropertiesChecksum_Enabled1Digit.name())) {
                if (!p.isStripChecksumEnabled(mContext)) {
                    checkSumSaveValue =  RqSymbologyConfigValue
                            .Code11.Code11PropertiesChecksum_Disabled1Digit;
                } else {
                    checkSumSaveValue =  RqSymbologyConfigValue
                            .Code11.Code11PropertiesChecksum_Enabled1Digit;
                }
            } else if(checkSum.equals(
                    Symbologies.Code11PropertiesChecksum.Code11PropertiesChecksum_Enabled2Digit.name())) {
                if (!p.isStripChecksumEnabled(mContext)) {
                    checkSumSaveValue =  RqSymbologyConfigValue
                            .Code11.Code11PropertiesChecksum_Disabled2Digit;
                } else {
                    checkSumSaveValue =  RqSymbologyConfigValue
                            .Code11.Code11PropertiesChecksum_Enabled2Digit;
                }
            }

            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.putString(RqSymbologyType.SymbologyType_Code11.getSampleName()
                    + RqSymbologyConfig.CHECKSUM_SUFFIX,checkSumSaveValue);
            e.apply();

        } else if (key.equals(RqSymbologyType.SymbologyType_Code128_CCA.getSampleName())) {
            Symbologies.Code128Properties p = new Symbologies.Code128Properties();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.putInt(RqSymbologyType.SymbologyType_Code128_CCA.getSampleName()
                    + RqSymbologyConfig.MIN_CHARS_SUFFIX,p.getMinChars(mContext));
            e.apply();

        } else if (key.equals(RqSymbologyType.SymbologyType_Code32.getSampleName())) {
            Symbologies.Code32Properties p = new Symbologies.Code32Properties();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_Code39.getSampleName())) {
            Symbologies.Code39Properties p = new Symbologies.Code39Properties();

            String checkSumSaveValue = RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled;
            String checkSum = p.getChecksumProperties(mContext).name();
            if(checkSum.equals(
                    Symbologies.Code39PropertiesChecksum.Code39PropertiesChecksum_Disabled.name())) {
                checkSumSaveValue =  RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled;
            } else if(checkSum.equals(
                    Symbologies.Code39PropertiesChecksum.Code39PropertiesChecksum_Enabled.name())) {
                checkSumSaveValue =  RqSymbologyConfigValue.NormalCheckSum.Checksum_Enabled;
            } else if(checkSum.equals(
                    Symbologies.Code39PropertiesChecksum.Code39PropertiesChecksum_EnabledStripCheckCharacter.name())) {
                checkSumSaveValue =  RqSymbologyConfigValue.NormalCheckSum.Checksum_EnabledStripCheckCharacter;
            }

            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.putString(RqSymbologyType.SymbologyType_Code39.getSampleName()
                    + RqSymbologyConfig.CHECKSUM_SUFFIX,checkSumSaveValue);
            e.putInt(RqSymbologyType.SymbologyType_Code39.getSampleName()
                    + RqSymbologyConfig.MIN_CHARS_SUFFIX,p.getMinChars(mContext));
            e.putBoolean(RqSymbologyType.SymbologyType_Code39.getSampleName()
                    + RqSymbologyConfig.ASCII_SUPPORT_SUFFIX,p.isAsciiModeEnabled(mContext));
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_Code49.getSampleName())) {
            Symbologies.Code49Properties p = new Symbologies.Code49Properties();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_Code93.getSampleName())) {
            Symbologies.Code93Properties p = new Symbologies.Code93Properties();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putInt(RqSymbologyType.SymbologyType_Code93.getSampleName()
                    + RqSymbologyConfig.MIN_CHARS_SUFFIX,p.getMinChars(mContext));
            e.putBoolean(key, p.isEnabled(mContext));
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_Codabar.getSampleName())) {
            Symbologies.CodabarProperties p = new Symbologies.CodabarProperties();

            String checkSumSaveValue = RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled;
            String checkSum = p.getChecksumProperties(mContext).name();
            if(checkSum.equals(
                    Symbologies.CodabarPropertiesChecksum.CodabarPropertiesChecksum_Disabled.name())) {
                checkSumSaveValue =  RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled;
            } else if(checkSum.equals(
                    Symbologies.CodabarPropertiesChecksum.CodabarPropertiesChecksum_Enabled.name())) {
                checkSumSaveValue =  RqSymbologyConfigValue.NormalCheckSum.Checksum_Enabled;
            } else if(checkSum.equals(
                    Symbologies.CodabarPropertiesChecksum.CodabarPropertiesChecksum_EnabledStripCheckCharacter.name())) {
                checkSumSaveValue =  RqSymbologyConfigValue.NormalCheckSum.Checksum_EnabledStripCheckCharacter;
            }

            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.putString(RqSymbologyType.SymbologyType_Codabar.getSampleName()
                    + RqSymbologyConfig.CHECKSUM_SUFFIX,checkSumSaveValue);
            e.putInt(RqSymbologyType.SymbologyType_Codabar.getSampleName()
                    + RqSymbologyConfig.MIN_CHARS_SUFFIX,p.getMinChars(mContext));
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_CodablockF.getSampleName())) {
            Symbologies.CodablockFProperties p = new Symbologies.CodablockFProperties();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_COMPOSITE.getSampleName())) {
            Symbologies.CompositeCodeProperties p = new Symbologies.CompositeCodeProperties();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_CCA.getSampleName())) {
            Symbologies.CompositeCodeProperties p = new Symbologies.CompositeCodeProperties();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.apply();
        }else if (key.equals(RqSymbologyType.SymbologyType_CCB.getSampleName())) {
            Symbologies.CompositeCodeProperties p = new Symbologies.CompositeCodeProperties();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.apply();
        }else if (key.equals(RqSymbologyType.SymbologyType_CCC.getSampleName())) {
            Symbologies.CompositeCodeProperties p = new Symbologies.CompositeCodeProperties();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.apply();
        }else if (key.equals(RqSymbologyType.SymbologyType_DataMatrix.getSampleName())) {
            Symbologies.DataMatrixProperties p = new Symbologies.DataMatrixProperties();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_EAN13.getSampleName())) {
            Symbologies.EAN13Properties p = new Symbologies.EAN13Properties();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.putBoolean(RqSymbologyType.SymbologyType_EAN13.getSampleName()+ RqSymbologyConfig.TWO_DIGIT_SUPP,
                    p.isSupplemental2DigitDecodingEnabled(mContext));
            e.putBoolean(RqSymbologyType.SymbologyType_EAN13.getSampleName()+ RqSymbologyConfig.FIVE_DIGIT_SUPP,
                    p.isSupplemental5DigitDecodingEnabled(mContext));
            e.putBoolean(RqSymbologyType.SymbologyType_EAN13.getSampleName()+ RqSymbologyConfig.ADD_SPACE,
                    p.isAddSpaceEnabled(mContext));
            e.putBoolean(RqSymbologyType.SymbologyType_EAN13.getSampleName()+ RqSymbologyConfig.REQUIRE_SUPP,
                    p.isRequireSupplemental(mContext));
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_EAN8.getSampleName())) {
            Symbologies.EAN8Properties p = new Symbologies.EAN8Properties();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.putBoolean(RqSymbologyType.SymbologyType_EAN8.getSampleName()+ RqSymbologyConfig.TWO_DIGIT_SUPP,
                    p.isSupplemental2DigitDecodingEnabled(mContext));
            e.putBoolean(RqSymbologyType.SymbologyType_EAN8.getSampleName()+ RqSymbologyConfig.FIVE_DIGIT_SUPP,
                    p.isSupplemental5DigitDecodingEnabled(mContext));
            e.putBoolean(RqSymbologyType.SymbologyType_EAN8.getSampleName()+ RqSymbologyConfig.ADD_SPACE,
                    p.isAddSpaceEnabled(mContext));
            e.putBoolean(RqSymbologyType.SymbologyType_EAN8.getSampleName()+ RqSymbologyConfig.REQUIRE_SUPP,
                    p.isRequireSupplemental(mContext));
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_GridMatrix.getSampleName())) {
            Symbologies.GridMatrixProperties p = new Symbologies.GridMatrixProperties();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_DOT.getSampleName())) {
            Symbologies.DotCodeProperties p = new Symbologies.DotCodeProperties();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_GS1_DATABAR.getSampleName())) {
            Symbologies.GS1DataBar14Properties p = new Symbologies.GS1DataBar14Properties();

            SharedPreferences.Editor e = sharedPreferences.edit();
            //e.putBoolean(key, p.isEnabled(mContext));
            e.putBoolean(RqSymbologyType.SymbologyType_GS1_DATABAR.getSampleName()+ RqSymbologyConfig.OMNI,
                    p.isOmniTruncatedDecodingEnabled(mContext));
            e.putBoolean(RqSymbologyType.SymbologyType_GS1_DATABAR.getSampleName()+ RqSymbologyConfig.STACKED_OMNI,
                    p.isStackedDecodingEnabled(mContext));
            e.putBoolean(RqSymbologyType.SymbologyType_GS1_DATABAR.getSampleName()+ RqSymbologyConfig.LIMITED,
                    p.isLimitedDecodingEnabled(mContext));
            e.putBoolean(RqSymbologyType.SymbologyType_GS1_DATABAR.getSampleName()+ RqSymbologyConfig.EXPANDED,
                    p.isExpandedDecodingEnabled(mContext));
            e.putBoolean(RqSymbologyType.SymbologyType_GS1_DATABAR.getSampleName()+ RqSymbologyConfig.EXPANDED_STACKED,
                    p.isExpandedStackDecodingEnabled(mContext));
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_HanXin.getSampleName())) {
            Symbologies.HanXinCodeProperties p = new Symbologies.HanXinCodeProperties();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_HongKong2of5.getSampleName())) {
            Symbologies.HongKong2of5Properties p = new Symbologies.HongKong2of5Properties();

            String checkSumSaveValue = RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled;
            String checkSum = p.getChecksumProperties(mContext).name();
            if(checkSum.equals(
                    Symbologies.Symbology2of5PropertiesChecksum.Checksum_Disabled.name())) {
                checkSumSaveValue =  RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled;
            } else if(checkSum.equals(
                    Symbologies.Symbology2of5PropertiesChecksum.Checksum_Enabled.name())) {
                checkSumSaveValue =  RqSymbologyConfigValue.NormalCheckSum.Checksum_Enabled;
            } else if(checkSum.equals(
                    Symbologies.Symbology2of5PropertiesChecksum.Checksum_EnabledStripCheckCharacter.name())) {
                checkSumSaveValue =  RqSymbologyConfigValue.NormalCheckSum.Checksum_EnabledStripCheckCharacter;
            }

            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.putString(RqSymbologyType.SymbologyType_HongKong2of5.getSampleName()
                    + RqSymbologyConfig.CHECKSUM_SUFFIX,checkSumSaveValue);
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_IATA2of5.getSampleName())) {
            Symbologies.IATA2of5Properties p = new Symbologies.IATA2of5Properties();

            String checkSumSaveValue = RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled;
            String checkSum = p.getChecksumProperties(mContext).name();
            if(checkSum.equals(
                    Symbologies.Symbology2of5PropertiesChecksum.Checksum_Disabled.name())) {
                checkSumSaveValue =  RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled;
            } else if(checkSum.equals(
                    Symbologies.Symbology2of5PropertiesChecksum.Checksum_Enabled.name())) {
                checkSumSaveValue =  RqSymbologyConfigValue.NormalCheckSum.Checksum_Enabled;
            } else if(checkSum.equals(
                    Symbologies.Symbology2of5PropertiesChecksum.Checksum_EnabledStripCheckCharacter.name())) {
                checkSumSaveValue =  RqSymbologyConfigValue.NormalCheckSum.Checksum_EnabledStripCheckCharacter;
            }

            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.putString(RqSymbologyType.SymbologyType_IATA2of5.getSampleName()
                    + RqSymbologyConfig.CHECKSUM_SUFFIX,checkSumSaveValue);
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_Interleaved2of5.getSampleName())) {
            Symbologies.Interleaved2of5Properties p = new Symbologies.Interleaved2of5Properties();

            String checkSumSaveValue = RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled;
            String checkSum = p.getChecksumProperties(mContext).name();
            if(checkSum.equals(
                    Symbologies.Interleaved2of5PropertiesChecksum.Interleaved2of5PropertiesChecksum_Disabled.name())) {
                checkSumSaveValue =  RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled;
            } else if(checkSum.equals(
                    Symbologies.Interleaved2of5PropertiesChecksum.Interleaved2of5PropertiesChecksum_Enabled.name())) {
                checkSumSaveValue =  RqSymbologyConfigValue.NormalCheckSum.Checksum_Enabled;
            } else if(checkSum.equals(
                    Symbologies.Interleaved2of5PropertiesChecksum.Interleaved2of5PropertiesChecksum_EnabledStripCheckCharacter.name())) {
                checkSumSaveValue =  RqSymbologyConfigValue.NormalCheckSum.Checksum_EnabledStripCheckCharacter;
            }

            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.putString(RqSymbologyType.SymbologyType_Interleaved2of5.getSampleName()
                    + RqSymbologyConfig.CHECKSUM_SUFFIX,checkSumSaveValue);

            e.putBoolean(RqSymbologyType.SymbologyType_Interleaved2of5.getSampleName()
                    + RqSymbologyConfig.REJECT_PARTIALDECODE,p.isRejectPartialDecode(mContext));
            e.putBoolean(RqSymbologyType.SymbologyType_Interleaved2of5.getSampleName()
                    + RqSymbologyConfig.ALLOW_SHORTQUIETZONE,p.isAllowShortQuietZone(mContext));
            e.putInt(RqSymbologyType.SymbologyType_Interleaved2of5.getSampleName()
                    + RqSymbologyConfig.MIN_CHARS_SUFFIX,p.getMinChars(mContext));

            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_MAXICODE.getSampleName())) {
            Symbologies.MaxiCodeProperties p = new Symbologies.MaxiCodeProperties();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_Matrix2of5.getSampleName())) {
            Symbologies.Matrix2of5Properties p = new Symbologies.Matrix2of5Properties();

            String checkSumSaveValue = RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled;
            String checkSum = p.getChecksumProperties(mContext).name();
            if(checkSum.equals(
                    Symbologies.Symbology2of5PropertiesChecksum.Checksum_Disabled.name())) {
                checkSumSaveValue =  RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled;
            } else if(checkSum.equals(
                    Symbologies.Symbology2of5PropertiesChecksum.Checksum_Enabled.name())) {
                checkSumSaveValue =  RqSymbologyConfigValue.NormalCheckSum.Checksum_Enabled;
            } else if(checkSum.equals(
                    Symbologies.Symbology2of5PropertiesChecksum.Checksum_EnabledStripCheckCharacter.name())) {
                checkSumSaveValue =  RqSymbologyConfigValue.NormalCheckSum.Checksum_EnabledStripCheckCharacter;
            }

            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.putString(RqSymbologyType.SymbologyType_Matrix2of5.getSampleName()
                    + RqSymbologyConfig.CHECKSUM_SUFFIX,checkSumSaveValue);
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_MPDF.getSampleName())) {
            Symbologies.MicroPDF417Properties p = new Symbologies.MicroPDF417Properties();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_QRMicro.getSampleName())) {
            Symbologies.MicroQRProperties p = new Symbologies.MicroQRProperties();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_MSIPlessy.getSampleName())) {
            Symbologies.MSIPlesseyProperties p = new Symbologies.MSIPlesseyProperties();

            String checkSumSaveValue = RqSymbologyConfigValue.MSIPlessy.MSIPlesseyPropertiesChecksum_Disabled;
            String checkSum = p.getChecksumProperties(mContext).name();
            if(checkSum.equals(
                    Symbologies.MSIPlesseyPropertiesChecksum.MSIPlesseyPropertiesChecksum_Disabled.name())) {
                if(!p.isStripChecksumEnabled(mContext))
                    checkSumSaveValue =  RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled;
            } else if(checkSum.equals(
                    Symbologies.MSIPlesseyPropertiesChecksum.MSIPlesseyPropertiesChecksum_EnabledMod10.name())) {
                if(!p.isStripChecksumEnabled(mContext)) {
                    checkSumSaveValue =  RqSymbologyConfigValue.MSIPlessy.MSIPlesseyPropertiesChecksum_DisabledMod10;
                } else {
                    checkSumSaveValue =  RqSymbologyConfigValue.MSIPlessy.MSIPlesseyPropertiesChecksum_EnabledMod10;
                }
            }else if(checkSum.equals(
                    Symbologies.MSIPlesseyPropertiesChecksum.MSIPlesseyPropertiesChecksum_EnabledMod10_10.name())) {
                if(!p.isStripChecksumEnabled(mContext)) {
                    checkSumSaveValue =  RqSymbologyConfigValue.MSIPlessy.MSIPlesseyPropertiesChecksum_DisabledMod10_10;
                } else {
                    checkSumSaveValue =  RqSymbologyConfigValue.MSIPlessy.MSIPlesseyPropertiesChecksum_EnabledMod10_10;
                }
            }else if(checkSum.equals(
                    Symbologies.MSIPlesseyPropertiesChecksum.MSIPlesseyPropertiesChecksum_EnabledMod11_10.name())) {
                if(!p.isStripChecksumEnabled(mContext)) {
                    checkSumSaveValue =  RqSymbologyConfigValue.MSIPlessy.MSIPlesseyPropertiesChecksum_DisabledMod11_10;
                } else {
                    checkSumSaveValue =  RqSymbologyConfigValue.MSIPlessy.MSIPlesseyPropertiesChecksum_EnabledMod11_10;
                }
            }

            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.putString(RqSymbologyType.SymbologyType_MSIPlessy.getSampleName()
                    + RqSymbologyConfig.CHECKSUM_SUFFIX,checkSumSaveValue);
            e.putInt(RqSymbologyType.SymbologyType_MSIPlessy.getSampleName() + RqSymbologyConfig.MIN_CHARS_SUFFIX,
                    p.getMinChars(mContext));
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_NEC2of5.getSampleName())) {
            Symbologies.NEC2of5Properties p = new Symbologies.NEC2of5Properties();
            String checkSumSaveValue = RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled;
            String checkSum = p.getChecksumProperties(mContext).name();
            if(checkSum.equals(
                    Symbologies.Symbology2of5PropertiesChecksum.Checksum_Disabled.name())) {
                checkSumSaveValue =  RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled;
            } else if(checkSum.equals(
                    Symbologies.Symbology2of5PropertiesChecksum.Checksum_Enabled.name())) {
                checkSumSaveValue =  RqSymbologyConfigValue.NormalCheckSum.Checksum_Enabled;
            } else if(checkSum.equals(
                    Symbologies.Symbology2of5PropertiesChecksum.Checksum_EnabledStripCheckCharacter.name())) {
                checkSumSaveValue =  RqSymbologyConfigValue.NormalCheckSum.Checksum_EnabledStripCheckCharacter;
            }

            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.putString(RqSymbologyType.SymbologyType_NEC2of5.getSampleName()
                    + RqSymbologyConfig.CHECKSUM_SUFFIX,checkSumSaveValue);
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_PDF417.getSampleName())) {
            Symbologies.PDF417Properties p = new Symbologies.PDF417Properties();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_Plessy.getSampleName())) {
            Symbologies.PlesseyProperties p = new Symbologies.PlesseyProperties();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_QR.getSampleName())) {
            Symbologies.QRProperties p = new Symbologies.QRProperties();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_Straight2of5.getSampleName())) {
            Symbologies.Straight2of5Properties p = new Symbologies.Straight2of5Properties();

            String checkSumSaveValue = RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled;
            String checkSum = p.getChecksumProperties(mContext).name();
            if(checkSum.equals(
                    Symbologies.Symbology2of5PropertiesChecksum.Checksum_Disabled.name())) {
                checkSumSaveValue =  RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled;
            } else if(checkSum.equals(
                    Symbologies.Symbology2of5PropertiesChecksum.Checksum_Enabled.name())) {
                checkSumSaveValue =  RqSymbologyConfigValue.NormalCheckSum.Checksum_Enabled;
            } else if(checkSum.equals(
                    Symbologies.Symbology2of5PropertiesChecksum.Checksum_EnabledStripCheckCharacter.name())) {
                checkSumSaveValue =  RqSymbologyConfigValue.NormalCheckSum.Checksum_EnabledStripCheckCharacter;
            }

            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.putString(RqSymbologyType.SymbologyType_Straight2of5.getSampleName()
                    + RqSymbologyConfig.CHECKSUM_SUFFIX,checkSumSaveValue);
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_Telepen.getSampleName())) {
            Symbologies.TelepenProperties p = new Symbologies.TelepenProperties();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_Trioptic.getSampleName())) {
            Symbologies.TriopticProperties p = new Symbologies.TriopticProperties();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_UPCA.getSampleName())) {
            Symbologies.UPCAProperties p = new Symbologies.UPCAProperties();

            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.putBoolean(RqSymbologyType.SymbologyType_UPCA.getSampleName()
                    + RqSymbologyConfig.TWO_DIGIT_SUPP,p.isSupplemental2DigitDecodingEnabled(mContext));
            e.putBoolean(RqSymbologyType.SymbologyType_UPCA.getSampleName()
                    + RqSymbologyConfig.FIVE_DIGIT_SUPP,p.isSupplemental5DigitDecodingEnabled(mContext));
            e.putBoolean(RqSymbologyType.SymbologyType_UPCA.getSampleName()
                    + RqSymbologyConfig.ADD_SPACE,p.isAddSpaceEnabled(mContext));
            e.putBoolean(RqSymbologyType.SymbologyType_UPCA.getSampleName()
                    + RqSymbologyConfig.REQUIRE_SUPP,p.isRequireSupplemental(mContext));
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_UPCE.getSampleName())) {
            Symbologies.UPCEProperties p = new Symbologies.UPCEProperties();

            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.putBoolean(RqSymbologyType.SymbologyType_UPCE.getSampleName()
                    + RqSymbologyConfig.TWO_DIGIT_SUPP,p.isSupplemental2DigitDecodingEnabled(mContext));
            e.putBoolean(RqSymbologyType.SymbologyType_UPCE.getSampleName()
                    + RqSymbologyConfig.FIVE_DIGIT_SUPP,p.isSupplemental5DigitDecodingEnabled(mContext));
            e.putBoolean(RqSymbologyType.SymbologyType_UPCE.getSampleName()
                    + RqSymbologyConfig.ADD_SPACE,p.isAddSpaceEnabled(mContext));
            e.putBoolean(RqSymbologyType.SymbologyType_UPCE.getSampleName()
                    + RqSymbologyConfig.REQUIRE_SUPP,p.isRequireSupplemental(mContext));
            e.putBoolean(RqSymbologyType.SymbologyType_UPCE.getSampleName()
                    + RqSymbologyConfig.ENABLE_EXPANSION,p.isExpansionEnabled(mContext));
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_AustraliaPost.getSampleName())) {
            Symbologies.AustraliaPostProperties p = new Symbologies.AustraliaPostProperties();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_CanadaPost.getSampleName())) {
            Symbologies.CanadaPostProperties p = new Symbologies.CanadaPostProperties();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_DutchPost.getSampleName())) {
            Symbologies.DutchPostProperties p = new Symbologies.DutchPostProperties();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_JapanMail.getSampleName())) {
            Symbologies.JapanPostProperties p = new Symbologies.JapanPostProperties();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_KoreaPost.getSampleName())) {
            Symbologies.KoreaPostProperties p = new Symbologies.KoreaPostProperties();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_RoyalMail.getSampleName())) {
            Symbologies.RoyalMailProperties p = new Symbologies.RoyalMailProperties();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_USPSIntelligentMail.getSampleName())) {
            Symbologies.USPSIntelligentMailProperties p = new Symbologies.USPSIntelligentMailProperties();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_USPSPlanet.getSampleName())) {
            Symbologies.USPSPlanetProperties p = new Symbologies.USPSPlanetProperties();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_USPSPostnet.getSampleName())) {
            Symbologies.USPSPostnetProperties p = new Symbologies.USPSPostnetProperties();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.apply();
        } else if (key.equals(RqSymbologyType.SymbologyType_UPU.getSampleName())) {
            Symbologies.UPUProperties p = new Symbologies.UPUProperties();
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putBoolean(key, p.isEnabled(mContext));
            e.apply();
        } else {
            Log.e(TAG, "Unknown preference key" + key);
            return null;
        }
        return sharedPreferences;
    }

    /**
     * 设置条码开关，checksum等属性
     * @param key
     * @param sharedPreferences
     * @return
     */
    public int pushBarcodePreference(String key, SharedPreferences sharedPreferences) {
        if(sharedPreferences == null || TextUtils.isEmpty(key)) {
            if(MiscUtil.DEBUG)
                Log.e(TAG, "pushBarcodePreference error: key or sharedPreferences is null. ");
            return MiscUtil.ERROR_NULL_POINT;
        }

        /**
         * 默认赋值为false的开光状态获取
         */
        boolean enabled = sharedPreferences.getBoolean(key, false);
        if (key.equals(RqSymbologyType.SymbologyType_Aztec.getSampleName())) {
            boolean enabledAztec = sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_Aztec.getSampleName(),
                    true);
            Symbologies.AztecProperties p = new Symbologies.AztecProperties();
            p.setEnabled(mContext, enabledAztec);
            p.setMirrorDecodingEnabled(mContext, enabledAztec);
            p.setPolarity(mContext, Symbologies.AztecPropertiesPolarity.AztecPropertiesPolarity_Either);
        } else if (key.equals(RqSymbologyType.SymbologyType_Code11.getSampleName())) {
            boolean enabledCode11 = sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_Code11.getSampleName(),
                    true);
            Symbologies.Code11Properties p = new Symbologies.Code11Properties();
            p.setEnabled(mContext, enabledCode11);
            String option = sharedPreferences.getString(
                    RqSymbologyType.SymbologyType_Code11.getSampleName()+ RqSymbologyConfig.CHECKSUM_SUFFIX,
                    RqSymbologyConfigValue
                            .Code11.Code11PropertiesChecksum_Disabled);
            switch (option) {
                case RqSymbologyConfigValue
                        .Code11.Code11PropertiesChecksum_Disabled:
                    p.setChecksumProperties(mContext, Code11PropertiesChecksum_Disabled);
                    p.setStripChecksumEnabled(mContext, false);
                    break;
                case RqSymbologyConfigValue
                        .Code11.Code11PropertiesChecksum_Disabled1Digit:
                    p.setChecksumProperties(mContext, Symbologies.Code11PropertiesChecksum.Code11PropertiesChecksum_Enabled1Digit);
                    p.setStripChecksumEnabled(mContext, false);
                    break;
                case RqSymbologyConfigValue
                        .Code11.Code11PropertiesChecksum_Enabled1Digit:
                    p.setChecksumProperties(mContext, Symbologies.Code11PropertiesChecksum.Code11PropertiesChecksum_Enabled1Digit);
                    p.setStripChecksumEnabled(mContext, true);
                    break;
                case RqSymbologyConfigValue
                        .Code11.Code11PropertiesChecksum_Disabled2Digit:
                    p.setChecksumProperties(mContext, Symbologies.Code11PropertiesChecksum.Code11PropertiesChecksum_Enabled2Digit);
                    p.setStripChecksumEnabled(mContext, false);
                    break;
                case RqSymbologyConfigValue
                        .Code11.Code11PropertiesChecksum_Enabled2Digit:
                    p.setChecksumProperties(mContext, Symbologies.Code11PropertiesChecksum.Code11PropertiesChecksum_Enabled2Digit);
                    p.setStripChecksumEnabled(mContext, true);
                    break;
            }
        } else if (key.equals(RqSymbologyType.SymbologyType_Code128_CCA.getSampleName())) {
            boolean enabledC128 = sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_Code128_CCA.getSampleName(),
                    true);
            Symbologies.Code128Properties p = new Symbologies.Code128Properties();
            p.setEnabled(mContext, enabledC128);
            p.setMinChars(mContext, sharedPreferences.getInt(
                    RqSymbologyType.SymbologyType_Code128_CCA.getSampleName()+ RqSymbologyConfig.MIN_CHARS_SUFFIX,
                    1));
        } else if (key.equals(RqSymbologyType.SymbologyType_Code32.getSampleName())) {
            Symbologies.Code32Properties p = new Symbologies.Code32Properties();
            p.setEnabled(mContext, enabled);
        } else if (key.equals(RqSymbologyType.SymbologyType_Code39.getSampleName())) {
            boolean enabledC39 = sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_Code39.getSampleName(),
                    true);
            Symbologies.Code39Properties p = new Symbologies.Code39Properties();
            p.setEnabled(mContext, enabledC39);
            p.setMinChars(mContext, sharedPreferences.getInt(
                    RqSymbologyType.SymbologyType_Code39.getSampleName()+ RqSymbologyConfig.MIN_CHARS_SUFFIX,
                    1));
            p.setAsciiModeEnabled(mContext, sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_Code39.getSampleName()+ RqSymbologyConfig.ASCII_SUPPORT_SUFFIX,
                    false));
            String option = sharedPreferences.getString(
                    RqSymbologyType.SymbologyType_Code39.getSampleName()+ RqSymbologyConfig.CHECKSUM_SUFFIX,
                    RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled);
            switch (option) {
                case RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled:
                    p.setChecksumProperties(mContext, Symbologies.Code39PropertiesChecksum.Code39PropertiesChecksum_Disabled);
                    break;
                case RqSymbologyConfigValue.NormalCheckSum.Checksum_Enabled:
                    p.setChecksumProperties(mContext, Symbologies.Code39PropertiesChecksum.Code39PropertiesChecksum_Enabled);
                    break;
                case RqSymbologyConfigValue.NormalCheckSum.Checksum_EnabledStripCheckCharacter:
                    p.setChecksumProperties(mContext, Symbologies.Code39PropertiesChecksum.Code39PropertiesChecksum_EnabledStripCheckCharacter);
                    break;
            }
        } else if (key.equals(RqSymbologyType.SymbologyType_Code49.getSampleName())) {
            Symbologies.Code49Properties p = new Symbologies.Code49Properties();
            p.setEnabled(mContext, enabled);
        } else if (key.equals(RqSymbologyType.SymbologyType_Code93.getSampleName())) {
            boolean enabledC93 = sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_Code93.getSampleName(),
                    true);
            Symbologies.Code93Properties p = new Symbologies.Code93Properties();
            p.setEnabled(mContext, enabledC93);
            p.setMinChars(mContext, sharedPreferences.getInt(
                    RqSymbologyType.SymbologyType_Code93.getSampleName()+ RqSymbologyConfig.MIN_CHARS_SUFFIX,
                    1));
        } else if (key.equals(RqSymbologyType.SymbologyType_Codabar.getSampleName())) {
            boolean enabledCBar = sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_Codabar.getSampleName(),
                    true);
            Symbologies.CodabarProperties p = new Symbologies.CodabarProperties();
            p.setEnabled(mContext, enabledCBar);
            p.setMinChars(mContext, sharedPreferences.getInt(
                    RqSymbologyType.SymbologyType_Codabar.getSampleName()+ RqSymbologyConfig.MIN_CHARS_SUFFIX,
                    4));
            String option = sharedPreferences.getString(
                    RqSymbologyType.SymbologyType_Codabar.getSampleName()+ RqSymbologyConfig.CHECKSUM_SUFFIX,
                    RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled);
            switch (option) {
                case RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled:
                    p.setChecksumProperties(mContext, Symbologies.CodabarPropertiesChecksum.CodabarPropertiesChecksum_Disabled);
                    break;
                case RqSymbologyConfigValue.NormalCheckSum.Checksum_Enabled:
                    p.setChecksumProperties(mContext, Symbologies.CodabarPropertiesChecksum.CodabarPropertiesChecksum_Enabled);
                    break;
                case RqSymbologyConfigValue.NormalCheckSum.Checksum_EnabledStripCheckCharacter:
                    p.setChecksumProperties(mContext, Symbologies.CodabarPropertiesChecksum.CodabarPropertiesChecksum_EnabledStripCheckCharacter);
                    break;
            }
        } else if (key.equals(RqSymbologyType.SymbologyType_CodablockF.getSampleName())) {
            Symbologies.CodablockFProperties p = new Symbologies.CodablockFProperties();
            p.setEnabled(mContext, enabled);
        } else if (key.equals(RqSymbologyType.SymbologyType_COMPOSITE.getSampleName())) {
            Symbologies.CompositeCodeProperties p = new Symbologies.CompositeCodeProperties();
            p.setEnabled(mContext, enabled);
            if (enabled) {
                p.setCcaDecodingEnabled(mContext,
                        sharedPreferences.getBoolean(RqSymbologyType.SymbologyType_CCA.getSampleName(), enabled));
                p.setCcbDecodingEnabled(mContext,
                        sharedPreferences.getBoolean(RqSymbologyType.SymbologyType_CCB.getSampleName(), enabled));
                p.setCccDecodingEnabled(mContext,
                        sharedPreferences.getBoolean(RqSymbologyType.SymbologyType_CCC.getSampleName(), enabled));
            }
        } else if (key.equals(RqSymbologyType.SymbologyType_DataMatrix.getSampleName())) {

            boolean enabledDM = sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_DataMatrix.getSampleName(),
                    true);
            Symbologies.DataMatrixProperties p = new Symbologies.DataMatrixProperties();
            p.setEnabled(mContext, enabledDM);
            p.setExtendedRectEnabled(mContext, enabledDM);
            p.setMirrorDecodingEnabled(mContext, enabledDM);
            p.setPolarity(mContext, Symbologies.DataMatrixPropertiesPolarity.DataMatrixPropertiesPolarity_Either);
        } else if (key.equals(RqSymbologyType.SymbologyType_EAN13.getSampleName())) {
            boolean enabledEAN13 = sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_EAN13.getSampleName(),
                    true);
            Symbologies.EAN13Properties p = new Symbologies.EAN13Properties();
            p.setEnabled(mContext, enabledEAN13);
            p.setSupplemental2DigitDecodingEnabled(mContext, sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_EAN13.getSampleName()+ RqSymbologyConfig.TWO_DIGIT_SUPP,
                    false));
            p.setSupplemental5DigitDecodingEnabled(mContext, sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_EAN13.getSampleName()+ RqSymbologyConfig.FIVE_DIGIT_SUPP,
                    false));
            p.setAddSpaceEnabled(mContext, sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_EAN13.getSampleName()+ RqSymbologyConfig.ADD_SPACE,
                    false));
            p.setRequireSupplemental(mContext, sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_EAN13.getSampleName()+ RqSymbologyConfig.REQUIRE_SUPP,
                    false));
        } else if (key.equals(RqSymbologyType.SymbologyType_EAN8.getSampleName())) {
            boolean enabledEAN8 = sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_EAN8.getSampleName(),
                    true);
            Symbologies.EAN8Properties p = new Symbologies.EAN8Properties();
            p.setEnabled(mContext, enabledEAN8);
            p.setSupplemental2DigitDecodingEnabled(mContext, sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_EAN8.getSampleName()+ RqSymbologyConfig.TWO_DIGIT_SUPP,
                    false));
            p.setSupplemental5DigitDecodingEnabled(mContext, sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_EAN8.getSampleName()+ RqSymbologyConfig.FIVE_DIGIT_SUPP,
                    false));
            p.setAddSpaceEnabled(mContext, sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_EAN8.getSampleName()+ RqSymbologyConfig.ADD_SPACE,
                    false));
            p.setRequireSupplemental(mContext, sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_EAN8.getSampleName()+ RqSymbologyConfig.REQUIRE_SUPP,
                    false));
        } else if (key.equals(RqSymbologyType.SymbologyType_GridMatrix.getSampleName())) {
            Symbologies.GridMatrixProperties p = new Symbologies.GridMatrixProperties();
            p.setEnabled(mContext, enabled);
            p.setMirrorDecodingEnabled(mContext, enabled);
            p.setPolarity(mContext, Symbologies.GridMatrixPropertiesPolarity.GridMatrixPropertiesPolarity_Either);
        } else if (key.equals(RqSymbologyType.SymbologyType_DOT.getSampleName())) {
            Symbologies.DotCodeProperties p = new Symbologies.DotCodeProperties();
            p.setEnabled(mContext, enabled);
            p.setMirrorDecodingEnabled(mContext, enabled);
            p.setPolarity(mContext, Symbologies.DotCodePropertiesPolarity.DotCodePropertiesPolarity_Either);
        } else if (key.equals(RqSymbologyType.SymbologyType_GS1_DATABAR.getSampleName())) {
            boolean enabledGS1 = sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_GS1_DATABAR.getSampleName(),
                    true);
            Symbologies.GS1DataBar14Properties p = new Symbologies.GS1DataBar14Properties();
            p.setEnabled(mContext, enabledGS1);
            if (enabledGS1) {
                p.setOmniTruncatedDecodingEnabled(mContext, sharedPreferences.getBoolean(
                        RqSymbologyType.SymbologyType_GS1_DATABAR.getSampleName()+ RqSymbologyConfig.OMNI,
                        true));
                p.setStackedDecodingEnabled(mContext, sharedPreferences.getBoolean(
                        RqSymbologyType.SymbologyType_GS1_DATABAR.getSampleName()+ RqSymbologyConfig.STACKED_OMNI,
                        true));
                p.setLimitedDecodingEnabled(mContext, sharedPreferences.getBoolean(
                        RqSymbologyType.SymbologyType_GS1_DATABAR.getSampleName()+ RqSymbologyConfig.LIMITED,
                        true));
                p.setExpandedDecodingEnabled(mContext, sharedPreferences.getBoolean(
                        RqSymbologyType.SymbologyType_GS1_DATABAR.getSampleName()+ RqSymbologyConfig.EXPANDED,
                        true));
                p.setExpandedStackDecodingEnabled(mContext, sharedPreferences.getBoolean(
                        RqSymbologyType.SymbologyType_GS1_DATABAR.getSampleName()+ RqSymbologyConfig.EXPANDED_STACKED,
                        true));
            }
        } else if (key.equals(RqSymbologyType.SymbologyType_HanXin.getSampleName())) {
            Symbologies.HanXinCodeProperties p = new Symbologies.HanXinCodeProperties();
            p.setEnabled(mContext, enabled);
        } else if (key.equals(RqSymbologyType.SymbologyType_HongKong2of5.getSampleName())) {
            Symbologies.HongKong2of5Properties p = new Symbologies.HongKong2of5Properties();
            p.setEnabled(mContext, enabled);
            String option = sharedPreferences.getString(
                    RqSymbologyType.SymbologyType_HongKong2of5.getSampleName()+RqSymbologyConfig.CHECKSUM_SUFFIX,
                    RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled);
            switch (option) {
                case RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled:
                    p.setChecksumProperties(mContext, Symbologies.Symbology2of5PropertiesChecksum.Checksum_Disabled);
                    break;
                case RqSymbologyConfigValue.NormalCheckSum.Checksum_Enabled:
                    p.setChecksumProperties(mContext, Symbologies.Symbology2of5PropertiesChecksum.Checksum_Enabled);
                    break;
                case RqSymbologyConfigValue.NormalCheckSum.Checksum_EnabledStripCheckCharacter:
                    p.setChecksumProperties(mContext, Symbologies.Symbology2of5PropertiesChecksum.Checksum_EnabledStripCheckCharacter);
                    break;
            }
        } else if (key.equals(RqSymbologyType.SymbologyType_IATA2of5.getSampleName())) {
            Symbologies.IATA2of5Properties p = new Symbologies.IATA2of5Properties();
            p.setEnabled(mContext, enabled);
            p.setMinChars(mContext, sharedPreferences.getInt(
                    RqSymbologyType.SymbologyType_IATA2of5.getSampleName()+RqSymbologyConfig.MIN_CHARS_SUFFIX,
                    1));
            String option = sharedPreferences.getString(
                    RqSymbologyType.SymbologyType_IATA2of5.getSampleName()+RqSymbologyConfig.CHECKSUM_SUFFIX,
                    RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled);
            switch (option) {
                case RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled:
                    p.setChecksumProperties(mContext, Symbologies.Symbology2of5PropertiesChecksum.Checksum_Disabled);
                    break;
                case RqSymbologyConfigValue.NormalCheckSum.Checksum_Enabled:
                    p.setChecksumProperties(mContext, Symbologies.Symbology2of5PropertiesChecksum.Checksum_Enabled);
                    break;
                case RqSymbologyConfigValue.NormalCheckSum.Checksum_EnabledStripCheckCharacter:
                    p.setChecksumProperties(mContext, Symbologies.Symbology2of5PropertiesChecksum.Checksum_EnabledStripCheckCharacter);
                    break;
            }
        } else if (key.equals(RqSymbologyType.SymbologyType_Interleaved2of5.getSampleName())) {
            boolean enabledI2of5 = sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_Interleaved2of5.getSampleName(),
                    true);
            Symbologies.Interleaved2of5Properties p = new Symbologies.Interleaved2of5Properties();
            p.setEnabled(mContext, enabledI2of5);
            p.setRejectPartialDecode(mContext, sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_IATA2of5.getSampleName()+RqSymbologyConfig.REJECT_PARTIALDECODE,
                    false));
            p.setAllowShortQuietZone(mContext, sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_IATA2of5.getSampleName()+RqSymbologyConfig.ALLOW_SHORTQUIETZONE,
                    false));
            p.setMinChars(mContext, sharedPreferences.getInt(
                    RqSymbologyType.SymbologyType_IATA2of5.getSampleName()+RqSymbologyConfig.MIN_CHARS_SUFFIX,
                    8));
            String option = sharedPreferences.getString(
                    RqSymbologyType.SymbologyType_IATA2of5.getSampleName()+RqSymbologyConfig.CHECKSUM_SUFFIX,
                    RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled);
            switch (option) {
                case RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled:
                    p.setChecksumProperties(mContext, Symbologies.Interleaved2of5PropertiesChecksum.Interleaved2of5PropertiesChecksum_Disabled);
                    break;
                case RqSymbologyConfigValue.NormalCheckSum.Checksum_Enabled:
                    p.setChecksumProperties(mContext, Symbologies.Interleaved2of5PropertiesChecksum.Interleaved2of5PropertiesChecksum_Enabled);
                    break;
                case RqSymbologyConfigValue.NormalCheckSum.Checksum_EnabledStripCheckCharacter:
                    p.setChecksumProperties(mContext, Symbologies.Interleaved2of5PropertiesChecksum.Interleaved2of5PropertiesChecksum_EnabledStripCheckCharacter);
                    break;
            }
        } else if (key.equals(RqSymbologyType.SymbologyType_MAXICODE.getSampleName())) {
            Symbologies.MaxiCodeProperties p = new Symbologies.MaxiCodeProperties();
            p.setEnabled(mContext, enabled);
        } else if (key.equals(RqSymbologyType.SymbologyType_Matrix2of5.getSampleName())) {
            Symbologies.Matrix2of5Properties p = new Symbologies.Matrix2of5Properties();
            p.setEnabled(mContext, enabled);
            String option = sharedPreferences.getString(
                    RqSymbologyType.SymbologyType_Matrix2of5.getSampleName()+RqSymbologyConfig.CHECKSUM_SUFFIX,
                    RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled);
            switch (option) {
                case RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled:
                    p.setChecksumProperties(mContext, Symbologies.Symbology2of5PropertiesChecksum.Checksum_Disabled);
                    break;
                case RqSymbologyConfigValue.NormalCheckSum.Checksum_Enabled:
                    p.setChecksumProperties(mContext, Symbologies.Symbology2of5PropertiesChecksum.Checksum_Enabled);
                    break;
                case RqSymbologyConfigValue.NormalCheckSum.Checksum_EnabledStripCheckCharacter:
                    p.setChecksumProperties(mContext, Symbologies.Symbology2of5PropertiesChecksum.Checksum_EnabledStripCheckCharacter);
                    break;
            }
        } else if (key.equals(RqSymbologyType.SymbologyType_MPDF.getSampleName())) {
            Symbologies.MicroPDF417Properties p = new Symbologies.MicroPDF417Properties();
            p.setEnabled(mContext, enabled);
        } else if (key.equals(RqSymbologyType.SymbologyType_QRMicro.getSampleName())) {
            boolean enabledMicroQR = sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_QRMicro.getSampleName(),
                    true);
            Symbologies.MicroQRProperties p = new Symbologies.MicroQRProperties();
            p.setEnabled(mContext, enabledMicroQR);
        } else if (key.equals(RqSymbologyType.SymbologyType_MSIPlessy.getSampleName())) {
            Symbologies.MSIPlesseyProperties p = new Symbologies.MSIPlesseyProperties();
            p.setEnabled(mContext, enabled);
            p.setMinChars(mContext, sharedPreferences.getInt(
                    RqSymbologyType.SymbologyType_MSIPlessy.getSampleName() + RqSymbologyConfig.MIN_CHARS_SUFFIX,
                    1));
            String option = sharedPreferences.getString(
                    RqSymbologyType.SymbologyType_MSIPlessy.getSampleName() + RqSymbologyConfig.CHECKSUM_SUFFIX,
                    RqSymbologyConfigValue.MSIPlessy.MSIPlesseyPropertiesChecksum_Disabled);
            switch (option) {
                case RqSymbologyConfigValue.MSIPlessy.MSIPlesseyPropertiesChecksum_Disabled:
                    p.setChecksumProperties(mContext, Symbologies.MSIPlesseyPropertiesChecksum.MSIPlesseyPropertiesChecksum_Disabled);
                    p.setStripChecksumEnabled(mContext, false);
                    break;
                case RqSymbologyConfigValue.MSIPlessy.MSIPlesseyPropertiesChecksum_DisabledMod10:
                    p.setChecksumProperties(mContext, Symbologies.MSIPlesseyPropertiesChecksum.MSIPlesseyPropertiesChecksum_EnabledMod10);
                    p.setStripChecksumEnabled(mContext, false);
                    break;
                case RqSymbologyConfigValue.MSIPlessy.MSIPlesseyPropertiesChecksum_EnabledMod10:
                    p.setChecksumProperties(mContext, Symbologies.MSIPlesseyPropertiesChecksum.MSIPlesseyPropertiesChecksum_EnabledMod10);
                    p.setStripChecksumEnabled(mContext, true);
                    break;
                case RqSymbologyConfigValue.MSIPlessy.MSIPlesseyPropertiesChecksum_DisabledMod10_10:
                    p.setChecksumProperties(mContext, Symbologies.MSIPlesseyPropertiesChecksum.MSIPlesseyPropertiesChecksum_EnabledMod10_10);
                    p.setStripChecksumEnabled(mContext, false);
                    break;
                case RqSymbologyConfigValue.MSIPlessy.MSIPlesseyPropertiesChecksum_EnabledMod10_10:
                    p.setChecksumProperties(mContext, Symbologies.MSIPlesseyPropertiesChecksum.MSIPlesseyPropertiesChecksum_EnabledMod10_10);
                    p.setStripChecksumEnabled(mContext, true);
                    break;
                case RqSymbologyConfigValue.MSIPlessy.MSIPlesseyPropertiesChecksum_DisabledMod11_10:
                    p.setChecksumProperties(mContext, Symbologies.MSIPlesseyPropertiesChecksum.MSIPlesseyPropertiesChecksum_EnabledMod11_10);
                    p.setStripChecksumEnabled(mContext, false);
                    break;
                case RqSymbologyConfigValue.MSIPlessy.MSIPlesseyPropertiesChecksum_EnabledMod11_10:
                    p.setChecksumProperties(mContext, Symbologies.MSIPlesseyPropertiesChecksum.MSIPlesseyPropertiesChecksum_EnabledMod11_10);
                    p.setStripChecksumEnabled(mContext, true);
                    break;
            }
        } else if (key.equals(RqSymbologyType.SymbologyType_NEC2of5.getSampleName())) {
            Symbologies.NEC2of5Properties p = new Symbologies.NEC2of5Properties();
            p.setEnabled(mContext, enabled);
            String option = sharedPreferences.getString(
                    RqSymbologyType.SymbologyType_NEC2of5.getSampleName() + RqSymbologyConfig.CHECKSUM_SUFFIX,
                    RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled);
            switch (option) {
                case RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled:
                    p.setChecksumProperties(mContext, Symbologies.Symbology2of5PropertiesChecksum.Checksum_Disabled);
                    break;
                case RqSymbologyConfigValue.NormalCheckSum.Checksum_Enabled:
                    p.setChecksumProperties(mContext, Symbologies.Symbology2of5PropertiesChecksum.Checksum_Enabled);
                    break;
                case RqSymbologyConfigValue.NormalCheckSum.Checksum_EnabledStripCheckCharacter:
                    p.setChecksumProperties(mContext, Symbologies.Symbology2of5PropertiesChecksum.Checksum_EnabledStripCheckCharacter);
                    break;
            }
        } else if (key.equals(RqSymbologyType.SymbologyType_PDF417.getSampleName())) {
            boolean enabledPDF417 = sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_PDF417.getSampleName(),
                    true);
            Symbologies.PDF417Properties p = new Symbologies.PDF417Properties();
            p.setEnabled(mContext, enabledPDF417);
        } else if (key.equals(RqSymbologyType.SymbologyType_Plessy.getSampleName())) {
            Symbologies.PlesseyProperties p = new Symbologies.PlesseyProperties();
            p.setEnabled(mContext, enabled);
        } else if (key.equals(RqSymbologyType.SymbologyType_QR.getSampleName())) {
            boolean enabledQR = sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_QR.getSampleName(),
                    true);
            Symbologies.QRProperties p = new Symbologies.QRProperties();
            p.setEnabled(mContext, enabledQR);
            p.setModel1DecodingEnabled(mContext, enabledQR);
            p.setMirrorDecodingEnabled(mContext, enabledQR);
            p.setPolarity(mContext, Symbologies.QRPropertiesPolarity.QRPropertiesPolarity_Either);
        } else if (key.equals(RqSymbologyType.SymbologyType_Straight2of5.getSampleName())) {
            Symbologies.Straight2of5Properties p = new Symbologies.Straight2of5Properties();
            p.setEnabled(mContext, enabled);
            String option = sharedPreferences.getString(
                    RqSymbologyType.SymbologyType_Straight2of5.getSampleName()+RqSymbologyConfig.CHECKSUM_SUFFIX,
                    RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled);
            switch (option) {
                case RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled:
                    p.setChecksumProperties(mContext, Symbologies.Symbology2of5PropertiesChecksum.Checksum_Disabled);
                    break;
                case RqSymbologyConfigValue.NormalCheckSum.Checksum_Enabled:
                    p.setChecksumProperties(mContext, Symbologies.Symbology2of5PropertiesChecksum.Checksum_Enabled);
                    break;
                case RqSymbologyConfigValue.NormalCheckSum.Checksum_EnabledStripCheckCharacter:
                    p.setChecksumProperties(mContext, Symbologies.Symbology2of5PropertiesChecksum.Checksum_EnabledStripCheckCharacter);
                    break;
            }
        } else if (key.equals(RqSymbologyType.SymbologyType_Telepen.getSampleName())) {
            Symbologies.TelepenProperties p = new Symbologies.TelepenProperties();
            p.setEnabled(mContext, enabled);
        } else if (key.equals(RqSymbologyType.SymbologyType_Trioptic.getSampleName())) {
            Symbologies.TriopticProperties p = new Symbologies.TriopticProperties();
            p.setEnabled(mContext, enabled);
        } else if (key.equals(RqSymbologyType.SymbologyType_UPCA.getSampleName())) {
            boolean enabledUPCA = sharedPreferences.getBoolean(RqSymbologyType.SymbologyType_UPCA.getSampleName(), true);
            Symbologies.UPCAProperties p = new Symbologies.UPCAProperties();
            p.setEnabled(mContext, enabledUPCA);
            p.setSupplemental2DigitDecodingEnabled(mContext, sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_UPCA.getSampleName()+RqSymbologyConfig.TWO_DIGIT_SUPP,
                    false));
            p.setSupplemental5DigitDecodingEnabled(mContext, sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_UPCA.getSampleName()+RqSymbologyConfig.FIVE_DIGIT_SUPP,
                    false));
            p.setAddSpaceEnabled(mContext, sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_UPCA.getSampleName()+RqSymbologyConfig.ADD_SPACE,
                    false));
            p.setRequireSupplemental(mContext, sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_UPCA.getSampleName()+RqSymbologyConfig.REQUIRE_SUPP,
                    false));
        } else if (key.equals(RqSymbologyType.SymbologyType_UPCE.getSampleName())) {
            boolean enabledUPCE = sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_UPCE.getSampleName(),
                    true);
            Symbologies.UPCEProperties p = new Symbologies.UPCEProperties();
            p.setEnabled(mContext, enabledUPCE);
            p.setSupplemental2DigitDecodingEnabled(mContext, sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_UPCE.getSampleName()+RqSymbologyConfig.TWO_DIGIT_SUPP,
                    false));
            p.setSupplemental5DigitDecodingEnabled(mContext, sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_UPCE.getSampleName()+RqSymbologyConfig.FIVE_DIGIT_SUPP,
                    false));
            p.setAddSpaceEnabled(mContext, sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_UPCE.getSampleName()+RqSymbologyConfig.ADD_SPACE,
                    false));
            p.setRequireSupplemental(mContext, sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_UPCE.getSampleName()+RqSymbologyConfig.REQUIRE_SUPP,
                    false));
            p.setExpansionEnabled(mContext, sharedPreferences.getBoolean(
                    RqSymbologyType.SymbologyType_UPCE.getSampleName()+RqSymbologyConfig.ENABLE_EXPANSION,
                    false));
        } else if (key.equals(RqSymbologyType.SymbologyType_AustraliaPost.getSampleName())) {
            Symbologies.AustraliaPostProperties p = new Symbologies.AustraliaPostProperties();
            p.setEnabled(mContext, enabled);
        } else if (key.equals(RqSymbologyType.SymbologyType_CanadaPost.getSampleName())) {
            Symbologies.CanadaPostProperties p = new Symbologies.CanadaPostProperties();
            p.setEnabled(mContext, enabled);
        } else if (key.equals(RqSymbologyType.SymbologyType_DutchPost.getSampleName())) {
            Symbologies.DutchPostProperties p = new Symbologies.DutchPostProperties();
            p.setEnabled(mContext, enabled);
        } else if (key.equals(RqSymbologyType.SymbologyType_JapanMail.getSampleName())) {
            Symbologies.JapanPostProperties p = new Symbologies.JapanPostProperties();
            p.setEnabled(mContext, enabled);
        } else if (key.equals(RqSymbologyType.SymbologyType_KoreaPost.getSampleName())) {
            Symbologies.KoreaPostProperties p = new Symbologies.KoreaPostProperties();
            p.setEnabled(mContext, enabled);
        } else if (key.equals(RqSymbologyType.SymbologyType_RoyalMail.getSampleName())) {
            Symbologies.RoyalMailProperties p = new Symbologies.RoyalMailProperties();
            p.setEnabled(mContext, enabled);
        } else if (key.equals(RqSymbologyType.SymbologyType_USPSIntelligentMail.getSampleName())) {
            Symbologies.USPSIntelligentMailProperties p = new Symbologies.USPSIntelligentMailProperties();
            p.setEnabled(mContext, enabled);
        } else if (key.equals(RqSymbologyType.SymbologyType_USPSPlanet.getSampleName())) {
            Symbologies.USPSPlanetProperties p = new Symbologies.USPSPlanetProperties();
            p.setEnabled(mContext, enabled);
        } else if (key.equals(RqSymbologyType.SymbologyType_USPSPostnet.getSampleName())) {
            Symbologies.USPSPostnetProperties p = new Symbologies.USPSPostnetProperties();
            p.setEnabled(mContext, enabled);
        } else if (key.equals(RqSymbologyType.SymbologyType_UPU.getSampleName())) {
            Symbologies.UPUProperties p = new Symbologies.UPUProperties();
            p.setEnabled(mContext, enabled);
        } else {
            Log.e(TAG, "Unknown preference key" + key);
            return RqDecoder.DECODE_ERROR_NOT_SUPPORT_TYPE;
        }
        return MiscUtil.NO_ERROR;
    }
}
