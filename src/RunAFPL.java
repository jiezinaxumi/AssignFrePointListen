
import java.beans.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.client.Workstation;
import com.db.operation.CRUD;
import com.listener.ConnectListener;
import com.listener.FileListener;
import com.listener.WorkstationListener;
import com.manager.ConnectManager;
import com.manager.CopyFileManager;
import com.manager.FileManager;
import com.pojo.FileInfo;
import com.util.Config;
import com.util.Constance;
import com.util.Log;
import com.util.Tools;


public class RunAFPL{
	private ConnectManager connectManager;
	private CRUD crud;
	private Tools tools;
	private String localSavePath;
	
	public RunAFPL(){
		connectManager = new ConnectManager();
		crud = new CRUD();
		tools = Tools.getTools();
		localSavePath = Config.LOCAL_SAVE_PATH;
	}
	
	public void connectReceiver(){
		connectManager.setConnectListener(new ConnectListener() {
			
			@Override
			public void onConnectEnd(Workstation workstation, String receiverIp, int receiverPort) {
				// TODO Auto-generated method stub
				Log.out.debug("连接到接收机 ip= " + receiverIp + " port " + receiverPort);
				
				updateReceiverStatus(receiverIp, receiverPort, Constance.Reveiver.FREE);
				
				
				//开始任务
				try {
					searchTask(workstation, receiverIp, receiverPort);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		new Thread(connectManager).start(); //开启线程  连接接收机
//		connectManager.connectReceiver();
	}
	
	public void searchTask(Workstation workstation, String receiverIp, int receiverPort) throws SQLException, InterruptedException{
//		CRUD crud = new CRUD();
		int taskPriorty = Constance.FreqPri.NORMAL;
		int receiverStatus = Constance.Reveiver.FREE;
		
		while(true){
			//按优先级查询任务 优先级高的任务排在前面
			String taskSql = "select g.grap_id,g.task_id,f.freq_name,r.ip,r.port,to_char(g.start_time,'yyyy-mm-dd hh24:mi:ss') start_time,g.length,g.priorty,g.freq_id," +
						 "(select inner_url from tab_app_storage  where sto_id=2) path from tab_grap_task g " +
						 "left join tab_task t on g.task_id=t.task_id " +
						 "left join tab_mam_freq f on f.freq_id=g.freq_id " +
						 "left join tab_mam_receiver r on r.receiver_id=g.receiver_id " +
						 "where g.status=70 and g.start_time<=sysdate and r.ip='"+ receiverIp + "' and r.port=" + receiverPort + " " +
						 "order by f.freq_pri";
			System.out.println(taskSql);
			
			ResultSet taskRS = crud.find(taskSql);
			
			printSearchContent(taskRS, "任务信息");
			
			//查询接收机状态
			String recveiverSql = "select r.status from tab_mam_receiver r where r.ip = '"+ receiverIp + "' and r.port = " + receiverPort;
			ResultSet receiverRS = crud.find(recveiverSql);
			while (receiverRS.next()){
				receiverStatus = receiverRS.getInt("status");
				break;
			}
			
			while (taskRS.next()) {
				System.out.println("----");
				int freqId = taskRS.getInt("freq_id");
				int taskId = taskRS.getInt("task_id");
				int fileTotalTime = taskRS.getInt("length");//取得分钟
				String frequence = String.format("%08d", Integer.parseInt(taskRS.getString("freq_name")));//长度8 不够填0
				String savePath = taskRS.getString("path");
				
				System.out.println("receiverStatus " + receiverStatus);
				if (receiverStatus == Constance.Reveiver.BUSY) {
					System.out.println("receiver busy");
					if (taskRS.getInt("priorty") > taskPriorty) {
						taskPriorty = taskRS.getInt("priorty");
						doTask(workstation, receiverIp, receiverPort, frequence, fileTotalTime, savePath, freqId, taskId);
					}
				}else if(receiverStatus == Constance.Reveiver.FREE){
					System.out.println("receiver free");
					taskPriorty = taskRS.getInt("priorty");
					doTask(workstation, receiverIp, receiverPort, frequence, fileTotalTime, savePath, freqId, taskId);
				}
				break;
			}
			
			break;
			
//			Thread.sleep(1000);
		}
	}
	
	public void doTask(Workstation workstation, final String receiverIp, final int receiverPort, final String frequence, int fileTotalTime, final String savePath, final int freqId, final int taskId){
		System.out.println("执行任务");
		//更新数据库接收机状态
		updateReceiverStatus(receiverIp, receiverPort, Constance.Reveiver.BUSY);
		updateTaskStatus(taskId, Constance.Task.DOING);
		
		workstation.regulatingRevevierFrequency(frequence, receiverIp, receiverPort);
		//配置文件管理类
		final FileManager fileManager = new FileManager();
		System.out.println("localSavePath " + localSavePath + "\nfileTime " + Config.FILE_TIME + "\ntotalTime " + fileTotalTime);
		fileManager.setFileMsg(frequence, localSavePath, Config.FILE_TIME, fileTotalTime);
		
		// 写文件
		workstation.setWorkstationListener(new WorkstationListener() {
			
			@Override
			public void onRevivedData(byte[] stcp, int startPos) {
				// TODO Auto-generated method stub
				fileManager.writeDataToFile(stcp, startPos);
			}
		
		});
		
		//写文件结束
		fileManager.setFileListener(new FileListener() {
			
			@Override
			public void onWriteFileEnd(String fileName, String path, String startTime,
					String endTime) {
				// TODO Auto-generated method stub
				String startT = tools.formatDate(startTime);
				String endT = tools.formatDate(endTime);
				
				System.out.println("【--------截取音频结束----------");
				System.out.println("fileName" + fileName);
				System.out.println("path " + path);
				System.out.println("startTime " + startT);
				System.out.println("endTime " + endT);
				System.out.println("fredid " + freqId);
				System.out.println("taskId " + taskId);
				System.out.println("------------------】");
				
				updateReceiverStatus(receiverIp, receiverPort, Constance.Reveiver.FREE);
				updateTaskStatus(taskId, Constance.Task.DONE);
				
				//插入文件表  id 没有自增
				 String sql = "insert into tab_file (file_id,file_name, start_time,end_time ,freq_id, sto_id, sto_path, score_status, task_id) " +
				              "values(seq_global.nextval,'"+fileName+"', to_date('"+startT+"','yyyy-mm-dd hh24:mi:ss'),to_date('"+endT+"','yyyy-mm-dd hh24:mi:ss'),"+freqId+",2,'"+path+"',70,"+taskId+")";
				 FileInfo fileInfo = new FileInfo(localSavePath + path, savePath + path, sql);
				 boolean isSuccess = CopyFileManager.getInstance().getqFileInfos().offer(fileInfo);
				 System.out.println("isSuccess " + isSuccess);
			}
		});
		
	}
	
	public void updateReceiverStatus(String receiverIp, int receiverPort, int status){
		String sql = "update tab_mam_receiver r set r.status = " + status + " where r.port = " + receiverPort + " and r.ip = '" + receiverIp + "'";
		crud.update(sql);
	}
	
	public void updateTaskStatus(int taskId, int status){
		System.out.println("更新task id " + taskId + " status" + status);
		String sql = "update tab_grap_task set status = " + status + " where task_id = " + taskId; 
//		crud.update(sql);
	}
	
	public void printSearchContent(ResultSet rs, String description){
		//获取列信息
		ResultSetMetaData m;
		int colNum = 0;
		try {
			m = rs.getMetaData();
			colNum = m.getColumnCount();
			
			String content = description + "： ";
			// 显示表格内容
			while (rs.next()) {
				for (int i = 1; i <= colNum; i++) {
					content += rs.getString(i) + " | ";
				}
				System.out.println(content);
			}
			
			rs.beforeFirst();
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		RunAFPL afpl = new RunAFPL();
		afpl.connectReceiver();
		
		new Thread(CopyFileManager.getInstance()).start();; //开启把本地文件拷贝到远程文件的线程
		
	}

	
//	public static void main(String[] args) throws Exception {
//		// TODO Auto-generated method stub  
//		
//		
//		
//		Log.out.debug("扫描任务");
//		
//		connectManager.setConnectListener(new ConnectListener() {
//			int frequence = 16220000;
//			
//			@Override
//			public void onConnectEnd() {
//				// TODO Auto-generated method stub
//				Vector<WRRelation> wrRelations = connectManager.getWrRelations();
//				
//				System.out.println("工作站数量 " + wrRelations.size());
//				
//				for (WRRelation wrRelation : wrRelations) {
//					Workstation workstation =  wrRelation.getWorkstation();
//					String ip = wrRelation.getReceiverIP();
//					int port = wrRelation.getReceiverPort();
//					
//					workstation.regulatingRevevierFrequency(frequence+"", ip, port);
//					System.out.println("------------");
//					
//					//配置文件管理类
//					final FileManager fileManager = new FileManager();
//					fileManager.setFileMsg(frequence+"", "D:\\", 5, 5);
//					frequence++;
//					
//					//注册工作站的监听事件
//					workstation.setWorkstationListener(new WorkstationListener() {
//						
//						@Override
//						public void onRevivedData(byte[] stcp, int startPos) {
//							// TODO Auto-generated method stub
//							fileManager.writeDataToFile(stcp, startPos);
//						}
//					
//					});
//					
//					fileManager.setFileListener(new FileListener() {
//						
//						@Override
//						public void onWriteFileEnd(String fileName, String path, String startTime,
//								String endTime) {
//							// TODO Auto-generated method stub
//							System.out.println("【--------截取音频结束----------");
//							System.out.println("fileName" + fileName);
//							System.out.println("path " + path);
//							System.out.println("startTime " + startTime);
//							System.out.println("endTime " + endTime);
//							System.out.println("------------------】");
//						}
//					});
//				}
//			}
//		});
//		
//	}
}
/*
 *select g.grap_id,g.task_id,f.freq_name,r.ip,r.port,to_char(g.start_time,'yyyy-mm-dd hh24:mi:ss') start_time,g.length,g.priorty,g.freq_id,
(select inner_url from tab_app_storage  where sto_id=2) path from tab_grap_task g
 left join tab_task t on g.task_id=t.task_id
 left join tab_mam_freq f on f.freq_id=g.freq_id
 left join tab_mam_receiver r on r.receiver_id=g.receiver_id
 where g.status=70 and g.start_time<sysdate and r.ip='192.168.10.112' and r.port=4410;
 
 insert into tab_file (file_id, file_name, start_time,end_time ,freq_id, sto_id, sto_path, score_status, task_id)
values(19, '20160812124030_20160812124032.wav', to_date('2016-08-12 12:40:30','yyyy-mm-dd hh24:mi:ss'),to_date('2016-08-12 12:40:32','yyyy-mm-dd hh24:mi:ss'),
1,2,'06210000\2016-08-12\20160812124030_20160812124032.wav',70,1);


 */