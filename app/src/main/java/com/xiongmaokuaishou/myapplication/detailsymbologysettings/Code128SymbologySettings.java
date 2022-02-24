package com.xiongmaokuaishou.myapplication.detailsymbologysettings;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.rq.barcode.RqDecoder;
import com.rq.barcode.RqEngineer;
import com.rq.barcode.RqSymbologyConfig;
import com.rq.barcode.RqSymbologyType;
import com.xiongmaokuaishou.myapplication.R;
import com.xiongmaokuaishou.myapplication.SeekBarPreference;

@SuppressWarnings("deprecation")
public class Code128SymbologySettings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    static final String TAG = Code128SymbologySettings.class.getSimpleName();
    private SeekBarPreference mCode128SeekBar;

    /**
     * 定义存储对象
     */
    private RqDecoder rqEngineer;
    private SharedPreferences pullPreference;
    private String key;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.code128_symbology_settings);
        /**
         * 初始化扫描参数preference
         */
        key = RqSymbologyType.SymbologyType_Code128_CCA.getSampleName();
        rqEngineer = RqEngineer.getInstence(this).getRqDecoder();
        pullPreference = rqEngineer.pullBarcodePreference(key);
        //获取MinChars
        int  minChar = pullPreference.getInt(key+ RqSymbologyConfig.MIN_CHARS_SUFFIX,0);

        mCode128SeekBar = (SeekBarPreference) getPreferenceScreen().findPreference("code128_min_chars");

        mCode128SeekBar.setSummary(Integer.toString(minChar));
        mCode128SeekBar.setProgress(minChar);


        if (getResources().getConfiguration().smallestScreenWidthDp < 600) {
            // Lock phone form factor to portrait.
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String tkey) {
        Log.i(TAG, "onSharedPreferenceChanged(" + key + ")");
        mCode128SeekBar.setSummary(Integer.toString(mCode128SeekBar.getProgress()));
        /**
         * 更新设置到条码库
         */
        SharedPreferences.Editor e = pullPreference.edit();
        e.putInt(key+ RqSymbologyConfig.MIN_CHARS_SUFFIX,mCode128SeekBar.getProgress());
        e.apply();
        rqEngineer.pushBarcodePreference(key,pullPreference);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
