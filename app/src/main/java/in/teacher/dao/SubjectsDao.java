package in.teacher.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SubjectsDao {
	
	public static String getSubjectName(int subjectId, SQLiteDatabase sqliteDatabase){
		Cursor c = sqliteDatabase.rawQuery("select SubjectName from subjects where SubjectId="+subjectId, null);
		c.moveToFirst();
		String s = c.getString(c.getColumnIndex("SubjectName"));
		c.close();
		return s;
	}
	
}
