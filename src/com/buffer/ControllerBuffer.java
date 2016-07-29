package com.buffer;

import com.util.Config;
import com.util.Tools;

/**
 * @author Boris
 *
 * 2016年7月28日
 */
public class ControllerBuffer extends Buffer {
	private byte[] makeSureBuffer  = new byte[10]; //确认报
	private byte[] distributionBuffer  = new byte[34]; //分配报
	
	public byte[] getMakeSureBuffer() {
		return makeSureBuffer;
	}
	
	public byte[] getDistributionBuffer() {
		return distributionBuffer;
	}

	public ControllerBuffer() {
		// TODO Auto-generated constructor stub
		initBuffer();
	}
	
	@Override
	public void initBuffer() {
		// TODO Auto-generated method stub
		//初始化确认报
		makeSureBuffer[0] = 0x02;
		makeSureBuffer[1] = 0x0A;
		makeSureBuffer[2] = 0x00;
		makeSureBuffer[3] = getCheckCode(makeSureBuffer, 3);
		makeSureBuffer[9] = 0x00;
		
		//初始化分配报
		distributionBuffer[0] = 0x03;
		distributionBuffer[1] = 0x22;
		distributionBuffer[2] = 0x00;
		distributionBuffer[3] = getCheckCode(distributionBuffer, 3);
		distributionBuffer[9] = 0x03;
		distributionBuffer[10] = 0x06;
		distributionBuffer[11] = 0x00;
		distributionBuffer[12] = Config.USE_WAY;
		distributionBuffer[13] = 0x07;
		distributionBuffer[14] = 0x00;
		distributionBuffer[30] = 0x08;
		distributionBuffer[31] = 0x00;
		
		//分配报中工作站IP
		byte[] workstationIp = Config.WORKSTATION_IP.getBytes();
		System.arraycopy(workstationIp, 0, distributionBuffer, 15, workstationIp.length);
		
		//分配报中工作站的STCP端口号
		distributionBuffer[32] = (byte) (Config.WORKSTATION_UDP_PORT & 0x00FF);
		distributionBuffer[33] = (byte) (Config.WORKSTATION_UDP_PORT >> 8 & 0xFF);			
	}
	
	/** 
	 * @Method: initMakeSureBufferByRegisterBuffer 
	 * @Description: 用登记报初始化确认报
	 * @param registerBuffer 登记报
	 * void
	 */ 
	public void initMakeSureBufferByRegisterBuffer(byte[] registerBuffer){
		makeSureBuffer[4] = registerBuffer[4];
		makeSureBuffer[5] = registerBuffer[5];
		makeSureBuffer[6] = registerBuffer[6];
		makeSureBuffer[7] = registerBuffer[7];
		makeSureBuffer[8] = registerBuffer[8];
	}
	
	/** 
	 * @Method: initDistributionBufferByApplyBuffer 
	 * @Description: 参考申请报初始化分配报
	 * @param applyBuffer 申请报
	 * void
	 */ 
	public void initDistributionBufferByApplyBuffer(byte[] applyBuffer){
		distributionBuffer[4] = applyBuffer[4];
		distributionBuffer[5] = applyBuffer[5];
		distributionBuffer[6] = applyBuffer[6];
		distributionBuffer[7] = applyBuffer[7];
		distributionBuffer[8] = applyBuffer[8];
	}
}
