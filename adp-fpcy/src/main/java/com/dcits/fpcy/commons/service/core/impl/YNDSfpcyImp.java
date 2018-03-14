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

public class YNDSfpcyImp implements InvoiceServerBase {
	private Log logger = LogFactory.getLog(YNDSfpcyImp.class);

	/**
	 * 云南地税发票查验
	 * 
	 * @param parameter
	 * @param regionNum
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");

			parameter.put("JSESSIONID", hscEntity.getCookie1());
			parameter.put("rand", hscEntity.getYzm());
			// 开始查验
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("云南地税发票查验:" + e);
			List<ResultBean> list = new ArrayList<ResultBean>();
			list.clear();
			list.add(new ResultBean("cwxx", "", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！"));
			result.put("list", list);
			result.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE103);
			result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		}
		return result;
	}

	public void getResult(TaxOfficeBean paras, Map<String, Object> in_Parameter, Map<String, Object> out_Result,
			IpAddress ipAddress) throws JSONException {

		Map<String, String> attributeMap = new HashMap<String, String>();
		Map<String, String> requestHeader = new HashMap<String, String>();
		attributeMap.put("rootVo", "true");
		attributeMap.put("rootVo@sid", "WB_QUERY_FPZWCX");
		attributeMap.put("rootVo.properties*fplx", "02");
		attributeMap.put("rootVo.properties*fpdm", in_Parameter.get("FPDM").toString());
		attributeMap.put("rootVo.properties*fphm", in_Parameter.get("FPHM").toString());
		attributeMap.put("rootVo.properties*fpje", "");
		attributeMap.put("rootVo.properties*yzm", in_Parameter.get("rand").toString());
		String attributeString = structureAttribute(attributeMap);
		attributeMap.put("jsxml", attributeString);

		String JSESSIONID = in_Parameter.get("JSESSIONID").toString();
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.COOKIE, JSESSIONID);
		InputStream in = SendResultRequest.sendRequestPost(requestHeader, ipAddress, paras.cy_dz, attributeMap);
		JSONObject json = parseInvoiceResult(in);
		// 新的返回方式
		List<ResultBean> list = new ArrayList<ResultBean>();
		try {
			if ("false".equals(json.get("result"))) {
				list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
				out_Result.put(SysConfig.INVOICEFALSESTATE, json.getString(SysConfig.INVOICEFALSESTATE));
				out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			} else {
				list.add(new ResultBean("FPDM", "发票代码", in_Parameter.get("FPDM").toString()));
				list.add(new ResultBean("FPHM", "发票号码", in_Parameter.get("FPHM").toString()));
				// list.add(new ResultBean("KPFMC",
				// "开票方名称",json.getString("KPFMC")));
				list.add(new ResultBean("SPFMC", "收票方名称", json.getString("SPFMC")));
				out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
			}

		} catch (Exception e) {
			logger.error(paras.swjg_mc + "解析返回参数异常", e);
			out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			out_Result.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE104);
			e.printStackTrace();
		}
		out_Result.put("list", list);
	}

	public JSONObject parseInvoiceResult(InputStream in) throws JSONException {
		JSONObject jso = new JSONObject();
		if (in == null) {
			jso.put("cwxx", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！");
			jso.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE102);
			return jso;
		}
		Document doc = (Document) SendResultRequest.iSToJSONOrDocument(in, "GBK", "text");
		String result = doc.toString();
		try {
			if (result.contains("查询不到该发票的信息")) {
				jso.put("result", "false");
				jso.put("cwxx", "查询不到该发票的信息。请认真核对输入的信息是否正确，如无误，请拨打0871-63647321进一步核实，或点击投诉举报按钮进行网上投诉举报。");
				jso.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE211);
				return jso;
			}
			if (result.contains("验证码错误")) {
				jso.put("result", "false");
				jso.put("cwxx", "很抱歉，系统出错，请您重新进行一次查询！");
				jso.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE101);
				return jso;
			}
			Elements eles = doc.select("font");
			for (Element e : eles) {
				if (e.attr("color").compareTo("green") == 0 || e.attr("color").compareTo("red") == 0) {
					jso = new JSONObject();
					if (e.attr("color").compareTo("green") == 0) {
						Elements elements = e.select("b");
						jso.put("KPFMC", elements.get(0).text().toString());
						jso.put("SPFMC", elements.get(1).text().toString());
						jso.put("result", "true");
						return jso;
					} else {
						jso.put("result", "false");
						jso.put("cwxx", "查验失败");
						jso.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE111);
						return jso;
					}
				}
			}

		} catch (Exception e) {
			jso.put("result", "false");
			jso.put("cwxx", "查验失败");
			jso.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE111);
			return jso;
		}
		return jso;
	}

	private String structureAttribute(Map<String, String> attributeMap) {
		String attributeString = "<?xml version=\"1.0\" encoding=\"GBK\"?><rootVo sid=\"WB_QUERY_FPZWCX\" ><head></head><properties><cell name=\"fpdm\" value=\""
				+ attributeMap.get("rootVo.properties*fpdm")
				+ "\" /><cell name=\"fphm\" value=\""
				+ attributeMap.get("rootVo.properties*fphm")
				+ "\" /><cell name=\"fpje\" value=\"\" /><cell name=\"fplx\" value=\"02\" /><cell name=\"yzm\" value=\""
				+ attributeMap.get("rootVo.properties*yzm") + "\" /></properties></rootVo>";
		return attributeString;
	}
}
