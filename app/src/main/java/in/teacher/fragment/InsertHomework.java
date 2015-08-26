package in.teacher.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import in.teacher.activity.R;
import in.teacher.adapter.Capitalize;
import in.teacher.adapter.HomeworkViewAdapter;
import in.teacher.dao.ClasDao;
import in.teacher.dao.HomeworkDao;
import in.teacher.dao.SectionDao;
import in.teacher.dao.TeacherDao;
import in.teacher.dao.TempDao;
import in.teacher.model.HW;
import in.teacher.sqlite.Homework;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.CommonDialogUtils;
import in.teacher.util.PKGenerator;
import in.teacher.util.ReplaceFragment;

public class InsertHomework extends Fragment {
    private Context context;
    private Activity act;
    private int sectionId, teacherId, classId, schoolId, block = 0, noOfHw = 0;
    private long hwId;
    private String hwDate, rawSubjectIDs, rawHomework, prepareSubjectIds, prepareHomeworks;
    private ArrayList<Integer> subjectIdList = new ArrayList<>();
    private ArrayList<String> subjectNameList = new ArrayList<>();
    private List<String> hwMessage;
    private List<Integer> childList1 = new ArrayList<>();
    private boolean newHw = false, editingHw;
    private SQLiteDatabase sqliteDatabase;
    private TextView hwTv;
    private ListView lv;
    private ArrayList<HW> hwList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_insert_homework, container, false);

        context = AppGlobal.getContext();
        sqliteDatabase = AppGlobal.getSqliteDatabase();
        act = AppGlobal.getActivity();

        lv = (ListView) view.findViewById(R.id.list);
        HomeworkViewAdapter homeworkViewAdapter = new HomeworkViewAdapter(context, hwList);
        lv.setAdapter(homeworkViewAdapter);

        hwTv = (TextView) view.findViewById(R.id.hwPleaseTap);
        Button submit = (Button) view.findViewById(R.id.hwSubmit);
        Button reset = (Button) view.findViewById(R.id.hwReset);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newHw) {
                    CommonDialogUtils.displayAlertWhiteDialog(act, "Homework is not entered");
                } else {
                    ReplaceFragment.replace(new VerifyHomework(), getFragmentManager());
                }
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!newHw) {
                    HomeworkDao.deleteHomework(hwId, sqliteDatabase);
                    ReplaceFragment.replace(new InsertHomework(), getFragmentManager());
                }
            }
        });

        Temp t = TempDao.selectTemp(sqliteDatabase);
        classId = t.getClassId();
        sectionId = t.getSectionId();
        teacherId = t.getTeacherId();
        schoolId = t.getSchoolId();

        hwDate = getToday();

        block = HomeworkDao.isHwPresent(sectionId, getToday(), sqliteDatabase);
        String className = ClasDao.getClassName(classId, sqliteDatabase);
        String sectionName = SectionDao.getSectionName(sectionId, sqliteDatabase);

        Cursor c = sqliteDatabase.rawQuery("select A.SubjectId, B.SubjectName from subjectteacher A, subjects B where A.SectionId=" + sectionId + " and A.SubjectId=B.SubjectId", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            subjectIdList.add(c.getInt(c.getColumnIndex("SubjectId")));
            subjectNameList.add(c.getString(c.getColumnIndex("SubjectName")));
            c.moveToNext();
        }
        c.close();
        subjectIdList.add(0);
        subjectNameList.add("Extra");

        if (classId == 0) {
            hwTv.setText("Not a class teacher.");
            lv.setVisibility(View.GONE);
            view.findViewById(R.id.rl).setVisibility(View.GONE);
        } else {
            StringBuilder hwString = new StringBuilder();
            hwString.append(className).append("-" + sectionName + "  ").append("Tap on the list to assign homework  ").append(getToday());
            hwTv.setText(hwString);
        }

        Button todayButton = (Button) view.findViewById(R.id.today);
        Button yesterdayButton = (Button) view.findViewById(R.id.yesterday);
        Button otherdayButton = (Button) view.findViewById(R.id.otherday);

        todayButton.setActivated(true);
        yesterdayButton.setActivated(false);
        otherdayButton.setActivated(false);

        yesterdayButton.setOnClickListener(yesterdayHomework);
        otherdayButton.setOnClickListener(otherdayHomework);

        populateList();

        if (block == 1) {
            LinearLayout hide = (LinearLayout) view.findViewById(R.id.rl);
            hide.setVisibility(View.GONE);
        }

        lv.setOnItemClickListener(clickOnSubject);

        return view;
    }

    private AdapterView.OnItemClickListener clickOnSubject = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            if (block != 1) {
                editingHw = false;
                AlertDialog.Builder builder = new AlertDialog.Builder(act);
                View view2 = act.getLayoutInflater().inflate(R.layout.hw_dialog, null);
                TextView hwSub = (TextView) view2.findViewById(R.id.hwtxt);
                hwSub.setText(hwList.get(position).getSubject() + " Homework");
                final EditText edListChild = (EditText) view2.findViewById(R.id.hwmessage);
                String s = hwList.get(position).getHomework();
                if (s.equals("-")) {
                    edListChild.setText("");
                } else {
                    editingHw = true;
                    edListChild.setText(s);
                }
                builder.setView(view2);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (edListChild.getText().toString().equals("") && newHw) {
                            CommonDialogUtils.displayAlertWhiteDialog(act, "Please enter homework");
                        } else if (newHw && !edListChild.getText().toString().equals("")) {
                            Homework hw = new Homework();
                            hw.setClassId(classId + "");
                            hw.setHomeworkId(PKGenerator.returnPrimaryKey(schoolId));
                            hw.setHomework(edListChild.getText().toString());
                            hw.setSchoolId(schoolId + "");
                            hw.setSectionId(sectionId + "");
                            hw.setSubjectIDs(hwList.get(position).getSubjectId() + "");
                            hw.setTeacherId(teacherId + "");
                            hw.setHomeworkDate(hwDate);
                            HomeworkDao.insertHW(hw, sqliteDatabase);
                            InputMethodManager imm2 = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm2.hideSoftInputFromWindow(edListChild.getWindowToken(), 0);
                            ReplaceFragment.replace(new InsertHomework(), getFragmentManager());
                        } else if (editingHw) {
                            if (noOfHw == 1) {
                                if (edListChild.getText().toString().equals("")) {
                                    HomeworkDao.deleteHomework(hwId, sqliteDatabase);
                                } else {
                                    HomeworkDao.deleteHomework(hwId, sqliteDatabase);
                                    Homework hw = new Homework();
                                    hw.setClassId(classId + "");
                                    hw.setHomework(edListChild.getText().toString());
                                    hw.setSchoolId(schoolId + "");
                                    hw.setSectionId(sectionId + "");
                                    hw.setSubjectIDs(childList1.get(position) + "");
                                    hw.setTeacherId(teacherId + "");
                                    hw.setHomeworkDate(hwDate);
                                    HomeworkDao.insertHW(hw, sqliteDatabase);
                                }
                            } else {
                                if (edListChild.getText().toString().equals("")) {
                                    HomeworkDao.deleteHomework(hwId, sqliteDatabase);
                                    prepareHomework(position);
                                    Homework hw = new Homework();
                                    hw.setClassId(classId + "");
                                    hw.setHomework(prepareHomeworks);
                                    hw.setSchoolId(schoolId + "");
                                    hw.setSectionId(sectionId + "");
                                    hw.setSubjectIDs(prepareSubjectIds);
                                    hw.setTeacherId(teacherId + "");
                                    hw.setHomeworkDate(hwDate);
                                    HomeworkDao.insertHW(hw, sqliteDatabase);
                                } else {
                                    HomeworkDao.deleteHomework(hwId, sqliteDatabase);
                                    prepareUpdateHomework(position, edListChild.getText().toString());
                                    Homework hw = new Homework();
                                    hw.setClassId(classId + "");
                                    hw.setHomework(prepareHomeworks);
                                    hw.setSchoolId(schoolId + "");
                                    hw.setSectionId(sectionId + "");
                                    hw.setSubjectIDs(prepareSubjectIds);
                                    hw.setTeacherId(teacherId + "");
                                    hw.setHomeworkDate(hwDate);
                                    HomeworkDao.insertHW(hw, sqliteDatabase);
                                }
                            }
                            ReplaceFragment.replace(new InsertHomework(), getFragmentManager());
                        } else if (!edListChild.getText().toString().equals("")) {
                            Homework hw = new Homework();
                            hw.setClassId(classId + "");
                            hw.setHomeworkId(PKGenerator.returnPrimaryKey(schoolId));
                            hw.setHomework(rawHomework + "#" + edListChild.getText().toString());
                            hw.setSchoolId(schoolId + "");
                            hw.setSectionId(sectionId + "");
                            hw.setSubjectIDs(rawSubjectIDs + "," + hwList.get(position).getSubjectId() + "");
                            hw.setTeacherId(teacherId + "");
                            hw.setHomeworkDate(hwDate);
                            HomeworkDao.deleteHomework(hwId, sqliteDatabase);
                            HomeworkDao.insertHW(hw, sqliteDatabase);
                            InputMethodManager imm2 = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm2.hideSoftInputFromWindow(edListChild.getWindowToken(), 0);
                            ReplaceFragment.replace(new InsertHomework(), getFragmentManager());
                        }
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();

            } else {
                CommonDialogUtils.displayAlertWhiteDialog(act, "Not allowed to edit homework");
            }
        }
    };

    private View.OnClickListener yesterdayHomework = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Bundle b = new Bundle();
            b.putInt("today", 0);
            b.putInt("yesterday", 1);
            b.putInt("otherday", 0);
            Fragment fragment = new HomeworkView();
            fragment.setArguments(b);
            getFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.animator.fade_in, R.animator.fade_out)
                    .replace(R.id.content_frame, fragment).addToBackStack(null).commit();
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
            getFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.animator.fade_in, R.animator.fade_out)
                    .replace(R.id.content_frame, fragment).addToBackStack(null).commit();
        }
    };

    private void populateList() {
        childList1.clear();
        List<Homework> listHW = HomeworkDao.selectHomework(sectionId, hwDate, sqliteDatabase);
        if (listHW.size() != 0) {
            newHw = false;
            extractHomework(listHW);
            loadHomework();
            if (block != 1) {
                loadSubjects();
            }
        } else {
            newHw = true;
            loadSubjects();
        }
    }

    private void loadSubjects() {
        for (int i = 0; i < subjectIdList.size(); i++) {
            if (!childList1.contains(subjectIdList.get(i))) {
                hwList.add(new HW(subjectIdList.get(i), subjectNameList.get(i), "-"));
            }
        }
    }

    private void loadHomework() {
        hwList.clear();
        int hwLoop = 0;
        for (Integer childList11 : childList1) {
            for (int loop = 0; loop < subjectIdList.size(); loop++) {
                if (childList11.equals(subjectIdList.get(loop))) {
                    hwList.add(new HW(subjectIdList.get(loop), subjectNameList.get(loop), hwMessage.get(hwLoop)));
                    break;
                }
            }
            hwLoop += 1;
        }
    }

    public void extractHomework(List<Homework> homeworkList) {
        hwMessage = new ArrayList<>();
        for (Homework hw : homeworkList) {
            hwId = hw.getHomeworkId();
            rawSubjectIDs = hw.getSubjectIDs();
            String splitBy = ",";
            String[] id = rawSubjectIDs.split(splitBy);
            for (String subjectId : id) {
                noOfHw += 1;
                childList1.add(Integer.parseInt(subjectId));
            }
            rawHomework = hw.getHomework();
            String splitBy2 = "#";
            String[] message = rawHomework.split(splitBy2);
            hwMessage.addAll(Arrays.asList(message));
        }
    }

    public void prepareHomework(int exclude) {
        StringBuilder sub = new StringBuilder();
        int subLoop = 0;
        for (Integer i : childList1) {
            if (subLoop != exclude) {
                sub.append(",").append(i);
            }
            subLoop += 1;
        }
        prepareSubjectIds = sub.substring(1, sub.length());
        StringBuilder mes = new StringBuilder();
        int mesLoop = 0;
        for (String m : hwMessage) {
            if (mesLoop != exclude) {
                mes.append("#").append(m);
            }
            mesLoop += 1;
        }
        prepareHomeworks = mes.substring(1, mes.length());
    }

    public void prepareUpdateHomework(int pos, String update) {
        StringBuilder sub = new StringBuilder();
        int idLoop = 0;
        for (Integer i : childList1) {
            if (idLoop != pos) {
                sub.append(",").append(i);
            } else {
                if (!update.equals("")) {
                    sub.append(",").append(i);
                }
            }
            idLoop += 1;
        }
        prepareSubjectIds = sub.substring(1, sub.length());
        StringBuilder mes = new StringBuilder();
        int mesLoop = 0;
        for (String m : hwMessage) {
            if (mesLoop != pos) {
                mes.append("#").append(m);
            } else {
                if (!update.equals("")) {
                    mes.append("#").append(update);
                }
            }
            mesLoop += 1;
        }
        prepareHomeworks = mes.substring(1, mes.length());
    }

    private String getToday() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date today = new Date();
        return dateFormat.format(today);
    }

}
