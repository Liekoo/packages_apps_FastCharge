package com.android.fastcharge;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import androidx.preference.PreferenceManager;
import com.android.fastcharge.utils.FileUtils;

public class FastChargeService extends Service {
    private static final String TAG = "FastCharge";

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            restoreChargingMode(context, action);
        }
    };

    private void restoreChargingMode(Context context, String reason) {
        FastChargeConfig mConfig = FastChargeConfig.getInstance(context);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String chargingMode = sharedPrefs.getString(mConfig.FASTCHARGE_KEY, FastChargeConfig.MODE_SUPER_FAST);

        if (chargingMode.equals(FastChargeConfig.MODE_SUPER_FAST)) return;

        try {
            if (FileUtils.fileExists(mConfig.getFastChargePath())) {
                String current = FileUtils.readLine(mConfig.getFastChargePath());
                if (!chargingMode.equals(current)) {
                    FileUtils.writeLine(mConfig.getFastChargePath(), chargingMode);
                    Log.d(TAG, "Restored charging mode [" + reason + "]: " + chargingMode);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to restore charging mode", e);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mReceiver, filter);
        Log.d(TAG, "FastChargeService started");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
