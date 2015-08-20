package in.teacher.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import in.teacher.sqlite.GradesClassWise;

public class GradesClassWiseDao {

    public static List<GradesClassWise> getGradeClassWise(int classId, SQLiteDatabase sqliteDatabase){
        List<GradesClassWise> gcwList = new ArrayList<GradesClassWise>();
        Cursor c = sqliteDatabase.rawQuery("select * from gradesclasswise where ClassId="+classId, null);
        c.moveToFirst();
        while(!c.isAfterLast()){
            GradesClassWise gcw = new GradesClassWise();
            gcw.setClassId(c.getInt(c.getColumnIndex("ClassId")));
            gcw.setGrade(c.getString(c.getColumnIndex("Grade")));
            gcw.setMarkFrom(c.getInt(c.getColumnIndex("MarkFrom")));
            gcw.setMarkTo(c.getInt(c.getColumnIndex("MarkTo")));
            gcw.setGradePoint(c.getInt(c.getColumnIndex("GradePoint")));
            gcwList.add(gcw);
            c.moveToNext();
        }
        c.close();
        return gcwList;
    }
}
