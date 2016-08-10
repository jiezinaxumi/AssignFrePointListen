
import java.util.Vector;

import com.client.Workstation;
import com.listener.ConnectListener;
import com.listener.FileListener;
import com.listener.WorkstationListener;
import com.manager.ConnectManager;
import com.manager.FileManager;
import com.pojo.WRRelation;


public class Test {
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub  
		
//		CWManager.getInstance().contorlAndGetData(06220000 + "", "D:\\", 60, 60);
		
		final ConnectManager connectManager = new ConnectManager();
		connectManager.connectReceiver();
		
		connectManager.setConnectListener(new ConnectListener() {
			int frequence = 16220000;
			
			@Override
			public void onConnectEnd() {
				// TODO Auto-generated method stub
				Vector<WRRelation> wrRelations = connectManager.getWrRelations();
				
				System.out.println("工作站数量 " + wrRelations.size());
				
				for (WRRelation wrRelation : wrRelations) {
					Workstation workstation =  wrRelation.getWorkstation();
					String ip = wrRelation.getReceiverIP();
					int port = wrRelation.getReceiverPort();
					
					workstation.regulatingRevevierFrequency(frequence+"", ip, port);
					System.out.println("------------");
					
					//配置文件管理类
					final FileManager fileManager = new FileManager();
					fileManager.setFileMsg(frequence+"", "D:\\", 5, 5);
					frequence++;
					
					//注册工作站的监听事件
					workstation.setWorkstationListener(new WorkstationListener() {
						
						@Override
						public void onRevivedData(byte[] stcp, int startPos) {
							// TODO Auto-generated method stub
							fileManager.writeDataToFile(stcp, startPos);
						}
					
					});
					
					fileManager.setFileListener(new FileListener() {
						
						@Override
						public void onWriteFileEnd(String fileName, String path, String startTime,
								String endTime) {
							// TODO Auto-generated method stub
							System.out.println("【--------截取音频结束----------");
							System.out.println("fileName" + fileName);
							System.out.println("path " + path);
							System.out.println("startTime " + startTime);
							System.out.println("endTime " + endTime);
							System.out.println("------------------】");
						}
					});
				}
			}
		});
		
	}
}
/*
 * select g.grap_id,g.task_id,f.freq_name,r.ip,r.port,to_char(g.start_time,'yyyy-mm-dd hh24:mi:ss'),g.length,g.priorty,g.freq_id,
(select inner_url from tab_app_storage  where sto_id=2) path from tab_grap_task g
 left join tab_task t on g.task_id=t.task_id
 left join tab_mam_freq f on f.freq_id=g.freq_id
 left join tab_mam_receiver r on r.receiver_id=g.receiver_id
 where g.status=70 and g.start_time<sysdate and r.ip='192.168.10.112' and r.port=4410;
 */