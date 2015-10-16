package in.teacher.examfragment;

import in.teacher.activity.R;
import in.teacher.adapter.Capitalize;
import in.teacher.dao.ClasDao;
import in.teacher.dao.SectionDao;
import in.teacher.dao.SubjectExamsDao;
import in.teacher.dao.TeacherDao;
import in.teacher.dao.TempDao;
import in.teacher.examfragment.StructuredExam;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.ReplaceFragment;

import java.util.ArrayList;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.TextView;

/**
 * Created by vinkrish.
 */
public class HasPartition extends Fragment {
    private SQLiteDatabase sqliteDatabase;
    private Button name;
    private TextView clasSecSubTv;
    private String subjectName, teacherName;
    private int classId, sectionId, subjectId, subId, teacherId;
    private ArrayList<HasPartitionObj> circleArrayGrid = new ArrayList<>();
    private CircleAdapter cA;
    private StringBuffer exmName = new StringBuffer();
    private ArrayList<Integer> subIdList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.has_partition, container, false);

        Context context = AppGlobal.getContext();
        sqliteDatabase = AppGlobal.getSqliteDatabase();

        GridView gridView = (GridView) view.findViewById(R.id.gridView);
        name = (Button) view.findViewById(R.id.teacherName);
        clasSecSubTv = (TextView) view.findViewById(R.id.headerClasSecSub);

        cA = new CircleAdapter(context, R.layout.has_partition_grid, circleArrayGrid);
        gridView.setAdapter(cA);

        Temp t = TempDao.selectTemp(sqliteDatabase);
        classId = t.getCurrentClass();
        sectionId = t.getCurrentSection();
        subId = t.getSubjectId();
        subjectId = t.getCurrentSubject();
        teacherId = t.getTeacherId();
        subjectName = SubjectExamsDao.selectSubjectName(subjectId, sqliteDatabase);

        circleArrayGrid.clear();

        new CalledBackLoad().execute();

        return view;
    }

    public class HasPartitionObj {
        private int progressInt;
        private String txt;

        public HasPartitionObj(int progressInt, String txt) {
            this.progressInt = progressInt;
            this.txt = txt;
        }

        public int getProgressInt() {
            return progressInt;
        }

        public void setProgressInt(int progressInt) {
            this.progressInt = progressInt;
        }

        public String getTxt() {
            return txt;
        }

        public void setTxt(String txt) {
            this.txt = txt;
        }
    }

    public class CircleAdapter extends ArrayAdapter<HasPartitionObj> {
        Context context;
        int layoutResourceId;
        ArrayList<HasPartitionObj> data = new ArrayList<>();

        public CircleAdapter(Context context, int layoutResourceId, ArrayList<HasPartitionObj> gridArray) {
            super(context, layoutResourceId, gridArray);
            this.context = context;
            this.layoutResourceId = layoutResourceId;
            this.data = gridArray;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View row = convertView;
            RecordHolder holder;

            if (row == null) {
                LayoutInflater inflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(layoutResourceId, parent, false);

                holder = new RecordHolder();
                holder.txt = (TextView) row.findViewById(R.id.txt);
                row.setTag(holder);
            } else {
                holder = (RecordHolder) row.getTag();
            }

            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            FrameLayout fl = (FrameLayout) row.findViewById(R.id.fl);

            HasPartitionObj gridItem = data.get(position);
            holder.txt.setText(gridItem.getTxt());
            SampleView sV = new SampleView(context, gridItem.getProgressInt());
            fl.addView(sV, layoutParams);

            sV.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewClickListener(position);
                }
            });
            return row;
        }

        public class RecordHolder {
            TextView txt;
        }

        private class SampleView extends View {
            Paint p, defaultPaint;
            RectF rectF1;

            //	int localInt;
            public SampleView(Context context, int i) {
                super(context);
                setFocusable(true);
                //	localInt = i;
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
                rectF1 = new RectF(10, 10, 140, 140);
            }

            @Override
            protected void onDraw(Canvas canvas) {
                p.setAntiAlias(true);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(6);
                p.setColor(getResources().getColor(R.color.universal));
                canvas.drawArc(rectF1, 0, 360, false, p);
                //	canvas.drawArc (rectF1, 270, Float.parseFloat(localInt+""), false, p);
            }
        }
    }

    public void viewClickListener(int pos) {
        Temp t = new Temp();
        t.setCurrentSection(sectionId);
        t.setCurrentSubject(subIdList.get(pos));
        t.setCurrentClass(classId);
        t.setSubjectId(subId);
        TempDao.updateSecSubClas(t, sqliteDatabase);
        ReplaceFragment.replace(new StructuredExam(), getFragmentManager());
    }

    class CalledBackLoad extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            String className = ClasDao.getClassName(classId, sqliteDatabase);
            String sectionName = SectionDao.getSectionName(sectionId, sqliteDatabase);
            teacherName = Capitalize.capitalThis((TeacherDao.selectTeacherName(teacherId, sqliteDatabase)));
            exmName.append(className).append("-").append(sectionName).append("  ").append(subjectName);

            Cursor c = sqliteDatabase.rawQuery("select SubjectId,SubjectName from subjects where " +
                    "SubjectId=(select TheorySubjectId from subjects where SubjectId=" + subjectId + ") UNION " +
                    "select SubjectId,SubjectName from subjects " +
                    "where SubjectId=(select PracticalSubjectId from subjects where SubjectId=" + subjectId + ")", null);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                subIdList.add(c.getInt(c.getColumnIndex("SubjectId")));
                String s = c.getString(c.getColumnIndex("SubjectName"));
                //	ArrayList<String> subNameList = new ArrayList<>();
                //	subNameList.add(s);
                if (s.length() > 20) {
                    circleArrayGrid.add(new HasPartitionObj(100, s.substring(0, 20)));
                } else {
                    circleArrayGrid.add(new HasPartitionObj(100, s));
                }
                c.moveToNext();
            }
            c.close();

            return null;
        }

        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (teacherName.length() > 11) {
                name.setText(teacherName.substring(0, 9) + "...");
            } else {
                name.setText(teacherName);
            }
            clasSecSubTv.setText(exmName);
            cA.notifyDataSetChanged();
        }
    }
}
