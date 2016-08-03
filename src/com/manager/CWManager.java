package com.manager;

import java.io.FileWriter;
import java.util.Date;

import com.client.Workstation;
import com.listener.ControllerListener;
import com.listener.WorkstationListener;
import com.service.ControllerService;
import com.util.Config;
import com.util.Tools;

/**
 * @author Boris
 *
 * 管理控制器和工作站
 * 2016年8月2日
 */
public class CWManager {
	private static CWManager cwManager = null;
	private FileWriter fileWriter;
	
	private String frequence;
	private String savePath;
	private String fileName;
	private int fileTime;
	private int totalTime;
	
	private long beginTime;
	private long currentTime;
	
	boolean beginWrite = false; // 开始写文件
		
	private void run(){
		//配置工作站
		final Workstation workstation = new Workstation();
		
		workstation.setFrequennce(frequence); //设置接收机频率
		
		workstation.setWorkstationListener(new WorkstationListener() {
			boolean isConfirm = false; // 已发送接受确认
			boolean isControlReceiver = false; //已发送调频指令
			boolean isSucces = false;  //调频是否成功
			
			@Override
			public void onReveicedSTCP(byte[] stcp, String ip, int port) {
				// TODO Auto-generated method stub
				Tools.printSTCP(stcp);
				
				//发送调频信息
				if (!isControlReceiver && isConfirm) {
					System.out.println("控制接收机 调频 " + Config.RF);
					workstation.sendSTCPBufferByReceivedBuffer(stcp, ip, port);
					isControlReceiver = true;
				}
				
				//发送接受确认信息
				workstation.sendConfirmSTCPBufferByReceivedBuffer(stcp, ip, port);
				isConfirm = true;
				
				//判断调频是否成功
				if (isControlReceiver && !isSucces) {
					byte[] frequenceBety = frequence.getBytes();
					for (int i = 0, j = 0; i < stcp.length; i++) {
						if (stcp[i] == frequenceBety[j]) {
							j++;
							if (j == frequence.length()) {
								System.out.println("调频成功");
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
				
				//存储调频后的数据区
				if (isSucces) {
					int i = 0;
					//查找数据区的起始位置
					for (; i < stcp.length; i++) {
						if ((stcp[i] & 0xFF) == 0x81) {
							i += 4;
							break;
						}
					}
					//写文件
					writeDataToFile(stcp, i);
				}
			}
		});
		
		
		//配置接收机
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
					System.out.println("\n《接收机空闲，虚连接建立失败》\n");
					break;
				case 0x01:
					System.out.println("\n《锁闭 已分配但还未建立虚连接》\n");
					break;
				case 0x02:
					System.out.println("\n《使用》\n");
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
	 * @Description: 把数据区写入文件
	 * @param stcp 报文
	 * @param startPos 数据区的起始位子
	 * void
	 */ 
	private void writeDataToFile(byte[] stcp, int startPos){
		//初始开始时间和文件名
		if (!beginWrite) {
			beginWrite = true;
			beginTime = Tools.getCurrentSecond();
			fileName = savePath + "record_" + frequence + "_" + Tools.getCurrentTime() + ".wav";
		}
		
		currentTime = Tools.getCurrentSecond();
		long betweenTime = currentTime - beginTime;
		
		if (betweenTime % fileTime == 0) {
			fileName = savePath + "record_" + frequence + "_" + Tools.getCurrentTime() + ".wav";
		}
		
		if (betweenTime >= totalTime) {
			Tools.writeToFileEnd();
			
			System.out.println("截取音频结束");
			System.exit(0);
		}
		
		for (int i = startPos; i < stcp.length; i++) {
			Tools.writeToFile(fileName, Integer.toHexString(stcp[i] & 0xFF));
		}
	}
	
	/////////////////////////// 外部接口 ///////////////////////////////////
	
	public static CWManager getInstance(){
		if (cwManager == null) {
			cwManager = new CWManager();
		}
		
		return cwManager;
	}
	
	/** 
	 * @Method: contorlAndGetData 
	 * @Description: 调频截取数据
	 * @param frequence  频率  （ 取值范围00 000 000~29 999 999）
	 * @param savePath 文件存储地址
	 * @param time 单文件时长(单位 秒)
	 * @param totalTime 总时长(单位 秒)
	 * void
	 */ 
	public void contorlAndGetData(final String frequence, final String savePath, int fileTime, int totalTime){
		this.frequence = frequence;
		this.savePath = savePath;
		this.fileTime = fileTime;
		this.totalTime = totalTime;
		
		
		run();
	}

}
