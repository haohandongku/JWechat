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
import org.jsoup.select.Elements;

import com.dcits.fpcy.commons.bean.HSCEntity;
import com.dcits.fpcy.commons.bean.IpAddress;
import com.dcits.fpcy.commons.bean.ResultBean;
import com.dcits.fpcy.commons.bean.TaxOfficeBean;
import com.dcits.fpcy.commons.constant.HeaderType;
import com.dcits.fpcy.commons.constant.SysConfig;
import com.dcits.fpcy.commons.service.core.InvoiceServerBase;
import com.dcits.fpcy.commons.utils.SendResultRequest;

public class SZGSfpcyImp implements InvoiceServerBase {
	private Log logger = LogFactory.getLog(QDGSfpcyImp.class);

	/**
	 * 深圳国税发票查验
	 * 
	 * @param parameter
	 * @param regionNum
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			parameter.put("rand", hscEntity.getYzm().toLowerCase());
			parameter.put("JSESSIONID", hscEntity.getCookie1()); // 开始查验
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("深圳国税,获取cook时出现异常", e);
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

	public void getResult(TaxOfficeBean paras, Map<String, Object> in_Parameter,
			Map<String, Object> out_Result, IpAddress ipAddress)
			throws JSONException {
		Map<String, String> attributeMap = new HashMap<String, String>();
		Map<String, String> requestHeader = new HashMap<String, String>();
		
		attributeMap.put("fpdm", in_Parameter.get("ptfpView.fpdm")
				.toString());
		attributeMap.put("fphm", in_Parameter.get("ptfpView.fphm")
				.toString());
		attributeMap.put("kpfzjh", in_Parameter.get("ptfpView.xhfsbh")
				.toString());
		attributeMap.put("jym", "");
		attributeMap.put("tagger", in_Parameter.get("rand")
				.toString());
		String JSESSIONID = in_Parameter.get("JSESSIONID").toString();
		//requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.COOKIE, JSESSIONID.split("; ")[0]);
		requestHeader.put("Host", "dzswj.szgs.gov.cn");
		requestHeader.put("Origin", "http://dzswj.szgs.gov.cn");
		requestHeader.put("Referer", "http://dzswj.szgs.gov.cn/BsfwtWeb/apps/views/fp/fpcy/fp_fpcy.html");
		requestHeader.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
		InputStream in = SendResultRequest.sendRequestPost(requestHeader,
				ipAddress, paras.cy_dz, attributeMap);
		JSONObject json = parseInvoiceResult(in, in_Parameter);
		List<ResultBean> list = new ArrayList<ResultBean>();
		try {
			if ("false".equals(json.get("result"))) {
				list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
				out_Result.put(SysConfig.INVOICEFALSESTATE,json.getString(SysConfig.INVOICEFALSESTATE));
				out_Result.put(SysConfig.CYJGSTATE,SysConfig.CODE20011);
			} else {
				if(json.get("key").equals("1")){
					list.add(new ResultBean("XHFSBH", "纳税人名称", json.getString("纳税人名称")));
					list.add(new ResultBean("XHFMC", "发票名称", json.getString("发票名称")));
					list.add(new ResultBean("FPDM", "发票代码", json.getString("发票代码")));
					list.add(new ResultBean("FPHM", "发票号码", json.getString("发票号码")));
					list.add(new ResultBean("LPRQ", "领购日期", json.getString("领购日期")));
				}else{
					list.add(new ResultBean("XHFSBH", "销售方识别号", json.getString("销货方识别号")));
					list.add(new ResultBean("XHFMC", "销货方名称", json.getString("销货方名称")));
					list.add(new ResultBean("FPDM", "发票代码", json.getString("发票代码")));
					list.add(new ResultBean("FPHM", "发票号码", json.getString("发票号码")));
					list.add(new ResultBean("LPRQ", "领购日期", json.getString("领购日期")));
					list.add(new ResultBean("YJRQ", "验旧日期", json.getString("验旧日期")));
					list.add(new ResultBean("HJJE", "合计金额", json.getString("合计金额")));
				}
				out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
			}
		} catch (Exception e) {
			logger.error(paras.swjg_mc + "解析返回参数异常", e);
			out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			out_Result.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE104);
			e.printStackTrace();
		}
		out_Result.put("list", list);
	}

	@SuppressWarnings("rawtypes")
	public JSONObject parseInvoiceResult(InputStream in, Map in_Parameter)
			throws JSONException {
		JSONObject json = new JSONObject();
		json.put("result", "false");
		String html = "";
		if (in == null) {
			json.put("cwxx", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！");
			json.put(SysConfig.INVOICEFALSESTATE, "102");
			return json;
		}
		Document doc = (Document) SendResultRequest.iSToJSONOrDocument(in,
				"UTF-8", "text");
		html = doc.toString();
		try {
				if (html.contains("您输入的效验码有误")) {
					json.put("cwxx", "您输入的效验码有误或改发票不存在");
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE203);
					return json;
				}
				if (html.contains("没找到识别号")) {
					json.put("cwxx", "没找到识别号为"
							+ in_Parameter.get("SBH").toString() + "的纳税人");
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE203);
					return json;
				}
				if (html.contains("该发票为非法领购发票")) {
					json.put("cwxx", "该发票为非法领购发票!");
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE203);
					return json;
				}
				if (html.contains("深圳市国家税务局没有发售过该发票")) {
					json.put("cwxx", "深圳市国家税务局没有发售过该发票");
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE201);
					return json;
				} else {
					Elements eles = doc.select("tbody").select("tr")
							.select("td");
					if(eles.toString().contains("请输入发票校验码")){
						int[] indexes = { 1,3, 5, 7, 9}; // 数组里存放的是解析过后所需的组件的索引
						String[] nameOfIndexes = { "发票名称", "发票代码", "发票号码",
								"纳税人名称", "领购日期"};
						for (int i = 0; i < indexes.length; ++i) {
							json.put(nameOfIndexes[i], eles.get(indexes[i]).text()
									.replace(" ", ""));
						}
						json.put("key", "1");
					}else{
						int[] indexes = { 3, 5, 7, 9, 11, 13, 15 }; // 数组里存放的是解析过后所需的组件的索引
						String[] nameOfIndexes = { "销货方识别号", "销货方名称", "发票代码",
								"发票号码", "领购日期", "验旧日期", "合计金额" };
						for (int i = 0; i < indexes.length; ++i) {
							json.put(nameOfIndexes[i], eles.get(indexes[i]).text()
									.replace(" ", ""));
						}
						json.put("key", "2");
					}
					json.put("cwxx", "查验成功");
					json.put("result", "true");
				}
				return json;
		} catch (Exception e) {
			System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
			json.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE111);
			json.put("cwxx", "您输入的信息有误，请重新输入！");
		}
		return json;
	}
}
