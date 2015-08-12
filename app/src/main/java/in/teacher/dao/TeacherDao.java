package in.teacher.dao;

import in.teacher.sqlite.Teacher;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TeacherDao {

    public static List<Teacher> selectTeacher(SQLiteDatabase sqliteDatabase) {
        Cursor c = sqliteDatabase.rawQuery("select * from teacher", null);
        List<Teacher> tList = new ArrayList<>();
        c.moveToFirst();
        while (!c.isAfterLast()) {
            Teacher t = new Teacher();
            t.setTeacherId(c.getInt(c.getColumnIndex("TeacherId")));
            t.setUsername(c.getString(c.getColumnIndex("Username")));
            t.setPassword(c.getString(c.getColumnIndex("Password")));
            t.setName(c.getString(c.getColumnIndex("Name")));
            t.setMobile(c.getString(c.getColumnIndex("Mobile")));
            t.setTabUser(c.getString(c.getColumnIndex("TabUser")));
            t.setTabPass(c.getString(c.getColumnIndex("TabPass")));
            tList.add(t);
            c.moveToNext();
        }
        c.close();
        return tList;
    }

    public static boolean isTeacherPresent(SQLiteDatabase sqliteDatabase) {
        boolean flag = false;
        Cursor c = sqliteDatabase.rawQuery("select count(*) as count from teacher", null);
        c.moveToFirst();
        if(c.getInt(c.getColumnIndex("count"))>0){
            flag = true;
        }
        c.close();
        return flag;
    }


    public static String selectTeacherName(int teacherId, SQLiteDatabase sqliteDatabase) {
        String s = "";
        Cursor c = sqliteDatabase.rawQuery("select * from teacher where TeacherId=" + teacherId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            s = c.getString(c.getColumnIndex("Name"));
            c.moveToNext();
        }
        c.close();
        return s;
    }

}
