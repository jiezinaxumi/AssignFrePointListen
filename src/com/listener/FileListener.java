package com.listener;

/**
 * @author Boris
 * @description 
 * 2016��8��10��
 */
public interface FileListener {
	void onWriteFileEnd(String fileName, String path, String startTime, String endTime);
}
