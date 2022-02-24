package com.rq.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.RelativeLayout;

import com.rq.barcode.RqDecoder;
import com.rq.barcode.RqEngineer;
import com.rq.barcode.RqSymbologyType;
import com.rq.camera.RqCameraManager;
import com.rq.misc.MiscUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * 预览工具类，定义控件时，
 * 请最好使用16:9的宽高比例，并占满整个屏幕宽度，可获得最佳预览图像
 */
public class RqDecodeComponent extends RelativeLayout implements RqDecoder.ResultCallback {
    private static final String TAG = MiscUtil.getTag(RqDecodeComponent.class);


    private Handler mHandler = new Handler();
    /**
     * Texture 获取SurfaceTexture对象需要一定延迟才能获取到
     */
    private final static int WAIT_SURFACETEXTURE_TIME = 500;

    /**
     * 扫到的条码框展示时间，超时自动消除
     */
    private final static int FIND_VIEW_DISPLAY_TIME = 300;

    /**
     * 预览组件
     */
    private TextureView mTextureView;
    private Surface mSurface;

    private int rotation = Surface.ROTATION_0;

    /**
     * 是否显示矩形框
     */
    private boolean mShowBarcoderRectangle = true;
    /**
     * 矩形条码框列表
     */
    private ArrayList<RqBarcodeFinderView> bfArr = new ArrayList<RqBarcodeFinderView>(); //for DrawBarcode

    /**
     * 动态布局属性
     */
    private LayoutParams matchParentLayoutParams =
            new LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);

    /**
     * 构造
     * @param context
     */
    public RqDecodeComponent(@NonNull Context context) {
        this(context,null);
    }
    public RqDecodeComponent(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(!inflateTextureView()){
            if(MiscUtil.DEBUG) {
                Log.e(TAG,"inflateTexture failed, please check.");
            }
        } else {
            //注册回调

            if(mShowBarcoderRectangle)
                RqEngineer.getInstence(getContext()).getRqDecoder().addResultCallback(this);

            mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                    /**
                     * 添加surface到CameraManager,用于预览回调
                     */
                    final RqCameraManager rqCameraManager = RqEngineer.getInstence(getContext()).getRqCameraManager();
                    if(rqCameraManager != null) {
                        if (RqCameraManager.DEFAULT_CAMERA_API == RqCameraManager.CAMERA_API_TWO) {
                            if(mSurface == null && mTextureView != null)
                                mSurface = new Surface(mTextureView.getSurfaceTexture());
                            if (!rqCameraManager.addSurface(mSurface)) {
                                if (MiscUtil.DEBUG)
                                    Log.e(TAG, "error:addSurface failed.");
                            }
                        } else if (RqCameraManager.DEFAULT_CAMERA_API == RqCameraManager.CAMERA_API_ONE) {
                            rqCameraManager.setTextureView(mTextureView);
                        }
                    }
                    /**
                     * 加入角度旋转
                     */
                    transformRotation(rotation);
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {}
                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                    return false;
                }
                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {}
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        RqEngineer.getInstence(getContext()).getRqDecoder().removeResultCallback(this);
        removeAllViews();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private boolean inflateTextureView(){
        /**
         * 初始化并装载核心组件
         */
        if(mTextureView == null) {
            mTextureView = new TextureView(getContext());
        }
        addView(mTextureView,matchParentLayoutParams);

        return true;
    }

    public boolean isShowBarcoderRectangle() {
        return mShowBarcoderRectangle;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void setShowBarcoderRectangle(boolean showBarcoderRectangle) {
        if(showBarcoderRectangle) {
            //注册回调
            RqEngineer.getInstence(getContext()).getRqDecoder().addResultCallback(this);
        } else {
            RqEngineer.getInstence(getContext()).getRqDecoder().removeResultCallback(this);
        }
        this.mShowBarcoderRectangle = showBarcoderRectangle;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onResultCallback(final String result, RqSymbologyType symbologyType,final  int[] corner) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                removeLocatorOverlays();
                createMultiTargetLocator(corner, result);
                mHandler.removeCallbacks(delayRemoveVoverlays);
                mHandler.postDelayed(delayRemoveVoverlays,FIND_VIEW_DISPLAY_TIME);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onMultipleResultCallback(final String[] results, RqSymbologyType[] symbologyTypes,final List<int[]> corners) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                String[] cacheResults = results;
                List<int[]> cacheCorners = (ArrayList<int[]>)((ArrayList)corners).clone();
                removeLocatorOverlays();
                for(int i = 0;i < cacheResults.length;i ++) {
                    if(cacheCorners.size() > i )
                        createMultiTargetLocator(cacheCorners.get(i),cacheResults[i]);
                }
                mHandler.removeCallbacks(delayRemoveVoverlays);
                mHandler.postDelayed(delayRemoveVoverlays,FIND_VIEW_DISPLAY_TIME);
                cacheResults = null;
                cacheCorners.clear();
                cacheCorners = null;
            }
        });

    }

    @Override
    public void onDecodeError(int errorCode) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                removeLocatorOverlays();
            }
        });
        Log.d(TAG, " onDecodeError ");
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public Size getPreviewOutputSize() {
        if(mTextureView == null) {
            if(MiscUtil.DEBUG)
                Log.e(TAG,"error:layout not attach window yet, call me let.");
            return null;
        }

        int width = mTextureView.getWidth();
        int height = mTextureView.getHeight();

        if(width == 0 && height == 0) {
            if(MiscUtil.DEBUG)
                Log.e(TAG,"error:layout not measure  yet, call me let.");
            return null;
        }

        return RqEngineer.getInstence(getContext()).getRqCameraManager()
                .getTargetPreviewSize(new Size(width,height));
    }

    /**
     * 设置预览的角度
     * @param rotation Surface.ROTATION_0 Surface.ROTATION_90 Surface.ROTATION_180 Surface.ROTATION_270
     */

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void transformRotation(int rotation) {
        this.rotation = rotation;
        if (RqCameraManager.DEFAULT_CAMERA_API == RqCameraManager.CAMERA_API_TWO) {
            Size previewOutputSize = getPreviewOutputSize();
            if(previewOutputSize == null) {
                if(MiscUtil.DEBUG)
                    Log.e(TAG,"error: layout not inflated yet, call me let.");
                return;
            }


            int width = mTextureView.getWidth();
            int height = mTextureView.getHeight();
            int previewHeight = previewOutputSize.getHeight();
            int previewWidth = previewOutputSize.getWidth();

            Matrix matrix = new Matrix();
            RectF textureRectF = new RectF(0, 0, width, height);
            RectF previewRectF = new RectF(0, 0, previewHeight, previewWidth);
            float centerX = textureRectF.centerX();
            float centerY = textureRectF.centerY();

            if (rotation == Surface.ROTATION_180) {
                matrix.postRotate(180, centerX, centerY);
            } else if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
                previewRectF.offset(centerX - previewRectF.centerX(), centerY - previewRectF.centerY());
                matrix.setRectToRect(textureRectF, previewRectF, Matrix.ScaleToFit.FILL);
                float scale = Math.max((float) width / previewWidth, (float) height / previewHeight);

                matrix.postScale(scale, scale, centerX, centerY);
                matrix.postRotate(90 * (rotation - 2), centerX, centerY);
            }
            mTextureView.setTransform(matrix);
        } else if (RqCameraManager.DEFAULT_CAMERA_API == RqCameraManager.CAMERA_API_ONE) {
            final RqCameraManager rqCameraManager = RqEngineer.getInstence(getContext()).getRqCameraManager();
            rqCameraManager.setDisplayRotation((rotation - 1) * 90);
        }

    }

    /**
     * 绘制多条码框
     * @param corners
     * @param result
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void createMultiTargetLocator(int[] corners, String result) {

        int pWidth = getWidth();
        int pHeight = getHeight();
        int screenDiff = 0;

        /**
         * 不同比例时进行数据裁剪
         * 只针对camera2 api
         * 预览和解码图像比例不同
         */
        if(RqCameraManager.DEFAULT_CAMERA_API == RqCameraManager.CAMERA_API_TWO) {
            float decodeRatio
                    = (float)RqCameraManager.DecodePreviewWidth/RqCameraManager.DecodePreviewHeight;
            float viewRatio = (float)pWidth / pHeight;
            /*
            在图像不拉伸的情况下，解码的预览宽高比小于UI预览的宽高比
            即，居中解码
            */
            if(decodeRatio < viewRatio) {
                pWidth = (int) (pHeight * decodeRatio);
                screenDiff = (int) ((viewRatio - decodeRatio) / 2
                        * pWidth) - 5;//5 是layout_marginLeft边距

            }
        }

        if(MiscUtil.DEBUG) {
            StringBuffer sb = new StringBuffer();
            for (int corner:corners) {
                sb.append(corner).append(",");
            }
            Log.d(TAG, " createMultiTargetLocator corners:" + sb.toString()
                    +" pWidth="+pWidth
                    +" pHeight="+pHeight
                    +" PreviewWidth="+RqCameraManager.DecodePreviewWidth
                    +" PreviewHeight="+RqCameraManager.DecodePreviewHeight
                    +" screenDiff="+screenDiff);
        }
        if (pWidth <= pHeight) {   //eric-zhao TODO
            float prh = (float) pWidth / RqCameraManager.DecodePreviewHeight;//sz.height;
            float prw = (float) pHeight / RqCameraManager.DecodePreviewWidth;//sz.width;

            final RqBarcodeFinderView bf = new RqBarcodeFinderView(getContext(), corners, pWidth, pHeight, screenDiff, prh, prw, result);  //zzz
            bfArr.add(bf);
            addView(bf,matchParentLayoutParams);
        } else {
            float prw = (float) pWidth / RqCameraManager.DecodePreviewWidth;//sz.height;
            float prh = (float) pHeight / RqCameraManager.DecodePreviewHeight;//sz.width;
            final RqBarcodeFinderView bf = new RqBarcodeFinderView(getContext(), corners, pWidth, pHeight, screenDiff, prh, prw, result);
            bfArr.add(bf);
            addView(bf,matchParentLayoutParams);
        }
    }

    /**
     * 清除绘制的条码框
     */
    private void removeLocatorOverlays() {
        for (Iterator<RqBarcodeFinderView> iterator = bfArr.listIterator(); iterator.hasNext(); ) {
            RqBarcodeFinderView b = iterator.next();
            iterator.remove();
            if(b.getParent() != null)
                removeView(b);
        }
    }

    Runnable delayRemoveVoverlays = new Runnable() {
        @Override
        public void run() {
            removeLocatorOverlays();
        }
    };
}
