package in.teacher.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

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

import in.teacher.dao.StAvgDao;
import in.teacher.dao.TempDao;
import in.teacher.dao.UploadSqlDao;
import in.teacher.sqlite.Temp;
import in.teacher.sync.FirstTimeDownload;
import in.teacher.sync.RequestResponseHandler;
import in.teacher.sync.StringConstant;
import in.teacher.sync.SyncIntentService;
import in.teacher.util.AppGlobal;
import in.teacher.util.ExceptionHandler;
import in.teacher.util.PKGenerator;
import in.teacher.util.PercentageSlipTest;

/**
 * Created by vinkrish.
 * This needs to be optimized, good luck with that.
 */
public class ProcessFiles extends BaseActivity implements StringConstant {
    private ProgressBar progressBar;
    private TextView txtPercentage, txtSync;
    private SQLiteDatabase sqliteDatabase;
    private String savedVersion;
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
        savedVersion = pref.getString("saved_version", "v1.3");

        txtPercentage = (TextView) findViewById(R.id.txtPercentage);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        txtSync = (TextView) findViewById(R.id.syncing);

        sqliteDatabase = AppGlobal.getSqliteDatabase();

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
            int schoolId = t.getSchoolId();
            String deviceId = t.getDeviceId();

            isFirstTimeSync = false;

            publishProgress(20 + "", 20 + "", "creating file to be uploaded");
            createUploadFile();
            publishProgress(40 + "", 40 + "", "creating file to be uploaded");

            ArrayList<String> downFileList2 = new ArrayList<>();
            Cursor c3 = sqliteDatabase.rawQuery("select filename from downloadedfile where processed = 0 and downloaded = 1", null);
            c3.moveToFirst();
            while (!c3.isAfterLast()) {
                downFileList2.add(c3.getString(c3.getColumnIndex("filename")));
                c3.moveToNext();
            }
            c3.close();

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
                                e.printStackTrace();
                                SharedPreferences sp = ProcessFiles.this.getSharedPreferences("db_access", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editr = sp.edit();
                                editr.putInt("tablet_lock", 1);
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

            publishProgress("50", 50 + "", "acknowledge processed file");

            StringBuffer sb = new StringBuffer();
            Cursor c4 = sqliteDatabase.rawQuery("select filename from downloadedfile where processed=1 and isack=0", null);
            c4.moveToFirst();
            while (!c4.isAfterLast()) {
                sb.append(c4.getString(c4.getColumnIndex("filename"))).append("','");
                c4.moveToNext();
            }
            c4.close();

            if (!isFirstTimeSync) {
                if (sb.length() > 3) {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("school", schoolId);
                        jsonObject.put("tab_id", deviceId);
                        jsonObject.put("file_name", "'" + sb.substring(0, sb.length() - 3) + "'");
                        jsonObject.put("version", savedVersion);
                        jsonReceived = new JSONObject(RequestResponseHandler.reachServer(update_processed_file, jsonObject));
                        if (jsonReceived.getInt(TAG_SUCCESS) == 1) {
                            sqliteDatabase.execSQL("update downloadedfile set isack=1 where processed=1 and filename in ('" + sb.substring(0, sb.length() - 3) + "')");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            publishProgress("0", 0 + "", "calculating average");

            ArrayList<Integer> subjectIdList = new ArrayList<>();
            ArrayList<Integer> sectionIdList = new ArrayList<>();


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

            publishProgress("70", 70 + "", "calculating average");

            UploadSqlDao.deleteTable("avgtrack", sqliteDatabase);

            return null;
        }

        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            SharedPreferences sharedPref = ProcessFiles.this.getSharedPreferences("db_access", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("is_sync", 0);
            editor.apply();
            if (isException) {
                Intent intent = new Intent(getApplicationContext(), in.teacher.activity.LockActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } else if (isFirstTimeSync) {
                editor.putInt("first_sync", 1);
                editor.apply();
                new FirstTimeDownload().callFirstTimeSync();
            } else if (sharedPref.getInt("update_apk", 0) == 1) {
                Intent syncService = new Intent(ProcessFiles.this, SyncIntentService.class);
                startService(syncService);
            } else {
                Intent intent = new Intent(getApplicationContext(), in.teacher.activity.LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
    public void onBackPressed() {
    }
}
