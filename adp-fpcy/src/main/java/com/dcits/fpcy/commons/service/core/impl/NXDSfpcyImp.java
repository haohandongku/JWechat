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

public class NXDSfpcyImp implements InvoiceServerBase {

	private Log logger = LogFactory.getLog(NXDSfpcyImp.class);
	/**
	 * 第二版 宁夏地税
	 * 
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map FPCY(Map parameter, TaxOfficeBean fppars){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			if(hscEntity.getCookie() == null) {
				parameter.put("JSESSIONID", hscEntity.getCookie1());	
			}else{
				parameter.put("JSESSIONID", hscEntity.getCookie());	
			}
			parameter.put("rand", hscEntity.getYzm());
			// 开始查验
			getResult(fppars, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("宁夏地税,获取cook时出现异常", e);
			List list = new ArrayList<ResultBean>();
			list.clear();
			list.add(new ResultBean("cwxx", "", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！"));
			result.put("list", list);
			result.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE103);
			result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		}
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result,
			IpAddress ipAddress) throws Exception{
		Map attributeMap = new HashMap();
		Map requestHeader = new HashMap();
		attributeMap.put("fpdm", in_parameter.get("FPDM").toString());
		attributeMap.put("fphm", in_parameter.get("FPHM").toString());
		attributeMap.put("cxm", in_parameter.get("cxm").toString());
		attributeMap.put("verificationCode", in_parameter.get("rand").toString().toLowerCase());
		String requestMethod = paras.cy_qqfs;
		String JSESSIONID = in_parameter.get("JSESSIONID").toString();
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.COOKIE, JSESSIONID);
		attributeMap.put("JSESSIONID", JSESSIONID);
		if ("post".equals(requestMethod)) {
			InputStream in = SendResultRequest.sendRequestPost(requestHeader,
					ipAddress, paras.cy_dz, attributeMap);
			JSONObject json = parseInvoiceResult(in);
			List<ResultBean> list = new ArrayList<ResultBean>();
			if (json == null||("false").equals(json.get("result").toString())) {
				list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
				out_result.put(SysConfig.INVOICEFALSESTATE,json.getString(SysConfig.INVOICEFALSESTATE));
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			} else {
				list.add(new ResultBean("发票号码", json.get("fphm").toString()));
				list.add(new ResultBean("开具项目", json.get("kjxm_mc").toString()));
				list.add(new ResultBean("收款方", json.get("skf_mc").toString()));
				list.add(new ResultBean("金额大写", json.get("kpjedx").toString()));
				list.add(new ResultBean("开票日期", json.get("kprq").toString()));
				list.add(new ResultBean("开票金额", json.get("kpje").toString()+"元"));
				list.add(new ResultBean("付款方", json.get("fkf_mc").toString()));
				list.add(new ResultBean("查询结果", json.get("cyjg").toString()));
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
			}
			out_result.put("list", list);
		}
	}

	/**
	 * 宁夏地税结果解析
	 */
	public JSONObject parseInvoiceResult(InputStream in) throws Exception{
		JSONObject json = new JSONObject();
		json.put("result", "false");
		Document doc = (Document) SendResultRequest.iSToJSONOrDocument(in,
				"UTF-8", "text");
		String result = doc.toString();
		try {
			if (in != null) {
                if (result.contains("疑为假票")) {
					String res = doc.select("font").text();
					json.put("result", "false");
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE201);
					json.put("cwxx", res);
				}else if(result.contains("error")&&result.contains("-1")){
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE101);
					json.put("cwxx", "发票查询失败，请稍候重试！");
				}else if(result.contains("error")&&result.contains("-2")){
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE201);
					json.put("cwxx", "该发票初步鉴定未通过，未查询到该发票代码、发票号码对应的开具信息。");
				}else if (result.contains("fpxx")) {
					String body = doc.select("body").text();
					json.put("result", "true");
					JSONObject jso = new JSONObject(body);
					String fpxx = jso.getString("fpxx");
					JSONObject jso1 = new JSONObject(fpxx);
					String[] key = { "fphm", "kjxm_mc", "skf_mc", "fp_dm",
							"kpjedx", "kprq", "kpje", "fkf_mc" };
					for (int i = 0; i < jso1.length(); i++) {
						json.put(key[i], jso1.get(key[i]));
					}
					json.put("cyjg", "该发票初步鉴定通过");
				} else {
					json.put("cwxx", "发票查询失败，请稍候重试！");
				}
			} else {
				json.put("cwxx", "您输入的发票信息不匹配，请检查是否输入正确！");
				json.put("result", "false");
			}
		} catch (Exception e) {
			json.put("cwxx", "您输入的发票信息不匹配，请检查是否输入正确！");
			json.put("result", "false");
			e.printStackTrace();
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception e2) {
				json.put("cwxx", "您输入的发票信息不匹配，请检查是否输入正确！");
				json.put("result", "false");
				e2.printStackTrace();
			}
		}
		return json;
	}
}
