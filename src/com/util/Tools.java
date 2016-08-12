package com.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class Tools {
	public static Tools getTools(){
		return new Tools();
	}
	
	// ��ָ��byte������16���Ƶ���ʽ��ӡ������̨
	public void printHexString(final byte[] b) {
		printHexString(b, b.length);
	}
	
    /** 
     * @Method: printHexString 
     * @Description: ��16�������
     * @param b
     * @param length
     * void
     */ 
    public void printHexString(final byte[] b, int length){
    	String msg = getBufferType(b);
    	if (msg.equals("��·̽�ⱨ�� ")) {
			return;
		}
    	
    	for (int i = 0; i < length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			msg += "[" + i + "]" + hex.toUpperCase() +  " ";
		}
		System.out.println(msg + "\n");
	}
    
    /** 
     * @Method: getBufferType 
     * @Description: ȡ�������ͣ�STCP����
     * @param b
     * return ���ر�������
     * String
     */  
    public String getBufferType(final byte[] b){
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
    	return bufferType;
    }
    
    /** 
     * @Method: printSTCP 
     * @Description: ��16���Ƹ�ʽ���SCTP���� 
     * @param buffer
     * void
     */ 
    public void printSTCP(byte[] buffer){
    	String msg = "STCP���ƿ飺 ";
    	for (int i = 0, j = 0; i < buffer.length; i++, j ++) {
    		String hex = Integer.toHexString(buffer[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			if((buffer[i] & 0xFF) == 0x7E){
				msg += "\n�����飺 ";
				j = 0;
			}else if((buffer[i] & 0xFF) == 0x81){
				msg += "\n���ݿ飺 ";
				j = 0;
			}
			
			//�����������ַ�����ʽ��ӡ
			if (j >= 5 && (buffer[i - j] & 0xFF) == 0x7E ) {
				if (j == 5) {
					msg += "������  ";
				}
				if (((buffer[i - j + 1] & 0xFF) == 0x16 && (buffer[i - j + 2] & 0xFF) == 0x0F) || ((buffer[i - j + 1] & 0xFF) == 0x16 && (buffer[i - j + 2] & 0xFF) == 0x8F)) {
					byte[] b = new byte[1];
					b[0] = buffer[i];
					try {
						msg += (b[0] & 0xFF) >= 0x80 ? hex.toUpperCase() : new String(b, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					continue;
				}
			}
			
			msg += "[" + j + "]" + hex.toUpperCase() +  " ";
//			msg += "[" + j + "]" + buffer[i] +  " ";
		}
    	System.out.println(msg + "\n");
    }
    
    /** 
     * @Method: getLocalIP 
     * @Description: ���ر�����ַ
     * @return
     * String
     */ 
    public String getLocalIP(){
		String ip = null;
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		
		return ip;
	}
    
    private FileOutputStream fw = null;
    private String filePath = null;
    /** 
     * @Method: writeToFile 
     * @Description: д���ݵ��ļ�
     * @param path �ļ������·��+�ļ���
     * @param content byte����  ��������
     * @param startPos ������ʼλ�� ����λ�õ��ļ�ĩβ
     * void
     */ 
    public void writeToFile(final String path, byte[] content, int startPos){
		File file = new File(path);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
    	
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
    public void writeToFileEnd(){
    	if (fw != null) {
			try {
				fw.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
//				try {
//					fw.close();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
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
    public void mvSrcFileToDestFile(final String srcFileName, final String destFileName){
    	File path = new File(destFileName);
		if (!path.getParentFile().exists()) {
			path.getParentFile().mkdirs();
		}
		
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
    
    /**
     * @Method: cpSrcFileToDestFile 
     * @Description:�����ļ�
     * @param srcFileName Դ�ļ�
     * @param destFileName Ŀ���ļ�
     * void
     */
    public void cpSrcFileToDestFile(final String srcFileName, final String destFileName){
    	File path = new File(destFileName);
		if (!path.getParentFile().exists()) {
			path.getParentFile().mkdirs();
		}
		
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
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");//�������ڸ�ʽ
    public String getCurrentTime(){
    	return df.format(new Date());
    }
    
    private SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");//�������ڸ�ʽ
    public String getCurrentDay(){
    	return df2.format(new Date());
    }
    
    public long getCurrentSecond(){
    	return new Date().getTime() / 1000;
    }
    
    SimpleDateFormat df3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public String formatDate(String date){
    	try {
			date = df3.format(df.parse(date));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return date;
    }
    
    //��ȡproperties�ļ�
    
    static Properties pps = new Properties();
    static{
		try {
			String path = Thread.currentThread().getContextClassLoader().getResource("assign_fre_point_listen.properties").getPath();
			pps.load(new FileInputStream(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	public String getProperty(String key) {
		return pps.getProperty(key);
	}
	
}
