package com.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

import com.buffer.WorkstationBuffer;
import com.listener.WorkstationListener;
import com.util.Config;
import com.util.Tools;


/**
 * @author Boris
 *
 * 2016年7月28日
 */
public class WorkstationMsgControl implements Runnable{
	private static DatagramSocket datagramSocket;
    private WorkstationBuffer workstationBuffer;
    
    private Tools tools;
    
    private WorkstationListener workstationListener = null;
    
    private final int MAX_RECEIVE_BUFFER = 65508;
    
    static{
    	try {
			datagramSocket = new DatagramSocket(Config.WORKSTATION_UDP_PORT);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public WorkstationMsgControl(){
    	tools = Tools.getTools();
    	workstationBuffer = new WorkstationBuffer();
    }
     
    public void setWorkstationListener(WorkstationListener workstationListener) {
		this.workstationListener = workstationListener;
	}
    
    /** 
     * @Method: setFrequennce 
     * @Description: 设置频率 需在调频之前设置
     * @param frequence  射频   取值范围00 000 000~29 999 999 字符串
     * void
     */ 
    public void setFrequennce(final String frequence){
    	workstationBuffer.setFrequence(frequence);
    	workstationBuffer.initBuffer();
    }

	/** 
     * @Method: sendMessage 
     * @Description: 采用UDP/Ip 发送数据的通用接口
     * @param bytes 数据
     * @param length 长度
     * @param IP 目标IP
     * @param port 目标端口
     * @throws IOException
     * void
     */ 
    public void sendMessage(byte[] bytes, String IP, int port){
    	try {
    		DatagramPacket dpSend = new DatagramPacket(bytes, bytes.length, InetAddress.getByName(IP), port); 
			datagramSocket.send(dpSend);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /** 
     * @Method: sendApplyMessageByReportBuffer 
     * @Description: 参考汇报报发送申请报
     * @param reportBuffer 汇报报
     * void
     */ 
    public void sendApplyMessageByReportBuffer(byte[] reportBuffer){
    	workstationBuffer.initApplyBufferByReportBuffer(reportBuffer);
    	byte[] applyBuffer = workstationBuffer.getApplyBuffer();
		sendMessage(applyBuffer, tools.getLocalIP(), Config.CONTROLLER_UDP_PORT);
    }
    
    /** 
     * @Method: sendConfirmSTCPBufferByReceivedBuffer 
     * @Description: 发送接收确认报文
     * @param receivedBuffer 接收机发过来的stcp报文
     * @param IP 接收机IP
     * @param port 接收机端口
     * void
     * @throws IOException 
     */ 
    public void sendConfirmSTCPBufferByReceivedBuffer(byte[] receivedBuffer, String IP, int port){
    	workstationBuffer.initStcpBufferByReveiverStcpBuffer(receivedBuffer);
    	
        byte[] buffer = workstationBuffer.getConfirmStcpBuffer();
    	
//    	tools.printSTCP(buffer);
    	
    	sendMessage(buffer, IP, port);
    }
    
    /** 
     * @Method: setConnectSTCPBufferByReceivedBuffer 
     * @Description: 发送带有参数块的STCP报文  控制接收机
     * @param receivedBuffer 接收机返回的stcp报文
     * @param IP 接收机IP
     * @param port 接收机端口
     * void
     * @throws IOException 
     */ 
    public void sendSTCPBufferByReceivedBuffer(byte[] receivedBuffer, String IP, int port){
    	workstationBuffer.initStcpBufferByReveiverStcpBuffer(receivedBuffer);
    	byte[] buffer = workstationBuffer.getStcpBufferByReveiverStcpBuffer(receivedBuffer);
    	
    	tools.printSTCP(buffer);
    	
    	sendMessage(buffer, IP, port);
    }

    /** 
     * @Method: listenReceiver 
     * @Description: 监听接收机
     * @throws IOException
     * void
     */ 
    public void listenReceiver() throws IOException{
    	byte[] buffer = new byte[MAX_RECEIVE_BUFFER];
		DatagramPacket dpReceived = new DatagramPacket(buffer, MAX_RECEIVE_BUFFER);
		
		boolean f = true;	
		while(f){
			datagramSocket.receive(dpReceived);//接受来自接收机的STCP数据
			
			int port = dpReceived.getPort();
			String ip = dpReceived.getAddress().toString();
			ip = ip.substring(1, ip.length());  // 原ip的格式为/192.168.10.107  去反斜杠

			byte[] stcp = Arrays.copyOfRange(dpReceived.getData(), 0, dpReceived.getLength()); 
			
			if (workstationListener != null) {
				workstationListener.onReveicedSTCP(stcp, ip, port);
			}
			
			dpReceived.setLength(MAX_RECEIVE_BUFFER);
		}
    }
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			System.out.println("工作站监听接收机线程启动");
			listenReceiver();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void start(){
		new Thread(this).start();
	}
}
