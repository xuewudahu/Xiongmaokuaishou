package com.rq.barcode;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.rq.barcode.cortex.CortexDecoder;
import com.rq.camera.Frame;
import com.rq.camera.FrameSaver;
import com.rq.camera.RqCameraManager;
import com.rq.camera.RqFrameReader;
import com.rq.misc.MiscJNI;
import com.rq.misc.MiscUtil;

import java.util.List;
import java.util.concurrent.locks.Lock;

import androidx.annotation.RequiresApi;

/**
 * SDK入口类，
 * 包含：注册激活，解码配置，解码回调，保存桢图像。etc
 * 都是从该单例为入口进入操作
 */
public class RqEngineer implements RqFrameReader.FrameCallback, RqDecoder.ResultCallback{
    private static final String TAG = MiscUtil.getTag(RqEngineer.class);
    /**
     * 解码库支持
     */
    public final static int CORTEX = 1;
    private int mDecodeProgram = CORTEX;

    /**
     * 关键属性
     */
    //初始化状态
    private boolean initialized = false;
    //Camera2相机管理
    private RqCameraManager mRqCameraManager;
    //解码数据图像Reader
    private RqFrameReader mRqFrameReader;
    //解码核心类
    private RqDecoder mRqDecoder;
    //用于存储Frame为文件的工具类
    private FrameSaver frameSaver;
    //JNI工具类
    private MiscJNI  miscJNI;

    /**
     * 解码线程属性
     */
    private final static int THREAD_STATE_NONE = 0;
    private final static int THREAD_STATE_RUNNING = 1;
    private final static int THREAD_STATE_IDLE = 2;
    private int decodeThreadState = THREAD_STATE_NONE; //for decode thread
    /**
     * 解码线程临时的Frame对象
     */
    private Frame currentFrame = null;
    private Frame currentFrameTemp = null;
    private Frame saveFrame;
    /**
     * 同步存文件
     */
    private boolean saveFileSynchronize = false;
    /**
     * 异步存图状态
     */
    private volatile boolean saving = false;

    /**
     * 是否为连续读码模式
     */
    private boolean continueScanMode = true;

    /**
     * 本来做成单例模式，instance构造
     */
    private static RqEngineer instance;
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private RqEngineer(Context context){
        doInit(context,null);
    }
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private RqEngineer(Context context,int decodeProgram){
        //扫描方案分流，注意调用时序
        mDecodeProgram = decodeProgram;
        doInit(context,null);
    }
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private RqEngineer(Context context,String cameraId){
        doInit(context,cameraId);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private RqEngineer(Context context,int decodeProgram,String cameraId){
        //扫描方案分流，注意调用时序
        mDecodeProgram = decodeProgram;
        doInit(context,cameraId);
    }
    /**
     * 单例初始化
     * @param context
     * @return 单例对象
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static final RqEngineer getInstence(Context context){
        if(instance == null)
            instance = new RqEngineer(context);
        return instance;
    }

    /**
     * 单例初始化
     * @param context
     * @param decodeProgram 解码程序代号
     * @return 单例对象
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static final RqEngineer getInstence(Context context,int decodeProgram){
        if(instance == null)
            instance = new RqEngineer(context,decodeProgram);
        return instance;
    }
    /**
     * 单例初始化
     * @param context
     * @param cameraId
     * @return 单例对象
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static final RqEngineer getInstence(Context context,String cameraId){
        if(instance == null)
            instance = new RqEngineer(context,cameraId);
        return instance;
    }

    /**
     * 单例初始化
     * @param context
     * @param decodeProgram
     * @param cameraId
     * @return 单例对象
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static final RqEngineer getInstence(Context context,int decodeProgram, String cameraId){
        if(instance == null)
            instance = new RqEngineer(context,decodeProgram,cameraId);
        return instance;
    }


    /**
     * 初始化操作提取，注意方法中的调用时序
     * @param context
     * @param cameraId
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void doInit(Context context,String cameraId){
        /**
         * JNI对象初始化
         */
        miscJNI = new MiscJNI();
        /**
         * Camera manager初始化
         */
        mRqCameraManager = new RqCameraManager(context,cameraId);
        /**
         * Camera2 特有的ImageReader，封装类初始化
         */
        mRqFrameReader = new RqFrameReader(mRqCameraManager);
        /**
         * 解码帧数据回调监听
         */
        mRqFrameReader.setFrameCallback(this);
        /**
         *  初始化解码库
         */
        if(mDecodeProgram == CORTEX) {
            mRqDecoder = new CortexDecoder(context);
            mRqDecoder.addResultCallback(this);
        } else {
            throw new IllegalArgumentException("wrong DecodeProgram："+mDecodeProgram);
        }

        if(mRqDecoder != null) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            initialized = mRqDecoder.initialize() == MiscUtil.NO_ERROR;
        }
        if (!initialized) {
            Log.e(TAG,"initialized failed,  may activating, but do not care,  " +
                    "will initialize() when activated.");
        }
        /**
         * 存图工具类初始化
         */
        frameSaver = new FrameSaver(context,FrameSaver.SAVE_NONE,FrameSaver.FORMAT_YUV);

        /**
         * 启动解码进程
         */
        startDecodeThread(THREAD_STATE_IDLE);
    }

    /**
     * 开放RqCameraManager对象
     * @return RqCameraManager
     */
    public RqCameraManager getRqCameraManager() {
        return mRqCameraManager;
    }

    /**
     * 开放mRqDecoder对象
     * @return RqDecoder
     */
    public RqDecoder getRqDecoder() {
        return mRqDecoder;
    }


    public RqFrameReader getRqFrameReader(){
        return mRqFrameReader;
    }

    /**
     * 开放解码方案信息
     * @return 解码方案代号
     */
    public int getDecodeProgram() {
        return mDecodeProgram;
    }

    /**
     * 用于Frame桢图像保存
     * @return FrameSaver
     */
    public FrameSaver getFrameSaver() {
        return frameSaver;
    }

    /**
     * 判断是否时连续读码状态
     * @return 连续读码开关状态
     */
    public boolean isContinueScanMode() {
        return continueScanMode;
    }

    /**
     * 设置连续读码状态
     * @param continueScanMode
     */
    public void setContinueScanMode(boolean continueScanMode) {
        this.continueScanMode = continueScanMode;
    }

    /**
     * 打开扫描头
     * @return 执行反馈
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public int openScannner(){
        if (!checkInitializedState()) {
            if (MiscUtil.DEBUG)
                Log.d(TAG, "openScannner failed, not initialize.");
            return RqDecoder.DECODE_ERROR_NOT_INIT;
        }

        if(mRqCameraManager == null) {
            if (MiscUtil.DEBUG)
                Log.d(TAG, "openScannner failed, mRqCameraManager is null.");
            return MiscUtil.ERROR_NULL_POINT;
        }

        return mRqCameraManager.doOpenCamera()?
                MiscUtil.NO_ERROR:
                RqDecoder.DECODE_ERROR_OPEN_CAMERA_FAILED;
    }

    /**
     * 开始解码，修改解码进程的状态值，开始解码
     * @return 执行反馈
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public int startDecode(){
        if (!checkInitializedState()) {
            if (MiscUtil.DEBUG)
                Log.e(TAG, "startDecode failed, not initialize.");
            return RqDecoder.DECODE_ERROR_NOT_INIT;
        }

        if ((RqCameraManager.DEFAULT_CAMERA_API == RqCameraManager.CAMERA_API_TWO &&
                mRqCameraManager.getCamera() == null)
                || (RqCameraManager.DEFAULT_CAMERA_API == RqCameraManager.CAMERA_API_ONE &&
                mRqCameraManager.getCameraOne() == null)) {
            if (MiscUtil.DEBUG)
                Log.e(TAG, "startDecode failed, Camera not opened yet, openScannner  first.");
            return RqDecoder.DECODE_ERROR_NOT_OPEN_CAMERA;
        }

        if (decodeThreadState == THREAD_STATE_RUNNING) {
            if (MiscUtil.DEBUG)
                Log.d(TAG, "startDecode warning, started already, repeat call.");
        }
        /**
         * 如果初始化时因为注册未成功而没有启动线程，在这里进行再次启动
         */
        if (decodeThreadState == THREAD_STATE_NONE) {
            /**
             * 启动解码进程
             */
            startDecodeThread(THREAD_STATE_RUNNING);
        } else {
            decodeThreadState = THREAD_STATE_RUNNING;
        }
        synchronized (Lock.class) {
            Lock.class.notify();
        }
        return MiscUtil.NO_ERROR;
    }

    /**
     * 停止解码
     * @return 执行反馈
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public int stopDecode(){
        if (!checkInitializedState()) {
            if (MiscUtil.DEBUG)
                Log.e(TAG, "stopDecode failed, not initialize.");
            return RqDecoder.DECODE_ERROR_NOT_INIT;
        }
        if (decodeThreadState == THREAD_STATE_NONE) {
            if (MiscUtil.DEBUG)
                Log.d(TAG, "stopDecode failed, thread is finish or been killed.");
            return RqDecoder.DECODE_ERROR_NOT_INIT;
        }

        if ((RqCameraManager.DEFAULT_CAMERA_API == RqCameraManager.CAMERA_API_TWO &&
                mRqCameraManager.getCamera() == null)
                || (RqCameraManager.DEFAULT_CAMERA_API == RqCameraManager.CAMERA_API_ONE &&
                mRqCameraManager.getCameraOne() == null)) {
            if (MiscUtil.DEBUG)
                Log.e(TAG, "stopDecode failed, Camera not opened yet, openScannner  first.");
            return RqDecoder.DECODE_ERROR_NOT_OPEN_CAMERA;
        }

        if (decodeThreadState == THREAD_STATE_IDLE) {
            if (MiscUtil.DEBUG)
                Log.d(TAG, "stopDecode return warning, stopped already, repeat call.");
        }

        decodeThreadState = THREAD_STATE_IDLE;
        doStopDecodeDirect();

        return MiscUtil.NO_ERROR;
    }

    /**
     * 关闭扫描sensor
     * @return 执行反馈
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public int closeScanner(){
        int retureCode = stopDecode();
        if(retureCode != MiscUtil.NO_ERROR) {
            if (MiscUtil.DEBUG)
                Log.e(TAG, "stopDecode before closeScanner, but stopDecode failed.");
            return retureCode;
        }
        if(mRqCameraManager != null) {
            mRqCameraManager.doCloseCamera();
        }
        return MiscUtil.NO_ERROR;
    }

    /**
     * 销毁一些初始化对象
     * @return 执行反馈
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public int destory(){
        if (!checkInitializedState()) {
            if (MiscUtil.DEBUG)
                Log.d(TAG, "destory failed, not initialize.");
            return RqDecoder.DECODE_ERROR_NOT_INIT;
        }

        if(mRqDecoder == null) {
            if (MiscUtil.DEBUG)
                Log.d(TAG, "destory failed, mRqDecoder is null.");
            return MiscUtil.ERROR_NULL_POINT;
        }
        if(mRqCameraManager == null) {
            if (MiscUtil.DEBUG)
                Log.d(TAG, "destory failed, mRqCameraManager is null.");
            return MiscUtil.ERROR_NULL_POINT;
        }

        /**
         * 销毁RqDecoder
         */
        int retureCode = mRqDecoder.destroy();
        if(retureCode != MiscUtil.NO_ERROR) {
            if (MiscUtil.DEBUG)
                Log.d(TAG, "destory mRqDecoder destroy failed, please check.");
            return retureCode;
        }
        /**
         * 去除解码图像回调
         */
        mRqFrameReader.setFrameCallback(null);
        /**
         * 关闭相机
         */
        mRqCameraManager.destory();
        /**
         * 停止解码线程
         */
        decodeThreadState = THREAD_STATE_NONE;
        synchronized (Lock.class) {
            Lock.class.notify();
        }
        /**
         * 销毁单例
         */
        instance = null;

        return retureCode;
    }


    /**
     * 数据桢回调，用户解码
     * @param frame
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onFrameCallback(Frame frame) {
        //synchronized (currentFrame) {
        /**
         * 解码frame和当前frame如果不是同一个，说明currentFrame是无用状态，进行一次回收
         */
        Frame recycleFrame = currentFrame;
        if(currentFrameTemp == null
                || (currentFrameTemp != null && !currentFrameTemp.equals(currentFrame))) {
            if (recycleFrame != null)
                recycleFrame.destory();
            recycleFrame = null;
        }
        currentFrame = frame;
        //}
    }

    /**
     * 适用桢对象Frame直接解码
     * @param frame
     * @return 执行反馈
     */
    public int doStartDecodeDirect(Frame frame){
        if (!checkInitializedState()) {
            if (MiscUtil.DEBUG)
                Log.d(TAG, "doStartDecodeDirect failed, not initialize.");
            return RqDecoder.DECODE_ERROR_NOT_INIT;
        }

        if(mRqDecoder == null) {
            if (MiscUtil.DEBUG)
                Log.d(TAG, "doStartDecodeDirect failed, mRqDecoder is null.");
            return MiscUtil.ERROR_NULL_POINT;
        }

        return mRqDecoder.startDecode(frame);
    }


    /**
     * 停止解码
     * @return 执行反馈
     */
    public int doStopDecodeDirect(){
        if (!checkInitializedState()) {
            if (MiscUtil.DEBUG)
                Log.d(TAG, "doStopDecodeDirect failed, not initialize.");
            return RqDecoder.DECODE_ERROR_NOT_INIT;
        }

        if(mRqDecoder == null) {
            if (MiscUtil.DEBUG)
                Log.d(TAG, "doStopDecodeDirect failed, mRqDecoder is null.");
            return MiscUtil.ERROR_NULL_POINT;
        }

        return mRqDecoder.stopDecode();
    }

    /**
     * 启动解码进程
     * @param state
     */
    private void startDecodeThread(int state) {
        decodeThreadState = state;
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public synchronized void  run(){
                while (decodeThreadState != THREAD_STATE_NONE) {
                    /**
                     * 判断是否初始化
                     */
                    if (!checkInitializedState()) {
                        decodeThreadState = THREAD_STATE_NONE;
                        if (MiscUtil.DEBUG)
                            Log.d(TAG, "DecodeThread startDecodeThread failed, not initialize.");
                        return;
                    }

                    /**
                     * 判断是否是Idel状态，暂停
                     */
                    if(decodeThreadState == THREAD_STATE_IDLE) {
                        synchronized (Lock.class) {
                            try{
                                if (MiscUtil.DEBUG)
                                    Log.d(TAG, "DecodeThread synchronized lock wait.");
                                Lock.class.wait();
                            }catch (InterruptedException e) {
                                if (MiscUtil.DEBUG)
                                    Log.d(TAG, "DecodeThread synchronized InterruptedException:"
                                            +e.getMessage());
                            }
                        }
                        continue;
                    }

                    /**
                     * 解码
                     */
                    if (currentFrame != null && !currentFrame.isEmpty()) {

                        currentFrameTemp = currentFrame;

                        /**
                         * 异步存图：每次重新创建线程存图，存储比较快，但可能丢帧
                         * 同步存图：速度比较慢，造成解码卡顿，需要借助getSaveTaskCache属性判断
                         */
                        if(!saveFileSynchronize) {
                            /**
                             * 如果开启异步存图模式(success,failed,all)，则降低解码频率，给予存图足够时间
                             * 否则会因为解码输出结果过快，存图线程未完成缓存buff未释放，
                             * 累计一定数量造成内存溢出
                             */
                            if(saveFrame == null) {
                                if (MiscUtil.DEBUG)
                                    Log.i(TAG, "saveFrame set value begin.");
                                synchronized (currentFrameTemp) {
                                    saveFrame = currentFrameTemp.clone();
                                }
                                if (MiscUtil.DEBUG)
                                    Log.i(TAG, "saveFrame set value end.");
                                //Thread.sleep(1000);
                            }
                        }

                        try {
                            if (MiscUtil.DEBUG)
                                Log.d(TAG, "DecodeThread loop.");
                            int errorCode = doStartDecodeDirect(currentFrameTemp);
                            if(errorCode != MiscUtil.NO_ERROR) {
                                if (MiscUtil.DEBUG)
                                    Log.e(TAG, "DecodeThread doDecode error:" + errorCode);
                            }
                            /**
                             * 如果需要输出图像
                             */
                            if(frameSaver.getFrameSaveType() == FrameSaver.SAVE_DEBUG) {
                                /**
                                 * debug模式为强制输出每桢图像
                                 */
                                String path = frameSaver.saveFrame(currentFrameTemp,false);
                                if(TextUtils.isEmpty(path)) {
                                    if (MiscUtil.DEBUG)
                                        Log.i(TAG, "frameSaver.saveFrame 1 fail.");
                                }
                            }

                            if(frameSaver.getFrameSaveType() != FrameSaver.SAVE_NONE
                                    && frameSaver.getFrameSaveType() != FrameSaver.SAVE_DEBUG) {
                                if(saveFileSynchronize) {
                                    /**
                                     * 检测等待存图任务
                                     */
                                    if (MiscUtil.DEBUG)
                                        Log.d(TAG, "DecodeThread check save task num:" + frameSaver.getSaveTaskCache().size());
                                    int skipTimes = 20;
                                    while (frameSaver.getSaveTaskCache().size() == 0) {
                                        Thread.sleep(30);
                                        if (frameSaver.getSaveTaskCache().size() > 0) {
                                            break;
                                        }
                                        skipTimes--;
                                        if (skipTimes <= 0)
                                            break;
                                    }
                                    /**
                                     * 执行存图任务
                                     */
                                    if (MiscUtil.DEBUG)
                                        Log.d(TAG, "DecodeThread do save task num:" + frameSaver.getSaveTaskCache().size());
                                    if (frameSaver.getSaveTaskCache().size() > 0) {
                                        /**
                                         * 因为目前没有对存图有高标准要求，
                                         * 不管缓存多少任务，检测到缓存任务之后只存一张图，
                                         * 如果后续对缓存图像有精准要求，需要进行Frame缓存
                                         */
                                        String path = frameSaver.saveFrame(currentFrameTemp,
                                                frameSaver.getSaveTaskCache().get(
                                                        frameSaver.getSaveTaskCache().size() - 1));
                                        if(TextUtils.isEmpty(path)) {
                                            if (MiscUtil.DEBUG)
                                                Log.i(TAG, "frameSaver.saveFrame 2 fail.");
                                        }

                                        frameSaver.getSaveTaskCache().clear();
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            if (MiscUtil.DEBUG)
                                Log.e(TAG, "DecodeThread doDecode exception:" + ex.getMessage());
                        } finally {
                            recycleTempFrameBuffer();
                        }
                    } else {
                        if (MiscUtil.DEBUG)
                            Log.d(TAG, "DecodeThread currentFrameTemp rightFrame is null!!");

                    }
                }

                if (MiscUtil.DEBUG)
                    Log.d(TAG, "DecodeThread end, trace decodeThreadState:"+decodeThreadState);
            }
        }).start();
    }

    /**
     * 回收缓存frame
     */
    private void recycleTempFrameBuffer(){
        if(currentFrameTemp != null)
            currentFrameTemp.destory();
        currentFrameTemp = null;
    }

    /**
     * 回收缓存frame
     */
    private void recycleSaveFrameBuffer(){
        if(saveFrame != null)
            saveFrame.destory();
        saveFrame = null;
    }



    /**
     * 添加扫描回调，适用于一些业务逻辑：
     * 成功或失败时存Frame为文件
     * 扫描状态控制,etc.
     * @param s
     * @param symbologyType
     * @param corner
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onResultCallback(final String s, RqSymbologyType symbologyType, int[] corner) {
        if (MiscUtil.DEBUG)
            Log.d(TAG, "onResultCallback:"+s);
        /**
         * 非连续读码，扫一票就停止扫码
         */
        if(!continueScanMode)
            stopDecode();
        /**
         * 输出图像控制，成功时输出
         */
        if(frameSaver.getFrameSaveType() == FrameSaver.SAVE_WHEN_DECODE_SUCCESS
                || frameSaver.getFrameSaveType() == FrameSaver.SAVE_ALL) {
            if(saveFileSynchronize) {
                frameSaver.getSaveTaskCache().add(true);
            } else {
                if(!saving) {

                    if (saveFrame == null || saveFrame.isEmpty()) {
                        if (MiscUtil.DEBUG)
                            Log.e(TAG, "saveFrame error, frame is null or empty.");
                        return;
                    }
                    if (MiscUtil.DEBUG)
                        Log.i(TAG, "saveFrame begin.");
                    /**
                     * 再次clone，保护数据的安全性
                     */
                    final Frame saveFrameTmp;
                    synchronized (saveFrame) {
                        saveFrameTmp = saveFrame.clone();
                        recycleSaveFrameBuffer();
                    }
                    /**
                     * 设置条码信息到frame中
                     */
                    saveFrameTmp.setBarcode(s);
                    /**
                     * 存图线程
                     */
                    new Thread(new Runnable() {
                        @Override
                        public synchronized void run() {
                            saving = true;
                            /**
                             * 使用条码信息存图，重复则直接返回null
                             */
                            String path = frameSaver.saveFrame(saveFrameTmp);
                            if (TextUtils.isEmpty(path)) {
                                if (MiscUtil.DEBUG)
                                    Log.i(TAG, "frameSaver.saveFrame 5 fail.");
                            }
                            saveFrameTmp.destory();
                            saving = false;
                            if (MiscUtil.DEBUG)
                                Log.i(TAG, "saveFrame end.");
                        }
                    }).start();
                }
            }
        }else if(!saveFileSynchronize
                && frameSaver.getFrameSaveType() == FrameSaver.SAVE_WHEN_DECODE_FAILED){
            recycleSaveFrameBuffer();
        }
    }

    /**
     * 添加扫描回调，适用于一些业务逻辑：
     * 成功或失败时存Frame为文件
     * 扫描状态控制,etc.
     * @param strings
     * @param symbologyTypes
     * @param corners
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onMultipleResultCallback(final String[] strings, RqSymbologyType[] symbologyTypes, List<int[]> corners) {
        if (MiscUtil.DEBUG)
            Log.d(TAG, "onResultCallback:"+strings.length);
        /**
         * 非连续读码，扫一票就停止扫码
         */
        if(!continueScanMode)
            stopDecode();
        /**
         * 输出图像控制，成功时输出
         */
        if(frameSaver.getFrameSaveType() == FrameSaver.SAVE_WHEN_DECODE_SUCCESS
                || frameSaver.getFrameSaveType() == FrameSaver.SAVE_ALL) {
            if(saveFileSynchronize) {
                frameSaver.getSaveTaskCache().add(true);
            } else {
                if(!saving) {
                    if (saveFrame == null || saveFrame.isEmpty()) {
                        if (MiscUtil.DEBUG)
                            Log.e(TAG, "saveFrame error, frame is null or empty.");
                        return;
                    }

                    /**
                     * 再次clone，保护数据的安全性
                     */
                    final Frame saveFrameTmp;
                    synchronized (saveFrame) {
                        saveFrameTmp = saveFrame.clone();
                        recycleSaveFrameBuffer();
                    }
                    saveFrameTmp.setBarcode(strings[0]);

                    /**
                     * 存图线程
                     */
                    new Thread(new Runnable() {
                        @Override
                        public synchronized void run() {
                            saving = true;
                            String path = frameSaver.saveFrame(saveFrameTmp);
                            if (TextUtils.isEmpty(path)) {
                                if (MiscUtil.DEBUG)
                                    Log.i(TAG, "frameSaver.saveFrame 4 fail.");
                            }
                            saveFrameTmp.destory();
                            saving = false;
                        }
                    }).start();
                }
            }
        } else if(!saveFileSynchronize
                && frameSaver.getFrameSaveType() == FrameSaver.SAVE_WHEN_DECODE_FAILED){
            recycleSaveFrameBuffer();
        }
    }

    /**
     * 扫描失败回调
     * @param errorCode
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onDecodeError(int errorCode) {
//        /**
//         * 非连续读码，扫一票就停止扫码
//         */
//        if(!continueScanMode)
//            stopDecode();
//        /**
//         * 输出图像控制，失败时输出
//         */
//        if(frameSaver.getFrameSaveType() == FrameSaver.SAVE_WHEN_DECODE_FAILED
//                || frameSaver.getFrameSaveType() == FrameSaver.SAVE_ALL) {
//            if(saveFileSynchronize) {
//                frameSaver.getSaveTaskCache().add(false);
//            } else {
//                if(!saving) {
//                    final Frame saveFrameTmp;
//                    synchronized (saveFrame) {
//                        saveFrameTmp = saveFrame.clone();
//                        recycleSaveFrameBuffer();
//                    }
//                    new Thread(new Runnable() {
//                        @Override
//                        public synchronized void run() {
//                            saving = true;
//
//                            String path = frameSaver.saveFrame(saveFrameTmp, false);
//
//                            if (TextUtils.isEmpty(path)) {
//                                if (MiscUtil.DEBUG)
//                                    Log.i(TAG, "frameSaver.saveFrame 3 fail.");
//                            }
//                            saveFrameTmp.destory();
//                            saving = false;
//                        }
//                    }).start();
//                }
//            }
//        } else if(!saveFileSynchronize
//                && frameSaver.getFrameSaveType() == FrameSaver.SAVE_WHEN_DECODE_SUCCESS){
//            recycleSaveFrameBuffer();
//        }
    }

    /**
     * 判断初始化状态
     * 调用initialized时如果为false,
     * 再去检测下初始化状态，可能注册用时比较久
     * @return 检测结果
     */
    public boolean checkInitializedState(){
        if (!initialized) {
            initialized = mRqDecoder.getActivateTool().getActivateState() == ActivateTool.ACTIVATIED;
        }

        return initialized;
    }

    /**
     * 补光灯亮度调节
     * @param val 亮度值：1~10
     */
    public void setIllState(int val) {
        miscJNI.SetIllState(val);
    }

    /**
     * 伽玛开关
     * @param val 0/1
     */
    public void setGammaOn(boolean val) {
        miscJNI.setGammaState(val?0:1);
    }

}
