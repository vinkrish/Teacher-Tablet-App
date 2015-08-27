package in.teacher.dao;

import in.teacher.sqlite.SlipTestMark;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class SlipTestMarkDao {
	
	public static int findSTMarkEntered(long slipId, int schoolId, SQLiteDatabase sqliteDatabase){
		int count = 0;
		Cursor c = sqliteDatabase.rawQuery("select Mark from sliptestmark_"+schoolId+" where SlipTestId="+slipId, null);
		if(c.getCount()>0){
			count = c.getCount();
		}
		return count;
	}
	
	public static double getSTMaxMark(long slipTestId, int school_Id, SQLiteDatabase sqliteDatabase){
		double maxMark = 0;
		Cursor c = sqliteDatabase.rawQuery("select Mark from sliptestmark_"+school_Id+" where SlipTestId="+slipTestId, null);
		c.moveToFirst();
		while(!c.isAfterLast()){
			try{
				if(Double.parseDouble(c.getString(c.getColumnIndex("Mark")))>maxMark){
					maxMark = Double.parseDouble(c.getString(c.getColumnIndex("Mark")));
				}
			}catch(NullPointerException e){
			}catch(NumberFormatException e){
			}
			c.moveToNext();
		}
		c.close();
		return maxMark;
	}
	
	public static int getSlipTestMarksCount(long slipTestId, int schoolId, SQLiteDatabase sqliteDatabase){
		int count = 0;
		Cursor c = sqliteDatabase.rawQuery("select count(*) as count from sliptestmark_"+schoolId+" where SlipTestId="+slipTestId, null);
		c.moveToFirst();
		while(!c.isAfterLast()){
			count = c.getInt(c.getColumnIndex("count"));
			c.moveToNext();
		}
		c.close();
		return count;
	}
	
	public static List<String> selectSlipTestMark(long slipTestId, List<Integer> studentId, int schoolId, SQLiteDatabase sqliteDatabase){
		List<String> stMarkList = new ArrayList<String>();
		for(Integer i: studentId){
			Cursor c = sqliteDatabase.rawQuery("select * from sliptestmark_"+schoolId+" where SlipTestId="+slipTestId+" and StudentId="+i, null);
			if(c.getCount()>0){
				c.moveToFirst();
				while(!c.isAfterLast()){
					stMarkList.add(c.getString(c.getColumnIndex("Mark")));
					c.moveToNext();
				}
			}else{
				stMarkList.add("");
			}
			c.close();
		}
		return stMarkList;
	}
	
	public static void updateSlipTestMark(List<SlipTestMark> stmList, SQLiteDatabase sqliteDatabase){
		int schoolId = 0;
		long slipTestId = 0;
		for(SlipTestMark stm: stmList){
			schoolId = stm.getSchoolId();
			slipTestId = stm.getSlipTestId();
			String sql="update sliptestmark_"+schoolId+" set Mark='"+stm.getMark()+"' where SlipTestId="+stm.getSlipTestId()+" and StudentId="+stm.getStudentId();
			try{
				sqliteDatabase.execSQL(sql);
			}catch(SQLException e){}
			ContentValues cv = new ContentValues();
			cv.put("Query", sql);
			sqliteDatabase.insert("uploadsql", null, cv);
		}

		double avgMark = 0;
		Cursor c = sqliteDatabase.rawQuery("select AVG(A.Mark) as Average from sliptestmark_"+schoolId+" A, sliptest S where A.SlipTestId="+slipTestId+" and A.SlipTestId=S.SlipTestId "
				+ "and A.Mark!='0' and A.Mark!='-1'", null);
		c.moveToFirst();
		while(!c.isAfterLast()){
			avgMark=c.getDouble(c.getColumnIndex("Average"));
			c.moveToNext();
		}
		String sql3 = "update sliptest set AverageMark="+avgMark+" where SlipTestId="+slipTestId;
		sqliteDatabase.execSQL(sql3);
		ContentValues cv = new ContentValues();
		cv.put("Query", sql3);
		sqliteDatabase.insert("uploadsql", null, cv);
	}
	
	public static void insertUpdateSTMark(List<SlipTestMark> stmList, SQLiteDatabase sqliteDatabase){
		int schoolId = 0;
		long slipTestId = 0;
		for(SlipTestMark stm: stmList){
			schoolId = stm.getSchoolId();
			slipTestId = stm.getSlipTestId();
			Cursor c = sqliteDatabase.rawQuery("select * from sliptestmark_"+schoolId+" where StudentId="+stm.getStudentId(), null);
			if(c.getCount()>0){
				String sql="update sliptestmark_"+schoolId+" set Mark='"+stm.getMark()+"' where SlipTestId="+stm.getSlipTestId()+" and StudentId="+stm.getStudentId();
				try{
					sqliteDatabase.execSQL(sql);
				}catch(SQLException e){}
				ContentValues cv = new ContentValues();
				cv.put("Query", sql);
				sqliteDatabase.insert("uploadsql", null, cv);
			}else{
				String sql = "insert into sliptestmark_"+schoolId+"(SchoolId,ClassId,SectionId,SubjectId,SlipTestId,StudentId,Mark) values("+stm.getSchoolId()+","+stm.getClassId()+","+stm.getSectionId()+","
						+ stm.getSubjectId()+","+stm.getSlipTestId()+","+stm.getStudentId()+",'"+stm.getMark()+"')";
				try{
					sqliteDatabase.execSQL(sql);
				}catch(SQLException e){}
				ContentValues cv = new ContentValues();
				cv.put("Query", sql);
				sqliteDatabase.insert("uploadsql", null, cv);
			}
		}

		double avgMark = 0;
		Cursor c = sqliteDatabase.rawQuery("select AVG(A.Mark) as Average from sliptestmark_"+schoolId+" A, sliptest S where A.SlipTestId="+slipTestId+" and A.SlipTestId=S.SlipTestId "
				+ "and A.Mark!='' and A.Mark!='-1'", null);
		c.moveToFirst();
		while(!c.isAfterLast()){
			avgMark=c.getDouble(c.getColumnIndex("Average"));
			c.moveToNext();
		}
		String sql3 = "update sliptest set AverageMark="+avgMark+" where SlipTestId="+slipTestId;
		sqliteDatabase.execSQL(sql3);
	}
	
	public static void insertSTMark(List<SlipTestMark> stmList, int schoolId, SQLiteDatabase sqliteDatabase){
		long slipTestId = 0;
		for(SlipTestMark stm: stmList){
			slipTestId = stm.getSlipTestId();
			String sql = "insert into sliptestmark_"+schoolId+"(SchoolId,ClassId,SectionId,SubjectId,SlipTestId,StudentId,Mark) values("+stm.getSchoolId()+","+stm.getClassId()+","+stm.getSectionId()+","
					+ stm.getSubjectId()+","+stm.getSlipTestId()+","+stm.getStudentId()+",'"+stm.getMark()+"')";
			try{
				sqliteDatabase.execSQL(sql);
			}catch(SQLException e){}
			ContentValues cv = new ContentValues();
			cv.put("Query", sql);
			sqliteDatabase.insert("uploadsql", null, cv);
		}

		double avgMark = 0;
		Cursor c = sqliteDatabase.rawQuery("select AVG(A.Mark) as Average from sliptestmark_"+schoolId+" A, sliptest S where A.SlipTestId="+slipTestId+" and A.SlipTestId=S.SlipTestId "
				+ "and A.Mark!='-1'", null);
		c.moveToFirst();
		while(!c.isAfterLast()){
			avgMark=c.getDouble(c.getColumnIndex("Average"));
			c.moveToNext();
		}
		String sql3 = "update sliptest set AverageMark="+avgMark+" where SlipTestId="+slipTestId;
		sqliteDatabase.execSQL(sql3);
		ContentValues cv = new ContentValues();
		cv.put("Query", sql3);
		sqliteDatabase.insert("uploadsql", null, cv);
	}
	
	public static void insertStAvg(int classId, int sectionId, int subjectId, SQLiteDatabase sqliteDatabase){
		String sql = "insert into stavg(ClassId, SectionId, SubjectId) values("+classId+","+sectionId+","+subjectId+")";
		try{
			sqliteDatabase.execSQL(sql);
		}catch(SQLException e){}
	}
	
}
