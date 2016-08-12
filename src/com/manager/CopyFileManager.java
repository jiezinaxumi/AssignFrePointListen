package com.manager;

import java.util.LinkedList;
import java.util.Queue;

import com.db.operation.CRUD;
import com.pojo.FileInfo;
import com.util.Config;
import com.util.Tools;

/**
 * @author Boris
 * @description �������ļ�������Զ��
 * 2016��8��12��
 */
public class CopyFileManager implements Runnable{
	private static CopyFileManager copyFileManager = null;
	private Tools tools = Tools.getTools();
	private CRUD crud = new CRUD();
	
	private Queue<FileInfo> qFileInfos = new LinkedList<FileInfo>();
	
	
	public Queue<FileInfo> getqFileInfos() {
		return qFileInfos;
	}

	private void cpFile() throws InterruptedException{
		System.out.println("���������߳�");
		while(true){
			System.out.println("����Ƿ����ļ�");
			if (qFileInfos.size() > 0) {
				System.out.println("�����ļ���Զ��");
				FileInfo fileInfo = qFileInfos.poll();

				String srcFileName = fileInfo.getSrcPath();
				String destFileName = fileInfo.getDestPath();
				tools.cpSrcFileToDestFile(srcFileName, destFileName);
				
				System.out.println("�����ļ���Զ�̽���");
				
				String sql = fileInfo.getSql();
				crud.instert(sql);
			}
			Thread.sleep(Config.COPY_FILE_TIME);
		}
	}
	
	public static CopyFileManager getInstance(){
		if (copyFileManager == null) {
			copyFileManager = new CopyFileManager();
		}
		
		return copyFileManager;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			cpFile();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
