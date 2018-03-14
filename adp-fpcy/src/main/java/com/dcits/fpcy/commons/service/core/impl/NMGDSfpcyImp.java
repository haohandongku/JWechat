package com.dcits.fpcy.commons.service.core.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONObject;
import org.jsoup.nodes.Document;

import com.dcits.fpcy.commons.bean.HSCEntity;
import com.dcits.fpcy.commons.bean.IpAddress;
import com.dcits.fpcy.commons.bean.ResultBean;
import com.dcits.fpcy.commons.bean.TaxOfficeBean;
import com.dcits.fpcy.commons.constant.BrowerType;
import com.dcits.fpcy.commons.constant.HeaderType;
import com.dcits.fpcy.commons.constant.SysConfig;
import com.dcits.fpcy.commons.service.core.InvoiceServerBase;
import com.dcits.fpcy.commons.utils.SendResultRequest;

public class NMGDSfpcyImp implements InvoiceServerBase {

	private Log logger = LogFactory.getLog(BJDSfpcyImp.class);

	/**
	 * 第二版 内蒙古地税
	 * 
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map FPCY(Map parameter, TaxOfficeBean fppars){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			// 开始查验
			getResult(fppars, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("内蒙古地税,获取cook时出现异常", e);
			List list = new ArrayList<ResultBean>();
			list.clear();
			list.add(new ResultBean("cwxx", "", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！"));
			result.put("list", list);
			result.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE103);
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
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result, IpAddress ipAddress) throws Exception {
		Map attributeMap = new HashMap();
		Map<String, String> requestHeader = new HashMap<String, String>();
		attributeMap.put("code", in_parameter.get("FPDM").toString());
		attributeMap.put("number", in_parameter.get("FPHM").toString());
		attributeMap.put("sjc", String.valueOf(System.currentTimeMillis()));
		// 这里设置JSESSIONID为空
		String JSESSIONID = null;
		String requestMethod = paras.cy_qqfs;
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.COOKIE, JSESSIONID);
		requestHeader.put("Origin", "http://www.nmds.gov.cn:9999");
		requestHeader.put("Referer", "http://www.nmds.gov.cn:9999/invoice-index.jsp");
		requestHeader.put("X-Requested-With", "XMLHttpRequest");
		requestHeader.put("Accept", "*/*");
		InputStream in = SendResultRequest.sendRequestIn(requestHeader, ipAddress, attributeMap, paras.cy_dz,
				paras.cy_qqfs);// 开始发送请求
		JSONObject json = parseInvoiceResult(in);
		List<ResultBean> list = new ArrayList<ResultBean>();
		if (("true").equals(json.getString("result"))) {
			list.add(new ResultBean("CXNR", "查询结果", json.getString("CXNR").replace("\n", "")));
			out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
		} else {
			list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
			out_result.put(SysConfig.INVOICEFALSESTATE, json.getString(SysConfig.INVOICEFALSESTATE));
			out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		}
		out_result.put("list", list);
	}

	@SuppressWarnings("unused")
	public JSONObject parseInvoiceResult(InputStream in) throws Exception {
		JSONObject jso = null;
		JSONObject json = new JSONObject();
		json.put("result", "false");
		Document doc = (Document) SendResultRequest.iSToJSONOrDocument(in, "utf-8", "text");
		String html = doc.toString();
		try {
			if (in != null) {
				jso = new JSONObject();
				if (html.contains("系统没有查询到相应发票信息")) {
					json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE201);
					json.put("cwxx", "您好,系统没有查询到相应发票信息,请向主管税务机关核实！");
				} else {
					json.put("result", "true");
					String value = html.substring(html.indexOf("您好"), html.indexOf("</body>"));
					json.put("CXNR", value);
				}
			} else {
				json.put("cwxx", "您输入的发票信息不存在！");
			}
		} catch (Exception e) {
			System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
			json.put("cwxx", "您输入的发票信息不存在！");
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception e) {
				System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
				json.put("cwxx", "您输入的发票信息不存在！");
			}
		}
		return json;
	}
}
