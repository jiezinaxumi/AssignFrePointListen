package com.manager;

import com.buffer.WaveHeader;
import com.listener.FileListener;
import com.util.Tools;

/**
 * @author Boris
 * @description 写文件管理
 * 2016年8月9日
 */
public class FileManager {
	private FileListener fileListener;
	private Tools tools = Tools.getTools();
	
	private String frequence;
	private String savePath; //文件的绝对路径，包含根目录。 如D：/xxx/xxx/1.wav
	private String filePath; //文件的路径  不包含根目录  如：xxx/xxx/1.wav
	private String fileName;
	private String fileTemp;
	private String beginWriteFileTime;//用于写文件名

	private int fileTime;
	private int totalTime;
	
	private long changeFileNameBetweenTime; //改变文件名的时间间隔 如果间隔一样 则不改变文件名
	private long beginTime;//用于算时间间隔
	
	private int dataLength;
	
	boolean beginWrite = false; // 开始写文件
	boolean isWriteFileEnd = false;
	boolean isWriteWaveHead =  false;
	
	public void setFileListener(FileListener fileListener){
		this.fileListener = fileListener;
	}
	
	public void setFileMsg(String frequence, String savePath, int fileTime, int totalTime){
		this.frequence = frequence;
		this.savePath = savePath;
		this.fileTime = fileTime;
		this.totalTime = totalTime;
		
		beginWrite = false;
		isWriteFileEnd = false;
	}
	
	/** 
	 * @Method: writeDataToFile 
	 * @Description: 把数据区写入文件
	 * @param stcp 报文
	 * @param startPos 数据区的起始位子
	 * void
	 */ 
	public void writeDataToFile(byte[] stcp, int startPos){
		if(isWriteFileEnd) return;
		
		//初始开始时间和文件名
		if (!beginWrite) {
			System.out.println("写文件...");
			
			beginWrite = true;
			isWriteWaveHead = false;
			dataLength = 0;
			changeFileNameBetweenTime = 0;
			
			filePath = frequence + "\\" + tools.getCurrentDay() +"\\";
			savePath += filePath;
			
			beginTime = tools.getCurrentSecond();
			beginWriteFileTime = tools.getCurrentTime();//保存最终文件时使用
			fileTemp = savePath  + tools.getCurrentTime() + "_temp.wav";
		}
		
		long betweenTime = tools.getCurrentSecond() - beginTime;
		
		//存储时间到达总时间 退出
		if (betweenTime >= totalTime) {
			writeWaveHeadToFile();
			tools.writeToFileEnd();
			tools.mvSrcFileToDestFile(fileTemp, savePath + fileName);
			
			if (fileListener != null) {
				fileListener.onWriteFileEnd(fileName, filePath + fileName, beginWriteFileTime, tools.getCurrentTime());
				fileListener.onWriteTotalFileEnd();
			}
			
			isWriteFileEnd = true;
			
			return;
		}
		
		//存储时间到达单个文件时间，则 修改文件名以便存另一个文件，然后 写入wave头
		if (betweenTime != changeFileNameBetweenTime && betweenTime % fileTime == 0) {
			changeFileNameBetweenTime = betweenTime;
			
			writeWaveHeadToFile();
			tools.mvSrcFileToDestFile(fileTemp, savePath + fileName);
			
			if (fileListener != null) {
				fileListener.onWriteFileEnd(fileName, filePath + fileName, beginWriteFileTime, tools.getCurrentTime());
			}
			
			beginWriteFileTime = tools.getCurrentTime();
			fileTemp = savePath + tools.getCurrentTime() + "_temp.wav";
			dataLength = 0;
		}
		
		dataLength += stcp.length - startPos;
		tools.writeToFile(fileTemp, stcp, startPos);
		isWriteWaveHead = false;
	}
	
	/** 
	 * @Method: writeWaveHeadToFile 
	 * @Description: 写wave头
	 * void
	 */ 
	private void writeWaveHeadToFile(){
		isWriteWaveHead = true;
		
		fileName = beginWriteFileTime + "_" + tools.getCurrentTime() + ".wav"; 
		int sample = Integer.parseInt(tools.getProperty("wave.samples_per_sec")) ;
		byte[] head = WaveHeader.getHeader(dataLength, sample);
	
		tools.writeToFile(savePath + fileName, head, 0);
	}
	
	public void stopCurrentWrite(){
		if (!isWriteWaveHead) {
			writeWaveHeadToFile();
			tools.writeToFileEnd();
			tools.mvSrcFileToDestFile(fileTemp, savePath + fileName);
			
			if (fileListener != null) {
				fileListener.onWriteFileEnd(fileName, filePath + fileName, beginWriteFileTime, tools.getCurrentTime());
				fileListener.onWriteTotalFileEnd();
			}
		}
	}
}
 