package in.teacher.util;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import in.teacher.activity.R;
import in.teacher.attendancefragment.MarkAttendance;
import in.teacher.dao.ClasDao;
import in.teacher.dao.SectionDao;
import in.teacher.dao.SlipTesttDao;
import in.teacher.dao.StudentsDao;
import in.teacher.dao.SubjectsDao;
import in.teacher.dao.TempDao;
import in.teacher.examfragment.HasPartition;
import in.teacher.sliptestfragment.SlipTest;
import in.teacher.examfragment.StructuredExam;
import in.teacher.fragment.StudentClassSec;
import in.teacher.sliptestfragment.ViewScore;
import in.teacher.sqlite.Students;
import in.teacher.sqlite.Temp;

/**
 * Created by vinkrish.
 */
public class CommonDialogUtils {
    private static boolean allowSelection;

    public static Dialog displayAlertWhiteDialog(Activity activity, String dialogBody) {
        final Dialog dialog = new Dialog(activity, R.style.DialogSlideAnim);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.white_ok_popup_dialog);
        if (dialogBody != null)
            ((TextView) dialog.findViewById(R.id.alertText)).setText(dialogBody);
        ((Button) dialog.findViewById(R.id.ok_button)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
        //Grab the window of the dialog, and change the width
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = dialog.getWindow();
        lp.copyFrom(window.getAttributes());
        //This makes the dialog take up the full width
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        return dialog;
    }

    public static Dialog displayDashbordSelector(final Activity activity, final SQLiteDatabase sqliteDatabase) {
        final Dialog dialog = new Dialog(activity, R.style.DialogSlideAnim);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
       // dialog.getWindow().setBackgroundDrawable(new ColorDrawable(activity.getResources().getColor(R.color.half_transparent)));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dashbord_selector);

        Temp temp = TempDao.selectTemp(sqliteDatabase);
        String className = ClasDao.getClassName(temp.getCurrentClass(), sqliteDatabase);
        String sectionName = SectionDao.getSectionName(temp.getCurrentSection(), sqliteDatabase);
        String subjectName = SubjectsDao.getSubjectName(temp.getSubjectId(), sqliteDatabase);
        ((TextView) dialog.findViewById(R.id.classSectionSubject)).setText(className + " - " + sectionName + "  " + subjectName);

        dialog.findViewById(R.id.es).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Temp temp = TempDao.selectTemp(sqliteDatabase);
                List<Students> studentsArray = StudentsDao.selectStudents2(temp.getCurrentSection(), temp.getCurrentSubject(), sqliteDatabase);
                if (studentsArray.size() > 0) {
                    SlipTesttDao.deleteSlipTest(sqliteDatabase);
                    ReplaceFragment.replace(new SlipTest(), activity.getFragmentManager());
                } else {
                    Toast.makeText(activity, "No students taken this subject", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.findViewById(R.id.vs).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SlipTesttDao.deleteSlipTest(sqliteDatabase);
                dialog.dismiss();
                ReplaceFragment.replace(new ViewScore(), activity.getFragmentManager());
            }
        });
        dialog.findViewById(R.id.se).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                int partition = SharedPreferenceUtil.getPartition(activity);
                if (partition == 1) {
                    ReplaceFragment.replace(new HasPartition(), activity.getFragmentManager());
                } else {
                    ReplaceFragment.replace(new StructuredExam(), activity.getFragmentManager());
                }
            }
        });
        dialog.findViewById(R.id.view_students).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                ReplaceFragment.replace(new StudentClassSec(), activity.getFragmentManager());
            }
        });
        dialog.show();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = dialog.getWindow();
        lp.copyFrom(window.getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        return dialog;
    }

    public static Dialog displaySwitchClass (final Activity activity, final SQLiteDatabase sqliteDatabase, final Fragment frag) {
        final Dialog dialog = new Dialog(activity, R.style.DialogSlideAnim);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.switch_layout);

        Temp temp = TempDao.selectTemp(sqliteDatabase);
        final int teacherId = temp.getTeacherId();

        final List<Integer> classIdList = new ArrayList<>();
        List<String> classNameList = new ArrayList<>();

        final List<Integer> sectionIdList = new ArrayList<>();
        final List<String> sectionNameList = new ArrayList<>();

        Cursor c = sqliteDatabase.rawQuery("select A.ClassId, B.ClassName from section A, class B where A.ClassTeacherId  = " + teacherId
                + " and A.ClassId = B.ClassId group by A.ClassId", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            classIdList.add(c.getInt(c.getColumnIndex("ClassId")));
            classNameList.add(c.getString(c.getColumnIndex("ClassName")));
            c.moveToNext();
        }
        c.close();

        Spinner classSpinner = (Spinner) dialog.findViewById(R.id.classSpinner);
        Spinner sectionSpinner = (Spinner) dialog.findViewById(R.id.sectionSpinner);

        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(activity, R.layout.spinner_header, classNameList);
        classAdapter.setDropDownViewResource(R.layout.spinner_droppeddown);
        classSpinner.setAdapter(classAdapter);

        final ArrayAdapter<String> sectionAdapter = new ArrayAdapter<>(activity, R.layout.spinner_header, sectionNameList);
        sectionAdapter.setDropDownViewResource(R.layout.spinner_droppeddown);
        sectionSpinner.setAdapter(sectionAdapter);

        final int[] classId = {0};

        classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                classId[0] = classIdList.get(position);
                if (allowSelection) {
                    sectionIdList.clear();
                    sectionNameList.clear();
                    sectionNameList.add("select section");
                    Cursor c2 = sqliteDatabase.rawQuery("select SectionId, SectionName from section where ClassId = " + classIdList.get(position) +
                            " and ClassTeacherId = " + teacherId, null);
                    c2.moveToFirst();
                    while (!c2.isAfterLast()) {
                        sectionIdList.add(c2.getInt(c2.getColumnIndex("SectionId")));
                        sectionNameList.add(c2.getString(c2.getColumnIndex("SectionName")));
                        c2.moveToNext();
                    }
                    c2.close();
                    sectionAdapter.notifyDataSetChanged();
                } else allowSelection = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    Temp t = new Temp();
                    t.setClassId(classId[0]);
                    t.setSectionId(sectionIdList.get(position-1));
                    t.setTeacherId(teacherId);
                    TempDao.updateTemp(t, sqliteDatabase);
                    dialog.dismiss();
                    ReplaceFragment.replace(frag , activity.getFragmentManager());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        dialog.show();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = dialog.getWindow();
        lp.copyFrom(window.getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);

        return dialog;
    }

    public static void hideKeyboard(Activity act) {
        InputMethodManager inputManager = (InputMethodManager) act
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View v = act.getCurrentFocus();
        if (v == null)
            return;

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

}
