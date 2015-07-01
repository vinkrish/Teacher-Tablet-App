package in.teacher.adapter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class BootSync extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences sharedPref = context.getSharedPreferences("db_access", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putInt("boot_sync", 1);
		editor.apply();

		SharedPreferences internetPref = context.getSharedPreferences("internet_access", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor2 = internetPref.edit();
		editor2.putInt("i_failed_status", 0);
		editor2.putInt("ignore_count", 0);
		editor2.putInt("i_failed_count", 0);
		editor2.apply();

		intent.setClassName("in.teacher.activity", "in.teacher.activity.LoginActivity");
        intent.putExtra("start_sync", 1);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intent);
	}

}
