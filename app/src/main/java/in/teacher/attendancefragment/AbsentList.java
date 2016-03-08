package in.teacher.attendancefragment;

import in.teacher.activity.R;
import in.teacher.adapter.AttendanceAdapter;
import in.teacher.adapter.Capitalize;
import in.teacher.util.StudentsSort;
import in.teacher.dao.ClasDao;
import in.teacher.dao.SectionDao;
import in.teacher.dao.StudentAttendanceDao;
import in.teacher.dao.StudentsDao;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.Students;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.CommonDialogUtils;
import in.teacher.util.ReplaceFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.TextView;

/**
 * Created by vinkrish.
 * Don't expect comments explaining every piece of code, class and function names are self explanatory.
 */
public class AbsentList extends Fragment {
    private Context context;
    private List<Students> studentsArray = new ArrayList<>();
    private ArrayList<Students> studentsArrayGrid = new ArrayList<>();
    private AttendanceAdapter attendanceAdapter;
    private static Activity act;
    private SQLiteDatabase sqliteDatabase;
    private int classId, sectionId;
    private static String otherdate;
    private static boolean absentListFlag;
    private static Button otherdayButton, yesterdayButton, todayButton;
    private TextView noAbsentees, ptTV;
    private GridView gridView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.absent_list, container, false);
        initView(view);
        init();

        return view;
    }

    private void initView(View view){
        noAbsentees = (TextView) view.findViewById(R.id.noAbsentee);
        todayButton = (Button) view.findViewById(R.id.today);
        yesterdayButton = (Button) view.findViewById(R.id.yesterday);
        otherdayButton = (Button) view.findViewById(R.id.otherday);
        ptTV = (TextView) view.findViewById(R.id.absentList);
        gridView = (GridView) view.findViewById(R.id.gridView);
    }

    private void init() {
        act = AppGlobal.getActivity();
        context = AppGlobal.getContext();
        sqliteDatabase = AppGlobal.getSqliteDatabase();

        CommonDialogUtils.hideKeyboard(getActivity());

        Temp t = TempDao.selectTemp(sqliteDatabase);
        classId = t.getClassId();
        sectionId = t.getSectionId();

        todayButton.setOnClickListener(todayAbsentees);
        yesterdayButton.setOnClickListener(yesterdayAbsentees);
        otherdayButton.setOnClickListener(otherdayAbsentees);

        if (classId == 0) {
            ptTV.setText("Not a class teacher.");
        }

        attendanceAdapter = new AttendanceAdapter(context, studentsArrayGrid);
        gridView.setAdapter(attendanceAdapter);

        Bundle b = getArguments();
        int today = b.getInt("today");
        int yesterday = b.getInt("yesterday");
        int otherday = b.getInt("otherday");
        if (today == 1) {
            todayButton.performClick();
        } else if (yesterday == 1) {
            yesterdayButton.performClick();
        } else if (otherday == 1) {
            otherdayButton.performClick();
        }
    }

    private View.OnClickListener todayAbsentees = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            noAbsentees.setVisibility(View.GONE);
            todayButton.setActivated(true);
            yesterdayButton.setActivated(false);
            otherdayButton.setActivated(false);
            studentsArray.clear();
            boolean flag = false;
            int marked = StudentAttendanceDao.isStudentAttendanceMarked(sectionId, getDate(), sqliteDatabase);
            if (marked == 1) {
                flag = true;
            }
            if (flag) {
                String today = getToday();
                //	List<Integer> studentIdList = sqlHandler.selectStudentIds(today, sectionId);
                List<Long> studentIdList = StudentAttendanceDao.selectStudentIds(today, sectionId, sqliteDatabase);
                if (!studentIdList.isEmpty()) {
                    studentsArray = StudentsDao.selectAbsentStudents(studentIdList, sqliteDatabase);
                    Collections.sort(studentsArray, new StudentsSort());
                } else {
                    noAbsentees.setText("No Absentees");
                    noAbsentees.setVisibility(View.VISIBLE);
                }
                populateGridArray();
            } else {
                ReplaceFragment.replace(new MarkAttendance(), getFragmentManager());
            }
        }
    };

    private View.OnClickListener yesterdayAbsentees = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            noAbsentees.setVisibility(View.GONE);
            todayButton.setActivated(false);
            yesterdayButton.setActivated(true);
            otherdayButton.setActivated(false);
            studentsArray.clear();
            String yesterday = getYesterday();

            boolean flag = false;
            int marked = StudentAttendanceDao.isStudentAttendanceMarked(sectionId, yesterday, sqliteDatabase);
            if (marked == 1) {
                flag = true;
            }
            if (flag) {
                List<Long> studentIdList = StudentAttendanceDao.selectStudentIds(yesterday, sectionId, sqliteDatabase);
                if (!studentIdList.isEmpty()) {
                    studentsArray = StudentsDao.selectAbsentStudents(studentIdList, sqliteDatabase);
                    Collections.sort(studentsArray, new StudentsSort());
                } else {
                    noAbsentees.setVisibility(View.VISIBLE);
                }
            } else {
                noAbsentees.setText("Attendance is not taken for this day");
                noAbsentees.setVisibility(View.VISIBLE);
            }
            populateGridArray();
        }
    };

    private View.OnClickListener otherdayAbsentees = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            noAbsentees.setVisibility(View.GONE);
            todayButton.setActivated(false);
            yesterdayButton.setActivated(false);
            otherdayButton.setActivated(true);
            studentsArray.clear();
            if (absentListFlag) {
                boolean flag = false;
                int marked = StudentAttendanceDao.isStudentAttendanceMarked(sectionId, otherdate, sqliteDatabase);
                if (marked == 1) {
                    flag = true;
                }
                if (flag) {
                    List<Long> studentIdList = StudentAttendanceDao.selectStudentIds(otherdate, sectionId, sqliteDatabase);
                    if (!studentIdList.isEmpty()) {
                        studentsArray = StudentsDao.selectAbsentStudents(studentIdList, sqliteDatabase);
                        Collections.sort(studentsArray, new StudentsSort());
                    } else {
                        noAbsentees.setVisibility(View.VISIBLE);
                    }
                } else {
                    noAbsentees.setText("Attendance is not taken for this day");
                    noAbsentees.setVisibility(View.VISIBLE);
                }
                populateGridArray();
            } else {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "datePicker");
            }
        }
    };


    private String getYesterday() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = GregorianCalendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        Date yesterday = cal.getTime();
        return dateFormat.format(yesterday);
    }

    private String getToday() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date today = new Date();
        return dateFormat.format(today);
    }

    private void populateGridArray() {
        if (classId != 0) {
            String clasName = ClasDao.getClassName(classId, sqliteDatabase);
            String secName = SectionDao.getSectionName(sectionId, sqliteDatabase);
            ptTV.setText(clasName + " - " + secName + "  " + getResources().getString(R.string.al));
        }
        absentListFlag = false;
        studentsArrayGrid.clear();
        Bitmap attendYes = BitmapFactory.decodeResource(this.getResources(), R.drawable.cross);
        for (Students s : studentsArray) {
            studentsArrayGrid.add(new Students(s.getRollNoInClass(), Capitalize.capitalThis(s.getName()), attendYes));
        }
        attendanceAdapter.notifyDataSetChanged();
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
                } else if (GregorianCalendar.getInstance().get(Calendar.MONTH) < cal.get(Calendar.MONTH)
                        && GregorianCalendar.getInstance().get(Calendar.YEAR) == cal.get(Calendar.YEAR)) {
                    CommonDialogUtils.displayAlertWhiteDialog(act, "Selected future date !");
                } else if (GregorianCalendar.getInstance().get(Calendar.DAY_OF_MONTH) < cal.get(Calendar.DAY_OF_MONTH) &&
                        GregorianCalendar.getInstance().get(Calendar.MONTH) <= cal.get(Calendar.MONTH)
                        && GregorianCalendar.getInstance().get(Calendar.YEAR) == cal.get(Calendar.YEAR)) {
                    CommonDialogUtils.displayAlertWhiteDialog(act, "Selected future date !");
                } else if (Calendar.SUNDAY == cal.get(Calendar.DAY_OF_WEEK)) {
                    CommonDialogUtils.displayAlertWhiteDialog(act, "Sundays are not working days");
                } else {
                    otherdate = dateFormat.format(d);
                    absentListFlag = true;
                    otherdayButton.performClick();
                }
            }
        }
    }

    private String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

}
