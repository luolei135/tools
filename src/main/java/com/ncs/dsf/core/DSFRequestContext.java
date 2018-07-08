package com.ncs.dsf.core;

import java.io.Serializable;
import java.sql.Connection;
import java.util.HashMap;


public class DSFRequestContext implements Serializable{

	private static final String dbconnection="db_connection";



	private HashMap<String, Object> map = null;

	public DSFRequestContext() {
		map = new HashMap<String, Object>();
	}

	public void setAttribute(String key, Object value) {
		map.put(key, value);
	}
	public void removeAttribute(String key) {
		map.remove(key);
	}

	public Object getAttribute(String key) {
		return map.get(key);
	}

	public Connection getConnection(){
		return (Connection) this.getAttribute(dbconnection);
	}
	public void setConnection(Connection connection){
		this.setAttribute(dbconnection,connection);
	}


}
