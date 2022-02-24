package com.xiongmaokuaishou.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;


public class SeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener, OnClickListener {
    // ------------------------------------------------------------------------------------------
    // Private attributes :
    private static final String androidns = "http://schemas.android.com/apk/res/android";
    private SeekBar mSeekBar;
    private TextView mValueText;
    private Context mContext;
    private String mSuffix;
    private int mDefault, mMax, mValue;

    // ------------------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------------------
    // Constructor :
    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        // Get string value for suffix (text attribute in xml file) :
        int mSuffixId = attrs.getAttributeResourceValue(androidns, "text", 0);
        if (mSuffixId == 0) mSuffix = attrs.getAttributeValue(androidns, "text");
        else mSuffix = mContext.getString(mSuffixId);
        // Get default and max seekbar values :
        mDefault = attrs.getAttributeIntValue(androidns, "defaultValue", 0);
        mValue = attrs.getAttributeIntValue(androidns, "defaultValue", 0);
        mMax = attrs.getAttributeIntValue(androidns, "max", 20);
    }

    // ------------------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------------------
    // DialogPreference methods :
    @Override
    protected View onCreateDialogView() {
        LinearLayout.LayoutParams params;
        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(6, 6, 6, 6);
        mValueText = new TextView(mContext);
        mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
        mValueText.setTextSize(24);
        params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.addView(mValueText, params);
        mSeekBar = new SeekBar(mContext);
        mSeekBar.setOnSeekBarChangeListener(this);
        layout.addView(mSeekBar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        if (shouldPersist())
            mValue = getPersistedInt(mDefault);
        mSeekBar.setMax(mMax);
        mSeekBar.setProgress(mValue);
        return layout;
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        mSeekBar.setMax(mMax);
        mSeekBar.setProgress(mValue);
    }

    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
        super.onSetInitialValue(restore, defaultValue);
        if (restore)
            mValue = shouldPersist() ? getPersistedInt(mDefault) : 0;
        else
            mValue = (Integer) defaultValue;
    }

    // ------------------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------------------
    // OnSeekBarChangeListener methods :
    @Override
    public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
        String t = String.valueOf(value);
        mValueText.setText(mSuffix == null ? t : t.concat(" " + mSuffix));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seek) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seek) {
    }

    public int getProgress() {
        return mValue;
    }

    // ------------------------------------------------------------------------------------------
    //Used to forcefully set the preference and Seekbar value when user selects an odd numbered minChars in Interleaved 2 out of 5 settings
    public void setProgress(int progressVal) {
        this.mValue = progressVal;
        if (mSeekBar != null) {
            mSeekBar.setProgress(mValue);
            persistInt(mSeekBar.getProgress());
            callChangeListener(mSeekBar.getProgress());
        } else {
            persistInt(this.mValue);
        }
    }

    // ------------------------------------------------------------------------------------------
    // Set the positive button listener and onClick action :
    @Override
    public void showDialog(Bundle state) {
        super.showDialog(state);
        Button positiveButton = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (shouldPersist()) {
            mValue = mSeekBar.getProgress();
            persistInt(mSeekBar.getProgress());
            callChangeListener(mSeekBar.getProgress());
        }
        (getDialog()).dismiss();
    }

    @Override
    public void setSummary(CharSequence summary) {
        super.setSummary(summary);
    }
    // ------------------------------------------------------------------------------------------
}