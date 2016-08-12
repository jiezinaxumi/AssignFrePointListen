package com.listener;

import com.client.Workstation;

/**
 * @author Boris
 * @description 
 * 2016Äê8ÔÂ9ÈÕ
 */

public interface ConnectListener {
	void onConnectEnd(Workstation workstation,String receiverIp, int receiverPort);
}
