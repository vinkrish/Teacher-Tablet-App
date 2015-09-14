package in.teacher.fragment;

import in.teacher.activity.R;
import in.teacher.adapter.Capitalize;
import in.teacher.dao.ActivitiDao;
import in.teacher.dao.ClasDao;
import in.teacher.dao.ExamsDao;
import in.teacher.dao.SectionDao;
import in.teacher.dao.SubActivityDao;
import in.teacher.dao.SubActivityGradeDao;
import in.teacher.dao.SubActivityMarkDao;
import in.teacher.dao.SubjectExamsDao;
import in.teacher.dao.TeacherDao;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.Activiti;
import in.teacher.sqlite.SeObject;
import in.teacher.sqlite.SubActivity;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.PKGenerator;
import in.teacher.util.ReplaceFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by vinkrish.
 */

public class SubActivityExam extends Fragment {
    private Context context;
    private SQLiteDatabase sqliteDatabase;
    private int sectionId, classId, subjectId, activityId, examId;
    private String className, sectionName, subjectName;
    private List<Integer> subActIdList = new ArrayList<>();
    private ArrayList<SeObject> circleArrayGrid = new ArrayList<>();
    private CircleAdapter cA;
    final Map<Object, Object> mi1 = new HashMap<>();
    final Map<Object, Object> mi2 = new HashMap<>();
    private GridView gridView;
    private TextView clasSecSubTv;
    private Bitmap inserted, notinserted;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.subactivity_exam, container, false);
        context = AppGlobal.getContext();
        sqliteDatabase = AppGlobal.getSqliteDatabase();

        gridView = (GridView) view.findViewById(R.id.gridView);
        cA = new CircleAdapter(context, R.layout.act_grid, circleArrayGrid);
        clasSecSubTv = (TextView) view.findViewById(R.id.headerClasSecSub);
        inserted = BitmapFactory.decodeResource(this.getResources(), R.drawable.tick);
        notinserted = BitmapFactory.decodeResource(this.getResources(), R.drawable.cross);

        initializeList();

        Temp t = TempDao.selectTemp(sqliteDatabase);
        examId = t.getExamId();
        classId = t.getCurrentClass();
        sectionId = t.getCurrentSection();
        subjectId = t.getCurrentSubject();
        activityId = t.getActivityId();
        subjectName = SubjectExamsDao.selectSubjectName(subjectId, sqliteDatabase);

        new CalledBackLoad().execute();

        view.findViewById(R.id.examButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ReplaceFragment.replace(new StructuredExam(), getFragmentManager());
            }
        });

        view.findViewById(R.id.activityButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ReplaceFragment.replace(new ActivityExam(), getFragmentManager());
            }
        });

        return view;
    }

    private void initializeList() {
        mi1.clear();
        mi2.clear();
        circleArrayGrid.clear();
        subActIdList.clear();
    }

    public void viewClickListener(int position) {
        int i = subActIdList.get(position);
        TempDao.updateSubActivityId(i, sqliteDatabase);
        Boolean b1 = (Boolean) mi1.get(i);
        Boolean b2 = (Boolean) mi2.get(i);
        if (b1 != null && b1) {
            ReplaceFragment.replace(new UpdateSubActivityMark(), getFragmentManager());
        } else if (b2 != null && b2) {
            ReplaceFragment.replace(new UpdateSubActivityGrade(), getFragmentManager());
        } else {
            ReplaceFragment.replace(new InsertSubActivityMark(), getFragmentManager());
        }
    }

    public class CircleAdapter extends ArrayAdapter<SeObject> {
        Context context;
        int layoutResourceId;
        ArrayList<SeObject> data = new ArrayList<>();

        public CircleAdapter(Context context, int layoutResourceId, ArrayList<SeObject> gridArray) {
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
                holder.image = (ImageView) row.findViewById(R.id.tickCross);
                holder.examTxt = (TextView) row.findViewById(R.id.exam);
                row.setTag(holder);

            } else {
                holder = (RecordHolder) row.getTag();
            }

            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            FrameLayout fl = (FrameLayout) row.findViewById(R.id.fl);

            SeObject gridItem = data.get(position);
            holder.examTxt.setText(gridItem.getExam());
            holder.image.setImageBitmap(gridItem.getTickCross());
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
            TextView examTxt;
            ImageView image;
        }

        private class SampleView extends View {
            Paint p, defaultPaint;
            RectF rectF1;
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
                defaultPaint.setStrokeWidth(5);
                Resources res = getResources();
                int defalt = res.getColor(R.color.defalt);
                defaultPaint.setColor(defalt);
                rectF1 = new RectF(10, 10, 130, 130);
            }

            @Override
            protected void onDraw(Canvas canvas) {
                p.setAntiAlias(true);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(5);

                if (localInt >= 270) {
                    p.setColor(getResources().getColor(R.color.green));
                } else if (localInt >= 180) {
                    p.setColor(getResources().getColor(R.color.orange));
                } else if (localInt > 0) {
                    p.setColor(getResources().getColor(R.color.red));
                }
                canvas.drawArc(rectF1, 0, 360, false, defaultPaint);
                canvas.drawArc(rectF1, 270, Float.parseFloat(localInt + ""), false, p);
            }

        }

    }

    class CalledBackLoad extends AsyncTask<String, String, String> {
        private StringBuffer sf = new StringBuffer();

        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            className = ClasDao.getClassName(classId, sqliteDatabase);
            sectionName = SectionDao.getSectionName(sectionId, sqliteDatabase);

            List<SubActivity> subActivityList = SubActivityDao.selectSubActivity(activityId, sqliteDatabase);

            String examName = ExamsDao.selectExamName(examId, sqliteDatabase);
            Activiti act = ActivitiDao.getActiviti(activityId, sqliteDatabase);
            String activityName = Capitalize.capitalThis(act.getActivityName());
            sf.append(className).append("-").append(sectionName).append("    ").append(subjectName).append("    ").append(examName).append("    ").append(activityName);

            for (SubActivity a : subActivityList) {
                int markEntry = SubActivityMarkDao.isThereSubActMark(a.getSubActivityId(), subjectId, sqliteDatabase);
                if (markEntry == 1)
                    mi1.put(a.getSubActivityId(), true);

                int gradeEntry = SubActivityGradeDao.isThereSubActGrade(a.getSubActivityId(), subjectId, sqliteDatabase);
                if (gradeEntry == 1)
                    mi2.put(a.getSubActivityId(), true);

                subActIdList.add(a.getSubActivityId());
                int avg = (int) a.getSubActivityAvg();

                if (a.getCompleteEntry() == 1) {
                    circleArrayGrid.add(new SeObject(avg, Capitalize.capitalThis(PKGenerator.trim(0, 20, a.getSubActivityName())), sf.toString(), inserted));
                } else {
                    circleArrayGrid.add(new SeObject(avg, Capitalize.capitalThis(PKGenerator.trim(0, 20, a.getSubActivityName())), sf.toString(), notinserted));
                }

            }

            return null;
        }

        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            clasSecSubTv.setText(sf);
            gridView.setAdapter(cA);
        }
    }

}
