package in.teacher.examfragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.SQLException;
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
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import in.teacher.activity.R;
import in.teacher.adapter.Capitalize;
import in.teacher.adapter.GradeAdapter;
import in.teacher.adapter.MarksAdapter;
import in.teacher.dao.ActivityGradeDao;
import in.teacher.sqlite.Activiti;
import in.teacher.util.GradeClassWiseSort;
import in.teacher.util.StudentsSort;
import in.teacher.dao.ActivitiDao;
import in.teacher.dao.ClasDao;
import in.teacher.dao.ExamsDao;
import in.teacher.dao.GradesClassWiseDao;
import in.teacher.dao.SectionDao;
import in.teacher.dao.StudentsDao;
import in.teacher.dao.SubActivityDao;
import in.teacher.dao.SubActivityGradeDao;
import in.teacher.dao.SubjectExamsDao;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.GradesClassWise;
import in.teacher.sqlite.Students;
import in.teacher.sqlite.SubActivity;
import in.teacher.sqlite.SubActivityGrade;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.CommonDialogUtils;
import in.teacher.util.PKGenerator;
import in.teacher.util.ReplaceFragment;

/**
 * Created by vinkrish.
 * Don't expect comments explaining every piece of code, class and function names are self explanatory.
 */
public class InsertSubActivityGrade extends Fragment {
    private Activity activity;
    private Context context;
    private SQLiteDatabase sqliteDatabase;
    private String activityName, subActivityName;
    private List<Students> studentsArray = new ArrayList<>();
    private List<Boolean> studentIndicate = new ArrayList<>();
    private ArrayList<Students> studentsArrayList = new ArrayList<>();
    private List<String> studentScore = new ArrayList<>();
    private List<String> gradeList = new ArrayList<>();
    private List<GradesClassWise> gradesClassWiseList = new ArrayList<>();
    private ListView lv;
    private MarksAdapter marksAdapter;
    private GradeAdapter gradeAdapter;
    private int index = 0, indexBound, top, lastVisible, firstVisible, totalVisible;
    private int schoolId, examId, subjectId, subId, classId, sectionId, calculation;
    private long activityId, subActivityId;
    private StringBuffer sf = new StringBuffer();
    private TextView clasSecSub;
    private Bitmap empty, entered;
    private SharedPreferences sharedPref;
    private Button previous, next, submit, clear;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mark_grade, container, false);

        activity = AppGlobal.getActivity();
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

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
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

        new CalledBackLoad().execute();

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
        activityId = t.getActivityId();
        subActivityId = t.getSubActivityId();

        activityName = ActivitiDao.selectActivityName(activityId, sqliteDatabase);

        SubActivity tempSubAct = SubActivityDao.getSubActivity(subActivityId, sqliteDatabase);
        subActivityName = tempSubAct.getSubActivityName();
        calculation = tempSubAct.getCalculation();

        view.findViewById(R.id.enter_marks).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReplaceFragment.replace(new InsertSubActivityMark(), getFragmentManager());
            }
        });
    }

    private void initButton() {
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                submit.setEnabled(false);
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

    class CalledSubmit extends AsyncTask<Void, Void, Void> {
        ProgressDialog pDialog = new ProgressDialog(activity);
        Boolean submitStatus = false;

        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Submitting grade...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            for (String ss : studentScore) {
                if (!ss.equals("")) {
                    submitStatus = true;
                    break;
                }
            }
            if (submitStatus) pushSubmit();
            return null;
        }

        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            pDialog.dismiss();
            if (submitStatus) {
                Toast.makeText(context, "grade entered has been saved", Toast.LENGTH_LONG).show();
                ReplaceFragment.replace(new SubActivityExam(), getFragmentManager());
            } else {
                submit.setEnabled(true);
                CommonDialogUtils.displayAlertWhiteDialog(activity, "Please enter grades to save !");
            }
        }

    }

    private void pushSubmit() {
        int i = 0;
        for (String ss : studentScore) {
            if (ss == null) studentScore.set(i, "");
            i++;
        }
        int j = 0;
        List<SubActivityGrade> mList = new ArrayList<>();
        for (Students st : studentsArray) {
            SubActivityGrade m = new SubActivityGrade();
            m.setSchoolId(schoolId);
            m.setExamId(examId);
            m.setActivityId(activityId);
            m.setSubActivityId(subActivityId);
            m.setSubjectId(subjectId);
            m.setStudentId(st.getStudentId());
            m.setGrade(studentScore.get(j));
            mList.add(m);
            j++;
        }
        SubActivityGradeDao.insertSubActivityGrade(mList, sqliteDatabase);
        activityWeightage();
    }

    /*
    * This logic is right, work out the math yourself if you don't believe.
    */
    private void activityWeightage() {
        boolean isDynamicWeightage = true;
        List<SubActivity> subActList = SubActivityDao.selectSubActivity(activityId, sqliteDatabase);
        List<Long> subActIdList = new ArrayList<>();
        List<Integer> weightageList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (SubActivity subAct : subActList) {
            sb.append(subAct.getSubActivityId() + ",");
            subActIdList.add(subAct.getSubActivityId());
            weightageList.add(subAct.getWeightage());
            if (subAct.getWeightage() == 0) isDynamicWeightage = false;
        }
        boolean exist = SubActivityGradeDao.isAllSubActGradeExist(subActIdList, sqliteDatabase);
        if (exist) {
            if (calculation == 0) {
                float totalGradeMark = 0f;
                for (Students st : studentsArray) {
                    totalGradeMark = 0f;
                    for (SubActivity subAct: subActList) {
                        float gradePoint = 0f;
                        float gradeWeightPoint;
                        String grade = SubActivityGradeDao.getSubActivityGrade(subAct.getSubActivityId(), st.getStudentId(), subAct.getSubjectId(), sqliteDatabase);

                        if (!grade.equals("")) gradePoint = getGradePoint(grade);

                        if (isDynamicWeightage) gradeWeightPoint = (float) subAct.getWeightage() / 10;
                        else gradeWeightPoint = (float) (100 / subActIdList.size()) / 10;

                        totalGradeMark += (gradePoint * gradeWeightPoint);
                    }

                    String finalGrade = getGrade(totalGradeMark);

                    String sql = "insert into activitygrade(SchoolId, ExamId, SubjectId, StudentId, ActivityId, Grade) " +
                            "values(" + schoolId + "," + examId + "," + subjectId + "," + st.getStudentId() + "," + activityId + ",'" + finalGrade + "')";

                    executeNsave(sql);
                }
            } else if (calculation == -1) {
                int totalGradePoint;
                for (Students st : studentsArray) {
                    totalGradePoint = 0;
                    for (SubActivity subAct : subActList) {
                        String grade = SubActivityGradeDao.getSubActivityGrade(subAct.getSubActivityId(), st.getStudentId(), subAct.getSubjectId(), sqliteDatabase);
                        if (!grade.equals("")) totalGradePoint += getGradePoint(grade);
                    }

                    float finalMark = (totalGradePoint / subActList.size()) * 10;
                    String finalGrade = getGrade(finalMark);

                    String sql = "insert into activitygrade(SchoolId, ExamId, SubjectId, StudentId, ActivityId, Grade) " +
                            "values(" + schoolId + "," + examId + "," + subjectId + "," + st.getStudentId() + "," + activityId + ",'" + finalGrade + "')";

                    executeNsave(sql);
                }
            } else {
                List<Float> gradePointList = new ArrayList<>();
                for (Students st : studentsArray) {
                    gradePointList.clear();

                    for (SubActivity subAct : subActList) {
                        String grade = SubActivityGradeDao.getSubActivityGrade(subAct.getSubActivityId(), st.getStudentId(), subAct.getSubjectId(), sqliteDatabase);
                        if (!grade.equals("")) gradePointList.add(getGradePoint(grade));
                        else gradePointList.add(0f);
                    }

                    float bestOfPoints = 0;
                    QuickSort quickSort = new QuickSort();
                    List<Float> sortedMarkList = quickSort.sort(gradePointList);
                    for (int cal = 0; cal < calculation; cal++)
                        bestOfPoints += sortedMarkList.get(cal);

                    float finalMark = (bestOfPoints / calculation) * 10;
                    String finalGrade = getGrade(finalMark);

                    String sql = "insert into activitygrade(SchoolId, ExamId, SubjectId, StudentId, ActivityId, Grade) " +
                            "values(" + schoolId + "," + examId + "," + subjectId + "," + st.getStudentId() + "," + activityId + ",'" + finalGrade + "')";

                    executeNsave(sql);
                }
            }
            examWeightage();
        }
    }

    /*
    * This logic is right, work out the math yourself if you don't believe.
    */
    private void examWeightage() {
        boolean isDynamicWeightage = true;
        List<Activiti> actList = ActivitiDao.selectActiviti(examId, subjectId, sectionId, sqliteDatabase);
        List<Long> actIdList = new ArrayList<>();
        List<Integer> weightageList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (Activiti Act : actList) {
            calculation = Act.getCalculation();
            sb.append(+Act.getActivityId() + ",");
            actIdList.add(Act.getActivityId());
            weightageList.add(Act.getWeightage());
            if (Act.getWeightage() == 0) isDynamicWeightage = false;
        }
        boolean exist = ActivityGradeDao.isAllActGradeExist(actIdList, sqliteDatabase);
        if (exist) {
            if (calculation == 0) {
                float totalGradeMark = 0f;
                for (Students st : studentsArray) {
                    totalGradeMark = 0f;
                    for (Activiti act : actList) {
                        float gradePoint = 0f;
                        float gradeWeightPoint;
                        String grade = ActivityGradeDao.getActivityGrade(act.getActivityId(), st.getStudentId(), act.getSubjectId(), sqliteDatabase);

                        if (!grade.equals("")) gradePoint = getGradePoint(grade);

                        if (isDynamicWeightage) gradeWeightPoint = (float) act.getWeightage() / 10;
                        else gradeWeightPoint = (float) (100 / actIdList.size()) / 10;

                        totalGradeMark += (gradePoint * gradeWeightPoint);
                    }

                    String finalGrade = getGrade(totalGradeMark);

                    String sql = "insert into marks(SchoolId, ExamId, SubjectId, StudentId, Grade) values(" +
                            schoolId + "," + examId + "," + subjectId + "," + st.getStudentId() + ",'" + finalGrade + "')";

                    executeNsave(sql);
                }
            } else if (calculation == -1) {
                int totalGradePoint;
                for (Students st : studentsArray) {
                    totalGradePoint = 0;
                    for (Activiti act : actList) {
                        String grade = ActivityGradeDao.getActivityGrade(act.getActivityId(), st.getStudentId(), act.getSubjectId(), sqliteDatabase);
                        if (!grade.equals("")) totalGradePoint += getGradePoint(grade);
                    }

                    float finalMark = (totalGradePoint / actList.size()) * 10;
                    String finalGrade = getGrade(finalMark);

                    String sql = "insert into marks(SchoolId, ExamId, SubjectId, StudentId, Grade) values(" +
                            schoolId + "," + examId + "," + subjectId + "," + st.getStudentId() + ",'" + finalGrade + "')";

                    executeNsave(sql);
                }
            } else {
                List<Float> gradePointList = new ArrayList<>();
                for (Students st : studentsArray) {
                    gradePointList.clear();

                    for (Activiti act : actList) {
                        String grade = ActivityGradeDao.getActivityGrade(act.getActivityId(), st.getStudentId(), act.getSubjectId(), sqliteDatabase);
                        if (!grade.equals("")) gradePointList.add(getGradePoint(grade));
                        else gradePointList.add(0f);
                    }

                    float bestOfPoints = 0;
                    QuickSort quickSort = new QuickSort();
                    List<Float> sortedMarkList = quickSort.sort(gradePointList);
                    for (int cal = 0; cal < calculation; cal++)
                        bestOfPoints += sortedMarkList.get(cal);

                    float finalMark = (bestOfPoints / calculation) * 10;
                    String finalGrade = getGrade(finalMark);

                    String sql = "insert into marks(SchoolId, ExamId, SubjectId, StudentId, Grade) values(" +
                            schoolId + "," + examId + "," + subjectId + "," + st.getStudentId() + ",'" + finalGrade + "')";

                    executeNsave(sql);
                }
            }
        }
    }

    private float getGradePoint(String grade) {
        int gradePoint = 0;
        for (GradesClassWise gcw : gradesClassWiseList) {
            if (grade.equals(gcw.getGrade())) {
                gradePoint = gcw.getGradePoint();
                break;
            }
        }
        return gradePoint;
    }

    private String getGrade(float mark) {
        String grade = "";
        for (GradesClassWise gcw : gradesClassWiseList) {
            if (mark <= gcw.getMarkTo()) {
                grade = gcw.getGrade();
                break;
            }
        }
        return grade;
    }

    private void executeNsave(String sql){
        try {
            sqliteDatabase.execSQL(sql);
            ContentValues cv = new ContentValues();
            cv.put("Query", sql);
            sqliteDatabase.insert("uploadsql", null, cv);
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
                studentsArrayList.add(new Students(s.getRollNoInClass(), Capitalize.capitalThis(s.getName()), s.getScore(), entered));
            } else {
                studentsArrayList.add(new Students(s.getRollNoInClass(), Capitalize.capitalThis(s.getName()), s.getScore(), empty));
            }
            studentScore.add("");
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
        if (index == lastVisible) lv.setSelectionFromTop(index - 1, top);
        else if (index < firstVisible) lv.setSelectionFromTop(index, firstVisible - totalVisible);
        else lv.setSelection(firstVisible);
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

            String examName = ExamsDao.selectExamName(examId, sqliteDatabase);

            sf.append(className).append("-").append(sectionName).append("   " + subjectName).append("   " + examName)
                    .append("   " + activityName).append("   " + subActivityName);

            int partition = sharedPref.getInt("partition", 0);
            if (partition == 1)
                studentsArray = StudentsDao.selectStudents2(sectionId, subId, sqliteDatabase);
            else
                studentsArray = StudentsDao.selectStudents2(sectionId, subjectId, sqliteDatabase);

            Collections.sort(studentsArray, new StudentsSort());
            for (int idx = 0; idx < studentsArray.size(); idx++)
                studentIndicate.add(false);

            gradesClassWiseList = GradesClassWiseDao.getGradeClassWise(classId, sqliteDatabase);
            Collections.sort(gradesClassWiseList, new GradeClassWiseSort());
            for (GradesClassWise gcw : gradesClassWiseList)
                gradeList.add(gcw.getGrade());

            return null;
        }

        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            clasSecSub.setText(PKGenerator.trim(0, 52, sf.toString()));
            populateListArray();
            gradeAdapter.notifyDataSetChanged();
            if (studentsArray.size() == 0) {
                Toast.makeText(context, "No students!", Toast.LENGTH_SHORT).show();
                ReplaceFragment.replace(new SubActivityExam(), getFragmentManager());
            }
        }
    }

}
