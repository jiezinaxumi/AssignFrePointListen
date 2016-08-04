package com.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
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

	private static Socket tcpSocket = null;
	private DatagramSocket udpSocket = null;
	
	private static ControllerListener controllerListener;
	private ControllerBuffer controllerBuffer;

	private final static String LISTEN_RECEIVER = "listenReceiver";
	private final static String LISTENE_WORKSTATION = "listeneWorkstation";
	
	
	
	//收到的报文类型
	private enum ReceivedBufferType{
		REGISTER,//登记报
		REPORT,//汇报报
		DETECT,//探测报
		APPLY,//申请报
		OTHER//其它
	}
	
	public ControllerService() throws SocketException{
		udpSocket = new DatagramSocket(Config.CONTROLLER_UDP_PORT);
		controllerBuffer = new ControllerBuffer();
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
		Tools.printHexString(controllerBuffer.getMakeSureBuffer());
		
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
		Tools.printHexString(controllerBuffer.getDistributionBuffer());
		
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
				Tools.printHexString(buffer, length);
				
				//判断报文类型（登记报还是汇报报等）
				ReceivedBufferType type = judgeBufferType(buffer);
				//根据报文类型 发送对应的信息
				switch (type) {
				case REGISTER://登记报
					sendMakeSureMessage(buffer);
					break;
				case DETECT://探测报
					break;
				case REPORT://汇报报 
					controllerListener.onReceivedReportMsg(buffer);
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
	public void listeneWorkstation() throws IOException{
		byte[] buffer = new byte[1024];
		DatagramPacket dpReceived = new DatagramPacket(buffer, 1024);
		
		boolean f = true;
		while(f){
			udpSocket.receive(dpReceived);//接受来自工作站的数据
			byte[] receivedBuffer = Arrays.copyOfRange(dpReceived.getData(), dpReceived.getOffset(), 
					dpReceived.getOffset() + dpReceived.getLength()); 
			
			//判断报文类型（登记报还是汇报报等）
			ReceivedBufferType type = judgeBufferType(receivedBuffer);
			//根据报文类型 发送对应的信息
			switch (type) {
			case APPLY://申请报
				System.out.print("收到");
				//打印
				Tools.printHexString(receivedBuffer);

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
			try {
				listeneWorkstation();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	/** 
	 * @Method: start 
	 * @Description: 开启服务（控制器）
	 * void
	 */ 
	public static void start(){
		//服务端在5770端口监听客户端请求的TCP连接  
        ServerSocket server;
		try {
			server = new ServerSocket(Config.CONTROLLER_TCP_PORT);
			ControllerService controllerService = new ControllerService();
			
			Socket receiver = null;  
			boolean f = true;  
			while(f){  
				//等待客户端的连接，如果没有获取连接  
				receiver = server.accept(); 
				tcpSocket = receiver;
				System.out.println("与客户端连接成功！");  
				//为每个客户端连接开启一个线程  
				new Thread(controllerService, ControllerService.LISTEN_RECEIVER).start();
				new Thread(controllerService, ControllerService.LISTENE_WORKSTATION).start();            
				
				f = false;
			}  
			server.close();  
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
	
	/** 
	 * @Method: setControllerListener 
	 * @Description: 设置控制器的监听， 收到报文时处罚
	 * @param listener
	 * void
	 */ 
	public static  void setControllerListener(ControllerListener listener){
		controllerListener = listener;
	}
}