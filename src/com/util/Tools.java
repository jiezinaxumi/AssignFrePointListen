package com.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

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
			
//			System.out.print("[" + j + "]" + hex.toUpperCase() +  " ");
			System.out.print("[" + j + "]" + buffer[i] +  " ");
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
    
    static FileOutputStream fw = null;
    static String filePath = null;
    /** 
     * @Method: writeToFile 
     * @Description: д���ݵ��ļ�
     * @param path �ļ������·��+�ļ���
     * @param content byte����  ��������
     * @param startPos ������ʼλ�� ����λ�õ��ļ�ĩβ
     * void
     */ 
    public static void writeToFile(final String path, byte[] content, int startPos){
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
				fw = new FileOutputStream(path, true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    
    
    	try {
			fw.write(content, startPos, content.length - startPos);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }
       
    /** 
     * @Method: writeToFileEnd 
     * @Description: д���ݽ���ʱ���� 
     * void
     */ 
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
    
    /** 
     * @Method: mvSrcFileToDestFile 
     * @Description: �ƶ��ļ�
     * @param srcFileName Դ�ļ�������·�����ļ�����
     * @param destFileName Ŀ���ļ�������·�����ļ�����
     * void
     */ 
    public static void mvSrcFileToDestFile(final String srcFileName, final String destFileName){
    	File file = new File(srcFileName);
    	try {
			FileInputStream is = new FileInputStream(file);
			FileOutputStream os = new FileOutputStream(destFileName, true);
			try {
				int data;
				while((data = is.read()) != -1){
					os.write(data);
				}
				os.flush();
				os.close();
				is.close();
				file.delete();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    static SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");//�������ڸ�ʽ
    public static String getCurrentTime(){
    	return df.format(new Date());
    }
    
    public static long getCurrentSecond(){
    	return new Date().getTime() / 1000;
    }
    
    //��ȡproperties�ļ�
	public static String getProperty(String key) {
		String value = null;
		Properties pps = new Properties();
		try {
			String path = Thread.currentThread().getContextClassLoader().getResource("assign_fre_point_listen.properties").getPath();
			pps.load(new FileInputStream(path));
			value = pps.getProperty(key);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return value;
	}
    
    public static void main(String[] args) {
		mvSrcFileToDestFile("D:\\haha.wav", "D:\\haha1.wav");
	}
}
