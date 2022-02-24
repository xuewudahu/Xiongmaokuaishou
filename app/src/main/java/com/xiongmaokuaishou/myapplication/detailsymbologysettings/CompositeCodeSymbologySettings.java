package com.xiongmaokuaishou.myapplication.detailsymbologysettings;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.rq.barcode.RqDecoder;
import com.rq.barcode.RqEngineer;
import com.rq.barcode.RqSymbologyType;
import com.xiongmaokuaishou.myapplication.R;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("deprecation")
public class CompositeCodeSymbologySettings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {

    static final String TAG = CompositeCodeSymbologySettings.class.getSimpleName();
    private PreferenceScreen mCompositeCodeSettingsScreen;
    private Set<String> mCCPreferenceSet = new HashSet<>();
    private CheckBoxPreference mCCA;
    private CheckBoxPreference mCCB;
    private CheckBoxPreference mCCC;
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
        addPreferencesFromResource(R.xml.compositecode_symbology_settings);
        /**
         * 初始化扫描参数preference
         */
        key = RqSymbologyType.SymbologyType_COMPOSITE.getSampleName();
        rqEngineer = RqEngineer.getInstence(this).getRqDecoder();
        pullPreference = rqEngineer.pullBarcodePreference(key);
        //获取子开关状态
        boolean  isCca = pullPreference.getBoolean(RqSymbologyType.SymbologyType_CCA.getSampleName(),false);
        //获取子开关状态
        boolean  isCcb = pullPreference.getBoolean(RqSymbologyType.SymbologyType_CCB.getSampleName(),false);
        //获取子开关状态
        boolean  isCcc = pullPreference.getBoolean(RqSymbologyType.SymbologyType_CCC.getSampleName(),false);

        mCompositeCodeSettingsScreen = (PreferenceScreen) getPreferenceScreen().findPreference("compositecode_settings");


        mCCA = (CheckBoxPreference) getPreferenceScreen().findPreference("cca");
        mCCA.setChecked(isCca);

        mCCB = (CheckBoxPreference) getPreferenceScreen().findPreference("ccb");
        mCCB.setChecked(isCcb);

        mCCC = (CheckBoxPreference) getPreferenceScreen().findPreference("ccc");
        mCCC.setChecked(isCcc);


        if (getResources().getConfiguration().smallestScreenWidthDp < 600) {
            // Lock phone form factor to portrait.
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private void loadCheckedPreferencesToSet() {
        for (int i = 0; i < mCompositeCodeSettingsScreen.getPreferenceCount(); i++) {
            if (((CheckBoxPreference) mCompositeCodeSettingsScreen.getPreference(i)).isChecked()) {
                mCCPreferenceSet.add(mCompositeCodeSettingsScreen.getPreference(i).getKey());
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    private void initializeClickListener() {
        for (int i = 0; i < mCompositeCodeSettingsScreen.getPreferenceCount(); i++) {
            mCompositeCodeSettingsScreen.getPreference(i).setOnPreferenceClickListener(this);
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
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String tkey) {
        /**
         * 更新设置到条码库
         */
        SharedPreferences.Editor e = pullPreference.edit();
        e.putBoolean(RqSymbologyType.SymbologyType_CCA.getSampleName(),mCCA.isChecked());
        e.putBoolean(RqSymbologyType.SymbologyType_CCB.getSampleName(),mCCB.isChecked());
        e.putBoolean(RqSymbologyType.SymbologyType_CCC.getSampleName(),mCCC.isChecked());
        e.apply();
        rqEngineer.pushBarcodePreference(key,sharedPreferences);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (((CheckBoxPreference) preference).isChecked() && !mCCPreferenceSet.contains(preference.getKey())) {
            mCCPreferenceSet.add(preference.getKey());
        } else if (!((CheckBoxPreference) preference).isChecked() && mCCPreferenceSet.contains(preference.getKey())) {
            if (mCCPreferenceSet.size() > 1) {
                mCCPreferenceSet.remove(preference.getKey());
            } else {
                ((CheckBoxPreference) preference).setChecked(true);
                Toast.makeText(this, "Atleast one setting should be enabled", Toast.LENGTH_SHORT).show();
                return true;
            }
        }

        return false;
    }


}
