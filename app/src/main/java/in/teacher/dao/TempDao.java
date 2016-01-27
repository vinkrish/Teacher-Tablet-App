package in.teacher.dao;

import in.teacher.sqlite.Temp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TempDao {

    public static Temp selectTemp(SQLiteDatabase sqliteDatabase) {
        Cursor c = sqliteDatabase.rawQuery("select * from temp where id = 1", null);
        Temp t = new Temp();
        c.moveToFirst();
        while (!c.isAfterLast()) {
            t.setId(c.getInt(c.getColumnIndex("id")));
            t.setDeviceId(c.getString(c.getColumnIndex("DeviceId")));
            t.setSchoolId(c.getInt(c.getColumnIndex("SchoolId")));
            t.setClassId(c.getInt(c.getColumnIndex("ClassId")));
            t.setSectionId(c.getInt(c.getColumnIndex("SectionId")));
            t.setClassInchargeId(c.getInt(c.getColumnIndex("ClassInchargeId")));
            t.setTeacherId(c.getInt(c.getColumnIndex("TeacherId")));
            t.setCurrentSection(c.getInt(c.getColumnIndex("CurrentSection")));
            t.setCurrentSubject(c.getInt(c.getColumnIndex("CurrentSubject")));
            t.setCurrentClass(c.getInt(c.getColumnIndex("CurrentClass")));
            t.setExamId(c.getInt(c.getColumnIndex("ExamId")));
            t.setActivityId(c.getLong(c.getColumnIndex("ActivityId")));
            t.setSubActivityId(c.getLong(c.getColumnIndex("SubActivityId")));
            t.setStudentId(c.getInt(c.getColumnIndex("StudentId")));
            t.setSubjectId(c.getInt(c.getColumnIndex("SubjectId")));
            t.setSlipTestId(c.getLong(c.getColumnIndex("SlipTestId")));
            t.setSyncTime(c.getString(c.getColumnIndex("SyncTime")));
            t.setIsSync(c.getInt(c.getColumnIndex("IsSync")));
            c.moveToNext();
        }
        c.close();
        return t;
    }

    public static void updateTemp(Temp t, SQLiteDatabase sqliteDatabase) {
        ContentValues cv = new ContentValues();
        cv.put("ClassId", t.getClassId());
        cv.put("SectionId", t.getSectionId());
        cv.put("TeacherId", t.getTeacherId());
        cv.put("ClassInchargeId", 0);
        sqliteDatabase.update("temp", cv, "id=1", null);
    }

    public static void updateDeviceId(String id, SQLiteDatabase sqliteDatabase) {
        ContentValues cv = new ContentValues();
        cv.put("DeviceId", id);
        sqliteDatabase.update("temp", cv, "id=1", null);
    }

    public static void updateClassInchargeId(int id, SQLiteDatabase sqliteDatabase) {
        ContentValues cv = new ContentValues();
        cv.put("ClassInchargeId", id);
        sqliteDatabase.update("temp", cv, "id=1", null);
    }

    public static void updateSecSubClas(Temp t, SQLiteDatabase sqliteDatabase) {
        ContentValues cv = new ContentValues();
        cv.put("CurrentSection", t.getCurrentSection());
        cv.put("CurrentSubject", t.getCurrentSubject());
        cv.put("CurrentClass", t.getCurrentClass());
        cv.put("SubjectId", t.getSubjectId());
        sqliteDatabase.update("temp", cv, "id=1", null);
    }

    public static void updateExamId(int id, SQLiteDatabase sqliteDatabase) {
        ContentValues cv = new ContentValues();
        cv.put("ExamId", id);
        sqliteDatabase.update("temp", cv, "id=1", null);
    }

    public static void updateActivityId(long id, SQLiteDatabase sqliteDatabase) {
        ContentValues cv = new ContentValues();
        cv.put("ActivityId", id);
        sqliteDatabase.update("temp", cv, "id=1", null);
    }

    public static void updateSubActivityId(long id, SQLiteDatabase sqliteDatabase) {
        ContentValues cv = new ContentValues();
        cv.put("SubActivityId", id);
        sqliteDatabase.update("temp", cv, "id=1", null);
    }

    public static void updateSlipTestId(long id, SQLiteDatabase sqliteDatabase) {
        ContentValues cv = new ContentValues();
        cv.put("SlipTestId", id);
        sqliteDatabase.update("temp", cv, "id=1", null);
    }

    public static void updateSchoolId(int schoolId, SQLiteDatabase sqliteDatabase) {
        ContentValues cv = new ContentValues();
        cv.put("SchoolId", schoolId);
        sqliteDatabase.update("temp", cv, "id=1", null);
    }

    public static void updateStudentId(int studentId, SQLiteDatabase sqliteDatabase) {
        ContentValues cv = new ContentValues();
        cv.put("StudentId", studentId);
        sqliteDatabase.update("temp", cv, "id=1", null);
    }

    public static void updateSubjectId(int subjectId, SQLiteDatabase sqliteDatabase) {
        ContentValues cv = new ContentValues();
        cv.put("SubjectId", subjectId);
        sqliteDatabase.update("temp", cv, "id=1", null);
    }

    public static void updateSyncTimer(SQLiteDatabase sqliteDatabase) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        Date today = new Date();
        ContentValues cv = new ContentValues();
        cv.put("SyncTime", dateFormat.format(today));
        sqliteDatabase.update("temp", cv, "id=1", null);
    }

    public static void updateSyncComplete(SQLiteDatabase sqliteDatabase) {
        ContentValues cv = new ContentValues();
        cv.put("SyncTime", "Successfully synced the tablet");
        sqliteDatabase.update("temp", cv, "id=1", null);
    }

    public static void updateSyncFailure(SQLiteDatabase sqliteDatabase) {
        ContentValues cv = new ContentValues();
        cv.put("SyncTime", "Failed to sync, try again..");
        sqliteDatabase.update("temp", cv, "id=1", null);
    }

    public static void updateSyncProgress(SQLiteDatabase sqliteDatabase) {
        ContentValues cv = new ContentValues();
        cv.put("SyncTime", "Sync is in Progress..");
        sqliteDatabase.update("temp", cv, "id=1", null);
    }

}
