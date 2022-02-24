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
public class IATA2of5SymbologySettings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    static final String TAG = IATA2of5SymbologySettings.class.getSimpleName();
    private SeekBarPreference mIATA2o5SeekBar;
    private ListPreference mIATA2o5Checksum;
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
        addPreferencesFromResource(R.xml.iata2o5_symbology_settings);
        /**
         * 初始化扫描参数preference
         */
        key = RqSymbologyType.SymbologyType_IATA2of5.getSampleName();
        rqEngineer = RqEngineer.getInstence(this).getRqDecoder();
        pullPreference = rqEngineer.pullBarcodePreference(key);
        //获取MinChars
        int  minChar = pullPreference.getInt(key+ RqSymbologyConfig.MIN_CHARS_SUFFIX,0);
        //获取checksum
        String checksum = pullPreference.getString(key+RqSymbologyConfig.CHECKSUM_SUFFIX,"");

        mIATA2o5SeekBar = (SeekBarPreference) getPreferenceScreen().findPreference("iata2o5_min_chars");
        mIATA2o5SeekBar.setSummary(Integer.toString(minChar));
        mIATA2o5SeekBar.setProgress(minChar);

        mIATA2o5Checksum = (ListPreference) getPreferenceScreen().findPreference("iata2o5_checksum");
        mIATA2o5Checksum.setSummary(get2of5ChecksumEntryForSummary(checksum));
        mIATA2o5Checksum.setValue(checksum);


        if (getResources().getConfiguration().smallestScreenWidthDp < 600) {
            // Lock phone form factor to portrait.
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String tkey) {
        Log.i(TAG, "onSharedPreferenceChanged(" + key + ")");
        mIATA2o5SeekBar.setSummary(Integer.toString(mIATA2o5SeekBar.getProgress()));
        mIATA2o5Checksum.setSummary(mIATA2o5Checksum.getEntry());
        /**
         * 更新设置到条码库
         */
        SharedPreferences.Editor e = pullPreference.edit();
        e.putString(key+ RqSymbologyConfig.CHECKSUM_SUFFIX,mIATA2o5Checksum.getValue());
        e.putInt(key+ RqSymbologyConfig.MIN_CHARS_SUFFIX,mIATA2o5SeekBar.getProgress());
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


    private CharSequence get2of5ChecksumEntryForSummary(String checksum) {
        switch (checksum) {
            case RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled:
                return mIATA2o5Checksum.getEntries()[0];
            case RqSymbologyConfigValue.NormalCheckSum.Checksum_Enabled:
                return mIATA2o5Checksum.getEntries()[1];
            case RqSymbologyConfigValue.NormalCheckSum.Checksum_EnabledStripCheckCharacter:
                return mIATA2o5Checksum.getEntries()[2];
            default:
                return mIATA2o5Checksum.getEntries()[0];
        }
    }
}
