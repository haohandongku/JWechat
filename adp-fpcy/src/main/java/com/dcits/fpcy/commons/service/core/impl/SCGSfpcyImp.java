package com.dcits.fpcy.commons.service.core.impl;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

public class SCGSfpcyImp implements InvoiceServerBase {
	/**
	 * 四川国税发票查验
	 * 
	 * 2017-04-11
	 */
	private Log logger = LogFactory.getLog(SCGSfpcyImp.class);

	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	@Override
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			Map<String, Object> map = hscEntity.getMap();
			parameter.put("JSESSION", hscEntity.getCookie());
			// parameter.put("in", map.get("in"));
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("四川国税,获取cook时出现异常", e);
			List<ResultBean> list = new ArrayList<ResultBean>();
			list.clear();
			list.add(new ResultBean("cwxx", "", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！"));
			result.put("list", list);
			result.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE103);
			result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		}
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result, IpAddress ipAddress)
			throws JSONException {
		Map attributeMap = new HashMap();
		Map requestHeader = new HashMap();
		String fpdm = in_parameter.get("fpcy.fpdm").toString();
		attributeMap.put("fpcy.fpdm", fpdm);
		attributeMap.put("fpcy.fphm", in_parameter.get("fpcy.fphm").toString());
		// inn=(InputStream) in_parameter.get("in");
		// String token = parseInvoiceResult111(inn);
		String jsonid = in_parameter.get("JSESSION").toString();
		String jsonid1 = jsonid.substring(1, jsonid.indexOf(";"));
		requestHeader.put("Cookie", jsonid1);

		// fpzl二次跳转
		/*
		 * InputStream in0 = ZLpost(requestHeader, ipAddress,
		 * "http://wsbs.sc-n-tax.gov.cn/fpcy/fpzl.htm", attributeMap);
		 * JSONObject json222 = parseInvoiceResult222(in0);
		 */

		if (fpdm.length() == 12) {
			if (paras.swjg_dm.equals("05100")) {
				attributeMap.put("fpcy.type", in_parameter.get("fpcy.type").toString());
				attributeMap.put("fpcy.xfsbh", in_parameter.get("fpcy.xfsbh").toString());
				attributeMap.put("fpcy.jshj", in_parameter.get("fpcy.jshj").toString());
			} else {
				attributeMap.put("fpcy.type", "0");
			}
		} else {
			if (in_parameter.get("fpcy.type").toString().equals("1")) {
				attributeMap.put("fpcy.type", in_parameter.get("fpcy.type").toString());
				attributeMap.put("fpcy.gfsbh", in_parameter.get("fpcy.gfsbh").toString());
				attributeMap.put("fpcy.xfsbh", in_parameter.get("fpcy.xfsbh").toString());
				attributeMap.put("fpcy.jshj", in_parameter.get("fpcy.jshj").toString());
				attributeMap.put("fpcy.gfmc", "");
				attributeMap.put("fpcy.xfmc", "");
				attributeMap.put("fpcy.kprq", "");
			} else {
				attributeMap.put("fpcy.type", in_parameter.get("fpcy.type").toString());
				attributeMap.put("fpcy.xfsbh", in_parameter.get("fpcy.xfsbh").toString());
				attributeMap.put("fpcy.jshj", in_parameter.get("fpcy.jshj").toString());
			}

		}
		// attributeMap.put("token", token);
		// requestHeader.put("HeaderType.COOKIE",
		// cookie.split(";")[0].toString().trim());
		requestHeader.put("Host", "wsbs.sc-n-tax.gov.cn");
		requestHeader.put("Origin", "http://wsbs.sc-n-tax.gov.cn");
		requestHeader.put("Referer", "http://wsbs.sc-n-tax.gov.cn/fpcy/index.htm");
		requestHeader
				.put("User-Agent",
						"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36");
		InputStream in = SendResultRequest.sendRequestPost(requestHeader, ipAddress, paras.cy_dz, attributeMap);
		JSONObject json = parseInvoiceResult(in);
		List<ResultBean> list = new ArrayList<ResultBean>();
		try {
			if (json == null || ("false").equals(json.get("result").toString())) {
				list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
				out_result.put(SysConfig.INVOICEFALSESTATE, json.getString(SysConfig.INVOICEFALSESTATE));
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			} else {
				list.add(new ResultBean("pz", "发票种类", paras.swjg_mc));
				list.add(new ResultBean("fpdm", "发票代码", in_parameter.get("FPDM").toString()));
				list.add(new ResultBean("fphm", "发票号码", in_parameter.get("FPHM").toString()));
				if (fpdm.length() == 12 && paras.swjg_dm.equals("15100")) {
					list.add(new ResultBean("fpmc", "发票名称", json.get("fpmc").toString()));
					list.add(new ResultBean("fpztmc", "发票查询结果", json.get("fpztmc").toString()));
					list.add(new ResultBean("fsdw", "发售单位", json.get("fsdw").toString()));
					list.add(new ResultBean("fsrq", "领用日期", json.get("fsrq").toString()));
					list.add(new ResultBean("lgr", "领用单位", json.get("lgr").toString()));
				} else {

					list.add(new ResultBean("res", "查询结果", json.get("RES").toString()));
				}
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
			}
		} catch (Exception e) {
			logger.error(paras.swjg_mc + "解析返回参数异常", e);
			out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			out_result.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE104);
			e.printStackTrace();
		}
		out_result.put(SysConfig.INVOICEFALSESTATE, json.get(SysConfig.INVOICEFALSESTATE).toString());
		out_result.put("list", list);
	}

	/**
	 * 四川国税结果解析
	 * 
	 * @throws JSONException
	 */
	public static JSONObject parseInvoiceResult(InputStream in) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("result", "false");
		if (in == null) {
			json.put("cwxx", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！");
			json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE102);
			return json;
		}
		try {
			Document doc = (Document) SendResultRequest.iSToJSONOrDocument(in, "UTF-8", "text");
			String result = doc.toString();
			if (result.contains("发票不存在")) {
				json.put("cwxx", "您所查验的发票不存在");
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE211);
				return json;
			} else if (result.contains("已发售")) {
				String body = doc.select("body").get(0).text();
				JSONObject jsonObject = new JSONObject(body);
				String fpcyResult = jsonObject.getString("fpcyResult");
				JSONObject res = new JSONObject(fpcyResult);
				String[] key = { "fpdm", "fphm", "fpmc", "fpztmc", "fsdw", "fsrq", "lgr" };
				for (String str : key) {
					json.put(str, res.get(str).toString());
				}
				json.put("result", "true");
			} else if (result.contains("系统繁忙")) {
				json.put("cwxx", "系统繁忙，请稍候重试！");
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE213);
				return json;
			} else if (result.contains("相符") || result.contains("不符")) {
				String body = doc.select("body").get(0).text();
				JSONObject jsonObject = new JSONObject(body);
				String fpcyResult = jsonObject.getString("fpcyResult");
				JSONObject res = new JSONObject(fpcyResult);
				String bdxf = res.getString("bdxf");
				if ("Y".equals(bdxf)) {
					bdxf = "您查验的发票比对相符。详情如下：<br/>";
				} else {
					bdxf = "您查验的发票比对不相符，请核实您填写的信息。比对详情如下：<br/>";
				}
				String bdxq = res.getString("bdxq");
				String[] bds = bdxq.substring(1, bdxq.lastIndexOf("]")).split(",");
				StringBuffer sb = new StringBuffer(bdxf);
				for (int i = 0; i < bds.length; i++) {
					if (bds[i].contains("<")) {
						bds[i] = bds[i].substring(1, bds[i].lastIndexOf("<"));
					} else {
						bds[i] = bds[i].substring(1, bds[i].lastIndexOf("\""));
					}
					sb.append((i + 1) + "、" + bds[i] + "<br/>");
				}
				json.put("RES", sb.toString());
				json.put("result", "true");
			} else {
				String body = doc.select("body").get(0).text();
				JSONObject jsonObject = new JSONObject(body);
				String fpcyResult = jsonObject.getString("fpcyResult");
				JSONObject res = new JSONObject(fpcyResult);
				String[] key = { "fpdm", "fphm", "fpmc", "fpztmc", "fsdw", "fsrq", "lgr" };
				for (String str : key) {
					json.put(str, res.get(str).toString());
				}
				json.put("result", "true");
			}
			json.put(SysConfig.INVOICEFALSESTATE, "000");
			return json;
		} catch (Exception e) {
			json.put("cwxx", "您输入的发票信息不匹配，请检查是否输入正确！");
			json.put("result", "false");
			json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE111);
			e.printStackTrace();
			return json;
		}

	}

	@SuppressWarnings("unused")
	public String parseInvoiceResult111(InputStream in) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("result", "false");
		try {
			if (in != null) {
				Document doc = (Document) SendResultRequest.iSToJSONOrDocument(in, "UTF-8", "text");
				String result = doc.toString();
				String body = doc.select("form").select("input").get(1).val();
				return body;
			}
		} catch (Exception e) {
			json.put("cwxx", "您输入的发票信息不匹配，请检查是否输入正确！");
			json.put("result", "false");
			e.printStackTrace();
		}

		return null;
	}

	public JSONObject parseInvoiceResult222(InputStream in) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("result", "false");
		try {
			if (in != null) {
				Document doc = (Document) SendResultRequest.iSToJSONOrDocument(in, "UTF-8", "");
				String body = doc.select("body").text();
				JSONObject json1 = new JSONObject(body);
				return json1;
			}
		} catch (Exception e) {
			json.put("cwxx", "您输入的发票信息不匹配，请检查是否输入正确！");
			json.put("result", "false");
			e.printStackTrace();
		}

		return null;
	}

	@SuppressWarnings("unused")
	public static InputStream ZLpost(Map<String, String> map, IpAddress ipAddress, String requestAddress,
			Map<String, String> attributeMap) {
		InputStream in = null;
		OutputStream out = null;
		DataOutputStream dos = null;
		String attributeString = null;
		HttpURLConnection conn = null;
		URL url = null;
		try {
			url = new URL(requestAddress);
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setUseCaches(false);
			if (null != map) {
				Iterator<String> iterator = map.keySet().iterator();
				while (iterator.hasNext()) {
					String key = iterator.next();
					String value = (String) map.get(key);
					conn.setRequestProperty(key, value);
				}
			} else {
				conn.setRequestProperty(HeaderType.USERAGENT, BrowerType.firfox);
			}
			attributeString = SendResultRequest.structureAttribute(attributeMap);

			out = conn.getOutputStream();
			dos = new DataOutputStream(out);
			dos.writeBytes(attributeString);
			dos.flush();

			if (conn.getResponseCode() != 200)
				return null;
			// 响应头信息
			Map<String, List<String>> topheader = conn.getHeaderFields();
			Set<Entry<String, List<String>>> headerSet = topheader.entrySet();// 返回此映射中包含的映射关系的
			Iterator<Entry<String, List<String>>> it = headerSet.iterator();

			in = conn.getInputStream();// 获取发票验真返回信息
			return in;
		} catch (Exception e) {

		}
		return null;
	}
}
