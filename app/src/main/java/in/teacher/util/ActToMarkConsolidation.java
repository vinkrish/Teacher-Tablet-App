package in.teacher.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import in.teacher.dao.ActivitiDao;
import in.teacher.dao.ActivityGradeDao;
import in.teacher.dao.GradesClassWiseDao;
import in.teacher.dao.SubjectExamsDao;
import in.teacher.dao.TempDao;
import in.teacher.examfragment.QuickSort;
import in.teacher.sqlite.Activiti;
import in.teacher.sqlite.GradesClassWise;
import in.teacher.sqlite.Students;
import in.teacher.sqlite.Temp;

/**
 * Created by vinkrish on 02/02/16.
 * This logic is right, work out the math yourself if you don't believe.
 */
public class ActToMarkConsolidation {

    static List<GradesClassWise> gradesClassWiseList = new ArrayList<>();

    private static void executeNsave(SQLiteDatabase sqliteDatabase, String sql) {
        try {
            sqliteDatabase.execSQL(sql);
            ContentValues cv = new ContentValues();
            cv.put("Query", sql);
            sqliteDatabase.insert("uploadsql", null, cv);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static float getGradePoint(String grade) {
        int gradePoint = 0;
        for (GradesClassWise gcw : gradesClassWiseList) {
            if (grade.equals(gcw.getGrade())) {
                gradePoint = gcw.getGradePoint();
                break;
            }
        }
        return gradePoint;
    }

    private static String getGrade(float mark) {
        String grade = "";
        for (GradesClassWise gcw : gradesClassWiseList) {
            if (mark <= gcw.getMarkTo()) {
                grade = gcw.getGrade();
                break;
            }
        }
        return grade;
    }

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

    public static void actMarkToMarkCalc(SQLiteDatabase sqliteDatabase, int calculation, List<Students> studentsArray) {
        Temp t = TempDao.selectTemp(sqliteDatabase);
        int schoolId = t.getSchoolId();
        int classId = t.getCurrentClass();
        int sectionId = t.getCurrentSection();
        int subjectId = t.getCurrentSubject();
        long examId = t.getExamId();

        List<Activiti> actList = ActivitiDao.selectActiviti(examId, subjectId, sectionId, sqliteDatabase);
        List<Long> actIdList = new ArrayList<>();
        List<Integer> weightageList = new ArrayList<>();
        List<Float> actMaxMarkList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (Activiti Act : actList) {
            calculation = Act.getCalculation();
            sb.append(Act.getActivityId() + ",");
            actIdList.add(Act.getActivityId());
            weightageList.add(Act.getWeightage());
            actMaxMarkList.add(Act.getMaximumMark());
        }
        boolean markExist;
        float exmMaxMark = SubjectExamsDao.getExmMaxMark(classId, examId, subjectId, sqliteDatabase);
        List<Float> weightMarkList = new ArrayList<>();
        if (calculation == 0) {
            for (int i = 0; i < actList.size(); i++) {
                if (weightageList.get(i) == 0) {
                    float dynamicWeightage = (float) (100.0 / actIdList.size());
                    weightMarkList.add((float) (dynamicWeightage / 100.0) * exmMaxMark);
                } else {
                    weightMarkList.add((float) (weightageList.get(i) / 100.0) * exmMaxMark);
                }
            }

            float finalMark;
            for (Students st : studentsArray) {
                Cursor cursor = sqliteDatabase.rawQuery("select Mark from marks where StudentId = " + st.getStudentId() + " and ExamId = " + examId + " and SubjectId = " + subjectId, null);
                if (cursor.getCount() > 0) markExist = true;
                else markExist = false;

                finalMark = 0;
                for (int j = 0; j < actList.size(); j++) {
                    float mark = 0;
                    Cursor c = sqliteDatabase.rawQuery("select Mark from activitymark where StudentId=" + st.getStudentId() + " and ActivityId=" + actIdList.get(j), null);
                    c.moveToFirst();
                    while (!c.isAfterLast()) {
                        mark = c.getFloat(c.getColumnIndex("Mark"));
                        c.moveToNext();
                    }
                    c.close();

                    if (mark == -1) {
                        finalMark += 0;
                    } else {
                        finalMark += (mark / actMaxMarkList.get(j)) * weightMarkList.get(j);
                    }
                }

                if (markExist) {
                    String sql = "update marks set Mark='" + finalMark + "' " +
                            "where ExamId=" + examId + " and SubjectId=" + subjectId + " and StudentId=" + st.getStudentId();
                    executeNsave(sqliteDatabase, sql);
                } else {
                    String sql = "insert into marks(SchoolId, ExamId, SubjectId, StudentId, Mark) values(" +
                            schoolId + "," + examId + "," + subjectId + "," + st.getStudentId() + ",'" + finalMark + "')";
                    executeNsave(sqliteDatabase, sql);
                }
            }
        } else if (calculation == -1) {
            Float actMaxMark = 0f;
            for (Float f : actMaxMarkList)
                actMaxMark += f;

            for (Students st : studentsArray) {
                Cursor cursor = sqliteDatabase.rawQuery("select Mark from marks where StudentId = " + st.getStudentId() + " and ExamId = " + examId + " and SubjectId = " + subjectId, null);
                if (cursor.getCount() > 0) markExist = true;
                else markExist = false;

                if (markExist) {
                    String sql = "update marks set Mark=((select SUM(Mark) from activitymark where ActivityId in" +
                            " (" + sb.substring(0, sb.length() - 1) + ") and StudentId=" + st.getStudentId() + ") /" + actMaxMark + ")*" + exmMaxMark + " where " +
                            "ExamId=" + examId + " and SubjectId=" + subjectId + " and StudentId=" + st.getStudentId();

                    executeNsave(sqliteDatabase, sql);
                } else {
                    String sql = "insert into marks(SchoolId, ExamId, SubjectId, StudentId, Mark) values(" +
                            schoolId + "," + examId + "," + subjectId + "," + st.getStudentId() + "," +
                            "((select SUM(Mark) from activitymark where Mark != -1 and ActivityId in (" + sb.substring(0, sb.length() - 1) + ") and " +
                            "StudentId=" + st.getStudentId() + ")/" + actMaxMark + ")*" + exmMaxMark + ")";

                    executeNsave(sqliteDatabase, sql);
                }
            }
        } else {
            Float actMaxMark = 1000f;
            for (Float f : actMaxMarkList)
                if (f < actMaxMark) actMaxMark = f;

            List<Float> markList = new ArrayList<>();
            for (Students st : studentsArray) {
                Cursor cursor = sqliteDatabase.rawQuery("select Mark from marks where StudentId = " + st.getStudentId() + " and ExamId = " + examId + " and SubjectId = " + subjectId, null);
                if (cursor.getCount() > 0) markExist = true;
                else markExist = false;

                markList.clear();
                for (int j = 0; j < actList.size(); j++) {
                    float mark = 0;
                    Cursor c = sqliteDatabase.rawQuery("select Mark from activitymark where StudentId=" + st.getStudentId() + " and ActivityId=" + actIdList.get(j), null);
                    c.moveToFirst();
                    while (!c.isAfterLast()) {
                        mark = c.getFloat(c.getColumnIndex("Mark"));
                        c.moveToNext();
                    }
                    c.close();

                    float actMax = actList.get(j).getMaximumMark();
                    if (actMax != actMaxMark) mark = (mark / actMax) * actMaxMark;

                    if (mark == -1) markList.add((float) 0);
                    else markList.add(mark);
                }
                float bestOfMarks = 0;
                QuickSort quickSort = new QuickSort();
                List<Float> sortedMarkList = quickSort.sort(markList);
                for (int cal = 0; cal < calculation; cal++)
                    bestOfMarks += sortedMarkList.get(cal);

                actMaxMark = actMaxMark * calculation;

                if (markExist) {
                    String sql = "update marks set Mark=(" + bestOfMarks + "/" + actMaxMark + ")*" + exmMaxMark + " where " +
                            "ExamId=" + examId + " and SubjectId=" + subjectId + " and StudentId=" + st.getStudentId();

                    executeNsave(sqliteDatabase, sql);
                } else {
                    String sql = "insert into marks(SchoolId, ExamId, SubjectId, StudentId, Mark) values(" +
                            schoolId + "," + examId + "," + subjectId + "," + st.getStudentId() + "," +
                            "(" + bestOfMarks + "/" + actMaxMark + ")*" + exmMaxMark + ")";

                    executeNsave(sqliteDatabase, sql);
                }
            }
        }
    }


    public static void actToMarkCalc(SQLiteDatabase sqliteDatabase, int calculation, List<Students> studentsArray) {
        Temp t = TempDao.selectTemp(sqliteDatabase);
        int schoolId = t.getSchoolId();
        int classId = t.getCurrentClass();
        int sectionId = t.getCurrentSection();
        int subjectId = t.getCurrentSubject();
        long examId = t.getExamId();

        List<Activiti> actList = ActivitiDao.selectActiviti(examId, subjectId, sectionId, sqliteDatabase);
        List<Long> actIdList = new ArrayList<>();
        List<Integer> weightageList = new ArrayList<>();
        List<Float> actMaxMarkList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (Activiti Act : actList) {
            calculation = Act.getCalculation();
            sb.append(Act.getActivityId() + ",");
            actIdList.add(Act.getActivityId());
            weightageList.add(Act.getWeightage());
            actMaxMarkList.add(Act.getMaximumMark());
        }
        boolean markExist;
        float exmMaxMark = SubjectExamsDao.getExmMaxMark(classId, examId, subjectId, sqliteDatabase);
        List<Float> weightMarkList = new ArrayList<>();
        if (calculation == 0) {
            for (int i = 0; i < actList.size(); i++) {
                if (weightageList.get(i) == 0) {
                    float dynamicWeightage = (float) (100.0 / actIdList.size());
                    weightMarkList.add((float) (dynamicWeightage / 100.0) * exmMaxMark);
                } else {
                    weightMarkList.add((float) (weightageList.get(i) / 100.0) * exmMaxMark);
                }
            }

            float finalMark;
            for (Students st : studentsArray) {
                Cursor cursor = sqliteDatabase.rawQuery("select Mark from marks where StudentId = " + st.getStudentId() + " and ExamId = " + examId + " and SubjectId = " + subjectId, null);
                if (cursor.getCount() > 0) markExist = true;
                else markExist = false;

                finalMark = 0;
                for (int j = 0; j < actList.size(); j++) {
                    float mark = 0;
                    int gradeMark = 0;
                    Cursor c = sqliteDatabase.rawQuery("select Mark from activitymark where StudentId=" + st.getStudentId() + " and ActivityId=" + actIdList.get(j), null);
                    if (c.getCount() > 0) {
                        c.moveToFirst();
                        while (!c.isAfterLast()) {
                            mark = c.getFloat(c.getColumnIndex("Mark"));
                            c.moveToNext();
                        }
                        c.close();
                    } else {
                        String grade = ActivityGradeDao.getActivityGrade(actIdList.get(j), st.getStudentId(), subjectId, sqliteDatabase);
                        if (!grade.equals("")) gradeMark = getMarkTo(grade);
                        mark = (float) ((gradeMark / 100.0) * actMaxMarkList.get(j));
                    }

                    if (mark == -1) {
                        finalMark += 0;
                    } else {
                        finalMark += (mark / actMaxMarkList.get(j)) * weightMarkList.get(j);
                    }
                }

                if (markExist) {
                    String sql = "update marks set Mark='" + finalMark + "' " +
                            "where ExamId=" + examId + " and SubjectId=" + subjectId + " and StudentId=" + st.getStudentId();
                    executeNsave(sqliteDatabase, sql);
                } else {
                    String sql = "insert into marks(SchoolId, ExamId, SubjectId, StudentId, Mark) values(" +
                            schoolId + "," + examId + "," + subjectId + "," + st.getStudentId() + ",'" + finalMark + "')";
                    executeNsave(sqliteDatabase, sql);
                }
            }
        } else if (calculation == -1) {
            Float actMaxMark = 0f;
            float totalMark;
            for (Float f : actMaxMarkList)
                actMaxMark += f;

            for (Students st : studentsArray) {
                Cursor cursor = sqliteDatabase.rawQuery("select Mark from marks where StudentId = " + st.getStudentId() + " and ExamId = " + examId + " and SubjectId = " + subjectId, null);
                if (cursor.getCount() > 0) markExist = true;
                else markExist = false;

                totalMark = 0;
                for (int j = 0; j < actList.size(); j++) {
                    float mark = 0;
                    int gradeMark = 0;
                    Cursor c = sqliteDatabase.rawQuery("select Mark from activitymark where StudentId=" + st.getStudentId() + " and ActivityId=" + actIdList.get(j), null);
                    if (c.getCount() > 0) {
                        c.moveToFirst();
                        while (!c.isAfterLast()) {
                            mark = c.getFloat(c.getColumnIndex("Mark"));
                            c.moveToNext();
                        }
                        c.close();
                    } else {
                        String grade = ActivityGradeDao.getActivityGrade(actIdList.get(j), st.getStudentId(), subjectId, sqliteDatabase);
                        if (!grade.equals("")) gradeMark = getMarkTo(grade);
                        mark = (float) ((gradeMark / 100.0) * actMaxMarkList.get(j));
                    }

                    if (mark == -1) {
                        totalMark += 0;
                    } else {
                        totalMark += mark;
                    }
                }

                float finalMark = (totalMark / actMaxMark) * exmMaxMark;

                if (markExist) {
                    String sql = "update marks set Mark='" + finalMark + "' " +
                            "where ExamId=" + examId + " and SubjectId=" + subjectId + " and StudentId=" + st.getStudentId();
                    executeNsave(sqliteDatabase, sql);
                } else {
                    String sql = "insert into marks(SchoolId, ExamId, SubjectId, StudentId, Mark) values(" +
                            schoolId + "," + examId + "," + subjectId + "," + st.getStudentId() + ",'" + finalMark + "')";
                    executeNsave(sqliteDatabase, sql);
                }
            }
        } else {
            Float actMaxMark = 1000f;
            for (Float f : actMaxMarkList)
                if (f < actMaxMark) actMaxMark = f;

            List<Float> markList = new ArrayList<>();
            for (Students st : studentsArray) {
                Cursor cursor = sqliteDatabase.rawQuery("select Mark from marks where StudentId = " + st.getStudentId() + " and ExamId = " + examId + " and SubjectId = " + subjectId, null);
                if (cursor.getCount() > 0) markExist = true;
                else markExist = false;

                markList.clear();
                for (int j = 0; j < actList.size(); j++) {
                    float mark = 0;
                    float gradeMark = 0;
                    Cursor c = sqliteDatabase.rawQuery("select Mark from activitymark where StudentId=" + st.getStudentId() + " and ActivityId=" + actIdList.get(j), null);
                    if (c.getCount() > 0) {
                        c.moveToFirst();
                        while (!c.isAfterLast()) {
                            mark = c.getFloat(c.getColumnIndex("Mark"));
                            c.moveToNext();
                        }
                        c.close();
                    } else {
                        String grade = ActivityGradeDao.getActivityGrade(actIdList.get(j), st.getStudentId(), subjectId, sqliteDatabase);
                        if (!grade.equals("")) gradeMark = getMarkTo(grade);
                        mark = (float) ((gradeMark / 100.0) * actMaxMarkList.get(j));
                    }

                    float actMax = actMaxMarkList.get(j);
                    if (actMax != actMaxMark) mark = (mark / actMax) * actMaxMark;

                    if (mark == -1) markList.add((float) 0);
                    else markList.add(mark);
                }
                float bestOfMarks = 0;
                QuickSort quickSort = new QuickSort();
                List<Float> sortedMarkList = quickSort.sort(markList);
                for (int cal = 0; cal < calculation; cal++)
                    bestOfMarks += sortedMarkList.get(cal);

                actMaxMark = actMaxMark * calculation;

                if (markExist) {
                    String sql = "update marks set Mark=(" + bestOfMarks + "/" + actMaxMark + ")*" + exmMaxMark + " where " +
                            "ExamId=" + examId + " and SubjectId=" + subjectId + " and StudentId=" + st.getStudentId();

                    executeNsave(sqliteDatabase, sql);
                } else {
                    String sql = "insert into marks(SchoolId, ExamId, SubjectId, StudentId, Mark) values(" +
                            schoolId + "," + examId + "," + subjectId + "," + st.getStudentId() + "," +
                            "(" + bestOfMarks + "/" + actMaxMark + ")*" + exmMaxMark + ")";

                    executeNsave(sqliteDatabase, sql);
                }
            }
        }
    }

    public static void actGradeToMarkCalc(SQLiteDatabase sqliteDatabase, int calculation, List<Students> studentsArray) {
        Temp t = TempDao.selectTemp(sqliteDatabase);
        int schoolId = t.getSchoolId();
        int classId = t.getCurrentClass();
        int sectionId = t.getCurrentSection();
        int subjectId = t.getCurrentSubject();
        long examId = t.getExamId();

        gradesClassWiseList = GradesClassWiseDao.getGradeClassWise(classId, sqliteDatabase);
        Collections.sort(gradesClassWiseList, new GradeClassWiseSort());

        boolean isDynamicWeightage = true;
        List<Activiti> actList = ActivitiDao.selectActiviti(examId, subjectId, sectionId, sqliteDatabase);
        List<Long> actIdList = new ArrayList<>();
        List<Integer> weightageList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (Activiti Act : actList) {
            calculation = Act.getCalculation();
            sb.append(Act.getActivityId() + ",");
            actIdList.add(Act.getActivityId());
            weightageList.add(Act.getWeightage());
            if (Act.getWeightage() == 0) isDynamicWeightage = false;
        }
        boolean markExist;
        if (calculation == 0) {
            float totalGradeMark = 0f;
            for (Students st : studentsArray) {
                Cursor cursor = sqliteDatabase.rawQuery("select Mark from marks where StudentId = " + st.getStudentId() + " and ExamId = " + examId + " and SubjectId = " + subjectId, null);
                if (cursor.getCount() > 0) markExist = true;
                else markExist = false;

                totalGradeMark = 0f;
                for (Activiti act : actList) {
                    float gradePoint = 0f;
                    float gradeWeightPoint;
                    String grade = ActivityGradeDao.getActivityGrade(act.getActivityId(), st.getStudentId(), act.getSubjectId(), sqliteDatabase);

                    if (!grade.equals("")) gradePoint = getGradePoint(grade);

                    if (isDynamicWeightage) gradeWeightPoint = (float) act.getWeightage() / 10;
                    else gradeWeightPoint = (float) (100 / actIdList.size()) / 10;

                    totalGradeMark += (gradePoint * gradeWeightPoint);
                }

                String finalGrade = getGrade(totalGradeMark);

                if (markExist) {
                    String sql = "update marks set Grade='" + finalGrade + "' where ExamId=" + examId + " and SubjectId=" + subjectId + " and StudentId=" + st.getStudentId();

                    executeNsave(sqliteDatabase, sql);
                } else {
                    String sql = "insert into marks(SchoolId, ExamId, SubjectId, StudentId, Grade) values(" +
                            schoolId + "," + examId + "," + subjectId + "," + st.getStudentId() + ",'" + finalGrade + "')";

                    executeNsave(sqliteDatabase, sql);
                }
            }
        } else if (calculation == -1) {
            int totalGradePoint;
            for (Students st : studentsArray) {
                Cursor cursor = sqliteDatabase.rawQuery("select Mark from marks where StudentId = " + st.getStudentId() + " and ExamId = " + examId + " and SubjectId = " + subjectId, null);
                if (cursor.getCount() > 0) markExist = true;
                else markExist = false;

                totalGradePoint = 0;
                for (Activiti act : actList) {
                    String grade = ActivityGradeDao.getActivityGrade(act.getActivityId(), st.getStudentId(), act.getSubjectId(), sqliteDatabase);
                    if (!grade.equals("")) totalGradePoint += getGradePoint(grade);
                }

                float finalMark = (totalGradePoint / actList.size()) * 10;
                String finalGrade = getGrade(finalMark);

                if (markExist) {
                    String sql = "update marks set Grade='" + finalGrade + "' where ExamId=" + examId + " and SubjectId=" + subjectId + " and StudentId=" + st.getStudentId();

                    executeNsave(sqliteDatabase, sql);
                } else {
                    String sql = "insert into marks(SchoolId, ExamId, SubjectId, StudentId, Grade) values(" +
                            schoolId + "," + examId + "," + subjectId + "," + st.getStudentId() + ",'" + finalGrade + "')";

                    executeNsave(sqliteDatabase, sql);
                }

            }
        } else {
            List<Float> gradePointList = new ArrayList<>();
            for (Students st : studentsArray) {
                Cursor cursor = sqliteDatabase.rawQuery("select Mark from marks where StudentId = " + st.getStudentId() + " and ExamId = " + examId + " and SubjectId = " + subjectId, null);
                if (cursor.getCount() > 0) markExist = true;
                else markExist = false;

                gradePointList.clear();

                for (Activiti act : actList) {
                    String grade = ActivityGradeDao.getActivityGrade(act.getActivityId(), st.getStudentId(), act.getSubjectId(), sqliteDatabase);
                    if (!grade.equals("")) gradePointList.add(getGradePoint(grade));
                    else gradePointList.add(0f);
                }

                float bestOfPoints = 0;
                QuickSort quickSort = new QuickSort();
                List<Float> sortedMarkList = quickSort.sort(gradePointList);
                for (int cal = 0; cal < calculation; cal++)
                    bestOfPoints += sortedMarkList.get(cal);

                float finalMark = (bestOfPoints / calculation) * 10;
                String finalGrade = getGrade(finalMark);

                if (markExist) {
                    String sql = "update marks set Grade='" + finalGrade + "' where ExamId=" + examId + " and SubjectId=" + subjectId + " and StudentId=" + st.getStudentId();

                    executeNsave(sqliteDatabase, sql);
                } else {
                    String sql = "insert into marks(SchoolId, ExamId, SubjectId, StudentId, Grade) values(" +
                            schoolId + "," + examId + "," + subjectId + "," + st.getStudentId() + ",'" + finalGrade + "')";

                    executeNsave(sqliteDatabase, sql);
                }
            }
        }
    }

}
