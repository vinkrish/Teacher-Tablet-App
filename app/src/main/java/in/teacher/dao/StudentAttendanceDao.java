package in.teacher.dao;

import in.teacher.sqlite.Students;
import in.teacher.sqlite.TempAttendance;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class StudentAttendanceDao {
	
	public static int isStudentAttendanceMarked(int sectionId, String date, SQLiteDatabase sqliteDatabase){
		int marked=0;
		Cursor c = sqliteDatabase.rawQuery("select * from studentattendance where SectionId="+sectionId+" and DateAttendance='"+date+"'", null);
		if(c.getCount()>0){
			marked = 1;
		}
		c.close();
		return marked;
	}
	
	public static List<Integer> selectStudentIds(String date, int sectionId, SQLiteDatabase sqliteDatabase){
		Cursor c = sqliteDatabase.rawQuery("select StudentId from studentattendance where DateAttendance='"+date+"' and SectionId="+sectionId+" and StudentId!=0  group by StudentId", null);
		List<Integer> sList = new ArrayList<Integer>();
		if(c.getCount()>0){
			c.moveToFirst();
			while(!c.isAfterLast()){
				Integer i = c.getInt(c.getColumnIndex("StudentId"));
				sList.add(i);
				c.moveToNext();
			}
		}
		c.close();
		return sList;
	}
	
	public static int clasMontAbsCnt(String startDate, String endDate, int clasId, SQLiteDatabase sqliteDatabase){
		int cnt = 0;
		String sql = "SELECT count(*) as count FROM studentattendance where DateAttendance>='"+startDate+"' and DateAttendance<='"+endDate+"' and ClassId="+clasId+" and " +
				"TypeOfLeave!='NA'";
		Cursor c = sqliteDatabase.rawQuery(sql, null);
		c.moveToFirst();
		while(!c.isAfterLast()){
			cnt = c.getInt(c.getColumnIndex("count"));
			c.moveToNext();
		}
		c.close();
		return cnt;
	}
	
	public static int studMontAbsCnt(String startDate, String endDate, int studId, SQLiteDatabase sqliteDatabase){
		int cnt = 0;
		String sql = "SELECT count(*) as count FROM studentattendance where DateAttendance>='"+startDate+"' and DateAttendance<='"+endDate+"' and StudentId="+studId;
		Cursor c = sqliteDatabase.rawQuery(sql, null);
		c.moveToFirst();
		while(!c.isAfterLast()){
			cnt = c.getInt(c.getColumnIndex("count"));
			c.moveToNext();
		}
		c.close();
		return cnt;
	}
	
	public static String selectFirstAtt(SQLiteDatabase sqliteDatabase){
		String date = "";
		String sql = "select DateAttendance from studentattendance limit 1";
		Cursor c = sqliteDatabase.rawQuery(sql, null);
		c.moveToFirst();
		while(!c.isAfterLast()){
			date = c.getString(c.getColumnIndex("DateAttendance"));
			c.moveToNext();
		}
		c.close();
		return date;
	}
	
	public static String selectLastAtt(SQLiteDatabase sqliteDatabase){
		String date = null;
		String sql = "select DateAttendance from studentattendance order by rowid desc limit 1";
		Cursor c = sqliteDatabase.rawQuery(sql, null);
		c.moveToFirst();
		while(!c.isAfterLast()){
			date = c.getString(c.getColumnIndex("DateAttendance"));
			c.moveToNext();
		}
		c.close();
		return date;
	}
	
	public static void noAbsentAttendance(int schoolId, int classId, int sectionId, String dateAttendance, SQLiteDatabase sqliteDatabase){
		String sql = "insert into studentattendance(SchoolId, ClassId, SectionId, DateAttendance, TypeOfLeave) values("+
				schoolId+","+classId+","+sectionId+",'"+dateAttendance+"','NA')";
		sqliteDatabase.execSQL(sql);
		ContentValues cv = new ContentValues();
		cv.put("Query", sql);
		sqliteDatabase.insert("uploadsql", null, cv);
	}
	
	public static void insertTempAttendance(TempAttendance ta, SQLiteDatabase sqliteDatabase){
		ContentValues cv = new ContentValues();
		cv.put("StudentId", ta.getStudentId());
		cv.put("ClassId", ta.getClassId());
		cv.put("SectionId", ta.getSectionId());
		cv.put("RollNoInClass", ta.getRollNoInClass());
		cv.put("Name", ta.getName());
		sqliteDatabase.insert("tempattendance", null, cv);
	}
	
	public static List<Students> selectTempAttendance(SQLiteDatabase sqliteDatabase){
		Cursor c = sqliteDatabase.rawQuery("select * from tempattendance", null);
		List<Students> sList = new ArrayList<Students>();
		c.moveToFirst();
		while(!c.isAfterLast()){
			Students s = new Students();
			s.setClassId(c.getInt(c.getColumnIndex("StudentId")));
			s.setClassId(c.getInt(c.getColumnIndex("ClassId")));
			s.setSectionId(c.getInt(c.getColumnIndex("SectionId")));
			s.setStudentId(c.getInt(c.getColumnIndex("StudentId")));
			s.setName(c.getString(c.getColumnIndex("Name")));
			s.setRollNoInClass(c.getInt(c.getColumnIndex("RollNoInClass")));
			sList.add(s);
			c.moveToNext();
		}
		c.close();
		return sList;
	}
}
