package com.listener;

/**
 * @author Boris
 *
 * 2016Äê8ÔÂ2ÈÕ
 */
public interface WorkstationListener {
	void onReveicedSTCP(byte[] stcp, String ip, int port);
}
