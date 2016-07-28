package com.util;

public class Tools {
	// 将指定byte数组以16进制的形式打印到控制台
	public static void printHexString(byte[] b, int length) {
		for (int i = 0; i < length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			System.out.print("[" + i + "]" + hex.toUpperCase() +  " ");
		}
		System.out.println("\n");
	}

}
