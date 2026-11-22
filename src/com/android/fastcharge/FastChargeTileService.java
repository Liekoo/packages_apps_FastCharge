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
import android.graphics.drawable.Icon;
import android.os.UserHandle;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.android.fastcharge.utils.FileUtils;

/**
 * Quick Settings tile. Tap cycles Slow -> Fast -> Super Fast and shows a toast.
 * The icon itself encodes the mode (1, 2 or 3 bars), since compact tiles hide text. Long-press opens
 * the settings page (QS_TILE_PREFERENCES on FastChargeActivity).
 */
public class FastChargeTileService extends TileService {

    private static final String TAG = "FastCharge";

    // Index lines up across all three arrays.
    private static final String[] MODES = {
            FastChargeConfig.MODE_SUPER_FAST,
            FastChargeConfig.MODE_FAST,
            FastChargeConfig.MODE_SLOW,
    };
    private static final int[] SUBTITLES = {
            R.string.tile_subtitle_super_fast,
            R.string.tile_subtitle_fast,
            R.string.tile_subtitle_slow,
    };
    private static final int[] ICONS = {
            R.drawable.ic_tile_super_fast,
            R.drawable.ic_tile_fast,
            R.drawable.ic_tile_slow,
    };

    private FastChargeConfig mConfig;

    private final BroadcastReceiver mModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI();
        }
    };

    @Override
    public void onStartListening() {
        super.onStartListening();
        mConfig = FastChargeConfig.getInstance(this);
        registerReceiver(mModeReceiver,
                new IntentFilter(FastChargeConfig.ACTION_FAST_CHARGE_SERVICE_CHANGED),
                Context.RECEIVER_NOT_EXPORTED);
        updateUI();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
        unregisterReceiver(mModeReceiver);
    }

    @Override
    public void onClick() {
        super.onClick();
        if (!FileUtils.fileExists(mConfig.getFastChargePath())) {
            updateUI();
            return;
        }

        final String nextMode = MODES[(indexOf(mConfig.getCurrentMode(this)) + 1) % MODES.length];

        // Only persist after the kernel accepted the value.
        if (!FileUtils.writeLine(mConfig.getFastChargePath(), nextMode)) {
            Log.e(TAG, "Failed to write charging mode " + nextMode);
            updateUI();
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putString(FastChargeConfig.FASTCHARGE_KEY, nextMode).apply();

        Intent intent = new Intent(FastChargeConfig.ACTION_FAST_CHARGE_SERVICE_CHANGED);
        intent.putExtra(FastChargeConfig.EXTRA_FAST_CHARGE_MODE, nextMode);
        intent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        sendBroadcastAsUser(intent, UserHandle.CURRENT);

        updateUI();

        // The compact tile hides its label and subtitle, so confirm the new mode.
        Toast.makeText(this,
                getString(R.string.tile_toast, getString(SUBTITLES[indexOf(nextMode)])),
                Toast.LENGTH_SHORT).show();
    }

    private void updateUI() {
        final Tile tile = getQsTile();
        if (tile == null) return;

        final String label = getString(R.string.tile_label);
        tile.setLabel(label);

        if (!FileUtils.fileExists(mConfig.getFastChargePath())) {
            final String subtitle = getString(R.string.tile_subtitle_unsupported);
            tile.setIcon(Icon.createWithResource(this, R.drawable.ic_tile_super_fast));
            tile.setSubtitle(subtitle);
            tile.setStateDescription(subtitle);
            tile.setState(Tile.STATE_UNAVAILABLE);
            tile.updateTile();
            return;
        }

        final int index = indexOf(mConfig.getCurrentMode(this));
        final String subtitle = getString(SUBTITLES[index]);

        tile.setIcon(Icon.createWithResource(this, ICONS[index]));
        tile.setSubtitle(subtitle);
        tile.setStateDescription(subtitle);
        tile.setContentDescription(label);
        // Slow looks "off" (the quiet state); Fast and Super Fast light up.
        tile.setState(index == 0 ? Tile.STATE_INACTIVE : Tile.STATE_ACTIVE);
        tile.updateTile();
    }

    private static int indexOf(String mode) {
        for (int i = 0; i < MODES.length; i++) {
            if (MODES[i].equals(mode)) return i;
        }
        return MODES.length - 1; // default: Super Fast
    }
}
