package in.teacher.sectionincharge;

import in.teacher.activity.R;
import in.teacher.dao.CCEStudentProfileDao;
import in.teacher.dao.StudentsDao;
import in.teacher.dao.SubjectsDao;
import in.teacher.dao.TempDao;
import in.teacher.model.Profile;
import in.teacher.sqlite.CCEStudentProfile;
import in.teacher.sqlite.Students;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.CommonDialogUtils;
import in.teacher.util.ReplaceFragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * Created by vinkrish.
 */
public class InsertCCEStudentProfile extends Fragment {
    private Context context;
    private SQLiteDatabase sqliteDatabase;
    private int sectionId, classId, schoolId, term, tag;
    private ArrayList<CCEStudentProfile> profileList = new ArrayList<>();
    private List<Students> studentsArray;
    private EditText totalDays;
    private int width1, width2, width3, width4, width5;
    private RelativeLayout tableLayout;
    private TableLayout table;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.enter_cce_student_profile, container, false);
        context = AppGlobal.getContext();
        sqliteDatabase = AppGlobal.getSqliteDatabase();
        tag = 0;

        tableLayout = (RelativeLayout) view.findViewById(R.id.table);

        Bundle b = getArguments();
        term = b.getInt("Term");

        Button insert = (Button) view.findViewById(R.id.insertUpdate);
        insert.setText("Insert");

        totalDays = (EditText) view.findViewById(R.id.today_days);
        Button submit = (Button) view.findViewById(R.id.submit);

        Temp t = TempDao.selectTemp(sqliteDatabase);
        sectionId = t.getSectionId();
        classId = t.getClassId();
        schoolId = t.getSchoolId();

        studentsArray = StudentsDao.selectStudents(sectionId, sqliteDatabase);
        for (Students stud : studentsArray) {
            CCEStudentProfile cceItem = new CCEStudentProfile();
            cceItem.setSchoolId(schoolId+"");
            cceItem.setClassId(classId + "");
            cceItem.setSectionId(sectionId + "");
            cceItem.setTerm(term);
            cceItem.setStudentId(stud.getStudentId() + "");
            cceItem.setRollNo(stud.getRollNoInClass());
            cceItem.setStudentName(stud.getName());
            profileList.add(cceItem);
        }

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!totalDays.getText().toString().equals("")) {
                    new SubmitTask().execute();
                } else {
                    CommonDialogUtils.displayAlertWhiteDialog(getActivity(), "Please enter total number of days");
                }
            }
        });

        Button cceProfile = (Button) view.findViewById(R.id.cce_profile);
        cceProfile.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ReplaceFragment.replace(new SelectCCEStudentProfile(), getFragmentManager());
            }
        });

        table = new TableLayout(getActivity());

        view.post(new Runnable() {
            @Override
            public void run() {
                width1 = view.findViewById(R.id.width1).getWidth();
                width2 = view.findViewById(R.id.width2).getWidth();
                width3 = view.findViewById(R.id.width3).getWidth();
                width4 = view.findViewById(R.id.width4).getWidth();
                width5 = view.findViewById(R.id.width5).getWidth();
                generateTable();
            }
        });

        return view;
    }

    class SubmitTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog pDialog = new ProgressDialog(getActivity());
        boolean validate;
        Double totDays = 0.0;

        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Submitting Student Profile...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
            try {
                totDays = Double.parseDouble(totalDays.getText().toString());
            } catch (NumberFormatException e) {
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            validate = validateDate();
            if (validate)
                CCEStudentProfileDao.insertCCEStudentProfile(totDays, profileList, sqliteDatabase);

            return null;
        }

        protected void onPostExecute(Void v){
            super.onPostExecute(v);
            pDialog.dismiss();

            if (validate) {
                ReplaceFragment.replace(new SelectCCEStudentProfile(), getFragmentManager());
            }else {
                CommonDialogUtils.displayAlertWhiteDialog(getActivity(), "Days attended for one or more students is more than Total Days!");
            }

        }
    }

    private boolean validateDate() {
        boolean flag = true;
        for (CCEStudentProfile p : profileList) {
            try {
                if (Integer.parseInt(totalDays.getText().toString()) < p.getDaysAttended1()) {
                    flag = false;
                }
            } catch (NumberFormatException e) {
            }
        }
        return flag;
    }

    private void generateTable() {
        table.removeAllViews();
        for (CCEStudentProfile cce : profileList) {
            TableRow tableRow = generateRow(cce);
            table.addView(tableRow);
        }
        tableLayout.addView(table);
    }

    private TableRow generateRow(CCEStudentProfile cce) {

        TableRow tableRowForTable = new TableRow(this.context);
        TableRow.LayoutParams params = new TableRow.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        LinearLayout verticalLayout = new LinearLayout(getActivity());
        verticalLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout horizontalLayout = new LinearLayout(getActivity());
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(width1, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(width2, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams p3 = new LinearLayout.LayoutParams(width3, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams p4 = new LinearLayout.LayoutParams(width4, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams p5 = new LinearLayout.LayoutParams(width5, LinearLayout.LayoutParams.WRAP_CONTENT);

        View verticalBorder = new View(getActivity());
        verticalBorder.setBackgroundColor(ContextCompat.getColor(context, R.color.border));
        LinearLayout.LayoutParams vlp = new LinearLayout.LayoutParams(1, LinearLayout.LayoutParams.MATCH_PARENT);
        verticalBorder.setLayoutParams(vlp);

        TextView tv1 = new TextView(getActivity());
        tv1.setLayoutParams(p1);
        tv1.setText(cce.getRollNo() + "");
        tv1.setPadding(20, 10, 0, 10);
        tv1.setTextSize(18);
        //tv1.setTextColor(getResources().getColor(R.color.dark_black));
        tv1.setTextColor(ContextCompat.getColor(context, R.color.dark_black));
        horizontalLayout.addView(tv1);

        View verticalBorder2 = new View(getActivity());
        verticalBorder2.setBackgroundColor(ContextCompat.getColor(context, R.color.border));
        verticalBorder2.setLayoutParams(vlp);
        horizontalLayout.addView(verticalBorder2);

        TextView tv2 = new TextView(getActivity());
        tv2.setLayoutParams(p2);
        tv2.setText(cce.getStudentName());
        tv2.setPadding(20, 10, 0, 10);
        tv2.setTextSize(18);
        tv2.setTextColor(ContextCompat.getColor(context, R.color.dark_black));
        horizontalLayout.addView(tv2);

        View verticalBorder3 = new View(getActivity());
        verticalBorder3.setBackgroundColor(ContextCompat.getColor(context, R.color.border));
        verticalBorder3.setLayoutParams(vlp);
        horizontalLayout.addView(verticalBorder3);

        EditText ed1 = new EditText(getActivity());
        ed1.setTag(tag);
        tag++;
        ed1.setLayoutParams(p3);
        ed1.setText("");
        ed1.setGravity(Gravity.CENTER);
        ed1.setInputType(InputType.TYPE_CLASS_NUMBER);
        ed1.addTextChangedListener(new MarksTextWatcher(ed1));
        horizontalLayout.addView(ed1);

        View verticalBorder4 = new View(getActivity());
        verticalBorder4.setBackgroundColor(ContextCompat.getColor(context, R.color.border));
        verticalBorder4.setLayoutParams(vlp);
        horizontalLayout.addView(verticalBorder4);

        EditText ed2 = new EditText(getActivity());
        ed2.setTag(tag);
        tag++;
        ed2.setLayoutParams(p4);
        ed2.setText("");
        ed2.setGravity(Gravity.CENTER);
        ed2.setInputType(InputType.TYPE_CLASS_NUMBER);
        ed2.addTextChangedListener(new MarksTextWatcher(ed2));
        horizontalLayout.addView(ed2);

        View verticalBorder5 = new View(getActivity());
        verticalBorder5.setBackgroundColor(ContextCompat.getColor(context, R.color.border));
        verticalBorder5.setLayoutParams(vlp);
        horizontalLayout.addView(verticalBorder5);

        EditText ed3 = new EditText(getActivity());
        ed3.setTag(tag);
        tag++;
        ed3.setLayoutParams(p5);
        ed3.setText("");
        ed3.setGravity(Gravity.CENTER);
        ed3.setInputType(InputType.TYPE_CLASS_NUMBER);
        ed3.addTextChangedListener(new MarksTextWatcher(ed3));
        horizontalLayout.addView(ed3);

        verticalLayout.addView(horizontalLayout);
        View horizontalBorder = new View(getActivity());
        horizontalBorder.setBackgroundColor(ContextCompat.getColor(context, R.color.border));
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
        horizontalBorder.setLayoutParams(hlp);
        verticalLayout.addView(horizontalBorder);

        tableRowForTable.addView(verticalLayout, params);

        return tableRowForTable;
    }

    private class MarksTextWatcher implements TextWatcher {

        private int pos;
        private int index;
        private View view;

        private MarksTextWatcher(View view) {
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            pos = (Integer) view.getTag();
            index = pos / 3;
            CCEStudentProfile c = profileList.get(index);

            if (pos % 3 == 0) {
                c.setHeight(s.toString());
            } else if (pos % 3 == 1) {
                c.setWeight(s.toString());
            } else {
                double d = 0;
                try {
                    d = Double.parseDouble(s.toString());
                } catch (NumberFormatException e) {
                }
                c.setDaysAttended1(d);
            }
            profileList.set(index, c);
        }
    }

}