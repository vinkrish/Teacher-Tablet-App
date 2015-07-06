package in.teacher.dao;

import in.teacher.sqlite.CCEStudentProfile;

import java.util.List;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

public class CCEStudentProfileDao {
	
	public static void insertCCEStudentProfile(List<CCEStudentProfile> cspList, SQLiteDatabase sqliteDatabase){
		for(CCEStudentProfile csp: cspList){
			String sql = "insert into ccestudentprofile(SchoolId, ClassId, SectionId, StudentId, StudentName, Term, Height, Weight, DaysAttended1) " +
					" values('"+csp.getSchoolId()+"','"+csp.getClassId()+"','"+csp.getSectionId()+"','"+csp.getStudentId()+"','"+csp.getStudentName()+
					"',"+csp.getTerm()+",'"+csp.getHeight()+"','"+csp.getWeight()+"',"+csp.getDaysAttended1()+")";
			sqliteDatabase.execSQL(sql);
			ContentValues cv = new ContentValues();
			cv.put("Query", sql);
			sqliteDatabase.insert("uploadsql", null, cv);
		}
	}
	
	public static void updateCCEStudentProfile(List<CCEStudentProfile> cspList, SQLiteDatabase sqliteDatabase){	
		for(CCEStudentProfile csp: cspList){
			String sql = "update ccestudentprofile set Height='"+csp.getHeight()+"', Weight='"+csp.getWeight()+"', DaysAttended1="+csp.getDaysAttended1()+
					" where StudentId='"+csp.getStudentId()+"' and Term="+csp.getTerm();
			sqliteDatabase.execSQL(sql);
			ContentValues cv = new ContentValues();
			cv.put("Query", sql);
			sqliteDatabase.insert("uploadsql", null, cv);
		}
	}

}