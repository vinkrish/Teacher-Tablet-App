package in.teacher.dao;

import in.teacher.sqlite.CceCoScholasticGrade;
import in.teacher.sqlite.StudentAttendance;
import in.teacher.sqlite.UploadSql;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UploadSqlDao {
	
	public static List<UploadSql> selectUploadSql(SQLiteDatabase sqliteDatabase){
		Cursor c = sqliteDatabase.rawQuery("select * from uploadsql", null);
		List<UploadSql> upList = new ArrayList<>();
		c.moveToFirst();
		while(!c.isAfterLast()){
			UploadSql up = new UploadSql();
			up.setQuery(c.getString(c.getColumnIndex("Query")));
			upList.add(up);
			c.moveToNext();
		}
		c.close();
		return upList;
	}

	public static boolean isUploadSql(SQLiteDatabase sqliteDatabase){
        Cursor c = sqliteDatabase.rawQuery("select * from uploadsql", null);
        if(c.getCount()>0){
            c.close();
            return true;
        }else {
            c.close();
            return false;
        }
    }
	
	public static void insertStudentAttendance(StudentAttendance sa, int sectionId, String date, SQLiteDatabase sqliteDatabase){
		String sql = "insert into studentattendance(SchoolId, ClassId, SectionId, StudentId, DateAttendance, TypeOfLeave) values("+
				sa.getSchoolId()+","+sa.getClassId()+","+sa.getSectionId()+","+sa.getStudentId()+",'"+sa.getDateAttendance()+"','"+sa.getTypeOfLeave()+"')";
		sqliteDatabase.execSQL(sql);

		ContentValues cv = new ContentValues();
		cv.put("Query", sql);
		sqliteDatabase.insert("uploadsql", null, cv);
	}
	
	public static void deleteTable(String tableName, SQLiteDatabase sqliteDatabase){
		sqliteDatabase.execSQL("delete from "+tableName);
	}
	
}
