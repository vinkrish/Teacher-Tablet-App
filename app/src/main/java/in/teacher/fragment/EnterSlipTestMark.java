package in.teacher.fragment;

import in.teacher.activity.R;
import in.teacher.adapter.Capitalize;
import in.teacher.adapter.MarksAdapter;
import in.teacher.adapter.StudentsSort;
import in.teacher.dao.ClasDao;
import in.teacher.dao.SectionDao;
import in.teacher.dao.SlipTestMarkDao;
import in.teacher.dao.SlipTesttDao;
import in.teacher.dao.StAvgDao;
import in.teacher.dao.StudentsDao;
import in.teacher.dao.SubjectExamsDao;
import in.teacher.dao.TeacherDao;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.SlipTestMark;
import in.teacher.sqlite.SlipTestt;
import in.teacher.sqlite.Students;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.PKGenerator;
import in.teacher.util.PercentageSlipTest;
import in.teacher.util.ReplaceFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

public class EnterSlipTestMark extends Fragment {
    private Activity activity;
    private Context context;
    private SQLiteDatabase sqliteDatabase;
    private int classId, sectionId, subjectId, schoolId;
    private float maxMark;
    private Long slipTestId;
    private List<Students> studentsArray;
    private List<Boolean> studentIndicate = new ArrayList<>();
    private ArrayList<Students> studentsArrayList = new ArrayList<>();
    private List<String> studentScore = new ArrayList<>();
    private ListView lv;
    private MarksAdapter marksAdapter;
    private int index = 0, indexBound, top, lastVisible, firstVisible, totalVisible;
    private Bitmap empty, entered;
    private StringBuffer sf = new StringBuffer();
    private TextView clasSecSub;
    private SlipTestt st;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.enter_mark, container, false);
        activity = AppGlobal.getActivity();
        context = AppGlobal.getContext();
        sqliteDatabase = AppGlobal.getSqliteDatabase();

        lv = (ListView) view.findViewById(R.id.list);
        marksAdapter = new MarksAdapter(context, studentsArrayList);
        lv.setAdapter(marksAdapter);

        clasSecSub = (TextView) view.findViewById(R.id.clasSecSub);
        empty = BitmapFactory.decodeResource(this.getResources(), R.drawable.deindicator);
        entered = BitmapFactory.decodeResource(this.getResources(), R.drawable.indicator);

        Button previous = (Button) view.findViewById(R.id.previous);
        Button next = (Button) view.findViewById(R.id.next);
        Button submit = (Button) view.findViewById(R.id.submit);
        Button clear = (Button) view.findViewById(R.id.clear);

        Temp t = TempDao.selectTemp(sqliteDatabase);
        schoolId = t.getSchoolId();
        classId = t.getCurrentClass();
        sectionId = t.getCurrentSection();
        subjectId = t.getCurrentSubject();

        slipTestId = PKGenerator.returnPrimaryKey(schoolId);
        maxMark = SlipTesttDao.selectSlipTestMaxMark(-1, sqliteDatabase);
        TextView maxMarkView = (TextView) view.findViewById(R.id.maxmark);
        maxMarkView.setText(maxMark + "");

        new CalledBackLoad().execute();

        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos,
                                    long id) {
                if (studentScore.get(index) != null
                        && !studentScore.get(index).equals("")
                        && studentScore.get(index).equals(".")) {
                    studentScore.set(index, "");
                }
                if (studentScore.get(index) != null
                        && !studentScore.get(index).equals("")
                        && Double.parseDouble(studentScore.get(index)) > maxMark) {
                    studentScore.set(index, "");
                    Toast.makeText(context, "marks entered is greater than max mark", Toast.LENGTH_SHORT).show();
                }
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
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
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
                    String s = "";
                    studentScore.set(index, s);
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

        return view;
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
            pushSubmit();
            return null;
        }

        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            pDialog.dismiss();
            ReplaceFragment.replace(new Dashbord(), getFragmentManager());
        }

    }

    private void pushSubmit() {
        int i = 0;
        for (String ss : studentScore) {
            if (ss == null || ss.equals(".") || ss.equals(""))
                studentScore.set(i, "0");
            i++;
        }
        int j = 0;
        List<SlipTestMark> mList = new ArrayList<>();
        for (Students sA : studentsArray) {
            SlipTestMark m = new SlipTestMark();
            m.setSchoolId(schoolId);
            m.setClassId(classId);
            m.setSectionId(sectionId);
            m.setSlipTestId(slipTestId);
            m.setSubjectId(subjectId);
            m.setStudentId(sA.getStudentId());
            m.setMark(studentScore.get(j));
            mList.add(m);
            j++;
        }
        st.setSlipTestId(slipTestId);
        st.setSubmissionDate(getToday());
        sqliteDatabase.execSQL("update sliptest set SlipTestId=" + slipTestId + ",SubmissionDate='" + getToday() + "' where SlipTestId=-1");
        SlipTesttDao.insertST2(st, sqliteDatabase);
        SlipTestMarkDao.insertSTMark(mList, schoolId, sqliteDatabase);
        SlipTestMarkDao.insertStAvg(classId, sectionId, subjectId, sqliteDatabase);
        double updatedSTAvg = PercentageSlipTest.findSlipTestPercentage(sectionId, subjectId, sqliteDatabase);
        StAvgDao.updateSlipTestAvg(sectionId, subjectId, updatedSTAvg, sqliteDatabase);
    }

    private String getToday() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date today = new Date();
        return dateFormat.format(today);
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
                studentsArrayList.add(new Students(s.getRollNoInClass(), Capitalize.capitalThis(s.getName()), s.getScore(), entered));
            } else {
                studentsArrayList.add(new Students(s.getRollNoInClass(), Capitalize.capitalThis(s.getName()), s.getScore(), empty));
            }
            studentScore.add(s.getScore());
            idx++;
        }
        marksAdapter.notifyDataSetChanged();
        lv.setSelection(index);
        //	lv.performItemClick(lv.getAdapter().getView(index, null, null), index, lv.getItemIdAtPosition(index));
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
        if (index == lastVisible)
            lv.setSelectionFromTop(index - 1, top);
        else if (index < firstVisible)
            lv.setSelectionFromTop(index, firstVisible - totalVisible);
        else
            lv.setSelection(firstVisible);
    }

    class CalledBackLoad extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String subjectName = SubjectExamsDao.selectSubjectName(subjectId, sqliteDatabase);
            String className = ClasDao.getClassName(classId, sqliteDatabase);
            String sectionName = SectionDao.getSectionName(sectionId, sqliteDatabase);

            String portionName = SlipTesttDao.selectSlipTestName(-1, sqliteDatabase);
            sf.append(className).append("-").append(sectionName).append("   " + subjectName).append("   " + portionName);

            st = SlipTesttDao.selectSlipTest(sqliteDatabase);

            studentsArray = StudentsDao.selectStudents2("" + sectionId, subjectId, sqliteDatabase);
            Collections.sort(studentsArray, new StudentsSort());
            for (int idx = 0; idx < studentsArray.size(); idx++)
                studentIndicate.add(false);

            return null;
        }

        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (sf.length() > 55) clasSecSub.setText(sf.substring(0, 53) + "...");
            else clasSecSub.setText(sf);
            populateListArray();
            if (studentsArray.size() == 0) getFragmentManager().popBackStack();
        }
    }

}
