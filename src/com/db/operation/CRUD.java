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
	
	public CRUD(){
		try {
			conn = DBConn.getConn();
			st = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ResultSet find(String sql){
		ResultSet rs = null;
		try {
			rs =  st.executeQuery(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return rs;
	}
	
	public void update(String sql){
		try {
			st.executeUpdate(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void instert(String sql){
		update(sql);
	}
}
