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
 * 2016��7��28��
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
     * @Description: ����Ƶ�� ���ڵ�Ƶ֮ǰ����
     * @param frequence  ��Ƶ   ȡֵ��Χ00 000 000~29 999 999 �ַ���
     * void
     */ 
    public void setFrequennce(final String frequence){
    	workstationBuffer.setFrequence(frequence);
    	workstationBuffer.initBuffer();
    }

	/** 
     * @Method: sendMessage 
     * @Description: ����UDP/Ip �������ݵ�ͨ�ýӿ�
     * @param bytes ����
     * @param length ����
     * @param IP Ŀ��IP
     * @param port Ŀ��˿�
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
     * @Description: �ο��㱨���������뱨
     * @param reportBuffer �㱨��
     * void
     */ 
    public void sendApplyMessageByReportBuffer(byte[] reportBuffer){
    	workstationBuffer.initApplyBufferByReportBuffer(reportBuffer);
    	byte[] applyBuffer = workstationBuffer.getApplyBuffer();
		sendMessage(applyBuffer, tools.getLocalIP(), Config.CONTROLLER_UDP_PORT);
    }
    
    /** 
     * @Method: sendConfirmSTCPBufferByReceivedBuffer 
     * @Description: ���ͽ���ȷ�ϱ���
     * @param receivedBuffer ���ջ���������stcp����
     * @param IP ���ջ�IP
     * @param port ���ջ��˿�
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
     * @Description: ���ʹ��в������STCP����  ���ƽ��ջ�
     * @param receivedBuffer ���ջ����ص�stcp����
     * @param IP ���ջ�IP
     * @param port ���ջ��˿�
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
     * @Description: �������ջ�
     * @throws IOException
     * void
     */ 
    public void listenReceiver() throws IOException{
    	byte[] buffer = new byte[MAX_RECEIVE_BUFFER];
		DatagramPacket dpReceived = new DatagramPacket(buffer, MAX_RECEIVE_BUFFER);
		
		boolean f = true;	
		while(f){
			datagramSocket.receive(dpReceived);//�������Խ��ջ���STCP����
			
			int port = dpReceived.getPort();
			String ip = dpReceived.getAddress().toString();
			ip = ip.substring(1, ip.length());  // ԭip�ĸ�ʽΪ/192.168.10.107  ȥ��б��

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
			System.out.println("����վ�������ջ��߳�����");
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
