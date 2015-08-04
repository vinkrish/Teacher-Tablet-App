package in.teacher.util;

import android.app.Activity;
import android.app.Dialog;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import in.teacher.activity.R;
import in.teacher.dao.ClasDao;
import in.teacher.dao.SectionDao;
import in.teacher.dao.SlipTesttDao;
import in.teacher.dao.StudentsDao;
import in.teacher.dao.SubjectsDao;
import in.teacher.dao.TempDao;
import in.teacher.fragment.HasPartition;
import in.teacher.fragment.SlipTest;
import in.teacher.fragment.StructuredExam;
import in.teacher.fragment.StudentClassSec;
import in.teacher.fragment.ViewScore;
import in.teacher.sqlite.Clas;
import in.teacher.sqlite.Students;
import in.teacher.sqlite.Temp;

public class CommonDialogUtils {

    public static Dialog displayAlertWhiteDialog(Activity activity, String dialogBody) {
        final Dialog dialog = new Dialog(activity,R.style.DialogSlideAnim);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.white_ok_popup_dialog);
        if(dialogBody != null)
            ((TextView) dialog.findViewById(R.id.alertText)).setText(dialogBody);
        ((Button) dialog.findViewById(R.id.ok_button)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
        //Grab the window of the dialog, and change the width
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = dialog.getWindow();
        lp.copyFrom(window.getAttributes());
        //This makes the dialog take up the full width
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        return dialog;
    }

    public static Dialog displayDashbordSelector(final Activity activity, final SQLiteDatabase sqliteDatabase){
        final Dialog dialog = new Dialog(activity,R.style.DialogSlideAnim);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dashbord_selector);
        Temp temp = TempDao.selectTemp(sqliteDatabase);
        String className = ClasDao.getClassName(temp.getCurrentClass(), sqliteDatabase);
        String sectionName = SectionDao.getSectionName(temp.getCurrentSection(), sqliteDatabase);
        String subjectName = SubjectsDao.getSubjectName(temp.getSubjectId(), sqliteDatabase);
        ((TextView)dialog.findViewById(R.id.classSectionSubject)).setText(className + " - " + sectionName + "  " + subjectName);
        dialog.findViewById(R.id.es).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Temp temp = TempDao.selectTemp(sqliteDatabase);
                List<Students> studentsArray = StudentsDao.selectStudents2("" + temp.getCurrentSection(), temp.getCurrentSubject(), sqliteDatabase);
                if(studentsArray.size()>0){
                    SlipTesttDao.deleteSlipTest(sqliteDatabase);
                    ReplaceFragment.replace(new SlipTest(), activity.getFragmentManager());
                }else{
                    Toast.makeText(activity, "No students taken this subject", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.findViewById(R.id.vs).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SlipTesttDao.deleteSlipTest(sqliteDatabase);
                dialog.dismiss();
                ReplaceFragment.replace(new ViewScore(), activity.getFragmentManager());
            }
        });
        dialog.findViewById(R.id.se).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                int partition = SharedPreferenceUtil.getPartition(activity);
                if(partition==1){
                    ReplaceFragment.replace(new HasPartition(), activity.getFragmentManager());
                }else{
                    ReplaceFragment.replace(new StructuredExam(), activity.getFragmentManager());
                }
            }
        });
        dialog.findViewById(R.id.view_students).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                ReplaceFragment.replace(new StudentClassSec(), activity.getFragmentManager());
            }
        });
        dialog.show();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = dialog.getWindow();
        lp.copyFrom(window.getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        return dialog;
    }

}
