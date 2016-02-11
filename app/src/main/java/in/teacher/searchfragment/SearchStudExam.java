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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.teacher.activity.R;
import in.teacher.adapter.StudExamAdapter;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.CommonObject;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.ReplaceFragment;

/**
 * Created by vinkrish.
 * My lawyer told me not to reveal.
 */
public class SearchStudExam extends Fragment {
    private Context context;
    private int studentId, sectionId, classId;
    private String studentName, className, secName;
    private SQLiteDatabase sqliteDatabase;
    private ListView lv;
    private ArrayList<CommonObject> commonObjectList = new ArrayList<>();
    private StudExamAdapter adapter;
    private List<Long> examIdList = new ArrayList<>();
    private List<String> examNameList = new ArrayList<>();
    private ProgressDialog pDialog;
    private TextView studTV, clasSecTV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_se_exam, container, false);

        context = AppGlobal.getContext();
        sqliteDatabase = AppGlobal.getSqliteDatabase();
        pDialog = new ProgressDialog(this.getActivity());

        clearList();

        studTV = (TextView) view.findViewById(R.id.studName);
        clasSecTV = (TextView) view.findViewById(R.id.studClasSec);
        lv = (ListView) view.findViewById(R.id.list);
        adapter = new StudExamAdapter(context, commonObjectList);
        lv.setAdapter(adapter);

        view.findViewById(R.id.slipSearch).setOnClickListener(searchSlipTest);
        view.findViewById(R.id.attSearch).setOnClickListener(searchAttendance);

        Temp t = TempDao.selectTemp(sqliteDatabase);
        studentId = t.getStudentId();

        new CalledBackLoad().execute();

        lv.setOnItemClickListener(clickListItem);

        return view;
    }

    private void clearList() {
        commonObjectList.clear();
        examIdList.clear();
        examNameList.clear();
    }

    private View.OnClickListener searchSlipTest = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ReplaceFragment.replace(new SearchStudST(), getFragmentManager());
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
            TempDao.updateExamId(examIdList.get(position), sqliteDatabase);
            ReplaceFragment.replace(new SearchStudExamSub(), getFragmentManager());
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
            Cursor c = sqliteDatabase.rawQuery("select A.Name, A.ClassId, A.SectionId, B.ClassName, C.SectionName from students A, class B, section C where" +
                    " A.StudentId=" + studentId + " and A.ClassId=B.ClassId and A.SectionId=C.SectionId group by A.StudentId", null);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                studentName = c.getString(c.getColumnIndex("Name"));
                classId = c.getInt(c.getColumnIndex("ClassId"));
                sectionId = c.getInt(c.getColumnIndex("SectionId"));
                className = c.getString(c.getColumnIndex("ClassName"));
                secName = c.getString(c.getColumnIndex("SectionName"));
                c.moveToNext();
            }
            c.close();

            TempDao.updateCurrentClass(classId, sqliteDatabase);

            Cursor c2 = sqliteDatabase.rawQuery("select ExamId,ExamName from exams where ClassId=" + classId, null);
            c2.moveToFirst();
            while (!c2.isAfterLast()) {
                examIdList.add(c2.getLong(c2.getColumnIndex("ExamId")));
                examNameList.add(c2.getString(c2.getColumnIndex("ExamName")));
                c2.moveToNext();
            }
            c2.close();

            List<Integer> subIdList = new ArrayList<>();
            Cursor cc = sqliteDatabase.rawQuery("select A.SubjectId from subjectteacher A, subjects B, teacher C where A.SectionId=" + sectionId + " and" +
                    " A.SubjectId=B.SubjectId and A.TeacherId=C.TeacherId", null);
            cc.moveToFirst();
            while (!cc.isAfterLast()) {
                subIdList.add(cc.getInt(cc.getColumnIndex("SubjectId")));
                cc.moveToNext();
            }
            cc.close();


            for (int i = 0; i < examIdList.size(); i++) {
                try {
                    commonObjectList.add(new CommonObject(examNameList.get(i), ""));
                } catch (IndexOutOfBoundsException e) {
                    commonObjectList.add(new CommonObject(examNameList.get(i), ""));
                }

            }
            return null;
        }

        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            studTV.setText(studentName);
            clasSecTV.setText(className + " - " + secName);
            adapter.notifyDataSetChanged();
            pDialog.dismiss();
        }
    }

}
