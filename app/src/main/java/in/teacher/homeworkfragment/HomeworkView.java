package in.teacher.homeworkfragment;

import in.teacher.activity.R;
import in.teacher.adapter.HomeworkViewAdapter;
import in.teacher.dao.ClasDao;
import in.teacher.dao.HomeworkDao;
import in.teacher.dao.SectionDao;
import in.teacher.dao.TempDao;
import in.teacher.model.HW;
import in.teacher.sqlite.Homework;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.CommonDialogUtils;
import in.teacher.util.ReplaceFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by vinkrish.
 */
public class HomeworkView extends Fragment {
    private Activity act;
    private Context context;
    private SQLiteDatabase sqliteDatabase;
    private int sectionId, classId;
    private String className, sectionName;
    private String otherdate, hwDate;
    private boolean otherdayFlag;
    private ArrayList<Integer> subjectIdList = new ArrayList<>();
    private ArrayList<String> subjectNameList = new ArrayList<>();
    private List<String> hwMessage;
    private List<Integer> childList1 = new ArrayList<>();
    private boolean showAlert;
    private TextView hwTv;
    private ListView lv;
    private ArrayList<HW> hwList = new ArrayList<>();
    private HomeworkViewAdapter homeworkViewAdapter;
    private Button todayButton, yesterdayButton, otherdayButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.homework_view, container, false);
        sqliteDatabase = AppGlobal.getSqliteDatabase();

        Bundle b = getArguments();

        act = AppGlobal.getActivity();
        context = AppGlobal.getContext();
        lv = (ListView) view.findViewById(R.id.list);
        homeworkViewAdapter = new HomeworkViewAdapter(context, hwList);
        lv.setAdapter(homeworkViewAdapter);

        Temp t = TempDao.selectTemp(sqliteDatabase);
        classId = t.getClassId();
        sectionId = t.getSectionId();

        className = ClasDao.getClassName(classId, sqliteDatabase);
        sectionName = SectionDao.getSectionName(sectionId, sqliteDatabase);

        Cursor c = sqliteDatabase.rawQuery("select  A.SubjectId, B.SubjectName from subjectteacher A, subjects B where A.SectionId=" + sectionId + " and A.SubjectId=B.SubjectId", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            subjectIdList.add(c.getInt(c.getColumnIndex("SubjectId")));
            subjectNameList.add(c.getString(c.getColumnIndex("SubjectName")));
            c.moveToNext();
        }
        c.close();

        hwTv = (TextView) view.findViewById(R.id.hwPleaseTap);

        todayButton = (Button) view.findViewById(R.id.today);
        yesterdayButton = (Button) view.findViewById(R.id.yesterday);
        otherdayButton = (Button) view.findViewById(R.id.otherday);

        todayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                ReplaceFragment.replace(new InsertHomework(), getFragmentManager());
            }
        });

        yesterdayButton.setOnClickListener(yesterdayHomework);
        otherdayButton.setOnClickListener(otherdayHomework);

        int yesterday = b.getInt("yesterday");
        int otherday = b.getInt("otherday");
        if (yesterday == 1) {
            yesterdayButton.performClick();
        } else if (otherday == 1) {
            otherdayButton.performClick();
        }

        return view;
    }

    private View.OnClickListener yesterdayHomework = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            todayButton.setActivated(false);
            yesterdayButton.setActivated(true);
            otherdayButton.setActivated(false);
            hwDate = getYesterday();
            updateSubHeader();
            prepareListData();
            homeworkViewAdapter.notifyDataSetChanged();
            showAlertDuplicate();
        }
    };

    private View.OnClickListener otherdayHomework = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            todayButton.setActivated(false);
            yesterdayButton.setActivated(false);
            otherdayButton.setActivated(true);
            if (otherdayFlag) {
                otherdayFlag = false;
                prepareListData();
                homeworkViewAdapter.notifyDataSetChanged();
                showAlertDuplicate();
            } else {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "datePicker");
            }
        }
    };

    private void updateSubHeader() {
        if (classId == 0) {
            hwTv.setText("Not a class teacher.");
            lv.setVisibility(View.GONE);
        } else {
            hwTv.setText(className + "-" + sectionName + "  Homework asssigned on  " + hwDate);
        }
    }

    private String getToday() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date today = new Date();
        return dateFormat.format(today);
    }

    public void showAlertDuplicate() {
        if (showAlert) {
            Cursor cur = sqliteDatabase.rawQuery("select count(*) from homeworkmessage where SectionId='" + sectionId + "' and HomeworkDate='" + hwDate + "'", null);
            cur.moveToFirst();
            if (cur.getCount() > 0) {
                if (cur.getInt(0) > 1) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(act);
                    builder.setMessage("Homework has been entered in multiple tablets, any discrepancies will be rectified soon.");
                    builder.setTitle("Notification");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.show();
                }
            }
            cur.close();
            showAlert = false;
        } else {
            showAlert = true;
        }
    }

    public void prepareListData() {
        childList1.clear();
        List<Homework> listHW = HomeworkDao.selectHomework(sectionId, hwDate, sqliteDatabase);
        if (listHW.size() != 0) {
            extractHomework(listHW);
        } else {
            if (classId == 0) {
                hwTv.setText("Not a class teacher.");
                lv.setVisibility(View.GONE);
            } else {
                hwTv.setText(className + " - " + sectionName + " Homework is not assigned on " + hwDate);
            }
        }

        hwList.clear();
        int hwLoop = 0;
        for (Integer childList11 : childList1) {
            for (int loop = 0; loop < subjectIdList.size(); loop++) {
                if (childList11.equals(subjectIdList.get(loop))) {
                    hwList.add(new HW(subjectNameList.get(loop), hwMessage.get(hwLoop)));
                    break;
                }
            }
            hwLoop += 1;
        }
    }

    public void extractHomework(List<Homework> hwList) {
        hwMessage = new ArrayList<>();
        for (Homework hw : hwList) {
            String subjectIds = hw.getSubjectIDs();
            String splitBy = ",";
            String[] id = subjectIds.split(splitBy);
            for (String subjectId : id) {
                childList1.add(Integer.parseInt(subjectId));
            }
            String messageBody = hw.getHomework();
            String splitBy2 = "#";
            String[] message = messageBody.split(splitBy2);
            hwMessage.addAll(Arrays.asList(message));
        }
    }

    private String getYesterday() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = GregorianCalendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        Date yesterday = cal.getTime();
        return dateFormat.format(yesterday);
    }

    public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
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
            /*Date d = null;
            try {
				d = dateFormat.parse(new StringBuilder().append(year).append("-").append(month + 1).append("-").append(day).toString());
			} catch (ParseException e) {
				e.printStackTrace();
			}*/
            if (view.isShown()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Calendar cal = GregorianCalendar.getInstance();
                cal.set(year, month, day);
                Date d = cal.getTime();
                if (GregorianCalendar.getInstance().get(Calendar.YEAR) < cal.get(Calendar.YEAR)) {
                    CommonDialogUtils.displayAlertWhiteDialog(act, "Selected future date !");
                } else if (GregorianCalendar.getInstance().get(Calendar.MONTH) < cal.get(Calendar.MONTH) && GregorianCalendar.getInstance().get(Calendar.YEAR) == cal.get(Calendar.YEAR)) {
                    CommonDialogUtils.displayAlertWhiteDialog(act, "Selected future date !");
                } else if (GregorianCalendar.getInstance().get(Calendar.DAY_OF_MONTH) < cal.get(Calendar.DAY_OF_MONTH) &&
                        GregorianCalendar.getInstance().get(Calendar.MONTH) <= cal.get(Calendar.MONTH) && GregorianCalendar.getInstance().get(Calendar.YEAR) == cal.get(Calendar.YEAR)) {
                    CommonDialogUtils.displayAlertWhiteDialog(act, "Selected future date !");
                } else if (Calendar.SUNDAY == cal.get(Calendar.DAY_OF_WEEK)) {
                    CommonDialogUtils.displayAlertWhiteDialog(act, "Sundays are not working days");
                } else {
                    otherdate = dateFormat.format(d);
                    otherdayFlag = true;
                    hwDate = otherdate;
                    if (otherdate.equals(getToday())) {
                        ReplaceFragment.replace(new InsertHomework(), getFragmentManager());
                    } else {
                        //	nohw.setVisibility(View.GONE);
                        updateSubHeader();
                        otherdayButton.performClick();
                    }
                }
            }
        }
    }
}
