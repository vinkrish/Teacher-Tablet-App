package in.teacher.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceUtil {

    public static void updateFailedStatus(Context context, int i){
        SharedPreferences sharedPref = context.getSharedPreferences("internet_access", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("i_failed_status", i);
        editor.apply();
    }

    public static void updateFailedCount(Context context, int i){
        SharedPreferences sharedPref = context.getSharedPreferences("internet_access", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("i_failed_count", i);
        editor.apply();
    }

    public static void updateStatusCountIgnore(Context context, int i, int j, int k){
        SharedPreferences sharedPref = context.getSharedPreferences("internet_access", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("i_failed_status", i);
        editor.putInt("i_failed_count", j);
        editor.putInt("ignore_count", k);
        editor.apply();
    }

    public static int getFailedStatus(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences("internet_access", Context.MODE_PRIVATE);
        return sharedPref.getInt("i_failed_status", 0);
    }

    public static int getFailedCount(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences("internet_access", Context.MODE_PRIVATE);
        return sharedPref.getInt("i_failed_count", 0);
    }

    public static int getIgnoreCount(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences("internet_access", Context.MODE_PRIVATE);
        return sharedPref.getInt("ignore_count", 0);
    }

    public static void updatePartition(Context context, int i){
        SharedPreferences sharedPref = context.getSharedPreferences("has_partition", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("partition", i);
        editor.apply();
    }

    public static int getPartition(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences("has_partition", Context.MODE_PRIVATE);
        return sharedPref.getInt("partition",0);
    }

    public static void updateTabletLock(Context context, int i){
        SharedPreferences sharedPref = context.getSharedPreferences("db_access", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("tablet_lock", i);
        editor.apply();
    }

    public static void updateApkUpdate(Context context, int i){
        SharedPreferences sharedPref = context.getSharedPreferences("db_access", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("apk_update", i);
        editor.putInt("newly_updated", 1);
        editor.apply();
    }

    public static void updateFirstSync(Context context, int i){
        SharedPreferences sharedPref = context.getSharedPreferences("db_access", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("first_sync", i);
        editor.apply();
    }

    public static void updateSleepSync(Context context, int i){
        SharedPreferences sharedPref = context.getSharedPreferences("db_access", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("sleep_sync", i);
        editor.apply();
    }

    public static void updateIsSync(Context context, int i){
        SharedPreferences sharedPref = context.getSharedPreferences("db_access", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("is_sync", i);
        editor.apply();
    }

    public static void updateManualSync(Context context, int i){
        SharedPreferences sharedPref = context.getSharedPreferences("db_access", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("manual_sync", i);
        editor.apply();
    }

    public static void updateBootSync(Context context, int i){
        SharedPreferences sharedPref = context.getSharedPreferences("db_access", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("boot_sync", i);
        editor.apply();
    }

    public static void updateSavedVersion(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences("db_access", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("saved_version", "v1.2");
        editor.apply();
    }

    public static String getSavedVersion(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences("db_access", Context.MODE_PRIVATE);
        return sharedPref.getString("saved_version", "v1.2");
    }

    public static int getTabletLock(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences("db_access", Context.MODE_PRIVATE);
        return sharedPref.getInt("tablet_lock", 0);
    }

    public static int getManualSync(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences("db_access", Context.MODE_PRIVATE);
        return sharedPref.getInt("manual_sync", 0);
    }

}
