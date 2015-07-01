package in.teacher.activity;

import java.io.IOException;
import java.util.List;

import in.teacher.adapter.Alert;
import in.teacher.dao.SectionDao;
import in.teacher.dao.TeacherDao;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.Section;
import in.teacher.sqlite.Teacher;
import in.teacher.sqlite.Temp;
import in.teacher.sync.CallFTP;
import in.teacher.util.AnimationUtils;
import in.teacher.util.AppGlobal;
import in.teacher.util.ExceptionHandler;
import in.teacher.util.NetworkUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class LoginActivity extends BaseActivity {
	private int isSync;
	private Context context;
	private boolean flag, tvflag, authflag;
	private int mappedId, sectionId;
	private String syncTimed, passwordText;
	private TextView userName,password,timeSync;
	private SharedPreferences sharedPref, internetPref;
	private SQLiteDatabase sqliteDatabase;
	private TextView noWifi;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

		context = AppGlobal.getContext();

		registerReceiver(broadcastReceiver, new IntentFilter("WIFI_STATUS"));
        registerReceiver(internetReceiver, new IntentFilter("I_FAILED_ME"));

		noWifi = (TextView)findViewById(R.id.no_wifi);
		updateWifiStatus();

		Intent intent = getIntent();
            if(intent.getIntExtra("create", 0) == 1){
                new CallFTP().syncFTP();
            }
            if(intent.getIntExtra("start_sync", 0) == 1){
                Intent service = new Intent(this, in.teacher.adapter.SyncService.class);
                startService(service);
            }

		sharedPref = context.getSharedPreferences("db_access", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putInt("boot_sync", 0);
		editor.apply();

        internetPref = context.getSharedPreferences("internet_access", Context.MODE_PRIVATE);
        int internetBlock = internetPref.getInt("i_failed_status", 0);
        int ignoreStatus = internetPref.getInt("ignore_status", 1);
        if(internetBlock == 1 && ignoreStatus == 0){
            Intent i = new Intent(this, in.teacher.activity.InternetBlock.class);
            startActivity(i);
        }

		int tabletLock = sharedPref.getInt("tablet_lock", 0);
		if(tabletLock==0){
			login();
        }else if(tabletLock==1){
			Intent i = new Intent(this, in.teacher.activity.LockActivity.class);
			startActivity(i);
		}else if(tabletLock==2){
			Intent i = new Intent(this, in.teacher.activity.ServerBlock.class);
			startActivity(i);
		}

	}

	private void login(){
		isSync = sharedPref.getInt("is_sync",0);
		if(isSync==0){
			sqliteDatabase = AppGlobal.getSqliteDatabase();
			ImageView admin = (ImageView)findViewById(R.id.admin);
			admin.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(LoginActivity.this, in.teacher.activity.MasterAuthentication.class);
					startActivity(i);
					AnimationUtils.activityEnter(LoginActivity.this);
				}
			});

			String android_id = Secure.getString(getBaseContext().getContentResolver(),Secure.ANDROID_ID);
			Log.d("id", android_id);
			TempDao.updateDeviceId(android_id, sqliteDatabase);

			timeSync = (TextView) findViewById(R.id.syncTime); 
			userName = (TextView) findViewById(R.id.userName);
			password = (TextView) findViewById(R.id.password);
			Button clear = (Button) findViewById(R.id.numclear);

			Temp t = TempDao.selectTemp(sqliteDatabase);
			syncTimed = t.getSyncTime();
			timeSync.setText(syncTimed);

			userName.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if(password.getText().toString().equalsIgnoreCase("|")){
						password.setText("");
						password.setHint("Password");
					}
					userName.setText("|");
					tvflag = false;
				}
			});

			password.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if(userName.getText().toString().equalsIgnoreCase("|")){
						userName.setText("");
						userName.setHint("Username");
					}
					password.setText("|");
					tvflag = true;
				}
			});

			int[] buttonIds = {R.id.num1,R.id.num2,R.id.num3,R.id.num4,R.id.num5,R.id.num6,R.id.num7,R.id.num8,R.id.num9,R.id.num0};
			for(int i=0; i<10; i++){
				Button b = (Button)findViewById(buttonIds[i]);
				b.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {

						if(userName.getText().toString().equals("Username")){
							userName.setText("");
						}else if(password.getText().toString().equals("Password") && tvflag){
							password.setText("");
						}
						if(userName.getText().toString().equalsIgnoreCase("|")){
							userName.setText("");
							password.setHint("Password");
						}else if(password.getText().toString().equalsIgnoreCase("|")){
							passwordText = "";
							password.setText("");
						}

						if(v.getId()==R.id.num1){
							updateFields("1");
						}
						if(v.getId()==R.id.num2){
							updateFields("2");
						}
						if(v.getId()==R.id.num3){
							updateFields("3");
						}
						if(v.getId()==R.id.num4){
							updateFields("4");
						}
						if(v.getId()==R.id.num5){
							updateFields("5");
						}
						if(v.getId()==R.id.num6){
							updateFields("6");
						}
						if(v.getId()==R.id.num7){
							updateFields("7");
						}
						if(v.getId()==R.id.num8){
							updateFields("8");
						}
						if(v.getId()==R.id.num9){
							updateFields("9");
						}					
						if(v.getId()==R.id.num0){
							updateFields("0");
						}
					}

				});
			}
			clear.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(tvflag){
						password.setText("|");
						if(userName.getText().toString().equalsIgnoreCase("|")){
							userName.setText("");
							userName.setHint("Username");
						}
					}else{
						userName.setText("|");
						if(password.getText().toString().equalsIgnoreCase("|")){
							password.setText("");
							password.setHint("Password");
						}
					}
				}
			});
		}
	}

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

	private void updateWifiStatus(){
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (mWifi!=null && mWifi.isConnected()) {
			noWifi.setVisibility(View.GONE);
		}else{
			noWifi.setVisibility(View.VISIBLE);
			noWifi.setText("No Internet access / check WiFi");
		}
	}

	private void updateFields(String value){
		if(tvflag){
			password.setText(new StringBuffer(password.getText()).append("*"));			
			String sb = passwordText+value;
			passwordText = sb;
			if(passwordText.length()==4)
				authenticate();
		}else{
			String s = userName.getText().toString();
			StringBuffer sb = new StringBuffer(s);
			sb.append(value);
			userName.setText(sb);
			String s2 = userName.getText().toString();
			if(s2.length()==4)
				preauthenticate();
		}
	}

	private void preauthenticate() {
		password.setText("|");
		tvflag = true;
	}

	private void authenticate() {
		String s = userName.getText().toString();
		if(s.isEmpty()){
			authflag = false;
		}else{
			List<Teacher> authList = TeacherDao.selectTeacher(sqliteDatabase);
			String enteredId = userName.getText().toString();
			for(Teacher t: authList){
				if(enteredId.equals(t.getTabUser()) && passwordText.equals(t.getTabPass())){
					mappedId = t.getTeacherId();
					authflag = true;
					authSuccess();
				}
			}				
		}
		if(!authflag){
			Alert ad = new Alert(LoginActivity.this);
			ad.showAlert("User is not Authenticated.");
		}
		userName.setText("Username");
		password.setText("Password");
		tvflag = false;
	}

	private void authSuccess() {
		flag = false;
		List<Section> list = SectionDao.selectSection(sqliteDatabase);
		for(Section l: list){
			if(mappedId == l.getClassTeacherId()){
				flag = true;
				sectionId = l.getSectionId();
				Temp t = new Temp();
				t.setClassId(l.getClassId());
				t.setSectionId(l.getSectionId());
				t.setSectionName(l.getSectionName());
				t.setTeacherId(l.getClassTeacherId());
				TempDao.updateTemp(t, sqliteDatabase);
				break;
			}
		}
		if(!flag){
			List<Teacher> tList = TeacherDao.selectTeacher(sqliteDatabase);
			for(Teacher t: tList){
				if(mappedId == t.getTeacherId()){
					flag = true;
					Temp t2 = new Temp();
					t2.setSectionId(0);
					t2.setSectionName("0");
					t2.setTeacherId(t.getTeacherId());
					TempDao.updateTemp(t2, sqliteDatabase);
					break;
				}
			}
		}

		Intent intentt = new Intent(this, in.teacher.activity.Dashboard.class);
		intentt.putExtra("sectionid", String.valueOf(sectionId));
		startActivity(intentt);
		AnimationUtils.activityEnterVertical(LoginActivity.this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed(){
		/*File sd = Environment.getExternalStorageDirectory();
		File data = Environment.getDataDirectory();
		FileChannel source=null;
		FileChannel destination=null;
		String currentDBPath = "/data/"+ "in.teacher.activity" +"/databases/teacher.db";
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
		} catch(IOException e) {
			e.printStackTrace();
		}*/
	}

	@Override
	protected void onPause(){
		super.onPause();

		SharedPreferences sharedPref = context.getSharedPreferences("db_access", Context.MODE_PRIVATE);
		int is_first_sync = sharedPref.getInt("first_sync",0);
		int sleepSync = sharedPref.getInt("sleep_sync",0);
		int tabletLock = sharedPref.getInt("tablet_lock", 0);

		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		boolean isScreen = pm.isScreenOn();

		if (NetworkUtils.isNetworkConnected(context) && sleepSync==1 && !isScreen && is_first_sync==0 && tabletLock==0){
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putInt("is_sync", 1);
			editor.apply();
			Intent intent = new Intent(this, in.teacher.activity.ProcessFiles.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();
	}

	@Override
	protected void onResume(){
		updateWifiStatus();
		registerReceiver(broadcastReceiver, new IntentFilter("INTERNET_STATUS"));
        registerReceiver(internetReceiver, new IntentFilter("INTERNET_STATUS"));
		super.onResume();
	}

	@Override
	protected void onStart(){
		super.onStart();
		registerReceiver(broadcastReceiver, new IntentFilter("INTERNET_STATUS"));
        registerReceiver(internetReceiver, new IntentFilter("INTERNET_STATUS"));
	}

	@Override
	protected void onRestart(){
		super.onRestart();
		registerReceiver(broadcastReceiver, new IntentFilter("INTERNET_STATUS"));
        registerReceiver(internetReceiver, new IntentFilter("INTERNET_STATUS"));
	}

	@Override
	protected void onStop(){
		unregisterReceiver(broadcastReceiver);
        unregisterReceiver(internetReceiver);
		super.onStop();
	}

}
