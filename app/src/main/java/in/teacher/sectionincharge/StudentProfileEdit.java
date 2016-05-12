package in.teacher.sectionincharge;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Arrays;
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
import in.teacher.util.ReplaceFragment;

/**
 * Created by vinkrish on 21/10/15.
 * Don't expect comments explaining every piece of code, class and function names are self explanatory.
 */
public class StudentProfileEdit extends Fragment {
    private SQLiteDatabase sqliteDatabase;
    private static TextView studentName, dob;
    private String gender;
    private EditText className, sectionName, rollNo, admissionNo;
    private EditText fatherName, motherName, mobile1, mobile2, address, pincode;
    private Button studentProfile, saveBtn, deleteBtn;
    private int classId, sectionId;
    private long studentId;
    private Spinner genderSpinner;

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
        dob = (TextView) view.findViewById(R.id.dob);

        className = (EditText) view.findViewById(R.id.class_name);
        sectionName = (EditText) view.findViewById(R.id.section_name);
        rollNo = (EditText) view.findViewById(R.id.roll_no);
        admissionNo = (EditText) view.findViewById(R.id.admission_no);
        fatherName = (EditText) view.findViewById(R.id.father_name);
        motherName = (EditText) view.findViewById(R.id.mother_name);
        genderSpinner = (Spinner) view.findViewById(R.id.gender);
        mobile1 = (EditText) view.findViewById(R.id.mobile1);
        mobile2 = (EditText) view.findViewById(R.id.mobile2);
        address = (EditText) view.findViewById(R.id.address);
        pincode = (EditText) view.findViewById(R.id.pincode);

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
        className.setKeyListener(null);
        sectionName.setText(SectionDao.getSectionName(sectionId, sqliteDatabase));
        sectionName.setKeyListener(null);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_header, Arrays.asList(new String[]{"Gender *", "Male", "Female"}));
        adapter.setDropDownViewResource(R.layout.spinner_droppeddown);
        genderSpinner.setAdapter(adapter);

        Cursor c = sqliteDatabase.rawQuery("select * from students where StudentId = " + studentId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            studentName.setText(c.getString(c.getColumnIndex("Name")));
            rollNo.setText(c.getString(c.getColumnIndex("RollNoInClass")));
            admissionNo.setText(c.getString(c.getColumnIndex("AdmissionNo")));
            fatherName.setText(c.getString(c.getColumnIndex("FatherName")));
            motherName.setText(c.getString(c.getColumnIndex("MotherName")));
            dob.setText(c.getString(c.getColumnIndex("DateOfBirth")));
            gender = c.getString(c.getColumnIndex("Gender"));
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
                        gender.equals("") ||
                        mobile1.getText().toString().equals("") ||
                        dob.getText().toString().equals("")) {
                    CommonDialogUtils.displayAlertWhiteDialog(getActivity(), "Fields marked * are mandatory");
                } else if (StudentsDao.isRollNoAvailable(sqliteDatabase, sectionId, Integer.parseInt(rollNo.getText().toString()), studentId)) {
                    CommonDialogUtils.displayAlertWhiteDialog(getActivity(), "Roll number is not unique");
                } else if (mobile1.getText().toString().length() > 10) {
                    CommonDialogUtils.displayAlertWhiteDialog(getActivity(), "Mobile number should be of 10 digits");
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

        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });

        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) gender = "Male";
                else if (position == 2) gender = "Female";
                else gender = "";
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        try{
            if (gender.equals("")) genderSpinner.setSelection(0, false);
            else if (gender.equals("Male") || gender.equalsIgnoreCase("m")) genderSpinner.setSelection(1);
            else if (gender.equals("Female") || gender.equalsIgnoreCase("f")) genderSpinner.setSelection(2);
        } catch(NullPointerException e) {
            genderSpinner.setSelection(0, false);
            e.printStackTrace();
        }
    }

    private void updateStudent() {
        String sql = "update students set FatherName = '" + fatherName.getText().toString().replaceAll("\n", " ") + "', " +
                "RollNoInClass =  " + rollNo.getText().toString() + ", " +
                "AdmissionNo = '" + admissionNo.getText().toString().replaceAll("\n", " ") + "', " +
                "MotherName = '" + motherName.getText().toString().replaceAll("\n", " ") + "', " +
                "DateOfBirth = '" + dob.getText().toString() + "' , " +
                "gender = '" + gender + "', " +
                "mobile1 = '" + mobile1.getText().toString() + "', " +
                "mobile2 = '" + mobile2.getText().toString() + "', " +
                "address = \"" + address.getText().toString().replaceAll("\n", " ").replace("\"", "'") + "\", " +
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
            e.printStackTrace();
        }
        ReplaceFragment.replace(new StudentProfile(), getFragmentManager());
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
