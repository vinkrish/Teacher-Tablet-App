package in.teacher.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SubjectExamsDao {
	
	public static float getExmMaxMark(int classId,int examId, int subjectId, SQLiteDatabase sqliteDatabase){
		float maxMark = 100;
		Cursor c = sqliteDatabase.rawQuery("select MaximumMark from subjectexams where ClassId="+classId+" and ExamId="+examId+
				" and SubjectId="+subjectId, null);
		c.moveToFirst();
		while(!c.isAfterLast()){
			maxMark = c.getFloat(c.getColumnIndex("MaximumMark"));
			c.moveToNext();
		}
		c.close();
		return maxMark;
	}
	
	public static String selectSubjectName(int subjectId, SQLiteDatabase sqliteDatabase){
		String subjectName = null;
		Cursor c = sqliteDatabase.rawQuery("select * from subjects where SubjectId="+subjectId, null);
		c.moveToFirst();
		while(!c.isAfterLast()){
			subjectName = c.getString(c.getColumnIndex("SubjectName"));
			c.moveToNext();
		}
		c.close();
		return subjectName;
	}
	
	public static int isExmMaxMarkDefined(int classId,int examId, int subjectId, SQLiteDatabase sqliteDatabase){
		int isDefined = 0;
		Cursor c = sqliteDatabase.rawQuery("select MaximumMark from subjectexams where ClassId="+classId+" and ExamId="+examId+
				" and SubjectId="+subjectId, null);
		if(c.getCount()>0){
			isDefined = 1;
		}
		c.close();
		return isDefined;
	}
	
}
