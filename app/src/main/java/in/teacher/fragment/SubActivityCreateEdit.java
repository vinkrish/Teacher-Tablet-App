package in.teacher.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import in.teacher.activity.R;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.Temp;
import in.teacher.util.AnimationUtils;
import in.teacher.util.AppGlobal;
import in.teacher.util.CommonDialogUtils;
import in.teacher.util.PKGenerator;

/**
 * Created by vinkrish on 28/10/15.
 * Don't expect comments explaining every piece of code, class and function names are self explanatory.
 */
public class SubActivityCreateEdit extends Fragment {
    private Context context;
    private SQLiteDatabase sqliteDatabase;
    private Spinner classSpinner, sectionSpinner, examSpinner, subjectSpinner, activitySpinner, bestOf;
    private int teacherId, classId, sectionId, examId, subjectId, subActivityPos, schoolId, subActCounter, width1, width2, calculationGlobal, tag, rgCounter;
    private long generatedId, activityId;
    final List<Integer> examIdList = new ArrayList<>();
    List<String> examNameList = new ArrayList<>();
    final List<Integer> sectionIdList = new ArrayList<>();
    List<String> sectionNameList = new ArrayList<>();
    final List<Integer> subjectIdList = new ArrayList<>();
    List<String> subjectNameList = new ArrayList<>();
    final List<Long> activityIdList = new ArrayList<>();
    List<String> activityNameList = new ArrayList<>();
    private Button createSubActivity, cancelSubActCreation;
    private ListView listView;
    private SubActivityAdapter subActivityAdapter;
    private List<SubActivityItem> subactivityItemList = new ArrayList<>();
    private List<SubActivityItem> subActCreateList = new ArrayList<>();
    private RadioGroup radioGroup;
    private RadioButton sum, avg, best;
    private LinearLayout createLayout;
    private TextView countTV;
    private ScrollView scrollView;
    private TableLayout table;
    private ArrayAdapter<String> bestOfAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.subact_create_edit, container, false);

        context = AppGlobal.getContext();
        sqliteDatabase = AppGlobal.getSqliteDatabase();

        initView(view);
        init();

        cancelSubActCreation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subActCreateList.clear();
                table.removeAllViews();
                subActCounter = 1;
                countTV.setText(subActCounter + "");
                createSubActivity.setVisibility(View.VISIBLE);
                createLayout.setVisibility(View.GONE);
                scrollView.setVisibility(View.GONE);
            }
        });

        view.findViewById(R.id.create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createSubActivityAction();
            }
        });

        view.findViewById(R.id.minus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (subActCounter != 1) {
                    subActCounter--;
                    countTV.setText(subActCounter + "");
                    subActCreateList.remove(subActCounter);
                    table.removeAllViews();
                    generateTable();
                }
            }
        });

        view.findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subActCounter++;
                countTV.setText(subActCounter + "");

                SubActivityItem ai = new SubActivityItem();
                ai.setSubactId(0);
                ai.setSubactName("");
                ai.setCalculation(calculationGlobal);
                ai.setWeightage(0 + "");
                ai.setNullCheck(false);

                subActCreateList.add(ai);
                table.removeAllViews();
                generateTable();
            }
        });

        view.post(new Runnable() {
            @Override
            public void run() {
                animateView();
            }
        });

        return view;
    }

    private void animateView(){
        AnimationUtils.alphaTranslate(createSubActivity, context);
    }

    private void initView(View view) {
        createSubActivity = (Button) view.findViewById(R.id.create_subact);
        listView = (ListView) view.findViewById(R.id.listView);

        classSpinner = (Spinner) view.findViewById(R.id.classSpinner);
        sectionSpinner = (Spinner) view.findViewById(R.id.sectionSpinner);
        examSpinner = (Spinner) view.findViewById(R.id.examSpinner);
        subjectSpinner = (Spinner) view.findViewById(R.id.subjectSpinner);
        activitySpinner = (Spinner) view.findViewById(R.id.activitySpinner);

        radioGroup = (RadioGroup) view.findViewById(R.id.radio_group);

        sum = (RadioButton) view.findViewById(R.id.sum);
        avg = (RadioButton) view.findViewById(R.id.average);
        best = (RadioButton) view.findViewById(R.id.best);

        bestOf = (Spinner) view.findViewById(R.id.bestof);

        createLayout = (LinearLayout) view.findViewById(R.id.create_layout);

        countTV = (TextView) view.findViewById(R.id.activity_count);
        cancelSubActCreation = (Button) view.findViewById(R.id.cancel);

        scrollView = (ScrollView) view.findViewById(R.id.scrollView);

        subActCounter = 1;

        initClassSpinner();
    }

    private void init() {
        tag = 0;
        table = new TableLayout(getActivity());
        scrollView.addView(table);
        subActivityAdapter = new SubActivityAdapter(context, subactivityItemList);
        listView.setAdapter(subActivityAdapter);

        createSubActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (createSubActivity.isActivated()) {
                    createSubActivity.setVisibility(View.GONE);
                    createLayout.setVisibility(View.VISIBLE);

                    SubActivityItem ai = new SubActivityItem();
                    ai.setSubactId(0);
                    ai.setSubactName("");
                    ai.setCalculation(calculationGlobal);
                    ai.setWeightage(0 + "");
                    ai.setNullCheck(false);

                    subActCreateList.add(ai);

                    scrollView.setVisibility(View.VISIBLE);

                    generateTable();
                    //activityCreateDialog();
                } else
                    CommonDialogUtils.displayAlertWhiteDialog(getActivity(), "Please select exam, subject and activity");
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                subActivityPos = position;
                subActivityUpdateDialog();
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.sum:
                        if (rgCounter != 0) {
                            calculationGlobal = -1;
                            updateSubActCalculation();
                        }
                        break;
                    case R.id.average:
                        if (rgCounter != 0) {
                            calculationGlobal = 0;
                            updateSubActCalculation();
                        }
                        break;
                    case R.id.best:
                        if (rgCounter != 0) {
                            calculationGlobal = bestOf.getSelectedItemPosition() + 1;
                            updateSubActCalculation();
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        width1 = (width / 2) - 2;
        width2 = width / 4;
    }

    private void generateTable() {
        tag = 0;
        for (SubActivityItem item : subActCreateList) {
            TableRow tableRow = tableRow();
            table.addView(tableRow);
        }
    }

    private TableRow tableRow() {

        TableRow tableRowForTable = new TableRow(this.context);
        TableRow.LayoutParams params = new TableRow.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        LinearLayout verticalLayout = new LinearLayout(getActivity());
        verticalLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout horizontalLayout = new LinearLayout(getActivity());
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(width1, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(width2, LinearLayout.LayoutParams.WRAP_CONTENT);

        View verticalBorder = new View(getActivity());
        verticalBorder.setBackgroundColor(getResources().getColor(R.color.border));
        LinearLayout.LayoutParams vlp = new LinearLayout.LayoutParams(1, LinearLayout.LayoutParams.MATCH_PARENT);
        verticalBorder.setLayoutParams(vlp);
        horizontalLayout.addView(verticalBorder);

        EditText ed = new EditText(getActivity());
        ed.setTag(tag);
        tag++;
        ed.setLayoutParams(p1);
        ed.setGravity(Gravity.CENTER);
        ed.setHint("SubActivity Name");
        ed.setInputType(InputType.TYPE_CLASS_TEXT);
        ed.addTextChangedListener(new SubActTextWatcher(ed));
        horizontalLayout.addView(ed);

        View verticalBorder2 = new View(getActivity());
        verticalBorder2.setBackgroundColor(ContextCompat.getColor(context, R.color.border));
        verticalBorder2.setLayoutParams(vlp);
        horizontalLayout.addView(verticalBorder2);

        EditText ed1 = new EditText(getActivity());
        ed1.setTag(tag);
        tag++;
        ed1.setLayoutParams(p2);
        ed1.setGravity(Gravity.CENTER);
        ed1.setHint("Maximum Marks");
        ed1.setInputType(InputType.TYPE_CLASS_NUMBER);
        ed1.addTextChangedListener(new SubActTextWatcher(ed1));
        horizontalLayout.addView(ed1);

        View verticalBorder3 = new View(getActivity());
        verticalBorder3.setBackgroundColor(ContextCompat.getColor(context, R.color.border));
        verticalBorder3.setLayoutParams(vlp);
        horizontalLayout.addView(verticalBorder3);

        EditText ed2 = new EditText(getActivity());
        ed2.setTag(tag);
        tag++;
        ed2.setLayoutParams(p2);
        ed2.setHint("Weightage");
        ed2.setGravity(Gravity.CENTER);
        ed2.setInputType(InputType.TYPE_CLASS_NUMBER);
        ed2.addTextChangedListener(new SubActTextWatcher(ed2));
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

    private class SubActTextWatcher implements TextWatcher {

        private int pos;
        private int index;
        private View view;

        private SubActTextWatcher(View view) {
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
            index = pos / 3;
            SubActivityItem ses = subActCreateList.get(index);

            if (s.toString().equals("")) {
                ses.setNullCheck(false);
            } else {
                if (pos % 3 == 0) {
                    ses.setSubactName(s.toString());
                } else if (pos % 3 == 1) {
                    ses.setMaxMark(Integer.parseInt(s.toString()));
                } else {
                    ses.setWeightage(s.toString());
                }
                ses.setNullCheck(true);
            }
            subActCreateList.set(index, ses);
        }
    }

    private void updateSubActCalculation() {
        if (subActCreateList.size() > 0) {
            for (SubActivityItem actItem : subActCreateList) {
                actItem.setCalculation(calculationGlobal);
            }
        }
        if (subactivityItemList.size() > 0) {
            for (SubActivityItem subActItem : subactivityItemList) {
                String sql = "update subactivity set Calculation = " + calculationGlobal + " where SubActivityId = " + subActItem.getSubactId();
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

        resetSubActivityList();
        subActivityAdapter.notifyDataSetChanged();
    }

    private void initClassSpinner() {
        Temp t = TempDao.selectTemp(sqliteDatabase);
        teacherId = t.getTeacherId();
        schoolId = t.getSchoolId();
        int classInchargeId = t.getClassInchargeId();

        int classInChargePos = 0;

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

        final ArrayAdapter<String> examAdapter = new ArrayAdapter<>(context, R.layout.spinner_header, examNameList);
        examAdapter.setDropDownViewResource(R.layout.spinner_droppeddown);
        examSpinner.setAdapter(examAdapter);

        final ArrayAdapter<String> sectionAdapter = new ArrayAdapter<>(context, R.layout.spinner_header, sectionNameList);
        sectionAdapter.setDropDownViewResource(R.layout.spinner_droppeddown);
        sectionSpinner.setAdapter(sectionAdapter);

        final ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(context, R.layout.spinner_header, subjectNameList);
        subjectAdapter.setDropDownViewResource(R.layout.spinner_droppeddown);
        subjectSpinner.setAdapter(subjectAdapter);

        final ArrayAdapter<String> activityAdapter = new ArrayAdapter<>(context, R.layout.spinner_header, activityNameList);
        activityAdapter.setDropDownViewResource(R.layout.spinner_droppeddown);
        activitySpinner.setAdapter(activityAdapter);

        bestOfAdapter = new ArrayAdapter<>(context, R.layout.spinner_header,
                Arrays.asList(new String[]{"1", "2", "3", "4", "5"}));
        bestOfAdapter.setDropDownViewResource(R.layout.spinner_droppeddown);
        bestOf.setAdapter(bestOfAdapter);

        classSpinner.setSelection(classInChargePos);

        classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                classId = classInchargeList.get(position);

                initExamSpinner();
                examAdapter.notifyDataSetChanged();
                examSpinner.setSelection(0);

                initSectionSpinner();
                sectionAdapter.notifyDataSetChanged();

                initSubjectSpinner();
                subjectAdapter.notifyDataSetChanged();
                subjectSpinner.setSelection(0);

                initActivitySpinner();
                activityAdapter.notifyDataSetChanged();
                activitySpinner.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sectionId = sectionIdList.get(position);

                initExamSpinner();
                examAdapter.notifyDataSetChanged();
                examSpinner.setSelection(0);

                initSubjectSpinner();
                subjectAdapter.notifyDataSetChanged();
                subjectSpinner.setSelection(0);

                initActivitySpinner();
                activityAdapter.notifyDataSetChanged();
                activitySpinner.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        examSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) examId = examIdList.get(position);
                else examId = 0;

                initSubjectSpinner();
                subjectAdapter.notifyDataSetChanged();
                subjectSpinner.setSelection(0);

                initActivitySpinner();
                activityAdapter.notifyDataSetChanged();
                activitySpinner.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) subjectId = subjectIdList.get(position);
                else subjectId = 0;

                initActivitySpinner();
                activityAdapter.notifyDataSetChanged();
                activitySpinner.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        activitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                rgCounter = 0;
                if (position != 0) {
                    activityId = activityIdList.get(position);
                    createSubActivity.setActivated(true);
                    resetSubActivityList();
                    subActivityAdapter.notifyDataSetChanged();
                } else {
                    createSubActivity.setActivated(false);
                    subactivityItemList.clear();
                    subActivityAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        bestOf.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (rgCounter != 0) {
                    if (calculationGlobal != -1 && calculationGlobal != 0) {
                        calculationGlobal = bestOf.getSelectedItemPosition() + 1;
                        updateSubActCalculation();
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void initSectionSpinner() {
        Cursor c = sqliteDatabase.rawQuery("select SectionId, SectionName from section where ClassId = " + classId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            sectionIdList.add(c.getInt(c.getColumnIndex("SectionId")));
            sectionNameList.add(c.getString(c.getColumnIndex("SectionName")));
            c.moveToNext();
        }
        c.close();
    }

    private void initExamSpinner() {
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

    private void initSubjectSpinner() {
        subjectIdList.clear();
        subjectNameList.clear();
        subjectIdList.add(0);
        subjectNameList.add("Select Subject");
        Cursor c = sqliteDatabase.rawQuery("select A.SubjectId, B.SubjectName from subjectexams A, subjects B where A.ClassId = " +
                classId + "  and A.ExamId = " + examId + " and A.SubjectId = B.SubjectId", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            subjectIdList.add(c.getInt(c.getColumnIndex("SubjectId")));
            subjectNameList.add(c.getString(c.getColumnIndex("SubjectName")));
            c.moveToNext();
        }
        c.close();
    }

    private void initActivitySpinner() {
        activityIdList.clear();
        activityNameList.clear();
        activityIdList.add(0L);
        activityNameList.add("Select Activity");
        Cursor c = sqliteDatabase.rawQuery("select ActivityId, ActivityName from activity where " +
                "SectionId = " + sectionId + " and ExamId = " + examId + " and SubjectId = " + subjectId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            activityIdList.add(c.getLong(c.getColumnIndex("ActivityId")));
            activityNameList.add(c.getString(c.getColumnIndex("ActivityName")));
            c.moveToNext();
        }
        c.close();
    }

    private void resetSubActivityList() {
        bestOfAdapter.notifyDataSetChanged();

        subactivityItemList.clear();
        Cursor c = sqliteDatabase.rawQuery("select SubActivityId, SubActivityName, MaximumMark, Weightage, Calculation from subactivity where " +
                "ActivityId = " + activityId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            SubActivityItem ai = new SubActivityItem();
            ai.setSubactId(c.getLong(c.getColumnIndex("SubActivityId")));
            ai.setSubactName(c.getString(c.getColumnIndex("SubActivityName")));
            ai.setMaxMark(c.getInt(c.getColumnIndex("MaximumMark")));
            int calculation = c.getInt(c.getColumnIndex("Calculation"));
            calculationGlobal = calculation;
            ai.setCalculation(calculation);
            if (calculation == 0) {
                ai.setWeightage(c.getInt(c.getColumnIndex("Weightage")) + "");
            } else {
                ai.setWeightage(" - ");
            }
            subactivityItemList.add(ai);
            c.moveToNext();
        }
        c.close();

        if (rgCounter == 0) {
            if (calculationGlobal == -1) {
                sum.setChecked(true);
            } else if (calculationGlobal == 0) {
                avg.setChecked(true);
            } else {
                best.setChecked(true);
                bestOf.setSelection(calculationGlobal - 1);
            }
            rgCounter++;
        }
    }

    public class SubActivityAdapter extends ArrayAdapter<SubActivityItem> {
        private List<SubActivityItem> data = new ArrayList<>();
        private LayoutInflater inflater;

        public SubActivityAdapter(Context context, List<SubActivityItem> objects) {
            super(context, R.layout.activity_list, objects);
            this.data = objects;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            RecordHolder holder;

            if (row == null) {
                row = inflater.inflate(R.layout.activity_list, parent, false);
                holder = new RecordHolder();
                holder.text1 = (TextView) row.findViewById(R.id.text1);
                holder.text2 = (TextView) row.findViewById(R.id.text2);
                holder.text3 = (TextView) row.findViewById(R.id.text3);
                row.setTag(holder);
            } else holder = (RecordHolder) row.getTag();

            if (position % 2 == 0)
                row.setBackgroundResource(R.drawable.list_selector1);
            else
                row.setBackgroundResource(R.drawable.list_selector2);

            SubActivityItem listItem = data.get(position);
            holder.text1.setText(listItem.getSubactName());
            holder.text2.setText(listItem.getMaxMark() + "");
            holder.text3.setText(listItem.getWeightage());

            return row;
        }

        public class RecordHolder {
            public TextView text1;
            public TextView text2;
            public TextView text3;
        }

    }

    class SubActivityItem {
        private long subactId;
        private String subactName;
        private int maxMark;
        private String weightage;
        private int calculation;
        private boolean nullCheck;

        public long getSubactId() {
            return subactId;
        }

        public void setSubactId(long subactId) {
            this.subactId = subactId;
        }

        public boolean isNullCheck() {
            return nullCheck;
        }

        public void setNullCheck(boolean nullCheck) {
            this.nullCheck = nullCheck;
        }

        public String getSubactName() {
            return subactName;
        }

        public void setSubactName(String subactName) {
            this.subactName = subactName;
        }

        public int getCalculation() {
            return calculation;
        }

        public void setCalculation(int calculation) {
            this.calculation = calculation;
        }

        public int getMaxMark() {
            return maxMark;
        }

        public void setMaxMark(int maxMark) {
            this.maxMark = maxMark;
        }

        public String getWeightage() {
            return weightage;
        }

        public void setWeightage(String weightage) {
            this.weightage = weightage;
        }
    }

    private void createSubActivityAction() {
        boolean canSave = true;

        for (SubActivityItem subActItem : subActCreateList) {
            if (!subActItem.isNullCheck()) {
                canSave = false;
                break;
            }
        }

        if (canSave) {

            for (SubActivityItem subActItem : subActCreateList) {
                String sql = "";
                generatedId = PKGenerator.returnPrimaryKey(schoolId);

                if (subActItem.getCalculation() == -1) {
                    sql = "insert into subactivity (SubActivityId, SchoolId, ClassId, SectionId, ExamId, " +
                            "SubjectId, ActivityId, SubActivityName, MaximumMark, Weightage, Calculation) " +
                            " values (" + generatedId + ", " + schoolId + ", " + classId + ", " + sectionId + ", " + examId +
                            ", " + subjectId + ", " + activityId + ",\"" + subActItem.getSubactName().toString().replaceAll("\n", " ").replaceAll("\"", "'") + "\"," + subActItem.getMaxMark() +
                            ", 0, -1)";
                } else if (subActItem.getCalculation() == 0) {
                    sql = "insert into subactivity (SubActivityId, SchoolId, ClassId, SectionId, ExamId, " +
                            "SubjectId, ActivityId, SubActivityName, MaximumMark, Weightage, Calculation) " +
                            " values (" + generatedId + ", " + schoolId + ", " + classId + ", " + sectionId + ", " + examId +
                            ", " + subjectId + ", " + activityId + ",\"" + subActItem.getSubactName().toString().replaceAll("\n", " ").replaceAll("\"", "'") + "\"," + subActItem.getMaxMark() +
                            ", " + subActItem.getWeightage().toString() + ", 0)";
                } else {
                    sql = "insert into subactivity (SubActivityId, SchoolId, ClassId, SectionId, ExamId, " +
                            "SubjectId, ActivityId, SubActivityName, MaximumMark, Weightage, Calculation) " +
                            " values (" + generatedId + ", " + schoolId + ", " + classId + ", " + sectionId + ", " + examId +
                            ", " + subjectId + ", " + activityId + ",\"" + subActItem.getSubactName().toString().replaceAll("\n", " ").replaceAll("\"", "'") + "\"," + subActItem.getMaxMark() +
                            ", 0, " + (bestOf.getSelectedItemPosition() + 1) + ")";
                }

                try {
                    sqliteDatabase.execSQL(sql);
                    ContentValues cv = new ContentValues();
                    cv.put("Query", sql);
                    sqliteDatabase.insert("uploadsql", null, cv);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            resetSubActivityList();
            subActivityAdapter.notifyDataSetChanged();
            cancelSubActCreation.performClick();
        } else
            CommonDialogUtils.displayAlertWhiteDialog(getActivity(), "subactivity name and maximum marks are compulsory");
    }

    private Dialog subActivityUpdateDialog() {
        final Dialog dialog = new Dialog(getActivity(), R.style.DialogSlideAnim);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_edit_dialog);
        dialog.setCancelable(false);

        final TextInputLayout hideLayout = (TextInputLayout) dialog.findViewById(R.id.hided);

        final EditText activityName = (EditText) dialog.findViewById(R.id.activity_name);
        final EditText maxMark = (EditText) dialog.findViewById(R.id.max_mark);
        final EditText weightage = (EditText) dialog.findViewById(R.id.weightage);

        final SubActivityItem ai = subactivityItemList.get(subActivityPos);

        if (ai.getCalculation() == -1) {
            activityName.setText(ai.getSubactName());
            maxMark.setText(ai.getMaxMark() + "");
            hideLayout.setVisibility(View.GONE);
        } else if (ai.getCalculation() == 0) {
            activityName.setText(ai.getSubactName());
            maxMark.setText(ai.getMaxMark() + "");
            weightage.setText(ai.getWeightage() + "");
            hideLayout.setVisibility(View.VISIBLE);
        } else {
            activityName.setText(ai.getSubactName());
            maxMark.setText(ai.getMaxMark() + "");
            hideLayout.setVisibility(View.GONE);
        }

        dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                confirmDelete(ai.subactId);
            }
        });

        dialog.findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sql = "";

                if (sum.isChecked()) {
                    if (!activityName.getText().toString().equals("") && !maxMark.getText().toString().equals("")) {
                        sql = "update subactivity set SubActivityName = \"" + activityName.getText().toString().replaceAll("\n", " ") +
                                "\", MaximumMark = " + maxMark.getText().toString() + ", Calculation = -1 " +
                                "where SubActivityId = " + ai.getSubactId();
                    } else {
                        CommonDialogUtils.displayAlertWhiteDialog(getActivity(), "No fields should be left blank");
                    }
                } else if (avg.isChecked()) {
                    if (!activityName.getText().toString().equals("") &&
                            !maxMark.getText().toString().equals("") &&
                            !weightage.getText().toString().equals("")) {
                        sql = "update subactivity set SubActivityName = \"" + activityName.getText().toString().replaceAll("\n", " ") +
                                "\", MaximumMark = " + maxMark.getText().toString() + ", Weightage = " + weightage.getText().toString() + " , Calculation = 0 " +
                                "where SubActivityId = " + ai.getSubactId();
                    } else {
                        CommonDialogUtils.displayAlertWhiteDialog(getActivity(), "No fields should be left blank");
                    }
                } else if (best.isChecked()) {
                    if (!activityName.getText().toString().equals("") && !maxMark.getText().toString().equals("")) {
                        sql = "update subactivity set SubActivityName = \"" + activityName.getText().toString().replaceAll("\n", " ") +
                                "\", MaximumMark = " + maxMark.getText().toString() + ", Calculation = " + (bestOf.getSelectedItemPosition() + 1) +
                                " where SubActivityId = " + ai.getSubactId();
                    } else {
                        CommonDialogUtils.displayAlertWhiteDialog(getActivity(), "No fields should be left blank");
                    }
                }

                try {
                    sqliteDatabase.execSQL(sql);
                    ContentValues cv = new ContentValues();
                    cv.put("Query", sql);
                    sqliteDatabase.insert("uploadsql", null, cv);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                dialog.dismiss();
                resetSubActivityList();
                subActivityAdapter.notifyDataSetChanged();
                //activitySpinner.setSelection(0);
            }
        });

        dialog.getWindow().setGravity(Gravity.TOP);
        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        layoutParams.y = 80;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(layoutParams);
        dialog.show();

        return dialog;
    }

    private void confirmDelete(final long subActivityId) {
        AlertDialog.Builder submitBuilder = new AlertDialog.Builder(getActivity());
        submitBuilder.setCancelable(false);
        submitBuilder.setTitle("Confirm your action");
        submitBuilder.setMessage("Do you want to delete activity ?");
        submitBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                dialog.cancel();
            }
        });
        submitBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                String sql = "delete from subactivity where SubActivityId = " + subActivityId;
                try {
                    sqliteDatabase.execSQL(sql);
                    ContentValues cv = new ContentValues();
                    cv.put("Query", sql);
                    sqliteDatabase.insert("uploadsql", null, cv);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                dialog.cancel();
                resetSubActivityList();
                subActivityAdapter.notifyDataSetChanged();
                //activitySpinner.setSelection(0);
            }
        });
        submitBuilder.show();
    }

}
