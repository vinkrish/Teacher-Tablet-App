package in.teacher.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import in.teacher.activity.R.animator;
import in.teacher.attendancefragment.AbsentList;
import in.teacher.attendancefragment.MarkAttendance;
import in.teacher.dao.ClasDao;
import in.teacher.dao.SlipTesttDao;
import in.teacher.dao.StudentAttendanceDao;
import in.teacher.dao.StudentsDao;
import in.teacher.dao.TempDao;
import in.teacher.examfragment.HasPartition;
import in.teacher.examfragment.StructuredExam;
import in.teacher.fragment.Dashbord;
import in.teacher.fragment.StudentClassSec;
import in.teacher.fragment.TeacherInCharge;
import in.teacher.fragment.ViewQueue;
import in.teacher.homeworkfragment.InsertHomework;
import in.teacher.searchfragment.SearchStudST;
import in.teacher.sectionincharge.CoScholastic;
import in.teacher.sectionincharge.MoveStudent;
import in.teacher.sectionincharge.SelectCCEStudentProfile;
import in.teacher.sectionincharge.StudentProfile;
import in.teacher.sectionincharge.SubjectMapStudentCreate;
import in.teacher.sectionincharge.SubjectMapStudentEdit;
import in.teacher.sectionincharge.SubjectTeacherMapping;
import in.teacher.sectionincharge.TextSms;
import in.teacher.sliptestfragment.SlipTest;
import in.teacher.sliptestfragment.ViewScore;
import in.teacher.sqlite.Students;
import in.teacher.sqlite.Temp;
import in.teacher.util.AnimationUtils;
import in.teacher.util.AppGlobal;
import in.teacher.util.CommonDialogUtils;
import in.teacher.util.ExceptionHandler;
import in.teacher.util.NetworkUtils;
import in.teacher.util.ReplaceFragment;
import in.teacher.util.SharedPreferenceUtil;

/**
 * Created by vinkrish.
 */
public class Dashboard extends BaseActivity {
    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private int sectionId;
    private Context context;
    private Activity activity;
    private SQLiteDatabase sqliteDatabase;
    private List<String> studNameList = new ArrayList<>();
    private List<Integer> studIdList = new ArrayList<>();
    private int teacherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_dashboard);

        context = AppGlobal.getContext();
        activity = AppGlobal.getActivity();
        sqliteDatabase = AppGlobal.getSqliteDatabase();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                menuItem.setChecked(true);
                drawerLayout.closeDrawers();

                switch (menuItem.getItemId()) {

                    case R.id.dashboard_item:
                        ReplaceFragment.replace(new Dashbord(), getFragmentManager());
                        return true;

                    case R.id.attendance_item:
                        if (isClassTeacher()) checkAttendance();
                        else showNotAClassTeacher();
                        return true;

                    case R.id.homework_item:
                        if (isClassTeacher())
                            ReplaceFragment.replace(new InsertHomework(), getFragmentManager());
                        else showNotAClassTeacher();
                        return true;

                    case R.id.co_scholastic_item:
                        if (isClassTeacher())
                            ReplaceFragment.replace(new CoScholastic(), getFragmentManager());
                        else showNotAClassTeacher();
                        return true;

                    case R.id.cce_student_profile_item:
                        if (isClassTeacher())
                            ReplaceFragment.replace(new SelectCCEStudentProfile(), getFragmentManager());
                        else showNotAClassTeacher();
                        return true;

                    case R.id.sms_item:
                        if (isClassTeacher()) {
                            if (NetworkUtils.isNetworkConnected(context)) {
                                ReplaceFragment.replace(new TextSms(), getFragmentManager());
                            } else {
                                CommonDialogUtils.displayAlertWhiteDialog(activity, "Please be in WiFi zone or check the status of WiFi");
                            }
                        } else showNotAClassTeacher();
                        return true;

                    case R.id.map_student_subject_item:
                        if (isClassTeacher()) {
                            Temp t = TempDao.selectTemp(sqliteDatabase);
                            int sectionId = t.getSectionId();
                            int classId = t.getClassId();
                            if (StudentsDao.isStudentPresent(sqliteDatabase, sectionId)) {
                                if (ClasDao.isSubjectGroupPresent(sqliteDatabase, classId)) {
                                    if (StudentsDao.isStudentMapped(sqliteDatabase, sectionId))
                                        ReplaceFragment.replace(new SubjectMapStudentEdit(), getFragmentManager());
                                    else
                                        ReplaceFragment.replace(new SubjectMapStudentCreate(), getFragmentManager());
                                } else
                                    CommonDialogUtils.displayAlertWhiteDialog(activity, "Subject group not assigned for this class");
                            } else
                                CommonDialogUtils.displayAlertWhiteDialog(activity, "No students present for this class");
                        } else showNotAClassTeacher();
                        return true;

                    case R.id.map_subject_teacher_item:
                        if (isClassTeacher())
                            ReplaceFragment.replace(new SubjectTeacherMapping(), getFragmentManager());
                        else showNotAClassTeacher();
                        return true;

                    case R.id.student_profile_item:
                        if (isClassTeacher())
                            ReplaceFragment.replace(new StudentProfile(), getFragmentManager());
                        else showNotAClassTeacher();
                        return true;

                    case R.id.move_student_item:
                        if (isClassTeacher())
                            ReplaceFragment.replace(new MoveStudent(), getFragmentManager());
                        else showNotAClassTeacher();
                        return true;

                    default:
                        if (isClassTeacher()) checkAttendance();
                        else showNotAClassTeacher();
                        return true;

                }
            }
        });

        // Initializing Drawer Layout and ActionBarToggle
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        android.support.v7.app.ActionBarDrawerToggle actionBarDrawerToggle = new android.support.v7.app.ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank

                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();

        if (savedInstanceState == null) {
            selectDefaultFragment();
        }

    }

    private void selectDefaultFragment() {
        ReplaceFragment.replace(new Dashbord(), getFragmentManager());
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.searchId:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                View view = this.getLayoutInflater().inflate(R.layout.dialog_search, null);

                studIdList.clear();
                studNameList.clear();

                Temp t = TempDao.selectTemp(sqliteDatabase);
                teacherId = t.getTeacherId();

                Cursor c = sqliteDatabase.rawQuery("select A.StudentId, A.Name, B.ClassName,C.SectionName " +
                        "from students A, class B, section C, subjectteacher D " +
                        "where (D.TeacherId=" + teacherId + " or C.ClassTeacherId=" + teacherId + ") " +
                        "and B.ClassId=A.ClassId and C.SectionId=A.SectionId and A.SectionId=D.SectionId " +
                        "group by A.StudentId", null);
                c.moveToFirst();
                while (!c.isAfterLast()) {
                    studIdList.add(c.getInt(c.getColumnIndex("StudentId")));
                    String s = c.getString(c.getColumnIndex("Name")) + " (" + c.getString(c.getColumnIndex("ClassName")) + " - " + c.getString(c.getColumnIndex("SectionName")) + ")";
                    studNameList.add(s);
                    c.moveToNext();
                }
                c.close();

                ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, studNameList);
                final AutoCompleteTextView textView2 = (AutoCompleteTextView) view.findViewById(R.id.autoCompleteTextView2);
                textView2.setAdapter(adapter2);

                builder.setView(view);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int idx = studNameList.indexOf(textView2.getText().toString());
                        if (idx != -1) {
                            TempDao.updateStudentId(studIdList.get(idx), sqliteDatabase);
                            ReplaceFragment.replace(new SearchStudST(), getFragmentManager());
                        }
                    }
                });
                builder.setNegativeButton("Cancel", null);

                AlertDialog dialog = builder.create();
                dialog.getWindow().setGravity(Gravity.TOP);
                WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
                layoutParams.y = 100; // top margin
                dialog.getWindow().setAttributes(layoutParams);
                dialog.show();

                // builder.show();
                return true;

            case R.id.action_logout:
                Intent intent = new Intent(Dashboard.this, in.teacher.activity.LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                AnimationUtils.activityExitVertical(Dashboard.this);
                return true;

            case R.id.action_queue:
                ReplaceFragment.replace(new ViewQueue(), getFragmentManager());
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isClassTeacher() {
        Temp t = TempDao.selectTemp(sqliteDatabase);
        if (t.getClassId() == 0) {
            return false;
        }
        return true;
    }

    private void showNotAClassTeacher() {
        CommonDialogUtils.displayAlertWhiteDialog(this, "You must be a class teacher to use this feature!");
    }

    private void checkAttendance() {
        boolean flag = false;
        Temp t = TempDao.selectTemp(sqliteDatabase);
        sectionId = t.getSectionId();
        int marked = StudentAttendanceDao.isStudentAttendanceMarked(sectionId, getDate(), sqliteDatabase);
        if (marked == 1) {
            flag = true;
        }
        Bundle b = new Bundle();
        b.putInt("today", 1);
        b.putInt("yesterday", 0);
        b.putInt("otherday", 0);
        if (flag) {
            Fragment fragment = new AbsentList();
            fragment.setArguments(b);
            getFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(animator.fade_in, animator.fade_out)
                    .replace(R.id.content_frame, fragment).addToBackStack(null).commit();
        } else {
            ReplaceFragment.replace(new MarkAttendance(), getFragmentManager());
        }
    }

    private String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard, menu);
        if (NetworkUtils.isNetworkConnected(context)) {
            menu.getItem(0).setVisible(false);
        } else {
            menu.getItem(0).setVisible(true);
        }
        return true;
    }

    public void callAttendance(View view) {
        if (isClassTeacher()) checkAttendance();
        else showNotAClassTeacher();
    }

    public void callHomework(View view) {
        if (isClassTeacher())
            ReplaceFragment.replace(new InsertHomework(), getFragmentManager());
        else showNotAClassTeacher();
    }

    public void callSlipTest(View view) {
        Temp temp = TempDao.selectTemp(sqliteDatabase);
        int sectId = temp.getCurrentSection();
        int subjId = temp.getCurrentSubject();
        List<Students> studentsArray = StudentsDao.selectStudents2(sectId, subjId, sqliteDatabase);
        if (studentsArray.size() > 0) {
            SlipTesttDao.deleteSlipTest(sqliteDatabase);
            ReplaceFragment.replace(new SlipTest(), getFragmentManager());
        } else {
            Toast.makeText(context, "No students taken this subject", Toast.LENGTH_SHORT).show();
        }
    }

    public void callViewScore(View view) {
        SlipTesttDao.deleteSlipTest(sqliteDatabase);
        ReplaceFragment.replace(new ViewScore(), getFragmentManager());
    }

    public void callStructuredExam(View view) {
        int partition = SharedPreferenceUtil.getPartition(this);
        if (partition == 1) {
            ReplaceFragment.replace(new HasPartition(), getFragmentManager());
        } else {
            ReplaceFragment.replace(new StructuredExam(), getFragmentManager());
        }
    }

    public void callViewStudents(View view) {
        ReplaceFragment.replace(new StudentClassSec(), getFragmentManager());
    }

    public void toDashbord(View v) {
        ReplaceFragment.replace(new Dashbord(), getFragmentManager());
    }

    public void toClassInchargeDash(View v) {
        ReplaceFragment.replace(new TeacherInCharge(), getFragmentManager());
    }

}
