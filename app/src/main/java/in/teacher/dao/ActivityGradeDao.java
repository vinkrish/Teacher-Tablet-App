package in.teacher.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import in.teacher.sqlite.ActivityGrade;
import in.teacher.sqlite.GradesClassWise;
import in.teacher.util.GradeClassWiseSort;

public class ActivityGradeDao {

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

    public static int getSectionAvg(int classId, long activityId, SQLiteDatabase sqliteDatabase) {
        gradesClassWiseList = GradesClassWiseDao.getGradeClassWise(classId, sqliteDatabase);
        Collections.sort(gradesClassWiseList, new GradeClassWiseSort());
        int avg = 0;
        Cursor c = sqliteDatabase.rawQuery("select Grade from activitygrade where ActivityId=" + activityId, null);
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

    public static String getActivityGrade(long actId, int studentId, int subjectId, SQLiteDatabase sqLiteDatabase) {
        String grade = "";
        Cursor c = sqLiteDatabase.rawQuery("select Grade from activitygrade " +
                "where StudentId = " + studentId + " and ActivityId = " + actId + " and SubjectId = " + subjectId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            grade = c.getString(c.getColumnIndex("Grade"));
            c.moveToNext();
        }
        c.close();
        return grade;
    }

    public static int isThereActGrade(long actId, int subjectId, SQLiteDatabase sqliteDatabase) {
        int isThere = 0;
        Cursor c = sqliteDatabase.rawQuery("select * from activitygrade where ActivityId=" + actId + " and SubjectId=" + subjectId + " LIMIT 1", null);
        if (c.getCount() > 0) {
            isThere = 1;
        }
        c.close();
        return isThere;
    }

    public static void insertActivityGrade(List<ActivityGrade> mList, SQLiteDatabase sqliteDatabase) {
        for (ActivityGrade m : mList) {
            String sql = "insert into activitygrade(SchoolId, ExamId, SubjectId, StudentId, ActivityId, Grade) values(" +
                    m.getSchoolId() + "," + m.getExamId() + "," + m.getSubjectId() + "," + m.getStudentId() + "," + m.getActivityId() + ",'" + m.getGrade() + "')";
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

    public static void updateActivityGrade(List<ActivityGrade> amList, SQLiteDatabase sqliteDatabase) {
        for (ActivityGrade am : amList) {
            String sql = "update activitygrade set grade='" + am.getGrade() + "' where ActivityId=" + am.getActivityId() + " and StudentId=" + am.getStudentId() + " and ExamId=" + am.getExamId() +
                    " and SubjectId=" + am.getSubjectId();
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

    public static List<String> selectActivityGrade(long activityId, List<Integer> studentId, SQLiteDatabase sqliteDatabase) {
        List<String> mList = new ArrayList<>();
        for (Integer i : studentId) {
            Cursor c = sqliteDatabase.rawQuery("select Grade from activitygrade where ActivityId=" + activityId + " and StudentId=" + i, null);
            c.moveToFirst();
            if (c.getCount() > 0) {
                mList.add(c.getString(c.getColumnIndex("Grade")));
            } else {
                mList.add("");
            }
            c.close();
        }
        return mList;
    }

    public static void insertUpdateActGrade(List<ActivityGrade> amList, SQLiteDatabase sqliteDatabase) {
        for (ActivityGrade am : amList) {
            Cursor c = sqliteDatabase.rawQuery("select Grade from activitygrade where ActivityId=" + am.getActivityId() + " and StudentId=" + am.getStudentId(), null);
            c.moveToFirst();
            if (c.getCount() > 0) {
                String sql = "update activitygrade set Grade='" + am.getGrade() + "' where ActivityId=" + am.getActivityId() + " and StudentId=" + am.getStudentId() + " and ExamId=" + am.getExamId() +
                        " and SubjectId=" + am.getSubjectId();
                try {
                    sqliteDatabase.execSQL(sql);
                    ContentValues cv = new ContentValues();
                    cv.put("Query", sql);
                    sqliteDatabase.insert("uploadsql", null, cv);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                String sql = "insert into activitygrade(SchoolId, ExamId, SubjectId, StudentId, ActivityId, Grade) values(" +
                        am.getSchoolId() + "," + am.getExamId() + "," + am.getSubjectId() + "," + am.getStudentId() + "," + am.getActivityId() + ",'" + am.getGrade() + "')";
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

    public static int getActGradeCount(long activityId, SQLiteDatabase sqliteDatabase) {
        int count = 0;
        Cursor c = sqliteDatabase.rawQuery("select count(*) as count from activitygrade where ActivityId=" + activityId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            count = c.getInt(c.getColumnIndex("count"));
            c.moveToNext();
        }
        c.close();
        return count;
    }

    public static boolean isAllActGradeExist(List<Long> actIdList, SQLiteDatabase sqliteDatabase) {
        int i;
        boolean exist = true;
        for (Long actId : actIdList) {
            Cursor c = sqliteDatabase.rawQuery("select count(*) as count from activitygrade where ActivityId=" + actId, null);
            c.moveToFirst();
            i = c.getInt(c.getColumnIndex("count"));
            if (i == 0) exist = false;
            c.close();
        }
        return exist;
    }
}
