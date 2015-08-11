package in.teacher.sync;

import in.teacher.adapter.SchoolId;
import in.teacher.dao.StAvgDao;
import in.teacher.dao.SubjectTeacherDao;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.SqlDbHelper;
import in.teacher.sqlite.SubjectTeacher;
import in.teacher.util.AppGlobal;
import in.teacher.util.PercentageSlipTest;

import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

public class SlipTestProgress {
    private Context context;
    private SqlDbHelper sqlHandler;
    private SQLiteDatabase sqliteDatabase;
    private ProgressDialog pDialog;

    public SlipTestProgress(Context con_text) {
        context = con_text;
    }

    class CalledStProgress extends AsyncTask<String, String, String> {
        private int avg;

        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(context);
            pDialog.setMessage("Preparing data (SlipTest Progress)...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            sqlHandler = AppGlobal.getSqlDbHelper();
            sqliteDatabase = AppGlobal.getSqliteDatabase();
            List<SubjectTeacher> stList = SubjectTeacherDao.selectSubjectTeacher(sqliteDatabase);
            for (SubjectTeacher st : stList) {
                avg = PercentageSlipTest.findSlipTestPercentage(context, st.getSectionId(), st.getSubjectId(), st.getSchoolId());
                StAvgDao.initStAvg(st.getClassId(), st.getSectionId(), st.getSubjectId(), avg, sqliteDatabase);
            }
            return null;
        }

        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            SharedPreferences sharedPref = context.getSharedPreferences("db_access", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("tablet_lock", 0);
            editor.putInt("is_sync", 0);
            editor.putInt("first_sync", 0);
            editor.putInt("sleep_sync", 0);
            editor.putInt("newly_updated", 0);
            editor.apply();

            TempDao.updateSyncComplete(sqliteDatabase);
            int schoolId = SchoolId.getSchoolId(context);
            sqlHandler.deleteLocked(sqliteDatabase);
            sqlHandler.createIndex(sqliteDatabase);
            sqlHandler.createTrigger(schoolId, sqliteDatabase);
            pDialog.dismiss();

            Intent intent = new Intent(context, in.teacher.adapter.SyncService.class);
            context.startService(intent);

            Intent i = new Intent(context, in.teacher.activity.LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(i);
        }
    }

    public void findStProgress() {
        new CalledStProgress().execute();
    }

}
