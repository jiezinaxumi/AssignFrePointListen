package com.buffer;

import com.util.Config;
import com.util.Tools;

/**
 * @author Boris
 *
 * 2016年7月28日
 */
public class WorkstationBuffer extends Buffer {
	private final boolean isSettingReceiverModel = true; //设置接收机型号
	
	private byte[] applyBuffer = new byte[isSettingReceiverModel? 68 : 52]; //申请报
	private byte[] stcpControlBuffer = new byte[16]; //stcp 控制块
	private byte[] stcpArgsBuffer = new byte[MAX_SIZE]; //stcp参数块
	private byte[] stcpBuffer;
	
	private int stcpSize = 0; //报文长度
	
	private Tools tools = Tools.getTools();
	
	//设置接受机的频段
	public void setFrequence(final String frequence) {
		// 参数区
		String args = "";
		if (frequence != null) {
			args += "F" + frequence;
		}
		args += "M01" + "DAT" + Integer.toHexString(Config.RETURN_TYPE)
				+ "LEN1024" + "T128"; // 设置射频和返回的数据类型
		byte[] argsBuffer = args.getBytes();
		int argsSize = argsBuffer.length;

		stcpArgsBuffer[3] = (byte) (argsSize & 0xFF);
		stcpArgsBuffer[4] = (byte) (argsSize >> 8 & 0xFF);

		// 添加参数
		System.arraycopy(argsBuffer, 0, stcpArgsBuffer, 5, argsSize);

		stcpSize = argsSize + 5 + 16; // 报文长度 = 参数 区 + 参数块（5字节）+ 控制块（16字节）
	}

	public void setPort(int port){
		applyBuffer[32] = (byte) (port & 0x00FF);
		applyBuffer[33] = (byte) (port >> 8 & 0xFF);
	}

	/** 
	 * @Method: getApplyBuffer 
	 * @Description: 返回申请报
	 * @return
	 * byte[]
	 */ 
	public byte[] getApplyBuffer(){
		return applyBuffer;
	}

	/** 
	 * @Method: getConfirmStcpBuffer 
	 * @Description: 返回确认连接的STCP报文 无参数块
	 * @return
	 * byte[]
	 */ 
	public byte[] getConfirmStcpBuffer() {
		return stcpControlBuffer;
	}
	
	/** 
	 * @Method: getStcpBuffer 
	 * @Description: 返回带有控制信息的STCP报文 有参数块
	 * @return
	 * byte[]
	 */ 
	public byte[] getStcpBufferByReveiverStcpBuffer(byte[] buffer){
		//重设控制块信息
		stcpControlBuffer[6] = 0x02;
		stcpControlBuffer[8] = buffer[0];
		stcpControlBuffer[9] = buffer[1];
		stcpControlBuffer[10] = buffer[2];
		stcpControlBuffer[11] = buffer[3];
		stcpControlBuffer[14] = (byte) (stcpSize & 0xFF);
		stcpControlBuffer[15] = (byte) (stcpSize >> 8 & 0xFF);
		
		stcpBuffer = new byte[stcpSize];
		
		//将控制块和参数块组装
		System.arraycopy(stcpControlBuffer, 0, stcpBuffer, 0, stcpControlBuffer.length);
		System.arraycopy(stcpArgsBuffer, 0, stcpBuffer, stcpControlBuffer.length, stcpSize - stcpControlBuffer.length);
		return stcpBuffer;	
	}

	@Override
	public void initBuffer() {
		// TODO Auto-generated method stub
		//申请报
		applyBuffer[0] = 0x04;
		applyBuffer[1] = isSettingReceiverModel? 0x44 : 0x34;//当选定接收机型号时填入0x44
		applyBuffer[2] = 0x00;
		applyBuffer[3] = getCheckCode(applyBuffer, 3);
		applyBuffer[5] = Config.UNIT_NUMBER & 0xFF;
		applyBuffer[6] = Config.UNIT_NUMBER >> 8 & 0xFF;
		applyBuffer[9] = isSettingReceiverModel? 0x05 : 0x04;
		applyBuffer[10] = 0x06;
		applyBuffer[11] = 0x00;
		applyBuffer[12] = Config.USE_WAY;
		applyBuffer[13] = 0x07;
		applyBuffer[14] = 0x00;		
		applyBuffer[30] = 0x08;
		applyBuffer[31] = 0x00;
		applyBuffer[34] = 0x20;
		applyBuffer[35] = 0x00;
		
		//ip
		byte[] ip = tools.getLocalIP().getBytes();
		System.arraycopy(ip, 0, applyBuffer, 15, ip.length);
		
		//申请口令
		byte[] applyPwd = Config.APPLAY_PWD.getBytes();
		System.arraycopy(applyPwd, 0, applyBuffer, 36, applyPwd.length);
		
		//stcp 参数模块
		stcpArgsBuffer[0] = 0x7E;
		stcpArgsBuffer[1] = 0x16;
		stcpArgsBuffer[2] = (byte)0x8F;
	}
	
	/** 
	 * @Method: initApplyBufferByReportBuffer 
	 * @Description: 根据汇报报初始化申请报
	 * @param reportBuffer 汇报报
	 * void
	 */ 
	public void initApplyBufferByReportBuffer(byte[] reportBuffer){
		applyBuffer[4] = reportBuffer[4];
		applyBuffer[7] = reportBuffer[7];
		applyBuffer[8] = reportBuffer[8];
		if (isSettingReceiverModel) {
			applyBuffer[52] = 0x00;
			applyBuffer[53] = 0x00;
			System.arraycopy(reportBuffer, 12, applyBuffer, 54, 14);
		}
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
		
		stcpControlBuffer[8] = buffer[0];
		stcpControlBuffer[9] = buffer[1];
		stcpControlBuffer[10] = buffer[2];
		stcpControlBuffer[11] = buffer[3];
		
		stcpControlBuffer[12] = 0x00;
		stcpControlBuffer[13] = 0x00;
		stcpControlBuffer[14] = (byte)0x10;
		stcpControlBuffer[15] = 0x00;
		
	}
}
