package com.listener;

/**
 * @author Boris
 *
 * 2016Äê7ÔÂ28ÈÕ
 */
public interface ControllerListener {
	void onNeedCreatedWorkstation(int workstationPort);
	void onReceivedReportMsg(byte[] reportBuffer);
}
