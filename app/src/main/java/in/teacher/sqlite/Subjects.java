package in.teacher.sqlite;

public class Subjects {
	private int subjectId;
	private int schoolId;
	private String subjectName;
	private int hasPartition;
	private int theorySubjectId;
	private int practicalSubjectId;
	
	public int getSubjectId() {
		return subjectId;
	}
	public void setSubjectId(int subjectId) {
		this.subjectId = subjectId;
	}
	public String getSubjectName() {
		return subjectName;
	}
	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}
	public int getSchoolId() {
		return schoolId;
	}
	public void setSchoolId(int schoolId) {
		this.schoolId = schoolId;
	}
	public int getHasPartition() {
		return hasPartition;
	}
	public void setHasPartition(int hasPartition) {
		this.hasPartition = hasPartition;
	}
	public int getTheorySubjectId() {
		return theorySubjectId;
	}
	public void setTheorySubjectId(int theorySubjectId) {
		this.theorySubjectId = theorySubjectId;
	}
	public int getPracticalSubjectId() {
		return practicalSubjectId;
	}
	public void setPracticalSubjectId(int practicalSubjectId) {
		this.practicalSubjectId = practicalSubjectId;
	}

}
