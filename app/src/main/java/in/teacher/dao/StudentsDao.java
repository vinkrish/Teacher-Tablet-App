package in.teacher.dao;

import in.teacher.sqlite.Students;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class StudentsDao {
	
	public static int clasTotalStrength(int classId, SQLiteDatabase sqliteDatabase){
		int i=0;
		String sql = "select Count(*) as count from students where ClassId="+classId;
		Cursor c = sqliteDatabase.rawQuery(sql, null);
		c.moveToFirst();
		while(!c.isAfterLast()){
			i = c.getInt(c.getColumnIndex("count"));
			c.moveToNext();
		}
		c.close();
		return i;
	}
	
	public static List<Students> selectAbsentStudents(List<Integer> ids, SQLiteDatabase sqliteDatabase){
		List<Students> sList = new ArrayList<Students>();
		for(Integer id: ids){
			Cursor c = sqliteDatabase.rawQuery("select * from students where StudentId="+id, null);
			c.moveToFirst();
			while(!c.isAfterLast()){
				Students stud = new Students();
				stud.setStudentId(c.getInt(c.getColumnIndex("StudentId")));
				stud.setClassId(c.getInt(c.getColumnIndex("ClassId")));
				stud.setSectionId(c.getInt(c.getColumnIndex("SectionId")));
				stud.setRollNoInClass(c.getInt(c.getColumnIndex("RollNoInClass")));
				stud.setName(c.getString(c.getColumnIndex("Name")));
				sList.add(stud);
				c.moveToNext();
			}
			c.close();
		}
		return sList;
	}
	
	public static List<Students> selectStudents(String sectionid, SQLiteDatabase sqliteDatabase){
		Cursor c = sqliteDatabase.rawQuery("select * from students where SectionId="+sectionid+" order by RollNoInClass", null);
		List<Students> sList = new ArrayList<Students>();
		c.moveToFirst();
		while(!c.isAfterLast()){
			Students stud = new Students();
			stud.setStudentId(c.getInt(c.getColumnIndex("StudentId")));
			stud.setClassId(c.getInt(c.getColumnIndex("ClassId")));
			stud.setSectionId(c.getInt(c.getColumnIndex("SectionId")));
			stud.setRollNoInClass(c.getInt(c.getColumnIndex("RollNoInClass")));
			stud.setName(c.getString(c.getColumnIndex("Name")));
			sList.add(stud);
			c.moveToNext();
		}
		c.close();
		return sList;
	}
	
	public static List<Students> selectStudents2(String sectionId, int subjectId, SQLiteDatabase sqliteDatabase){
		String sql = "select * from students where SectionId="+sectionId+" and SubjectIds LIKE '"+subjectId+"#%' UNION " +
				"select * from students where SectionId="+sectionId+" and  SubjectIds LIKE '%#"+subjectId+"#%' UNION " +
				"select * from students where SectionId="+sectionId+" and SubjectIds LIKE '%#"+subjectId+"' UNION " +
				"select * from students where SectionId="+sectionId+" and SubjectIds='"+subjectId +
				"' order by RollNoInClass";
		Cursor c = sqliteDatabase.rawQuery(sql, null);
		//	Cursor c = sqliteDatabase.rawQuery("select * from students where SectionId="+sectionId+" and SubjectIds REGEXP '(^"+subjectId+"#|#"+subjectId+"#|#"+subjectId+"$)' " +
	//			"order by RollNoInClass", null);
		
		List<Students> sList = new ArrayList<Students>();
		c.moveToFirst();
		while(!c.isAfterLast()){
			Students stud = new Students();
			stud.setStudentId(c.getInt(c.getColumnIndex("StudentId")));
			stud.setClassId(c.getInt(c.getColumnIndex("ClassId")));
			stud.setSectionId(c.getInt(c.getColumnIndex("SectionId")));
			stud.setRollNoInClass(c.getInt(c.getColumnIndex("RollNoInClass")));
			stud.setName(c.getString(c.getColumnIndex("Name")));
			sList.add(stud);
			c.moveToNext();
		}
		c.close();
		return sList;
	}
	
	public static List<Integer> selectStudentIds(String sectionid, SQLiteDatabase sqliteDatabase){
		Cursor c = sqliteDatabase.rawQuery("select RollNoInClass from students where SectionId="+sectionid+" order by RollNoInClass", null);
		List<Integer> sList = new ArrayList<Integer>();
		c.moveToFirst();
		while(!c.isAfterLast()){
			sList.add(c.getInt(c.getColumnIndex("RollNoInClass")));
			c.moveToNext();
		}
		c.close();
		return sList;
	}
	
}