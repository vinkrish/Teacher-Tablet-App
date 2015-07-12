package in.teacher.sync;

import in.teacher.dao.TempDao;
import in.teacher.model.TransferModel;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.Constants;
import in.teacher.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.json.JSONException;
import org.json.JSONObject;

import com.amazonaws.mobileconnectors.s3.transfermanager.Download;
import com.amazonaws.mobileconnectors.s3.transfermanager.Transfer;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.amazonaws.services.s3.model.ProgressEvent;
import com.amazonaws.services.s3.model.ProgressListener;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

@SuppressWarnings("deprecation")
public class IntermediateDownloadTask extends AsyncTask<String, String, String> implements StringConstant{
	private TransferManager mTransferManager;
	private String fileName;
	private Context context;
	private boolean downloadCompleted;
	private SQLiteDatabase sqliteDatabase;
	private JSONObject jsonReceived;
	private String deviceId, zipFile;
	private int schoolId, manualSync;

	public IntermediateDownloadTask(Context context, String fileName){
		this.context = context;
		zipFile = fileName;
		sqliteDatabase = AppGlobal.getSqliteDatabase();
		Temp t = TempDao.selectTemp(sqliteDatabase);
		deviceId = t.getDeviceId();
		schoolId = t.getSchoolId();
		this.fileName = "download/"+schoolId+"/zipped_folder/"+fileName;
	}

	@Override
	protected String doInBackground(String... params) {

		downloadCompleted = false;
		mTransferManager = new TransferManager(Util.getCredProvider(context));

		DownloadModel model = new DownloadModel(context, fileName, mTransferManager);
		model.download();


		while(!downloadCompleted){
			Log.d("download", "...");
		}

		unZipIt(zipFile);

		StringBuffer sb = new StringBuffer();
		Cursor c2 = sqliteDatabase.rawQuery("select filename from downloadedfile where downloaded=0", null);
		c2.moveToFirst();
		while(!c2.isAfterLast()){
			sb.append(c2.getString(c2.getColumnIndex("filename"))).append("','");
			c2.moveToNext();
		}
		c2.close();

		if(sb.length()>3){
			sqliteDatabase.execSQL("update downloadedfile set downloaded=1 where filename in('"+sb.substring(0, sb.length()-3)+"')");	
			try{
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("school", schoolId);
				jsonObject.put("tab_id", deviceId);
				jsonObject.put("file_name", "'"+sb.substring(0, sb.length()-3)+"'");
				jsonReceived = UploadSyncParser.makePostRequest(update_downloaded_file, jsonObject);
				if(jsonReceived.getInt(TAG_SUCCESS)==1){}
			}catch(JSONException e){
				e.printStackTrace();
			}catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}


	protected void onPostExecute(String s){
		super.onPostExecute(s);
		SharedPreferences sharedPref = context.getSharedPreferences("db_access", Context.MODE_PRIVATE);
		manualSync = sharedPref.getInt("manual_sync", 0);
		SharedPreferences.Editor editor = sharedPref.edit();
		/*PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
		boolean isScreen = pm.isScreenOn();*/
		KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
		boolean screenLocked = km.inKeyguardRestrictedInputMode();

		if (manualSync == 1) {
            editor.putInt("manual_sync", 0);
            editor.apply();
			Intent intent = new Intent(context, in.teacher.activity.LoginActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			context.startActivity(intent);
		} else if(screenLocked) {
			/*KeyguardManager km = (KeyguardManager) appContext.getSystemService(Context.KEYGUARD_SERVICE); 
			final KeyguardManager.KeyguardLock kl = km .newKeyguardLock("MyKeyguardLock"); 
			try{
			    kl.disableKeyguard();
			}catch (SecurityException e){
			    e.printStackTrace();
			}*/
			editor.putInt("is_sync", 1);
			editor.apply();

			Intent i = new Intent(context, in.teacher.activity.ProcessFiles.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			context.startActivity(i);
		}else{
			editor.putInt("sleep_sync", 1);
			editor.apply();
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
						Log.d("downloading", "completed");
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

	public void unZipIt(String zipFile){
		byte[] buffer = new byte[1024];
		File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
		try{
			if(!dir.exists()){
				dir.mkdir();
			}
			ZipInputStream zis = new ZipInputStream(new FileInputStream(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), zipFile)));
			ZipEntry ze = zis.getNextEntry();

			while(ze!=null){
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

		}catch(IOException ex){
			ex.printStackTrace();
		}
	}

}
