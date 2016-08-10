package com.manager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import com.client.Workstation;
import com.listener.*;
import com.pojo.WRRelation;
import com.service.ControllerService;
import com.util.Config;
import com.util.Constance;
import com.util.Tools;

/**
 * @author Boris
 *
 * 连接接收机
 * 
 * 2016年8月2日
 */
public class ConnectManager {
	Vector<WRRelation> wrRelations = null; //存储接收机和工作站的对应关系
	
	ConnectListener connectListener = null;
	
	int workStationNum = 0;
	
	public ConnectListener getConnectListener() {
		return connectListener;
	}

	public void setConnectListener(ConnectListener connectListener) {
		this.connectListener = connectListener;
	}

	public Vector<WRRelation> getWrRelations() {
		return wrRelations;
	}
	
	private final Workstation createWorkstation(){
		
		final Workstation workstation = new Workstation(Config.WORKSTATION_UDP_PORT + workStationNum);
		workStationNum++;
		workstation.start();
		
		return workstation;
	}
	
	public void connectReceiver(){
		wrRelations = new Vector<WRRelation>();
		int findReveiverNum = 0;
		try {
		    ServerSocket server = new ServerSocket(Config.CONTROLLER_TCP_PORT);
			Socket receiver = null;  
			boolean f = true;  
			while(f){  
				//等待客户端的连接，如果没有获取连接  
				receiver = server.accept(); 
				System.out.println("搜索到接收机！"); 
				findReveiverNum++;
				
				ControllerService controllerService = new ControllerService(receiver);
				//一个接收机对应一个工作站
				final Workstation workstation = createWorkstation();
				
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
							
							//取连接上的接收机端口和ip
							byte[] ipBuffer = new byte[15];
							byte[] portBuffer = new byte[2];
							System.arraycopy(reportBuffer, 52, ipBuffer, 0, 15);
							System.arraycopy(reportBuffer, 69, portBuffer, 0, 2);
							
							String ip = new String(ipBuffer).trim();
							int port = ((portBuffer[1] & 0xFF) << 8) + (portBuffer[0] & 0xFF);
							
							WRRelation wrRelation = new WRRelation(workstation, ip, port, Constance.Reveiver.FREE);
							wrRelations.add(wrRelation);
							
							if (wrRelations.size() >= Config.RECEIVER_NUM) {
								connectListener.onConnectEnd();
							}
							
							break;

						default:
							break;
						}
					
						
					}
				});
				
				//为控制器开启监听接收机和监听工作站的线程
				new Thread(controllerService, ControllerService.LISTEN_RECEIVER).start();
				new Thread(controllerService, ControllerService.LISTENE_WORKSTATION).start(); 
				
				System.out.println("---" + findReveiverNum);
				//全部的接收机都已连接 退出搜索
				if (findReveiverNum >= Config.RECEIVER_NUM) {
					f = false;
				}
			}  
			server.close();  
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		
	}
}
