package in.teacher.dao;

import in.teacher.sqlite.Section;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SectionDao {
	
	public static List<Section> selectSection(SQLiteDatabase sqliteDatabase){
		Cursor c = sqliteDatabase.rawQuery("select * from section", null);
		List<Section> sList = new ArrayList<Section>();
		c.moveToFirst();
		while(!c.isAfterLast()){
			Section sec = new Section();
			sec.setSectionId(c.getInt(c.getColumnIndex("SectionId")));
			sec.setClassId(c.getInt(c.getColumnIndex("ClassId")));
			sec.setSectionName(c.getString(c.getColumnIndex("SectionName")));
			sec.setClassTeacherId(c.getInt(c.getColumnIndex("ClassTeacherId")));
			sList.add(sec);
			c.moveToNext();
		}
		c.close();
		return sList;
	}
	
	public static String getSectionName(int secId, SQLiteDatabase sqliteDatabase){
		String s = "";
		Cursor c = sqliteDatabase.rawQuery("select SectionName from section where SectionId="+secId, null);
		c.moveToFirst();
		if(c.getCount()>0){
			s = c.getString(c.getColumnIndex("SectionName"));
		}
		c.close();
		return s;
	}
	
}
