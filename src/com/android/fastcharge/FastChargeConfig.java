/*
 * SPDX-FileCopyrightText: 2025 kenway214
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.fastcharge;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import com.android.fastcharge.utils.FileUtils;

public class FastChargeConfig {

    private static FastChargeConfig instance = null;

    public static FastChargeConfig getInstance(Context context) {

        if (instance == null) {
            instance = new FastChargeConfig(context.getApplicationContext());
        }

        return instance;
    }

    public static final String FASTCHARGE_KEY = "wired_charging_mode";

    public static final String FASTCHARGE_PATH = "/sys/class/power_supply/battery/charge_control_limit";

    public static final String MODE_SLOW = "12";
    public static final String MODE_FAST = "9";
    public static final String MODE_SUPER_FAST = "0";

    public static final String ACTION_FAST_CHARGE_SERVICE_CHANGED = "com.android.fastcharge.FAST_CHARGE_SERVICE_CHANGED";
    public static final String EXTRA_FAST_CHARGE_MODE = "fastchargingmode";

    private FastChargeConfig(Context context) {
    }

    public String getFastChargePath() {
        return FASTCHARGE_PATH;
    }

    public String getCurrentMode(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String savedMode = sharedPrefs.getString(FASTCHARGE_KEY, MODE_SUPER_FAST);
        if (savedMode.equals(MODE_SLOW) || savedMode.equals(MODE_FAST) || savedMode.equals(MODE_SUPER_FAST)) {
            return savedMode;
        }
        return MODE_SUPER_FAST;
    }
}
