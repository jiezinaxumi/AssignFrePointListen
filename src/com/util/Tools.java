package com.util;

public class Tools {
	// 将指定byte数组以16进制的形式打印到控制台
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
			bufferType = "登记报: ";
			break;
		case 0x01:
			bufferType = "汇报报: ";
			break;
		case 0x02:
			bufferType = "确认报: ";
			break;
		case 0x03:
			bufferType = "分配报: ";
			break;
		case 0x04:
			bufferType = "申请报： ";
			break;
		case 0x05:
			bufferType = "线路探测报： ";
			break;
		default:
			bufferType = "STCP： ";
			break;
		}
    	System.out.print(bufferType);
    }

}
