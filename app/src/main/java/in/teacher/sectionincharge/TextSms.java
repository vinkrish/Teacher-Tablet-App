package in.teacher.sectionincharge;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.s3.transfermanager.Transfer;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.amazonaws.mobileconnectors.s3.transfermanager.Upload;
import com.amazonaws.services.s3.model.ProgressEvent;
import com.amazonaws.services.s3.model.ProgressListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import in.teacher.activity.R;
import in.teacher.dao.StudentsDao;
import in.teacher.dao.TempDao;
import in.teacher.fragment.Dashbord;
import in.teacher.model.TransferModel;
import in.teacher.sqlite.Students;
import in.teacher.sqlite.Temp;
import in.teacher.sync.RequestResponseHandler;
import in.teacher.sync.StringConstant;
import in.teacher.util.AppGlobal;
import in.teacher.util.CommonDialogUtils;
import in.teacher.util.Constants;
import in.teacher.util.PKGenerator;
import in.teacher.util.ReplaceFragment;
import in.teacher.util.Util;

/**
 * Created by vinkrish on 03/09/15.
 */
public class TextSms extends Fragment implements StringConstant {
    private Activity act;
    private SQLiteDatabase sqliteDatabase;
    private Button allStudentsBtn, studentBtn, allMaleStudBtn, allFemaleStudBtn, submitBtn;
    private FrameLayout allStudentsFrame;
    private LinearLayout selectionFrame;
    private EditText studentSpinner, textSms;
    private TextView studentContext;
    private int sectionId, teacherId, schoolId;
    private ArrayList<Integer> studIdList;
    private ArrayList<String> studNameList;
    private ArrayList<Long> idList = new ArrayList<>();
    protected boolean[] studentSelections;

    private String ids, zipName, deviceId;
    private int target;
    private ProgressDialog progressBar;

    private Context appContext;
    private TransferManager mTransferManager;
    private boolean uploadComplete, exception;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.text_sms, container, false);

        appContext = AppGlobal.getContext();
        act = AppGlobal.getActivity();
        sqliteDatabase = AppGlobal.getSqliteDatabase();
        CommonDialogUtils.hideKeyboard(getActivity());
        initializeList();

        studentContext = (TextView) view.findViewById(R.id.student_context);
        allStudentsBtn = (Button) view.findViewById(R.id.allStudents);
        studentBtn = (Button) view.findViewById(R.id.stud);
        allMaleStudBtn = (Button) view.findViewById(R.id.male_students);
        allFemaleStudBtn = (Button) view.findViewById(R.id.female_students);
        submitBtn = (Button) view.findViewById(R.id.submit);

        allStudentsFrame = (FrameLayout) view.findViewById(R.id.allStudentsFrame);
        selectionFrame = (LinearLayout) view.findViewById(R.id.selectionFrame);

        studentSpinner = (EditText) view.findViewById(R.id.studSpinner);
        textSms = (EditText) view.findViewById(R.id.textSms);

        studentSpinner.setOnTouchListener(studentTouch);

        allStudentsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deActivate();
                v.setActivated(true);
                submitBtn.setEnabled(true);
                allStudentsFrame.setVisibility(View.VISIBLE);
                studentContext.setText(getResources().getText(R.string.all_students_mes));
                target = 0;
            }
        });

        studentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deActivate();
                v.setActivated(true);
                selectionFrame.setVisibility(View.VISIBLE);
                studentSpinner.setVisibility(View.VISIBLE);
                studentSpinner.setText("");
                studIdList.clear();
                studNameList.clear();
                List<Students> studentList = StudentsDao.selectStudents(sectionId, sqliteDatabase);
                for (Students s : studentList) {
                    studIdList.add(s.getStudentId());
                    studNameList.add(s.getName());
                }
                studentSelections = new boolean[studIdList.size()];
                target = 1;
            }
        });

        allMaleStudBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deActivate();
                v.setActivated(true);
                submitBtn.setEnabled(true);
                allStudentsFrame.setVisibility(View.VISIBLE);
                studentContext.setText(getResources().getText(R.string.all_male_stud_mes));
                target = 2;
            }
        });

        allFemaleStudBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deActivate();
                v.setActivated(true);
                submitBtn.setEnabled(true);
                allStudentsFrame.setVisibility(View.VISIBLE);
                studentContext.setText(getResources().getText(R.string.all_female_stud_mes));
                target = 3;
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                idList.clear();
                if (textSms.getText().toString().equals("")) {
                    CommonDialogUtils.displayAlertWhiteDialog(TextSms.this.getActivity(), "Please enter message to deliver");
                } else {
                    new CalledFTPSync().execute();
                }
            }
        });

        return view;

    }

    private void initializeList() {
        Temp t = TempDao.selectTemp(sqliteDatabase);
        sectionId = t.getSectionId();
        teacherId = t.getTeacherId();
        studIdList = new ArrayList<>();
        studNameList = new ArrayList<>();
    }

    private void hideKeyboard() {
        View view = act.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private View.OnTouchListener studentTouch = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                showStudentDialog();
            }
            int inType = studentSpinner.getInputType();
            studentSpinner.setInputType(InputType.TYPE_NULL);
            studentSpinner.onTouchEvent(event);
            studentSpinner.setInputType(inType);
            return false;
        }
    };

    private void deActivate() {
        allStudentsBtn.setActivated(false);
        allMaleStudBtn.setActivated(false);
        allFemaleStudBtn.setActivated(false);
        studentBtn.setActivated(false);
        submitBtn.setEnabled(false);
        allStudentsFrame.setVisibility(View.GONE);
        selectionFrame.setVisibility(View.GONE);
    }

    public void showStudentDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Students")
                .setCancelable(false)
                .setMultiChoiceItems(studNameList.toArray(new CharSequence[studIdList.size()]), studentSelections, new StudentSelectionClickHandler())
                .setPositiveButton("OK", new StudentButtonClickHandler())
                .show();
    }

    public class StudentSelectionClickHandler implements DialogInterface.OnMultiChoiceClickListener {
        public void onClick(DialogInterface dialog, int clicked, boolean selected) {
            if (selected) studentSelections[clicked] = true;
            else studentSelections[clicked] = false;
        }
    }

    public class StudentButtonClickHandler implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int clicked) {
            switch (clicked) {
                case DialogInterface.BUTTON_POSITIVE:
                    studentSpinner.clearFocus();
                    selectedStudent();
                    break;
            }
        }
    }

    protected void selectedStudent() {
        submitBtn.setEnabled(false);
        boolean isSelected = false;
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        for (int i = 0; i < studIdList.size(); i++) {
            if (studentSelections[i]) {
                isSelected = true;
                sb.append(studIdList.get(i) + ",");
                sb2.append(studNameList.get(i) + ", ");
            }
        }
        if (isSelected) {
            studentSpinner.setText(sb2.substring(0, sb2.length() - 2));
            ids = sb.substring(0, sb.length() - 1);
            submitBtn.setEnabled(true);
        } else {
            studentSpinner.setText("");
        }
    }

    class CalledFTPSync extends AsyncTask<String, Integer, String> {
        private JSONObject jsonReceived;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar = new ProgressDialog(TextSms.this.getActivity());
            progressBar.setCancelable(false);
            progressBar.setMessage("Sending SMS...");
            //	progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            //	progressBar.setProgress(0);
            //	progressBar.setMax(100);
            progressBar.show();
        }

		/*@Override
        protected void onProgressUpdate(Integer... progress) {
			progressBar.setProgress(progress[0]);
		}*/

        protected String doInBackground(String... arg0) {
            mTransferManager = new TransferManager(Util.getCredProvider(appContext));
            uploadComplete = false;
            exception = false;
            prepareIds();
            createUploadFile();

            File root = android.os.Environment.getExternalStorageDirectory();
            File dir = new File(root.getAbsolutePath() + "/Upload");

            File file = new File(dir, zipName);
            UploadModel model = new UploadModel(appContext, zipName, mTransferManager);
            model.upload();

            while (!uploadComplete) {
                Log.d("upload", "...");
            }

            if (!exception) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("school", schoolId);
                    jsonObject.put("tab_id", deviceId);
                    jsonObject.put("file_name", zipName.substring(0, zipName.length() - 3) + "sql");
                    jsonReceived = new JSONObject(RequestResponseHandler.reachServer(acknowledge_uploaded_file, jsonObject));
                    if (jsonReceived.getInt(TAG_SUCCESS) == 1) {
                        file.delete();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBar.dismiss();
            ReplaceFragment.clearBackStack(getFragmentManager());
            ReplaceFragment.replace(new Dashbord(), getFragmentManager());
        }
    }

    private void prepareIds() {
        if (target == 0) {
            Cursor c = sqliteDatabase.rawQuery("select Mobile1 from students where SectionId = "+sectionId , null);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                idList.add(c.getLong(c.getColumnIndex("Mobile1")));
                c.moveToNext();
            }
            c.close();
        } else if (target == 1) {
            Cursor c = sqliteDatabase.rawQuery("select Mobile1 from students where StudentId in (" + ids + ")", null);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                idList.add(c.getLong(c.getColumnIndex("Mobile1")));
                c.moveToNext();
            }
            c.close();
        } else if (target == 2) {
            Cursor c = sqliteDatabase.rawQuery("select Mobile1 from students where SectionId = "+sectionId +" and Gender = 'M'", null);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                idList.add(c.getLong(c.getColumnIndex("Mobile1")));
                c.moveToNext();
            }
            c.close();
        } else if (target == 3) {
            Cursor c = sqliteDatabase.rawQuery("select Mobile1 from students where SectionId = "+sectionId +" and Gender = 'F'", null);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                idList.add(c.getLong(c.getColumnIndex("Mobile1")));
                c.moveToNext();
            }
            c.close();
        }
    }

    private void createUploadFile() {
        Temp t = TempDao.selectTemp(sqliteDatabase);
        deviceId = t.getDeviceId();
        schoolId = t.getSchoolId();
        long timeStamp = PKGenerator.getPrimaryKey();
        zipName = timeStamp + "_" + deviceId + "_" + schoolId + ".zip";
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + "/Upload");
        dir.mkdirs();
        File file = new File(dir, timeStamp + "_" + deviceId + "_" + schoolId + ".sql");
        file.delete();
        try {
            file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            for (Long id : idList) {
                writer.write("insert into queue_transaction(SchoolId, Phone, Message, UserId, Role) " +
                        "values(" + schoolId + ",'" + id + "','" + textSms.getText().toString().replaceAll("\n", "-").replace("'", "\\'").replace("\"", "\\\"") +
                        "'," + teacherId + ", 'Teacher');");
                writer.newLine();
            }
            writer.close();
            FileOutputStream fileOutputStream = new FileOutputStream(new File(dir, zipName));
            ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
            ZipEntry zipEntry = new ZipEntry(file.getName());
            zipOutputStream.putNextEntry(zipEntry);
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buf)) > 0) {
                zipOutputStream.write(buf, 0, bytesRead);
            }
            fileInputStream.close();
            zipOutputStream.closeEntry();
            zipOutputStream.close();
            fileOutputStream.close();
            file.delete();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
    }

    public class UploadModel extends TransferModel {
        private String fileNam;
        private Upload mUpload;
        private ProgressListener mListener;
        private Status mStatus;

        public UploadModel(Context context, String key, TransferManager manager) {
            super(context, Uri.parse(key), manager);
            fileNam = key;
            mStatus = Status.IN_PROGRESS;
            mListener = new ProgressListener() {
                @Override
                public void progressChanged(ProgressEvent event) {
                    if (event.getEventCode() == ProgressEvent.COMPLETED_EVENT_CODE) {
                        mStatus = Status.COMPLETED;
                        Log.d("upload", "complete");
                        uploadComplete = true;
                    } else if (event.getEventCode() == ProgressEvent.FAILED_EVENT_CODE) {
                        exception = true;
                        uploadComplete = true;
                    } else if (event.getEventCode() == ProgressEvent.CANCELED_EVENT_CODE) {
                        exception = true;
                        uploadComplete = true;
                    }
                }
            };
        }

        @Override
        public Status getStatus() {
            return mStatus;
        }

        @Override
        public Transfer getTransfer() {
            return mUpload;
        }

        public void upload() {
            try {
                File root = android.os.Environment.getExternalStorageDirectory();
                File dir = new File(root.getAbsolutePath() + "/Upload");
                File file = new File(dir, fileNam);
                mUpload = getTransferManager().upload(
                        Constants.BUCKET_NAME.toLowerCase(Locale.US), "upload/zipped_folder/" + fileNam,
                        file);
                mUpload.addProgressListener(mListener);
            } catch (Exception e) {
            }
        }

        @Override
        public void abort() {
        }

        @Override
        public void pause() {
        }

        @Override
        public void resume() {
        }
    }
}
