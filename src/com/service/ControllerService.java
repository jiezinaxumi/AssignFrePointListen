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
 * 2016��7��28��
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
	
	//�յ��ı�������
	private static enum ReceivedBufferType{
		REGISTER,//�ǼǱ�
		REPORT,//�㱨��
		DETECT,//̽�ⱨ
		APPLY,//���뱨
		OTHER//����
	}
	
	public ControllerService(){};
	
	public ControllerService(Socket socket) throws IOException{
		tcpSocket = socket;
		controllerBuffer = new ControllerBuffer();
		tools = Tools.getTools();
	}
	
	/** 
	 * @Method: setControllerListener 
	 * @Description: ���ÿ������ļ����� �յ�����ʱ����
	 * @param listener
	 * void
	 */ 
	public void setControllerListener(ControllerListener listener){
		controllerListener = listener;
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
		tools.printHexString(controllerBuffer.getMakeSureBuffer());
		
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
		tools.printHexString(controllerBuffer.getDistributionBuffer());
		
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
				tools.printHexString(buffer, length);
				
				//�жϱ������ͣ��ǼǱ����ǻ㱨���ȣ�
				ReceivedBufferType type = judgeBufferType(buffer);
				//���ݱ������� ���Ͷ�Ӧ����Ϣ
				switch (type) {
				case REGISTER://�ǼǱ�
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
				case DETECT://̽�ⱨ
					break;
				case REPORT://�㱨�� 
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
	 * @Description: ��������վ
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
			}//�������Թ���վ������
			byte[] receivedBuffer = Arrays.copyOfRange(dpReceived.getData(), dpReceived.getOffset(), 
					dpReceived.getOffset() + dpReceived.getLength()); 
			
			//�жϱ������ͣ��ǼǱ����ǻ㱨���ȣ�
			ReceivedBufferType type = judgeBufferType(receivedBuffer);
			//���ݱ������� ���Ͷ�Ӧ����Ϣ
			switch (type) {
			case APPLY://���뱨
				//��ӡ
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