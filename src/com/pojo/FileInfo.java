package com.pojo;

/**
 * @author Boris
 * @description 
 * 2016年8月12日
 */
public class FileInfo {
	String srcPath;//源路径
	String destPath;//目标路径
	String sql; //更新数据库的sql
	
	public FileInfo() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @param destPath
	 * @param srcPath
	 * @param sql
	 */
	public FileInfo(String srcPath, String destPath, String sql) {
		super();
		this.destPath = destPath;
		this.srcPath = srcPath;
		this.sql = sql;
	}
	public String getDestPath() {
		return destPath;
	}
	public void setDestPath(String destPath) {
		this.destPath = destPath;
	}
	public String getSrcPath() {
		return srcPath;
	}
	public void setSrcPath(String srcPath) {
		this.srcPath = srcPath;
	}
	public String getSql() {
		return sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}
	
	
}
