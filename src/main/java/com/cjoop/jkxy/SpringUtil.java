package com.cjoop.jkxy;

import java.util.UUID;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring工具类
 * @author chenjun
 *
 */
@Component
public class SpringUtil implements ApplicationContextAware {
	private ApplicationContext applicationContext;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	public <T> T getBean(Class<T> requiredType){
		return applicationContext.getBean(requiredType);	
	}
	
	public String getUUID(){
		return UUID.randomUUID().toString();
	}
}
