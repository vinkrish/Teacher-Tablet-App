package in.teacher.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import in.teacher.activity.R;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.Activiti;
import in.teacher.sqlite.Exams;
import in.teacher.sqlite.SubActivity;
import in.teacher.sqlite.SubjectExams;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.CommonDialogUtils;
import in.teacher.util.PKGenerator;
import in.teacher.util.ReplaceFragment;

/**
 * Created by vinkrish on 16/11/15.
 */
public class CopyExamStructure extends Fragment {
    private Context context;
    private SQLiteDatabase sqliteDatabase;
    private Spinner classSpinner, examSpinner, secSpinner;
    private int classInChargePos, schoolId, classId, examId, sectionId;
    final List<Integer> examIdList = new ArrayList<>();
    private List<String> examNameList = new ArrayList<>();
    private List<Integer> othExamIdList = new ArrayList<>();
    private List<String> othExamNameList = new ArrayList<>();
    private List<Integer> selExamIdList = new ArrayList<>();
    private List<String> selExamNameList = new ArrayList<>();
    final List<Integer> sectionIdList = new ArrayList<>();
    List<String> sectionNameList = new ArrayList<>();
    private List<Integer> othSecIdList = new ArrayList<>();
    private List<String> othSecNameList = new ArrayList<>();
    private List<Integer> selSecIdList = new ArrayList<>();
    private List<String> selSecNameList = new ArrayList<>();
    protected boolean[] exmGroupSelections, secGroupSelections;
    private Button cpyExm, cpyRubrix, examSel, rbxSel, confirmExmCpy, confirmRbxCpy;
    private LinearLayout exmFrame, rubrixFrame;
    private TextView cpyExmTo, cpyRbxTo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.copy_exam_structure, container, false);

        initView(view);
        init();

        return view;

    }

    private void initView(View view) {
        classSpinner = (Spinner) view.findViewById(R.id.classSpinner);
        examSpinner = (Spinner) view.findViewById(R.id.examSpinner);
        cpyExm = (Button) view.findViewById(R.id.cpy_exm);
        cpyRubrix = (Button) view.findViewById(R.id.cpy_rubrix);
        exmFrame = (LinearLayout) view.findViewById(R.id.exam_frame);
        rubrixFrame = (LinearLayout) view.findViewById(R.id.rubrix_frame);

        secSpinner = (Spinner) view.findViewById(R.id.sec_sel);
        examSel = (Button) view.findViewById(R.id.exm_sel);
        rbxSel = (Button) view.findViewById(R.id.rubrix_sel);
        confirmExmCpy = (Button) view.findViewById(R.id.confirm_exm_cpy);
        confirmRbxCpy = (Button) view.findViewById(R.id.confirm_rubrix_cpy);

        cpyExmTo = (TextView) view.findViewById(R.id.copied_exm_to);
        cpyRbxTo = (TextView) view.findViewById(R.id.copied_rubrix_to);
    }

    private void init() {

        context = AppGlobal.getContext();
        sqliteDatabase = AppGlobal.getSqliteDatabase();

        Temp t = TempDao.selectTemp(sqliteDatabase);
        schoolId = t.getSchoolId();
        int teacherId = t.getTeacherId();
        final int classInchargeId = t.getClassInchargeId();

        final List<Integer> classInchargeList = new ArrayList<>();
        List<String> classNameIncharge = new ArrayList<>();

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

        final ArrayAdapter<String> adapter2 = new ArrayAdapter<>(context, R.layout.spinner_header, examNameList);
        adapter2.setDropDownViewResource(R.layout.spinner_droppeddown);
        examSpinner.setAdapter(adapter2);

        final ArrayAdapter<String> sectionAdapter = new ArrayAdapter<>(context, R.layout.spinner_header, sectionNameList);
        sectionAdapter.setDropDownViewResource(R.layout.spinner_droppeddown);
        secSpinner.setAdapter(sectionAdapter);

        classSpinner.setSelection(classInChargePos);

        classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                classId = classInchargeList.get(position);
                updateExamSpinner();
                initSectionSpinner();
                adapter2.notifyDataSetChanged();
                sectionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        examSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    examId = examIdList.get(position);
                    resetOtherExam();
                } else {
                    othExamIdList.clear();
                    othExamNameList.clear();
                    selExamIdList.clear();
                    cpyExmTo.setText("copied to :");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        secSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sectionId = sectionIdList.get(position);
                resetOtherSec();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        cpyExm.setOnClickListener(exmCpyListener);
        cpyRubrix.setOnClickListener(rubrixCpyListener);

        examSel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showExamDialog();
            }
        });

        rbxSel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSectionDialog();
            }
        });

        confirmExmCpy.setOnClickListener(confirmExmListener);
        confirmRbxCpy.setOnClickListener(confirmRbxListener);

    }

    private void initSectionSpinner() {
        sectionIdList.clear();
        sectionNameList.clear();
        Cursor c = sqliteDatabase.rawQuery("select SectionId, SectionName from section where ClassId = " + classId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            sectionIdList.add(c.getInt(c.getColumnIndex("SectionId")));
            sectionNameList.add(c.getString(c.getColumnIndex("SectionName")));
            c.moveToNext();
        }
        c.close();
    }

    private void updateExamSpinner() {
        examIdList.clear();
        examNameList.clear();
        othExamIdList.clear();
        othExamNameList.clear();
        examIdList.add(0);
        examNameList.add("Select Exam");
        Cursor c = sqliteDatabase.rawQuery("select ExamId, ExamName from exams where ClassId = " + classId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            examIdList.add(c.getInt(c.getColumnIndex("ExamId")));
            examNameList.add(c.getString(c.getColumnIndex("ExamName")));
            c.moveToNext();
        }
        c.close();
    }

    private View.OnClickListener exmCpyListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            rubrixFrame.setVisibility(View.GONE);
            cpyRubrix.setActivated(false);
            cpyExm.setActivated(true);
            exmFrame.setVisibility(View.VISIBLE);
        }
    };

    private View.OnClickListener rubrixCpyListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            exmFrame.setVisibility(View.GONE);
            cpyExm.setActivated(false);
            cpyRubrix.setActivated(true);
            rubrixFrame.setVisibility(View.VISIBLE);
        }
    };

    private View.OnClickListener confirmExmListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (selExamIdList.size() > 0) {
                new TaskCopyExam().execute();
            } else {
                CommonDialogUtils.displayAlertWhiteDialog(getActivity(), "please select exam to copy");
            }
        }
    };

    class TaskCopyExam extends AsyncTask<Void, Void, Void> {
        ProgressDialog pDialog = new ProgressDialog(getActivity());

        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Submitting ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                copyExam();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            pDialog.dismiss();
            Toast.makeText(getActivity(), "Exam copied", Toast.LENGTH_SHORT).show();
            ReplaceFragment.replace(new CopyExamStructure(), getFragmentManager());
        }
    }

    private View.OnClickListener confirmRbxListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (selSecIdList.size() > 0 && examId != 0) {
                new TaskCopyRubrix().execute();
            } else {
                CommonDialogUtils.displayAlertWhiteDialog(getActivity(), "please select exam and section to copy");
            }
        }
    };

    class TaskCopyRubrix extends AsyncTask<Void, Void, Void> {
        ProgressDialog pDialog = new ProgressDialog(getActivity());

        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Submitting ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                copyRubrix();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            pDialog.dismiss();
            Toast.makeText(getActivity(), "Rubrix copied", Toast.LENGTH_SHORT).show();
            ReplaceFragment.replace(new CopyExamStructure(), getFragmentManager());
        }
    }

    private void resetOtherExam() {
        selExamIdList.clear();
        selExamNameList.clear();
        cpyExmTo.setText("copied to :");
        othExamIdList.clear();
        othExamNameList.clear();
        for (int i = 1; i < examIdList.size(); i++) {
            if (examId != examIdList.get(i)) {
                othExamIdList.add(examIdList.get(i));
                othExamNameList.add(examNameList.get(i));
            }
        }
        exmGroupSelections = new boolean[othExamIdList.size()];

        selSecIdList.clear();
        selSecNameList.clear();
        cpyRbxTo.setText("copied to :");
    }

    private void showExamDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Select Exams")
                .setCancelable(false)
                .setMultiChoiceItems(othExamNameList.toArray(new CharSequence[othExamIdList.size()]),
                        exmGroupSelections, new ExamSelectionClickHandler())
                .setPositiveButton("OK", new ExamButtonClickHandler())
                .show();
    }

    private class ExamSelectionClickHandler implements DialogInterface.OnMultiChoiceClickListener {
        public void onClick(DialogInterface dialog, int clicked, boolean selected) {
            if (selected) exmGroupSelections[clicked] = true;
            else exmGroupSelections[clicked] = false;
        }
    }

    private class ExamButtonClickHandler implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int clicked) {
            switch (clicked) {
                case DialogInterface.BUTTON_POSITIVE:
                    selectedExmGroup();
                    break;
            }
        }
    }

    private void selectedExmGroup() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < othExamIdList.size(); i++) {
            if (exmGroupSelections[i]) {
                sb.append(othExamNameList.get(i) + ",");
                selExamIdList.add(othExamIdList.get(i));
                selExamNameList.add(othExamNameList.get(i));
            }
        }
        if (sb.length() > 0) {
            cpyExmTo.setText("copied to : " + sb.substring(0, sb.length() - 1));
        }
    }

    private void resetOtherSec() {
        selSecIdList.clear();
        selSecNameList.clear();
        cpyRbxTo.setText("copied to :");
        othSecIdList.clear();
        othSecNameList.clear();
        for (int i = 0; i < sectionIdList.size(); i++) {
            if (sectionId != sectionIdList.get(i)) {
                othSecIdList.add(sectionIdList.get(i));
                othSecNameList.add(sectionNameList.get(i));
            }
        }
        secGroupSelections = new boolean[othSecIdList.size()];
    }

    private void showSectionDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Select Sections")
                .setCancelable(false)
                .setMultiChoiceItems(othSecNameList.toArray(new CharSequence[othSecIdList.size()]),
                        secGroupSelections, new SecSelectionClickHandler())
                .setPositiveButton("OK", new SecButtonClickHandler())
                .show();
    }

    private class SecSelectionClickHandler implements DialogInterface.OnMultiChoiceClickListener {
        public void onClick(DialogInterface dialog, int clicked, boolean selected) {
            if (selected) secGroupSelections[clicked] = true;
            else secGroupSelections[clicked] = false;
        }
    }

    private class SecButtonClickHandler implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int clicked) {
            switch (clicked) {
                case DialogInterface.BUTTON_POSITIVE:
                    selectedSecGroup();
                    break;
            }
        }
    }

    private void selectedSecGroup() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < othSecIdList.size(); i++) {
            if (secGroupSelections[i]) {
                sb.append(othSecNameList.get(i) + ",");
                selSecIdList.add(othSecIdList.get(i));
                selSecNameList.add(othSecNameList.get(i));
            }
        }
        if (sb.length() > 0) {
            cpyRbxTo.setText("copied to : " + sb.substring(0, sb.length() - 1));
        }
    }

    private void copyExam() throws NoSuchAlgorithmException {
        Exams exm = new Exams();
        Cursor c = sqliteDatabase.rawQuery("select * from exams where ExamId = " + examId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            exm.setSchoolId(c.getInt(c.getColumnIndex("SchoolId")));
            exm.setClassId(c.getInt(c.getColumnIndex("ClassId")));
            exm.setSubjectIDs(c.getString(c.getColumnIndex("SubjectIDs")));
            exm.setSubjectGroupIds(c.getString(c.getColumnIndex("SubjectGroupIds")));
            exm.setOrderId(c.getInt(c.getColumnIndex("OrderId")));
            exm.setPercentage(c.getString(c.getColumnIndex("Percentage")));
            exm.setTimeTable(c.getString(c.getColumnIndex("TimeTable")));
            exm.setPortions(c.getString(c.getColumnIndex("Portions")));
            exm.setFileName(c.getString(c.getColumnIndex("FileName")));
            exm.setGradeSystem(c.getInt(c.getColumnIndex("GradeSystem")));
            exm.setTerm(c.getInt(c.getColumnIndex("Term")));
            exm.setMarkUploaded(0);
            c.moveToNext();
        }
        c.close();

        List<SubjectExams> seList = new ArrayList<>();
        Cursor c2 = sqliteDatabase.rawQuery("select * from subjectexams where ExamId = " + examId, null);
        c2.moveToFirst();
        while (!c2.isAfterLast()) {
            SubjectExams se = new SubjectExams();
            se.setSchoolId(c2.getInt(c2.getColumnIndex("SchoolId")));
            se.setClassId(c2.getInt(c2.getColumnIndex("ClassId")));
            se.setSubjectId(c2.getInt(c2.getColumnIndex("SubjectId")));
            se.setMaximumMark(c2.getInt(c2.getColumnIndex("MaximumMark")));
            se.setFailMark(c2.getInt(c2.getColumnIndex("FailMark")));
            seList.add(se);
            c2.moveToNext();
        }
        c2.close();


        for (Integer eId : selExamIdList) {

            try {
                String sql = "update exams set SubjectIDs='" + exm.getSubjectIDs() + "', SubjectGroupIds='" + exm.getSubjectGroupIds() +
                        "', Percentage='" + exm.getPercentage() + "', GradeSystem=" + exm.getGradeSystem() + ", Term=" + exm.getTerm() + " where ExamId=" + eId;
                sqliteDatabase.execSQL(sql);
                ContentValues cv = new ContentValues();
                cv.put("Query", sql);
                sqliteDatabase.insert("uploadsql", null, cv);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                String sql = "delete from subjectexams where ExamId = " + eId;
                sqliteDatabase.execSQL(sql);
                ContentValues cv = new ContentValues();
                cv.put("Query", sql);
                sqliteDatabase.insert("uploadsql", null, cv);
            } catch (SQLException e) {
                e.printStackTrace();
            }


            List<Activiti> actList = new ArrayList<>();
            List<SubActivity> subActList = new ArrayList<>();
            for (SubjectExams se : seList) {
                String sql = "insert into subjectexams(SchoolId, ClassId, ExamId, SubjectId, MaximumMark, FailMark) " +
                        "values(" + schoolId + ", " + classId + ", " + eId + ", " + se.getSubjectId() + ", " + se.getMaximumMark() + ", " + se.getFailMark() + ")";
                try {
                    sqliteDatabase.execSQL(sql);
                    ContentValues cv = new ContentValues();
                    cv.put("Query", sql);
                    sqliteDatabase.insert("uploadsql", null, cv);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                for (Integer secI : sectionIdList) {
                    actList.clear();
                    Cursor c3 = sqliteDatabase.rawQuery("select * from activity where SectionId = " + secI + " and " +
                            "ExamId = " + examId + " and SubjectId = " + se.getSubjectId(), null);
                    c3.moveToFirst();
                    while (!c3.isAfterLast()) {
                        Activiti act = new Activiti();
                        act.setActivityId(c3.getInt(c3.getColumnIndex("ActivityId")));
                        act.setSchoolId(c3.getInt(c3.getColumnIndex("SchoolId")));
                        act.setClassId(c3.getInt(c3.getColumnIndex("ClassId")));
                        act.setSectionId(c3.getInt(c3.getColumnIndex("SectionId")));
                        act.setExamId(eId);
                        act.setSubjectId(c3.getInt(c3.getColumnIndex("SubjectId")));
                        act.setRubrixId(c3.getInt(c3.getColumnIndex("RubrixId")));
                        act.setActivityName(c3.getString(c3.getColumnIndex("ActivityName")));
                        act.setMaximumMark(c3.getInt(c3.getColumnIndex("MaximumMark")));
                        act.setWeightage(c3.getInt(c3.getColumnIndex("Weightage")));
                        act.setSubActivity(c3.getInt(c3.getColumnIndex("SubActivity")));
                        act.setCalculation(c3.getInt(c3.getColumnIndex("Calculation")));
                        actList.add(act);
                        c3.moveToNext();
                    }
                    c3.close();

                    for (Activiti act : actList) {
                        int newActivityId = PKGenerator.getMD5(schoolId, secI, act.getActivityName());
                        createNewActivity(newActivityId, act, secI);

                        subActList.clear();
                        Cursor c4 = sqliteDatabase.rawQuery("select * from subactivity where ActivityId = " + act.getActivityId(), null);
                        c4.moveToFirst();
                        while (!c4.isAfterLast()) {
                            SubActivity subAct = new SubActivity();
                            subAct.setSubActivityId(c4.getInt(c4.getColumnIndex("SubActivityId")));
                            subAct.setSchoolId(c4.getInt(c4.getColumnIndex("SchoolId")));
                            subAct.setClassId(c4.getInt(c4.getColumnIndex("ClassId")));
                            subAct.setSectionId(c4.getInt(c4.getColumnIndex("SectionId")));
                            subAct.setExamId(eId);
                            subAct.setSubjectId(c4.getInt(c4.getColumnIndex("SubjectId")));
                            subAct.setActivityId(c4.getInt(c4.getColumnIndex("ActivityId")));
                            subAct.setSubActivityName(c4.getString(c4.getColumnIndex("SubActivityName")));
                            subAct.setMaximumMark(c4.getInt(c4.getColumnIndex("MaximumMark")));
                            subAct.setWeightage(c4.getInt(c4.getColumnIndex("Weightage")));
                            subAct.setCalculation(c4.getInt(c4.getColumnIndex("Calculation")));
                            subActList.add(subAct);
                            c4.moveToNext();
                        }
                        c4.close();

                        for (SubActivity subAct : subActList) {
                            int newSubActivityId = PKGenerator.getMD5(schoolId, secI, subAct.getSubActivityName());
                            createNewSubActivity(newSubActivityId, subAct, secI, newActivityId);
                        }
                    }
                }

            }

        }

    }

    private void copyRubrix() throws NoSuchAlgorithmException {
        List<Integer> subjectIdList = new ArrayList<>();
        Cursor c = sqliteDatabase.rawQuery("select SubjectId from subjectexams where ClassId = " + classId + " and " +
                "ExamId = " + examId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            subjectIdList.add(c.getInt(c.getColumnIndex("SubjectId")));
            c.moveToNext();
        }
        c.close();

        List<Activiti> actList = new ArrayList<>();
        List<SubActivity> subActList = new ArrayList<>();

        for (Integer subjectId : subjectIdList) {
            actList.clear();
            Cursor c2 = sqliteDatabase.rawQuery("select * from activity where SectionId = " + sectionId + " and " +
                    "ExamId = " + examId + " and SubjectId = " + subjectId, null);
            c2.moveToFirst();
            while (!c2.isAfterLast()) {
                Activiti act = new Activiti();
                act.setActivityId(c2.getInt(c2.getColumnIndex("ActivityId")));
                act.setSchoolId(c2.getInt(c2.getColumnIndex("SchoolId")));
                act.setClassId(c2.getInt(c2.getColumnIndex("ClassId")));
                act.setSectionId(c2.getInt(c2.getColumnIndex("SectionId")));
                act.setExamId(c2.getInt(c2.getColumnIndex("ExamId")));
                act.setSubjectId(c2.getInt(c2.getColumnIndex("SubjectId")));
                act.setRubrixId(c2.getInt(c2.getColumnIndex("RubrixId")));
                act.setActivityName(c2.getString(c2.getColumnIndex("ActivityName")));
                act.setMaximumMark(c2.getInt(c2.getColumnIndex("MaximumMark")));
                act.setWeightage(c2.getInt(c2.getColumnIndex("Weightage")));
                act.setSubActivity(c2.getInt(c2.getColumnIndex("SubActivity")));
                act.setCalculation(c2.getInt(c2.getColumnIndex("Calculation")));
                actList.add(act);
                c2.moveToNext();
            }
            c2.close();

            for (Activiti act : actList) {
                int newActivityId = PKGenerator.getMD5(schoolId, sectionId, act.getActivityName());
                for (Integer secId : selSecIdList) {
                    createNewActivity(newActivityId, act, secId);
                }

                subActList.clear();
                Cursor c3 = sqliteDatabase.rawQuery("select * from subactivity where ActivityId = " + act.getActivityId(), null);
                c3.moveToFirst();
                while (!c3.isAfterLast()) {
                    SubActivity subAct = new SubActivity();
                    subAct.setSubActivityId(c3.getInt(c3.getColumnIndex("SubActivityId")));
                    subAct.setSchoolId(c3.getInt(c3.getColumnIndex("SchoolId")));
                    subAct.setClassId(c3.getInt(c3.getColumnIndex("ClassId")));
                    subAct.setSectionId(c3.getInt(c3.getColumnIndex("SectionId")));
                    subAct.setExamId(c3.getInt(c3.getColumnIndex("ExamId")));
                    subAct.setSubjectId(c3.getInt(c3.getColumnIndex("SubjectId")));
                    subAct.setActivityId(c3.getInt(c3.getColumnIndex("ActivityId")));
                    subAct.setSubActivityName(c3.getString(c3.getColumnIndex("SubActivityName")));
                    subAct.setMaximumMark(c3.getInt(c3.getColumnIndex("MaximumMark")));
                    subAct.setWeightage(c3.getInt(c3.getColumnIndex("Weightage")));
                    subAct.setCalculation(c3.getInt(c3.getColumnIndex("Calculation")));
                    subActList.add(subAct);
                    c3.moveToNext();
                }
                c3.close();

                for (SubActivity subAct : subActList) {
                    int newSubActivityId = PKGenerator.getMD5(schoolId, sectionId, subAct.getSubActivityName());
                    for (Integer secId : selSecIdList) {
                        createNewSubActivity(newSubActivityId, subAct, secId, newActivityId);
                    }
                }
            }
        }

    }

    private void createNewActivity(int actId, Activiti act, int secId) {
        String sql = "insert into activity (ActivityId, SchoolId, ClassId, SectionId, ExamId, " +
                "SubjectId, RubrixId, ActivityName, MaximumMark, Weightage, SubActivity, Calculation) " +
                " values (" + actId + ", " + act.getSchoolId() + ", " + act.getClassId() + ", " + secId + ", " +
                act.getExamId() + ", " + act.getSubjectId() + ", " + act.getRubrixId() + ", '" + act.getActivityName() + "', " +
                act.getMaximumMark() + ", " + act.getWeightage() + ", " + act.getSubActivity() + ", " + act.getCalculation() + ")";
        try {
            sqliteDatabase.execSQL(sql);
            ContentValues cv = new ContentValues();
            cv.put("Query", sql);
            sqliteDatabase.insert("uploadsql", null, cv);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createNewSubActivity(int subActId, SubActivity subAct, int secId, int actId) {
        String sql = "insert into subactivity (SubActivityId, SchoolId, ClassId, SectionId, ExamId, " +
                "SubjectId, ActivityId, SubActivityName, MaximumMark, Weightage, Calculation) " +
                " values (" + subActId + ", " + subAct.getSchoolId() + ", " + subAct.getClassId() + ", " + secId + ", " +
                subAct.getExamId() + ", " + subAct.getSubjectId() + ", " + actId + ", '" + subAct.getSubActivityName() + "', " +
                subAct.getMaximumMark() + ", " + subAct.getWeightage() + ", " + subAct.getCalculation() + ")";
        try {
            sqliteDatabase.execSQL(sql);
            ContentValues cv = new ContentValues();
            cv.put("Query", sql);
            sqliteDatabase.insert("uploadsql", null, cv);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
