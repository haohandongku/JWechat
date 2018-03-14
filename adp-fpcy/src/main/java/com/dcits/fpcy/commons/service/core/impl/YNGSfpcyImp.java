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

public class YNGSfpcyImp implements InvoiceServerBase {
	private Log logger = LogFactory.getLog(YNGSfpcyImp.class);

	/**
	 * 云南国税发票查询
	 * 
	 * @param parameter
	 * @param regionNum
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			String cookie = hscEntity.getList1().get(0).toString();
			String cookie1 = hscEntity.getList1().get(1).toString();
			cookie = cookie.substring(0, cookie.indexOf(";"));
			cookie1 = cookie1.substring(0, cookie1.indexOf(";"));
			parameter.put("rand", hscEntity.getYzm().toLowerCase());
			parameter.put("JSESSIONID", cookie + ";" + cookie1);
			// 开始查验
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("云南国税发票查询:" + e);
			List<ResultBean> list = new ArrayList<ResultBean>();
			list.add(new ResultBean("cwxx", "", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！"));
			result.put("list", list);
			result.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE103);
			result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result, IpAddress ipAddress) throws Exception {
		Map attributeMap = new HashMap();
		Map requestHeader = new HashMap();
		InputStream in = null;
		JSONObject json = null;
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.COOKIE, in_parameter.get("JSESSIONID").toString());
		requestHeader.put(HeaderType.REFERER, "http://www.yngs.gov.cn/newWeb/template/bsfw.jsp");
		requestHeader.put("Accept", "application/json, text/javascript, */*; q=0.01");
		requestHeader.put("X-Requested-With", "XMLHttpRequest");
		requestHeader.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		String target = in_parameter.get("target").toString();
		if ("tyjdfp".equals(target) || "swjg_dktyfp".equals(target)) {
			String rand = in_parameter.get("rand").toString();
			attributeMap.put("ssqx_tyjdfp_form_fphm", in_parameter.get("FPHM"));
			attributeMap.put("ssqx_tyjdfp_form_fpdm", in_parameter.get("FPDM"));
			attributeMap.put("ssqx_tyjdfp_form_xhsbh", in_parameter.get("HrAddress"));
			attributeMap.put("ssqx_tyjdfp_form_je", in_parameter.get("HrSalary"));
			attributeMap.put("kaptStr", rand);
			attributeMap.put("target", target);
			attributeMap.put("ssqx_tyjdfp_form_shxydm", "");
			attributeMap.put("ssqx_tyjdfp_form_shxydm", "");
		} else if ("jdcxsfp".equals(target)) {
			attributeMap.put("target", target);
			attributeMap.put("ssqx_fpxx_form_fphm", in_parameter.get("FPHM"));
			attributeMap.put("ssqx_fpxx_form_fpdm", in_parameter.get("FPDM"));
			attributeMap.put("ssqx_fpxx_form_xhsbh", in_parameter.get("xhsbh"));
			attributeMap.put("ssqx_fpxx_form_kprq", in_parameter.get("kprq"));
			attributeMap.put("ssqx_fpxx_form_je", in_parameter.get("je"));
			attributeMap.put("ssqx_fpxx_form_se", in_parameter.get("se"));
			attributeMap.put("ssqx_fpxx_form_ghsbh", in_parameter.get("ghsbh"));
		}

		in = SendResultRequest.sendRequestPost(requestHeader, ipAddress, paras.cy_dz, attributeMap);
		json = parseInvoiceResult(in);
		// 新的参数返回方式
		List<ResultBean> list = new ArrayList<ResultBean>();

		try {
			if ("true".equals(json.get("result"))) {
				if (!json.isNull("json")) {
					json = json.getJSONObject("json");
					list.add(new ResultBean("fpdm", "发票代码", json.getString("fpdm")));
					list.add(new ResultBean("fphm", "发票号码", json.get("fphm").toString()));
					list.add(new ResultBean("cxjg", "对比结果", json.get("cxjg").toString()));
					list.add(new ResultBean("fpzt", "发票状态", json.get("fpzt").toString()));
					if ("jdcxsfp".equals(target)) {
						list.add(new ResultBean("ghfmc", "购货方名称", json.get("ghfmc").toString()));
						list.add(new ResultBean("xhfmc", "销货方名称", json.get("xhfmc").toString()));
					}
				} else {
					list.add(new ResultBean("cxjg", "查询结果", json.get("cxjg").toString()));
				}
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
			} else if ("false".equals(json.get("result").toString())) {
				list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
				out_result.put(SysConfig.INVOICEFALSESTATE, json.getString(SysConfig.INVOICEFALSESTATE));
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			} else {
				list.add(new ResultBean("cwxx", "", json.get("cxjg").toString()));
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			}
		} catch (Exception e) {
			logger.error(paras.swjg_mc + "解析返回参数异常", e);
			out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			out_result.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE104);
			e.printStackTrace();
		}
		out_result.put("list", list);
	}

	/**
	 * 云南国税查询结果解析
	 */
	public JSONObject parseInvoiceResult(InputStream in) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("result", "false");
		if (in == null) {
			json.put("cwxx", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！");
			json.put(SysConfig.INVOICEFALSESTATE, "102");
			return json;
		}
		Document doc = (Document) SendResultRequest.iSToJSONOrDocument(in, "utf-8", "text");
		String result = doc.toString();
		try {
			if (result.contains("success")) {
				String body = doc.select("body").get(0).text();
				JSONObject jso = new JSONObject(body);
				JSONArray data = (JSONArray) jso.get("data");
				jso = data.getJSONObject(0);
				String cxjg = jso.get("cxjg").toString();
				if (cxjg.contains("不符") || cxjg.contains("查询不到")) {
					// 查询不到该销货单位开具的该份发票
					json.put("cwxx", cxjg);
					json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE201);
				} else {
					json.put("json", jso);
					json.put("result", "true");
				}

			} else {
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE101);
				json.put("cwxx", "发票查询失败,请稍候重试！");
			}

		} catch (Exception e) {
			json.put("cxjg", "您输入的发票信息不匹配，请检查是否输入正确!s");
			json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE111);
			json.put("result", "false");
			e.printStackTrace();
		}
		return json;
	}
}
