package com.manager;

import com.buffer.WaveHeader;
import com.listener.FileListener;
import com.util.Tools;

/**
 * @author Boris
 * @description д�ļ�����
 * 2016��8��9��
 */
public class FileManager {
	private FileListener fileListener;
	private Tools tools = Tools.getTools();
	
	private String frequence;
	private String savePath; //�ļ��ľ���·����������Ŀ¼�� ��D��/xxx/xxx/1.wav
	private String filePath; //�ļ���·��  ��������Ŀ¼  �磺xxx/xxx/1.wav
	private String fileName;
	private String fileTemp;
	private String beginWriteFileTime;//����д�ļ���

	private int fileTime;
	private int totalTime;
	
	private long changeFileNameBetweenTime; //�ı��ļ�����ʱ���� ������һ�� �򲻸ı��ļ���
	private long beginTime;//������ʱ����
	
	private int dataLength;
	
	boolean beginWrite = false; // ��ʼд�ļ�
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
	 * @Description: ��������д���ļ�
	 * @param stcp ����
	 * @param startPos ����������ʼλ��
	 * void
	 */ 
	public void writeDataToFile(byte[] stcp, int startPos){
		if(isWriteFileEnd) return;
		
		//��ʼ��ʼʱ����ļ���
		if (!beginWrite) {
			System.out.println("д�ļ�...");
			
			beginWrite = true;
			isWriteWaveHead = false;
			dataLength = 0;
			changeFileNameBetweenTime = 0;
			
			filePath = frequence + "\\" + tools.getCurrentDay() +"\\";
			savePath += filePath;
			
			beginTime = tools.getCurrentSecond();
			beginWriteFileTime = tools.getCurrentTime();//���������ļ�ʱʹ��
			fileTemp = savePath  + tools.getCurrentTime() + "_temp.wav";
		}
		
		long betweenTime = tools.getCurrentSecond() - beginTime;
		
		//�洢ʱ�䵽����ʱ�� �˳�
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
		
		//�洢ʱ�䵽�ﵥ���ļ�ʱ�䣬�� �޸��ļ����Ա����һ���ļ���Ȼ�� д��waveͷ
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
	 * @Description: дwaveͷ
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
 