package in.teacher.fragment;

import android.app.Fragment;
import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.security.NoSuchAlgorithmException;

import in.teacher.activity.R;
import in.teacher.dao.ClasDao;
import in.teacher.dao.SectionDao;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.CommonDialogUtils;
import in.teacher.util.PKGenerator;
import in.teacher.util.ReplaceFragment;

/**
 * Created by vinkrish on 26/10/15.
 */
public class StudentAdd extends Fragment {
    private SQLiteDatabase sqliteDatabase;
    private int schoolId, classId, sectionId, studentId;
    private EditText studentName, className, sectionName, rollNo, admissionNo;
    private EditText fatherName, motherName, dob, gender, mobile1, mobile2, address, pincode;
    private Button studentProfile, addStudent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.student_add, container, false);

        initView(view);
        init();

        return view;
    }

    private void initView(View view) {
        studentName = (EditText) view.findViewById(R.id.student_name);
        ((TextView) view.findViewById(R.id.name_tv)).setText(Html.fromHtml("Name <sup><small> * </small></sup>"));

        className = (EditText) view.findViewById(R.id.class_name);
        sectionName = (EditText) view.findViewById(R.id.section_name);

        rollNo = (EditText) view.findViewById(R.id.roll_no);
        ((TextView) view.findViewById(R.id.roll_tv)).setText(Html.fromHtml("Roll No <sup><small> * </small></sup>"));

        admissionNo = (EditText) view.findViewById(R.id.admission_no);

        fatherName = (EditText) view.findViewById(R.id.father_name);
        ((TextView) view.findViewById(R.id.father_tv)).setText(Html.fromHtml("Father's Name <sup><small> * </small></sup>"));

        motherName = (EditText) view.findViewById(R.id.mother_name);
        dob = (EditText) view.findViewById(R.id.dob);

        gender = (EditText) view.findViewById(R.id.gender);
        ((TextView) view.findViewById(R.id.gender_tv)).setText(Html.fromHtml("Gender <sup><small> * </small></sup>"));

        mobile1 = (EditText) view.findViewById(R.id.mobile1);
        ((TextView) view.findViewById(R.id.mobile1_tv)).setText(Html.fromHtml("Mobile <sup><small> * </small></sup>"));

        mobile2 = (EditText) view.findViewById(R.id.mobile2);
        address = (EditText) view.findViewById(R.id.address);
        pincode = (EditText) view.findViewById(R.id.pincode);

        studentProfile = (Button) view.findViewById(R.id.student_profile);
        addStudent = (Button) view.findViewById(R.id.add_student);
    }

    private void init() {
        sqliteDatabase = AppGlobal.getSqliteDatabase();

        Temp t = TempDao.selectTemp(sqliteDatabase);
        schoolId = t.getSchoolId();
        classId = t.getClassId();
        sectionId = t.getSectionId();

        className.setText(ClasDao.getClassName(classId, sqliteDatabase));
        sectionName.setText(SectionDao.getSectionName(sectionId, sqliteDatabase));

        studentProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReplaceFragment.replace(new StudentProfile(), getFragmentManager());
            }
        });

        addStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createStudent();
            }
        });
    }

    private void createStudent() {
        if (studentName.getText().toString().equals("") || fatherName.getText().toString().equals("") ||
                rollNo.getText().toString().equals("") || gender.getText().toString().equals("") ||
                mobile1.getText().toString().equals("")) {
            CommonDialogUtils.displayAlertWhiteDialog(getActivity(), "Fields marked * are mandatory");
        } else {
            Toast.makeText(getActivity(), "Student created", Toast.LENGTH_SHORT).show();
            try {
                studentId = PKGenerator.getMD5(schoolId, sectionId, studentName.getText().toString() + fatherName.getText().toString());
                String sql = "insert into students(StudentId, ClassId, SectionId, AdmissionNo, RollNoInClass, Name, " +
                        "FatherName, MotherName, DateOfBirth, Gender, Mobile1, Mobile2, Address, Pincode) values ("+studentId+", " +
                        classId+ ", "+sectionId+", '"+admissionNo.getText().toString()+"', "+rollNo.getText().toString()+", '" +
                        studentName.getText().toString()+"', '"+fatherName.getText().toString()+"', '"+motherName.getText().toString()+"', '" +
                        dob.getText().toString()+"', '"+gender.getText().toString()+"', '"+mobile1.getText().toString()+"', '" +
                        mobile2.getText().toString() + "', \""+address.getText().toString().replaceAll("\n", " ").replace("\"", "'")+"\", '"+
                        pincode.getText().toString()+"')";
                try {
                    sqliteDatabase.execSQL(sql);
                    ContentValues cv = new ContentValues();
                    cv.put("Query", sql);
                    sqliteDatabase.insert("uploadsql", null, cv);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            ReplaceFragment.replace(new StudentProfile(), getFragmentManager());
        }
    }
}
