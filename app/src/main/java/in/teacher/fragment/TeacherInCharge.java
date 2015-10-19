package in.teacher.fragment;

import android.app.Fragment;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import in.teacher.activity.R;
import in.teacher.adapter.Capitalize;
import in.teacher.dao.TeacherDao;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.ReplaceFragment;

/**
 * Created by vinkrish on 19/10/15.
 */
public class TeacherInCharge extends Fragment {
    private SQLiteDatabase sqliteDatabase;
    private TextView name;
    private Switch teacherIncharge;
    private int teacherId;
    private String teacherName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.teacher_incharge, container, false);

        name = (TextView) view.findViewById(R.id.teacherName);
        teacherIncharge = (Switch) view.findViewById(R.id.classIncharge);

        init();

        return view;

    }

    private void init(){
        sqliteDatabase = AppGlobal.getSqliteDatabase();

        Temp t = TempDao.selectTemp(sqliteDatabase);
        teacherId = t.getTeacherId();

        teacherName = Capitalize.capitalThis((TeacherDao.selectTeacherName(teacherId, sqliteDatabase)));
        name.setText("[ "+ teacherName + " ]");

        teacherIncharge.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    ReplaceFragment.replace(new Dashbord(), getFragmentManager());
                }
            }
        });

    }

}
