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
	// 将指定byte数组以16进制的形式打印到控制台
	public static void printHexString(final byte[] b) {
		printHexString(b, b.length);
	}
	
    /** 
     * @Method: printHexString 
     * @Description: 以16进制输出
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
     * @Description: 打印报文类型，STCP除外
     * @param b
     * void
     */  
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
			break;
		}
    	System.out.print(bufferType);
    }
    
    /** 
     * @Method: printSTCP 
     * @Description: 以16进制格式输出SCTP报文 
     * @param buffer
     * void
     */ 
    public static void printSTCP(byte[] buffer){
    	System.out.print("STCP控制块： ");
    	for (int i = 0, j = 0; i < buffer.length; i++, j ++) {
    		String hex = Integer.toHexString(buffer[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			if((buffer[i] & 0xFF) == 0x7E){
				System.out.print("\n参数块： ");
				j = 0;
			}else if((buffer[i] & 0xFF) == 0x81){
				System.out.print("\n数据块： ");
				j = 0;
			}
			
			//将参数区以字符的形式打印
			if (j >= 5 && (buffer[i - j] & 0xFF) == 0x7E ) {
				if (j == 5) {
					System.out.print("参数区 ");
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
     * @Description: 返回本机地址
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
    	//是新文件 如果fw不为空 说明已写过文件 则flush到文件然后close， 然后重新new一个
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
    
    static SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");//设置日期格式
    public static String getCurrentTime(){
    	return df.format(new Date());
    }
    
    public static long getCurrentSecond(){
    	return new Date().getTime() / 1000;
    }
}
