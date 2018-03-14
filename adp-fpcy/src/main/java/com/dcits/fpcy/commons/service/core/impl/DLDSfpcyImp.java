package com.dcits.fpcy.commons.service.core.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.jsoup.nodes.Document;

import com.dcits.fpcy.commons.bean.HSCEntity;
import com.dcits.fpcy.commons.bean.IpAddress;
import com.dcits.fpcy.commons.bean.ResultBean;
import com.dcits.fpcy.commons.bean.TaxOfficeBean;
import com.dcits.fpcy.commons.constant.HeaderType;
import com.dcits.fpcy.commons.constant.SysConfig;
import com.dcits.fpcy.commons.service.core.InvoiceServerBase;
import com.dcits.fpcy.commons.utils.ResultUtils;
import com.dcits.fpcy.commons.utils.SendResultRequest;

public class DLDSfpcyImp implements InvoiceServerBase {

	private Log logger = LogFactory.getLog(DLDSfpcyImp.class);

	/**
	 * 大连地税发票查验
	 * 
	 * @param parameter
	 * @param regionNum
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			parameter.put("yzm", hscEntity.getYzm());
			parameter.put("JSESSIONID", hscEntity.getCookie());
			// 开始查验
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("大连地税,获取cook时出现异常", e);
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

	/**
	 * 
	 * @param paras
	 *            查验参数
	 * @param in_Parameter
	 *            请求数据 [ 验证码 jessionid]
	 * @param out_Result
	 *            返回结果
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result,
			IpAddress ipAddress) throws Exception {
		Map<String, String> requestHeader = new HashMap<String, String>();
		String requestMethod = paras.cy_qqfs;
		requestHeader.put(HeaderType.COOKIE, in_parameter.get("JSESSIONID")
				.toString());
		requestHeader.put(HeaderType.REQUESTTYPE, "AJAX_REQUEST");
		InputStream in = SendResultRequest.sendRequestIn(requestHeader,
				ipAddress,in_parameter, paras.cy_dz, paras.cy_qqfs);
		JSONObject json = parseInvoiceResult(in);
		List<ResultBean> list = new ArrayList<ResultBean>();
		try {
			if (json == null) {
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			} else if (json.getString("result").equals("true")) {
				String key[] = {"发票代码","发票号码","开票日期","开具金额","付款方名称","收款方名称","发票名称",
						"发票状态","收款方识别号"};
				list = ResultUtils.getListInfoFromJson(key, json);
				list.add(new ResultBean("CXJG", "查询结果", "查询成功"));
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
			} else {
				list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
				out_result.put(SysConfig.INVOICEFALSESTATE,json.getString(SysConfig.INVOICEFALSESTATE));
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			}
		} catch (Exception e) {
			logger.error(paras.swjg_mc + "解析返回参数异常", e);
			out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			out_result.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE104);
			e.printStackTrace();
		}
		out_result.put("list", list);
	}

	public JSONObject parseInvoiceResult(InputStream in) throws Exception {
		JSONObject json = new JSONObject();
		json.put("result", "false");
		if (in == null) {
			json.put("cwxx", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！");
			json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE102);
			return json;
		}
		try {
				Document doc = (Document) SendResultRequest.iSToJSONOrDocument(
						in, "GBK", "text");
				String html = doc.toString();
				if (html.contains("您输入的查询信息有误")) {
					json.put("cwxx",
							"您输入的查询信息有误，请认真核实，如有疑义或需鉴定发票真伪，请拨打12366-2咨询。 ");
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE211);
					return json;
				}
				if (html.contains("验证码输入错误，请重新输入！")) {
					json.put("cwxx", "发票查询失败，请稍后重试！");
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE101);
					return json;
				}
				String body = doc.select("body").get(0).text();
				JSONObject result = new JSONObject(body);
				JSONArray ja = result.getJSONArray("listMap");
				json.put("FPMC", ja.getJSONObject(0).getString("value"));
				json.put("FPDM", ja.getJSONObject(2).getString("value"));
				json.put("KPRQ", ja.getJSONObject(1).getString("value"));
				json.put("FPHM", ja.getJSONObject(3).getString("value"));
				json.put("FKFMC", ja.getJSONObject(4).getString("value"));
				json.put("KJJE", ja.getJSONObject(6).getString("value"));
				json.put("SKFMC", ja.getJSONObject(7).getString("value"));
				json.put("FPZT", ja.getJSONObject(8).getString("value"));
				json.put("SKFSBH", ja.getJSONObject(9).getString("value"));
				json.put("result", "true");
				return json;
		} catch (Exception e) {
			logger.error("", e);
			json.put("cwxx", "发票查询时出现异常，请重试");
			json.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE111);
			return json;
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception e2) {
				logger.error("", e2);
				json.put("cwxx", "您输入的发票信息不存在！");
				return json;
			}
		}
	}
}
