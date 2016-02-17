package in.teacher.sync;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import in.teacher.sqlite.SqlDbHelper;

/**
 * Created by vinkrish.
 * Currently commented the logic to calculate average of activity on master data.
 */
public class ActivityProgress {
    private Context context;
    static SqlDbHelper sqlHandler;
    private SQLiteDatabase sqliteDatabase;
    private ProgressDialog pDialog;

    public ActivityProgress(Context con_text) {
        context = con_text;
    }

    class CalledActivityProgress extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(context);
            pDialog.setMessage("Preparing data (Activity Progress)...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            sqlHandler = SqlDbHelper.getInstance(context);
            sqliteDatabase = sqlHandler.getWritableDatabase();

            //ActivitiDao.updateActivityAvg(sqliteDatabase);
            //ActivitiDao.updateSubactActAvg(sqliteDatabase);

            return null;
        }

        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pDialog.dismiss();
            new ExamProgress(context).findExmProgress();
        }
    }

    public void findActProgress() {
        new CalledActivityProgress().execute();
    }

}
