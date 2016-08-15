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
public class Workstation implements Runnable{
	private DatagramSocket datagramSocket;
    private WorkstationBuffer workstationBuffer;
    
    private Tools tools;
    
    private WorkstationListener workstationListener = null;
    
    private final int MAX_RECEIVE_BUFFER = 65508;
    
    private byte[] stcp;
    
    private boolean isControled = false; //���ջ���Ƶ���Ƿ��ѵ���
    private String frequence = null; //���ջ���Ƶ��
    
    public Workstation(int port){
    	try {
			datagramSocket = new DatagramSocket(port);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	tools = Tools.getTools();
    	workstationBuffer = new WorkstationBuffer();
    	workstationBuffer.initBuffer();
    	workstationBuffer.setPort(port);
    }
     
    public void setWorkstationListener(WorkstationListener workstationListener) {
		this.workstationListener = workstationListener;
	}
    
    /** drop
     * @Method: setFrequennce 
     * @Description: ����Ƶ�� ���ڵ�Ƶ֮ǰ����
     * @param frequence  ��Ƶ   ȡֵ��Χ00 000 000~29 999 999 �ַ���
     * void
     */ 
    public void setFrequennce(final String frequence){
    	workstationBuffer.setFrequence(frequence);
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
    
    /** drop
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
     * @Method: regulatingRevevierFrequency 
     * @Description: ��ָ��ip�Ͷ˿ڵĽ��ջ�����Ƶ
     * @param frequence  ��Ƶ   ȡֵ��Χ00 000 000~29 999 999 �ַ���
     * @param IP
     * @param port
     * void
     */
    public void regulatingRevevierFrequency(String frequence,String IP, int port){
    	this.frequence = frequence;
    	isControled = false;
    	
    	workstationBuffer.setFrequence(frequence);
    	workstationBuffer.initStcpBufferByReveiverStcpBuffer(stcp);
    	byte[] buffer = workstationBuffer.getStcpBufferByReveiverStcpBuffer(stcp);
    	
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

			stcp = Arrays.copyOfRange(dpReceived.getData(), 0, dpReceived.getLength()); //�յ��ı���
			
			//����ȷ�ϱ�
			sendConfirmSTCPBufferByReceivedBuffer(stcp, ip, port);
			
//			tools.printSTCP(stcp);
			
			//�жϵ�Ƶ�Ƿ�ɹ�
			if (frequence != null && !isControled) {
				byte[] frequenceBety = frequence.getBytes();
				for (int i = 0, j = 0; i < stcp.length; i++) {
					if (stcp[i] == frequenceBety[j]) {
						j++;
						if (j == frequence.length()) {
							System.out.println("����Ƶ�ɹ���\n");
							isControled = true;
							break;
						}
					}else{
						if (j != 0) {
							j = 0;
							i--;
						}
					}
				}
			}
			
			//ȡ��Ƶ�������
			if (isControled) {
				int i = 15;
				//��������������ʼλ��
				for (; i < stcp.length; i++) {
					if ((stcp[i] & 0xFF) == 0x81) {
						i += 4;
						break;
					}
				}
				if (workstationListener != null) {
					workstationListener.onRevivedData(stcp, i);
				}
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
