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
import com.dcits.fpcy.commons.utils.ResultUtils;
import com.dcits.fpcy.commons.utils.SendResultRequest;
/**
 * 贵州国税 2017-04-12 15201 poolid:51
 * 
 * 
 */
public class GZGSfpcyImp implements InvoiceServerBase {

	private Log logger = LogFactory.getLog(GZGSfpcyImp.class);

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			Map<String, Object> map = hscEntity.getMap();
			parameter.put("cookie", hscEntity.getCookie());
			parameter.put("YZM", hscEntity.getYzm());
			parameter.put("CaptchaDeText1", map.get("CaptchaDeText1"));
			parameter.put("__RequestVerificationToken",
					map.get("__RequestVerificationToken"));
			parameter.put("_multiple_",
					map.get("1"));
			getResult(fpcyParas, parameter, result, hscEntity
					.getIpAddress());
		} catch (Exception e) {
			logger.error("贵州国税,获取cook时出现异常", e);
			List list = new ArrayList<ResultBean>();
			list.clear();
			list.add(new ResultBean("cwxx", "", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！"));
			result.put("list", list);
			result.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE103);
			result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		} 
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void getResult(TaxOfficeBean paras, Map<String, String> in_Parameter,
			Map out_Result, IpAddress ipAddress) throws JSONException {
		Map requestHeader = new HashMap();
		boolean flag = false;
		Map<String, String> attributeMap = new HashMap<String, String>();
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		String cookie1[] = in_Parameter.get("cookie").toString().split(";");
		String cookie = cookie1[3]+ ";" + cookie1[0]+";Hm_lvt_2fa03c64ddc18b0c8aa0b52e87aeda16=1502164748; Hm_lpvt_2fa03c64ddc18b0c8aa0b52e87aeda16=1502248932" ;
		requestHeader.put(HeaderType.COOKIE, cookie);
		attributeMap.put("__RequestVerificationToken",
				in_Parameter.get("__RequestVerificationToken").toString());
		attributeMap.put("InvoiceCode", in_Parameter.get("FPDM").toString());
		attributeMap.put("InvoiceNumber", in_Parameter.get("FPHM").toString());
		attributeMap.put("_multiple_", "1");
		attributeMap.put("CaptchaDeText1", in_Parameter.get("CaptchaDeText1")
				.toString());
		attributeMap.put("CaptchaInputText1", in_Parameter.get("YZM")
				.toString().toLowerCase());
		requestHeader.put(HeaderType.ACCEPTLANGUAGE, "zh-CN,zh;q=0.8");
		requestHeader.put(HeaderType.CONTENTLENGTH, "0");
		requestHeader.put(HeaderType.HOST, "yun.gzgs12366.gov.cn");
		requestHeader.put(HeaderType.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		requestHeader.put(HeaderType.REFERER,
				"http://yun.gzgs12366.gov.cn/Service/Invoice");
		flag = true;
		InputStream in = SendResultRequest.sendRequestPostToGZGS(requestHeader,
				null, paras.cy_dz, attributeMap);
		JSONObject json = parseInvoiceResult(in, flag);
		List<ResultBean> list = new ArrayList<ResultBean>();
		try {
			if ("false".equals(json.get("result"))) {
				list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
				out_Result.put(SysConfig.INVOICEFALSESTATE,json.getString(SysConfig.INVOICEFALSESTATE));
				out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			} else {
				String key[] = { "发票代码", "发票号码", "发票种类", "开票方" };
				list = ResultUtils.getListInfoFromJson(key, json);
				list.add(new ResultBean("CXJG", "查询结果", "查验成功"));
				out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
			}
		} catch (Exception e) {
			logger.error(paras.swjg_mc + "解析返回参数异常", e);
			out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			out_Result.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE104);
			e.printStackTrace();
		}
		out_Result.put("list", list);
	}

	public JSONObject parseInvoiceResult(InputStream in, boolean flag) {

		JSONObject jso = new JSONObject();
		String html = "";

		try {
			if (in == null) {
				jso.put("result", "false");
				jso.put("cwxx", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！");
				jso.put(SysConfig.INVOICEFALSESTATE, "102");
				return jso;
			}
			Document doc = (Document) SendResultRequest.iSToJSONOrDocument(in,
					"UTF-8", "text");
			html = doc.toString();
			if (html.contains("没查到该发票") || doc.select("p").get(3).toString().contains("查询失败")) {
				jso.put("result", "false");
				jso.put(SysConfig.INVOICEFALSESTATE,
						SysConfig.INVOICEFALSESTATECODE111);
				jso.put("cwxx",
						"没查到该发票,最可能的原因：(1) 发票代码、发票号码错误；(2) 不是贵州省国家税务局的发票；(3) 本系统不能查询部分代开、自印发票；(4) 后台数据还未同步；如果需要进一步核实，可致电：12366 ");
			} else if (html.contains("__RequestVerificationToken") && !flag) {
				Elements ele = doc
						.select("input[name=__RequestVerificationToken]");
				String hid = ele.val();
				jso.put("hid", hid);
			} else {
				jso.put("result", "true");
				jso.put("cwxx", "查验成功");

				Element element = doc.select("p").get(3);
				String str = element.text().toString();
				str = str.replace("： ", "：").replace(" ", "：");
				String[] string = str.split("：");
				int[] indexes = { 1, 3, 5, 7 }; // 数组里存放的是解析过后所需的组件的索引
				String[] nameOfIndexes = { "FPDM", "FPHM", "FPZL", "KPF" };
				for (int i = 0; i < indexes.length; i++) {
					jso.put(nameOfIndexes[i], string[indexes[i]]);
				}
			}
			return jso;

		} catch (Exception e) {
			e.printStackTrace();

			logger.error("贵州国税,获取cook时出现异常", e);
			try {
				jso.put("result", "false");
				jso.put(SysConfig.INVOICEFALSESTATE,
						SysConfig.INVOICEFALSESTATECODE111);
				jso.put("cwxx", "返回结果错误!");
			} catch (JSONException e1) {
				e1.printStackTrace();
				logger.error("贵州国税,获取cook时出现异常", e);
			}
		}
		return jso;
	}
public static void main(String[] args) {
/*	List<String>  list = SendResultRequest.sendRequestCookie(null,
			null, null, "http://etax.gzgs12366.gov.cn:8080/TaxInquiry/Invoice", "GET");*/
}
}
