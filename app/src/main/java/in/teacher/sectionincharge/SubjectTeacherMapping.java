package in.teacher.sectionincharge;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import in.teacher.activity.R;
import in.teacher.dao.ClasDao;
import in.teacher.dao.SubjectGroupDao;
import in.teacher.dao.SubjectsDao;
import in.teacher.dao.TeacherDao;
import in.teacher.dao.TempDao;
import in.teacher.fragment.Dashbord;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.CommonDialogUtils;
import in.teacher.util.ReplaceFragment;

/**
 * Created by vinkrish on 15/10/15.
 * Don't expect comments explaining every piece of code, class and function names are self explanatory.
 */
public class SubjectTeacherMapping extends Fragment {
    private SQLiteDatabase sqliteDatabase;
    private int classId, sectionId, schoolId;
    private ListView listView;
    private List<Integer> subjectGroupIdList = new ArrayList<>();
    private List<Integer> subjectIdList = new ArrayList<>();
    private List<String> subjectNameList = new ArrayList<>();
    private List<Integer> teacherIdList = new ArrayList<>();
    private List<String> teacherNameList = new ArrayList<>();
    private List<SubjectTeacherItem> subjectTeacherList = new ArrayList<>();
    private SubjectTeacherAdapter stAdapter;
    private Button save;
    int pos;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.subject_teacher_mapping, container, false);
        listView = (ListView) view.findViewById(R.id.listView);
        save = (Button) view.findViewById(R.id.save);

        sqliteDatabase = AppGlobal.getSqliteDatabase();

        new CalledInit().execute();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CalledSubmit().execute();
            }
        });

        return view;
    }

    private void init() {

        Temp t = TempDao.selectTemp(sqliteDatabase);
        schoolId = t.getSchoolId();
        classId = t.getClassId();
        sectionId = t.getSectionId();
        CommonDialogUtils.hideKeyboard(getActivity());

        subjectGroupIdList = ClasDao.getSubjectGroupIds(sqliteDatabase, classId);

        if (subjectGroupIdList.size() != 0) {
            for (Integer groupId : subjectGroupIdList) {
                subjectIdList.addAll(SubjectGroupDao.getSubjectIdsInGroup(sqliteDatabase, groupId));
            }
            subjectNameList = SubjectsDao.getSubjectNameList(sqliteDatabase, subjectIdList);

            Cursor c = sqliteDatabase.rawQuery("select TeacherId, Name from teacher", null);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                teacherIdList.add((c.getInt(c.getColumnIndex("TeacherId"))));
                teacherNameList.add((c.getString(c.getColumnIndex("Name"))));
                c.moveToNext();
            }
            c.close();

            List<Integer> mappedSubjectId = new ArrayList<>();
            List<Integer> mappedTeacherId = new ArrayList<>();
            Cursor c2 = sqliteDatabase.rawQuery("select SubjectId,TeacherId from subjectteacher where SectionId=" + sectionId, null);
            c2.moveToNext();
            while (!c2.isAfterLast()) {
                mappedSubjectId.add(c2.getInt(c2.getColumnIndex("SubjectId")));
                mappedTeacherId.add(c2.getInt(c2.getColumnIndex("TeacherId")));
                c2.moveToNext();
            }
            c2.close();

            List<String> mappedSubjectName = new ArrayList<>();
            for (Integer id : mappedSubjectId) {
                mappedSubjectName.add(SubjectsDao.getSubjectName(id, sqliteDatabase));
            }

            List<String> mappedTeacherName = new ArrayList<>();
            for (Integer id : mappedTeacherId) {
                mappedTeacherName.add(TeacherDao.selectTeacherName(id, sqliteDatabase));
            }

            for (int i = 0; i < mappedSubjectId.size(); i++) {
                SubjectTeacherItem st = new SubjectTeacherItem();
                st.setSubjectId(mappedSubjectId.get(i));
                st.setSubjectName(mappedSubjectName.get(i));
                st.setTeacherId(mappedTeacherId.get(i));
                st.setTeacherName(mappedTeacherName.get(i));
                st.setInsert(false);
                subjectTeacherList.add(st);
            }

            for (int j = 0; j < subjectIdList.size(); j++) {
                if (!mappedSubjectId.contains(subjectIdList.get(j))) {
                    SubjectTeacherItem st = new SubjectTeacherItem();
                    st.setSubjectId(subjectIdList.get(j));
                    st.setSubjectName(subjectNameList.get(j));
                    st.setTeacherId(0);
                    st.setTeacherName("");
                    st.setInsert(true);
                    subjectTeacherList.add(st);
                }
            }
        }
    }

    class CalledInit extends AsyncTask<Void, Void, Void> {
        ProgressDialog pDialog = new ProgressDialog(getActivity());

        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Loading Data...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            init();
            return null;
        }

        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            pDialog.dismiss();
            if (subjectGroupIdList.size() == 0) {
                CommonDialogUtils.displayAlertWhiteDialog(getActivity(), "Class has no subjects assigned, please contact the admin");
                ReplaceFragment.replace(new Dashbord(), getFragmentManager());
            } else {
                stAdapter = new SubjectTeacherAdapter(getActivity(), subjectTeacherList);
                listView.setAdapter(stAdapter);
            }
        }
    }

    class CalledSubmit extends AsyncTask<Void, Void, Void> {
        ProgressDialog pDialog = new ProgressDialog(getActivity());

        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Saving Changes...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            saveChanges();
            return null;
        }

        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            pDialog.dismiss();
            Toast.makeText(getActivity(), "Teachers have been mapped to respective subjects.", Toast.LENGTH_LONG).show();
            ReplaceFragment.replace(new Dashbord(), getFragmentManager());
        }
    }

    private void saveChanges() {
        String sql = "";
        for (SubjectTeacherItem st : subjectTeacherList) {
            if (st.getTeacherId() != 0) {
                if (st.isInsert()) {
                    sql = "insert into subjectteacher (ClassId, SubjectId, SchoolId, TeacherId, SectionId) " +
                            "values(" + classId + ", " + st.getSubjectId() + ", " + schoolId + ", " + st.getTeacherId() + ", " + sectionId + ")";
                } else {
                    sql = "update subjectteacher set TeacherId = " + st.getTeacherId() +
                            " where SubjectId = " + st.getSubjectId() + " and SectionId = " + sectionId;
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
        }
    }

    public void showTeacherDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Teachers")
                .setCancelable(true)
                .setSingleChoiceItems(teacherNameList.toArray(new CharSequence[teacherNameList.size()]), -1, new TeacherSelectionClickHandler())
                .show();
    }

    private class TeacherSelectionClickHandler implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            SubjectTeacherItem sei = subjectTeacherList.get(pos);
            sei.setTeacherName(teacherNameList.get(which));
            sei.setTeacherId(teacherIdList.get(which));
            subjectTeacherList.set(pos, sei);
            stAdapter.notifyDataSetChanged();
            dialog.dismiss();
        }
    }

    class SubjectTeacherAdapter extends BaseAdapter {
        private List<SubjectTeacherItem> data = new ArrayList<>();
        private LayoutInflater inflater = null;

        public SubjectTeacherAdapter(Context context, List<SubjectTeacherItem> gridArray) {
            this.data = gridArray;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View row = convertView;
            RecordHolder holder;

            if (row == null) {
                row = inflater.inflate(R.layout.subject_teacher_list, parent, false);
                holder = new RecordHolder();
                holder.subjectName = (TextView) row.findViewById(R.id.subject_name);
                holder.teacherName = (TextView) row.findViewById(R.id.teacher_name);
                holder.selectTeacher = (LinearLayout) row.findViewById(R.id.select_teacher);
                row.setTag(holder);
            } else holder = (RecordHolder) row.getTag();

            SubjectTeacherItem item = data.get(position);
            holder.subjectName.setText(item.getSubjectName());
            holder.teacherName.setText(item.getTeacherName());

            holder.selectTeacher.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTeacherDialog();
                    pos = position;
                }
            });

            return row;
        }

        public class RecordHolder {
            public TextView subjectName;
            public TextView teacherName;
            public LinearLayout selectTeacher;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

    }

    class SubjectTeacherItem {
        private int subjectId;
        private int teacherId;
        private String subjectName;
        private String teacherName;
        private boolean insert;

        public int getSubjectId() {
            return subjectId;
        }

        public void setSubjectId(int subjectId) {
            this.subjectId = subjectId;
        }

        public int getTeacherId() {
            return teacherId;
        }

        public void setTeacherId(int teacherId) {
            this.teacherId = teacherId;
        }

        public boolean isInsert() {
            return insert;
        }

        public void setInsert(boolean insert) {
            this.insert = insert;
        }

        public String getSubjectName() {
            return subjectName;
        }

        public void setSubjectName(String subjectName) {
            this.subjectName = subjectName;
        }

        public String getTeacherName() {
            return teacherName;
        }

        public void setTeacherName(String teacherName) {
            this.teacherName = teacherName;
        }
    }
}
