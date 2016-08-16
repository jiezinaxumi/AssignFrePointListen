package com.buffer;

import com.util.Config;
import com.util.Tools;

/**
 * @author Boris
 *
 * 2016��7��28��
 */
public class WorkstationBuffer extends Buffer {
	private final boolean isSettingReceiverModel = true; //���ý��ջ��ͺ�
	
	private byte[] applyBuffer = new byte[isSettingReceiverModel? 68 : 52]; //���뱨
	private byte[] stcpControlBuffer = new byte[16]; //stcp ���ƿ�
	private byte[] stcpArgsBuffer = new byte[MAX_SIZE]; //stcp������
	private byte[] stcpBuffer;
	
	private int stcpSize = 0; //���ĳ���
	
	private Tools tools = Tools.getTools();
	
	//���ý��ܻ���Ƶ��
	public void setFrequence(final String frequence) {
		// ������
		String args = "";
		if (frequence != null) {
			args += "F" + frequence;
		}
		args += "M01" + "DAT" + Integer.toHexString(Config.RETURN_TYPE)
				+ "LEN1024" + "T128"; // ������Ƶ�ͷ��ص���������
		byte[] argsBuffer = args.getBytes();
		int argsSize = argsBuffer.length;

		stcpArgsBuffer[3] = (byte) (argsSize & 0xFF);
		stcpArgsBuffer[4] = (byte) (argsSize >> 8 & 0xFF);

		// ��Ӳ���
		System.arraycopy(argsBuffer, 0, stcpArgsBuffer, 5, argsSize);

		stcpSize = argsSize + 5 + 16; // ���ĳ��� = ���� �� + �����飨5�ֽڣ�+ ���ƿ飨16�ֽڣ�
	}

	public void setPort(int port){
		applyBuffer[32] = (byte) (port & 0x00FF);
		applyBuffer[33] = (byte) (port >> 8 & 0xFF);
	}

	/** 
	 * @Method: getApplyBuffer 
	 * @Description: �������뱨
	 * @return
	 * byte[]
	 */ 
	public byte[] getApplyBuffer(){
		return applyBuffer;
	}

	/** 
	 * @Method: getConfirmStcpBuffer 
	 * @Description: ����ȷ�����ӵ�STCP���� �޲�����
	 * @return
	 * byte[]
	 */ 
	public byte[] getConfirmStcpBuffer() {
		return stcpControlBuffer;
	}
	
	/** 
	 * @Method: getStcpBuffer 
	 * @Description: ���ش��п�����Ϣ��STCP���� �в�����
	 * @return
	 * byte[]
	 */ 
	public byte[] getStcpBufferByReveiverStcpBuffer(byte[] buffer){
		//������ƿ���Ϣ
		stcpControlBuffer[6] = 0x02;
		stcpControlBuffer[8] = buffer[0];
		stcpControlBuffer[9] = buffer[1];
		stcpControlBuffer[10] = buffer[2];
		stcpControlBuffer[11] = buffer[3];
		stcpControlBuffer[14] = (byte) (stcpSize & 0xFF);
		stcpControlBuffer[15] = (byte) (stcpSize >> 8 & 0xFF);
		
		stcpBuffer = new byte[stcpSize];
		
		//�����ƿ�Ͳ�������װ
		System.arraycopy(stcpControlBuffer, 0, stcpBuffer, 0, stcpControlBuffer.length);
		System.arraycopy(stcpArgsBuffer, 0, stcpBuffer, stcpControlBuffer.length, stcpSize - stcpControlBuffer.length);
		return stcpBuffer;	
	}

	@Override
	public void initBuffer() {
		// TODO Auto-generated method stub
		//���뱨
		applyBuffer[0] = 0x04;
		applyBuffer[1] = isSettingReceiverModel? 0x44 : 0x34;//��ѡ�����ջ��ͺ�ʱ����0x44
		applyBuffer[2] = 0x00;
		applyBuffer[3] = getCheckCode(applyBuffer, 3);
		applyBuffer[5] = Config.UNIT_NUMBER & 0xFF;
		applyBuffer[6] = Config.UNIT_NUMBER >> 8 & 0xFF;
		applyBuffer[9] = isSettingReceiverModel? 0x05 : 0x04;
		applyBuffer[10] = 0x06;
		applyBuffer[11] = 0x00;
		applyBuffer[12] = Config.USE_WAY;
		applyBuffer[13] = 0x07;
		applyBuffer[14] = 0x00;		
		applyBuffer[30] = 0x08;
		applyBuffer[31] = 0x00;
		applyBuffer[34] = 0x20;
		applyBuffer[35] = 0x00;
		
		//ip
		byte[] ip = tools.getLocalIP().getBytes();
		System.arraycopy(ip, 0, applyBuffer, 15, ip.length);
		
		//�������
		byte[] applyPwd = Config.APPLAY_PWD.getBytes();
		System.arraycopy(applyPwd, 0, applyBuffer, 36, applyPwd.length);
		
		//stcp ����ģ��
		stcpArgsBuffer[0] = 0x7E;
		stcpArgsBuffer[1] = 0x16;
		stcpArgsBuffer[2] = (byte)0x8F;
	}
	
	/** 
	 * @Method: initApplyBufferByReportBuffer 
	 * @Description: ���ݻ㱨����ʼ�����뱨
	 * @param reportBuffer �㱨��
	 * void
	 */ 
	public void initApplyBufferByReportBuffer(byte[] reportBuffer){
		applyBuffer[4] = reportBuffer[4];
		applyBuffer[7] = reportBuffer[7];
		applyBuffer[8] = reportBuffer[8];
		if (isSettingReceiverModel) {
			applyBuffer[52] = 0x00;
			applyBuffer[53] = 0x00;
			System.arraycopy(reportBuffer, 12, applyBuffer, 54, 14);
		}
	}
	
	/** 
	 * @Method: initStcpBufferByReveiverStcpBuffer 
	 * @Description: ��ʼ��STCP �ο����ջ���������STCP����
	 * @param buffer ���ջ�STCP����
	 * void
	 */ 
	public void initStcpBufferByReveiverStcpBuffer(byte[] buffer){		
		long headSequence = (buffer[0] & 0xFF) + ((buffer[1] & 0xFF) << 8) + ((buffer[2] & 0xFF) << 16) + ((buffer[3] & 0xFF) << 24);
		headSequence = (headSequence + 1) % (1L << 32);
		
		stcpControlBuffer[0] = (byte) (headSequence & 0xFF);
		stcpControlBuffer[1] = (byte) (headSequence >> 8 & 0xFF);
		stcpControlBuffer[2] = (byte) (headSequence >> 16 & 0xFF);
		stcpControlBuffer[3] = (byte) (headSequence >> 24 & 0xFF);
		stcpControlBuffer[4] = getCheckCode(stcpControlBuffer, 4);
		
		stcpControlBuffer[5] = buffer[5];
		stcpControlBuffer[6] = 0x00;
		stcpControlBuffer[7] = 0x01;
		
		stcpControlBuffer[8] = buffer[0];
		stcpControlBuffer[9] = buffer[1];
		stcpControlBuffer[10] = buffer[2];
		stcpControlBuffer[11] = buffer[3];
		
		stcpControlBuffer[12] = 0x00;
		stcpControlBuffer[13] = 0x00;
		stcpControlBuffer[14] = (byte)0x10;
		stcpControlBuffer[15] = 0x00;
		
	}
}
