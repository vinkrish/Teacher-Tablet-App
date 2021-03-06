package in.teacher.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import in.teacher.sqlite.GradesClassWise;
import in.teacher.sqlite.Marks;
import in.teacher.util.GradeClassWiseSort;

public class MarksDao {

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

    public static int getSectionAvg(int classId, int sectionId, int subjectId, long examId, SQLiteDatabase sqliteDatabase) {
        gradesClassWiseList = GradesClassWiseDao.getGradeClassWise(classId, sqliteDatabase);
        Collections.sort(gradesClassWiseList, new GradeClassWiseSort());
        int avg = 0;
        Cursor c = sqliteDatabase.rawQuery("select Grade from marks where ExamId=" + examId + " and SubjectId=" + subjectId +
                " and StudentId in (select StudentId from Students where SectionId = "+sectionId+")", null);
        try {
            if (c.getCount() > 0) {
                c.moveToFirst();
                while (!c.isAfterLast()) {
                    avg += getMarkTo(c.getString(c.getColumnIndex("Grade")));
                    c.moveToNext();
                }
                c.close();
                return avg / c.getCount();
            } else return 0;
        } catch(NullPointerException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int getSectionAvg(long examId, int subjectId, int sectionId, SQLiteDatabase sqliteDatabase){
        int avg = 0;
        String sql = "SELECT (AVG(Mark)/C.MaximumMark)*100 as Average FROM exams A, marks B, subjectexams C WHERE A.ExamId = B.ExamId and C.ExamId=A.ExamId and A.ExamId = " + examId +
                " and B.SubjectId="+subjectId+" and B.StudentId in (select StudentId from Students where SectionId = "+sectionId+") and B.Mark!='-1'" ;
        Cursor c = sqliteDatabase.rawQuery(sql, null);
        c.moveToFirst();
        while(!c.isAfterLast()){
            avg = c.getInt(c.getColumnIndex("Average"));
            c.moveToNext();
        }
        c.close();
        return avg;
    }

    public static List<String> selectMarks(long examId, int subjectId, List<Long> studentId, SQLiteDatabase sqliteDatabase) {
        List<String> mList = new ArrayList<>();
        for (Long i : studentId) {
            Cursor c = sqliteDatabase.rawQuery("select Mark from marks where ExamId=" + examId + " AND SubjectId=" + subjectId + " AND StudentId=" + i, null);
            if (c.getCount() > 0) {
                c.moveToFirst();
                mList.add(c.getString(c.getColumnIndex("Mark")));
            } else {
                mList.add("");
            }
            c.close();
        }
        return mList;
    }

    public static List<String> selectGrade(long examId, int subjectId, List<Long> studentId, SQLiteDatabase sqliteDatabase) {
        List<String> mList = new ArrayList<>();
        for (Long i : studentId) {
            Cursor c = sqliteDatabase.rawQuery("select Grade from marks where ExamId=" + examId + " AND SubjectId=" + subjectId + " AND StudentId=" + i, null);
            if (c.getCount() > 0) {
                c.moveToFirst();
                mList.add(c.getString(c.getColumnIndex("Grade")));
            } else {
                mList.add("");
            }
            c.close();
        }
        return mList;
    }

    public static int getMarksCount(long examId, int subjectId, SQLiteDatabase sqliteDatabase) {
        int count = 0;
        Cursor c = sqliteDatabase.rawQuery("select count(*) as count from marks where ExamId=" + examId + " and SubjectId=" + subjectId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            count = c.getInt(c.getColumnIndex("count"));
            c.moveToNext();
        }
        c.close();
        return count;
    }

    public static List<Marks> selectMarks(int examId, int sectionId, int subjectId, SQLiteDatabase sqliteDatabase) {
        Cursor c = sqliteDatabase.rawQuery("SELECT A.StudentId,ExamId,B.SectionId,SubjectId,Mark FROM marks A, students B" +
                " where A.ExamId=" + examId + " and B.SectionId=" + sectionId + " and A.SubjectId=" + subjectId + " and A.StudentId=B.StudentId group by B.RollNoInClass", null);
        List<Marks> mList = new ArrayList<>();
        c.moveToFirst();
        while (!c.isAfterLast()) {
            Marks m = new Marks();
            m.setStudentId(c.getInt(c.getColumnIndex("StudentId")));
            m.setMark(c.getString(c.getColumnIndex("Mark")));
            mList.add(m);
            c.moveToNext();
        }
        c.close();
        return mList;
    }

    public static void insertMarks(List<Marks> mList, SQLiteDatabase sqliteDatabase) {
        for (Marks m : mList) {
            String sql = "insert into marks(SchoolId, ExamId, SubjectId, StudentId, Mark) values(" +
                    m.getSchoolId() + "," + m.getExamId() + "," + m.getSubjectId() + "," + m.getStudentId() + ",'" + m.getMark() + "')";
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

    public static void insertGrade(List<Marks> mList, SQLiteDatabase sqliteDatabase) {
        for (Marks m : mList) {
            String sql = "insert into marks(SchoolId, ExamId, SubjectId, StudentId, Mark, Grade) values(" +
                    m.getSchoolId() + "," + m.getExamId() + "," + m.getSubjectId() + "," + m.getStudentId() + ",'" + m.getMark() + "','" + m.getGrade() + "')";
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

    public static void updateMarks(List<Marks> mList, SQLiteDatabase sqliteDatabase) {
        for (Marks m : mList) {
            String sql = "update marks set Mark='" + m.getMark() + "' where ExamId=" + m.getExamId() + " and SubjectId=" + m.getSubjectId() + " and StudentId=" + m.getStudentId();
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

    public static void updateGrade(List<Marks> mList, SQLiteDatabase sqliteDatabase) {
        for (Marks m : mList) {
            String sql = "update marks set Grade='" + m.getGrade() + "' where ExamId=" + m.getExamId() + " and SubjectId=" + m.getSubjectId() + " and StudentId=" + m.getStudentId();
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

    public static void insertUpdateMarks(List<Marks> mList, SQLiteDatabase sqliteDatabase) {
        for (Marks m : mList) {
            Cursor c = sqliteDatabase.rawQuery("select Mark from marks where ExamId=" + m.getExamId() + " AND SubjectId=" + m.getSubjectId() + " AND StudentId=" + m.getStudentId(), null);
            if (c.getCount() > 0) {
                String sql = "update marks set Mark='" + m.getMark() + "' where ExamId=" + m.getExamId() + " and SubjectId=" + m.getSubjectId() + " and StudentId=" + m.getStudentId();
                try {
                    sqliteDatabase.execSQL(sql);
                    if (m.getMark().equals("")) {
                        String sql2 = "update marks set Mark=NULL where ExamId=" + m.getExamId() + " and SubjectId=" + m.getSubjectId() + " and StudentId=" + m.getStudentId();
                        ContentValues cv = new ContentValues();
                        cv.put("Query", sql2);
                        sqliteDatabase.insert("uploadsql", null, cv);
                    } else {
                        ContentValues cv = new ContentValues();
                        cv.put("Query", sql);
                        sqliteDatabase.insert("uploadsql", null, cv);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                String sql = "insert into marks(SchoolId, ExamId, SubjectId, StudentId, Mark) values(" +
                        m.getSchoolId() + "," + m.getExamId() + "," + m.getSubjectId() + "," + m.getStudentId() + ",'" + m.getMark() + "')";
                try {
                    sqliteDatabase.execSQL(sql);
                    if (m.getMark().equals("")) {
                        String sql2 = "insert into marks(SchoolId, ExamId, SubjectId, StudentId, Mark) values(" +
                                m.getSchoolId() + "," + m.getExamId() + "," + m.getSubjectId() + "," + m.getStudentId() + ",NULL)";
                        ContentValues cv = new ContentValues();
                        cv.put("Query", sql2);
                        sqliteDatabase.insert("uploadsql", null, cv);
                    } else {
                        ContentValues cv = new ContentValues();
                        cv.put("Query", sql);
                        sqliteDatabase.insert("uploadsql", null, cv);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            c.close();
        }
    }

    public static void insertUpdateGrade(List<Marks> mList, SQLiteDatabase sqliteDatabase) {
        for (Marks m : mList) {
            Cursor c = sqliteDatabase.rawQuery("select Mark from marks where ExamId=" + m.getExamId() + " AND SubjectId=" + m.getSubjectId() + " AND StudentId=" + m.getStudentId(), null);
            if (c.getCount() > 0) {
                String sql = "update marks set Grade='" + m.getGrade() + "' where ExamId=" + m.getExamId() + " and SubjectId=" + m.getSubjectId() + " and StudentId=" + m.getStudentId();
                try {
                    sqliteDatabase.execSQL(sql);
                    if (m.getMark().equals("")) {
                        String sql2 = "update marks set Grade=NULL where ExamId=" + m.getExamId() + " and SubjectId=" + m.getSubjectId() + " and StudentId=" + m.getStudentId();
                        ContentValues cv = new ContentValues();
                        cv.put("Query", sql2);
                        sqliteDatabase.insert("uploadsql", null, cv);
                    } else {
                        ContentValues cv = new ContentValues();
                        cv.put("Query", sql);
                        sqliteDatabase.insert("uploadsql", null, cv);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                String sql = "insert into marks(SchoolId, ExamId, SubjectId, StudentId, Mark, Grade) values(" +
                        m.getSchoolId() + "," + m.getExamId() + "," + m.getSubjectId() + "," + m.getStudentId() + ",'" + m.getMark() + "','" + m.getGrade() + "')";
                try {
                    sqliteDatabase.execSQL(sql);
                    if (m.getMark().equals("")) {
                        String sql2 = "insert into marks(SchoolId, ExamId, SubjectId, StudentId, Mark, Grade) values(" +
                                m.getSchoolId() + "," + m.getExamId() + "," + m.getSubjectId() + "," + m.getStudentId() + ",'0'," + "NULL)";
                        ContentValues cv = new ContentValues();
                        cv.put("Query", sql2);
                        sqliteDatabase.insert("uploadsql", null, cv);
                    } else {
                        ContentValues cv = new ContentValues();
                        cv.put("Query", sql);
                        sqliteDatabase.insert("uploadsql", null, cv);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            c.close();
        }
    }

    public static int getStudExamAvg(long studentId, int subjectId, long examId, SQLiteDatabase sqliteDatabase) {
        int i = 0;
        Cursor c = sqliteDatabase.rawQuery("select (AVG(A.Mark)/B.MaximumMark)*100 as avg from Marks A, subjectexams B where A.ExamId=B.ExamId and A.StudentId=" + studentId + " and" +
                " A.SubjectId=B.SubjectId and A.SubjectId=" + subjectId + " and A.ExamId=" + examId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            i = c.getInt(c.getColumnIndex("avg"));
            c.moveToNext();
        }
        c.close();
        return i;
    }

    public static int getStudExamMark(long studentId, int subjectId, long examId, SQLiteDatabase sqliteDatabase) {
        int i = 0;
        Cursor c = sqliteDatabase.rawQuery("select Mark from Marks where ExamId=" + examId + " and SubjectId=" + subjectId + " and StudentId=" + studentId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            i = c.getInt(c.getColumnIndex("Mark"));
            c.moveToNext();
        }
        c.close();
        return i;
    }

    public static int getExamMaxMark(int subjectId, long examId, SQLiteDatabase sqliteDatabase) {
        int i = 0;
        Cursor c = sqliteDatabase.rawQuery("select MaximumMark from subjectexams where ExamId=" + examId + " and SubjectId=" + subjectId, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            i = c.getInt(c.getColumnIndex("MaximumMark"));
            c.moveToNext();
        }
        c.close();
        return i;
    }

    public static int isThereExamMark(long examId, int sectionId, int subjectId, SQLiteDatabase sqliteDatabase) {
        int isThere = 0;
        Cursor c = sqliteDatabase.rawQuery("SELECT A.Mark from marks A, students B where A.ExamId=" + examId + " and A.StudentId=B.StudentId and B.SectionId=" + sectionId
                + " and A.SubjectId=" + subjectId + " and A.Mark!=0", null);
        if (c.getCount() > 0) {
            isThere = 1;
        }
        c.close();
        return isThere;
    }

    public static int isThereExamGrade(long examId, int sectionId, int subjectId, SQLiteDatabase sqliteDatabase) {
        int isThere = 0;
        Cursor c = sqliteDatabase.rawQuery("SELECT A.Grade from marks A, students B where A.ExamId=" + examId + " and A.StudentId=B.StudentId and B.SectionId=" + sectionId
                + " and A.SubjectId=" + subjectId + " LIMIT 1", null);
        if (c.getCount() > 0) {
            isThere = 1;
        }
        c.close();
        return isThere;
    }
}
