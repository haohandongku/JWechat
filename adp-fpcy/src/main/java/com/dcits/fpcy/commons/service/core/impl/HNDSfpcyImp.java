package com.dcits.fpcy.commons.service.core.impl;

import java.io.BufferedReader;
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

import com.dcits.app.util.JacksonUtils;
import com.dcits.fpcy.commons.bean.HSCEntity;
import com.dcits.fpcy.commons.bean.IpAddress;
import com.dcits.fpcy.commons.bean.ResultBean;
import com.dcits.fpcy.commons.bean.TaxOfficeBean;
import com.dcits.fpcy.commons.constant.BrowerType;
import com.dcits.fpcy.commons.constant.HeaderType;
import com.dcits.fpcy.commons.constant.SysConfig;
import com.dcits.fpcy.commons.service.core.InvoiceServerBase;
import com.dcits.fpcy.commons.utils.SendResultRequest;

public class HNDSfpcyImp implements InvoiceServerBase {
	
	private Log logger = LogFactory.getLog(HNDSfpcyImp.class);
	/**
	 * 第二版湖南发票查验
	 * 
	 * @param parameter
	 * @param regionNum
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked"})
	public Map FPCY(Map parameter, TaxOfficeBean fppars){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			parameter.put("rand", hscEntity.getYzm());
			parameter.put("JSESSIONID", hscEntity.getCookie());
			parameter.put("verify", hscEntity.getYzm());
			// 开始查验
			getResult(fppars, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("湖南地税,获取cook时出现异常", e);
			List list = new ArrayList<ResultBean>();
			list.clear();
			list.add(new ResultBean("cwxx", "", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！"));
			result.put("list", list);
			result.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE103);
			result.put(SysConfig.CYJGSTATE,SysConfig.CODE20011);
		}
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result,
			IpAddress ipAddress) throws Exception {
		Map<String, String> requestHeader = new HashMap<String, String>();
		String requestMethod = paras.cy_qqfs;
		String JSESSIONID = String.valueOf(in_parameter.get("JSESSIONID"));
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.COOKIE, JSESSIONID);
		if ("post".equals(requestMethod)) {
			InputStream in = SendResultRequest.sendRequestPost(requestHeader,
					ipAddress, paras.cy_dz, in_parameter);
			JSONObject json = parseInvoiceResult(in);
			List<ResultBean> list = new ArrayList<ResultBean>();
			if (json == null) {
				out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE20011);
			} else if (json.get("result").toString().equals("false")) {
				list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
				out_result.put(SysConfig.INVOICEFALSESTATE,SysConfig.INVOICEFALSESTATECODE211);
				out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE20011);
			} else {
				JSONObject cxjg = new JSONObject(json.get("cxjg").toString());
				list.add(new ResultBean("fpdm", "发票代码", in_parameter.get("FPDM").toString()));
				list.add(new ResultBean("fphm", "发票号码", in_parameter.get("FPHM").toString()));
				list.add(new ResultBean("SKF", "收款方名称", cxjg.get("skfmc").toString()));
				out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE1000);
			}
			out_result.put("list", list);
		}
		if ("get".equals(requestMethod)) {
			out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE20011);
		}
	}

	/**
	 * 湖南地税结果解析
	 */
	@SuppressWarnings("rawtypes")
	public JSONObject parseInvoiceResult(InputStream in) throws Exception {
		JSONObject jso = new JSONObject();
		BufferedReader bufferedReader = null;
		JSONObject json = new JSONObject();
		json.put("result", "false");
		Document document = (Document) SendResultRequest.iSToJSONOrDocument(in,
				"GBK", "text");
		String html = document.toString();
		try {
			if (in != null) {
				Document doc = Jsoup.parse(html);
				if (html.contains("error")) {
					json.put("result", "false");
					json.put(SysConfig.INVOICEFALSESTATE,SysConfig.INVOICEFALSESTATECODE203);
					json.put("cwxx", "您输入的发票信息不匹配，请检查是否输入正确！");
				} else {
					Elements body = doc.select("body");
					for (Element e : body) {
						jso.put("body", e.text().toString());
					}
					Map bodyMap = JacksonUtils.getMapFromJson(jso.getString("body"));
					json.put("result", "true");
					json.put("cxjg", "{\"fpdm\":\"" + bodyMap.get("INV_CODE")
							+ "\",\"fphm\":\"" + bodyMap.get("INV_NO")
							+ "\",\"skfmc\":\"" + bodyMap.get("accNam")
							+ "\"}");
				}
			} else {
				json.put("cwxx", "您输入的发票信息不匹配，请检查是否输入正确！");
				json.put("result", "false");
			}

		} catch (Exception e) {
			System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
			json.put("cwxx", "您输入的发票信息不匹配，请检查是否输入正确！");
			json.put("result", "false");
		} finally {
			try {
				if (in != null)
					in.close();
				if (bufferedReader != null)
					bufferedReader.close();
			} catch (Exception e) {
				System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
				json.put("cwxx", "您输入的发票信息不匹配，请检查是否输入正确！");
				json.put("result", "false");
			}
		}
		return json;
	}
}
