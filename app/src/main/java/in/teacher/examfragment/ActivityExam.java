package in.teacher.examfragment;

import in.teacher.activity.R;
import in.teacher.adapter.Capitalize;
import in.teacher.dao.ActivitiDao;
import in.teacher.dao.ActivityGradeDao;
import in.teacher.dao.ActivityMarkDao;
import in.teacher.dao.ClasDao;
import in.teacher.dao.ExamsDao;
import in.teacher.dao.SectionDao;
import in.teacher.dao.SubjectExamsDao;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.Activiti;
import in.teacher.sqlite.SeObject;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
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
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by vinkrish.
 * Don't expect comments explaining every piece of code, class and function names are self explanatory.
 */
public class ActivityExam extends Fragment {
    private SQLiteDatabase sqliteDatabase;
    private int sectionId, classId, subjectId;
    private long examId;
    private String className, sectionName, subjectName;
    private static boolean isSubActivity;
    private List<Long> actIdList = new ArrayList<>();
    private ArrayList<SeObject> circleArrayGrid = new ArrayList<>();
    private CircleAdapter cA;
    private final Map<Object, Object> mi1 = new HashMap<>();
    private final Map<Object, Object> mi2 = new HashMap<>();
    private GridView gridView;
    private TextView clasSecSubTv;
    private Bitmap inserted, notinserted;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_exam, container, false);

        gridView = (GridView) view.findViewById(R.id.gridView);
        clasSecSubTv = (TextView) view.findViewById(R.id.headerClasSecSub);

        init();

        new CalledBackLoad().execute();

        view.findViewById(R.id.examButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ReplaceFragment.replace(new StructuredExam(), getFragmentManager());
            }
        });

        return view;
    }

    private void init() {
        Context context = AppGlobal.getContext();
        sqliteDatabase = AppGlobal.getSqliteDatabase();

        cA = new CircleAdapter(context, R.layout.se_grid, circleArrayGrid);
        inserted = BitmapFactory.decodeResource(this.getResources(), R.drawable.tick);
        notinserted = BitmapFactory.decodeResource(this.getResources(), R.drawable.cross);

        Temp t = TempDao.selectTemp(sqliteDatabase);
        classId = t.getCurrentClass();
        sectionId = t.getCurrentSection();
        subjectId = t.getCurrentSubject();
        examId = t.getExamId();
        subjectName = SubjectExamsDao.selectSubjectName(subjectId, sqliteDatabase);
    }

    public void viewClickListener(int position) {
        long i = actIdList.get(position);
        ActivityMarkDao.updateActivityId(i, sqliteDatabase);
        isSubActivity = ActivityMarkDao.selectSubActivity(i, sqliteDatabase);
        if (isSubActivity) {
            ReplaceFragment.replace(new SubActivityExam(), getFragmentManager());
        } else {
            Boolean b1 = (Boolean) mi1.get(i);
            Boolean b2 = (Boolean) mi2.get(i);
            if (b1 != null && b1) {
                ReplaceFragment.replace(new UpdateActivityMark(), getFragmentManager());
            } else if (b2 != null && b2) {
                ReplaceFragment.replace(new UpdateActivityGrade(), getFragmentManager());
            } else {
                ReplaceFragment.replace(new InsertActivityMark(), getFragmentManager());
            }

        }
    }

    public class CircleAdapter extends ArrayAdapter<SeObject> {
        Context context;
        int layoutResourceId;
        ArrayList<SeObject> data = new ArrayList<>();
        private LayoutInflater inflater = null;

        public CircleAdapter(Context context, int layoutResourceId, ArrayList<SeObject> gridArray) {
            super(context, layoutResourceId, gridArray);
            this.context = context;
            this.layoutResourceId = layoutResourceId;
            this.data = gridArray;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View row = convertView;
            RecordHolder holder;

            if (row == null) {
                row = inflater.inflate(layoutResourceId, parent, false);
                holder = new RecordHolder();
                //holder.image = (ImageView) row.findViewById(R.id.tickCross);
                holder.examTxt = (TextView) row.findViewById(R.id.exam);
                holder.seGrid = (LinearLayout) row.findViewById(R.id.se_grid);
                row.setTag(holder);
            } else {
                holder = (RecordHolder) row.getTag();
            }

            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            FrameLayout fl = (FrameLayout) row.findViewById(R.id.fl);

            SeObject gridItem = data.get(position);
            holder.examTxt.setText(gridItem.getExam());
            //holder.image.setImageBitmap(gridItem.getTickCross());
            SampleView sV = new SampleView(context, gridItem.getProgressInt());
            fl.addView(sV, layoutParams);

            holder.seGrid.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewClickListener(position);
                }
            });

            return row;

        }

        public class RecordHolder {
            TextView examTxt;
            LinearLayout seGrid;
            //ImageView image;
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
                defaultPaint.setStrokeWidth(6);
                Resources res = getResources();
                int defalt = res.getColor(R.color.defalt);
                defaultPaint.setColor(defalt);
                rectF1 = new RectF(15, 25, 115, 125);
            }

            @Override
            protected void onDraw(Canvas canvas) {
                p.setAntiAlias(true);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(6);

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
            List<Activiti> activitiList = ActivitiDao.selectActiviti(examId, subjectId, sectionId, sqliteDatabase);

            String examName = ExamsDao.selectExamName(examId, sqliteDatabase);
            sf.append(className).append("-").append(sectionName).append("    ").append(subjectName).append("    ").append(examName);

            for (Activiti a : activitiList) {
                int markEntry = ActivityMarkDao.isThereActMark(a.getActivityId(), subjectId, sqliteDatabase);
                if (markEntry == 1)
                    mi1.put(a.getActivityId(), true);

                int gradeEntry = ActivityGradeDao.isThereActGrade(a.getActivityId(), subjectId, sqliteDatabase);
                if (gradeEntry == 1)
                    mi2.put(a.getActivityId(), true);

                actIdList.add(a.getActivityId());
                int avg = (int) a.getActivityAvg();

                if (a.getCompleteEntry() == 1) {
                    circleArrayGrid.add(new SeObject(avg, Capitalize.capitalThis(a.getActivityName()), inserted));
                } else {
                    circleArrayGrid.add(new SeObject(avg, Capitalize.capitalThis(a.getActivityName()), notinserted));
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
