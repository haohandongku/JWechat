package com.dcits.fpcy.commons.service.core.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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
import com.dcits.fpcy.commons.constant.HeaderType;
import com.dcits.fpcy.commons.constant.SysConfig;
import com.dcits.fpcy.commons.service.core.InvoiceServerBase;
import com.dcits.fpcy.commons.utils.SendResultRequest;
import com.dcits.fpcy.commons.utils.TrustAllHosts;
 
/**
 * 湖北国税 14201
 *2017-04-12
 */
public class HBGSfpcyImp implements InvoiceServerBase {
	private Log logger = LogFactory.getLog(HBGSfpcyImp.class);
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			String rand = hscEntity.getYzm();
			parameter.put("rand", rand);
			if(StringUtils.isEmpty(hscEntity.getCookie())) {
				parameter.put("JSESSIONID", hscEntity.getCookie1());
			}else{
				parameter.put("JSESSIONID", hscEntity.getCookie());
			}
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("湖北国税,获取cook时出现异常", e);
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
	
	@SuppressWarnings({ "rawtypes" })
	public Map FPCY(Map parameter) throws Exception {
		
		return null;
	}

	public String toUtf8String(String s) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c >= 0 && c <= 255) {
				sb.append(c);
			} else {
				byte[] b;
				try {
					b = String.valueOf(c).getBytes("utf-8");
				} catch (Exception ex) {
					System.out.println(ex);
					b = new byte[0];
				}
				for (int j = 0; j < b.length; j++) {
					int k = b[j];
					if (k < 0)
						k += 256;
					sb.append("%" + Integer.toHexString(k).toUpperCase());
				}
			}
		}
		return sb.toString();
	}

	@SuppressWarnings({ "unchecked", "rawtypes", })
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result,
			IpAddress ipAddress) throws Exception{
		Map attributeMap = new HashMap();
		Map requestHeader = new HashMap();
		String JSESSIONID = in_parameter.get("JSESSIONID").toString();
		int i = JSESSIONID.indexOf(';');
		String ssid = JSESSIONID.substring(11, i);
		String yzm = toUtf8String(in_parameter.get("rand").toString());
		attributeMap.put("ssid", ssid);
		attributeMap.put("fp_code", in_parameter.get("FPDM").toString());
		attributeMap.put("fp_number", in_parameter.get("FPHM").toString());
		attributeMap.put("verify_code", yzm);
		attributeMap.put("x", "32");
		attributeMap.put("y", "16");
		requestHeader.put(HeaderType.USERAGENT, "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
		requestHeader.put(HeaderType.COOKIE, JSESSIONID.split("; ")[0]);
		
		
		String requestMethod = paras.cy_qqfs;
		List<ResultBean> list = new ArrayList<ResultBean>();
		if ("post".equals(requestMethod)) {
			JSONObject json= null;
			if(paras.swjg_dm.equals("4200")) {
				Map attributeMap1 = new HashMap();
				attributeMap1.put("str1", "");
				attributeMap1.put("str2", in_parameter.get("FPDM").toString());
				attributeMap1.put("str3", in_parameter.get("FPHM").toString());
				String md5v = null;
				TrustAllHosts.trustAllHosts();
				InputStream in = SendResultRequest.sendRequestIn(requestHeader,
						null,  attributeMap1,"https://swcx.hb-n-tax.gov.cn:7013/include1/fpcxjm.jsp","get");	
				md5v =  parseInvoiceResult1(in);
				Map attributeMap2 = new HashMap();
				requestHeader.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
				requestHeader.put("Accept-Encoding", "gzip, deflate, br	");
				requestHeader.put("Accept-Language", "zh-CN,zh;q=0.8");
				requestHeader.put("Cache-Control", "max-age=0");
				requestHeader.put("Connection", "keep-alive");
				requestHeader.put("Content-Type", "application/x-www-form-urlencoded");
				requestHeader.put("Host", "swcx.hb-n-tax.gov.cn:7013");
				requestHeader.put("Origin", "https://swcx.hb-n-tax.gov.cn:7013");
				requestHeader.put("Referer", "https://swcx.hb-n-tax.gov.cn:7013/include1/cx_sgfplxcx.jsp");
				requestHeader.put("Upgrade-Insecure-Requests", "1");
				
				attributeMap2.put("md5v", md5v);
				attributeMap2.put("ywlx", "FPCX_LXCX");
				attributeMap2.put("ywlxbf", "FPCX_LXCX");
				attributeMap2.put("cxbz", "lscx");
				attributeMap2.put("kjfsbh", "");
				attributeMap2.put("fpdm", in_parameter.get("FPDM").toString());
				attributeMap2.put("fphm", in_parameter.get("FPHM").toString());
				attributeMap2.put("rq", in_parameter.get("kprq").toString().replace("-", ""));
				attributeMap2.put("je", in_parameter.get("kpje").toString());
				attributeMap2.put("kaptchafield", yzm.toLowerCase());
				TrustAllHosts.trustAllHosts();
				InputStream in1 = SendResultRequest.sendRequestPost(requestHeader,
						null, paras.cy_dz, attributeMap2);	
				json = parseInvoiceResult(in1);
			}else{
			InputStream in = SendResultRequest.sendRequestPost(requestHeader,
					ipAddress, paras.cy_dz, attributeMap);
			json = parseInvoiceResult(in);
			String newurl=json.get("newurl").toString();
			in=SendResultRequest.sendRequestIn(requestHeader,
					ipAddress,attributeMap, newurl,"GET");
			json = parseInvoiceResult(in);
			}
			if ("false".equals(json.get("result"))) {
				
				list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
				out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE20011);
			} else {
				if(paras.swjg_dm.equals("4200")) {
					list.add(new ResultBean("MSG","查询结果", json.get("cwxx").toString()));
					out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE1000);
				}else if ("".equals(json.get("纳税人名称："))) {
					list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
					out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE1000);
				} else {
					list.add(new ResultBean("NSRMC","纳税人名称", json.get("纳税人名称：").toString()));
					list.add(new ResultBean("FPMC", "发票名称", json.get("发票名称：").toString()));
					list.add(new ResultBean("FSRQ","发售日期",  json.get("发售日期：").toString()));
					list.add(new ResultBean("MSG","查询结果", json.get("jg").toString()));
					out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE1000);
				}
			}
		}
		out_result.put("list", list);
	}
	private String parseInvoiceResult1(InputStream in) {
		Document doc = (Document) SendResultRequest.iSToJSONOrDocument(in,
				"UTF-8", "text");
		String result = doc.select("body").text();
		if (in != null){
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		};
		return result;
	}

	public JSONObject parseInvoiceResult(InputStream in) throws Exception {
		JSONObject json = new JSONObject();
		json.put("result", "false");
		Document doc = (Document) SendResultRequest.iSToJSONOrDocument(in,
				"GBK", "text");
		String result = doc.toString();
		String url="http://swcx.hb-n-tax.gov.cn:8001/hbgs/invoice/front/";
		try {
			if (in != null) {
				if (result.contains("提示：请核对您的输入信息")) {
					json.put("cwxx", "这张发票没有被企业认领，疑为假票！<br/>提示:请核对您的输入信息");
				}else if(result.contains("查询失败")){
					json.put("cwxx", "查询失败!");
				}else if(result.contains("location.href")){
					String newurl=result.substring(result.indexOf("/")+1, result.indexOf("';"));
					newurl=url+newurl;
					json.put("newurl", newurl);
				}else if(result.contains("税务机关未发售过此发票")){
					json.put("cwxx", "税务机关未发售过此发票。请确认您输入的查验信息是否有误！");
				}else if(result.contains("验证码错误")){
					json.put("cwxx", "查询失败请稍后重试!");
				} else if(result.contains("您查验的发票")) {
					Elements ele = doc.select("tbody");
					json.put("cwxx", ele.text().split("              ")[0]);
					json.put("result", "true");
				} else if(result.contains("这张发票是正常发售被领用！")){
					Elements ele = doc.select("table[class=TabLe1]");
					ele = ele.select("tbody").select("tr").select("td");
					ele = ele.select("table").select("tbody").select("tr")
							.select("td");
					Boolean b = false;
					String[] key = new String[5];
					String[] value = new String[5];
					int i = 0, j = 0;
					for (Element e : ele) {
						// System.out.println(e.text().toString());
						if (i == 5 && j == 5)
							break;
						/*
						 * if(e.text().toString().equals("")){ json.put("cwxx",
						 * "请求错误请重试");//验真码错误 return json; }
						 */
						if (b) {
							if (e.text().toString().contains("：")) {

								key[i] = e.text().toString();
								i++;
							} else {

								String val = e.text().toString();
								if (val.contains("]")) {
									val = val.substring(val.indexOf("]") + 1);
								}
								value[j] = val;
								j++;
							}
						} else {
							if (e.text().toString().equals("这张发票是正常发售被领用！")) {
								b = true;
							} else {
								// 非正确页面
								json.put("cwxx", "查询失败请重试");
								return json;
							}
						}
					}
					for (i = 0; i < key.length; i++) {
						json.put(key[i], value[i]);
					}
					json.put("jg", "这张发票是正常发售被领用！");
					json.put("result", "true");
				}else{
					json.put("cwxx", "发票查询失败，请稍候重试!");
				}
				return json;

			} else {
				json.put("cwxx", "没有查询到此税控发票信息");
				return json;
			}

		} catch (Exception e) {
			e.printStackTrace();
			json.put("cwxx", "查询失败请重试");
			return json;
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception e) {
				json.put("cwxx", "没有查询到此税控发票信息");
				return json;
			}
		}

	}
}
