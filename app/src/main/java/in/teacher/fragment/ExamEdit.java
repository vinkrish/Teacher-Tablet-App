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
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.teacher.activity.R;
import in.teacher.dao.SubjectsDao;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.CommonDialogUtils;
import in.teacher.util.ReplaceFragment;

/**
 * Created by vinkrish on 27/10/15.
 */
public class ExamEdit extends Fragment {
    private Context context;
    private SQLiteDatabase sqliteDatabase;
    private Spinner classSpinner, examSpinner;
    private int classInChargePos, classId, examId, width, strippedWidth, tag;
    final List<Integer> examIdList = new ArrayList<>();
    List<String> examNameList = new ArrayList<>();
    private List<SubExams> subjectExams = new ArrayList<>();
    private TableLayout table;
    private ScrollView scrollView;
    private ArrayAdapter<String> examAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.exam_update, container, false);

        classSpinner = (Spinner) view.findViewById(R.id.classSpinner);
        examSpinner = (Spinner) view.findViewById(R.id.examSpinner);
        scrollView = (ScrollView) view.findViewById(R.id.scrollView);

        init();

        view.findViewById(R.id.delete_butt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDelete();
            }
        });

        view.findViewById(R.id.save_butt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSavable()) new CalledSubmit().execute();
                else
                    CommonDialogUtils.displayAlertWhiteDialog(getActivity(),
                            "please enter max / fail mark for all subjects & fail mark should be less than max mark.");
            }
        });

        return view;
    }

    private void confirmDelete() {
        AlertDialog.Builder submitBuilder = new AlertDialog.Builder(getActivity());
        submitBuilder.setCancelable(false);
        submitBuilder.setTitle("Confirm your action");
        submitBuilder.setMessage("Do you want to delete exam?");
        submitBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                dialog.cancel();
            }
        });
        submitBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                String sql = "delete from exams where ExamId = " + examId + " and ClassId = " + classId;
                try {
                    sqliteDatabase.execSQL(sql);
                    ContentValues cv = new ContentValues();
                    cv.put("Query", sql);
                    sqliteDatabase.insert("uploadsql", null, cv);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                table.removeAllViews();
                updateExamSpinner();
                examAdapter.notifyDataSetChanged();
                examSpinner.setSelection(0);
                //ReplaceFragment.replace(new TeacherInCharge(), getFragmentManager());
            }
        });
        submitBuilder.show();
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

        examAdapter = new ArrayAdapter<>(context, R.layout.spinner_header, examNameList);
        examAdapter.setDropDownViewResource(R.layout.spinner_droppeddown);
        examSpinner.setAdapter(examAdapter);

        classSpinner.setSelection(classInChargePos);

        classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                classId = classInchargeList.get(position);
                updateExamSpinner();
                examAdapter.notifyDataSetChanged();
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
                    generateTable();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        width = displayMetrics.widthPixels;
        strippedWidth = (width / 3) - 1;

        tag = 0;

        table = new TableLayout(getActivity());
        LayoutInflater inflater =
                (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.exam_update_header, null);
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(v);
        linearLayout.addView(table);
        scrollView.addView(linearLayout);
    }

    private void updateExamSpinner() {
        examIdList.clear();
        examNameList.clear();
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

    private void generateTable() {
        table.removeAllViews();
        subjectExams.clear();
        tag = 0;

        Cursor c = sqliteDatabase.rawQuery("select SubjectId, MaximumMark, FailMark from subjectexams where ClassId = " + classId + " and " +
                "ExamId = " + examId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            int subjectId = c.getInt(c.getColumnIndex("SubjectId"));
            int maxMark = c.getInt(c.getColumnIndex("MaximumMark"));
            int minMark = c.getInt(c.getColumnIndex("FailMark"));
            subjectExams.add(new SubExams(subjectId, maxMark, minMark));
            c.moveToNext();
        }
        c.close();

        for (int k = 0; k < subjectExams.size(); k++) {
            TableRow tableRow = generateRow(subjectExams.get(k));
            table.addView(tableRow);
        }
    }

    private TableRow generateRow(SubExams se) {

        TableRow tableRowForTable = new TableRow(this.context);
        TableRow.LayoutParams params = new TableRow.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        LinearLayout verticalLayout = new LinearLayout(getActivity());
        verticalLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout horizontalLayout = new LinearLayout(getActivity());
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(strippedWidth, LinearLayout.LayoutParams.WRAP_CONTENT);

        View verticalBorder = new View(getActivity());
        verticalBorder.setBackgroundColor(getResources().getColor(R.color.border));
        LinearLayout.LayoutParams vlp = new LinearLayout.LayoutParams(1, LinearLayout.LayoutParams.MATCH_PARENT);
        verticalBorder.setLayoutParams(vlp);

        String subjName = SubjectsDao.getSubjectName(se.getSubjectId(), sqliteDatabase);
        TextView tv1 = new TextView(getActivity());
        tv1.setLayoutParams(p);
        tv1.setText(subjName);
        tv1.setPadding(15, 10, 0, 10);
        tv1.setTextSize(18);
        tv1.setTextColor(ContextCompat.getColor(context, R.color.dark_black));
        horizontalLayout.addView(tv1);

        View verticalBorder2 = new View(getActivity());
        verticalBorder2.setBackgroundColor(ContextCompat.getColor(context, R.color.border));
        verticalBorder2.setLayoutParams(vlp);
        horizontalLayout.addView(verticalBorder2);

        EditText ed1 = new EditText(getActivity());
        ed1.setTag(tag);
        tag++;
        ed1.setLayoutParams(p);
        ed1.setText(se.getMaxMark() + "");
        ed1.setGravity(Gravity.CENTER);
        ed1.setInputType(InputType.TYPE_CLASS_NUMBER);
        ed1.addTextChangedListener(new MarksTextWatcher(ed1));
        horizontalLayout.addView(ed1);

        View verticalBorder3 = new View(getActivity());
        verticalBorder3.setBackgroundColor(ContextCompat.getColor(context, R.color.border));
        verticalBorder3.setLayoutParams(vlp);
        horizontalLayout.addView(verticalBorder3);

        EditText ed2 = new EditText(getActivity());
        ed2.setTag(tag);
        tag++;
        ed2.setLayoutParams(p);
        ed2.setText(se.getMinMark() + "");
        ed2.setGravity(Gravity.CENTER);
        ed2.setInputType(InputType.TYPE_CLASS_NUMBER);
        ed2.addTextChangedListener(new MarksTextWatcher(ed2));
        horizontalLayout.addView(ed2);


        verticalLayout.addView(horizontalLayout);
        View horizontalBorder = new View(getActivity());
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
                ses.setUpdatable(true);
                if (ses.getMinMark() > ses.getMaxMark()) {
                    ses.setNullCheck(false);
                }
            }
            subjectExams.set(index, ses);
        }
    }

    class SubExams {
        private int subjectId;
        private int maxMark;
        private int minMark;
        private boolean nullCheck;
        private boolean updatable;

        public SubExams(int subjectId, int maxMark, int minMark) {
            this.maxMark = maxMark;
            this.minMark = minMark;
            this.subjectId = subjectId;
            this.nullCheck = true;
            this.updatable = false;
        }

        public boolean isUpdatable() {
            return updatable;
        }

        public void setUpdatable(boolean updatable) {
            this.updatable = updatable;
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

    private boolean isSavable() {
        for (SubExams xm : subjectExams) {
            if (!xm.isNullCheck()) {
                return false;
            }
        }
        return true;
    }

    class CalledSubmit extends AsyncTask<Void, Void, Void> {
        ProgressDialog pDialog = new ProgressDialog(getActivity());

        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Submitting marks...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            updateSubjectExams();
            return null;
        }

        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            table.removeAllViews();
            updateExamSpinner();
            examAdapter.notifyDataSetChanged();
            generateTable();
            pDialog.dismiss();
        }
    }

    private void updateSubjectExams() {
        for (SubExams se : subjectExams) {
            if (se.isUpdatable()) {
                String sql = "update subjectexams set MaximumMark = " + se.getMaxMark() + ", FailMark = " + se.getMinMark() + " where " +
                        "SubjectId = " + se.getSubjectId() + " and ClassId = " + classId + " and ExamId = " + examId;
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

}
