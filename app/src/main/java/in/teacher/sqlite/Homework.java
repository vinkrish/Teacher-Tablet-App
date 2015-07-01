package in.teacher.sqlite;

public class Homework {
	private long homeworkId;
	private String schoolId;
	private String classId;
	private String sectionId;
	private String teacherId;
	private String messageFrom;
	private String messageVia;
	private String subjectIDs;
	private String Homework;
	private String HomeworkDate;
	private int isNew;
	
	public int getIsNew() {
		return isNew;
	}
	public void setIsNew(int isNew) {
		this.isNew = isNew;
	}
	public long getHomeworkId() {
		return homeworkId;
	}
	public void setHomeworkId(long homeworkId) {
		this.homeworkId = homeworkId;
	}
	public String getSchoolId() {
		return schoolId;
	}
	public void setSchoolId(String schoolId) {
		this.schoolId = schoolId;
	}
	public String getClassId() {
		return classId;
	}
	public void setClassId(String classId) {
		this.classId = classId;
	}
	public String getSectionId() {
		return sectionId;
	}
	public void setSectionId(String sectionId) {
		this.sectionId = sectionId;
	}
	public String getTeacherId() {
		return teacherId;
	}
	public void setTeacherId(String teacherId) {
		this.teacherId = teacherId;
	}
	public String getMessageFrom() {
		return messageFrom;
	}
	public void setMessageFrom(String messageFrom) {
		this.messageFrom = messageFrom;
	}
	public String getMessageVia() {
		return messageVia;
	}
	public void setMessageVia(String messageVia) {
		this.messageVia = messageVia;
	}
	public String getSubjectIDs() {
		return subjectIDs;
	}
	public void setSubjectIDs(String subjectIDs) {
		this.subjectIDs = subjectIDs;
	}
	public String getHomework() {
		return Homework;
	}
	public void setHomework(String homework) {
		Homework = homework;
	}
	public String getHomeworkDate() {
		return HomeworkDate;
	}
	public void setHomeworkDate(String homeworkDate) {
		HomeworkDate = homeworkDate;
	}

}
