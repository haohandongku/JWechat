package com.dcits.fpcy.commons.service.core.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jsoup.nodes.Document;

import com.dcits.fpcy.commons.bean.HSCEntity;
import com.dcits.fpcy.commons.bean.IpAddress;
import com.dcits.fpcy.commons.bean.ResultBean;
import com.dcits.fpcy.commons.bean.TaxOfficeBean;
import com.dcits.fpcy.commons.constant.BrowerType;
import com.dcits.fpcy.commons.constant.HeaderType;
import com.dcits.fpcy.commons.constant.SysConfig;
import com.dcits.fpcy.commons.service.core.InvoiceServerBase;
import com.dcits.fpcy.commons.utils.SendResultRequest;

public class SDDSfpcyImp implements InvoiceServerBase {

	private Log logger = LogFactory.getLog(SDDSfpcyImp.class);

	/**
	 * 山东地税 https类型
	 * 
	 * @param parameter
	 * @param regionNum
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			parameter.put("sjm", hscEntity.getYzm().toString());
			if (hscEntity.getCookie1() == null) {
				parameter.put("JSESSIONID", hscEntity.getCookie());
			} else {
				parameter.put("JSESSIONID", hscEntity.getCookie1());
			}
			// 开始查验
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("山东地税,获取cook时出现异常", e);
			List list = new ArrayList<ResultBean>();
			list.clear();
			list.add(new ResultBean("cwxx", "", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！"));
			result.put("list", list);
			result.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE103);
			result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		}
		return result;
	}

	/**
	 * 
	 * @param paras
	 *            查验参数
	 * @param in_Parameter
	 *            请求数据 [ 验证码 jessionid]
	 * @param out_Result
	 *            返回结果
	 * @throws JSONException
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result, IpAddress ipAddress)
			throws JSONException {
		Map attributeMap = new HashMap();
		Map<String, String> requestHeader = new HashMap<String, String>();
		attributeMap
				.put("requestXml",
						"<taxML+xmlns=\"http://www.chinatax.gov.cn/gt3nf\"+xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"+xsi:type=\"SbFpcyCshReq\"+cnName=\"\"+name=\"SbFpcyCshReq\"+version=\"SW5001-2006\"><fpdm>"
								+ in_parameter.get("FPDM").toString()
								+ "</fpdm><fphm>"
								+ in_parameter.get("FPHM").toString()
								+ "</fphm><fpje>undefined</fpje><ip>undefined</ip></taxML>");
		attributeMap.put("sid", "ETax.SB.fpcx.Fpcy");
		String requestMethod = paras.cy_qqfs;
		String JSESSIONID = (String) in_parameter.get("JSESSIONID");
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.COOKIE, JSESSIONID);
		requestHeader.put("Accept", "text/plain;charset=UTF-8");
		requestHeader.put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
		requestHeader.put("", "");
		InputStream in = SendResultRequest.sendRequestIn(requestHeader, ipAddress, attributeMap, paras.cy_dz,
				paras.cy_qqfs);// 开始发送请求
		Map map = parseInvoiceResult1(in);
		map.put("action", "queryXml");
		String Nexturl = "https://www.12366.cn/etax/bizfront/rejoinQuery.do";
		in = SendResultRequest.sendRequestIn(requestHeader, ipAddress, map, Nexturl, paras.cy_qqfs);// 开始发送请求
		JSONObject json = parseInvoiceResult2(in);
		List<ResultBean> list = new ArrayList<ResultBean>();
		if (json == null || ("false").equals(json.get("result").toString())) {
			list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
			out_result.put(SysConfig.INVOICEFALSESTATE, json.getString(SysConfig.INVOICEFALSESTATE));
			out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);// 解析返回结果有误
		} else {
			list.add(new ResultBean("FPMC", "发票名称", json.get("fpmc").toString()));
			list.add(new ResultBean("NSRSBH", "纳税人识别号", json.get("nsrsbh").toString()));
			list.add(new ResultBean("KJDW", "纳锐人名称", json.get("nsrmc").toString()));
			out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
		}
		out_result.put("list", list);
	}

	/**
	 * 山东地税第一次请求
	 */
	@SuppressWarnings({ "rawtypes", "unused" })
	public Map parseInvoiceResult1(InputStream in) {
		JSONObject jso = null;
		BufferedReader bufferedReader = null;
		String html = "";
		Map<String, String> map = new HashMap<String, String>();
		try {
			if (in != null) {
				jso = new JSONObject();
				bufferedReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
				String ss = null;
				while ((ss = bufferedReader.readLine()) != null) {
					html += ss;
				}
				String sid = html.substring(html.indexOf("<sid>") + 5, html.indexOf("</sid>"));
				String tid = html.substring(html.indexOf("<tid>") + 5, html.indexOf("</tid>"));
				map.put("sid", sid);
				map.put("tid", tid);
				return map;
			} else {
			}
		} catch (Exception e) {
			System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
		} finally {
			try {
				if (in != null)
					in.close();
				if (bufferedReader != null)
					bufferedReader.close();
			} catch (Exception e) {
				System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
			}
		}
		return map;
	}

	/**
	 * 山东地税第二次请求
	 * 
	 * @throws JSONException
	 */
	@SuppressWarnings("unused")
	public JSONObject parseInvoiceResult2(InputStream in) throws JSONException {
		JSONObject jso = null;
		BufferedReader bufferedReader = null;
		JSONObject json = new JSONObject();
		json.put("result", "false");
		Document document = (Document) SendResultRequest.iSToJSONOrDocument(in, "utf-8", "text");
		String html = document.toString();
		try {
			if (in != null) {
				jso = new JSONObject();
				if (html.contains("<taxML><fpcxResult></fpcxResult></taxML>")) {
					json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE211);
					json.put("cwxx", "未能查询到该发票信息");
					return json;
				} else {
					json.put("fpzl_dm", html.substring(html.indexOf("<fpzl_dm>") + 9, html.indexOf("</fpzl_dm>")));
					json.put("nsrsbh", html.substring(html.indexOf("<nsrsbh>") + 8, html.indexOf("</nsrsbh>")));
					json.put("nsrmc", html.substring(html.indexOf("<nsrmc>") + 7, html.indexOf("</nsrmc>")));
					json.put("fpmc", html.substring(html.indexOf("<fpmc>") + 6, html.indexOf("</fpmc>")));
					json.put("nsrzhdah", html.substring(html.indexOf("<nsrzhdah>") + 10, html.indexOf("</nsrzhdah>")));

					json.put("result", "true");
				}
			} else {
				json.put("cwxx", "发票查询失败，请稍后重试！");
				json.put("result", "false");
			}
		} catch (Exception e) {
			json.put("cwxx", "您输入的发票信息不匹配，请检查是否输入正确！");
			json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE203);
			json.put("result", "false");
			e.printStackTrace();
		} finally {
			try {
				if (in != null)
					in.close();
				if (bufferedReader != null)
					bufferedReader.close();
			} catch (Exception e2) {
				json.put("cwxx", "您输入的发票信息不匹配，请检查是否输入正确！");
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE203);
				json.put("result", "false");
			}
		}
		return json;
	}

	public static void trustAllHosts() {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[] {};
			}

			public void checkClientTrusted(X509Certificate[] chain, String authType) {
			}

			public void checkServerTrusted(X509Certificate[] chain, String authType) {
			}
		} };

		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
