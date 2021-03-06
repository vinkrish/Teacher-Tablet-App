package in.teacher.sqlite;

/**
 * Created by vinkrish.
 */
public interface SqlConstant {

    String DATABASE_NAME = "teacher.db";
    int DATABASE_VERSION = 8;

    String CREATE_CLASS = "CREATE TABLE class(SchoolId INTEGER, ClassId INT DEFAULT 0, " +
            "ClassName TEXT, ClassType TEXT, DateTimeRecordInserted DATETIME, SubjectGroupIds TEXT)";

    String CREATE_SCHOOL = "CREATE TABLE school(SchoolId INTEGER PRIMARY KEY, SchoolName TEXT, Website TEXT, " +
            "ShortenedSchoolName TEXT, SenderID TEXT, ContactPersonName TEXT, SchoolAdminUserName TEXT, SchoolAdminPassword TEXT, " +
            "Address TEXT, address_short TEXT, Landline TEXT, Mobile TEXT, Mobile2 TEXT, Email TEXT, City TEXT, State TEXT, " +
            "District TEXT, Pincode TEXT, CreationDateTime TEXT, LastLoginTime TEXT, IPAddress TEXT, NumberofMobiles INTEGER, " +
            "RouteId INTEGER, Locked INTEGER, PrincipalTeacherId INTEGER, PathtoOpen TEXT, Syllabus TEXT, NumberofStudents INTEGER, " +
            "Launched TEXT, ClassIDs TEXT, CCEClassIDs TEXT, DateTimeRecordInserted DATETIME)";

    String CREATE_SECTION = "CREATE TABLE section(SchoolId INTEGER, SectionId INT DEFAULT 0, " +
            "ClassId INTEGER, SectionName TEXT, ClassTeacherId INTEGER, DateTimeRecordInserted DATETIME)";

    String CREATE_STUDENT_ATTENDANCE = "CREATE TABLE studentattendance(SchoolId INTEGER, " +
            "ClassId INTEGER, SectionId INTEGER, StudentId INTEGER, DateAttendance TEXT, TypeOfLeave TEXT, DateTimeRecordInserted DATETIME)";

    String ATTENDANCE_TRIGGER = "CREATE TRIGGER before_attendance BEFORE INSERT ON studentattendance " +
            "WHEN ((SELECT count() FROM studentattendance WHERE studentattendance.StudentId=NEW.StudentId and studentattendance.DateAttendance=NEW.DateAttendance) > 0) " +
            "BEGIN " +
            "DELETE FROM studentattendance WHERE studentattendance.StudentId=NEW.StudentId AND studentattendance.DateAttendance=NEW.DateAttendance; " +
            "END ";

    String CREATE_TEMP_ATTENDANCE = "CREATE TABLE tempattendance(StudentId INTEGER, ClassId INTEGER, " +
            "SectionId INTEGER, RollNoInClass INTEGER, Name TEXT)";

    String CREATE_STUDENTS = "CREATE TABLE students(SOWSID TEXT, StudentId INT DEFAULT 0, SchoolId INTEGER, " +
            "ClassId INTEGER, SectionId INTEGER, SubjectIds TEXT, AdmissionNo TEXT, RollNoInClass INTEGER, Username TEXT, " +
            "Password TEXT, Image TEXT, Name TEXT, FatherName TEXT, MotherName TEXT, DateOfBirth TEXT, Gender TEXT, Email TEXT, " +
            "Mobile1 TEXT, Mobile2 TEXT, Pincode TEXT, Address TEXT, TransportationTypeId INTEGER, Community TEXT, Income INTEGER, " +
            "IsLoggedIn INTEGER, Locked INTEGER, NoSms INTEGER, CreationDateTime TEXT, LastLoginTime TEXT, LoginCount INTEGER, " +
            "FeedbackSkip INTEGER, NotificationsRead TEXT, IPAddress TEXT)";

    String CREATE_STUDENTS_TRIGGER = "CREATE TRIGGER before_students BEFORE INSERT ON students " +
            "WHEN ((SELECT count() FROM students WHERE students.StudentId=NEW.StudentId) > 0) " +
            "BEGIN " +
            "DELETE FROM students WHERE students.StudentId=NEW.StudentId; " +
            "END";

    String CREATE_SUBJECTS = "CREATE TABLE subjects(SubjectId INTEGER, SchoolId INTEGER, SubjectName TEXT, " +
            "has_partition INTEGER, TheorySubjectId INTEGER, PracticalSubjectId INTEGER, DateTimeRecordInserted DATETIME)";

    String CREATE_SUB_GROUPS = "CREATE TABLE subjects_groups(id INTEGER, SchoolId INTEGER, " +
            "SubjectGroup TEXT, subject_ids TEXT, DateTimeRecordInserted DATETIME)";

    String CREATE_SUBJECT_TEACHER = "CREATE TABLE subjectteacher(id INTEGER, ClassId INTEGER, " +
            "SubjectId INTEGER, SchoolId INTEGER, TeacherId INTEGER, SectionId INTEGER, DateTimeRecordInserted DATETIME, " +
            "PRIMARY KEY(SectionId, SubjectId, TeacherId))";

    String CREATE_SUBJECTTEACHER_TRIGGER = "CREATE TRIGGER before_subjectteacher BEFORE INSERT ON subjectteacher " +
            "WHEN ((SELECT count() FROM subjectteacher WHERE subjectteacher.TeacherId=NEW.TeacherId AND subjectteacher.SubjectId=NEW.SubjectId AND subjectteacher.SectionId=NEW.SectionId) > 0) " +
            "BEGIN " +
            "DELETE FROM subjectteacher WHERE subjectteacher.TeacherId=NEW.TeacherId AND subjectteacher.SubjectId=NEW.SubjectId AND subjectteacher.SectionId=NEW.SectionId; " +
            "END";

    String CREATE_TEACHER = "CREATE TABLE teacher(SOWTID TEXT, TeacherId INT DEFAULT 0, Image TEXT, " +
            "Username TEXT, Password TEXT,SchoolId INTEGER, Name TEXT, DOB TEXT, Mobile TEXT, Qualification TEXT, " +
            "Address TEXT, DateOfJoining TEXT, Gender TEXT, Email TEXT, Pincode INTEGER, TransportationTypeId INTEGER, " +
            "Community TEXT, CreationDateTime TEXT, LastLoginTime TEXT, IPAddress TEXT, Locked INTEGER, TabUser TEXT, TabPass TEXT)";

    String CREATE_EXAMS = "CREATE TABLE exams(SchoolId INTEGER, ClassId INTEGER, ExamId INT DEFAULT 0, " +
            "SubjectIDs TEXT, SubjectGroupIds TEXT, ExamName TEXT, OrderId INTEGER, Percentage TEXT, TimeTable TEXT, " +
            "Portions TEXT, FileName TEXT, GradeSystem INTEGER, Term INTEGER, MarkUploaded INTEGER, DateTimeRecordInserted DATETIME)";

    String CREATE_EXAMS_TRIGGER = "CREATE TRIGGER before_exams BEFORE INSERT ON exams " +
            "WHEN ((SELECT count() FROM exams WHERE exams.ExamId=NEW.ExamId) > 0) " +
            "BEGIN " +
            "DELETE FROM exams WHERE exams.ExamId=NEW.ExamId; " +
            "END";

    String CREATE_SUBJECT_EXAMS = "CREATE TABLE subjectexams(SchoolId INTEGER, ClassId INTEGER, " +
            "ExamId INTEGER, SubjectId INTEGER, TimeTable TEXT, Session TEXT, MaximumMark INTEGER, FailMark INTEGER, " +
            "UniqueKey TEXT, DateTimeRecordInserted DATETIME)";

    String CREATE_SUBJECTEXAMS_TRIGGER = "CREATE TRIGGER before_subjectexams BEFORE INSERT ON subjectexams " +
            "WHEN ((SELECT count() FROM subjectexams WHERE subjectexams.ClassId=NEW.ClassId AND subjectexams.SubjectId=NEW.SubjectId AND subjectexams.ExamId=NEW.ExamId) > 0) " +
            "BEGIN " +
            "DELETE FROM subjectexams WHERE subjectexams.ClassId=NEW.ClassId AND subjectexams.SubjectId=NEW.SubjectId AND subjectexams.ExamId=NEW.ExamId; " +
            "END";

    String CREATE_MARKS = "CREATE TABLE marks(SchoolId INTEGER, ExamId INTEGER, Is_Present INTEGER, " +
            "SubjectId INTEGER, StudentId INTEGER, Mark TEXT, Grade TEXT, DateTimeRecordInserted DATETIME, " +
            "PRIMARY KEY(ExamId, SubjectId, StudentId))";

    String MARKS_TRIGGER = "CREATE TRIGGER before_marks BEFORE INSERT ON marks " +
            "WHEN ((SELECT count() FROM marks WHERE marks.ExamId=NEW.ExamId AND marks.SubjectId=NEW.SubjectId AND marks.StudentId=NEW.StudentId) > 0) " +
            "BEGIN " +
            "DELETE FROM marks WHERE marks.ExamId=NEW.ExamId AND marks.SubjectId=NEW.SubjectId AND marks.StudentId=NEW.StudentId; " +
            "END";

    String CREATE_TEMP = "CREATE TABLE temp(id INTEGER PRIMARY KEY, DeviceId TEXT, SchoolId INTEGER, ClassId INTEGER, " +
            "SectionId INTEGER, ClassInchargeId INTEGER, TeacherId INTEGER, StudentId INTEGER, SubjectId INTEGER, CurrentSection INTEGER, " +
            "CurrentSubject INTEGER, CurrentClass INTEGER, ExamId INTEGER, ActivityId INTEGER, SubActivityId INTEGER, SlipTestId INTEGER, " +
            "SyncTime TEXT, IsSync INTEGER)";

    String CREATE_ACTIVITY = "CREATE TABLE activity(ActivityId INT DEFAULT 0, SchoolId INTEGER, " +
            "ClassId INTEGER, SectionId INTEGER, ExamId INTEGER, SubjectId INTEGER, RubrixId INTEGER, " +
            "ActivityName TEXT, MaximumMark INTEGER, Weightage REAL, SubActivity INTEGER, Calculation INTEGER, " +
            "DateTimeRecordInserted DATETIME, ActivityAvg REAL DEFAULT 0, CompleteEntry INTEGER DEFAULT 0, UniqueKey TEXT)";

    String CREATE_ACTIVITY_TRIGGER = "CREATE TRIGGER before_activity BEFORE INSERT ON activity " +
            "WHEN ((SELECT count() FROM activity WHERE activity.ActivityId=NEW.ActivityId) > 0) " +
            "BEGIN " +
            "DELETE FROM activity WHERE activity.ActivityId=NEW.ActivityId; " +
            "END";

    String CREATE_ACTIVITY_MARK = "CREATE TABLE activitymark(SchoolId INTEGER, ExamId INTEGER, SubjectId INTEGER, StudentId INTEGER, ActivityId INTEGER, Mark TEXT, DateTimeRecordInserted DATETIME," +
            "PRIMARY KEY(ActivityId, StudentId))";

    String ACTIVITYMARK_TRIGGER = "CREATE TRIGGER before_actmark BEFORE INSERT ON activitymark " +
            "WHEN ((SELECT count() FROM activitymark WHERE activitymark.ActivityId=NEW.ActivityId AND activitymark.StudentId=NEW.StudentId) > 0) " +
            "BEGIN " +
            "DELETE FROM activitymark WHERE activitymark.ActivityId=NEW.ActivityId AND activitymark.StudentId=NEW.StudentId; " +
            "END";

    String CREATE_ACTIVITY_GRADE = "CREATE TABLE activitygrade(SchoolId INTEGER, ExamId INTEGER, SubjectId INTEGER," +
            "StudentId INTEGER, ActivityId INTEGER, Grade TEXT, DateTimeRecordInserted DATETIME, PRIMARY KEY(ActivityId, StudentId))";

    String ACTIVITYGRADE_TRIGGER = "CREATE TRIGGER before_actgrade BEFORE INSERT ON activitygrade " +
            "WHEN ((SELECT count() FROM activitygrade WHERE activitygrade.ActivityId=NEW.ActivityId AND activitygrade.StudentId=NEW.StudentId) > 0) " +
            "BEGIN " +
            "DELETE FROM activitygrade WHERE activitygrade.ActivityId=NEW.ActivityId AND activitygrade.StudentId=NEW.StudentId; " +
            "END";

    String CREATE_SUB_ACTIVITY = "CREATE TABLE subactivity(SubActivityId INT DEFAULT 0, SchoolId INTEGER, " +
            "ClassId INTEGER, SectionId INTEGER, ExamId INTEGER, SubjectId INTEGER, ActivityId INTEGER, SubActivityName TEXT, " +
            "MaximumMark INTEGER, Weightage INTEGER, Calculation INTEGER, SubActivityAvg REAL DEFAULT 0, " +
            "CompleteEntry INTEGER DEFAULT 0, UniqueKey TEXT, DateTimeRecordInserted DATETIME)";

    String CREATE_SUB_ACTIVITY_TRIGGER = "CREATE TRIGGER before_subactivity BEFORE INSERT ON subactivity " +
            "WHEN ((SELECT count() FROM subactivity WHERE subactivity.SubActivityId=NEW.SubActivityId) > 0) " +
            "BEGIN " +
            "DELETE FROM subactivity WHERE subactivity.SubActivityId=NEW.SubActivityId;" +
            "END";

    String CREATE_SUB_ACTIVITY_MARK = "CREATE TABLE subactivitymark(SchoolId INTEGER, ExamId INTEGER, " +
            "SubjectId INTEGER, StudentId INTEGER, ActivityId INTEGER, SubActivityId INTEGER, Mark TEXT, Description TEXT, " +
            "DateTimeRecordInserted DATETIME, PRIMARY KEY(SubActivityId, StudentId))";

    String SUBACTMARK_TRIGGER = "CREATE TRIGGER before_subactmark BEFORE INSERT ON subactivitymark " +
            "WHEN ((SELECT count() FROM subactivitymark WHERE subactivitymark.SubActivityId=NEW.SubActivityId AND subactivitymark.StudentId=NEW.StudentId) > 0) " +
            "BEGIN " +
            "DELETE FROM subactivitymark WHERE subactivitymark.SubActivityId=NEW.SubActivityId AND subactivitymark.StudentId=NEW.StudentId; " +
            "END";

    String CREATE_SUB_ACTIVITY_GRADE = "CREATE TABLE subactivitygrade(SchoolId INTEGER, ExamId INTEGER, SubjectId INTEGER," +
            "StudentId INTEGER, ActivityId INTEGER, SubActivityId INTEGER, Grade TEXT, Description TEXT, " +
            "DateTimeRecordInserted DATETIME, PRIMARY KEY(SubActivityId, StudentId))";

    String SUBACTGRADE_TRIGGER = "CREATE TRIGGER before_subactgrade BEFORE INSERT ON subactivitygrade " +
            "WHEN ((SELECT count() FROM subactivitygrade WHERE subactivitygrade.SubActivityId=NEW.SubActivityId AND subactivitygrade.StudentId=NEW.StudentId) > 0) " +
            "BEGIN " +
            "DELETE FROM subactivitygrade WHERE subactivitygrade.SubActivityId=NEW.SubActivityId AND subactivitygrade.StudentId=NEW.StudentId; " +
            "END";

    String CREATE_UPLOAD_SQL = "CREATE TABLE uploadsql(SyncId INTEGER PRIMARY KEY AUTOINCREMENT,Query TEXT)";

    String CREATE_PORTION = "CREATE TABLE portion(PortionId INT DEFAULT 0, SchoolId INTEGER, ClassId INTEGER, " +
            "SubjectId INTEGER, NewSubjectId INTEGER, Portion TEXT, DateTimeRecordInserted DATETIME)";

    String CREATE_SLIPTEST = "CREATE TABLE sliptest(SlipTestId INTEGER PRIMARY KEY, SchoolId INTEGER, " +
            "ClassId INTEGER, SectionId INTEGER, SlipTestName TEXT, IsActivity INTEGER, Grade INTEGER, Count INTEGER, " +
            "SubjectId INTEGER,NewSubjectId INTEGER, Portion INTEGER, ExtraPortion TEXT, PortionName TEXT, " +
            "MaximumMark INTEGER, AverageMark REAL, TestDate TEXT, SubmissionDate TEXT, MarkEntered INTEGER,EmployeeId INTEGER, " +
            "DateTimeRecordInserted DATETIME,Weightage REAL)";

    String SLIPTEST_TRIGGER = "CREATE TRIGGER before_sliptest BEFORE INSERT ON sliptest " +
            "WHEN ((SELECT count() FROM sliptest WHERE sliptest.SlipTestId=NEW.SlipTestId)>0) " +
            "BEGIN " +
            "DELETE FROM sliptest WHERE sliptest.SlipTestId=NEW.SlipTestId; " +
            "END";

    String CREATE_HOMEWORK = "CREATE TABLE homeworkmessage(HomeworkId INTEGER, SchoolId TEXT, ClassId TEXT, " +
            "SectionId TEXT, TeacherId TEXT, MessageFrom TEXT, MessageVia TEXT, SubjectIDs TEXT, Homework TEXT, " +
            "IsNew INTEGER DEFAULT 0, HomeworkDate TEXT, DateTimeRecordInserted DATETIME)";

    String HOMEWORK_TRIGGER = "CREATE TRIGGER before_homework BEFORE INSERT ON homeworkmessage " +
            "WHEN ((SELECT count() FROM homeworkmessage WHERE homeworkmessage.SectionId=NEW.SectionId and homeworkmessage.HomeworkDate=NEW.HomeworkDate)>0) " +
            "BEGIN " +
            "DELETE FROM homeworkmessage WHERE homeworkmessage.SectionId=NEW.SectionId and homeworkmessage.HomeworkDate=NEW.HomeworkDate; " +
            "END";

    String CREATE_STAVG = "CREATE TABLE stavg(ClassId INTEGER, SectionId INTEGER, SubjectId INTEGER, " +
            "SlipTestAvg REAL DEFAULT 0, PRIMARY KEY(SectionId, SubjectId))";

    String CREATE_GCW = "CREATE TABLE gradesclasswise(GradeId INTEGER, SchoolId INTEGER, ClassId INTEGER, " +
            "Grade TEXT, MarkFrom INTEGER, MarkTo INTEGER, GradePoint INTEGER, DateTimeRecordInserted DATETIME)";

    String CREATE_CCECOSCHOLASTIC = "CREATE TABLE ccecoscholastic(CoScholasticId INTEGER, SchoolId INTEGER, " +
            "Name TEXT, ClassIDs TEXT, DateTimeRecordInserted DATETIME)";

    String CREATE_CCESECTIONHEADING = "CREATE TABLE ccesectionheading(SectionHeadingId INTEGER, SchoolId INTEGER, " +
            "CoScholasticId INTEGER, SectionName TEXT, DateTimeRecordInserted DATETIME)";

    String CREATE_CCETOPICPRIMARY = "CREATE TABLE ccetopicprimary(TopicId INTEGER, SchoolId INTEGER, " +
            "CoScholasticId INTEGER, SectionHeadingId INTEGER, TopicName TEXT, Evaluation INTEGER, DateTimeRecordInserted DATETIME)";

    String CREATE_CCEASPECTPRIMARY = "CREATE TABLE cceaspectprimary(AspectId INTEGER, SchoolId INTEGER, " +
            "CoScholasticId INTEGER, SectionHeadingId INTEGER, TopicId INTEGER, AspectName TEXT, DateTimeRecordInserted DATETIME)";

    String CREATE_CCETOPICGRADE = "CREATE TABLE ccetopicgrade(ccetopicgradeid INTEGER, SchoolId INTEGER, " +
            "TopicId INTEGER, Grade TEXT, Value INTEGER, CoScholasticId INTEGER, SectionHeadingId INTEGER)";

    String CREATE_CCECOSCHOLASTICGRADE = "CREATE TABLE ccecoscholasticgrade(Id INTEGER, SchoolId INTEGER, " +
            "ClassId INTEGER, SectionId INTEGER, StudentId INTEGER, Type INTEGER, Term INTEGER, TopicId INTEGER, " +
            "AspectId INTEGER, Grade INTEGER, Description TEXT, DateTimeRecordInserted DATETIME)";

    String CCEGRADE_TRIGGER = "CREATE TRIGGER before_ccegrade BEFORE INSERT ON ccecoscholasticgrade " +
            "WHEN ((SELECT count() FROM ccecoscholasticgrade WHERE ccecoscholasticgrade.Term=New.Term AND ccecoscholasticgrade.AspectId=NEW.AspectId AND ccecoscholasticgrade.StudentId=NEW.StudentId AND ccecoscholasticgrade.Term=NEW.Term) > 0) " +
            "BEGIN " +
            "DELETE FROM ccecoscholasticgrade WHERE ccecoscholasticgrade.Term=New.Term AND ccecoscholasticgrade.AspectId=NEW.AspectId AND ccecoscholasticgrade.StudentId=NEW.StudentId AND ccecoscholasticgrade.Term=NEW.Term; " +
            "END";

    String CREATE_DOWNLOADED_FILE = "CREATE TABLE downloadedfile(id INTEGER PRIMARY KEY, filename TEXT UNIQUE, " +
            "downloaded INTEGER DEFAULT 0, processed INTEGER DEFAULT 0, isack INTEGER DEFAULT 0)";

    String CREATE_UPLOADED_FILE = "CREATE TABLE uploadedfile(id INTEGER PRIMARY KEY, filename TEXT UNIQUE, " +
            "processed INTEGER DEFAULT 0)";

    String CREATE_AVGTRACK = "CREATE TABLE avgtrack(ExamId INTEGER DEFAULT 0, ActivityId INTEGER DEFAULT 0, " +
            "SubActivityId INTEGER DEFAULT 0, SubjectId INTEGER DEFAULT 0, SectionId INTEGER DEFAULT 0, Type INTEGER)";

    String CREATE_LOCKED = "CREATE TABLE locked(FileName TEXT UNIQUE, LineNumber INTEGER, StackTrace TEXT, " +
            "IsSent INTEGER DEFAULT 0)";

    String CREATE_CCE_STUDENT_PROFILE = "CREATE TABLE ccestudentprofile(CceId INTEGER, SchoolId TEXT, " +
            "ClassId TEXT, SectionId TEXT, StudentId TEXT, StudentName TEXT, Section TEXT, House TEXT, AdmissionNo TEXT, " +
            "DateofBirth TEXT, Address TEXT, PhoneNo TEXT, RegistrationNo TEXT, MotherName TEXT, FatherName TEXT, Height TEXT, " +
            "Weight TEXT, BloodGroup TEXT, VisionL TEXT, VisionR TEXT, Ailment TEXT, OralHygiene TEXT, TotalDays1 REAL, " +
            "DaysAttended1 REAL, TotalDays2 REAL, DaysAttended2 REAL, TotalDays3 REAL, DaysAttended3 REAL, " +
            "DateTimeRecordInserted DATETIME, Gender TEXT, HealthStatus TEXT, Term INTEGER DEFAULT 1, FromDate TEXT, ToDate TEXT)";

    String CSP_TRIGGER = "CREATE TRIGGER before_csp BEFORE INSERT ON ccestudentprofile " +
            "WHEN ((SELECT count() FROM ccestudentprofile WHERE ccestudentprofile.SectionId=NEW.SectionId AND ccestudentprofile.Term=NEW.Term AND ccestudentprofile.StudentId=NEW.StudentId)>0) " +
            "BEGIN " +
            "DELETE FROM ccestudentprofile WHERE ccestudentprofile.SectionId=NEW.SectionId AND ccestudentprofile.Term=NEW.Term AND ccestudentprofile.StudentId=NEW.StudentId; " +
            "END";

    String CREATE_TIMETABLE = "CREATE TABLE timetable(SchoolId INTEGER, ClassId INTEGER, SectionId INTEGER, " +
            "DayId INTEGER, PeriodId INTEGER, SubjectId INTEGER, DateTimeRecordInserted DATETIME)";

    String CREATE_TIMETABLE_TIMING = "CREATE TABLE timetabletimings(SchoolId INTEGER, ClassId INTEGER, Period INTEGER, " +
            "FromTiming TEXT, ToTiming TEXT, DateTimeRecordInserted DATETIME)";

    String CREATE_TEACHER_INCHARGE = "CREATE TABLE classteacher_incharge(SchoolId INTEGER, ClassId INTEGER, TeacherId INTEGER)";

    String CREATE_TERM_REMARK = "CREATE TABLE term_remark(term_remark_id INTEGER, SchoolId INTEGER, ClassId INTEGER, " +
            "SectionId INTEGER, StudentId INTEGER, Term INTEGER, Remark TEXT)";

    String TERM_REMARK_TRIGGER = "CREATE TRIGGER before_term_remark BEFORE INSERT ON term_remark " +
            "WHEN ((SELECT count() FROM term_remark WHERE term_remark.StudentId=NEW.StudentId AND term_remark.Term=NEW.Term) > 0) " +
            "BEGIN " +
            "DELETE FROM term_remark WHERE term_remark.StudentId=NEW.StudentId AND term_remark.Term=NEW.Term; " +
            "END";

    String CREATE_MOVE_STUDENT = "CREATE TABLE movestudent(SchoolId INTEGER, Query TEXT, StudentId INTEGER, StudentName TEXT, ClassName TEXT, " +
            "SecIdFrom INTEGER, SecIdTo INTEGER, SectionFrom TEXT, SectionTo TEXT, Status INTEGER, PRIMARY KEY(StudentId, SecIdFrom, SecIdTo))";

    String MOVE_STUDENT_TRIGGER = "CREATE TRIGGER before_move_student BEFORE INSERT ON movestudent " +
            "WHEN ((SELECT count() FROM movestudent WHERE movestudent.StudentId=NEW.StudentId) > 0) " +
            "BEGIN " +
            "DELETE FROM movestudent WHERE movestudent.StudentId=NEW.StudentId; " +
            "END";

}
