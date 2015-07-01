package in.teacher.fragment;

import in.teacher.activity.R;
import in.teacher.adapter.Capitalize;
import in.teacher.dao.CCEStudentProfileDao;
import in.teacher.dao.StudentsDao;
import in.teacher.dao.TeacherDao;
import in.teacher.dao.TempDao;
import in.teacher.model.Profile;
import in.teacher.sqlite.CCEStudentProfile;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.ReplaceFragment;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

@SuppressLint("InflateParams")
public class UpdateCCEStudentProfile extends Fragment {
	private Activity act;
	private Context context;
	private SQLiteDatabase sqliteDatabase;
	private int sectionId, classId, teacherId, schoolId, term;
	private String teacherName;
	private ProfileAdapter profileAdapter;
	private List<Integer> studentsRoll;
	private ArrayList<Profile> profileList = new ArrayList<Profile>();
	private ListView lv;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState){
		View view = inflater.inflate(R.layout.enter_cce_student_profile, container, false);
		act = AppGlobal.getActivity();
		context = AppGlobal.getContext();
		sqliteDatabase = AppGlobal.getSqliteDatabase();
		
		Bundle b = getArguments();
		term = b.getInt("Term");

		Button submit = (Button)view.findViewById(R.id.submit);

		Temp t = TempDao.selectTemp(sqliteDatabase);
		sectionId = t.getSectionId();
		classId = t.getClassId();
		teacherId = t.getTeacherId();
		schoolId = t.getSchoolId();

		teacherName = Capitalize.capitalThis((TeacherDao.selectTeacherName(teacherId, sqliteDatabase)));
		Button name = (Button)view.findViewById(R.id.classSection);
		if(teacherName.length()>11){
			StringBuilder sb2 = new StringBuilder(teacherName.substring(0, 9)).append("...");
			name.setText(sb2.toString());
		}else{
			name.setText(teacherName);
		}

		lv = (ListView) view.findViewById(R.id.list);
		studentsRoll = StudentsDao.selectStudentIds(""+sectionId, sqliteDatabase);

		Cursor c = sqliteDatabase.rawQuery("select Height, Weight, DaysAttended1, StudentId, StudentName from ccestudentprofile where Term="+term+" and StudentId in " +
				"(select StudentId from students where SectionId="+sectionId+" order by RollNoInClass)", null);
		c.moveToFirst();
		int loop = 0;
		while(!c.isAfterLast()){
			int cid = c.getInt(c.getColumnIndex("StudentId"));
			String cname = c.getString(c.getColumnIndex("StudentName"));
			String cheight = c.getString(c.getColumnIndex("Height"));
			String cweight = c.getString(c.getColumnIndex("Weight"));
			double cdays = c.getDouble(c.getColumnIndex("DaysAttended1"));
			profileList.add(new Profile(cid, studentsRoll.get(loop)+"", cname, cheight, cweight, cdays+""));
			c.moveToNext();
			loop += 1;
		}
		c.close();

		profileAdapter = new ProfileAdapter(context, R.layout.profile_adapter, profileList);
		lv.setAdapter(profileAdapter);

		submit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				List<CCEStudentProfile> cspList = new ArrayList<CCEStudentProfile>();
				for(Profile p: profileList){
					CCEStudentProfile csp = new CCEStudentProfile();
					csp.setSchoolId(schoolId+"");
					csp.setClassId(classId+"");
					csp.setSectionId(sectionId+"");
					csp.setStudentId(p.getStudentId()+"");
					csp.setStudentName(p.getName());
					csp.setHeight(p.getHeight());
					csp.setWeight(p.getWeight());
					try{
						csp.setDaysAttended1(Double.parseDouble(p.getDaysAttended()));
					}catch(NumberFormatException e){
						csp.setDaysAttended1(0);
					}
					csp.setTerm(term);
					cspList.add(csp);
				}
				CCEStudentProfileDao.updateCCEStudentProfile(cspList, sqliteDatabase);
				ReplaceFragment.clearBackStack(getFragmentManager());
				ReplaceFragment.replace(new Dashbord(), getFragmentManager());
			}
		});

		return view;
	}

	public class ProfileAdapter extends ArrayAdapter<Profile>{
		int resource;
		Context context;
		ArrayList<Profile> data = new ArrayList<Profile>();

		public ProfileAdapter(Context context, int resource, ArrayList<Profile> listArray) {
			super(context, resource, listArray);
			this.context = context;
			this.resource = resource;
			this.data = listArray;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			RecordHolder holder = null;

			if (row == null) {
				LayoutInflater inflater = (LayoutInflater)context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				row = inflater.inflate(resource, parent, false);

				holder = new RecordHolder();
				holder.tv1 = (TextView) row.findViewById(R.id.roll);
				holder.tv2 = (TextView) row.findViewById(R.id.name);
				holder.tv3 = (TextView) row.findViewById(R.id.height);
				holder.tv4 = (TextView) row.findViewById(R.id.weight);
				holder.tv5 = (TextView) row.findViewById(R.id.days_attended);

				row.setTag(holder);
			}else{
				holder = (RecordHolder) row.getTag();
			}

			if(position % 2 == 0){
				//	row.setBackgroundResource(R.drawable.list_selector1);
				row.setBackgroundColor(Color.rgb(255, 255, 255));
			}else{
				//	row.setBackgroundResource(R.drawable.list_selector2);
				row.setBackgroundColor(Color.rgb(237, 239, 242));
			}

			Profile listItem = data.get(position);
			holder.tv1.setText(listItem.getRoll());
			holder.tv2.setText(listItem.getName());
			holder.tv3.setText(listItem.getHeight());
			holder.tv4.setText(listItem.getWeight());
			if(!listItem.getDaysAttended().equals("0.0")){
				holder.tv5.setText(listItem.getDaysAttended());
			}else{
				holder.tv5.setText("");
			}


			holder.tv3.setOnClickListener(heightClickListener);
			holder.tv4.setOnClickListener(weightClickListener);
			holder.tv5.setOnClickListener(daysClickListener);

			return row;
		}

		class RecordHolder{
			TextView tv1;
			TextView tv2;
			TextView tv3;
			TextView tv4;
			TextView tv5;
		}
	}

	private OnClickListener heightClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			ListView mListView = (ListView) v.getParent().getParent();
			final int position = mListView.getPositionForView((View) v.getParent());
			final Profile p = profileList.get(position);

			AlertDialog.Builder builder = new AlertDialog.Builder(act);
			View view = act.getLayoutInflater().inflate(R.layout.profile_dialog, null);
			TextView h = (TextView)view.findViewById(R.id.name_profile);
			h.setText(p.getName()+" - [Height]");
			final EditText edListChild = (EditText)view.findViewById(R.id.value);
			edListChild.setText(p.getHeight());
			edListChild.setSelection(edListChild.length());

			builder.setView(view);
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(edListChild.getText().toString().equals("")){

					}else{
						Profile prof = new Profile(p.getStudentId(),studentsRoll.get(position)+"", p.getName(), 
								edListChild.getText().toString(), profileList.get(position).getWeight(), profileList.get(position).getDaysAttended());
						profileList.set(position, prof);
						profileAdapter.notifyDataSetChanged();
					}
				}
			});
			builder.setNegativeButton("Cancel", null);        
			builder.show();
		}
	};

	private OnClickListener weightClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			ListView mListView = (ListView) v.getParent().getParent();
			final int position = mListView.getPositionForView((View) v.getParent());
			final Profile p = profileList.get(position);

			AlertDialog.Builder builder = new AlertDialog.Builder(act);
			View view = act.getLayoutInflater().inflate(R.layout.profile_dialog, null);
			TextView w = (TextView)view.findViewById(R.id.name_profile);
			w.setText(p.getName()+" - [Weight]");
			final EditText edListChild = (EditText)view.findViewById(R.id.value);
			edListChild.setText(p.getWeight());
			edListChild.setSelection(edListChild.length());

			builder.setView(view);
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(edListChild.getText().toString().equals("")){

					}else{
						Profile prof = new Profile(p.getStudentId(),studentsRoll.get(position)+"", p.getName(), 
								profileList.get(position).getHeight(), edListChild.getText().toString(), profileList.get(position).getDaysAttended());
						profileList.set(position, prof);
						profileAdapter.notifyDataSetChanged();
					}
				}
			});
			builder.setNegativeButton("Cancel", null);        
			builder.show();
		}
	};

	private OnClickListener daysClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			ListView mListView = (ListView) v.getParent().getParent();
			final int position = mListView.getPositionForView((View) v.getParent());
			final Profile p = profileList.get(position);

			AlertDialog.Builder builder = new AlertDialog.Builder(act);
			View view = act.getLayoutInflater().inflate(R.layout.profile_dialog, null);
			TextView dA = (TextView)view.findViewById(R.id.name_profile);
			dA.setText(p.getName()+" - [Days Attended]");
			final EditText edListChild = (EditText)view.findViewById(R.id.value);
			if(p.getDaysAttended().equals("0.0"))
				edListChild.setText("");	
			else
				edListChild.setText(p.getDaysAttended());
			edListChild.setSelection(edListChild.length());

			builder.setView(view);
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(edListChild.getText().toString().equals("")){

					}else{
						Profile prof = new Profile(p.getStudentId(),studentsRoll.get(position)+"", p.getName(), 
								profileList.get(position).getHeight(), profileList.get(position).getWeight(), edListChild.getText().toString());
						profileList.set(position, prof);
						profileAdapter.notifyDataSetChanged();
					}
				}
			});
			builder.setNegativeButton("Cancel", null);        
			builder.show();
		}
	};

}
