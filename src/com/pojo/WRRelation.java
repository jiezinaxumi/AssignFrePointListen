package com.pojo;

import com.client.Workstation;

/**
 * @author Boris
 * 
 * 工作站和接收机的对应关系
 *
 * 2016年8月9日
 */
public class WRRelation {
	private Workstation workstation;
	
	private String receiverIP;
	
	private int receiverPort;
	private int receiverStatus;
	
	public WRRelation(){};	
	
	/**
	 * @param workstation
	 * @param receiverIP
	 * @param receiverPort
	 */
	public WRRelation(Workstation workstation, String receiverIP,
			int receiverPort, int receiverStatus) {
		super();
		this.workstation = workstation;
		this.receiverIP = receiverIP;
		this.receiverPort = receiverPort;
		this.receiverStatus = receiverStatus;
	}
	public int getReceiverStatus() {
		return receiverStatus;
	}

	public void setReceiverStatus(int receiverStatus) {
		this.receiverStatus = receiverStatus;
	}

	public Workstation getWorkstation() {
		return workstation;
	}
	public void setWorkstation(Workstation workstation) {
		this.workstation = workstation;
	}
	public String getReceiverIP() {
		return receiverIP;
	}
	public void setReceiverIP(String receiverIP) {
		this.receiverIP = receiverIP;
	}
	public int getReceiverPort() {
		return receiverPort;
	}
	public void setReceiverPort(int receiverPort) {
		this.receiverPort = receiverPort;
	}
	
	
	

}
