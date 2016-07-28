package com.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.buffer.ControllerBuffer;
import com.util.Tools;

public class ControllerService implements Runnable {

	private Socket client = null;
	
	//收到的报文类型
	private enum ReceivedBufferType{
		REGISTER,//登记报
		REPORT,//汇报报
		DETECT,//探测报
		APPLY,//申请报
		OTHER//其它
	}

	public ControllerService(Socket client) {
		this.client = client;
	}
	
	/**
	 * @Method: sendBuffer 
	 * @Description: 发送数据的统一接口
	 * @param buffer 数据
	 * void
	 */
	public void sendBuffer(final byte buffer[]){
		try {
			OutputStream os = client.getOutputStream();
			os.write(buffer);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/**
	 * @Method: judgeBufferType 
	 * @Description: 判断报文类型
	 * @param buffer 报文
	 * @return 返回报文类型
	 * ReceivedBufferType
	 */
	public ReceivedBufferType judgeBufferType(final byte[] buffer) {
		switch (buffer[0] & 0xff) {
		case 0x00:
			return ReceivedBufferType.REGISTER;
		case 0x01:
			return ReceivedBufferType.REPORT;
		case 0x04:
			return ReceivedBufferType.APPLY;
		case 0x05:
			return ReceivedBufferType.DETECT;
		default:
			return ReceivedBufferType.OTHER;
		}
	}
	
	/** 
	 * @Method: sendMakeSureMessage 
	 * @Description: 发送确认报
	 * @param buffer 登记报（需根据登记报设置确认报）
	 * void
	 */ 
	public void sendMakeSureMessage(final byte[] buffer){
		ControllerBuffer controllerBuffer = new ControllerBuffer();
		controllerBuffer.setAntennaPort(buffer[7], buffer[8]);
		controllerBuffer.setPositionCode(buffer[4]);
		controllerBuffer.setUnitCode(buffer[5], buffer[6]);
		
		sendBuffer(controllerBuffer.getMakeSureBuffer());
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			// 获得输入流
			InputStream is = client.getInputStream();

			while (true) {
				//接收到的数据
				byte[] buffer = new byte[1024];
				int length = is.read(buffer);
				
				//输出
				Tools.printHexString(buffer, length);
				
				//判断报文类型（登记报还是汇报报等）
				ReceivedBufferType type = judgeBufferType(buffer);
				//根据报文类型 发送对应的信息
				switch (type) {
				case REGISTER://登记报
					sendMakeSureMessage(buffer);
					break;
				case DETECT://探测报
					System.out.println("探测报");
					break;
				case APPLY://申请报
					break;
				case REPORT://汇报报 
					break;
				default:
					break;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}