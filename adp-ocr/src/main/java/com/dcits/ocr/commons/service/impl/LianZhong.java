package com.dcits.ocr.commons.service.impl;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.MultipartPostMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.dcits.ocr.commons.utils.HttpUtils;

//联众验证码识别

@SuppressWarnings("deprecation")
public class LianZhong {
	/** 主机 */
	private static String getDomainArray;
	private static String userName;
	private static String password;
	private static String code;
	private static String toolToken;
	private static final Log logger = LogFactory.getLog(LianZhong.class);
	public final static String strChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-=+";

	public static String YZMSB(byte[] filePath, String min, String max, String codeType, String fileType)
			throws Exception {
		InputStream in = null;
		OutputStream out = null;
		HttpURLConnection con = null;
		Map<String, String> paramMap = getParamMap(min, max, codeType);
		String returnYZM = "";
		String BOUNDARY = "---------------------------68163001211748"; // boundary就是request头和上传文件内容的分隔符
		String str = "http://bbb4.hyslt.com/api.php?mod=php&act=upload";
		try {
			con = HttpUtils.getConnection(new URL(str));
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("content-type", "multipart/form-data; boundary=" + BOUNDARY);
			con.setConnectTimeout(15000);
			con.setReadTimeout(15000);
			out = new DataOutputStream(con.getOutputStream());
			// 普通参数
			if (paramMap != null) {
				StringBuffer strBuf = new StringBuffer();
				Iterator<Entry<String, String>> iter = paramMap.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<String, String> entry = iter.next();
					String inputName = entry.getKey();
					String inputValue = entry.getValue();
					strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
					strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"\r\n\r\n");
					strBuf.append(inputValue);
				}
				out.write(strBuf.toString().getBytes());
			}
			String contentType = "image/" + fileType;
			StringBuffer strBuf = new StringBuffer();
			strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
			strBuf.append("Content-Disposition: form-data; name=\"" + "upload" + "\"; filename=\""
					+ new Date().getTime() + "." + fileType + "\"\r\n");
			strBuf.append("Content-Type:" + contentType + "\r\n\r\n");
			out.write(strBuf.toString().getBytes());
			out.write(filePath);
			byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
			out.write(endData);
			out.flush();

			// 读取URLConnection的响应
			in = con.getInputStream();
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			while (true) {
				int rc = in.read(buf);
				if (rc <= 0) {
					break;
				} else {
					bout.write(buf, 0, rc);
				}
			}

			// 结果输出
			returnYZM = new String(bout.toByteArray());

			// Map<String,String> result= ParseJsonYZM(returnYZM);
			return returnYZM;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("验证码识别超时", e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;

	}

	public static Map<String, String> getParamMap(String minlen, String maxlen, String yzm_lx) {
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("user_name", userName);
		paramMap.put("user_pw", password);
		paramMap.put("yzm_minlen", minlen); // 验证码最小长度
		paramMap.put("yzm_maxlen", maxlen); // 验证码最大
		paramMap.put("yzmtype_mark", yzm_lx); // 计算题类型为5，一般为0
		paramMap.put("zztool_token", toolToken);
		return paramMap;
	}

	/**
	 * 联众纠错接口
	 * 
	 * by fangysa 16-7-28
	 * 
	 * void ReportError(LPSTR strVcodeUser,LPTSTR stryzmid) 命令名称:ReportError
	 * 命令功能:对打错的验证码进行报告。 注明：：联众返回值类型:文本型 成功返回->验证码结果|!|验证码标识
	 * 参数1（strVcodeUser):联众账号 参数类型: 文本型 参数2（stryzmid):验证码标识 参数类型: 文本型 返回值类型:空
	 * 无返回值
	 * 
	 * @param user_name
	 * @param user_pw
	 * @param yzm_id
	 */
	public static String ReportError(String yzm_id) {
		String responseMsg = "";
		HttpClient httpClient = new HttpClient();
		httpClient.getParams().setContentCharset("GBK");
		PostMethod postMethod = new PostMethod("http://bbb4.hyslt.com/api.php?mod=php&act=error");// 联众纠错系统接口地址
		postMethod.addParameter("user_name", userName);
		postMethod.addParameter("user_pw", password);
		postMethod.addParameter("yzm_id", yzm_id);
		try {
			httpClient.executeMethod(postMethod);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InputStream in = postMethod.getResponseBodyAsStream();
			int len = 0;
			byte[] buf = new byte[1024];
			while ((len = in.read(buf)) != -1) {
				out.write(buf, 0, len);
			}
			responseMsg = out.toString("UTF-8");
			return responseMsg;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			postMethod.releaseConnection();
		}
		return null;
	}

	// 查询点数
	public static String point(String domain) throws Exception {
		HttpClient client = new HttpClient();
		String result = "";
		MultipartPostMethod filePost = new MultipartPostMethod("http://" + domain + "/api.php?mod=yzm&act=point");
		filePost.addParameter("mac", getStrMac());
		filePost.addParameter("key", getKey());
		filePost.addParameter("user_name", userName);
		filePost.addParameter("user_pw", getBASE64(password));
		client.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
		client.executeMethod(filePost);
		result = getTranCode(filePost.getResponseBodyAsString(), code);
		return result;
	}

	public static String getBASE64(String s) {
		if (s == null)
			return null;
		return (new sun.misc.BASE64Encoder()).encode(s.getBytes());
	}

	public static String getTranCode(String str, String code) {
		if (System.getProperty("os.name").contains("Windows")) {
			byte[] data;
			try {
				data = str.getBytes("ISO8859-1");
				return new String(data, code);
			} catch (UnsupportedEncodingException e) {
				return str;
			}
		} else {
			return str;
		}

	}

	// 获取服务器
	@SuppressWarnings("unchecked")
	public static String getDoMainArryAndValidUser() throws Exception {
		HttpClient client = new HttpClient();
		String result = "";
		PostMethod postMethod;
		postMethod = new PostMethod( getDomainArray+ "/api.php?mod=yzm&act=server");
		postMethod.addParameter("user_name", userName);
		postMethod.addParameter("user_pw", getBASE64(password));
		postMethod.addParameter("mac", getStrMac());
		postMethod.addParameter("key", getKey());
		postMethod.addParameter("submit", "%CC%ED+%BC%D3");
		client.executeMethod(postMethod);
		result = new String(postMethod.getResponseBody(), "gbk");
		logger.debug("getDoMainArryAndValidUser:" + result);
		Map<String, String> rm = getMapFromJson(result);
		return (String) rm.get("data");

	}

	/**
	 * 每天邮件提示是否还有提分
	 * 
	 * @param args
	 */
	public static String Point() {
		String s = null;
		try {
			String domain = getDoMainArryAndValidUser();
			s = point(domain) + "   tgcs" + "联众";
			return s;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}

	public static String getKey() {
		return entryPassEntryPass(getMacAddress(), getMacAddress());

	}

	private static String getStrMac() {
		return new sun.misc.BASE64Encoder().encodeBuffer(getMacAddress().getBytes());
	}

	public static String entryPassEntryPass(String strTxt, String strKey) {
		int nh = (int) (Math.random() * (strChars.length()));
		// System.out.println("nh---->" + nh );
		// int nh=42;
		char ch = strChars.charAt(nh);
		// System.out.println("nh---->" + nh + "   ch--->" + ch);

		String strCh = String.format("%c", ch);
		String strMKey = strKey;
		strMKey += strCh;
		// String strRMkey = strMKey.Mid(nh % 8, nh % 8 + 7);
		int s = nh % 8;
		int e = nh % 8 + 7;
		// System.out.println("s-->" + s + "   e-->" + e);
		String strRMkey = "";
		strRMkey = strMKey.substring(s, (strMKey.length() > s + e) ? s + e : strMKey.length());

		// String strBase64Code = m_Base64.Encode(strTxt, strTxt.GetLength());
		String strBase64Code = new sun.misc.BASE64Encoder().encodeBuffer(strTxt.getBytes());

		// System.out.println("strCh--->" + strCh + "    strMKey--->" + strMKey
		// + "  strRMeky--->" + strRMkey + "   strBase64Code-->" +
		// strBase64Code);
		String temp = "";
		int i = 0, j = 0, k = 0;

		for (i = 0; i < strBase64Code.length(); i++) {
			if (k == strRMkey.length()) {
				k = 0;
			}
			int x1 = nh + strChars.indexOf(strBase64Code.charAt(i), 0);
			int x2 = (int) (strRMkey.charAt(k++));
			j = (x1 + x2) % 64;
			char b = strChars.charAt(j);
			String strB = String.format("%c", b);
			temp += strB;
		}
		String strR = strCh;
		strR += temp;
		return strR;

	}

	public static String getMacAddress() {
		String mac = "YzBjYjM4MzQ1MzY2";
		String line = "JRfsfZfJ2Ff7xNvV0";

		String os = System.getProperty("os.name");

		if (os != null && os.startsWith("Windows")) {
			try {
				String command = "cmd.exe /c ipconfig /all";
				Process p = Runtime.getRuntime().exec(command);

				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));

				while ((line = br.readLine()) != null) {
					// System.out.println(line);
					if (line.indexOf("Physical Address") > 0 || line.indexOf("物理地址") > 0) {
						int index = line.indexOf(":") + 2;

						mac = line.substring(index);

						break;
					}
				}

				br.close();

			} catch (IOException e) {
			}
		}

		return mac;
	}

	public static String getMacAddress(String host) {
		String mac = "";
		StringBuffer sb = new StringBuffer();

		try {
			NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getByName(host));

			byte[] macs = ni.getHardwareAddress();

			for (int i = 0; i < macs.length; i++) {
				mac = Integer.toHexString(macs[i] & 0xFF);

				if (mac.length() == 1) {
					mac = '0' + mac;
				}

				sb.append(mac + "-");
			}

		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		mac = sb.toString();
		mac = mac.substring(0, mac.length() - 1);

		return mac;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Map getMapFromJson(String jsonString) {
		JSONObject jsonObject;
		Map map = new HashMap();
		try {
			jsonObject = new JSONObject(jsonString);
			for (Iterator iter = jsonObject.keys(); iter.hasNext();) {
				String key = (String) iter.next();
				map.put(key, jsonObject.get(key));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return map;
	}

	public static void setGetDomainArray(String getDomainArray) {
		LianZhong.getDomainArray = getDomainArray;
	}

	public static void setUserName(String userName) {
		LianZhong.userName = userName;
	}

	public static void setPassword(String password) {
		LianZhong.password = password;
	}

	public static void setCode(String code) {
		LianZhong.code = code;
	}

	public static void setToolToken(String toolToken) {
		LianZhong.toolToken = toolToken;
	}

}
