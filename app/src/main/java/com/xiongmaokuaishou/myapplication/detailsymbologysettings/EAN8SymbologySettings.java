package com.xiongmaokuaishou.myapplication.detailsymbologysettings;


import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;

import androidx.annotation.RequiresApi;

import com.rq.barcode.RqDecoder;
import com.rq.barcode.RqEngineer;
import com.rq.barcode.RqSymbologyConfig;
import com.rq.barcode.RqSymbologyType;
import com.xiongmaokuaishou.myapplication.R;

@SuppressWarnings("deprecation")
public class EAN8SymbologySettings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    static final String TAG = EAN8SymbologySettings.class.getSimpleName();
    private CheckBoxPreference m2DigitSupplemental;
    private CheckBoxPreference m5DigitSupplemental;
    private CheckBoxPreference mAddSpace;
    private CheckBoxPreference mRequireSupplemental;
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
        addPreferencesFromResource(R.xml.ean8_symbology_settings);
        /**
         * 初始化扫描参数preference
         */
        key = RqSymbologyType.SymbologyType_EAN8.getSampleName();
        rqEngineer = RqEngineer.getInstence(this).getRqDecoder();
        pullPreference = rqEngineer.pullBarcodePreference(key);
        //获取属性
        boolean  m2DigitSupplementalV = pullPreference.getBoolean(key+ RqSymbologyConfig.TWO_DIGIT_SUPP,false);
        boolean  m5DigitSupplementalV = pullPreference.getBoolean(key+ RqSymbologyConfig.FIVE_DIGIT_SUPP,false);
        boolean  mAddSpaceV = pullPreference.getBoolean(key+ RqSymbologyConfig.ADD_SPACE,false);
        boolean  mRequireSupplementalV = pullPreference.getBoolean(key+ RqSymbologyConfig.REQUIRE_SUPP,false);


        m2DigitSupplemental = (CheckBoxPreference) getPreferenceScreen().findPreference("ean8_2digit_supp");
        m2DigitSupplemental.setChecked(m2DigitSupplementalV);

        m5DigitSupplemental = (CheckBoxPreference) getPreferenceScreen().findPreference("ean8_5digit_supp");
        m5DigitSupplemental.setChecked(m5DigitSupplementalV);

        mAddSpace = (CheckBoxPreference) getPreferenceScreen().findPreference("ean8_add_space");
        mAddSpace.setChecked(mAddSpaceV);

        mRequireSupplemental = (CheckBoxPreference) getPreferenceScreen().findPreference("ean8_require_supp");
        mRequireSupplemental.setChecked(mRequireSupplementalV);

        loadSettings();

        if (getResources().getConfiguration().smallestScreenWidthDp < 600) {
            // Lock phone form factor to portrait.
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String tkey) {
        loadSettings();
        /**
         * 更新设置到条码库
         */
        SharedPreferences.Editor e = pullPreference.edit();
        e.putBoolean(key+ RqSymbologyConfig.TWO_DIGIT_SUPP,m2DigitSupplemental.isChecked());
        e.putBoolean(key+ RqSymbologyConfig.FIVE_DIGIT_SUPP,m5DigitSupplemental.isChecked());
        e.putBoolean(key+ RqSymbologyConfig.ADD_SPACE,mAddSpace.isChecked());
        e.putBoolean(key+ RqSymbologyConfig.REQUIRE_SUPP,mRequireSupplemental.isChecked());
        e.apply();
        rqEngineer.pushBarcodePreference(key,pullPreference);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        loadSettings();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    public void loadSettings() {
        if (!m2DigitSupplemental.isChecked() && !m5DigitSupplemental.isChecked()) {
            mAddSpace.setChecked(false);
            mAddSpace.setEnabled(false);

            mRequireSupplemental.setChecked(false);
            mRequireSupplemental.setEnabled(false);
        } else {
            mAddSpace.setEnabled(true);
            mRequireSupplemental.setEnabled(true);
        }
    }
}
