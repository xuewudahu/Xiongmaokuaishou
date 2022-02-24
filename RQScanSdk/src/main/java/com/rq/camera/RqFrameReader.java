package com.rq.camera;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.rq.misc.MiscUtil;

import androidx.annotation.RequiresApi;
/**
 * @deprecated
 */
public class RqFrameReader {
    private static final String TAG = MiscUtil.getTag(RqFrameReader.class);

    private Handler mHandler = new Handler();
    /**
     * Texture 获取SurfaceTexture对象需要一定延迟才能获取到
     */
    private final static int WAIT_SURFACE_TIME = 500;

    /**
     * 核心组件
     */
    private RqCameraManager mRqCameraManager;
    private ImageReader mImageReader;

    /**
     * 回调
     */
    private FrameCallback mFrameCallback;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public RqFrameReader(RqCameraManager rqCameraManager){
        mRqCameraManager = rqCameraManager;
       Log.d("TTTT","----initImageReaderForDecode--");
        initImageReaderForDecode();
    }

    /**
     * 初始化图像监听，只针对Camera API 2
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void initImageReaderForDecode() {
        if(RqCameraManager.DEFAULT_CAMERA_API == RqCameraManager.CAMERA_API_TWO) {
            if (mImageReader == null) {
                //前三个参数分别是需要的尺寸和格式，最后一个参数代表每次最多获取几帧数据，本例的2代表ImageReader中最多可以获取两帧图像流
                mImageReader = ImageReader.newInstance(RqCameraManager.DecodePreviewWidth,
                        RqCameraManager.DecodePreviewHeight,
                        ImageFormat.YUV_420_888, 2);
                //监听ImageReader的事件，当有图像流数据可用时会回调onImageAvailable方法，它的参数就是预览帧数据，可以对这帧数据进行处理
                mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                    @Override
                    public synchronized void onImageAvailable(ImageReader reader) {
                        Log.i("cenon","onImageAvailable");
                        Image image = reader.acquireLatestImage();
                        //我们可以将这帧数据转成字节数组，类似于Camera1的PreviewCallback回调的预览帧数据
                        if (mFrameCallback != null) {
                            mFrameCallback.onFrameCallback(MiscUtil.packImageDataToFrame(image));
                        }
                        if (image != null)
                            image.close();

                    }
                }, null);
            }

            mRqCameraManager.addSurface(mImageReader.getSurface());
        }
    }

    /**
     * 初始化图像回调，只针对Camera API 1
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    protected void initPreviewCallbackForDecode(){
        if(RqCameraManager.DEFAULT_CAMERA_API == RqCameraManager.CAMERA_API_ONE) {
            Camera camera = mRqCameraManager.getCameraOne();
            if (camera == null) {
                if (MiscUtil.DEBUG)
                    Log.e(TAG,"mRqCameraManager not opened");
            }
            camera.setPreviewCallback(new Camera.PreviewCallback() {
                                          @Override
                                          public void onPreviewFrame(byte[] bytes, Camera camera) {
                                              if (mFrameCallback != null) {
                                                  mFrameCallback.onFrameCallback(new Frame(SystemClock.uptimeMillis(),bytes));
                                              }
                                          }
                                      }
            );
        }
    }



    /**
     * 回调对象赋值
     * @param frameCallback
     */
    public void setFrameCallback(FrameCallback frameCallback) {
        this.mFrameCallback = frameCallback;
    }

    /**
     * Frame 回调
     */
    public interface FrameCallback{
        void onFrameCallback(Frame frame);
    }
}
