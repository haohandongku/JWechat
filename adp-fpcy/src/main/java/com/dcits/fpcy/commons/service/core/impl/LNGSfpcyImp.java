package com.dcits.fpcy.commons.service.core.impl;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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


public class LNGSfpcyImp implements InvoiceServerBase {
	private Log logger = LogFactory.getLog(LNGSfpcyImp.class);

	/**
	 * 第二版 辽宁国税
	 * 
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			if(StringUtils.isEmpty(hscEntity.getCookie1())) {
				parameter.put("cookie", hscEntity.getCookie());
			}else{
				parameter.put("cookie", hscEntity.getCookie1());
			}
			parameter.put("yzm", hscEntity.getYzm());
			// 开始查验
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("辽宁国税,获取cook时出现异常", e);
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
			IpAddress ipAddress) throws UnsupportedEncodingException,
			JSONException {
		Map requestHeader = new HashMap();
		Map attributeMap = new HashMap();
		if(paras.getSwjg_dm().equals("2100")) {
			attributeMap.put("service", "S_WLFPCY");
			attributeMap.put("serviceMethod", "doService");
			attributeMap.put("parameters", getParameters(in_parameter));
			requestHeader
			.put("Referer",
					"http://218.25.58.87:7006/wlfpcy/wlfp/fpcy_gz/index.jsp");
		}else{
			attributeMap.put("fpdm", in_parameter.get("FPDM").toString());
			attributeMap.put("fphm", in_parameter.get("FPHM").toString());
			attributeMap.put("nsrsbh", in_parameter.get("nsrsbh").toString());
			requestHeader
			.put("Referer",
					"http://wsbst.tax.ln.cn/fpxgxx.do?service=fpxgxxService&method=init");
		}
		
		String requestMethod = paras.cy_qqfs;
		
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.COOKIE, in_parameter.get("cookie").toString().split("; ")[0]);
		if ("post".equals(requestMethod)) {
			InputStream in = SendResultRequest.sendRequestPost(requestHeader,
					ipAddress, paras.cy_dz, attributeMap);
			JSONObject cxjg = parseInvoiceResult(in);
			List<ResultBean> list = new ArrayList<ResultBean>();
			try {
				if(paras.getSwjg_dm().equals("2100") && ("true").equals(cxjg.getString("result"))) {
					list.add(new ResultBean("FPDM", "发票代码", in_parameter.get("FPDM").toString()));
					list.add(new ResultBean("FPHM", "发票号码", in_parameter.get("FPHM").toString()));
					list.add(new ResultBean("FPNR", "查询结果", cxjg.getString("cwxx")));
					out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
				}else if (("true").equals(cxjg.getString("result"))) {
					list.add(new ResultBean("FPDM", "发票代码", cxjg.getString("FPDM")));
					list.add(new ResultBean("FPHM", "发票号码", cxjg.getString("FPHM")));
					list.add(new ResultBean("NSRSBH", "纳税人识别号", cxjg
							.getString("NSRSBH")));
					list.add(new ResultBean("FPNR", "查询结果", cxjg.getString("CXNR")));
					out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
				} else {
					list.add(new ResultBean("cwxx", "", cxjg.get("cwxx").toString()));
					out_result.put(SysConfig.INVOICEFALSESTATE,cxjg.getString(SysConfig.INVOICEFALSESTATE));
					out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
				}
			} catch (Exception e) {
				logger.error("辽宁国税解析JSONObject异常：" + e);
				logger.error(paras.swjg_mc + "解析返回参数异常", e);
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
				out_result.put(SysConfig.INVOICEFALSESTATE,
						SysConfig.INVOICEFALSESTATECODE104);
				e.printStackTrace();
			}
			out_result.put("list", list);
		}

	}

	@SuppressWarnings("rawtypes")
	private Object getParameters(Map in_parameter) {
		StringBuffer sb = new StringBuffer();
		//{"fp_dm":"2100154350","fphm":"10611572","kprq":"2016-09-13","je":"236.89","cxh":"","ip":"172.12.32.17","rand":"4925","pageNo":1,"pageSize":20,"dealMethod":"doService"}
		//{"fp_dm":"2100154350","fphm":"10611572","kprq":"","je":"236.89","cxh":"","ip":"172.12.32.17","rand":"1473","pageNo":"1","pageSize":"20","dealMethod":"doService"}
		sb.append("{").append("\"fp_dm").append("\":").append("\"").append(in_parameter.get("FPDM")+"\",")
		.append("\"fphm").append("\":").append("\"").append(in_parameter.get("FPHM")+"\",")
		.append("\"kprq").append("\":").append("\"").append(""+"\",")
		.append("\"je").append("\":").append("\"").append(in_parameter.get("kjje")+"\",")
		.append("\"cxh").append("\":").append("\"").append(""+"\",")
		.append("\"ip").append("\":").append("\"").append("172.12.32.17"+"\",")
		.append("\"rand").append("\":").append("\"").append(in_parameter.get("yzm")+"\",")
		.append("\"pageNo").append("\":").append("\"").append("1"+"\",")
		.append("\"pageSize").append("\":").append("\"").append("20"+"\",")
		.append("\"dealMethod").append("\":").append("\"").append("doService"+"\"").append("}");
		
		return sb.toString();
	}

	public JSONObject parseInvoiceResult(InputStream in) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("result", "false");
		Document doc = (Document) SendResultRequest.iSToJSONOrDocument(in,
				"UTF-8", "text");
		if (in == null) {
			json.put("cwxx", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！");
			json.put(SysConfig.INVOICEFALSESTATE, "102");
			return json;
		}
		String result = null;
		JSONObject json1 = null;
		if(doc.toString().contains("该张发票是增值税普通发票")) {
			result = doc.text();
			json1 = new JSONObject(result);
		}else{
			result = doc.toString();
		}
		try {
				if(result.contains("该张发票是增值税普通发票")) {
					json.put("cwxx", json1.get("rtnMsg"));
					json.put("result", "true");
				}else if(result.contains("无此纳税人信息")) {
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE211);
					json.put("cwxx", "无此纳税人信息!此票疑为假票。<br/>提示:请核对信息是否输入正确。");
				} else if (result.contains("无此纳税人识别号")) {
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE211);
					json.put("cwxx", "无此纳税人识别号!此票疑为假票。<br/>提示:请核对信息是否输入正确。");
				} else if (result.contains("该纳税人未购买过此种发票")) {
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE211);
					json.put("cwxx", "该纳税人未购买过此种发票!此票疑为假票。<br/>提示:请核对信息是否输入正确。");
				} else if (result.contains(" id=\"result\"")) {
					json.put("cwxx", "查验成功");
					json.put("result", "true");
					Elements eles = doc.select("table").select("tbody")
							.select("tr").select("td").select("table")
							.select("tbody").select("tr").select("td")
							.select("table").select("tbody").select("tr")
							.select("td");
					for (Element e : eles) {
						if (e.select("table").isEmpty()) {
							if (e.attr("id").compareTo("result") == 0) {
								json.put("CXNR", e.text());
							} else if (!(e.select("input").isEmpty())) {
								if (e.select("input").attr("name")
										.compareTo("nsrsbh") == 0) {
									json.put("NSRSBH",
											e.select("input").attr("value"));
								} else if (e.select("input").attr("name")
										.compareTo("fpdm") == 0) {
									json.put("FPDM",
											e.select("input").attr("value"));
								} else if (e.select("input").attr("name")
										.compareTo("fphm") == 0) {
									json.put("FPHM",
											e.select("input").attr("value"));
								}
							}
						}
					}
				} else {
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE111);
					json.put("cwxx", "发票查询失败！请稍候重试。");
				}
		} catch (Exception e) {
			logger.error("辽宁国税获取解析异常",e);
			json.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE111);
			json.put("cwxx", "此种票样查询发生异常，请稍后重试");
			json.put("result", "false");
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception e2) {
				json.put("cwxx", "发票查询失败！请稍候重试");
				json.put("result", "false");
			}
		}
		return json;
	}
}
