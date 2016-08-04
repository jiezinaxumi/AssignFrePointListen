package com.manager;

import com.buffer.WaveHeader;
import com.client.Workstation;
import com.listener.ControllerListener;
import com.listener.WorkstationListener;
import com.service.ControllerService;
import com.util.Tools;

/**
 * @author Boris
 *
 * ����������͹���վ
 * 2016��8��2��
 */
public class CWManager {
	private static CWManager cwManager = null;
	private String frequence;
	private String savePath;
	private String fileName;
	private String fileTemp;

	private int fileTime;
	private int totalTime;
	
	private long changeFileNameBetweenTime; //�ı��ļ�����ʱ���� ������һ�� �򲻸ı��ļ���
	private long beginTime;
	private long currentTime;
	
	private int dataLength;
	
	boolean beginWrite = false; // ��ʼд�ļ�
	
	private void run(){
		//���ù���վ
		final Workstation workstation = new Workstation();
		
		workstation.setFrequennce(frequence); //���ý��ջ�Ƶ��
		
		workstation.setWorkstationListener(new WorkstationListener() {
			boolean isConfirm = false; // �ѷ��ͽ���ȷ��
			boolean isControlReceiver = false; //�ѷ��͵�Ƶָ��
			boolean isSucces = false;  //��Ƶ�Ƿ�ɹ�
			
			@Override
			public void onReveicedSTCP(byte[] stcp, String ip, int port) {
				// TODO Auto-generated method stub
//				Tools.printSTCP(stcp);
				
				//���͵�Ƶ��Ϣ
				if (!isControlReceiver && isConfirm) {
					System.out.println("���ƽ��ջ� ��Ƶ " + frequence);
					workstation.sendSTCPBufferByReceivedBuffer(stcp, ip, port);
					isControlReceiver = true;
				}
				
				//���ͽ���ȷ����Ϣ
				workstation.sendConfirmSTCPBufferByReceivedBuffer(stcp, ip, port);
				isConfirm = true;
				
				//�жϵ�Ƶ�Ƿ�ɹ�
				if (isControlReceiver && !isSucces) {
					byte[] frequenceBety = frequence.getBytes();
					for (int i = 0, j = 0; i < stcp.length; i++) {
						if (stcp[i] == frequenceBety[j]) {
							j++;
							if (j == frequence.length()) {
								System.out.println("����Ƶ�ɹ���\n");
								isSucces = true;
								break;
							}
						}else{
							if (j != 0) {
								j = 0;
								i--;
							}
						}
					}
				}
				
				//�洢��Ƶ���������
				if (isSucces) {
					int i = 15;
					//��������������ʼλ��
					for (; i < stcp.length; i++) {
						if ((stcp[i] & 0xFF) == 0x81) {
							i += 4;
							break;
						}
					}
					//д�ļ�
					writeDataToFile(stcp, i);
				}
			}
		});
		
		
		//���ý��ջ�
		ControllerService.setControllerListener(new ControllerListener() {
			
		    @Override
			public void onReceivedReportMsg(byte[] reportBuffer) {
				// TODO Auto-generated method stub
				if ((reportBuffer[99] & 0xFF) == 0x00 && (reportBuffer[100] & 0xFF) == 0x00) {
					workstation.sendApplyMessageByReportBuffer(reportBuffer);
					return;
				}
				switch (reportBuffer[76] & 0xFF) {
				case 0x00:
					System.out.println("\n�����ջ����У������ӽ���ʧ�ܡ�\n");
					break;
				case 0x01:
					System.out.println("\n������ �ѷ��䵫��δ���������ӡ�\n");
					break;
				case 0x02:
					System.out.println("\n��ʹ�á�\n");
					break;

				default:
					break;
				}
			}
		});
		
		ControllerService.start();
		workstation.start();
	}
	
	/** 
	 * @Method: writeDataToFile 
	 * @Description: ��������д���ļ�
	 * @param stcp ����
	 * @param startPos ����������ʼλ��
	 * void
	 */ 
	private void writeDataToFile(byte[] stcp, int startPos){
		//��ʼ��ʼʱ����ļ���
		if (!beginWrite) {
			beginWrite = true;
			dataLength = 0;
			changeFileNameBetweenTime = 0;
			beginTime = Tools.getCurrentSecond();
			fileName = savePath + "record_" + frequence + "_" + Tools.getCurrentTime() + ".wav";
			fileTemp = savePath + "record_" + frequence + "_" + Tools.getCurrentTime() + "_temp.wav";
		}
		
		currentTime = Tools.getCurrentSecond();
		long betweenTime = currentTime - beginTime;
		
		//�洢ʱ�䵽����ʱ�� �˳�
		if (betweenTime >= totalTime) {
			writeWaveHeadToFile();
			Tools.writeToFileEnd();
			Tools.mvSrcFileToDestFile(fileTemp, fileName);

			System.out.println("����ȡ��Ƶ������\n");
			System.exit(0);
		}
		
		//�洢ʱ�䵽�ﵥ���ļ�ʱ�䣬�� �޸��ļ����Ա����һ���ļ���Ȼ�� д��waveͷ
		if (betweenTime != changeFileNameBetweenTime && betweenTime % fileTime == 0) {
			changeFileNameBetweenTime = betweenTime;
			
			writeWaveHeadToFile();
			Tools.mvSrcFileToDestFile(fileTemp, fileName);
			
			fileName = savePath + "record_" + frequence + "_" + Tools.getCurrentTime() + ".wav";
			fileTemp = savePath + "record_" + frequence + "_" + Tools.getCurrentTime() + "_temp.wav";
			dataLength = 0;
		}
		
		dataLength += stcp.length - startPos;
		Tools.writeToFile(fileTemp, stcp, startPos);
	}
	
	/** 
	 * @Method: writeWaveHeadToFile 
	 * @Description: дwaveͷ
	 * void
	 */ 
	private void writeWaveHeadToFile(){
		int sample = Integer.parseInt(Tools.getProperty("wave.samples_per_sec")) ;
		byte[] head = WaveHeader.getHeader(dataLength, sample);
	
		Tools.writeToFile(fileName, head, 0);
	}
	
	/////////////////////////// �ⲿ�ӿ� ///////////////////////////////////
	
	public static CWManager getInstance(){
		if (cwManager == null) {
			cwManager = new CWManager();
		}
		
		return cwManager;
	}
	
	/** 
	 * @Method: contorlAndGetData 
	 * @Description: ��Ƶ��ȡ����
	 * @param frequence  Ƶ��  �� ȡֵ��Χ00 000 000~29 999 999��
	 * @param savePath �ļ��洢��ַ
	 * @param time ���ļ�ʱ��(��λ ��)
	 * @param totalTime ��ʱ��(��λ ��)
	 * void
	 * @throws Exception 
	 */ 
	public void contorlAndGetData(final String frequence, final String savePath, int fileTime, int totalTime) throws Exception{
		if (fileTime > totalTime) {
			throw new Exception("��ʱ������ڵ��ļ�ʱ��");
		}
		this.frequence = frequence;
		this.savePath = savePath;
		this.fileTime = fileTime;
		this.totalTime = totalTime;
		
		
		run();
	}

}
