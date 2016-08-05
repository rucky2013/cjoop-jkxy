package com.cjoop.jkxy.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cjoop.jkxy.Constant;
import com.cjoop.jkxy.dao.LessonDao;
import com.cjoop.jkxy.domain.Course;
import com.cjoop.jkxy.domain.Lesson;

@Service
public class LessonService {
	@Autowired
	private HttpClient httpClient;
	@Autowired
	private RequestConfig requestConfig;
	@Autowired
	private LessonDao lessonDao;
	/**
	 * 根据指定的课程地址获取到课时信息
	 * @param course 课程信息
	 * @return 课时集合
	 */
	protected List<Lesson> getLessonList(Course course){
		HttpEntity httpEntity = null;
		List<Lesson> lessonList = new ArrayList<Lesson>();
		try {
			HttpGet httpGet = new HttpGet(course.getHref());
			httpGet.setConfig(requestConfig);
			httpGet.setHeader("Host",Constant.jikexueyuan_host);
			HttpResponse httpResponse = httpClient.execute(httpGet);
			httpEntity = httpResponse.getEntity();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if(statusCode == HttpStatus.SC_OK){
				String html = EntityUtils.toString(httpEntity,Constant.UTF_8);
				Document doc = Jsoup.parse(html);
				Elements text_box_list = doc.select(".video-list > .lesson-box").first().select(".text-box");
				for (int i = 0; i < text_box_list.size(); i++) {
					Element text_box = text_box_list.get(i);
					Element a = text_box.select("a[href]").first();
					String href = a.attr("href");
					String title = a.text().replaceAll("", "");
					String time = text_box.select("p").first().text();
					Lesson lesson = new Lesson();
					lesson.setTitle(title);
					lesson.setHref(href);
					lesson.setTime(time);
					lesson.setNumber(i);
					lesson.setCourseId(course.getId());
					String fileName = course.getSaveDir() + File.separator + (i+1)+"." + lesson.getTitle()+".mp4";
					lesson.setFileName(fileName);
					Lesson currLesson = course.getLessonCache().get(fileName);
					if(currLesson==null){
						parseLessonDownloadUrl(lesson);
						parseLessonFileSize(lesson);
						lessonDao.saveLesson(lesson);
					}else if(!currLesson.isDownload()){
						parseLessonDownloadUrl(lesson);
						lesson.setFileSize(currLesson.getFileSize());
						lesson.setId(currLesson.getId());
					}else{
						lesson = currLesson;
					}
					lessonList.add(lesson);
				}
			}
		} catch (Exception e) {
			System.out.println("获取"+course.getTitle()+"的课时信息失败:" + e.getMessage());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
			}
			return getLessonList(course);
		}finally {
			if(httpEntity!=null){
				try {
					EntityUtils.consume(httpEntity);
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}
			}
		}
		return lessonList;
	}
	
	/**
	 * 解析课时下载地址信息
	 * @param lesson 课时信息
	 */
	private void parseLessonDownloadUrl(Lesson lesson){
		HttpEntity httpEntity = null;
		try {
			HttpGet httpGet = new HttpGet(lesson.getHref());
			httpGet.setConfig(requestConfig);
			HttpResponse httpResponse = httpClient.execute(httpGet);
			httpEntity = httpResponse.getEntity();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if(statusCode == HttpStatus.SC_OK){
				String html = EntityUtils.toString(httpEntity,Constant.UTF_8);
				Document doc = Jsoup.parse(html);
				Element source = doc.select("source").first();
				String downloadUrl = source.attr("src");
				lesson.setDownloadUrl(downloadUrl);
			}
		} catch (Exception e) {
			System.out.println("构建课时：(" + lesson.getTitle() + ")的下载地址失败.:"+e.getMessage());
			parseLessonDownloadUrl(lesson);
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
	 * 解析课时文件大小
	 * @param lesson 课时信息
	 */
	private void parseLessonFileSize(Lesson lesson){
		HttpEntity httpEntity = null;
		try {
			HttpGet httpGet = new HttpGet(lesson.getDownloadUrl());
			httpGet.setConfig(requestConfig);
			HttpResponse httpResponse = httpClient.execute(httpGet);
			String contentLength = httpResponse.getFirstHeader(Constant.content_length).getValue();
			long length = Long.parseLong(contentLength);
			httpEntity = httpResponse.getEntity();
			lesson.setFileSize(length);
		} catch (Exception e) {
			System.out.println("获取文件："+lesson.getTitle()+"长度失败,重新获取");
			parseLessonFileSize(lesson);
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
}
