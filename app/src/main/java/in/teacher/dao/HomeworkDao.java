package in.teacher.dao;

import in.teacher.sqlite.Homework;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

public class HomeworkDao {
	
	public static List<Homework> selectHomework(int sectionId,String homeworkDate, SQLiteDatabase sqliteDatabase){
		Cursor c = sqliteDatabase.rawQuery("select * from homeworkmessage where SectionId='"+sectionId+"' and HomeworkDate='"+homeworkDate+"'", null);
		List<Homework> hList = new ArrayList<>();
		c.moveToLast();
		while(!c.isAfterLast()){
			Homework h = new Homework();
			h.setClassId(c.getString(c.getColumnIndex("ClassId")));
			h.setHomework(c.getString(c.getColumnIndex("Homework")));
			h.setHomeworkDate(c.getString(c.getColumnIndex("HomeworkDate")));
			h.setHomeworkId(c.getLong(c.getColumnIndex("HomeworkId")));
			h.setMessageFrom(c.getString(c.getColumnIndex("MessageFrom")));
			h.setMessageVia(c.getString(c.getColumnIndex("MessageVia")));
			h.setSchoolId(c.getString(c.getColumnIndex("SchoolId")));
			h.setSectionId(c.getString(c.getColumnIndex("SectionId")));
			h.setSubjectIDs(c.getString(c.getColumnIndex("SubjectIDs")));
			h.setTeacherId(c.getString(c.getColumnIndex("TeacherId")));
			hList.add(h);
			c.moveToNext();
		}
		c.close();
		return hList;
	}
	
	public static void deleteHomework(long id, SQLiteDatabase sqliteDatabase){
		sqliteDatabase.delete("homeworkmessage", "HomeworkId="+id, null);
	}
	
	public static int isHwPresent(int sectionId, String date, SQLiteDatabase sqliteDatabase){
		int i=0;
		Cursor c = sqliteDatabase.rawQuery("select IsNew from homeworkmessage where SectionId="+sectionId+" and HomeworkDate='"+date+"'", null);
		c.moveToFirst();
		while(!c.isAfterLast()){
			i = c.getInt(c.getColumnIndex("IsNew"));
			c.moveToNext();
		}
		c.close();
		return i;
	}
	
	public static void insertHW(Homework h, SQLiteDatabase sqliteDatabase){
		//	String escape = h.getHomework().replaceAll("['\"]", " ");
		String sql = "insert into homeworkmessage(HomeworkId,SchoolId,ClassId,SectionId,TeacherId,SubjectIDs,Homework,HomeworkDate) values("+ h.getHomeworkId()+","+
				h.getSchoolId()+","+h.getClassId()+","+h.getSectionId()+","+h.getTeacherId()+",'"+h.getSubjectIDs()+"',"+DatabaseUtils.sqlEscapeString(h.getHomework().replaceAll("\n", " "))+",'"+h.getHomeworkDate()+"')";
		sqliteDatabase.execSQL(sql);
	}
	
	public static void deleteHomework(long id, int schoolId, SQLiteDatabase sqliteDatabase){
		String sql = "delete from homeworkmessage where HomeworkId="+id;
		sqliteDatabase.execSQL(sql);
		ContentValues cv = new ContentValues();
		cv.put("SchoolId", schoolId);
		cv.put("Action", "delete");
		cv.put("TableName", "homeworkmessage");
		cv.put("Query", sql);
		sqliteDatabase.insert("uploadsql", null, cv);
	}
	
	public static void insertHwPresent(int sectionId, String date, SQLiteDatabase sqliteDatabase){
		sqliteDatabase.execSQL("update homeworkmessage set IsNew=1 where SectionId="+sectionId+" and HomeworkDate='"+date+"'");
	}
	
	public static void insertHwSql(Homework h, SQLiteDatabase sqliteDatabase){
		//	String escape = h.getHomework().replaceAll("['\"]", " ");
		String sql = "insert into homeworkmessage(HomeworkId,SchoolId,ClassId,SectionId,TeacherId,SubjectIDs,Homework,HomeworkDate,IsNew) values("+ h.getHomeworkId()+","+
				h.getSchoolId()+","+h.getClassId()+","+h.getSectionId()+","+h.getTeacherId()+",'"+h.getSubjectIDs()+"',"+DatabaseUtils.sqlEscapeString(h.getHomework().replaceAll("\n", " "))+",'"+h.getHomeworkDate()+"',1)";
		ContentValues cv = new ContentValues();
		cv.put("Query", sql);
		sqliteDatabase.insert("uploadsql", null, cv);
	}
	
	public static void updateHomework(int id, String s, String subIds, int schoolId, SQLiteDatabase sqliteDatabase){	
		String sql = "update homeworkmessage set Homework='"+s+"',SubjectIDs='"+subIds+"' where HomeworkId="+id;
		sqliteDatabase.execSQL(sql);
		ContentValues cv = new ContentValues();
		cv.put("SchoolId", schoolId);
		cv.put("Action", "update");
		cv.put("TableName", "homeworkmessage");
		cv.put("Query", sql);
		sqliteDatabase.insert("uploadsql", null, cv);
	}
	
}