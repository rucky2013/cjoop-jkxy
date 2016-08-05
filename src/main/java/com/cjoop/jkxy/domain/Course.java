package com.cjoop.jkxy.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 课程信息
 * 
 * @author chenjun
 *
 */
public class Course {

	/**
	 * 课程ID
	 */
	private String id;

	/**
	 * 课程名称
	 */
	private String title;
	/**
	 * 课程地址
	 */
	private String href;
	/**
	 * 课程序号
	 */
	private int number;

	/**
	 * 保存课程的文件夹相对路径
	 */
	private String saveDir;

	/**
	 * 父级课程id
	 */
	private String pid;

	/**
	 * 课时总数
	 */
	private int lessonCount;

	/**
	 * 包含的课程
	 */
	private List<Course> childCourse = new ArrayList<Course>();

	/**
	 * 包含的课时信息
	 */
	private Map<String, Lesson> lessonCache = new HashMap<String, Lesson>();

	public Course() {
	}

	public Course(String title) {
		this.title = title;
	}

	public Course(String title, String href) {
		this.title = title;
		this.href = href;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	@Override
	public String toString() {
		return title;
	}

	public List<Course> getChildCourse() {
		return childCourse;
	}

	public void setChildCourse(List<Course> childCourse) {
		this.childCourse = childCourse;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getSaveDir() {
		return saveDir;
	}

	public void setSaveDir(String saveDir) {
		this.saveDir = saveDir;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public int getLessonCount() {
		return lessonCount;
	}

	public void setLessonCount(int lessonCount) {
		this.lessonCount = lessonCount;
	}

	public Map<String, Lesson> getLessonCache() {
		return lessonCache;
	}

	public void setLessonCache(Map<String, Lesson> lessonCache) {
		this.lessonCache = lessonCache;
	}

}
