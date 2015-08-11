package in.teacher.adapter;

import in.teacher.sync.CallFTP;
import in.teacher.util.AppGlobal;
import in.teacher.util.NetworkUtils;
import in.teacher.util.SharedPreferenceUtil;

import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class SyncServiceReceiver extends BroadcastReceiver {
    @SuppressWarnings("deprecation")
    @Override
    public void onReceive(final Context context, Intent intent) {
        SharedPreferences sharedPref = context.getSharedPreferences("db_access", Context.MODE_PRIVATE);
        int is_first_sync = sharedPref.getInt("first_sync", 0);
        int tabletLock = sharedPref.getInt("tablet_lock", 0);
        int bootSync = sharedPref.getInt("boot_sync", 0);

        new Thread(new Runnable() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                boolean isScreen = powerManager.isScreenOn();
                WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK
                        | PowerManager.ACQUIRE_CAUSES_WAKEUP, "WakeLock");

                KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                boolean screenLocked = km.inKeyguardRestrictedInputMode();

                if (screenLocked || !isScreen) {
                    wakeLock.acquire();
                    long endTime = System.currentTimeMillis() + 5 * 1000;
                    while (System.currentTimeMillis() < endTime) {
                    }
                    wakeLock.release();
                }
            }
        }).start();

        if (NetworkUtils.isNetworkConnected(context) && is_first_sync == 0 && tabletLock == 0 && bootSync == 0) {
            if (AppGlobal.isActive()) {
                new CallFTP().syncFTP();
            } else {
                Intent i = new Intent(context, in.teacher.activity.LoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra("create", 1);
                context.startActivity(i);
            }
        } else if (tabletLock == 1 && is_first_sync == 0) {
            Intent i = new Intent(context, in.teacher.activity.LockActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(i);
        } else if (tabletLock == 2 && is_first_sync == 0) {
            Intent i = new Intent(context, in.teacher.activity.ServerBlock.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(i);
        } else if (is_first_sync == 0) {
            SharedPreferenceUtil.updateSleepSync(context, 1);
        }
    }

    public void SetAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, SyncServiceReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 3, pi);
    }
}
