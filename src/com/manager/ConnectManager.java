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
 * ���ӽ��ջ�
 * 
 * 2016��8��2��
 */
public class ConnectManager {
	Vector<WRRelation> wrRelations = null; //�洢���ջ��͹���վ�Ķ�Ӧ��ϵ
	
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
				//�ȴ��ͻ��˵����ӣ����û�л�ȡ����  
				receiver = server.accept(); 
				System.out.println("���������ջ���"); 
				findReveiverNum++;
				
				ControllerService controllerService = new ControllerService(receiver);
				//һ�����ջ���Ӧһ������վ
				final Workstation workstation = createWorkstation();
				
				//ע��������ļ����¼�
				controllerService.setControllerListener(new ControllerListener() {
	
					@Override
					public void onReceivedReportMsg(byte[] reportBuffer) {
						// TODO Auto-generated method stub

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
				
				//Ϊ�����������������ջ��ͼ�������վ���߳�
				new Thread(controllerService, ControllerService.LISTEN_RECEIVER).start();
				new Thread(controllerService, ControllerService.LISTENE_WORKSTATION).start(); 
				
				System.out.println("---" + findReveiverNum);
				//ȫ���Ľ��ջ��������� �˳�����
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
