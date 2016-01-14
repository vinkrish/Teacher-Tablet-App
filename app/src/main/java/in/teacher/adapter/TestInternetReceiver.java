package in.teacher.adapter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.util.Calendar;

import in.teacher.util.NetworkUtils;
import in.teacher.util.SharedPreferenceUtil;

/**
 * Created by vinkrish.
 */

public class TestInternetReceiver extends BroadcastReceiver {
    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour >= 9 && hour <= 18) {
            new InternetTask().execute();
        }
    }

    class InternetTask extends AsyncTask<Void, Void, Void> {
        boolean connection;

        @Override
        protected Void doInBackground(Void... params) {
            connection = NetworkUtils.hasInternetAccess();
            return null;
        }

        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            if (connection) {
                SharedPreferenceUtil.updateStatusCountIgnore(context, 0, 0, 0);
            } else {
                int internetFailedCount = SharedPreferenceUtil.getFailedCount(context);
                if (internetFailedCount >= 17) {
                    SharedPreferenceUtil.updateFailedStatus(context, 1);
                    context.sendBroadcast(new Intent("I_FAILED_ME"));
                } else {
                    internetFailedCount++;
                    SharedPreferenceUtil.updateFailedCount(context, internetFailedCount);
                }
            }
        }
    }

    public void SetAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, TestInternetReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 10, pi);
    }
}
