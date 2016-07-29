package com.buffer;

import com.util.Config;
import com.util.Tools;

/**
 * @author Boris
 *
 * 2016��7��28��
 */
public class ControllerBuffer extends Buffer {
	private byte[] makeSureBuffer  = new byte[10]; //ȷ�ϱ�
	private byte[] distributionBuffer  = new byte[34]; //���䱨
	
	public byte[] getMakeSureBuffer() {
		return makeSureBuffer;
	}
	
	public byte[] getDistributionBuffer() {
		return distributionBuffer;
	}

	public ControllerBuffer() {
		// TODO Auto-generated constructor stub
		initBuffer();
	}
	
	@Override
	public void initBuffer() {
		// TODO Auto-generated method stub
		//��ʼ��ȷ�ϱ�
		makeSureBuffer[0] = 0x02;
		makeSureBuffer[1] = 0x0A;
		makeSureBuffer[2] = 0x00;
		makeSureBuffer[3] = getCheckCode(makeSureBuffer, 3);
		makeSureBuffer[9] = 0x00;
		
		//��ʼ�����䱨
		distributionBuffer[0] = 0x03;
		distributionBuffer[1] = 0x22;
		distributionBuffer[2] = 0x00;
		distributionBuffer[3] = getCheckCode(distributionBuffer, 3);
		distributionBuffer[9] = 0x03;
		distributionBuffer[10] = 0x06;
		distributionBuffer[11] = 0x00;
		distributionBuffer[12] = Config.USE_WAY;
		distributionBuffer[13] = 0x07;
		distributionBuffer[14] = 0x00;
		distributionBuffer[30] = 0x08;
		distributionBuffer[31] = 0x00;
		
		//���䱨�й���վIP
		byte[] workstationIp = Config.WORKSTATION_IP.getBytes();
		System.arraycopy(workstationIp, 0, distributionBuffer, 15, workstationIp.length);
		
		//���䱨�й���վ��STCP�˿ں�
		distributionBuffer[32] = (byte) (Config.WORKSTATION_UDP_PORT & 0x00FF);
		distributionBuffer[33] = (byte) (Config.WORKSTATION_UDP_PORT >> 8 & 0xFF);			
	}
	
	/** 
	 * @Method: initMakeSureBufferByRegisterBuffer 
	 * @Description: �õǼǱ���ʼ��ȷ�ϱ�
	 * @param registerBuffer �ǼǱ�
	 * void
	 */ 
	public void initMakeSureBufferByRegisterBuffer(byte[] registerBuffer){
		makeSureBuffer[4] = registerBuffer[4];
		makeSureBuffer[5] = registerBuffer[5];
		makeSureBuffer[6] = registerBuffer[6];
		makeSureBuffer[7] = registerBuffer[7];
		makeSureBuffer[8] = registerBuffer[8];
	}
	
	/** 
	 * @Method: initDistributionBufferByApplyBuffer 
	 * @Description: �ο����뱨��ʼ�����䱨
	 * @param applyBuffer ���뱨
	 * void
	 */ 
	public void initDistributionBufferByApplyBuffer(byte[] applyBuffer){
		distributionBuffer[4] = applyBuffer[4];
		distributionBuffer[5] = applyBuffer[5];
		distributionBuffer[6] = applyBuffer[6];
		distributionBuffer[7] = applyBuffer[7];
		distributionBuffer[8] = applyBuffer[8];
	}
}
