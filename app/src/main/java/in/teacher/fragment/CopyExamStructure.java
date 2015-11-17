package in.teacher.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import in.teacher.activity.R;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.CommonDialogUtils;

/**
 * Created by vinkrish on 16/11/15.
 */
public class CopyExamStructure extends Fragment {
    private Context context;
    private SQLiteDatabase sqliteDatabase;
    private Spinner classSpinner, examSpinner, secSpinner;
    private int classInChargePos, classId, examId, sectionId;
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
                copyExam();
            } else {
                CommonDialogUtils.displayAlertWhiteDialog(getActivity(), "please select exam to copy");
            }
        }
    };

    private View.OnClickListener confirmRbxListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (selSecIdList.size() > 0 && examId != 0) {
                copyRubrix();
            } else {
                CommonDialogUtils.displayAlertWhiteDialog(getActivity(), "please select exam and section to copy");
            }
        }
    };

    private void resetOtherExam() {
        othExamIdList.clear();
        othExamNameList.clear();
        for (int i = 1; i < examIdList.size(); i++) {
            if (examId != examIdList.get(i)) {
                othExamIdList.add(examIdList.get(i));
                othExamNameList.add(examNameList.get(i));
            }
        }
        exmGroupSelections = new boolean[othExamIdList.size()];
    }

    private void showExamDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Select Subject Group")
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
                .setTitle("Select Subject Group")
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

    private void copyExam() {

    }

    private void copyRubrix() {

    }


}
