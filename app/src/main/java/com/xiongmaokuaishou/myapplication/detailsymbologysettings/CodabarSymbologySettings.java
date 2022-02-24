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
public class CodabarSymbologySettings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    static final String TAG = CodabarSymbologySettings.class.getSimpleName();
    private SeekBarPreference mCodabarSeekBar;
    private ListPreference mCodabarChecksum;
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
        addPreferencesFromResource(R.xml.codabar_symbology_settings);

        /**
         * 初始化扫描参数preference
         */
        key = RqSymbologyType.SymbologyType_Codabar.getSampleName();
        rqEngineer = RqEngineer.getInstence(this).getRqDecoder();
        pullPreference = rqEngineer.pullBarcodePreference(key);
        //获取MinChars
        int  minChar = pullPreference.getInt(key+ RqSymbologyConfig.MIN_CHARS_SUFFIX,0);
        //获取checksum
        String checksum = pullPreference.getString(key+RqSymbologyConfig.CHECKSUM_SUFFIX,"");

        mCodabarSeekBar = (SeekBarPreference) getPreferenceScreen().findPreference("codabar_min_chars");
        mCodabarSeekBar.setSummary(Integer.toString(minChar));
        mCodabarSeekBar.setProgress(minChar);
        mCodabarChecksum = (ListPreference) getPreferenceScreen().findPreference("codabar_checksum");

        mCodabarChecksum.setValue(checksum);
        mCodabarChecksum.setSummary(getCodabarChecksumEntryForSummary(checksum));

        if (getResources().getConfiguration().smallestScreenWidthDp < 600) {
            // Lock phone form factor to portrait.
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String tkey) {
        Log.i(TAG, "onSharedPreferenceChanged(" + key + ")");
        mCodabarSeekBar.setSummary(Integer.toString(mCodabarSeekBar.getProgress()));
        mCodabarChecksum.setSummary(mCodabarChecksum.getEntry());

        /**
         * 更新设置到条码库
         */
        SharedPreferences.Editor e = pullPreference.edit();
        e.putInt(key+ RqSymbologyConfig.MIN_CHARS_SUFFIX,mCodabarSeekBar.getProgress());
        e.putString(key+ RqSymbologyConfig.CHECKSUM_SUFFIX,mCodabarChecksum.getValue());
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

    private CharSequence getCodabarChecksumEntryForSummary(String checksum) {
        switch (checksum) {
            case RqSymbologyConfigValue.NormalCheckSum.Checksum_Disabled:
                return mCodabarChecksum.getEntries()[0];
            case RqSymbologyConfigValue.NormalCheckSum.Checksum_Enabled:
                return mCodabarChecksum.getEntries()[1];
            case RqSymbologyConfigValue.NormalCheckSum.Checksum_EnabledStripCheckCharacter:
                return mCodabarChecksum.getEntries()[2];
            default:
                return mCodabarChecksum.getEntries()[0];
        }
    }

}
