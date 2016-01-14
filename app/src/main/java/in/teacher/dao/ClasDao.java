package in.teacher.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import in.teacher.sqlite.Clas;

public class ClasDao {

    public static String getClassName(int classId, SQLiteDatabase sqliteDatabase) {
        String s = null;
        Cursor c = sqliteDatabase.rawQuery("select ClassName from class where ClassId=" + classId, null);
        c.moveToFirst();
        if (c.getCount() > 0) {
            s = c.getString(c.getColumnIndex("ClassName"));
        }
        c.close();
        return s;
    }

    public static boolean isSwitchClass(int teacherId, SQLiteDatabase sqliteDatabase) {
        Cursor c = sqliteDatabase.rawQuery("select A.SectionId, B.ClassName from section A, class B where A.ClassTeacherId  = " + teacherId
                + " and A.ClassId = B.ClassId group by A.SectionId", null);
        if (c.getCount() > 1) {
            c.close();
            return true;
        } else {
            c.close();
            return false;
        }
    }

    public static List<Clas> selectClas(SQLiteDatabase sqliteDatabase) {
        Cursor c = sqliteDatabase.rawQuery("select * from class", null);
        List<Clas> cList = new ArrayList<>();
        c.moveToFirst();
        while (!c.isAfterLast()) {
            Clas clas = new Clas();
            clas.setClassId(c.getInt(c.getColumnIndex("ClassId")));
            clas.setClassName(c.getString(c.getColumnIndex("ClassName")));
            cList.add(clas);
            c.moveToNext();
        }
        c.close();
        return cList;
    }

    public static List<Integer> getSubjectGroupIds(SQLiteDatabase sqliteDatabase, int classId) {
        Cursor c = sqliteDatabase.rawQuery("select SubjectGroupIds from class where ClassId = " + classId, null);
        List<Integer> idList = new ArrayList<>();
        String ids = "";
        c.moveToFirst();
        while (!c.isAfterLast()) {
            ids = c.getString(c.getColumnIndex("SubjectGroupIds"));
            c.moveToNext();
        }
        c.close();
        String[] idArray = ids.split("#");
        try {
            for (String id : idArray) {
                idList.add(Integer.parseInt(id));
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return idList;
    }

}
