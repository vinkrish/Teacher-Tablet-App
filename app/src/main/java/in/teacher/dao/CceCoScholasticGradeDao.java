package in.teacher.dao;

import in.teacher.sqlite.CceCoScholasticGrade;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CceCoScholasticGradeDao {
	
	public static int isThereCoSchGrade(int aspectId, int term, int sectionId, SQLiteDatabase sqliteDatabase){
		int isThere = 0;
		Cursor c = sqliteDatabase.rawQuery("select * from ccecoscholasticgrade where AspectId="+aspectId+" and Term="+term+" and SectionId="+sectionId, null);
		if(c.getCount()>0){
			isThere = 1;
		}
		c.close();
		return isThere;
	}
	
	public static List<Integer> selectCCECoSchGrade(int sectionId, int aspectId, SQLiteDatabase sqliteDatabase){
		List<Integer> gradeList = new ArrayList<Integer>();
		Cursor c = sqliteDatabase.rawQuery("select Grade,Description from ccecoscholasticgrade where AspectId="+aspectId+" and StudentId in (select * from students where SectionId="+sectionId+" order by RollNoInClass)", null);
		c.moveToFirst();
		while(!c.isAfterLast()){
			gradeList.add(c.getInt(c.getColumnIndex("Grade")));
			c.moveToNext();
		}
		c.close();
		return gradeList;
	}
	
	public static void insertCoSchGrade(List<CceCoScholasticGrade> cceList, SQLiteDatabase sqliteDatabase){
		for(CceCoScholasticGrade cce: cceList){
			String sql = "insert into ccecoscholasticgrade (SchoolId, ClassId, SectionId, StudentId, Type, Term, TopicId, AspectId," +
					"Grade, Description) values("+cce.getSchoolId()+","+cce.getClassId()+","+cce.getSectionId()+"," +
					cce.getStudentId()+","+cce.getType()+","+cce.getTerm()+","+cce.getTopicId()+","+cce.getAspectId()+"," +
					cce.getGrade()+",'"+cce.getDescription()+"')";
			sqliteDatabase.execSQL(sql);
			ContentValues cv = new ContentValues();
			cv.put("Query", sql);
			sqliteDatabase.insert("uploadsql", null, cv);
		}
	}
	
}
