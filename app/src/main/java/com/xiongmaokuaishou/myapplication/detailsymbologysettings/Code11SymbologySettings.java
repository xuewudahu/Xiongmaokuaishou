package com.xiongmaokuaishou.myapplication.detailsymbologysettings;


import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

import androidx.annotation.RequiresApi;

import com.rq.barcode.RqDecoder;
import com.rq.barcode.RqEngineer;
import com.rq.barcode.RqSymbologyConfig;
import com.rq.barcode.RqSymbologyConfigValue;
import com.rq.barcode.RqSymbologyType;
import com.xiongmaokuaishou.myapplication.R;

@SuppressWarnings("deprecation")
public class Code11SymbologySettings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    static final String TAG = Code11SymbologySettings.class.getSimpleName();
    private ListPreference mCode11Checksum;
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
        addPreferencesFromResource(R.xml.code11_symbology_settings);
        mCode11Checksum = (ListPreference) getPreferenceScreen().findPreference("code11_checksum");

        /**
         * 初始化扫描参数preference
         */
        key = RqSymbologyType.SymbologyType_Code11.getSampleName();
        rqEngineer = RqEngineer.getInstence(this).getRqDecoder();
        pullPreference = rqEngineer.pullBarcodePreference(key);
        //获取checksum
        String checksum = pullPreference.getString(key+RqSymbologyConfig.CHECKSUM_SUFFIX,"");

        mCode11Checksum.setSummary(getCode11ChecksumEntryForSummary(checksum));
        mCode11Checksum.setValue(checksum);


        if (getResources().getConfiguration().smallestScreenWidthDp < 600) {
            // Lock phone form factor to portrait.
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String tkey) {
        mCode11Checksum.setSummary(mCode11Checksum.getEntry());

        /**
         * 更新设置到条码库
         */
        SharedPreferences.Editor e = pullPreference.edit();
        e.putString(key+ RqSymbologyConfig.CHECKSUM_SUFFIX,mCode11Checksum.getValue());
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

    private CharSequence getCode11ChecksumEntryForSummary(String checkSum) {
        switch (checkSum) {
            case RqSymbologyConfigValue
                    .Code11.Code11PropertiesChecksum_Disabled:
                return mCode11Checksum.getEntries()[0];
            case RqSymbologyConfigValue
                    .Code11.Code11PropertiesChecksum_Disabled1Digit:
                return mCode11Checksum.getEntries()[1];
            case RqSymbologyConfigValue
                    .Code11.Code11PropertiesChecksum_Enabled1Digit:
                return mCode11Checksum.getEntries()[2];
            case RqSymbologyConfigValue
                    .Code11.Code11PropertiesChecksum_Disabled2Digit:
                return mCode11Checksum.getEntries()[3];
            case RqSymbologyConfigValue
                    .Code11.Code11PropertiesChecksum_Enabled2Digit:
                return mCode11Checksum.getEntries()[4];
        }
        return mCode11Checksum.getEntries()[0];
    }
}
