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

import com.dcits.fpcy.commons.bean.IpAddress;
import com.dcits.fpcy.commons.bean.ResultBean;
import com.dcits.fpcy.commons.bean.TaxOfficeBean;
import com.dcits.fpcy.commons.constant.SysConfig;
import com.dcits.fpcy.commons.service.core.InvoiceServerBase;
import com.dcits.fpcy.commons.utils.ResultUtils;
import com.dcits.fpcy.commons.utils.SendResultRequest;
public class JSNJGSfpcyImp implements InvoiceServerBase {

	private Log logger = LogFactory.getLog(JSNJGSfpcyImp.class);

	/**
	 * 江苏南京国税
	 * 
	 * @return
	 */
	@SuppressWarnings({"rawtypes" })
	public Map FPCY(Map parameter, TaxOfficeBean fppars){
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			// 开始查验
			IpAddress ip=new IpAddress("58.53.128.130", 1111);
			getResult(fppars, parameter, result, ip);
		} catch (Exception e) {
			logger.error("南京国税,获取cook时出现异常", e);
			result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		} 
		return result;
	}

	/**
	 * 
	 * @param paras
	 *            查验参数
	 * @param in_Parameter
	 *            请求数据 [ 验证码 jessionid]
	 * @param out_Result
	 *            返回结果
	 * @throws JSONException
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void getResult(TaxOfficeBean paras, Map in_Parameter, Map out_Result,
			IpAddress ipAddress) throws JSONException {
		InputStream in = null;
		JSONObject json = null;
		List list1 = SendResultRequest.sendRequestCookie(null, null,
				null, "http://etax.jsgs.gov.cn:9999/mobile-invoice/fpcy.action?sign=bill_check_zzs", "get");
		String cookie =  list1.get(1).toString().split(";")[0].toString()+"; "+list1.get(0).toString().split(";")[0].toString();
		String address1 = "http://etax.jsgs.gov.cn:9999/mobile-invoice/fpcy.action?sign=check";
		Map map = new HashMap<String, String>();
		//map.put("sign", "check");
		map.put("fpdm", in_Parameter.get("fpdm"));
		map.put("fphm", in_Parameter.get("fphm"));
		map.put("skfsbh", "");
		map.put("kprq", in_Parameter.get("kprq"));
		map.put("kpje", in_Parameter.get("fpje"));
		map.put("code", "vdzt");
		map.put("zzsBz", "Y");
		
		Map map1 = new HashMap<String, String>();
		map1.put("Referer", "http://etax.jsgs.gov.cn:9999/mobile-invoice/fpcy.action?sign=bill_check_zzs");
		map1.put("Host", "etax.jsgs.gov.cn:9999");
		map1.put("Origin", "http://etax.jsgs.gov.cn:9999");
		map1.put("Cookie", cookie);
		in = SendResultRequest.sendRequestPost(map1, null, address1,map);
		json = parseInvoiceResult(in);
		List<ResultBean> list = new ArrayList<ResultBean>();
		if (json.get("result").toString().equals("false")) {
			list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
			out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		} else {
			/*list.add(new ResultBean("cwxx", "查询结果", json.get("cwxx")
					.toString()));*/
			json.put("票种",paras.swjg_mc);
			json.put("发票代码", in_Parameter.get("fpdm"));
			json.put("发票号码", in_Parameter.get("fphm"));
			String key[] = {"票种","发票代码","发票号码","查询结果","销售方名称","购买方名称","金额","开票日期"};
			try {
				list = ResultUtils.getListInfoFromJson1(key, json);
			} catch (Exception e) {
				e.printStackTrace();
			}
			out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
		}
		out_Result.put(SysConfig.INVOICEFALSESTATE,
				json.get(SysConfig.INVOICEFALSESTATE).toString());
		out_Result.put("list", list);
	}

	// 获取json对象(里面放验真之后的结果)
	public static JSONObject parseInvoiceResult(InputStream in) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("result", "false");
		if(in==null){
			json.put("cwxx", "发票查询异常，请重试！");
			json.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE102);
		}
		Document document = (Document) SendResultRequest.iSToJSONOrDocument(in,
				"utf-8", "text");
		String result = document.toString();
		try {
			if (result.contains("没有查询到相关发票信息")) {
				json.put("cwxx", "没有查询到相关发票信息！");
				json.put(SysConfig.INVOICEFALSESTATE,
						SysConfig.INVOICEFALSESTATECODE201);
				return json;
			}
			if (result.contains("验证码错误")) {
				json.put("cwxx", "发票查询异常，请重试！");
				json.put(SysConfig.INVOICEFALSESTATE,
						SysConfig.INVOICEFALSESTATECODE101);
				return json;
			}
			if (result.contains("此发票当天查验超过5次，请于24小时后再进行查验操作！")) {
				json.put("cwxx", "此发票当天查验超过5次，请于24小时后再进行查验操作！");
				json.put(SysConfig.INVOICEFALSESTATE,
						SysConfig.INVOICEFALSESTATECODE202);
				return json;
			}
			if (result.contains("信息查验不一致")) {
				json.put("cwxx", "发票信息填写有误");
				json.put(SysConfig.INVOICEFALSESTATE,
						SysConfig.INVOICEFALSESTATECODE203);
				return json;
			} else {
				String key[] = {"查询结果","销售方名称","购买方名称","金额","开票日期"};
				Elements elements = document.select("body").select("p");
				//String val[] = elements.toString().split("：");
				for(int i = 0;i<key.length;i++) {
					if(i == 0) {
						json.put(key[i], "成功");
					}else{
						String ff = elements.get(i).toString();
						json.put(key[i], ff.subSequence(ff.indexOf("：")+1, ff.indexOf("&lt")));
					}
				}
				json.put("result", "true");
				json.put(SysConfig.INVOICEFALSESTATE, "000");
				/*if (json.get("result").toString().equals("true")) {
					String result1 = elements.toString();
					json.put("cwxx", result1.substring(result1.indexOf(">")+1, result1.indexOf("</")));
				}*/
				return json;
			}
		} catch (Exception e) {
			json.put("cwxx", "发票查询异常，请重试！");
			json.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE101);
			return json;
		}
	}
}
