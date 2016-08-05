package com.cjoop.jkxy.domain;

/**
 * 课时信息
 * 
 * @author chenjun
 *
 */
public class Lesson {

	private int id;

	/**
	 * 课时名称
	 */
	private String title;
	/**
	 * 课时页面链接
	 */
	private String href;
	/**
	 * 课时时长
	 */
	private String time;
	/**
	 * 课时下载地址
	 */
	private String downloadUrl;
	/**
	 * 课时的序号
	 */
	private int number;
	/**
	 * 课时保存的相对路径
	 */
	private String fileName;
	/**
	 * 课时大小
	 */
	private long fileSize;

	/**
	 * 所属课程id
	 */
	private String courseId;

	/**
	 * 是否已经下载
	 */
	private boolean isDownload = false;

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

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCourseId() {
		return courseId;
	}

	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}

	public boolean isDownload() {
		return isDownload;
	}

	public void setDownload(boolean isDownload) {
		this.isDownload = isDownload;
	}

}
