package in.teacher.sqlite;

import android.graphics.Bitmap;

public class SeObject {
	private int progressInt;
	private String clasSecSub;
	private String exam;
	private Bitmap tickCross;
	
	public String getExam() {
		return exam;
	}

	public void setExam(String exam) {
		this.exam = exam;
	}

	public int getProgressInt() {
		return progressInt;
	}

	public void setProgressInt(int progressInt) {
		this.progressInt = progressInt;
	}

	public String getClasSecSub() {
		return clasSecSub;
	}

	public void setClasSecSub(String clasSecSub) {
		this.clasSecSub = clasSecSub;
	}

	public Bitmap getTickCross() {
		return tickCross;
	}

	public void setTickCross(Bitmap tickCross) {
		this.tickCross = tickCross;
	}

	public SeObject(int progressInt,String exam, Bitmap tickCross){
		this.progressInt = progressInt;
		this.exam = exam;
		this.tickCross = tickCross;
	}

}
