package in.teacher.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import in.teacher.sqlite.SubActivityMark;

public class SubActivityMarkDao {

    public static int getSectionAvg(long subActivityId, SQLiteDatabase sqliteDatabase) {
        int avg = 0;
        String sql = "SELECT (AVG(Mark)/A.MaximumMark)*100 as Average FROM subactivity A, subactivitymark B WHERE A.SubActivityId = B.SubActivityId" +
                " and B.Mark!='-1' and A.SubActivityId=" + subActivityId;
        Cursor c = sqliteDatabase.rawQuery(sql, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            avg = c.getInt(c.getColumnIndex("Average"));
            c.moveToNext();
        }
        c.close();
        return avg;
    }

    public static int getStudSubActMark(long studentId, long subActivityId, SQLiteDatabase sqliteDatabase) {
        int i = 0;
        Cursor c = sqliteDatabase.rawQuery("select Mark from subactivitymark where StudentId=" + studentId + " and SubActivityId=" + subActivityId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            i = c.getInt(c.getColumnIndex("Mark"));
            c.moveToNext();
        }
        c.close();
        return i;
    }

    public static int getSubActMarksCount(long subActivityId, SQLiteDatabase sqliteDatabase) {
        int count = 0;
        Cursor c = sqliteDatabase.rawQuery("select count(*) as count from subactivitymark where SubActivityId=" + subActivityId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            count = c.getInt(c.getColumnIndex("count"));
            c.moveToNext();
        }
        c.close();
        return count;
    }

    public static boolean isAllSubActMarkExist(List<Long> subActIdList, SQLiteDatabase sqliteDatabase) {
        int i;
        boolean exist = true;
        for (Long subActId : subActIdList) {
            Cursor c = sqliteDatabase.rawQuery("select count(*) as count from subactivitymark where SubActivityId=" + subActId, null);
            c.moveToFirst();
            i = c.getInt(c.getColumnIndex("count"));
            if (i == 0) exist = false;
            c.close();
        }
        return exist;
    }

    public static boolean isSubActMarkExist(List<Long> subActIdList, SQLiteDatabase sqliteDatabase) {
        boolean exist = false;
        for (Long subActId : subActIdList) {
            Cursor c = sqliteDatabase.rawQuery("select Mark from subactivitymark where SubActivityId=" + subActId + " LIMIT 1", null);
            if (c.getCount() > 0) {
                exist = true;
            }
            c.close();
        }
        return exist;
    }

    public static boolean isAllSubActMarkOrGradeExist(List<Long> subActIdList, SQLiteDatabase sqliteDatabase) {
        int i;
        boolean exist = true;
        for (Long subActId : subActIdList) {
            Cursor c = sqliteDatabase.rawQuery("select count(*) as count from subactivitymark where SubActivityId=" + subActId, null);
            c.moveToFirst();
            i = c.getInt(c.getColumnIndex("count"));
            if (i == 0) {
                Cursor c2 = sqliteDatabase.rawQuery("select count(*) as count from subactivitygrade where SubActivityId=" + subActId, null);
                c2.moveToFirst();
                i = c2.getInt(c2.getColumnIndex("count"));
                if (i == 0) exist = false;
                c2.close();
            }
            c.close();
        }
        return exist;
    }

    public static int isThereSubActMark(long subActId, int subjectId, SQLiteDatabase sqliteDatabase) {
        int isThere = 0;
        Cursor c = sqliteDatabase.rawQuery("select * from subactivitymark where SubActivityId=" + subActId + " and SubjectId=" + subjectId + " LIMIT 1", null);
        if (c.getCount() > 0) {
            isThere = 1;
        }
        c.close();
        return isThere;
    }

    public static List<String> selectSubActivityMarc(Long subActivityId, List<Long> studentId, SQLiteDatabase sqliteDatabase) {
        List<String> aList = new ArrayList<>();
        for (Long i : studentId) {
            Cursor c = sqliteDatabase.rawQuery("select Mark from subactivitymark where SubActivityId=" + subActivityId + " and StudentId=" + i, null);
            c.moveToFirst();
            if (c.getCount() > 0) {
                aList.add(c.getString(c.getColumnIndex("Mark")));
            } else {
                aList.add("");
            }
            c.close();
        }
        return aList;
    }

    public static void updateSubActivityMark(List<SubActivityMark> amList, SQLiteDatabase sqliteDatabase) {
        for (SubActivityMark am : amList) {
            String sql = "update subactivitymark set Mark='" + am.getMark() + "' where SubActivityId=" + am.getSubActivityId() + " and StudentId=" + am.getStudentId() + " and ExamId=" + am.getExamId()
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

    public static void insertUpdateSubActMark(List<SubActivityMark> amList, SQLiteDatabase sqliteDatabase) {
        for (SubActivityMark am : amList) {
            Cursor c = sqliteDatabase.rawQuery("select Mark from subactivitymark where SubActivityId=" + am.getSubActivityId() + " and StudentId=" + am.getStudentId(), null);
            c.moveToFirst();
            if (c.getCount() > 0) {
                String sql = "update subactivitymark set Mark='" + am.getMark() + "' where SubActivityId=" + am.getSubActivityId() + " and StudentId=" + am.getStudentId() + " and ExamId=" + am.getExamId()
                        + " and ActivityId=" + am.getActivityId() + " and SubjectId=" + am.getSubjectId();
                try {
                    sqliteDatabase.execSQL(sql);
                } catch (SQLException e) {
                }
                if (am.getMark().equals("")) {
                    String sql2 = "update subactivitymark set Mark=NULL where SubActivityId=" + am.getSubActivityId() + " and StudentId=" + am.getStudentId() + " and ExamId=" + am.getExamId()
                            + " and ActivityId=" + am.getActivityId() + " and SubjectId=" + am.getSubjectId();
                    ContentValues cv = new ContentValues();
                    cv.put("Query", sql2);
                    sqliteDatabase.insert("uploadsql", null, cv);
                } else {
                    ContentValues cv = new ContentValues();
                    cv.put("Query", sql);
                    sqliteDatabase.insert("uploadsql", null, cv);
                }
            } else {
                String sql = "insert into subactivitymark(SchoolId, ExamId, SubjectId, StudentId, ActivityId, SubActivityId, Mark) values(" +
                        am.getSchoolId() + "," + am.getExamId() + "," + am.getSubjectId() + "," + am.getStudentId() + "," + am.getActivityId() + "," + am.getSubActivityId() + ",'" + am.getMark() + "')";
                try {
                    sqliteDatabase.execSQL(sql);
                } catch (SQLException e) {
                }
                if (am.getMark().equals("")) {
                    String sql2 = "insert into subactivitymark(SchoolId, ExamId, SubjectId, StudentId, ActivityId, SubActivityId, Mark) values(" +
                            am.getSchoolId() + "," + am.getExamId() + "," + am.getSubjectId() + "," + am.getStudentId() + "," + am.getActivityId() + "," + am.getSubActivityId() + ",NULL)";
                    ContentValues cv = new ContentValues();
                    cv.put("Query", sql2);
                    sqliteDatabase.insert("uploadsql", null, cv);
                } else {
                    ContentValues cv = new ContentValues();
                    cv.put("Query", sql);
                    sqliteDatabase.insert("uploadsql", null, cv);
                }
            }
            c.close();
        }
    }

    public static void insertSubActivityMark(List<SubActivityMark> mList, SQLiteDatabase sqliteDatabase) {
        for (SubActivityMark m : mList) {
            String sql = "insert into subactivitymark(SchoolId, ExamId, SubjectId, StudentId, ActivityId, SubActivityId, Mark) values(" +
                    m.getSchoolId() + "," + m.getExamId() + "," + m.getSubjectId() + "," + m.getStudentId() + "," + m.getActivityId() + "," + m.getSubActivityId() + ",'" + m.getMark() + "')";
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

    public static int getStudSubActAvg(long studentId, long subActivityId, SQLiteDatabase sqliteDatabase) {
        int i = 0;
        Cursor c = sqliteDatabase.rawQuery("select (Avg(A.Mark)/B.MaximumMark)*100 as avg " +
                "from subactivitymark A, subactivity B " +
                "where A.SubActivityId = B.SubActivityId and B.Mark!='-1' and A.SubActivityId=" + subActivityId +
                " and StudentId=" + studentId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            i = c.getInt(c.getColumnIndex("avg"));
            c.moveToNext();
        }
        c.close();
        return i;
    }

}
