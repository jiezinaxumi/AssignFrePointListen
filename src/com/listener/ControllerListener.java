package com.listener;

/**
 * @author Boris
 *
 * 2016��7��28��
 */
public interface ControllerListener {
	void onNeedCreatedWorkstation(int workstationPort);
	void onReceivedReportMsg(byte[] reportBuffer);
}
