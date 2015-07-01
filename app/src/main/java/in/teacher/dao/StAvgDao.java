package in.teacher.dao;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class StAvgDao {
	
	public static void initStAvg(int classId, int sectionId, int subjectId, int avg, SQLiteDatabase sqliteDatabase){
		String sql = "insert into stavg(ClassId, SectionId, SubjectId, SlipTestAvg) values("+classId+","+sectionId+","+
				subjectId+","+avg+")";
		try{
			sqliteDatabase.execSQL(sql);
		}catch(SQLException e){

		}
	}

	public static int selectStAvg(int sectionId, int subjectId, SQLiteDatabase sqliteDatabase){
		Cursor c = sqliteDatabase.rawQuery("select * from stavg where SectionId="+sectionId+" and SubjectId="+subjectId, null);
		c.moveToFirst();
		int avg = 0;
		while(!c.isAfterLast()){
			avg = c.getInt(c.getColumnIndex("SlipTestAvg"));
			c.moveToNext();
		}
		c.close();
		return avg;
	}
	
	public static void updateSlipTestAvg(int sectionId, int subjectId, int avg, int schoolId, SQLiteDatabase sqliteDatabase){
		String sql = "update stavg set SlipTestAvg="+avg+" where SectionId="+sectionId+" and SubjectId="+subjectId;
		sqliteDatabase.execSQL(sql);
	}

}
