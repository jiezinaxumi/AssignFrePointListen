package com.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.buffer.ControllerBuffer;
import com.util.Tools;

public class ControllerService implements Runnable {

	private Socket client = null;
	
	//�յ��ı�������
	private enum ReceivedBufferType{
		REGISTER,//�ǼǱ�
		REPORT,//�㱨��
		DETECT,//̽�ⱨ
		APPLY,//���뱨
		OTHER//����
	}

	public ControllerService(Socket client) {
		this.client = client;
	}
	
	/**
	 * @Method: sendBuffer 
	 * @Description: �������ݵ�ͳһ�ӿ�
	 * @param buffer ����
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
			// ���������
			InputStream is = client.getInputStream();

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
					System.out.println("̽�ⱨ");
					break;
				case APPLY://���뱨
					break;
				case REPORT://�㱨�� 
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