package com.cjoop.jkxy.service;

import java.io.File;
import java.util.List;

import javax.swing.JCheckBox;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cjoop.jkxy.dao.LessonDao;
import com.cjoop.jkxy.domain.Course;
import com.cjoop.jkxy.domain.Lesson;
import com.cjoop.jkxy.view.CourseView;

@Component
@Scope("prototype")
public class CheckCourseThread extends Thread {

	private JCheckBox checkBox;
	private CourseView courseView;
	private Course course;
	@Autowired
	PropertiesConfiguration mainConfig;
	@Autowired
	private LessonDao lessonDao;

	@Override
	public void run() {
		boolean selected = true;
		if(course.getLessonCount()!=0){//下载过
			List<Lesson> lessonList = lessonDao.GetLessonList(course);
			String saveDir = mainConfig.getProperty("saveDir")+"";
			for (Lesson lesson : lessonList) {
				File dest = new File(saveDir,lesson.getFileName());
				boolean result = lesson.getId()==0 || !dest.exists() || (lesson.getFileSize()!=dest.length());
				if(result){//有新的课时，文件不存在，或者大小不一致
					selected = false;
				};
				lesson.setDownload(!result);
				course.getLessonCache().put(lesson.getFileName(), lesson);
			}
		}else{
			selected = false;
		}
		checkBox.setSelected(selected);
		courseView.updateCheckThreadCount();
	}

	public JCheckBox getCheckBox() {
		return checkBox;
	}

	public void setCheckBox(JCheckBox checkBox) {
		this.checkBox = checkBox;
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	public void setCourseView(CourseView courseView) {
		this.courseView = courseView;
	}

}
