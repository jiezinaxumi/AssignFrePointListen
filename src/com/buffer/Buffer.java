package com.buffer;

/**
 * @author Boris
 *
 * 2016��7��28��
 */
abstract class Buffer {
	public static final int MAX_SIZE = 1024;
	
	/** 
	 * @Method: initBuffer 
	 * @Description: ��ʼ���̶����ֽ���
	 * void
	 */ 
	protected abstract void initBuffer();
	
	
	/** 
	 * @Method: getCheckCode 
	 * @Description: ʹ������ȡ��֤��
	 * @param buffer �����ֽ�����
	 * @param n ǰnλ�������
	 * @return ��֤��
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
