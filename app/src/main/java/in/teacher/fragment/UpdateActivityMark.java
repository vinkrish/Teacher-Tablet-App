package in.teacher.fragment;

import in.teacher.activity.R;
import in.teacher.adapter.Capitalize;
import in.teacher.adapter.MarksAdapter;
import in.teacher.dao.ActivitiDao;
import in.teacher.dao.ActivityMarkDao;
import in.teacher.dao.ClasDao;
import in.teacher.dao.ExamsDao;
import in.teacher.dao.ExmAvgDao;
import in.teacher.dao.SectionDao;
import in.teacher.dao.StudentsDao;
import in.teacher.dao.SubjectExamsDao;
import in.teacher.dao.TeacherDao;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.Activiti;
import in.teacher.sqlite.ActivityMark;
import in.teacher.sqlite.Students;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.ReplaceFragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

public class UpdateActivityMark extends Fragment {
	private Context context;
	private SQLiteDatabase sqliteDatabase;
	private Activity act;
	private int sectionId,teacherId;
	private float maxMark;
	private String teacherName, activityName;
	private List<Students> studentsArray = new ArrayList<>();
	private List<Boolean> studentIndicate = new ArrayList<>();
	private ArrayList<Students> studentsArrayList =  new ArrayList<>();
	private List<Integer> studentsArrayId = new ArrayList<>();
	private List<String> studentScore = new ArrayList<>();
	private ListView lv;
	private MarksAdapter marksAdapter;
	private int index=0,indexBound,top,firstVisible,lastVisible,totalVisible,marksCount;
	private int schoolId,examId,subjectId,subId,classId,activityId,calculation;
	private Bitmap empty, entered;
	private Button name;
	private TextView clasSecSub;
	private StringBuffer sf = new StringBuffer();
	private SharedPreferences sharedPref;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState){

		View view = inflater.inflate(R.layout.exam_mark, container, false);
		act = AppGlobal.getActivity();
		context = AppGlobal.getContext();
		sqliteDatabase = AppGlobal.getSqliteDatabase();
		sharedPref = context.getSharedPreferences("has_partition", Context.MODE_PRIVATE);

		lv = (ListView)view.findViewById(R.id.list);
		marksAdapter = new MarksAdapter(context, studentsArrayList);
		lv.setAdapter(marksAdapter);

		name = (Button)view.findViewById(R.id.classSection);
		clasSecSub = (TextView)view.findViewById(R.id.clasSecSub);
		empty = BitmapFactory.decodeResource(this.getResources(), R.drawable.deindicator);
		entered = BitmapFactory.decodeResource(this.getResources(), R.drawable.indicator);

		Button previous = (Button)view.findViewById(R.id.previous);
		Button next = (Button)view.findViewById(R.id.next);
		Button submit = (Button)view.findViewById(R.id.submit);
		Button clear = (Button)view.findViewById(R.id.clear);

		Temp t = TempDao.selectTemp(sqliteDatabase);
		schoolId = t.getSchoolId();
		classId = t.getCurrentClass();
		sectionId = t.getCurrentSection();
		subjectId = t.getCurrentSubject();
		subId = t.getSubjectId();
		teacherId = t.getTeacherId();
		examId = t.getExamId();
		activityId = t.getActivityId();
		schoolId = t.getSchoolId();

		Activiti a = ActivitiDao.getActiviti(activityId, sqliteDatabase);
		maxMark = a.getMaximumMark();
		activityName = Capitalize.capitalThis(a.getActivityName());
		calculation = a.getCalculation();

		TextView maxMarkView = (TextView)view.findViewById(R.id.maxmark);
		maxMarkView.setText(maxMark+"");
		marksCount = ActivityMarkDao.getActMarksCount(activityId, sqliteDatabase);

		new CalledBackLoad().execute();

		/*for(Students s: studentsArray){
			for(ActivityMark am: amList){
				if(s.getStudentId()==am.getStudentId()){
					if(am.getMark().equals("0")){
						studentScore.add("");
					}else{
						studentScore.add(am.getMark());
					}
					break;
				}
			}
		}*/

		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long id) {
				if(studentScore.get(index)!=null && !studentScore.get(index).equals("") && studentScore.get(index).equals(".")){
					studentScore.set(index, "");
				}
				if(studentScore.get(index)!=null && !studentScore.get(index).equals("") && Double.parseDouble(studentScore.get(index))>maxMark){
					studentScore.set(index, "");
					Toast.makeText(context, "marks entered is greater than max mark", Toast.LENGTH_SHORT).show();
				}

				index = pos;
				View v = lv.getChildAt(0);
				top = (v == null) ? 0 : v.getTop();
				for(int idx=0; idx<studentsArray.size(); idx++){
					studentIndicate.set(idx, false);
				}
				Boolean b = studentIndicate.get(index);
				if(!b){
					studentIndicate.set(index, true);
				}
				repopulateListArray();
			}
		});

		lv.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				firstVisible = lv.getFirstVisiblePosition();
				lastVisible = lv.getLastVisiblePosition();
				totalVisible = lastVisible - firstVisible;
			}
		});

		int[] buttonIds = {R.id.one,R.id.two,R.id.three,R.id.four,R.id.five,R.id.six,R.id.seven,R.id.eight,R.id.nine,R.id.zero,R.id.decimal,R.id.minus};
		for(int i=0; i<12; i++){
			Button b = (Button)view.findViewById(buttonIds[i]);
			b.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(v.getId()==R.id.one)
						updateScoreField("1");
					if(v.getId()==R.id.two)
						updateScoreField("2");
					if(v.getId()==R.id.three)
						updateScoreField("3");
					if(v.getId()==R.id.four)
						updateScoreField("4");
					if(v.getId()==R.id.five)
						updateScoreField("5");
					if(v.getId()==R.id.six)
						updateScoreField("6");
					if(v.getId()==R.id.seven)
						updateScoreField("7");
					if(v.getId()==R.id.eight)
						updateScoreField("8");
					if(v.getId()==R.id.nine)
						updateScoreField("9");
					if(v.getId()==R.id.zero)
						updateScoreField("0");
					if(v.getId()==R.id.decimal)
						updateScoreField(".");
					if(v.getId()==R.id.minus){
						studentScore.set(index, "-1");
						repopulateListArray();
					}
				}
			});
		}
		submit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {

				if(studentScore.get(index)!=null && !studentScore.get(index).equals("") && !studentScore.get(index).equals(".") && Double.parseDouble(studentScore.get(index))>maxMark){
					String s = "";
					studentScore.set(index, s);
					Toast.makeText(context, "Marks Entered is Greater than Max Mark", Toast.LENGTH_SHORT).show();
					repopulateListArray();
				}else{
					Toast.makeText(context, "marks entered has been saved", Toast.LENGTH_LONG).show();
					new CalledSubmit().execute();
				}
			}
		});
		clear.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				studentScore.set(index, "");
				repopulateListArray();
			}
		});
		previous.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(studentScore.get(index)!=null && !studentScore.get(index).equals("") && studentScore.get(index).equals(".")){
					studentScore.set(index, "");
				}
				if(studentScore.get(index)!=null && !studentScore.get(index).equals("") && Double.parseDouble(studentScore.get(index))>maxMark){
					String s = "";
					studentScore.set(index, s);
					Toast.makeText(context, "Marks Entered is Greater than Max Mark", Toast.LENGTH_SHORT).show();
				}else{
					if(index!=0){
						index--;
					}
					for(int idx=0; idx<studentsArray.size(); idx++){
						studentIndicate.set(idx, false);
					}
					Boolean b = studentIndicate.get(index);
					if(!b){
						studentIndicate.set(index, true);
					}
				}			
				repopulateListArray();
			}
		});
		next.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				if(studentScore.get(index)!=null && !studentScore.get(index).equals("") && studentScore.get(index).equals(".")){
					studentScore.set(index, "");
				}
				if(studentScore.get(index)!=null && !studentScore.get(index).equals("") && Double.parseDouble(studentScore.get(index))>maxMark){
					String s = "";
					studentScore.set(index, s);
					Toast.makeText(context, "Marks Entered is Greater than Max Mark", Toast.LENGTH_SHORT).show();
				}else{
					if(index<indexBound-1){
						index++;
					}
					for(int idx=0; idx<studentsArray.size(); idx++){
						studentIndicate.set(idx, false);
					}
					Boolean b = studentIndicate.get(index);
					if(!b){
						studentIndicate.set(index, true);
					}
				}			
				repopulateListArray();
			}
		});

		return view;
	}

	class CalledSubmit extends AsyncTask<Void, Void, Void>{
		ProgressDialog pDialog = new ProgressDialog(act);

		protected void onPreExecute(){
			super.onPreExecute();
			pDialog.setMessage("Submitting marks...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			pushSubmit();
			return null;
		}

		protected void onPostExecute(Void v){
			super.onPostExecute(v);
			pDialog.dismiss();
			ReplaceFragment.clearBackStack(getFragmentManager());
			ReplaceFragment.replace(new ActivityExam(), getFragmentManager());
		}

	}

	private void pushSubmit(){
		int i=0;
		for(String ss: studentScore){
			if(ss==null || ss.equals(".") || ss.equals("")){
				studentScore.set(i, "0");
			}
			i++;
		}
		int j=0;
		List<ActivityMark> mList = new ArrayList<>();
		for(Students st: studentsArray){
			ActivityMark m = new ActivityMark();
			m.setSchoolId(schoolId);
			m.setExamId(examId);
			m.setActivityId(activityId);
			m.setSubjectId(subjectId);
			m.setStudentId(st.getStudentId());
			m.setMark(studentScore.get(j));
			mList.add(m);
			j++;
		}
		if(studentsArray.size()==marksCount){
			ActivityMarkDao.updateActivityMark(mList, sqliteDatabase);
		}else{
			ActivityMarkDao.insertUpdateActMark(mList, sqliteDatabase);
		}

		int entry = ExmAvgDao.checkExmEntry(sectionId, subjectId, examId, sqliteDatabase);
		if(entry==0){
			ExmAvgDao.insertIntoExmAvg(classId, sectionId, subjectId, examId, schoolId, sqliteDatabase);
		}
		ActivitiDao.updateActivityAvg(activityId,schoolId, sqliteDatabase);
		ExmAvgDao.updateActExmAvg(sectionId,subjectId,examId,schoolId, sqliteDatabase);
		ActivitiDao.checkActMarkEmpty(activityId,schoolId,sqliteDatabase);
		ExmAvgDao.checkExmActMarkEmpty(examId, sectionId, subjectId, schoolId, sqliteDatabase);
		weightageCalculation();
	}

	private void weightageCalculation(){
		List<Activiti> actList = ActivitiDao.selectActiviti(examId, subjectId, sectionId, sqliteDatabase);
		List<Integer> actIdList = new ArrayList<>();
		List<Integer> weightageList = new ArrayList<>();
		List<Float> actMaxMarkList = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		for(Activiti Act: actList){
			sb.append(+Act.getActivityId()+",");
			actIdList.add(Act.getActivityId());
			weightageList.add(Act.getWeightage());
			actMaxMarkList.add(Act.getMaximumMark());
		}
		boolean exist = ActivityMarkDao.isAllActMarkExist(actIdList, sqliteDatabase);
		if(exist){
			float exmMaxMark = SubjectExamsDao.getExmMaxMark(classId, examId, subjectId, sqliteDatabase);
			List<Float> weightMarkList = new ArrayList<>();
			if(calculation==0){
				for(int i=0; i<actList.size(); i++){
					weightMarkList.add((float)(weightageList.get(i)/100.0)*exmMaxMark);
				}
				List<Float> markList = new ArrayList<>();
				for(Students st: studentsArray){
					markList.clear();
					for(int j=0; j<actList.size(); j++){
						float mark = 0;
						Cursor c = sqliteDatabase.rawQuery("select Mark from activitymark where StudentId="+st.getStudentId()+" and ActivityId="+actIdList.get(j), null);
						c.moveToFirst();
						while(!c.isAfterLast()){
							mark = c.getFloat(c.getColumnIndex("Mark"));
							c.moveToNext();
						}
						c.close();
						markList.add((float)(mark/actMaxMarkList.get(j))*weightMarkList.get(j));
					}
					float finalMark = 0;
					for(Float flo: markList){
						finalMark += flo;
					}
					String sql = "update marks set Mark='"+finalMark+"' where ExamId="+examId+" and SubjectId="+subjectId+" and StudentId="+st.getStudentId();
					try{
						sqliteDatabase.execSQL(sql);
					}catch(SQLException e){
						e.printStackTrace();
					}
					ContentValues cv = new ContentValues();
					cv.put("Query", sql);
					sqliteDatabase.insert("uploadsql", null, cv);
				}
			}else if(calculation==-1){
				Float actMaxMark = 0f; 
				for(Float f: actMaxMarkList){
					actMaxMark += f;
				}
				for(Students st: studentsArray){
					String sql = "update marks set Mark=((select SUM(Mark) from activitymark where ActivityId in" +
							" ("+sb.substring(0, sb.length()-1)+") and StudentId="+st.getStudentId()+") /"+actMaxMark+")*"+exmMaxMark+" where " +
							"ExamId="+examId+" and SubjectId="+subjectId+" and StudentId="+st.getStudentId();
					try{
						sqliteDatabase.execSQL(sql);
					}catch(SQLException e){
                        e.printStackTrace();
                    }
					ContentValues cv = new ContentValues();
					cv.put("Query", sql);
					sqliteDatabase.insert("uploadsql", null, cv);
				}
			}
		}
	}

	private void updateScoreField(String upScore){
		try{
			if(studentScore.get(index)!=null && !studentScore.get(index).equals("") && !studentScore.get(index).equals("-1")){
				studentScore.set(index, studentScore.get(index)+upScore);
			//	Double.parseDouble(studentScore.get(index));
			}else{
				studentScore.set(index, upScore);
			}
		}catch(NumberFormatException e){
			studentScore.set(index, upScore);
		}
		repopulateListArray();
	}

	private void populateListArray() {
		indexBound = studentsArray.size();
		int idx=0;
		for(Students s: studentsArray){
			if(idx==0){
				studentIndicate.set(idx, true);
				studentsArrayList.add(new Students(s.getRollNoInClass(),Capitalize.capitalThis(s.getName()),studentScore.get(idx),entered));
			}else{
				studentsArrayList.add(new Students(s.getRollNoInClass(),Capitalize.capitalThis(s.getName()),studentScore.get(idx),empty));
			}
			idx++;
		}
		marksAdapter.notifyDataSetChanged();
		lv.setSelection(index);
	}

	private void repopulateListArray() {
		studentsArrayList.clear();
		int idx=0;
		for(Students s: studentsArray){
			if(studentIndicate.get(idx)){
				studentsArrayList.add(new Students(s.getRollNoInClass(),Capitalize.capitalThis(s.getName()),studentScore.get(idx),entered));
			}else{
				studentsArrayList.add(new Students(s.getRollNoInClass(),Capitalize.capitalThis(s.getName()),studentScore.get(idx),empty));
			}
			idx++;
		}
		marksAdapter.notifyDataSetChanged();
		if(index == lastVisible)
			lv.setSelectionFromTop(index-1, top);
		else if(index < firstVisible)
			lv.setSelectionFromTop(index, firstVisible-totalVisible);
		else
			lv.setSelection(firstVisible);
	}

	class CalledBackLoad extends AsyncTask<String, String, String>{
		protected void onPreExecute(){
			super.onPreExecute();
		}
		@Override
		protected String doInBackground(String... params) {

			String subjectName = SubjectExamsDao.selectSubjectName(subjectId, sqliteDatabase);
			String className = ClasDao.getClassName(classId, sqliteDatabase);
			String sectionName = SectionDao.getSectionName(sectionId, sqliteDatabase);
			teacherName = Capitalize.capitalThis((TeacherDao.selectTeacherName(teacherId, sqliteDatabase)));

			String examName = ExamsDao.selectExamName(examId, sqliteDatabase);
			sf.append(className).append("-").append(sectionName).append(" "+subjectName).append("    "+examName).append("    "+activityName);		

			int partition = sharedPref.getInt("partition",0);
			if(partition==1){
				studentsArray = StudentsDao.selectStudents2(""+sectionId, subId, sqliteDatabase);
			}else{
				studentsArray = StudentsDao.selectStudents2(""+sectionId, subjectId, sqliteDatabase);
			}
			//	Collections.sort(studentsArray, new StudentsSort());
			for(int idx=0; idx<studentsArray.size(); idx++){
				studentIndicate.add(false);
			}
			for(Students s: studentsArray){
				studentsArrayId.add(s.getStudentId());
			}

			List<String> amList = ActivityMarkDao.selectActivityMarc(activityId, studentsArrayId, sqliteDatabase);
			for(String m: amList){
				/*if(m.equals("0")){
						studentScore.add("");
					}else{
						studentScore.add(m);
					}*/
				studentScore.add(m);
			}
			return null;
		}
		protected void onPostExecute(String s){
			super.onPostExecute(s);
			if(teacherName.length()>11){
				name.setText(teacherName.substring(0, 9)+"...");
			}else{
				name.setText(teacherName);
			}
			if(sf.length()>55){
				clasSecSub.setText(sf.substring(0,53)+"...");
			}else{
				clasSecSub.setText(sf);
			}	
			populateListArray();
			if(studentsArray.size()==0){
				getFragmentManager().popBackStack();
			}
		}
	}
}