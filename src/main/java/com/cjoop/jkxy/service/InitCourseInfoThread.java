package com.cjoop.jkxy.service;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cjoop.jkxy.Constant;
import com.cjoop.jkxy.SpringUtil;
import com.cjoop.jkxy.dao.CourseDao;
import com.cjoop.jkxy.domain.Course;
import com.cjoop.jkxy.view.MainView;

@Component
@Scope("prototype")
public class InitCourseInfoThread extends Thread {
	private Course parentCourse;
	@Autowired
	private HttpClient httpClient;
	@Autowired
	private CourseDao courseDao;
	@Autowired
	private MainView mainView;
	@Autowired
	private SpringUtil springUtil;
	
	@Override
	public void run() {
		long start = System.currentTimeMillis();
		initCourceLevelInfo();
		for (Course firstCourse : parentCourse.getChildCourse()) {
			saveCourseInfo(firstCourse,parentCourse);
			for (Course secondCourse : firstCourse.getChildCourse()) {
				saveCourseInfo(secondCourse,firstCourse);
				for (Course thirdCourse : secondCourse.getChildCourse()) {
					saveCourseInfo(thirdCourse,secondCourse);
				}
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("初始化课程"+parentCourse.getTitle()+"信息完成:" + (end - start)/1000 + "秒");
	}

	private void saveCourseInfo(Course course,Course parentCourse) {
		Course oldCourse = courseDao.findCourseBySaveDir(course.getSaveDir());
		course.setPid(parentCourse.getId());
		if(oldCourse==null){
			course.setId(springUtil.getUUID());
			courseDao.saveCourse(course);
		}else{
			course.setId(oldCourse.getId());
			course.setLessonCount(oldCourse.getLessonCount());
		}
	}

	public Course getParentCourse() {
		return parentCourse;
	}

	public void setParentCourse(Course parentCourse) {
		this.parentCourse = parentCourse;
	}
	
	/**
	 * 初始化课程级别信息
	 * @param course
	 */
	public void initCourceLevelInfo() {
		HttpEntity httpEntity = null;
		try {
			HttpGet httpGet = new HttpGet(parentCourse.getHref());
			HttpResponse httpResponse = httpClient.execute(httpGet);
			httpEntity = httpResponse.getEntity();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if(statusCode == HttpStatus.SC_OK){
				String html = EntityUtils.toString(httpEntity,Constant.UTF_8);
				Document doc = Jsoup.parse(html);
				Elements lesson_unit_list = doc.select("section.lesson-unit");
				for (int i = 0; i < lesson_unit_list.size(); i++) {
					Element lesson_unit = lesson_unit_list.get(i);
					//一级课程级别信息
					Element h3 = lesson_unit.select("h3").first();
					Course firstCourse = new Course(h3.ownText());
					firstCourse.setNumber(i);
					buildCourse(firstCourse,parentCourse);
					//二级课程分类信息
					Elements lesson_step_list = lesson_unit.select("table.lesson-step");
					for (int j = 0; j < lesson_step_list.size(); j++) {
						Element lesson_step = lesson_step_list.get(j);
						Element th = lesson_step.select("thead > tr > th").first();
						Course secondCourse = new Course(th.ownText());
						secondCourse.setNumber(j);
						buildCourse(secondCourse,firstCourse);
						//三级课程详细信息
						Elements a_list = lesson_step.select("a[href]");
						for (int k = 0; k < a_list.size(); k++) {
							Element a = a_list.get(k);
							String title = a.select("span").first().text();
							String href = a.attr("href");
							Course thirdCourse = new Course(title, href);
							thirdCourse.setNumber(k);
							buildCourse(thirdCourse,secondCourse);
						}
					}
					
				}
				if(parentCourse.getNumber()==0 ){
					mainView.getCboxCourseLevel().removeAllItems();
					for (Course item : parentCourse.getChildCourse()) {
						mainView.getCboxCourseLevel().addItem(item);
					}
				}
			}
		} catch (Exception e) {
			System.out.println("构建职业课程基本信息失败.");
			initCourceLevelInfo();
		}finally {
			if(httpEntity!=null){
				try {
					EntityUtils.consume(httpEntity);
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}
			}
		}
	}

	/**
	 * 构建课程信息
	 * @param course 当前课程
	 * @param parentCourse 父级课程
	 */
	private void buildCourse(Course course,Course parentCourse){
		if(course.getTitle().matches("^\\d+\\..+")){
			course.setSaveDir(parentCourse.getSaveDir() + File.separator + course.getTitle().replaceAll(":|", "-"));
		}else{
			course.setSaveDir(parentCourse.getSaveDir() + File.separator + (course.getNumber()+1) +"." + course.getTitle().replaceAll(":|", "-"));
		}
		parentCourse.getChildCourse().add(course);
	}
	
}
