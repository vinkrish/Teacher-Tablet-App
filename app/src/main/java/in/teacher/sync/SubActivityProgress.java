package in.teacher.sync;

import in.teacher.dao.SubActivityDao;
import in.teacher.sqlite.SqlDbHelper;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

/**
 * Created by vinkrish.
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

            SubActivityDao.updateSubActivityAvg(sqliteDatabase);
            SubActivityDao.checkSubActivityIsMark(sqliteDatabase);
            SubActivityDao.checkSubActivityMarkEmpty(sqliteDatabase);

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
