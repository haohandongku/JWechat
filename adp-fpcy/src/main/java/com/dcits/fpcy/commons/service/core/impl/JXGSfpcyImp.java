package com.dcits.fpcy.commons.service.core.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONObject;
import org.jsoup.Jsoup;
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

public class JXGSfpcyImp implements InvoiceServerBase {
	private Log logger = LogFactory.getLog(JXGSfpcyImp.class);

	/**
	 * 第二版 江西国税
	 * 
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map FPCY(Map parameter, TaxOfficeBean fppars){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			parameter.put("rand", hscEntity.getYzm().toLowerCase());
			if(hscEntity.getCookie() == null) {
				parameter.put("JSESSIONID", hscEntity.getCookie1());
			}else{
				parameter.put("JSESSIONID", hscEntity.getCookie());
			}
			
			// 开始查验
			getResult(fppars, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("江西国税,获取cook时出现异常", e);
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
			IpAddress ipAddress) throws Exception {
		Map attributeMap = new HashMap();
		Map<String, String> requestHeader = new HashMap<String, String>();
		attributeMap.put("search_yzm", in_parameter.get("rand").toString());
		attributeMap.put("search_fpdm", in_parameter.get("FPDM").toString());
		attributeMap.put("search_kprq", in_parameter.get("search_kprq")
				.toString());
		attributeMap.put("search_kjfsbh", in_parameter.get("search_kjfsbh")
				.toString());
		attributeMap.put("search_je", in_parameter.get("search_je").toString());
		attributeMap.put("readType", "1");
		attributeMap.put("search_fphm", in_parameter.get("FPHM").toString());
		attributeMap.put("token", "0.7220967267939056");
		String requestMethod = paras.cy_qqfs;
		String JSESSIONID[] = in_parameter.get("JSESSIONID").toString().split(";");
		attributeMap.put("JSESSIONID", JSESSIONID[0]);
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.COOKIE, JSESSIONID[0]/* + "; path=/"*/);
		if ("post".equals(requestMethod)) {
			InputStream in = SendResultRequest.sendRequestPost(requestHeader,
					ipAddress, paras.cy_dz, attributeMap);
			JSONObject json = parseInvoiceResult(in,in_parameter.get("FPDM").toString());
			List<ResultBean> list = new ArrayList<ResultBean>();
			if (json == null || ("false").equals(json.get("result").toString())) {
				list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
				out_result.put(SysConfig.INVOICEFALSESTATE,json.getString(SysConfig.INVOICEFALSESTATE));
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);// 解析返回结果有误
			} else {
				try{
					list.add(new ResultBean("sycs", "剩余次数", json.getString("sycs")));
					list.add(new ResultBean("nsrsbh", "纳税人识别号", json
							.getString("nsrsbh")));
				}catch (Exception e) {
					list.add(new ResultBean("fpdm", "发票代码", json.getString("fpdm")));
					list.add(new ResultBean("fphm", "发票号码", json
							.getString("fphm")));
				}
				list.add(new ResultBean("cxcs", "查询次数", json.getString("cxcs")));
				list.add(new ResultBean("swjgmc", "税务机关名称", json
						.getString("swjgmc")));
				list.add(new ResultBean("gpfmc", "购票方名称", json
						.getString("gpfmc")));
				list.add(new ResultBean("", "查询状态","成功"));
				/*list.add(new ResultBean("spsj", "售票时间", json.get("spsj")
						.toString()));*/
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
			}
			out_result.put("list", list);
		}
	}

	@SuppressWarnings("unused")
	public JSONObject parseInvoiceResult(InputStream in,String fpdm) throws Exception {
		JSONObject json = new JSONObject();
		json.put("result", "false");
		Document document = (Document) SendResultRequest.iSToJSONOrDocument(in,
				"UTF-8", "");
		String html = document.toString();
	//	FileUtils.writeStringToFile(new File("D:/123.xml"), html);
		try {
			if (in != null) {
				Document doc = Jsoup.parse(html);
				System.out.println(doc);
				String cxxx = null;
				Element script = null;
				String scr[] = null;
				if(fpdm.substring(0, 4).equals("3600")) {
					Elements doc1= doc.select("script");
					try{
						script = doc.select("script").get(12);
						scr = script.data().split("';|= '");
					}catch (Exception e) {
						script = doc.select("script").get(11);
						scr = script.data().split("';|= '");
					}
				}else{
					script = doc.select("script").get(11);
					 scr = script.data().split("';|= '");
				}
				if (html.contains("查询次数超过规定的")) {
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE202);
					json.put("cwxx", "查询次数超过规定的10次");
					return json;
				} else if (html.contains("验证码不正确")) {
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE101);
					json.put("cwxx", "消息很抱歉，系统未能得到发票信息，请您重新进行一次查询！");
					return json;
				} else if (!fpdm.substring(0, 4).equals("3600")) {
					if(scr[15].contains("疑似问题发票")) {
						json.put(SysConfig.INVOICEFALSESTATE,
								SysConfig.INVOICEFALSESTATECODE201);
						json.put("cwxx",
								"您所查询的发票无相关记录，疑似问题发票，欢迎拨打12366或向当地主管国税机关举报。");
						return json;
					}
					
				} else {
					if(fpdm.substring(0, 4).equals("3600")) {
						try{
							json.put("cxcs", scr[11]);
							json.put("swjgmc", scr[13]);
							json.put("gpfmc", scr[15]);
							json.put("spsj", scr[35]);
							json.put("sycs", scr[37]);
							json.put("nsrsbh", scr[43]);
							json.put("result", "true");
						}catch (Exception e) {
							json.put("fpdm", scr[5]);
							json.put("fphm", scr[7]);
							json.put("cxcs", scr[11]);
							json.put("swjgmc", scr[13]);
							json.put("gpfmc", scr[15]);
							//json.put("spsj", scr[23]);
							//json.put("sycs", count);
							//json.put("nsrsbh", scr[43]);
							json.put("result", "true");
						}
						
					}else{
						json.put("cxcs", scr[11]);
						json.put("swjgmc", scr[13]);
						json.put("gpfmc", scr[15]);
						json.put("spsj", scr[35]);
						json.put("sycs", scr[37]);
						json.put("nsrsbh", scr[43]);
						//json.put("result", "true");
					}
				}
			} else {
				json.put("cwxx", "没有查询到此税控发票信息");
				return json;
			}
		} catch (Exception e) {
			logger.error("江西国税解析异常", e);
			json.put("cwxx", "消息很抱歉，系统未能得到发票信息，请您重新进行一次查询！!");
			return json;
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception e) {
				logger.error("江西国税解析异常", e);
				json.put("cwxx", "消息很抱歉，系统未能得到发票信息，请您重新进行一次查询！!");
				return json;
			}
		}
		return json;
	}
}
