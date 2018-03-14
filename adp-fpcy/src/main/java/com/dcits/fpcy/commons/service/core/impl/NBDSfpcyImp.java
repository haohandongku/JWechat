package com.dcits.fpcy.commons.service.core.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

public class NBDSfpcyImp implements InvoiceServerBase {

	private Log logger = LogFactory.getLog(NBDSfpcyImp.class);

	/**
	 * 第二版 宁波地税
	 * 
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map FPCY(Map parameter, TaxOfficeBean fppars){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			parameter.put("JSESSIONID", hscEntity.getCookie());
			parameter.put("randomStr", hscEntity.getYzm());
			// 开始查验
			getResult(fppars, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("宁波地税,获取cook时出现异常", e);
			List list = new ArrayList<ResultBean>();
			list.clear();
			list.add(new ResultBean("cwxx", "", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！"));
			result.put("list", list);
			result.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE103);
			result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		}
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result, IpAddress ipAddress) throws Exception {
		Map requestHeader = new HashMap();
		Map attributeMap = new HashMap();
		attributeMap.put("randomStr", in_parameter.get("randomStr").toString());
		attributeMap.put("fpdm", in_parameter.get("FPDM").toString());
		attributeMap.put("fpxh", in_parameter.get("FPHM").toString());
		attributeMap.put("fpmm", in_parameter.get("fpmm").toString());
		String requestMethod = paras.cy_qqfs;
		String JSESSIONID = in_parameter.get("JSESSIONID").toString();
		int i = JSESSIONID.indexOf(';');
		JSESSIONID = JSESSIONID.substring(11, i);
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.COOKIE, JSESSIONID);
		if ("post".equals(requestMethod)) {
			InputStream in = SendResultRequest.sendRequestPost(requestHeader, ipAddress, paras.cy_dz, attributeMap);
			JSONObject json = parseInvoiceResult(in);
			List<ResultBean> list = new ArrayList<ResultBean>();
			if (("false").equals(json.get("result").toString())) {
				list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
				out_result.put(SysConfig.INVOICEFALSESTATE, json.getString(SysConfig.INVOICEFALSESTATE));
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			} else {
				JSONObject cxjg = new JSONObject(json.getString("cxjg"));
				list.add(new ResultBean("FPMC", "发票名称", cxjg.get("发票名称").toString()));
				list.add(new ResultBean("GPDW", "购票单位", cxjg.get("购票单位（购票人）名称").toString()));
				list.add(new ResultBean("GMRQ", "购买日期", cxjg.get("购买日期").toString()));
				list.add(new ResultBean("ZGSWJGMC", "主管税务机关名称", cxjg.get("主管税务机关名称").toString()));
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
			}
			out_result.put("list", list);
		}
	}

	/**
	 * 宁波地税发票解析
	 */
	public static JSONObject parseInvoiceResult(InputStream in) throws Exception {
		JSONObject jso = new JSONObject();
		JSONObject json = new JSONObject();
		json.put("result", "false");
		try {
			Document doc = (Document) SendResultRequest.iSToJSONOrDocument(in, "GBK", "text");
			String result = doc.toString();
			System.out.println(doc);
			if (in != null) {
				String[] result_array = { "fpmc", "gpdw", "kprq", "swjg" };
				if (result.contains("系统中没有该发票的销售信息")) {
					json.put("result", "false");
					json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE201);
					json.put("cwxx", "系统中没有该发票的销售信息");

				} else if (result.contains("查询过于频繁，请稍后查询")) {
					json.put("result", "false");
					json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE202);
					json.put("cwxx", "查询过于频繁，请稍后查询");

				} else if (result.contains("该发票密码为空")) {
					json.put("result", "false");
					json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE211);
					json.put("cwxx", "该发票密码为空");
				} else {
					Elements els = doc.select("table#table2").select("tbody").select("tr").select("td");
					int i = 0;
					int j = 0;
					while (i < els.size()) {
						if (els.get(i).attr("class").compareTo("blue") == 0) {
							jso.put(result_array[j++], els.get(i + 1).text());
							i += 2;
						} else
							i += 1;
					}
					els = doc.select("table#table2").select("td.red");
					for (Element e : els) {
						if (e.attr("width").compareTo("717") == 0 && e.attr("colspan").compareTo("2") == 0) {
							if (e.text().contains("验证码")) {
								json.put("error", "");
								json.put("cxjg", "");
							} else {
								json.put("error", "");
								json.put("cxjg", "");
							}
							return json;
						}
					}
					json.put("result", "true");
					json.put(
							"cxjg",
							"{\"发票名称\":\"" + jso.getString("fpmc") + "\",\"购票单位（购票人）名称\":\"" + jso.getString("gpdw")
									+ "\",\"购买日期\":\"" + jso.getString("kprq") + "\",\"主管税务机关名称\":\""
									+ jso.getString("swjg") + "\"}");
				}
				return json;
			} else {
				json.put("cwxx", "系统中没有该发票的销售信息!");
				json.put("result", "false");
			}
		} catch (Exception e) {
			json.put("cwxx", "系统中没有该发票的销售信息!");
			json.put("result", "false");
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception e) {
				System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
				json.put("cwxx", "系统中没有该发票的销售信息!");
				json.put("result", "false");
			}
		}
		return json;
	}

	/**
	 * 宁波地税BASE64编码
	 * 
	 * @return
	 */
	public static String getBASE64() {
		String s = createCode();
		s = (new sun.misc.BASE64Encoder()).encode(s.getBytes());
		s = s.replace("=", "%3D");
		return s;
	}

	public static String createCode() {
		String code = "";
		String[] random = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "g", "h",
				"i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z" };
		for (int i = 0; i < 4; i++) {// 循环操作
			int index = (int) Math.floor(Math.random() * 36); // 取得随机数的索引（0~35）
			code += random[index];// 根据索引取得随机数加到code上
		}
		return code;
	}
}