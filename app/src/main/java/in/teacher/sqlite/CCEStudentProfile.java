package in.teacher.sqlite;

public class CCEStudentProfile {
    private String schoolId;
    private int rollNo;
    private String classId;
    private String sectionId;
    private String studentId;
    private String studentName;
    private String height;
    private String weight;
    private double daysAttended1;
    private double totalDays1;
    private String visionL;
    private String visionR;
    private String termRemark;
    private int term;
    private boolean cceExist;
    private boolean remarkExist;

    public int getRollNo() {
        return rollNo;
    }

    public void setRollNo(int rollNo) {
        this.rollNo = rollNo;
    }

    public double getTotalDays1() {
        return totalDays1;
    }

    public void setTotalDays1(double todayDays1) {
        this.totalDays1 = todayDays1;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
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

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public double getDaysAttended1() {
        return daysAttended1;
    }

    public void setDaysAttended1(double daysAttended1) {
        this.daysAttended1 = daysAttended1;
    }

    public String getVisionL() {
        return visionL;
    }

    public void setVisionL(String visionL) {
        this.visionL = visionL;
    }

    public String getVisionR() {
        return visionR;
    }

    public void setVisionR(String visionR) {
        this.visionR = visionR;
    }

    public String getTermRemark() {
        return termRemark;
    }

    public void setTermRemark(String termRemark) {
        this.termRemark = termRemark;
    }

    public boolean isCceExist() {
        return cceExist;
    }

    public void setCceExist(boolean cceExist) {
        this.cceExist = cceExist;
    }

    public boolean isRemarkExist() {
        return remarkExist;
    }

    public void setRemarkExist(boolean remarkExist) {
        this.remarkExist = remarkExist;
    }
}
