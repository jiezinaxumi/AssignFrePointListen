package com.util;

public class Tools {
	// ��ָ��byte������16���Ƶ���ʽ��ӡ������̨
	public static void printHexString(final byte[] b) {
		printHexString(b, b.length);
	}
	
    public static void printHexString(final byte[] b, int length){
    	printBufferType(b);
    	
    	for (int i = 0; i < length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			System.out.print("[" + i + "]" + hex.toUpperCase() +  " ");
			if (i >= 10 && i % 10 == 0) {
				System.out.println("");
			}
		}
		System.out.println("\n");
	}
    
    public static void printBufferType(final byte[] b){
    	String bufferType = null;
    	switch (b[0] & 0xff) {
		case 0x00:
			bufferType = "�ǼǱ�: ";
			break;
		case 0x01:
			bufferType = "�㱨��: ";
			break;
		case 0x02:
			bufferType = "ȷ�ϱ�: ";
			break;
		case 0x03:
			bufferType = "���䱨: ";
			break;
		case 0x04:
			bufferType = "���뱨�� ";
			break;
		case 0x05:
			bufferType = "��·̽�ⱨ�� ";
			break;
		default:
			bufferType = "STCP�� ";
			break;
		}
    	System.out.print(bufferType);
    }

}
