package in.teacher.fragment;

import in.teacher.activity.R;
import in.teacher.activity.R.animator;
import in.teacher.adapter.Capitalize;
import in.teacher.dao.TeacherDao;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;

import java.util.ArrayList;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class SelectCCEStudentProfile extends Fragment {
	private Context context;
	private SQLiteDatabase sqliteDatabase;
	private int sectionId;
	private ArrayList<String> termList = new ArrayList<>();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState){
		View view = inflater.inflate(R.layout.cce_student_profile, container, false);
		context = AppGlobal.getContext();
		sqliteDatabase = AppGlobal.getSqliteDatabase();
		
		Spinner spin = (Spinner)view.findViewById(R.id.spinner);

		Temp t = TempDao.selectTemp(sqliteDatabase);
		sectionId = t.getSectionId();
		int classId = t.getClassId();
		int teacherId = t.getTeacherId();

		String teacherName = Capitalize.capitalThis((TeacherDao.selectTeacherName(teacherId, sqliteDatabase)));
		Button name = (Button)view.findViewById(R.id.classSection);
		if(teacherName.length()>11){
			name.setText(teacherName.substring(0, 9)+"...");
		}else{
			name.setText(teacherName);
		}
		
		Cursor c1 = sqliteDatabase.rawQuery("select distinct Term from exams where ClassId="+classId, null);
		c1.moveToFirst();
		termList.clear();
		termList.add("Select Term");
		while(!c1.isAfterLast()){
			termList.add(c1.getInt(c1.getColumnIndex("Term"))+"");
			c1.moveToNext();
		}
		c1.close();
		
		ArrayAdapter<String> adapter = new ArrayAdapter<>(context,  R.layout.spinner_header, termList);
		adapter.setDropDownViewResource(R.layout.spinner_droppeddown);
		spin.setAdapter(adapter);
		
		spin.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if(position!=0){
					Bundle b = new Bundle();
					b.putInt("Term", Integer.parseInt(termList.get(position)));
					Cursor c2 = sqliteDatabase.rawQuery("select * from ccestudentprofile where Term="+termList.get(position)+" and SectionId='"+sectionId+"'", null);
					if(c2.getCount()>0){
						Fragment fragment = new UpdateCCEStudentProfile();
						fragment.setArguments(b);
						getFragmentManager()
						.beginTransaction()
						.setCustomAnimations(animator.fade_in,animator.fade_out)
						.replace(R.id.content_frame, fragment).addToBackStack(null).commit();
					}else{
						Fragment fragment = new InsertCCEStudentProfile();
						fragment.setArguments(b);
						getFragmentManager()
						.beginTransaction()
						.setCustomAnimations(animator.fade_in,animator.fade_out)
						.replace(R.id.content_frame, fragment).addToBackStack(null).commit();
					}
				}
				
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {}
		});
		
		return view;
	}
}