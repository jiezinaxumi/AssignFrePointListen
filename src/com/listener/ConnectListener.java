package com.listener;

import com.client.Workstation;

/**
 * @author Boris
 * @description 
 * 2016��8��9��
 */

public interface ConnectListener {
	void onConnectEnd(Workstation workstation,String receiverIp, int receiverPort);
}
