package in.teacher.model;

public class HW {
	private String subject;
	private int subjectId;
	private String homework;
	private boolean entered;

    public HW(int subjectId, String subject, String homework){
        this.subjectId = subjectId;
        this.subject = subject;
        this.homework = homework;
    }

    public HW(String subject, String homework){
        this.subject = subject;
        this.homework = homework;
    }

	public boolean isEntered() {
		return entered;
	}

	public void setEntered(boolean entered) {
		this.entered = entered;
	}

	public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getHomework() {
		return homework;
	}
	public void setHomework(String homework) {
		this.homework = homework;
	}

}
