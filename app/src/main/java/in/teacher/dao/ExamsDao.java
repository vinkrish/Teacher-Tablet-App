package in.teacher.dao;

import in.teacher.sqlite.Exams;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class ExamsDao {

    public static List<Exams> selectExams(int classId, int subjectId, SQLiteDatabase sqliteDatabase) {
        Cursor c = sqliteDatabase.rawQuery("select * from exams where ClassId=" + classId + " and SubjectIDs LIKE '" + subjectId + ",%' UNION " +
                "select * from exams where ClassId=" + classId + " and SubjectIDs LIKE '%," + subjectId + ",%' UNION " +
                "select * from exams where ClassId=" + classId + " and SubjectIDs LIKE '%," + subjectId + "' UNION " +
                "select * from exams where ClassId=" + classId + " and SubjectIDs=" + subjectId, null);
        List<Exams> eList = new ArrayList<>();
        c.moveToFirst();
        while (!c.isAfterLast()) {
            Exams e = new Exams();
            e.setClassId(c.getInt(c.getColumnIndex("ClassId")));
            e.setExamId(c.getInt(c.getColumnIndex("ExamId")));
            e.setSubjectIDs(c.getString(c.getColumnIndex("SubjectIDs")));
            e.setExamName(c.getString(c.getColumnIndex("ExamName")));
            e.setMarkUploaded(c.getInt(c.getColumnIndex("MarkUploaded")));
            e.setGradeSystem(c.getInt(c.getColumnIndex("GradeSystem")));
            eList.add(e);
            c.moveToNext();
        }
        c.close();
        return eList;
    }

    public static String selectExamName(int examId, SQLiteDatabase sqliteDatabase) {
        String s = null;
        Cursor c = sqliteDatabase.rawQuery("select * from exams where ExamId=" + examId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            s = c.getString(c.getColumnIndex("ExamName"));
            c.moveToNext();
        }
        c.close();
        return s;
    }

    public static void insertExam(SQLiteDatabase sqliteDatabase, Exams exams){
        String sql = "insert into exams(SchoolId, ClassId, ExamId, SubjectIDs, SubjectGroupIds, ExamName, OrderId, Percentage, GradeSystem, Term)" +
                " values ("+ exams.getSchoolId()+", "+ exams.getClassId()+", "+exams.getExamId()+", '"+exams.getSubjectIDs()+"','"+
                exams.getSubjectGroupIds()+"', '"+exams.getExamName()+"', "+exams.getOrderId()+", "+exams.getPercentage()+", "+
                exams.getGradeSystem()+", "+exams.getTerm()+")";
        try {
            sqliteDatabase.execSQL(sql);
        }catch(SQLException e){
            e.printStackTrace();
        }
        ContentValues cv = new ContentValues();
        cv.put("Query", sql);
        sqliteDatabase.insert("uploadsql", null, cv);
    }

}
