package in.teacher.dao;

import in.teacher.sqlite.SubActivityMark;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class SubActivityMarkDao {

	public static int getSubActMarksCount(int subActivityId, SQLiteDatabase sqliteDatabase){
		int count = 0;
		Cursor c = sqliteDatabase.rawQuery("select count(*) as count from subactivitymark where SubActivityId="+subActivityId, null);
		c.moveToFirst();
		while(!c.isAfterLast()){
			count = c.getInt(c.getColumnIndex("count"));
			c.moveToNext();
		}
		c.close();
		return count;
	}

	public static boolean isAllSubActMarkExist(List<Integer> subActIdList, SQLiteDatabase sqliteDatabase){
		int i = 1;
		boolean exist = true;
		for(Integer subActId: subActIdList){
			Cursor c = sqliteDatabase.rawQuery("select count(*) as count from subactivitymark where SubActivityId="+subActId, null);
			c.moveToFirst();
			i = c.getInt(c.getColumnIndex("count"));
			if(i==0){
				exist = false;
			}
			c.close();
		}
		return exist;
	}

	public static int isThereSubActMark(int subActId, int subjectId, SQLiteDatabase sqliteDatabase){
		int isThere = 0;
		Cursor c = sqliteDatabase.rawQuery("select * from subactivitymark where SubActivityId="+subActId+" and SubjectId="+subjectId+" LIMIT 1", null);
		if(c.getCount()>0){
			isThere = 1;
		}
		c.close();
		return isThere;
	}

	public static List<String> selectSubActivityMarc(int subActivityId, List<Integer> studentId, SQLiteDatabase sqliteDatabase){
		List<String> aList = new ArrayList<String>();
		for(Integer i: studentId){
			Cursor c = sqliteDatabase.rawQuery("select * from subactivitymark where SubActivityId="+subActivityId+" and StudentId="+i, null);	
			c.moveToFirst();
			if(c.getCount()>0){
				aList.add(c.getString(c.getColumnIndex("Mark")));
			}else{
				aList.add("");
			}
			c.close();		
		}
		return aList;
	}

	public static void updateSubActivityMark(List<SubActivityMark> amList, SQLiteDatabase sqliteDatabase){
		for(SubActivityMark am: amList){
			String sql = "update subactivitymark set Mark='"+am.getMark()+"' where SubActivityId="+am.getSubActivityId()+" and StudentId="+am.getStudentId()+" and ExamId="+am.getExamId()
					+" and ActivityId="+am.getActivityId()+" and SubjectId="+am.getSubjectId();
			try{
				sqliteDatabase.execSQL(sql);
			}catch(SQLException e){}
			ContentValues cv = new ContentValues();
			cv.put("Query", sql);
			sqliteDatabase.insert("uploadsql", null, cv);
		}
	}

	public static void insertUpdateSubActMark(List<SubActivityMark> amList, SQLiteDatabase sqliteDatabase){
		for(SubActivityMark am: amList){
			Cursor c = sqliteDatabase.rawQuery("select * from subactivitymark where SubActivityId="+am.getSubActivityId()+" and StudentId="+am.getStudentId(), null);	
			c.moveToFirst();
			if(c.getCount()>0){
				String sql = "update subactivitymark set Mark='"+am.getMark()+"' where SubActivityId="+am.getSubActivityId()+" and StudentId="+am.getStudentId()+" and ExamId="+am.getExamId()
						+" and ActivityId="+am.getActivityId()+" and SubjectId="+am.getSubjectId();
				try{
					sqliteDatabase.execSQL(sql);
				}catch(SQLException e){}
				if(am.getMark().equals("")){
					String sql2 = "update subactivitymark set Mark=NULL where SubActivityId="+am.getSubActivityId()+" and StudentId="+am.getStudentId()+" and ExamId="+am.getExamId()
							+" and ActivityId="+am.getActivityId()+" and SubjectId="+am.getSubjectId();
					ContentValues cv = new ContentValues();
					cv.put("Query", sql2);
					sqliteDatabase.insert("uploadsql", null, cv);
				}else{
					ContentValues cv = new ContentValues();
					cv.put("Query", sql);
					sqliteDatabase.insert("uploadsql", null, cv);
				}
			}else{
				String sql = "insert into subactivitymark(SchoolId, ExamId, SubjectId, StudentId, ActivityId, SubActivityId, Mark) values("+
						am.getSchoolId()+","+am.getExamId()+","+am.getSubjectId()+","+am.getStudentId()+","+am.getActivityId()+","+am.getSubActivityId()+",'"+am.getMark()+"')";
				try{
					sqliteDatabase.execSQL(sql);
				}catch(SQLException e){}
				if(am.getMark().equals("")){
					String sql2 = "insert into subactivitymark(SchoolId, ExamId, SubjectId, StudentId, ActivityId, SubActivityId, Mark) values("+
							am.getSchoolId()+","+am.getExamId()+","+am.getSubjectId()+","+am.getStudentId()+","+am.getActivityId()+","+am.getSubActivityId()+",NULL)";
					ContentValues cv = new ContentValues();
					cv.put("Query", sql2);
					sqliteDatabase.insert("uploadsql", null, cv);
				}else{
					ContentValues cv = new ContentValues();
					cv.put("Query", sql);
					sqliteDatabase.insert("uploadsql", null, cv);
				}
			}
			c.close();	
		}
	}

	public static void insertSubActivityMark(List<SubActivityMark> mList, SQLiteDatabase sqliteDatabase){
		for(SubActivityMark m: mList){
			String sql = "insert into subactivitymark(SchoolId, ExamId, SubjectId, StudentId, ActivityId, SubActivityId, Mark) values("+
					m.getSchoolId()+","+m.getExamId()+","+m.getSubjectId()+","+m.getStudentId()+","+m.getActivityId()+","+m.getSubActivityId()+",'"+m.getMark()+"')";
			try{
				sqliteDatabase.execSQL(sql);
			}catch(SQLException e){}
			ContentValues cv = new ContentValues();
			cv.put("Query", sql);
			sqliteDatabase.insert("uploadsql", null, cv);
		}
	}

}
