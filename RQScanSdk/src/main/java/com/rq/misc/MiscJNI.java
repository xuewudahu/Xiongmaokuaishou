package com.rq.misc;

import android.util.Log;

/**
 * JNI工具类，用于硬件控制，比如补光灯调节
 * @deprecated
 */
public class MiscJNI {
    private static final String TAG = MiscUtil.getTag(MiscJNI.class);
    static {
        System.loadLibrary("lightctrl_factory");
    }

    private native void setAimState(int status);

    private native void setIllState(int status);

    public native void setGammaState(int status);

    public MiscJNI() {}

    public void SetIllState(int status) {
        if (MiscUtil.DEBUG)
            Log.d("MiscJNI", "setIllState - status:" + status);
        setIllState(status);
    }

    public void SetAimState(int status) {
        if (MiscUtil.DEBUG)
            Log.d("MiscJNI", "setAimState - status:" + status);
        setAimState(status);
    }


}
