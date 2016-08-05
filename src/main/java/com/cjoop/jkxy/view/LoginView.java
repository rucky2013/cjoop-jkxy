package com.cjoop.jkxy.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import com.cjoop.jkxy.Application;
import com.cjoop.jkxy.Constant;
import com.cjoop.jkxy.componet.JImage;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 登陆页面
 * @author chenjun
 *
 */
@Component
public class LoginView extends JFrame {
	private static final long serialVersionUID = -4647884037620619492L;
	private JPanel contentPane;
	private JTextField txtAccount;
	private JPasswordField txtPassword;
	private JTextField txtVerify;
	private JImage verifyImage;
	private JLabel lblVerify;
	private JLabel lblMsg;
	@Autowired
	ObjectMapper objectMapper;
	@Autowired
	private HttpClient httpClient;
	@Autowired
	private MainView mainView;
	@Autowired
	private RequestConfig requestConfig;
	
	public HttpClient getHttpClient() {
		return httpClient;
	}

	/**
	 * ajax校验验证码正确性
	 * @return true正确，false不正确
	 */
	public boolean ajaxVerify(){
		HttpEntity httpEntity = null;
		try {
			HttpPost httpPost = new HttpPost(Constant.check_Verify_Url);
			httpPost.setHeader("Origin",Constant.passport_url);
			httpPost.setHeader(Constant.x_requested_with,Constant.xmlHttpRequest);
			httpPost.setHeader("Referer",Constant.login_page_url);
			httpPost.setHeader("Accept",Constant.accept_json);
			List<NameValuePair> formParams = new ArrayList<NameValuePair>();
			formParams.add(new BasicNameValuePair("verify", txtVerify.getText()));
			httpPost.setEntity(new UrlEncodedFormEntity(formParams));
			HttpResponse httpResponse = getHttpClient().execute(httpPost);
			httpEntity = httpResponse.getEntity();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				String result = EntityUtils.toString(httpEntity);
				Map<?,?> map = objectMapper.readValue(result, Map.class);
				int status = Integer.parseInt(map.get("status").toString());
				if(status==1){
					lblMsg.setText("");
					return true;
				}else{
					lblMsg.setText("验证码输入错误");
				}	
			}
		} catch (Exception e) {
			lblMsg.setText("服务器验证异常,尝试重新登陆.");
		}finally {
			if(httpEntity!=null){
				try {
					EntityUtils.consume(httpEntity);
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}
			}
		}
		return false;
	}
	
	/**
	 * ajax登陆极客学院
	 */
	public void ajaxLogin(){
		HttpEntity httpEntity = null;
		try {
			HttpPost httpPost = new HttpPost(Constant.login_submit_url);
			httpPost.setConfig(requestConfig);
			httpPost.setHeader(Constant.host,Constant.passport_host);
			httpPost.setHeader("Origin",Constant.passport_host);
			httpPost.setHeader(Constant.x_requested_with,Constant.xmlHttpRequest);
			httpPost.setHeader("Referer",Constant.login_page_url);
			httpPost.setHeader("Accept",Constant.accept_json);
			httpPost.setHeader("Content-Type",Constant.content_type_form_utf8);
			String password = new String(txtPassword.getPassword());
			List<NameValuePair> formParams = new ArrayList<NameValuePair>();
			formParams.add(new BasicNameValuePair("expire", "7"));
			formParams.add(new BasicNameValuePair("uname", txtAccount.getText()));
			formParams.add(new BasicNameValuePair("password",password));
			formParams.add(new BasicNameValuePair("verify", txtVerify.getText()));
			httpPost.setEntity(new UrlEncodedFormEntity(formParams));
			HttpResponse httpResponse = getHttpClient().execute(httpPost);
			httpEntity = httpResponse.getEntity();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				String result = EntityUtils.toString(httpEntity);
				ObjectMapper objectMapper = new ObjectMapper();
				Map<?,?> map = objectMapper.readValue(result, Map.class);
				int status = Integer.parseInt(map.get("status")+"");
				if(status!=1){//输入错误
					visitVerify();
					lblMsg.setText(map.get("msg")+"");
				}else{
					if(!isVIP()){
						lblMsg.setText("登陆的账号不是vip，请使用vip账号进行登陆.");
						logout();
						visitVerify();
					}else{//登陆成功，跳转到主界面
						this.setVisible(false);
						mainView.setLogin(true);
						for (CourseView courseView : mainView.getCourseViewCache().values()) {
							courseView.startCheckCourse();
						}
						mainView.setLocationRelativeTo(null);
						mainView.setVisible(true);
					}
				}
				
			}
		} catch (Exception e) {
			System.out.println("登陆失败");
			visitVerify();
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
	 * 登出系统
	 */
	public void logout(){
		HttpEntity httpEntity = null;
		try {
			HttpGet httpGet = new HttpGet(Constant.logout_page_url);
			httpGet.setConfig(requestConfig);
			httpGet.setHeader("Host", Constant.passport_host);
			httpGet.setHeader("Referer", Constant.jikexueyuan_url);
			httpGet.setHeader("User-Agent",Constant.user_agent_chrome);
			HttpResponse httpResponse = httpClient.execute(httpGet);
			httpEntity = httpResponse.getEntity();
		} catch (Exception e) {
			System.out.println("登陆系统失败");
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
	 * 判断是否是VIP
	 * @return
	 */
	public boolean isVIP(){
		HttpEntity httpEntity = null;
		try {
			HttpGet httpGet = new HttpGet(Constant.member_page_url);
			httpGet.setConfig(requestConfig);
			httpGet.setHeader("Host", Constant.jikexueyuan_host);
			httpGet.setHeader("User-Agent",Constant.user_agent_chrome);
			HttpResponse httpResponse = httpClient.execute(httpGet);
			httpEntity = httpResponse.getEntity();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if(statusCode == HttpStatus.SC_OK){
				String html = EntityUtils.toString(httpEntity,Constant.UTF_8);
				Document doc = Jsoup.parse(html);
				String vipInfo = doc.select("p.account-top-vipinfo > span > a > b").first().text();
				return !"会员已过期".equals(vipInfo.trim());
			}
		} catch (Exception e) {
			lblMsg.setText("检验会员VIP失败,正在尝试重新检验");
			return isVIP();
		}finally {
			if(httpEntity!=null){
				try {
					EntityUtils.consume(httpEntity);
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}
			}
		}
		return false;
	}
	
	/**
	 * 获取验证码
	 */
	public void visitVerify() {
		HttpEntity httpEntity = null;
		try {
			if(verifyImage!=null){
				contentPane.remove(verifyImage);
				contentPane.updateUI();
			}
			HttpGet httpGet = new HttpGet(Constant.verify_url + "?"
					+ System.currentTimeMillis());
			httpGet.setHeader("Accept", "image/webp,*/*;q=0.8");
			httpGet.setHeader("Referer", Constant.login_page_url);
			HttpResponse httpResponse = getHttpClient().execute(httpGet);
			httpEntity = httpResponse.getEntity();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				IOUtils.copy(httpEntity.getContent(), output);
				verifyImage = new JImage(output.toByteArray());
				verifyImage.setBounds(218, 65, 100, 40);
				verifyImage.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				verifyImage.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						visitVerify();
					}
				});
				contentPane.add(verifyImage);
				txtVerify.setText("");
			}
		} catch (Exception e) {
			System.out.println("加载验证码失败!");
			visitVerify();
		} finally {
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
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ApplicationContext applicationContext = new AnnotationConfigApplicationContext(Application.class);
					LoginView loginView = applicationContext.getBean(LoginView.class);
					loginView.setVisible(true);
					System.out.println(applicationContext);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Create the frame.
	 */
	public LoginView() {
		setTitle("登陆界面");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 346, 196);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblAccount = new JLabel("用户名：");
		lblAccount.setFont(Constant.font_song_14);
		lblAccount.setBounds(22, 12, 67, 15);
		contentPane.add(lblAccount);

		txtAccount = new JTextField();
		txtAccount.setBounds(88, 10, 120, 21);
		contentPane.add(txtAccount);
		txtAccount.setColumns(10);

		JLabel lblPassword = new JLabel("密　码：");
		lblPassword.setFont(Constant.font_song_14);
		lblPassword.setBounds(22, 47, 67, 15);
		
		contentPane.add(lblPassword);
		txtPassword = new JPasswordField();
		txtPassword.setBounds(88, 44, 120, 21);
		contentPane.add(txtPassword);

		txtVerify = new JTextField();
		txtVerify.setBounds(88, 78, 120, 21);
		contentPane.add(txtVerify);
		txtVerify.setColumns(10);
		
		final JButton btnLogin = new JButton("登　陆");
		btnLogin.setFont(Constant.font_song_14);
		btnLogin.setBounds(115, 109, 93, 23);
		contentPane.add(btnLogin);

		lblVerify = new JLabel("验证码：");
		lblVerify.setFont(Constant.font_song_14);
		lblVerify.setBounds(22, 81, 67, 15);
		contentPane.add(lblVerify);
		
		lblMsg = new JLabel("");
		lblMsg.setForeground(Color.RED);
		lblMsg.setFont(Constant.font_song_14);
		lblMsg.setBounds(10, 143, 320, 15);
		contentPane.add(lblMsg);
		txtAccount.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					if(checkAccount()){
						txtPassword.requestFocus();
					}
				}
			}
		});
		txtPassword.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					if(checkPassword()){
						txtVerify.requestFocus();
					}
				}
			}
		});
		txtVerify.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					login();
				}
			}
		});
		txtVerify.addFocusListener(new FocusAdapter(){
			public void focusLost(FocusEvent e) {
				if(StringUtils.isNotBlank(txtVerify.getText())){
					ajaxVerify();
				}
			}
		});
		btnLogin.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					login();
				}
			}
		});
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				login();
			}
		});
	}
	
	/**
	 * 检查账号是否不为空
	 * @return true or false
	 */
	public boolean checkAccount(){
		if(StringUtils.isBlank(txtAccount.getText())){
			lblMsg.setText("账号不能为空");
			txtAccount.requestFocus();
			return false;
		}
		return true;
	}
	
	/**
	 * 检查密码是否不为空
	 * @return
	 */
	public boolean checkPassword(){
		if(txtPassword.getPassword().length==0){
			lblMsg.setText("密码不能为空");
			txtPassword.requestFocus();
			return false;
		}
		return true;
	}
	
	/**
	 * 检查验证码是否不为空
	 * @return
	 */
	public boolean checkVerify(){
		if(StringUtils.isBlank(txtVerify.getText())){
			lblMsg.setText("验证码不能为空");
			txtVerify.requestFocus();
			return false;
		}
		return true;
	}
	
	/**
	 * 登陆极客学院
	 */
	public void login(){
		if(!checkAccount() || !checkPassword() || !checkVerify()){
			return;
		}
		if(ajaxVerify()){
			ajaxLogin();
		};
	}
	
}
