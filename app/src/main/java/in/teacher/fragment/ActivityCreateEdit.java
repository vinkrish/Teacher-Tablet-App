package in.teacher.fragment;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;;

import java.util.ArrayList;
import java.util.List;

import in.teacher.activity.R;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.NestedListView;

/**
 * Created by vinkrish on 28/10/15.
 */
public class ActivityCreateEdit extends Fragment {
    private Context context;
    private SQLiteDatabase sqliteDatabase;
    private Spinner classSpinner, sectionSpinner, examSpinner, subjectSpinner;
    private int teacherId, classId, sectionId, examId, subjectId, activityId;
    final List<Integer> examIdList = new ArrayList<>();
    List<String> examNameList = new ArrayList<>();
    final List<Integer> sectionIdList = new ArrayList<>();
    List<String> sectionNameList = new ArrayList<>();
    final List<Integer> subjectIdList = new ArrayList<>();
    List<String> subjectNameList = new ArrayList<>();
    private Button createActivity;
    private ListView listView;
    private ActivityAdapter activityAdapter;
    private List<ActivityItem> activiyItemList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.act_create_edit, container, false);
        context = AppGlobal.getContext();
        sqliteDatabase = AppGlobal.getSqliteDatabase();

        initView(view);

        createActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        activityAdapter = new ActivityAdapter(context, activiyItemList);
        listView.setAdapter(activityAdapter);

        return view;
    }

    private void initView(View view) {
        createActivity = (Button) view.findViewById(R.id.create_act);
        listView = (ListView) view.findViewById(R.id.listView);

        classSpinner = (Spinner) view.findViewById(R.id.classSpinner);
        sectionSpinner = (Spinner) view.findViewById(R.id.sectionSpinner);
        examSpinner = (Spinner) view.findViewById(R.id.examSpinner);
        subjectSpinner = (Spinner) view.findViewById(R.id.subjectSpinner);

        initClassSpinner();
    }

    private void initClassSpinner() {
        Temp t = TempDao.selectTemp(sqliteDatabase);
        teacherId = t.getTeacherId();
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

        classSpinner.setSelection(classInChargePos);

        classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                classId = classInchargeList.get(position);
                initExamSpinner();
                initSectionSpinner();
                examAdapter.notifyDataSetChanged();
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
                    initSubjectSpinner();
                    subjectAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sectionId = sectionIdList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    subjectId = subjectIdList.get(position);
                    createActivity.setActivated(true);
                    resetActivityList();
                    activityAdapter.notifyDataSetChanged();
                    //setListViewHeightBasedOnChildren(listView);
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

    private void resetActivityList() {

        Log.d("log", sectionId + " - " + examId + " - " + subjectId);

        activiyItemList.clear();
        Cursor c = sqliteDatabase.rawQuery("select ActivityId, ActivityName, MaximumMark, Weightage, Calculation from activity where " +
                "SectionId = " + sectionId + " and ExamId = " + examId + " and SubjectId = " + subjectId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            ActivityItem ai = new ActivityItem();
            ai.setActId(c.getInt(c.getColumnIndex("ActivityId")));
            ai.setActName(c.getString(c.getColumnIndex("ActivityName")));
            ai.setMaxMark(c.getInt(c.getColumnIndex("MaximumMark")));
            int calculation = c.getInt(c.getColumnIndex("Calculation"));
            ai.setCalculation(calculation);
            if (calculation == 0) {
                ai.setWeightage(c.getInt(c.getColumnIndex("Weightage")) + "");
            } else {
                ai.setWeightage(" - ");
            }
            activiyItemList.add(ai);
            c.moveToNext();
        }
        c.close();
    }

        public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    public class ActivityAdapter extends ArrayAdapter<ActivityItem> {
        private List<ActivityItem> data = new ArrayList<>();
        private LayoutInflater inflater;

        public ActivityAdapter(Context context, List<ActivityItem> objects) {
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

            ActivityItem listItem = data.get(position);
            holder.text1.setText(listItem.getActName());
            holder.text2.setText(listItem.getMaxMark()+"");
            holder.text3.setText(listItem.getWeightage());

            return row;
        }

        public class RecordHolder {
            public TextView text1;
            public TextView text2;
            public TextView text3;
        }

    }

    class ActivityItem {
        private int actId;
        private String actName;
        private int maxMark;
        private String weightage;
        private int calculation;

        public int getCalculation() {
            return calculation;
        }

        public void setCalculation(int calculation) {
            this.calculation = calculation;
        }

        public int getActId() {
            return actId;
        }

        public void setActId(int actId) {
            this.actId = actId;
        }

        public String getActName() {
            return actName;
        }

        public void setActName(String actName) {
            this.actName = actName;
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
}
