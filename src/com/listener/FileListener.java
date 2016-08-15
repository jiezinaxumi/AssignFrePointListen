package com.listener;

/**
 * @author Boris
 * @description 
 * 2016年8月10日
 */
public interface FileListener {
	/**
	 * @Method: onWriteFileEnd 
	 * @Description: 单个文件写入结束
	 * @param fileName 文件名
	 * @param path 文件路径
	 * @param startTime 开始时间
	 * @param endTime 结束时间
	 * void
	 */
	void onWriteFileEnd(String fileName, String path, String startTime, String endTime);
	/**
	 * @Method: onWriteTotalFileEnd 
	 * @Description: 整个文件写入结束
	 * void
	 */
	void onWriteTotalFileEnd();
}
