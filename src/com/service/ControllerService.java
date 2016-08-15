package com.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.Arrays;

import com.buffer.ControllerBuffer;
import com.listener.ControllerListener;
import com.util.Config;
import com.util.Tools;

/**
 * @author Boris
 *
 * 2016年7月28日
 */
public class ControllerService implements Runnable {
	private static DatagramSocket udpSocket = null;
	private Socket tcpSocket = null;
	
	private ControllerListener controllerListener =  null;
	private ControllerBuffer controllerBuffer;
	private Tools tools;

	public final static String LISTEN_RECEIVER = "listenReceiver";
	public final static String LISTENE_WORKSTATION = "listeneWorkstation";
	
	static{
		try {
			udpSocket = new DatagramSocket(Config.CONTROLLER_UDP_PORT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//收到的报文类型
	private static enum ReceivedBufferType{
		REGISTER,//登记报
		REPORT,//汇报报
		DETECT,//探测报
		APPLY,//申请报
		OTHER//其它
	}
	
	public ControllerService(){};
	
	public ControllerService(Socket socket) throws IOException{
		tcpSocket = socket;
		controllerBuffer = new ControllerBuffer();
		tools = Tools.getTools();
	}
	
	/** 
	 * @Method: setControllerListener 
	 * @Description: 设置控制器的监听， 收到报文时处罚
	 * @param listener
	 * void
	 */ 
	public void setControllerListener(ControllerListener listener){
		controllerListener = listener;
	}
	
	/**
	 * @Method: sendBuffer 
	 * @Description: 发送数据的统一接口
	 * @param buffer 数据
	 * void
	 */
	public void sendBuffer(final byte buffer[]){
		try {
			OutputStream os = tcpSocket.getOutputStream();
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
	public void sendMakeSureMessage(final byte[] registerBuffer){
		controllerBuffer.initMakeSureBufferByRegisterBuffer(registerBuffer);
		
		//打印
		tools.printHexString(controllerBuffer.getMakeSureBuffer());
		
		sendBuffer(controllerBuffer.getMakeSureBuffer());
	}
	
	/** 
	 * @Method: sendDistributionBuffer 
	 * @Description: 发送分配报
	 * @param applyBuffer 申请报
	 * void
	 */ 
	public void sendDistributionBuffer(final byte[] applyBuffer){
		controllerBuffer.initDistributionBufferByApplyBuffer(applyBuffer);
		
		//打印
		tools.printHexString(controllerBuffer.getDistributionBuffer());
		
		sendBuffer(controllerBuffer.getDistributionBuffer());
	}
	
	/** 
	 * @Method: listenReveiver 
	 * @Description: 监听接收机
	 * void
	 */ 
	public void listenReveiver(){
		try {
			// 获得输入流
			InputStream is = tcpSocket.getInputStream();

			while (true) {
				//接收到的数据
				byte[] buffer = new byte[1024];
				int length = is.read(buffer);
				
				//输出
				tools.printHexString(buffer, length);
				
				//判断报文类型（登记报还是汇报报等）
				ReceivedBufferType type = judgeBufferType(buffer);
				//根据报文类型 发送对应的信息
				switch (type) {
				case REGISTER://登记报
					String ip = new String(buffer, 52, 15).trim();;
					int port = ((buffer[70] & 0xFF) << 8) + (buffer[69] & 0xFF);
					String[] receivers = tools.getProperty("receivers").split(",");
					for (String receiver : receivers) {
						String rPort = tools.getProperty(receiver + ".port");
						String rIp = tools.getProperty(receiver + ".ip");
						String workstationPort =  tools.getProperty(receiver + ".workstation.port");
						if (ip.equals(rIp) && port == Integer.parseInt(rPort)) {
							sendMakeSureMessage(buffer);
							controllerListener.onNeedCreatedWorkstation(Integer.parseInt(workstationPort));
						}
					}
					break;
				case DETECT://探测报
					break;
				case REPORT://汇报报 
					if (controllerListener != null) {
						controllerListener.onReceivedReportMsg(buffer);
					}
					break;
				default:
					break;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** 
	 * @throws IOException 
	 * @Method: listeneWorkstation 
	 * @Description: 监听工作站
	 * void
	 */ 
	public void listeneWorkstation(){
		byte[] buffer = new byte[1024];
		DatagramPacket dpReceived = new DatagramPacket(buffer, 1024);
		
		boolean f = true;
		while(f){
			try {
				udpSocket.receive(dpReceived);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}//接受来自工作站的数据
			byte[] receivedBuffer = Arrays.copyOfRange(dpReceived.getData(), dpReceived.getOffset(), 
					dpReceived.getOffset() + dpReceived.getLength()); 
			
			//判断报文类型（登记报还是汇报报等）
			ReceivedBufferType type = judgeBufferType(receivedBuffer);
			//根据报文类型 发送对应的信息
			switch (type) {
			case APPLY://申请报
				//打印
				tools.printHexString(receivedBuffer);

				sendDistributionBuffer(receivedBuffer);
				break;
			default:
				break;
			}
			dpReceived.setLength(1024);
		}
		
		udpSocket.close();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		if (Thread.currentThread().getName().equals(LISTEN_RECEIVER)) {
			listenReveiver();
		}else{
			listeneWorkstation();
		}
		
	}
	
}