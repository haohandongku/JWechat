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

public class JSDSfpcyImp implements InvoiceServerBase {

	private Log logger = LogFactory.getLog(JSDSfpcyImp.class);

	/**
	 * 第二版 江苏地税
	 * 
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			String jessionID = hscEntity.getCookie1();
			if (jessionID == null) {
				jessionID = hscEntity.getCookie();
			}
			jessionID = jessionID.substring(0, jessionID.indexOf(";"));
			parameter.put("JSESSIONID", jessionID);
			parameter.put("rand", hscEntity.getYzm());
			// 开始查验
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("江苏地税,获取cook时出现异常", e);
			List<ResultBean> list = new ArrayList<ResultBean>();
			list.clear();
			list.add(new ResultBean("cwxx", "", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！"));
			result.put("list", list);
			result.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE103);
			result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		}
		return result;
	}

	public void getResult(TaxOfficeBean paras, Map<String, Object> in_Parameter, Map<String, Object> out_Result,
			IpAddress ipAddress) throws Exception {
		Map<String, String> attributeMap = new HashMap<String, String>();
		Map<String, String> requestHeader = new HashMap<String, String>();
		attributeMap.put("cxfs", "0");
		attributeMap.put("hideshow_tm1", "0");
		attributeMap.put("fpdm", in_Parameter.get("FPDM").toString());
		attributeMap.put("fphm", in_Parameter.get("FPHM").toString());
		attributeMap.put("kprq", in_Parameter.get("kprq").toString());
		attributeMap.put("jine", in_Parameter.get("jine").toString());
		attributeMap.put("INVOICE_CHECKING_CHECKCODE", "6002");
		attributeMap.put("fptxm", "");
		attributeMap.put("yzm", in_Parameter.get("rand").toString());
		String JSESSIONID = in_Parameter.get("JSESSIONID").toString();
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.COOKIE, JSESSIONID);
		InputStream in = SendResultRequest.sendRequestPost(requestHeader, ipAddress, paras.cy_dz, attributeMap);
		JSONObject json = parseInvoiceResult(in);
		List<ResultBean> list = new ArrayList<ResultBean>();
		try {
			if (json.get("result").toString().equals("false")) {
				list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
				out_Result.put(SysConfig.INVOICEFALSESTATE, json.getString(SysConfig.INVOICEFALSESTATE));
				out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			} else {
				list.add(new ResultBean("FPDM", "发票代码", in_Parameter.get("FPDM").toString()));
				list.add(new ResultBean("FPHM", "发票号码", in_Parameter.get("FPHM").toString()));
				list.add(new ResultBean("KPRQ", "开票日期", in_Parameter.get("kprq").toString()));
				list.add(new ResultBean("KPJE", "开票金额", in_Parameter.get("jine").toString()));
				list.add(new ResultBean("FKFMC", "付款方名称", json.getString("付款方名称")));
				list.add(new ResultBean("SKFMC", "收款方名称", json.getString("收款方名称")));
				list.add(new ResultBean("SKFSBH", "收款方识别号", json.getString("收款方识别号")));
				list.add(new ResultBean("KPFMC", "开票方名称", json.getString("开票方名称")));
				list.add(new ResultBean("KPFSBM", "开票方识别号", json.getString("开票方识别号")));
				list.add(new ResultBean("CXJG", "查询结果", "查询成功"));
				out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
			}
		} catch (Exception e) {
			logger.error(paras.swjg_mc + "解析返回参数异常", e);
			out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			out_Result.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE104);
			e.printStackTrace();
		}
		out_Result.put("list", list);
	}

	public JSONObject parseInvoiceResult(InputStream in) throws Exception {
		JSONObject json = new JSONObject();
		json.put("result", "false");
		String html = "";
		if (in == null) {
			json.put("cwxx", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！");
			json.put(SysConfig.INVOICEFALSESTATE, "102");
			return json;
		}
		Document doc = (Document) SendResultRequest.iSToJSONOrDocument(in, "GBK", "text");
		html = doc.toString();
		// System.out.println(html);
		try {
			json.put("result", "false");
			Elements yzm = doc.select("input");
			for (Element eyzm : yzm) {
				if (eyzm.attr("name").compareTo("txm_error") == 0 && eyzm.attr("value").compareTo("验证码错误！") == 0) {

					json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE101);
					json.put("cwxx", "查询失败，请重试！");
					return json;
				}
			}

			Element element = doc.getElementById("pm_middle");
			if (null == element) {
				if (html.contains("信息不一致") || html.contains("未查询到发票")) {
					json.put("cwxx", "您输入的开票信息与数据库记录开票信息不一致！");
					json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE211);
					return json;
				} else if (html.contains("核实发票真伪")) {
					json.put("cwxx", "发票种类无法界定，请至主管税务机关进一步核实发票真伪情况！");
					json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE212);
					return json;
				} else if (html.contains("未查询到发票")) {
					json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE211);
					json.put("cwxx", "未查询到发票开具信息");
					return json;
				} else {
					json.put("cwxx", "查询失败，请重试！");
					json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE111);
					return json;
				}
			}
			Elements elements = element.select("td");
			int[] indexes = { 1, 3, 5, 7, 9 }; // 数组里存放的是解析过后所需的组件的索引
			String[] nameOfIndexes = { "付款方名称", "收款方名称", "收款方识别号", "开票方名称", "开票方识别号" };
			for (int i = 0; i < indexes.length; i++) {
				json.put(nameOfIndexes[i], elements.get(indexes[i]).text().toString());
			}
			json.put("result", "true");
			json.put("cwxx", "查验成功");
		} catch (Exception e) {
			json.put("result", "false");
			json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE111);
			json.put("cwxx", "您输入的信息有误，请重新输入！");
		}
		return json;
	}
}
