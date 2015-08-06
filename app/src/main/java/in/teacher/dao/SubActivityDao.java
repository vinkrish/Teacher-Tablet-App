package in.teacher.dao;

import in.teacher.sqlite.SubActivity;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class SubActivityDao {

	public static float getSubActMaxMark(int subActivityId, SQLiteDatabase sqliteDatabase){
		float maxMark = 0;
		Cursor c = sqliteDatabase.rawQuery("select MaximumMark from subactivity where SubActivityId="+subActivityId, null);
		c.moveToFirst();
		while(!c.isAfterLast()){
			maxMark = c.getFloat(c.getColumnIndex("MaximumMark"));
			c.moveToNext();
		}
		c.close();
		return maxMark;
	}
	
	public static List<SubActivity> selectSubActivity(int activityId, SQLiteDatabase sqliteDatabase){
		Cursor c = sqliteDatabase.rawQuery("select * from subactivity where ActivityId="+activityId, null);
		List<SubActivity> aList = new ArrayList<>();
		c.moveToFirst();
		while(!c.isAfterLast()){
			SubActivity a = new SubActivity();
			a.setSubActivityId(c.getInt(c.getColumnIndex("SubActivityId")));
			a.setActivityId(c.getInt(c.getColumnIndex("ActivityId")));
			a.setSubActivityName(c.getString(c.getColumnIndex("SubActivityName")));
			a.setCalculation(c.getInt(c.getColumnIndex("Calculation")));
			a.setClassId(c.getInt(c.getColumnIndex("ClassId")));
			a.setExamId(c.getInt(c.getColumnIndex("ExamId")));
			a.setMaximumMark(c.getFloat(c.getColumnIndex("MaximumMark")));
			a.setSectionId(c.getInt(c.getColumnIndex("SectionId")));
			a.setSubjectId(c.getInt(c.getColumnIndex("SubjectId")));
			a.setWeightage(c.getInt(c.getColumnIndex("Weightage")));
			a.setSubActivityAvg(c.getFloat(c.getColumnIndex("SubActivityAvg")));
			a.setCompleteEntry(c.getInt(c.getColumnIndex("CompleteEntry")));
			aList.add(a);
			c.moveToNext();
		}
		c.close();
		return aList;
	}
	
	public static SubActivity getSubActivity(int subActivityId, SQLiteDatabase sqliteDatabase){
		Cursor c = sqliteDatabase.rawQuery("select * from subactivity where SubActivityId="+subActivityId, null);
		SubActivity a = new SubActivity();
		c.moveToFirst();
		while(!c.isAfterLast()){
			a.setSubActivityId(c.getInt(c.getColumnIndex("SubActivityId")));
			a.setActivityId(c.getInt(c.getColumnIndex("ActivityId")));
			a.setSubActivityName(c.getString(c.getColumnIndex("SubActivityName")));
			a.setCalculation(c.getInt(c.getColumnIndex("Calculation")));
			a.setClassId(c.getInt(c.getColumnIndex("ClassId")));
			a.setExamId(c.getInt(c.getColumnIndex("ExamId")));
			a.setMaximumMark(c.getFloat(c.getColumnIndex("MaximumMark")));
			a.setSectionId(c.getInt(c.getColumnIndex("SectionId")));
			a.setSubjectId(c.getInt(c.getColumnIndex("SubjectId")));
			a.setWeightage(c.getInt(c.getColumnIndex("Weightage")));
			a.setSubActivityAvg(c.getFloat(c.getColumnIndex("SubActivityAvg")));
			a.setCompleteEntry(c.getInt(c.getColumnIndex("CompleteEntry")));
			c.moveToNext();
		}
		c.close();
		return a;
	}
	
	public static void checkSubActMarkEmpty(int subActId, int schoolId, SQLiteDatabase sqliteDatabase){
		Cursor c = sqliteDatabase.rawQuery( "SELECT A.SubActivityId, COUNT(*) FROM subactivity A, subactivitymark B WHERE A.SubActivityId=B.SubActivityId AND A.SubActivityId="+subActId+
				" AND B.Mark='0' GROUP BY A.SubActivityId HAVING COUNT(*)>0",null);
		if(c.getCount()>0){
			String sql = "update subactivity set CompleteEntry="+0+" where SubActivityId="+subActId;
			sqliteDatabase.execSQL(sql);
		}else{
			String sql = "update subactivity set CompleteEntry="+1+" where SubActivityId="+subActId;
			sqliteDatabase.execSQL(sql);
		}
		c.close();
	}
	
	public static void updateSubActivityAvg(List<Integer> subActList, SQLiteDatabase sqliteDatabase){
		for(Integer subAct: subActList){
			sqliteDatabase.execSQL("update subactivity set CompleteEntry=1, SubActivityAvg=(SELECT (AVG(Mark)/A.MaximumMark)*360 as Average FROM subactivity A, subactivitymark B WHERE A.SubActivityId = B.SubActivityId and"+
					" B.Mark!='0' and B.Mark!='-1' and A.SubActivityId="+subAct+") where SubActivityId="+subAct);
		}
	}
	
	public static void updateSubActivityAvg(int subActivityId, int schoolId, SQLiteDatabase sqliteDatabase){
		String sql = "SELECT A.SubActivityId, (AVG(Mark)/A.MaximumMark)*360 as Average FROM subactivity A, subactivitymark B WHERE A.SubActivityId = B.SubActivityId "+
				" and B.Mark!='0' and B.Mark!='-1' and A.SubActivityId="+subActivityId+" GROUP BY A.SubActivityId,B.SubActivityId";
		Cursor c = sqliteDatabase.rawQuery(sql,null);
		c.moveToFirst();
		if(c.getCount()>0){
			String sql2 = "update subactivity set SubActivityAvg="+c.getDouble(c.getColumnIndex("Average"))+" where SubActivityId="+subActivityId;
			sqliteDatabase.execSQL(sql2);
		}else{
			String sql2 = "update subactivity set SubActivityAvg=0 where SubActivityId="+subActivityId;
			sqliteDatabase.execSQL(sql2);
		}
		c.close();
	}
	
	public static void updateSubActivityAvg(SQLiteDatabase sqliteDatabase){
		String sql = "SELECT A.SubActivityId, (AVG(Mark)/A.MaximumMark)*360 as Average FROM subactivity A, subactivitymark B, students C WHERE B.StudentId=C.StudentId and A.SubActivityId = B.SubActivityId and " +
				"B.Mark!='0' and B.Mark!='-1' GROUP BY A.SubActivityId,B.SubActivityId";
		Cursor c = sqliteDatabase.rawQuery(sql,null);
		String sql2 = "update subactivity set SubActivityAvg=? where SubActivityId=?";
		sqliteDatabase.beginTransaction();
		SQLiteStatement stmt = sqliteDatabase.compileStatement(sql2);
		c.moveToFirst();
		while(!c.isAfterLast()){
			stmt.bindDouble(1, c.getDouble(c.getColumnIndex("Average")));
			stmt.bindLong(2, c.getInt(c.getColumnIndex("SubActivityId")));
			stmt.execute();
			stmt.clearBindings();
			c.moveToNext();
		}
		c.close();
		sqliteDatabase.setTransactionSuccessful();
		sqliteDatabase.endTransaction();
	}
	
	public static int isThereSubAct(int actId, SQLiteDatabase sqliteDatabase){
		Cursor c = sqliteDatabase.rawQuery("select SubActivityId from subactivity where ActivityId="+actId, null);
		int count = 0;
		if(c.getCount()>0){
			count = 1;
		}
		c.close();
		return count;
	}
	
	public static void checkSubActivityMarkEmpty(SQLiteDatabase sqliteDatabase){
		Cursor c = sqliteDatabase.rawQuery( "SELECT A.SubActivityId, COUNT(*) FROM subactivity A, subactivitymark B WHERE A.SubActivityId=B.SubActivityId"+
				" GROUP BY A.SubActivityId HAVING COUNT(*)>0",null);
		String sql = "update subactivity set CompleteEntry=0 where SubActivityId=?";
		sqliteDatabase.beginTransaction();
		SQLiteStatement stmt = sqliteDatabase.compileStatement(sql);
		c.moveToFirst();
		while(!c.isAfterLast()){
			stmt.bindLong(1, c.getInt(c.getColumnIndex("SubActivityId")));
			stmt.execute();
			stmt.clearBindings();
			c.moveToNext();
		}
		c.close();
		sqliteDatabase.setTransactionSuccessful();
		sqliteDatabase.endTransaction();
	}
	
	public static void checkSubActivityIsMark(SQLiteDatabase sqliteDatabase){
		Cursor c = sqliteDatabase.rawQuery( "SELECT A.SubActivityId, COUNT(*) FROM subactivity A, subactivitymark B WHERE A.SubActivityId=B.SubActivityId"+
				" GROUP BY A.SubActivityId HAVING COUNT(*)>0",null);
		String sql = "update subactivity set CompleteEntry=1 where SubActivityId=?";
		sqliteDatabase.beginTransaction();
		SQLiteStatement stmt = sqliteDatabase.compileStatement(sql);
		c.moveToFirst();
		while(!c.isAfterLast()){
			stmt.bindLong(1, c.getInt(c.getColumnIndex("SubActivityId")));
			stmt.execute();
			stmt.clearBindings();
			c.moveToNext();
		}
		c.close();
		sqliteDatabase.setTransactionSuccessful();
		sqliteDatabase.endTransaction();
	}
	
	public static void checkSubActivityMarkEmpty(List<Integer> subactList, SQLiteDatabase sqliteDatabase){
		StringBuilder sb = new StringBuilder();
		for(Integer subact: subactList){
			sb.append(",").append(subact+"");
		}
		String s = sb.substring(1, sb.length());
		sqliteDatabase.execSQL("update subactivity set CompleteEntry=1 where SubActivityId in ("+s+") and (select count(*) from subactivity A, subactivitymark B WHERE A.SubActivityId=B.SubActivityId"
				+ " AND A.SubActivityId in ("+s+") GROUP BY A.SubActivityId HAVING COUNT(*)>0)");
	}
	
}
