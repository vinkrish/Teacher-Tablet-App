package in.teacher.sqlite;

public class Activiti {
    private int schoolId;
    private int activityId;
    private int classId;
    private int sectionId;
    private int examId;
    private int subjectId;
    private String activityName;
    private float maximumMark;
    private int weightage;
    private int subActivity;
    private int calculation;
    private int rubrixId;
    private float activityAvg;
    private int completeEntry;
    private String uniqueKey;

    public int getWeightage() {
        return weightage;
    }

    public void setWeightage(int weightage) {
        this.weightage = weightage;
    }

    public float getMaximumMark() {
        return maximumMark;
    }

    public void setMaximumMark(float maximumMark) {
        this.maximumMark = maximumMark;
    }

    public float getActivityAvg() {
        return activityAvg;
    }

    public void setActivityAvg(float activityAvg) {
        this.activityAvg = activityAvg;
    }

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public int getSubActivity() {
        return subActivity;
    }

    public void setSubActivity(int subActivity) {
        this.subActivity = subActivity;
    }

    public int getCalculation() {
        return calculation;
    }

    public void setCalculation(int calculation) {
        this.calculation = calculation;
    }

    public int getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(int schoolId) {
        this.schoolId = schoolId;
    }

    public int getRubrixId() {
        return rubrixId;
    }

    public void setRubrixId(int rubrixId) {
        this.rubrixId = rubrixId;
    }

    public int getCompleteEntry() {
        return completeEntry;
    }

    public void setCompleteEntry(int completeEntry) {
        this.completeEntry = completeEntry;
    }
}
