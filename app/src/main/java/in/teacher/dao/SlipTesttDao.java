package in.teacher.dao;

import in.teacher.sqlite.SlipTestt;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SlipTesttDao {
	
	public static void deleteSlipTest(long slipTestId, int schoolId, SQLiteDatabase sqliteDatabase){
		String sql = "delete from sliptest where SlipTestId="+slipTestId;
		String sql2 = "delete from sliptestmark_"+schoolId+" where SlipTestId="+slipTestId;
		sqliteDatabase.execSQL(sql);
		sqliteDatabase.execSQL(sql2);
		ContentValues cv = new ContentValues();
		cv.put("Query", sql);
		sqliteDatabase.insert("uploadsql", null, cv);
		ContentValues cv2 = new ContentValues();
		cv2.put("Query", sql2);
		sqliteDatabase.insert("uploadsql", null, cv2);
	}
	
	public static List<SlipTestt> selectSlipTest(int sectionId, int subjectId, SQLiteDatabase sqliteDatabase){
		Cursor c = sqliteDatabase.rawQuery("select * from sliptest where SectionId="+sectionId+" and SubjectId="+subjectId, null);
		List<SlipTestt> stList = new ArrayList<SlipTestt>();
		c.moveToFirst();
		while(!c.isAfterLast()){
			SlipTestt st = new SlipTestt();
			st.setAverageMark(c.getDouble(c.getColumnIndex("AverageMark")));
			st.setClassId(c.getInt(c.getColumnIndex("ClassId")));
			st.setMarkEntered(c.getInt(c.getColumnIndex("MarkEntered")));
			st.setMaximumMark(c.getInt(c.getColumnIndex("MaximumMark")));
			st.setPortion(c.getString(c.getColumnIndex("Portion")));
			st.setExtraPortion(c.getString(c.getColumnIndex("ExtraPortion")));
			st.setPortionName(c.getString(c.getColumnIndex("PortionName")));
			st.setSchoolId(c.getInt(c.getColumnIndex("SchoolId")));
			st.setSectionId(c.getInt(c.getColumnIndex("SectionId")));
			st.setSlipTestId(c.getLong(c.getColumnIndex("SlipTestId")));
			st.setSubjectId(c.getInt(c.getColumnIndex("SubjectId")));
			st.setTestDate(c.getString(c.getColumnIndex("TestDate")));
			stList.add(st);
			c.moveToNext();
		}
		c.close();
		return stList;
	}
	
	public static void deleteSlipTest(SQLiteDatabase sqliteDatabase){
		sqliteDatabase.execSQL("delete from sliptest where SlipTestId=-1");
	}

	public static String selectSlipTestName(long slipTestId, SQLiteDatabase sqliteDatabase){
		String s = null;
		Cursor c = sqliteDatabase.rawQuery("select * from sliptest where SlipTestId="+slipTestId, null);
		c.moveToFirst();
		while(!c.isAfterLast()){
			s = c.getString(c.getColumnIndex("PortionName"));
			c.moveToNext();
		}
		c.close();
		return s;
	}

	public static SlipTestt selectSlipTest(SQLiteDatabase sqliteDatabase){
		Cursor c = sqliteDatabase.rawQuery("SELECT * FROM sliptest where SlipTestId=-1", null);
		c.moveToFirst();
		SlipTestt st = new SlipTestt();
		st.setSchoolId(c.getInt(c.getColumnIndex("SchoolId")));
		st.setClassId(c.getInt(c.getColumnIndex("ClassId")));
		st.setMaximumMark(c.getInt(c.getColumnIndex("MaximumMark")));
		st.setPortion(c.getString(c.getColumnIndex("Portion")));
		st.setExtraPortion(c.getString(c.getColumnIndex("ExtraPortion")));
		st.setPortionName(c.getString(c.getColumnIndex("PortionName")));
		st.setSectionId(c.getInt(c.getColumnIndex("SectionId")));
		st.setSlipTestName(c.getString(c.getColumnIndex("SlipTestName")));
		st.setSubjectId(c.getInt(c.getColumnIndex("SubjectId")));
		st.setTestDate(c.getString(c.getColumnIndex("TestDate")));
		st.setSubmissionDate(c.getString(c.getColumnIndex("SubmissionDate")));
		c.close();
		return st;
	}
	
	public static SlipTestt selectSlipTest(long slipTestId, SQLiteDatabase sqliteDatabase){
		Cursor c = sqliteDatabase.rawQuery("select * from sliptest where SlipTestId="+slipTestId, null);
		SlipTestt st = new SlipTestt();
		c.moveToFirst();
		while(!c.isAfterLast()){
			st.setAverageMark(c.getDouble(c.getColumnIndex("AverageMark")));
			st.setClassId(c.getInt(c.getColumnIndex("ClassId")));
			st.setMarkEntered(c.getInt(c.getColumnIndex("MarkEntered")));
			st.setMaximumMark(c.getInt(c.getColumnIndex("MaximumMark")));
			st.setPortion(c.getString(c.getColumnIndex("Portion")));
			st.setExtraPortion(c.getString(c.getColumnIndex("ExtraPortion")));
			st.setPortionName(c.getString(c.getColumnIndex("PortionName")));
			st.setSchoolId(c.getInt(c.getColumnIndex("SchoolId")));
			st.setSectionId(c.getInt(c.getColumnIndex("SectionId")));
			st.setSlipTestId(c.getLong(c.getColumnIndex("SlipTestId")));
			st.setSubjectId(c.getInt(c.getColumnIndex("SubjectId")));
			st.setTestDate(c.getString(c.getColumnIndex("TestDate")));
			c.moveToNext();
		}
		c.close();
		return st;
	}
	
	public static float selectSlipTestMaxMark(long slipTestId, SQLiteDatabase sqliteDatabase){
		float maxMark = 0;
		Cursor c = sqliteDatabase.rawQuery("select * from sliptest where SlipTestId="+slipTestId, null);
		c.moveToFirst();
		while(!c.isAfterLast()){
			maxMark = c.getFloat(c.getColumnIndex("MaximumMark"));
			c.moveToNext();
		}
		c.close();
		return maxMark;
	}
	
	public static void editSlipTest(SlipTestt st, SQLiteDatabase sqliteDatabase){
		String sql = "update sliptest set Portion='"+st.getPortion()+"',ExtraPortion='"+st.getExtraPortion()+"',MaximumMark="+st.getMaximumMark()+",TestDate='"+st.getTestDate()
				+ "',PortionName='"+st.getPortionName()+"' where SlipTestId="+st.getSlipTestId();
		sqliteDatabase.execSQL(sql);
		ContentValues cv = new ContentValues();
		cv.put("Query", sql);
		sqliteDatabase.insert("uploadsql", null, cv);
	}
	
	public static void insertST2(SlipTestt st, SQLiteDatabase sqliteDatabase){
		String sql = "insert into sliptest(SlipTestId,SchoolId,ClassId,SectionId,SubjectId,Portion,ExtraPortion,PortionName,MaximumMark,AverageMark,TestDate,SubmissionDate,MarkEntered)"
				+ " values("+st.getSlipTestId()+","+st.getSchoolId()+","+st.getClassId()+","+st.getSectionId()+","+st.getSubjectId()+","+st.getPortion()+",'"
				+ st.getExtraPortion()+"','"+st.getPortionName()+"',"+st.getMaximumMark()+","+st.getAverageMark()+",'"+st.getTestDate()+"','"+st.getSubmissionDate()+"',1)";
		ContentValues cv = new ContentValues();
		cv.put("Query", sql);
		sqliteDatabase.insert("uploadsql", null, cv);
	}
	
	public static void insertST(SlipTestt st, SQLiteDatabase sqliteDatabase){
		String sql = "insert into sliptest(SlipTestId,SchoolId,ClassId,SectionId,SubjectId,Portion,ExtraPortion,PortionName,MaximumMark,AverageMark,TestDate,MarkEntered)"
				+ " values("+st.getSlipTestId()+","+st.getSchoolId()+","+st.getClassId()+","+st.getSectionId()+","+st.getSubjectId()+","+st.getPortion()+",'"
				+ st.getExtraPortion()+"','"+st.getPortionName()+"',"+st.getMaximumMark()+","+st.getAverageMark()+",'"+st.getTestDate()+"',1)";
		sqliteDatabase.execSQL(sql);
	}
	
}
