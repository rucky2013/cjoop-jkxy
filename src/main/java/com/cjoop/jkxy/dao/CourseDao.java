package com.cjoop.jkxy.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.cjoop.jkxy.domain.Course;

/**
 * 课程数据接口
 * @author 陈均
 *
 */
@Repository
public class CourseDao implements InitializingBean{
	private static final String UPDATE_SQL = "UPDATE JKXY_COURSE SET TITLE=?,HREF=?,NUMBER=?,SAVE_DIR=?,PID=?,LESSON_COUNT=? WHERE ID=?;";
	private static final String INSERT_SQL = "INSERT INTO JKXY_COURSE(ID,TITLE,HREF,NUMBER,SAVE_DIR,PID,LESSON_COUNT) VALUES(?,?,?,?,?,?,?);";
	private final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS JKXY_COURSE(ID VARCHAR(255) PRIMARY KEY, TITLE VARCHAR(255),HREF VARCHAR(255),NUMBER INT,SAVE_DIR VARCHAR(255),PID VARCHAR(255),LESSON_COUNT VARCHAR(255));"; 
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	
	@Override
	public void afterPropertiesSet() throws Exception {
		createCourseTable();
	}

	/**
	 * 根据文件路径查找课程信息
	 * @param saveDir
	 * @return Course
	 */
	public Course findCourseBySaveDir(String saveDir){
		List<Course> list = jdbcTemplate.query("select * from JKXY_COURSE where SAVE_DIR=?", new RowMapper<Course>(){
			@Override
			public Course mapRow(ResultSet rs, int rowNum) throws SQLException {
				Course course = new Course();
				course.setId(rs.getString("ID"));
				course.setTitle(rs.getString("TITLE"));
				course.setHref(rs.getString("HREF"));
				course.setNumber(rs.getInt("NUMBER"));
				course.setSaveDir(rs.getString("SAVE_DIR"));
				course.setPid(rs.getString("PID"));
				course.setLessonCount(rs.getInt("LESSON_COUNT"));
				return course;
			}
		}, saveDir);
		if(list.size()>0){
			return list.get(0);
		}
		return null;
	}
	
	/**
	 * 保存课程信息
	 * @param course
	 */
	public int saveCourse(Course course){
		int result = jdbcTemplate.update(INSERT_SQL,
				course.getId(),
				course.getTitle(),
				course.getHref(),
				course.getNumber(),
				course.getSaveDir(),
				course.getPid(),course.getLessonCount());
		return result;
	}
	
	/**
	 * 更新课程信息
	 * @param course
	 */
	public int updateLesson(Course course){
		return jdbcTemplate.update(UPDATE_SQL,
				course.getTitle(),
				course.getHref(),
				course.getNumber(),
				course.getSaveDir(),
				course.getPid(),
				course.getLessonCount(),
				course.getId());
	}
	
	/**
	 * 创建课程表信息
	 */
	private void createCourseTable() {
		jdbcTemplate.execute(CREATE_TABLE_SQL);
	}
	
}
