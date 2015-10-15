package in.teacher.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.teacher.activity.R;
import in.teacher.dao.ClasDao;
import in.teacher.dao.SubjectGroupDao;
import in.teacher.dao.SubjectsDao;
import in.teacher.dao.TeacherDao;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;

/**
 * Created by vinkrish on 15/10/15.
 */
public class SubjectTeacherMapping extends Fragment {
    private SQLiteDatabase sqliteDatabase;
    private Activity activity;
    private Context context;
    private int classId, sectionId;
    private ListView listView;
    private List<Integer> subjectGroupIdList = new ArrayList<>();
    private List<Integer> subjectIdList = new ArrayList<>();
    private List<String> subjectNameList = new ArrayList<>();
    private List<Integer> teacherIdList = new ArrayList<>();
    private List<String> teacherNameList = new ArrayList<>();
    private List<SubjectTeacherItem> subjectTeacherList = new ArrayList<>();
    private SubjectTeacherAdapter stAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.subject_teacher_mapping, container, false);
        listView = (ListView) view.findViewById(R.id.listView);

        init();

        return view;
    }

    private void init(){
        sqliteDatabase = AppGlobal.getSqliteDatabase();
        activity = AppGlobal.getActivity();
        context = AppGlobal.getContext();

        Temp t = TempDao.selectTemp(sqliteDatabase);
        classId = t.getClassId();
        sectionId = t.getSectionId();

        subjectGroupIdList = ClasDao.getSubjectGroupIds(sqliteDatabase, classId);
        for (Integer groupId: subjectGroupIdList) {
            subjectIdList.addAll(SubjectGroupDao.getSubjectIdsInGroup(sqliteDatabase, groupId));
        }
        StringBuilder sb = new StringBuilder();
        for (Integer ids : subjectIdList) {
            sb.append(ids + ",");
        }
        subjectNameList = SubjectsDao.getSubjectNameList(sqliteDatabase, sb.substring(0, sb.length() - 1));

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
        Cursor c2 = sqliteDatabase.rawQuery("select SubjectId,TeacherId from subjectteacher where SectionId="+sectionId, null);
        c2.moveToNext();
        while (!c2.isAfterLast()) {
            mappedSubjectId.add(c2.getInt(c2.getColumnIndex("SubjectId")));
            mappedTeacherId.add(c2.getInt(c2.getColumnIndex("TeacherId")));
            c2.moveToNext();
        }
        c2.close();

        List<String> mappedSubjectName = new ArrayList<>();
        for (Integer id: mappedSubjectId){
            mappedSubjectName.add(SubjectsDao.getSubjectName(id, sqliteDatabase));
        }

        List<String> mappedTeacherName = new ArrayList<>();
        for (Integer id: mappedTeacherId){
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

        for (int j = 0; j<subjectIdList.size(); j++) {
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

        stAdapter = new SubjectTeacherAdapter(context, subjectTeacherList);
        listView.setAdapter(stAdapter);

    }

    class SubjectTeacherAdapter extends BaseAdapter {
        private List<SubjectTeacherItem> data = new ArrayList<>();
        private LayoutInflater inflater = null;

        public SubjectTeacherAdapter(Context context, List<SubjectTeacherItem> gridArray) {
            this.data = gridArray;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            RecordHolder holder;

            if (row == null) {
                row = inflater.inflate(R.layout.subject_teacher_list, parent, false);
                holder = new RecordHolder();
                holder.subjectName = (TextView) row.findViewById(R.id.subject_name);
                holder.teacherName = (EditText) row.findViewById(R.id.teacher_name);
                row.setTag(holder);
            } else holder = (RecordHolder) row.getTag();

            SubjectTeacherItem item = data.get(position);
            holder.subjectName.setText(item.getSubjectName());
            holder.teacherName.setText(item.getTeacherName());
            holder.teacherName.getBackground().setColorFilter(getResources().getColor(R.color.light_black), PorterDuff.Mode.SRC_ATOP);
            return row;
        }

        public class RecordHolder {
            public TextView subjectName;
            public EditText teacherName;
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

        public SubjectTeacherItem() {

        }

        public SubjectTeacherItem(int subjectId, int teacherId, String subjectName, String teacherName, boolean insert){
            this.subjectId= subjectId;
            this.teacherId = teacherId;
            this.subjectName = subjectName;
            this.teacherName = teacherName;
            this.insert = insert;
        }

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
