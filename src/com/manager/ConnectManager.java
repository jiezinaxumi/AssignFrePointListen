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
 * ���ӽ��ջ�
 * 
 * 2016��8��2��
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
		System.out.println("���ӽ��ջ�...");
		
		int findReveiverNum = 0;
		try {
		    ServerSocket server = new ServerSocket(Config.CONTROLLER_TCP_PORT);
			Socket socket = null;
			boolean f = true;  
			while(f){  
				//�ȴ��ͻ��˵����ӣ����û�л�ȡ����  
				socket = server.accept(); 
				findReveiverNum++;
				
				ControllerService controllerService = new ControllerService(socket);
				
				//ע��������ļ����¼�
				controllerService.setControllerListener(new ControllerListener() {
					//һ�����ջ���Ӧһ������վ
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

						if ((reportBuffer[99] & 0xFF) == 0x00 && (reportBuffer[100] & 0xFF) == 0x00) { //�յ��㱨�� ֪ͨ����վ�������뱨
							System.out.println("���������뱨��");
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
							
							//ȡ�����ϵĽ��ջ��˿ں�ip
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
				
				//Ϊ�����������������ջ��ͼ�������վ���߳�
				new Thread(controllerService, ControllerService.LISTEN_RECEIVER).start();
				new Thread(controllerService, ControllerService.LISTENE_WORKSTATION).start(); 
				
				//ȫ���Ľ��ջ��������� �˳�����
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
