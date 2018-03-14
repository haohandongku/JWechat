package com.dcits.fpcy.commons.service.core.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.dcits.fpcy.commons.bean.HSCEntity;
import com.dcits.fpcy.commons.bean.IpAddress;
import com.dcits.fpcy.commons.bean.ResultBean;
import com.dcits.fpcy.commons.bean.TaxOfficeBean;
import com.dcits.fpcy.commons.constant.BrowerType;
import com.dcits.fpcy.commons.constant.HeaderType;
import com.dcits.fpcy.commons.constant.SysConfig;
import com.dcits.fpcy.commons.service.core.InvoiceServerBase;
import com.dcits.fpcy.commons.utils.SendResultRequest;

/**
 * 河南国税:14100
 * 
 * 2017-04-11
 * 
 */
public class HeNGSfpcyImp implements InvoiceServerBase {
	private Log logger = LogFactory.getLog(HeNGSfpcyImp.class);

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			parameter.put("rand", hscEntity.getYzm());
			parameter.put("JSESSIONID", hscEntity.getCookie1());
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("河南国税,获取cook时出现异常", e);
			List<ResultBean> list = new ArrayList<ResultBean>();
			list.clear();
			list.add(new ResultBean("cwxx", "", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！"));
			result.put("list", list);
			result.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE103);
			result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		}
		return result;
	}

	@SuppressWarnings({ "rawtypes" })
	public Map FPCY(Map parameter) throws Exception {
		return null;
	}

	public void getResult(TaxOfficeBean paras, Map<String, Object> in_Parameter, Map<String, Object> out_Result,
			IpAddress ipAddress) throws Exception {
		Map<String, String> attributeMap = new HashMap<String, String>();
		Map<String, String> requestHeader = new HashMap<String, String>();
		attributeMap.put("ACTION", "getFpzw");
		attributeMap.put("FPDM", in_Parameter.get("FPDM").toString());
		attributeMap.put("FPHM", in_Parameter.get("FPHM").toString());
		attributeMap.put("NSRSBH", in_Parameter.get("NSRSBH").toString());
		attributeMap.put("rcode", in_Parameter.get("rand").toString());
		String JSESSIONID = in_Parameter.get("JSESSIONID").toString();
		requestHeader.put(HeaderType.REFERER, "http://bsfwt.12366.ha.cn/bsfwt/wsbsfwt/sscx/fpzwcx/fpzwcx.html");
		requestHeader.put("X-Requested-With", "XMLHttpRequest");
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.COOKIE, JSESSIONID);
		InputStream in = SendResultRequest.sendRequestPost(requestHeader, ipAddress, paras.cy_dz, attributeMap);
		JSONObject json = parseInvoiceResult(in);
		List<ResultBean> list = new ArrayList<ResultBean>();
		try {
			if ("false".equals(json.get("result"))) {
				list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
				out_Result.put(SysConfig.INVOICEFALSESTATE, json.getString(SysConfig.INVOICEFALSESTATE));
				out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			} else {
				list.add(new ResultBean("NSRSBH", "纳税人识别号", json.getString("NSRSBH")));
				list.add(new ResultBean("NSRMC", "纳税人名称", json.getString("NSRMC")));
				list.add(new ResultBean("FPMC", "发票名称", json.getString("FPMC")));
				list.add(new ResultBean("NSRZT", "纳税人状态", json.getString("NSRZT")));
				list.add(new ResultBean("CXJG", "查询结果", "查询成功"));
				out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
			}
		} catch (Exception e) {
			logger.error("河南国税解析JSONObject异常：" + e);
			logger.error(paras.swjg_mc + "解析返回参数异常", e);
			out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			out_Result.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE104);
			e.printStackTrace();
		}
		out_Result.put("list", list);
	}

	public JSONObject parseInvoiceResult(InputStream in) {
		JSONObject jso = null;
		JSONObject json = new JSONObject();
		try {
			json.put("result", "false");
			if (in == null) {
				json.put("cwxx", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！");
				json.put(SysConfig.INVOICEFALSESTATE, "102");
				return json;
			}
		} catch (JSONException e2) {

			e2.printStackTrace();
		}
		JSONObject result = (JSONObject) SendResultRequest.iSToJSONOrDocument(in, "UTF-8", "json");
		try {
			jso = new JSONObject();
			if (result.getString("code") == "-1") {
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE203);
				json.put("cwxx", "请将表单填写正确完整！");
				return json;
			} else if (result.getString("code") == "-4") {
				json.put("cwxx", "验证码无效!");
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE101);
				return json;
			} else {
				jso = result.getJSONObject("data");
				if (jso.getString("code").equals("0")) {
					JSONArray ja = jso.getJSONArray("rtn");
					JSONObject myjso = ja.getJSONObject(0);
					json.put("FPDM", myjso.getString("FP_DM"));
					json.put("FPHM", myjso.getString("FPHM"));
					json.put("NSRSBH", myjso.getString("NSRSBH"));
					json.put("NSRMC", myjso.getString("NSRMC"));
					json.put("FPMC", myjso.getString("FPZL_MC"));
					json.put("NSRZT", myjso.getString("NSRZT_MC"));
					json.put("result", "true");
				} else if (jso.getString("code").equals("4")) {
					json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE201);
					json.put("cwxx", "无此发票信息，请到国税机关核实！");
				}
				return json;
			}

		} catch (Exception e) {
			System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
			try {
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE111);
				json.put("cwxx", "您输入的发票信息不存在！");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			return json;
		}

	}
}
