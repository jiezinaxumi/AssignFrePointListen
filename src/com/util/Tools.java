package com.util;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Tools {
	// ��ָ��byte������16���Ƶ���ʽ��ӡ������̨
	public static void printHexString(final byte[] b) {
		printHexString(b, b.length);
	}
	
    /** 
     * @Method: printHexString 
     * @Description: ��16�������
     * @param b
     * @param length
     * void
     */ 
    public static void printHexString(final byte[] b, int length){
    	printBufferType(b);
    	
    	for (int i = 0; i < length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			System.out.print("[" + i + "]" + hex.toUpperCase() +  " ");
		}
		System.out.println("\n");
	}
    
    /** 
     * @Method: printBufferType 
     * @Description: ��ӡ�������ͣ�STCP����
     * @param b
     * void
     */  
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
			break;
		}
    	System.out.print(bufferType);
    }
    
    /** 
     * @Method: printSTCP 
     * @Description: ��16���Ƹ�ʽ���SCTP���� 
     * @param buffer
     * void
     */ 
    public static void printSTCP(byte[] buffer){
    	System.out.print("STCP���ƿ飺 ");
    	for (int i = 0, j = 0; i < buffer.length; i++, j ++) {
    		String hex = Integer.toHexString(buffer[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			if((buffer[i] & 0xFF) == 0x7E){
				System.out.print("\n�����飺 ");
				j = 0;
			}else if((buffer[i] & 0xFF) == 0x81){
				System.out.print("\n���ݿ飺 ");
				j = 0;
			}
			
			//�����������ַ�����ʽ��ӡ
			if (j >= 5 && (buffer[i - j] & 0xFF) == 0x7E ) {
				if (j == 5) {
					System.out.print("������ ");
				}
				if (((buffer[i - j + 1] & 0xFF) == 0x16 && (buffer[i - j + 2] & 0xFF) == 0x0F) || ((buffer[i - j + 1] & 0xFF) == 0x16 && (buffer[i - j + 2] & 0xFF) == 0x8F)) {
					byte[] b = new byte[1];
					b[0] = buffer[i];
					try {
						System.out.print((b[0] & 0xFF) >= 0x80 ? hex.toUpperCase() : new String(b, "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					continue;
				}
			}
			
			System.out.print("[" + j + "]" + hex.toUpperCase() +  " ");
		}
    	System.out.println("\n");
    }
    
    /** 
     * @Method: getLocalIP 
     * @Description: ���ر�����ַ
     * @return
     * String
     */ 
    public static String getLocalIP(){
		String ip = null;
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		
		return ip;
	}
    
    static FileWriter fw = null;
    static String filePath = null;
    public static void writeToFile(final String path, String content){
    	//�����ļ� ���fw��Ϊ�� ˵����д���ļ� ��flush���ļ�Ȼ��close�� Ȼ������newһ��
    	if (!path.equals(filePath)) {
    		if (fw != null) {
    			try {
					fw.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					try {
						fw.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
			
    		
    		filePath = path;
		    try {
				fw = new FileWriter(path, true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    
    
    	try {
			fw.write(content);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }
    
    public static void writeToFileEnd(){
    	if (fw != null) {
			try {
				fw.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				try {
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
    }
    
    static SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");//�������ڸ�ʽ
    public static String getCurrentTime(){
    	return df.format(new Date());
    }
    
    public static long getCurrentSecond(){
    	return new Date().getTime() / 1000;
    }
}
