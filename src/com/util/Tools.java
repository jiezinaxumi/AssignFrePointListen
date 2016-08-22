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
	
	// 将指定byte数组以16进制的形式打印到控制台
	public void printHexString(final byte[] b) {
		printHexString(b, b.length);
	}
	
    /** 
     * @Method: printHexString 
     * @Description: 以16进制输出
     * @param b
     * @param length
     * void
     */ 
    public void printHexString(final byte[] b, int length){
    	String msg = getBufferType(b);
    	if (msg.equals("线路探测报： ")) {
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
     * @Description: 取报文类型，STCP除外
     * @param b
     * return 返回报文类型
     * String
     */  
    public String getBufferType(final byte[] b){
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
    	return bufferType;
    }
    
    /** 
     * @Method: printSTCP 
     * @Description: 以16进制格式输出SCTP报文 
     * @param buffer
     * void
     */ 
    public void printSTCP(byte[] buffer){
    	String msg = "STCP控制块： ";
    	for (int i = 0, j = 0; i < buffer.length; i++, j ++) {
    		String hex = Integer.toHexString(buffer[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			if((buffer[i] & 0xFF) == 0x7E){
				msg += "\n参数块： ";
				j = 0;
			}else if((buffer[i] & 0xFF) == 0x81){
				msg += "\n数据块： ";
				j = 0;
			}
			
			//将参数区以字符的形式打印
			if (j >= 5 && (buffer[i - j] & 0xFF) == 0x7E ) {
				if (j == 5) {
					msg += "参数区  ";
				}
				if (((buffer[i - j + 1] & 0xFF) == 0x16 && (buffer[i - j + 2] & 0xFF) == 0x0F) || ((buffer[i - j + 1] & 0xFF) == 0x16 && (buffer[i - j + 2] & 0xFF) == 0x8F)) {
					byte[] b = new byte[1];
					b[0] = buffer[i];
					try {
						msg += (b[0] & 0xFF) >= 0x80 ? hex.toUpperCase() : new String(b, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						Log.out.debug(e);
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
     * @Description: 返回本机地址
     * @return
     * String
     */ 
    public String getLocalIP(){
		String ip = null;
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			Log.out.debug(e);
		}  
		
		return ip;
	}
    
    private FileOutputStream out = null;
    private String filePath = null;
    /** 
     * @Method: writeToFile 
     * @Description: 写数据到文件
     * @param path 文件的输出路径+文件名
     * @param content byte数组  数据内容
     * @param startPos 数据起始位子 结束位置到文件末尾
     * void
     */ 
    public void writeToFile(final String path, byte[] content, int startPos){
    	//是新文件 如果out不为空 说明已写过文件 则flush到文件然后close， 然后重新new一个
    	if (!path.equals(filePath)) {
    		//判断目录是否存在 不存在创建
    		File file = new File(path);
    		if (!file.getParentFile().exists()) {
    			file.getParentFile().mkdirs();
    		}
    		
    		if (out != null) {
    			try {
					out.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.out.debug(e);
				}finally{
					try {
						out.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Log.out.debug(e);
					}
				}
				
			}
    		try {
				out = new FileOutputStream(path, true);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				Log.out.debug(e);
			}
    		filePath = path;
		}
    
    
    	try {
    		if (!out.getFD().valid()) {
    			out = new FileOutputStream(path, true);
			}
			out.write(content, startPos, content.length - startPos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.out.debug(e);
		}
    }
       
    /** 
     * @Method: writeToFileEnd 
     * @Description: 写数据结束时调用 
     * void
     */ 
    public void writeToFileEnd(){
    	if (out != null) {
			try {
				out.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.out.debug(e);
			}finally{
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.out.debug(e);
				}
			}
		}
    }
    
    /** 
     * @Method: mvSrcFileToDestFile 
     * @Description: 移动文件
     * @param srcFileName 源文件（绝对路径加文件名）
     * @param destFileName 目标文件（绝对路径加文件名）
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
			byte[] buffer = new byte[1024];
			int byteRead;
			while((byteRead = is.read(buffer)) != -1){
				os.write(buffer, 0, byteRead);
			}

			os.flush();
			os.close();
			is.close();
			file.delete();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Log.out.debug(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.out.debug(e);
		}
    }
    
    /**
     * @Method: cpSrcFileToDestFile 
     * @Description:拷贝文件
     * @param srcFileName 源文件
     * @param destFileName 目标文件
     * void
     */
    public boolean cpSrcFileToDestFile(final String srcFileName, final String destFileName){
    	File path = new File(destFileName);
		if (!path.getParentFile().exists()) {
			path.getParentFile().mkdirs();
		}
		
    	File file = new File(srcFileName);
    	if (file.length() < 102400) { //文件小于100k
			return false; //不拷贝
		}
    	try {
			FileInputStream is = new FileInputStream(file);
			FileOutputStream os = new FileOutputStream(destFileName, true);
			
			byte[] buffer = new byte[1024];
			int byteRead;
			while((byteRead = is.read(buffer)) != -1){
				os.write(buffer, 0, byteRead);
			}
			
			os.flush();
			os.close();
			is.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Log.out.debug(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.out.debug(e);
		}
    	
    	return true;
    }
    
    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");//设置日期格式
    public String getCurrentTime(){
    	return df.format(new Date());
    }
    
    private SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
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
			Log.out.debug(e);
		}
    	
    	return date;
    }
    
    //读取properties文件
    
    static Properties pps = new Properties();
    static{
		try {
			String path = Thread.currentThread().getContextClassLoader().getResource("afpl.properties").getPath();
			pps.load(new FileInputStream(path));
		} catch (IOException e) {
			Log.out.debug(e);
		}
    }
	public String getProperty(String key) {
		return pps.getProperty(key);
	}
	
}
