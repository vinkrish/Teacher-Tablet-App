package in.teacher.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import in.teacher.activity.R.animator;
import in.teacher.adapter.NavDrawerListAdapter;
import in.teacher.dao.SlipTesttDao;
import in.teacher.dao.StudentAttendanceDao;
import in.teacher.dao.StudentsDao;
import in.teacher.dao.TempDao;
import in.teacher.fragment.AbsentList;
import in.teacher.fragment.CoScholastic;
import in.teacher.fragment.Dashbord;
import in.teacher.fragment.HasPartition;
import in.teacher.fragment.InsertHomework;
import in.teacher.fragment.MarkAttendance;
import in.teacher.fragment.SearchStudST;
import in.teacher.fragment.SelectCCEStudentProfile;
import in.teacher.fragment.SlipTest;
import in.teacher.fragment.StructuredExam;
import in.teacher.fragment.ViewQueue;
import in.teacher.fragment.ViewScore;
import in.teacher.model.NavDrawerItem;
import in.teacher.sqlite.Students;
import in.teacher.sqlite.Temp;
import in.teacher.util.AnimationUtils;
import in.teacher.util.AppGlobal;
import in.teacher.util.ExceptionHandler;
import in.teacher.util.NetworkUtils;
import in.teacher.util.ReplaceFragment;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class Dashboard extends BaseActivity {
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private NavDrawerListAdapter navDrawerListAdapter;
	private String[] navMenuTitles;
	private TypedArray navMenuIcons;
	private ArrayList<NavDrawerItem> navDrawerItems;
	private int sectionId;
	private Context context;
	private SQLiteDatabase sqliteDatabase;
	private List<String> studNameList = new ArrayList<>();
	private List<Integer> studIdList = new ArrayList<>();
	private int teacherId;
	private SharedPreferences sharedPref, internetPref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dashboard);

		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
		setContentView(R.layout.activity_dashboard);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		context = AppGlobal.getContext();
		sqliteDatabase = AppGlobal.getSqliteDatabase();

		sharedPref = context.getSharedPreferences("has_partition", Context.MODE_PRIVATE);
		internetPref = context.getSharedPreferences("internet_access", Context.MODE_PRIVATE);

		getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getActionBar().setCustomView(R.layout.action_bar);

		navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
		navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		navDrawerItems = new ArrayList<>();
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));

		navMenuIcons.recycle();
		navDrawerListAdapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
		mDrawerList.setAdapter(navDrawerListAdapter);

		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(
				this,
				mDrawerLayout,
				R.drawable.ic_drawer,
				R.string.drawer_open,
				R.string.drawer_close){

			public void onDrawerClosed(View view){
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView){
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if(savedInstanceState == null){
			selectItem(0);
		}

		registerReceiver(broadcastReceiver, new IntentFilter("WIFI_STATUS"));
	}

	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			invalidateOptionsMenu();
		}
	};

	@Override
	protected void onDestroy(){
		super.onDestroy();
	}

	@Override
	protected void onPause(){
		super.onPause();
	}

	@Override
	protected void onResume(){
		registerReceiver(broadcastReceiver, new IntentFilter("INTERNET_STATUS"));
		super.onResume();
	}

	@Override
	protected void onStart(){
		super.onStart();
		registerReceiver(broadcastReceiver, new IntentFilter("INTERNET_STATUS"));
	}

	@Override
	protected void onRestart(){
		super.onRestart();
		registerReceiver(broadcastReceiver, new IntentFilter("INTERNET_STATUS"));
	}

	@Override
	protected void onStop(){
		unregisterReceiver(broadcastReceiver);
		super.onStop();
	}

	@Override
	public void onBackPressed() {
		if (getFragmentManager().getBackStackEntryCount() != 1)
			getFragmentManager().popBackStack();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(mDrawerToggle.onOptionsItemSelected(item)){
			return true;
		}
		switch (item.getItemId()) {
		case R.id.searchId:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			View view = this.getLayoutInflater().inflate(R.layout.dialog_search, null);

			studIdList.clear();
			studNameList.clear();

			Temp t = TempDao.selectTemp(sqliteDatabase);
			teacherId = t.getTeacherId();

			Cursor c = sqliteDatabase.rawQuery("select A.StudentId, A.Name, B.ClassName,C.SectionName from students A, class B, section C, subjectteacher D where (D.TeacherId="+teacherId+
					" or C.ClassTeacherId="+teacherId+") and B.ClassId=A.ClassId and C.SectionId=A.SectionId and A.SectionId=D.SectionId group by A.StudentId", null);
			c.moveToFirst();
			while(!c.isAfterLast()){
				studIdList.add(c.getInt(c.getColumnIndex("StudentId")));
				String s = c.getString(c.getColumnIndex("Name"))+" ("+c.getString(c.getColumnIndex("ClassName"))+" - "+c.getString(c.getColumnIndex("SectionName"))+")";
				studNameList.add(s);
				c.moveToNext();
			}
			c.close();

			ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this,android.R.layout.simple_dropdown_item_1line, studNameList);
			final AutoCompleteTextView textView2 = (AutoCompleteTextView) view.findViewById(R.id.autoCompleteTextView2);
			textView2.setAdapter(adapter2);

			builder.setView(view);

			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					int idx = studNameList.indexOf(textView2.getText().toString());
					if(idx!=-1){
						TempDao.updateStudentId(studIdList.get(idx), sqliteDatabase);
						ReplaceFragment.replace(new SearchStudST(), getFragmentManager());
					}
				}
			});
			builder.setNegativeButton("Cancel", null);
			builder.show();
			return true;

		case R.id.action_logout:
			Intent intent = new Intent(Dashboard.this, in.teacher.activity.LoginActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			AnimationUtils.activityExitVertical(Dashboard.this);
			return true;

		case R.id.action_queue:
			ReplaceFragment.replace(new ViewQueue(), getFragmentManager());
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private	class DrawerItemClickListener implements ListView.OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectItem(position);	
		}
	}

	private void selectItem(int position) {
		if(position == 0){
			ReplaceFragment.replace(new Dashbord(), getFragmentManager());
		}else if(position == 1){
			boolean flag = false;
			Temp t = TempDao.selectTemp(sqliteDatabase);
			sectionId = t.getSectionId();
			int marked = StudentAttendanceDao.isStudentAttendanceMarked(sectionId, getDate(), sqliteDatabase);
			if(marked==1){
				flag = true;
			}
			Bundle b = new Bundle();
			b.putInt("today", 1);
			b.putInt("yesterday", 0);
			b.putInt("otherday", 0);
			if(flag){
				Fragment fragment = new AbsentList();
				fragment.setArguments(b);
				getFragmentManager()
				.beginTransaction()
				.setCustomAnimations(animator.fade_in,animator.fade_out)
				.replace(R.id.content_frame, fragment).addToBackStack(null).commit();
			}else{
				Fragment fragment = new MarkAttendance();
				fragment.setArguments(b);
				getFragmentManager()
				.beginTransaction()
				.setCustomAnimations(animator.fade_in,animator.fade_out)
				.replace(R.id.content_frame, fragment).addToBackStack(null).commit();
			}
		}else if(position == 2){
			ReplaceFragment.replace(new InsertHomework(), getFragmentManager());
		}else if(position == 3){
			ReplaceFragment.replace(new CoScholastic(), getFragmentManager());
		}else if(position == 4){
			ReplaceFragment.replace(new SelectCCEStudentProfile(), getFragmentManager());
		}
		mDrawerList.setItemChecked(position, true);
		setTitle(navMenuTitles[position]);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	private String getDate() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		Date date = new Date();
		return dateFormat.format(date);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.dashboard, menu);
		if (NetworkUtils.isNetworkConnected(context)) {
			menu.getItem(0).setVisible(false);
		}else{
			menu.getItem(0).setVisible(true);
		}

		return true;
	}

	public void callSlipTest(View view){
		Temp temp = TempDao.selectTemp(sqliteDatabase);
		int sectId = temp.getCurrentSection();
		int subjId = temp.getCurrentSubject();
		List<Students>  studentsArray = StudentsDao.selectStudents2(""+sectId, subjId, sqliteDatabase);
		if(studentsArray.size()>0){
			SlipTesttDao.deleteSlipTest(sqliteDatabase);
			ReplaceFragment.replace(new SlipTest(), getFragmentManager());
		}else{
			Toast.makeText(context, "No students taken this subject", Toast.LENGTH_SHORT).show();
		}
	}

	public void callViewScore(View view){
		SlipTesttDao.deleteSlipTest(sqliteDatabase);
		ReplaceFragment.replace(new ViewScore(), getFragmentManager());
	}

	public void callStructuredExam(View view){
		int partition = sharedPref.getInt("partition",0);
		if(partition==1){
			ReplaceFragment.replace(new HasPartition(), getFragmentManager());
		}else{
			ReplaceFragment.replace(new StructuredExam(), getFragmentManager());
		}
	}

	public void toDashbord(View v){
		ReplaceFragment.replace(new Dashbord(), getFragmentManager());
	}

}