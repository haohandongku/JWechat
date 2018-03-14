package com.dcits.fpcy.commons.service.core.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jsoup.Jsoup;
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

/**
 * 新疆地税，新疆地税26500，
 * 
 */
public class XJDSfpcyImp implements InvoiceServerBase {
	private Log logger = LogFactory.getLog(XJDSfpcyImp.class);

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			parameter.put("JSESSIONID", hscEntity.getCookie());
			parameter.put("rand", hscEntity.getYzm());
			// 开始查验
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());

		} catch (Exception e) {
			logger.error("新疆地税,获取cook时出现异常", e);
			List list = new ArrayList<ResultBean>();
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
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result, IpAddress ipAddress)
			throws JSONException {
		Map attributeMap = new HashMap();
		attributeMap.put("fpdm", in_parameter.get("FPDM").toString());
		attributeMap.put("fphm", in_parameter.get("FPHM").toString());
		attributeMap.put("swkfjm", in_parameter.get("rand").toString());
		attributeMap.put("find1", "查询");
		attributeMap.put("clear1", "清除");
		attributeMap.put("bb", "真伪鉴别");
		attributeMap.put("swkfjmSome", "");
		attributeMap.put("find2", "查询");
		attributeMap.put("clear2", "清除");
		attributeMap.put("handleDesc", "单张票据情况查询");
		attributeMap.put("handleCode", "QueryData");
		attributeMap.put("errorMessage", "");
		attributeMap.put("sucessMsg", "");
		String ccs = "&fphm0=&fpdm0=&fphm0=&fpdm0=&fphm0=&fpdm0=&fphm0=&fpdm0=&fphm0=&fpdm0=&fphm0=&fpdm0=&fphm0=&fpdm0=&fphm0=&fpdm0=&fphm0=&fpdm0=&fphm0=";
		attributeMap.put("fpdm0", ccs);
		String JSESSIONID = in_parameter.get("JSESSIONID").toString();
		Map requestHeader = new HashMap();
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.COOKIE, JSESSIONID);
		InputStream in = SendResultRequest.sendRequestPost(requestHeader, ipAddress, paras.cy_dz, attributeMap);
		JSONObject json = parseInvoiceResult(in);
		// 新的返回方式
		List<ResultBean> list = new ArrayList<ResultBean>();
		try {
			if (json == null) {
				list.add(new ResultBean("CXJG", "查询结果", "发票查询异常，请重试！"));
			} else if ("false".equals(json.get("result"))) {
				list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			} else {
				Iterator it = json.keys();
				String[] name = { "结果", "日期", "金额", "机关名称", "开票方", "发票类型", "开票日期", "收票方" };
				int i = 0;
				while (it.hasNext()) {
					String key = String.valueOf(it.next());
					String value = (String) json.get(key);
					if ("true".equals(value)) {
						value = "查询成功";
					}
					list.add(new ResultBean(key, name[i], value));
					i++;
				}
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
			}
		} catch (Exception e) {
			logger.error("新疆地税解析JSONObject异常：" + e);
			logger.error(paras.swjg_mc + "解析返回参数异常", e);
			out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			out_result.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE104);
			e.printStackTrace();
		}
		out_result.put("list", list);
	}

	public JSONObject parseInvoiceResult(InputStream in) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("result", "false");
		Document document = (Document) SendResultRequest.iSToJSONOrDocument(in, "UTF-8", "text");
		if (in == null) {
			json.put("cwxx", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！");
			json.put(SysConfig.INVOICEFALSESTATE, "102");
			return json;
		}
		String html = document.toString();
		try {
			Document doc = Jsoup.parse(html);
			Elements fseleDiv = doc.select("div #fs").select("tr");
			Elements eleDiv = doc.select("div #kp").select("tr");
			if (fseleDiv.size() > 2) {
				Element ele = fseleDiv.get(2);
				String str = ele.text();
				String[] strs = str.split(" ");
				json.put("fsrq", strs[1]);
				json.put("je", "定额");
				json.put("fsjgmc", strs[3]);
				json.put("skfmc", strs[4]);
				String fplx = ele.select("input").get(1).val();
				json.put("result", "true");
				json.put("fplx", fplx);
				json.put("kprq", "无");
				json.put("fkfmc", "无");
				if (eleDiv.size() > 2) {
					Element fele = eleDiv.get(2);
					String fstr = fele.text();
					String[] fstrs = fstr.split(" ");
					json.put("kprq", fstrs[1]);
					json.put("je", fstrs[2]);
					if (fstrs.length >= 5)
						json.put("fkfmc", fstrs[4]);
				}
			} else {
				json.put("cwxx", "没有查询到此税控发票信息");
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE213);
				return json;
			}
		} catch (Exception e) {
			logger.error("新疆国税获取解析异常", e);
			json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE111);
			json.put("cwxx", "此种票样查询发生异常，请稍后重试");
		} finally {
		}
		return json;
	}
}
