package com.dcits.fpcy.commons.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


/**
 * http session cache
 * 
 */
public class HSCEntity implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//验证码
	private String yzm;
	//cyym 查验页面 cookieList
	private List<String> list;
	//yzm cookieList
	private List<String> list1;
	//ip地址
	private IpAddress ipAddress;
	//配置信息
	private TaxOfficeBean fpcyParas;
	//cyym cookie info
	private String cookie;
	//yzm cookie info
	private String cookie1;
	//cyym cookie和in流
    private Map<String,Object>  map;
	public String getYzm() {
		return yzm;
	}

	public Map<String, Object> getMap() {
		return map;
	}

	public void setMap(Map<String, Object> map) {
		this.map = map;
	}

	public void setYzm(String yzm) {
		this.yzm = yzm;
	}

	public List<String> getList() {
		return list;
	}

	public void setList(List<String> list) {
		this.list = list;
	}

	public IpAddress getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(IpAddress ipAddress) {
		this.ipAddress = ipAddress;
	}

	public TaxOfficeBean getFpcyParas() {
		return fpcyParas;
	}

	public void setFpcyParas(TaxOfficeBean fpcyParas) {
		this.fpcyParas = fpcyParas;
	}

	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	public String getCookie1() {
		return cookie1;
	}

	public void setCookie1(String cookie1) {
		this.cookie1 = cookie1;
	}

	public List<String> getList1() {
		return list1;
	}

	public void setList1(List<String> list1) {
		this.list1 = list1;
	}

   	
	
}
