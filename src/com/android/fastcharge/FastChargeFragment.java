/*
 * SPDX-FileCopyrightText: 2025 kenway214
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.fastcharge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import com.android.fastcharge.utils.FileUtils;
import com.android.settingslib.widget.SelectorWithWidgetPreference;
import com.android.settingslib.widget.SettingsBasePreferenceFragment;

public class FastChargeFragment extends SettingsBasePreferenceFragment
        implements SelectorWithWidgetPreference.OnClickListener {

    private static final String TAG = "FastCharge";

    private static final String KEY_FOOTER = "fastcharge_footer";
    private static final String KEY_ILLUSTRATION = "fastcharge_illustration";
    private static final String KEY_STATUS = "fastcharge_status";

    // Order matters: index lines up with MODE_VALUES / MODE_COLORS.
    private static final String[] MODE_KEYS = {"mode_slow", "mode_fast", "mode_super_fast"};
    private static final String[] MODE_VALUES = {
            FastChargeConfig.MODE_SLOW,
            FastChargeConfig.MODE_FAST,
            FastChargeConfig.MODE_SUPER_FAST,
    };
    private static final int[] MODE_COLORS = {
            R.color.mode_slow,        // orange
            R.color.mode_fast,        // green
            R.color.mode_super_fast,  // yellow
    };

    private final SelectorWithWidgetPreference[] mModePrefs =
            new SelectorWithWidgetPreference[MODE_KEYS.length];
    private ImagePreference mIllustration;
    private Preference mStatus;
    private FastChargeConfig mConfig;

    /** Another surface (QS tile) changed the mode: just re-sync the UI. */
    private final BroadcastReceiver mModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refresh(mConfig.getCurrentMode(context));
        }
    };

    private final BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateStatus(intent);
        }
    };

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.fastcharge_settings, rootKey);
        mConfig = FastChargeConfig.getInstance(getContext());

        mIllustration = findPreference(KEY_ILLUSTRATION);
        mStatus = findPreference(KEY_STATUS);

        final boolean supported = FileUtils.fileExists(mConfig.getFastChargePath());
        for (int i = 0; i < MODE_KEYS.length; i++) {
            mModePrefs[i] = findPreference(MODE_KEYS[i]);
            mModePrefs[i].setOnClickListener(this);
            mModePrefs[i].setEnabled(supported);
        }

        if (!supported) {
            Preference footer = findPreference(KEY_FOOTER);
            if (footer != null) {
                footer.setSummary(R.string.wired_charging_mode_not_supported);
            }
        }

        refresh(mConfig.getCurrentMode(getContext()));
    }

    @Override
    public void onStart() {
        super.onStart();
        getContext().registerReceiver(mModeReceiver,
                new IntentFilter(FastChargeConfig.ACTION_FAST_CHARGE_SERVICE_CHANGED),
                Context.RECEIVER_NOT_EXPORTED);
        // Sticky: returns the latest battery state immediately.
        Intent battery = getContext().registerReceiver(mBatteryReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (battery != null) updateStatus(battery);
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh(mConfig.getCurrentMode(getContext()));
    }

    @Override
    public void onStop() {
        super.onStop();
        getContext().unregisterReceiver(mModeReceiver);
        getContext().unregisterReceiver(mBatteryReceiver);
    }

    @Override
    public void onRadioButtonClicked(SelectorWithWidgetPreference emitter) {
        final Context context = getContext();
        final int index = indexOfKey(emitter.getKey());
        if (index < 0) return;
        final String mode = MODE_VALUES[index];

        if (!FileUtils.writeLine(mConfig.getFastChargePath(), mode)) {
            Log.e(TAG, "Failed to write charging mode " + mode);
            Toast.makeText(context, R.string.write_failed, Toast.LENGTH_SHORT).show();
            refresh(mConfig.getCurrentMode(context)); // revert the radio
            return;
        }

        // Only persist after the kernel accepted the value.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(FastChargeConfig.FASTCHARGE_KEY, mode).apply();

        Intent intent = new Intent(FastChargeConfig.ACTION_FAST_CHARGE_SERVICE_CHANGED);
        intent.putExtra(FastChargeConfig.EXTRA_FAST_CHARGE_MODE, mode);
        intent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        context.sendBroadcastAsUser(intent, UserHandle.CURRENT);

        refresh(mode);
    }

    private void refresh(String mode) {
        final int selected = indexOfValue(mode);
        for (int i = 0; i < mModePrefs.length; i++) {
            if (mModePrefs[i] != null) mModePrefs[i].setChecked(i == selected);
        }
        if (mIllustration != null) {
            mIllustration.setTint(getContext().getColor(MODE_COLORS[selected]));
        }
    }

    private void updateStatus(Intent battery) {
        if (mStatus == null) return;
        final int level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        final int scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        final float temp = battery.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f;
        final boolean plugged = battery.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0;
        final int percent = scale > 0 ? Math.round(level * 100f / scale) : level;

        mStatus.setSummary(getString(
                plugged ? R.string.status_charging : R.string.status_not_charging,
                percent, temp));
    }

    private static int indexOfKey(String key) {
        for (int i = 0; i < MODE_KEYS.length; i++) {
            if (MODE_KEYS[i].equals(key)) return i;
        }
        return -1;
    }

    private static int indexOfValue(String value) {
        for (int i = 0; i < MODE_VALUES.length; i++) {
            if (MODE_VALUES[i].equals(value)) return i;
        }
        return MODE_VALUES.length - 1; // default: Super Fast
    }
}
