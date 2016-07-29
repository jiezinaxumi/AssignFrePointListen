package com.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

import com.buffer.WorkstationBuffer;
import com.util.Config;
import com.util.Tools;


/**
 * @author Boris
 *
 * 2016年7月28日
 */
public class Workstation implements Runnable{
    private WorkstationBuffer workstationBuffer;
    private DatagramSocket datagramSocket;
    
    private final int MAX_RECEIVE_BUFFER = 65508;
    
    /**
     * @param port 工作站端口
     */
    public Workstation(int port){
    	try {
			datagramSocket = new DatagramSocket(port);
			workstationBuffer = new WorkstationBuffer();
			
			new Thread(this).start();
			
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /** 
     * @Method: sendMessage 
     * @Description: 采用UDP/Ip 发送数据的通用接口
     * @param buffer 数据
     * @param length 长度
     * @param IP 目标IP
     * @param port 目标端口
     * @throws IOException
     * void
     */ 
    public void sendMessage(byte []buffer, String IP, int port) throws IOException{
    	DatagramPacket dpSend = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(IP), port); 
    	datagramSocket.send(dpSend);
    }
    
    /** 
     * @Method: sendApplyMessageByReportBuffer 
     * @Description: 参考汇报报发送申请报
     * @param reportBuffer 汇报报
     * void
     */ 
    public void sendApplyMessageByReportBuffer(byte[] reportBuffer){
    	workstationBuffer.initApplyBufferByReportBuffer(reportBuffer);
    	try {
    		byte[] applyBuffer = workstationBuffer.getApplyBuffer();
    		
    		//打印
    		System.out.print("发送");
    		Tools.printHexString(applyBuffer);
    		
			sendMessage(applyBuffer, Config.CONTROLLER_IP, Config.CONTROLLER_UDP_PORT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /** 
     * @Method: setConnectSTCPBufferByReceivedBuffer 
     * @Description: 发送确认连接（收到接收机的虚连接请求，做出应答）
     * @param receivedBuffer 连接请求的stcp报文
     * void
     * @throws IOException 
     */ 
    public void sendConnectSTCPBufferByReceivedBuffer(byte[] receivedBuffer) throws IOException{
    	workstationBuffer.initStcpBufferByReveiverStcpBuffer(receivedBuffer);
    	sendMessage(workstationBuffer.getStcpControlBuffer(), Config.RECEIVER_IP, Config.RECEIVER_UDP_PORT);
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
			datagramSocket.receive(dpReceived);//接受来自接收机的数据
			byte[] receivedBuffer = Arrays.copyOfRange(dpReceived.getData(), 0, dpReceived.getLength()); 
			
			Tools.printHexString(receivedBuffer);
			
//			sendConnectSTCPBufferByReceivedBuffer(receivedBuffer);
			
			
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
    

}
