package com.listener;

/**
 * @author Boris
 *
 * 2016年8月2日
 */
public interface WorkstationListener {
	/**
	 * @Method: onRevivedData 
	 * @Description: 收到调频后返回的数据
	 * @param stcp 报文
	 * @param startPos 数据区的起始位置
	 * void
	 */
	void onRevivedData(byte[] stcp, int startPos);
}
