package in.teacher.fragment;

import in.teacher.activity.R;
import in.teacher.adapter.Capitalize;
import in.teacher.dao.PortionDao;
import in.teacher.dao.SlipTestMarkDao;
import in.teacher.dao.SlipTesttDao;
import in.teacher.dao.TeacherDao;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.Portion;
import in.teacher.sqlite.SlipTestt;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.CommonDialogUtils;
import in.teacher.util.ReplaceFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class EditSlipTest extends Fragment {
	private static Context newContext;
	private Context context;
	private static Activity act;
	private SQLiteDatabase sqliteDatabase;
	private Long slipTestId;
	private double maxMark;
	private static String portionId;
	private static String extraPortion = "", otherdate;
	private static TextView portion,stdate;
	private static RelativeLayout rlcommon, rlgeneral;
	private EditText maxmark,maxmark2,manualPortion;
	private static ArrayList<String> portionIdList = new ArrayList<>();
	private static ArrayList<String> portionList = new ArrayList<>();
	static CharSequence[] portionNameList; 
	static boolean checked_state[];
	private static boolean dateFlag;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState){

		View view = inflater.inflate(R.layout.slip_test, container, false);
		
		newContext = AppGlobal.getContext();
		context = AppGlobal.getContext();
		sqliteDatabase = AppGlobal.getSqliteDatabase();
		act = this.getActivity();

		RadioGroup evaluationType = (RadioGroup)view.findViewById(R.id.evaluationType);
		maxmark = (EditText)view.findViewById(R.id.stmaxmark);
		maxmark2 = (EditText)view.findViewById(R.id.stmaxmark2);
		manualPortion = (EditText)view.findViewById(R.id.manualportion);

		portion = (TextView)view.findViewById(R.id.selectedportion);
		stdate = (TextView)view.findViewById(R.id.selecteddate);

		Button sliptestButton = (Button)view.findViewById(R.id.sliptestsubmit);
		Button sliptestReset = (Button)view.findViewById(R.id.sliptestreset);
		Button sliptestButton2 = (Button)view.findViewById(R.id.sliptestsubmit2);
		Button sliptestReset2 = (Button)view.findViewById(R.id.sliptestreset2);

		rlcommon = (RelativeLayout) view.findViewById(R.id.rlhide);
		rlgeneral = (RelativeLayout) view.findViewById(R.id.rlhide2);

		Temp t = TempDao.selectTemp(sqliteDatabase);
		int schoolId = t.getSchoolId();
		int classId = t.getCurrentClass();
	//	sectionId = t.getSectionId();
		int subjectId = t.getCurrentSubject();
		int teacherId = t.getTeacherId();
		slipTestId = t.getSlipTestId();

		String teacherName = Capitalize.capitalThis((TeacherDao.selectTeacherName(teacherId, sqliteDatabase)));
		Button name = (Button)view.findViewById(R.id.classSection);
		if(teacherName.length()>11){
			name.setText(teacherName.substring(0, 9)+"...");
		}else{
			name.setText(teacherName);
		}

		SlipTestt slipTest = SlipTesttDao.selectSlipTest(slipTestId, sqliteDatabase);
		portionId = slipTest.getPortion();
		portion.setText(slipTest.getPortionName());
		stdate.setText(slipTest.getTestDate());
		maxmark.setText(slipTest.getMaximumMark()+"");

		maxMark = SlipTestMarkDao.getSTMaxMark(slipTestId, schoolId, sqliteDatabase);
		List<Portion> pList = PortionDao.selectPortion(classId, subjectId, sqliteDatabase);

		portionIdList.clear();
		portionList.clear();
		portionIdList.add(-1+"");
		portionList.add("General");
		for(Portion p: pList){
			portionIdList.add(p.getPortionId()+"");
			portionList.add(p.getPortion());
		}

		portionNameList = portionList.toArray(new CharSequence[portionList.size()]);
		checked_state = new boolean[portionList.size()];


		portion.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				showDialog();			
			}
		});

		sliptestReset.setOnClickListener(new View.OnClickListener() {		
			@Override
			public void onClick(View v) {
				portion.setText("");
				stdate.setText("");
				maxmark.setText("");
			}
		});
		sliptestReset2.setOnClickListener(new View.OnClickListener() {		
			@Override
			public void onClick(View v) {
				manualPortion.setText("");
				stdate.setText("");
				maxmark2.setText("");
			}
		});

		stdate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(dateFlag){
					stdate.setText(otherdate);
					dateFlag = false;
				}else{
					DialogFragment newFragment = new DatePickerFragment();
					newFragment.show(getFragmentManager(), "datePicker");
				}	
			}
		});

		sliptestButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(portion.getText().toString().equals("")){
					CommonDialogUtils.displayAlertWhiteDialog(act, "Please select portion");
				}else if(stdate.getText().toString().equals("")){
                    CommonDialogUtils.displayAlertWhiteDialog(act, "Please select slip test date");
				}else if(maxmark.getText().toString().equals("")){
                    CommonDialogUtils.displayAlertWhiteDialog(act, "Please enter maximum mark");
				}else if(Double.parseDouble(maxmark.getText().toString())<maxMark){
                    CommonDialogUtils.displayAlertWhiteDialog(act, "Entered maximum mark is lesser than stored marks");
				}else{
					submitSlipTest();
				}
			}
		});
		sliptestButton2.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(portion.getText().toString().equals("")){
                    CommonDialogUtils.displayAlertWhiteDialog(act, "Please select portion");
				}else if(stdate.getText().toString().equals("")){
                    CommonDialogUtils.displayAlertWhiteDialog(act, "Please select slip test date");
				}else if(manualPortion.getText().toString().equals("")){
                    CommonDialogUtils.displayAlertWhiteDialog(act, "Please Enter Title");
				}else if(maxmark2.getText().toString().equals("")){
                    CommonDialogUtils.displayAlertWhiteDialog(act, "Please enter maximum mark");
				}else if(Integer.parseInt(maxmark2.getText().toString())<maxMark){
                    CommonDialogUtils.displayAlertWhiteDialog(act, "Entered maximum mark is lesser than stored marks");
				}else{
					submitSlipTest2();
				}
			}
		});

		evaluationType.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch(checkedId) {
				case R.id.slipTest:  	
					portion.setText("");
					portion.setHint("Click here to select portion");
					rlcommon.setVisibility(View.VISIBLE);
					rlgeneral.setVisibility(View.INVISIBLE);
					break;
				case R.id.projects:
					portion.setText("");
					portion.setHint("Click here to select portion");
					rlcommon.setVisibility(View.INVISIBLE);
					rlgeneral.setVisibility(View.VISIBLE);
					break;
				case R.id.activities:
					portion.setText("");
					portion.setHint("Click here to select portion");
					rlcommon.setVisibility(View.INVISIBLE);
					rlgeneral.setVisibility(View.VISIBLE);
					break;
				}

			}
		});
		for(int j=0; j<checked_state.length;j++){
			if(portionId.equals(portionIdList.get(j))&&j==0){
				portion.setText("General");
				portionId = slipTest.getPortion();
				manualPortion.setText(slipTest.getPortionName());
				stdate.setText(slipTest.getTestDate());
				maxmark2.setText(slipTest.getMaximumMark()+"");
				checked_state[j] = true;
				rlcommon.setVisibility(View.INVISIBLE);
				rlgeneral.setVisibility(View.VISIBLE);
			}

		}
		return view;

	}

	void showDialog() {
		// Create the fragment and show it as a dialog.
		DialogFragment newFragment = MyDialogFragment.newInstance();
		newFragment.show(getFragmentManager(), "dialog");
	}

	public static class MyDialogFragment extends DialogFragment {
		static MyDialogFragment newInstance() {
			return new MyDialogFragment();
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState){
			AlertDialog.Builder builder = new AlertDialog.Builder(newContext)
			.setTitle("Select portions")
			.setMultiChoiceItems(portionNameList, checked_state, new DialogInterface.OnMultiChoiceClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					checked_state[which]=isChecked;
				}}).setPositiveButton("OK", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						portion.setText("");
						extraPortion = "";
						for(int i=0;i<portionList.size();i++){
							if(i==0){
								if(checked_state[i]){
									rlcommon.setVisibility(View.INVISIBLE);
									rlgeneral.setVisibility(View.VISIBLE);
									portion.setText(portionNameList[i]);
									portionId = portionIdList.get(i);
									for(int j=1; j<checked_state.length;j++){
										checked_state[j] = false;
									}
								}else if(!checked_state[i]){
									rlcommon.setVisibility(View.VISIBLE);
									rlgeneral.setVisibility(View.INVISIBLE);
								}	
							}else if(checked_state[i]){
								if(portion.getText().toString().equals("") || portion.getText().toString().equals(null)){
									portion.setText(portionNameList[i]);
									portionId = portionIdList.get(i);
								}else{
									StringBuilder strBuilder = new StringBuilder(portion.getText().toString()).append(",").append(portionNameList[i]);
									portion.setText(strBuilder);
									if(extraPortion.equals("")){
										extraPortion = portionIdList.get(i);
									}else{
										String porBuilder = extraPortion;
										extraPortion = porBuilder+","+portionIdList.get(i);
									}
								}
							}
						}
						dialog.dismiss();
					}
				});
		//	AlertDialog alertdialog = builder.create();
			return builder.create();
		}
	}

	private void submitSlipTest(){
		InputMethodManager imm = (InputMethodManager)context.getSystemService(
				Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(maxmark.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(manualPortion.getWindowToken(), 0);	

		SlipTestt st = new SlipTestt();
		st.setMaximumMark(Integer.parseInt(maxmark.getText().toString()));
		st.setPortion(portionId);
		st.setExtraPortion(extraPortion);
		st.setPortionName(portion.getText().toString().replace("\n", " "));
		st.setTestDate(otherdate);
		st.setSlipTestId(slipTestId);
		SlipTesttDao.editSlipTest(st, sqliteDatabase);

		//	int updatedSTAvg = PercentageSlipTest.findSlipTestPercentage(context, sectionId, subjectId, schoolId);
		//	sqlHandler.updateSlipTestAvg(sectionId, subjectId, updatedSTAvg, schoolId);

		ReplaceFragment.replace(new ViewScore(), getFragmentManager());
	}

	private void submitSlipTest2(){
		InputMethodManager imm = (InputMethodManager)context.getSystemService(
				Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(maxmark2.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(manualPortion.getWindowToken(), 0);	

		SlipTestt st = new SlipTestt();
		st.setMaximumMark(Integer.parseInt(maxmark2.getText().toString()));
		st.setPortion(portionId);
		st.setExtraPortion("0");
		st.setPortionName(manualPortion.getText().toString().replace("\n", " "));
		st.setTestDate(otherdate);
		st.setSlipTestId(slipTestId);
		SlipTesttDao.editSlipTest(st, sqliteDatabase);

		//	int updatedSTAvg = PercentageSlipTest.findSlipTestPercentage(context, sectionId, subjectId, schoolId);
		//	sqlHandler.updateSlipTestAvg(sectionId, subjectId, updatedSTAvg, schoolId);

		ReplaceFragment.replace(new ViewScore(), getFragmentManager());
	}

	public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {        
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		@Override
		public void onDateSet(DatePicker view, int year, int month, int day) {
			
			if(view.isShown()){
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
				Calendar cal = GregorianCalendar.getInstance();
				cal.set(year,month,day);
				Date d = cal.getTime();

				if(GregorianCalendar.getInstance().get(Calendar.YEAR)<cal.get(Calendar.YEAR)){
                    CommonDialogUtils.displayAlertWhiteDialog(act, "Selected future date !");
				}else if(GregorianCalendar.getInstance().get(Calendar.MONTH)<cal.get(Calendar.MONTH) && GregorianCalendar.getInstance().get(Calendar.YEAR)==cal.get(Calendar.YEAR)){
                    CommonDialogUtils.displayAlertWhiteDialog(act, "Selected future date !");
				}else if(GregorianCalendar.getInstance().get(Calendar.DAY_OF_MONTH)<cal.get(Calendar.DAY_OF_MONTH) && 
						GregorianCalendar.getInstance().get(Calendar.MONTH)<=cal.get(Calendar.MONTH) && GregorianCalendar.getInstance().get(Calendar.YEAR)==cal.get(Calendar.YEAR)){
                    CommonDialogUtils.displayAlertWhiteDialog(act, "Selected future date !");
				}else if(Calendar.SUNDAY==cal.get(Calendar.DAY_OF_WEEK)){
                    CommonDialogUtils.displayAlertWhiteDialog(act, "Sundays are not working days");
				}else{
					otherdate = dateFormat.format(d);
					dateFlag = true;
					stdate.performClick();
				}
			}
		}
	}

}
