package in.teacher.sectionincharge;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
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

import in.teacher.activity.R;
import in.teacher.dao.ClasDao;
import in.teacher.dao.SectionDao;
import in.teacher.dao.TempDao;
import in.teacher.sectionincharge.StudentProfile;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.CommonDialogUtils;
import in.teacher.util.ReplaceFragment;

/**
 * Created by vinkrish on 21/10/15.
 */
public class StudentProfileEdit extends Fragment {
    private SQLiteDatabase sqliteDatabase;
    private TextView studentName;
    private EditText className, sectionName, rollNo, admissionNo;
    private EditText fatherName, motherName, dob, gender, mobile1, mobile2, address, pincode;
    private Button studentProfile, saveBtn, deleteBtn;
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

        ((TextView) view.findViewById(R.id.roll_tv)).setText(Html.fromHtml("Roll No <sup><small> * </small></sup>"));
        ((TextView) view.findViewById(R.id.father_tv)).setText(Html.fromHtml("Father's Name <sup><small> * </small></sup>"));
        ((TextView) view.findViewById(R.id.gender_tv)).setText(Html.fromHtml("Gender <sup><small> * </small></sup>"));
        ((TextView) view.findViewById(R.id.mobile1_tv)).setText(Html.fromHtml("Mobile <sup><small> * </small></sup>"));

        studentProfile = (Button) view.findViewById(R.id.student_profile);
        saveBtn = (Button) view.findViewById(R.id.save_butt);
        deleteBtn = (Button) view.findViewById(R.id.delete_butt);
    }

    private void init() {
        sqliteDatabase = AppGlobal.getSqliteDatabase();

        Temp t = TempDao.selectTemp(sqliteDatabase);
        studentId = t.getStudentId();
        classId = t.getClassId();
        sectionId = t.getSectionId();

        className.setText(ClasDao.getClassName(classId, sqliteDatabase));
        sectionName.setText(SectionDao.getSectionName(sectionId, sqliteDatabase));

        Cursor c = sqliteDatabase.rawQuery("select * from students where StudentId = " + studentId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
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

        studentProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReplaceFragment.replace(new StudentProfile(), getFragmentManager());
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (fatherName.getText().toString().equals("") ||
                        rollNo.getText().toString().equals("") ||
                        gender.getText().toString().equals("") ||
                        mobile1.getText().toString().equals("")) {
                    CommonDialogUtils.displayAlertWhiteDialog(getActivity(), "Fields marked * are mandatory");
                } else {
                    Toast.makeText(getActivity(), "Saved Changes", Toast.LENGTH_SHORT).show();
                    updateStudent();
                    ReplaceFragment.replace(new StudentProfile(), getFragmentManager());
                }
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteStudent();
            }
        });
    }

    private void updateStudent() {
        String sql = "update students set FatherName = '" + fatherName.getText().toString().replaceAll("\n", " ") + "' and " +
                "RollNoInClass =  " + rollNo.getText().toString() + " and " +
                "AdmissionNo = '" + admissionNo.getText().toString().replaceAll("\n", " ") + "' and " +
                "MotherName = '" + motherName.getText().toString().replaceAll("\n", " ") + "' and " +
                "DateOfBirth = '" + dob.getText().toString() + "' and " +
                "gender = '" + gender.getText().toString() + "' and " +
                "mobile1 = '" + mobile1.getText().toString() + "' and " +
                "mobile2 = '" + mobile2.getText().toString() + "' and " +
                "address = \"" + address.getText().toString().replaceAll("\n", " ").replace("\"", "'") + "\" and " +
                "pincode = '" + pincode.getText().toString() + "' where StudentId = " + studentId;
        try {
            sqliteDatabase.execSQL(sql);
            ContentValues cv = new ContentValues();
            cv.put("Query", sql);
            sqliteDatabase.insert("uploadsql", null, cv);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteStudent() {
        AlertDialog.Builder submitBuilder = new AlertDialog.Builder(getActivity());
        submitBuilder.setCancelable(false);
        submitBuilder.setTitle("Confirm your action");
        submitBuilder.setMessage("Do you want to delete student");
        submitBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                dialog.cancel();
            }
        });
        submitBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                deleteConfirm();
            }
        });
        submitBuilder.show();
    }

    private void deleteConfirm() {
        String sql = "delete from students where StudentId = " + studentId;
        try {
            sqliteDatabase.execSQL(sql);
            ContentValues cv = new ContentValues();
            cv.put("Query", sql);
            sqliteDatabase.insert("uploadsql", null, cv);
        } catch (SQLException e) {
        }

        ReplaceFragment.replace(new StudentProfile(), getFragmentManager());
    }
}
