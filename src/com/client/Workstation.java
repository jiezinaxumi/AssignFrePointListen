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
 * 2016��7��28��
 */
public class Workstation implements Runnable{
    private WorkstationBuffer workstationBuffer;
    private DatagramSocket datagramSocket;
    
    private final int MAX_RECEIVE_BUFFER = 65508;
    
    /**
     * @param port ����վ�˿�
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
     * @Description: ����UDP/Ip �������ݵ�ͨ�ýӿ�
     * @param buffer ����
     * @param length ����
     * @param IP Ŀ��IP
     * @param port Ŀ��˿�
     * @throws IOException
     * void
     */ 
    public void sendMessage(byte []buffer, String IP, int port) throws IOException{
    	DatagramPacket dpSend = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(IP), port); 
    	datagramSocket.send(dpSend);
    }
    
    /** 
     * @Method: sendApplyMessageByReportBuffer 
     * @Description: �ο��㱨���������뱨
     * @param reportBuffer �㱨��
     * void
     */ 
    public void sendApplyMessageByReportBuffer(byte[] reportBuffer){
    	workstationBuffer.initApplyBufferByReportBuffer(reportBuffer);
    	try {
    		byte[] applyBuffer = workstationBuffer.getApplyBuffer();
    		
    		//��ӡ
    		System.out.print("����");
    		Tools.printHexString(applyBuffer);
    		
			sendMessage(applyBuffer, Config.CONTROLLER_IP, Config.CONTROLLER_UDP_PORT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /** 
     * @Method: setConnectSTCPBufferByReceivedBuffer 
     * @Description: ����ȷ�����ӣ��յ����ջ�����������������Ӧ��
     * @param receivedBuffer ���������stcp����
     * void
     * @throws IOException 
     */ 
    public void sendConnectSTCPBufferByReceivedBuffer(byte[] receivedBuffer) throws IOException{
    	workstationBuffer.initStcpBufferByReveiverStcpBuffer(receivedBuffer);
    	sendMessage(workstationBuffer.getStcpControlBuffer(), Config.RECEIVER_IP, Config.RECEIVER_UDP_PORT);
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
			datagramSocket.receive(dpReceived);//�������Խ��ջ�������
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
			System.out.println("����վ�������ջ��߳�����");
			listenReceiver();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
    

}
