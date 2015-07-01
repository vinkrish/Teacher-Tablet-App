package in.teacher.dao;

import in.teacher.sqlite.Portion;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class PortionDao {
	
	public static List<Portion> selectPortion(int classId, int subjectId, SQLiteDatabase sqliteDatabase){
		Cursor c = sqliteDatabase.rawQuery("select * from portion where ClassId="+classId+" and SubjectId="+subjectId , null);
		List<Portion> pList = new ArrayList<Portion>();
		c.moveToFirst();
		while(!c.isAfterLast()){
			Portion p = new Portion();
			p.setClassId(c.getInt(c.getColumnIndex("ClassId")));
			p.setNewSubjectId(c.getInt(c.getColumnIndex("NewSubjectId")));
			p.setPortion(c.getString(c.getColumnIndex("Portion")));
			p.setPortionId(c.getInt(c.getColumnIndex("PortionId")));
			p.setSchoolId(c.getInt(c.getColumnIndex("SchoolId")));
			p.setSubjectId(c.getInt(c.getColumnIndex("SubjectId")));
			pList.add(p);
			c.moveToNext();
		}
		c.close();
		return pList;
	}
	
}
