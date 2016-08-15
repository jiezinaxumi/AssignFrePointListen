package com.listener;

/**
 * @author Boris
 * @description 
 * 2016��8��10��
 */
public interface FileListener {
	/**
	 * @Method: onWriteFileEnd 
	 * @Description: �����ļ�д�����
	 * @param fileName �ļ���
	 * @param path �ļ�·��
	 * @param startTime ��ʼʱ��
	 * @param endTime ����ʱ��
	 * void
	 */
	void onWriteFileEnd(String fileName, String path, String startTime, String endTime);
	/**
	 * @Method: onWriteTotalFileEnd 
	 * @Description: �����ļ�д�����
	 * void
	 */
	void onWriteTotalFileEnd();
}
