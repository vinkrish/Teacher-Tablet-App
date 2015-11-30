package in.teacher.sync;

import in.teacher.dao.TempDao;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by vinkrish.
 */
public class UploadError implements StringConstant {
    private SQLiteDatabase sqliteDatabase;
    private Context appContext;
    private String deviceId;
    private int schoolId;
    private String errorReport;
    private ProgressDialog pDialog;

    public UploadError(Activity context, String error) {
        errorReport = error;
        appContext = context;
        sqliteDatabase = AppGlobal.getSqliteDatabase();
        pDialog = new ProgressDialog(context);
    }

    class CalledUploadError extends AsyncTask<String, String, String> {
        private JSONObject jsonReceived;

        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Uploading Error...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... arg0) {
            JSONObject json = new JSONObject();
            Temp t = TempDao.selectTemp(sqliteDatabase);
            deviceId = t.getDeviceId();
            schoolId = t.getSchoolId();

            try {
                json.put("school", schoolId);
                json.put("tab_id", deviceId);
                json.put("log", errorReport);
                json.put("date", getToday());
                jsonReceived = new JSONObject(RequestResponseHandler.reachServer(logged, json));
            } catch (JSONException e1) {
                e1.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pDialog.dismiss();
            Intent intent = new Intent(appContext, in.teacher.activity.LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            appContext.startActivity(intent);
        }
    }

    private String getToday() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date today = new Date();
        return dateFormat.format(today);
    }

    public void upError() {
        new CalledUploadError().execute();
    }

}
