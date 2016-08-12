package com.db.operation;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import com.db.jdbc.DBConn;

/**
 * @author Boris
 * @description 增删改查
 * 2016年8月10日
 */
public class CRUD {
	private Connection conn = null;
	private Statement st = null;
	
	public ResultSet find(String sql){
		conn = DBConn.getConn();
		
		ResultSet rs = null;
		try {
			st = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			rs = st.executeQuery(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return rs;
	}
	
	public void update(String sql){
		conn = DBConn.getConn();
		try {
			st = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			st.executeUpdate(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			close();
		}
	}
	
	public void instert(String sql){
		update(sql);
	}
	
	public void close(){
		try {
			st.close();
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
