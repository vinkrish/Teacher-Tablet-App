package in.teacher.sqlite;

public class ExmAvg {
	private int classId;
	private int sectionId;
	private int subjectId;
	private int examId;
	private double examAvg;
	private int completeEntry;
	
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
	public int getExamId() {
		return examId;
	}
	public void setExamId(int examId) {
		this.examId = examId;
	}
	public double getExamAvg() {
		return examAvg;
	}
	public void setExamAvg(double examAvg) {
		this.examAvg = examAvg;
	}
	public int getCompleteEntry() {
		return completeEntry;
	}
	public void setCompleteEntry(int completeEntry) {
		this.completeEntry = completeEntry;
	}

}
