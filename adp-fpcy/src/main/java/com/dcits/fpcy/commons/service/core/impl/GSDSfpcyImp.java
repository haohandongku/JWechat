package com.dcits.fpcy.commons.service.core.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.dcits.fpcy.commons.bean.HSCEntity;
import com.dcits.fpcy.commons.bean.IpAddress;
import com.dcits.fpcy.commons.bean.ResultBean;
import com.dcits.fpcy.commons.bean.TaxOfficeBean;
import com.dcits.fpcy.commons.constant.HeaderType;
import com.dcits.fpcy.commons.constant.SysConfig;
import com.dcits.fpcy.commons.service.core.InvoiceServerBase;
import com.dcits.fpcy.commons.utils.ResultUtils;
import com.dcits.fpcy.commons.utils.SendResultRequest;

public class GSDSfpcyImp implements InvoiceServerBase {

	/**
	 * 甘肃地税发票查验
	 * 
	 * @param parameter
	 * @param regionNum
	 * @return
	 */
	private Log logger = LogFactory.getLog(GSDSfpcyImp.class);

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Object> FPCY(Map parameter, TaxOfficeBean fpcyParas){

		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			parameter.put("checkcode", String.valueOf(hscEntity.getYzm()));
			parameter.put("JSESSIONID", hscEntity.getCookie());
			// 开始查验
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("甘肃地税,获取cook时出现异常", e);
			List list = new ArrayList<ResultBean>();
			list.clear();
			list.add(new ResultBean("cwxx", "", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！"));
			result.put("list", list);
			result.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE103);
			result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		}
		return result;
	}

	// 获取验证码验真结果
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result, IpAddress ipAddress) throws Exception {
		Map<String, String> requestHeader = new HashMap<String, String>();
		requestHeader.put(HeaderType.COOKIE, in_parameter.get("JSESSIONID").toString());
		InputStream in = SendResultRequest.sendRequestPost(requestHeader, ipAddress, paras.cy_dz, in_parameter);
		JSONObject json = parseInvoiceResult(in);
		List<ResultBean> list = new ArrayList<ResultBean>();
		try {
			if (json == null) {
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			}
			if (json.get("result").toString().equals("false")) {
				list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
				out_result.put(SysConfig.INVOICEFALSESTATE, json.getString(SysConfig.INVOICEFALSESTATE));
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			} else {
				String key[] = { "发票种类", "发票代码", "发票号码", "发票金额", "付款方名称", "收款方名称", "开票日期", "发票状态", "备注" };
				list = ResultUtils.getListInfoFromJson(key, json);
				list.add(new ResultBean("CXJG", "查询结果", "查询结果"));
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		out_result.put("list", list);
	}

	/**
	 * 甘肃地税解析结果
	 */
	public JSONObject parseInvoiceResult(InputStream in) {

		JSONObject json = new JSONObject();
		try {
			json.put("result", "false");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		Document doc = (Document) SendResultRequest.iSToJSONOrDocument(in, "utf-8", "text");
		String html = doc.toString();
		try {
			if (in != null) {
				if (html.contains("该发票在系统中不存在")) {
					json.put("result", "false");
					json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE201);
					json.put("cwxx", "该发票在系统中不存在！");
					return json;
				} else if (html.contains("发票查询结果")) {
					Elements jg = doc.select("#rfkf");
					json.put("FPZL", doc.getElementById("rcategory").text());
					json.put("FPDM", doc.getElementById("rfpdm").text());
					json.put("FPHM", doc.getElementById("rfphm").text());
					json.put("FPJE", doc.getElementById("rfpje").text());
					json.put("FKFMC", jg.get(0).text());
					json.put("SKFMC", doc.getElementById("rskf").text());
					json.put("KPRQ", doc.getElementById("rkprq").text());
					json.put("FPZT", doc.getElementById("rfpzt").text());
					json.put("BZ", jg.get(1).text());
					json.put("result", "true");
					return json;
				} else {
					json.put("cwxx", "发票查询失败，请稍候重试！");
				}
			}
		} catch (Exception e) {
			System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
			e.printStackTrace();
			try {
				json.put("cwxx", "查询发生错误");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			return json;
		}
		return json;
	}
}
