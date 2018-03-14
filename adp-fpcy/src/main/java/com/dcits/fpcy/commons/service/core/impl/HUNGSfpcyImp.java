package com.dcits.fpcy.commons.service.core.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
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
import com.dcits.fpcy.commons.constant.SysConfig;
import com.dcits.fpcy.commons.service.core.InvoiceServerBase;
import com.dcits.fpcy.commons.utils.SendResultRequest;
/**
 * 湖南国税 14300
 * 
 * 2017-04-11
 * 
 */
public class HUNGSfpcyImp implements InvoiceServerBase {
	private Log logger = LogFactory.getLog(HUNGSfpcyImp.class);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity  hscEntity=null;
		try {
			hscEntity=(HSCEntity) parameter.get("hscEntity");
			// 开始查验
			parameter.put("rand", hscEntity.getYzm().toLowerCase());
			if(StringUtils.isEmpty((String)hscEntity.getCookie1())) {
				parameter.put("JSESSIONID", hscEntity.getCookie());
			}else{
				parameter.put("JSESSIONID", hscEntity.getCookie1());
			}
			IpAddress ip = new IpAddress("58.53.128.130", 1111);
			getResult(fpcyParas, parameter, result, ip);
		} catch (Exception e) {
			logger.error("湖南国税,获取cook时出现异常", e);
			result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		} 
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result,
			IpAddress ipAddress) throws Exception {

		// 获取验证码
		Map requestHeader = new HashMap();
		if(in_parameter.get("JSESSIONID").toString().contains("Cookie")) {
			String[] cookie = in_parameter.get("JSESSIONID").toString().split("; ");
			requestHeader.put("Cookie",cookie[2].toString().split(";")[1]+"; "+cookie[0]);
		}else{
			String cookie = in_parameter.get("JSESSIONID").toString();
		}
		
		// 渲染
		Map<String, String> attributeMap0 = new HashMap<String, String>();
		attributeMap0.put("fpdm", in_parameter.get("fpdm").toString());
		InputStream in0 = SendResultRequest.sendRequestIn(requestHeader, null,
				attributeMap0, "http://fpbw.hntax.gov.cn/fpbw/pc/fpsjly",
				"POST");
		JSONObject json0 = parseInvoiceResult0(in0);

		// 查询流向校验http://fpbw.hntax.gov.cn/fpbw/pc/fplxjy
		Map<String, String> attributeMap1 = new HashMap<String, String>();
		attributeMap1.put("sjly", json0.getString("sjly"));
		attributeMap1.put("fpdm", in_parameter.get("fpdm").toString());
		attributeMap1.put("fphm", in_parameter.get("fphm").toString());
		attributeMap1.put("fpjym", in_parameter.get("fpjym").toString());
		attributeMap1.put("yzm", in_parameter.get("rand").toString().toLowerCase());
		InputStream in1 = SendResultRequest.sendRequestPost(requestHeader, null, "http://fpbw.hntax.gov.cn/fpbw/pc/fplxjy", attributeMap1);
		JSONObject json1 = parseInvoiceResult0(in1);
		
		// 查询流向http://fpbw.hntax.gov.cn/fpbw/pc/fpsjly
		
		Map<String, String> attributeMap2 = new HashMap<String, String>();
		attributeMap2.put("sjly", json0.getString("sjly"));
		attributeMap2.put("errorMsg", "");
		attributeMap2.put("fpdm", in_parameter.get("fpdm").toString());
		attributeMap2.put("fphm", in_parameter.get("fphm").toString());
		
		attributeMap2.put("fpjym", in_parameter.get("fpjym").toString());
		attributeMap2.put("yzm", in_parameter.get("rand").toString().toLowerCase());
		InputStream in2 = SendResultRequest.sendRequestPost(requestHeader, null, "http://fpbw.hntax.gov.cn/fpbw/pc/fpmxView", attributeMap2);
		JSONObject json2 = parseInvoiceResult2(in2);
		
		// 查询详情校验http://fpbw.hntax.gov.cn/fpbw/pc/fpmxjy
		Map<String, String> attributeMap3 = new HashMap<String, String>();
		attributeMap3.put("sjly", json0.getString("sjly"));
		attributeMap3.put("validateMsg", in_parameter.get("validateMsg")
				.toString() + ".00");
		attributeMap3.put("fpdm", in_parameter.get("fpdm").toString());
		attributeMap3.put("fphm", in_parameter.get("fphm").toString());
		InputStream in3 = SendResultRequest.sendRequestIn(requestHeader, null,
				attributeMap3, "http://fpbw.hntax.gov.cn/fpbw/pc/fpmxjy",
				"POST");

		// 查询详情http://fpbw.hntax.gov.cn/fpbw/pc/fpmxFinal
		Map<String, String> attributeMap4 = new HashMap<String, String>();
		attributeMap4.put("sjly", json0.getString("sjly"));
		attributeMap4.put("validateMsg", in_parameter.get("validateMsg").toString()
				+ ".00");
		attributeMap4.put("fpdm", in_parameter.get("fpdm").toString());
		attributeMap4.put("fphm", in_parameter.get("fphm").toString());
		attributeMap4.put("avoid_submit", "");
		attributeMap4.put("skm", "");
		attributeMap4.put("kjrq", "");
		attributeMap4.put("je", in_parameter.get("validateMsg").toString());
		InputStream in4 = SendResultRequest.sendRequestIn(requestHeader, ipAddress,
				attributeMap4, "http://fpbw.hntax.gov.cn/fpbw/pc/fpmxFinal",
				"POST");
		JSONObject json = parseInvoiceResult1(in4);

		List<ResultBean> list = new ArrayList<ResultBean>();
		if (json.get("result").toString().equals("false")) {
			list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
			out_result.put(SysConfig.INVOICEFALSESTATE,
					json.getString(SysConfig.INVOICEFALSESTATE));
			out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		} else {
			String key[] = { "发票代码", "发票号码", "（购货方）纳税人识别号", "（购货方）纳税人名称",
					"（销货方）纳税人识别号", "（销货方）纳税人名称", "品目", "开票日期", "金额", "税额" };
			for (int i = 0; i < key.length; i++) {
				if (StringUtils.isNotBlank(key[i])) {
					list.add(new ResultBean(key[i].toUpperCase(), key[i], json
							.getString(key[i])));
				}
			}
			out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
		}
		out_result.put(SysConfig.INVOICEFALSESTATE,
				json.get(SysConfig.INVOICEFALSESTATE).toString());
		out_result.put("list", list);
	}

	public JSONObject parseInvoiceResult0(InputStream in) throws Exception {
		JSONObject document = (JSONObject) SendResultRequest
				.iSToJSONOrDocument(in, "utf-8", "json");
		System.out.println(document.toString());
		return document;
	}
	
	@SuppressWarnings("unused")
	public JSONObject parseInvoiceResult2(InputStream in) throws Exception {
		 Document document = (Document) SendResultRequest
				.iSToJSONOrDocument(in, "utf-8", "text");
		return null;
	}

	public JSONObject parseInvoiceResult1(InputStream in) throws Exception {
		JSONObject json = new JSONObject();
		json.put("result", "false");
		if(in==null){
			json.put("cwxx", "发票查询异常，请重试！");
			json.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE102);
		}else{
			
		Document document = (Document) SendResultRequest.iSToJSONOrDocument(in,
				"utf-8", "text");
			Elements elementth = document.select("table[class=jieguo]")
					.select("tr").select("th");
			Elements elementtd = document.select("table[class=jieguo]")
					.select("tr").select("td");
			List<String> listth = new ArrayList<String>();
			List<String> listtd = new ArrayList<String>();
			for (Element element : elementth) {
				String el = element.text();
				listth.add(el);
			}
			for (Element element : elementtd) {
				String el = element.text();
				if (!el.contains("购货方") && !el.contains("销货方")) {
					listtd.add(el);
				}
			}
			System.out.println(listth.toString());
			System.out.println(listtd.toString());

			for (int i = 0; i < listtd.size(); i++) {
				if (i == 2 || i == 3) {
					json.put("（购货方）" + listth.get(i), listtd.get(i));
				} else if (i == 4 || i == 5) {
					json.put("（销货方）" + listth.get(i), listtd.get(i));
				} else {
					json.put(listth.get(i), listtd.get(i));
				}
			}
			json.put("result", "true");
			json.put(SysConfig.INVOICEFALSESTATE, "000");
		}
		
		return json;
	}
}
