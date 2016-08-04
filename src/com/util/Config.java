package com.util;


/**
 * @author Boris
 *
 * 2016年7月28日
 */
public class Config {
	public static final int USE_WAY = 0x00; //0独占  ；1 共享有控制权； 2共享没有控制权
	
	public static final int CONTROLLER_TCP_PORT = 5770;
	public static final int CONTROLLER_UDP_PORT = 5772;
	public static final int WORKSTATION_UDP_PORT = 5771;
	
	public static final String APPLAY_PWD = "abcdeabcdeabcdea"; //申请口令 16个数字，英文字符
	
	public static final int RETURN_TYPE = 0x80; //数据返回类型
	
	public static final int UNIT_NUMBER = 0x0000; //单位编号  2字节
}
