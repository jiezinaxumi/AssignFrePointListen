package com.manager;

import com.db.operation.CRUD;
import com.pojo.FileInfo;
import com.util.Tools;

/**
 * @author Boris
 * @description 将本地文件拷贝到远程
 * 2016年8月12日
 */
public class CopyFileManager implements Runnable{
	private Tools tools = Tools.getTools();
	private CRUD crud = new CRUD();
	
	private FileInfo fileInfo;
	
	public CopyFileManager(FileInfo fileInfo){
		this.fileInfo = fileInfo;
	}
	
	

	private void cpFile() throws InterruptedException{
		System.out.println("拷贝文件到远程");
		String srcFileName = fileInfo.getSrcPath();
		String destFileName = fileInfo.getDestPath();
		if (tools.cpSrcFileToDestFile(srcFileName, destFileName)) {
			System.out.println("拷贝文件到远程结束");
			
			String sql = fileInfo.getSql();
			crud.instert(sql);
		}else{
			System.out.println("文件小于100k 不拷贝");
		}
		
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
