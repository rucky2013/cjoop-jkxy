package com.cjoop.jkxy.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
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
import org.springframework.stereotype.Component;

import com.cjoop.jkxy.Constant;
import com.cjoop.jkxy.SpringUtil;
import com.cjoop.jkxy.dao.CourseDao;
import com.cjoop.jkxy.domain.Course;
import com.cjoop.jkxy.service.InitCourseInfoThread;

/**
 * 主界面展示
 * 
 * @author chenjun
 *
 */
@Component
public class MainView extends JFrame {
	private static final long serialVersionUID = -7760917512462953328L;
	private JPanel contentPane;
	JComboBox<Course> cboxZhiye;
	private JComboBox<Course> cboxCourseLevel;
	private JTabbedPane tabbedPane;
	private JTextField textFilePath;
	private JButton btnSelFile;
	@Autowired
	private HttpClient httpClient;
	@Autowired
	private SpringUtil springUtil;
	@Autowired
	private PropertiesConfiguration mainConfig;
	@Autowired
	private RequestConfig requestConfig;
	private Map<String, CourseView> courseViewCache = new HashMap<String, CourseView>();
	@Autowired
	private CourseDao courseDao;
	/**
	 * 是否登录
	 */
	private boolean isLogin = false;

	public MainView() {
	}

	public void init() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(1000, 500));
		setTitle("极客学院");
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);

		contentPane.add(panel, BorderLayout.NORTH);

		JLabel lblCkk = new JLabel("职业课程库：");
		lblCkk.setFont(Constant.font_song_12);
		panel.add(lblCkk);

		cboxZhiye = new JComboBox<Course>();
		cboxZhiye.setFont(Constant.font_song_12);
		cboxZhiye.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				Course course = (Course) e.getItem();
				if (e.getStateChange() == ItemEvent.SELECTED) {
					cboxCourseLevel.removeAllItems();
					for (Course item : course.getChildCourse()) {
						cboxCourseLevel.addItem(item);
					}
				}
			}
		});
		panel.add(cboxZhiye);

		JLabel lblCkjb = new JLabel("课程级别：");
		lblCkjb.setFont(Constant.font_song_12);
		panel.add(lblCkjb);

		cboxCourseLevel = new JComboBox<Course>();
		cboxCourseLevel.setFont(Constant.font_song_12);
		cboxCourseLevel.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				Course course = (Course) e.getItem();
				if (e.getStateChange() == ItemEvent.SELECTED) {
					showSecondCourseView(course.getChildCourse());
				}
			}

		});
		panel.add(cboxCourseLevel);

		JLabel lblSavePath = new JLabel("保存路径：");
		lblSavePath.setFont(Constant.font_song_12);
		panel.add(lblSavePath);

		textFilePath = new JTextField();
		textFilePath.setFont(Constant.font_song_12);
		textFilePath.setText(mainConfig.getString("saveDir"));
		panel.add(textFilePath);
		textFilePath.setColumns(40);

		btnSelFile = new JButton("选择");
		btnSelFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc = new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				jfc.showDialog(new JLabel(), "确定");
				File file = jfc.getSelectedFile();
				if (file.isDirectory()) {
					textFilePath.setText(file.getAbsolutePath());
					mainConfig.setProperty("saveDir", file.getAbsolutePath());
					try {
						mainConfig.save();
					} catch (ConfigurationException ce) {
						System.out.println("保存配置文件属性saveDir失败");
					}
				}
			}
		});
		btnSelFile.setFont(Constant.font_song_12);
		panel.add(btnSelFile);
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setFont(Constant.font_song_12);
		contentPane.add(tabbedPane, BorderLayout.CENTER);

		List<Course> list = initZhiyeCourse();
		for (Course course : list) {
			InitCourseInfoThread courseInfoThread = springUtil.getBean(InitCourseInfoThread.class);
			courseInfoThread.setParentCourse(course);
			courseInfoThread.start();
		}
	}

	/**
	 * 展示二级课程内容
	 * 
	 * @param childCourse
	 */
	public void showSecondCourseView(List<Course> list) {
		tabbedPane.removeAll();
		Course zhiye = (Course) (getCboxZhiye().getSelectedItem());
		Course courseLevel = (Course) getCboxCourseLevel().getSelectedItem();
		for (int i = 0; i < list.size(); i++) {
			Course course = list.get(i);
			String key = zhiye.getTitle() + "_" + courseLevel.getTitle() + "_" + (i + 1) + "." + course.getTitle();
			CourseView courseView = courseViewCache.get(key);
			if (courseView == null) {
				courseView = springUtil.getBean(CourseView.class);
				courseView.setCourse(course);
				courseViewCache.put(key, courseView);
				if(isLogin){
					courseView.startCheckCourse();
				}
			}
			tabbedPane.addTab((i + 1) + "." + course.getTitle(), courseView);
		}
	}

	/**
	 * 初始化职业课程列表
	 */
	public List<Course> initZhiyeCourse() {
		HttpEntity httpEntity = null;
		List<Course> list = new ArrayList<Course>();
		try {
			HttpGet httpGet = new HttpGet(Constant.zhiye_page_url);
			httpGet.setConfig(requestConfig);
			HttpResponse httpResponse = httpClient.execute(httpGet);
			httpEntity = httpResponse.getEntity();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				String html = EntityUtils.toString(httpEntity, Constant.UTF_8);
				Document doc = Jsoup.parse(html);
				Elements overview_text_list = doc.select(".layout-inner > .overview-text");
				for (int i = 0; i < overview_text_list.size(); i++) {
					Element overview_text = overview_text_list.get(i);
					Element a = overview_text.select("a").first();
					Course course = new Course(a.text(), a.attr("href"));
					course.setSaveDir(course.getTitle());
					course.setPid("0");
					course.setNumber(i);
					cboxZhiye.addItem(course);
					saveCourseInfo(course);
					list.add(course);
				}
			}
			return list;
		} catch (Exception e) {
			System.out.println("构建职业课程库列表失败.");
			return initZhiyeCourse();
		} finally {
			if (httpEntity != null) {
				try {
					EntityUtils.consume(httpEntity);
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}
			}
		}
	}

	private void saveCourseInfo(Course course) {
		Course oldCourse = courseDao.findCourseBySaveDir(course.getSaveDir());
		if (oldCourse == null) {
			course.setId(springUtil.getUUID());
			courseDao.saveCourse(course);
		} else {
			course.setId(oldCourse.getId());
		}
	}

	public JTextField getTextFilePath() {
		return textFilePath;
	}

	public JComboBox<Course> getCboxZhiye() {
		return cboxZhiye;
	}

	public JComboBox<Course> getCboxCourseLevel() {
		return cboxCourseLevel;
	}

	public Map<String, CourseView> getCourseViewCache() {
		return courseViewCache;
	}

	public boolean isLogin() {
		return isLogin;
	}

	public void setLogin(boolean isLogin) {
		this.isLogin = isLogin;
	}

}
