package in.teacher.adapter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import in.teacher.util.NetworkUtils;

public class TestInternetReceiver extends BroadcastReceiver {
    SharedPreferences sharedPref;
    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        sharedPref = context.getSharedPreferences("internet_access", Context.MODE_PRIVATE);
       // if(NetworkUtils.isNetworkConnected(context)){
            new InternetTask().execute();
       // }
    }

    class InternetTask extends AsyncTask<Void, Void, Void>{
        boolean connection;
        @Override
        protected Void doInBackground(Void... params) {
            connection = NetworkUtils.hasInternetAccess();
            return null;
        }

        protected void onPostExecute(Void v){
            super.onPostExecute(v);
            if(connection) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("i_failed_status", 0);
                editor.putInt("i_failed_count", 0);
                editor.putInt("ignore_count", 0);
                editor.apply();
            }else{
                int internetFailedCount = sharedPref.getInt("i_failed_count", 0);
                if(internetFailedCount>=17){
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt("i_failed_status", 1);
                    editor.apply();
                    context.sendBroadcast(new Intent("I_FAILED_ME"));
                }else{
                    internetFailedCount++;
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt("i_failed_count", internetFailedCount);
                    editor.apply();
                }
            }
        }
    }

    public void SetAlarm(Context context){
        AlarmManager am =(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, TestInternetReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 5, pi);
    }
}
