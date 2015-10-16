package in.teacher.examfragment;

import in.teacher.activity.R;
import in.teacher.adapter.Capitalize;
import in.teacher.adapter.MarksAdapter;
import in.teacher.dao.ClasDao;
import in.teacher.dao.ExamsDao;
import in.teacher.dao.ExmAvgDao;
import in.teacher.dao.MarksDao;
import in.teacher.dao.SectionDao;
import in.teacher.dao.StudentsDao;
import in.teacher.dao.SubjectExamsDao;
import in.teacher.dao.TempDao;
import in.teacher.examfragment.StructuredExam;
import in.teacher.sqlite.Marks;
import in.teacher.sqlite.Students;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.ReplaceFragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Created by vinkrish.
 */
public class UpdateExamMark extends Fragment {
    private Context context;
    private SQLiteDatabase sqliteDatabase;
    private Activity act;
    private int sectionId, schoolId, subjectId, subId, classId, examId, partition;
    private float maxMark;
    private List<Students> studentsArray = new ArrayList<>();
    private List<Integer> studentsArrayId = new ArrayList<>();
    private List<Boolean> studentIndicate = new ArrayList<>();
    private ArrayList<Students> studentsArrayList = new ArrayList<>();
    private List<String> studentScore = new ArrayList<>();
    private ListView lv;
    private MarksAdapter marksAdapter;
    private int index = 0, indexBound, top, firstVisible, lastVisible, totalVisible, marksCount;
    private Button previous, next, clear, submit;
    private Bitmap empty, entered;
    private StringBuffer sf = new StringBuffer();
    private TextView clasSecSub;
    private SharedPreferences sharedPref;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mark_score, container, false);

        act = AppGlobal.getActivity();
        context = AppGlobal.getContext();
        sqliteDatabase = AppGlobal.getSqliteDatabase();
        sharedPref = context.getSharedPreferences("has_partition", Context.MODE_PRIVATE);

        lv = (ListView) view.findViewById(R.id.list);
        marksAdapter = new MarksAdapter(context, studentsArrayList);
        lv.setAdapter(marksAdapter);

        initView(view);

        new CalledBackLoad().execute();

        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos,
                                    long id) {
                if (studentScore.get(index) != null && !studentScore.get(index).equals("") && studentScore.get(index).equals(".")) {
                    studentScore.set(index, "");
                }
                if (studentScore.get(index) != null && !studentScore.get(index).equals("") && Double.parseDouble(studentScore.get(index)) > maxMark) {
                    studentScore.set(index, "");
                    Toast.makeText(context, "marks entered is greater than max mark", Toast.LENGTH_SHORT).show();
                }

                index = pos;
                View v = lv.getChildAt(0);
                top = (v == null) ? 0 : v.getTop();
                for (int idx = 0; idx < studentsArray.size(); idx++) {
                    studentIndicate.set(idx, false);
                }
                Boolean b = studentIndicate.get(index);
                if (!b) {
                    studentIndicate.set(index, true);
                }
                repopulateListArray();
            }
        });

        lv.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                firstVisible = lv.getFirstVisiblePosition();
                lastVisible = lv.getLastVisiblePosition();
                totalVisible = lastVisible - firstVisible;
            }
        });

        initButton(view);

        return view;
    }

    class CalledSubmit extends AsyncTask<String, String, String> {
        ProgressDialog pDialog = new ProgressDialog(act);

        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Submitting marks...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            pushSubmit();
            return null;
        }

        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pDialog.dismiss();
            ReplaceFragment.replace(new StructuredExam(), getFragmentManager());
        }
    }

    private void pushSubmit() {
        int i = 0;
        for (String ss : studentScore) {
            if (ss == null || ss.equals(".") || ss.equals("")) studentScore.set(i, "0");
            i++;
        }
        int k = 0;
        List<Marks> mList = new ArrayList<>();
        for (Students st : studentsArray) {
            Marks m = new Marks();
            m.setSchoolId(schoolId);
            m.setExamId(examId);
            m.setSubjectId(subjectId);
            m.setStudentId(st.getStudentId());
            m.setMark(studentScore.get(k));
            //	m.setGrade(gradeScore.get(k));
            m.setSectionId(sectionId);
            mList.add(m);
            k++;
        }
        if (studentsArray.size() == marksCount) {
            MarksDao.updateMarks(mList, sqliteDatabase);
        } else {
            MarksDao.insertUpdateMarks(mList, sqliteDatabase);
        }
        int entry = ExmAvgDao.checkExmEntry(sectionId, subjectId, examId, sqliteDatabase);
        if (entry == 0) {
            ExmAvgDao.insertIntoExmAvg(classId, sectionId, subjectId, examId, schoolId, sqliteDatabase);
        }
        ExmAvgDao.insertAvgIntoExmAvg(sectionId, subjectId, examId, schoolId, sqliteDatabase);
        ExmAvgDao.checkExmMarkEmpty(examId, sectionId, subjectId, schoolId, sqliteDatabase);
        if (partition == 1) {
            updatePartitionMarks();
        }
    }

    private void updatePartitionMarks() {
        int subjectId1 = 0;
        int subjectId2 = 0;
        Cursor c1 = sqliteDatabase.rawQuery("select TheorySubjectId, PracticalSubjectId from subjects " +
                "where has_partition=1 and SubjectId=" + subId, null);
        c1.moveToFirst();
        while (!c1.isAfterLast()) {
            subjectId1 = c1.getInt(c1.getColumnIndex("TheorySubjectId"));
            subjectId2 = c1.getInt(c1.getColumnIndex("PracticalSubjectId"));
            c1.moveToNext();
        }
        c1.close();

        Cursor c2 = sqliteDatabase.rawQuery("select StudentId, sum(Mark) as Mark " +
                "from marks where (SubjectId=" + subjectId1 + " or SubjectId=" + subjectId2 + ") and ExamId=" + examId + " group by ExamId, StudentId", null);
        c2.moveToFirst();
        while (!c2.isAfterLast()) {
            int val1 = c2.getInt(c2.getColumnIndex("StudentId"));
            int val2 = c2.getInt(c2.getColumnIndex("Mark"));
            String sql = "update marks set Mark='" + val2 + "' where StudentId=" + val1 + " and ExamId=" + examId + " and SubjectId=" + subId;
            try {
                sqliteDatabase.execSQL(sql);
                ContentValues cv = new ContentValues();
                cv.put("Query", sql);
                sqliteDatabase.insert("uploadsql", null, cv);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            c2.moveToNext();
        }
        c2.close();
    }

    private void initView(View view) {
        clasSecSub = (TextView) view.findViewById(R.id.clasSecSub);
        empty = BitmapFactory.decodeResource(this.getResources(), R.drawable.deindicator);
        entered = BitmapFactory.decodeResource(this.getResources(), R.drawable.indicator);

        previous = (Button) view.findViewById(R.id.previous);
        next = (Button) view.findViewById(R.id.next);
        submit = (Button) view.findViewById(R.id.submit);
        clear = (Button) view.findViewById(R.id.clear);
        TextView maxMarkTv = (TextView) view.findViewById(R.id.maxmark);

        Temp t = TempDao.selectTemp(sqliteDatabase);
        schoolId = t.getSchoolId();
        classId = t.getCurrentClass();
        sectionId = t.getCurrentSection();
        subjectId = t.getCurrentSubject();
        subId = t.getSubjectId();
        examId = t.getExamId();

        maxMark = SubjectExamsDao.getExmMaxMark(classId, examId, subjectId, sqliteDatabase);
        maxMarkTv.setText(maxMark + "");
        marksCount = MarksDao.getMarksCount(examId, subjectId, sqliteDatabase);

        view.findViewById(R.id.enter_grade).setBackgroundColor(Color.TRANSPARENT);
    }

    private void initButton(final View view) {
        int[] buttonIds = {R.id.one, R.id.two, R.id.three, R.id.four, R.id.five, R.id.six, R.id.seven,
                R.id.eight, R.id.nine, R.id.zero, R.id.decimal, R.id.minus};
        for (int i = 0; i < 12; i++) {
            Button b = (Button) view.findViewById(buttonIds[i]);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v.getId() == R.id.one) updateScoreField("1");
                    if (v.getId() == R.id.two) updateScoreField("2");
                    if (v.getId() == R.id.three) updateScoreField("3");
                    if (v.getId() == R.id.four) updateScoreField("4");
                    if (v.getId() == R.id.five) updateScoreField("5");
                    if (v.getId() == R.id.six) updateScoreField("6");
                    if (v.getId() == R.id.seven) updateScoreField("7");
                    if (v.getId() == R.id.eight) updateScoreField("8");
                    if (v.getId() == R.id.nine) updateScoreField("9");
                    if (v.getId() == R.id.zero) updateScoreField("0");
                    if (v.getId() == R.id.decimal) updateScoreField(".");
                    if (v.getId() == R.id.minus) {
                        studentScore.set(index, "-1");
                        repopulateListArray();
                    }
                }
            });
        }

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                studentScore.set(index, "");
                repopulateListArray();
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (studentScore.get(index) != null
                        && !studentScore.get(index).equals("")
                        && studentScore.get(index).equals(".")) {
                    studentScore.set(index, "");
                }
                if (studentScore.get(index) != null
                        && !studentScore.get(index).equals("")
                        && Double.parseDouble(studentScore.get(index)) > maxMark) {
                    String s = "";
                    studentScore.set(index, s);
                    Toast.makeText(context, "Marks Entered is Greater than Max Mark", Toast.LENGTH_SHORT).show();
                } else {
                    if (index != 0) index--;
                    for (int idx = 0; idx < studentsArray.size(); idx++)
                        studentIndicate.set(idx, false);

                    Boolean b = studentIndicate.get(index);
                    if (!b) studentIndicate.set(index, true);
                }
                repopulateListArray();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (studentScore.get(index) != null
                        && !studentScore.get(index).equals("")
                        && studentScore.get(index).equals(".")) {
                    studentScore.set(index, "");
                }
                if (studentScore.get(index) != null
                        && !studentScore.get(index).equals("")
                        && Double.parseDouble(studentScore.get(index)) > maxMark) {
                    studentScore.set(index, "");
                    Toast.makeText(context, "Marks Entered is Greater than Max Mark", Toast.LENGTH_SHORT).show();
                } else {
                    if (index < indexBound - 1) index++;
                    for (int idx = 0; idx < studentsArray.size(); idx++)
                        studentIndicate.set(idx, false);

                    Boolean b = studentIndicate.get(index);
                    if (!b) studentIndicate.set(index, true);
                }
                repopulateListArray();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (studentScore.get(index) != null
                        && !studentScore.get(index).equals("")
                        && !studentScore.get(index).equals(".")
                        && Double.parseDouble(studentScore.get(index)) > maxMark) {
                    String s = "";
                    studentScore.set(index, s);
                    Toast.makeText(context, "Marks Entered is Greater than Max Mark", Toast.LENGTH_SHORT).show();
                    repopulateListArray();
                } else {
                    Toast.makeText(context, "marks entered has been saved", Toast.LENGTH_LONG).show();
                    new CalledSubmit().execute();
                }
            }
        });
    }

    private void updateScoreField(String upScore) {
        try {
            if (studentScore.get(index) != null
                    && !studentScore.get(index).equals("")
                    && !studentScore.get(index).equals("-1")) {
                studentScore.set(index, studentScore.get(index) + upScore);
            } else {
                studentScore.set(index, upScore);
            }
        } catch (NumberFormatException e) {
            studentScore.set(index, upScore);
        }
        repopulateListArray();
    }

    private void populateListArray() {
        indexBound = studentsArray.size();
        int idx = 0;
        for (Students s : studentsArray) {
            if (idx == 0) {
                studentIndicate.set(idx, true);
                studentsArrayList.add(new Students(s.getRollNoInClass(), Capitalize.capitalThis(s.getName()), studentScore.get(idx), entered));
            } else {
                studentsArrayList.add(new Students(s.getRollNoInClass(), Capitalize.capitalThis(s.getName()), studentScore.get(idx), empty));
            }
            idx++;
        }
        marksAdapter.notifyDataSetChanged();
        lv.performItemClick(lv.getAdapter().getView(index, null, null), index, lv.getItemIdAtPosition(index));
    }

    private void repopulateListArray() {
        studentsArrayList.clear();
        int idx = 0;
        for (Students s : studentsArray) {
            if (studentIndicate.get(idx)) {
                studentsArrayList.add(new Students(s.getRollNoInClass(), Capitalize.capitalThis(s.getName()), studentScore.get(idx), entered));
            } else {
                studentsArrayList.add(new Students(s.getRollNoInClass(), Capitalize.capitalThis(s.getName()), studentScore.get(idx), empty));
            }
            idx++;
        }
        marksAdapter.notifyDataSetChanged();
        if (index == lastVisible) lv.setSelectionFromTop(index, top);
        else if (index < firstVisible) lv.setSelectionFromTop(index, firstVisible - totalVisible);
        else lv.setSelection(firstVisible);
    }

    class CalledBackLoad extends AsyncTask<Void, Void, Void> {
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            String subjectName = SubjectExamsDao.selectSubjectName(subjectId, sqliteDatabase);
            String className = ClasDao.getClassName(classId, sqliteDatabase);
            String sectionName = SectionDao.getSectionName(sectionId, sqliteDatabase);
            String examName = ExamsDao.selectExamName(examId, sqliteDatabase);
            sf.append(className).append("-").append(sectionName).append("   " + subjectName).append("   " + examName);

            partition = sharedPref.getInt("partition", 0);
            if (partition == 1)
                studentsArray = StudentsDao.selectStudents2(sectionId, subId, sqliteDatabase);
            else
                studentsArray = StudentsDao.selectStudents2(sectionId, subjectId, sqliteDatabase);

            for (int idx = 0; idx < studentsArray.size(); idx++)
                studentIndicate.add(false);

            for (Students s : studentsArray)
                studentsArrayId.add(s.getStudentId());

            List<String> mList = MarksDao.selectMarks(examId, subjectId, studentsArrayId, sqliteDatabase);
            for (String m : mList)
                studentScore.add(m);

            return null;
        }

        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            clasSecSub.setText(sf);
            populateListArray();
            if (studentsArray.size() == 0) {
                Toast.makeText(context, "No students!", Toast.LENGTH_SHORT).show();
                ReplaceFragment.replace(new StructuredExam(), getFragmentManager());
            }
        }
    }

}