package in.teacher.sync;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import in.teacher.sqlite.SqlDbHelper;

/**
 * Created by vinkrish.
 * Currently removed the logic to calculate average of exam on master data.
 */
public class ExamProgress {
    private Context context;
    private SqlDbHelper sqlHandler;
    private ProgressDialog pDialog;

    public ExamProgress(Context con_text) {
        context = con_text;
    }

    class CalledExamProgress extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(context);
            pDialog.setMessage("Preparing data (Exam Progress)...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            sqlHandler = SqlDbHelper.getInstance(context);

            return null;
        }

        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pDialog.dismiss();
            new SlipTestProgress(context).findStProgress();
        }
    }


    public void findExmProgress() {
        new CalledExamProgress().execute();
    }

}
