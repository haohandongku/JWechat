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
import com.dcits.fpcy.commons.constant.HeaderType;
import com.dcits.fpcy.commons.constant.SysConfig;
import com.dcits.fpcy.commons.service.core.InvoiceServerBase;
import com.dcits.fpcy.commons.utils.ResultUtils;
import com.dcits.fpcy.commons.utils.SendResultRequest;

public class BJDSfpcyImp implements InvoiceServerBase {

	private Log logger = LogFactory.getLog(BJDSfpcyImp.class);

	/**
	 * 第二版 北京地税
	 * 
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map FPCY(Map parameter, TaxOfficeBean fppars){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			parameter.put("rand", hscEntity.getYzm());
			parameter.put("JSESSIONID", hscEntity.getCookie());
			// 开始查验
			getResult(fppars, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("北京地税,获取cook时出现异常", e);
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
	 * @throws Exception
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void getResult(TaxOfficeBean paras, Map in_Parameter, Map out_Result, IpAddress ipAddress) throws Exception {
		Map<String, String> requestHeader = new HashMap<String, String>();
		String JSESSIONID = in_Parameter.get("JSESSIONID").toString();
		requestHeader.put(HeaderType.COOKIE, JSESSIONID);
		requestHeader.put("localid", "1");
		InputStream in = SendResultRequest.sendRequestIn(requestHeader, ipAddress, in_Parameter, paras.cy_dz,
				paras.cy_qqfs);
		JSONObject json = parseInvoiceResult(in);
		json.put("FPDM", in_Parameter.get("FPDM").toString());
		List<ResultBean> list = new ArrayList<ResultBean>();
		if (json.get("result").toString().equals("false")) {
			list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
			out_Result.put(SysConfig.INVOICEFALSESTATE, json.getString(SysConfig.INVOICEFALSESTATE));
			out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		} else {
			String key[] = { "发票代码", "发票号码", "开具单位", "纳税人识别号" };
			list = ResultUtils.getListInfoFromJson(key, json);
			out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
		}
		out_Result.put("list", list);
	}

	// 获取json对象(里面放验真之后的结果)
	public JSONObject parseInvoiceResult(InputStream in) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("result", "false");
		if (in == null) {
			json.put("cwxx", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！");
			json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE102);
			return json;
		}
		Document document = (Document) SendResultRequest.iSToJSONOrDocument(in, "GBK", "text");
		String result = document.toString();
		try {
			if (result.contains("此发票与北京市地方税务局后台数据信息不符!")) {
				json.put("cwxx", "此发票与北京市地方税务局后台数据信息不符!");
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE203);
				return json;
			}
			if (result.contains("验证码填写错误，请重新填写！")) {
				json.put("cwxx", "发票查询异常，请重试！");
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE101);
				return json;
			}
			if (result.contains("此发票未显示购票单位，请到主管税务机关进行发票真伪鉴定！")) {
				json.put("cwxx", "此发票未显示购票单位，请到主管税务机关进行发票真伪鉴定！");
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE204);
				return json;
			} else {
				Elements elements = document.select("input[type=hidden]");
				json.put("result", "true");
				if (json.get("result").toString().equals("true")) {
					for (Element element : elements) {
						if ("nsrsh".equals(element.attr("name"))) {
							String nsr = "NSRSBH";
							json.put(nsr, element.attr("value"));
						} else {
							json.put(element.attr("name").toUpperCase(), element.attr("value"));
						}
					}
					json.put("FPHM", json.get("FPHM").toString().substring(12));
				}
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICETRUESTATE000);
				return json;
			}
		} catch (Exception e) {
			try {
				json.put("cwxx", "返回结果错误!");
			} catch (JSONException e1) {
				logger.error("北京地税,解析时异常", e);
			}
			return json;
		}
	}

}
