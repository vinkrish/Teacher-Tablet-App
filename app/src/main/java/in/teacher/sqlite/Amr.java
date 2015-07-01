package in.teacher.sqlite;

public class Amr {
	private String text1;
	private String text2;
	private int int1;
	private String text3;
	private int int2;
	private int int3;
	
	public Amr(String txt1, String txt2, int int1, int int2, int int3){
		this.text1 = txt1;
		this.text2 = txt2;
		this.int1 = int1;
		this.int2 = int2;
		this.int3 = int3;
	}
	
	public Amr(int int1, String text1, String text2, int int2, int int3){
		this.int1 = int1;
		this.int2 = int2;
		this.int3 = int3;
		this.text1 = text1;
		this.text2 = text2;
	}
	
	public Amr(String txt1, String txt2, String txt3){
		this.text1 = txt1;
		this.text2 = txt2;
		this.text3 = txt3;
	}
	
	public Amr(String clas, int student, String absentee, int progress){
		this.text2 = clas;
		this.int3 = student;
		this.text3 = absentee;
		this.int2 = progress;
	}
	
	public Amr(String text1, String text2, String text3, int progress){
		this.text1 = text1;
		this.text2 = text2;
		this.text3 = text3;
		this.int1 = progress;
	}
	
	public Amr(String text1, String absentee, int progress){
		this.text1 = text1;
		this.text3 = absentee;
		this.int2 = progress;
	}
	public Amr(String text1, String text2, int int1, int int2){
		this.text1 = text1;
		this.text2 = text2;
		this.int1 = int1;
		this.int2 = int2;
	}
	public Amr(String text1, int int1, int int2){
		this.text1 = text1;
		this.int1 = int1;
		this.int2 = int2;
	}
	public Amr(String text1, int int1, int int2, int int3){
		this.text1 = text1;
		this.int1 = int1;
		this.int2 = int2;
		this.int3 = int3;
	}
	public Amr(String text1, String text3){
		this.text1 = text1;
		this.text3 = text3;
	}
	public Amr(String text1, int int1){
		this.text1 = text1;
		this.int1 = int1;
	}

	public String getText1() {
		return text1;
	}

	public void setText1(String text1) {
		this.text1 = text1;
	}

	public String getText2() {
		return text2;
	}

	public void setText2(String text2) {
		this.text2 = text2;
	}

	public int getInt1() {
		return int1;
	}

	public void setInt1(int int1) {
		this.int1 = int1;
	}

	public String getText3() {
		return text3;
	}

	public void setText3(String text3) {
		this.text3 = text3;
	}

	public int getInt2() {
		return int2;
	}

	public void setInt2(int int2) {
		this.int2 = int2;
	}

	public int getInt3() {
		return int3;
	}

	public void setInt3(int int3) {
		this.int3 = int3;
	}


}
