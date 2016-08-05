package com.cjoop.jkxy.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.MatteBorder;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cjoop.jkxy.Constant;
import com.cjoop.jkxy.SpringUtil;
import com.cjoop.jkxy.domain.Course;
import com.cjoop.jkxy.service.CheckCourseThread;
import com.cjoop.jkxy.service.DownloadCourseThread;

/**
 * 课程详细展示页面
 * @author chenjun
 *
 */
@Component
@Scope("prototype")
public class CourseView extends JPanel {

	private static final long serialVersionUID = 5002588583906267551L;
	/**
	 * 浅灰色
	 */
	Color light_gray  = new Color(Integer.parseInt("f5f5f5", 16));
	/**
	 * 深绿
	 */
	Color dark_green  = new Color(Integer.parseInt("228B22", 16));
	
	@Autowired
	private MainView mainView;
	@Autowired
	private PropertiesConfiguration mainConfig;
	/**
	 * 消息提示
	 */
	private JLabel lblDownloadTip;
	@Autowired
	private SpringUtil springUtil;
	/**
	 * 总的课程课时线程计数
	 */
	private int totalLessonThreadCount;
	/**
	 * 线程池
	 */
	@Autowired 
	private ExecutorService executorService;
	/**
	 * 检查课程线程的集合
	 */
	private List<CheckCourseThread> checkCourseThreadCache = new ArrayList<CheckCourseThread>();
	/**
	 * 复选框数据集合
	 */
	private Map<Course,JCheckBox> checkBoxCache = new HashMap<Course,JCheckBox>();
	/**
	 * 课程信息
	 */
	private Course course;
	/**
	 * 计数器
	 */
	private int checkThreadCount;
	private JPanel bodyPanel = new JPanel(); 
	JButton btnDownload = new JButton("下载当前课程");
	
	public void startCheckCourse(){
		checkThreadCount = checkCourseThreadCache.size();
		for (CheckCourseThread checkCourseThread : checkCourseThreadCache) {
			executorService.execute(checkCourseThread);
		}
	}
	
	public synchronized void updateCheckThreadCount(){
		this.checkThreadCount--;
		if(checkThreadCount<=0){
			btnDownload.setEnabled(true);
			lblDownloadTip.setText("课程信息校对完成,可以开始下载");
			lblDownloadTip.setForeground(dark_green);
		}
	}
	
	/**
	 * 获取保存根路径
	 * @return
	 */
	public File getSaveDir(){
		String filePath = mainView.getTextFilePath().getText();
		if("".equals(filePath)){
			return null;
		}
		File saveDir = new File(filePath);
		return saveDir;
	}
	
	public void setCourse(Course course) {
		this.course = course;
		buildBodyPanel();
		add(bodyPanel);
	}

	public CourseView() {
		setLayout(null);
		
		btnDownload.setFont(Constant.font_song_12);
		btnDownload.setBounds(10, 10, 120, 23);
		btnDownload.addActionListener(new DownloadHandler());
		btnDownload.setEnabled(false);
		add(btnDownload);
		
		lblDownloadTip = new JLabel();
		lblDownloadTip.setText("正在校验课程信息...");
		lblDownloadTip.setForeground(Color.RED);
		lblDownloadTip.setBounds(340, 10, 600, 23);
		lblDownloadTip.setFont(Constant.font_song_12);
		add(lblDownloadTip);
		
	}

	/**
	 * 构建主体内容
	 * @return
	 */
	public JPanel buildBodyPanel(){
		final List<Course> thirdCourseList = course.getChildCourse();
		int rows = (thirdCourseList.size() + 3 - 1) / 3;
		int len = rows * 3;
		bodyPanel.setBounds(10, 38, 950, 38*rows);
		bodyPanel.setBorder(new MatteBorder(1, 1, 1, 0, Color.LIGHT_GRAY));
		bodyPanel.setLayout(new GridLayout(rows, 6, 0, 0));
		Color color = Color.WHITE;
		for (int j = 0; j < len; j++) {
			JPanel panel = new JPanel();
			panel.setLayout(null);
			if(j%3==0){
				color = (color == light_gray)?Color.WHITE:light_gray;
			}
			if(j<thirdCourseList.size()){
				Course thirdCourse = thirdCourseList.get(j);
				JLabel lblTitle = new JLabel(thirdCourse.getTitle());
				lblTitle.setFont(new Font("宋体", Font.PLAIN, 12));
				lblTitle.setBounds(10, 12, 295, 15);
				JCheckBox checkBox = new JCheckBox("");
				checkBox.setEnabled(false);
				checkBox.setBounds(290, 8, 20, 20);
				checkBox.setBackground(color);
				
				CheckCourseThread checkCourseThread = springUtil.getBean(CheckCourseThread.class);
				checkCourseThread.setCheckBox(checkBox);
				checkCourseThread.setCourse(thirdCourse);
				checkCourseThread.setCourseView(this);
				checkCourseThreadCache.add(checkCourseThread);
				
				panel.add(lblTitle);
				panel.add(checkBox);
				checkBoxCache.put(thirdCourse, checkBox);
			}
			panel.setBackground(color);
			panel.setBorder(new MatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
			panel.setMinimumSize(new Dimension(120, 50));
			bodyPanel.add(panel);
		}
		return bodyPanel;
	}
	
	/**
	 * 下载课程事件处理
	 * @author chenjun
	 *
	 */
	protected class DownloadHandler implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			File saveFileDir = getSaveDir();
			if(saveFileDir==null){
				JOptionPane.showMessageDialog(null, "请选择视频保存的路径","错误提示",JOptionPane.ERROR_MESSAGE);
			}else{
				mainConfig.setProperty("saveDir", saveFileDir.getAbsolutePath());
				try {
					mainConfig.save();
					totalLessonThreadCount = 0;
					lblDownloadTip.setText("正在下载当前课程视频,耐心等待...");
					lblDownloadTip.setForeground(Color.red);
					btnDownload.setEnabled(false);
					System.out.println("------------所有课程开始下载-----------");
					boolean hasDownloadCourseThread = false;
					for (Course item : course.getChildCourse()) {
						JCheckBox checkBox = checkBoxCache.get(item);
						if(!checkBox.isSelected()){
							hasDownloadCourseThread = true;
							DownloadCourseThread downloadCourseThread = springUtil.getBean(DownloadCourseThread.class);
							downloadCourseThread.setCourse(item);
							downloadCourseThread.setCourseView(CourseView.this);
							downloadCourseThread.start();
						}
					}
					if(!hasDownloadCourseThread){
						subLessonThreadCount();
					}
				} catch (Exception ce) {
					ce.printStackTrace();
				}
			}
		}
	}
	
	public synchronized void addLessonThreadCount(int count){
		this.totalLessonThreadCount+=count;
	}
	
	public synchronized void subLessonThreadCount(){
		this.totalLessonThreadCount--;
		if(totalLessonThreadCount<=0){
			lblDownloadTip.setText("当前课程视频下载完成√");
			System.out.println("------------所有课程下载完毕-----------");
			lblDownloadTip.setForeground(dark_green);
			btnDownload.setEnabled(true);
		}
	}
	
	/**
	 * 更新复选框的状态为true
	 * @param course
	 */
	public void updateChecked(Course course){
		JCheckBox checkBox = checkBoxCache.get(course);
		checkBox.setSelected(true);
	}
	

}
