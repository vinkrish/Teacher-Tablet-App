package in.teacher.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.teacher.activity.R;
import in.teacher.dao.ClasDao;
import in.teacher.dao.StudentsDao;
import in.teacher.dao.SubjectGroupDao;
import in.teacher.dao.SubjectsDao;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.Students;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.CommonDialogUtils;
import in.teacher.util.ReplaceFragment;

/**
 * Created by vinkrish on 30/09/15.
 */
public class SubjectMapStudentEdit extends Fragment {
    private SQLiteDatabase sqliteDatabase;
    private Activity activity;
    private Context context;
    private int classId, sectionId, studentId;
    private ListView listView;
    private ScrollView scrollView;
    private Button save;
    private ArrayList<Integer> studIdList = new ArrayList<>();
    private StudentAdapter studentAdapter;
    private List<Integer> subjectGroupIdList = new ArrayList<>();
    private List<String> subjectGroupNameList = new ArrayList<>();
    private List<Integer> subjectIdList = new ArrayList<>();
    private List<String> subjectNameList = new ArrayList<>();
    private List<Integer> selectedSubjectId = new ArrayList<>();
    private TableLayout table;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.subject_map_student_edit, container, false);
        listView = (ListView) view.findViewById(R.id.listView);
        scrollView = (ScrollView) view.findViewById(R.id.scrollView);
        save = (Button) view.findViewById(R.id.save);

        init();

        return view;
    }

    public void init() {
        sqliteDatabase = AppGlobal.getSqliteDatabase();
        activity = AppGlobal.getActivity();
        context = AppGlobal.getContext();

        Temp t = TempDao.selectTemp(sqliteDatabase);
        classId = t.getClassId();
        sectionId = t.getSectionId();
        studentId = 0;

        List<Students> studentList = StudentsDao.selectStudents(sectionId, sqliteDatabase);
        for (Students s : studentList) {
            studIdList.add(s.getStudentId());
        }

        studentAdapter = new StudentAdapter(context, studentList);
        listView.setAdapter(studentAdapter);
        //listView.setSelector(android.R.color.holo_blue_light);
        listView.setOnItemClickListener(listViewItemClicked);
        // listView.setAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, android.R.id.text1, listArray));

        subjectGroupIdList = ClasDao.getSubjectGroupIds(sqliteDatabase, classId);
        StringBuilder sb = new StringBuilder();
        for (Integer ids : subjectGroupIdList) {
            sb.append(ids + ",");
        }
        subjectGroupNameList = SubjectGroupDao.getSubjectGroupNameList(sqliteDatabase, sb.substring(0, sb.length() - 1));

        table = new TableLayout(activity);
        //generateTable();
        scrollView.addView(table);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedSubjectId.size() == subjectGroupIdList.size()) {
                    if (studentId != 0) new CalledSubmit().execute();
                } else
                    CommonDialogUtils.displayAlertWhiteDialog(activity, "Please select at least one subject in each subject group");
            }
        });
    }

    AdapterView.OnItemClickListener listViewItemClicked = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            table.removeAllViews();
            studentAdapter.setSelectedIndex(position);

            studentId = studIdList.get(position);
            getSelectedSubjects(studentId);
            generateTable();
        }
    };

    private void getSelectedSubjects(int studentId) {
        selectedSubjectId.clear();
        String ids = null;
        Cursor c = sqliteDatabase.rawQuery("select SubjectIds from students where StudentId = " + studentId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            ids = c.getString(c.getColumnIndex("SubjectIds"));
            c.moveToNext();
        }
        c.close();
        String[] idArray = ids.split("#");
        for (String id : idArray) {
            if (id != "") selectedSubjectId.add(Integer.parseInt(id));
        }
    }

    private void generateTable() {
        for (int i = 0; i < subjectGroupNameList.size(); i++) {
            TableRow tableRow = tableRow(subjectGroupIdList.get(i), subjectGroupNameList.get(i));
            //tableRow.setBackgroundColor(Color.LTGRAY);
            table.addView(tableRow);
        }
    }

    TableRow tableRow(int groupId, String groupName) {

        TableRow taleRowForTableD = new TableRow(this.context);
        TableRow.LayoutParams params = new TableRow.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        //params.setMargins(2, 2, 0, 0);

        LinearLayout ll = new LinearLayout(activity);
        ll.setOrientation(LinearLayout.VERTICAL);

        TextView tv = new TextView(activity);
        tv.setText(groupName);
        tv.setPadding(20, 5, 0, 5);
        tv.setTextSize(18);
        //tv.setBackgroundColor(Color.WHITE);

        ll.addView(tv);

        subjectIdList.clear();
        subjectIdList = SubjectGroupDao.getSubjectIdsInGroup(sqliteDatabase, groupId);
        StringBuilder sb = new StringBuilder();
        for (Integer ids : subjectIdList) {
            sb.append(ids + ",");
        }
        subjectNameList.clear();
        subjectNameList = SubjectsDao.getSubjectNameList(sqliteDatabase, sb.substring(0, sb.length() - 1));

        final RadioButton[] rb = new RadioButton[subjectIdList.size()];
        RadioGroup rg = new RadioGroup(activity);
        rg.setGravity(Gravity.CENTER_VERTICAL);
        //rg.setBackgroundColor(Color.WHITE);
        rg.setPadding(20, 5, 0, 5);
        rg.setTag(groupId);
        // rg.setBackgroundResource(R.drawable.radio_border);
        rg.setOrientation(RadioGroup.VERTICAL);
        for (int j = 0; j < subjectNameList.size(); j++) {
            rb[j] = new RadioButton(activity);
            rb[j].setId(View.generateViewId());
            rb[j].setGravity(Gravity.CENTER_VERTICAL);
            rb[j].setPadding(5, 10, 0, 10);
            rb[j].setTag(subjectIdList.get(j));
            rb[j].setText(subjectNameList.get(j));
            if (selectedSubjectId.contains(subjectIdList.get(j))) {
                //rb[j].setChecked(true);
                rb[j].toggle();
                rg.check(rb[j].getId());
            }
            rb[j].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int id = (Integer) v.getTag();
                    if (!selectedSubjectId.contains(id)) {
                        if (((RadioGroup) v.getParent()).getChildCount() == 1) {
                            selectedSubjectId.add(id);
                        } else {
                            List<Integer> idList = SubjectGroupDao.getSubjectIdsInGroup(sqliteDatabase, (Integer) ((RadioGroup) v.getParent()).getTag());
                            for (Integer ids : idList) {
                                selectedSubjectId.remove(ids);
                            }
                            selectedSubjectId.add(id);
                        }
                    }
                }
            });
            rg.addView(rb[j]);
        }
        ll.addView(rg);

        View border = new View(activity);
        ScrollView.LayoutParams param = new ScrollView.LayoutParams(1000, 1);
        //ScrollView.LayoutParams param = new ScrollView.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
        border.setBackgroundColor(Color.LTGRAY);
        ll.addView(border, param);

        taleRowForTableD.addView(ll, params);

        return taleRowForTableD;

    }

    class StudentAdapter extends BaseAdapter {
        private List<Students> data = new ArrayList<>();
        private LayoutInflater inflater = null;
        private int selectedIndex;

        public StudentAdapter(Context context, List<Students> gridArray) {
            this.data = gridArray;
            selectedIndex = -1;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setSelectedIndex(int index) {
            selectedIndex = index;
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            RecordHolder holder;

            if (row == null) {
                row = inflater.inflate(R.layout.student_list, parent, false);
                holder = new RecordHolder();
                holder.ll = (LinearLayout) row.findViewById(R.id.ll);
                holder.txtRollNo = (TextView) row.findViewById(R.id.rollNo);
                holder.txtName = (TextView) row.findViewById(R.id.name);
                row.setTag(holder);
            } else holder = (RecordHolder) row.getTag();

            if (selectedIndex != -1 && position == selectedIndex) {
                holder.ll.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
            } else {
                holder.ll.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            }

            Students gridItem = data.get(position);
            holder.txtRollNo.setText(String.valueOf(gridItem.getRollNoInClass()));
            holder.txtName.setText(gridItem.getName());
            return row;
        }

        public class RecordHolder {
            public LinearLayout ll;
            public TextView txtRollNo;
            public TextView txtName;
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
            StringBuilder sb = new StringBuilder();
            for (Integer sbi : selectedSubjectId) {
                sb.append(sbi).append("#");
            }
            String sql = "update students set SubjectIds = '" + sb.substring(0, sb.length() - 1) + "' where StudentId = " + studentId;
            try {
                sqliteDatabase.execSQL(sql);
                ContentValues cv = new ContentValues();
                cv.put("Query", sql);
                sqliteDatabase.insert("uploadsql", null, cv);
            } catch (SQLException e) {
            }
            return null;
        }

        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            pDialog.dismiss();
        }
    }

}
