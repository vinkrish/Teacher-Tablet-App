package in.teacher.sqlite;

/**
 * Created by vinkrish on 30/11/15.
 */
public class MovStudent {
    private int schooId;
    private String query;
    private long studentId;
    private String studentName;
    private String className;
    private int secIdFrom;
    private int secIdTo;
    private String sectionFrom;
    private String sectionTo;
    private int status;

    public long getStudentId() {
        return studentId;
    }

    public void setStudentId(long studentId) {
        this.studentId = studentId;
    }

    public String getSectionFrom() {
        return sectionFrom;
    }

    public void setSectionFrom(String sectionFrom) {
        this.sectionFrom = sectionFrom;
    }

    public String getSectionTo() {
        return sectionTo;
    }

    public void setSectionTo(String sectionTo) {
        this.sectionTo = sectionTo;
    }

    public int getSecIdFrom() {
        return secIdFrom;
    }

    public void setSecIdFrom(int secIdFrom) {
        this.secIdFrom = secIdFrom;
    }

    public int getSecIdTo() {
        return secIdTo;
    }

    public void setSecIdTo(int secIdTo) {
        this.secIdTo = secIdTo;
    }

    public int getSchooId() {
        return schooId;
    }

    public void setSchooId(int schooId) {
        this.schooId = schooId;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
