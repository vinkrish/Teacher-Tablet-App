package in.teacher.sqlite;

import android.graphics.Bitmap;

public class TempAttendance {
    private int classId;
    private int sectionId;
    private long studentId;
    private String name;
    private int rollNoInClass;
    private Bitmap attMarker;

    public long getStudentId() {
        return studentId;
    }

    public void setStudentId(long studentId) {
        this.studentId = studentId;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRollNoInClass() {
        return rollNoInClass;
    }

    public void setRollNoInClass(int rollNoInClass) {
        this.rollNoInClass = rollNoInClass;
    }

    public Bitmap getAttMarker() {
        return attMarker;
    }

    public void setAttMarker(Bitmap attMarker) {
        this.attMarker = attMarker;
    }

}
