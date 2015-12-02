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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import in.teacher.activity.R;
import in.teacher.dao.ClasDao;
import in.teacher.dao.SectionDao;
import in.teacher.dao.TempDao;
import in.teacher.examfragment.ActivityExam;
import in.teacher.sqlite.CommonObject;
import in.teacher.sqlite.MovStudent;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.CommonDialogUtils;
import in.teacher.util.ReplaceFragment;

/**
 * Created by vinkrish on 30/11/15.
 */
public class MoveStudent extends Fragment {
    private SQLiteDatabase sqliteDatabase;
    private int classId, sectionId, schoolId, selSectionId = 0;
    private String secName, selSecName, className;
    private TextView movingClassTv, selStudents;
    private Button studentsSelBtn, confirmMoveBtn;
    private Spinner sectionSpinner;
    private ListView listView;
    private List<Integer> idList = new ArrayList<>();
    private List<String> nameList = new ArrayList<>();
    private List<Integer> selIdList = new ArrayList<>();
    private List<String> selNameList = new ArrayList<>();
    private List<Integer> sectionIdList = new ArrayList<>();
    private List<String> sectionNameList = new ArrayList<>();
    private boolean[] studentsSelections;
    private MoveStudAdapter moveStudAdapter;
    private List<MovStudent> movStudentList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.move_student, container, false);

        initView(view);
        init();

        return view;
    }

    private void initView(View view) {
        movingClassTv = (TextView) view.findViewById(R.id.moving_class);
        selStudents = (TextView) view.findViewById(R.id.sel_students);

        studentsSelBtn = (Button) view.findViewById(R.id.student_sel);
        confirmMoveBtn = (Button) view.findViewById(R.id.confirm_move);

        sectionSpinner = (Spinner) view.findViewById(R.id.secSpinner);

        listView = (ListView) view.findViewById(R.id.listView);
        view.findViewById(R.id.switchClass).setOnClickListener(switchClass);
    }

    private void init() {
        sqliteDatabase = AppGlobal.getSqliteDatabase();

        Temp t = TempDao.selectTemp(sqliteDatabase);
        schoolId = t.getSchoolId();
        classId = t.getClassId();
        sectionId = t.getSectionId();

        className = ClasDao.getClassName(classId, sqliteDatabase);
        secName = SectionDao.getSectionName(sectionId, sqliteDatabase);
        movingClassTv.setText("Move students from " + className + " - " + secName);

        CommonDialogUtils.hideKeyboard(getActivity());

        Cursor c = sqliteDatabase.rawQuery("select StudentId, Name from students where SectionId = " +
                sectionId + " order by RollNoInClass", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            idList.add(c.getInt(c.getColumnIndex("StudentId")));
            nameList.add(c.getString(c.getColumnIndex("Name")));
            c.moveToNext();
        }
        c.close();

        studentsSelections = new boolean[idList.size()];

        studentsSelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selIdList.clear();
                selNameList.clear();
                showStudentsDialog();
            }
        });

        initSectionSpinner();

        final ArrayAdapter<String> sectionAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_header, sectionNameList);
        sectionAdapter.setDropDownViewResource(R.layout.spinner_droppeddown);
        sectionSpinner.setAdapter(sectionAdapter);

        sectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    selSectionId = sectionIdList.get(position);
                    selSecName = SectionDao.getSectionName(selSectionId, sqliteDatabase);
                } else selSectionId = 0;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        initPendingStudent();
        moveStudAdapter = new MoveStudAdapter(getActivity(), movStudentList);
        listView.setAdapter(moveStudAdapter);

        confirmMoveBtn.setOnClickListener(confirmClick);
    }

    private View.OnClickListener switchClass = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CommonDialogUtils.displaySwitchClass(getActivity(), sqliteDatabase, new MoveStudent());
        }
    };

    private void initSectionSpinner() {
        Cursor c = sqliteDatabase.rawQuery("select SectionId, SectionName from section where ClassId = " + classId + " and SectionId!=" + sectionId, null);
        sectionIdList.add(0);
        if (c.getCount() > 0) {
            sectionNameList.add("Select Section");
        } else {
            sectionNameList.add("No section to move!");
        }
        c.moveToFirst();
        while (!c.isAfterLast()) {
            sectionIdList.add(c.getInt(c.getColumnIndex("SectionId")));
            sectionNameList.add(c.getString(c.getColumnIndex("SectionName")));
            c.moveToNext();
        }
        c.close();
    }

    private void showStudentsDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Select Students")
                .setCancelable(false)
                .setMultiChoiceItems(nameList.toArray(new CharSequence[idList.size()]),
                        studentsSelections, new StudentSelectionClickHandler())
                .setPositiveButton("OK", new StudentButtonClickHandler())
                .show();
    }

    private class StudentSelectionClickHandler implements DialogInterface.OnMultiChoiceClickListener {
        public void onClick(DialogInterface dialog, int clicked, boolean selected) {
            if (selected) studentsSelections[clicked] = true;
            else studentsSelections[clicked] = false;
        }
    }

    private class StudentButtonClickHandler implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int clicked) {
            switch (clicked) {
                case DialogInterface.BUTTON_POSITIVE:
                    selectedStudents();
                    break;
            }
        }
    }

    private void selectedStudents() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < idList.size(); i++) {
            if (studentsSelections[i]) {
                sb.append(nameList.get(i) + ",");
                selIdList.add(idList.get(i));
                selNameList.add(nameList.get(i));
            }
        }
        if (sb.length() > 0) {
            selStudents.setText("students to move : " + sb.substring(0, sb.length() - 1));
        }
    }

    View.OnClickListener confirmClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (selIdList.size() == 0) {
                CommonDialogUtils.displayAlertWhiteDialog(getActivity(), "Please select student to move !");
            } else if (selSectionId == 0) {
                CommonDialogUtils.displayAlertWhiteDialog(getActivity(), "Please select section to move !");
            } else {
                new CalledConfirm().execute();
            }
        }
    };

    class CalledConfirm extends AsyncTask<Void, Void, Void> {
        ProgressDialog pDialog = new ProgressDialog(getActivity());

        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Requesting move student...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            for (int i = 0; i < selIdList.size(); i++) {
                String query = "update students set SectionId = " + selSectionId + " where StudentId = " + selIdList.get(i);
                String sql = "insert into movestudent (SchoolId, Query, StudentId, StudentName, ClassName, " +
                        "SecIdFrom, SecIdTo, SectionFrom, SectionTo, Status) " +
                        "values (" + schoolId + ", '" + query + "', " + selIdList.get(i) + ", '" + selNameList.get(i) + "', '" +
                        className + "'," + sectionId + "," + selSectionId + ",'" + secName + "','" + selSecName + "', 0)";
                try {
                    sqliteDatabase.execSQL(sql);
                    ContentValues cv = new ContentValues();
                    cv.put("Query", sql);
                    sqliteDatabase.insert("uploadsql", null, cv);
                } catch (SQLException e) {
                }
            }
            return null;
        }

        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            pDialog.dismiss();
            ReplaceFragment.replace(new MoveStudent(), getFragmentManager());
        }

    }

    private void initPendingStudent() {
        Cursor c = sqliteDatabase.rawQuery("select * from movestudent where SecIdFrom = " + sectionId + " and Status = 0", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            MovStudent ms = new MovStudent();
            ms.setSchooId(schoolId);
            ms.setStudentName(c.getString(c.getColumnIndex("StudentName")));
            ms.setQuery(c.getString(c.getColumnIndex("Query")));
            ms.setClassName(c.getString(c.getColumnIndex("ClassName")));
            ms.setSectionFrom(c.getString(c.getColumnIndex("SectionFrom")));
            ms.setSectionTo(c.getString(c.getColumnIndex("SectionTo")));
            movStudentList.add(ms);
            c.moveToNext();
        }
        c.close();
    }

    private class MoveStudAdapter extends ArrayAdapter<MovStudent> {
        private List<MovStudent> data = new ArrayList<>();
        private LayoutInflater inflater;

        public MoveStudAdapter(Context context, List<MovStudent> objects) {
            super(context, R.layout.mov_stud_pending_list, objects);
            this.data = objects;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            RecordHolder holder;

            if (row == null) {
                row = inflater.inflate(R.layout.mov_stud_pending_list, parent, false);
                holder = new RecordHolder();
                holder.text1 = (TextView) row.findViewById(R.id.text1);
                holder.text2 = (TextView) row.findViewById(R.id.text2);
                holder.text3 = (TextView) row.findViewById(R.id.text3);
                holder.text4 = (TextView) row.findViewById(R.id.text4);
                row.setTag(holder);
            } else holder = (RecordHolder) row.getTag();

            if (position % 2 == 0)
                row.setBackgroundResource(R.drawable.list_selector1);
            else
                row.setBackgroundResource(R.drawable.list_selector2);

            MovStudent listItem = data.get(position);
            holder.text1.setText(listItem.getStudentName());
            holder.text2.setText(listItem.getClassName());
            holder.text3.setText(listItem.getSectionFrom());
            holder.text4.setText(listItem.getSectionTo());

            return row;
        }

        public class RecordHolder {
            TextView text1;
            TextView text2;
            TextView text3;
            TextView text4;
        }

    }

}
