package in.teacher.fragment;

import android.app.Activity;
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
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.Subject;

import in.teacher.activity.R;
import in.teacher.dao.ClasDao;
import in.teacher.dao.ExamsDao;
import in.teacher.dao.SubjectGroupDao;
import in.teacher.dao.SubjectsDao;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.Exams;
import in.teacher.sqlite.SubjectExams;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.CommonDialogUtils;
import in.teacher.util.PKGenerator;

/**
 * Created by vinkrish on 12/10/15.
 */
public class ExamCreate extends Fragment {
    private SQLiteDatabase sqliteDatabase;
    private Activity activity;
    private Context context;
    private int schoolId, classId;
    private Button selectSubjectGroup, save;
    private ScrollView scrollView;
    private LinearLayout ll;
    private List<Integer> subjectGroupIdList = new ArrayList<>();
    private List<String> subjectGroupNameList = new ArrayList<>();
    private List<Integer> selectedSubGroupId = new ArrayList<>();
    protected boolean[] groupSelections;
    private EditText examName_et, percentage_et, term_et;
    private String examName;
    private TableLayout table;
    private Switch gradeSwitch;
    private int width, strippedWidth, tag, percentage, term, grade, examId;
    private List<SubExams> subjectExams = new ArrayList<>();
    private StringBuilder ids = new StringBuilder();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.exam_create, container, false);

        initView(view);
        init();

        return view;
    }

    private void initView(View view) {
        scrollView = (ScrollView) view.findViewById(R.id.scrollView);
        ll = (LinearLayout) view.findViewById(R.id.ll);
        examName_et = (EditText) view.findViewById(R.id.exam_name);
        percentage_et = (EditText) view.findViewById(R.id.percentage);
        term_et = (EditText) view.findViewById(R.id.term);
        gradeSwitch = (Switch) view.findViewById(R.id.grade);
        save = (Button) view.findViewById(R.id.save);
        selectSubjectGroup = (Button) view.findViewById(R.id.sel_sub_group);
    }

    private void init() {
        sqliteDatabase = AppGlobal.getSqliteDatabase();
        activity = AppGlobal.getActivity();
        context = AppGlobal.getContext();

        Temp t = TempDao.selectTemp(sqliteDatabase);
        schoolId = t.getSchoolId();
        classId = t.getClassInchargeId();

        selectSubjectGroup.setActivated(true);
        selectSubjectGroup.setOnClickListener(checkSubjectGroup);

        save.setOnClickListener(submitListener);

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        width = displayMetrics.widthPixels;
        strippedWidth = (width / 4) - 1;

        tag = 0;
    }

    View.OnClickListener submitListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isSavable()) new CalledSubmit().execute();
            else
                CommonDialogUtils.displayAlertWhiteDialog(activity, "please enter max / fail mark for all subjects");
        }
    };

    private boolean isSavable() {
        for (SubExams xm : subjectExams) {
            if (!xm.isNullCheck()) {
                return false;
            }
        }
        return true;
    }

    View.OnClickListener checkSubjectGroup = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(examName_et.getWindowToken(), 0);
            if (!examName_et.getText().toString().equals("") &&
                    !term_et.getText().toString().equals("") &&
                    !percentage_et.getText().toString().equals("")) {
                subjectGroupIdList = ClasDao.getSubjectGroupIds(sqliteDatabase, classId);
                StringBuilder sb = new StringBuilder();
                for (Integer ids : subjectGroupIdList) {
                    sb.append(ids + ",");
                }
                subjectGroupNameList = SubjectGroupDao.getSubjectGroupNameList(sqliteDatabase, sb.substring(0, sb.length() - 1));
                groupSelections = new boolean[subjectGroupIdList.size()];
                showGroupDialog();
            } else {
                CommonDialogUtils.displayAlertWhiteDialog(activity, "please enter all fields");
            }
        }
    };

    public void showGroupDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Select Subject Group")
                .setCancelable(false)
                .setMultiChoiceItems(subjectGroupNameList.toArray(new CharSequence[subjectGroupNameList.size()]),
                        groupSelections, new GroupSelectionClickHandler())
                .setPositiveButton("OK", new GroupButtonClickHandler())
                .show();
    }

    public class GroupSelectionClickHandler implements DialogInterface.OnMultiChoiceClickListener {
        public void onClick(DialogInterface dialog, int clicked, boolean selected) {
            if (selected) groupSelections[clicked] = true;
            else groupSelections[clicked] = false;
        }
    }

    public class GroupButtonClickHandler implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int clicked) {
            switch (clicked) {
                case DialogInterface.BUTTON_POSITIVE:
                    selectedGroup();
                    break;
            }
        }
    }

    protected void selectedGroup() {
        for (int i = 0; i < subjectGroupIdList.size(); i++) {
            if (groupSelections[i]) {
                selectedSubGroupId.add(subjectGroupIdList.get(i));
            }
        }

        examName = examName_et.getText().toString();
        percentage = Integer.parseInt(percentage_et.getText().toString());
        term = Integer.parseInt(term_et.getText().toString());

        if (gradeSwitch.isChecked()) grade = 1;
        else grade = 0;

        try {
            examId = PKGenerator.getMD5(schoolId, classId, examName);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        ll.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
        save.setVisibility(View.VISIBLE);
        table = new TableLayout(activity);
        LayoutInflater inflater =
                (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.exam_create_header, null);
        LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(v);
        linearLayout.addView(table);
        scrollView.addView(linearLayout);
        generateTable();
    }

    private void generateTable() {
        for (int i = 0; i < selectedSubGroupId.size(); i++) {
            List<Integer> subjectIdList = SubjectGroupDao.getSubjectIdsInGroup(sqliteDatabase, selectedSubGroupId.get(i));
            StringBuilder sb = new StringBuilder();
            for (Integer ids : subjectIdList) {
                sb.append(ids + ",");
            }
            List<String> subjectNameList = SubjectsDao.getSubjectNameList(sqliteDatabase, sb.substring(0, sb.length() - 1));
            for (int k = 0; k < subjectIdList.size(); k++) {
                TableRow tableRow = tableRow(subjectIdList.get(k), subjectNameList.get(k));
                table.addView(tableRow);
            }
        }
    }

    private TableRow tableRow(int subId, String subName) {

        boolean partition = SubjectsDao.isPartition(sqliteDatabase, subId);

        TableRow tableRowForTable = new TableRow(this.context);
        TableRow.LayoutParams params = new TableRow.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        LinearLayout verticalLayout = new LinearLayout(activity);
        verticalLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout horizontalLayout = new LinearLayout(activity);
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(strippedWidth, LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView tv1 = new TextView(activity);
        tv1.setLayoutParams(p);
        tv1.setText(subName);
        tv1.setPadding(15, 10, 0, 10);
        tv1.setTextSize(18);
        tv1.setTextColor(ContextCompat.getColor(context, R.color.dark_black));
        horizontalLayout.addView(tv1);

        View verticalBorder = new View(activity);
        verticalBorder.setBackgroundColor(getResources().getColor(R.color.border));
        LinearLayout.LayoutParams vlp = new LinearLayout.LayoutParams(1, LinearLayout.LayoutParams.MATCH_PARENT);
        verticalBorder.setLayoutParams(vlp);
        horizontalLayout.addView(verticalBorder);

        if (partition) {

            int theoryId = 0;
            int practicalId = 0;
            Cursor c = sqliteDatabase.rawQuery("select TheorySubjectId, PracticalSubjectId from subjects where SubjectId = " + subId, null);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                theoryId = c.getInt(c.getColumnIndex("TheorySubjectId"));
                practicalId = c.getInt(c.getColumnIndex("PracticalSubjectId"));
                c.moveToNext();
            }
            c.close();

            String theoryName = SubjectsDao.getSubjectName(theoryId, sqliteDatabase);
            String practicalName = SubjectsDao.getSubjectName(practicalId, sqliteDatabase);

            subjectExams.add(new SubExams(theoryId));
            subjectExams.add(new SubExams(practicalId));

            ids.append(theoryId + "").append(",");
            ids.append(practicalId + "").append(",");

            LinearLayout innerVerticalLayout = new LinearLayout(activity);
            innerVerticalLayout.setOrientation(LinearLayout.VERTICAL);

            LinearLayout innerHorizontalLayout1 = new LinearLayout(activity);

            TextView tv2 = new TextView(activity);
            tv2.setLayoutParams(p);
            tv2.setText(theoryName);
            tv2.setPadding(15, 10, 0, 10);
            tv2.setTextSize(18);
            tv2.setTextColor(ContextCompat.getColor(context, R.color.dark_black));
            innerHorizontalLayout1.addView(tv2);

            View verticalBorder2 = new View(activity);
            verticalBorder2.setBackgroundColor(ContextCompat.getColor(context, R.color.border));
            verticalBorder2.setLayoutParams(vlp);
            innerHorizontalLayout1.addView(verticalBorder2);

            EditText ed1 = new EditText(activity);
            ed1.setTag(tag);
            tag++;
            ed1.setLayoutParams(p);
            ed1.setGravity(Gravity.CENTER);
            ed1.setInputType(InputType.TYPE_CLASS_NUMBER);
            ed1.addTextChangedListener(new MarksTextWatcher(ed1));
            innerHorizontalLayout1.addView(ed1);

            View verticalBorder3 = new View(activity);
            verticalBorder3.setBackgroundColor(ContextCompat.getColor(context, R.color.border));
            verticalBorder3.setLayoutParams(vlp);
            innerHorizontalLayout1.addView(verticalBorder3);

            EditText ed2 = new EditText(activity);
            ed2.setTag(tag);
            tag++;
            ed2.setLayoutParams(p);
            ed2.setText("0");
            ed2.setGravity(Gravity.CENTER);
            ed2.setInputType(InputType.TYPE_CLASS_NUMBER);
            ed2.addTextChangedListener(new MarksTextWatcher(ed2));
            innerHorizontalLayout1.addView(ed2);

            innerVerticalLayout.addView(innerHorizontalLayout1);

            View innerHorizontalBorder = new View(activity);
            innerHorizontalBorder.setBackgroundColor(ContextCompat.getColor(context, R.color.border));
            LinearLayout.LayoutParams ihlp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
            innerHorizontalBorder.setLayoutParams(ihlp);
            innerVerticalLayout.addView(innerHorizontalBorder);

            LinearLayout innerHorizontalLayout2 = new LinearLayout(activity);

            TextView tv22 = new TextView(activity);
            tv22.setLayoutParams(p);
            tv22.setText(practicalName);
            tv22.setPadding(20, 10, 0, 10);
            tv22.setTextSize(18);
            tv22.setTextColor(ContextCompat.getColor(context, R.color.dark_black));
            innerHorizontalLayout2.addView(tv22);

            View verticalBorder22 = new View(activity);
            verticalBorder22.setBackgroundColor(ContextCompat.getColor(context, R.color.border));
            verticalBorder22.setLayoutParams(vlp);
            innerHorizontalLayout2.addView(verticalBorder22);

            EditText ed12 = new EditText(activity);
            ed12.setTag(tag);
            tag++;
            ed12.setLayoutParams(p);
            ed12.setGravity(Gravity.CENTER);
            ed12.setInputType(InputType.TYPE_CLASS_NUMBER);
            ed12.addTextChangedListener(new MarksTextWatcher(ed12));
            innerHorizontalLayout2.addView(ed12);

            View verticalBorder32 = new View(activity);
            verticalBorder32.setBackgroundColor(ContextCompat.getColor(context, R.color.border));
            verticalBorder32.setLayoutParams(vlp);
            innerHorizontalLayout2.addView(verticalBorder32);

            EditText ed22 = new EditText(activity);
            ed22.setTag(tag);
            tag++;
            ed22.setLayoutParams(p);
            ed22.setText("0");
            ed22.setGravity(Gravity.CENTER);
            ed22.setInputType(InputType.TYPE_CLASS_NUMBER);
            ed22.addTextChangedListener(new MarksTextWatcher(ed22));
            innerHorizontalLayout2.addView(ed22);

            innerVerticalLayout.addView(innerHorizontalLayout2);

            horizontalLayout.addView(innerVerticalLayout);

        } else {
            subjectExams.add(new SubExams(subId));
            ids.append(subId + "").append(",");

            String subjName = SubjectsDao.getSubjectName(subId, sqliteDatabase);
            TextView tv2 = new TextView(activity);
            tv2.setLayoutParams(p);
            tv2.setText(subjName);
            tv2.setPadding(20, 10, 0, 10);
            tv2.setTextSize(18);
            tv2.setTextColor(ContextCompat.getColor(context, R.color.dark_black));
            horizontalLayout.addView(tv2);

            View verticalBorder2 = new View(activity);
            verticalBorder2.setBackgroundColor(ContextCompat.getColor(context, R.color.border));
            verticalBorder2.setLayoutParams(vlp);
            horizontalLayout.addView(verticalBorder2);

            EditText ed1 = new EditText(activity);
            ed1.setTag(tag);
            tag++;
            ed1.setLayoutParams(p);
            ed1.setGravity(Gravity.CENTER);
            ed1.setInputType(InputType.TYPE_CLASS_NUMBER);
            ed1.addTextChangedListener(new MarksTextWatcher(ed1));
            horizontalLayout.addView(ed1);

            View verticalBorder3 = new View(activity);
            verticalBorder3.setBackgroundColor(ContextCompat.getColor(context, R.color.border));
            verticalBorder3.setLayoutParams(vlp);
            horizontalLayout.addView(verticalBorder3);

            EditText ed2 = new EditText(activity);
            ed2.setTag(tag);
            tag++;
            ed2.setLayoutParams(p);
            ed2.setText("0");
            ed2.setGravity(Gravity.CENTER);
            ed2.setInputType(InputType.TYPE_CLASS_NUMBER);
            ed2.addTextChangedListener(new MarksTextWatcher(ed2));
            horizontalLayout.addView(ed2);

        }

        verticalLayout.addView(horizontalLayout);
        View horizontalBorder = new View(activity);
        horizontalBorder.setBackgroundColor(ContextCompat.getColor(context, R.color.border));
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
        horizontalBorder.setLayoutParams(hlp);
        verticalLayout.addView(horizontalBorder);
        tableRowForTable.addView(verticalLayout, params);

        return tableRowForTable;
    }

    private class MarksTextWatcher implements TextWatcher {

        private int pos;
        private int index;
        private View view;

        private MarksTextWatcher(View view) {
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            pos = (Integer) view.getTag();
            index = pos / 2;
            SubExams ses = subjectExams.get(index);

            if (s.toString().equals("")) {
                ses.setNullCheck(false);
            } else {
                if (pos % 2 == 0) {
                    ses.setMaxMark(Integer.parseInt(s.toString()));
                } else {
                    ses.setMinMark(Integer.parseInt(s.toString()));
                }
                ses.setNullCheck(true);
            }
            subjectExams.set(index, ses);
        }
    }

    class SubExams {
        private int subjectId;
        private int maxMark;
        private int minMark;
        private boolean nullCheck;

        public SubExams(int subjectId) {
            maxMark = 0;
            minMark = 0;
            this.subjectId = subjectId;
            this.nullCheck = false;
        }

        public boolean isNullCheck() {
            return nullCheck;
        }

        public void setNullCheck(boolean nullCheck) {
            this.nullCheck = nullCheck;
        }

        public int getSubjectId() {
            return subjectId;
        }

        public void setSubjectId(int subjectId) {
            this.subjectId = subjectId;
        }

        public int getMaxMark() {
            return maxMark;
        }

        public void setMaxMark(int maxMark) {
            this.maxMark = maxMark;
        }

        public int getMinMark() {
            return minMark;
        }

        public void setMinMark(int minMark) {
            this.minMark = minMark;
        }
    }

    class CalledSubmit extends AsyncTask<Void, Void, Void> {
        ProgressDialog pDialog = new ProgressDialog(activity);

        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Submitting marks...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            submitExam();
            submitSubjectExams();
            return null;
        }

        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            pDialog.dismiss();
        }
    }

    private void submitExam() {

        StringBuilder groupIds = new StringBuilder();
        for (Integer gids : selectedSubGroupId) {
            groupIds.append(gids + "").append(",");
        }

        Exams exams = new Exams();
        exams.setSchoolId(schoolId);
        exams.setClassId(classId);
        exams.setExamId(examId);
        exams.setSubjectIDs(ids.substring(0, ids.length() - 1));
        exams.setSubjectGroupIds(groupIds.substring(0, groupIds.length() - 1));
        exams.setExamName(examName);
        exams.setOrderId(0);
        exams.setPercentage(percentage + "");
        exams.setGradeSystem(grade);
        exams.setTerm(term);

        ExamsDao.insertExam(sqliteDatabase, exams);
    }

    private void submitSubjectExams() {
        for (SubExams se : subjectExams) {
            String sql = "insert into subjectexams(SchoolId, ClassId, ExamId, SubjectId, MaximumMark, FailMark) " +
                    "values(" + schoolId + ", " + classId + ", " + examId + ", " + se.getSubjectId() + ", " + se.getMaxMark() + ", " + se.getMinMark() + ")";
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

}
