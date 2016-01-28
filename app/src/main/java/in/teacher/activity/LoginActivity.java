package in.teacher.activity;

import android.animation.ObjectAnimator;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings.Secure;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import in.teacher.dao.SectionDao;
import in.teacher.dao.TeacherDao;
import in.teacher.dao.TempDao;
import in.teacher.dao.UploadSqlDao;
import in.teacher.sqlite.Section;
import in.teacher.sqlite.Teacher;
import in.teacher.sqlite.Temp;
import in.teacher.sync.SyncIntentService;
import in.teacher.util.AnimationUtils;
import in.teacher.util.AppGlobal;
import in.teacher.util.CommonDialogUtils;
import in.teacher.util.ExceptionHandler;
import in.teacher.util.NetworkUtils;
import in.teacher.util.PKGenerator;
import in.teacher.util.SharedPreferenceUtil;

/**
 * Created by vinkrish.
 * I would write this class a better way if i've to start over again, optimize it if you can.
 */
public class LoginActivity extends BaseActivity {
    private Context context;
    private boolean flag, tvflag, authflag;
    private int mappedId, sectionId, internetStatus;
    private String passwordText;
    private TextView userName, password;
    private SharedPreferences sharedPref;
    private SQLiteDatabase sqliteDatabase;
    private TextView noWifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

        context = AppGlobal.getContext();
        sqliteDatabase = AppGlobal.getSqliteDatabase();

        noWifi = (TextView) findViewById(R.id.no_wifi);
        updateWifiStatus();

        SharedPreferenceUtil.updateSavedVersion(this);
        sharedPref = context.getSharedPreferences("db_access", Context.MODE_PRIVATE);

        int internetBlock = SharedPreferenceUtil.getFailedStatus(this);
        if (internetBlock == 1) {
            Intent i = new Intent(this, in.teacher.activity.InternetBlock.class);
            startActivity(i);
        }

        int tabletLock = sharedPref.getInt("tablet_lock", 0);
        if (tabletLock == 0) {
            login();
        } else if (tabletLock == 1) {
            Intent i = new Intent(this, in.teacher.activity.LockActivity.class);
            startActivity(i);
        } else if (tabletLock == 2) {
            Intent i = new Intent(this, in.teacher.activity.ServerBlock.class);
            startActivity(i);
        }

        checkSyncUpdate();

        int bootSync = sharedPref.getInt("boot_sync", 0);
        if (bootSync == 1) {
            Intent service = new Intent(this, in.teacher.adapter.SyncService.class);
            startService(service);
            SharedPreferenceUtil.updateBootSync(this, 0);
        }

        alertSync();
    }

    private void checkSyncUpdate() {
        if (!TeacherDao.isTeacherPresent(sqliteDatabase)) {
            SharedPreferences.Editor editr = sharedPref.edit();
            editr.putInt("newly_updated", 1);
            editr.apply();
        }

        int apkUpdate = sharedPref.getInt("apk_update", 0);
        int newlyUpdated = sharedPref.getInt("newly_updated", 0);
        if (apkUpdate == 1) {
            Intent i = new Intent(this, in.teacher.activity.UpdateApk.class);
            startActivity(i);
            AnimationUtils.activityEnter(this);
        } else if (newlyUpdated == 1) {
            Intent i = new Intent(this, in.teacher.activity.MasterAuthentication.class);
            startActivity(i);
        }
    }

    private void alertSync() {
        boolean isFile = false;
        internetStatus = SharedPreferenceUtil.getFailedCount(this);
        Cursor c = sqliteDatabase.rawQuery("select filename from uploadedfile where processed=0", null);
        if (c.getCount() > 0) {
            isFile = true;
        }
        c.close();
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if ((UploadSqlDao.isUploadSql(sqliteDatabase) || isFile) && hour >= 15) {
            findViewById(R.id.sync_me).setBackgroundColor(getResources().getColor(R.color.original_red));
        } else if ((UploadSqlDao.isUploadSql(sqliteDatabase) || isFile) && internetStatus != 0) {
            findViewById(R.id.sync_me).setBackgroundColor(getResources().getColor(R.color.original_red));
        } else {
            findViewById(R.id.sync_me).setBackgroundResource(android.R.drawable.btn_default);
            // findViewById(R.id.sync_me).setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void login() {
        int isSync = sharedPref.getInt("is_sync", 0);
        if (isSync == 0) {
            findViewById(R.id.admin).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(LoginActivity.this, in.teacher.activity.MasterAuthentication.class);
                    startActivity(i);
                    AnimationUtils.activityEnter(LoginActivity.this);
                }
            });

            findViewById(R.id.admin).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    View image = findViewById(R.id.admin);
                    ObjectAnimator anim
                            = ObjectAnimator.ofFloat(image, "alpha",
                            1.0f, 0.25f, 0.75f, 0.5f, 0.25f, 1.0f);
                    anim.setDuration(4000);
                    anim.start();
                    return true;
                }
            });

            String android_id = Secure.getString(getBaseContext().getContentResolver(), Secure.ANDROID_ID);
            TempDao.updateDeviceId(android_id, sqliteDatabase);

            TextView timeSync = (TextView) findViewById(R.id.syncTime);
            userName = (TextView) findViewById(R.id.userName);
            password = (TextView) findViewById(R.id.password);

            Temp t = TempDao.selectTemp(sqliteDatabase);
            timeSync.setText(t.getSyncTime());

            int[] buttonIds = {R.id.num1, R.id.num2, R.id.num3, R.id.num4, R.id.num5, R.id.num6, R.id.num7, R.id.num8, R.id.num9, R.id.num0};
            for (int i = 0; i < 10; i++) {
                Button b = (Button) findViewById(buttonIds[i]);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (userName.getText().toString().equals("Username")) {
                            userName.setText("");
                        } else if (password.getText().toString().equals("Password") && tvflag) {
                            password.setText("");
                        }
                        if (userName.getText().toString().equalsIgnoreCase("|")) {
                            userName.setText("");
                            password.setHint("Password");
                        } else if (password.getText().toString().equalsIgnoreCase("|")) {
                            passwordText = "";
                            password.setText("");
                        }

                        if (v.getId() == R.id.num1) updateFields("1");
                        else if (v.getId() == R.id.num2) updateFields("2");
                        else if (v.getId() == R.id.num3) updateFields("3");
                        else if (v.getId() == R.id.num4) updateFields("4");
                        else if (v.getId() == R.id.num5) updateFields("5");
                        else if (v.getId() == R.id.num6) updateFields("6");
                        else if (v.getId() == R.id.num7) updateFields("7");
                        else if (v.getId() == R.id.num8) updateFields("8");
                        else if (v.getId() == R.id.num9) updateFields("9");
                        else if (v.getId() == R.id.num0) updateFields("0");
                    }

                });
            }
            findViewById(R.id.numclear).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (tvflag) {
                        password.setText("|");
                        if (userName.getText().toString().equalsIgnoreCase("|")) {
                            userName.setText("");
                            userName.setHint("Username");
                        }
                    } else {
                        userName.setText("|");
                        if (password.getText().toString().equalsIgnoreCase("|")) {
                            password.setText("");
                            password.setHint("Password");
                        }
                    }
                }
            });
        }
    }

    public void syncClicked(View v) {
        if (NetworkUtils.isNetworkConnected(context))
            new FileCreation().execute();
        else
            CommonDialogUtils.displayAlertWhiteDialog(this, "Please be in WiFi zone or check the status of WiFi");

    }

    public void usernameClicked(View v) {
        if (password.getText().toString().equalsIgnoreCase("|")) {
            password.setText("");
            password.setHint("Password");
        }
        userName.setText("|");
        tvflag = false;
    }

    public void passwordClicked(View v) {
        if (userName.getText().toString().equalsIgnoreCase("|")) {
            userName.setText("");
            userName.setHint("Username");
        }
        password.setText("|");
        tvflag = true;
    }

    BroadcastReceiver fileUploadBCReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            alertSync();
        }
    };

    BroadcastReceiver internetReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent i = new Intent(context, in.teacher.activity.InternetBlock.class);
            context.startActivity(i);
        }
    };

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateWifiStatus();
        }
    };

    BroadcastReceiver screenOnOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String strAction = intent.getAction();

            KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);

            if (strAction.equals(Intent.ACTION_SCREEN_OFF) || strAction.equals(Intent.ACTION_SCREEN_ON)) {
                if (myKM.inKeyguardRestrictedInputMode()) {
                    SharedPreferences pref = context.getSharedPreferences("db_access", Context.MODE_PRIVATE);
                    int is_first_sync = pref.getInt("first_sync", 0);
                    int tabletLock = pref.getInt("tablet_lock", 0);

                    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                    boolean isScreen = pm.isScreenOn();

                    if (NetworkUtils.isNetworkConnected(context) &&
                            !isScreen &&
                            is_first_sync == 0 &&
                            tabletLock == 0) {
                        Intent intentProcess = new Intent(context, in.teacher.activity.ProcessFiles.class);
                        intentProcess.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intentProcess);
                    }
                    //System.out.println("Screen off " + "LOCKED");
                } else {
                    //System.out.println("Screen off " + "UNLOCKED");
                }
            }
        }
    };

    private void updateWifiStatus() {
        if (NetworkUtils.isNetworkConnected(this)) {
            noWifi.setVisibility(View.GONE);
        } else {
            noWifi.setVisibility(View.VISIBLE);
            noWifi.setText("No Internet access / check WiFi");
        }
    }

    private void updateFields(String value) {
        if (tvflag) {
            password.setText(new StringBuffer(password.getText()).append("*"));
            String sb = passwordText + value;
            passwordText = sb;
            if (passwordText.length() == 4)
                authenticate();
        } else {
            String s = userName.getText().toString();
            StringBuffer sb = new StringBuffer(s);
            sb.append(value);
            userName.setText(sb);
            String s2 = userName.getText().toString();
            if (s2.length() == 4)
                preauthenticate();
        }
    }

    private void preauthenticate() {
        password.setText("|");
        tvflag = true;
    }

    private void authenticate() {
        String s = userName.getText().toString();
        if (s.isEmpty()) {
            authflag = false;
        } else {
            List<Teacher> authList = TeacherDao.selectTeacher(sqliteDatabase);
            String enteredId = userName.getText().toString();
            for (Teacher t : authList) {
                if (enteredId.equals(t.getTabUser()) && passwordText.equals(t.getTabPass())) {
                    mappedId = t.getTeacherId();
                    authflag = true;
                    authSuccess();
                }
            }
        }
        if (!authflag) {
            CommonDialogUtils.displayAlertWhiteDialog(this, "User is not Authenticated");
        }
        userName.setText("Username");
        password.setText("Password");
        tvflag = false;
    }

    private void authSuccess() {
        flag = false;
        List<Section> list = SectionDao.selectSection(sqliteDatabase);
        for (Section l : list) {
            if (mappedId == l.getClassTeacherId()) {
                flag = true;
                sectionId = l.getSectionId();
                Temp t = new Temp();
                t.setClassId(l.getClassId());
                t.setSectionId(l.getSectionId());
                t.setTeacherId(l.getClassTeacherId());
                TempDao.updateTemp(t, sqliteDatabase);
                break;
            }
        }
        if (!flag) {
            List<Teacher> tList = TeacherDao.selectTeacher(sqliteDatabase);
            for (Teacher t : tList) {
                if (mappedId == t.getTeacherId()) {
                    flag = true;
                    Temp t2 = new Temp();
                    t2.setClassId(0);
                    t2.setSectionId(0);
                    t2.setTeacherId(t.getTeacherId());
                    TempDao.updateTemp(t2, sqliteDatabase);
                    break;
                }
            }
        }

        Intent intent = new Intent(this, in.teacher.activity.Dashboard.class);
        intent.putExtra("sectionid", String.valueOf(sectionId));
        startActivity(intent);
        AnimationUtils.activityEnterVertical(LoginActivity.this);
    }

    @Override
    public void onBackPressed() {
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        FileChannel source = null;
        FileChannel destination = null;
        String currentDBPath = "/data/" + "in.teacher.activity" + "/databases/teacher.db";
        String backupDBPath = "teacher";
        File currentDB = new File(data, currentDBPath);
        File backupDB = new File(sd, backupDBPath);
        try {
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
            Toast.makeText(this, "DB Exported!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        checkSyncUpdate();
        updateWifiStatus();
        registerBroadcastReceiver();
        registerReceiver(fileUploadBCReceiver, new IntentFilter("FILE_UPLOADED"));
        registerReceiver(broadcastReceiver, new IntentFilter("INTERNET_STATUS"));
        registerReceiver(internetReceiver, new IntentFilter("INTERNET_STATUS"));
        alertSync();
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(fileUploadBCReceiver,
                new IntentFilter("FILE_UPLOADED"));
        //registerReceiver(fileUploadBCReceiver, new IntentFilter("in.teacher.FILE_UPLOADED"));
        registerReceiver(broadcastReceiver, new IntentFilter("INTERNET_STATUS"));
        registerReceiver(internetReceiver, new IntentFilter("INTERNET_STATUS"));
    }

    @Override
    protected void onStop() {
        unregisterReceiver(screenOnOffReceiver);
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(internetReceiver);
        unregisterReceiver(fileUploadBCReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(fileUploadBCReceiver);
        super.onStop();
    }

    private void registerBroadcastReceiver() {
        final IntentFilter theFilter = new IntentFilter();
        theFilter.addAction(Intent.ACTION_SCREEN_ON);
        theFilter.addAction(Intent.ACTION_SCREEN_OFF);
        this.registerReceiver(screenOnOffReceiver, theFilter);
    }

    class FileCreation extends AsyncTask<Void, Void, Void> {
        ProgressDialog pDialog = new ProgressDialog(LoginActivity.this);

        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Submitting marks...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            createUploadFile();
            return null;
        }

        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            pDialog.dismiss();
            Intent syncService = new Intent(context, SyncIntentService.class);
            context.startService(syncService);
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

            } catch (IOException e) {
            }
            sqliteDatabase.execSQL("delete from uploadsql");
        }
    }

}
