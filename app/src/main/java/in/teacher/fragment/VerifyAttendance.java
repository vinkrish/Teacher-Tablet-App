package in.teacher.fragment;

import in.teacher.activity.R;
import in.teacher.activity.R.animator;
import in.teacher.adapter.AttendanceAdapter;
import in.teacher.adapter.Capitalize;
import in.teacher.adapter.StudentsSort;
import in.teacher.dao.StudentAttendanceDao;
import in.teacher.dao.TeacherDao;
import in.teacher.dao.TempDao;
import in.teacher.dao.UploadSqlDao;
import in.teacher.sqlite.StudentAttendance;
import in.teacher.sqlite.Students;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.ReplaceFragment;
import in.teacher.adapter.AttendanceAdapter.RecordHolder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
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
import android.widget.Toast;

/**
 * Created by vinkrish.
 */
public class VerifyAttendance extends Fragment {
    private List<Students> studentsArray = new ArrayList<>();
    private ArrayList<Students> studentsArrayGrid = new ArrayList<>();
    private List<Boolean> studentAttend = new ArrayList<>();
    private GridView gridView;
    private AttendanceAdapter attendanceAdapter;
    private Context context;
    private SQLiteDatabase sqliteDatabase;
    private int index, schoolId, sectionId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.verify_attendance, container, false);
        gridView = (GridView) view.findViewById(R.id.gridView);

        init();

        gridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Boolean b = studentAttend.get(position);
                if (!b) {
                    ImageView iV = ((RecordHolder) view.getTag()).imageAttend;
                    iV.setImageResource(R.drawable.cross);
                    studentAttend.set(position, true);
                }
                if (b) {
                    ImageView iV = ((RecordHolder) view.getTag()).imageAttend;
                    iV.setImageResource(R.drawable.tick);
                    studentAttend.set(position, false);
                }
                index = gridView.getFirstVisiblePosition();
                repopulateGridArray();
            }

        });

        view.findViewById(R.id.submit).setOnClickListener(submitAttendance);

        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                ReplaceFragment.replace(new MarkAttendance(), getFragmentManager());
            }
        });

        return view;
    }

    private void init() {
        context = AppGlobal.getContext();
        sqliteDatabase = AppGlobal.getSqliteDatabase();

        attendanceAdapter = new AttendanceAdapter(context, studentsArrayGrid);
        gridView.setAdapter(attendanceAdapter);

        Temp t = TempDao.selectTemp(sqliteDatabase);
        schoolId = t.getSchoolId();
        sectionId = t.getSectionId();

        studentsArray = StudentAttendanceDao.selectTempAttendance(sqliteDatabase);
        Collections.sort(studentsArray, new StudentsSort());

        populateGridArray();
    }

    private View.OnClickListener submitAttendance = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int pos = 0;
            String sql = "delete from studentattendance where SectionId=" + sectionId + " and DateAttendance='" + getToday() + "'";
            sqliteDatabase.execSQL(sql);
            ContentValues cv = new ContentValues();
            cv.put("Query", sql);
            sqliteDatabase.insert("uploadsql", null, cv);
            for (Students s : studentsArray) {
                if (studentAttend.get(pos)) {
                    StudentAttendance sa = new StudentAttendance();
                    sa.setSchoolId(schoolId);
                    sa.setClassId(s.getClassId());
                    sa.setSectionId(s.getSectionId());
                    sa.setStudentId(s.getStudentId());
                    sa.setTypeOfLeave("A");
                    sa.setDateAttendance(getToday());
                    UploadSqlDao.insertStudentAttendance(sa, s.getSectionId(), getToday(), sqliteDatabase);
                }
                pos++;
            }
            Toast.makeText(context, "attendance has been updated successfully", Toast.LENGTH_LONG).show();

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
    };

    private String getToday() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date today = new Date();
        return dateFormat.format(today);
    }

    private void populateGridArray() {
        Bitmap attendNo = BitmapFactory.decodeResource(this.getResources(), R.drawable.cross);

        for (Students s : studentsArray) {
            studentsArrayGrid.add(new Students(s.getRollNoInClass(), Capitalize.capitalThis(s.getName()), attendNo));
            studentAttend.add(true);
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
