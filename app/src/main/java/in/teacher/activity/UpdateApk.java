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
import java.util.Locale;

import in.teacher.dao.TempDao;
import in.teacher.model.TransferModel;
import in.teacher.sqlite.Temp;
import in.teacher.sync.StringConstant;
import in.teacher.util.AppGlobal;
import in.teacher.util.Constants;
import in.teacher.util.Util;

public class UpdateApk extends BaseActivity {
    private SharedPreferences sharedPref;
    private ProgressDialog pDialog;
    private String apkName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_apk);
        pDialog = new ProgressDialog(this);

        sharedPref = this.getSharedPreferences("db_access", Context.MODE_PRIVATE);
        int apkUpdate = sharedPref.getInt("apk_update", 0);
        apkName = sharedPref.getString("apk_name", "teacher");

        if (apkUpdate == 2) {
            new ApkDownloadTask(this.getApplicationContext(), apkName).execute();
        }

    }

    public void updateClicked(View v) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("apk_update", 1);
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
        private SQLiteDatabase sqliteDatabase;

        public ApkDownloadTask(Context context, String fileName) {
            this.context = context;
            sqliteDatabase = AppGlobal.getSqliteDatabase();
            this.fileName = "download/" + fileName;
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

            while (!downloadCompleted) {
                Log.d("download", "...");
            }

            return null;
        }


        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pDialog.dismiss();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("apk_update", 0);
            editor.apply();

            if(!exception){
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), apkName+".apk")), "application/vnd.android.package-archive");
                startActivity(intent);
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
            public void abort() {
            }

            @Override
            public void pause() {
            }

            @Override
            public void resume() {
            }
        }

    }


}
