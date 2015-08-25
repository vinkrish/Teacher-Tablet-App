package in.teacher.sqlite;

public class CircleObject {
    private int progressInt;
    private String clas;
    private String section;
    private String subject;

    public CircleObject(int progressInt, String clas, String section, String subject) {
        this.progressInt = progressInt;
        this.clas = clas;
        this.section = section;
        this.subject = subject;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getClas() {
        return clas;
    }

    public void setClas(String clas) {
        this.clas = clas;
    }

    public int getProgressInt() {
        return progressInt;
    }

    public void setProgressInt(int progressInt) {
        this.progressInt = progressInt;
    }

}
