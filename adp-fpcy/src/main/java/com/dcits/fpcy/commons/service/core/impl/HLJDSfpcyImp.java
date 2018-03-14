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

public class HLJDSfpcyImp implements InvoiceServerBase {
	/**
	 * 黑龙江地税发票查验
	 * 
	 * @param parameter
	 * @param regionNum
	 * @return
	 */
	private Log logger = LogFactory.getLog(HLJDSfpcyImp.class);

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			parameter.put("rand", hscEntity.getYzm());
			parameter.put("JSESSIONID", hscEntity.getCookie());
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("黑龙江地税,获取cook时出现异常", e);
			result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		}
		return result;
	}

	@SuppressWarnings({ "rawtypes" })
	public Map FPCY(Map parameter) throws Exception {
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result, IpAddress ipAddress) throws Exception {
		Map attributeMap = new HashMap();
		Map requestHeader = new HashMap();
		attributeMap.put("num", in_parameter.get("rand").toString());
		attributeMap.put("fp_dm", in_parameter.get("FPDM").toString());
		attributeMap.put("fp_hm", in_parameter.get("FPHM").toString());
		String requestMethod = paras.cy_qqfs;
		String JSESSIONID = in_parameter.get("JSESSIONID").toString();
		int i = JSESSIONID.indexOf(';');
		JSESSIONID = JSESSIONID.substring(11, i);
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.COOKIE, JSESSIONID);
		if ("post".equals(requestMethod)) {
			InputStream in = SendResultRequest.sendRequestPost(requestHeader, ipAddress, paras.cy_dz, attributeMap);
			JSONObject json = parseInvoiceResult(in);
			List<ResultBean> list = new ArrayList<ResultBean>();
			if ("false".equals(json.get("result"))) {
				list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			} else {
				list.add(new ResultBean("LGRQ", "开票日期", json.getString("date")));
				list.add(new ResultBean("NSRSBH", "纳税人名称", json.getString("nsr")));
				list.add(new ResultBean("SWJG", "税务机关", json.getString("swjg")));
				list.add(new ResultBean("FPMC", "发票名称", json.getString("fpmc")));
				list.add(new ResultBean("CXJG", "查询结果", "查验成功"));
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
			}
			out_result.put("list", list);
		}

	}

	@SuppressWarnings({ "unused", "null" })
	public JSONObject parseInvoiceResult(InputStream in) throws Exception {
		JSONObject jso = null;
		JSONObject json = new JSONObject();
		json.put("result", "false");
		Document doc = (Document) SendResultRequest.iSToJSONOrDocument(in, "gbk", "text");
		try {
			if (in != null) {
				jso = new JSONObject();
				String ss = null;
				String html = "";
				Elements eles = doc.select("body").select("table").select("tbody").select("tr").select("td")
						.select("table").select("tbody").select("tr").select("td").select("table").select("tr")
						.select("td");

				jso.put("result", "false");
				jso.put("cwxx", "");
				if (eles.isEmpty()) {
					jso.put("cwxx", "没有查询到发票数据");
				}
				int cnt = 0;
				String Format[] = { "date", "fpmc", "fpdm", "fphm", "swjg", "nsr" };
				for (Element e : eles) {
					if (e.attr("align").compareTo("center") == 0 && e.attr("bgcolor").compareTo("#FFFFFF") == 0) {
						cnt += 1;
					}
				}
				if (cnt != 0) {
					int i = 0;
					for (Element e : eles) {
						if (e.attr("align").compareTo("center") == 0 && e.attr("bgcolor").compareTo("#FFFFFF") == 0) {
							json.put(Format[i % Format.length], e.text());
							i++;
						}
					}
					jso = json;
					jso.put("result", "true");
				} else {
					jso.put("result", "false");
					jso.put("cwxx", "没有查询到发票数据！");
					return jso;
				}
			} else {
				jso.put("cwxx", "没有查询到发票数据");
				jso.put("result", "false");
				return jso;
			}
		} catch (Exception e) {
			System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
			jso.put("cwxx", "没有查询到发票数据");
			jso.put("result", "false");
			return jso;
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception e2) {
				System.out.println(("[ERROR] 程序执行操作出现异常：" + e2.getMessage()));
				json.put("cwxx", "没有查询到发票数据");
				json.put("result", "false");
				return jso;
			}
		}
		return jso;
	}
}
