package in.teacher.sqlite;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by vinkrish.
 */
public class SqlDbHelper extends SQLiteOpenHelper implements SqlConstant {
    private static SqlDbHelper dbHelper;
    public SQLiteDatabase sqliteDatabase;

    private SqlDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static SqlDbHelper getInstance(Context context) {
        if (dbHelper == null) {
            dbHelper = new SqlDbHelper(context.getApplicationContext());
        }
        return dbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_CLASS);
        db.execSQL(CREATE_SCHOOL);
        db.execSQL(CREATE_SECTION);
        db.execSQL(CREATE_STUDENTS);
        db.execSQL(CREATE_STUDENT_ATTENDANCE);
        db.execSQL(CREATE_TEMP_ATTENDANCE);
        db.execSQL(CREATE_SUBJECTS);
        db.execSQL(CREATE_SUBJECT_TEACHER);
        db.execSQL(CREATE_TEACHER);
        db.execSQL(CREATE_EXAMS);
        db.execSQL(CREATE_MARKS);
        db.execSQL(CREATE_TEMP);
        db.execSQL(CREATE_ACTIVITY);
        db.execSQL(CREATE_ACTIVITY_MARK);
        db.execSQL(CREATE_ACTIVITY_GRADE);
        db.execSQL(CREATE_SUB_ACTIVITY);
        db.execSQL(CREATE_SUB_ACTIVITY_MARK);
        db.execSQL(CREATE_SUB_ACTIVITY_GRADE);
        db.execSQL(CREATE_DOWNLOADED_FILE);
        db.execSQL(CREATE_UPLOAD_SQL);
        db.execSQL(CREATE_UPLOADED_FILE);
        db.execSQL(CREATE_PORTION);
        db.execSQL(CREATE_SLIPTEST);
        db.execSQL(CREATE_HOMEWORK);
        db.execSQL(CREATE_STAVG);
        db.execSQL(CREATE_SUBJECT_EXAMS);
        db.execSQL(CREATE_GCW);
        db.execSQL(CREATE_CCECOSCHOLASTIC);
        db.execSQL(CREATE_CCESECTIONHEADING);
        db.execSQL(CREATE_CCETOPICPRIMARY);
        db.execSQL(CREATE_CCEASPECTPRIMARY);
        db.execSQL(CREATE_CCETOPICGRADE);
        db.execSQL(CREATE_CCECOSCHOLASTICGRADE);
        db.execSQL(CREATE_AVGTRACK);
        db.execSQL(CREATE_LOCKED);
        db.execSQL("insert into temp(id,DeviceId, SchoolId, ClassId, SectionId, ClassInchargeId, TeacherId, StudentId, SubjectId, " +
                "CurrentSection, CurrentSubject, CurrentClass, ExamId, ActivityId, SubActivityId, SlipTestId, SyncTime, IsSync) " +
                "values(1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,'Not Yet Synced',0)");
        db.execSQL(CREATE_CCE_STUDENT_PROFILE);
        db.execSQL(CREATE_SUB_GROUPS);
        db.execSQL(CREATE_TIMETABLE);
        db.execSQL(CREATE_TIMETABLE_TIMING);
        db.execSQL(CREATE_TEACHER_INCHARGE);
        db.execSQL(CREATE_TERM_REMARK);
        db.execSQL(CREATE_MOVE_STUDENT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS class");
        db.execSQL("DROP TABLE IF EXISTS school");
        db.execSQL("DROP TABLE IF EXISTS section");
        db.execSQL("DROP TABLE IF EXISTS studentattendance");
        db.execSQL("DROP TABLE IF EXISTS tempattendance");
        db.execSQL("DROP TABLE IF EXISTS students");
        db.execSQL("DROP TABLE IF EXISTS subjectteacher");
        db.execSQL("DROP TABLE IF EXISTS subjects");
        db.execSQL("DROP TABLE IF EXISTS teacher");
        db.execSQL("DROP TABLE IF EXISTS exams");
        db.execSQL("DROP TABLE IF EXISTS marks");
        db.execSQL("DROP TABLE IF EXISTS temp");
        db.execSQL("DROP TABLE IF EXISTS activity");
        db.execSQL("DROP TABLE IF EXISTS activitymark");
        db.execSQL("DROP TABLE IF EXISTS activitygrade");
        db.execSQL("DROP TABLE IF EXISTS subactivity");
        db.execSQL("DROP TABLE IF EXISTS subactivitymark");
        db.execSQL("DROP TABLE IF EXISTS subactivitygrade");
        db.execSQL("DROP TABLE IF EXISTS uploadsql");
        db.execSQL("DROP TABLE IF EXISTS uploadedfile");
        db.execSQL("DROP TABLE IF EXISTS downloadedfile");
        db.execSQL("DROP TABLE IF EXISTS portion");
        db.execSQL("DROP TABLE IF EXISTS sliptest");
        db.execSQL("DROP TABLE IF EXISTS homeworkmessage");
        db.execSQL("DROP TABLE IF EXISTS stavg");
        db.execSQL("DROP TABLE IF EXISTS subjectexams");
        db.execSQL("DROP TABLE IF EXISTS dwnautoid");
        db.execSQL("DROP TABLE IF EXISTS gradesclasswise");
        db.execSQL("DROP TABLE IF EXISTS ccecoscholastic");
        db.execSQL("DROP TABLE IF EXISTS ccesectionheading");
        db.execSQL("DROP TABLE IF EXISTS ccetopicprimary");
        db.execSQL("DROP TABLE IF EXISTS cceaspectprimary");
        db.execSQL("DROP TABLE IF EXISTS ccetopicgrade");
        db.execSQL("DROP TABLE IF EXISTS ccecoscholasticgrade");
        db.execSQL("DROP TABLE IF EXISTS avgtrack");
        db.execSQL("DROP TABLE IF EXISTS locked");
        db.execSQL("DROP TABLE IF EXISTS ccestudentprofile");
        db.execSQL("DROP TABLE IF EXISTS subjects_groups");
        db.execSQL("DROP TABLE IF EXISTS timetable");
        db.execSQL("DROP TABLE IF EXISTS timetabletimings");
        db.execSQL("DROP TABLE IF EXISTS classteacher_incharge");
        db.execSQL("DROP TABLE IF EXISTS term_remark");
        db.execSQL("DROP TABLE IF EXISTS movestudent");
        onCreate(db);
    }

    public void createIndex(SQLiteDatabase sqliteDatabase) {
        sqliteDatabase.execSQL("CREATE INDEX marks_index ON marks(ExamId,SubjectId,StudentId,StudentId,Mark)");
        sqliteDatabase.execSQL("CREATE INDEX actmarks_index ON activitymark(ExamId,SubjectId,ActivityId,StudentId,Mark)");
        sqliteDatabase.execSQL("CREATE INDEX subactmarks_index ON subactivitymark(ExamId,ActivityId,SubjectId,SubActivityId,StudentId,Mark)");
    }

    public void createTrigger(int schoolId, SQLiteDatabase sqliteDatabase) {
        sqliteDatabase.execSQL(ATTENDANCE_TRIGGER);
        sqliteDatabase.execSQL(SLIPTEST_TRIGGER);
        sqliteDatabase.execSQL(CCEGRADE_TRIGGER);
        sqliteDatabase.execSQL(CSP_TRIGGER);
        sqliteDatabase.execSQL(CREATE_STUDENTS_TRIGGER);
        sqliteDatabase.execSQL(CREATE_EXAMS_TRIGGER);
        sqliteDatabase.execSQL(CREATE_ACTIVITY_TRIGGER);
        sqliteDatabase.execSQL(CREATE_SUB_ACTIVITY_TRIGGER);
        sqliteDatabase.execSQL(TERM_REMARK_TRIGGER);
        sqliteDatabase.execSQL(MOVE_STUDENT_TRIGGER);
        sqliteDatabase.execSQL(CREATE_SUBJECTTEACHER_TRIGGER);
        sqliteDatabase.execSQL(CREATE_SUBJECTEXAMS_TRIGGER);
        sqliteDatabase.execSQL(HOMEWORK_TRIGGER);
        sqliteDatabase.execSQL(MARKS_TRIGGER);
        sqliteDatabase.execSQL(ACTIVITYMARK_TRIGGER);
        sqliteDatabase.execSQL(ACTIVITYGRADE_TRIGGER);
        sqliteDatabase.execSQL(SUBACTMARK_TRIGGER);
        sqliteDatabase.execSQL(SUBACTGRADE_TRIGGER);
        sqliteDatabase.execSQL("CREATE TRIGGER insert_stmark BEFORE INSERT ON sliptestmark_" + schoolId +
                " FOR EACH ROW " +
                "BEGIN " +
                "INSERT INTO avgtrack(SubjectId,SectionId,Type) values(NEW.SubjectId, NEW.SectionId, 0); " +
                "END");
        sqliteDatabase.execSQL("CREATE TRIGGER update_stmark BEFORE UPDATE ON sliptestmark_" + schoolId +
                " FOR EACH ROW " +
                "BEGIN " +
                "INSERT INTO avgtrack(SubjectId,SectionId,Type) values(NEW.SubjectId, NEW.SectionId, 1); " +
                "END");
        sqliteDatabase.execSQL("CREATE TRIGGER stmark_trigger BEFORE INSERT ON sliptestmark_" + schoolId +
                " WHEN ((SELECT count() FROM sliptestmark_" + schoolId + " WHERE sliptestmark_" + schoolId + ".SlipTestId=NEW.SlipTestId and sliptestmark_" + schoolId + ".StudentId=NEW.StudentId)>0) " +
                "BEGIN " +
                "DELETE FROM sliptestmark_" + schoolId + " WHERE sliptestmark_" + schoolId + ".SlipTestId=NEW.SlipTestId and sliptestmark_" + schoolId + ".StudentId=NEW.StudentId; " +
                "END");
    }

    public void removeIndex(SQLiteDatabase sqliteDatabase) {
        sqliteDatabase.execSQL("DROP INDEX IF EXISTS marks_index");
        sqliteDatabase.execSQL("DROP INDEX IF EXISTS actmarks_index");
        sqliteDatabase.execSQL("DROP INDEX IF EXISTS subactmarks_index");
    }

    public void dropTrigger(SQLiteDatabase sqliteDatabase) {
        sqliteDatabase.execSQL("DROP TRIGGER IF EXISTS before_homework");
        sqliteDatabase.execSQL("DROP TRIGGER IF EXISTS before_marks");
        sqliteDatabase.execSQL("DROP TRIGGER IF EXISTS before_actmark");
        sqliteDatabase.execSQL("DROP TRIGGER IF EXISTS before_actgrade");
        sqliteDatabase.execSQL("DROP TRIGGER IF EXISTS before_subactgrade");
        sqliteDatabase.execSQL("DROP TRIGGER IF EXISTS before_subactmark");
        sqliteDatabase.execSQL("DROP TRIGGER IF EXISTS insert_stmark");
        sqliteDatabase.execSQL("DROP TRIGGER IF EXISTS update_stmark");
        sqliteDatabase.execSQL("DROP TRIGGER IF EXISTS stmark_trigger");
        sqliteDatabase.execSQL("DROP TRIGGER IF EXISTS before_attendance");
        sqliteDatabase.execSQL("DROP TRIGGER IF EXISTS before_sliptest");
        sqliteDatabase.execSQL("DROP TRIGGER IF EXISTS before_ccegrade");
        sqliteDatabase.execSQL("DROP TRIGGER IF EXISTS before_csp");
        sqliteDatabase.execSQL("DROP TRIGGER IF EXISTS before_exams");
        sqliteDatabase.execSQL("DROP TRIGGER IF EXISTS before_students");
        sqliteDatabase.execSQL("DROP TRIGGER IF EXISTS before_activity");
        sqliteDatabase.execSQL("DROP TRIGGER IF EXISTS before_subactivity");
        sqliteDatabase.execSQL("DROP TRIGGER IF EXISTS before_term_remark");
        sqliteDatabase.execSQL("DROP TRIGGER IF EXISTS before_move_student");
        sqliteDatabase.execSQL("DROP TRIGGER IF EXISTS before_subjectteacher");
        sqliteDatabase.execSQL("DROP TRIGGER IF EXISTS before_subjectexams");
    }

    public void clearTempAttendance(SQLiteDatabase sqliteDatabase) {
        sqliteDatabase.execSQL("delete from tempattendance");
    }

    public void insertDownloadedFile(String fileName, SQLiteDatabase sqliteDatabase) {
        try {
            sqliteDatabase.execSQL("insert into downloadedfile(filename) values('" + fileName + "')");
        } catch (SQLException e) {
        }
    }

    public void deleteLocked(SQLiteDatabase sqliteDatabase) {
        sqliteDatabase.delete("locked", null, null);
    }

    public void deleteTables(SQLiteDatabase sqliteDatabase) {
        sqliteDatabase.delete("stavg", null, null);
        sqliteDatabase.delete("tempattendance", null, null);
        sqliteDatabase.delete("downloadedfile", null, null);
        sqliteDatabase.delete("avgtrack", null, null);
        sqliteDatabase.delete("activity", null, null);
        sqliteDatabase.delete("activitymark", null, null);
        sqliteDatabase.delete("activitygrade", null, null);
        sqliteDatabase.delete("cceaspectprimary", null, null);
        sqliteDatabase.delete("ccecoscholastic", null, null);
        sqliteDatabase.delete("ccecoscholasticgrade", null, null);
        sqliteDatabase.delete("ccesectionheading", null, null);
        sqliteDatabase.delete("ccetopicgrade", null, null);
        sqliteDatabase.delete("ccetopicprimary", null, null);
        sqliteDatabase.delete("class", null, null);
        sqliteDatabase.delete("exams", null, null);
        sqliteDatabase.delete("homeworkmessage", null, null);
        sqliteDatabase.delete("marks", null, null);
        sqliteDatabase.delete("portion", null, null);
        sqliteDatabase.delete("school", null, null);
        sqliteDatabase.delete("section", null, null);
        sqliteDatabase.delete("sliptest", null, null);
        sqliteDatabase.delete("students", null, null);
        sqliteDatabase.delete("subactivity", null, null);
        sqliteDatabase.delete("subactivitymark", null, null);
        sqliteDatabase.delete("subactivitygrade", null, null);
        sqliteDatabase.delete("subjectexams", null, null);
        sqliteDatabase.delete("subjects", null, null);
        sqliteDatabase.delete("subjectteacher", null, null);
        sqliteDatabase.delete("teacher", null, null);
        sqliteDatabase.delete("gradesclasswise", null, null);
        sqliteDatabase.delete("studentattendance", null, null);
        sqliteDatabase.delete("ccestudentprofile", null, null);
        sqliteDatabase.delete("subjects_groups", null, null);
        sqliteDatabase.delete("timetable", null, null);
        sqliteDatabase.delete("timetabletimings", null, null);
        sqliteDatabase.delete("classteacher_incharge", null, null);
        sqliteDatabase.delete("term_remark", null, null);
    }
}
