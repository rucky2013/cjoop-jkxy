package com.cjoop.jkxy;

import java.awt.Font;

/**
 * 整个程序用到的常量信息
 * @author chenjun
 *
 */
public interface Constant {
	/**
	 * 12号宋体
	 */
	Font font_song_12 = new Font("宋体", Font.PLAIN, 12);
	/**
	 * 14号宋体
	 */
	Font font_song_14 = new Font("宋体", Font.PLAIN, 14);
	///////////////////////////////http请求头常量信息////////////////////////////////
	String UTF_8 = "UTF-8";
	String content_length = "Content-Length";
	String accept_json = "application/json, text/javascript, */*; q=0.01";
	String x_requested_with = "X-Requested-With";
	String xmlHttpRequest = "XMLHttpRequest";
	String host = "Host";
	String user_agent_chrome = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36";
	String content_type_form_utf8 = "application/x-www-form-urlencoded; charset=UTF-8"; 
	///////////////////////////////极客学院的访问地址/////////////////////////////////
	
	String passport_url = "http://passport.jikexueyuan.com";
	String jikexueyuan_url = "http://www.jikexueyuan.com";
	String passport_host = "passport.jikexueyuan.com";
	String jikexueyuan_host = "www.jikexueyuan.com";
	String cv4_host = "cv4.jikexueyuan.com";
	/**
	 * //获取验证码图片地址
	 */
	String verify_url = "http://passport.jikexueyuan.com/sso/verify";
	/**
	 * 登陆页面地址
	 */
	String login_page_url = "http://passport.jikexueyuan.com/sso/login";
	/**
	 * 会员页面地址
	 */
	String member_page_url = "http://www.jikexueyuan.com/member/";
	/**
	 * 登出页面地址
	 */
	String logout_page_url = "http://passport.jikexueyuan.com/submit/logout";
	/**
	 * 职业课程页面地址
	 */
	String zhiye_page_url = "http://ke.jikexueyuan.com/zhiye/";
	/**
	 * 登陆提交地址
	 */
	String login_submit_url = "http://passport.jikexueyuan.com/submit/login?is_ajax=1";
	/**
	 * 校验验证码是否正确地址
	 */
	String check_Verify_Url = "http://passport.jikexueyuan.com/check/verify?is_ajax=1";
	
}
