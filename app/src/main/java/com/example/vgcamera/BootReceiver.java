package com.example.vgcamera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * BootReceiver — tự động khởi động VG-CAMERA sau khi thiết bị reboot.
 * Yêu cầu: RECEIVE_BOOT_COMPLETED permission trong Manifest.
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "VGCamera-Boot";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                || "android.intent.action.LOCKED_BOOT_COMPLETED".equals(action)) {
            Log.i(TAG, "📱 Boot completed — launching VG-Camera");

            Intent launch = new Intent(context, MainActivity.class);
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(launch);
        }
    }
}
