package com.manager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.client.Workstation;
import com.listener.ConnectListener;
import com.listener.ControllerListener;
import com.service.ControllerService;
import com.util.Config;
import com.util.Log;
import com.util.Tools;

/**
 * @author Boris
 *
 * 连接接收机
 * 
 * 2016年8月2日
 */
public class ConnectManager implements Runnable{
	ConnectListener connectListener = null;
	Tools tools = Tools.getTools();
	
	int workStationNum = 0;

	public void setConnectListener(ConnectListener connectListener) {
		this.connectListener = connectListener;
	}

	private final Workstation createWorkstation(int workstationPort){
		
		final Workstation workstation = new Workstation(workstationPort);
		workstation.start();
		
		return workstation;
	}
	
	public void connectReceiver(){
		System.out.println("连接接收机...");
		
		int findReveiverNum = 0;
		try {
		    ServerSocket server = new ServerSocket(Config.CONTROLLER_TCP_PORT);
			Socket socket = null;
			boolean f = true;  
			while(f){  
				//等待客户端的连接，如果没有获取连接  
				socket = server.accept(); 
				findReveiverNum++;
				
				ControllerService controllerService = new ControllerService(socket);
				
				//注册控制器的监听事件
				controllerService.setControllerListener(new ControllerListener() {
					//一个接收机对应一个工作站
					Workstation workstation = null;
					
					@Override
					public void onNeedCreatedWorkstation(int workstationPort) {
						// TODO Auto-generated method stub
						workstation = createWorkstation(workstationPort);
						workStationNum++;
					}
					
					@Override
					public void onReceivedReportMsg(byte[] reportBuffer) {
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
							String ip = new String(reportBuffer, 52, 15).trim();;
							int port = ((reportBuffer[70] & 0xFF) << 8) + (reportBuffer[69] & 0xFF);
							
							if (connectListener != null) {
								connectListener.onConnectEnd(workstation, ip, port);
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
				
				//全部的接收机都已连接 退出搜索
				if (findReveiverNum >= tools.getProperty("receivers").split(",").length) {
					f = false;
				}
			}  
			server.close();  
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.out.debug(e);
		}  
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		connectReceiver();
	}
	
}
