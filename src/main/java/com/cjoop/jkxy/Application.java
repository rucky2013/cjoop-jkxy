package com.cjoop.jkxy;

import java.awt.EventQueue;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.cjoop.jkxy.view.LoginView;
import com.cjoop.jkxy.view.MainView;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 应用程序入口
 * @author chenjun
 *
 */
@Configuration
@ComponentScan
public class Application {

	/**
	 * 应用程序启动入口
	 * @param args
	 */
	public static void main(String[] args) {
		final ApplicationContext applicationContext = new AnnotationConfigApplicationContext(Application.class);
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoginView loginView = applicationContext.getBean(LoginView.class);
					loginView.visitVerify();
					loginView.setLocationRelativeTo(null);
					loginView.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		MainView mainView = applicationContext.getBean(MainView.class);
		mainView.init();
		System.out.println(applicationContext);
	}

	@Bean
	public HttpClient httpClient() {
		return HttpClients.createDefault();
	}
	
	/**
	 * json工具类
	 * @return
	 */
	@Bean
	public ObjectMapper objectMapper(){
		return new ObjectMapper();
	}
	
	/**
	 * 主配置文件初始化
	 * @return
	 * @throws Exception
	 */
	@Bean
	public PropertiesConfiguration mainConfig() throws Exception{
		String fileName = "application.properties";
		File appPropFile = new File(fileName);
		if(!appPropFile.exists()){
			appPropFile.createNewFile();
		}
		return new PropertiesConfiguration(fileName);
	}
	
	/**
	 * http request 配置信息
	 * @return
	 */
	@Bean
	public RequestConfig requestConfig(){
		Builder builder = RequestConfig.custom().setSocketTimeout(2000)
				.setConnectTimeout(2000);
		return builder.build();
	}
	
	/**
	 * 连接池管理
	 * @return
	 */
	@Bean
	public ExecutorService executorService(){
		return Executors.newFixedThreadPool(5);
	}
	
	/**
	 * 数据源配置
	 * @return
	 */
	@Bean
	public DataSource dataSource(){
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("org.h2.Driver");
		dataSource.setUrl("jdbc:h2:jkxy");
		dataSource.setUsername("jkxy");
		dataSource.setPassword("jkxy");
		return dataSource;
	}
	
	/**
	 * jdbc模板工具类
	 * @param dataSource
	 * @return
	 */
	@Bean
	public JdbcTemplate jdbcTemplate(DataSource dataSource){
		return new JdbcTemplate(dataSource);
	}
	
}
