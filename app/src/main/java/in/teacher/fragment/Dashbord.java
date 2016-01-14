package in.teacher.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import in.teacher.activity.R;
import in.teacher.adapter.Capitalize;
import in.teacher.dao.ClasDao;
import in.teacher.dao.SectionDao;
import in.teacher.dao.StAvgDao;
import in.teacher.dao.TeacherDao;
import in.teacher.dao.TempDao;
import in.teacher.sectionincharge.AcceptStudent;
import in.teacher.sqlite.CircleObject;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.CommonDialogUtils;
import in.teacher.util.ReplaceFragment;
import in.teacher.util.SharedPreferenceUtil;

/**
 * Created by vinkrish.
 */
public class Dashbord extends Fragment {
    private Activity activity;
    private Context context;
    private int teacherId, sectionId, classId;
    private ArrayList<Integer> sectionIdList = new ArrayList<>();
    private ArrayList<Integer> classIdList = new ArrayList<>();
    private ArrayList<String> classNameList = new ArrayList<>();
    private ArrayList<String> sectionNameList = new ArrayList<>();
    private String teacherName;
    private ArrayList<Integer> subjectIdList = new ArrayList<>();
    private ArrayList<String> subjectNameList = new ArrayList<>();
    private ArrayList<Integer> hasPartitionList = new ArrayList<>();
    private SQLiteDatabase sqliteDatabase;
    private ArrayList<CircleObject> circleArrayGrid = new ArrayList<>();
    private CircleAdapter cA;
    private GridView gridView;
    private TextView name;
    private SwitchCompat classIncharge;
    private boolean isClassIncharge, isMoveNotification;
    private LinearLayout moveNotification;
    private Button moveAction;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dashbord, container, false);

        gridView = (GridView) view.findViewById(R.id.gridView);
        name = (TextView) view.findViewById(R.id.teacherName);
        classIncharge = (SwitchCompat) view.findViewById(R.id.classIncharge);
        moveNotification = (LinearLayout) view.findViewById(R.id.move_notification);
        moveAction = (Button) view.findViewById(R.id.move_action);

        init();

        if (ClasDao.isSwitchClass(teacherId, sqliteDatabase)) {
            view.findViewById(R.id.switchClass).setOnClickListener(switchClass);
        } else {
            view.findViewById(R.id.switchClass).setVisibility(View.GONE);
        }

        new CalledBackLoad().execute();

        return view;
    }

    private void init() {
        activity = AppGlobal.getActivity();
        context = AppGlobal.getContext();
        sqliteDatabase = AppGlobal.getSqliteDatabase();

        CommonDialogUtils.hideKeyboard(getActivity());

        Temp t = TempDao.selectTemp(sqliteDatabase);
        classId = t.getClassId();
        sectionId = t.getSectionId();
        teacherId = t.getTeacherId();

        cA = new CircleAdapter(context, R.layout.dashboard_grid, circleArrayGrid);
        gridView.setAdapter(cA);

        classIncharge.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ReplaceFragment.replace(new TeacherInCharge(), getFragmentManager());
                }
            }
        });

        moveAction.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ReplaceFragment.replace(new AcceptStudent(), getFragmentManager());
            }
        });

    }

    private View.OnClickListener switchClass = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CommonDialogUtils.displaySwitchClass(getActivity(), sqliteDatabase, new Dashbord());
        }
    };

    public void callUpdateTemp(int pos) {
        Temp t = new Temp();
        t.setCurrentSection(sectionIdList.get(pos));
        t.setCurrentSubject(subjectIdList.get(pos));
        t.setCurrentClass(classIdList.get(pos));
        t.setSubjectId(subjectIdList.get(pos));
        TempDao.updateSecSubClas(t, sqliteDatabase);
        if (hasPartitionList.get(pos) == 1) {
            SharedPreferenceUtil.updatePartition(context, 1);
        } else {
            SharedPreferenceUtil.updatePartition(context, 0);
        }
    }

    public class CircleAdapter extends ArrayAdapter<CircleObject> {
        private Context context2;
        private int layoutResourceId;
        private ArrayList<CircleObject> data = new ArrayList<>();
        private LayoutInflater inflater;

        public CircleAdapter(Context context, int layoutResourceId, ArrayList<CircleObject> gridArray) {
            super(context, layoutResourceId, gridArray);
            this.context2 = context;
            this.layoutResourceId = layoutResourceId;
            this.data = gridArray;
            inflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View row = convertView;
            RecordHolder holder;

            if (row == null) {
                row = inflater.inflate(layoutResourceId, parent, false);
                holder = new RecordHolder();
                holder.classTxt = (TextView) row.findViewById(R.id.class_name);
                holder.secTxt = (TextView) row.findViewById(R.id.section_name);
                holder.subTxt = (TextView) row.findViewById(R.id.subject_name);
                holder.dashbordGrid = (LinearLayout) row.findViewById(R.id.dashboard_grid);
                row.setTag(holder);
            } else
                holder = (RecordHolder) row.getTag();

            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            FrameLayout fl = (FrameLayout) row.findViewById(R.id.fl);

            CircleObject gridItem = data.get(position);
            holder.classTxt.setText(gridItem.getClas());
            holder.secTxt.setText(gridItem.getSection());
            holder.subTxt.setText(gridItem.getSubject());

            SampleView sV = new SampleView(context2, gridItem.getProgressInt());
            fl.addView(sV, layoutParams);

            holder.dashbordGrid.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewClickListener(position);
                }
            });
            return row;
        }

        public class RecordHolder {
            LinearLayout dashbordGrid;
            TextView classTxt;
            TextView secTxt;
            TextView subTxt;
        }

        private class SampleView extends View {
            Paint p, defaultPaint;
            RectF rectF;
            int localInt;

            public SampleView(Context context, int i) {
                super(context);
                setFocusable(true);
                localInt = i;
                init();
            }

            public void init() {
                p = new Paint();
                defaultPaint = new Paint();
                defaultPaint.setAntiAlias(true);
                defaultPaint.setStyle(Paint.Style.STROKE);
                defaultPaint.setStrokeWidth(6);
                Resources res = getResources();
                int defalt = res.getColor(R.color.defalt);
                defaultPaint.setColor(defalt);

                rectF = new RectF(25, 35, 105, 115);
            }

            @Override
            protected void onDraw(Canvas canvas) {
                p.setAntiAlias(true);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(6);

                if (localInt >= 270)
                    p.setColor(getResources().getColor(R.color.green));
                else if (localInt >= 180)
                    p.setColor(getResources().getColor(R.color.orange));
                else if (localInt > 0)
                    p.setColor(getResources().getColor(R.color.red));

                canvas.drawArc(rectF, 0, 360, false, defaultPaint);
                canvas.drawArc(rectF, 270, Float.parseFloat(localInt + ""), false, p);
            }
        }
    }

    public void viewClickListener(int position) {
        callUpdateTemp(position);
        CommonDialogUtils.displayDashbordSelector(activity, sqliteDatabase);
    }

    class CalledBackLoad extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            Cursor c2 = sqliteDatabase.rawQuery("select * from classteacher_incharge where TeacherId = " + teacherId, null);
            if (c2.getCount() > 0) {
                isClassIncharge = true;
            }
            c2.close();

            Cursor c = sqliteDatabase.rawQuery("select A.ClassId, A.SectionId, A.SubjectId, B.ClassName, C.SectionName, D.SubjectName, D.has_partition " +
                    "from subjectteacher A, class B, section C, subjects D " +
                    "where A.TeacherId=" + teacherId + " and A.ClassId=B.ClassId and A.SectionId=C.SectionId and A.SubjectId=D.SubjectId", null);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                classIdList.add(c.getInt(c.getColumnIndex("ClassId")));
                sectionIdList.add(c.getInt(c.getColumnIndex("SectionId")));
                subjectIdList.add(c.getInt(c.getColumnIndex("SubjectId")));
                classNameList.add(c.getString(c.getColumnIndex("ClassName")));
                sectionNameList.add(c.getString(c.getColumnIndex("SectionName")));
                subjectNameList.add(c.getString(c.getColumnIndex("SubjectName")));
                hasPartitionList.add(c.getInt(c.getColumnIndex("has_partition")));
                c.moveToNext();
            }
            c.close();

            for (int i = 0; i < sectionIdList.size(); i++) {
                int avg = StAvgDao.selectStAvg(sectionIdList.get(i), subjectIdList.get(i), sqliteDatabase);
                circleArrayGrid.add(new CircleObject(avg, classNameList.get(i), sectionNameList.get(i), subjectNameList.get(i)));
            }

            Cursor c3 = sqliteDatabase.rawQuery("select Status from movestudent where SecIdTo = " + sectionId + " and Status = 0", null);
            if (c3.getCount() > 0) {
                isMoveNotification = true;
            }
            c3.close();

            return null;
        }

        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (!isClassIncharge) classIncharge.setVisibility(View.GONE);

            teacherName = Capitalize.capitalThis((TeacherDao.selectTeacherName(teacherId, sqliteDatabase)));
            String clasName = ClasDao.getClassName(classId, sqliteDatabase);
            String secName = SectionDao.getSectionName(sectionId, sqliteDatabase);
            if (classId == 0) {
                name.setText("[ "+teacherName + " ] " + "Classes");
            } else
                name.setText(teacherName + "  [ " + clasName + " - " + secName + " ]");
            cA.notifyDataSetChanged();

            if (isMoveNotification) moveNotification.setVisibility(View.VISIBLE);
        }
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

}
