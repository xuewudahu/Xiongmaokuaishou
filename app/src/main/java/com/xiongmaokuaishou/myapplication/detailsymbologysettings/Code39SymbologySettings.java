package com.xiongmaokuaishou.myapplication.detailsymbologysettings;


import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
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
public class Code39SymbologySettings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    static final String TAG = Code39SymbologySettings.class.getSimpleName();
    private SeekBarPreference mCode39SeekBar;
    private ListPreference mCode39Checksum;
    private CheckBoxPreference mFullASCIIModeEnabled;
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
        addPreferencesFromResource(R.xml.code39_symbology_settings);
        /**
         * 初始化扫描参数preference
         */
        key = RqSymbologyType.SymbologyType_Code39.getSampleName();
        rqEngineer = RqEngineer.getInstence(this).getRqDecoder();
        pullPreference = rqEngineer.pullBarcodePreference(key);
        //获取MinChars
        int  minChar = pullPreference.getInt(key+ RqSymbologyConfig.MIN_CHARS_SUFFIX,0);
        //获取checksum
        String checksum = pullPreference.getString(key+RqSymbologyConfig.CHECKSUM_SUFFIX,"");
        //获取asciimode
        boolean isAsciiMode = pullPreference.getBoolean(key+RqSymbologyConfig.ASCII_SUPPORT_SUFFIX,false);

        mCode39SeekBar = (SeekBarPreference) getPreferenceScreen().findPreference("code39_min_chars");
        mCode39SeekBar.setSummary(Integer.toString(minChar));
        mCode39SeekBar.setProgress(minChar);

        mFullASCIIModeEnabled = (CheckBoxPreference) getPreferenceScreen().findPreference("code39_ascii_support");
        mFullASCIIModeEnabled.setChecked(isAsciiMode);

        mCode39Checksum = (ListPreference) getPreferenceScreen().findPreference("code39_checksum");

        mCode39Checksum.setSummary(getCode39ChecksumEntryForSummary(checksum));
        mCode39Checksum.setValue(checksum);


        if (getResources().getConfiguration().smallestScreenWidthDp < 600) {
            // Lock phone form factor to portrait.
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String tkey) {
        Log.i(TAG, "onSharedPreferenceChanged(" + key + ")");
        mCode39SeekBar.setSummary(Integer.toString(mCode39SeekBar.getProgress()));
        mCode39Checksum.setSummary(mCode39Checksum.getEntry());

        /**
         * 更新设置到条码库
         */
        SharedPreferences.Editor e = pullPreference.edit();
        e.putString(key+ RqSymbologyConfig.CHECKSUM_SUFFIX,mCode39Checksum.getValue());
        e.putInt(key+ RqSymbologyConfig.MIN_CHARS_SUFFIX,mCode39SeekBar.getProgress());
        e.putBoolean(key + RqSymbologyConfig.ASCII_SUPPORT_SUFFIX,mFullASCIIModeEnabled.isChecked());
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

    private CharSequence getCode39ChecksumEntryForSummary(String checksum) {
        switch (checksum) {
            case RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled:
                return mCode39Checksum.getEntries()[0];
            case RqSymbologyConfigValue.NormalCheckSum.Checksum_Enabled:
                return mCode39Checksum.getEntries()[1];
            case RqSymbologyConfigValue.NormalCheckSum.Checksum_EnabledStripCheckCharacter:
                return mCode39Checksum.getEntries()[2];
            default:
                return mCode39Checksum.getEntries()[0];
        }
    }
}
