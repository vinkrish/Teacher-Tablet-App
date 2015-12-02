package in.teacher.sectionincharge;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.teacher.activity.R;
import in.teacher.dao.ClasDao;
import in.teacher.dao.SectionDao;
import in.teacher.dao.SlipTesttDao;
import in.teacher.dao.StAvgDao;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.MovStudent;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.CommonDialogUtils;
import in.teacher.util.PercentageSlipTest;
import in.teacher.util.ReplaceFragment;

/**
 * Created by vinkrish on 02/12/15.
 */
public class AcceptStudent extends Fragment {
    private SQLiteDatabase sqliteDatabase;
    private int classId, sectionId, schoolId;
    private String secName, className;
    private TextView movingClassTv;
    private ListView listView;
    private MoveStudAdapter moveStudAdapter;
    private List<MovStudent> movStudentList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.accept_student, container, false);

        initView(view);
        init();

        return view;
    }

    private void initView(View view) {
        movingClassTv = (TextView) view.findViewById(R.id.moving_class);
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
        movingClassTv.setText("Accept students to " + className + " - " + secName);

        initPendingStudent();
        moveStudAdapter = new MoveStudAdapter(getActivity(), movStudentList);
        listView.setAdapter(moveStudAdapter);
    }

    private OnClickListener switchClass = new OnClickListener() {
        @Override
        public void onClick(View v) {
            CommonDialogUtils.displaySwitchClass(getActivity(), sqliteDatabase, new AcceptStudent());
        }
    };

    private void initPendingStudent() {
        Cursor c = sqliteDatabase.rawQuery("select * from movestudent where SecIdTo = " + sectionId + " and Status = 0", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            MovStudent ms = new MovStudent();
            ms.setSchooId(schoolId);
            ms.setStudentId(c.getInt(c.getColumnIndex("StudentId")));
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
            super(context, R.layout.accept_stud_pending_list, objects);
            this.data = objects;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            RecordHolder holder;

            if (row == null) {
                row = inflater.inflate(R.layout.accept_stud_pending_list, parent, false);
                holder = new RecordHolder();
                holder.text1 = (TextView) row.findViewById(R.id.text1);
                holder.text2 = (TextView) row.findViewById(R.id.text2);
                holder.text3 = (TextView) row.findViewById(R.id.text3);
                holder.text4 = (TextView) row.findViewById(R.id.text4);
                holder.btn = (Button) row.findViewById(R.id.btn);
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
            holder.btn.setOnClickListener(respondClickListener);

            return row;
        }

        private OnClickListener respondClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                ListView mListView = (ListView) v.getParent().getParent();
                final int position = mListView.getPositionForView((View) v.getParent());
                respond(position);
            }
        };

        public void respond(final int position) {
            final MovStudent ms = movStudentList.get(position);
            AlertDialog.Builder submitBuilder = new AlertDialog.Builder(getActivity());
            submitBuilder.setCancelable(false);
            submitBuilder.setTitle("Confirm your action");
            submitBuilder.setMessage("Do you want to accept " + ms.getStudentName() + " to " + ms.getClassName() +" - " + ms.getSectionTo());
            submitBuilder.setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int arg1) {
                    dialog.cancel();
                    String sql = "update movestudent set Status = 2 where StudentId = " + ms.getStudentId();
                    try {
                        sqliteDatabase.execSQL(sql);
                        ContentValues cv = new ContentValues();
                        cv.put("Query", sql);
                        sqliteDatabase.insert("uploadsql", null, cv);
                    } catch (SQLException e) {
                    }
                    ReplaceFragment.replace(new AcceptStudent(), getFragmentManager());
                }
            });
            submitBuilder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int arg1) {
                    String sql = "update movestudent set Status = 1 where StudentId = " + ms.getStudentId();
                    String query = ms.getQuery();
                    try {
                        sqliteDatabase.execSQL(sql);
                        ContentValues cv = new ContentValues();
                        cv.put("Query", sql);
                        sqliteDatabase.insert("uploadsql", null, cv);
                    } catch (SQLException e) {
                    }
                    try {
                        sqliteDatabase.execSQL(query);
                        ContentValues cv = new ContentValues();
                        cv.put("Query", query);
                        sqliteDatabase.insert("uploadsql", null, cv);
                    } catch (SQLException e) {
                    }
                    ReplaceFragment.replace(new AcceptStudent(), getFragmentManager());
                }
            });
            submitBuilder.show();
        }

        public class RecordHolder {
            TextView text1;
            TextView text2;
            TextView text3;
            TextView text4;
            Button btn;
        }

    }

}
