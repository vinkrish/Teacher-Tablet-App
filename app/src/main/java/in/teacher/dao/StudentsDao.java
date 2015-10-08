package in.teacher.dao;

import in.teacher.sqlite.Students;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class StudentsDao {

    public static int clasTotalStrength(int classId, SQLiteDatabase sqliteDatabase) {
        int i = 0;
        String sql = "select Count(*) as count from students where ClassId=" + classId;
        Cursor c = sqliteDatabase.rawQuery(sql, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            i = c.getInt(c.getColumnIndex("count"));
            c.moveToNext();
        }
        c.close();
        return i;
    }

    public static List<Students> selectAbsentStudents(List<Integer> ids, SQLiteDatabase sqliteDatabase) {
        List<Students> sList = new ArrayList<>();
        for (Integer id : ids) {
            Cursor c = sqliteDatabase.rawQuery("select * from students where StudentId=" + id, null);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                Students stud = new Students();
                stud.setStudentId(c.getInt(c.getColumnIndex("StudentId")));
                stud.setClassId(c.getInt(c.getColumnIndex("ClassId")));
                stud.setSectionId(c.getInt(c.getColumnIndex("SectionId")));
                stud.setRollNoInClass(c.getInt(c.getColumnIndex("RollNoInClass")));
                stud.setName(c.getString(c.getColumnIndex("Name")));
                sList.add(stud);
                c.moveToNext();
            }
            c.close();
        }
        return sList;
    }

    public static List<Students> selectStudents(int sectionId, SQLiteDatabase sqliteDatabase) {
        Cursor c = sqliteDatabase.rawQuery("select * from students where SectionId=" + sectionId + " order by RollNoInClass", null);
        List<Students> sList = new ArrayList<>();
        c.moveToFirst();
        while (!c.isAfterLast()) {
            Students stud = new Students();
            stud.setStudentId(c.getInt(c.getColumnIndex("StudentId")));
            stud.setClassId(c.getInt(c.getColumnIndex("ClassId")));
            stud.setSectionId(c.getInt(c.getColumnIndex("SectionId")));
            stud.setRollNoInClass(c.getInt(c.getColumnIndex("RollNoInClass")));
            stud.setName(c.getString(c.getColumnIndex("Name")));
            sList.add(stud);
            c.moveToNext();
        }
        c.close();
        return sList;
    }

    public static List<Students> selectStudentsUnmapped(int sectionId, SQLiteDatabase sqliteDatabase) {
        Cursor c = sqliteDatabase.rawQuery("select StudentId, RollNoInClass, Name from students where SectionId=" + sectionId + " and SubjectIds = '' order by RollNoInClass", null);
        List<Students> sList = new ArrayList<>();
        c.moveToFirst();
        while (!c.isAfterLast()) {
            Students stud = new Students();
            stud.setStudentId(c.getInt(c.getColumnIndex("StudentId")));
            stud.setRollNoInClass(c.getInt(c.getColumnIndex("RollNoInClass")));
            stud.setName(c.getString(c.getColumnIndex("Name")));
            sList.add(stud);
            c.moveToNext();
        }
        c.close();
        return sList;
    }

    public static List<Students> selectStudents2(int sectionId, int subjectId, SQLiteDatabase sqliteDatabase) {
        String sql = "select * from students where SectionId=" + sectionId + " and SubjectIds LIKE '" + subjectId + "#%' UNION " +
                "select * from students where SectionId=" + sectionId + " and SubjectIds LIKE '%#" + subjectId + "#%' UNION " +
                "select * from students where SectionId=" + sectionId + " and SubjectIds LIKE '%#" + subjectId + "' UNION " +
                "select * from students where SectionId=" + sectionId + " and SubjectIds='" + subjectId +
                "' order by RollNoInClass";
        Cursor c = sqliteDatabase.rawQuery(sql, null);
        //	Cursor c = sqliteDatabase.rawQuery("select * from students where SectionId="+sectionId+" and SubjectIds REGEXP '(^"+subjectId+"#|#"+subjectId+"#|#"+subjectId+"$)' " +
        //			"order by RollNoInClass", null);

        List<Students> sList = new ArrayList<>();
        c.moveToFirst();
        while (!c.isAfterLast()) {
            Students stud = new Students();
            stud.setStudentId(c.getInt(c.getColumnIndex("StudentId")));
            stud.setClassId(c.getInt(c.getColumnIndex("ClassId")));
            stud.setSectionId(c.getInt(c.getColumnIndex("SectionId")));
            stud.setRollNoInClass(c.getInt(c.getColumnIndex("RollNoInClass")));
            stud.setName(c.getString(c.getColumnIndex("Name")));
            sList.add(stud);
            c.moveToNext();
        }
        c.close();
        return sList;
    }

    public static List<Integer> selectStudentIds(String sectionid, SQLiteDatabase sqliteDatabase) {
        Cursor c = sqliteDatabase.rawQuery("select RollNoInClass from students where SectionId=" + sectionid + " order by RollNoInClass", null);
        List<Integer> sList = new ArrayList<>();
        c.moveToFirst();
        while (!c.isAfterLast()) {
            sList.add(c.getInt(c.getColumnIndex("RollNoInClass")));
            c.moveToNext();
        }
        c.close();
        return sList;
    }

    public static boolean isStudentMapped(SQLiteDatabase sqliteDatabase, int sectionId) {
        Cursor c = sqliteDatabase.rawQuery("select StudentId from students where SubjectIds='' and SectionId="+sectionId, null);
        if (c.getCount() > 0) {
            Log.d("null exist", "false");
            c.close();
            return false;
        }
        Log.d("null doesn't exist", "true");
        c.close();
        return true;
    }

    public static boolean isStudentMapped2 (SQLiteDatabase sqliteDatabase, int sectionId) {
        Cursor c = sqliteDatabase.rawQuery("select SubjectIds from students where SubjectIds!='' and SectionId="+sectionId, null);
        if (c.getCount() > 0) {
            Log.d("c count", c.getCount()+"");
            Cursor c2 = sqliteDatabase.rawQuery("select count(*) as count from students where SectionId="+sectionId, null);
            Log.d("c2 count", c2.getCount()+"");
            if (c.getCount() == c2.getCount()) {
                c.close();
                c2.close();
                return true;
            }
            c2.close();
        }
        Log.d("doesn't exist", "false");
        c.close();
        return false;
    }

    public static boolean isFewStudentMapped (SQLiteDatabase sqliteDatabase, int sectionId) {
        Cursor c = sqliteDatabase.rawQuery("select SubjectIds from students where SubjectIds!='' and SectionId="+sectionId, null);
        if (c.getCount() > 0) {
           c.close();
            return true;
        }
        c.close();
        return false;
    }

}
