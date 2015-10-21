package in.teacher.fragment;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import in.teacher.activity.R;
import in.teacher.dao.ClasDao;
import in.teacher.dao.SectionDao;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;

/**
 * Created by vinkrish on 21/10/15.
 */
public class StudentProfileEdit extends Fragment {
    private TextView studentName;
    private EditText className, sectionName, rollNo, admissionNo;
    private EditText fatherName, motherName, dob, gender, mobile1, mobile2, address, pincode;
    private Button saveBtn, deleteBtn;
    private Context context;
    private SQLiteDatabase sqliteDatabase;
    private int studentId, classId, sectionId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.student_profile_edit, container, false);

        initView(view);
        init();

        return view;
    }

    private void initView(View view) {
        studentName = (TextView) view.findViewById(R.id.student_name);

        className = (EditText) view.findViewById(R.id.class_name);
        sectionName = (EditText) view.findViewById(R.id.section_name);
        rollNo = (EditText) view.findViewById(R.id.roll_no);
        admissionNo = (EditText) view.findViewById(R.id.admission_no);
        fatherName = (EditText) view.findViewById(R.id.father_name);
        motherName = (EditText) view.findViewById(R.id.mother_name);
        dob = (EditText) view.findViewById(R.id.dob);
        gender = (EditText) view.findViewById(R.id.gender);
        mobile1 = (EditText) view.findViewById(R.id.mobile1);
        mobile2 = (EditText) view.findViewById(R.id.mobile2);
        address = (EditText) view.findViewById(R.id.address);
        pincode = (EditText) view.findViewById(R.id.pincode);

        saveBtn = (Button) view.findViewById(R.id.save_butt);
        deleteBtn = (Button) view.findViewById(R.id.delete_butt);
    }

    private void init(){
        context = AppGlobal.getContext();
        sqliteDatabase = AppGlobal.getSqliteDatabase();

        Temp t = TempDao.selectTemp(sqliteDatabase);
        studentId = t.getStudentId();
        classId = t.getClassId();
        sectionId = t.getSectionId();

        className.setText(ClasDao.getClassName(classId, sqliteDatabase));
        sectionName.setText(SectionDao.getSectionName(sectionId, sqliteDatabase));

        Cursor c = sqliteDatabase.rawQuery("select * from students where StudentId = " + studentId, null);
        c.moveToFirst();
        while(!c.isAfterLast()){
            studentName.setText(c.getString(c.getColumnIndex("Name")));
            rollNo.setText(c.getString(c.getColumnIndex("RollNoInClass")));
            admissionNo.setText(c.getString(c.getColumnIndex("AdmissionNo")));
            fatherName.setText(c.getString(c.getColumnIndex("FatherName")));
            motherName.setText(c.getString(c.getColumnIndex("MotherName")));
            dob.setText(c.getString(c.getColumnIndex("DateOfBirth")));
            gender.setText(c.getString(c.getColumnIndex("Gender")));
            mobile1.setText(c.getString(c.getColumnIndex("Mobile1")));
            mobile2.setText(c.getString(c.getColumnIndex("Mobile2")));
            address.setText(c.getString(c.getColumnIndex("Address")));
            pincode.setText(c.getString(c.getColumnIndex("Pincode")));

            c.moveToNext();
        }
        c.close();
    }
}
