package in.teacher.searchfragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import in.teacher.activity.R;
import in.teacher.adapter.StudActAdapter;
import in.teacher.dao.ActivitiDao;
import in.teacher.dao.ExamsDao;
import in.teacher.dao.GradesClassWiseDao;
import in.teacher.dao.SubActivityDao;
import in.teacher.dao.SubActivityGradeDao;
import in.teacher.dao.SubActivityMarkDao;
import in.teacher.dao.SubjectsDao;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.GradesClassWise;
import in.teacher.sqlite.SubActivity;
import in.teacher.sqlite.CommonObject;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.GradeClassWiseSort;
import in.teacher.util.ReplaceFragment;

/**
 * Created by vinkrish.
 * My lawyer told me not to reveal.
 */
public class SearchStudSubAct extends Fragment {
    private Context context;
    private int studentId, subjectId, classId;
    private long examId, activityId;
    private String studentName, className, secName, examName, subjectName, activityName;
    private SQLiteDatabase sqliteDatabase;
    private List<Long> subActIdList = new ArrayList<>();
    private List<String> subActNameList = new ArrayList<>();
    private List<Integer> avgList1 = new ArrayList<>();
    private List<Integer> avgList2 = new ArrayList<>();
    private List<SubActivity> subActList = new ArrayList<>();
    private ArrayList<CommonObject> commonObjectList = new ArrayList<>();
    private List<String> scoreList = new ArrayList<>();
    private StudActAdapter adapter;
    private ListView lv;
    private ProgressDialog pDialog;
    private TextView studTV, clasSecTV;
    private Button examBut, subBut, actBut;
    private List<GradesClassWise> gradesClassWiseList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_se_subact, container, false);
        context = AppGlobal.getContext();
        sqliteDatabase = AppGlobal.getSqliteDatabase();

        pDialog = new ProgressDialog(this.getActivity());

        clearList();

        examBut = (Button) view.findViewById(R.id.examSubButton);
        subBut = (Button) view.findViewById(R.id.examSubActButton);
        actBut = (Button) view.findViewById(R.id.examSubActiButton);
        studTV = (TextView) view.findViewById(R.id.studName);
        clasSecTV = (TextView) view.findViewById(R.id.studClasSec);

        lv = (ListView) view.findViewById(R.id.list);
        adapter = new StudActAdapter(context, commonObjectList);
        lv.setAdapter(adapter);

        view.findViewById(R.id.slipSearch).setOnClickListener(searchSlipTest);
        view.findViewById(R.id.seSearch).setOnClickListener(searchExam);
        view.findViewById(R.id.examButton).setOnClickListener(searchExam);
        view.findViewById(R.id.examSubButton).setOnClickListener(searchExamSub);
        view.findViewById(R.id.attSearch).setOnClickListener(searchAttendance);
        view.findViewById(R.id.examSubActButton).setOnClickListener(searchStudAct);

        Temp t = TempDao.selectTemp(sqliteDatabase);
        studentId = t.getStudentId();
        classId = t.getCurrentClass();
        examId = t.getExamId();
        subjectId = t.getSubjectId();
        activityId = t.getActivityId();

        new CalledBackLoad().execute();

        return view;
    }

    private void clearList() {
        subActIdList.clear();
        subActList.clear();
        avgList1.clear();
        avgList2.clear();
        subActNameList.clear();
        commonObjectList.clear();
        scoreList.clear();
    }

    private View.OnClickListener searchSlipTest = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ReplaceFragment.replace(new SearchStudST(), getFragmentManager());
        }
    };

    private View.OnClickListener searchExam = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ReplaceFragment.replace(new SearchStudExam(), getFragmentManager());
        }
    };

    private View.OnClickListener searchExamSub = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ReplaceFragment.replace(new SearchStudExamSub(), getFragmentManager());
        }
    };

    private View.OnClickListener searchStudAct = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ReplaceFragment.replace(new SearchStudAct(), getFragmentManager());
        }
    };

    private View.OnClickListener searchAttendance = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ReplaceFragment.replace(new SearchStudAtt(), getFragmentManager());
        }
    };

    class CalledBackLoad extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Preparing data ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            examName = ExamsDao.selectExamName(examId, sqliteDatabase);
            subjectName = SubjectsDao.getSubjectName(subjectId, sqliteDatabase);
            activityName = ActivitiDao.selectActivityName(activityId, sqliteDatabase);

            Cursor c = sqliteDatabase.rawQuery("select A.Name, A.ClassId, A.SectionId, B.ClassName, C.SectionName from students A, class B, section C where" +
                    " A.StudentId=" + studentId + " and A.ClassId=B.ClassId and A.SectionId=C.SectionId group by A.StudentId", null);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                studentName = c.getString(c.getColumnIndex("Name"));
                className = c.getString(c.getColumnIndex("ClassName"));
                secName = c.getString(c.getColumnIndex("SectionName"));
                c.moveToNext();
            }
            c.close();

            gradesClassWiseList = GradesClassWiseDao.getGradeClassWise(classId, sqliteDatabase);
            Collections.sort(gradesClassWiseList, new GradeClassWiseSort());

            subActList = SubActivityDao.selectSubActivity(activityId, sqliteDatabase);
            for (SubActivity subact : subActList) {
                subActNameList.add(subact.getSubActivityName());
                subActIdList.add(subact.getSubActivityId());

                Cursor cursor = sqliteDatabase.rawQuery("select StudentId from subactivitymark where StudentId = " + studentId + " and SubActivityId = " + subact.getSubActivityId(), null);
                if (cursor.getCount() > 0) {
                    int avg = SubActivityMarkDao.getStudSubActAvg(studentId, subact.getSubActivityId(), sqliteDatabase);
                    avgList1.add(avg);
                    if (avg == 0) {
                        scoreList.add("-");
                    } else {
                        int score = SubActivityMarkDao.getStudSubActMark(studentId, subact.getSubActivityId(), sqliteDatabase);
                        float maxScore = SubActivityDao.getSubActMaxMark(subact.getSubActivityId(), sqliteDatabase);
                        scoreList.add(score + "/" + maxScore);
                    }
                    avgList2.add(SubActivityMarkDao.getSectionAvg(subact.getSubActivityId(), sqliteDatabase));
                } else {
                    Cursor cursor1 = sqliteDatabase.rawQuery("select Grade from subactivitygrade where StudentId = " + studentId + " and SubActivityId = " + subact.getSubActivityId(), null);
                    if (cursor1.getCount() > 0) {
                        cursor1.moveToFirst();
                        while (!cursor1.isAfterLast()) {
                            scoreList.add(cursor1.getString(cursor1.getColumnIndex("Grade")));
                            avgList1.add(getMarkTo(cursor1.getString(cursor1.getColumnIndex("Grade"))));
                            avgList2.add(SubActivityGradeDao.getSectionAvg(classId, subact.getSubActivityId(), sqliteDatabase));
                            cursor1.moveToNext();
                        }
                    } else {
                        avgList1.add(0);
                        scoreList.add("-");
                        avgList2.add(0);
                    }
                    cursor1.close();
                }
                cursor.close();
            }

            for (int i = 0; i < subActIdList.size(); i++) {
                try {
                    commonObjectList.add(new CommonObject(subActNameList.get(i), scoreList.get(i), avgList1.get(i), avgList2.get(i)));
                } catch (IndexOutOfBoundsException e) {
                    commonObjectList.add(new CommonObject(subActNameList.get(i), "", 0, 0));
                }
            }
            return null;
        }

        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            examBut.setText(examName);
            subBut.setText(subjectName);
            actBut.setText(activityName);
            studTV.setText(studentName);
            clasSecTV.setText(className + " - " + secName);
            adapter.notifyDataSetChanged();
            pDialog.dismiss();
        }
    }

    private int getMarkTo(String grade) {
        int markTo = 0;
        for (GradesClassWise gcw : gradesClassWiseList) {
            if (grade.equals(gcw.getGrade())) {
                markTo = gcw.getMarkTo();
                break;
            }
        }
        return markTo;
    }

}
