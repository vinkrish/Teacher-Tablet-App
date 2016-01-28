package in.teacher.fragment;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.teacher.activity.R;
import in.teacher.adapter.Capitalize;
import in.teacher.dao.TeacherDao;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.ReplaceFragment;

/**
 * Created by vinkrish on 19/10/15.
 * Don't expect comments explaining every piece of code, class and function names are self explanatory.
 */
public class TeacherInCharge extends Fragment {
    private Context context;
    private SQLiteDatabase sqliteDatabase;
    private TextView name;
    private SwitchCompat teacherIncharge;
    private int teacherId, classInchargeId, classInChargePos;
    private String teacherName;
    private Spinner classSpinner;
    private List<Integer> classInchargeList = new ArrayList<>();
    private List<String> classNameIncharge = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.teacher_incharge, container, false);

        name = (TextView) view.findViewById(R.id.teacherName);
        teacherIncharge = (SwitchCompat) view.findViewById(R.id.classIncharge);
        classSpinner = (Spinner) view.findViewById(R.id.classSpinner);

        init();

        view.findViewById(R.id.create_exam).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReplaceFragment.replace(new ExamCreate(), getFragmentManager());
            }
        });

        view.findViewById(R.id.update_exam).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReplaceFragment.replace(new ExamEdit(), getFragmentManager());
            }
        });

        view.findViewById(R.id.create_edit_act).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReplaceFragment.replace(new ActivityCreateEdit(), getFragmentManager());
            }
        });

        view.findViewById(R.id.create_edit_subact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReplaceFragment.replace(new SubActivityCreateEdit(), getFragmentManager());
            }
        });

        view.findViewById(R.id.copy_exam_structure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReplaceFragment.replace(new CopyExamStructure(), getFragmentManager());
            }
        });

        return view;

    }

    private void init() {
        context = AppGlobal.getContext();
        sqliteDatabase = AppGlobal.getSqliteDatabase();

        Temp t = TempDao.selectTemp(sqliteDatabase);
        teacherId = t.getTeacherId();
        classInchargeId = t.getClassInchargeId();

        teacherName = Capitalize.capitalThis((TeacherDao.selectTeacherName(teacherId, sqliteDatabase)));
        name.setText("[ " + teacherName + " ]");

        Cursor c = sqliteDatabase.rawQuery("select A.ClassId, B.ClassName from classteacher_incharge A, class B " +
                "where A.TeacherId = " + teacherId + " and B.ClassId = A.ClassId", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            classInchargeList.add(c.getInt(c.getColumnIndex("ClassId")));
            classNameIncharge.add(c.getString(c.getColumnIndex("ClassName")));
            c.moveToNext();
        }
        c.close();

        for (int i = 0; i < classInchargeList.size(); i++) {
            if (classInchargeList.get(i) == classInchargeId) {
                classInChargePos = i;
                break;
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_header, classNameIncharge);
        adapter.setDropDownViewResource(R.layout.spinner_droppeddown);
        classSpinner.setAdapter(adapter);

        classSpinner.setSelection(classInChargePos);

        classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TempDao.updateClassInchargeId(classInchargeList.get(position), sqliteDatabase);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        teacherIncharge.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    ReplaceFragment.replace(new Dashbord(), getFragmentManager());
                }
            }
        });

    }
}
