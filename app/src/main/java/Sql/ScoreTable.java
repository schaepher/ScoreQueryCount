package Sql;

/**
 * Created by Schaepher on 2015/7/16.
 */
public class ScoreTable {
    private String semester;   //学期
    private String courseName;  //课程名
    private Float courseCredit;   //学分
    private String courseScore; //成绩
    private Float courseScorePoint;   //绩点
    private String courseTeacher;   //任课教师

    public ScoreTable(String csemester, String cname, Float ccredit, String cscore,
                      Float cspoint, String cteacher)
    {
        semester = csemester;
        courseName = cname;
        courseCredit = ccredit;
        courseScore = cscore;
        courseScorePoint = cspoint;
        courseTeacher = cteacher;
    }

    public void setSemester(String time)
    {
        semester = time;
    }

    public String getSemester()
    {
        return semester;
    }

    public void setCourseName(String cname)
    {
        courseName = cname;
    }

    public String getCourseName()
    {
        return courseName;
    }

    public void setCourseCredit(Float ccredit)
    {
        courseCredit = ccredit;
    }

    public Float getCourseCredit()
    {
        return courseCredit;
    }

    public void setCourseScore(String cscore)
    {
        courseScore = cscore;
    }

    public String getCourseScore()
    {
        return courseScore;
    }

    public void setCourseScorePoint(Float cspoint)
    {
        courseScorePoint = cspoint;
    }

    public Float getCourseScorePoint()
    {
        return courseScorePoint;
    }

    public void setCourseTeacher(String cteacher)
    {
        courseTeacher = cteacher;
    }

    public String getCourseTeacher()
    {
        return courseTeacher;
    }


}
