package in.teacher.sqlite;

import android.graphics.Bitmap;

public class TempAttendance {
	
	private int classId;
	private int sectionId;
	private int studentId;
	private String name;
	private int rollNoInClass;
	private Bitmap attMarker;
	
	public TempAttendance(){
		
	}
	
	public TempAttendance(int rollno, String name, Bitmap attMarker){
		super();
		this.name = name;
		this.rollNoInClass = rollno;
		this.setAttMarker(attMarker);
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
	public int getStudentId() {
		return studentId;
	}
	public void setStudentId(int studentId) {
		this.studentId = studentId;
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
