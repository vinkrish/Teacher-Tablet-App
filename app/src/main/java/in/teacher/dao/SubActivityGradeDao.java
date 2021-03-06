package in.teacher.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import in.teacher.sqlite.GradesClassWise;
import in.teacher.sqlite.SubActivityGrade;
import in.teacher.util.GradeClassWiseSort;

public class SubActivityGradeDao {

    static List<GradesClassWise> gradesClassWiseList = new ArrayList<>();

    private static int getMarkTo(String grade) {
        int markTo = 0;
        for (GradesClassWise gcw : gradesClassWiseList) {
            if (grade.equals(gcw.getGrade())) {
                markTo = gcw.getMarkTo();
                break;
            }
        }
        return markTo;
    }

    public static int getSectionAvg(int classId, long subActivityId, SQLiteDatabase sqliteDatabase) {
        gradesClassWiseList = GradesClassWiseDao.getGradeClassWise(classId, sqliteDatabase);
        Collections.sort(gradesClassWiseList, new GradeClassWiseSort());
        int avg = 0;
        Cursor c = sqliteDatabase.rawQuery("select Grade from subactivitygrade where SubActivityId=" + subActivityId, null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                avg += getMarkTo(c.getString(c.getColumnIndex("Grade")));
                c.moveToNext();
            }
            c.close();
            return avg / c.getCount();
        } else return 0;
    }

    public static String getSubActivityGrade(long subActId, long studentId, int subjectId, SQLiteDatabase sqLiteDatabase) {
        String grade = "";
        Cursor c = sqLiteDatabase.rawQuery("select Grade from subactivitygrade " +
                "where StudentId = " + studentId + " and SubActivityId = " + subActId + " and SubjectId = " + subjectId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            grade = c.getString(c.getColumnIndex("Grade"));
            c.moveToNext();
        }
        c.close();
        return grade;
    }

    public static boolean isAllSubActGradeExist(List<Long> subActIdList, SQLiteDatabase sqliteDatabase) {
        int i;
        boolean exist = true;
        for (Long subActId : subActIdList) {
            Cursor c = sqliteDatabase.rawQuery("select count(*) as count from subactivitygrade where SubActivityId=" + subActId, null);
            c.moveToFirst();
            i = c.getInt(c.getColumnIndex("count"));
            if (i == 0) exist = false;
            c.close();
        }
        return exist;
    }

    public static boolean isSubActGradeExist(List<Long> subActIdList, SQLiteDatabase sqliteDatabase) {
        boolean exist = false;
        for (Long subActId : subActIdList) {
            Cursor c = sqliteDatabase.rawQuery("select Grade from subactivitygrade where SubActivityId=" + subActId + " LIMIT 1", null);
            if (c.getCount()>0) {
                exist = true;
            }
            c.close();
        }
        return exist;
    }

    public static int isThereSubActGrade(long subActId, int subjectId, SQLiteDatabase sqliteDatabase) {
        int isThere = 0;
        Cursor c = sqliteDatabase.rawQuery("select * from subactivitygrade where SubActivityId=" + subActId + " and SubjectId=" + subjectId + " LIMIT 1", null);
        if (c.getCount() > 0) {
            isThere = 1;
        }
        c.close();
        return isThere;
    }

    public static void insertSubActivityGrade(List<SubActivityGrade> mList, SQLiteDatabase sqliteDatabase) {
        for (SubActivityGrade m : mList) {
            String sql = "insert into subactivitygrade(SchoolId, ExamId, SubjectId, StudentId, ActivityId, SubActivityId, Grade) values(" +
                    m.getSchoolId() + "," + m.getExamId() + "," + m.getSubjectId() + "," + m.getStudentId() + "," + m.getActivityId() + "," + m.getSubActivityId() + ",'" + m.getGrade() + "')";
            try {
                sqliteDatabase.execSQL(sql);
                ContentValues cv = new ContentValues();
                cv.put("Query", sql);
                sqliteDatabase.insert("uploadsql", null, cv);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateSubActivityGrade(List<SubActivityGrade> amList, SQLiteDatabase sqliteDatabase) {
        for (SubActivityGrade am : amList) {
            String sql = "update subactivitygrade set Grade='" + am.getGrade() + "' where SubActivityId=" + am.getSubActivityId() + " and StudentId=" + am.getStudentId() + " and ExamId=" + am.getExamId()
                    + " and ActivityId=" + am.getActivityId() + " and SubjectId=" + am.getSubjectId();
            try {
                sqliteDatabase.execSQL(sql);
                ContentValues cv = new ContentValues();
                cv.put("Query", sql);
                sqliteDatabase.insert("uploadsql", null, cv);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<String> selectSubActivityGrade(long subActivityId, List<Long> studentId, SQLiteDatabase sqliteDatabase) {
        List<String> aList = new ArrayList<>();
        for (Long i : studentId) {
            Cursor c = sqliteDatabase.rawQuery("select Grade from subactivitygrade where SubActivityId=" + subActivityId + " and StudentId=" + i, null);
            c.moveToFirst();
            if (c.getCount() > 0) {
                aList.add(c.getString(c.getColumnIndex("Grade")));
            } else {
                aList.add("");
            }
            c.close();
        }
        return aList;
    }

    public static void insertUpdateSubActGrade(List<SubActivityGrade> amList, SQLiteDatabase sqliteDatabase) {
        for (SubActivityGrade am : amList) {
            Cursor c = sqliteDatabase.rawQuery("select Grade from subactivitygrade where SubActivityId=" + am.getSubActivityId() + " and StudentId=" + am.getStudentId(), null);
            c.moveToFirst();
            if (c.getCount() > 0) {
                String sql = "update subactivitygrade set Grade='" + am.getGrade() + "' where SubActivityId=" + am.getSubActivityId() + " and StudentId=" + am.getStudentId() + " and ExamId=" + am.getExamId()
                        + " and ActivityId=" + am.getActivityId() + " and SubjectId=" + am.getSubjectId();
                try {
                    sqliteDatabase.execSQL(sql);
                    ContentValues cv = new ContentValues();
                    cv.put("Query", sql);
                    sqliteDatabase.insert("uploadsql", null, cv);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                String sql = "insert into subactivitygrade(SchoolId, ExamId, SubjectId, StudentId, ActivityId, SubActivityId, Grade) values(" +
                        am.getSchoolId() + "," + am.getExamId() + "," + am.getSubjectId() + "," + am.getStudentId() + "," + am.getActivityId() + "," + am.getSubActivityId() + ",'" + am.getGrade() + "')";
                try {
                    sqliteDatabase.execSQL(sql);
                    ContentValues cv = new ContentValues();
                    cv.put("Query", sql);
                    sqliteDatabase.insert("uploadsql", null, cv);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            c.close();
        }
    }

    public static int getSubActGradeCount(long subActivityId, SQLiteDatabase sqliteDatabase) {
        int count = 0;
        Cursor c = sqliteDatabase.rawQuery("select count(*) as count from subactivitygrade where SubActivityId=" + subActivityId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            count = c.getInt(c.getColumnIndex("count"));
            c.moveToNext();
        }
        c.close();
        return count;
    }

}
