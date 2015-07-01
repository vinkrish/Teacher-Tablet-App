package in.teacher.fragment;

import in.teacher.activity.R;
import in.teacher.adapter.Capitalize;
import in.teacher.adapter.HomeworkViewAdapter;
import in.teacher.dao.ClasDao;
import in.teacher.dao.HomeworkDao;
import in.teacher.dao.SectionDao;
import in.teacher.dao.TeacherDao;
import in.teacher.dao.TempDao;
import in.teacher.model.HW;
import in.teacher.sqlite.Homework;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.ReplaceFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("InflateParams")
public class VerifyHomework extends Fragment {
	private Context context;
	private static Activity act;
	private int sectionId;
	private List<String> listDataHeader1,hwMessage;
	private HashMap<String, List<String>> listDataChild1;
	private List<Integer> childList1 = new ArrayList<>();
	private ArrayList<Integer> subjectIdList = new ArrayList<>();
	private ArrayList<String> subjectNameList = new ArrayList<>();
	private ArrayList<String> subNameList1 = new ArrayList<>();
	private static String hwDate;
	private SQLiteDatabase sqliteDatabase;
	private Homework homework;
	private ListView lv;
	private ArrayList<HW> hwList = new ArrayList<>();
	private HomeworkViewAdapter homeworkViewAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState){

		View view = inflater.inflate(R.layout.verify_homework, container, false);
		context = AppGlobal.getContext();
		sqliteDatabase = AppGlobal.getSqliteDatabase();
		act = AppGlobal.getActivity();
		hwDate = getToday();
		homework = new Homework();

		Temp t = TempDao.selectTemp(sqliteDatabase);
		int classId = t.getClassId();
		sectionId = t.getSectionId();
		int teacherId = t.getTeacherId();

		String teacherName = Capitalize.capitalThis((TeacherDao.selectTeacherName(teacherId, sqliteDatabase)));
		Button name = (Button)view.findViewById(R.id.classSection);
		if(teacherName.length()>11){
			name.setText(teacherName.substring(0, 9)+"...");
		}else{
			name.setText(teacherName);
		}
        String className = ClasDao.getClassName(classId, sqliteDatabase);
        String sectionName= SectionDao.getSectionName(sectionId, sqliteDatabase);

		Cursor c = sqliteDatabase.rawQuery("select  A.SubjectId, B.SubjectName from subjectteacher A, subjects B where A.SectionId="+sectionId+" and A.SubjectId=B.SubjectId", null);
		c.moveToFirst();
		while(!c.isAfterLast()){
			subjectIdList.add(c.getInt(c.getColumnIndex("SubjectId")));
			subjectNameList.add(c.getString(c.getColumnIndex("SubjectName")));
			c.moveToNext();
		}
		c.close();

		TextView hwTv = (TextView)view.findViewById(R.id.hwPleaseTap);
		if(classId==0){
			hwTv.setText("Not a class teacher.");
		}else{
			StringBuilder hwString = new StringBuilder();
			hwString.append(className).append("-"+sectionName+"  ").append("Confirm your submission - ").append(hwDate);
			hwTv.setText(hwString);
		}

		Button submit = (Button)view.findViewById(R.id.submit);
		Button back = (Button) view.findViewById(R.id.back);

		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ReplaceFragment.replace(new InsertHomework(), getFragmentManager());
			}
		});

		submit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(context, "homework has been updated successfully", Toast.LENGTH_LONG).show();
				HomeworkDao.insertHwPresent(sectionId, getToday(), sqliteDatabase);
				HomeworkDao.insertHwSql(homework, sqliteDatabase);
				ReplaceFragment.replace(new InsertHomework(), getFragmentManager());
			}
		});

		final Button todayButton = (Button)view.findViewById(R.id.today);
		final Button yesterdayButton = (Button)view.findViewById(R.id.yesterday);
		Button otherdayButton = (Button)view.findViewById(R.id.otherday);

		todayButton.setActivated(true);
		yesterdayButton.setActivated(false);
		otherdayButton.setActivated(false);

		yesterdayButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Bundle b = new Bundle();
				b.putInt("today", 0);
				b.putInt("yesterday", 1);
				b.putInt("otherday", 0);
				Fragment fragment = new HomeworkView();
				fragment.setArguments(b);
				FragmentManager fragmentManager = getFragmentManager();
				fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(null).commit();	
			}
		});

		otherdayButton.setOnClickListener(new View.OnClickListener() {		
			@Override
			public void onClick(View arg0) {
				Bundle b = new Bundle();
				b.putInt("today", 0);
				b.putInt("yesterday", 0);
				b.putInt("otherday", 1);
				Fragment fragment = new HomeworkView();
				fragment.setArguments(b);
				FragmentManager fragmentManager = getFragmentManager();
				fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(null).commit();
			}
		});

		prepareListDataNew();

        lv = (ListView) view.findViewById(R.id.list);
        homeworkViewAdapter = new HomeworkViewAdapter(context, hwList);
        lv.setAdapter(homeworkViewAdapter);

		return view;
	}

	private String getToday() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		Date today = new Date();
		return dateFormat.format(today);
	}

    public void prepareListDataNew(){
        childList1.clear();
        List<Homework> listHW = HomeworkDao.selectHomework(sectionId, hwDate, sqliteDatabase);
        if(listHW.size()!=0){
            extractHomeworkNew(listHW);
        }

        hwList.clear();
        int hwLoop = 0;
        for(Integer childList11 : childList1){
            for(int loop=0; loop<subjectIdList.size(); loop++){
                if(childList11.equals(subjectIdList.get(loop))){
                    hwList.add(new HW(subjectNameList.get(loop), hwMessage.get(hwLoop)));
                    break;
                }
            }
            hwLoop += 1;
        }
    }

    public void extractHomeworkNew(List<Homework> hwList){
        hwMessage = new ArrayList<>();
        for(Homework hw: hwList){
            homework.setClassId(hw.getClassId());
            homework.setHomeworkId(hw.getHomeworkId());
            homework.setHomework(hw.getHomework());
            homework.setSchoolId(hw.getSchoolId());
            homework.setSectionId(hw.getSectionId());
            homework.setSubjectIDs(hw.getSubjectIDs());
            homework.setTeacherId(hw.getTeacherId());
            homework.setHomeworkDate(hw.getHomeworkDate());

            String subjectIds = hw.getSubjectIDs();
            String splitBy = ",";
            String[] id = subjectIds.split(splitBy);
            for(String subjectId: id){
                childList1.add(Integer.parseInt(subjectId));
            }
            String messageBody = hw.getHomework();
            String splitBy2 = "#";
            String[] message = messageBody.split(splitBy2);
            hwMessage.addAll(Arrays.asList(message));
        }
    }

}
