package in.teacher.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import in.teacher.dao.ActivitiDao;
import in.teacher.dao.ActivityMarkDao;
import in.teacher.dao.GradesClassWiseDao;
import in.teacher.dao.SubActivityDao;
import in.teacher.dao.SubActivityGradeDao;
import in.teacher.dao.TempDao;
import in.teacher.examfragment.QuickSort;
import in.teacher.sqlite.GradesClassWise;
import in.teacher.sqlite.Students;
import in.teacher.sqlite.SubActivity;
import in.teacher.sqlite.Temp;

/**
 * Created by vinkrish on 02/02/16.
 * This logic is right, work out the math yourself if you don't believe.
 */
public class SubActToActConsolidation {

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

    public static void subActMarkToMarkCalc(SQLiteDatabase sqliteDatabase, int calculation, List<Students> studentsArray) {
        Temp t = TempDao.selectTemp(sqliteDatabase);
        int schoolId = t.getSchoolId();
        int sectionId = t.getCurrentSection();
        int subjectId = t.getCurrentSubject();
        long examId = t.getExamId();
        long activityId = t.getActivityId();

        List<SubActivity> subActList = SubActivityDao.selectSubActivity(activityId, sqliteDatabase);
        List<Long> subActIdList = new ArrayList<>();
        List<Integer> weightageList = new ArrayList<>();
        List<Float> subActMaxMarkList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (SubActivity subAct : subActList) {
            sb.append(subAct.getSubActivityId() + ",");
            subActIdList.add(subAct.getSubActivityId());
            weightageList.add(subAct.getWeightage());
            subActMaxMarkList.add(subAct.getMaximumMark());
        }
        boolean markExist;
        float activityMaxMark = ActivitiDao.getActivityMaxMark(activityId, sqliteDatabase);
        List<Float> weightMarkList = new ArrayList<>();
        if (calculation == 0) {
            for (int i = 0; i < subActList.size(); i++) {
                if (weightageList.get(i) == 0) {
                    float dynamicWeightage = (float) (100.0 / subActIdList.size());
                    weightMarkList.add((float) (dynamicWeightage / 100.0) * activityMaxMark);
                } else {
                    weightMarkList.add((float) (weightageList.get(i) / 100.0) * activityMaxMark);
                }
            }

            float finalMark;
            for (Students st : studentsArray) {
                finalMark = 0;
                Cursor cursor = sqliteDatabase.rawQuery("select Mark from activitymark where StudentId = " + st.getStudentId() + " and ActivityId = " + activityId, null);
                if (cursor.getCount() > 0) markExist = true;
                else markExist = false;

                for (int j = 0; j < subActList.size(); j++) {
                    float mark = 0;
                    Cursor c = sqliteDatabase.rawQuery("select Mark from subactivitymark where StudentId=" + st.getStudentId() + " and SubActivityId=" + subActIdList.get(j), null);
                    c.moveToFirst();
                    while (!c.isAfterLast()) {
                        mark = c.getFloat(c.getColumnIndex("Mark"));
                        c.moveToNext();
                    }
                    c.close();

                    if (mark == -1) {
                        finalMark += 0;
                    } else {
                        finalMark += (mark / subActMaxMarkList.get(j)) * weightMarkList.get(j);
                    }
                }

                if (markExist) {
                    String sql = "update activitymark set Mark='" + finalMark + "' where ActivityId=" + activityId + " and " +
                            "StudentId=" + st.getStudentId() + " and SubjectId=" + subjectId;

                    executeNsave(sqliteDatabase, sql);
                } else {
                    String sql = "insert into activitymark(SchoolId, ExamId, SubjectId, StudentId, ActivityId, Mark) " +
                            "values(" + schoolId + "," + examId + "," + subjectId + "," + st.getStudentId() + "," + activityId + ",'" + finalMark + "')";

                    executeNsave(sqliteDatabase, sql);
                }
            }
        } else if (calculation == -1) {
            Float subActMaxMark = 0f;
            for (Float f : subActMaxMarkList)
                subActMaxMark += f;

            for (Students st : studentsArray) {
                Cursor cursor = sqliteDatabase.rawQuery("select Mark from activitymark where StudentId = " + st.getStudentId() + " and ActivityId = " + activityId, null);
                if (cursor.getCount() > 0) markExist = true;
                else markExist = false;

                if (markExist) {
                    String sql = "update activitymark set Mark = ((select SUM(Mark) from subactivitymark where Mark!=-1 and SubActivityId in" +
                            " (" + sb.substring(0, sb.length() - 1) + ") and StudentId=" + st.getStudentId() + ")/" + subActMaxMark + ")*" + activityMaxMark + " where ActivityId=" + activityId + " and" +
                            " StudentId=" + st.getStudentId() + " and SubjectId=" + subjectId;

                    executeNsave(sqliteDatabase, sql);
                } else {
                    String sql = "insert into activitymark(SchoolId, ExamId, SubjectId, StudentId, ActivityId, Mark) " +
                            "values(" + schoolId + "," + examId + "," + subjectId + "," + st.getStudentId() + "," + activityId + "," +
                            "((select SUM(Mark) from subactivitymark where Mark!=-1 and SubActivityId in (" + sb.substring(0, sb.length() - 1) + ") and " +
                            "StudentId=" + st.getStudentId() + ")/" + subActMaxMark + ")*" + activityMaxMark + ")";

                    executeNsave(sqliteDatabase, sql);
                }
            }
        } else {
            Float subActMaxMark = 1000f;
            for (Float f : subActMaxMarkList)
                if (f < subActMaxMark) subActMaxMark = f;

            List<Float> markList = new ArrayList<>();
            for (Students st : studentsArray) {
                Cursor cursor = sqliteDatabase.rawQuery("select Mark from activitymark where StudentId = " + st.getStudentId() + " and ActivityId = " + activityId, null);
                if (cursor.getCount() > 0) markExist = true;
                else markExist = false;

                markList.clear();
                for (int j = 0; j < subActList.size(); j++) {
                    float mark = 0;
                    Cursor c = sqliteDatabase.rawQuery("select Mark from subactivitymark where StudentId=" + st.getStudentId() + " and SubActivityId=" + subActIdList.get(j), null);
                    c.moveToFirst();
                    while (!c.isAfterLast()) {
                        mark = c.getFloat(c.getColumnIndex("Mark"));
                        c.moveToNext();
                    }
                    c.close();

                    float subActMax = subActList.get(j).getMaximumMark();
                    if (subActMax != subActMaxMark) mark = (mark / subActMax) * subActMaxMark;

                    if (mark == -1) markList.add((float) 0);
                    else markList.add(mark);

                }

                float bestOfMarks = 0;
                QuickSort quickSort = new QuickSort();
                List<Float> sortedMarkList = quickSort.sort(markList);
                for (int cal = 0; cal < calculation; cal++)
                    bestOfMarks += sortedMarkList.get(cal);

                subActMaxMark = subActMaxMark * calculation;

                if (markExist) {
                    String sql = "update activitymark set Mark = (" + bestOfMarks + "/" + subActMaxMark + ")*" + activityMaxMark + " where ActivityId=" + activityId + " and" +
                            " StudentId=" + st.getStudentId() + " and SubjectId=" + subjectId;

                    executeNsave(sqliteDatabase, sql);
                } else {
                    String sql = "insert into activitymark(SchoolId, ExamId, SubjectId, StudentId, ActivityId, Mark) " +
                            "values(" + schoolId + "," + examId + "," + subjectId + "," + st.getStudentId() + "," + activityId + "," +
                            "(" + bestOfMarks + "/" + subActMaxMark + ")*" + activityMaxMark + ")";

                    executeNsave(sqliteDatabase, sql);
                }
            }
        }
        List<Long> actIdList = ActivitiDao.getActivityIds(examId, subjectId, sectionId, sqliteDatabase);
        if (ActivityMarkDao.isAllActMarkExist(actIdList, sqliteDatabase))
            ActToMarkConsolidation.actMarkToMarkCalc(sqliteDatabase, calculation, studentsArray);
        else if (ActivityMarkDao.isAllActMarkOrGradeExist(actIdList, sqliteDatabase))
            ActToMarkConsolidation.actToMarkCalc(sqliteDatabase, calculation, studentsArray);
    }

    public static void subActToActMarkCalc(SQLiteDatabase sqliteDatabase, int calculation, List<Students> studentsArray) {
        Temp t = TempDao.selectTemp(sqliteDatabase);
        int schoolId = t.getSchoolId();
        int sectionId = t.getCurrentSection();
        int subjectId = t.getCurrentSubject();
        long examId = t.getExamId();
        long activityId = t.getActivityId();

        List<SubActivity> subActList = SubActivityDao.selectSubActivity(activityId, sqliteDatabase);
        List<Long> subActIdList = new ArrayList<>();
        List<Integer> weightageList = new ArrayList<>();
        List<Float> subActMaxMarkList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (SubActivity subAct : subActList) {
            sb.append(subAct.getSubActivityId() + ",");
            subActIdList.add(subAct.getSubActivityId());
            weightageList.add(subAct.getWeightage());
            subActMaxMarkList.add(subAct.getMaximumMark());
        }
        boolean markExist;
        float activityMaxMark = ActivitiDao.getActivityMaxMark(activityId, sqliteDatabase);
        List<Float> weightMarkList = new ArrayList<>();
        if (calculation == 0) {
            for (int i = 0; i < subActList.size(); i++) {
                if (weightageList.get(i) == 0) {
                    float dynamicWeightage = (float) (100.0 / subActIdList.size());
                    weightMarkList.add((float) (dynamicWeightage / 100.0) * activityMaxMark);
                } else {
                    weightMarkList.add((float) (weightageList.get(i) / 100.0) * activityMaxMark);
                }
            }

            float finalMark;
            for (Students st : studentsArray) {
                finalMark = 0;
                Cursor cursor = sqliteDatabase.rawQuery("select Mark from activitymark where StudentId = " + st.getStudentId() + " and ActivityId = " + activityId, null);
                if (cursor.getCount() > 0) markExist = true;
                else markExist = false;

                for (int j = 0; j < subActList.size(); j++) {
                    float mark = 0;
                    float gradeMark = 0;
                    Cursor c = sqliteDatabase.rawQuery("select Mark from subactivitymark where StudentId=" + st.getStudentId() + " and SubActivityId=" + subActIdList.get(j), null);
                    if (c.getCount() > 0) {
                        c.moveToFirst();
                        while (!c.isAfterLast()) {
                            mark = c.getFloat(c.getColumnIndex("Mark"));
                            c.moveToNext();
                        }
                        c.close();
                    } else {
                        String grade = SubActivityGradeDao.getSubActivityGrade(subActIdList.get(j), st.getStudentId(), subjectId, sqliteDatabase);
                        if (!grade.equals("")) gradeMark = getMarkTo(grade);
                        mark = (float) ((gradeMark / 100.0) * subActMaxMarkList.get(j));
                    }

                    if (mark == -1) {
                        finalMark += 0;
                    } else {
                        finalMark += (mark / subActMaxMarkList.get(j)) * weightMarkList.get(j);
                    }

                }

                if (markExist) {
                    String sql = "update activitymark set Mark='" + finalMark + "' where ActivityId=" + activityId + " and " +
                            "StudentId=" + st.getStudentId() + " and SubjectId=" + subjectId;

                    executeNsave(sqliteDatabase, sql);
                } else {
                    String sql = "insert into activitymark(SchoolId, ExamId, SubjectId, StudentId, ActivityId, Mark) " +
                            "values(" + schoolId + "," + examId + "," + subjectId + "," + st.getStudentId() + "," + activityId + ",'" + finalMark + "')";

                    executeNsave(sqliteDatabase, sql);
                }
            }
        } else if (calculation == -1) {
            Float subActMaxMark = 0f;
            float totalMark = 0;
            for (Float f : subActMaxMarkList)
                subActMaxMark += f;

            for (Students st : studentsArray) {
                totalMark = 0;
                Cursor cursor = sqliteDatabase.rawQuery("select Mark from activitymark where StudentId = " + st.getStudentId() + " and ActivityId = " + activityId, null);
                if (cursor.getCount() > 0) markExist = true;
                else markExist = false;

                for (int j = 0; j < subActList.size(); j++) {
                    float mark = 0;
                    float gradeMark = 0;
                    Cursor c = sqliteDatabase.rawQuery("select Mark from subactivitymark where StudentId=" + st.getStudentId() + " and SubActivityId=" + subActIdList.get(j), null);
                    if (c.getCount() > 0) {
                        c.moveToFirst();
                        while (!c.isAfterLast()) {
                            mark = c.getFloat(c.getColumnIndex("Mark"));
                            c.moveToNext();
                        }
                        c.close();
                    } else {
                        String grade = SubActivityGradeDao.getSubActivityGrade(subActIdList.get(j), st.getStudentId(), subjectId, sqliteDatabase);
                        if (!grade.equals("")) gradeMark = getMarkTo(grade);
                        mark = (float) ((gradeMark / 100.0) * subActMaxMarkList.get(j));
                    }

                    if (mark == -1) {
                        totalMark += 0;
                    } else {
                        totalMark += mark;
                    }

                }

                float finalMark = (totalMark / subActMaxMark) * activityMaxMark;

                if (markExist) {
                    String sql = "update activitymark set Mark='" + finalMark + "' where ActivityId=" + activityId + " and " +
                            "StudentId=" + st.getStudentId() + " and SubjectId=" + subjectId;

                    executeNsave(sqliteDatabase, sql);
                } else {
                    String sql = "insert into activitymark(SchoolId, ExamId, SubjectId, StudentId, ActivityId, Mark) " +
                            "values(" + schoolId + "," + examId + "," + subjectId + "," + st.getStudentId() + "," + activityId + ",'" + finalMark + "')";

                    executeNsave(sqliteDatabase, sql);
                }
            }
        } else {
            Float subActMaxMark = 1000f;
            for (Float f : subActMaxMarkList)
                if (f < subActMaxMark) subActMaxMark = f;

            List<Float> markList = new ArrayList<>();
            for (Students st : studentsArray) {
                Cursor cursor = sqliteDatabase.rawQuery("select Mark from activitymark where StudentId = " + st.getStudentId() + " and ActivityId = " + activityId, null);
                if (cursor.getCount() > 0) markExist = true;
                else markExist = false;

                markList.clear();
                for (int j = 0; j < subActList.size(); j++) {
                    float mark = 0;
                    float gradeMark = 0;
                    Cursor c = sqliteDatabase.rawQuery("select Mark from subactivitymark where StudentId=" + st.getStudentId() + " and SubActivityId=" + subActIdList.get(j), null);
                    if (c.getCount() > 0) {
                        c.moveToFirst();
                        while (!c.isAfterLast()) {
                            mark = c.getFloat(c.getColumnIndex("Mark"));
                            c.moveToNext();
                        }
                        c.close();
                    } else {
                        String grade = SubActivityGradeDao.getSubActivityGrade(subActIdList.get(j), st.getStudentId(), subjectId, sqliteDatabase);
                        if (!grade.equals("")) gradeMark = getMarkTo(grade);
                        mark = (float) ((gradeMark / 100.0) * subActMaxMarkList.get(j));
                    }

                    float subActMax = subActMaxMarkList.get(j);
                    if (subActMax != subActMaxMark) mark = (mark / subActMax) * subActMaxMark;

                    if (mark == -1) markList.add((float) 0);
                    else markList.add(mark);

                }

                float bestOfMarks = 0;
                QuickSort quickSort = new QuickSort();
                List<Float> sortedMarkList = quickSort.sort(markList);
                for (int cal = 0; cal < calculation; cal++)
                    bestOfMarks += sortedMarkList.get(cal);

                subActMaxMark = subActMaxMark * calculation;

                if (markExist) {
                    String sql = "update activitymark set Mark = (" + bestOfMarks + "/" + subActMaxMark + ")*" + activityMaxMark + " where ActivityId=" + activityId + " and" +
                            " StudentId=" + st.getStudentId() + " and SubjectId=" + subjectId;

                    executeNsave(sqliteDatabase, sql);
                } else {
                    String sql = "insert into activitymark(SchoolId, ExamId, SubjectId, StudentId, ActivityId, Mark) " +
                            "values(" + schoolId + "," + examId + "," + subjectId + "," + st.getStudentId() + "," + activityId + "," +
                            "(" + bestOfMarks + "/" + subActMaxMark + ")*" + activityMaxMark + ")";

                    executeNsave(sqliteDatabase, sql);
                }
            }
        }
        List<Long> actIdList = ActivitiDao.getActivityIds(examId, subjectId, sectionId, sqliteDatabase);
        if (ActivityMarkDao.isAllActMarkExist(actIdList, sqliteDatabase))
            ActToMarkConsolidation.actMarkToMarkCalc(sqliteDatabase, calculation, studentsArray);
        else if (ActivityMarkDao.isAllActMarkOrGradeExist(actIdList, sqliteDatabase))
            ActToMarkConsolidation.actToMarkCalc(sqliteDatabase, calculation, studentsArray);
    }

    public static void subActGradeToActGradeCalc(SQLiteDatabase sqliteDatabase, int calculation, List<Students> studentsArray) {
        Temp t = TempDao.selectTemp(sqliteDatabase);
        int schoolId = t.getSchoolId();
        int classId = t.getCurrentClass();
        int sectionId = t.getCurrentSection();
        int subjectId = t.getCurrentSubject();
        long activityId = t.getActivityId();
        long examId = t.getExamId();

        gradesClassWiseList = GradesClassWiseDao.getGradeClassWise(classId, sqliteDatabase);
        Collections.sort(gradesClassWiseList, new GradeClassWiseSort());

        boolean isDynamicWeightage = true;
        List<SubActivity> subActList = SubActivityDao.selectSubActivity(activityId, sqliteDatabase);
        List<Long> subActIdList = new ArrayList<>();
        List<Integer> weightageList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (SubActivity subAct : subActList) {
            sb.append(subAct.getSubActivityId() + ",");
            subActIdList.add(subAct.getSubActivityId());
            weightageList.add(subAct.getWeightage());
            if (subAct.getWeightage() == 0) isDynamicWeightage = false;
        }
        boolean gradeExist;
        if (calculation == 0) {
            float totalGradeMark;
            for (Students st : studentsArray) {
                Cursor cursor = sqliteDatabase.rawQuery("select Grade from activitygrade where StudentId = " + st.getStudentId() + " and ActivityId = " + activityId, null);
                if (cursor.getCount() > 0) gradeExist = true;
                else gradeExist = false;

                totalGradeMark = 0f;
                for (SubActivity subAct : subActList) {
                    float gradePoint = 0f;
                    float gradeWeightPoint;
                    String grade = SubActivityGradeDao.getSubActivityGrade(subAct.getSubActivityId(), st.getStudentId(), subAct.getSubjectId(), sqliteDatabase);

                    if (!grade.equals("")) gradePoint = getGradePoint(grade);

                    if (isDynamicWeightage) gradeWeightPoint = (float) subAct.getWeightage() / 10;
                    else gradeWeightPoint = (float) (100 / subActIdList.size()) / 10;

                    totalGradeMark += (gradePoint * gradeWeightPoint);
                }
                String finalGrade = getGrade(totalGradeMark);

                if (gradeExist) {
                    String sql = "update activitygrade set Grade='" + finalGrade + "' where ActivityId=" + activityId + " and " +
                            "StudentId=" + st.getStudentId() + " and SubjectId=" + subjectId;

                    executeNsave(sqliteDatabase, sql);
                } else {
                    String sql = "insert into activitygrade(SchoolId, ExamId, SubjectId, StudentId, ActivityId, Grade) " +
                            "values(" + schoolId + "," + examId + "," + subjectId + "," + st.getStudentId() + "," + activityId + ",'" + finalGrade + "')";

                    executeNsave(sqliteDatabase, sql);
                }
            }
        } else if (calculation == -1) {
            int totalGradePoint;
            for (Students st : studentsArray) {
                Cursor cursor = sqliteDatabase.rawQuery("select Grade from activitygrade where StudentId = " + st.getStudentId() + " and ActivityId = " + activityId, null);
                if (cursor.getCount() > 0) gradeExist = true;
                else gradeExist = false;

                totalGradePoint = 0;
                for (SubActivity subAct : subActList) {
                    String grade = SubActivityGradeDao.getSubActivityGrade(subAct.getSubActivityId(), st.getStudentId(), subAct.getSubjectId(), sqliteDatabase);
                    if (!grade.equals("")) totalGradePoint += getGradePoint(grade);
                }

                float finalMark = (totalGradePoint / subActList.size()) * 10;
                String finalGrade = getGrade(finalMark);

                if (gradeExist) {
                    String sql = "update activitygrade set Grade='" + finalGrade + "' where ActivityId=" + activityId + " and " +
                            "StudentId=" + st.getStudentId() + " and SubjectId=" + subjectId;

                    executeNsave(sqliteDatabase, sql);
                } else {
                    String sql = "insert into activitygrade(SchoolId, ExamId, SubjectId, StudentId, ActivityId, Grade) " +
                            "values(" + schoolId + "," + examId + "," + subjectId + "," + st.getStudentId() + "," + activityId + ",'" + finalGrade + "')";

                    executeNsave(sqliteDatabase, sql);
                }
            }
        } else {
            List<Float> gradePointList = new ArrayList<>();
            for (Students st : studentsArray) {
                Cursor cursor = sqliteDatabase.rawQuery("select Grade from activitygrade where StudentId = " + st.getStudentId() + " and ActivityId = " + activityId, null);
                if (cursor.getCount() > 0) gradeExist = true;
                else gradeExist = false;

                gradePointList.clear();
                for (SubActivity subAct : subActList) {
                    String grade = SubActivityGradeDao.getSubActivityGrade(subAct.getSubActivityId(), st.getStudentId(), subAct.getSubjectId(), sqliteDatabase);
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

                if (gradeExist) {
                    String sql = "update activitygrade set Grade='" + finalGrade + "' where ActivityId=" + activityId + " and " +
                            "StudentId=" + st.getStudentId() + " and SubjectId=" + subjectId;

                    executeNsave(sqliteDatabase, sql);
                } else {
                    String sql = "insert into activitygrade(SchoolId, ExamId, SubjectId, StudentId, ActivityId, Grade) " +
                            "values(" + schoolId + "," + examId + "," + subjectId + "," + st.getStudentId() + "," + activityId + ",'" + finalGrade + "')";

                    executeNsave(sqliteDatabase, sql);
                }
            }
        }
        List<Long> actIdList = ActivitiDao.getActivityIds(examId, subjectId, sectionId, sqliteDatabase);
        if (ActivityMarkDao.isAllActMarkExist(actIdList, sqliteDatabase))
            ActToMarkConsolidation.actGradeToMarkCalc(sqliteDatabase, calculation, studentsArray);
        else if (ActivityMarkDao.isAllActMarkOrGradeExist(actIdList, sqliteDatabase))
            ActToMarkConsolidation.actToMarkCalc(sqliteDatabase, calculation, studentsArray);
    }

}
