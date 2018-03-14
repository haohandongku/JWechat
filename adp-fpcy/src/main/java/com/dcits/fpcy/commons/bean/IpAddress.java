package com.dcits.fpcy.commons.bean;

import java.io.Serializable;

public class IpAddress implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String ip;
	private int port;
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public IpAddress(){}
	public IpAddress(String ip, int port) {
		super();
		this.ip = ip;
		this.port = port;
	}
	@Override
	public String toString() {
		return "IpAddress [ip=" + ip + ", port=" + port + "]";
	}
	
}
