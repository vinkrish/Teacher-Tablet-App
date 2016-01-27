package in.teacher.sectionincharge;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import in.teacher.activity.R;
import in.teacher.dao.ClasDao;
import in.teacher.dao.SectionDao;
import in.teacher.dao.StudentsDao;
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
    private static TextView dob;
    private EditText studentName, className, sectionName, rollNo, admissionNo;
    private EditText fatherName, motherName, gender, mobile1, mobile2, address, pincode;
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

        dob = (TextView) view.findViewById(R.id.dob);
        ((TextView) view.findViewById(R.id.dob_tv)).setText(Html.fromHtml("Date Of Birth <sup><small> * </small></sup>"));

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

        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });
    }

    private void createStudent() {
        if (studentName.getText().toString().equals("") || fatherName.getText().toString().equals("") ||
                rollNo.getText().toString().equals("") || gender.getText().toString().equals("") ||
                mobile1.getText().toString().equals("") || dob.getText().toString().equals("")) {
            CommonDialogUtils.displayAlertWhiteDialog(getActivity(), "Fields marked * are mandatory");
        } else if (StudentsDao.isRollNoExist(sqliteDatabase, sectionId, Integer.parseInt(rollNo.getText().toString()))) {
            CommonDialogUtils.displayAlertWhiteDialog(getActivity(), "Roll number is not unique");
        } else if (mobile1.getText().toString().length() > 10) {
            CommonDialogUtils.displayAlertWhiteDialog(getActivity(), "Mobile number should be of 10 digits");
        } else {
            Toast.makeText(getActivity(), "Student created", Toast.LENGTH_SHORT).show();
            try {
                studentId = PKGenerator.generateExamId(schoolId, sectionId, studentName.getText().toString() + fatherName.getText().toString());
                String sql = "insert into students(StudentId, SchoolId , ClassId, SectionId, SubjectIds, AdmissionNo, RollNoInClass, Username, Password, Name, " +
                        "FatherName, MotherName, DateOfBirth, Gender, Mobile1, Mobile2, Address, Pincode) values (" + studentId + ", " +
                        schoolId + ", " + classId + ", " + sectionId + ", '','" + admissionNo.getText().toString() + "', " + rollNo.getText().toString() + ", 'S" +
                        mobile1.getText().toString() + "','S" + mobile1.getText().toString() + "','" +
                        studentName.getText().toString() + "', '" + fatherName.getText().toString() + "', '" + motherName.getText().toString() + "', '" +
                        dob.getText().toString() + "', '" + gender.getText().toString() + "', '" + mobile1.getText().toString() + "', '" +
                        mobile2.getText().toString() + "', \"" + address.getText().toString().replaceAll("\n", " ").replace("\"", "'") + "\", '" +
                        pincode.getText().toString() + "')";
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
            if (view.isShown()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                Calendar cal = GregorianCalendar.getInstance();
                cal.set(year, month, day);
                Date d = cal.getTime();

                if (GregorianCalendar.getInstance().get(Calendar.YEAR) < cal.get(Calendar.YEAR)) {
                    CommonDialogUtils.displayAlertWhiteDialog(getActivity(), "Selected future date !");
                } else if (GregorianCalendar.getInstance().get(Calendar.MONTH) < cal.get(Calendar.MONTH) &&
                        GregorianCalendar.getInstance().get(Calendar.YEAR) == cal.get(Calendar.YEAR)) {
                    CommonDialogUtils.displayAlertWhiteDialog(getActivity(), "Selected future date !");
                } else if (GregorianCalendar.getInstance().get(Calendar.DAY_OF_MONTH) < cal.get(Calendar.DAY_OF_MONTH) &&
                        GregorianCalendar.getInstance().get(Calendar.MONTH) <= cal.get(Calendar.MONTH) &&
                        GregorianCalendar.getInstance().get(Calendar.YEAR) == cal.get(Calendar.YEAR)) {
                    CommonDialogUtils.displayAlertWhiteDialog(getActivity(), "Selected future date !");
                } else {
                    dob.setText(dateFormat.format(d));
                }
            }
        }
    }
}
