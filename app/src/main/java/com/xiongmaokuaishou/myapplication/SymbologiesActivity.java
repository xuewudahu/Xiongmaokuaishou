package com.xiongmaokuaishou.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.TwoStatePreference;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.xiongmaokuaishou.myapplication.detailsymbologysettings.CodabarSymbologySettings;
import com.xiongmaokuaishou.myapplication.detailsymbologysettings.Code11SymbologySettings;
import com.xiongmaokuaishou.myapplication.detailsymbologysettings.Code128SymbologySettings;
import com.xiongmaokuaishou.myapplication.detailsymbologysettings.Code39SymbologySettings;
import com.xiongmaokuaishou.myapplication.detailsymbologysettings.Code93SymbologySettings;
import com.xiongmaokuaishou.myapplication.detailsymbologysettings.CompositeCodeSymbologySettings;
import com.xiongmaokuaishou.myapplication.detailsymbologysettings.EAN13SymbologySettings;
import com.xiongmaokuaishou.myapplication.detailsymbologysettings.EAN8SymbologySettings;
import com.xiongmaokuaishou.myapplication.detailsymbologysettings.GS1DataBarSymbologySettings;
import com.xiongmaokuaishou.myapplication.detailsymbologysettings.HongKong2of5SymbologySettings;
import com.xiongmaokuaishou.myapplication.detailsymbologysettings.IATA2of5SymbologySettings;
import com.xiongmaokuaishou.myapplication.detailsymbologysettings.Interleaved2of5SymbologySettings;
import com.xiongmaokuaishou.myapplication.detailsymbologysettings.MSIPlesseySymbologySettings;
import com.xiongmaokuaishou.myapplication.detailsymbologysettings.Matrix2of5SymbologySettings;
import com.xiongmaokuaishou.myapplication.detailsymbologysettings.NEC2of5SymbologySettings;
import com.xiongmaokuaishou.myapplication.detailsymbologysettings.Straight2of5SymbologySettings;
import com.xiongmaokuaishou.myapplication.detailsymbologysettings.UPCASymbologySettings;
import com.xiongmaokuaishou.myapplication.detailsymbologysettings.UPCESymbologySettings;
import com.rq.barcode.RqDecoder;
import com.rq.barcode.RqEngineer;
import com.rq.barcode.RqSymbologyType;

@SuppressWarnings("deprecation")
public class SymbologiesActivity extends PreferenceActivity implements
        Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
    private static final String TAG = "SymbologiesActivity";
    private Preference mEnableAll;
    private Preference mDisableAll;
    private PreferenceScreen mSymbologies;
    private TwoStatePreference mAztec;
    private TwoStatePreference mCodablockF;
    private TwoStatePreference mCode32;
    private TwoStatePreference mCode49;
    private TwoStatePreference mDataMatrix;
    private TwoStatePreference mGridMatrix;
    private TwoStatePreference mDotCode;
    private TwoStatePreference mHanXinCode;
    private TwoStatePreference mMaxiCode;
    private TwoStatePreference mMicroPDF;
    private TwoStatePreference mMicroQR;
    private TwoStatePreference mPDF417;
    private TwoStatePreference mPlessey;
    private TwoStatePreference mQRCode;
    private TwoStatePreference mTelepen;
    private TwoStatePreference mTrioptic;
    /*
        private TwoStatePreference mAustraliaPost;
        private TwoStatePreference mCanadaPost;
        private TwoStatePreference mDutchPost;
        private TwoStatePreference mJapanPost;
        private TwoStatePreference mKoreaPost;
        private TwoStatePreference mRoyalPost;
        private TwoStatePreference mUSPSIntelligentMail;
        private TwoStatePreference mUSPSPlanet;
        private TwoStatePreference mUSPSPostnet;
        private TwoStatePreference mUPU;
    */
    private Preference mCodabar;
    private Preference mCode128;
    private Preference mCode11;
    private Preference mCode93;
    private Preference mCode39;
    private Preference mCodeEAN13;
    private Preference mCodeEAN8;
    private Preference mHK2o5;
    private Preference mIATA2o5;
    private Preference mInterleaved2o5;
    private Preference mMatrix2o5;
    private Preference mNEC2o5;
    private Preference mStraight2o5;
    private Preference mMSIPlessey;
    private Preference mUPCA;
    private Preference mUPCE;
    private Preference mGS1Databar;
    private Preference mCompositeCode;
    /**
     * 定义存储对象
     */
    private RqDecoder rqEngineer;
    private SharedPreferences pullPreference;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * 初始化扫描参数preference
         */
        rqEngineer = RqEngineer.getInstence(this).getRqDecoder();
        pullPreference = PreferenceManager.getDefaultSharedPreferences(this);
        // Load the XML preferences file
        addPreferencesFromResource(R.xml.symbologies);
        getPreferenceScreen().setOnPreferenceChangeListener(this);
        mSymbologies = (PreferenceScreen) getPreferenceScreen().findPreference(
                "symbologies");
        mEnableAll = getPreferenceScreen().findPreference(
                "enable_all");
        mDisableAll = getPreferenceScreen().findPreference(
                "disable_all");
        mAztec = (TwoStatePreference) getPreferenceScreen().findPreference("aztec");
        mCodablockF = (TwoStatePreference) getPreferenceScreen().findPreference("codablockf");
        mCode32 = (TwoStatePreference) getPreferenceScreen().findPreference("code32");
        mDataMatrix = (TwoStatePreference) getPreferenceScreen().findPreference("data_matrix");
        mGridMatrix = (TwoStatePreference) getPreferenceScreen().findPreference("grid_matrix");
        mDotCode = (TwoStatePreference) getPreferenceScreen().findPreference("dot_code");
        mCode49 = (TwoStatePreference) getPreferenceScreen().findPreference("code49");
        mHanXinCode = (TwoStatePreference) getPreferenceScreen().findPreference("hanxin_code");
        mMaxiCode = (TwoStatePreference) getPreferenceScreen().findPreference("maxi_code");
        mMicroPDF = (TwoStatePreference) getPreferenceScreen().findPreference("micropdf417");
        mMicroQR = (TwoStatePreference) getPreferenceScreen().findPreference("microqr");
        mPDF417 = (TwoStatePreference) getPreferenceScreen().findPreference("pdf417");
        mPlessey = (TwoStatePreference) getPreferenceScreen().findPreference("plessey");
        mQRCode = (TwoStatePreference) getPreferenceScreen().findPreference("qr_code");
        mTelepen = (TwoStatePreference) getPreferenceScreen().findPreference("telepen");
        mTrioptic = (TwoStatePreference) getPreferenceScreen().findPreference("trioptic");
	/*	
        mAustraliaPost = (TwoStatePreference) getPreferenceScreen().findPreference("australia_post");
        mCanadaPost = (TwoStatePreference) getPreferenceScreen().findPreference("canada_post");
        mDutchPost = (TwoStatePreference) getPreferenceScreen().findPreference("dutch_post");
        mJapanPost = (TwoStatePref erence) getPreferenceScreen().findPreference("japan_post");
        mKoreaPost = (TwoStatePreference) getPreferenceScreen().findPreference("korea_post");
        mRoyalPost = (TwoStatePreference) getPreferenceScreen().findPreference("royal_mail");
        mUSPSIntelligentMail = (TwoStatePreference) getPreferenceScreen().findPreference("usps_intelligent_mail");
        mUSPSPlanet = (TwoStatePreference) getPreferenceScreen().findPreference("usps_planet");
        mUSPSPostnet = (TwoStatePreference) getPreferenceScreen().findPreference("usps_postnet");
        mUPU = (TwoStatePreference) getPreferenceScreen().findPreference("upu");
        */
        mCodabar = getPreferenceScreen().findPreference("codabar");
        mCode128 = getPreferenceScreen().findPreference("code128");
        mCode11 = getPreferenceScreen().findPreference("code11");
        mCode93 = getPreferenceScreen().findPreference("code93");
        mCode39 = getPreferenceScreen().findPreference("code39");
        mCodeEAN13 = getPreferenceScreen().findPreference("ean13");
        mCodeEAN8 = getPreferenceScreen().findPreference("ean8");
        mHK2o5 = getPreferenceScreen().findPreference("hong_kong_2_of_5");
        mIATA2o5 = getPreferenceScreen().findPreference("iata_2_of_5");
        mInterleaved2o5 = getPreferenceScreen().findPreference("interleaved_2_of_5");
        mMatrix2o5 = getPreferenceScreen().findPreference("matrix_2_of_5");
        mNEC2o5 = getPreferenceScreen().findPreference("nec_2_of_5");
        mStraight2o5 = getPreferenceScreen().findPreference("straight_2_of_5");
        mMSIPlessey = getPreferenceScreen().findPreference("msi_plessey");
        mUPCA = getPreferenceScreen().findPreference("upc-a");
        mUPCE = getPreferenceScreen().findPreference("upc-e");
        mGS1Databar = getPreferenceScreen().findPreference("gs1_databar");
        mCompositeCode = getPreferenceScreen().findPreference("composite_code");
     /*  if (getResources().getConfiguration().smallestScreenWidthDp < 600) {
            // Lock phone form factor to portrait.
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }*/
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onResume() {
        super.onResume();
        setDecoderStatus();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {

        if (preference == mEnableAll) {
            int count = mSymbologies.getPreferenceCount();
            for (int i = 0; i < count; i++) {
                Preference p = mSymbologies.getPreference(i);
                if (p.isEnabled()) {
                    if (p instanceof AdvancedPreference) {
                        ((AdvancedPreference) p).setChecked(true);
                    }
                    if (p instanceof TwoStatePreference)
                        ((TwoStatePreference) p).setChecked(true);
                    /**
                     * 不能触发onPreferenceChange，主动触发
                     */
                    onPreferenceChange(p, true);
                }
            }
            return true;
        } else if (preference == mDisableAll) {
            int count = mSymbologies.getPreferenceCount();
            for (int i = 0; i < count; i++) {
                Preference p = mSymbologies.getPreference(i);
                if (p.isEnabled()) {
                    if (p instanceof AdvancedPreference) {
                        ((AdvancedPreference) p).setChecked(false);
                    }
                    if (p instanceof TwoStatePreference)
                        ((TwoStatePreference) p).setChecked(false);
                    /**
                     * 不能触发onPreferenceChange，主动触发
                     */
                    onPreferenceChange(p, false);
                }
            }
            return true;
        } else if (preference == mCodabar) {
            if (((AdvancedPreference) preference).isChecked()) {
                Intent intent = new Intent(this, CodabarSymbologySettings.class);
                startActivity(intent);
            }
            return true;
        } else if (preference == mCode128) {
            if (((AdvancedPreference) preference).isChecked()) {
                Intent intent = new Intent(this, Code128SymbologySettings.class);
                startActivity(intent);
            }
            return true;
        } else if (preference == mCode11) {
            if (((AdvancedPreference) preference).isChecked()) {
                Intent intent = new Intent(this, Code11SymbologySettings.class);
                startActivity(intent);
            }
            return true;
        } else if (preference == mCode93) {
            if (((AdvancedPreference) preference).isChecked()) {
                Intent intent = new Intent(this, Code93SymbologySettings.class);
                startActivity(intent);
            }
            return true;
        } else if (preference == mCode39) {
            if (((AdvancedPreference) preference).isChecked()) {
                Intent intent = new Intent(this, Code39SymbologySettings.class);
                startActivity(intent);
            }
            return true;
        } else if (preference == mCompositeCode) {
            if (((AdvancedPreference) preference).isChecked()) {
                Intent intent = new Intent(this, CompositeCodeSymbologySettings.class);
                startActivity(intent);
            }
        } else if (preference == mCodeEAN13) {
            if (((AdvancedPreference) preference).isChecked()) {
                Intent intent = new Intent(this, EAN13SymbologySettings.class);
                startActivity(intent);
            }
            return true;
        } else if (preference == mCodeEAN8) {
            if (((AdvancedPreference) preference).isChecked()) {
                Intent intent = new Intent(this, EAN8SymbologySettings.class);
                startActivity(intent);
            }
            return true;
        } else if (preference == mHK2o5) {
            if (((AdvancedPreference) preference).isChecked()) {
                Intent intent = new Intent(this, HongKong2of5SymbologySettings.class);
                startActivity(intent);
            }
            return true;
        } else if (preference == mIATA2o5) {
            if (((AdvancedPreference) preference).isChecked()) {
                Intent intent = new Intent(this, IATA2of5SymbologySettings.class);
                startActivity(intent);
            }
            return true;
        } else if (preference == mInterleaved2o5) {
            if (((AdvancedPreference) preference).isChecked()) {
                Intent intent = new Intent(this, Interleaved2of5SymbologySettings.class);
                startActivity(intent);
            }
            return true;
        } else if (preference == mMatrix2o5) {
            if (((AdvancedPreference) preference).isChecked()) {
                Intent intent = new Intent(this, Matrix2of5SymbologySettings.class);
                startActivity(intent);
            }
            return true;
        } else if (preference == mNEC2o5) {
            if (((AdvancedPreference) preference).isChecked()) {
                Intent intent = new Intent(this, NEC2of5SymbologySettings.class);
                startActivity(intent);
            }
            return true;
        } else if (preference == mStraight2o5) {
            if (((AdvancedPreference) preference).isChecked()) {
                Intent intent = new Intent(this, Straight2of5SymbologySettings.class);
                startActivity(intent);
            }
            return true;
        } else if (preference == mMSIPlessey) {
            if (((AdvancedPreference) preference).isChecked()) {
                Intent intent = new Intent(this, MSIPlesseySymbologySettings.class);
                startActivity(intent);
            }
            return true;
        } else if (preference == mUPCA) {
            if (((AdvancedPreference) preference).isChecked()) {
                Intent intent = new Intent(this, UPCASymbologySettings.class);
                startActivity(intent);
            }
            return true;
        } else if (preference == mUPCE) {
            if (((AdvancedPreference) preference).isChecked()) {
                Intent intent = new Intent(this, UPCESymbologySettings.class);
                startActivity(intent);
            }
            return true;
        } else if (preference == mGS1Databar) {
            if (((AdvancedPreference) preference).isChecked()) {
                Intent intent = new Intent(this, GS1DataBarSymbologySettings.class);
                startActivity(intent);
            }
            return true;
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        for (int i = 0; i < mSymbologies.getPreferenceCount(); i++) {
            Preference preference = mSymbologies.getPreference(i);
            preference.setOnPreferenceChangeListener(null);
            preference.setOnPreferenceClickListener(null);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        String key = preference.getKey();
        Log.i(TAG, "onPreferenceChange key=" + key);
        if (preference == mAztec) {
            key = RqSymbologyType.SymbologyType_Aztec.getSampleName();
        } else if (preference == mCodablockF) {
            key = RqSymbologyType.SymbologyType_CodablockF.getSampleName();
        } else if (preference == mCode32) {
            key = RqSymbologyType.SymbologyType_Code32.getSampleName();
        } else if (preference == mCode49) {
            key = RqSymbologyType.SymbologyType_Code49.getSampleName();
        } else if (preference == mDataMatrix) {
            key = RqSymbologyType.SymbologyType_DataMatrix.getSampleName();
        } else if (preference == mGridMatrix) {
            key = RqSymbologyType.SymbologyType_GridMatrix.getSampleName();
        } else if (preference == mDotCode) {
            key = RqSymbologyType.SymbologyType_DOT.getSampleName();
        } else if (preference == mHanXinCode) {
            key = RqSymbologyType.SymbologyType_HanXin.getSampleName();
        } else if (preference == mMaxiCode) {
            key = RqSymbologyType.SymbologyType_MAXICODE.getSampleName();
        } else if (preference == mMicroPDF) {
            key = RqSymbologyType.SymbologyType_MPDF.getSampleName();
        } else if (preference == mMicroQR) {
            key = RqSymbologyType.SymbologyType_QRMicro.getSampleName();
        } else if (preference == mPDF417) {
            key = RqSymbologyType.SymbologyType_PDF417.getSampleName();
        } else if (preference == mPlessey) {
            key = RqSymbologyType.SymbologyType_Plessy.getSampleName();
        } else if (preference == mQRCode) {
            key = RqSymbologyType.SymbologyType_QR.getSampleName();
        } else if (preference == mTelepen) {
            key = RqSymbologyType.SymbologyType_Telepen.getSampleName();
        } else if (preference == mTrioptic) {
            key = RqSymbologyType.SymbologyType_Trioptic.getSampleName();
        } else if (preference == mCodabar) {
            key = RqSymbologyType.SymbologyType_Codabar.getSampleName();
        } else if (preference == mCode128) {
            key = RqSymbologyType.SymbologyType_Code128_CCA.getSampleName();
        } else if (preference == mCode11) {
            key = RqSymbologyType.SymbologyType_Code11.getSampleName();
        } else if (preference == mCode39) {
            key = RqSymbologyType.SymbologyType_Code39.getSampleName();
        } else if (preference == mCode93) {
            key = RqSymbologyType.SymbologyType_Code93.getSampleName();
        } else if (preference == mCompositeCode) {
            key = RqSymbologyType.SymbologyType_COMPOSITE.getSampleName();
        } else if (preference == mCodeEAN13) {
            key = RqSymbologyType.SymbologyType_EAN13.getSampleName();
        } else if (preference == mCodeEAN8) {
            key = RqSymbologyType.SymbologyType_EAN8.getSampleName();
        } else if (preference == mGS1Databar) {
            key = RqSymbologyType.SymbologyType_GS1_DATABAR.getSampleName();
        } else if (preference == mHK2o5) {
            key = RqSymbologyType.SymbologyType_HongKong2of5.getSampleName();
        } else if (preference == mIATA2o5) {
            key = RqSymbologyType.SymbologyType_IATA2of5.getSampleName();
        } else if (preference == mInterleaved2o5) {
            key = RqSymbologyType.SymbologyType_Interleaved2of5.getSampleName();
        } else if (preference == mMatrix2o5) {
            key = RqSymbologyType.SymbologyType_Matrix2of5.getSampleName();
        } else if (preference == mMSIPlessey) {
            key = RqSymbologyType.SymbologyType_MSIPlessy.getSampleName();
        } else if (preference == mNEC2o5) {
            key = RqSymbologyType.SymbologyType_NEC2of5.getSampleName();
        } else if (preference == mStraight2o5) {
            key = RqSymbologyType.SymbologyType_Straight2of5.getSampleName();
        } else if (preference == mUPCA) {
            key = RqSymbologyType.SymbologyType_UPCA.getSampleName();
        } else if (preference == mUPCE) {
            key = RqSymbologyType.SymbologyType_UPCE.getSampleName();
        }
        SharedPreferences.Editor e = pullPreference.edit();

        e.putBoolean(key, (boolean) newValue);
        e.apply();
        Log.e(TAG, "onPreferenceChange key=" + key + " value=" + pullPreference.getBoolean(key, false));
        rqEngineer.pushBarcodePreference(key, pullPreference);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void setDecoderStatus() {

        for (int i = 0; i < mSymbologies.getPreferenceCount(); i++) {
            Preference preference = mSymbologies.getPreference(i);
            preference.setOnPreferenceChangeListener(this);
            preference.setOnPreferenceClickListener(this);
            if (preference == mAztec) {
                String key = RqSymbologyType.SymbologyType_Aztec.getSampleName();
                pullPreference = rqEngineer.pullBarcodePreference(key);
                if (pullPreference != null)
                    mAztec.setChecked(pullPreference.getBoolean(key, false));
                else
                    Log.e(SymbologiesActivity.class.getSimpleName(), "" + key + " is not support.");
            } else if (preference == mCodablockF) {
                String key = RqSymbologyType.SymbologyType_CodablockF.getSampleName();
                pullPreference = rqEngineer.pullBarcodePreference(key);
                if (pullPreference != null)
                    mCodablockF.setChecked(pullPreference.getBoolean(key, false));
                else
                    Log.e(SymbologiesActivity.class.getSimpleName(), "" + key + " is not support.");
            } else if (preference == mCode32) {
                String key = RqSymbologyType.SymbologyType_Code32.getSampleName();
                pullPreference = rqEngineer.pullBarcodePreference(key);
                if (pullPreference != null)
                    mCode32.setChecked(pullPreference.getBoolean(key, false));
                else
                    Log.e(SymbologiesActivity.class.getSimpleName(), "" + key + " is not support.");
            } else if (preference == mCode49) {
                String key = RqSymbologyType.SymbologyType_Code49.getSampleName();
                pullPreference = rqEngineer.pullBarcodePreference(key);
                if (pullPreference != null)
                    mCode49.setChecked(pullPreference.getBoolean(key, false));
                else
                    Log.e(SymbologiesActivity.class.getSimpleName(), "" + key + " is not support.");
            } else if (preference == mDataMatrix) {
                String key = RqSymbologyType.SymbologyType_DataMatrix.getSampleName();
                pullPreference = rqEngineer.pullBarcodePreference(key);
                if (pullPreference != null)
                    mDataMatrix.setChecked(pullPreference.getBoolean(key, false));
                else
                    Log.e(SymbologiesActivity.class.getSimpleName(), "" + key + " is not support.");
            } else if (preference == mGridMatrix) {
                String key = RqSymbologyType.SymbologyType_GridMatrix.getSampleName();
                pullPreference = rqEngineer.pullBarcodePreference(key);
                if (pullPreference != null)
                    mGridMatrix.setChecked(pullPreference.getBoolean(key, false));
                else
                    Log.e(SymbologiesActivity.class.getSimpleName(), "" + key + " is not support.");
            } else if (preference == mDotCode) {
                String key = RqSymbologyType.SymbologyType_DOT.getSampleName();
                pullPreference = rqEngineer.pullBarcodePreference(key);
                if (pullPreference != null)
                    mDotCode.setChecked(pullPreference.getBoolean(key, false));
                else
                    Log.e(SymbologiesActivity.class.getSimpleName(), "" + key + " is not support.");
            } else if (preference == mHanXinCode) {
                String key = RqSymbologyType.SymbologyType_HanXin.getSampleName();
                pullPreference = rqEngineer.pullBarcodePreference(key);
                if (pullPreference != null)
                    mHanXinCode.setChecked(pullPreference.getBoolean(key, false));
                else
                    Log.e(SymbologiesActivity.class.getSimpleName(), "" + key + " is not support.");
            } else if (preference == mMaxiCode) {
                String key = RqSymbologyType.SymbologyType_MAXICODE.getSampleName();
                pullPreference = rqEngineer.pullBarcodePreference(key);
                if (pullPreference != null)
                    mMaxiCode.setChecked(pullPreference.getBoolean(key, false));
                else
                    Log.e(SymbologiesActivity.class.getSimpleName(), "" + key + " is not support.");
            } else if (preference == mMicroPDF) {
                String key = RqSymbologyType.SymbologyType_MPDF.getSampleName();
                pullPreference = rqEngineer.pullBarcodePreference(key);
                if (pullPreference != null)
                    mMicroPDF.setChecked(pullPreference.getBoolean(key, false));
                else
                    Log.e(SymbologiesActivity.class.getSimpleName(), "" + key + " is not support.");
            } else if (preference == mMicroQR) {
                String key = RqSymbologyType.SymbologyType_QRMicro.getSampleName();
                pullPreference = rqEngineer.pullBarcodePreference(key);
                if (pullPreference != null)
                    mMicroQR.setChecked(pullPreference.getBoolean(key, false));
                else
                    Log.e(SymbologiesActivity.class.getSimpleName(), "" + key + " is not support.");
            } else if (preference == mPDF417) {
                String key = RqSymbologyType.SymbologyType_PDF417.getSampleName();
                pullPreference = rqEngineer.pullBarcodePreference(key);
                if (pullPreference != null)
                    mPDF417.setChecked(pullPreference.getBoolean(key, false));
                else
                    Log.e(SymbologiesActivity.class.getSimpleName(), "" + key + " is not support.");
            } else if (preference == mPlessey) {
                String key = RqSymbologyType.SymbologyType_Plessy.getSampleName();
                pullPreference = rqEngineer.pullBarcodePreference(key);
                if (pullPreference != null)
                    mPlessey.setChecked(pullPreference.getBoolean(key, false));
                else
                    Log.e(SymbologiesActivity.class.getSimpleName(), "" + key + " is not support.");
            } else if (preference == mQRCode) {
                String key = RqSymbologyType.SymbologyType_QR.getSampleName();
                pullPreference = rqEngineer.pullBarcodePreference(key);
                if (pullPreference != null)
                    mQRCode.setChecked(pullPreference.getBoolean(key, false));
                else
                    Log.e(SymbologiesActivity.class.getSimpleName(), "" + key + " is not support.");
            } else if (preference == mTelepen) {
                String key = RqSymbologyType.SymbologyType_Telepen.getSampleName();
                pullPreference = rqEngineer.pullBarcodePreference(key);
                if (pullPreference != null)
                    mTelepen.setChecked(pullPreference.getBoolean(key, false));
                else
                    Log.e(SymbologiesActivity.class.getSimpleName(), "" + key + " is not support.");
            } else if (preference == mTrioptic) {
                String key = RqSymbologyType.SymbologyType_Trioptic.getSampleName();
                pullPreference = rqEngineer.pullBarcodePreference(key);
                if (pullPreference != null)
                    mTrioptic.setChecked(pullPreference.getBoolean(key, false));
                else
                    Log.e(SymbologiesActivity.class.getSimpleName(), "" + key + " is not support.");
            } else if (preference == mCodabar) {
                String key = RqSymbologyType.SymbologyType_Codabar.getSampleName();
                pullPreference = rqEngineer.pullBarcodePreference(key);
                if (pullPreference != null)
                    ((AdvancedPreference) mCodabar).setChecked(pullPreference.getBoolean(key, false));
                else
                    Log.e(SymbologiesActivity.class.getSimpleName(), "" + key + " is not support.");
            } else if (preference == mCode128) {
                String key = RqSymbologyType.SymbologyType_Code128_CCA.getSampleName();
                pullPreference = rqEngineer.pullBarcodePreference(key);
                if (pullPreference != null)
                    ((AdvancedPreference) mCode128).setChecked(pullPreference.getBoolean(key, false));
                else
                    Log.e(SymbologiesActivity.class.getSimpleName(), "" + key + "ssss is not support.");
            } else if (preference == mCode11) {
                String key = RqSymbologyType.SymbologyType_Code11.getSampleName();
                pullPreference = rqEngineer.pullBarcodePreference(key);
                if (pullPreference != null)
                    ((AdvancedPreference) mCode11).setChecked(pullPreference.getBoolean(key, false));
                else
                    Log.e(SymbologiesActivity.class.getSimpleName(), "" + key + " is not support.");
            } else if (preference == mCode39) {
                String key = RqSymbologyType.SymbologyType_Code39.getSampleName();
                pullPreference = rqEngineer.pullBarcodePreference(key);
                if (pullPreference != null)
                    ((AdvancedPreference) mCode39).setChecked(pullPreference.getBoolean(key, false));
                else
                    Log.e(SymbologiesActivity.class.getSimpleName(), "" + key + " is not support.");
            } else if (preference == mCode93) {
                String key = RqSymbologyType.SymbologyType_Code93.getSampleName();
                pullPreference = rqEngineer.pullBarcodePreference(key);
                if (pullPreference != null)
                    ((AdvancedPreference) mCode93).setChecked(pullPreference.getBoolean(key, false));
                else
                    Log.e(SymbologiesActivity.class.getSimpleName(), "" + key + " is not support.");
            } else if (preference == mCompositeCode) {
                String key = RqSymbologyType.SymbologyType_COMPOSITE.getSampleName();
                pullPreference = rqEngineer.pullBarcodePreference(key);
                if (pullPreference != null)
                    ((AdvancedPreference) mCompositeCode).setChecked(pullPreference.getBoolean(key, false));
                else
                    Log.e(SymbologiesActivity.class.getSimpleName(), "" + key + " is not support.");
            } else if (preference == mCodeEAN13) {
                String key = RqSymbologyType.SymbologyType_EAN13.getSampleName();
                pullPreference = rqEngineer.pullBarcodePreference(key);
                if (pullPreference != null)
                    ((AdvancedPreference) mCodeEAN13).setChecked(pullPreference.getBoolean(key, false));
                else
                    Log.e(SymbologiesActivity.class.getSimpleName(), "" + key + " is not support.");
            } else if (preference == mCodeEAN8) {
                String key = RqSymbologyType.SymbologyType_EAN8.getSampleName();
                pullPreference = rqEngineer.pullBarcodePreference(key);
                if (pullPreference != null) {
                    ((AdvancedPreference) mCodeEAN8).setChecked(pullPreference.getBoolean(key, false));

                } else
                    Log.e(SymbologiesActivity.class.getSimpleName(), "" + key + " is not support.");
            } else if (preference == mGS1Databar) {
                String key = RqSymbologyType.SymbologyType_GS1_DATABAR.getSampleName();
                pullPreference = rqEngineer.pullBarcodePreference(key);
                if (pullPreference != null) {

                    ((AdvancedPreference) mGS1Databar).setChecked(pullPreference.getBoolean(key, false));
                } else
                    Log.e(SymbologiesActivity.class.getSimpleName(), "" + key + " is not support.");
            } else if (preference == mHK2o5) {
                String key = RqSymbologyType.SymbologyType_HongKong2of5.getSampleName();
                pullPreference = rqEngineer.pullBarcodePreference(key);
                if (pullPreference != null)
                    ((AdvancedPreference) mHK2o5).setChecked(pullPreference.getBoolean(key, false));
                else
                    Log.e(SymbologiesActivity.class.getSimpleName(), "" + key + " is not support.");
            } else if (preference == mIATA2o5) {
                String key = RqSymbologyType.SymbologyType_IATA2of5.getSampleName();
                pullPreference = rqEngineer.pullBarcodePreference(key);
                if (pullPreference != null)
                    ((AdvancedPreference) mIATA2o5).setChecked(pullPreference.getBoolean(key, false));
                else
                    Log.e(SymbologiesActivity.class.getSimpleName(), "" + key + " is not support.");
            } else if (preference == mInterleaved2o5) {
                String key = RqSymbologyType.SymbologyType_Interleaved2of5.getSampleName();
                pullPreference = rqEngineer.pullBarcodePreference(key);
                if (pullPreference != null)
                    ((AdvancedPreference) mInterleaved2o5).setChecked(pullPreference.getBoolean(key, false));
                else
                    Log.e(SymbologiesActivity.class.getSimpleName(), "" + key + " is not support.");
            } else if (preference == mMatrix2o5) {
                String key = RqSymbologyType.SymbologyType_Matrix2of5.getSampleName();
                pullPreference = rqEngineer.pullBarcodePreference(key);
                if (pullPreference != null)
                    ((AdvancedPreference) mMatrix2o5).setChecked(pullPreference.getBoolean(key, false));
                else
                    Log.e(SymbologiesActivity.class.getSimpleName(), "" + key + " is not support.");
            } else if (preference == mMSIPlessey) {
                String key = RqSymbologyType.SymbologyType_MSIPlessy.getSampleName();
                pullPreference = rqEngineer.pullBarcodePreference(key);
                if (pullPreference != null)
                    ((AdvancedPreference) mMSIPlessey).setChecked(pullPreference.getBoolean(key, false));
                else
                    Log.e(SymbologiesActivity.class.getSimpleName(), "" + key + " is not support.");
            } else if (preference == mNEC2o5) {
                String key = RqSymbologyType.SymbologyType_NEC2of5.getSampleName();
                pullPreference = rqEngineer.pullBarcodePreference(key);
                if (pullPreference != null)
                    ((AdvancedPreference) mNEC2o5).setChecked(pullPreference.getBoolean(key, false));
                else
                    Log.e(SymbologiesActivity.class.getSimpleName(), "" + key + " is not support.");
            } else if (preference == mStraight2o5) {
                String key = RqSymbologyType.SymbologyType_Straight2of5.getSampleName();
                pullPreference = rqEngineer.pullBarcodePreference(key);
                if (pullPreference != null)
                    ((AdvancedPreference) mStraight2o5).setChecked(pullPreference.getBoolean(key, false));
                else
                    Log.e(SymbologiesActivity.class.getSimpleName(), "" + key + " is not support.");
            } else if (preference == mUPCA) {
                String key = RqSymbologyType.SymbologyType_UPCA.getSampleName();
                pullPreference = rqEngineer.pullBarcodePreference(key);
                if (pullPreference != null)
                    ((AdvancedPreference) mUPCA).setChecked(pullPreference.getBoolean(key, false));
                else
                    Log.e(SymbologiesActivity.class.getSimpleName(), "" + key + " is not support.");
            } else if (preference == mUPCE) {
                String key = RqSymbologyType.SymbologyType_UPCE.getSampleName();
                pullPreference = rqEngineer.pullBarcodePreference(key);
                if (pullPreference != null)
                    ((AdvancedPreference) mUPCE).setChecked(pullPreference.getBoolean(key, false));
                else
                    Log.e(SymbologiesActivity.class.getSimpleName(), "" + key + " is not support.");
            }
        }
    }

}
