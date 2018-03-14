package com.dcits.fpcy.commons.service.core.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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

/**
 * 黑龙江国税 2017-04-11
 * 
 */
public class HLJGSfpcyImp implements InvoiceServerBase {
	private Log logger = LogFactory.getLog(HLJGSfpcyImp.class);

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			parameter.put("rand", hscEntity.getYzm());
			parameter.put("JSESSIONID", hscEntity.getCookie());
			parameter.put("snow", hscEntity.getCookie1());
			getResult(fpcyParas, parameter, result, null);
		} catch (Exception e) {
			logger.error("黑龙江国税,获取cook时出现异常", e);
			List<ResultBean> list = new ArrayList<ResultBean>();
			list.clear();
			list.add(new ResultBean("cwxx", "", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！"));
			result.put("list", list);
			result.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE103);
			result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		}
		return result;
	}

	@SuppressWarnings({ "rawtypes" })
	public Map FPCY(Map parameter) throws Exception {
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void getResult(TaxOfficeBean paras, Map<String, String> in_Parameter, Map out_Result, IpAddress ipAddress)
			throws JSONException {
		Map<String, String> attributeMap = new HashMap<String, String>();
		Map<String, String> requestHeader = new HashMap<String, String>();
		String fpdm = in_Parameter.get("FPDM").toString();
		String tabledata = getTableData(in_Parameter);
		attributeMap.put("xhbl", "1");
		attributeMap.put("tabledata", tabledata);
		attributeMap.put("se", in_Parameter.get("rand").toString());
		attributeMap.put("snow", in_Parameter.get("snow").toString());
		String JSESSIONID = in_Parameter.get("JSESSIONID").toString();
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.COOKIE, JSESSIONID);
		if (fpdm.length() == 12) {
			requestHeader.put(HeaderType.REFERER, paras.cyym);
		} else {
			requestHeader.put(HeaderType.REFERER, paras.cyym);
		}
		// InputStream in=SendResultRequest.sendRequestIn(new HashMap(),
		// ipAddress, new HashMap(),
		// "http://221.212.153.203/fpcx/YbSession.do?noww="+in_Parameter.get("snow").toString(),
		// "get");
		InputStream in = SendResultRequest.sendRequestPost(requestHeader, ipAddress, paras.cy_dz, attributeMap);
		JSONObject json = parseInvoiceResult(in);
		List<ResultBean> list = new ArrayList<ResultBean>();
		try {
			if ("false".equals(json.get("result"))) {
				list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
				out_Result.put(SysConfig.INVOICEFALSESTATE, json.getString(SysConfig.INVOICEFALSESTATE));
				out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			} else if (in_Parameter.get("FPDM").subSequence(0, 4).toString().equals("2300")
					&& "true".equals(json.get("result"))) {
				list.add(new ResultBean("FPDM", "发票代码", json.getString("FPDM")));
				list.add(new ResultBean("FPHM", "发票号码", json.getString("FPHM")));
				list.add(new ResultBean("KPRQ", "开票日期", json.getString("KPRQ")));
				list.add(new ResultBean("JE", "金额", json.getString("JE")));
				list.add(new ResultBean("XHFNSRSBH", "销货方纳税人识别号", json.getString("XHFNSRSBH")));
				list.add(new ResultBean("XHFMC", "销货方名称", json.getString("XHFMC")));
				list.add(new ResultBean("GHFMC", "购货方名称", json.getString("GHFMC")));
				list.add(new ResultBean("CXJG", "查询结果", "查询成功"));
				out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
			} else {
				list.add(new ResultBean("FPDM", "发票代码", json.getString("FPDM")));
				list.add(new ResultBean("FPHM", "发票号码", json.getString("FPHM")));
				list.add(new ResultBean("XHDW", "销货单位", json.getString("XHDW")));
				list.add(new ResultBean("FPMC", "发票名称", json.getString("FPMC")));
				list.add(new ResultBean("SWJG", "税务机关", json.getString("SWJG")));
				list.add(new ResultBean("CXJG", "查询结果", "查询成功"));
				out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
			}
		} catch (Exception e) {
			logger.error("黑龙江国税解析JSONObject异常：" + e);
			logger.error(paras.swjg_mc + "解析返回参数异常", e);
			out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			out_Result.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE104);
			e.printStackTrace();
		}
		out_Result.put("list", list);
	}

	private String getTableData(Map<String, String> in_Parameter) {
		String fpdm = in_Parameter.get("FPDM").toString();
		String attributeString = "";
		if (fpdm.length() == 12) {
			attributeString = "<?xml version=\"1.0\"?><rows><row id='1'><cell>1</cell><cell>"
					+ in_Parameter.get("FPDM") + "</cell><cell>" + in_Parameter.get("FPHM")
					+ "</cell><cell></cell><cell></cell><cell></cell><cell></cell></row></rows>";
		} else {
			attributeString = "<?xml version=\"1.0\"?><rows><row id='2'><cell>1</cell><cell>"
					+ in_Parameter.get("FPDM").toString() + "</cell><cell>" + in_Parameter.get("FPHM").toString()
					+ "</cell><cell>" + in_Parameter.get("kprq").toString()
					+ "</cell><cell></cell><cell></cell><cell></cell><cell></cell><cell></cell></row></rows>";
		}
		return attributeString;
	}

	public JSONObject parseInvoiceResult(InputStream in) throws JSONException {
		JSONObject json = new JSONObject();
		String html = "";
		if (in == null) {
			json.put("cwxx", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！");
			json.put(SysConfig.INVOICEFALSESTATE, "102");
			return json;
		}
		try {
			Document doc = (Document) SendResultRequest.iSToJSONOrDocument(in, "GBK", "text");
			html = doc.toString();
			if (html.contains("请录入正确的计算结果验证码")) {
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE101);
				json.put("result", "false");
				json.put("cwxx", "？为计算结果，请录入正确的计算结果验证码！！");
			} else if (html.contains("无此发票信息")) {
				json.put("result", "false");
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE201);
				json.put("cwxx", "无此发票信息");
			} else if ("".equals(html)) {
				json.put("result", "false");
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE213);
				json.put("cwxx", "查询失败，请重试！");
			} else {
				json.put("result", "true");
				Elements elements = doc.select("cell");
				if (elements.size() > 5) {
					String[] nameOfIndexes = { "FPDM", "FPHM", "XHDW", "FPMC", "SWJG", "CXJG" };
					for (int i =1; i < 7; i++) {
						json.put(nameOfIndexes[i-1], elements.get(i).text().toString());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			json.put("cwxx", "该票种查询异常，请稍后重试");
			json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE111);
			json.put("result", "false");
		}
		return json;
	}

	// 黑龙江国税
	@SuppressWarnings("unused")
	public static String getHLJGSYzm(String JSESSIONID, String snow) {
		InputStream in = null;
		BufferedReader bufferedReader = null;
		String html = "";
		try {
			URL url = new URL("http://221.212.153.203/fpcx/YbSession.do?noww=" + snow);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("GET");
			con.setUseCaches(false);
			con.setRequestProperty(HeaderType.USERAGENT, BrowerType.firfox);
			con.setRequestProperty("Referer", "http://221.212.153.203/fpcx/qtptfpcx.jsp");
			con.setRequestProperty("Cookie", JSESSIONID);
			in = con.getInputStream();// 获取发票验真返回信息
			return html;
		} catch (Exception w) {
		}
		return html;
	}

}
