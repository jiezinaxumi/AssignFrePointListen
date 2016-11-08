
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
	private Tools tools;
	private String localSavePath;
	
	public RunAFPL(){
		connectManager = new ConnectManager();
		tools = Tools.getTools();
		localSavePath = tools.getProperty("local_save_path");
	}
	
	public void connectReceiver(){
		connectManager.setConnectListener(new ConnectListener() {
			
			@Override
			public void onConnectEnd(Workstation workstation, String receiverIp, int receiverPort) {
				// TODO Auto-generated method stub
				Log.out.debug("���ӵ����ջ� ip= " + receiverIp + " port " + receiverPort);
				
				updateReceiverStatus(receiverIp, receiverPort, Constance.Reveiver.FREE);
				
				
				//��ʼ����
				try {
					FileManager fileManager = new FileManager();
					searchTask(workstation, fileManager, receiverIp, receiverPort);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					Log.out.debug(e);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					Log.out.debug(e);
				}
			}
		});
		new Thread(connectManager).start(); //�����߳�  ���ӽ��ջ�
	}
	
	public void searchTask(Workstation workstation, FileManager fileManager, String receiverIp, int receiverPort) throws SQLException, InterruptedException{
		Log.out.debug("��ѯ����...");
		CRUD crud = new CRUD();
		int receiverStatus = Constance.Reveiver.FREE;
		int currentTaskPri = Constance.FreqPri.NORMAL;
		
		while(true){
			//��ѯ���ջ�״̬
			String recveiverSql = "select r.status from tab_mam_receiver r where r.ip = '"+ receiverIp + "' and r.port = " + receiverPort;
			ResultSet receiverRS = crud.find(recveiverSql);
			
			while (receiverRS.next()){
				receiverStatus = receiverRS.getInt("status");
				break;
			}
			receiverRS.close();
			crud.close();
			
			//�����ȼ���ѯ���� ���ȼ��ߵ���������ǰ��
			String taskSql = "select g.grap_id,g.task_id,f.freq_name,r.ip,r.port,to_char(g.start_time,'yyyy-mm-dd hh24:mi:ss') start_time,g.length,g.priorty,g.freq_id," +
						 "(select inner_url from tab_app_storage  where sto_id=2) path from tab_grap_task g " +
						 "left join tab_task t on g.task_id=t.task_id " +
						 "left join tab_mam_freq f on f.freq_id=g.freq_id " +
						 "left join tab_mam_receiver r on r.receiver_id=g.receiver_id " +
						 "where g.status=70 and g.start_time<=sysdate and r.ip='"+ receiverIp + "' and r.port=" + receiverPort + " " +
						 "order by g.priorty ASC";
			ResultSet taskRS = crud.find(taskSql);
			
//			System.out.println(taskSql);
			
//			printSearchContent(taskRS, "������Ϣ");
		
			while (taskRS.next()) {
				int grapId = taskRS.getInt("grap_id");
				int freqId = taskRS.getInt("freq_id");
				int taskId = taskRS.getInt("task_id");
				int fileTotalTime = taskRS.getInt("length") * 60;//ȡ�÷���
				String frequence = String.format("%08d", Integer.parseInt(taskRS.getString("freq_name")) * 1000);//ȡ�õ���kHZ ��1000���HZ ����8 ������0
				String savePath = taskRS.getString("path");
				
				if (receiverStatus == Constance.Reveiver.BUSY) {
					if (taskRS.getInt("priorty") == Constance.FreqPri.URGENCY && currentTaskPri != Constance.FreqPri.URGENCY) {//�Ӽ����� ���������Ĳ��ǼӼ����� ��ֹͣ��ǰ���� ���Ӽ�����
						currentTaskPri = Constance.FreqPri.URGENCY;
						fileManager.stopCurrentWrite();
						doTask(workstation, fileManager, receiverIp, receiverPort, frequence, fileTotalTime, savePath, freqId, taskId, grapId);
					}
				}else if(receiverStatus == Constance.Reveiver.FREE){
					currentTaskPri = taskRS.getInt("priorty");
					doTask(workstation, fileManager, receiverIp, receiverPort, frequence, fileTotalTime, savePath, freqId, taskId, grapId);
				}
				break;
			}
			taskRS.close();
			crud.close();
//			break;
			
			Thread.sleep(Config.SEARCH_TASK_TIME);
		}
	}
	
	public void doTask(Workstation workstation, final FileManager fileManager, final String receiverIp, final int receiverPort, final String frequence, int fileTotalTime, final String savePath, final int freqId, final int taskId, final int grapId){
//		System.out.println("��--------ִ�� " + grapId + " ����----------��");
		Log.out.debug("��--------ִ�� " + grapId + " ����----------��");
		//�������ݿ���ջ�״̬
		updateReceiverStatus(receiverIp, receiverPort, Constance.Reveiver.BUSY);
		updateGrapTaskStatus(grapId, Constance.Task.DOING);
		
		workstation.regulatingRevevierFrequency(frequence, receiverIp, receiverPort);
		//�����ļ�������
		System.out.println("localSavePath " + localSavePath + "\nfileTime " + Config.FILE_TIME + "\ntotalTime " + fileTotalTime);
//		fileManager.setFileMsg(frequence, localSavePath, Config.FILE_TIME, fileTotalTime); //���ļ���ֳɶ��1���ӵ�С�ļ�
		fileManager.setFileMsg(frequence, localSavePath, fileTotalTime, fileTotalTime); //�����һ�����ļ�
		// д�ļ�
		workstation.setWorkstationListener(new WorkstationListener() {
			
			@Override
			public void onRevivedData(byte[] stcp, int startPos) {
				// TODO Auto-generated method stub
				fileManager.writeDataToFile(stcp, startPos);
			}
		
		});
		
		//д�ļ�����
		fileManager.setFileListener(new FileListener() {
			
			@Override
			public void onWriteFileEnd(String fileName, String path, String startTime,
					String endTime) {
				// TODO Auto-generated method stub
				String startT = tools.formatDate(startTime);
				String endT = tools.formatDate(endTime);
				
				System.out.println("��--------��ȡ������Ƶ����----------");
				System.out.println("fileName" + fileName);
				System.out.println("path " + path);
				System.out.println("startTime " + startT);
				System.out.println("endTime " + endT);
				System.out.println("fredid " + freqId);
				System.out.println("taskId " + taskId);
				System.out.println("------------------��");
				
				//�����ļ���  id û������
				 String sql = "insert into tab_file (file_id,file_name, start_time,end_time ,freq_id, sto_id, sto_path, score_status, task_id, grap_id) " +
				              "values(seq_global.nextval,'"+fileName+"', to_date('"+startT+"','yyyy-mm-dd hh24:mi:ss'),to_date('"+endT+"','yyyy-mm-dd hh24:mi:ss'),"+freqId+",2,'"+path+"',70,"+taskId+","+grapId+")";
				 FileInfo fileInfo = new FileInfo(localSavePath + path, savePath + path, sql);
				 new Thread(new CopyFileManager(fileInfo)).start();
			}

			@Override
			public void onWriteTotalFileEnd() {
				// TODO Auto-generated method stub
				System.out.println("��--------���� " + grapId + " ����----------��");
				updateReceiverStatus(receiverIp, receiverPort, Constance.Reveiver.FREE);
				updateGrapTaskStatus(grapId, Constance.Task.DONE);
			}
		});
		
	}
	
	public void updateReceiverStatus(String receiverIp, int receiverPort, int status){
		CRUD crud = new CRUD();
		String sql = "update tab_mam_receiver r set r.status = " + status + " where r.port = " + receiverPort + " and r.ip = '" + receiverIp + "'";
		crud.update(sql);
	}
	
	public void updateGrapTaskStatus(int grapId, int status){
		CRUD crud = new CRUD();
		System.out.println("����task id " + grapId + " status" + status);
		String sql = "update tab_grap_task set status = " + status + " where grap_id = " + grapId; 
		crud.update(sql);
	}
	
	public void printSearchContent(ResultSet rs, String description){
		//��ȡ����Ϣ
		ResultSetMetaData m;
		int colNum = 0;
		try {
			m = rs.getMetaData();
			colNum = m.getColumnCount();
			
			String content = description + "�� ";
			// ��ʾ�������
			while (rs.next()) {
				for (int i = 1; i <= colNum; i++) {
					content += rs.getString(i) + " | ";
				}
				System.out.println(content);
			}
			
			rs.beforeFirst();
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Log.out.debug(e);
		}
	}
	
	public static void main(String[] args) {
		Log.out.debug("---------------------------");
//		String re = Tools.getTools().getProperty("receivers");
//		System.out.println(re);
		RunAFPL afpl = new RunAFPL();
		afpl.connectReceiver();
	}
}
/*
 *select g.grap_id,g.task_id,f.freq_name,r.ip,r.port,to_char(g.start_time,'yyyy-mm-dd hh24:mi:ss') start_time,g.length,g.priorty,g.freq_id,
(select inner_url from tab_app_storage  where sto_id=2) path from tab_grap_task g
 left join tab_task t on g.task_id=t.task_id
 left join tab_mam_freq f on f.freq_id=g.freq_id
 left join tab_mam_receiver r on r.receiver_id=g.receiver_id
 where g.status=70 and g.start_time<sysdate and r.ip='192.168.10.120' and r.port=4410 order by f.freq_pri DESC;
 
 insert into tab_file (file_id, file_name, start_time,end_time ,freq_id, sto_id, sto_path, score_status, task_id)
values(19, '20160812124030_20160812124032.wav', to_date('2016-08-12 12:40:30','yyyy-mm-dd hh24:mi:ss'),to_date('2016-08-12 12:40:32','yyyy-mm-dd hh24:mi:ss'),
1,2,'06210000\2016-08-12\20160812124030_20160812124032.wav',70,1);


 */