package com.dcits.fpcy.commons.service.core.impl;

import java.io.InputStream;
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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.dcits.fpcy.commons.bean.HSCEntity;
import com.dcits.fpcy.commons.bean.IpAddress;
import com.dcits.fpcy.commons.bean.ResultBean;
import com.dcits.fpcy.commons.bean.TaxOfficeBean;
import com.dcits.fpcy.commons.constant.BrowerType;
import com.dcits.fpcy.commons.constant.HeaderType;
import com.dcits.fpcy.commons.constant.SysConfig;
import com.dcits.fpcy.commons.service.core.InvoiceServerBase;
import com.dcits.fpcy.commons.utils.SendResultRequest;

public class SHGSfpcyImp implements InvoiceServerBase {
	private Log logger = LogFactory.getLog(SHGSfpcyImp.class);

	/**
	 * 上海国税发票查验
	 * 
	 * 2017-04-11
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){

		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			parameter.put("yzm", hscEntity.getYzm());
			parameter.put("JSESSIONID", hscEntity.getCookie()); // 开始查验
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("上海国税发票查验" + e);
			List<ResultBean> list = new ArrayList<ResultBean>();
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
		// 这里改成网址传参数时需要的名称
		attributeMap.put("invoiceNo", in_parameter.get("invoiceNo").toString());
		attributeMap.put("fphm", in_parameter.get("fphm").toString());
		attributeMap.put("revenueRegisterId", in_parameter.get("revenueRegisterId").toString());
		attributeMap.put("yzm", in_parameter.get("yzm"));
		String requestMethod = paras.cy_qqfs;
		String JSESSIONID = in_parameter.get("JSESSIONID").toString();
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.COOKIE, JSESSIONID);
		InputStream in = SendResultRequest.sendRequestIn(requestHeader, ipAddress, attributeMap, paras.cy_dz,
				paras.cy_qqfs);
		JSONObject json = parseInvoiceResult(in, in_parameter);
		List<ResultBean> list = new ArrayList<ResultBean>();
		try {
			if (json == null) {
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			} else if ("false".equals(json.get("result"))) {
				list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
				out_result.put(SysConfig.INVOICEFALSESTATE, json.getString(SysConfig.INVOICEFALSESTATE));
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			} else {
				list.add(new ResultBean("CXMXSS", "查询明细", json.get("查询明细").toString()));
				list.add(new ResultBean("CXZTSS", "查询状态", json.get("查询状态").toString()));
				list.add(new ResultBean("YQTS", "友情提示", json.get("友情提示").toString()));
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
			}
		} catch (Exception e) {
			logger.error("上海国地税解析JSONObject异常：" + e);
			logger.error(paras.swjg_mc + "解析返回参数异常", e);
			out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			out_result.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE104);
			e.printStackTrace();
		}
		out_result.put("list", list);
	}

	/**
	 * 上海国税查询结果解析
	 * 
	 * @throws JSONException
	 */
	@SuppressWarnings({ "unused", "rawtypes" })
	public JSONObject parseInvoiceResult(InputStream in, Map in_parameter) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("result", "false");
		if (in == null) {
			json.put("cwxx", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！");
			json.put(SysConfig.INVOICEFALSESTATE, "102");
			return json;
		}
		Document docu = (Document) SendResultRequest.iSToJSONOrDocument(in, "UTF-8", "text");
		String html = docu.toString();
		try {
			if (html.contains("该发票不是纳税人")) {
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE211);
				json.put("cwxx", "该发票不是纳税人" + in_parameter.get("KPFSWDJH").toString() + "购买的。 ");
				return json;
			} else if (html.contains("验证码错误")) {
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE101);
				json.put("cwxx", "发票查询失败，请稍后重试！");
			} else if (html.contains("查询失败") && html.contains("该发票可能不是纳税人")) {
				String key[] = { "查询状态", "cwxx" };
				Elements ele = docu.select("strong");
				int count = 0;
				for (Element element : ele) {
					String jieguo = ele.get(count).text().toString();
					json.put(key[count], jieguo);
					count++;
				}
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE206);
			} else {
				String key[] = { "查询状态", "查询明细" };
				Elements ele = docu.select("strong");
				int count = 0;
				for (Element element : ele) {
					String jieguo = ele.get(count).text().toString();
					json.put(key[count], jieguo);
					count++;
				}
				json.put("友情提示", "查询结果只表明发票在税务征管系统中的记录状态，不作为鉴别假票、虚开、非法开具发票的法律依据。");
				json.put("result", "true");
			}
		} catch (Exception e) {
			System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
			json.put("cwxx", "您输入的发票信息不匹配，请检查是否输入正确！");
			json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE111);
			json.put("result", "false");
		} finally {
		}
		return json;
	}

	public static void trustAllHosts() {
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
