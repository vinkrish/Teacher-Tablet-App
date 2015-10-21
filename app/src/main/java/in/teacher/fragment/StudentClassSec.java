package in.teacher.fragment;

import android.app.Fragment;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.teacher.activity.R;
import in.teacher.adapter.StudentClassSecAdapter;
import in.teacher.adapter.Capitalize;
import in.teacher.dao.ClasDao;
import in.teacher.dao.SectionDao;
import in.teacher.dao.StudentsDao;
import in.teacher.dao.SubjectExamsDao;
import in.teacher.dao.TeacherDao;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.CommonObject;
import in.teacher.sqlite.Students;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;

/**
 * Created by vinkrish.
 */
public class StudentClassSec extends Fragment {
    private Context context;
    private SQLiteDatabase sqliteDatabase;
    private String subjectName, teacherName;
    private int classId, sectionId, subjectId, teacherId;
    private Button name;
    private TextView clasSecSubTv;
    private ArrayList<CommonObject> commonObjectList = new ArrayList<>();
    private List<Integer> studentIdList = new ArrayList<>();
    private List<Integer> studIDList = new ArrayList<>();
    private List<String> studentNameList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.student_class_sec, container, false);
        context = AppGlobal.getContext();
        sqliteDatabase = AppGlobal.getSqliteDatabase();

        name = (Button) view.findViewById(R.id.teacherName);
        clasSecSubTv = (TextView) view.findViewById(R.id.headerClasSecSub);

        clearList();

        Temp t = TempDao.selectTemp(sqliteDatabase);
        classId = t.getCurrentClass();
        sectionId = t.getCurrentSection();
        subjectId = t.getCurrentSubject();
        teacherId = t.getTeacherId();
        subjectName = SubjectExamsDao.selectSubjectName(subjectId, sqliteDatabase);

        String className = ClasDao.getClassName(classId, sqliteDatabase);
        String sectionName = SectionDao.getSectionName(sectionId, sqliteDatabase);
        teacherName = Capitalize.capitalThis((TeacherDao.selectTeacherName(teacherId, sqliteDatabase)));
        StringBuilder exmName = new StringBuilder();
        exmName.append(className).append("-").append(sectionName).append("    ").append(subjectName);
        if (teacherName.length() > 11) {
            name.setText(teacherName.substring(0, 9) + "...");
        } else {
            name.setText(teacherName);
        }
        clasSecSubTv.setText(exmName);

        updateListView();

        ListView lv = (ListView) view.findViewById(R.id.listView);
        StudentClassSecAdapter studentClassSecAdapter = new StudentClassSecAdapter(context, R.layout.asec_list, commonObjectList);
        lv.setAdapter(studentClassSecAdapter);

        return view;
    }

    private void updateListView() {
        List<Students> studentList = StudentsDao.selectStudents2(sectionId, subjectId, sqliteDatabase);
        for (Students s : studentList) {
            studIDList.add(s.getStudentId());
            studentIdList.add(s.getRollNoInClass());
            studentNameList.add(s.getName());
        }
        commonObjectList.clear();
        for (int i = 0; i < studentList.size(); i++) {
            commonObjectList.add(new CommonObject(studentIdList.get(i) + "", studentNameList.get(i)));
        }
    }

    private void clearList() {
        studentIdList.clear();
        studentNameList.clear();
        studIDList.clear();
    }

}
