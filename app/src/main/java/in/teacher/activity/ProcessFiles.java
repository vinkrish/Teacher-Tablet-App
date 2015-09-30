package in.teacher.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import in.teacher.dao.ActivitiDao;
import in.teacher.dao.ExmAvgDao;
import in.teacher.dao.StAvgDao;
import in.teacher.dao.SubActivityDao;
import in.teacher.dao.TempDao;
import in.teacher.dao.UploadSqlDao;
import in.teacher.sqlite.Temp;
import in.teacher.sync.CallFTP;
import in.teacher.sync.FirstTimeSync;
import in.teacher.sync.RequestResponseHandler;
import in.teacher.sync.StringConstant;
import in.teacher.util.AppGlobal;
import in.teacher.util.ExceptionHandler;
import in.teacher.util.PKGenerator;
import in.teacher.util.PercentageSlipTest;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by vinkrish.
 */
public class ProcessFiles extends BaseActivity implements StringConstant {
    private Context context;
    private ProgressBar progressBar;
    private TextView txtPercentage, txtSync;
    private SQLiteDatabase sqliteDatabase;
    private int schoolId, manualSync;
    private String deviceId, savedVersion;
    private boolean isException = false, isFirstTimeSync = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_files);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

		/*PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WakeLock");
			wakeLock.acquire();*/

        SharedPreferences pref = getSharedPreferences("db_access", Context.MODE_PRIVATE);
        manualSync = pref.getInt("manual_sync", 0);
        savedVersion = pref.getString("saved_version", "v1.3");

        txtPercentage = (TextView) findViewById(R.id.txtPercentage);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        txtSync = (TextView) findViewById(R.id.syncing);

        sqliteDatabase = AppGlobal.getSqliteDatabase();
        context = AppGlobal.getContext();

        new ProcessedFiles().execute();
    }

    class ProcessedFiles extends AsyncTask<String, String, String> {
        private JSONObject jsonReceived;

        @Override
        protected void onProgressUpdate(String... progress) {
            txtPercentage.setText(progress[0] + "%");
            progressBar.setProgress(Integer.parseInt(progress[1]));
            txtSync.setText(progress[2]);
        }

        @Override
        protected String doInBackground(String... params) {
            Temp t = TempDao.selectTemp(sqliteDatabase);
            schoolId = t.getSchoolId();
            deviceId = t.getDeviceId();

            isFirstTimeSync = false;

            publishProgress(40 + "", 40 + "", "creating file to be uploaded");
            createUploadFile();
            publishProgress(80 + "", 80 + "", "creating file to be uploaded");

            ArrayList<String> downFileList2 = new ArrayList<>();
            Cursor c3 = sqliteDatabase.rawQuery("select filename from downloadedfile where processed=0", null);
            c3.moveToFirst();
            while (!c3.isAfterLast()) {
                downFileList2.add(c3.getString(c3.getColumnIndex("filename")));
                c3.moveToNext();
            }
            c3.close();

            Log.d("process_file_req", "...");

            int fileCount = downFileList2.size();
            int fileIndex = 0;
            int queryCount = 0;
            int queryIndex = 0;
            try {
                for (String f : downFileList2) {
                    fileIndex += 1;
                    queryIndex = 0;
                    queryCount = countLines(f);
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), f);
                    BufferedReader input = new BufferedReader(new FileReader(file));
                    try {
                        String line = null;
                        while ((line = input.readLine()) != null) {
                            queryIndex += 1;
                            int percent = (int) (((double) queryIndex / queryCount) * 100);
                            publishProgress(percent + "", percent + "", "processing file " + fileIndex + " of " + fileCount);
                            try {
                                sqliteDatabase.execSQL(line);
                            } catch (SQLException e) {
                                SharedPreferences sp = ProcessFiles.this.getSharedPreferences("db_access", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editr = sp.edit();
                                editr.putInt("tablet_lock", 1);
                                editr.putInt("is_sync", 0);
                                editr.putInt("sleep_sync", 0);
                                editr.apply();
                                String except = e + "";
                                try {
                                    sqliteDatabase.execSQL("insert into locked(FileName,LineNumber,StackTrace) values('" + f + "'," + queryIndex + ",'" + except.replaceAll("['\"]", " ") + "')");
                                } catch (SQLException ex) {
                                    ex.printStackTrace();
                                }
                                isException = true;
                            }
                        }
                    } finally {
                        input.close();
                    }
                    try {
                        sqliteDatabase.execSQL("update downloadedfile set processed=1 where filename='" + f + "'");
                        file.delete();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                isFirstTimeSync = true;
            }
            Log.d("process_file_res", "...");

            publishProgress("100", 100 + "", "acknowledge processed file");

            StringBuffer sb = new StringBuffer();
            Cursor c4 = sqliteDatabase.rawQuery("select filename from downloadedfile where processed=1 and isack=0", null);
            c4.moveToFirst();
            while (!c4.isAfterLast()) {
                sb.append(c4.getString(c4.getColumnIndex("filename"))).append("','");
                c4.moveToNext();
            }
            c4.close();

            if (!isFirstTimeSync) {
                Log.d("ack_file_req", "...");
                if (sb.length() > 3) {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("school", schoolId);
                        jsonObject.put("tab_id", deviceId);
                        jsonObject.put("file_name", "'" + sb.substring(0, sb.length() - 3) + "'");
                        jsonObject.put("version", savedVersion);
                        jsonReceived = new JSONObject(RequestResponseHandler.reachServer(update_processed_file, jsonObject));
                        Log.d("request_update", jsonObject + "");
                        if (jsonReceived.getInt(TAG_SUCCESS) == 1) {
                            sqliteDatabase.execSQL("update downloadedfile set isack=1 where processed=1 and filename in ('" + sb.substring(0, sb.length() - 3) + "')");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.d("ack_file_res", "...");
            }

            publishProgress("0", 0 + "", "calculating average");

            ArrayList<Integer> examIdList = new ArrayList<>();
            ArrayList<Integer> subjectIdList = new ArrayList<>();
            ArrayList<Integer> activityIdList = new ArrayList<>();
            ArrayList<Integer> subActIdList = new ArrayList<>();
            ArrayList<Integer> sectionIdList = new ArrayList<>();

            Cursor c5 = sqliteDatabase.rawQuery("select distinct ExamId,SubjectId from avgtrack " +
                    "where Type=0 and ActivityId=0 and SubActivityId=0 and SectionId=0 and ExamId!=0 and SubjectId!=0", null);
            c5.moveToFirst();
            while (!c5.isAfterLast()) {
                examIdList.add(c5.getInt(c5.getColumnIndex("ExamId")));
                subjectIdList.add(c5.getInt(c5.getColumnIndex("SubjectId")));
                c5.moveToNext();
            }
            c5.close();
            for (int i = 0, j = examIdList.size(); i < j; i++) {
                ExmAvgDao.insertExmAvg(examIdList.get(i), subjectIdList.get(i), sqliteDatabase);
                ExmAvgDao.checkExamMarkEmpty(examIdList.get(i), subjectIdList.get(i), sqliteDatabase);
            }
            examIdList.clear();
            subjectIdList.clear();

            Cursor c6 = sqliteDatabase.rawQuery("select distinct ExamId,SubjectId from avgtrack " +
                    "where Type=1 and ActivityId=0 and SubActivityId=0 and SectionId=0 and ExamId!=0 and SubjectId!=0", null);
            c6.moveToFirst();
            while (!c6.isAfterLast()) {
                examIdList.add(c6.getInt(c6.getColumnIndex("ExamId")));
                subjectIdList.add(c6.getInt(c6.getColumnIndex("SubjectId")));
                c6.moveToNext();
            }
            c6.close();
            for (int i = 0, j = examIdList.size(); i < j; i++) {
                ExmAvgDao.updateExmAvg(examIdList.get(i), subjectIdList.get(i), sqliteDatabase);
                ExmAvgDao.checkExamMarkEmpty(examIdList.get(i), subjectIdList.get(i), sqliteDatabase);
            }
            examIdList.clear();
            subjectIdList.clear();

            publishProgress("20", 20 + "", "calculating average");

            Cursor c7 = sqliteDatabase.rawQuery("select distinct ExamId,ActivityId,SubjectId from avgtrack " +
                    "where Type=0 and SubActivityId=0 and SectionId=0 and ExamId!=0 and ActivityId!=0 and SubjectId!=0", null);
            c7.moveToFirst();
            while (!c7.isAfterLast()) {
                examIdList.add(c7.getInt(c7.getColumnIndex("ExamId")));
                activityIdList.add(c7.getInt(c7.getColumnIndex("ActivityId")));
                subjectIdList.add(c7.getInt(c7.getColumnIndex("SubjectId")));
                c7.moveToNext();
            }
            if (c7.getCount() > 0) {
                ActivitiDao.updateActivityAvg(activityIdList, sqliteDatabase);
                ActivitiDao.checkActivityMarkEmpty(activityIdList, sqliteDatabase);
                ExmAvgDao.insertExmActAvg(examIdList, subjectIdList, sqliteDatabase);
                ExmAvgDao.checkExmActMarkEmpty(examIdList, subjectIdList, sqliteDatabase);
            }
            c7.close();
            examIdList.clear();
            activityIdList.clear();
            subjectIdList.clear();

            Cursor c8 = sqliteDatabase.rawQuery("select distinct ExamId,ActivityId,SubjectId from avgtrack " +
                    "where Type=1 and SubActivityId=0 and SectionId=0 and ExamId!=0 and ActivityId!=0 and SubjectId!=0", null);
            c8.moveToFirst();
            while (!c8.isAfterLast()) {
                examIdList.add(c8.getInt(c8.getColumnIndex("ExamId")));
                activityIdList.add(c8.getInt(c8.getColumnIndex("ActivityId")));
                subjectIdList.add(c8.getInt(c8.getColumnIndex("SubjectId")));
                c8.moveToNext();
            }
            if (c8.getCount() > 0) {
                ActivitiDao.updateActivityAvg(activityIdList, sqliteDatabase);
                ActivitiDao.checkActivityMarkEmpty(activityIdList, sqliteDatabase);
                ExmAvgDao.updateExmActAvg(examIdList, subjectIdList, sqliteDatabase);
                ExmAvgDao.checkExmActMarkEmpty(examIdList, subjectIdList, sqliteDatabase);
            }
            c8.close();
            examIdList.clear();
            activityIdList.clear();
            subjectIdList.clear();

            publishProgress("40", 40 + "", "calculating average");

            Cursor c9 = sqliteDatabase.rawQuery("select distinct ExamId,ActivityId,SubActivityId,SubjectId from avgtrack " +
                    "where Type=0 and SectionId=0 and ExamId!=0 and ActivityId!=0 and SubActivityId!=0 and SubjectId!=0", null);
            c9.moveToFirst();
            while (!c9.isAfterLast()) {
                examIdList.add(c9.getInt(c9.getColumnIndex("ExamId")));
                activityIdList.add(c9.getInt(c9.getColumnIndex("ActivityId")));
                subActIdList.add(c9.getInt(c9.getColumnIndex("SubActivityId")));
                subjectIdList.add(c9.getInt(c9.getColumnIndex("SubjectId")));
                c9.moveToNext();
            }
            if (c9.getCount() > 0) {
                SubActivityDao.updateSubActivityAvg(subActIdList, sqliteDatabase);
                ActivitiDao.updateActSubActAvg(activityIdList, sqliteDatabase);
                ExmAvgDao.insertExmActAvg(examIdList, subjectIdList, sqliteDatabase);
                SubActivityDao.checkSubActivityMarkEmpty(subActIdList, sqliteDatabase);
                ActivitiDao.checkActivityMarkEmpty(activityIdList, sqliteDatabase);
                ExmAvgDao.checkExmSubActMarkEmpty(examIdList, subjectIdList, sqliteDatabase);
            }
            c9.close();
            examIdList.clear();
            activityIdList.clear();
            subActIdList.clear();
            subjectIdList.clear();

            Cursor c10 = sqliteDatabase.rawQuery("select distinct ExamId,ActivityId,SubActivityId,SubjectId from avgtrack " +
                    "where Type=1 and SectionId=0 and ExamId!=0 and ActivityId!=0 and SubActivityId!=0 and SubjectId!=0", null);
            c10.moveToFirst();
            while (!c10.isAfterLast()) {
                examIdList.add(c10.getInt(c10.getColumnIndex("ExamId")));
                activityIdList.add(c10.getInt(c10.getColumnIndex("ActivityId")));
                subActIdList.add(c10.getInt(c10.getColumnIndex("SubActivityId")));
                subjectIdList.add(c10.getInt(c10.getColumnIndex("SubjectId")));
                c10.moveToNext();
            }
            if (c10.getCount() > 0) {
                SubActivityDao.updateSubActivityAvg(subActIdList, sqliteDatabase);
                ActivitiDao.updateActSubActAvg(activityIdList, sqliteDatabase);
                ExmAvgDao.updateExmActAvg(examIdList, subjectIdList, sqliteDatabase);
                SubActivityDao.checkSubActivityMarkEmpty(subActIdList, sqliteDatabase);
                ActivitiDao.checkActivityMarkEmpty(activityIdList, sqliteDatabase);
                ExmAvgDao.checkExmSubActMarkEmpty(examIdList, subjectIdList, sqliteDatabase);
            }
            c10.close();
            examIdList.clear();
            activityIdList.clear();
            subActIdList.clear();
            subjectIdList.clear();

            publishProgress("60", 60 + "", "calculating average");

            Cursor c11 = sqliteDatabase.rawQuery("select distinct SubjectId,SectionId from avgtrack " +
                    "where Type=0 and ExamId=0 and ActivityId=0 and SubActivityId=0 and SubjectId!=0 and SectionId!=0", null);
            c11.moveToFirst();
            while (!c11.isAfterLast()) {
                subjectIdList.add(c11.getInt(c11.getColumnIndex("SubjectId")));
                sectionIdList.add(c11.getInt(c11.getColumnIndex("SectionId")));
                c11.moveToNext();
            }
            c11.close();
            for (int i = 0, j = subjectIdList.size(); i < j; i++) {
                double updatedSTAvg = PercentageSlipTest.findSlipTestPercentage(sectionIdList.get(i), subjectIdList.get(i), sqliteDatabase);
                StAvgDao.updateSlipTestAvg(sectionIdList.get(i), subjectIdList.get(i), updatedSTAvg, sqliteDatabase);
            }
            subjectIdList.clear();
            sectionIdList.clear();

            Cursor c12 = sqliteDatabase.rawQuery("select distinct SubjectId,SectionId from avgtrack " +
                    "where Type=1 and ExamId=0 and ActivityId=0 and SubActivityId=0 and SubjectId!=0 and SectionId!=0", null);
            c12.moveToFirst();
            while (!c12.isAfterLast()) {
                subjectIdList.add(c12.getInt(c12.getColumnIndex("SubjectId")));
                sectionIdList.add(c12.getInt(c12.getColumnIndex("SectionId")));
                c12.moveToNext();
            }
            c12.close();
            for (int i = 0, j = subjectIdList.size(); i < j; i++) {
                double updatedSTAvg = PercentageSlipTest.findSlipTestPercentage(sectionIdList.get(i), subjectIdList.get(i), sqliteDatabase);
                StAvgDao.updateSlipTestAvg(sectionIdList.get(i), subjectIdList.get(i), updatedSTAvg, sqliteDatabase);
            }
            subjectIdList.clear();
            sectionIdList.clear();

            publishProgress("80", 80 + "", "calculating average");

            UploadSqlDao.deleteTable("avgtrack", sqliteDatabase);

            return null;
        }

        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            SharedPreferences sharedPref = ProcessFiles.this.getSharedPreferences("db_access", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("is_sync", 0);
            editor.putInt("sleep_sync", 0);
            editor.apply();
            if (isException) {
                editor.putInt("manual_sync", 0);
                editor.apply();
                Intent intent = new Intent(context, in.teacher.activity.LockActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else if (isFirstTimeSync) {
                editor.putInt("manual_sync", 0);
                editor.putInt("first_sync", 1);
                editor.apply();
                new FirstTimeSync().callFirstTimeSync();
            } else if (manualSync == 1) {
                new CallFTP().syncFTP();
            } else {
                editor.putInt("manual_sync", 0);
                editor.apply();
                Intent intent = new Intent(context, in.teacher.activity.LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }

    private int countLines(String filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename)));
        try {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars = 0;
            boolean empty = true;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
            return (count == 0 && !empty) ? 1 : count;
        } finally {
            is.close();
        }
    }

    private void createUploadFile() {
        Cursor c = sqliteDatabase.rawQuery("select * from temp where id = 1", null);
        String deviceId = "";
        int schoolId = 0;
        c.moveToFirst();
        while (!c.isAfterLast()) {
            deviceId = c.getString(c.getColumnIndex("DeviceId"));
            schoolId = c.getInt(c.getColumnIndex("SchoolId"));
            c.moveToNext();
        }
        c.close();

        long timeStamp = PKGenerator.getPrimaryKey();

        Cursor c1 = sqliteDatabase.rawQuery("select Query from uploadsql", null);
        if (c1.getCount() > 0) {
            File root = android.os.Environment.getExternalStorageDirectory();
            File dir = new File(root.getAbsolutePath() + "/Upload");
            dir.mkdirs();
            File file = new File(dir, timeStamp + "_" + deviceId + "_" + schoolId + ".sql");
            file.delete();
            try {
                file.createNewFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
                c1.moveToFirst();
                while (!c1.isAfterLast()) {
                    writer.write(c1.getString(c1.getColumnIndex("Query")) + ";");
                    writer.newLine();
                    c1.moveToNext();
                }
                c1.close();
                writer.close();

                FileOutputStream fileOutputStream = new FileOutputStream(new File(dir, timeStamp + "_" + deviceId + "_" + schoolId + ".zip"));
                ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
                ZipEntry zipEntry = new ZipEntry(file.getName());
                zipOutputStream.putNextEntry(zipEntry);
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] buf = new byte[1024];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buf)) > 0) {
                    zipOutputStream.write(buf, 0, bytesRead);
                }
                fileInputStream.close();
                zipOutputStream.closeEntry();
                zipOutputStream.close();
                fileOutputStream.close();
                sqliteDatabase.execSQL("insert into uploadedfile(filename) values('" + timeStamp + "_" + deviceId + "_" + schoolId + ".zip" + "')");

                file.delete();

            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            }
            sqliteDatabase.execSQL("delete from uploadsql");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.process_files, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
    }
}
