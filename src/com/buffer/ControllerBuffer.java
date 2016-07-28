package com.buffer;

import com.util.Tools;

public class ControllerBuffer extends Buffer {
	private byte []makeSureBuffer  = new byte[10];
	
	public byte[] getMakeSureBuffer() {
		return makeSureBuffer;
	}
	
	public ControllerBuffer() {
		// TODO Auto-generated constructor stub
		initBuffer();
	}
	
	@Override
	public void initBuffer() {
		// TODO Auto-generated method stub
		makeSureBuffer[0] = 0x02;
		makeSureBuffer[1] = 0x0A;
		makeSureBuffer[2] = 0x00;
		makeSureBuffer[3] = getCheckCode(makeSureBuffer, 3);
		makeSureBuffer[9] = 0x00;
		
	}
	
	public void setPositionCode(byte code){
		makeSureBuffer[4] = code;
	}
	
	public void setUnitCode(byte code1, byte code2){
		makeSureBuffer[5] = code1;
		makeSureBuffer[6] = code2;
	}
	
	public void setAntennaPort(byte code1, byte code2){
		makeSureBuffer[7] = code1;
		makeSureBuffer[8] = code2;
	}
	




	public static void main(String[] args) {
		ControllerBuffer buffer = new ControllerBuffer();
		
		Tools.printHexString(buffer.getMakeSureBuffer(), 4);
	}

}
