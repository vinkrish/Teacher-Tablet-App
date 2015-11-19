package in.teacher.sectionincharge;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import in.teacher.activity.R;
import in.teacher.adapter.StudentProfileAdapter;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.CommonObject;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.CommonDialogUtils;
import in.teacher.util.ReplaceFragment;

/**
 * Created by vinkrish on 21/10/15.
 */
public class StudentProfile extends Fragment {
    private Context context;
    private SQLiteDatabase sqliteDatabase;
    private int sectionId;
    private List<Integer> idList = new ArrayList<>();
    private List<String> nameList = new ArrayList<>();
    private List<Integer> rollNoList = new ArrayList<>();
    private List<String> admissionList = new ArrayList<>();
    private ListView listView;
    private ArrayList<CommonObject> commonObjectList = new ArrayList<>();
    private Button addBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.student_profile, container, false);
        listView = (ListView) view.findViewById(R.id.listView);
        addBtn = (Button) view.findViewById(R.id.add_student);

        init();

        return view;

    }

    private void init(){
        context = AppGlobal.getContext();
        sqliteDatabase = AppGlobal.getSqliteDatabase();
        CommonDialogUtils.hideKeyboard(getActivity());

        Temp t = TempDao.selectTemp(sqliteDatabase);
        sectionId = t.getSectionId();

        Cursor c = sqliteDatabase.rawQuery("select StudentId, AdmissionNo, RollNoInClass, Name from students where SectionId = " +
                sectionId + " order by RollNoInClass", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            idList.add(c.getInt(c.getColumnIndex("StudentId")));
            String admission = c.getString(c.getColumnIndex("AdmissionNo"));
            int rollNo = c.getInt(c.getColumnIndex("RollNoInClass"));
            String name = c.getString(c.getColumnIndex("Name"));

            admissionList.add(admission);
            rollNoList.add(rollNo);
            nameList.add(name);

            commonObjectList.add(new CommonObject(rollNo+"", admission, name));

            c.moveToNext();
        }
        c.close();

        StudentProfileAdapter studentProfileAdapter = new StudentProfileAdapter(context, R.layout.student_profile_list, commonObjectList);
        listView.setAdapter(studentProfileAdapter);

        listView.setOnItemClickListener(listSelector);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReplaceFragment.replace(new StudentAdd(), getFragmentManager());
            }
        });

    }

    AdapterView.OnItemClickListener listSelector = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            TempDao.updateStudentId(idList.get(position), sqliteDatabase);
            ReplaceFragment.replace(new StudentProfileEdit(), getFragmentManager());
        }
    };

}
