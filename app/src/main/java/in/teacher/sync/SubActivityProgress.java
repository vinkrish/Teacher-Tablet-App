package in.teacher.sync;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import in.teacher.sqlite.SqlDbHelper;

/**
 * Created by vinkrish.
 * Currently commented the logic to calculate average of subactivity on master data.
 */
public class SubActivityProgress {
    private Context context;
    private SqlDbHelper sqlHandler;
    private SQLiteDatabase sqliteDatabase;
    private ProgressDialog pDialog;

    public SubActivityProgress(Context con_text) {
        context = con_text;
    }

    class CalledSubActivityProgress extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(context);
            pDialog.setMessage("Preparing data (SubActivities Progress)...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            sqlHandler = SqlDbHelper.getInstance(context);
            sqliteDatabase = sqlHandler.getWritableDatabase();

            //SubActivityDao.updateSubActivityAvg(sqliteDatabase);

            return null;
        }

        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pDialog.dismiss();
            new ActivityProgress(context).findActProgress();
        }

    }

    public void findSubActProgress() {
        new CalledSubActivityProgress().execute();
    }

}
