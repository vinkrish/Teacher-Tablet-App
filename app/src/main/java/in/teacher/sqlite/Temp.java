package in.teacher.sqlite;

public class Temp {

    private int id;
    private String deviceId;
    private int schoolId;
    private int classId;
    private int sectionId;
    private int classInchargeId;
    private int teacherId;
    private int currentSection;
    private int currentSubject;
    private int currentClass;
    private int examId;
    private long activityId;
    private long subActivityId;
    private long slipTestId;
    private String syncTime;
    private int isSync;
    private int studentId;
    private int subjectId;

    public long getActivityId() {
        return activityId;
    }

    public void setActivityId(long activityId) {
        this.activityId = activityId;
    }

    public long getSubActivityId() {
        return subActivityId;
    }

    public void setSubActivityId(long subActivityId) {
        this.subActivityId = subActivityId;
    }

    public int getClassInchargeId() {
        return classInchargeId;
    }

    public void setClassInchargeId(int classInchargeId) {
        this.classInchargeId = classInchargeId;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public int getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(int schoolId) {
        this.schoolId = schoolId;
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public int getCurrentSection() {
        return currentSection;
    }

    public void setCurrentSection(int currentSection) {
        this.currentSection = currentSection;
    }

    public int getCurrentSubject() {
        return currentSubject;
    }

    public void setCurrentSubject(int currentSubject) {
        this.currentSubject = currentSubject;
    }

    public int getCurrentClass() {
        return currentClass;
    }

    public void setCurrentClass(int currentClass) {
        this.currentClass = currentClass;
    }

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public long getSlipTestId() {
        return slipTestId;
    }

    public void setSlipTestId(long slipTestId) {
        this.slipTestId = slipTestId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getSyncTime() {
        return syncTime;
    }

    public void setSyncTime(String syncTime) {
        this.syncTime = syncTime;
    }

    public int getIsSync() {
        return isSync;
    }

    public void setIsSync(int isSync) {
        this.isSync = isSync;
    }

}
