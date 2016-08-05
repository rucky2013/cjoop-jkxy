package com.cjoop.jkxy.service;

import java.util.List;
import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cjoop.jkxy.SpringUtil;
import com.cjoop.jkxy.dao.CourseDao;
import com.cjoop.jkxy.domain.Course;
import com.cjoop.jkxy.domain.Lesson;
import com.cjoop.jkxy.view.CourseView;
/**
* 下载单个课程的主线程
*/
@Component
@Scope("prototype")
public class DownloadCourseThread extends Thread{
	private Course course;
	private int lessonThreadCount = 0;
	private CourseView courseView;
	@Autowired
	private ExecutorService executorService;
	@Autowired
	private LessonService lessonService;
	@Autowired
	private SpringUtil springUtil;
	@Autowired
	private CourseDao courseDao;
	
	public synchronized void updateLessonThreadCount(){
		this.lessonThreadCount--;
		courseView.subLessonThreadCount();
		if(lessonThreadCount==0){
			courseView.updateChecked(course);
		}
	}
	
	public DownloadCourseThread() {
		
	}
	
	@Override
	public void run() {
		setName("course_" + course.getTitle());
		List<Lesson> lessonList = lessonService.getLessonList(course);
		course.setLessonCount(lessonList.size());
		courseDao.updateLesson(course);
		lessonThreadCount = lessonThreadCount+lessonList.size();
		courseView.addLessonThreadCount(lessonList.size());
		for (int i = 0; i < lessonList.size(); i++) {
			Lesson lesson = lessonList.get(i);
			DownloadLessonThread downloadLessonThread = springUtil.getBean(DownloadLessonThread.class);
			downloadLessonThread.setLesson(lesson);
			downloadLessonThread.setDownloadCourseThread(this);
			executorService.execute(downloadLessonThread);
		}
	}

	
	public void setCourse(Course course) {
		this.course = course;
	}

	public void setCourseView(CourseView courseView) {
		this.courseView = courseView;
	}
}

