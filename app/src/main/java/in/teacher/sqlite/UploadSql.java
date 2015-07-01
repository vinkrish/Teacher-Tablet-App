package in.teacher.sqlite;

public class UploadSql {
	private int schoolId;
	private String action;
	private String tableName;
	private String query;
	private long syncId;
	private int examId;
	private int sectionId;
	private int subjectId;
	private int activityId;
	private int subActivityId;
	private String synId;
	
	public String getSynId() {
		return synId;
	}

	public void setSynId(String synId) {
		this.synId = synId;
	}

	public int getExamId() {
		return examId;
	}

	public void setExamId(int examId) {
		this.examId = examId;
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

	public int getActivityId() {
		return activityId;
	}

	public void setActivityId(int activityId) {
		this.activityId = activityId;
	}

	public int getSubActivityId() {
		return subActivityId;
	}

	public void setSubActivityId(int subActivityId) {
		this.subActivityId = subActivityId;
	}

	public UploadSql(){
		
	}
	
	public UploadSql(String tableName, String action, String query){
		this.tableName = tableName;
		this.action = action;
		this.query = query;
	}

	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public int getSchoolId() {
		return schoolId;
	}
	public void setSchoolId(int schoolId) {
		this.schoolId = schoolId;
	}

	public long getSyncId() {
		return syncId;
	}

	public void setSyncId(long syncId) {
		this.syncId = syncId;
	}

}
