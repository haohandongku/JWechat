package com.dcits.fpcy.commons.bean;

import java.io.Serializable;

/**
 * 验证税务总局所需要的参数
 * @author Administrator
 *
 */
@SuppressWarnings("serial")
public class CacheEntity implements Serializable {
	private String key1;
	private String key2;
	private String key3;
	private String key4;
	private String yzm;
	private String ip;
	//打码服务返回id
	private String imageId;
	//验证码不可用错误编码
	private String errorCode;
	//验证码不可用错误编码msg
	private String errorMsg;
	//系统生成id(做业务查询)	
	private String threadId;
	
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	public String getErrorMsg() {
		return errorMsg;
	}
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	public String getKey1() {
		return key1;
	}
	public String getYzm() {
		return yzm;
	}
	public void setYzm(String yzm) {
		this.yzm = yzm;
	}
	public String getImageId() {
		return imageId;
	}
	public void setImageId(String imageId) {
		this.imageId = imageId;
	}
	public void setKey1(String key1) {
		this.key1 = key1;
	}
	public String getKey2() {
		return key2;
	}
	public void setKey2(String key2) {
		this.key2 = key2;
	}
	public String getKey3() {
		return key3;
	}
	public void setKey3(String key3) {
		this.key3 = key3;
	}
	public String getKey4() {
		return key4;
	}
	public void setKey4(String key4) {
		this.key4 = key4;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	
	public String getThreadId() {
		return threadId;
	}
	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}
    
	
	@Override
	public String toString() {
		return "CacheEntity [key1=" + key1 + ", key2=" + key2 + ", key3="
				+ key3 + ", key4=" + key4 + ", yzm=" + yzm + ", ip=" + ip
				+ ", imageId=" + imageId + ", errorCode=" + errorCode
				+ ", errorMsg=" + errorMsg + ", threadId=" + threadId + "]";
	}
	public String toJsonString(){
		return "{\"key1\":\"" + key1 + "\",\"key2\":\"" + key2 + "\",\"key3\":\""
				+ key3 + "\",\"key4\":\"" + key4 + "\",\"yzm\":\"" + yzm + "\",\"ip\":\"" + ip
				+ "\",\"imageId\":\"" + imageId + "\",\"errorCode\":\"" + errorCode + "\"," +
				 "\", errorMsg\":\"" + errorMsg + "\",\"threadId\":\"" + threadId + "\"}";
	}
	
}
