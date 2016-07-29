package com.buffer;

import com.util.Config;
import com.util.Tools;

/**
 * @author Boris
 *
 * 2016年7月28日
 */
public class WorkstationBuffer extends Buffer {
	private byte[] applyBuffer = new byte[68]; //申请报
	private byte[] stcpControlBuffer = new byte[16]; //stcp 控制块
	
	public WorkstationBuffer(){
		initBuffer();
	}
	
	public byte[] getApplyBuffer(){
		return applyBuffer;
	}

	public byte[] getStcpControlBuffer() {
		return stcpControlBuffer;
	}

	@Override
	public void initBuffer() {
		// TODO Auto-generated method stub
		applyBuffer[0] = 0x04;
		applyBuffer[1] = 0x34;//当选定接收机型号时填入0x44
		applyBuffer[2] = 0x00;
		applyBuffer[3] = getCheckCode(applyBuffer, 3);
		applyBuffer[9] = 0x04;
		applyBuffer[10] = 0x06;
		applyBuffer[11] = 0x00;
		applyBuffer[12] = Config.USE_WAY;
		applyBuffer[13] = 0x07;
		applyBuffer[14] = 0x00;		
		applyBuffer[30] = 0x08;
		applyBuffer[31] = 0x00;
		applyBuffer[32] = (byte) (Config.WORKSTATION_UDP_PORT & 0x00FF);
		applyBuffer[33] = (byte) (Config.WORKSTATION_UDP_PORT >> 8 & 0xFF);
		applyBuffer[34] = 0x20;
		applyBuffer[35] = 0x00;
		
		//ip
		byte[] ip = Config.WORKSTATION_IP.getBytes();
		System.arraycopy(ip, 0, applyBuffer, 15, ip.length);
		
		//申请口令
		byte[] applyPwd = Config.APPLAY_PWD.getBytes();
		System.arraycopy(applyPwd, 0, applyBuffer, 36, applyPwd.length);	
	}
	
	/** 
	 * @Method: initApplyBufferByReportBuffer 
	 * @Description: 根据汇报报初始化申请报
	 * @param reportBuffer 汇报报
	 * void
	 */ 
	public void initApplyBufferByReportBuffer(byte[] reportBuffer){
		applyBuffer[4] = reportBuffer[4];
		applyBuffer[5] = reportBuffer[5];
		applyBuffer[6] = reportBuffer[6];
		applyBuffer[7] = reportBuffer[7];
		applyBuffer[8] = reportBuffer[8];
	}
	
	/** 
	 * @Method: initStcpBufferByReveiverStcpBuffer 
	 * @Description: 初始化STCP 参考接收机发过来的STCP报文
	 * @param buffer 接收机STCP报文
	 * void
	 */ 
	public void initStcpBufferByReveiverStcpBuffer(byte[] buffer){		
		long headSequence = (buffer[0] & 0xFF) + ((buffer[1] & 0xFF) << 8) + ((buffer[2] & 0xFF) << 16) + ((buffer[3] & 0xFF) << 24);
		headSequence = (headSequence + 1) % (1L << 32);
		
		stcpControlBuffer[0] = (byte) (headSequence & 0xFF);
		stcpControlBuffer[1] = (byte) (headSequence >> 8 & 0xFF);
		stcpControlBuffer[2] = (byte) (headSequence >> 16 & 0xFF);
		stcpControlBuffer[3] = (byte) (headSequence >> 24 & 0xFF);
		
		stcpControlBuffer[4] = getCheckCode(stcpControlBuffer, 4);
		stcpControlBuffer[5] = buffer[5];
		
		stcpControlBuffer[6] = 0x00;
		stcpControlBuffer[7] = 0x01;
		stcpControlBuffer[8] = 0x00;
		stcpControlBuffer[9] = 0x00;
		stcpControlBuffer[10] = 0x00;
		stcpControlBuffer[11] = 0x00;
		
		stcpControlBuffer[12] = 0x00;
		stcpControlBuffer[13] = 0x00;
		stcpControlBuffer[14] = (byte)0xF1;
		stcpControlBuffer[15] = 0x00;
		
	}
}
