package com.xiongmaokuaishou.myapplication.detailsymbologysettings;


import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.rq.barcode.RqDecoder;
import com.rq.barcode.RqEngineer;
import com.rq.barcode.RqSymbologyConfig;
import com.rq.barcode.RqSymbologyConfigValue;
import com.rq.barcode.RqSymbologyType;
import com.xiongmaokuaishou.myapplication.R;
import com.xiongmaokuaishou.myapplication.SeekBarPreference;

@SuppressWarnings("deprecation")
public class MSIPlesseySymbologySettings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    static final String TAG = MSIPlesseySymbologySettings.class.getSimpleName();
    private SeekBarPreference mMSIPlesseySeekBar;
    private ListPreference mMSIPlesseyChecksum;
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
        addPreferencesFromResource(R.xml.msiplessey_symbology_settings);
        /**
         * 初始化扫描参数preference
         */
        key = RqSymbologyType.SymbologyType_MSIPlessy.getSampleName();
        rqEngineer = RqEngineer.getInstence(this).getRqDecoder();
        pullPreference = rqEngineer.pullBarcodePreference(key);
        //获取MinChars
        int  minChar = pullPreference.getInt(key+ RqSymbologyConfig.MIN_CHARS_SUFFIX,0);
        //获取checksum
        String checksum = pullPreference.getString(key+RqSymbologyConfig.CHECKSUM_SUFFIX,"");

        mMSIPlesseySeekBar = (SeekBarPreference) getPreferenceScreen().findPreference("msiplessey_min_chars");
        mMSIPlesseySeekBar.setSummary(Integer.toString(minChar));
        mMSIPlesseySeekBar.setProgress(minChar);

        mMSIPlesseyChecksum = (ListPreference) getPreferenceScreen().findPreference("msiplessey_checksum");
        mMSIPlesseyChecksum.setSummary(getMSIPlesseyChecksumEntryForSummary(checksum));
        mMSIPlesseyChecksum.setValue(checksum);


        if (getResources().getConfiguration().smallestScreenWidthDp < 600) {
            // Lock phone form factor to portrait.
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String tkey) {
        Log.i(TAG, "onSharedPreferenceChanged(" + key + ")");
        mMSIPlesseySeekBar.setSummary(Integer.toString(mMSIPlesseySeekBar.getProgress()));
        mMSIPlesseyChecksum.setSummary(mMSIPlesseyChecksum.getEntry());
        /**
         * 更新设置到条码库
         */
        SharedPreferences.Editor e = pullPreference.edit();
        e.putString(key+ RqSymbologyConfig.CHECKSUM_SUFFIX,mMSIPlesseyChecksum.getValue());
        e.putInt(key+ RqSymbologyConfig.MIN_CHARS_SUFFIX,mMSIPlesseySeekBar.getProgress());
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

    private CharSequence getMSIPlesseyChecksumEntryForSummary(String checkSum) {
        switch (checkSum) {
            case RqSymbologyConfigValue
                    .MSIPlessy.MSIPlesseyPropertiesChecksum_Disabled:
                return mMSIPlesseyChecksum.getEntries()[0];
            case RqSymbologyConfigValue
                    .MSIPlessy.MSIPlesseyPropertiesChecksum_DisabledMod10:
                return mMSIPlesseyChecksum.getEntries()[1];
            case RqSymbologyConfigValue
                    .MSIPlessy.MSIPlesseyPropertiesChecksum_EnabledMod10:
                return mMSIPlesseyChecksum.getEntries()[2];
            case RqSymbologyConfigValue
                    .MSIPlessy.MSIPlesseyPropertiesChecksum_DisabledMod10_10:
                return mMSIPlesseyChecksum.getEntries()[3];
            case RqSymbologyConfigValue
                    .MSIPlessy.MSIPlesseyPropertiesChecksum_EnabledMod10_10:
                return mMSIPlesseyChecksum.getEntries()[4];
            case RqSymbologyConfigValue
                    .MSIPlessy.MSIPlesseyPropertiesChecksum_DisabledMod11_10:
                return mMSIPlesseyChecksum.getEntries()[5];
            case RqSymbologyConfigValue
                    .MSIPlessy.MSIPlesseyPropertiesChecksum_EnabledMod11_10:
                return mMSIPlesseyChecksum.getEntries()[6];
        }
        return mMSIPlesseyChecksum.getEntries()[0];
    }
}

