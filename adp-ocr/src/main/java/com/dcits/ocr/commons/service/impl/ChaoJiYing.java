package com.dcits.ocr.commons.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dcits.ocr.commons.utils.HttpUtils;

public class ChaoJiYing {
	private static Log LOG = LogFactory.getLog(ChaoJiYing.class);
	private static String userName;
	private static String password;
	private static String softId;
	private static String timeAdd;
	private static String strDebug;

	/**
	 * 字符串MD5加密
	 * 
	 * @param s
	 *            原始字符串
	 * @return 加密后字符串
	 */
	public final static String MD5(String s) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		try {
			byte[] btInput = s.getBytes();
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			mdInst.update(btInput);
			byte[] md = mdInst.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 通用POST方法
	 * 
	 * @param url
	 *            请求URL
	 * @param param
	 *            请求参数，如：username=test&password=1
	 * @return response
	 * @throws IOException
	 */
	public static String httpRequestData(String url, String param) throws IOException {
		URL u;
		HttpURLConnection con = null;
		OutputStreamWriter osw;
		StringBuffer buffer = new StringBuffer();

		u = new URL(url);
		con = HttpUtils.getConnection(u);
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

		osw = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
		osw.write(param);
		osw.flush();
		osw.close();

		BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
		String temp;
		while ((temp = br.readLine()) != null) {
			buffer.append(temp);
			buffer.append("\n");
		}

		return buffer.toString();
	}

	/**
	 * 查询题分
	 * 
	 * @param username
	 *            用户名
	 * @param password
	 *            密码
	 * @return response
	 * @throws IOException
	 */
	public static String GetScore(String username, String password) {
		String param = String.format("user=%s&pass=%s", username, password);
		String result;
		try {
			result = ChaoJiYing.httpRequestData("http://code.chaojiying.net/Upload/GetScore.php", param);
		} catch (IOException e) {
			result = "未知问题："+e.getMessage();
		}
		return result;
	}

	/**
	 * 注册账号
	 * 
	 * @param username
	 *            用户名
	 * @param password
	 *            密码
	 * @return response
	 * @throws IOException
	 */
	public static String UserReg(String username, String password) {
		String param = String.format("user=%s&pass=%s", username, password);
		String result;
		try {
			result = ChaoJiYing.httpRequestData("http://code.chaojiying.net/Upload/UserReg.php", param);
		} catch (IOException e) {
			result = "未知问题:"+e.getMessage();
		}
		return result;
	}

	/**
	 * 账号充值
	 * 
	 * @param username
	 *            用户名
	 * @param card
	 *            卡号
	 * @return response
	 * @throws IOException
	 */
	public static String UserPay(String username, String card) {

		String param = String.format("user=%s&card=%s", username, card);
		String result;
		try {
			result = ChaoJiYing.httpRequestData("http://code.chaojiying.net/Upload/UserPay.php", param);
		} catch (IOException e) {
			result = "未知问题:"+e.getMessage();
		}
		return result;
	}

	/**
	 * 报错返分
	 * 
	 * @param username
	 *            用户名
	 * @param password
	 *            用户密码
	 * @param softId
	 *            软件ID
	 * @param id
	 *            图片ID
	 * @return response
	 * @throws IOException
	 */
	public static String ReportError(String id) {

		String param = String.format("user=%s&pass=%s&softid=%s&id=%s", userName, password, softId, id);
		String result;
		try {
			result = ChaoJiYing.httpRequestData("http://code.chaojiying.net/Upload/ReportError.php", param);
		} catch (IOException e) {
			LOG.error(e.getMessage());
			result = "未知问题:"+e.getMessage();
		}

		return result;
	}

	/**
	 * 核心上传函数
	 * 
	 * @param url
	 *            请求URL
	 * @param param
	 *            请求参数，如：username=test&password=1
	 * @param data
	 *            图片二进制流
	 * @return response
	 * @throws IOException
	 */
	public static String httpPostImage(String url, String param, byte[] data,String imgType) throws IOException {
		long time = (new Date()).getTime();
		URL u = new URL(url);
		HttpURLConnection con = null;
		String boundary = "----------" + MD5(String.valueOf(time));
		String boundarybytesString = "\r\n--" + boundary + "\r\n";
		OutputStream out = null;

		con = HttpUtils.getConnection(u);
		con.setRequestMethod("POST");
		// con.setReadTimeout(60000);
		con.setConnectTimeout(60000);
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setUseCaches(true);
		con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
		out = con.getOutputStream();
		for (String paramValue : param.split("[&]")) {
			out.write(boundarybytesString.getBytes("UTF-8"));
			String paramString = "Content-Disposition: form-data; name=\"" + paramValue.split("[=]")[0] + "\"\r\n\r\n"
					+ paramValue.split("[=]")[1];
			out.write(paramString.getBytes("UTF-8"));
		}
		out.write(boundarybytesString.getBytes("UTF-8"));
		String paramString = "Content-Disposition: form-data; name=\"userfile\"; filename=\"" + "chaojiying_java."+imgType
				+ "\"\r\nContent-Type: application/octet-stream\r\n\r\n";
		out.write(paramString.getBytes("UTF-8"));
		out.write(data);
		String tailer = "\r\n--" + boundary + "--\r\n";
		out.write(tailer.getBytes("UTF-8"));
		out.flush();
		out.close();
		StringBuffer buffer = new StringBuffer();
		BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
		String temp;
		while ((temp = br.readLine()) != null) {
			buffer.append(temp);
			buffer.append("\n");
		}

		return buffer.toString();
	}


	/**
	 * 识别图片_按图片二进制流
	 * 
	 * @param username
	 *            用户名
	 * @param password
	 *            密码
	 * @param softid
	 *            软件ID
	 * @param codetype
	 *            图片类型
	 * 
	 * @param len_min
	 *            最小位数
	 * @param time_add
	 *            附加时间
	 * @param str_debug
	 *            开发者自定义信息
	 * @param byteArr
	 *            图片二进制数据流
	 * @return
	 * @throws IOException
	 */
	public static String PostPic(byte[] byteArr, String codeType, String len_min,String imgType) {
		String result = "";
		String param = String.format("user=%s&pass=%s&softid=%s&codetype=%s&len_min=%s&time_add=%s&str_debug=%s",
				userName, password, softId, codeType, len_min, timeAdd, strDebug);
		try {
			result = ChaoJiYing.httpPostImage("http://upload.chaojiying.net/Upload/Processing.php", param, byteArr,imgType);
			LOG.error("---------超级鹰返回--------"+result);
		} catch (Exception e) {
			LOG.error("超级鹰识别错误"+e);
			result = "未知问题";
		}

		return result;
	}

	public static void setUserName(String userName) {
		ChaoJiYing.userName = userName;
	}

	public static void setPassword(String password) {
		ChaoJiYing.password = password;
	}

	public static void setSoftId(String softId) {
		ChaoJiYing.softId = softId;
	}

	public static void setTimeAdd(String timeAdd) {
		ChaoJiYing.timeAdd = timeAdd;
	}

	public static void setStrDebug(String strDebug) {
		ChaoJiYing.strDebug = strDebug;
	}

}
