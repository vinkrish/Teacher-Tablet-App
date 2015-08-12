package in.teacher.fragment;

import in.teacher.activity.R;
import in.teacher.adapter.StudExamSubAdapter;
import in.teacher.dao.ActivitiDao;
import in.teacher.dao.ActivityMarkDao;
import in.teacher.dao.ExamsDao;
import in.teacher.dao.ExmAvgDao;
import in.teacher.dao.MarksDao;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.Activiti;
import in.teacher.sqlite.Amr;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.ReplaceFragment;

import java.util.ArrayList;
import java.util.List;

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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class SearchStudExamSub extends Fragment {
    private Context context;
    private int studentId, sectionId, examId;
    private String studentName, className, secName, examName;
    private SQLiteDatabase sqliteDatabase;
    private ArrayList<Amr> amrList = new ArrayList<>();
    private StudExamSubAdapter adapter;
    private ListView lv;
    private List<Activiti> activitiList = new ArrayList<>();
    private List<Integer> isSubGotActList = new ArrayList<>();
    private ProgressDialog pDialog;
    private TextView studTV, clasSecTV;
    private List<Integer> subIdList = new ArrayList<>();
    private List<String> scoreList = new ArrayList<>();
    private Button examBut;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_se_exam_sub, container, false);
        context = AppGlobal.getContext();
        sqliteDatabase = AppGlobal.getSqliteDatabase();
        pDialog = new ProgressDialog(this.getActivity());

        clearList();

        examBut = (Button) view.findViewById(R.id.examSubButton);
        lv = (ListView) view.findViewById(R.id.list);
        studTV = (TextView) view.findViewById(R.id.studName);
        clasSecTV = (TextView) view.findViewById(R.id.studClasSec);
        adapter = new StudExamSubAdapter(context, amrList);
        lv.setAdapter(adapter);

        view.findViewById(R.id.slipSearch).setOnClickListener(searchSlipTest);
        view.findViewById(R.id.seSearch).setOnClickListener(searchExam);
        view.findViewById(R.id.examButton).setOnClickListener(searchExam);
        view.findViewById(R.id.attSearch).setOnClickListener(searchAttendance);

        Temp t = TempDao.selectTemp(sqliteDatabase);
        studentId = t.getStudentId();
        examId = t.getExamId();
        sectionId = t.getSectionId();

        new CalledBackLoad().execute();

        lv.setOnItemClickListener(clickListItem);

        return view;
    }

    private void clearList() {
        amrList.clear();
        activitiList.clear();
        subIdList.clear();
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

    private View.OnClickListener searchAttendance = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ReplaceFragment.replace(new SearchStudAtt(), getFragmentManager());
        }
    };

    private OnItemClickListener clickListItem = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            TempDao.updateSubjectId(subIdList.get(position), sqliteDatabase);
            int cache = ActivitiDao.isThereActivity(sectionId, subIdList.get(position), examId, sqliteDatabase);
            if (cache == 1) {
                ReplaceFragment.replace(new SearchStudAct(), getFragmentManager());
            }
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

            Cursor c = sqliteDatabase.rawQuery("select A.Name, A.ClassId, A.SectionId, B.ClassName, C.SectionName from students A, class B, section C where" +
                    " A.StudentId=" + studentId + " and A.ClassId=B.ClassId and A.SectionId=C.SectionId group by A.StudentId", null);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                studentName = c.getString(c.getColumnIndex("Name"));
                //	classId = c.getInt(c.getColumnIndex("ClassId"));
                sectionId = c.getInt(c.getColumnIndex("SectionId"));
                className = c.getString(c.getColumnIndex("ClassName"));
                secName = c.getString(c.getColumnIndex("SectionName"));
                c.moveToNext();
            }
            c.close();

            final List<Integer> teacherIdList = new ArrayList<>();
            List<String> subNameList = new ArrayList<>();
            List<String> teacherNameList = new ArrayList<>();
            List<Integer> progressList1 = new ArrayList<>();
            List<Integer> progressList2 = new ArrayList<>();

            Cursor c2 = sqliteDatabase.rawQuery("select A.SubjectId, A.TeacherId, B.SubjectName,C.Name from subjectteacher A, subjects B, teacher C where A.SectionId=" + sectionId + " and" +
                    " A.SubjectId=B.SubjectId and A.TeacherId=C.TeacherId", null);
            c2.moveToFirst();
            while (!c2.isAfterLast()) {
                subIdList.add(c2.getInt(c2.getColumnIndex("SubjectId")));
                teacherIdList.add(c2.getInt(c2.getColumnIndex("TeacherId")));
                subNameList.add(c2.getString(c2.getColumnIndex("SubjectName")));
                teacherNameList.add(c2.getString(c2.getColumnIndex("Name")));
                c2.moveToNext();
            }
            c2.close();

            for (Integer subId : subIdList) {
                int cache = ActivitiDao.isThereActivity(sectionId, subId, examId, sqliteDatabase);
                if (cache == 1) {
                    isSubGotActList.add(subId);
                }
            }

            List<Integer> actList = new ArrayList<>();
            int actAvg = 0;
            int overallActAvg = 0;
            for (Integer sub : subIdList) {
                int avg = 0;
                if (isSubGotActList.contains(sub)) {
                    actList.clear();
                    actAvg = 0;
                    Cursor c3 = sqliteDatabase.rawQuery("select ActivityId from activity where ExamId=" + examId + " and SubjectId=" + sub + " and SectionId=" + sectionId, null);
                    c3.moveToFirst();
                    while (!c3.isAfterLast()) {
                        actList.add(c3.getInt(c3.getColumnIndex("ActivityId")));
                        c3.moveToNext();
                    }
                    c3.close();

                    for (Integer actId : actList) {
                        actAvg += ActivityMarkDao.getStudActAvg(studentId, actId, sqliteDatabase);
                    }
                    overallActAvg = actAvg / actList.size();
                    progressList1.add(overallActAvg);
                    scoreList.add(" ");
                } else {
                    avg = MarksDao.getStudExamAvg(studentId, sub, examId, sqliteDatabase);
                    if (avg != 0) {
                        int score = MarksDao.getStudExamMark(studentId, sub, examId, sqliteDatabase);
                        int maxScore = MarksDao.getExamMaxMark(sub, examId, sqliteDatabase);
                        scoreList.add(score+"/"+maxScore);
                    }else{
                        scoreList.add("-");
                    }
                    progressList1.add(avg);
                }
            }

            for (Integer subId : subIdList) {
                progressList2.add(ExmAvgDao.selectSeAvg2(sectionId, subId, examId, sqliteDatabase));
            }

            for (int i = 0; i < subIdList.size(); i++) {
                amrList.add(new Amr(subNameList.get(i), teacherNameList.get(i), scoreList.get(i), progressList1.get(i), progressList2.get(i)));
            }
            return null;
        }

        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            examBut.setText(examName);
            studTV.setText(studentName);
            clasSecTV.setText(className + " - " + secName);
            adapter.notifyDataSetChanged();
            pDialog.dismiss();
        }
    }

}
