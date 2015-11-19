package in.teacher.sync;

import android.app.IntentService;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Environment;
import android.util.Log;

import com.amazonaws.mobileconnectors.s3.transfermanager.Download;
import com.amazonaws.mobileconnectors.s3.transfermanager.Transfer;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.amazonaws.mobileconnectors.s3.transfermanager.Upload;
import com.amazonaws.services.s3.model.ProgressEvent;
import com.amazonaws.services.s3.model.ProgressListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import in.teacher.dao.TempDao;
import in.teacher.model.TransferModel;
import in.teacher.sqlite.SqlDbHelper;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.Constants;
import in.teacher.util.SharedPreferenceUtil;
import in.teacher.util.Util;

public class SyncIntentService extends IntentService implements StringConstant {

    private SqlDbHelper sqlHandler;
    private SQLiteDatabase sqliteDatabase;
    private Context context;
    private int schoolId, block, manualSync;
    private String zipFile, deviceId, fileName;
    private TransferManager mTransferManager;
    private SharedPreferences sharedPref;

    public SyncIntentService() {
        super("SyncIntentService");
        context = AppGlobal.getContext();
        sqlHandler = AppGlobal.getSqlDbHelper();
        sqliteDatabase = AppGlobal.getSqliteDatabase();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            checkDownloadStatus();
            decideUploadDownload();
        }
    }

    private void checkDownloadStatus() {
        sharedPref = context.getSharedPreferences("db_access", Context.MODE_PRIVATE);

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.getApplicationContext().registerReceiver(null, ifilter);
        int batteryLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);

        mTransferManager = new TransferManager(Util.getCredProvider(context));

        JSONObject ack_json = new JSONObject();
        Temp t = TempDao.selectTemp(sqliteDatabase);
        schoolId = t.getSchoolId();
        deviceId = t.getDeviceId();
        try {
            ack_json.put("school", schoolId);
            ack_json.put("tab_id", deviceId);
            ack_json.put("battery_status", batteryLevel);
            JSONObject jsonReceived = new JSONObject(RequestResponseHandler.reachServer(ask_for_download_file, ack_json));
            block = jsonReceived.getInt(TAG_SUCCESS);
            Log.d("block", block + "");
            if (jsonReceived.getInt("update") == 1) {
                String folder = jsonReceived.getString("version");
                SharedPreferenceUtil.updateApkUpdate(context, 1, folder);
            }
            zipFile = jsonReceived.getString("folder_name");
            String s = jsonReceived.getString("files");

            String[] sArray = s.split(",");
            for (String split : sArray) {
                TempDao.updateSyncTimer(sqliteDatabase);
                sqlHandler.insertDownloadedFile(split, sqliteDatabase);
            }
        } catch (JSONException e) {
            zipFile = "";
            e.printStackTrace();
        }
    }

    private void decideUploadDownload() {
        int length = 0;
        Cursor c = sqliteDatabase.rawQuery("select filename from uploadedfile where processed=0", null);
        c.moveToFirst();
        length = c.getCount();
        c.close();
        if (length > 0 && block != 2) {
            uploadFile();
        } else if (block != 2 && zipFile != "") {
            downloadFile();
        } else {
            exitSync();
        }
    }

    private void uploadFile() {
        Log.d("uploadFile", "uh");
        Cursor c = sqliteDatabase.rawQuery("select filename from uploadedfile where processed=0", null);
        c.moveToFirst();
        String f = c.getString(c.getColumnIndex("filename"));
        c.close();

        TempDao.updateSyncTimer(sqliteDatabase);
        UploadModel model = new UploadModel(context, f, mTransferManager);
        model.upload();
    }

    public class UploadModel extends TransferModel {
        private String fileNam;
        private Upload mUpload;
        private ProgressListener mListener;
        private Status mStatus;

        public UploadModel(Context context, String key, TransferManager manager) {
            super(context, Uri.parse(key), manager);
            fileNam = key;
            mStatus = Status.IN_PROGRESS;
            mListener = new ProgressListener() {
                @Override
                public void progressChanged(ProgressEvent event) {
                    if (event.getEventCode() == ProgressEvent.COMPLETED_EVENT_CODE) {
                        mStatus = Status.COMPLETED;
                        ackUploadedFile(fileNam);
                    } else if (event.getEventCode() == ProgressEvent.FAILED_EVENT_CODE) {
                        exitSync();
                    }
                }
            };
        }

        @Override
        public Status getStatus() {
            return mStatus;
        }

        @Override
        public Transfer getTransfer() {
            return mUpload;
        }

        public void upload() {
            try {
                File root = android.os.Environment.getExternalStorageDirectory();
                File dir = new File(root.getAbsolutePath() + "/Upload");
                File file = new File(dir, fileNam);
                mUpload = getTransferManager().upload(
                        Constants.BUCKET_NAME.toLowerCase(Locale.US), "upload/zipped_folder/" + fileNam,
                        file);
                mUpload.addProgressListener(mListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void abort() {
        }

        @Override
        public void pause() {
        }

        @Override
        public void resume() {
        }
    }

    private void ackUploadedFile(String f) {
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + "/Upload");
        File file = new File(dir, f);

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("school", schoolId);
            jsonObject.put("tab_id", deviceId);
            jsonObject.put("file_name", f.substring(0, f.length() - 3) + "sql");
            JSONObject jsonReceived = new JSONObject(RequestResponseHandler.reachServer(acknowledge_uploaded_file, jsonObject));
            if (jsonReceived.getInt(TAG_SUCCESS) == 1) {
                TempDao.updateSyncTimer(sqliteDatabase);
                file.delete();
                sqliteDatabase.execSQL("update uploadedfile set processed=1 where filename='" + f + "'");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        decideUploadDownload();
    }

    private void downloadFile() {
        Log.d("downloadFile", "uh");
        fileName = "download/" + schoolId + "/zipped_folder/" + zipFile;
        DownloadModel model = new DownloadModel(context, fileName, mTransferManager);
        model.download();
    }

    private void exitSync() {
        Log.d("exitSync", "uh");
        manualSync = sharedPref.getInt("manual_sync", 0);
        int updateApk = sharedPref.getInt("update_apk", 0);
        SharedPreferences.Editor editor = sharedPref.edit();
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        boolean screenLocked = km.inKeyguardRestrictedInputMode();
        if (block == 2) {
            editor.putInt("manual_sync", 0);
            editor.putInt("tablet_lock", 2);
            editor.apply();
        } else if (updateApk == 1) {
            editor.putInt("manual_sync", 0);
            editor.putInt("update_apk", 2);
            editor.apply();
            Intent intent = new Intent(context, in.teacher.activity.UpdateApk.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else if (manualSync == 1) {
            editor.putInt("manual_sync", 0);
            editor.apply();
            Intent intent = new Intent(context, in.teacher.activity.LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else if (screenLocked) {
            SharedPreferenceUtil.updateIsSync(context, 1);
            Intent i = new Intent(context, in.teacher.activity.ProcessFiles.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        } else {
            SharedPreferenceUtil.updateSleepSync(context, 1);
        }

    }

    public class DownloadModel extends TransferModel {
        private Download mDownload;
        private ProgressListener mListener;
        private String mKey;
        private Status mStatus;

        public DownloadModel(Context context, String key, TransferManager manager) {
            super(context, Uri.parse(key), manager);
            mKey = key;
            mStatus = Status.IN_PROGRESS;
            mListener = new ProgressListener() {
                @Override
                public void progressChanged(ProgressEvent event) {
                    if (event.getEventCode() == ProgressEvent.COMPLETED_EVENT_CODE) {
                        mStatus = Status.COMPLETED;
                        unzipAndAck();
                    } else if (event.getEventCode() == ProgressEvent.FAILED_EVENT_CODE) {
                        finishSync();
                    }
                }
            };
        }

        @Override
        public Status getStatus() {
            return mStatus;
        }

        @Override
        public Transfer getTransfer() {
            return mDownload;
        }

        public void download() {
            try {
                mStatus = Status.IN_PROGRESS;
                File file = new File(
                        Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOWNLOADS),
                        getFileName());

                mDownload = getTransferManager().download(
                        Constants.BUCKET_NAME.toLowerCase(Locale.US), mKey, file);

                if (mListener != null) {
                    mDownload.addProgressListener(mListener);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void abort() {
        }

        @Override
        public void pause() {
        }

        @Override
        public void resume() {
        }
    }

    public void unZipIt() {
        byte[] buffer = new byte[1024];
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
        try {
            if (!dir.exists()) {
                dir.mkdir();
            }
            ZipInputStream zis = new ZipInputStream(new FileInputStream(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), zipFile)));
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(dir + File.separator + fileName);
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();

            File zip = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), zipFile);
            zip.delete();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void unzipAndAck() {
        Log.d("unzipAndAck", "uh");
        unZipIt();
        StringBuffer sb = new StringBuffer();
        Cursor c2 = sqliteDatabase.rawQuery("select filename from downloadedfile where downloaded=0", null);
        c2.moveToFirst();
        while (!c2.isAfterLast()) {
            sb.append(c2.getString(c2.getColumnIndex("filename"))).append("','");
            c2.moveToNext();
        }
        c2.close();

        if (sb.length() > 3) {
            sqliteDatabase.execSQL("update downloadedfile set downloaded=1 where filename in('" + sb.substring(0, sb.length() - 3) + "')");
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("school", schoolId);
                jsonObject.put("tab_id", deviceId);
                jsonObject.put("file_name", "'" + sb.substring(0, sb.length() - 3) + "'");
                JSONObject jsonReceived = new JSONObject(RequestResponseHandler.reachServer(update_downloaded_file, jsonObject));
                if (jsonReceived.getInt(TAG_SUCCESS) == 1)
                    Log.d("update", "downloaded_file");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        finishSync();
    }

    private void finishSync() {
        Log.d("finishSync", "uh");
        manualSync = SharedPreferenceUtil.getManualSync(context);
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        boolean screenLocked = km.inKeyguardRestrictedInputMode();

        if (manualSync == 1) {
            SharedPreferenceUtil.updateManualSync(context, 2);
            Intent i = new Intent(context, in.teacher.activity.ProcessFiles.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        } else if (screenLocked) {
            SharedPreferenceUtil.updateIsSync(context, 1);
            Intent i = new Intent(context, in.teacher.activity.ProcessFiles.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        } else {
            SharedPreferenceUtil.updateSleepSync(context, 1);
        }
    }

}
