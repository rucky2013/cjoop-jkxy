package com.cjoop.jkxy.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cjoop.jkxy.Constant;
import com.cjoop.jkxy.dao.LessonDao;
import com.cjoop.jkxy.domain.Lesson;

/**
* 下载单个课时的子线程
*
*/
@Component
@Scope("prototype")
public  class DownloadLessonThread extends Thread{
	private Lesson lesson;
	private Lesson oldLesson;
	private boolean isFinish = false;
	protected int failCount = 0;
	String title ;
	DownloadCourseThread downloadCourseThread;
	@Autowired
	private HttpClient httpClient;
	@Autowired
	private RequestConfig requestConfig;
	@Autowired
	PropertiesConfiguration mainConfig;
	@Autowired
	private LessonDao lessonDao;
	
	public DownloadLessonThread() {
	}
	
	@Override
	public void run() {
		setName("lesson_" + lesson.getTitle());
		while(!isFinish){
			downloadLessonFile();
			try {
				if(!isFinish){
					Thread.sleep(1000);
				}else{
					saveOrUpdateLesson();
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		System.out.println(title+"下载完成√");
		downloadCourseThread.updateLessonThreadCount();
	}
	
	
	public void saveOrUpdateLesson(){
		if(oldLesson==null){
			lessonDao.saveLesson(lesson);
		}else{
			lesson.setId(oldLesson.getId());
			BeanUtils.copyProperties(lesson, oldLesson);
			lessonDao.updateLesson(oldLesson);
		}
	}
	
	/**
	 * 下载指定路径的文件信息
	 * @param url 文件地址
	 * @param file 保存文件的路径
	 * @throws Exception
	 */
	private void downloadFile(String url,File file) throws Exception{
		HttpEntity httpEntity = null;
		try {
			HttpGet httpGet = new HttpGet(url);
			httpGet.setConfig(requestConfig);
			HttpResponse httpResponse = httpClient.execute(httpGet);
			httpEntity = httpResponse.getEntity();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				file.getParentFile().mkdirs();
				try(FileOutputStream fos = new FileOutputStream(file)){
					IOUtils.copyLarge(httpEntity.getContent(), fos);
				}
			}
		} catch (Exception e) {
			System.out.println("下载文件失败," + e.getMessage());
			downloadFile(url, file);
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
	 * 下载指定路径的文件信息
	 * @param url 文件地址
	 * @param offset 保存的偏移量开始位置
	 * @param len 下载的文件长度
	 * @param file 保存文件的路径
	 * @throws Exception
	 */
	protected void downloadFile(String url, long offset, long len, File file) throws Exception{
		HttpEntity httpEntity = null;
		try {
			HttpGet httpGet = new HttpGet(url);
			httpGet.setConfig(requestConfig);
			httpGet.setHeader("User-Agent",Constant.user_agent_chrome);
			httpGet.addHeader("Range", "bytes=" + offset + "-" + len);
			HttpResponse httpResponse = httpClient.execute(httpGet);
			httpEntity = httpResponse.getEntity();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_PARTIAL_CONTENT) {
				byte[] bytes = EntityUtils.toByteArray(httpEntity);
				try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
					raf.seek(offset);
					raf.write(bytes);
				}
			}
		} catch (Exception e) {
			System.out.println("下载文件失败,"+e.getMessage());
			downloadFile(url, offset, len, file);
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
	 * 下载单个课时文件
	 * @param dest 保存的文件
	 * @param lesson 课时信息
	 */
	public void downloadLessonFile(){
		String downloadUrl = lesson.getDownloadUrl();
		try {
			String saveDir = mainConfig.getProperty("saveDir")+"";
			File dest = new File(saveDir,lesson.getFileName());
			if(dest.exists()){
				long contentLength = lesson.getFileSize();
				long offset = 0;
				long len = contentLength-1;
				long fileLength = dest.length();
				if(contentLength==fileLength){
					isFinish = true;
				}else if(fileLength<contentLength){
					System.out.println(title + ":继续之前的文件进行下载");
					offset = fileLength;
					downloadFile(downloadUrl,offset,len,dest);
					isFinish = true;
				}else{
					System.out.println(title + ":链接已经失效");
				}
			}else{
				System.out.println(title + "开始下载...");
				downloadFile(downloadUrl,dest);
				isFinish = true;
			}
		} catch (Exception e) {
			failCount++;
			System.out.println("课时文件:(" + title+")下载失败,2秒后重新下载."+e.getMessage());
		}
	}
	
	public void setLesson(Lesson lesson) {
		this.lesson = lesson;
		title=(lesson.getNumber() + 1) + "." + lesson.getTitle() + ".mp4";
		oldLesson = lessonDao.findLessonByFileName(lesson.getFileName());
	}

	public void setDownloadCourseThread(DownloadCourseThread downloadCourseThread) {
		this.downloadCourseThread = downloadCourseThread;
	}
}

