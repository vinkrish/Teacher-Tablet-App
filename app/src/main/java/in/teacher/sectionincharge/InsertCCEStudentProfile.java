package in.teacher.sectionincharge;

import in.teacher.activity.R;
import in.teacher.dao.CCEStudentProfileDao;
import in.teacher.dao.StudentsDao;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.CCEStudentProfile;
import in.teacher.sqlite.Students;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.CommonDialogUtils;
import in.teacher.util.ReplaceFragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * Created by vinkrish.
 * Don't expect comments explaining every piece of code, class and function names are self explanatory.
 */
public class InsertCCEStudentProfile extends Fragment {
    private Context context;
    private SQLiteDatabase sqliteDatabase;
    private int sectionId, classId, schoolId, term, tag, imageTag;
    private ArrayList<CCEStudentProfile> profileList = new ArrayList<>();
    private List<Students> studentsArray;
    private EditText totalDays;
    private int width1, width2, width3, width4, width5, width6;
    private RelativeLayout tableLayout;
    private TableLayout table;
    private Button submit;
    ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.enter_cce_student_profile, container, false);
        progressDialog = new ProgressDialog(getActivity());
        context = AppGlobal.getContext();
        sqliteDatabase = AppGlobal.getSqliteDatabase();
        tag = 0;

        tableLayout = (RelativeLayout) view.findViewById(R.id.table);

        Bundle b = getArguments();
        term = b.getInt("Term");

        Button insert = (Button) view.findViewById(R.id.insertUpdate);
        insert.setText("Insert");

        totalDays = (EditText) view.findViewById(R.id.today_days);
        submit = (Button) view.findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit.setEnabled(false);
                if (!totalDays.getText().toString().equals("")) {
                    new SubmitTask().execute();
                } else {
                    submit.setEnabled(true);
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
                width6 = view.findViewById(R.id.width6).getWidth();
                new OffLoadTask().execute();
            }
        });

        return view;
    }

    private void init(){
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
            cceItem.setHeight("");
            cceItem.setWeight("");
            cceItem.setVisionL("");
            cceItem.setVisionR("");
            cceItem.setTermRemark("");
            profileList.add(cceItem);
        }
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
                submit.setEnabled(true);
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

        progressDialog.dismiss();
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
        LinearLayout.LayoutParams p6 = new LinearLayout.LayoutParams(width6, LinearLayout.LayoutParams.MATCH_PARENT);
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ilp.gravity = Gravity.CENTER;

        View verticalBorder = new View(getActivity());
        verticalBorder.setBackgroundColor(ContextCompat.getColor(context, R.color.border));
        LinearLayout.LayoutParams vlp = new LinearLayout.LayoutParams(1, LinearLayout.LayoutParams.MATCH_PARENT);
        verticalBorder.setLayoutParams(vlp);

        TextView tv1 = new TextView(getActivity());
        tv1.setLayoutParams(p1);
        tv1.setText(cce.getRollNo() + "");
        tv1.setGravity(Gravity.CENTER);
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
        tv2.setPadding(10, 10, 0, 10);
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
        ed1.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
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
        ed2.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
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
        ed3.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
        horizontalLayout.addView(ed3);

        View verticalBorder6 = new View(getActivity());
        verticalBorder6.setBackgroundColor(ContextCompat.getColor(context, R.color.border));
        verticalBorder6.setLayoutParams(vlp);
        horizontalLayout.addView(verticalBorder6);

        LinearLayout imageView = new LinearLayout(getActivity());
        imageView.setLayoutParams(p6);
        imageView.setGravity(Gravity.CENTER);
        imageView.setOrientation(LinearLayout.HORIZONTAL);
        ImageView iv = new ImageView(getActivity());
        iv.setImageResource(R.drawable.add_box);
        iv.setLayoutParams(ilp);
        imageView.addView(iv);
        imageView.setTag(imageTag);
        imageTag++;
        imageView.setOnClickListener(plusRemark);
        horizontalLayout.addView(imageView);


        verticalLayout.addView(horizontalLayout);
        View horizontalBorder = new View(getActivity());
        horizontalBorder.setBackgroundColor(ContextCompat.getColor(context, R.color.border));
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
        horizontalBorder.setLayoutParams(hlp);
        verticalLayout.addView(horizontalBorder);

        tableRowForTable.addView(verticalLayout, params);

        return tableRowForTable;
    }

    private View.OnClickListener plusRemark = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int pos = (Integer) v.getTag();
            displayRemark(pos);
        }
    };

    public Dialog displayRemark(int position) {
        int pos = position;
        final Dialog dialog = new Dialog(getActivity(), R.style.DialogSlideAnim);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.cce_term_remark);

        TextView name = (TextView) dialog.findViewById(R.id.name);
        final TextView remark = (TextView) dialog.findViewById(R.id.remark);
        final TextView rightVision = (TextView) dialog.findViewById(R.id.r_vision);
        final TextView leftVision = (TextView) dialog.findViewById(R.id.l_vision);

        final CCEStudentProfile cp = profileList.get(pos);
        name.setText(cp.getStudentName());
        remark.setText(cp.getTermRemark());
        leftVision.setText(cp.getVisionL());
        rightVision.setText(cp.getVisionR());

        dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cp.setTermRemark(remark.getText().toString().replaceAll("\n", " ").replaceAll("\"", "'"));
                cp.setVisionL(leftVision.getText().toString().replaceAll("\n", " ").replaceAll("\"", "'"));
                cp.setVisionR(rightVision.getText().toString().replaceAll("\n"," ").replaceAll("\"", "'"));
                dialog.dismiss();
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
                    e.printStackTrace();
                }
                c.setDaysAttended1(d);
            }
            profileList.set(index, c);
        }
    }

    class OffLoadTask extends AsyncTask<Void, Void, Void> {

        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Loading Data...");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            init();
            return null;
        }

        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            generateTable();
        }
    }

}