package com.manager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.buffer.WaveHeader;
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
	private Tools tools = Tools.getTools();
	private String frequence;
	private String savePath;
	private String fileName;
	private String fileTemp;

	private int fileTime;
	private int totalTime;
	
	private long changeFileNameBetweenTime; //改变文件名的时间间隔 如果间隔一样 则不改变文件名
	private long beginTime;
	private long currentTime;
	
	private int dataLength;
	
	boolean beginWrite = false; // 开始写文件
	
	private void r(){
		try {
			ServerSocket server = new ServerSocket(Config.CONTROLLER_TCP_PORT);
			Socket receiver = null;  
			boolean f = true;  
			int n = 1;
			while(f && n <= 3){  
				//等待客户端的连接，如果没有获取连接  
				receiver = server.accept(); 
				ControllerService controllerService = new ControllerService(receiver);
				
				//一个接收机对应一个工作站 所以当搜索到一个接收机是new一个工作站
				final Workstation workstation = new Workstation(Config.WORKSTATION_UDP_PORT + n);
				workstation.setFrequennce(frequence); //设置接收机频率
				//注册工作站的监听事件
				workstation.setWorkstationListener(new WorkstationListener() {
					boolean isConfirm = false; // 已发送接受确认
					boolean isControlReceiver = false; //已发送调频指令
					boolean isSucces = false;  //调频是否成功
					
					@Override
					public void onReveicedSTCP(byte[] stcp, String ip, int port) {
						// TODO Auto-generated method stub
						tools.printSTCP(stcp);
						
						//发送调频信息
						if (!isControlReceiver && isConfirm) {
							System.out.println("接受机端口号" + port);
							System.out.println("控制接收机 调频 " + frequence);
							workstation.sendSTCPBufferByReceivedBuffer(stcp, ip, port);
							isControlReceiver = true;
							return;
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
										System.out.println("《调频成功》\n");
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
							int i = 15;
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
				workstation.start();
				//-----
				
				
				//注册控制器的监听事件
				controllerService.setControllerListener(new ControllerListener() {
	
					@Override
					public void onReceivedReportMsg(byte[] reportBuffer) {
						// TODO Auto-generated method stub

						// TODO Auto-generated method stub
						if ((reportBuffer[99] & 0xFF) == 0x00 && (reportBuffer[100] & 0xFF) == 0x00) { //收到汇报报 通知工作站发送申请报
							System.out.println("《发送申请报》");
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
				
				System.out.println("搜索到接收机！");  
				//为控制器开启监听接收机和监听工作站的线程
				new Thread(controllerService, ControllerService.LISTEN_RECEIVER).start();
				new Thread(controllerService, ControllerService.LISTENE_WORKSTATION).start();            
				
				n++;
//				f = false;
			}  
			server.close();  
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		
	}
	
	
	
	
	
//	private void run(){
//		
//		
//		
//		//配置接收机
//		ControllerService.getInstance().setControllerListener(new ControllerListener() {
//		    @Override
//			public void onReceivedReportMsg(byte[] reportBuffer) {
//				// TODO Auto-generated method stub
//				if ((reportBuffer[99] & 0xFF) == 0x00 && (reportBuffer[100] & 0xFF) == 0x00) {
//					System.out.println("《接收机发送申请报》");
//					
//					//-----
//					//配置工作站
//					final Workstation workstation = new Workstation();
//					
//					System.out.println(workstation);
//					
//					workstation.setFrequennce(frequence); //设置接收机频率
//					
//					workstation.setWorkstationListener(new WorkstationListener() {
//						boolean isConfirm = false; // 已发送接受确认
//						boolean isControlReceiver = false; //已发送调频指令
//						boolean isSucces = false;  //调频是否成功
//						
//						@Override
//						public void onReveicedSTCP(byte[] stcp, String ip, int port) {
//							// TODO Auto-generated method stub
////							Tools.printSTCP(stcp);
//							
//							//发送调频信息
//							if (!isControlReceiver && isConfirm) {
//								System.out.println(workstation + "控制接收机 调频 " + frequence);
//								workstation.sendSTCPBufferByReceivedBuffer(stcp, ip, port);
//								isControlReceiver = true;
//							}
//							
//							//发送接受确认信息
//							workstation.sendConfirmSTCPBufferByReceivedBuffer(stcp, ip, port);
//							isConfirm = true;
//							
//							//判断调频是否成功
//							if (isControlReceiver && !isSucces) {
//								byte[] frequenceBety = frequence.getBytes();
//								for (int i = 0, j = 0; i < stcp.length; i++) {
//									if (stcp[i] == frequenceBety[j]) {
//										j++;
//										if (j == frequence.length()) {
//											System.out.println("《调频成功》\n");
//											isSucces = true;
//											break;
//										}
//									}else{
//										if (j != 0) {
//											j = 0;
//											i--;
//										}
//									}
//								}
//							}
//							
//							//存储调频后的数据区
//							if (isSucces) {
//								int i = 15;
//								//查找数据区的起始位置
//								for (; i < stcp.length; i++) {
//									if ((stcp[i] & 0xFF) == 0x81) {
//										i += 4;
//										break;
//									}
//								}
//								//写文件
//								writeDataToFile(stcp, i);
//							}
//						}
//					});
//					workstation.start();
//					//-----
//					
//					workstation.sendApplyMessageByReportBuffer(reportBuffer);
//					return;
//				}
//				switch (reportBuffer[76] & 0xFF) {
//				case 0x00:
//					System.out.println("\n《接收机空闲，虚连接建立失败》\n");
//					break;
//				case 0x01:
//					System.out.println("\n《锁闭 已分配但还未建立虚连接》\n");
//					break;
//				case 0x02:
//					System.out.println("\n《使用》\n");
//					break;
//
//				default:
//					break;
//				}
//			}
//		});
//		
//		ControllerService.getInstance().start();
//	}
	
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
			dataLength = 0;
			changeFileNameBetweenTime = 0;
			beginTime = tools.getCurrentSecond();
			fileName = savePath + "record_" + frequence + "_" + tools.getCurrentTime() + ".wav";
			fileTemp = savePath + "record_" + frequence + "_" + tools.getCurrentTime() + "_temp.wav";
		}
		
		currentTime = tools.getCurrentSecond();
		long betweenTime = currentTime - beginTime;
		
		//存储时间到达总时间 退出
		if (betweenTime >= totalTime) {
			writeWaveHeadToFile();
			tools.writeToFileEnd();
			tools.mvSrcFileToDestFile(fileTemp, fileName);

			System.out.println("《截取音频结束》\n");
			System.exit(0);
		}
		
		//存储时间到达单个文件时间，则 修改文件名以便存另一个文件，然后 写入wave头
		if (betweenTime != changeFileNameBetweenTime && betweenTime % fileTime == 0) {
			changeFileNameBetweenTime = betweenTime;
			
			writeWaveHeadToFile();
			tools.mvSrcFileToDestFile(fileTemp, fileName);
			
			fileName = savePath + "record_" + frequence + "_" + tools.getCurrentTime() + ".wav";
			fileTemp = savePath + "record_" + frequence + "_" + tools.getCurrentTime() + "_temp.wav";
			dataLength = 0;
		}
		
		dataLength += stcp.length - startPos;
		tools.writeToFile(fileTemp, stcp, startPos);
	}
	
	/** 
	 * @Method: writeWaveHeadToFile 
	 * @Description: 写wave头
	 * void
	 */ 
	private void writeWaveHeadToFile(){
		int sample = Integer.parseInt(tools.getProperty("wave.samples_per_sec")) ;
		byte[] head = WaveHeader.getHeader(dataLength, sample);
	
		tools.writeToFile(fileName, head, 0);
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
	 * @throws Exception 
	 */ 
	public void contorlAndGetData(final String frequence, final String savePath, int fileTime, int totalTime) throws Exception{
		if (fileTime > totalTime) {
			throw new Exception("总时常需大于单文件时常");
		}
		this.frequence = frequence;
		this.savePath = savePath;
		this.fileTime = fileTime;
		this.totalTime = totalTime;
		
		
//		run();
		r();
	}

}
