package in.teacher.dao;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.util.List;

public class ExmAvgDao {

    public static int selectedExmComplete(int sectionId, int subjectId, long examId, SQLiteDatabase sqliteDatabase) {
        Cursor c = sqliteDatabase.rawQuery("select * from exmavg where SectionId=" + sectionId + " and SubjectId=" + subjectId + " and ExamId=" + examId, null);
        int i = 0;
        c.moveToFirst();
        while (!c.isAfterLast()) {
            i = c.getInt(c.getColumnIndex("CompleteEntry"));
            c.moveToNext();
        }
        c.close();
        return i;
    }

    public static int selectedExmAvg(int sectionId, int subjectId, long examId, SQLiteDatabase sqliteDatabase) {
        Cursor c = sqliteDatabase.rawQuery("select ExamAvg from exmavg where SectionId=" + sectionId + " and SubjectId=" + subjectId + " and ExamId=" + examId, null);
        double i = 0;
        c.moveToFirst();
        while (!c.isAfterLast()) {
            i = c.getDouble(c.getColumnIndex("ExamAvg"));
            c.moveToNext();
        }
        c.close();
        return (int) i;
    }

    public static void insertExmAvg(SQLiteDatabase sqliteDatabase) {
        String sql = "SELECT A.ExamId, A.SubjectId, B.ClassId, B.SectionId, (AVG(Mark)/C.MaximumMark)*360 as Average FROM marks A, students B, subjectexams C WHERE" +
                " A.StudentId = B.StudentId and C.ExamId=A.ExamId and C.SubjectId=A.SubjectId and A.Mark!='-1' GROUP BY A.ExamId, A.SubjectId, B.SectionId";
        Cursor c = sqliteDatabase.rawQuery(sql, null);
        String sql2 = "insert into exmavg (ClassId,SectionId,SubjectId,ExamId,ExamAvg) Values(?,?,?,?,?)";
        sqliteDatabase.beginTransaction();
        SQLiteStatement stmt = sqliteDatabase.compileStatement(sql2);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            stmt.bindLong(1, c.getInt(c.getColumnIndex("ClassId")));
            stmt.bindLong(2, c.getInt(c.getColumnIndex("SectionId")));
            stmt.bindLong(3, c.getInt(c.getColumnIndex("SubjectId")));
            stmt.bindLong(4, c.getLong(c.getColumnIndex("ExamId")));
            stmt.bindDouble(5, c.getDouble(c.getColumnIndex("Average")));
            stmt.execute();
            stmt.clearBindings();
            c.moveToNext();
        }
        sqliteDatabase.setTransactionSuccessful();
        sqliteDatabase.endTransaction();
        c.close();
    }

    public static void insertExmAvg(long examId, int subjectId, SQLiteDatabase sqliteDatabase) {
        String sql = "SELECT A.ExamId, A.SubjectId, B.ClassId, B.SectionId, (AVG(Mark)/C.MaximumMark)*360 as Average FROM marks A, students B, subjectexams C WHERE A.StudentId = B.StudentId" +
                " and A.Mark!='0' and A.Mark!='-1' and A.ExamId=" + examId + " and C.ExamId=A.ExamId and A.SubjectId=" + subjectId + " and C.SubjectId=A.SubjectId GROUP BY A.ExamId, A.SubjectId, B.SectionId";
        Cursor c = sqliteDatabase.rawQuery(sql, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            try {
                String sql1 = "insert into exmavg(ClassId, SectionId, SubjectId, ExamId, ExamAvg, CompleteEntry) values(" + c.getInt(c.getColumnIndex("ClassId")) +
                        "," + c.getInt(c.getColumnIndex("SectionId")) + "," + c.getInt(c.getColumnIndex("SubjectId")) + "," + c.getInt(c.getColumnIndex("ExamId")) +
                        "," + c.getDouble(c.getColumnIndex("Average")) + ",1)";
                sqliteDatabase.execSQL(sql1);
            } catch (SQLException e) {
                String sql2 = "update exmavg set ExamAvg=" + c.getDouble(c.getColumnIndex("Average")) + " where SectionId=" + c.getInt(c.getColumnIndex("SectionId")) +
                        " and SubjectId=" + c.getInt(c.getColumnIndex("SubjectId")) + " and ExamId=" + c.getInt(c.getColumnIndex("ExamId"));
                sqliteDatabase.execSQL(sql2);
            }
            c.moveToNext();
        }
        c.close();
    }

    public static void updateExmAvg(long examId, int subjectId, SQLiteDatabase sqliteDatabase) {
        String sql = "SELECT A.ExamId, A.SubjectId, B.SectionId, (AVG(Mark)/C.MaximumMark)*360 as Average FROM marks A, students B, subjectexams C WHERE A.StudentId=B.StudentId" +
                " and A.Mark!='0' and A.Mark!='-1' and A.ExamId=" + examId + " and C.ExamId=A.ExamId and A.SubjectId=" + subjectId + " and C.SubjectId=A.SubjectId GROUP BY A.ExamId, A.SubjectId, B.SectionId";
        Cursor c = sqliteDatabase.rawQuery(sql, null);
        String sql2 = "update exmavg set ExamAvg=?,CompleteEntry=1 where ExamId=? and SectionId=? and SubjectId=?";
        sqliteDatabase.beginTransaction();
        SQLiteStatement stmt = sqliteDatabase.compileStatement(sql2);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            stmt.bindDouble(1, c.getDouble(c.getColumnIndex("Average")));
            stmt.bindLong(2, c.getLong(c.getColumnIndex("ExamId")));
            stmt.bindLong(3, c.getInt(c.getColumnIndex("SectionId")));
            stmt.bindLong(4, c.getInt(c.getColumnIndex("SubjectId")));
            stmt.execute();
            stmt.clearBindings();
            c.moveToNext();
        }
        sqliteDatabase.setTransactionSuccessful();
        sqliteDatabase.endTransaction();
        c.close();
    }

    public static void insertAvgIntoExmAvg(int sectionId, int subjectId, long examId, SQLiteDatabase sqliteDatabase) {
        String sql = "SELECT A.ExamId, A.SubjectId, B.SectionId, (AVG(Mark)/C.MaximumMark)*360 as Average FROM marks A, students B, subjectexams C WHERE A.StudentId=B.StudentId and A.Mark!='0'" +
                " and A.Mark!='-1' and A.ExamId=" + examId + " and C.ExamId=" + examId + " and C.SubjectId=" + subjectId + " and A.SubjectId=" + subjectId + " and B.SectionId=" + sectionId + " GROUP BY A.ExamId, A.SubjectId, B.SectionId";
        Cursor c = sqliteDatabase.rawQuery(sql, null);
        c.moveToFirst();
        if (c.getCount() > 0) {
            String sql2 = "update exmavg set ExamAvg=" + c.getDouble(c.getColumnIndex("Average")) + " where SectionId=" + sectionId + " and SubjectId=" + subjectId + " and ExamId=" + examId;
            sqliteDatabase.execSQL(sql2);
        } else {
            String sql2 = "update exmavg set ExamAvg=0 where SectionId=" + sectionId + " and SubjectId=" + subjectId + " and ExamId=" + examId;
            sqliteDatabase.execSQL(sql2);
        }
        c.close();
    }

    public static void insertIntoExmAvg(int classId, int sectionId, int subjectId, long examId, SQLiteDatabase sqliteDatabase) {
        try {
            String sql = "insert into exmavg(ClassId, SectionId,SubjectId,ExamId) values(" + classId + "," + sectionId + "," + subjectId + "," + examId + ")";
            sqliteDatabase.execSQL(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int checkExmEntry(int sectionId, int subjectId, long examId, SQLiteDatabase sqliteDatabase) {
        int entry = 0;
        Cursor c = sqliteDatabase.rawQuery("select ExamAvg from exmavg where SectionId=" + sectionId + " and SubjectId=" + subjectId + " and ExamId=" + examId, null);
        if (c.getCount() > 0) {
            entry = 1;
        }
        c.close();
        return entry;
    }

    public static void insertExmActAvg(SQLiteDatabase sqliteDatabase) {
        String sql_query = "Select AB.ExamId,AB.ClassId,AB.SectionId,AB.SubjectId,AVG(B)*360 as Average from (SELECT A.ExamId,B.ClassId,B.SectionId,A.ActivityId,A.SubjectId,C.MaximumMark," +
                "AVG(mark)/C.MaximumMark B FROM activitymark A,students B,activity C WHERE A.StudentId=B.StudentId and A.ActivityId=C.ActivityId and A.Mark!='-1' " +
                "group by A.ExamId,A.ActivityId,A.SubjectId,B.SectionId) AB group by AB.ExamId,AB.SectionId,AB.SubjectId";
        Cursor c = sqliteDatabase.rawQuery(sql_query, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            try {
                String sql1 = "insert into exmavg(ClassId, SectionId, SubjectId, ExamId, ExamAvg) values(" + c.getInt(c.getColumnIndex("ClassId")) + "," +
                        c.getInt(c.getColumnIndex("SectionId")) + "," + c.getInt(c.getColumnIndex("SubjectId")) + "," + c.getLong(c.getColumnIndex("ExamId")) + "," +
                        c.getDouble(c.getColumnIndex("Average")) + ")";
                sqliteDatabase.execSQL(sql1);
            } catch (SQLException e) {
                String sql2 = "update exmavg set ExamAvg=" + c.getDouble(c.getColumnIndex("Average")) + " where ExamId=" + c.getLong(c.getColumnIndex("ExamId")) +
                        " and SectionId=" + c.getInt(c.getColumnIndex("SectionId")) + " and SubjectId=" + c.getInt(c.getColumnIndex("SubjectId"));
                sqliteDatabase.execSQL(sql2);
            }
            c.moveToNext();
        }
        c.close();
    }

    public static void insertExmSubActAvg(SQLiteDatabase sqliteDatabase) {
        String sql_query = "Select AB.ExamId,AB.ClassId,AB.SectionId,AB.SubjectId,AVG(B)*360 as Average from " +
                "(SELECT A.ExamId,B.ClassId,B.SectionId,A.SubActivityId,A.SubjectId,C.MaximumMark,AVG(mark)/C.MaximumMark B FROM " +
                "subactivitymark A,students B,subactivity C WHERE A.StudentId=B.StudentId and A.SubActivityId=C.SubActivityId and A.Mark!='-1' " +
                "group by A.ExamId,A.SubActivityId,A.SubjectId) AB group by AB.ExamId,AB.SectionId,AB.SubjectId";
        Cursor c = sqliteDatabase.rawQuery(sql_query, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            try {
                String sql1 = "insert into exmavg(ClassId, SectionId, SubjectId, ExamId, ExamAvg) values(" + c.getInt(c.getColumnIndex("ClassId")) + "," +
                        c.getInt(c.getColumnIndex("SectionId")) + "," + c.getInt(c.getColumnIndex("SubjectId")) + "," + c.getLong(c.getColumnIndex("ExamId")) + "," +
                        c.getDouble(c.getColumnIndex("Average")) + ")";
                sqliteDatabase.execSQL(sql1);
            } catch (SQLException e) {
                String sql2 = "update exmavg set ExamAvg=" + c.getDouble(c.getColumnIndex("Average")) + " where ExamId=" + c.getLong(c.getColumnIndex("ExamId")) +
                        " and SectionId=" + c.getInt(c.getColumnIndex("SectionId")) + " and SubjectId=" + c.getInt(c.getColumnIndex("SubjectId"));
                sqliteDatabase.execSQL(sql2);
            }
            c.moveToNext();
        }
        c.close();
    }

    public static void updateActExmAvg(int sectionId, int subjectId, long examId, SQLiteDatabase sqliteDatabase) {
        String sql = "SELECT A.SectionId, A.ExamId, A.SubjectId, AVG(ActivityAvg) as Average FROM activity A WHERE A.SectionId=" + sectionId + " and A.SubjectId=" + subjectId + " and A.ExamId=" + examId + " and A.ActivityAvg!=0";
        Cursor c = sqliteDatabase.rawQuery(sql, null);
        c.moveToFirst();
        if (c.getCount() > 0) {
            String sql2 = "update exmavg set ExamAvg=" + c.getDouble(c.getColumnIndex("Average")) + " where SectionId=" + sectionId + " and SubjectId=" + subjectId + " and ExamId=" + examId;
            sqliteDatabase.execSQL(sql2);
        } else {
            String sql2 = "update exmavg set ExamAvg=0 where SectionId=" + sectionId + " and SubjectId=" + subjectId + " and ExamId=" + examId;
            sqliteDatabase.execSQL(sql2);
        }
        c.close();
    }

    public static void insertExmActAvg(List<Long> examIdList, List<Integer> subjectIdList, SQLiteDatabase sqliteDatabase) {
        for (int i = 0, j = examIdList.size(); i < j; i++) {
            String sql_query = "SELECT A.ClassId, A.SectionId, A.ExamId, A.SubjectId, AVG(ActivityAvg) as Average FROM activity A WHERE A.ExamId=" + examIdList.get(i) +
                    " and A.SubjectId=" + subjectIdList.get(i) + " and A.ActivityAvg!=0 group by A.SectionId, A.ExamId, A.SubjectId";
            Cursor c = sqliteDatabase.rawQuery(sql_query, null);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                try {
                    String sql1 = "insert into exmavg(ClassId, SectionId, SubjectId, ExamId, ExamAvg, CompleteEntry) values(" + c.getInt(c.getColumnIndex("ClassId")) +
                            "," + c.getInt(c.getColumnIndex("SectionId")) + "," + c.getInt(c.getColumnIndex("SubjectId")) + "," + c.getLong(c.getColumnIndex("ExamId")) +
                            "," + c.getDouble(c.getColumnIndex("Average")) + ",1)";
                    sqliteDatabase.execSQL(sql1);
                } catch (SQLException e) {
                    String sql2 = "update exmavg set ExamAvg=" + c.getDouble(c.getColumnIndex("Average")) + " where SectionId=" + c.getInt(c.getColumnIndex("SectionId")) +
                            " and SubjectId=" + c.getInt(c.getColumnIndex("SubjectId")) + " and ExamId=" + c.getLong(c.getColumnIndex("ExamId"));
                    sqliteDatabase.execSQL(sql2);
                }
                c.moveToNext();
            }
            c.close();
        }
    }

    public static void updateExmActAvg(List<Long> examIdList, List<Integer> subjectIdList, SQLiteDatabase sqliteDatabase) {
        for (int i = 0, j = examIdList.size(); i < j; i++) {
            String sql = "SELECT A.SectionId, A.ExamId, A.SubjectId, AVG(ActivityAvg) as Average FROM activity A WHERE A.ExamId=" + examIdList.get(i) +
                    " and A.ActivityAvg!=0 and A.SubjectId=" + subjectIdList.get(i) + " group by A.SectionId, A.ExamId, A.SubjectId";
            Cursor c = sqliteDatabase.rawQuery(sql, null);
            String sql2 = "update exmavg set ExamAvg=? where SectionId=? and SubjectId=? and ExamId=?";
            sqliteDatabase.beginTransaction();
            SQLiteStatement stmt = sqliteDatabase.compileStatement(sql2);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                stmt.bindDouble(1, c.getDouble(c.getColumnIndex("Average")));
                stmt.bindLong(2, c.getInt(c.getColumnIndex("SectionId")));
                stmt.bindLong(3, c.getInt(c.getColumnIndex("SubjectId")));
                stmt.bindLong(4, c.getLong(c.getColumnIndex("ExamId")));
                stmt.execute();
                stmt.clearBindings();
                c.moveToNext();
            }
            c.close();
            sqliteDatabase.setTransactionSuccessful();
            sqliteDatabase.endTransaction();
        }
    }

    public static void checkExamIsMark(SQLiteDatabase sqliteDatabase) {
        Cursor c = sqliteDatabase.rawQuery("Select A.ExamId,B.SectionId,A.SubjectId,Count(*) From Marks A,Students B Where A.StudentId=B.StudentId group by A.ExamId,B.SectionId,A.SubjectId", null);
        String sql = "update exmavg set CompleteEntry=1 where ExamId=? and SectionId=? and SubjectId=?";
        sqliteDatabase.beginTransaction();
        SQLiteStatement stmt = sqliteDatabase.compileStatement(sql);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            stmt.bindLong(1, c.getLong(c.getColumnIndex("ExamId")));
            stmt.bindLong(2, c.getInt(c.getColumnIndex("SectionId")));
            stmt.bindLong(3, c.getInt(c.getColumnIndex("SubjectId")));
            stmt.execute();
            stmt.clearBindings();
            c.moveToNext();
        }
        c.close();
        sqliteDatabase.setTransactionSuccessful();
        sqliteDatabase.endTransaction();
    }

    public static void checkExamMarkEmpty(SQLiteDatabase sqliteDatabase) {
        Cursor c = sqliteDatabase.rawQuery("Select A.ExamId,B.SectionId,A.SubjectId,Count(*) From Marks A,Students B Where A.StudentId=B.StudentId" +
                " group by A.ExamId,B.SectionId,A.SubjectId", null);
        String sql = "update exmavg set CompleteEntry=0 where ExamId=? and SectionId=? and SubjectId=?";
        sqliteDatabase.beginTransaction();
        SQLiteStatement stmt = sqliteDatabase.compileStatement(sql);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            stmt.bindLong(1, c.getLong(c.getColumnIndex("ExamId")));
            stmt.bindLong(2, c.getInt(c.getColumnIndex("SectionId")));
            stmt.bindLong(3, c.getInt(c.getColumnIndex("SubjectId")));
            stmt.execute();
            stmt.clearBindings();
            c.moveToNext();
        }
        c.close();
        sqliteDatabase.setTransactionSuccessful();
        sqliteDatabase.endTransaction();
    }

    public static void checkExamMarkEmpty(long examId, int subjectId, SQLiteDatabase sqliteDatabase) {
        Cursor c = sqliteDatabase.rawQuery("Select A.ExamId,B.SectionId,A.SubjectId,Count(*) From Marks A,Students B Where A.ExamId=" + examId + " AND A.StudentId=B.StudentId AND" +
                " A.SubjectId=" + subjectId + " group by A.ExamId,B.SectionId,A.SubjectId", null);
        String sql = "update exmavg set CompleteEntry=1 where ExamId=? and SectionId=? and SubjectId=?";
        sqliteDatabase.beginTransaction();
        SQLiteStatement stmt = sqliteDatabase.compileStatement(sql);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            stmt.bindLong(1, c.getLong(c.getColumnIndex("ExamId")));
            stmt.bindLong(2, c.getInt(c.getColumnIndex("SectionId")));
            stmt.bindLong(3, c.getInt(c.getColumnIndex("SubjectId")));
            stmt.execute();
            stmt.clearBindings();
            c.moveToNext();
        }
        c.close();
        sqliteDatabase.setTransactionSuccessful();
        sqliteDatabase.endTransaction();
    }

    public static void checkExmActIsMark(SQLiteDatabase sqliteDatabase) {
        Cursor c = sqliteDatabase.rawQuery("Select A.ExamId,B.SectionId,A.SubjectId,Count(*) From activitymark A,students B Where A.StudentId=B.StudentId" +
                " group by A.ExamId,B.SectionId,A.SubjectId", null);
        String sql = "update exmavg set CompleteEntry=1 where ExamId=? and SectionId=? and SubjectId=?";
        sqliteDatabase.beginTransaction();
        SQLiteStatement stmt = sqliteDatabase.compileStatement(sql);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            stmt.bindLong(1, c.getLong(c.getColumnIndex("ExamId")));
            stmt.bindLong(2, c.getInt(c.getColumnIndex("SectionId")));
            stmt.bindLong(3, c.getInt(c.getColumnIndex("SubjectId")));
            stmt.execute();
            stmt.clearBindings();
            c.moveToNext();
        }
        c.close();
        sqliteDatabase.setTransactionSuccessful();
        sqliteDatabase.endTransaction();
    }

    public static void checkExmActMarkEmpty(SQLiteDatabase sqliteDatabase) {
        Cursor c = sqliteDatabase.rawQuery("Select A.ExamId,B.SectionId,A.SubjectId,Count(*) From activitymark A,students B Where A.StudentId=B.StudentId" +
                " group by A.ExamId,B.SectionId,A.SubjectId", null);
        String sql = "update exmavg set CompleteEntry=0 where ExamId=? and SectionId=? and SubjectId=?";
        sqliteDatabase.beginTransaction();
        SQLiteStatement stmt = sqliteDatabase.compileStatement(sql);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            stmt.bindLong(1, c.getLong(c.getColumnIndex("ExamId")));
            stmt.bindLong(2, c.getInt(c.getColumnIndex("SectionId")));
            stmt.bindLong(3, c.getInt(c.getColumnIndex("SubjectId")));
            stmt.execute();
            stmt.clearBindings();
            c.moveToNext();
        }
        c.close();
        sqliteDatabase.setTransactionSuccessful();
        sqliteDatabase.endTransaction();
    }

    public static void checkExmActMarkEmpty(long examId, int subjectId, SQLiteDatabase sqliteDatabase) {
        Cursor c = sqliteDatabase.rawQuery("Select A.ExamId,B.SectionId,A.SubjectId,Count(*) From activitymark A,students B Where A.StudentId=B.StudentId" +
                " and A.ExamId=" + examId + " and A.SubjectId=" + subjectId + " and (A.Mark=0 or A.Mark='') group by A.ExamId,B.SectionId,A.SubjectId", null);
        String sql = "update exmavg set CompleteEntry=0 where ExamId=? and SectionId=? and SubjectId=?";
        sqliteDatabase.beginTransaction();
        SQLiteStatement stmt = sqliteDatabase.compileStatement(sql);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            stmt.bindLong(1, c.getLong(c.getColumnIndex("ExamId")));
            stmt.bindLong(2, c.getInt(c.getColumnIndex("SectionId")));
            stmt.bindLong(3, c.getInt(c.getColumnIndex("SubjectId")));
            stmt.execute();
            stmt.clearBindings();
            c.moveToNext();
        }
        c.close();
        sqliteDatabase.setTransactionSuccessful();
        sqliteDatabase.endTransaction();
    }

    public static void checkExmActMarkEmpty(List<Long> examIdList, List<Integer> subjectIdList, SQLiteDatabase sqliteDatabase) {
        for (int i = 0, j = examIdList.size(); i < j; i++) {
            Cursor c1 = sqliteDatabase.rawQuery("select B.SectionId From activitymark A, students B where A.StudentId=B.StudentId and A.ExamId=" + examIdList.get(i) + " and A.SubjectId=" +
                    subjectIdList.get(i), null);
            c1.moveToFirst();
            StringBuilder sb = new StringBuilder();
            if (c1.getCount() > 0) {
                while (!c1.isAfterLast()) {
                    sb.append(",").append(c1.getInt(c1.getColumnIndex("SectionId")));
                    c1.moveToNext();
                }
                c1.close();
                String s = sb.substring(1, sb.length());
                sqliteDatabase.execSQL("update exmavg set CompleteEntry=1 where SectionId in (" + s + ") and ExamId=" + examIdList.get(i) + " and SubjectId=" + subjectIdList.get(i));
            }
        }
    }

    public static void checkExmSubActMarkEmpty(List<Long> examIdList, List<Integer> subjectIdList, SQLiteDatabase sqliteDatabase) {
        for (int i = 0, j = examIdList.size(); i < j; i++) {
            Cursor c = sqliteDatabase.rawQuery("Select A.ExamId,B.SectionId,A.SubjectId,Count(*) From subactivitymark A,students B Where A.StudentId=B.StudentId" +
                    " and A.ExamId=" + examIdList.get(i) + " and A.SubjectId=" + subjectIdList.get(i) + " group by A.ExamId,B.SectionId,A.SubjectId", null);
            String sql = "update exmavg set CompleteEntry=1 where ExamId=? and SectionId=? and SubjectId=?";
            sqliteDatabase.beginTransaction();
            SQLiteStatement stmt = sqliteDatabase.compileStatement(sql);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                stmt.bindLong(1, c.getLong(c.getColumnIndex("ExamId")));
                stmt.bindLong(2, c.getInt(c.getColumnIndex("SectionId")));
                stmt.bindLong(3, c.getInt(c.getColumnIndex("SubjectId")));
                stmt.execute();
                stmt.clearBindings();
                c.moveToNext();
            }
            c.close();
            sqliteDatabase.setTransactionSuccessful();
            sqliteDatabase.endTransaction();
        }
    }

    public static void checkExmSubActIsMark(SQLiteDatabase sqliteDatabase) {
        Cursor c = sqliteDatabase.rawQuery("Select A.ExamId,B.SectionId,A.SubjectId,Count(*) From subactivitymark A,students B Where A.StudentId=B.StudentId" +
                " group by A.ExamId,B.SectionId,A.SubjectId", null);
        String sql = "update exmavg set CompleteEntry=1 where ExamId=? and SectionId=? and SubjectId=?";
        sqliteDatabase.beginTransaction();
        SQLiteStatement stmt = sqliteDatabase.compileStatement(sql);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            stmt.bindLong(1, c.getLong(c.getColumnIndex("ExamId")));
            stmt.bindLong(2, c.getInt(c.getColumnIndex("SectionId")));
            stmt.bindLong(3, c.getInt(c.getColumnIndex("SubjectId")));
            stmt.execute();
            stmt.clearBindings();
            c.moveToNext();
        }
        c.close();
        sqliteDatabase.setTransactionSuccessful();
        sqliteDatabase.endTransaction();
    }

    public static void checkExmSubActMarkEmpty(SQLiteDatabase sqliteDatabase) {
        Cursor c = sqliteDatabase.rawQuery("Select A.ExamId,B.SectionId,A.SubjectId,Count(*) From subactivitymark A,students B Where A.StudentId=B.StudentId" +
                " group by A.ExamId,B.SectionId,A.SubjectId", null);
        String sql = "update exmavg set CompleteEntry=0 where ExamId=? and SectionId=? and SubjectId=?";
        sqliteDatabase.beginTransaction();
        SQLiteStatement stmt = sqliteDatabase.compileStatement(sql);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            stmt.bindLong(1, c.getLong(c.getColumnIndex("ExamId")));
            stmt.bindLong(2, c.getInt(c.getColumnIndex("SectionId")));
            stmt.bindLong(3, c.getInt(c.getColumnIndex("SubjectId")));
            stmt.execute();
            stmt.clearBindings();
            c.moveToNext();
        }
        c.close();
        sqliteDatabase.setTransactionSuccessful();
        sqliteDatabase.endTransaction();
    }

    public static void checkExmSubActMarkEmpty(long examId, int subjectId, SQLiteDatabase sqliteDatabase) {
        Cursor c = sqliteDatabase.rawQuery("Select A.ExamId,B.SectionId,A.SubjectId,Count(*) From subactivitymark A,students B Where A.StudentId=B.StudentId" +
                " and A.ExamId=" + examId + " and A.SubjectId=" + subjectId + " and (A.Mark=0 or A.Mark='') group by A.ExamId,B.SectionId,A.SubjectId", null);
        String sql = "update exmavg set CompleteEntry=0 where ExamId=? and SectionId=? and SubjectId=?";
        sqliteDatabase.beginTransaction();
        SQLiteStatement stmt = sqliteDatabase.compileStatement(sql);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            stmt.bindLong(1, c.getLong(c.getColumnIndex("ExamId")));
            stmt.bindLong(2, c.getInt(c.getColumnIndex("SectionId")));
            stmt.bindLong(3, c.getInt(c.getColumnIndex("SubjectId")));
            stmt.execute();
            stmt.clearBindings();
            c.moveToNext();
        }
        c.close();
        sqliteDatabase.setTransactionSuccessful();
        sqliteDatabase.endTransaction();
    }

    public static void checkExmMarkEmpty(long examId, int sectionId, int subjectId, SQLiteDatabase sqliteDatabase) {
        Cursor c = sqliteDatabase.rawQuery("SELECT A.ExamId, COUNT(*) FROM marks A, students B WHERE A.ExamId=" + examId + " and A.SubjectId=" + subjectId +
                " and A.StudentId=B.StudentId and B.SectionId=" + sectionId + " and A.Mark='0' GROUP BY A.ExamId HAVING COUNT(*)>0", null);
        if (c.getCount() > 0) {
            String sql = "update exmavg set CompleteEntry=" + 0 + " where ExamId=" + examId + " and SectionId=" + sectionId + " and SubjectId=" + subjectId;
            sqliteDatabase.execSQL(sql);
        } else {
            String sql = "update exmavg set CompleteEntry=" + 1 + " where ExamId=" + examId + " and SectionId=" + sectionId + " and SubjectId=" + subjectId;
            sqliteDatabase.execSQL(sql);
        }
        c.close();
    }

    public static void checkExmActMarkEmpty(long examId, int sectionId, int subjectId, SQLiteDatabase sqliteDatabase) {
        Cursor c = sqliteDatabase.rawQuery("Select A.ExamId,B.SectionId,A.SubjectId,Count(*) From activitymark A,students B Where A.StudentId=B.StudentId" +
                " and A.ExamId=" + examId + " and A.SubjectId=" + subjectId + " and B.SectionId=" + sectionId + " and A.Mark='0' group by A.ExamId,B.SectionId,A.SubjectId", null);
        if (c.getCount() > 0) {
            String sql = "update exmavg set CompleteEntry=" + 0 + " where ExamId=" + examId + " and SectionId=" + sectionId + " and SubjectId=" + subjectId;
            sqliteDatabase.execSQL(sql);
        } else {
            String sql = "update exmavg set CompleteEntry=" + 1 + " where ExamId=" + examId + " and SectionId=" + sectionId + " and SubjectId=" + subjectId;
            sqliteDatabase.execSQL(sql);
        }
        c.close();
    }

    public static void checkExmSubActMarkEmpty(long examId, int sectionId, int subjectId, SQLiteDatabase sqliteDatabase) {
        Cursor c = sqliteDatabase.rawQuery("Select A.ExamId,B.SectionId,A.SubjectId,Count(*) From subactivitymark A,students B Where A.StudentId=B.StudentId" +
                " and A.ExamId=" + examId + " and A.SubjectId=" + subjectId + " and B.SectionId=" + sectionId + " and A.Mark='0' group by A.ExamId,B.SectionId,A.SubjectId", null);
        if (c.getCount() > 0) {
            String sql = "update exmavg set CompleteEntry=" + 0 + " where ExamId=" + examId + " and SectionId=" + sectionId + " and SubjectId=" + subjectId;
            sqliteDatabase.execSQL(sql);
        } else {
            String sql = "update exmavg set CompleteEntry=" + 1 + " where ExamId=" + examId + " and SectionId=" + sectionId + " and SubjectId=" + subjectId;
            sqliteDatabase.execSQL(sql);
        }
        c.close();
    }

    public static int getSeExamAvg(long examId, int sectionId, SQLiteDatabase sqliteDatabase) {
        double avg = 0;
        Cursor c = sqliteDatabase.rawQuery("select AVG((ExamAvg/360.00)*100.00) as avg from exmavg where ExamId=" + examId + " and SectionId=" + sectionId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            avg = c.getDouble(c.getColumnIndex("avg"));
            c.moveToNext();
        }
        c.close();
        return (int) avg;
    }

    public static int selectSeAvg2(int sectionId, int subjectId, long examId, SQLiteDatabase sqliteDatabase) {
        Cursor c = sqliteDatabase.rawQuery("select Avg(ExamAvg) as avg from exmavg where SectionId=" + sectionId + " and SubjectId=" + subjectId + " and ExamId=" + examId, null);
        c.moveToFirst();
        double avg = 0;
        while (!c.isAfterLast()) {
            double a = c.getDouble(c.getColumnIndex("avg"));
            if (a != 0) {
                avg = (a / (360.0)) * 100;
            }
            c.moveToNext();
        }
        c.close();
        return (int) avg;
    }

}
