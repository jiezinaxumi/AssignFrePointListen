package com.listener;

/**
 * @author Boris
 *
 * 2016��8��2��
 */
public interface WorkstationListener {
	/**
	 * @Method: onRevivedData 
	 * @Description: �յ���Ƶ�󷵻ص�����
	 * @param stcp ����
	 * @param startPos ����������ʼλ��
	 * void
	 */
	void onRevivedData(byte[] stcp, int startPos);
}
