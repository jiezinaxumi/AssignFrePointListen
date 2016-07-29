package com.buffer;

/**
 * @author Boris
 *
 * 2016年7月28日
 */
abstract class Buffer {
	public static final int MAX_SIZE = 1024;
	
	/** 
	 * @Method: initBuffer 
	 * @Description: 初始化固定的字节流
	 * void
	 */ 
	protected abstract void initBuffer();
	
	
	/** 
	 * @Method: getCheckCode 
	 * @Description: 使用异或获取验证码
	 * @param buffer 异或的字节数组
	 * @param n 前n位进行异或
	 * @return 验证码
	 * byte
	 */ 
	public byte getCheckCode(byte []buffer, int n){
		byte checkCode = buffer[0];
		
		for (int i = 1; i < n; i++) {
			checkCode ^= buffer[i];
		}
		
		return checkCode;
	}
	
}
