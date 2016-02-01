package in.teacher.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import in.teacher.sqlite.SubjectExams;

public class SubjectExamsDao {

    public static float getExmMaxMark(int classId, long examId, int subjectId, SQLiteDatabase sqliteDatabase) {
        float maxMark = 100;
        Cursor c = sqliteDatabase.rawQuery("select MaximumMark from subjectexams where ClassId=" + classId + " and ExamId=" + examId +
                " and SubjectId=" + subjectId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            maxMark = c.getFloat(c.getColumnIndex("MaximumMark"));
            c.moveToNext();
        }
        c.close();
        return maxMark;
    }

    public static String selectSubjectName(int subjectId, SQLiteDatabase sqliteDatabase) {
        String subjectName = null;
        Cursor c = sqliteDatabase.rawQuery("select * from subjects where SubjectId=" + subjectId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            subjectName = c.getString(c.getColumnIndex("SubjectName"));
            c.moveToNext();
        }
        c.close();
        return subjectName;
    }

    public static int isExmMaxMarkDefined(int classId, long examId, int subjectId, SQLiteDatabase sqliteDatabase) {
        int isDefined = 0;
        Cursor c = sqliteDatabase.rawQuery("select MaximumMark from subjectexams where ClassId=" + classId + " and ExamId=" + examId +
                " and SubjectId=" + subjectId, null);
        if (c.getCount() > 0) {
            isDefined = 1;
        }
        c.close();
        return isDefined;
    }

    public static void insertSubjectExams(SQLiteDatabase sqliteDatabase, List<SubjectExams> seList) {
        for (SubjectExams se: seList) {
            String sql = "insert into subjectexams(SchoolId, ClassId, ExamId, SubjectId, MaximumMark, FailMark) " +
                    "values("+se.getSchoolId()+", "+se.getClassId()+", "+se.getExamId()+", "+se.getSubjectId()+", "+se.getMaximumMark()+", "+se.getFailMark()+")";
            try {
                sqliteDatabase.execSQL(sql);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            ContentValues cv = new ContentValues();
            cv.put("Query", sql);
            sqliteDatabase.insert("uploadsql", null, cv);
        }
    }

}
