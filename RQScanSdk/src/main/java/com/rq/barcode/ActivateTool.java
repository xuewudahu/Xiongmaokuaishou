package com.rq.barcode;

/**
 * 算法激活工具类
 */
public abstract class ActivateTool {
    /**
     * 注册状态机
     */
    public static final int UNACTIVATIED = 1;
    public static final int ACTIVATING = 2;
    public static final int ACTIVATIED = 3;

    /**
     * 构造函数
     * @param rqDecoder 传输用于注册的对象
     */
    public ActivateTool(RqDecoder rqDecoder) {}
    /**
     * 注册激活
     * @param code
     */
    public abstract void activateLicense(String code);

    /**
     * 返回注册状态
     * @return 注册状态
     */
    public abstract int getActivateState();

    /**
     * 注册状态变化回调
     * @param activateCallback
     */
    public abstract void setActivateCallback(ActivateCallback activateCallback);


    /**
     * 注册状态变化回调
     * @deprecated
     */
    public interface ActivateCallback{
        /**
         * 注册监听
         * @param newState
         */
        void onActivateStateChange(int newState);

        /**
         * 设备信息生成回调
         * @param resultCode
         * @param filePath
         */
        void onDeviceIDFileGenerated(int resultCode, String filePath);
    }
}
