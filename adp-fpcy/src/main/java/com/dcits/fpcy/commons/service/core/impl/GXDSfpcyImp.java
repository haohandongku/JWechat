package com.dcits.fpcy.commons.service.core.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONObject;

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

public class GXDSfpcyImp implements InvoiceServerBase {

	/**
	 * 广西地税发票查验
	 * 
	 * @param parameter
	 * @param regionNum
	 * @return
	 */
	private Log logger = LogFactory.getLog(GXDSfpcyImp.class);

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			parameter.put("yzm", hscEntity.getYzm());
			if (hscEntity.getCookie() == null) {
				parameter.put("JSESSIONID", hscEntity.getCookie1());
			} else {
				parameter.put("JSESSIONID", hscEntity.getCookie());
			}
			// 开始查验
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("广西地税,获取cook时出现异常", e);
			List list = new ArrayList<ResultBean>();
			list.clear();
			list.add(new ResultBean("cwxx", "", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！"));
			result.put("list", list);
			result.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE103);
			result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		}
		return result;

	}

	public void getResult(TaxOfficeBean paras, Map<String, String> in_Parameter, Map<String, Object> out_Result,
			IpAddress ipAddress) throws Exception {

		Map<String, String> requestHeader = new HashMap<String, String>();
		String JSESSIONID = in_Parameter.get("JSESSIONID").toString();
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.COOKIE, JSESSIONID);
		InputStream in = SendResultRequest.sendRequestPost(requestHeader, ipAddress, paras.cy_dz, in_Parameter);
		JSONObject json = parseInvoiceResult(in);
		List<ResultBean> list = new ArrayList<ResultBean>();
		if (json.get("result").toString().equals("false")) {
			list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
			out_Result.put(SysConfig.INVOICEFALSESTATE, json.getString(SysConfig.INVOICEFALSESTATE));
			out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		} else {
			String key[] = { "发票代码", "发票号码", "开票时间", "查询次数", "开票金额", "供票机关", "项目名称", "付款单位", "收款单位", "领票单位" };
			list = ResultUtils.getListInfoFromJson(key, json);
			list.add(new ResultBean("CXJG", "查询结果", "查询成功"));
			out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
		}
		out_Result.put("list", list);
	}

	public JSONObject parseInvoiceResult(InputStream in) throws Exception {
		JSONObject json = null;
		String html = "";
		if (in != null) {
			JSONObject result = (JSONObject) SendResultRequest.iSToJSONOrDocument(in, "UTF-8", "json");
			html = result.toString();
			json = new JSONObject();
			json.put("result", false);
			try {
				if (html.contains("该组号码不存在")) {
					json.put("cwxx", "该组号码不存在");
					json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE201);
					return json;
				}
				if (html.contains("验证码错误")) {
					json.put("cwxx", "查询失败，请重试！");
					json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE101);
					return json;
				}
				if (html.contains("该发票不存在")) {
					json.put("cwxx", result.get("msg").toString());
					json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE201);
					return json;
				} else if (html.contains("查询码格式错误")) {
					json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE205);
					json.put("cwxx", "查询码错误，请检查是否输入正确。");
				} else if (html.contains("success")) {
					JSONObject cxjg = result.getJSONObject("data");
					json.put("FPDM", cxjg.get("fpdm").toString());
					json.put("FPHM", cxjg.get("fphm").toString());
					json.put("XMMC", cxjg.get("xmmc").toString());// 项目名称
					json.put("KPSJ", cxjg.get("kpsj").toString());
					json.put("CXCS", cxjg.get("cxcs").toString());
					json.put("KPJE", cxjg.get("kpje").toString());
					json.put("GPJG", cxjg.get("gpjg").toString());
					json.put("FKDW", cxjg.get("fkdw").toString());
					json.put("SKDW", cxjg.get("skdw").toString());
					json.put("LPDW", cxjg.get("lpdw").toString());
					json.put("result", "true");
					json.put("cwxx", "查验成功");
					return json;
				} else {
					json.put("cwxx", "发票查询失败,请稍候重试！");
				}
			} catch (Exception e) {
				System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
				json.put("cwxx", "查询失败，请重试！");
				return json;
			}
		}
		return json;
	}
}
