package com.manager;

import com.db.operation.CRUD;
import com.pojo.FileInfo;
import com.util.Tools;

/**
 * @author Boris
 * @description �������ļ�������Զ��
 * 2016��8��12��
 */
public class CopyFileManager implements Runnable{
	private Tools tools = Tools.getTools();
	private CRUD crud = new CRUD();
	
	private FileInfo fileInfo;
	
	public CopyFileManager(FileInfo fileInfo){
		this.fileInfo = fileInfo;
	}
	
	

	private void cpFile() throws InterruptedException{
		System.out.println("�����ļ���Զ��");
		String srcFileName = fileInfo.getSrcPath();
		String destFileName = fileInfo.getDestPath();
		if (tools.cpSrcFileToDestFile(srcFileName, destFileName)) {
			System.out.println("�����ļ���Զ�̽���");
			
			String sql = fileInfo.getSql();
			crud.instert(sql);
		}else{
			System.out.println("�ļ�С��100k ������");
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
