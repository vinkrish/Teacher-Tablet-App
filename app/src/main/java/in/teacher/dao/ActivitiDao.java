package in.teacher.dao;

import in.teacher.sqlite.Activiti;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

 public class ActivitiDao {
	 
	 public static float getActivityMaxMark(long activityId, SQLiteDatabase sqliteDatabase){
		 float maxMark = 0;
		 Cursor c = sqliteDatabase.rawQuery("select MaximumMark from activity where ActivityId="+activityId, null);
		 c.moveToFirst();
		 while(!c.isAfterLast()){
			 maxMark = c.getFloat(c.getColumnIndex("MaximumMark"));
			 c.moveToNext();
		 }
		 c.close();
		 return maxMark;
	 }
	 
	 public static String selectActivityName(long activityId, SQLiteDatabase sqliteDatabase){
			String s = null;
			Cursor c = sqliteDatabase.rawQuery("select ActivityName from activity where ActivityId="+activityId, null);
			c.moveToFirst();
			while(!c.isAfterLast()){
				s = c.getString(c.getColumnIndex("ActivityName"));
				c.moveToNext();
			}
			c.close();
			return s;
		}
	
	public static List<Activiti> selectActiviti(long examId, int subjectId, int sectionId, SQLiteDatabase sqliteDatabase){
		Cursor c = sqliteDatabase.rawQuery("select * from activity where ExamId="+examId+" and SubjectId="+subjectId+" and SectionId="+sectionId, null);
		List<Activiti> aList = new ArrayList<>();
		c.moveToFirst();
		while(!c.isAfterLast()){
			Activiti a = new Activiti();
			a.setActivityId(c.getLong(c.getColumnIndex("ActivityId")));
			a.setActivityName(c.getString(c.getColumnIndex("ActivityName")));
			a.setCalculation(c.getInt(c.getColumnIndex("Calculation")));
			a.setClassId(c.getInt(c.getColumnIndex("ClassId")));
			a.setExamId(c.getLong(c.getColumnIndex("ExamId")));
			a.setMaximumMark(c.getInt(c.getColumnIndex("MaximumMark")));
			a.setSectionId(c.getInt(c.getColumnIndex("SectionId")));
			a.setSubActivity(c.getInt(c.getColumnIndex("SubActivity")));
			a.setSubjectId(c.getInt(c.getColumnIndex("SubjectId")));
			a.setWeightage(c.getInt(c.getColumnIndex("Weightage")));
			a.setActivityAvg(c.getFloat(c.getColumnIndex("ActivityAvg")));
			a.setCompleteEntry(c.getInt(c.getColumnIndex("CompleteEntry")));
			aList.add(a);
			c.moveToNext();
		}
		c.close();
		return aList;
	}
	
	public static Activiti getActiviti(long activityId, SQLiteDatabase sqliteDatabase){
		Activiti a = new Activiti();
		Cursor c = sqliteDatabase.rawQuery("select * from activity where ActivityId="+activityId, null);
		c.moveToFirst();
		while(!c.isAfterLast()){
			a.setActivityId(c.getLong(c.getColumnIndex("ActivityId")));
			a.setActivityName(c.getString(c.getColumnIndex("ActivityName")));
			a.setCalculation(c.getInt(c.getColumnIndex("Calculation")));
			a.setClassId(c.getInt(c.getColumnIndex("ClassId")));
			a.setExamId(c.getLong(c.getColumnIndex("ExamId")));
			a.setMaximumMark(c.getInt(c.getColumnIndex("MaximumMark")));
			a.setSectionId(c.getInt(c.getColumnIndex("SectionId")));
			a.setSubActivity(c.getInt(c.getColumnIndex("SubActivity")));
			a.setSubjectId(c.getInt(c.getColumnIndex("SubjectId")));
			a.setWeightage(c.getInt(c.getColumnIndex("Weightage")));
			a.setActivityAvg(c.getFloat(c.getColumnIndex("ActivityAvg")));
			a.setCompleteEntry(c.getInt(c.getColumnIndex("CompleteEntry")));
			c.moveToNext();
		}
		c.close();
		return a;
	}
	
	public static void updateActivityAvg(SQLiteDatabase sqliteDatabase){
		String sql = "SELECT A.ActivityId, (AVG(Mark)/A.MaximumMark)*360 as Average FROM activity A, activitymark B , Students C WHERE B.StudentId=C.StudentId and A.ActivityId = B.ActivityId and " +
				"B.Mark!='0' and B.Mark!='-1' GROUP BY A.ActivityId,B.ActivityId";
		Cursor c = sqliteDatabase.rawQuery(sql,null);
		String sql2 = "update activity set ActivityAvg=? where ActivityId=?";
		sqliteDatabase.beginTransaction();
		SQLiteStatement stmt = sqliteDatabase.compileStatement(sql2);
		c.moveToFirst();
		while(!c.isAfterLast()){
			stmt.bindDouble(1, c.getDouble(c.getColumnIndex("Average")));
			stmt.bindLong(2, c.getLong(c.getColumnIndex("ActivityId")));
			stmt.execute();
			stmt.clearBindings();
			c.moveToNext();
		}
		c.close();
		sqliteDatabase.setTransactionSuccessful();
		sqliteDatabase.endTransaction();
	}

	public static void updateActivityAvg(List<Long> actList, SQLiteDatabase sqliteDatabase){
		String sql = "Update activity set CompleteEntry=1,ActivityAvg= (SELECT (AVG(Mark)/A.MaximumMark)*360 as Average FROM activity A, activitymark B WHERE A.ActivityId = "+
				"B.ActivityId and A.ActivityId=? and B.Mark!='0' and B.Mark!='-1') where ActivityId=?";
		sqliteDatabase.beginTransaction();
		SQLiteStatement stmt = sqliteDatabase.compileStatement(sql);
		for(Long act: actList){
			stmt.bindLong(1, act);
			stmt.bindLong(2, act);
			stmt.execute();
			stmt.clearBindings();	
		}
		sqliteDatabase.setTransactionSuccessful();
		sqliteDatabase.endTransaction();
	}
	
	public static void updateSubactActAvg(SQLiteDatabase sqliteDatabase){
		String sql = "SELECT SectionId, ExamId, SubjectId, ActivityId, AVG(SubActivityAvg) as Average FROM subactivity where SubActivityAvg!=0 group by SectionId, ExamId, SubjectId, ActivityId";
		Cursor c = sqliteDatabase.rawQuery(sql,null);
		String sql2 = "update activity set ActivityAvg=? where ActivityId=?";
		sqliteDatabase.beginTransaction();
		SQLiteStatement stmt = sqliteDatabase.compileStatement(sql2);
		c.moveToFirst();
		while(!c.isAfterLast()){
			stmt.bindDouble(1, c.getDouble(c.getColumnIndex("Average")));
			stmt.bindLong(2, c.getLong(c.getColumnIndex("ActivityId")));
			stmt.execute();
			stmt.clearBindings();
			c.moveToNext();
		}
		c.close();
		sqliteDatabase.setTransactionSuccessful();
		sqliteDatabase.endTransaction();
	}

	public static void updateActSubActAvg(List<Long> actList, SQLiteDatabase sqliteDatabase){
		for(Long act: actList){
			String sql = "SELECT A.SectionId, A.ExamId, A.SubjectId, A.ActivityId, AVG(SubActivityAvg) as Average FROM subactivity A where A.ActivityId="+act+
					" and A.SubActivityAvg!=0 group by A.SectionId, A.ExamId, A.SubjectId, A.ActivityId";
			Cursor c = sqliteDatabase.rawQuery(sql, null);
			c.moveToFirst();
			while(!c.isAfterLast()){
				String sql2 = "update activity set ActivityAvg="+c.getInt(c.getColumnIndex("Average"))+" where ActivityId="+c.getLong(c.getColumnIndex("ActivityId"));
				sqliteDatabase.execSQL(sql2);
				c.moveToNext();
			}
			c.close();
		}
	}

	public static void updateSubactActAvg(long actId, SQLiteDatabase sqliteDatabase){
		String sql = "SELECT A.SectionId, A.ExamId, A.SubjectId, A.ActivityId, AVG(SubActivityAvg) as Average FROM subactivity A WHERE A.ActivityId="+actId+" and A.SubActivityAvg!=0";
		Cursor c = sqliteDatabase.rawQuery(sql,null);
		c.moveToFirst();
		if(c.getCount()>0){
			String sql2 = "update activity set ActivityAvg="+c.getInt(c.getColumnIndex("Average"))+" where ActivityId="+actId;
			sqliteDatabase.execSQL(sql2);
		}else{
			String sql2 = "update activity set ActivityAvg=0 where ActivityId="+actId;
			sqliteDatabase.execSQL(sql2);
		}
		c.close();
	}
	
	public static void updateActivityAvg(long activityId, SQLiteDatabase sqliteDatabase){
		String sql = "SELECT A.ActivityId, (AVG(Mark)/A.MaximumMark)*360 as Average FROM activity A, activitymark B WHERE A.ActivityId = B.ActivityId and A.ActivityId = "+activityId+
				" and B.Mark!='0' and B.Mark!='-1' GROUP BY A.ActivityId";
		Cursor c = sqliteDatabase.rawQuery(sql,null);
		c.moveToFirst();
		if(c.getCount()>0){
			String sql2 = "update activity set ActivityAvg="+c.getDouble(c.getColumnIndex("Average"))+" where ActivityId="+activityId;
			sqliteDatabase.execSQL(sql2);
		}else{
			String sql2 = "update activity set ActivityAvg=0 where ActivityId="+activityId;
			sqliteDatabase.execSQL(sql2);
		}
		c.close();
	}
	
	public static int isThereActivity(int sectionId, int subjectId, long examId, SQLiteDatabase sqliteDatabase){
		Cursor c = sqliteDatabase.rawQuery("select ActivityId from activity where ExamId="+examId+" and SubjectId="+subjectId+" and SectionId="+sectionId, null);
		int count=0;
		if(c.getCount()>0){
			count=1;
		}
		c.close();
		return count;
	}
	
	public static void checkActMarkEmpty(long actId, SQLiteDatabase sqliteDatabase){
		Cursor c = sqliteDatabase.rawQuery( "SELECT A.ActivityId, COUNT(*) FROM activity A, activitymark B WHERE A.ActivityId=B.ActivityId AND A.ActivityId="+actId+
				" AND B.Mark='0' GROUP BY A.ActivityId HAVING COUNT(*)>0",null);
		if(c.getCount()>0){
			String sql = "update activity set CompleteEntry="+0+" where ActivityId="+actId;
			sqliteDatabase.execSQL(sql);
		}else{
			String sql = "update activity set CompleteEntry="+1+" where ActivityId="+actId;
			sqliteDatabase.execSQL(sql);
		}
		c.close();
	}

	public static void checkActSubActMarkEmpty(long activityId, SQLiteDatabase sqliteDatabase){
		Cursor c = sqliteDatabase.rawQuery( "SELECT A.ActivityId, COUNT(*) FROM subactivity A, subactivitymark B WHERE A.ActivityId=B.ActivityId"+
				" and B.Mark='0' and A.ActivityId="+activityId+" GROUP BY A.ActivityId HAVING COUNT(*)>0",null);
		if(c.getCount()>0){
			String sql = "update activity set CompleteEntry="+0+" where ActivityId="+activityId;
			sqliteDatabase.execSQL(sql);
		}else{
			String sql = "update activity set CompleteEntry="+1+" where ActivityId="+activityId;
			sqliteDatabase.execSQL(sql);
		}
		c.close();
	}

	
	public static void checkActivityMarkEmpty(List<Long> actList, SQLiteDatabase sqliteDatabase){
		StringBuilder sb = new StringBuilder();
		for(Long act: actList){
			sb.append(",").append(act+"");
		}
		String s = sb.substring(1, sb.length());
		sqliteDatabase.execSQL("update activity set CompleteEntry=1 where ActivityId in ("+s+") and (select count(*) from activity A, activitymark B WHERE A.ActivityId=B.ActivityId"
				+ " AND A.ActivityId in ("+s+") GROUP BY A.ActivityId HAVING COUNT(*)>0)");
	}

}
