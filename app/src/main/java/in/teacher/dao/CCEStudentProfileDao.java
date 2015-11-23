package in.teacher.dao;

import in.teacher.sqlite.CCEStudentProfile;

import java.util.List;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

public class CCEStudentProfileDao {

    public static void insertCCEStudentProfile(Double totalDays, List<CCEStudentProfile> cspList, SQLiteDatabase sqliteDatabase) {
        for (CCEStudentProfile csp : cspList) {
            String sql = "insert into ccestudentprofile(SchoolId, ClassId, SectionId, StudentId, StudentName, Term, Height, Weight, " +
                    "TotalDays1, DaysAttended1, VisionL, VisionR) values('" + csp.getSchoolId() + "','" + csp.getClassId() + "','" +
                    csp.getSectionId() + "','" + csp.getStudentId() + "','" + csp.getStudentName() + "'," + csp.getTerm() + ",'" +
                    csp.getHeight() + "','" + csp.getWeight() + "'," + totalDays + "," + csp.getDaysAttended1() + ",\"" +
                    csp.getVisionL() + "\",\"" + csp.getVisionR() + "\")";
            sqliteDatabase.execSQL(sql);
            ContentValues cv = new ContentValues();
            cv.put("Query", sql);
            sqliteDatabase.insert("uploadsql", null, cv);

            String sql2 = "insert into term_remark(SchoolId, ClassId, SectionId, StudentId, Term, Remark) values(" +
                    csp.getSchoolId() + "," + csp.getClassId() + "," + csp.getSectionId() + "," + csp.getStudentId() + "," +
                    csp.getTerm() + ",\"" + csp.getTermRemark() + "\")";
            sqliteDatabase.execSQL(sql2);
            ContentValues cv2 = new ContentValues();
            cv2.put("Query", sql2);
            sqliteDatabase.insert("uploadsql", null, cv2);
        }
    }

    public static void updateCCEStudentProfile(Double totalDays, List<CCEStudentProfile> cspList, SQLiteDatabase sqliteDatabase) {
        for (CCEStudentProfile csp : cspList) {
            String sql = "update ccestudentprofile set TotalDays1=" + totalDays + ", Height='" + csp.getHeight() + "', Weight='" +
                    csp.getWeight() + "', DaysAttended1=" + csp.getDaysAttended1() + ", VisionL=\"" +
                    csp.getVisionL() + "\", VisionR=\"" + csp.getVisionR() + "\" where StudentId='" + csp.getStudentId() + "' and Term=" + csp.getTerm();
            sqliteDatabase.execSQL(sql);
            ContentValues cv = new ContentValues();
            cv.put("Query", sql);
            sqliteDatabase.insert("uploadsql", null, cv);

            String sql2 = "update term_remark set Remark=\"" + csp.getTermRemark() + "\" where " +
                    "StudentId = " + csp.getStudentId() + " and Term=" + csp.getTerm();
            sqliteDatabase.execSQL(sql2);
            ContentValues cv2 = new ContentValues();
            cv2.put("Query", sql2);
            sqliteDatabase.insert("uploadsql", null, cv2);
        }
    }

}
