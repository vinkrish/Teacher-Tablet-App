package in.teacher.sqlite;

public class SubActivity {
    private int schoolId;
    private long subActivityId;
    private int classId;
    private int sectionId;
    private long examId;
    private int subjectId;
    private long activityId;
    private String subActivityName;
    private float maximumMark;
    private int weightage;
    private int calculation;
    private float subActivityAvg;
    private int completeEntry;
    private String uniqueKey;

    public long getExamId() {
        return examId;
    }

    public void setExamId(long examId) {
        this.examId = examId;
    }

    public long getSubActivityId() {
        return subActivityId;
    }

    public void setSubActivityId(long subActivityId) {
        this.subActivityId = subActivityId;
    }

    public long getActivityId() {
        return activityId;
    }

    public void setActivityId(long activityId) {
        this.activityId = activityId;
    }

    public int getWeightage() {
        return weightage;
    }

    public void setWeightage(int weightage) {
        this.weightage = weightage;
    }

    public float getSubActivityAvg() {
        return subActivityAvg;
    }

    public void setSubActivityAvg(float subActivityAvg) {
        this.subActivityAvg = subActivityAvg;
    }

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
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

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubActivityName() {
        return subActivityName;
    }

    public void setSubActivityName(String subActivityName) {
        this.subActivityName = subActivityName;
    }

    public float getMaximumMark() {
        return maximumMark;
    }

    public void setMaximumMark(float maximumMark) {
        this.maximumMark = maximumMark;
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

    public int getCompleteEntry() {
        return completeEntry;
    }

    public void setCompleteEntry(int completeEntry) {
        this.completeEntry = completeEntry;
    }
}
