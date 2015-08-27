package in.teacher.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.amazonaws.mobileconnectors.s3.transfermanager.Download;
import com.amazonaws.mobileconnectors.s3.transfermanager.Transfer;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.amazonaws.services.s3.model.ProgressEvent;
import com.amazonaws.services.s3.model.ProgressListener;

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
import in.teacher.sqlite.Temp;
import in.teacher.sync.StringConstant;
import in.teacher.util.AppGlobal;
import in.teacher.util.Constants;
import in.teacher.util.SharedPreferenceUtil;
import in.teacher.util.Util;

public class UpdateApk extends BaseActivity {
    private SharedPreferences sharedPref;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_apk);
        pDialog = new ProgressDialog(this);

        sharedPref = getSharedPreferences("db_access", Context.MODE_PRIVATE);
        int updateApk = sharedPref.getInt("update_apk", 0);
        if (updateApk == 2) {
            new ApkDownloadTask(this.getApplicationContext(), "teacher.zip").execute();
        }

    }

    public void updateClicked(View v) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("update_apk", 1);
        editor.putInt("manual_sync", 1);
        editor.putInt("is_sync", 1);
        editor.apply();
        Intent intent = new Intent(this, ProcessFiles.class);
        startActivity(intent);
    }

    @SuppressWarnings("deprecation")
    public class ApkDownloadTask extends AsyncTask<String, String, String> implements StringConstant {
        private TransferManager mTransferManager;
        private String fileName;
        private Context context;
        private boolean downloadCompleted, exception;

        public ApkDownloadTask(Context context, String fName) {
            this.context = context;
            this.fileName = "download/" + fName;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Downloading Apk ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            exception = false;
            downloadCompleted = false;
            mTransferManager = new TransferManager(Util.getCredProvider(context));

            DownloadModel model = new DownloadModel(context, fileName, mTransferManager);
            model.download();

            while (!downloadCompleted)
                Log.d("download", "...");

            if(!exception)
                unZipIt("teacher.zip");

            return null;
        }


        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pDialog.dismiss();

            if(!exception){
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("apk_update", 0);
                editor.putInt("update_apk", 0);
                editor.apply();

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setDataAndType(Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "teacher.apk")), "application/vnd.android.package-archive");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
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
                            Log.d("download", "completed");
                            mStatus = Status.COMPLETED;
                            downloadCompleted = true;
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
                    downloadCompleted = true;
                    exception = true;
                }
            }

            @Override
            public void abort() {}

            @Override
            public void pause() {}

            @Override
            public void resume() {}
        }

    }

    public void unZipIt(String zipFile) {
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

    @Override
    public void onBackPressed(){

    }

}
