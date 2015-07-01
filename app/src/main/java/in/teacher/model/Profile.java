package in.teacher.model;

public class Profile {
	private int studentId;
	private String roll;
	private String name;
	private String height;
	private String weight;
	private String daysAttended;

	public Profile(int id, String roll, String name, String height, String weight, String daysAttended){
		this.studentId = id;
		this.roll = roll;
		this.name = name;
		this.height = height;
		this.weight = weight;
		this.daysAttended = daysAttended;
	}

	public int getStudentId() {
		return studentId;
	}

	public void setStudentId(int studentId) {
		this.studentId = studentId;
	}

	public String getRoll() {
		return roll;
	}
	public void setRoll(String roll) {
		this.roll = roll;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
	public String getDaysAttended() {
		return daysAttended;
	}
	public void setDaysAttended(String daysAttended) {
		this.daysAttended = daysAttended;
	}
}


