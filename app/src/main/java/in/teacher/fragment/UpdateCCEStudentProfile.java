package in.teacher.fragment;

import in.teacher.activity.R;
import in.teacher.adapter.Capitalize;
import in.teacher.dao.CCEStudentProfileDao;
import in.teacher.dao.StudentsDao;
import in.teacher.dao.TeacherDao;
import in.teacher.dao.TempDao;
import in.teacher.model.Profile;
import in.teacher.sqlite.CCEStudentProfile;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.CommonDialogUtils;
import in.teacher.util.ReplaceFragment;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

@SuppressLint("InflateParams")
public class UpdateCCEStudentProfile extends Fragment {
    private Activity act;
    private Context context;
    private SQLiteDatabase sqliteDatabase;
    private int sectionId, classId, schoolId, term, totalDay;
    private ProfileAdapter profileAdapter;
    private List<Integer> studentsRoll;
    private ArrayList<Profile> profileList = new ArrayList<>();
    private ListView lv;
    private EditText totalDays;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.enter_cce_student_profile, container, false);
        act = AppGlobal.getActivity();
        context = AppGlobal.getContext();
        sqliteDatabase = AppGlobal.getSqliteDatabase();

        Bundle b = getArguments();
        term = b.getInt("Term");

        Button insert = (Button) view.findViewById(R.id.insertUpdate);
        insert.setText("Update");

        totalDays = (EditText) view.findViewById(R.id.today_days);
        Button submit = (Button) view.findViewById(R.id.submit);

        Temp t = TempDao.selectTemp(sqliteDatabase);
        sectionId = t.getSectionId();
        classId = t.getClassId();
        schoolId = t.getSchoolId();

        lv = (ListView) view.findViewById(R.id.list);
        studentsRoll = StudentsDao.selectStudentIds("" + sectionId, sqliteDatabase);

        Cursor c = sqliteDatabase.rawQuery("select TotalDays1, Height, Weight, DaysAttended1, StudentId, StudentName from ccestudentprofile where Term=" + term + " and StudentId in " +
                "(select StudentId from students where SectionId=" + sectionId + " order by RollNoInClass)", null);
        c.moveToFirst();
        int loop = 0;
        while (!c.isAfterLast()) {
            totalDay = c.getInt(c.getColumnIndex("TotalDays1"));
            int cid = c.getInt(c.getColumnIndex("StudentId"));
            String cname = c.getString(c.getColumnIndex("StudentName"));
            String cheight = c.getString(c.getColumnIndex("Height"));
            String cweight = c.getString(c.getColumnIndex("Weight"));
            double cdays = c.getDouble(c.getColumnIndex("DaysAttended1"));
            profileList.add(new Profile(cid, studentsRoll.get(loop) + "", cname, cheight, cweight, cdays + ""));
            c.moveToNext();
            loop += 1;
        }
        c.close();

        totalDays.setText(totalDay + "");

        profileAdapter = new ProfileAdapter(context, R.layout.profile_adapter, profileList);
        lv.setAdapter(profileAdapter);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!totalDays.getText().toString().equals("")) {
                    if (validateDate()) {
                        List<CCEStudentProfile> cspList = new ArrayList<>();
                        for (Profile p : profileList) {
                            CCEStudentProfile csp = new CCEStudentProfile();
                            csp.setSchoolId(schoolId + "");
                            csp.setClassId(classId + "");
                            csp.setSectionId(sectionId + "");
                            csp.setStudentId(p.getStudentId() + "");
                            csp.setStudentName(p.getName());
                            csp.setHeight(p.getHeight());
                            csp.setWeight(p.getWeight());
                            try {
                                csp.setDaysAttended1(Double.parseDouble(p.getDaysAttended()));
                            } catch (NumberFormatException e) {
                                csp.setDaysAttended1(0);
                            }
                            csp.setTotalDays1(Double.parseDouble(totalDays.getText().toString()));
                            csp.setTerm(term);
                            cspList.add(csp);
                        }
                        CCEStudentProfileDao.updateCCEStudentProfile(cspList, sqliteDatabase);
                        ReplaceFragment.replace(new SelectCCEStudentProfile(), getFragmentManager());
                    } else {
                        CommonDialogUtils.displayAlertWhiteDialog(act, "Days attended for one or more students is more than Total Days!");
                    }
                } else {
                    CommonDialogUtils.displayAlertWhiteDialog(act, "Please enter valid total number of days");
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

        return view;
    }

    private boolean validateDate() {
        boolean flag = true;
        for (Profile p : profileList) {
            try {
                if (Integer.parseInt(totalDays.getText().toString()) < Double.parseDouble(p.getDaysAttended())) {
                    flag = false;
                }
            } catch (NumberFormatException e) {
            }
        }
        return flag;
    }

    public class ProfileAdapter extends ArrayAdapter<Profile> {
        int resource;
        Context context;
        ArrayList<Profile> data = new ArrayList<>();
        LayoutInflater inflater = null;

        public ProfileAdapter(Context context, int resource, ArrayList<Profile> listArray) {
            super(context, resource, listArray);
            this.context = context;
            this.resource = resource;
            this.data = listArray;
            inflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            RecordHolder holder;

            if (row == null) {
                row = inflater.inflate(resource, parent, false);
                holder = new RecordHolder();
                holder.tv1 = (TextView) row.findViewById(R.id.roll);
                holder.tv2 = (TextView) row.findViewById(R.id.name);
                holder.tv3 = (TextView) row.findViewById(R.id.height);
                holder.tv4 = (TextView) row.findViewById(R.id.weight);
                holder.tv5 = (TextView) row.findViewById(R.id.days_attended);
                row.setTag(holder);
            } else {
                holder = (RecordHolder) row.getTag();
            }

            if (position % 2 == 0) {
                row.setBackgroundResource(R.drawable.list_selector1);
            } else {
                row.setBackgroundResource(R.drawable.list_selector2);
            }

            Profile listItem = data.get(position);
            holder.tv1.setText(listItem.getRoll());
            holder.tv2.setText(listItem.getName());
            holder.tv3.setText(listItem.getHeight());
            holder.tv4.setText(listItem.getWeight());
            if (!listItem.getDaysAttended().equals("0.0")) {
                holder.tv5.setText(listItem.getDaysAttended());
            } else {
                holder.tv5.setText("");
            }

            holder.tv3.setOnClickListener(heightClickListener);
            holder.tv4.setOnClickListener(weightClickListener);
            holder.tv5.setOnClickListener(daysClickListener);

            return row;
        }

        class RecordHolder {
            TextView tv1;
            TextView tv2;
            TextView tv3;
            TextView tv4;
            TextView tv5;
        }
    }

    private OnClickListener heightClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            ListView mListView = (ListView) v.getParent().getParent();
            final int position = mListView.getPositionForView((View) v.getParent());
            final Profile p = profileList.get(position);

            AlertDialog.Builder builder = new AlertDialog.Builder(act);
            View view = act.getLayoutInflater().inflate(R.layout.profile_dialog, null);
            TextView h = (TextView) view.findViewById(R.id.name_profile);
            h.setText(p.getName() + " - [Height]");
            final EditText edListChild = (EditText) view.findViewById(R.id.value);
            edListChild.setText(p.getHeight());
            edListChild.setSelection(edListChild.length());

            builder.setView(view);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (edListChild.getText().toString().equals("")) {
                    } else {
                        Profile prof = new Profile(p.getStudentId(), studentsRoll.get(position) + "", p.getName(),
                                edListChild.getText().toString().replaceAll("\n", " "), profileList.get(position).getWeight(), profileList.get(position).getDaysAttended());
                        profileList.set(position, prof);
                        profileAdapter.notifyDataSetChanged();
                    }
                }
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        }
    };

    private OnClickListener weightClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            ListView mListView = (ListView) v.getParent().getParent();
            final int position = mListView.getPositionForView((View) v.getParent());
            final Profile p = profileList.get(position);

            AlertDialog.Builder builder = new AlertDialog.Builder(act);
            View view = act.getLayoutInflater().inflate(R.layout.profile_dialog, null);
            TextView w = (TextView) view.findViewById(R.id.name_profile);
            w.setText(p.getName() + " - [Weight]");
            final EditText edListChild = (EditText) view.findViewById(R.id.value);
            edListChild.setText(p.getWeight());
            edListChild.setSelection(edListChild.length());

            builder.setView(view);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (edListChild.getText().toString().equals("")) {
                    } else {
                        Profile prof = new Profile(p.getStudentId(), studentsRoll.get(position) + "", p.getName(),
                                profileList.get(position).getHeight(), edListChild.getText().toString().replaceAll("\n", " "), profileList.get(position).getDaysAttended());
                        profileList.set(position, prof);
                        profileAdapter.notifyDataSetChanged();
                    }
                }
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        }
    };

    private OnClickListener daysClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            ListView mListView = (ListView) v.getParent().getParent();
            final int position = mListView.getPositionForView((View) v.getParent());
            final Profile p = profileList.get(position);

            AlertDialog.Builder builder = new AlertDialog.Builder(act);
            View view = act.getLayoutInflater().inflate(R.layout.profile_dialog, null);
            TextView dA = (TextView) view.findViewById(R.id.name_profile);
            dA.setText(p.getName() + " - [Days Attended]");
            final EditText edListChild = (EditText) view.findViewById(R.id.value);
            if (p.getDaysAttended().equals("0.0"))
                edListChild.setText("");
            else
                edListChild.setText(p.getDaysAttended());
            edListChild.setSelection(edListChild.length());

            builder.setView(view);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String s1 = edListChild.getText().toString();
                    String s2 = totalDays.getText().toString();
                    if (!s1.equals("")) {
                        if (!s2.equals("")) {
                            if (Double.parseDouble(s1) <= Integer.parseInt(s2)) {
                                Profile prof = new Profile(p.getStudentId(), studentsRoll.get(position) + "", p.getName(),
                                        profileList.get(position).getHeight(), profileList.get(position).getWeight(), s1.replaceAll("\n", " "));
                                profileList.set(position, prof);
                                profileAdapter.notifyDataSetChanged();
                            } else {
                                CommonDialogUtils.displayAlertWhiteDialog(act, "Entered day is greater than total days");
                            }
                        } else {
                            Profile prof = new Profile(p.getStudentId(), studentsRoll.get(position) + "", p.getName(),
                                    profileList.get(position).getHeight(), profileList.get(position).getWeight(), s1.replaceAll("\n", " "));
                            profileList.set(position, prof);
                            profileAdapter.notifyDataSetChanged();
                        }
                    }
                }
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        }
    };

}
