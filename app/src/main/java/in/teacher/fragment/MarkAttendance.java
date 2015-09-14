package in.teacher.fragment;

import in.teacher.activity.R;
import in.teacher.activity.R.animator;
import in.teacher.adapter.AttendanceAdapter;
import in.teacher.adapter.Capitalize;
import in.teacher.dao.ClasDao;
import in.teacher.dao.SectionDao;
import in.teacher.dao.StudentAttendanceDao;
import in.teacher.dao.StudentsDao;
import in.teacher.dao.TeacherDao;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.SqlDbHelper;
import in.teacher.sqlite.Students;
import in.teacher.sqlite.Temp;
import in.teacher.sqlite.TempAttendance;
import in.teacher.util.AppGlobal;
import in.teacher.util.CommonDialogUtils;
import in.teacher.util.ReplaceFragment;
import in.teacher.adapter.AttendanceAdapter.RecordHolder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by vinkrish.
 */

public class MarkAttendance extends Fragment {
    private List<Students> studentsArray = new ArrayList<>();
    private ArrayList<Students> studentsArrayGrid = new ArrayList<>();
    private List<Boolean> studentAttend = new ArrayList<>();
    private GridView gridView;
    private AttendanceAdapter attendanceAdapter;
    private Context context;
    private Activity act;
    private SqlDbHelper sqlHandler;
    private SQLiteDatabase sqliteDatabase;
    private int schoolId, classId, sectionId;
    private int index, absentCount;
    private TextView ptTV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mark_attendance, container, false);

        act = AppGlobal.getActivity();
        context = AppGlobal.getContext();
        sqlHandler = AppGlobal.getSqlDbHelper();
        sqliteDatabase = AppGlobal.getSqliteDatabase();
        sqlHandler.clearTempAttendance(sqliteDatabase);

        attendanceAdapter = new AttendanceAdapter(context, studentsArrayGrid);
        gridView = (GridView) view.findViewById(R.id.gridView);
        gridView.setAdapter(attendanceAdapter);

        ptTV = (TextView) view.findViewById(R.id.pleaseTap);

        Temp t = TempDao.selectTemp(sqliteDatabase);
        schoolId = t.getSchoolId();
        classId = t.getClassId();
        sectionId = t.getSectionId();

        populateGridArray();

        gridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view2, int position,
                                    long id) {
                Boolean b = studentAttend.get(position);
                if (!b) {
                    ImageView iV = ((RecordHolder) view2.getTag()).imageAttend;
                    iV.setImageResource(R.drawable.cross);
                    studentAttend.set(position, true);
                }
                if (b) {
                    ImageView iV = ((RecordHolder) view2.getTag()).imageAttend;
                    iV.setImageResource(R.drawable.tick);
                    studentAttend.set(position, false);
                }
                index = gridView.getFirstVisiblePosition();
                repopulateGridArray();
            }

        });

        view.findViewById(R.id.verify).setOnClickListener(verifyAbsentees);
        view.findViewById(R.id.noAbsentees).setOnClickListener(noAbsentees);
        view.findViewById(R.id.yesterday).setOnClickListener(yesterdayAbsentees);
        view.findViewById(R.id.otherday).setOnClickListener(otherdayAbsentees);

        return view;
    }

    private View.OnClickListener verifyAbsentees = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            sqlHandler.clearTempAttendance(sqliteDatabase);
            absentCount = 0;
            int pos = 0;
            for (Students s : studentsArray) {
                if (studentAttend.get(pos)) {
                    TempAttendance ta = new TempAttendance();
                    ta.setStudentId(s.getStudentId());
                    ta.setClassId(s.getClassId());
                    ta.setSectionId(s.getSectionId());
                    ta.setRollNoInClass(s.getRollNoInClass());
                    ta.setName(s.getName());
                    StudentAttendanceDao.insertTempAttendance(ta, sqliteDatabase);
                    absentCount += 1;
                }
                pos++;
            }
            if (absentCount > 0) {
                ReplaceFragment.replace(new VerifyAttendance(), getFragmentManager());
            } else {
                CommonDialogUtils.displayAlertWhiteDialog(act, "No Absentees are marked");
            }
        }
    };

    private View.OnClickListener noAbsentees = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(act);
            builder.setTitle("Notification");
            builder.setMessage("No absentees today..?");
            builder.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(context, "no absentees are marked", Toast.LENGTH_LONG).show();
                    StudentAttendanceDao.noAbsentAttendance(schoolId, classId, sectionId, getToday(), sqliteDatabase);
                    Bundle b = new Bundle();
                    b.putInt("today", 1);
                    b.putInt("yesterday", 0);
                    b.putInt("otherday", 0);
                    Fragment fragment = new AbsentList();
                    fragment.setArguments(b);
                    getFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(animator.fade_in, animator.fade_out)
                            .replace(R.id.content_frame, fragment).addToBackStack(null).commit();
                }
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        }
    };

    private View.OnClickListener yesterdayAbsentees = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Bundle b = new Bundle();
            b.putInt("today", 0);
            b.putInt("yesterday", 1);
            b.putInt("otherday", 0);
            Fragment fragment = new AbsentList();
            fragment.setArguments(b);
            getFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(animator.fade_in, animator.fade_out)
                    .replace(R.id.content_frame, fragment).addToBackStack(null).commit();
        }
    };

    private View.OnClickListener otherdayAbsentees = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Bundle b = new Bundle();
            b.putInt("today", 0);
            b.putInt("yesterday", 0);
            b.putInt("otherday", 1);
            Fragment fragment = new AbsentList();
            fragment.setArguments(b);
            getFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(animator.fade_in, animator.fade_out)
                    .replace(R.id.content_frame, fragment).addToBackStack(null).commit();
        }
    };

    private String getToday() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date today = new Date();
        return dateFormat.format(today);
    }

    private void populateGridArray() {
        if (classId != 0) {
            String clasName = ClasDao.getClassName(classId, sqliteDatabase);
            String secName = SectionDao.getSectionName(sectionId, sqliteDatabase);
            ptTV.setText(clasName + " - " + secName + "  " + getResources().getString(R.string.pt));
        }
        studentsArray = StudentsDao.selectStudents(sectionId, sqliteDatabase);
        for (int idx = 0; idx < studentsArray.size(); idx++) {
            studentAttend.add(false);
        }

        Bitmap attendYes = BitmapFactory.decodeResource(this.getResources(), R.drawable.tick);
        Bitmap attendNo = BitmapFactory.decodeResource(this.getResources(), R.drawable.cross);
        studentsArrayGrid.clear();
        int pos = 0;
        for (Students s : studentsArray) {
            if (studentAttend.get(pos)) {
                studentsArrayGrid.add(new Students(s.getRollNoInClass(), Capitalize.capitalThis(s.getName()), attendNo));
            } else {
                studentsArrayGrid.add(new Students(s.getRollNoInClass(), Capitalize.capitalThis(s.getName()), attendYes));
            }
            pos++;
        }
        attendanceAdapter.notifyDataSetChanged();
    }

    private void repopulateGridArray() {
        Bitmap attendYes = BitmapFactory.decodeResource(this.getResources(), R.drawable.tick);
        Bitmap attendNo = BitmapFactory.decodeResource(this.getResources(), R.drawable.cross);
        studentsArrayGrid.clear();
        int pos = 0;
        for (Students s : studentsArray) {
            if (studentAttend.get(pos)) {
                studentsArrayGrid.add(new Students(s.getRollNoInClass(), Capitalize.capitalThis(s.getName()), attendNo));
            } else {
                studentsArrayGrid.add(new Students(s.getRollNoInClass(), Capitalize.capitalThis(s.getName()), attendYes));
            }
            pos++;
        }
        attendanceAdapter.notifyDataSetChanged();
        gridView.setSelection(index);
    }

}
