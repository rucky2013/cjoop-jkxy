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
import com.cjoop.jkxy.domain.Lesson;

/**
 * 课时数据接口
 * @author 陈均
 *
 */
@Repository
public class LessonDao implements InitializingBean{
	private static final String UPDATE_SQL = "UPDATE JKXY_LESSON SET TITLE=?,HREF=?,TIME=?,NUMBER=?,FILE_NAME=?,FILE_SIZE=?,COURSE_ID=? WHERE ID=?;";
	private static final String INSERT_SQL = "INSERT INTO JKXY_LESSON(TITLE,HREF,TIME,NUMBER,FILE_NAME,FILE_SIZE,COURSE_ID) VALUES(?,?,?,?,?,?,?);";
	private final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS JKXY_LESSON(ID INT IDENTITY PRIMARY KEY, TITLE VARCHAR(255),HREF VARCHAR(255),TIME VARCHAR(255),NUMBER INT,FILE_NAME VARCHAR(255),FILE_SIZE LONG,COURSE_ID VARCHAR(255));"; 
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public void afterPropertiesSet() throws Exception {
		createLessonTable();
	}
	
	/**
	 * 根据文件路径查找课时信息
	 * @param fileName
	 * @return Lesson
	 */
	public Lesson findLessonByFileName(String fileName){
		List<Lesson> list = jdbcTemplate.query("select * from JKXY_LESSON where FILE_NAME=?", new RowMapper<Lesson>(){
			@Override
			public Lesson mapRow(ResultSet rs, int rowNum) throws SQLException {
				Lesson lesson = new Lesson();
				lesson.setId(rs.getInt("ID"));
				lesson.setTitle(rs.getString("TITLE"));
				lesson.setHref(rs.getString("HREF"));
				lesson.setTime(rs.getString("TIME"));
				lesson.setNumber(rs.getInt("NUMBER"));
				lesson.setFileName(rs.getString("FILE_NAME"));
				lesson.setFileSize(rs.getLong("FILE_SIZE"));
				lesson.setCourseId(rs.getString("COURSE_ID"));
				return lesson;
			}
		}, fileName);
		if(list.size()>0){
			return list.get(0);
		}
		return null;
	}
	
	/**
	 * 保存课时信息
	 * @param lesson
	 */
	public int saveLesson(Lesson lesson){
		return jdbcTemplate.update(INSERT_SQL,
				lesson.getTitle(),lesson.getHref(),
				lesson.getTime(),lesson.getNumber(),
				lesson.getFileName(),lesson.getFileSize(),
				lesson.getCourseId());
	}
	
	/**
	 * 更新课时信息
	 * @param lesson
	 */
	public int updateLesson(Lesson lesson){
		return jdbcTemplate.update(UPDATE_SQL,
				lesson.getTitle(),lesson.getHref(),
				lesson.getTime(),lesson.getNumber(),
				lesson.getFileName(),lesson.getFileSize(),
				lesson.getCourseId(),lesson.getId());
	}
	
	/**
	 * 创建课时表信息
	 */
	public void createLessonTable(){
		jdbcTemplate.execute(CREATE_TABLE_SQL);
	}

	public List<Lesson> GetLessonList(Course course) {
		return jdbcTemplate.query("SELECT * FROM JKXY_LESSON WHERE COURSE_ID = ?", new RowMapper<Lesson>(){
			@Override
			public Lesson mapRow(ResultSet rs, int rowNum) throws SQLException {
				Lesson lesson = new Lesson();
				lesson.setId(rs.getInt("ID"));
				lesson.setTitle(rs.getString("TITLE"));
				lesson.setHref(rs.getString("HREF"));
				lesson.setTime(rs.getString("TIME"));
				lesson.setNumber(rs.getInt("NUMBER"));
				lesson.setFileName(rs.getString("FILE_NAME"));
				lesson.setFileSize(rs.getLong("FILE_SIZE"));
				lesson.setCourseId(rs.getString("COURSE_ID"));
				return lesson;
			}
		}, course.getId());
	}

	
}
