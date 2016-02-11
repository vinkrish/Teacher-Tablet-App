package in.teacher.dao;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import in.teacher.sqlite.CCEStudentProfile;

public class CCEStudentProfileDao {

    public static void insertCCEStudentProfile(Double totalDays, List<CCEStudentProfile> cspList, SQLiteDatabase sqliteDatabase) {
        for (CCEStudentProfile csp : cspList) {
            try {
                String sql = "insert into ccestudentprofile(SchoolId, ClassId, SectionId, StudentId, StudentName, Term, Height, Weight, " +
                        "TotalDays1, DaysAttended1, VisionL, VisionR) values('" + csp.getSchoolId() + "','" + csp.getClassId() + "','" +
                        csp.getSectionId() + "','" + csp.getStudentId() + "',\"" + csp.getStudentName() + "\"," + csp.getTerm() + ",'" +
                        csp.getHeight() + "','" + csp.getWeight() + "'," + totalDays + "," + csp.getDaysAttended1() + ",\"" +
                        csp.getVisionL() + "\",\"" + csp.getVisionR() + "\")";
                sqliteDatabase.execSQL(sql);
                ContentValues cv = new ContentValues();
                cv.put("Query", sql);
                sqliteDatabase.insert("uploadsql", null, cv);
            } catch (SQLException e){
                e.printStackTrace();
            }

            try {
                String sql2 = "insert into term_remark(SchoolId, ClassId, SectionId, StudentId, Term, Remark) values(" +
                        csp.getSchoolId() + "," + csp.getClassId() + "," + csp.getSectionId() + "," + csp.getStudentId() + "," +
                        csp.getTerm() + ",\"" + csp.getTermRemark() + "\")";
                sqliteDatabase.execSQL(sql2);
                ContentValues cv2 = new ContentValues();
                cv2.put("Query", sql2);
                sqliteDatabase.insert("uploadsql", null, cv2);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateCCEStudentProfile(Double totalDays, List<CCEStudentProfile> cspList, SQLiteDatabase sqliteDatabase) {
        for (CCEStudentProfile csp : cspList) {
            if (csp.isCceExist()) {
                try {
                    String sql = "update ccestudentprofile set TotalDays1=" + totalDays + ", Height='" + csp.getHeight() + "', Weight='" +
                            csp.getWeight() + "', DaysAttended1=" + csp.getDaysAttended1() + ", VisionL=\"" +
                            csp.getVisionL() + "\", VisionR=\"" + csp.getVisionR() + "\" where StudentId='" + csp.getStudentId() + "' and Term=" + csp.getTerm();
                    sqliteDatabase.execSQL(sql);
                    ContentValues cv = new ContentValues();
                    cv.put("Query", sql);
                    sqliteDatabase.insert("uploadsql", null, cv);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                if (csp.isRemarkExist()){
                    try {
                        String sql2 = "update term_remark set Remark=\"" + csp.getTermRemark() + "\" where " +
                                "StudentId = " + csp.getStudentId() + " and Term=" + csp.getTerm();
                        sqliteDatabase.execSQL(sql2);
                        ContentValues cv2 = new ContentValues();
                        cv2.put("Query", sql2);
                        sqliteDatabase.insert("uploadsql", null, cv2);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        String sql2 = "insert into term_remark(SchoolId, ClassId, SectionId, StudentId, Term, Remark) values(" +
                                csp.getSchoolId() + "," + csp.getClassId() + "," + csp.getSectionId() + "," + csp.getStudentId() + "," +
                                csp.getTerm() + ",\"" + csp.getTermRemark() + "\")";
                        sqliteDatabase.execSQL(sql2);
                        ContentValues cv2 = new ContentValues();
                        cv2.put("Query", sql2);
                        sqliteDatabase.insert("uploadsql", null, cv2);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }


            } else {
                try {
                    String sql = "insert into ccestudentprofile(SchoolId, ClassId, SectionId, StudentId, StudentName, Term, Height, Weight, " +
                            "TotalDays1, DaysAttended1, VisionL, VisionR) values('" + csp.getSchoolId() + "','" + csp.getClassId() + "','" +
                            csp.getSectionId() + "','" + csp.getStudentId() + "','" + csp.getStudentName() + "'," + csp.getTerm() + ",'" +
                            csp.getHeight() + "','" + csp.getWeight() + "'," + totalDays + "," + csp.getDaysAttended1() + ",\"" +
                            csp.getVisionL() + "\",\"" + csp.getVisionR() + "\")";
                    sqliteDatabase.execSQL(sql);
                    ContentValues cv = new ContentValues();
                    cv.put("Query", sql);
                    sqliteDatabase.insert("uploadsql", null, cv);
                } catch (SQLException e){
                    e.printStackTrace();
                }

                try {
                    String sql2 = "insert into term_remark(SchoolId, ClassId, SectionId, StudentId, Term, Remark) values(" +
                            csp.getSchoolId() + "," + csp.getClassId() + "," + csp.getSectionId() + "," + csp.getStudentId() + "," +
                            csp.getTerm() + ",\"" + csp.getTermRemark() + "\")";
                    sqliteDatabase.execSQL(sql2);
                    ContentValues cv2 = new ContentValues();
                    cv2.put("Query", sql2);
                    sqliteDatabase.insert("uploadsql", null, cv2);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
