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
 * 2016��7��28��
 */
public class ControllerService implements Runnable {

	private static Socket tcpSocket = null;
	private DatagramSocket udpSocket = null;
	
	private static ControllerListener controllerListener;
	private ControllerBuffer controllerBuffer;

	private final static String LISTEN_RECEIVER = "listenReceiver";
	private final static String LISTENE_WORKSTATION = "listeneWorkstation";
	
	
	
	//�յ��ı�������
	private enum ReceivedBufferType{
		REGISTER,//�ǼǱ�
		REPORT,//�㱨��
		DETECT,//̽�ⱨ
		APPLY,//���뱨
		OTHER//����
	}
	
	public ControllerService() throws SocketException{
		udpSocket = new DatagramSocket(Config.CONTROLLER_UDP_PORT);
		controllerBuffer = new ControllerBuffer();
	}
	
	/**
	 * @Method: sendBuffer 
	 * @Description: �������ݵ�ͳһ�ӿ�
	 * @param buffer ����
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
	 * @Description: �жϱ�������
	 * @param buffer ����
	 * @return ���ر�������
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
	 * @Description: ����ȷ�ϱ�
	 * @param buffer �ǼǱ�������ݵǼǱ�����ȷ�ϱ���
	 * void
	 */ 
	public void sendMakeSureMessage(final byte[] registerBuffer){
		controllerBuffer.initMakeSureBufferByRegisterBuffer(registerBuffer);
		
		//��ӡ
		Tools.printHexString(controllerBuffer.getMakeSureBuffer());
		
		sendBuffer(controllerBuffer.getMakeSureBuffer());
	}
	
	/** 
	 * @Method: sendDistributionBuffer 
	 * @Description: ���ͷ��䱨
	 * @param applyBuffer ���뱨
	 * void
	 */ 
	public void sendDistributionBuffer(final byte[] applyBuffer){
		controllerBuffer.initDistributionBufferByApplyBuffer(applyBuffer);
		
		//��ӡ
		Tools.printHexString(controllerBuffer.getDistributionBuffer());
		
		sendBuffer(controllerBuffer.getDistributionBuffer());
	}
	
	/** 
	 * @Method: listenReveiver 
	 * @Description: �������ջ�
	 * void
	 */ 
	public void listenReveiver(){
		try {
			// ���������
			InputStream is = tcpSocket.getInputStream();

			while (true) {
				//���յ�������
				byte[] buffer = new byte[1024];
				int length = is.read(buffer);
				
				//���
				Tools.printHexString(buffer, length);
				
				//�жϱ������ͣ��ǼǱ����ǻ㱨���ȣ�
				ReceivedBufferType type = judgeBufferType(buffer);
				//���ݱ������� ���Ͷ�Ӧ����Ϣ
				switch (type) {
				case REGISTER://�ǼǱ�
					sendMakeSureMessage(buffer);
					break;
				case DETECT://̽�ⱨ
					break;
				case REPORT://�㱨�� 
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
	 * @Description: ��������վ
	 * void
	 */ 
	public void listeneWorkstation() throws IOException{
		byte[] buffer = new byte[1024];
		DatagramPacket dpReceived = new DatagramPacket(buffer, 1024);
		
		boolean f = true;
		while(f){
			udpSocket.receive(dpReceived);//�������Թ���վ������
			byte[] receivedBuffer = Arrays.copyOfRange(dpReceived.getData(), dpReceived.getOffset(), 
					dpReceived.getOffset() + dpReceived.getLength()); 
			
			//�жϱ������ͣ��ǼǱ����ǻ㱨���ȣ�
			ReceivedBufferType type = judgeBufferType(receivedBuffer);
			//���ݱ������� ���Ͷ�Ӧ����Ϣ
			switch (type) {
			case APPLY://���뱨
				System.out.print("�յ�");
				//��ӡ
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
	 * @Description: �������񣨿�������
	 * void
	 */ 
	public static void start(){
		//�������5770�˿ڼ����ͻ��������TCP����  
        ServerSocket server;
		try {
			server = new ServerSocket(Config.CONTROLLER_TCP_PORT);
			ControllerService controllerService = new ControllerService();
			
			Socket receiver = null;  
			boolean f = true;  
			while(f){  
				//�ȴ��ͻ��˵����ӣ����û�л�ȡ����  
				receiver = server.accept(); 
				tcpSocket = receiver;
				System.out.println("��ͻ������ӳɹ���");  
				//Ϊÿ���ͻ������ӿ���һ���߳�  
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
	 * @Description: ���ÿ������ļ����� �յ�����ʱ����
	 * @param listener
	 * void
	 */ 
	public static  void setControllerListener(ControllerListener listener){
		controllerListener = listener;
	}
}