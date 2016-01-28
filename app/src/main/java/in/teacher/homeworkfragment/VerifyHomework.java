package in.teacher.homeworkfragment;

import in.teacher.activity.R;
import in.teacher.adapter.HomeworkViewAdapter;
import in.teacher.dao.ClasDao;
import in.teacher.dao.HomeworkDao;
import in.teacher.dao.SectionDao;
import in.teacher.dao.TempDao;
import in.teacher.homeworkfragment.HomeworkView;
import in.teacher.homeworkfragment.InsertHomework;
import in.teacher.model.HW;
import in.teacher.sqlite.Homework;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.ReplaceFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by vinkrish.
 * Don't expect comments explaining every piece of code, class and function names are self explanatory.
 */
@SuppressLint("InflateParams")
public class VerifyHomework extends Fragment {
    private Context context;
    private int sectionId;
    private List<String> hwMessage;
    private List<Integer> childList1 = new ArrayList<>();
    private ArrayList<Integer> subjectIdList = new ArrayList<>();
    private ArrayList<String> subjectNameList = new ArrayList<>();
    private static String hwDate;
    private SQLiteDatabase sqliteDatabase;
    private Homework homework;
    private ListView lv;
    private ArrayList<HW> hwList = new ArrayList<>();
    private HomeworkViewAdapter homeworkViewAdapter;
    private EditText sectionSpinner;
    private ArrayList<Integer> sectionIdList = new ArrayList<>();
    private ArrayList<String> sectionNameList = new ArrayList<>();
    private ArrayList<Integer> secTeacherIdList = new ArrayList<>();
    private ArrayList<Integer> selectedSecIdList = new ArrayList<>();
    private ArrayList<Integer> selectedTeacherIdList = new ArrayList<>();
    protected boolean[] secSelections;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.verify_homework, container, false);
        context = AppGlobal.getContext();
        sqliteDatabase = AppGlobal.getSqliteDatabase();
        hwDate = getToday();
        homework = new Homework();
        sectionSpinner = (EditText) view.findViewById(R.id.secSpinner);

        Temp t = TempDao.selectTemp(sqliteDatabase);
        int classId = t.getClassId();
        sectionId = t.getSectionId();

        String className = ClasDao.getClassName(classId, sqliteDatabase);
        String sectionName = SectionDao.getSectionName(sectionId, sqliteDatabase);

        Cursor c = sqliteDatabase.rawQuery("select  A.SubjectId, B.SubjectName from subjectteacher A, subjects B where A.SectionId=" + sectionId + " and A.SubjectId=B.SubjectId", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            subjectIdList.add(c.getInt(c.getColumnIndex("SubjectId")));
            subjectNameList.add(c.getString(c.getColumnIndex("SubjectName")));
            c.moveToNext();
        }
        c.close();

        String sql = "select B.SectionId, B.ClassTeacherId, B.SectionName from homeworkmessage A, section B " +
                "where A.SectionId!=" + sectionId + " and B.SectionId!=" + sectionId + " and B.ClassId=" + classId + " group by B.SectionId";
        Cursor c2 = sqliteDatabase.rawQuery(sql, null);
        c2.moveToFirst();
        while (!c2.isAfterLast()) {
            sectionIdList.add(c2.getInt(c2.getColumnIndex("SectionId")));
            secTeacherIdList.add(c2.getInt(c2.getColumnIndex("ClassTeacherId")));
            sectionNameList.add(c2.getString(c2.getColumnIndex("SectionName")));
            c2.moveToNext();
        }
        c2.close();
        secSelections = new boolean[sectionIdList.size()];

        TextView hwTv = (TextView) view.findViewById(R.id.hwPleaseTap);
        if (classId == 0) {
            hwTv.setText("Not a class teacher.");
        } else {
            String hwString = className + " - " + sectionName + "  Confirm your submission:   " + hwDate;
            hwTv.setText(hwString);
        }

        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReplaceFragment.replace(new InsertHomework(), getFragmentManager());
            }
        });

        view.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "homework has been updated successfully", Toast.LENGTH_LONG).show();
                HomeworkDao.insertHwPresent(sectionId, getToday(), sqliteDatabase);
                HomeworkDao.insertHwSql(homework, selectedSecIdList, selectedTeacherIdList, sqliteDatabase);
                ReplaceFragment.replace(new InsertHomework(), getFragmentManager());
            }
        });

        final Button todayButton = (Button) view.findViewById(R.id.today);
        final Button yesterdayButton = (Button) view.findViewById(R.id.yesterday);
        Button otherdayButton = (Button) view.findViewById(R.id.otherday);

        todayButton.setActivated(true);
        yesterdayButton.setActivated(false);
        otherdayButton.setActivated(false);

        yesterdayButton.setOnClickListener(yesterdayHomework);
        otherdayButton.setOnClickListener(otherdayHomework);

        prepareListDataNew();

        lv = (ListView) view.findViewById(R.id.list);
        homeworkViewAdapter = new HomeworkViewAdapter(context, hwList);
        lv.setAdapter(homeworkViewAdapter);

        sectionSpinner.setOnTouchListener(sectionTouch);

        return view;
    }

    private View.OnTouchListener sectionTouch = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                showSectionDialog();
            }
            int inType = sectionSpinner.getInputType();
            sectionSpinner.setInputType(InputType.TYPE_NULL);
            sectionSpinner.onTouchEvent(event);
            sectionSpinner.setInputType(inType);
            return false;
        }
    };

    public void showSectionDialog() {
        /*for (int i = 0; i < secSelections.length; i++) {
            if (selectedSecIdList.contains(sectionIdList.get(i))) {
                secSelections[i] = true;
            }
        }*/
        new AlertDialog.Builder(getActivity())
                .setTitle("Sections")
                .setCancelable(false)
                .setMultiChoiceItems(sectionNameList.toArray(new CharSequence[sectionIdList.size()]), secSelections, new SecSelectionClickHandler())
                .setPositiveButton("OK", new SectionButtonClickHandler()).show();

    }

    public class SecSelectionClickHandler implements DialogInterface.OnMultiChoiceClickListener {
        public void onClick(DialogInterface dialog, int clicked, boolean selected) {
            if (selected) secSelections[clicked] = true;
            else secSelections[clicked] = false;
        }
    }

    public class SectionButtonClickHandler implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int clicked) {
            selectedSection();
        }
    }

    private void selectedSection() {
        selectedSecIdList.clear();
        selectedTeacherIdList.clear();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sectionIdList.size(); i++) {
            if (secSelections[i]) {
                sb.append(sectionNameList.get(i) + ",");
                selectedSecIdList.add(sectionIdList.get(i));
                selectedTeacherIdList.add(secTeacherIdList.get(i));
            }
        }
        if (sb.length() > 0) sectionSpinner.setText(sb.toString().substring(0, sb.length() - 1));
        else sectionSpinner.setText("");
    }

    private View.OnClickListener yesterdayHomework = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Bundle b = new Bundle();
            b.putInt("today", 0);
            b.putInt("yesterday", 1);
            b.putInt("otherday", 0);
            Fragment fragment = new HomeworkView();
            fragment.setArguments(b);
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        }
    };

    private View.OnClickListener otherdayHomework = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Bundle b = new Bundle();
            b.putInt("today", 0);
            b.putInt("yesterday", 0);
            b.putInt("otherday", 1);
            Fragment fragment = new HomeworkView();
            fragment.setArguments(b);
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        }
    };

    private String getToday() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date today = new Date();
        return dateFormat.format(today);
    }

    public void prepareListDataNew() {
        childList1.clear();
        List<Homework> listHW = HomeworkDao.selectHomework(sectionId, hwDate, sqliteDatabase);
        if (listHW.size() != 0) {
            extractHomeworkNew(listHW);
        }
        hwList.clear();
        int hwLoop = 0;
        for (Integer childList11 : childList1) {
            for (int loop = 0; loop < subjectIdList.size(); loop++) {
                if (childList11.equals(subjectIdList.get(loop))) {
                    hwList.add(new HW(subjectNameList.get(loop), hwMessage.get(hwLoop)));
                    break;
                }
            }
            hwLoop += 1;
        }
    }

    public void extractHomeworkNew(List<Homework> hwList) {
        hwMessage = new ArrayList<>();
        for (Homework hw : hwList) {
            homework.setClassId(hw.getClassId());
            homework.setHomeworkId(hw.getHomeworkId());
            homework.setHomework(hw.getHomework());
            homework.setSchoolId(hw.getSchoolId());
            homework.setSectionId(hw.getSectionId());
            homework.setSubjectIDs(hw.getSubjectIDs());
            homework.setTeacherId(hw.getTeacherId());
            homework.setHomeworkDate(hw.getHomeworkDate());

            String subjectIds = hw.getSubjectIDs();
            String splitBy = ",";
            String[] id = subjectIds.split(splitBy);
            for (String subjectId : id) {
                childList1.add(Integer.parseInt(subjectId));
            }
            String messageBody = hw.getHomework();
            String splitBy2 = "#";
            String[] message = messageBody.split(splitBy2);
            hwMessage.addAll(Arrays.asList(message));
        }
    }

}
