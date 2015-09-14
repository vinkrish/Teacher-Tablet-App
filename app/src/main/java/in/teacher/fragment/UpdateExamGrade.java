package in.teacher.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import in.teacher.activity.R;
import in.teacher.adapter.Capitalize;
import in.teacher.adapter.GradeAdapter;
import in.teacher.adapter.MarksAdapter;
import in.teacher.dao.ClasDao;
import in.teacher.dao.ExamsDao;
import in.teacher.dao.GradesClassWiseDao;
import in.teacher.dao.MarksDao;
import in.teacher.dao.SectionDao;
import in.teacher.dao.StudentsDao;
import in.teacher.dao.SubjectExamsDao;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.GradesClassWise;
import in.teacher.sqlite.Marks;
import in.teacher.sqlite.Students;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.ReplaceFragment;

/**
 * Created by vinkrish.
 */

public class UpdateExamGrade extends Fragment {
    private Context context;
    private SQLiteDatabase sqliteDatabase;
    private Activity act;
    private int sectionId, schoolId, subjectId, subId, classId, examId, partition;
    private List<Students> studentsArray = new ArrayList<>();
    private List<Integer> studentsArrayId = new ArrayList<>();
    private List<Boolean> studentIndicate = new ArrayList<>();
    private ArrayList<Students> studentsArrayList = new ArrayList<>();
    private List<String> studentScore = new ArrayList<>();
    private List<String> gradeList = new ArrayList<>();
    private ListView lv;
    private MarksAdapter marksAdapter;
    private GradeAdapter gradeAdapter;
    private int index = 0, indexBound, top, firstVisible, lastVisible, totalVisible, marksCount;
    private Button previous, next, clear, submit;
    private Bitmap empty, entered;
    private StringBuffer sf = new StringBuffer();
    private TextView clasSecSub;
    private SharedPreferences sharedPref;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mark_grade, container, false);

        act = AppGlobal.getActivity();
        context = AppGlobal.getContext();
        sqliteDatabase = AppGlobal.getSqliteDatabase();
        sharedPref = context.getSharedPreferences("has_partition", Context.MODE_PRIVATE);

        lv = (ListView) view.findViewById(R.id.list);
        marksAdapter = new MarksAdapter(context, studentsArrayList);
        lv.setAdapter(marksAdapter);

        gradeAdapter = new GradeAdapter(context, gradeList);
        GridView gridView = (GridView) view.findViewById(R.id.gridView);
        gridView.setAdapter(gradeAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                updateScoreField(gradeList.get(position));
            }
        });

        initView(view);

        new CalledBackLoad().execute();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos,
                                    long id) {
                index = pos;
                View v = lv.getChildAt(0);
                top = (v == null) ? 0 : v.getTop();
                for (int idx = 0; idx < studentsArray.size(); idx++)
                    studentIndicate.set(idx, false);

                Boolean b = studentIndicate.get(index);
                if (!b) studentIndicate.set(index, true);

                repopulateListArray();
            }
        });

        lv.setOnScrollListener(new AbsListView.OnScrollListener() {
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

        initButton();

        return view;
    }

    private void initView(View view) {
        clasSecSub = (TextView) view.findViewById(R.id.clasSecSub);
        empty = BitmapFactory.decodeResource(this.getResources(), R.drawable.deindicator);
        entered = BitmapFactory.decodeResource(this.getResources(), R.drawable.indicator);

        previous = (Button) view.findViewById(R.id.previous);
        next = (Button) view.findViewById(R.id.next);
        submit = (Button) view.findViewById(R.id.submit);
        clear = (Button) view.findViewById(R.id.clear);

        Temp t = TempDao.selectTemp(sqliteDatabase);
        schoolId = t.getSchoolId();
        classId = t.getCurrentClass();
        sectionId = t.getCurrentSection();
        subjectId = t.getCurrentSubject();
        subId = t.getSubjectId();
        examId = t.getExamId();

        marksCount = MarksDao.getMarksCount(examId, subjectId, sqliteDatabase);
        view.findViewById(R.id.enter_marks).setBackgroundColor(Color.TRANSPARENT);
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
            if (ss == null) studentScore.set(i, "");
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
            m.setMark("0");
            m.setGrade(studentScore.get(k));
            m.setSectionId(sectionId);
            mList.add(m);
            k++;
        }
        if (studentsArray.size() == marksCount)
            MarksDao.updateGrade(mList, sqliteDatabase);
        else
            MarksDao.insertUpdateGrade(mList, sqliteDatabase);

    }

    private void initButton() {
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "grades entered has been saved", Toast.LENGTH_LONG).show();
                new CalledSubmit().execute();
            }
        });

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
                if (index != 0) index--;
                for (int idx = 0; idx < studentsArray.size(); idx++)
                    studentIndicate.set(idx, false);

                Boolean b = studentIndicate.get(index);
                if (!b) studentIndicate.set(index, true);

                repopulateListArray();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (index < indexBound - 1) index++;
                for (int idx = 0; idx < studentsArray.size(); idx++)
                    studentIndicate.set(idx, false);

                Boolean b = studentIndicate.get(index);
                if (!b) studentIndicate.set(index, true);

                repopulateListArray();
            }
        });
    }

    private void updateScoreField(String upScore) {
        try {
            if (studentScore.get(index) != null
                    && !studentScore.get(index).equals("")
                    && !studentScore.get(index).equals("-1")) {
                studentScore.set(index, upScore);
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

            List<String> mList = MarksDao.selectGrade(examId, subjectId, studentsArrayId, sqliteDatabase);
            for (String m : mList)
                studentScore.add(m);

            List<GradesClassWise> gcwList = GradesClassWiseDao.getGradeClassWise(classId, sqliteDatabase);
            for (GradesClassWise gcw : gcwList)
                gradeList.add(gcw.getGrade());

            return null;
        }

        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            clasSecSub.setText(sf);
            populateListArray();
            gradeAdapter.notifyDataSetChanged();
            if (studentsArray.size() == 0) {
                Toast.makeText(context, "No students!", Toast.LENGTH_SHORT).show();
                ReplaceFragment.replace(new StructuredExam(), getFragmentManager());
            }
        }
    }

}
