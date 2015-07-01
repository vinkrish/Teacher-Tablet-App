package in.teacher.sqlite;

public class Temp {
	
	private int id;
	private String deviceId;
	private int schoolId;
	private int classId;
	private int sectionId;
	private String sectionName;
	private int teacherId;
	private int currentSection;
	private int currentSubject;
	private int currentClass;
	private int examId;
	private int activityId;
	private int subActivityId;
	private long slipTestId;
	private String syncTime;
	private int isSync;
	private int studentId;
	private int subjectId;
	
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
	public String getSectionName() {
		return sectionName;
	}
	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
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
	public int getActivityId() {
		return activityId;
	}
	public void setActivityId(int activityId) {
		this.activityId = activityId;
	}
	public int getSubActivityId() {
		return subActivityId;
	}
	public void setSubActivityId(int subActivityId) {
		this.subActivityId = subActivityId;
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
