package com.xiongmaokuaishou.myapplication.ui;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Build;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.rq.misc.MiscUtil;
import com.xiongmaokuaishou.myapplication.R;
import com.xiongmaokuaishou.myapplication.ScanActivity;
import com.xiongmaokuaishou.myapplication.http.Api;
import com.xiongmaokuaishou.myapplication.http.ApiListener;
import com.xiongmaokuaishou.myapplication.http.httpNetApi;
import com.xiongmaokuaishou.myapplication.utils.AdroidUtil;

import java.io.File;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import okhttp3.Response;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraTexture2 extends TextureView {
    private static final String TAG = "CameraPreview";
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();//从屏幕旋转转换为JPEG方向 是保存的相册方向 不是预览
    private static final int MAX_PREVIEW_WIDTH = 1920;//Camera2 API 保证的最大预览宽高
    private static final int MAX_PREVIEW_HEIGHT = 1080;
    private static final int STATE_PREVIEW = 0;//显示相机预览
    private static final int STATE_WAITING_LOCK = 1;//焦点锁定中
    private static final int STATE_WAITING_PRE_CAPTURE = 2;//拍照中
    private static final int STATE_WAITING_NON_PRE_CAPTURE = 3;//其它状态
    private static final int STATE_PICTURE_TAKEN = 4;//拍照完毕
    private int mState = STATE_PREVIEW;
    private int mRatioWidth = 0, mRatioHeight = 0;
    private int mSensorOrientation;
    private boolean mFlashSupported;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);//使用信号量 Semaphore 进行多线程任务调度
    private Activity activity;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private Size mPreviewSize;
    private String mCameraId;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CaptureRequest mPreviewRequest;
    private CameraCaptureSession mCaptureSession;
    private MediaRecorder mMediaRecorder;
    private Context mContext;
    private int fps;
    private Long time;
    private ImageReader mImageReader;
    public  Bitmap bitmapImage;
    public  Bitmap bitmapDecode;
    private String mFolderPath; //保存视频,图片的文件夹路径
    private  ScanActivity scanActivity;
    private String orderId;
    private httpNetApi httpApi;
    private String arriveTime;
    private String parcelId;
    private String path1;
    private String arriveTime2;
    private String parcelId2;
    private String path2;
    private String arriveTime3;
    private String parcelId3;
    private String path3;
    private Bitmap bitmap_decode;
    static {
        ORIENTATIONS.append(Surface.ROTATION_90, 0);//0
        ORIENTATIONS.append(Surface.ROTATION_0, 90);//1
        ORIENTATIONS.append(Surface.ROTATION_180, 270);//2
        ORIENTATIONS.append(Surface.ROTATION_270, 180);//3
    }

    public CameraTexture2(Context context) {
        this(context, null);
    }

    public CameraTexture2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraTexture2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
         scanActivity =new ScanActivity();
        mContext = context;
        httpApi = httpNetApi.getInstance();
        httpApi.Init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        MiscUtil.setListener(new MiscUtil.Listener() {
            @Override
            public void savePicture(Bitmap bitmap,String s) {
                bitmapDecode=bitmap;
                orderId=s;
            }
        });
        // int width = getMeasuredWidth(); //
        //  int height = getMeasuredHeight(); //
        int width = 200;
        int height = 150;
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);//设置预览框高度和宽度的
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, height);
            } else {
                // setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
                setMeasuredDimension(width, height);
            }
        }
    }

    public void onResume(Activity activity) {
        this.activity = activity;
        startBackgroundThread(); //当Activity或Fragment OnResume()时,可以冲洗打开一个相机并开始预览,否则,这个Surface已经准备就绪
        if (this.isAvailable()) {
            openCamera(this.getWidth(), this.getHeight());
        } else {
            this.setSurfaceTextureListener(mSurfaceTextureListener);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("BROADCAST_ACTION");
        activity.registerReceiver(MyBroadcastReceiver, intentFilter);
    }

    private final BroadcastReceiver MyBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
           Log.d("logcat_qxj","--------接收到的扫码图片");
           bitmap_decode =AdroidUtil.adjustPhotoRotation(bitmapDecode,180);
           parcelId= intent.getStringExtra("parcelId");
           arriveTime= intent.getStringExtra("arriveTime");
           Log.d("qxj","---"+parcelId+"---"+arriveTime);
          lockFocus();
        }
    };

    public void onStop() {

        closeCamera();
        stopBackgroundThread();
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
        }
    }



    public void setAspectRatio(int width, int height) {

        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size can't be negative");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setAutoFlash(CaptureRequest.Builder requestBuilder) {

        if (mFlashSupported) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
        }
    }


    private void startBackgroundThread() {

        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());


    }

    /**
     * 运行preCapture序列来捕获静止图像
     */
    private void runPreCaptureSequence() {
        try {
            // 设置拍照参数请求
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            mState = STATE_WAITING_PRE_CAPTURE;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private void stopBackgroundThread() {
        if (mBackgroundThread != null) {
            mBackgroundThread.quitSafely();
            try {
                mBackgroundThread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     *
     */
    private final SurfaceTextureListener mSurfaceTextureListener = new SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {

        }
    };
    /**
     * 相机状态改变回调
     */
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            if (null != activity) {
                activity.finish();
            }
        }
    };
    /**
     * 处理与照片捕获相关数据的事件
     */
    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        private void process(CaptureResult result) {
            switch (mState) {
                case STATE_PREVIEW://显示相机预览
                    break;

                case STATE_WAITING_LOCK: {//焦点锁定中
                    Integer afState = 4;

                    if (afState == null ) {
                        captureStillPicture();
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                            CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        Integer aeState = 2;

                        if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            mState = STATE_PICTURE_TAKEN;
                            captureStillPicture();
                        } else {
                            runPreCaptureSequence();
                        }
                    }
                    break;
                }
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            process(result);
            Face faces[]=result.get(CaptureResult.STATISTICS_FACES);


            scanActivity.onCameraImagePreviewed(result);
        }

    };


    /**
     * 在确定相机预览大小后应调用此方法
     *
     * @param viewWidth  宽
     * @param viewHeight 高
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        Log.d("wxwE","-1----"+mPreviewSize.getHeight()+" "+mPreviewSize.getWidth());
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        Log.d("wxwE","-1----"+rotation);
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max((float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
            Log.d("wxwE","-----");
        } else if (Surface.ROTATION_180 == rotation) {
            Log.d("wxwE","-2----");
            matrix.postRotate(180, centerX, centerY);
        }

        this.setTransform(matrix);
    }

    /**
     * 根据mCameraId打开相机
     */

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void openCamera(int width, int height) {

        setUpCameraOutputs(width, height);
        configureTransform(width, height);
        CameraManager manager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
        try {

            if (mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {

                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    /**
     * 关闭相机
     */
    private void closeCamera() {

        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }

    }


    /**
     * 设置相机相关的属性或变量
     *
     * @param width  相机预览的可用尺寸的宽度
     * @param height 相机预览的可用尺寸的高度
     */
    @SuppressWarnings("SuspiciousNameCombination")
    public void setUpCameraOutputs(int width, int height) {

        CameraManager manager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                //不使用前置摄像
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                Size cPixelSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE);//获取成像尺寸，同上

                Log.d("wxwII","----"+cPixelSize.getHeight()+"---"+cPixelSize.getWidth());
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;//不结束循环,只跳出本次循环
                }
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }
                //对于静态图像拍照, 使用最大的可用尺寸
                Size largest = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new CompareSizesByArea()
                );
                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, 2);
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
                //获取手机旋转的角度以调整图片的方向

                int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                Log.d("qqq", "displayRotation:" + displayRotation + " mSensorOrientation: " + mSensorOrientation);
                boolean swappedDimensions = false;

                switch (displayRotation) {
                    case Surface.ROTATION_0://0
                    case Surface.ROTATION_180://2
                        //横屏
                        if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                            swappedDimensions = true;
                        }
                        break;
                    case Surface.ROTATION_90://1
                    case Surface.ROTATION_270://3
                        //竖屏
                        if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                            swappedDimensions = true;
                        }
                        break;
                    default:
                        Log.e(TAG, "Display rotation is invalid: " + displayRotation);
                }

                Point displaySize = new Point();
                activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;

                if (swappedDimensions) {
                    rotatedPreviewWidth = height;
                    rotatedPreviewHeight = width;
                    maxPreviewWidth = displaySize.y;
                    maxPreviewHeight = displaySize.x;
                    Log.d("qqq", "rotatedPreviewWidth" + rotatedPreviewWidth + " " + rotatedPreviewHeight + " " + maxPreviewWidth + " " + maxPreviewHeight);
                }

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }

                DisplayMetrics displayMetrics = new DisplayMetrics();
                activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int widthPixels = displayMetrics.widthPixels;
                int heightPixels = displayMetrics.heightPixels;

                //尝试使用太大的预览大小可能会超出摄像头的带宽限制
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                        maxPreviewHeight, largest);
                for (Size i : map.getOutputSizes(SurfaceTexture.class)) {
                    Log.d("wxwQQ","---"+i);
                }
                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {
                    setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }
                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = available == null ? false : available;
                mCameraId = "1";
                mMediaRecorder = new MediaRecorder();
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
        }
    }

    /**
     * 获取一个合适的相机预览尺寸
     *
     * @param choices           支持的预览尺寸列表
     * @param textureViewWidth  相对宽度
     * @param textureViewHeight 相对高度
     * @param maxWidth          可以选择的最大宽度
     * @param maxHeight         可以选择的最大高度
     * @param aspectRatio       宽高比
     * @return 最佳预览尺寸
     */
    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth, int textureViewHeight, int maxWidth,
                                          int maxHeight, Size aspectRatio) {

        List<Size> bigEnough = new ArrayList<>();
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight && option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth && option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            return choices[0];
        }
    }


    int[] faceDetectModes;
    private int getFaceDetectMode(){
        if(faceDetectModes == null){
            return CaptureRequest.STATISTICS_FACE_DETECT_MODE_FULL;
        }else{
            return faceDetectModes[faceDetectModes.length-1];
        }
    }
    /**
     * 为相机预览创建CameraCaptureSession
     */
    private void createCameraPreviewSession() {

        try {
            SurfaceTexture texture = this.getSurfaceTexture();
            assert texture != null; // 将默认缓冲区的大小配置为想要的相机预览的大小
            //设置TextureView的缓冲区大小
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            //获取Surface显示预览数据
            Surface surface = new Surface(texture);
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface); // 我们创建一个 CameraCaptureSession 来进行相机预览
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {//modified by xss
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if (null == mCameraDevice) {
                        return;
                    } // 会话准备好后，我们开始显示预览
                    mCaptureSession = cameraCaptureSession;
                    try {
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                        mPreviewRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE,getFaceDetectMode());//设置人脸检测级别
                        setAutoFlash(mPreviewRequestBuilder);
                        mPreviewRequest = mPreviewRequestBuilder.build();
                        time = System.currentTimeMillis();
                        mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(
                        @NonNull CameraCaptureSession cameraCaptureSession) {
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    /**
     * 输出视频地址
     */
    private File getVideoFilePath() {

        return new File(activity.getExternalFilesDir(null) + "/" + System.currentTimeMillis() + ".mp4");
    }

    /**
     * 比较两者大小
     */
    private static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }
    }


    /**
     * 设置需要保存文件的文件夹路径
     *
     * @param path
     */
    public void setFolderPath(String path) {
        this.mFolderPath = path;
        File mFolder = new File(path);
        if (!mFolder.exists()) {
            mFolder.mkdirs();
            Log.d(TAG, "文件夹不存在去创建");
        } else {
            Log.d(TAG, "文件夹已创建");
        }
    }

    public String getFolderPath() {
        return mFolderPath;
    }

    /**
     * 获取当前时间,用来给文件夹命名
     */
    private String getNowDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(new Date());
    }

    private void lockFocus() {

        //创建文件
        try {
            if (mPreviewRequestBuilder != null) {

                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                        CameraMetadata.CONTROL_AF_TRIGGER_START);
            }
            mState = STATE_WAITING_LOCK;
            if (mCaptureSession != null) {

                mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                        mBackgroundHandler);

            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {

            //等图片可以得到的时候获取图片并保存
            Image image = reader.acquireNextImage();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.capacity()];
            buffer.get(bytes);
            bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);

           Log.d("picture------1","--");
            Bitmap bitmap_face1=AdroidUtil.setImgSize(bitmapImage,480,640);
            Log.d("picture------2","--");
            Bitmap bitmap_face=AdroidUtil.adjustPhotoRotation(bitmap_face1,90);


            Log.d("picture------4","--");
            Bitmap bitmap1 = Bitmap.createBitmap(
                    bitmap_decode.getWidth(),
                    bitmap_decode.getHeight(),
                    bitmap_decode.getConfig()
            );
            Log.d("picture------5","--");
            Canvas canvas = new Canvas(bitmap1);
            canvas.drawBitmap(bitmap_decode, 0, 0, null);
            canvas.drawBitmap(bitmap_face, bitmap_decode.getWidth()-bitmap_face.getWidth()-64,64, null);

             path1 =mContext.getExternalFilesDir(null)+"/"+orderId+".jpg";
            Log.d("picture------6","--"+mContext.getExternalFilesDir(null)+"/"+path1);
            AdroidUtil.rgbSave(bitmap1, path1);
            Thread thread = new Thread(){
                @Override
                public void run() {
                    super.run();
                    Log.d("logcat_qxj","---bmxUploadParcel_2-"+parcelId);
                    httpApi.bmxUploadParcel(parcelId2, arriveTime2, path2, new ApiListener() {
                        @Override
                        public void success(Api api) {
                            Log.d("logcat_qxj","---bmxUploadParcel-success"+api.jsonObject);
                            deleteSingleFile(path1);

                        }
                        @Override
                        public void failure(Api api) {
                            parcelId3=parcelId2;
                            arriveTime3=arriveTime2;
                            path3=path2;
                            Log.d("logcat_qxj","---bmxUploadParcel-failure"+api.jsonObject);
                            new Handler().postDelayed(new Thread(){
                                @Override
                                public void run() {
                                    super.run();
                                    Log.d("logcat_qxj","---bmxUploadParcel_3-"+parcelId);
                                    httpApi.bmxUploadParcel(parcelId3, arriveTime3, path3, new ApiListener() {
                                        @Override
                                        public void success(Api api) {
                                            Log.d("logcat_qxj","---bmxUploadParcel-success"+api.jsonObject);
                                            deleteSingleFile(path1);
                                        }
                                        @Override
                                        public void failure(Api api) {
                                            Log.d("logcat_qxj","---bmxUploadParcel-failure"+api.jsonObject);
                                        }
                                        @Override
                                        public void onData(Response response) {}
                                    });
                                }
                            },10*1000);
                        }
                        @Override
                        public void onData(Response response) {

                        }
                    });
                }
            };
            Log.d("logcat_qxj","---bmxUploadParcel_1-"+parcelId);
            httpApi.bmxUploadParcel(parcelId, arriveTime, path1, new ApiListener() {
                @Override
                public void success(Api api) {
                    Log.d("logcat_qxj","---bmxUploadParcel-success"+api.jsonObject);
                    deleteSingleFile(path1);
                }

                @Override
                public void failure(Api api) {
                    Log.d("logcat_qxj","---bmxUploadParcel-failure"+api.jsonObject);
                    parcelId2=parcelId;
                    arriveTime2=arriveTime;
                    path2=path1;
                   new Handler().postDelayed(thread,10*1000);
                }

                @Override
                public void onData(Response response) {

                }
            });


            if (bitmap1 != null) {
                bitmap1.recycle();
            }
            if (bitmap_decode != null) {
                bitmap_decode.recycle();
            }
            if (bitmap_face != null) {
                bitmap_face.recycle();
            }
            Log.d("picture------7","--");
            image.close();
        }
    };
    public static boolean deleteSingleFile(String filePath$Name) {
        File file = new File(filePath$Name);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                Log.e("--wxwtime--", "Copy_Delete.deleteSingleFile: 删除单个文件" + filePath$Name + "成功！");
                return true;
            } else {
                Log.e("wxwtime", "删除单个文件" + filePath$Name + "失败！");
                return false;
            }
        } else {
            Log.e("wxwtime", "删除单个文件失败：" + filePath$Name + "不存在！");
            return false;
        }
    }
    private int getOrientation(int rotation) {
        return (ORIENTATIONS.get(rotation) + mSensorOrientation) % 360;
    }

    private void showToast(final String text) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 解锁焦点
     */
    private void unlockFocus() {

        try {
            // 重置自动对焦
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            setAutoFlash(mPreviewRequestBuilder);
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
            // 将相机恢复正常的预览状态。
            mState = STATE_PREVIEW;
            // 打开连续取景模式
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback,
                    mBackgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 拍摄静态图片。
     */
    private void captureStillPicture() {

        try {
            if (null == mCameraDevice) {
                return;
            }
            // 这是用来拍摄照片的CaptureRequest.Builder。
            final CaptureRequest.Builder captureBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());

            // 使用相同的AE和AF模式作为预览。
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            setAutoFlash(captureBuilder);

            // 方向
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));

            CameraCaptureSession.CaptureCallback CaptureCallback
                    = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    unlockFocus();
                    Face faces[]=result.get(CaptureResult.STATISTICS_FACES);

                }
            };
            //停止连续取景
            mCaptureSession.stopRepeating();
            //捕获图片
            mCaptureSession.capture(captureBuilder.build(), CaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

}