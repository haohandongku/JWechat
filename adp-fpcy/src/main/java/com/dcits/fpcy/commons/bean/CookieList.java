package com.dcits.fpcy.commons.bean;

import java.util.List;

public class CookieList {

	private static String[] items = new String[] {
		"TSCX_SESSION","gt3nf_etax",
		"FPXKSESSIONID","FpzxSession",
		"node","BWDSESSION",
		"nsfwsession","JSESSION",
		"Session","JSID_KPGL",
		"wsswCookie","bsfwt_SESSIONID","Cookie",
		"JSID_WSBS","WSBSSESSIONID","f5cookie","sto-id-20480-wb_nxwbbsd","cookie"
		,"BIGipServerDMZ_Pool_FPCX","sid"};
	
	public static String getItem(List<String> list){
		if (list == null) {
			return null;
		}
		StringBuffer str=new StringBuffer();
		for(int i = 0; i < items.length; i++){
				for (String str0 : list) {
					if(str0.contains(items[i])){
						str=str.append(str0+";");
					}
				}
		}
		if(str.length() == 0) {
			return null;
		}
		str.deleteCharAt(str.length()-1);
		String str1=new String(str);
		return str1;
	}
	
	public static String[] getItem1(List<String> list){
		for(int i = 0; i < items.length; i++){
			for (String str : list) {
				if(str.contains(items[i])){
					return new String[]{items[i],str};
				}
			}
		}
		return null;
	}
}
