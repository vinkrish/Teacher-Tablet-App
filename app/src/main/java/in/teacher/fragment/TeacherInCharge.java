package in.teacher.fragment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.teacher.activity.R;
import in.teacher.adapter.Capitalize;
import in.teacher.dao.TeacherDao;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.Temp;
import in.teacher.util.AnimationUtils;
import in.teacher.util.AppGlobal;
import in.teacher.util.ReplaceFragment;

/**
 * Created by vinkrish on 19/10/15.
 * Don't expect comments explaining every piece of code, class and function names are self explanatory.
 */
public class TeacherInCharge extends Fragment {
    private Context context;
    private SQLiteDatabase sqliteDatabase;
    private TextView name;
    private SwitchCompat teacherIncharge;
    private int teacherId, classInchargeId, classInChargePos;
    private String teacherName;
    private Spinner classSpinner;
    private List<Integer> classInchargeList = new ArrayList<>();
    private List<String> classNameIncharge = new ArrayList<>();
    private Button createExmBtn, updateExmBtn, actBtn, subActBtn, cpyExmBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.teacher_incharge, container, false);

        name = (TextView) view.findViewById(R.id.teacherName);
        teacherIncharge = (SwitchCompat) view.findViewById(R.id.classIncharge);
        classSpinner = (Spinner) view.findViewById(R.id.classSpinner);

        init();

        createExmBtn = (Button) view.findViewById(R.id.create_exam);
        updateExmBtn = (Button) view.findViewById(R.id.update_exam);
        actBtn = (Button) view.findViewById(R.id.create_edit_act);
        subActBtn = (Button) view.findViewById(R.id.create_edit_subact);
        cpyExmBtn = (Button) view.findViewById(R.id.copy_exam_structure);

        createExmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReplaceFragment.replace(new ExamCreate(), getFragmentManager());
            }
        });

        updateExmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReplaceFragment.replace(new ExamEdit(), getFragmentManager());
            }
        });

        actBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReplaceFragment.replace(new ActivityCreateEdit(), getFragmentManager());
            }
        });

        subActBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReplaceFragment.replace(new SubActivityCreateEdit(), getFragmentManager());
            }
        });

        cpyExmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReplaceFragment.replace(new CopyExamStructure(), getFragmentManager());
            }
        });

        view.post(new Runnable() {
            @Override
            public void run() {
                animateView();
            }
        });

        return view;

    }

    private void animateView(){
/*
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(createExmBtn, View.TRANSLATION_Y, 100f, 0);
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(updateExmBtn, "translationY", 100f, 0);
        ObjectAnimator anim3 = ObjectAnimator.ofFloat(actBtn, "translationY", 100f, 0);
        ObjectAnimator anim4 = ObjectAnimator.ofFloat(subActBtn, "translationY", 100f, 0);
        ObjectAnimator anim5 = ObjectAnimator.ofFloat(cpyExmBtn, "translationY", 100f, 0);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(anim1).before(anim2).after(300);
        animatorSet.play(anim3).after(anim2).after(200);
        animatorSet.play(anim4).after(anim3).after(100);
        animatorSet.play(anim5).after(anim4).after(50);

        animatorSet.start();
*/
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int height = displayMetrics.heightPixels;

        Animation anim1 = new TranslateAnimation(0, 0, 0, 0,  Animation.ABSOLUTE, height,  Animation.RELATIVE_TO_SELF, 0);
        anim1.setDuration(300);
        createExmBtn.startAnimation(anim1);

        Animation anim2 = new TranslateAnimation(0, 0, 0, 0,  Animation.ABSOLUTE, height,  Animation.RELATIVE_TO_SELF, 0);
        anim2.setDuration(300);
        anim2.setStartOffset(200);
        updateExmBtn.startAnimation(anim2);

        Animation anim3 = new TranslateAnimation(0, 0, 0, 0,  Animation.ABSOLUTE, height,  Animation.RELATIVE_TO_SELF, 0);
        anim3.setDuration(300);
        anim3.setStartOffset(300);
        actBtn.startAnimation(anim3);

        Animation anim4 = new TranslateAnimation(0, 0, 0, 0,  Animation.ABSOLUTE, height,  Animation.RELATIVE_TO_SELF, 0);
        anim4.setDuration(300);
        anim4.setStartOffset(400);
        subActBtn.startAnimation(anim4);

        Animation anim5 = new TranslateAnimation(0, 0, 0, 0,  Animation.ABSOLUTE, height,  Animation.RELATIVE_TO_SELF, 0);
        anim5.setDuration(300);
        anim5.setStartOffset(500);
        cpyExmBtn.startAnimation(anim5);

    }

    private void init() {
        context = AppGlobal.getContext();
        sqliteDatabase = AppGlobal.getSqliteDatabase();

        Temp t = TempDao.selectTemp(sqliteDatabase);
        teacherId = t.getTeacherId();
        classInchargeId = t.getClassInchargeId();

        teacherName = Capitalize.capitalThis((TeacherDao.selectTeacherName(teacherId, sqliteDatabase)));
        name.setText("[ " + teacherName + " ]");

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

        classSpinner.setSelection(classInChargePos);

        classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TempDao.updateClassInchargeId(classInchargeList.get(position), sqliteDatabase);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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
