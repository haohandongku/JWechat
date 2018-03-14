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
import com.dcits.fpcy.commons.constant.BrowerType;
import com.dcits.fpcy.commons.constant.HeaderType;
import com.dcits.fpcy.commons.constant.SysConfig;
import com.dcits.fpcy.commons.service.core.InvoiceServerBase;
import com.dcits.fpcy.commons.utils.SendResultRequest;

public class SCDSfpcyImp implements InvoiceServerBase {
	/**
	 * 四川地税发票校验
	 */
	private Log logger = LogFactory.getLog(SCDSfpcyImp.class);
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Map FPCY(Map parameter, TaxOfficeBean fppars){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			parameter.put("imgcode", hscEntity.getYzm());
			if(hscEntity.getCookie1() == null) {
				parameter.put("JSESSIONID",hscEntity.getCookie());	
			}else{
				parameter.put("JSESSIONID", hscEntity.getCookie1());	
			}
			// 开始查验
			getResult(fppars, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("四川地税,获取cook时出现异常", e);
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result,
			IpAddress ipAddress) throws JSONException {
		Map requestHeader = new HashMap();
		String JSESSIONID=null;
		 JSESSIONID = in_parameter.get("JSESSIONID").toString();
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.COOKIE, JSESSIONID);
		requestHeader.put("Host", "182.151.197.163:7002");
		requestHeader.put("Origin", "http://182.151.197.163:7002");
		requestHeader.put("Referer", "http://182.151.197.163:7002/FPCY_SCDS_WW/wwfpcy");
		requestHeader.put("Connection", "keep-alive");
		requestHeader.put("Accept-Encoding", "gzip, deflate");
		requestHeader.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		InputStream in = SendResultRequest.sendRequestPost(requestHeader,
				ipAddress, paras.cy_dz, in_parameter);
		JSONObject json = parseInvoiceResult(in);
		List<ResultBean> list = new ArrayList<ResultBean>();
		if (json == null||("false").equals(json.get("result").toString())) {
			list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
			out_result.put(SysConfig.INVOICEFALSESTATE,json.getString(SysConfig.INVOICEFALSESTATE));
			out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE20011);
		} else {
			list.add(new ResultBean("RES", "查询结果", json.get("rtnMsg").toString()));
			out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
		}
		out_result.put("list", list);
	}

	/**
	 * 四川地税结果解析
	 * @throws JSONException 
	 */
	public JSONObject parseInvoiceResult(InputStream in) throws JSONException {

		JSONObject json = new JSONObject();
		json.put("result", "false");
		Document document = (Document) SendResultRequest.iSToJSONOrDocument(in,
				"UTF-8", "text");
		String result = document.toString();
		try {
			if (in != null) {
				if (result.contains("验证码错误")) {
					json.put("cwxx", "发票查询错误，请重试！");
					json.put(SysConfig.INVOICEFALSESTATE,SysConfig.INVOICEFALSESTATECODE101);
					return json;
				} else if (result.contains("您所查询的发票信息与系统中不相符")) {
					json.put("cwxx",
							"您所查询的发票信息与系统中不相符，请拨打四川省成都市地方税务局举报电话028-12366。");
					json.put(SysConfig.INVOICEFALSESTATE,SysConfig.INVOICEFALSESTATECODE211);
					return json;
				} else if (result.contains("次数过多")) {
					String rtnMsg = document.select("#message").get(0).text();
					json.put(SysConfig.INVOICEFALSESTATE,SysConfig.INVOICEFALSESTATECODE202);
					json.put("cwxx", rtnMsg + "稍后再试！");
					return json;
				} else if (result.contains("您所查询的发票信息本系统中不存在")) {
					json.put("cwxx",
							"您所查询的发票信息本系统中不存在，如有疑问请拨打四川省成都市地方税务局纳税服务电话028-12366进行咨询！");
					json.put(SysConfig.INVOICEFALSESTATE,SysConfig.INVOICEFALSESTATECODE201);
					return json;
				} else if(result.contains("错误信息(调试使用)：")){
					json.put("cwxx", "查询过程中出现错误请重新查询");
					json.put(SysConfig.INVOICEFALSESTATE,SysConfig.INVOICEFALSESTATECODE203);
					return json;
				} else {
					Elements els = document.select("#cxjj");
					String rtnMsg = els.get(0).text();
					json.put("rtnMsg", rtnMsg);
					json.put("result", "true");
				}
				return json;
			}
		} catch (Exception e) {
			System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
			json.put("cwxx", "您输入的发票信息不匹配，请检查是否输入正确！");
			e.printStackTrace();
		}
		return null;
	}
}
