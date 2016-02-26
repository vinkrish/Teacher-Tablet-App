package in.teacher.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class SubjectsDao {

    public static String getSubjectName(int subjectId, SQLiteDatabase sqliteDatabase) {
        Cursor c = sqliteDatabase.rawQuery("select SubjectName from subjects where SubjectId=" + subjectId, null);
        c.moveToFirst();
        String s = c.getString(c.getColumnIndex("SubjectName"));
        c.close();
        return s;
    }

    public static List<String> getSubjectNameList(SQLiteDatabase sqliteDatabase, List<Integer> ids) {
        List<String> list = new ArrayList<>();
        for (Integer id: ids) {
            Cursor c = sqliteDatabase.rawQuery("SELECT SubjectName FROM subjects where SubjectId =" + id, null);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                list.add(c.getString(c.getColumnIndex("SubjectName")));
                c.moveToNext();
            }
            c.close();
        }
        return list;
    }

    public static boolean isPartition (SQLiteDatabase sqliteDatabase, int subjectId) {
        Cursor c = sqliteDatabase.rawQuery("select has_partition from subjects where SubjectId = "+subjectId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            if (c.getInt(c.getColumnIndex("has_partition")) == 1) {
                c.close();
                return true;
            }
            c.moveToNext();
        }
        c.close();
        return false;
    }

}
