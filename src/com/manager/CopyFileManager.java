package com.manager;

import java.util.LinkedList;
import java.util.Queue;

import com.db.operation.CRUD;
import com.pojo.FileInfo;
import com.util.Config;
import com.util.Tools;

/**
 * @author Boris
 * @description 将本地文件拷贝到远程
 * 2016年8月12日
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
		System.out.println("开启拷贝线程");
		while(true){
			System.out.println("检擦是否有文件");
			if (qFileInfos.size() > 0) {
				System.out.println("拷贝文件到远程");
				FileInfo fileInfo = qFileInfos.poll();

				String srcFileName = fileInfo.getSrcPath();
				String destFileName = fileInfo.getDestPath();
				tools.cpSrcFileToDestFile(srcFileName, destFileName);
				
				System.out.println("拷贝文件到远程结束");
				
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
