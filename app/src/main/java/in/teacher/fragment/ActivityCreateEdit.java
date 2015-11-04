package in.teacher.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import in.teacher.activity.R;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.CommonDialogUtils;
import in.teacher.util.NestedListView;
import in.teacher.util.PKGenerator;
import in.teacher.util.ReplaceFragment;

/**
 * Created by vinkrish on 28/10/15.
 */
public class ActivityCreateEdit extends Fragment {
    private Context context;
    private SQLiteDatabase sqliteDatabase;
    private Spinner classSpinner, sectionSpinner, examSpinner, subjectSpinner;
    private int teacherId, classId, sectionId, examId, subjectId, activityPos, schoolId, generatedId, lastSubjectPos;
    final List<Integer> examIdList = new ArrayList<>();
    List<String> examNameList = new ArrayList<>();
    final List<Integer> sectionIdList = new ArrayList<>();
    List<String> sectionNameList = new ArrayList<>();
    final List<Integer> subjectIdList = new ArrayList<>();
    List<String> subjectNameList = new ArrayList<>();
    private Button createActivity;
    private ListView listView;
    private ActivityAdapter activityAdapter;
    private List<ActivityItem> activityItemList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.act_create_edit, container, false);
        context = AppGlobal.getContext();
        sqliteDatabase = AppGlobal.getSqliteDatabase();

        initView(view);

        activityAdapter = new ActivityAdapter(context, activityItemList);
        listView.setAdapter(activityAdapter);

        createActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (createActivity.isActivated())
                    activityCreateDialog();
                else
                    CommonDialogUtils.displayAlertWhiteDialog(getActivity(), "Please select exam and subject");
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                activityPos = position;
                activityUpdateDialog();
            }
        });

        return view;
    }

    private void initView(View view) {
        createActivity = (Button) view.findViewById(R.id.create_act);
        listView = (ListView) view.findViewById(R.id.listView);

        classSpinner = (Spinner) view.findViewById(R.id.classSpinner);
        sectionSpinner = (Spinner) view.findViewById(R.id.sectionSpinner);
        examSpinner = (Spinner) view.findViewById(R.id.examSpinner);
        subjectSpinner = (Spinner) view.findViewById(R.id.subjectSpinner);

        initClassSpinner();
    }

    private void initClassSpinner() {
        Temp t = TempDao.selectTemp(sqliteDatabase);
        teacherId = t.getTeacherId();
        schoolId = t.getSchoolId();
        int classInchargeId = t.getClassInchargeId();

        int classInChargePos = 0;

        final List<Integer> classInchargeList = new ArrayList<>();
        List<String> classNameIncharge = new ArrayList<>();

        Cursor c = sqliteDatabase.rawQuery("select A.ClassId, B.ClassName from classteacher_incharge A, class B " +
                "where A.TeacherId = " + teacherId + " and B.ClassId = A.ClassId", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            classInchargeList.add(c.getInt(c.getColumnIndex("ClassId")));
            classNameIncharge.add(c.getString(c.getColumnIndex("ClassName")));
            c.moveToNext();
        }
        c.close();

        for (int i = 0; i < classInchargeList.size(); i++) {
            if (classInchargeList.get(i) == classInchargeId) {
                classInChargePos = i;
                break;
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_header, classNameIncharge);
        adapter.setDropDownViewResource(R.layout.spinner_droppeddown);
        classSpinner.setAdapter(adapter);

        final ArrayAdapter<String> examAdapter = new ArrayAdapter<>(context, R.layout.spinner_header, examNameList);
        examAdapter.setDropDownViewResource(R.layout.spinner_droppeddown);
        examSpinner.setAdapter(examAdapter);

        final ArrayAdapter<String> sectionAdapter = new ArrayAdapter<>(context, R.layout.spinner_header, sectionNameList);
        sectionAdapter.setDropDownViewResource(R.layout.spinner_droppeddown);
        sectionSpinner.setAdapter(sectionAdapter);

        final ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(context, R.layout.spinner_header, subjectNameList);
        subjectAdapter.setDropDownViewResource(R.layout.spinner_droppeddown);
        subjectSpinner.setAdapter(subjectAdapter);

        classSpinner.setSelection(classInChargePos);

        classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                classId = classInchargeList.get(position);
                initExamSpinner();
                initSectionSpinner();
                examAdapter.notifyDataSetChanged();
                sectionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        examSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) examId = examIdList.get(position);
                else examId = 0;

                initSubjectSpinner();
                subjectAdapter.notifyDataSetChanged();
                subjectSpinner.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sectionId = sectionIdList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    lastSubjectPos = position;
                    subjectId = subjectIdList.get(position);
                    createActivity.setActivated(true);
                    resetActivityList();
                    activityAdapter.notifyDataSetChanged();
                    //setListViewHeightBasedOnChildren(listView);
                } else {
                    createActivity.setActivated(false);
                    activityItemList.clear();
                    activityAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void initSectionSpinner() {
        Cursor c = sqliteDatabase.rawQuery("select SectionId, SectionName from section where ClassId = " + classId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            sectionIdList.add(c.getInt(c.getColumnIndex("SectionId")));
            sectionNameList.add(c.getString(c.getColumnIndex("SectionName")));
            c.moveToNext();
        }
        c.close();
    }

    private void initExamSpinner() {
        examIdList.clear();
        examNameList.clear();
        examIdList.add(0);
        examNameList.add("Select Exam");
        Cursor c = sqliteDatabase.rawQuery("select ExamId, ExamName from exams where ClassId = " + classId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            examIdList.add(c.getInt(c.getColumnIndex("ExamId")));
            examNameList.add(c.getString(c.getColumnIndex("ExamName")));
            c.moveToNext();
        }
        c.close();
    }

    private void initSubjectSpinner() {
        subjectIdList.clear();
        subjectNameList.clear();
        subjectIdList.add(0);
        subjectNameList.add("Select Subject");
        Cursor c = sqliteDatabase.rawQuery("select A.SubjectId, B.SubjectName from subjectexams A, subjects B where A.ClassId = " +
                classId + "  and A.ExamId = " + examId + " and A.SubjectId = B.SubjectId", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            subjectIdList.add(c.getInt(c.getColumnIndex("SubjectId")));
            subjectNameList.add(c.getString(c.getColumnIndex("SubjectName")));
            c.moveToNext();
        }
        c.close();
    }

    private void resetActivityList() {

        activityItemList.clear();
        Cursor c = sqliteDatabase.rawQuery("select ActivityId, ActivityName, MaximumMark, Weightage, Calculation from activity where " +
                "SectionId = " + sectionId + " and ExamId = " + examId + " and SubjectId = " + subjectId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            ActivityItem ai = new ActivityItem();
            ai.setActId(c.getInt(c.getColumnIndex("ActivityId")));
            ai.setActName(c.getString(c.getColumnIndex("ActivityName")));
            ai.setMaxMark(c.getInt(c.getColumnIndex("MaximumMark")));
            int calculation = c.getInt(c.getColumnIndex("Calculation"));
            ai.setCalculation(calculation);
            if (calculation == 0) {
                ai.setWeightage(c.getInt(c.getColumnIndex("Weightage")) + "");
            } else {
                ai.setWeightage(" - ");
            }
            activityItemList.add(ai);
            c.moveToNext();
        }
        c.close();
    }

    public class ActivityAdapter extends ArrayAdapter<ActivityItem> {
        private List<ActivityItem> data = new ArrayList<>();
        private LayoutInflater inflater;

        public ActivityAdapter(Context context, List<ActivityItem> objects) {
            super(context, R.layout.activity_list, objects);
            this.data = objects;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            RecordHolder holder;

            if (row == null) {
                row = inflater.inflate(R.layout.activity_list, parent, false);
                holder = new RecordHolder();
                holder.text1 = (TextView) row.findViewById(R.id.text1);
                holder.text2 = (TextView) row.findViewById(R.id.text2);
                holder.text3 = (TextView) row.findViewById(R.id.text3);
                row.setTag(holder);
            } else holder = (RecordHolder) row.getTag();

            if (position % 2 == 0)
                row.setBackgroundResource(R.drawable.list_selector1);
            else
                row.setBackgroundResource(R.drawable.list_selector2);

            ActivityItem listItem = data.get(position);
            holder.text1.setText(listItem.getActName());
            holder.text2.setText(listItem.getMaxMark() + "");
            holder.text3.setText(listItem.getWeightage());

            return row;
        }

        public class RecordHolder {
            public TextView text1;
            public TextView text2;
            public TextView text3;
        }

    }

    class ActivityItem {
        private int actId;
        private String actName;
        private int maxMark;
        private String weightage;
        private int calculation;

        public int getCalculation() {
            return calculation;
        }

        public void setCalculation(int calculation) {
            this.calculation = calculation;
        }

        public int getActId() {
            return actId;
        }

        public void setActId(int actId) {
            this.actId = actId;
        }

        public String getActName() {
            return actName;
        }

        public void setActName(String actName) {
            this.actName = actName;
        }

        public int getMaxMark() {
            return maxMark;
        }

        public void setMaxMark(int maxMark) {
            this.maxMark = maxMark;
        }

        public String getWeightage() {
            return weightage;
        }

        public void setWeightage(String weightage) {
            this.weightage = weightage;
        }
    }

    private Dialog activityCreateDialog() {
        final Dialog dialog = new Dialog(getActivity(), R.style.DialogSlideAnim);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_create_dialog);
        dialog.setCancelable(false);

        RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.radio_group);
        final TextInputLayout hideLayout = (TextInputLayout) dialog.findViewById(R.id.hided);

        final EditText activityName = (EditText) dialog.findViewById(R.id.activity_name);
        final EditText maxMark = (EditText) dialog.findViewById(R.id.max_mark);
        final EditText weightage = (EditText) dialog.findViewById(R.id.weightage);

        final RadioButton sum = (RadioButton) dialog.findViewById(R.id.sum);
        final RadioButton avg = (RadioButton) dialog.findViewById(R.id.average);
        final RadioButton best = (RadioButton) dialog.findViewById(R.id.best);

        final Spinner bestOf = (Spinner) dialog.findViewById(R.id.bestof);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_header,
                Arrays.asList(new String[]{"1", "2", "3", "4", "5"}));
        adapter.setDropDownViewResource(R.layout.spinner_droppeddown);
        bestOf.setAdapter(adapter);

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.sum:
                        hideLayout.setVisibility(View.GONE);
                        break;
                    case R.id.average:
                        hideLayout.setVisibility(View.VISIBLE);
                        break;
                    case R.id.best:
                        hideLayout.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }
            }
        });

        dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sql = "";
                try {
                    generatedId = PKGenerator.getMD5(schoolId, sectionId, activityName.getText().toString());
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

                if (sum.isChecked()){
                    if (!activityName.getText().toString().equals("") && !maxMark.getText().toString().equals("")) {
                        sql = "insert into activity (ActivityId, SchoolId, ClassId, SectionId, ExamId, " +
                                "SubjectId, RubrixId, ActivityName, MaximumMark, Weightage, Calculation) " +
                                " values ("+generatedId+", "+schoolId+", "+classId+", "+sectionId+", "+examId+
                                ", "+subjectId+", 0,\""+activityName.getText().toString().replaceAll("\n", " ")+"\","+maxMark.getText().toString()+
                                ", 0, -1)";
                    } else {
                        CommonDialogUtils.displayAlertWhiteDialog(getActivity(), "No fields should be left blank");
                    }
                } else if (avg.isChecked()) {
                    if (!activityName.getText().toString().equals("") &&
                            !maxMark.getText().toString().equals("") &&
                            !weightage.getText().toString().equals("")) {
                        sql = "insert into activity (ActivityId, SchoolId, ClassId, SectionId, ExamId, " +
                                "SubjectId, RubrixId, ActivityName, MaximumMark, Weightage, Calculation) " +
                                " values ("+generatedId+", "+schoolId+", "+classId+", "+sectionId+", "+examId+
                                ", "+subjectId+", 0,\""+activityName.getText().toString().replaceAll("\n", " ")+"\","+maxMark.getText().toString()+
                                ", "+weightage.getText().toString()+", 0)";
                    } else {
                        CommonDialogUtils.displayAlertWhiteDialog(getActivity(), "No fields should be left blank");
                    }
                } else if (best.isChecked()) {
                    if (!activityName.getText().toString().equals("") && !maxMark.getText().toString().equals("")) {
                        sql = "insert into activity (ActivityId, SchoolId, ClassId, SectionId, ExamId, " +
                                "SubjectId, RubrixId, ActivityName, MaximumMark, Weightage, Calculation) " +
                                " values ("+generatedId+", "+schoolId+", "+classId+", "+sectionId+", "+examId+
                                ", "+subjectId+", 0,\""+activityName.getText().toString().replaceAll("\n", " ")+"\","+maxMark.getText().toString()+
                                ", 0, "+(bestOf.getSelectedItemPosition()+1)+")";
                    } else {
                        CommonDialogUtils.displayAlertWhiteDialog(getActivity(), "No fields should be left blank");
                    }
                }

                try {
                    sqliteDatabase.execSQL(sql);
                    ContentValues cv = new ContentValues();
                    cv.put("Query", sql);
                    sqliteDatabase.insert("uploadsql", null, cv);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                dialog.dismiss();
                subjectSpinner.setSelection(0);
            }
        });

        dialog.getWindow().setGravity(Gravity.TOP);
        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        layoutParams.y = 80;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(layoutParams);

        dialog.show();

        /*WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.y = 80;
        Window window = dialog.getWindow();
        lp.copyFrom(window.getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);*/

        return dialog;
    }

    private Dialog activityUpdateDialog() {
        final Dialog dialog = new Dialog(getActivity(), R.style.DialogSlideAnim);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_edit_dialog);
        dialog.setCancelable(false);

        RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.radio_group);
        final TextInputLayout hideLayout = (TextInputLayout) dialog.findViewById(R.id.hided);

        final EditText activityName = (EditText) dialog.findViewById(R.id.activity_name);
        final EditText maxMark = (EditText) dialog.findViewById(R.id.max_mark);
        final EditText weightage = (EditText) dialog.findViewById(R.id.weightage);

        final RadioButton sum = (RadioButton) dialog.findViewById(R.id.sum);
        final RadioButton avg = (RadioButton) dialog.findViewById(R.id.average);
        final RadioButton best = (RadioButton) dialog.findViewById(R.id.best);

        final Spinner bestOf = (Spinner) dialog.findViewById(R.id.bestof);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_header,
                Arrays.asList(new String[]{"1", "2", "3", "4", "5"}));
        adapter.setDropDownViewResource(R.layout.spinner_droppeddown);
        bestOf.setAdapter(adapter);

        final ActivityItem ai = activityItemList.get(activityPos);

        if (ai.getCalculation() == -1){
            activityName.setText(ai.getActName());
            maxMark.setText(ai.getMaxMark() + "");
            sum.setChecked(true);
            hideLayout.setVisibility(View.GONE);
        } else if (ai.getCalculation() == 0) {
            activityName.setText(ai.getActName());
            maxMark.setText(ai.getMaxMark() + "");
            weightage.setText(ai.getWeightage() + "");
            avg.setChecked(true);
            hideLayout.setVisibility(View.VISIBLE);
        } else {
            activityName.setText(ai.getActName());
            maxMark.setText(ai.getMaxMark() + "");
            best.setChecked(true);
            bestOf.setSelection(ai.getCalculation() - 1);
            hideLayout.setVisibility(View.GONE);
        }

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.sum:
                        hideLayout.setVisibility(View.GONE);
                        break;
                    case R.id.average:
                        hideLayout.setVisibility(View.VISIBLE);
                        break;
                    case R.id.best:
                        hideLayout.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }
            }
        });

        dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                confirmDelete(ai.actId);
            }
        });

        dialog.findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sql = "";

                if (sum.isChecked()){
                    if (!activityName.getText().toString().equals("") && !maxMark.getText().toString().equals("")) {
                        sql = "update activity set ActivityName = \""+activityName.getText().toString().replaceAll("\n", " ")+
                                "\", MaximumMark = " + maxMark.getText().toString() + ", Calculation = -1 " +
                                "where ActivityId = " + ai.getActId();
                    } else {
                        CommonDialogUtils.displayAlertWhiteDialog(getActivity(), "No fields should be left blank");
                    }
                } else if (avg.isChecked()) {
                    if (!activityName.getText().toString().equals("") &&
                            !maxMark.getText().toString().equals("") &&
                            !weightage.getText().toString().equals("")) {
                        sql = "update activity set ActivityName = \""+activityName.getText().toString().replaceAll("\n", " ")+
                                "\", MaximumMark = " + maxMark.getText().toString() + ", Weightage = "+weightage.getText().toString()+" , Calculation = 0 " +
                                "where ActivityId = " + ai.getActId();
                    } else {
                        CommonDialogUtils.displayAlertWhiteDialog(getActivity(), "No fields should be left blank");
                    }
                } else if (best.isChecked()) {
                    if (!activityName.getText().toString().equals("") && !maxMark.getText().toString().equals("")) {
                        sql = "update activity set ActivityName = \""+activityName.getText().toString().replaceAll("\n", " ")+
                                "\", MaximumMark = " + maxMark.getText().toString() + ", Calculation = " + (bestOf.getSelectedItemPosition()+1) +
                                " where ActivityId = " + ai.getActId();
                    } else {
                        CommonDialogUtils.displayAlertWhiteDialog(getActivity(), "No fields should be left blank");
                    }
                }

                try {
                    sqliteDatabase.execSQL(sql);
                    ContentValues cv = new ContentValues();
                    cv.put("Query", sql);
                    sqliteDatabase.insert("uploadsql", null, cv);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                dialog.dismiss();

                subjectSpinner.setSelection(0);
                //subjectSpinner.setSelection(lastSubjectPos);
                //ReplaceFragment.replace(new ActivityCreateEdit(), getFragmentManager());
            }
        });

        dialog.getWindow().setGravity(Gravity.TOP);
        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        layoutParams.y = 80;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(layoutParams);
        dialog.show();

        return dialog;
    }

    private void confirmDelete (final int activityId) {
        AlertDialog.Builder submitBuilder = new AlertDialog.Builder(getActivity());
        submitBuilder.setCancelable(false);
        submitBuilder.setTitle("Confirm your action");
        submitBuilder.setMessage("Do you want to delete activity ?");
        submitBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                dialog.cancel();
            }
        });
        submitBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                String sql = "delete from activity where ActivityId = " + activityId;
                try {
                    sqliteDatabase.execSQL(sql);
                    ContentValues cv = new ContentValues();
                    cv.put("Query", sql);
                    sqliteDatabase.insert("uploadsql", null, cv);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                dialog.cancel();
                subjectSpinner.setSelection(0);
            }
        });
        submitBuilder.show();
    }

}
