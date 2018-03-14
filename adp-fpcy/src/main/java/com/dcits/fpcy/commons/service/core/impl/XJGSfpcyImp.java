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
import com.dcits.fpcy.commons.constant.BrowerType;
import com.dcits.fpcy.commons.constant.HeaderType;
import com.dcits.fpcy.commons.constant.SysConfig;
import com.dcits.fpcy.commons.service.core.InvoiceServerBase;
import com.dcits.fpcy.commons.utils.SendResultRequest;

/**
 * 新疆国税 16500
 * 
 * 2017-04-11
 * 
 */
public class XJGSfpcyImp implements InvoiceServerBase {

	private Log logger = LogFactory.getLog(XJGSfpcyImp.class);

	@SuppressWarnings("rawtypes")
	@Override
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("新疆国税,获取cook时出现异常", e);
			List<ResultBean> list = new ArrayList<ResultBean>();
			list.clear();
			list.add(new ResultBean("cwxx", "", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！"));
			result.put("list", list);
			result.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE103);
			result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		}
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result, IpAddress ipAddress)
			throws JSONException {
		Map attributeMap = new HashMap();
		String fplb = in_parameter.get("fplb").toString();
		attributeMap.put("fplb", fplb);
		attributeMap.put("fpdm", in_parameter.get("FPDM").toString());
		attributeMap.put("fphm", in_parameter.get("FPHM").toString());
		attributeMap.put("kpje", in_parameter.get("kpje").toString());
		attributeMap.put("zwcxxh", "");
		Map requestHeader = new HashMap();
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.REFERER,
				"http://www.xj-n-tax.gov.cn:8001/sspt_web/pages/fpyw/fpywFpywcxAction.action");
		InputStream in = SendResultRequest.sendRequestPost(requestHeader, ipAddress, paras.cy_dz, attributeMap);
		JSONObject json = parseInvoiceResult(in, fplb);
		// 新的参数返回方式
		List<ResultBean> list = new ArrayList<ResultBean>();
		try {
			if (json == null) {
				list.add(new ResultBean("CXJG", "查询结果", "发票查询异常，请重试！"));
			} else if (json.get("result").toString().equals("false")) {
				list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
				out_result.put(SysConfig.INVOICEFALSESTATE, json.getString(SysConfig.INVOICEFALSESTATE));
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			} else {
				String nr = json.getString("nr").toString();
				// 20160224增加发票专用类“7”
				if ("3".equals(fplb) || "4".equals(fplb) || "7".equals(fplb) || "11".equals(fplb)) {
					String[] str1 = nr.split(" ");
					list.add(new ResultBean("nsrsbh", "纳税人识别号", str1[0]));
					list.add(new ResultBean("gfnsrmc", "购方纳税人名称", str1[1]));
					list.add(new ResultBean("xfnsrsbh", "销方纳税人识别号", str1[2]));
					list.add(new ResultBean("xfnsrmc", "销方纳税人名称", str1[3]));
					list.add(new ResultBean("rq", "日期", str1[4]));
				} else {
					String[] str = nr.split(" ");
					list.add(new ResultBean("xh", "序号", str[0]));
					list.add(new ResultBean("fpmc", "发票名称", str[1]));
					list.add(new ResultBean("lgdwmc", "领购单位名称", str[2]));
					list.add(new ResultBean("nsrsbh", "纳税人识别号", str[3]));
					list.add(new ResultBean("rq", "日期", str[4].substring(str[4].indexOf(":") + 1)));
				}
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
			}
		} catch (Exception e) {
			logger.error(paras.swjg_mc + "解析返回参数异常", e);
			out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			out_result.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE104);
			e.printStackTrace();
		}
		out_result.put("list", list);
	}

	public static JSONObject parseInvoiceResult(InputStream in, String fplb) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("result", "false");
		if (in == null) {
			json.put("cwxx", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！");
			json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE102);
			return json;
		}
		Document doc = (Document) SendResultRequest.iSToJSONOrDocument(in, "UTF-8", "text");
		String html = doc.toString();

		try {
			if (html.contains("该发票不存在")) {
				json.put("cwxx", "没有查询到此发票信息！");
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE211);
				return json;
			} else if (html.contains("该发票存在，但未查到该发票开具信息")) {

				json.put("cwxx", "该发票存在，但未查到该发票开具信息！");
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE215);
				return json;
			} else if (html.contains("该发票非定额发票")) {
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE211);
				json.put("cwxx", "该发票非定额发票，请选择正确的发票种类进行查询！");
				return json;
			} else {
				Elements init = doc.getElementsByClass("kf_list");
				if ("3".equals(fplb) || "4".equals(fplb) || "11".equals(fplb)) {
					Elements zzs = init.select("td");
					String str = zzs.text();
					json.put("nr", str);
					json.put("result", "true");
				} else if ("1".equals(fplb)) {
					Elements ptfp = init.select("table").select("tr").select("td");
					String nr = ptfp.text();
					json.put("nr", nr);
					json.put("result", "true");
				} else {
					Elements ptfp = init.select("tbody").select("tr").select("td");
					String nr = ptfp.text();
					json.put("nr", nr);
					json.put("result", "true");
				}
				return json;
			}

		} catch (Exception e) {
			System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
			json.put("cwxx", "没有查询到此税控发票信息");
			json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE111);
			return json;
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception e) {
				System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
				json.put("cwxx", "没有查询到此税控发票信息");
			}
		}
	}
}
