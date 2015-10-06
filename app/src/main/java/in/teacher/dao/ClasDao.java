package in.teacher.dao;

import in.teacher.sqlite.Clas;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

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
        for(String id: idArray){
            idList.add(Integer.parseInt(id));
        }
        return idList;
    }

}
