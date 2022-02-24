package com.rq.camera;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;

import com.rq.barcode.RqEngineer;
import com.rq.misc.MiscUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

/**
 * 自定义CameraManager,主要用于相机打开关闭，
 * 以及surface管理，和桢图像回调
 */
@RequiresApi(api = Build.VERSION_CODES.Q)
public class RqCameraManager{
    private static final String TAG = MiscUtil.getTag(RqCameraManager.class);
   /**
     * 不同的API版本
     */
    public final static int CAMERA_API_ONE = 1;
    public final static int CAMERA_API_TWO = 2;
    public final static int DEFAULT_CAMERA_API = CAMERA_API_ONE;
    /**
     * cameraId需要提前赋值，预览尺寸需要提前获取
     */
    private String mCameraId = "0";
    /**
     * 解码尺寸，固定数据
     */
    public final static int DecodePreviewWidth = 2560;//2560;//2720;//3264//2720;//3840;//1920;
    public final static int DecodePreviewHeight = 1920;//2304;//2448,2160;//1080;
   /**
     * 关键属性
     */
    private Context mContext;
    private CameraManager mCameraManager;//only for camera API 2
   /**
     * camera对象
     */
    private CameraDevice mCamera;//only for camera API 2
    private Camera mCameraOne;//only for camera API 1
   /**
     * 预览角度，only for Camera API 1
     */
    private int rotation = 90;
   /**
     * 用于设置相机属性值, only for camera API 2
     */
    private CameraCaptureSession cameraCaptureSession;
   /**
     * 需要加入到预览的suface, only for camera API 2
     */
    private List<Surface> surfaceList = new ArrayList<Surface>();
   /**
     * 用于预览显示的TextureView, only for camera API 2
     */
    private TextureView textureView;
   /**
     * 预览surface最大数量, only for camera API 2
     */
    private final static int MAX_SURFACE_NUM = 3;
   @RequiresApi(api = Build.VERSION_CODES.Q)
    public RqCameraManager(Context context,@NonNull String cameraId) {
        mContext = context;
        /**
         * 需要做为空指针判断，为空则使用默认值0
         */
        if(!TextUtils.isEmpty(cameraId))
            mCameraId = cameraId;
        mCameraManager = (CameraManager) mContext.getSystemService(
                Context.CAMERA_SERVICE);
    }
   /**
     * 打开扫描相机
     * @return 是否打开成功
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public boolean doOpenCamera() {
        if (MiscUtil.DEBUG)
            Log.i(TAG, "doOpenCamera BEGIN: cameraId=" + mCameraId);
        try {
            /**
             * 检查权限
             */
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (MiscUtil.DEBUG)
                    Log.e(TAG, "checkSelfPermission failed, please check Camera permission.");
                return false;
            }
            if (DEFAULT_CAMERA_API == CAMERA_API_TWO) {
                /**
                 * 检查预览窗口是否添加，如果没有添加预览窗口则不去opencamera
                 */
                if (surfaceList.size() == 0) {
                    if (MiscUtil.DEBUG)
                        Log.e(TAG, "surface window not added yet.");
                    return false;
                }
                /**
                 * 防止重复打开
                 */
                if(mCamera != null){
                    if (MiscUtil.DEBUG)
                        Log.e(TAG, "camera is opened yet.");
                    return false;
                }
                /**
                 * openCamera
                 */
                mCameraManager.openCamera(
                        mCameraId,
                        new CameraDevice.StateCallback() {
                            @Override
                            public void onOpened(CameraDevice camera) {
                                mCamera = camera;
                               try {
                                    /**
                                     * 配置预览回调
                                     */
                                    mCamera.createCaptureSession(surfaceList, new CameraCaptureSession.StateCallback() {
                                        @Override
                                        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                                            if (mCamera == null) {
                                                if (MiscUtil.DEBUG)
                                                    Log.e(TAG, "onConfigured return, cameradevice is null.");
                                                return;
                                            }
                                           try {
                                                RqCameraManager.this.cameraCaptureSession = cameraCaptureSession;
                                                CaptureRequest.Builder previewRequestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                                for (Surface surface :
                                                        surfaceList) {
                                                    previewRequestBuilder.addTarget(surface);
                                                }
                                        /*Range<Integer>[] ranges = getFPSRange();
                                        for(Range range:ranges) {
                                            Log.i(TAG,"FPS: range"+range.getLower()+"~"+range.getUpper());
                                        }*/
                                                //previewRequestBuilder.set(CaptureRequest.CONTROL_SCENE_MODE,CaptureRequest.CONTROL_SCENE_MODE_SPORTS);
                                                previewRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, new Range<Integer>(60, 60));
                                                CaptureRequest previewRequest = previewRequestBuilder.build();//显示预览
                                                cameraCaptureSession.setRepeatingRequest(previewRequest, null, null);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                if (MiscUtil.DEBUG)
                                                    Log.e(TAG, "previewRequestBuilder exception error:" + e.getMessage());
                                            }
                                        }
                                       @Override
                                        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                                            if (MiscUtil.DEBUG)
                                                Log.e(TAG, "onConfigureFailed");
                                        }
                                    }, null);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    if (MiscUtil.DEBUG)
                                        Log.e(TAG, "createCaptureRequest exception error:" + e.getMessage());
                                }
                            }
                           @Override
                            public void onDisconnected(CameraDevice camera) {
                                if (MiscUtil.DEBUG)
                                    Log.i(TAG, "onDisconnected");
                            }
                           @Override
                            public void onError(CameraDevice camera, int error) {
                                if (MiscUtil.DEBUG)
                                    Log.e(TAG, "onError error code:" + error);
                            }
                        },
                        null);
            } else if (DEFAULT_CAMERA_API == CAMERA_API_ONE) {
                if(textureView == null || !textureView.isAvailable()) {
                    if (MiscUtil.DEBUG)
                        Log.e(TAG, "textureView have not set or  not available.");
                    return false;
                }
                /**
                 * 防止重复打开
                 */
                if(mCameraOne != null){
                    if (MiscUtil.DEBUG)
                        Log.e(TAG, "camera one is opened yet.");
                    return false;
                }
               mCameraOne = Camera.open(Integer.parseInt(mCameraId));
                Camera.Parameters parameters = mCameraOne.getParameters();

                List<String> focusModeList = parameters.getSupportedFocusModes();
                for (String focusMode : focusModeList){//检查支持的对焦
                    if (focusMode.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)){
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                    }else if (focusMode.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)){
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                    }
                }
                parameters.setPreviewSize(DecodePreviewWidth,DecodePreviewHeight);
                mCameraOne.setParameters(parameters);
                mCameraOne.setPreviewTexture(textureView.getSurfaceTexture());
                mCameraOne.setDisplayOrientation(rotation);
                mCameraOne.startPreview();
               RqEngineer.getInstence(mContext).getRqFrameReader().initPreviewCallbackForDecode();
            }
        } catch(CameraAccessException | IOException e){
            e.printStackTrace();
            if (MiscUtil.DEBUG)
                Log.e(TAG, "openCamera exception error:" + e.getMessage());
            return false;
        }
       return true;
    }
   /**
     * 关闭扫描相机
     */
    public void doCloseCamera() {
        if (MiscUtil.DEBUG)
            Log.i(TAG, "doCloseCamera begin:");
       /**
         * 关闭相机
         */
        if (mCamera != null) {
            mCamera.close();
            mCamera = null;
        }
        if(mCameraOne != null) {
            mCameraOne.setPreviewCallback(null);
            mCameraOne.release();
            mCameraOne = null;
        }
    }
   /**
     * 销毁
     */
    public void destory(){
        doCloseCamera();
        /**
         * 清除surface缓存
         */
        if(surfaceList != null)
            surfaceList.clear();
       textureView = null;
    }
   /**
     * 设置用于显示的TextureView ，仅适用于Camera API 1
     * @param tv
     */
    public void setTextureView(TextureView tv){
        textureView = tv;
    }
    /**
     * 添加surface，仅适用于Camera API 2
     * @param surface
     * @return 添加结果
     */
    public boolean addSurface(Surface surface) {
        if (DEFAULT_CAMERA_API != CAMERA_API_TWO) {
            if (MiscUtil.DEBUG)
                Log.e(TAG, "addSurface only support Camera API 2.");
            return false;
        }
        if (surfaceList.contains(surface)) {
            if (MiscUtil.DEBUG)
                Log.e(TAG, "addSurface failed:duplicate surface.");
            return false;
        }
       if (surfaceList.size() >= MAX_SURFACE_NUM) {
            if (MiscUtil.DEBUG)
                Log.e(TAG, "addSurface failed:surfaceList.size() >= MAX_SURFACE_NUM");
            return false;
        }
       if(mCamera != null) {
            if (MiscUtil.DEBUG)
                Log.e(TAG, "addSurface failed:Camera is opened, please addSurface before openCamera.");
            return false;
        }
       surfaceList.add(surface);
        return true;
    }
   /**
     * 获取扫描相机ID
     * @return 相机ID
     */
    public String getCameraId() {
        return mCameraId;
    }
   /**
     * 获取相机对象，Camera API TWO
     * @return 相机对象
     */
    public CameraDevice getCamera(){
        return mCamera;
    }
   /**
     * 获取相机对象，Camera API ONE
     * @return 相机对象
     */
    public Camera getCameraOne(){
        return mCameraOne;
    }
   /**
     * 设置相机ISO
     * @param iso
     */
    public void setISO(int iso) {
        if (DEFAULT_CAMERA_API == CAMERA_API_TWO) {
            if (cameraCaptureSession != null) {
                try {
                    CaptureRequest.Builder previewRequestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                    //previewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_3);
                    previewRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, iso);
                    for (Surface surface :
                            surfaceList) {
                        previewRequestBuilder.addTarget(surface);
                    }
                    cameraCaptureSession.setRepeatingRequest(previewRequestBuilder.build(), null, null);
                    if (MiscUtil.DEBUG)
                        Log.e(TAG, "addSurface failed:Camera is opened, please addSurface before openCamera.");
               } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        } else if(DEFAULT_CAMERA_API == CAMERA_API_ONE) {
            if(mCameraOne != null) {
                Camera.Parameters parameters = mCameraOne.getParameters();
                parameters.set("iso-values",iso);
                mCameraOne.setParameters(parameters);
            }
        }
    }
   /**
     * 获取扫描相机ISO的数据
     * @return 相机ISO的范围数据
     */
    public Range<Integer> getISORange(){
        try {
            CameraCharacteristics props = mCameraManager.getCameraCharacteristics(mCameraId);
            return props.get(
                    CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
   /**
     * 获取扫描相机ISO的数据
     * @return 相机ISO的范围数据
     */
    public Range<Integer>[] getFPSRange(){
        try {
            CameraCharacteristics props = mCameraManager.getCameraCharacteristics(mCameraId);
            return props.get(
                    CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 根据预览窗口尺寸获取输出的预览尺寸
     * @param surfaceSize
     * @return 最佳预览尺寸
     */
    public Size getTargetPreviewSize(Size surfaceSize){
        int diffs = Integer.MAX_VALUE;
        int bestPreviewWidth=0,bestPreviewHeight=0;
        try {
           CameraCharacteristics props = mCameraManager.getCameraCharacteristics(mCameraId);
            StreamConfigurationMap configurationMap = props.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size[] availablePreviewSizes = configurationMap.getOutputSizes(SurfaceTexture.class);
           for (Size previewSize : availablePreviewSizes) {
                if(MiscUtil.DEBUG)
                    Log.v(TAG, " PreviewSizes = " + previewSize);
                int previewWidth = previewSize.getWidth();
                int previewHeight = previewSize.getHeight();
                int newDiffs = Math.abs(previewWidth - surfaceSize.getWidth())
                        + Math.abs(previewHeight - surfaceSize.getHeight());
                if(MiscUtil.DEBUG)
                    Log.v(TAG, "newDiffs = " + newDiffs+" "+surfaceSize.getWidth()+" "+surfaceSize.getHeight());
               if (newDiffs == 0) {
                    bestPreviewWidth = previewWidth;
                    bestPreviewHeight = previewHeight;
                    break;
                }
                if (diffs > newDiffs) {
                    bestPreviewWidth = previewWidth;
                    bestPreviewHeight = previewHeight;
                    diffs = newDiffs;
                }
            }
        } catch (CameraAccessException cae) {
            Log.e(TAG,"exception:" + cae.getMessage());
            cae.printStackTrace();
            return  null;
        }
       return new Size(bestPreviewWidth,bestPreviewHeight);
    }
   /**
     * 设置预览角度，only for Camera API 1
     * @param rotation
     */
    public void setDisplayRotation(int rotation) {
        this.rotation = rotation;
        if (mCameraOne != null) {
            Log.d("wxwGQ","旋转角度是： "+rotation);
            mCameraOne.setDisplayOrientation(rotation);
        }
    }
}
