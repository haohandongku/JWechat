package com.dcits.fpcy.commons.service.core.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

public class SXGSfpcyImp implements InvoiceServerBase {

	private Log logger = LogFactory.getLog(SXGSfpcyImp.class);

	/**
	 * 陕西国税发票查验
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
			parameter.put("rand", hscEntity.getYzm());
			parameter.put("JSESSIONID", hscEntity.getCookie1()); // 开始查验
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("陕西地税,获取cook时出现异常", e);
			List list = new ArrayList<ResultBean>();
			list.clear();
			list.add(new ResultBean("cwxx", "", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！"));
			result.put("list", list);
			result.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE103);
			result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		}
		return result;
	}

	public void getResult(TaxOfficeBean paras, Map<String, Object> in_Parameter, Map<String, Object> out_Result,
			IpAddress ipAddress) throws Exception {

		Map<String, String> attributeMap = new HashMap<String, String>();
		Map<String, String> requestHeader = new HashMap<String, String>();
		attributeMap.put("service", "S_WLFPCY");
		attributeMap.put("serviceMethod", "doService");
		JSONObject parameters = new JSONObject();
		parameters.put("fp_dm", in_Parameter.get("FPDM").toString());
		parameters.put("fphm", in_Parameter.get("FPHM").toString());
		parameters.put("cxh", in_Parameter.get("cxh").toString());
		parameters.put("rand", in_Parameter.get("rand").toString());
		parameters.put("ip", "92.16.202.23");
		parameters.put("pageNo", "1");
		parameters.put("pageSize", "20");
		parameters.put("dealMethod", "deService");
		attributeMap.put("parameters", parameters.toString());
		String JSESSIONID = in_Parameter.get("JSESSIONID").toString();
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.COOKIE, JSESSIONID);
		requestHeader.put(HeaderType.REFERER, "http://219.144.206.60:8000/sxgs-fpdj-front/main/fpcx_ce");
		requestHeader.put(HeaderType.HOST, "219.144.206.60:8000");
		requestHeader.put(HeaderType.ACCEPT, "text/xml; charset=utf-8");
		requestHeader.put("X-Requested-With", "XMLHttpRequest");
		requestHeader.put(HeaderType.CONNECTION, "keep-alive");
		String yzdz = paras.cy_dz + "?requestXml=<body><fpdm>" + in_Parameter.get("FPDM").toString() + "</fpdm><fphm>"
				+ in_Parameter.get("FPHM").toString() + "</fphm><fpcxm>" + in_Parameter.get("cxh").toString()
				+ "</fpcxm><yzm>" + in_Parameter.get("rand").toString() + "</yzm><jrlx></jrlx></body>";
		InputStream in = SendResultRequest.sendRequestPost(requestHeader, ipAddress, yzdz, attributeMap);
		JSONObject json = parseInvoiceResult(in);
		List<ResultBean> list = new ArrayList<ResultBean>();
		try {
			if ("false".equals(json.get("result"))) {
				list.add(new ResultBean("cwxx", "查询结果", json.get("cwxx").toString()));
				out_Result.put(SysConfig.INVOICEFALSESTATE, json.getString(SysConfig.INVOICEFALSESTATE));
				out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			} else {
				list.add(new ResultBean("FPDM", "发票代码", json.getString("fpdm")));
				list.add(new ResultBean("FPHM", "发票号码", json.getString("fphm")));
				list.add(new ResultBean("CXH", "查询号", json.getString("fpcxm")));
				list.add(new ResultBean("KPRQ", "开票日期", json.getString("kprq")));
				list.add(new ResultBean("HJJE", "开票金额", json.getString("kjje")));
				list.add(new ResultBean("SKFMC", "受票方名称", json.getString("sprdwmc")));
				list.add(new ResultBean("KPFMC", "开票方名称", json.getString("kpfdwmc")));
				list.add(new ResultBean("KPFSBH", "开票方识别号", json.getString("kpfnsrsbh")));
				list.add(new ResultBean("KPR", "受票方识别号", json.getString("spfnsrsbh")));
				list.add(new ResultBean("CXJG", "查询结果", json.get("cwxx").toString()));
				out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
			}
		} catch (Exception e) {
			logger.error("陕西国税解析JSONObject异常：" + e);
			logger.error(paras.swjg_mc + "解析返回参数异常", e);
			out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			out_Result.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE104);
			e.printStackTrace();
		}
		out_Result.put("list", list);
	}

	public JSONObject parseInvoiceResult(InputStream in) throws Exception {

		JSONObject json = new JSONObject();
		json.put("result", "false");
		String html = "";
		if (in == null) {
			json.put("cwxx", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！");
			json.put(SysConfig.INVOICEFALSESTATE, "102");
			return json;
		}
		JSONObject doc = (JSONObject) SendResultRequest.iSToXMLDocument(in, "UTF-8", "XML");
		html = doc.toString();
		try {// 查询号无效
			if (html.contains("查无此票，请核对并向税务机关报告")) {
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE211);
				json.put("cwxx", "查无此票，请核对并向税务机关报告。");
				return json;
			}
			if (html.contains("查询号无效")) {
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE211);
				json.put("cwxx", "查询号无效。");
				return json;
			}
			if (html.contains("校验码不符")) {
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE211);
				json.put("cwxx", "校验码不符！</br>请仔细核对后，再进行查询。");
				return json;
			}
			if (html.contains("item_1")) {
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE211);
				json.put("cwxx", "对不起，您查询的发票信息不存在！ 请仔细核对后，再进行查询。");
				return json;
			}
			if (html.contains("您查询的发票信息不存在")) {
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE211);
				json.put("cwxx", "对不起，您查询的发票信息不存在！ 请仔细核对后，再进行查询。");
				return json;
			} else {
				JSONObject result = new JSONObject(html);
				result = result.getJSONObject("root");
				result = result.getJSONObject("body");
				json.put("fpdm", result.get("fpdm"));
				json.put("fphm", result.get("fphm"));
				json.put("fpcxm", result.get("fpcxm"));
				json.put("sprdwmc", result.get("sprdwmc"));
				json.put("spfnsrsbh", result.get("spfnsrsbh"));
				json.put("kjje", result.get("kjje"));
				json.put("kpfnsrsbh", result.get("kpfnsrsbh"));
				json.put("kpfdwmc", result.get("kpfdwmc"));
				json.put("kprq", result.get("kprq"));
				json.put("result", "true");
				json.put("cwxx", "查验成功");
				return json;
			}
		} catch (Exception e) {
			logger.error("陕西国税解析错误", e);
			json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE111);
			json.put("cwxx", "对不起，您查询的发票信息不存在！ 请仔细核对后，再进行查询。");
		}
		return json;
	}
}
