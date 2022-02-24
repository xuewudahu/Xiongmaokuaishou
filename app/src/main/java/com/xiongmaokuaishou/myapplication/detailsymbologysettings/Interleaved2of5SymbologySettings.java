package com.xiongmaokuaishou.myapplication.detailsymbologysettings;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.rq.barcode.RqDecoder;
import com.rq.barcode.RqEngineer;
import com.rq.barcode.RqSymbologyConfig;
import com.rq.barcode.RqSymbologyConfigValue;
import com.rq.barcode.RqSymbologyType;
import com.xiongmaokuaishou.myapplication.R;
import com.xiongmaokuaishou.myapplication.SeekBarPreference;

@SuppressWarnings("deprecation")
public class Interleaved2of5SymbologySettings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    static final String TAG = Interleaved2of5SymbologySettings.class.getSimpleName();
    private SeekBarPreference mInterleaved2o5SeekBar;
    private ListPreference mInterleaved2o5Checksum;
    private CheckBoxPreference mAllowShortQuietZone;
    private CheckBoxPreference mRejectPartialDecode;
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
        addPreferencesFromResource(R.xml.interleaved2o5_symbology_settings);
        /**
         * 初始化扫描参数preference
         */
        key = RqSymbologyType.SymbologyType_Interleaved2of5.getSampleName();
        rqEngineer = RqEngineer.getInstence(this).getRqDecoder();
        pullPreference = rqEngineer.pullBarcodePreference(key);
        //获取MinChars
        int  minChar = pullPreference.getInt(key+ RqSymbologyConfig.MIN_CHARS_SUFFIX,0);
        //获取checksum
        String checksum = pullPreference.getString(key+RqSymbologyConfig.CHECKSUM_SUFFIX,"");
        //其他属性
        boolean mAllowShortQuietZoneV = pullPreference.getBoolean(key+RqSymbologyConfig.ALLOW_SHORTQUIETZONE,false);
        boolean mRejectPartialDecodeV = pullPreference.getBoolean(key+RqSymbologyConfig.REJECT_PARTIALDECODE,false);

        mInterleaved2o5SeekBar = (SeekBarPreference) getPreferenceScreen().findPreference("interleaved2o5_min_chars");
        mInterleaved2o5SeekBar.setSummary(Integer.toString(minChar));
        mInterleaved2o5SeekBar.setProgress(minChar);

        mInterleaved2o5Checksum = (ListPreference) getPreferenceScreen().findPreference("interleaved2o5_checksum");
        mInterleaved2o5Checksum.setSummary(get2of5ChecksumEntryForSummary(checksum));
        mInterleaved2o5Checksum.setValue(checksum);



        mAllowShortQuietZone = (CheckBoxPreference) getPreferenceScreen().findPreference("interleaved2o5_allow_shortQuietZone");
        mAllowShortQuietZone.setChecked(mAllowShortQuietZoneV);

        mRejectPartialDecode = (CheckBoxPreference) getPreferenceScreen().findPreference("interleaved2o5_reject_partialDecode");
        mRejectPartialDecode.setChecked(mRejectPartialDecodeV);

        if (getResources().getConfiguration().smallestScreenWidthDp < 600) {
            // Lock phone form factor to portrait.
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String tkey) {
        Log.i(TAG, "onSharedPreferenceChanged(" + key + ")");
        if (mInterleaved2o5SeekBar.getProgress() % 2 != 0 || mInterleaved2o5SeekBar.getProgress() < 2) {
            showToastForOddMinCharsVal();
            mInterleaved2o5SeekBar.setProgress(8); //If the selected value is an odd number then default it back to 2
        }
        mInterleaved2o5Checksum.setSummary(mInterleaved2o5Checksum.getEntry());
        mInterleaved2o5SeekBar.setSummary(Integer.toString(mInterleaved2o5SeekBar.getProgress()));

        /**
         * 更新设置到条码库
         */
        SharedPreferences.Editor e = pullPreference.edit();
        e.putString(key+ RqSymbologyConfig.CHECKSUM_SUFFIX,mInterleaved2o5Checksum.getValue());
        e.putInt(key+ RqSymbologyConfig.MIN_CHARS_SUFFIX,mInterleaved2o5SeekBar.getProgress());
        e.putBoolean(key+ RqSymbologyConfig.ALLOW_SHORTQUIETZONE,mAllowShortQuietZone.isChecked());
        e.putBoolean(key+ RqSymbologyConfig.REJECT_PARTIALDECODE,mRejectPartialDecode.isChecked());
        e.apply();
        rqEngineer.pushBarcodePreference(key,pullPreference);
    }

    private void showToastForOddMinCharsVal() {
        Toast.makeText(this, "Please choose an even integer value for minimum characters", Toast.LENGTH_SHORT).show();
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
                return mInterleaved2o5Checksum.getEntries()[0];
            case RqSymbologyConfigValue.NormalCheckSum.Checksum_Enabled:
                return mInterleaved2o5Checksum.getEntries()[1];
            case RqSymbologyConfigValue.NormalCheckSum.Checksum_EnabledStripCheckCharacter:
                return mInterleaved2o5Checksum.getEntries()[2];
            default:
                return mInterleaved2o5Checksum.getEntries()[0];
        }
    }
}
