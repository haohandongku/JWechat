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
 * 山东国税
 * 
 * @author wangkej
 * 
 */
public class SDGSfpcyImp implements InvoiceServerBase {
	private Log logger = LogFactory.getLog(SDGSfpcyImp.class);
	@SuppressWarnings({ "rawtypes" })
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("山东国税,获取cook时出现异常", e);
			List<ResultBean> list = new ArrayList<ResultBean>();
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
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result,
			IpAddress ipAddress) throws JSONException {
		Map attributeMap = new HashMap();
		attributeMap.put("fpdm", in_parameter.get("fpdm").toString());
		attributeMap.put("fphm", in_parameter.get("fphm").toString());
		attributeMap.put("fpmm", in_parameter.get("fpmm").toString());
		attributeMap.put("oper", "zwcx");
		Map requestHeader = new HashMap();
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put("Referer",
				"http://www.sd-n-tax.gov.cn/col/col47487/index.html");
		InputStream in = SendResultRequest.sendRequestPost(requestHeader,
				ipAddress, paras.cy_dz, attributeMap);
		JSONObject json = parseInvoiceResult(in);
		List<ResultBean> list = new ArrayList<ResultBean>();
		try {
			if (json == null||("false").equals(json.get("result").toString())) {
				list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
				out_result.put(SysConfig.INVOICEFALSESTATE,json.getString(SysConfig.INVOICEFALSESTATE));
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			} else {
				list.add(new ResultBean("CXJG", "查询信息", json.get("cwxx").toString()));
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
			}
		} catch (Exception e) {
			logger.error("山东国税解析JSONObject异常：" + e);
			logger.error(paras.swjg_mc + "解析返回参数异常", e);
			out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			out_result.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE104);
			e.printStackTrace();
		}
		out_result.put("list", list);
	}

	public JSONObject parseInvoiceResult(InputStream in) throws JSONException {
		JSONObject json = new JSONObject();
		try {
			json.put("result", "false");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		if (in == null) {
			json.put("cwxx", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！");
			json.put(SysConfig.INVOICEFALSESTATE, "102");
			return json;
		}
		String html = SendResultRequest.iSToJSONOrDocument(in, "GBK", "")
				.toString();
		json.put("result", "false");
		try {
				if (html.contains("不符") || html.contains("没有查询到该发票信息")) {
					json.put("cwxx",
							"您查询的发票代码、发票号码与发票密码与系统记录的信息不符，您可以到当地主管国税机关进行进一步判断确认。");
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE211);
					return json;
				} else if (html.contains("一致")) {
					json.put("result", "true");
					json.put("cwxx",
							"您查询的发票代码、发票号码与发票密码与系统记录的信息相一致，若仍有疑问，请咨询当地主管国税机关。");
					return json;
				} else {
					json.put("cwxx", "网站解析出错");
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE111);
					return json;
				}
		} catch (Exception e) {
			System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
			json.put("cwxx", "没有查询到此税控发票信息");
			json.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE111);
			return json;
		} finally {
		}
	}
}
