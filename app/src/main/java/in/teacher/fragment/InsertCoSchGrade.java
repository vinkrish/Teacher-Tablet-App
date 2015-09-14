package in.teacher.fragment;

import in.teacher.activity.R;
import in.teacher.adapter.Capitalize;
import in.teacher.dao.CceCoScholasticGradeDao;
import in.teacher.dao.ClasDao;
import in.teacher.dao.SectionDao;
import in.teacher.dao.StudentsDao;
import in.teacher.dao.TeacherDao;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.CceCoScholasticGrade;
import in.teacher.sqlite.Students;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.ReplaceFragment;

import java.util.ArrayList;
import java.util.HashMap;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by vinkrish.
 */

@SuppressLint("InflateParams")
public class InsertCoSchGrade extends Fragment {
    private int Term, TopicId, AspectId, schoolId, classId, sectionId;
    private Activity act;
    private SQLiteDatabase sqliteDatabase;
    private CoSchAdapter coSchAdapter;
    private ArrayList<CoSch> coSchList = new ArrayList<>();
    private List<Students> studentsArray = new ArrayList<>();
    private ArrayList<String> gradList = new ArrayList<>();
    private ArrayList<Integer> valueList = new ArrayList<>();
    private ArrayList<String> inGradList = new ArrayList<>();
    private HashMap<String, Integer> map = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.co_sch_grade, container, false);
        Bundle b = getArguments();
        Term = b.getInt("Term");
        TopicId = b.getInt("TopicId");
        AspectId = b.getInt("AspectId");

        Button submit = (Button) view.findViewById(R.id.submit);
        Button insertA = (Button) view.findViewById(R.id.insertA);

        act = AppGlobal.getActivity();
        Context context = AppGlobal.getContext();
        sqliteDatabase = AppGlobal.getSqliteDatabase();

        Temp t = TempDao.selectTemp(sqliteDatabase);
        schoolId = t.getSchoolId();
        classId = t.getClassId();
        sectionId = t.getSectionId();

        gradList.add("");
        valueList.add(0);
        map.put("", 0);
        Cursor c = sqliteDatabase.rawQuery("select * from ccetopicgrade where TopicId=" + TopicId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            gradList.add(c.getString(c.getColumnIndex("Grade")));
            valueList.add(c.getInt(c.getColumnIndex("Value")));
            map.put(c.getString(c.getColumnIndex("Grade")), c.getInt(c.getColumnIndex("Value")));
            c.moveToNext();
        }
        c.close();

        ListView lv = (ListView) view.findViewById(R.id.list);
        studentsArray = StudentsDao.selectStudents(sectionId, sqliteDatabase);

        for (Students stud : studentsArray) {
            coSchList.add(new CoSch(stud.getRollNoInClass() + "", stud.getName(), "", gradList));
            inGradList.add("");
        }

        coSchAdapter = new CoSchAdapter(context, R.layout.co_sch_list, coSchList);
        lv.setAdapter(coSchAdapter);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<CceCoScholasticGrade> cceCoSchGrade = new ArrayList<>();
                int subLoop = 0;
                for (Students st : studentsArray) {
                    CceCoScholasticGrade ccsg = new CceCoScholasticGrade();
                    ccsg.setSchoolId(schoolId);
                    ccsg.setClassId(classId);
                    ccsg.setSectionId(sectionId);
                    ccsg.setStudentId(st.getStudentId());
                    ccsg.setType(1);
                    ccsg.setTerm(Term);
                    ccsg.setTopicId(TopicId);
                    ccsg.setAspectId(AspectId);
                    CoSch coSch = coSchList.get(subLoop);
                    ccsg.setGrade(map.get(inGradList.get(subLoop)));
                    ccsg.setDescription(coSch.getRemark().replaceAll("['\"]", " ").replaceAll("\n", " "));
                    subLoop += 1;
                    cceCoSchGrade.add(ccsg);
                }
                CceCoScholasticGradeDao.insertCoSchGrade(cceCoSchGrade, sqliteDatabase);
                ReplaceFragment.replace(new CoScholastic(), getFragmentManager());
            }
        });

        insertA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < studentsArray.size(); i++) {
                    inGradList.set(i, "A");
                }
                coSchAdapter.notifyDataSetChanged();
            }
        });

        Button coSch = (Button) view.findViewById(R.id.coSchButton);
        coSch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ReplaceFragment.replace(new CoScholastic(), getFragmentManager());
            }
        });

        Button insertUpdate = (Button) view.findViewById(R.id.coSchInsertUpdate);
        insertUpdate.setText("Insert");

        TextView classSec = (TextView) view.findViewById(R.id.classSec);
        String className = ClasDao.getClassName(classId, sqliteDatabase);
        String secName = SectionDao.getSectionName(sectionId, sqliteDatabase);
        classSec.setText(className + "  -  " + secName);

        return view;
    }

    public class CoSchAdapter extends ArrayAdapter<CoSch> {
        int resource;
        Context context;
        ArrayList<CoSch> data = new ArrayList<>();
        LayoutInflater inflater = null;

        public CoSchAdapter(Context context, int resource, ArrayList<CoSch> listArray) {
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
                holder.tv3 = (TextView) row.findViewById(R.id.remark);
                holder.spin = (Spinner) row.findViewById(R.id.grade);

                row.setTag(holder);
            } else {
                holder = (RecordHolder) row.getTag();
            }

            if (position % 2 == 0)
                row.setBackgroundResource(R.drawable.list_selector1);
            else
                row.setBackgroundResource(R.drawable.list_selector2);

            CoSch listItem = data.get(position);
            holder.tv1.setText(listItem.getRoll());
            holder.tv2.setText(listItem.getName());
            holder.tv3.setText(listItem.getRemark());

            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_header, listItem.getGradeList());
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            holder.spin.setAdapter(adapter);
            holder.spin.setOnItemSelectedListener(new spinnerOnItemSelected(position));

            for (int l = 0; l < adapter.getCount(); l++) {
                if (inGradList.get(position).equals(adapter.getItem(l))) {
                    holder.spin.setSelection(l);
                    break;
                }
            }

            holder.tv3.setOnClickListener(remarkClickListener);
            return row;
        }

        public class spinnerOnItemSelected implements OnItemSelectedListener {
            public int pos;

            public spinnerOnItemSelected(int p) {
                pos = p;
            }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                inGradList.set(pos, gradList.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        }

        private OnClickListener remarkClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                ListView mListView = (ListView) v.getParent().getParent();
                final int position = mListView.getPositionForView((View) v.getParent());

                AlertDialog.Builder builder = new AlertDialog.Builder(act);
                View view = act.getLayoutInflater().inflate(R.layout.hw_dialog, null);
                TextView hwSub = (TextView) view.findViewById(R.id.hwtxt);
                hwSub.setText(studentsArray.get(position).getName());
                final EditText edListChild = (EditText) view.findViewById(R.id.hwmessage);
                CoSch cS = coSchList.get(position);
                edListChild.setText(cS.getRemark());

                builder.setView(view);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!edListChild.getText().toString().equals("")) {
                            CoSch cosch = new CoSch(studentsArray.get(position).getRollNoInClass() + "", studentsArray.get(position).getName(), edListChild.getText().toString(), gradList);
                            coSchList.set(position, cosch);
                            coSchAdapter.notifyDataSetChanged();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();
            }
        };

        class RecordHolder {
            TextView tv1;
            TextView tv2;
            TextView tv3;
            Spinner spin;
        }
    }

    public class CoSch {
        private String roll;
        private String name;
        private String remark;
        private ArrayList<String> gradeList = new ArrayList<String>();

        public CoSch(String roll, String name, String remark, ArrayList<String> gradeList) {
            this.roll = roll;
            this.name = name;
            this.remark = remark;
            this.gradeList = gradeList;
        }

        public String getRoll() {
            return roll;
        }

        public void setRoll(String roll) {
            this.roll = roll;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public ArrayList<String> getGradeList() {
            return gradeList;
        }

        public void setGradeList(ArrayList<String> gradeList) {
            this.gradeList = gradeList;
        }
    }

}
