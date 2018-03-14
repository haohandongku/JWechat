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
import com.dcits.fpcy.commons.utils.SendResultRequest;


public class GXGSfpcyImp implements InvoiceServerBase {
	/**
	 * 广西国税发票查验
	 * 
	 * 2017-04-11
	 */
	private Log logger = LogFactory.getLog(GXGSfpcyImp.class);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity  hscEntity=null;
		try {
			hscEntity=(HSCEntity) parameter.get("hscEntity");
			String lastSession=hscEntity.getCookie();
			parameter.put("yzm", hscEntity.getYzm());
			if(lastSession == null) {
				parameter.put("JSESSIONID", hscEntity.getCookie1());	
			}else{
				parameter.put("JSESSIONID", hscEntity.getCookie());	
			}
			System.out.println(hscEntity.toString());
			// 开始查验
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("广西国税,获取cook时出现异常", e);
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
			IpAddress ipAddress) {
		Map  attributeMap=new HashMap();
		attributeMap.put("queryMothd", "0");
		attributeMap.put("fpdm", (String)in_parameter.get("fpdm"));
		attributeMap.put("fphm", (String)in_parameter.get("fphm"));
		attributeMap.put("nsrsbh",(String)in_parameter.get("nsrsbh"));
		attributeMap.put("fpje", (String)in_parameter.get("fpje"));
		attributeMap.put("kprq", (String)in_parameter.get("kprq"));
		attributeMap.put("yzm", (String)in_parameter.get("yzm"));
		Map requestHeader = new HashMap();
		requestHeader.put(HeaderType.COOKIE, (String)in_parameter.get("JSESSIONID"));
		InputStream in = SendResultRequest.sendRequestIn(requestHeader,
				ipAddress, attributeMap, paras.cy_dz, paras.cy_qqfs);
		JSONObject json = parseInvoiceResult(in, in_parameter);
		List<ResultBean> list = new ArrayList<ResultBean>();
		try {
			if (json == null) {
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			} else if (json.get("result").toString().equals("false")) {
				list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
				out_result.put(SysConfig.INVOICEFALSESTATE,json.getString(SysConfig.INVOICEFALSESTATE));
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			} else {
				list.add(new ResultBean("CXJG", "查询结果", json.get("CXNR")
						.toString()));
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
			}
			out_result.put("list", list);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("rawtypes")
	public JSONObject parseInvoiceResult(InputStream in, Map in_parameter) {
		JSONObject json = new JSONObject();
		try {
			json.put("result", "false");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		Document doc = (Document) SendResultRequest.iSToJSONOrDocument(in,
				"GBK", "text");
		String result = doc.toString();
		try {
			if (in != null) {
				if (result.contains("信息不一致，疑为假票")) {
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE211);
					json.put("cwxx", "您查询的发票代码为"
							+ in_parameter.get("FPDM").toString() + "，发票号码为"
							+ in_parameter.get("FPHM").toString()
							+ "，销售方税务登记证号为</br>"
							+ in_parameter.get("NSRSBH").toString()
							+ "的发票与税务机关登记信息不一致，疑为假票。");
					return json;
				} else if (result.contains("获取验证码失败")) {
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE101);
					json.put("cwxx", "发票查询失败，请重试！");
				} else if (result.contains("验证码输入有误")) {
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE203);
					json.put("cwxx", "发票查询失败，请重试！");
				} else {
					json.put("result", "true");
					Elements eles = doc.getElementsByClass("STYLE4");
					json.put("CXNR", eles.get(0).text().toString());
				}
				return json;

			} else {
				json.put("cwxx", "发票查询失败，请稍候重试！");
				json.put(SysConfig.INVOICEFALSESTATE,
						SysConfig.INVOICEFALSESTATECODE102);
				return json;
			}
		} catch (Exception e) {
			System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
			try {
				json.put("cwxx", "您查询的发票与税务机关登记信息不一致，疑为假票。");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			return json;
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception e2) {
				System.out.println(("[ERROR] 程序执行操作出现异常：" + e2.getMessage()));
				return json;
			}
		}
	}
}
