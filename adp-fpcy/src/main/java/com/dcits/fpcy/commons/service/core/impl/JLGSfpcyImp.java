package com.dcits.fpcy.commons.service.core.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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
import com.dcits.fpcy.commons.utils.ResultUtils;
import com.dcits.fpcy.commons.utils.SendResultRequest;

public class JLGSfpcyImp implements InvoiceServerBase {

	private Log logger = LogFactory.getLog(JLGSfpcyImp.class);

	/**
	 *  吉林国税12200
	 * 
	 * 2017-04-11
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			if (StringUtils.isEmpty((String) hscEntity.getCookie1())) {
				parameter.put("JSESSIONID", hscEntity.getCookie());
			} else {
				parameter.put("JSESSIONID", hscEntity.getCookie1());
			}
			parameter.put("rand", hscEntity.getYzm().toLowerCase());
			// 开始查验
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("吉林国税,获取cook时出现异常", e);
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
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result, IpAddress ipAddress) throws Exception {
		Map<String, String> attributeMap = new HashMap<String, String>();
		Map requestHeader = new HashMap();
		attributeMap.put("err_txt", "");
		attributeMap.put("fpdm", in_parameter.get("FPDM").toString());
		attributeMap.put("fphm", in_parameter.get("FPHM").toString());
		attributeMap.put("yzm", in_parameter.get("rand").toString());
		String requestMethod = paras.cy_qqfs;
		String JSESSIONID = in_parameter.get("JSESSIONID").toString();
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.COOKIE, JSESSIONID);
		if (in_parameter.get("fpdm").toString().substring(0, 4).equals("2200")) {
			attributeMap.put("url", "wlfp/searchGT.do");
			attributeMap.put("cxfsval", "");
			attributeMap.put("cxfs", "0");
		} else {
			attributeMap.put("JSESSIONID", JSESSIONID);
		}

		String fpzl = in_parameter.get("FPDM").toString().substring(7, 8);
		if ("post".equals(requestMethod)) {
			InputStream in = SendResultRequest.sendRequestPost(requestHeader, ipAddress, paras.cy_dz, attributeMap);
			JSONObject json = parseInvoiceResult(in, fpzl);
			List<ResultBean> list = new ArrayList<ResultBean>();
			String[] key = { "发票代码", "发票号码", "纳税人识别号", "社会信用代码（新税号）", "纳税人名称", "领购日期" };
			try {
				if ((fpzl.equals("1") || fpzl.equals("5")) && json.getString("result").equals("true")) {
					list = ResultUtils.getListInfoFromJson1(key, json);
					out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
				} else if (json.getString("result").equals("true")) {
					list.add(new ResultBean("FPDM", "发票代码", json.getString("发票代码")));
					list.add(new ResultBean("FPHM", "发票号码", json.getString("发票号码")));
					list.add(new ResultBean("LGRQ", "领购日期", json.getString("领购日期")));
					list.add(new ResultBean("NSRMC", "纳税人名称", json.getString("纳税人名称")));
					list.add(new ResultBean("NSRSBH", "纳税人识别号", json.getString("纳税人识别号")));
					out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
				} else {
					list.add(new ResultBean("", "cwxx", json.getString("cwxx")));
					out_result.put(SysConfig.INVOICEFALSESTATE, json.getString(SysConfig.INVOICEFALSESTATE));
					out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
				}
			} catch (Exception e) {
				logger.error("吉林国税解析JSONObject异常：" + e);
				logger.error(paras.swjg_mc + "解析返回参数异常", e);
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
				out_result.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE104);
				e.printStackTrace();
			}
			out_result.put("list", list);
		}
	}

	@SuppressWarnings("unused")
	public JSONObject parseInvoiceResult(InputStream in, String fpzl) throws Exception {
		JSONObject jso = null;
		JSONObject json = new JSONObject();
		json.put("result", "false");
		if (in == null) {
			json.put("cwxx", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！");
			json.put(SysConfig.INVOICEFALSESTATE, "102");
			return json;
		}
		Document doc = (Document) SendResultRequest.iSToJSONOrDocument(in, "GBK", "");
		String result = doc.toString();
		try {
			jso = new JSONObject();
			if (result.contains("验证码")) {
				json.put("cwxx", "发票查询失败，请重试！");
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE101);
				return json;
			} else if (result.contains("发票查询失败")) {
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE213);
				json.put("cwxx", "请确定是否输入有误！若输入无误，则此票疑为假票");
				return json;
			} else if (result.equals("")) {
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE213);
				json.put("cwxx", "系统繁忙，请稍后再试！");
				return json;
			} else {
				json.put("result", "true");

				String[] key1 = { "发票代码", "发票号码", "纳税人识别号", "社会信用代码（新税号）", "纳税人名称", "领购日期" };
				String[] key = { "发票代码", "发票号码", "纳税人识别号", "纳税人名称", "领购日期" };
				Elements td = doc.select("td");
				int i = 0;
				boolean b = false;
				/*
				 * 查询结果： 发票代码 122011577103 发票号码 03372159 纳税人识别号 22010567732607X
				 * 纳税人名称 中国电信股份有限公司长春分公司 领购日期 2015-08-18
				 */
				if (fpzl.equals("1") || fpzl.equals("5")) {
					for (Element e : td) {
						if (b) {
							json.put(key1[i], e.text().toString());
							b = false;
							i++;
						}
						if (i == 6)
							break;
						if (e.text().toString().equals(key1[i])) {
							b = true;
						}
					}
				} else {
					for (Element e : td) {
						if (b) {
							json.put(key[i], e.text().toString());
							b = false;
							i++;
						}
						if (i == 5)
							break;
						if (e.text().toString().equals(key[i])) {
							b = true;
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("吉林国税获取解析异常", e);
			json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE111);
			json.put("cwxx", "此种票样查询发生异常，请稍后重试");
		}
		return json;
	}
}
