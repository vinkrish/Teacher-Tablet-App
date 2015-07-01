package in.teacher.fragment;

import in.teacher.activity.R;
import in.teacher.activity.R.animator;
import in.teacher.adapter.Capitalize;
import in.teacher.dao.StAvgDao;
import in.teacher.dao.StudentAttendanceDao;
import in.teacher.dao.TeacherDao;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.CircleObject;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.ReplaceFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class Dashbord extends Fragment implements AnimationListener{
	private Context context;
	private int teacherId,sectionId;
	private ArrayList<Integer> sectionIdList = new ArrayList<Integer>();
	private ArrayList<Integer> classIdList = new ArrayList<Integer>();
	private ArrayList<String> classNameList = new ArrayList<String>();
	private ArrayList<String> sectionNameList = new ArrayList<String>();
	private String teacherName;
	private ArrayList<Integer> subjectIdList = new ArrayList<Integer>();
	private ArrayList<String> subjectNameList = new ArrayList<String>();
	private ArrayList<Integer> hasPartitionList = new ArrayList<Integer>();
	private boolean[] frameFlag;
	private Animation animFadeIn,animFadeOut;
	private ImageView[] ivList;
	private SQLiteDatabase sqliteDatabase;
	private ArrayList<CircleObject> circleArrayGrid = new ArrayList<CircleObject>();
	private CircleAdapter cA;
	private View fl2,fl22,fl222,fl2222;
	private GridView gridView;
	private Button name;
	private SharedPreferences sharedPref;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState){
		View view = inflater.inflate(R.layout.dashbord, container, false);
		context = AppGlobal.getContext();
		sqliteDatabase = AppGlobal.getSqliteDatabase();

		initializeList();

		gridView = (GridView) view.findViewById(R.id.gridView);
		sharedPref = context.getSharedPreferences("has_partition", Context.MODE_PRIVATE);

		animFadeIn = AnimationUtils.loadAnimation(context.getApplicationContext(),R.anim.fade_in);
		animFadeIn.setAnimationListener(this);
		animFadeOut = AnimationUtils.loadAnimation(context.getApplicationContext(),R.anim.fade_out);
		animFadeOut.setAnimationListener(this);

		Temp t = TempDao.selectTemp(sqliteDatabase);
		teacherId = t.getTeacherId();
		sectionId = t.getSectionId();

		for(int i=0; i<20; i++){
			frameFlag[i] = false;
		}

		Button attendance = (Button)view.findViewById(R.id.attendanceButton);
		attendance.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean flag = false;
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
					ReplaceFragment.replace(new MarkAttendance(), getFragmentManager());
				}
			}
		});

		cA = new CircleAdapter(context, R.layout.circle_grid, circleArrayGrid);
		gridView.setAdapter(cA);

		fl2 = view.findViewById(R.id.fl2);
		fl22 = view.findViewById(R.id.fl22);	
		fl222 = view.findViewById(R.id.fl222);
		fl2222 = view.findViewById(R.id.fl2222);
		name = (Button)view.findViewById(R.id.classSection);

		final Integer[] ivIntList = {R.id.fl3iv1,R.id.fl3iv2,R.id.fl3iv3,R.id.fl3iv4,R.id.fl3iv5,R.id.fl3iv6,R.id.fl3iv7,R.id.fl3iv8,R.id.fl3iv9,
				R.id.fl3iv10,R.id.fl3iv11,R.id.fl3iv12,R.id.fl3iv13,R.id.fl3iv14,R.id.fl3iv15,R.id.fl3iv16,R.id.fl3iv17,R.id.fl3iv18,R.id.fl3iv19,R.id.fl3iv20};
		for(int i=0; i<ivIntList.length; i++){
			ivList[i] = (ImageView) view.findViewById(ivIntList[i]);
		}

		new CalledBackLoad().execute();

		return view;
	}

	public void callUpdateTemp(int pos){
		Temp t = new Temp();
		t.setCurrentSection(sectionIdList.get(pos));
		t.setCurrentSubject(subjectIdList.get(pos));
		t.setCurrentClass(classIdList.get(pos));
		t.setSubjectId(subjectIdList.get(pos));
		TempDao.updateSecSubClas(t, sqliteDatabase);
		SharedPreferences.Editor editor = sharedPref.edit();
		if(hasPartitionList.get(pos)==1){
			editor.putInt("partition", 1);
			editor.apply();
		}else{
			editor.putInt("partition", 0);
			editor.apply();
		}
	}

	private String getDate() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		Date date = new Date();
		return dateFormat.format(date);
	}

	public void clearFrameFlag(){
		for(int i=0; i<20; i++){
			frameFlag[i] = false;
		}
	}

	public void setFrameFlag(){
		for(int i=0; i<20; i++){
			frameFlag[i] = true;
		}
	}

	public class CircleAdapter extends ArrayAdapter<CircleObject>{
		private Context context2;
		private int layoutResourceId;
		private ArrayList<CircleObject>	data = new ArrayList<CircleObject>();
		private LayoutInflater inflater;

		public CircleAdapter(Context context, int layoutResourceId, ArrayList<CircleObject> gridArray) {
			super(context, layoutResourceId, gridArray);
			this.context2 = context;
			this.layoutResourceId = layoutResourceId;
			this.data = gridArray;
			inflater = (LayoutInflater)context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View row = convertView;
			RecordHolder holder = null;

			if(row == null){
				row = inflater.inflate(layoutResourceId, parent, false);
				holder = new RecordHolder();
				holder.clasTxt = (TextView) row.findViewById(R.id.clas);
				row.setTag(holder);
			}else
				holder = (RecordHolder) row.getTag();

			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			FrameLayout fl = (FrameLayout)row.findViewById(R.id.fl);

			CircleObject gridItem = data.get(position);
			holder.clasTxt.setText(gridItem.getClas());
			SampleView sV = new SampleView(context2, gridItem.getProgressInt());
			fl.addView(sV,layoutParams);

			sV.setOnClickListener(new OnClickListener(){	
				@Override
				public void onClick(View v) {
					viewClickListener(position);
				}
			});
			return row;
		}

		public class RecordHolder {
			TextView clasTxt;
		}

		private class SampleView extends View {
			Paint p,defaultPaint;
			RectF rectF;
			int localInt;
			public SampleView(Context context, int i) {
				super(context);
				setFocusable(true);
				localInt = i;
				init();
			}

			public void init(){
				p = new Paint();
				defaultPaint = new Paint();
				defaultPaint.setAntiAlias(true);
				defaultPaint.setStyle(Paint.Style.STROKE); 
				defaultPaint.setStrokeWidth(4);
				Resources res = getResources();
				int defalt = res.getColor(R.color.defalt);
				defaultPaint.setColor(defalt);

				rectF = new RectF(20, 20, 100, 100);
			}

			@Override
			protected void onDraw(Canvas canvas) {
				p.setAntiAlias(true);
				p.setStyle(Paint.Style.STROKE); 
				p.setStrokeWidth(4);

				if(localInt>=270){
					p.setColor(getResources().getColor(R.color.green));
				}else if(localInt>=180){
					p.setColor(getResources().getColor(R.color.orange));
				}else if(localInt>0){
					p.setColor(getResources().getColor(R.color.red));
				}
				canvas.drawArc (rectF, 0, 360, false, defaultPaint);
				canvas.drawArc (rectF, 270, Float.parseFloat(localInt+""), false, p);
			}

		}

	}

	public void viewClickListener(int position){
		if(position == 0){				
			if(frameFlag[0]){
				clearAnim();
				fl2.setVisibility(View.INVISIBLE);
				fl22.setVisibility(View.INVISIBLE);
				fl222.setVisibility(View.INVISIBLE);
				fl2222.setVisibility(View.INVISIBLE);
				clearFrameFlag();
				clearImageView();
			}else{
				clearAnim();
				fl2.setVisibility(View.VISIBLE);
				fl2.startAnimation(animFadeIn);
				setFrameFlag();
				clearImageView();
				ivList[0].setVisibility(View.VISIBLE);
				ivList[0].startAnimation(animFadeIn);
			}
			callUpdateTemp(0);
		}
		if(position == 1){				
			if(frameFlag[1]){
				clearAnim();
				fl2.setVisibility(View.INVISIBLE);
				fl22.setVisibility(View.INVISIBLE);
				fl222.setVisibility(View.INVISIBLE);
				fl2222.setVisibility(View.INVISIBLE);
				clearFrameFlag();
				clearImageView();
			}else{
				clearAnim();
				fl2.setVisibility(View.VISIBLE);
				fl2.startAnimation(animFadeIn);
				setFrameFlag();
				clearImageView();
				ivList[1].setVisibility(View.VISIBLE);
				ivList[1].startAnimation(animFadeIn);
			}
			callUpdateTemp(1);
		}
		if(position == 2){
			if(frameFlag[2]){
				clearAnim();
				fl2.setVisibility(View.INVISIBLE);
				fl22.setVisibility(View.INVISIBLE);
				fl222.setVisibility(View.INVISIBLE);
				fl2222.setVisibility(View.INVISIBLE);
				clearFrameFlag();
				clearImageView();
			}else{
				clearAnim();
				fl2.setVisibility(View.VISIBLE);
				fl2.startAnimation(animFadeIn);
				setFrameFlag();
				clearImageView();
				ivList[2].setVisibility(View.VISIBLE);
				ivList[2].startAnimation(animFadeIn);
			}
			callUpdateTemp(2);
		}
		if(position == 3){
			if(frameFlag[3]){
				clearAnim();
				fl2.setVisibility(View.INVISIBLE);
				fl22.setVisibility(View.INVISIBLE);
				fl222.setVisibility(View.INVISIBLE);
				fl2222.setVisibility(View.INVISIBLE);
				clearFrameFlag();
				clearImageView();
			}else{
				clearAnim();
				fl2.setVisibility(View.VISIBLE);
				fl2.startAnimation(animFadeIn);
				setFrameFlag();
				clearImageView();
				ivList[3].setVisibility(View.VISIBLE);
				ivList[3].startAnimation(animFadeIn);
			}
			callUpdateTemp(3);
		}
		if(position == 4){						
			if(frameFlag[4]){
				clearAnim();
				fl2.setVisibility(View.INVISIBLE);
				fl22.setVisibility(View.INVISIBLE);
				fl222.setVisibility(View.INVISIBLE);
				fl2222.setVisibility(View.INVISIBLE);
				clearFrameFlag();
				clearImageView();
			}else{
				clearAnim();
				fl2.setVisibility(View.VISIBLE);
				fl2.startAnimation(animFadeIn);
				setFrameFlag();
				clearImageView();
				ivList[4].setVisibility(View.VISIBLE);
				ivList[4].startAnimation(animFadeIn);
			}
			callUpdateTemp(4);
		}
		if(position == 5){						
			if(frameFlag[5]){
				clearAnim();
				fl2.setVisibility(View.INVISIBLE);
				fl22.setVisibility(View.INVISIBLE);
				fl222.setVisibility(View.INVISIBLE);
				fl2222.setVisibility(View.INVISIBLE);
				clearFrameFlag();
				clearImageView();
			}else{
				clearAnim();
				fl22.setVisibility(View.VISIBLE);
				fl22.startAnimation(animFadeIn);
				setFrameFlag();
				clearImageView();
				ivList[5].setVisibility(View.VISIBLE);
				ivList[5].startAnimation(animFadeIn);
			}
			callUpdateTemp(5);
		}
		if(position == 6){						
			if(frameFlag[6]){
				clearAnim();
				fl2.setVisibility(View.INVISIBLE);
				fl22.setVisibility(View.INVISIBLE);
				fl222.setVisibility(View.INVISIBLE);
				fl2222.setVisibility(View.INVISIBLE);
				clearFrameFlag();
				clearImageView();
			}else{
				clearAnim();
				fl22.setVisibility(View.VISIBLE);
				fl22.startAnimation(animFadeIn);
				setFrameFlag();
				clearImageView();
				ivList[6].setVisibility(View.VISIBLE);
				ivList[6].startAnimation(animFadeIn);
			}
			callUpdateTemp(6);
		}
		if(position == 7){						
			if(frameFlag[7]){
				clearAnim();
				fl2.setVisibility(View.INVISIBLE);
				fl22.setVisibility(View.INVISIBLE);
				fl222.setVisibility(View.INVISIBLE);
				fl2222.setVisibility(View.INVISIBLE);
				clearFrameFlag();
				clearImageView();
			}else{
				clearAnim();
				fl22.setVisibility(View.VISIBLE);
				fl22.startAnimation(animFadeIn);
				setFrameFlag();
				clearImageView();
				ivList[7].setVisibility(View.VISIBLE);
				ivList[7].startAnimation(animFadeIn);
			}
			callUpdateTemp(7);
		}
		if(position == 8){						
			if(frameFlag[8]){
				clearAnim();
				fl2.setVisibility(View.INVISIBLE);
				fl22.setVisibility(View.INVISIBLE);
				fl222.setVisibility(View.INVISIBLE);
				fl2222.setVisibility(View.INVISIBLE);
				clearFrameFlag();
				clearImageView();
			}else{
				clearAnim();
				fl22.setVisibility(View.VISIBLE);
				fl22.startAnimation(animFadeIn);
				setFrameFlag();
				clearImageView();
				ivList[8].setVisibility(View.VISIBLE);
				ivList[8].startAnimation(animFadeIn);
			}
			callUpdateTemp(8);
		}
		if(position == 9){						
			if(frameFlag[9]){
				clearAnim();
				fl2.setVisibility(View.INVISIBLE);
				fl22.setVisibility(View.INVISIBLE);
				fl222.setVisibility(View.INVISIBLE);
				fl2222.setVisibility(View.INVISIBLE);
				clearFrameFlag();
				clearImageView();
			}else{
				clearAnim();
				fl22.setVisibility(View.VISIBLE);
				fl22.startAnimation(animFadeIn);
				setFrameFlag();
				clearImageView();
				ivList[9].setVisibility(View.VISIBLE);
				ivList[9].startAnimation(animFadeIn);
			}
			callUpdateTemp(9);
		}
		if(position == 10){						
			if(frameFlag[10]){
				clearAnim();
				fl2.setVisibility(View.INVISIBLE);
				fl22.setVisibility(View.INVISIBLE);
				fl222.setVisibility(View.INVISIBLE);
				fl2222.setVisibility(View.INVISIBLE);
				clearFrameFlag();
				clearImageView();
			}else{
				clearAnim();
				fl222.setVisibility(View.VISIBLE);
				fl222.startAnimation(animFadeIn);
				setFrameFlag();
				clearImageView();
				ivList[10].setVisibility(View.VISIBLE);
				ivList[10].startAnimation(animFadeIn);
			}
			callUpdateTemp(10);
		}
		if(position == 11){						
			if(frameFlag[11]){
				clearAnim();
				fl2.setVisibility(View.INVISIBLE);
				fl22.setVisibility(View.INVISIBLE);
				fl222.setVisibility(View.INVISIBLE);
				fl2222.setVisibility(View.INVISIBLE);
				clearFrameFlag();
				clearImageView();
			}else{
				clearAnim();
				fl222.setVisibility(View.VISIBLE);
				fl222.startAnimation(animFadeIn);
				setFrameFlag();
				clearImageView();
				ivList[11].setVisibility(View.VISIBLE);
				ivList[11].startAnimation(animFadeIn);
			}
			callUpdateTemp(11);
		}
		if(position == 12){						
			if(frameFlag[12]){
				clearAnim();
				fl2.setVisibility(View.INVISIBLE);
				fl22.setVisibility(View.INVISIBLE);
				fl222.setVisibility(View.INVISIBLE);
				fl2222.setVisibility(View.INVISIBLE);
				clearFrameFlag();
				clearImageView();
			}else{
				clearAnim();
				fl222.setVisibility(View.VISIBLE);
				fl222.startAnimation(animFadeIn);
				setFrameFlag();
				clearImageView();
				ivList[12].setVisibility(View.VISIBLE);
				ivList[12].startAnimation(animFadeIn);
			}
			callUpdateTemp(12);
		}
		if(position == 13){						
			if(frameFlag[13]){
				clearAnim();
				fl2.setVisibility(View.INVISIBLE);
				fl22.setVisibility(View.INVISIBLE);
				fl222.setVisibility(View.INVISIBLE);
				fl2222.setVisibility(View.INVISIBLE);
				clearFrameFlag();
				clearImageView();
			}else{
				clearAnim();
				fl222.setVisibility(View.VISIBLE);
				fl222.startAnimation(animFadeIn);
				setFrameFlag();
				clearImageView();
				ivList[13].setVisibility(View.VISIBLE);
				ivList[13].startAnimation(animFadeIn);
			}
			callUpdateTemp(13);
		}
		if(position == 14){
			if(frameFlag[14]){
				clearAnim();
				fl2.setVisibility(View.INVISIBLE);
				fl22.setVisibility(View.INVISIBLE);
				fl222.setVisibility(View.INVISIBLE);
				fl2222.setVisibility(View.INVISIBLE);
				clearFrameFlag();
				clearImageView();
			}else{
				clearAnim();
				fl222.setVisibility(View.VISIBLE);
				fl222.startAnimation(animFadeIn);
				setFrameFlag();
				clearImageView();
				ivList[14].setVisibility(View.VISIBLE);
				ivList[14].startAnimation(animFadeIn);
			}
			callUpdateTemp(14);
		}
		if(position == 15){
			if(frameFlag[15]){
				clearAnim();
				fl2.setVisibility(View.INVISIBLE);
				fl22.setVisibility(View.INVISIBLE);
				fl222.setVisibility(View.INVISIBLE);
				fl2222.setVisibility(View.INVISIBLE);
				clearFrameFlag();
				clearImageView();
			}else{
				clearAnim();
				fl2222.setVisibility(View.VISIBLE);
				fl2222.startAnimation(animFadeIn);
				setFrameFlag();
				clearImageView();
				ivList[15].setVisibility(View.VISIBLE);
				ivList[15].startAnimation(animFadeIn);
			}
			callUpdateTemp(15);
		}
		if(position == 16){
			if(frameFlag[16]){
				clearAnim();
				fl2.setVisibility(View.INVISIBLE);
				fl22.setVisibility(View.INVISIBLE);
				fl222.setVisibility(View.INVISIBLE);
				fl2222.setVisibility(View.INVISIBLE);
				clearFrameFlag();
				clearImageView();
			}else{
				clearAnim();
				fl2222.setVisibility(View.VISIBLE);
				fl2222.startAnimation(animFadeIn);
				setFrameFlag();
				clearImageView();
				ivList[16].setVisibility(View.VISIBLE);
				ivList[16].startAnimation(animFadeIn);
			}
			callUpdateTemp(16);
		}
		if(position == 17){
			if(frameFlag[17]){
				clearAnim();
				fl2.setVisibility(View.INVISIBLE);
				fl22.setVisibility(View.INVISIBLE);
				fl222.setVisibility(View.INVISIBLE);
				fl2222.setVisibility(View.INVISIBLE);
				clearFrameFlag();
				clearImageView();
			}else{
				clearAnim();
				fl2222.setVisibility(View.VISIBLE);
				fl2222.startAnimation(animFadeIn);
				setFrameFlag();
				clearImageView();
				ivList[17].setVisibility(View.VISIBLE);
				ivList[17].startAnimation(animFadeIn);
			}
			callUpdateTemp(17);
		}
		if(position == 18){
			if(frameFlag[18]){
				clearAnim();
				fl2.setVisibility(View.INVISIBLE);
				fl22.setVisibility(View.INVISIBLE);
				fl222.setVisibility(View.INVISIBLE);
				fl2222.setVisibility(View.INVISIBLE);
				clearFrameFlag();
				clearImageView();
			}else{
				clearAnim();
				fl2222.setVisibility(View.VISIBLE);
				fl2222.startAnimation(animFadeIn);
				setFrameFlag();
				clearImageView();
				ivList[18].setVisibility(View.VISIBLE);
				ivList[18].startAnimation(animFadeIn);
			}
			callUpdateTemp(18);
		}
		if(position == 19){
			if(frameFlag[19]){
				clearAnim();
				fl2.setVisibility(View.INVISIBLE);
				fl22.setVisibility(View.INVISIBLE);
				fl222.setVisibility(View.INVISIBLE);
				fl2222.setVisibility(View.INVISIBLE);
				clearFrameFlag();
				clearImageView();
			}else{
				clearAnim();
				fl2222.setVisibility(View.VISIBLE);
				fl2222.startAnimation(animFadeIn);
				setFrameFlag();
				clearImageView();
				ivList[19].setVisibility(View.VISIBLE);
				ivList[19].startAnimation(animFadeIn);
			}
			callUpdateTemp(19);
		}
	}

	private void clearImageView() {
		for(int i=0; i<ivList.length; i++){
			ivList[i].setVisibility(View.INVISIBLE);
		}
	}

	private void clearAnim(){
		fl2.setAnimation(null);
		fl22.setAnimation(null);
		fl222.setAnimation(null);
		fl2222.setAnimation(null);
		for(int i=0; i<ivList.length; i++){
			ivList[i].setAnimation(null);
		}
	}

	private void initializeList(){
		frameFlag = new boolean[20];
		ivList = new ImageView[20];
		circleArrayGrid.clear();
		sectionIdList.clear();
		classIdList.clear();
		subjectIdList.clear();
		sectionNameList.clear();
		classNameList.clear();
		subjectNameList.clear();
		hasPartitionList.clear();
	}

	class CalledBackLoad extends AsyncTask<String, String, String>{
		protected void onPreExecute(){
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {			
			Cursor c = sqliteDatabase.rawQuery("select A.ClassId, A.SectionId, A.SubjectId, B.ClassName, C.SectionName, D.SubjectName, D.has_partition " +
					"from subjectteacher A, class B, section C, subjects D "+
					"where A.TeacherId="+teacherId+" and A.ClassId=B.ClassId and A.SectionId=C.SectionId and A.SubjectId=D.SubjectId", null);
			c.moveToFirst();
			while(!c.isAfterLast()){
				classIdList.add(c.getInt(c.getColumnIndex("ClassId")));
				sectionIdList.add(c.getInt(c.getColumnIndex("SectionId")));
				subjectIdList.add(c.getInt(c.getColumnIndex("SubjectId")));
				classNameList.add(c.getString(c.getColumnIndex("ClassName")));
				sectionNameList.add(c.getString(c.getColumnIndex("SectionName")));
				subjectNameList.add(c.getString(c.getColumnIndex("SubjectName")));
				hasPartitionList.add(c.getInt(c.getColumnIndex("has_partition")));
				c.moveToNext();
			}
			c.close();

			for(int i=0; i<sectionIdList.size(); i++){
				int avg = StAvgDao.selectStAvg(sectionIdList.get(i), subjectIdList.get(i), sqliteDatabase);
				StringBuilder sb = new StringBuilder();
				sb.append(trim(7,classNameList.get(i))+"-"+(trim(3,sectionNameList.get(i)))+"  "+trim(3,subjectNameList.get(i)));
				String s = sb.toString();
				circleArrayGrid.add(new CircleObject(avg, s));
			}

			return null;
		}
		private String trim(int pos2, String s) {
			if(s.length()>pos2){
				StringBuilder sb = new StringBuilder(s.substring(0, pos2));
				return sb.toString();
			}else{
				return s;
			}
		}
		protected void onPostExecute(String s){
			super.onPostExecute(s);
			teacherName = Capitalize.capitalThis((TeacherDao.selectTeacherName(teacherId, sqliteDatabase)));
			if(teacherName.length()>11){
				StringBuilder sb2 = new StringBuilder(teacherName.substring(0, 9)).append("...");
				name.setText(sb2.toString());
			}else{
				name.setText(teacherName);
			}
			cA.notifyDataSetChanged();
		}
	}

	@Override
	public void onAnimationEnd(Animation animation) {}
	@Override
	public void onAnimationRepeat(Animation animation) {}
	@Override
	public void onAnimationStart(Animation animation) {}

}
