package com.xiongmaokuaishou.myapplication.detailsymbologysettings;


import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.rq.barcode.RqDecoder;
import com.rq.barcode.RqEngineer;
import com.rq.barcode.RqSymbologyConfig;
import com.rq.barcode.RqSymbologyType;
import com.xiongmaokuaishou.myapplication.R;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("deprecation")
public class GS1DataBarSymbologySettings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {

    static final String TAG = GS1DataBarSymbologySettings.class.getSimpleName();
    private HashSet mLicensedSymbologies;
    private PreferenceScreen mGS1DatabarSettingsScreen;
    private Set<String> mGS1PreferenceSet = new HashSet<>();

    private CheckBoxPreference mGS1DatabarOmni;
    private CheckBoxPreference mGS1DatabarStackedOmni;
    private CheckBoxPreference mGS1DatabarLimited;
    private CheckBoxPreference mGS1DatabarExpanded;
    private CheckBoxPreference mGS1DatabarExpandedStacked;
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
        addPreferencesFromResource(R.xml.gs1databar_symbology_settings);
        /**
         * 初始化扫描参数preference
         */
        key = RqSymbologyType.SymbologyType_GS1_DATABAR.getSampleName();
        rqEngineer = RqEngineer.getInstence(this).getRqDecoder();
        pullPreference = rqEngineer.pullBarcodePreference(key);
        //获取属性
        boolean  mGS1DatabarOmniV = pullPreference.getBoolean(key+ RqSymbologyConfig.OMNI,false);
        boolean  mGS1DatabarStackedOmniV = pullPreference.getBoolean(key+ RqSymbologyConfig.STACKED_OMNI,false);
        boolean  mGS1DatabarLimitedV = pullPreference.getBoolean(key+ RqSymbologyConfig.LIMITED,false);
        boolean  mGS1DatabarExpandedV = pullPreference.getBoolean(key+ RqSymbologyConfig.EXPANDED,false);
        boolean  mGS1DatabarExpandedStackedV = pullPreference.getBoolean(key+ RqSymbologyConfig.EXPANDED_STACKED,false);


        mGS1DatabarSettingsScreen = (PreferenceScreen) getPreferenceScreen().findPreference("gs1databar_settings");

        mGS1DatabarOmni = (CheckBoxPreference) getPreferenceScreen().findPreference("gs1databar_omni");
        mGS1DatabarOmni.setChecked(mGS1DatabarOmniV);

        mGS1DatabarStackedOmni = (CheckBoxPreference) getPreferenceScreen().findPreference("gs1databar_stacked_omni");
        mGS1DatabarStackedOmni.setChecked(mGS1DatabarStackedOmniV);

        mGS1DatabarLimited = (CheckBoxPreference) getPreferenceScreen().findPreference("gs1databar_limited");
        mGS1DatabarLimited.setChecked(mGS1DatabarLimitedV);

        mGS1DatabarExpanded = (CheckBoxPreference) getPreferenceScreen().findPreference("gs1databar_expanded");
        mGS1DatabarExpanded.setChecked(mGS1DatabarExpandedV);

        mGS1DatabarExpandedStacked = (CheckBoxPreference) getPreferenceScreen().findPreference("gs1databar_expanded_stacked");
        mGS1DatabarExpandedStacked.setChecked(mGS1DatabarExpandedStackedV);


        if (getResources().getConfiguration().smallestScreenWidthDp < 600) {
            // Lock phone form factor to portrait.
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private void loadCheckedPreferencesToSet() {
        for (int i = 0; i < mGS1DatabarSettingsScreen.getPreferenceCount(); i++) {
            if (((CheckBoxPreference) mGS1DatabarSettingsScreen.getPreference(i)).isChecked()) {
                mGS1PreferenceSet.add(mGS1DatabarSettingsScreen.getPreference(i).getKey());
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String tkey) {
        /**
         * 更新设置到条码库
         */
        Log.d("wxwLL","----");
        SharedPreferences.Editor e = pullPreference.edit();
        e.putBoolean(key+ RqSymbologyConfig.OMNI,mGS1DatabarOmni.isChecked());
        e.putBoolean(key+ RqSymbologyConfig.STACKED_OMNI,mGS1DatabarStackedOmni.isChecked());
        e.putBoolean(key+ RqSymbologyConfig.LIMITED,mGS1DatabarLimited.isChecked());
        e.putBoolean(key+ RqSymbologyConfig.EXPANDED,mGS1DatabarExpanded.isChecked());
        e.putBoolean(key+ RqSymbologyConfig.EXPANDED_STACKED,mGS1DatabarExpandedStacked.isChecked());
        e.apply();
        rqEngineer.pushBarcodePreference(key,pullPreference);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    private void initializeClickListener() {
        for (int i = 0; i < mGS1DatabarSettingsScreen.getPreferenceCount(); i++) {
            mGS1DatabarSettingsScreen.getPreference(i).setOnPreferenceClickListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        initializeClickListener();
        loadCheckedPreferencesToSet();
    }



    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (((CheckBoxPreference) preference).isChecked() && !mGS1PreferenceSet.contains(preference.getKey())) {
            mGS1PreferenceSet.add(preference.getKey());
        } else if (!((CheckBoxPreference) preference).isChecked() && mGS1PreferenceSet.contains(preference.getKey())) {
            if (mGS1PreferenceSet.size() > 1) {
                mGS1PreferenceSet.remove(preference.getKey());
            } else {
                ((CheckBoxPreference) preference).setChecked(true);
                Toast.makeText(this, "Atleast one setting should be enabled", Toast.LENGTH_SHORT).show();
                return true;
            }
        }

        return false;
    }
}
