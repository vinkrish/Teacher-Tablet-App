package in.teacher.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import in.teacher.sqlite.ActivityMark;

public class ActivityMarkDao {

    public static int getSectionAvg(long activityId, SQLiteDatabase sqliteDatabase){
        int avg = 0;
        String sql = "SELECT A.ActivityId, (AVG(Mark)/A.MaximumMark)*100 as Average FROM activity A, activitymark B WHERE A.ActivityId = B.ActivityId and A.ActivityId = " + activityId +
                " and B.Mark!='-1'" ;
        Cursor c = sqliteDatabase.rawQuery(sql, null);
        c.moveToFirst();
        while(!c.isAfterLast()){
            avg = c.getInt(c.getColumnIndex("Average"));
            c.moveToNext();
        }
        c.close();
        return avg;
    }

    public static List<String> selectActivityMarc(long activityId, List<Long> studentId, SQLiteDatabase sqliteDatabase) {
        List<String> mList = new ArrayList<>();
        for (Long i : studentId) {
            Cursor c = sqliteDatabase.rawQuery("select Mark from activitymark where ActivityId=" + activityId + " and StudentId=" + i, null);
            c.moveToFirst();
            if (c.getCount() > 0) {
                mList.add(c.getString(c.getColumnIndex("Mark")));
            } else {
                mList.add("");
            }
            c.close();
        }
        return mList;
    }

    public static boolean isAllActMarkExist(List<Long> actIdList, SQLiteDatabase sqliteDatabase) {
        int i;
        boolean exist = true;
        for (Long actId : actIdList) {
            Cursor c = sqliteDatabase.rawQuery("select count(*) as count from activitymark where ActivityId=" + actId, null);
            c.moveToFirst();
            i = c.getInt(c.getColumnIndex("count"));
            if (i == 0) exist = false;
            c.close();
        }
        return exist;
    }

    public static boolean isActMarkExist(List<Long> actIdList, SQLiteDatabase sqliteDatabase) {
        boolean exist = false;
        for (Long actId : actIdList) {
            Cursor c = sqliteDatabase.rawQuery("select Mark from activitymark where ActivityId=" + actId+" LIMIT 1", null);
            if (c.getCount()>0){
                exist = true;
            }
            c.close();
        }
        return exist;
    }

    public static boolean isAllActMarkOrGradeExist (List<Long> actIdList, SQLiteDatabase sqliteDatabase) {
        int i;
        boolean exist = true;
        for (Long actId : actIdList) {
            Cursor c = sqliteDatabase.rawQuery("select count(*) as count from activitymark where ActivityId=" + actId, null);
            c.moveToFirst();
            i = c.getInt(c.getColumnIndex("count"));
            if (i == 0) {
                Cursor c2 = sqliteDatabase.rawQuery("select count(*) as count from activitygrade where ActivityId="+actId, null);
                c2.moveToFirst();
                i = c2.getInt(c2.getColumnIndex("count"));
                if (i == 0) exist = false;
                c2.close();
            }
            c.close();
        }
        return exist;
    }

    public static int getActMarksCount(long activityId, SQLiteDatabase sqliteDatabase) {
        int count = 0;
        Cursor c = sqliteDatabase.rawQuery("select count(*) as count from activitymark where ActivityId=" + activityId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            count = c.getInt(c.getColumnIndex("count"));
            c.moveToNext();
        }
        c.close();
        return count;
    }

    public static void updateActivityMark(List<ActivityMark> amList, SQLiteDatabase sqliteDatabase) {
        for (ActivityMark am : amList) {
            String sql = "update activitymark set Mark='" + am.getMark() + "' where ActivityId=" + am.getActivityId() + " and StudentId=" + am.getStudentId() + " and ExamId=" + am.getExamId() +
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

    public static void insertUpdateActMark(List<ActivityMark> amList, SQLiteDatabase sqliteDatabase) {
        for (ActivityMark am : amList) {
            Cursor c = sqliteDatabase.rawQuery("select Mark from activitymark where ActivityId=" + am.getActivityId() + " and StudentId=" + am.getStudentId(), null);
            c.moveToFirst();
            if (c.getCount() > 0) {
                String sql = "update activitymark set Mark='" + am.getMark() + "' where ActivityId=" + am.getActivityId() + " and StudentId=" + am.getStudentId() + " and ExamId=" + am.getExamId() +
                        " and SubjectId=" + am.getSubjectId();
                try {
                    sqliteDatabase.execSQL(sql);
                } catch (SQLException e) {

                }
                if (am.getMark().equals("")) {
                    String sql2 = "update activitymark set Mark=NULL where ActivityId=" + am.getActivityId() + " and StudentId=" + am.getStudentId() + " and ExamId=" + am.getExamId() +
                            " and SubjectId=" + am.getSubjectId();
                    ContentValues cv = new ContentValues();
                    cv.put("Query", sql2);
                    sqliteDatabase.insert("uploadsql", null, cv);
                } else {
                    ContentValues cv = new ContentValues();
                    cv.put("Query", sql);
                    sqliteDatabase.insert("uploadsql", null, cv);
                }
            } else {
                String sql = "insert into activitymark(SchoolId, ExamId, SubjectId, StudentId, ActivityId, Mark) values(" +
                        am.getSchoolId() + "," + am.getExamId() + "," + am.getSubjectId() + "," + am.getStudentId() + "," + am.getActivityId() + ",'" + am.getMark() + "')";
                try {
                    sqliteDatabase.execSQL(sql);
                } catch (SQLException e) {

                }
                if (am.getMark().equals("")) {
                    String sql2 = "insert into activitymark(SchoolId, ExamId, SubjectId, StudentId, ActivityId, Mark) values(" +
                            am.getSchoolId() + "," + am.getExamId() + "," + am.getSubjectId() + "," + am.getStudentId() + "," + am.getActivityId() + ",NULL)";
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

    public static int getStudActAvg(long studentId, long activityId, SQLiteDatabase sqliteDatabase) {
        int i = 0;
        Cursor c = sqliteDatabase.rawQuery("select (Avg(A.Mark)/B.MaximumMark)*100 as avg from activitymark A, activity B where A.ActivityId=B.ActivityId and A.ActivityId=" + activityId +
                " and StudentId=" + studentId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            i = c.getInt(c.getColumnIndex("avg"));
            c.moveToNext();
        }
        c.close();
        return i;
    }

    public static int getStudActMark(long studentId, long activityId, SQLiteDatabase sqliteDatabase) {
        int i = 0;
        Cursor c = sqliteDatabase.rawQuery("select Mark from activitymark where StudentId=" + studentId + " and ActivityId=" + activityId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            i = c.getInt(c.getColumnIndex("Mark"));
            c.moveToNext();
        }
        c.close();
        return i;
    }

    public static void updateActivityId(long id, SQLiteDatabase sqliteDatabase) {
        sqliteDatabase.execSQL("update temp set ActivityId=" + id);
    }

    public static boolean selectSubActivity(long activityId, SQLiteDatabase sqliteDatabase) {
        boolean bool = false;
        Cursor c = sqliteDatabase.rawQuery("select * from subactivity where ActivityId=" + activityId, null);
        if (c.getCount() > 0) {
            bool = true;
        }
        c.close();
        return bool;
    }

    public static int isThereActMark(long actId, int subjectId, SQLiteDatabase sqliteDatabase) {
        int isThere = 0;
        Cursor c = sqliteDatabase.rawQuery("select * from activitymark where ActivityId=" + actId + " and SubjectId=" + subjectId + " LIMIT 1", null);
        if (c.getCount() > 0) {
            isThere = 1;
        }
        c.close();
        return isThere;
    }

    public static void insertActivityMark(List<ActivityMark> mList, SQLiteDatabase sqliteDatabase) {
        for (ActivityMark m : mList) {
            String sql = "insert into activitymark(SchoolId, ExamId, SubjectId, StudentId, ActivityId, Mark) values(" +
                    m.getSchoolId() + "," + m.getExamId() + "," + m.getSubjectId() + "," + m.getStudentId() + "," + m.getActivityId() + ",'" + m.getMark() + "')";
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

}
