package in.teacher.fragment;

import in.teacher.activity.R;
import in.teacher.activity.R.animator;
import in.teacher.adapter.StudExamAdapter;
import in.teacher.dao.ActivitiDao;
import in.teacher.dao.ActivityMarkDao;
import in.teacher.dao.ExmAvgDao;
import in.teacher.dao.MarksDao;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.Amr;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.ReplaceFragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class SearchStudExam extends Fragment {
	private Context context;
	private int studentId, sectionId, classId;
	private String studentName, className, secName;
	private SQLiteDatabase sqliteDatabase;
	private ListView lv;
	private ArrayList<Amr> amrList = new ArrayList<>();
	private StudExamAdapter adapter;
	private List<Integer> examIdList = new ArrayList<>();
	private List<String> examNameList = new ArrayList<>();
	private List<Integer> avgList1 = new ArrayList<>();
	private List<Integer> avgList2 = new ArrayList<>();
	private List<Integer>isSubGotActList = new ArrayList<>();
	private ProgressDialog pDialog;
	private TextView studTV, clasSecTV;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState){
		View view = inflater.inflate(R.layout.search_se_exam, container, false);

		context = AppGlobal.getContext();
		sqliteDatabase = AppGlobal.getSqliteDatabase();
		pDialog  = new ProgressDialog(this.getActivity());

		clearList();

		studTV = (TextView)view.findViewById(R.id.studName);
		clasSecTV = (TextView)view.findViewById(R.id.studClasSec);
		lv = (ListView)view.findViewById(R.id.list);
		adapter = new StudExamAdapter(context, amrList);
		lv.setAdapter(adapter);

		view.findViewById(R.id.slipSearch).setOnClickListener(searchSlipTest);
		view.findViewById(R.id.attSearch).setOnClickListener(searchAttendance);

		Temp t = TempDao.selectTemp(sqliteDatabase);
		studentId = t.getStudentId();

		new CalledBackLoad().execute();

		lv.setOnItemClickListener(clickListItem);

		return view;
	}

	private void clearList(){
		amrList.clear();
		examIdList.clear();
		examNameList.clear();
		avgList1.clear();
		avgList2.clear();
	}

	private View.OnClickListener searchSlipTest = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			ReplaceFragment.replace(new SearchStudST(), getFragmentManager());
		}
	};

	private View.OnClickListener searchAttendance = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			ReplaceFragment.replace(new SearchStudAtt(), getFragmentManager());
		}
	};

    private OnItemClickListener clickListItem = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            TempDao.updateExamId(examIdList.get(position), sqliteDatabase);
            ReplaceFragment.replace(new SearchStudExamSub(), getFragmentManager());
        }
    };

	class CalledBackLoad extends AsyncTask<String, String, String>{
		protected void onPreExecute(){
			super.onPreExecute();
			pDialog.setMessage("Preparing data ...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}
		@Override
		protected String doInBackground(String... params) {
			Cursor c = sqliteDatabase.rawQuery("select A.Name, A.ClassId, A.SectionId, B.ClassName, C.SectionName from students A, class B, section C where"+
					" A.StudentId="+studentId+" and A.ClassId=B.ClassId and A.SectionId=C.SectionId group by A.StudentId", null);
			c.moveToFirst();
			while(!c.isAfterLast()){
				studentName = c.getString(c.getColumnIndex("Name"));
				classId = c.getInt(c.getColumnIndex("ClassId"));
				sectionId = c.getInt(c.getColumnIndex("SectionId"));
				className = c.getString(c.getColumnIndex("ClassName"));
				secName = c.getString(c.getColumnIndex("SectionName"));
				c.moveToNext();
			}
			c.close();

			Cursor c2 = sqliteDatabase.rawQuery("select ExamId,ExamName from exams where ClassId="+classId, null);
			c2.moveToFirst();
			while(!c2.isAfterLast()){
				examIdList.add(c2.getInt(c2.getColumnIndex("ExamId")));
				examNameList.add(c2.getString(c2.getColumnIndex("ExamName")));
				c2.moveToNext();
			}
			c2.close();

			List<Integer> subIdList = new ArrayList<>();
			Cursor cc = sqliteDatabase.rawQuery("select A.SubjectId from subjectteacher A, subjects B, teacher C where A.SectionId="+sectionId +" and"+
					" A.SubjectId=B.SubjectId and A.TeacherId=C.TeacherId", null);
			cc.moveToFirst();
			while(!cc.isAfterLast()){
				subIdList.add(cc.getInt(cc.getColumnIndex("SubjectId")));
				cc.moveToNext();
			}
			cc.close();

			List<Integer> progressList1 = new ArrayList<>();
			int cache = 0;
			int average = 0;
			int len = 0;
			int actAvg = 0;
			int overallActAvg = 0;
			List<Integer> actList = new ArrayList<>();
			for(Integer id:examIdList){
				len=0;
				isSubGotActList.clear();
				for(Integer subId: subIdList){
					cache = ActivitiDao.isThereActivity(sectionId, subId, id, sqliteDatabase);
					if(cache==1){
						isSubGotActList.add(subId);
					}
				}

				overallActAvg = 0;
				for(Integer sub: subIdList){
					int avg = 0;
					if(isSubGotActList.contains(sub)){
						actList.clear();
						actAvg = 0;
						Cursor c3 = sqliteDatabase.rawQuery("select ActivityId from activity where ExamId="+id+" and SubjectId="+sub+" and SectionId="+sectionId, null);
						c3.moveToFirst();
						while(!c3.isAfterLast()){
							actList.add(c3.getInt(c3.getColumnIndex("ActivityId")));
							c3.moveToNext();
						}
						c3.close();

						for(Integer actId: actList){
							actAvg+= ActivityMarkDao.getStudActAvg(studentId, actId, sqliteDatabase);
						}
						overallActAvg = actAvg/actList.size();
						if(overallActAvg!=0){
							len++;
						}
						progressList1.add(overallActAvg);
					}else{
						avg = MarksDao.getStudExamAvg(studentId, sub, id, sqliteDatabase);
						if(avg!=0){
							len++;
						}
						progressList1.add(avg);
					}
				}
				average  = 0;
				for(Integer i: progressList1){
					average+=i;
				}
				if(len==0){
					len = 1;
				}
				avgList1.add(average/len);
				progressList1.clear();
			}

			for(Integer id : examIdList){
				avgList2.add(ExmAvgDao.getSeExamAvg(id, sectionId, sqliteDatabase));
			}

			for(int i=0; i<examIdList.size(); i++){
				try{
					amrList.add(new Amr(examNameList.get(i),avgList1.get(i),avgList2.get(i)));
				}catch(IndexOutOfBoundsException e){
					amrList.add(new Amr(examNameList.get(i),0,0));
				}

			}
			return null;
		}
		
		protected void onPostExecute(String s){
			super.onPostExecute(s);
			studTV.setText(studentName);
			clasSecTV.setText(className+" - "+secName);
			adapter.notifyDataSetChanged();
			pDialog.dismiss();
		}
	}

}
