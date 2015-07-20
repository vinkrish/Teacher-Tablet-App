package in.teacher.fragment;

import in.teacher.activity.R;
import in.teacher.adapter.Capitalize;
import in.teacher.dao.ActivitiDao;
import in.teacher.dao.ClasDao;
import in.teacher.dao.ExamsDao;
import in.teacher.dao.ExmAvgDao;
import in.teacher.dao.MarksDao;
import in.teacher.dao.SectionDao;
import in.teacher.dao.SubjectExamsDao;
import in.teacher.dao.TeacherDao;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.Activiti;
import in.teacher.sqlite.Exams;
import in.teacher.sqlite.SeObject;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.CommonDialogUtils;
import in.teacher.util.PKGenerator;
import in.teacher.util.ReplaceFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class StructuredExam extends Fragment {
	private Context context;
	private Activity act;
	private SQLiteDatabase sqliteDatabase;
	private String subjectName, teacherName;
	private int classId, sectionId, subjectId, teacherId;
	private List<Activiti> activitiList = new ArrayList<>();;
	private final Map<Object,Object> m = new HashMap<>();
	private List<Integer> examIdList = new ArrayList<>();
	private ArrayList<SeObject> circleArrayGrid = new ArrayList<>();
	private CircleAdapter cA;
	private GridView gridView;
	private Button name;
	private TextView clasSecSubTv;
	private Bitmap inserted, notinserted;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState){
		View view = inflater.inflate(R.layout.se, container, false);
		act = AppGlobal.getActivity();
		context = AppGlobal.getContext();
		sqliteDatabase = AppGlobal.getSqliteDatabase();
		
		initializeList();
		
		gridView = (GridView) view.findViewById(R.id.gridView);
		name = (Button)view.findViewById(R.id.teacherName);
		clasSecSubTv = (TextView)view.findViewById(R.id.headerClasSecSub);
		inserted = BitmapFactory.decodeResource(this.getResources(), R.drawable.tick);
		notinserted = BitmapFactory.decodeResource(this.getResources(), R.drawable.cross);
		cA = new CircleAdapter(context, R.layout.se_grid, circleArrayGrid);
		gridView.setAdapter(cA);

		Temp t = TempDao.selectTemp(sqliteDatabase);
		classId = t.getCurrentClass();
		sectionId = t.getCurrentSection();
		subjectId = t.getCurrentSubject();
		teacherId = t.getTeacherId();
		subjectName = SubjectExamsDao.selectSubjectName(subjectId, sqliteDatabase);

		new CalledBackLoad().execute();

		return view;
	}
	
	private void initializeList(){
		m.clear();
		activitiList.clear();
		examIdList.clear();
		circleArrayGrid.clear();
	}

	public class CircleAdapter extends ArrayAdapter<SeObject> {
		Context context;
		int layoutResourceId;
		ArrayList<SeObject>	data = new ArrayList<>();

		public CircleAdapter(Context context, int layoutResourceId,ArrayList<SeObject> gridArray) {
			super(context, layoutResourceId, gridArray);
			this.context = context;
			this.layoutResourceId = layoutResourceId;
			this.data = gridArray;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View row = convertView;
			RecordHolder holder;

			if (row == null) {
				LayoutInflater inflater = (LayoutInflater)context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				row = inflater.inflate(layoutResourceId, parent, false);

				holder = new RecordHolder();
				holder.image = (ImageView) row.findViewById(R.id.tickCross);
				holder.examTxt = (TextView) row.findViewById(R.id.exam);
				row.setTag(holder);

			} else {
				holder = (RecordHolder) row.getTag();
			}

			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			FrameLayout fl = (FrameLayout)row.findViewById(R.id.fl);

			SeObject gridItem = data.get(position);
			holder.examTxt.setText(gridItem.getExam());
			holder.image.setImageBitmap(gridItem.getTickCross());
			SampleView sV = new SampleView(context, gridItem.getProgressInt());
			fl.addView(sV,layoutParams);

			sV.setOnClickListener(new OnClickListener() {	
				@Override
				public void onClick(View v) {
					viewClickListener(position);
				}
			});
			return row;
		}

		public class RecordHolder {
			TextView examTxt;
			ImageView image;
		}

		private class SampleView extends View {
			Paint p,defaultPaint;
			RectF rectF1;
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
				defaultPaint.setStrokeWidth(6);
				Resources res = getResources();
				int defalt = res.getColor(R.color.defalt);
				defaultPaint.setColor(defalt);
				rectF1 = new RectF(10, 10, 120, 120);
			}

			@Override
			protected void onDraw(Canvas canvas) {
				p.setAntiAlias(true);
				p.setStyle(Paint.Style.STROKE); 
				p.setStrokeWidth(6);

				if(localInt>=270){
					p.setColor(getResources().getColor(R.color.green));
				}else if(localInt>=180){
					p.setColor(getResources().getColor(R.color.orange));
				}else if(localInt>0){
					p.setColor(getResources().getColor(R.color.red));
				}
				canvas.drawArc (rectF1, 0, 360, false, defaultPaint);
				canvas.drawArc (rectF1, 270, Float.parseFloat(localInt+""), false, p);
			}
		}
	}
	
	public void viewClickListener(int position){
		int i = examIdList.get(position);
		TempDao.updateExamId(i, sqliteDatabase);
		activitiList.clear();
		activitiList = ActivitiDao.selectActiviti(i,subjectId,sectionId,sqliteDatabase);
		if(activitiList.size()!=0){
			ReplaceFragment.replace(new ActivityExam(), getFragmentManager());
		}else{
			int isExmMaxMark = SubjectExamsDao.isExmMaxMarkDefined(classId, i, subjectId, sqliteDatabase);
			if(isExmMaxMark==1){
				Boolean b = (Boolean)m.get(i);
				if(b!=null && b){
					ReplaceFragment.replace(new UpdateExamMark(), getFragmentManager());
				}else{
					ReplaceFragment.replace(new InsertExamMark(), getFragmentManager());
				}
			}else{
				CommonDialogUtils.displayAlertWhiteDialog(act, "Exam mark is not defined");
			}
		}
	}

	class CalledBackLoad extends AsyncTask<String, String, String>{
		private StringBuilder exmName = new StringBuilder();
		protected void onPreExecute(){
			super.onPreExecute();
		}
		@Override
		protected String doInBackground(String... params) {
			String className = ClasDao.getClassName(classId, sqliteDatabase);
			String sectionName = SectionDao.getSectionName(sectionId, sqliteDatabase);
			teacherName = Capitalize.capitalThis((TeacherDao.selectTeacherName(teacherId, sqliteDatabase)));	
			exmName.append(className).append("-").append(sectionName).append("    ").append(subjectName);
			List<Exams> examList = ExamsDao.selectExams(classId, subjectId, sqliteDatabase);
			for(Exams exam: examList){
				int markEntry = MarksDao.isThereExamMark(exam.getExamId(), sectionId, subjectId, sqliteDatabase);
				if(markEntry==1){
					m.put(exam.getExamId(), true);
				}
				examIdList.add(exam.getExamId());
				int avg = ExmAvgDao.selectedExmAvg(sectionId, subjectId, exam.getExamId(), sqliteDatabase);
				boolean imageFlag = false;
				int i = ExmAvgDao.selectedExmComplete(sectionId, subjectId, exam.getExamId(), sqliteDatabase);
				if(i==1){
					imageFlag = true;
				}
				if(imageFlag){
					circleArrayGrid.add(new SeObject(avg, PKGenerator.trim(0, 20, exam.getExamName()), exmName.toString(), inserted));
				}else{
					circleArrayGrid.add(new SeObject(avg, PKGenerator.trim(0, 20, exam.getExamName()), exmName.toString(), notinserted));
				}
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
			clasSecSubTv.setText(exmName);
			cA.notifyDataSetChanged();
		}
	}

}
