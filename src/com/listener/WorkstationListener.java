package com.listener;

/**
 * @author Boris
 *
 * 2016��8��2��
 */
public interface WorkstationListener {
	void onReveicedSTCP(byte[] stcp, String ip, int port);
}
