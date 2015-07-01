package in.teacher.util;

import in.teacher.activity.Restart;

import java.lang.Thread.UncaughtExceptionHandler;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.io.PrintWriter;
import java.io.StringWriter;
import android.provider.Settings.Secure;
import android.util.Log;

public class ExceptionHandler implements UncaughtExceptionHandler {
	private final Activity myContext;
	private final String LINE_SEPARATOR = "\n";

	public ExceptionHandler(Activity act) {
		myContext = act;
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		StringWriter stackTrace = new StringWriter();
		ex.printStackTrace(new PrintWriter(stackTrace));
		StringBuilder errorReport = new StringBuilder();
		errorReport.append(stackTrace.toString());
		errorReport.append(LINE_SEPARATOR);
		String android_id = Secure.getString(myContext.getBaseContext().getContentResolver(),Secure.ANDROID_ID);
		errorReport.append(android_id);
		
		Log.d("error", errorReport+"");

		//	new UploadError(myContext, errorReport.toString()).upError();
		SharedPreferences sharedPref = myContext.getApplicationContext().getSharedPreferences("db_access", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putInt("is_sync", 0);
		editor.putInt("first_sync", 0);
		editor.apply();
		
		int isSync = sharedPref.getInt("is_sync",0);
		Log.d("ExceptionHandling", isSync+"");

		Intent intent = new Intent(myContext, Restart.class);
		intent.putExtra("error", errorReport.toString());
		myContext.startActivity(intent);
		
//		Intent i = new Intent(myContext, TrackWifi.class);
//	    PendingIntent pendingInt = PendingIntent.getBroadcast(myContext, 1, i, PendingIntent.FLAG_UPDATE_CURRENT);
//	    AlarmManager alarmMgr = (AlarmManager) myContext.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
//	    alarmMgr.cancel(pendingInt);

//		Intent intent = new Intent(myContext, in.schoolcom.MainActivity.class);
//		myContext.startActivity(intent);

		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(10);

		/*Intent intent = new Intent();
		intent.setClassName("in.schoolcom", "in.schoolcom.Restart");
		myContext.startActivity(intent);*/

		/*long endTime = System.currentTimeMillis() + 5*1000;
	      while (System.currentTimeMillis() < endTime) {
	          synchronized (this) {
	              try {
	                  wait(endTime - System.currentTimeMillis());
	              } catch (Exception e) {
	              }
	              android.os.Process.killProcess(android.os.Process.myPid());
	      		System.exit(10);
	          }
	      }*/
	}

}
