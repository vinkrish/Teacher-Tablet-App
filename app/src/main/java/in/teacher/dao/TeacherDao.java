package in.teacher.dao;

import in.teacher.sqlite.Teacher;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TeacherDao {
	
	public static List<Teacher> selectTeacher(SQLiteDatabase sqliteDatabase){
		Cursor c = sqliteDatabase.rawQuery("select * from teacher", null);
		List<Teacher> tList = new ArrayList<Teacher>();
		c.moveToFirst();
		while(!c.isAfterLast()){
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
	
	public static String selectTeacherName(int teacherId, SQLiteDatabase sqliteDatabase){
		String s = "";
		Cursor c = sqliteDatabase.rawQuery("select * from teacher where TeacherId="+teacherId, null);
		c.moveToFirst();
		while(!c.isAfterLast()){
			s = c.getString(c.getColumnIndex("Name"));
			c.moveToNext();
		}
		c.close();
		return s;
	}
	
}
