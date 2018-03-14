package com.dcits.fpcy.commons.service.core.impl;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONObject;

import com.dcits.fpcy.commons.bean.HSCEntity;
import com.dcits.fpcy.commons.bean.IpAddress;
import com.dcits.fpcy.commons.bean.ResultBean;
import com.dcits.fpcy.commons.bean.TaxOfficeBean;
import com.dcits.fpcy.commons.constant.HeaderType;
import com.dcits.fpcy.commons.constant.SysConfig;
import com.dcits.fpcy.commons.service.core.InvoiceServerBase;
import com.dcits.fpcy.commons.utils.ResultUtils;
import com.dcits.fpcy.commons.utils.SendResultRequest;

public class NBGSfpcyImp implements InvoiceServerBase {
	private Log logger = LogFactory.getLog(NBGSfpcyImp.class);

	/**
	 * 第二版 宁波国税
	 * 
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map FPCY(Map parameter, TaxOfficeBean fppars){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			parameter.put("rand", hscEntity.getYzm());
			parameter.put("JSESSIONID", hscEntity.getCookie());
			// 开始查验
			getResult(fppars, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("宁波国税,获取cook时出现异常", e);
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
	// 获取验证码验真结果
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_Result,
			IpAddress ipAddress) throws Exception {
		Map attributeMap = new HashMap();
		Map<String, String> requestHeader = new HashMap<String, String>();
		String base64 = "action=getColStr4&fpdm="
				+ in_parameter.get("FPDM").toString() + "&fphm="
				+ in_parameter.get("FPHM").toString() + "&rand="
				+ in_parameter.get("rand").toString() + "&kprq="
				+ in_parameter.get("kprq").toString();
		attributeMap.put("params", encode(base64));
		requestHeader.put(HeaderType.COOKIE, in_parameter.get("JSESSIONID")
				.toString());
		InputStream in = SendResultRequest.sendRequestIn(requestHeader,
				ipAddress, attributeMap, paras.cy_dz, paras.cy_qqfs);
		JSONObject json = parseInvoiceResult(in);
		List<ResultBean> list = new ArrayList<ResultBean>();
		if (json.get("result").toString().equals("false")) {
			list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
			out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		} else {
			if(in_parameter.get("FPDM").toString().length() == 10){
				String key[] = {"金额","类别","税额","发票代码","发票号码","开票日期","开票方",
						"受票方名称或税号","货物劳务名称","发票状态"};	
				list = ResultUtils.getListInfoFromJson(key, json);
			}else{
				String key[] = {"金额","类别","税额","发票代码","发票号码","开票日期","销货方名称",
						"销货方识别号","付款方名称","付款方识别号","货物劳务名称","发票状态"};	
				list = ResultUtils.getListInfoFromJson(key, json);
			}
			out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
		}
		out_Result.put("list", list);
	}

	// 获取json对象(里面放验真之后的结果)
	public JSONObject parseInvoiceResult(InputStream in) throws Exception {
		JSONObject json = new JSONObject();
		json.put("result", "true");
		JSONObject doc = (JSONObject) SendResultRequest.iSToJSONOrDocument(in,
				"UTF-8", "json");
		String docre = doc.getString("results");		
		String[] str = docre.split("tr><tr>");
		if(docre.contains("增值税")){
			String key[] = {"FPDM","FPHM","KPRQ","JE","SE","KPF","SPFMCHSH","HWLWMC","LB","FPZT"};
			for(int i = 0;i<str.length;i++) {
				String[] splitStr = str[i].split("：");
				json.put(key[i], splitStr[1].substring(22, splitStr[1].indexOf("<", 22)));
			}	
		}else{
			String key[] = {"FPDM","FPHM","KPRQ","JE","SE","XHFMC","XHFSBH","FKFSBH","FKFMC","HWLWMC","LB","FPZT"};
			for(int i = 0;i<str.length;i++) {
				String[] splitStr = str[i].split("：");
				json.put(key[i], splitStr[1].substring(22, splitStr[1].indexOf("<", 22)));
			}	
		}
		return json;
	}
	public static String encode(String str) throws UnsupportedEncodingException {
		byte[] encodeBase64 = Base64.encodeBase64(str.getBytes("UTF-8"));
		String s = new String(encodeBase64);
		return s;
	}
}
