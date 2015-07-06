package in.teacher.sync;

import in.teacher.dao.TempDao;
import in.teacher.model.TransferModel;
import in.teacher.sqlite.SqlDbHelper;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.Constants;
import in.teacher.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;

import com.amazonaws.mobileconnectors.s3.transfermanager.Transfer;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.amazonaws.mobileconnectors.s3.transfermanager.Upload;
import com.amazonaws.services.s3.model.ProgressListener;
import com.amazonaws.services.s3.model.ProgressEvent;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.util.Log;

@SuppressWarnings("deprecation")
public class CallFTP implements StringConstant{
	private SqlDbHelper sqlHandler;
	private SQLiteDatabase sqliteDatabase;
	private Context appContext;
	private int schoolId, block, batteryLevel;
	private String deviceId, zipFile;
	private IntentFilter ifilter;
	private Intent batteryStatus;
	private TransferManager mTransferManager;
	private boolean uploadComplete;
	private boolean exception;

	public CallFTP(){
		appContext = AppGlobal.getContext();
		sqlHandler = AppGlobal.getSqlDbHelper();
		sqliteDatabase = AppGlobal.getSqliteDatabase();
	}

	class CalledFTPSync extends AsyncTask<String, String, String>{
		private JSONObject jsonReceived;
		@Override
		protected String doInBackground(String... arg0){
			mTransferManager = new TransferManager(Util.getCredProvider(appContext));
			uploadComplete = false;
			exception = false;

			JSONObject ack_json = new JSONObject();
			Temp t = TempDao.selectTemp(sqliteDatabase);
			schoolId = t.getSchoolId();
			deviceId = t.getDeviceId();
			try{
				ack_json.put("school", schoolId);
				ack_json.put("tab_id", deviceId);
				ack_json.put("battery_status", batteryLevel);
				Log.d("get_file_req", "1");
				jsonReceived = UploadSyncParser.makePostRequest(ask_for_download_file, ack_json);
				Log.d("get_file_res", "1");
				block = jsonReceived.getInt(TAG_SUCCESS);
				Log.d("block", block+"");
				zipFile = jsonReceived.getString("folder_name");
				String s = jsonReceived .getString("files");
				String[] sArray = s.split(",");
				for(String split: sArray){
					TempDao.updateSyncTimer(sqliteDatabase);
					sqlHandler.insertDownloadedFile(split, sqliteDatabase);
				}
			} catch (JSONException e){
				zipFile = "";
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			ArrayList<String> upFileList = new ArrayList<>();
			Cursor c1 = sqliteDatabase.rawQuery("select filename from uploadedfile where processed=0", null);
			c1.moveToFirst();
			while(!c1.isAfterLast()){
				upFileList.add(c1.getString(c1.getColumnIndex("filename")));
				c1.moveToNext();
			}
			c1.close();

			if(block!=2){

				Log.d("Upload_file_req", "2");

				File root = android.os.Environment.getExternalStorageDirectory();
				File dir = new File (root.getAbsolutePath() + "/Upload");

				for(String f: upFileList){
					TempDao.updateSyncTimer(sqliteDatabase);
					File file = new File(dir, f);
					UploadModel model = new UploadModel(appContext, f, mTransferManager);
					model.upload();

					while(!uploadComplete){
						Log.d("upload", "...");
					}
					
					if(exception){
						uploadComplete = false;
						exception = false;
						break;
					}

					try{
						JSONObject jsonObject = new JSONObject();
						jsonObject.put("school", schoolId);
						jsonObject.put("tab_id", deviceId);
						jsonObject.put("file_name", f.substring(0, f.length()-3)+"sql");
						jsonReceived = UploadSyncParser.makePostRequest(acknowledge_uploaded_file, jsonObject);
						if(jsonReceived.getInt(TAG_SUCCESS)==1){
							TempDao.updateSyncTimer(sqliteDatabase);
							file.delete();
							sqliteDatabase.execSQL("update uploadedfile set processed=1 where filename='"+f+"'");
						}
					}catch(JSONException e){
						e.printStackTrace();
					}catch (IOException e) {
						e.printStackTrace();
					}
					uploadComplete = false;
				}
			}
			return null;
		}

		protected void onPostExecute(String s){
			super.onPostExecute(s);
			SharedPreferences sharedPref = appContext.getSharedPreferences("db_access", Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPref.edit();
			KeyguardManager km = (KeyguardManager) appContext.getSystemService(Context.KEYGUARD_SERVICE);
			boolean screenLocked = km.inKeyguardRestrictedInputMode();
			if(block!=2 && zipFile!=""){
				new IntermediateDownloadTask(appContext, zipFile).execute();
			}else if(block==2){
				editor.putInt("tablet_lock", 2);
				editor.apply();
			}else if(screenLocked){
				Intent i = new Intent(appContext, in.teacher.activity.ProcessFiles.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				appContext.startActivity(i);
			}else{
				editor.putInt("sleep_sync", 1);
				editor.apply();
			}
		}
	}

	public void syncFTP(){
		ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		batteryStatus = appContext.getApplicationContext().registerReceiver(null, ifilter);
		batteryLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		new CalledFTPSync().execute();
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
						Log.d("upload", "complete");
						uploadComplete = true;
					}else if(event.getEventCode() == ProgressEvent.FAILED_EVENT_CODE){
						exception = true;
						uploadComplete = true;
					}else if(event.getEventCode() == ProgressEvent.CANCELED_EVENT_CODE){
						exception = true;
						uploadComplete = true;
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
				File dir = new File (root.getAbsolutePath() + "/Upload");
				File file = new File(dir, fileNam);
				mUpload = getTransferManager().upload(
						Constants.BUCKET_NAME.toLowerCase(Locale.US),"upload/zipped_folder/"+fileNam,
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

}