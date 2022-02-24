package com.xiongmaokuaishou.myapplication;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.Switch;

public class AdvancedPreference extends Preference {
    private static final String androidns = "http://schemas.android.com/apk/res/android";
    private SharedPreferences prefs;
    private String mkey;
    private boolean mChecked;
    private boolean mCheckedSet;
    private final Listener mListener = new Listener();

    public AdvancedPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Get default for the switch values :
        boolean mValue = attrs.getAttributeBooleanValue(androidns, "defaultValue", false);
        mkey = attrs.getAttributeValue(androidns, "key");
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (!prefs.contains(mkey)) {
            saveOnSharedPreferences(mValue);
        }
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        View checkableView = view.findViewById(R.id.switchCustom);
        if (checkableView != null && checkableView instanceof Checkable) {
            if (checkableView instanceof Switch) {
                final Switch switchView = (Switch) checkableView;
                switchView.setOnCheckedChangeListener(null);
            }
            ((Checkable) checkableView).setChecked(mChecked);
            if (checkableView instanceof Switch) {
                final Switch switchView = (Switch) checkableView;
                switchView.setOnCheckedChangeListener(mListener);
            }
        }
    }

    private class Listener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!callChangeListener(isChecked)) {
                // Listener didn't like it, change it back.
                // CompoundButton will make sure we don't recurse.
                buttonView.setChecked(!isChecked);
                return;
            }
            AdvancedPreference.this.setChecked(isChecked);
        }
    }

    public void setChecked(boolean checked) {
        // Always persist/notify the first time; don't assume the field's default of false.
        final boolean changed = mChecked != checked;
        if (changed || !mCheckedSet) {
            mChecked = checked;
            mCheckedSet = true;
            persistBoolean(checked);
            if (changed) {
                saveOnSharedPreferences(checked);
                notifyDependencyChange(shouldDisableDependents());
                notifyChanged();
            }
        }
    }

    public boolean isChecked() {
        return mChecked;
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setChecked(restoreValue ? getPersistedBoolean(mChecked)
                : (Boolean) defaultValue);
    }

    private void saveOnSharedPreferences(boolean enable) {
        SharedPreferences.Editor e = prefs.edit();
        e.putBoolean(mkey, enable);
        e.apply();
    }
}
