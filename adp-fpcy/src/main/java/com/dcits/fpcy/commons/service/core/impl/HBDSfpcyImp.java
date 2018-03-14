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
import com.dcits.fpcy.commons.utils.ResultUtils;
import com.dcits.fpcy.commons.utils.SendResultRequest;
 

/**
 * 湖北地税 24200 
 * poolid: 70
 *
 */
public class HBDSfpcyImp implements InvoiceServerBase {
	private Log logger = LogFactory.getLog(HBDSfpcyImp.class);
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			parameter.put("rand", hscEntity.getYzm());
			parameter.put("JSESSIONID", hscEntity.getCookie1());
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("湖北地税,获取cook时出现异常", e);
			List list = new ArrayList<ResultBean>();
			list.clear();
			list.add(new ResultBean("cwxx", "", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！"));
			result.put("list", list);
			result.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE103);
			result.put(SysConfig.CYJGSTATE,SysConfig.CODE20011);
		}
		return result;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes"})
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result,
			IpAddress ipAddress) throws Exception {
		Map attributeMap = new HashMap();
		Map<String, String> requestHeader = new HashMap<String, String>();
		attributeMap.put("CXM", in_parameter.get("CXM").toString());
		attributeMap.put("FPHM", in_parameter.get("FPHM").toString());
		attributeMap.put("JMYZM", in_parameter.get("rand").toString());
		attributeMap.put("FPDM", in_parameter.get("FPDM").toString());
		attributeMap.put("method", in_parameter.get("method").toString());
		String JSESSIONID = in_parameter.get("JSESSIONID").toString();
		JSESSIONID = JSESSIONID.substring(0, JSESSIONID.indexOf(";"));
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.COOKIE, JSESSIONID);
		requestHeader.put("Host", "221.232.128.57:8000");
		requestHeader.put("Connection", "Keep-Alive");
		requestHeader.put("Accept", "*/*");
		requestHeader.put("Accept-Language", "zh-CN");
		//requestHeader.put("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 10.0; WOW64; Trident/7.0; .NET4.0C; .NET4.0E; .NET CLR 2.0.50727; .NET CLR 3.0.30729; .NET CLR 3.5.30729; InfoPath.2)");
		InputStream in = SendResultRequest.sendRequestIn(requestHeader,
				ipAddress, attributeMap, paras.cy_dz, paras.cy_qqfs);
		JSONObject json = parseInvoiceResult(in);
		List<ResultBean> list = new ArrayList<ResultBean>();
		String key[] = null;
		if(json.length() == 4 || json.length() == 5) {
			String key1[] = {"用票人","开票日期"};
			key = key1;
		}else if(json.length() == 9) {
			String key3[] = {"付款方名称","身份证号/组织机构代码/纳税人识别号","收款方名称","项目","金额","备注",
					"合计金额(元)(大写)","主管税务机关及代码"};
			key = key3;
		} else {
			String key2[] = {"机器编号","付款单位","开票日期","收款单位","收款员",
					"发票内容","小写合计","税控码"};
			key = key2;
		}
		try {
			if (json.get("result").toString() == "false") {
				list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
				out_result.put(SysConfig.INVOICEFALSESTATE,json.getString(SysConfig.INVOICEFALSESTATE));
				out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE20011);
			} else {
				JSONObject cxjg = json;
				list = ResultUtils.getListInfoFromJson1(key, cxjg);
				list.add(new ResultBean("CXJG","查询结果", "查验成功"));
				out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE1000);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		out_result.put("list", list);
	}

	/**
	 * 
	 * 湖北地税解析
	 * 
	 * @param in
	 * @return
	 */
	public JSONObject parseInvoiceResult(InputStream in) {
		JSONObject jso = new JSONObject();
		JSONObject json = new JSONObject();
		try {
			json.put("result", "false");
			if (in != null) {
				Document document = (Document) SendResultRequest
						.iSToJSONOrDocument(in, "GBK", "text");
				String html = document.toString();
				if (html.contains("没有查询到此税控发票信息")
						|| html.contains("没有查询到该发票信息")) {
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE201);
					json.put("cwxx", "没有查询到此发票信息！");
					return json;
				}else if(html.contains("验证码不正确")){
					json.put("cwxx", "发票查询失败，请重试！");
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE101);
					return json;
				}else if(html.contains("验证码不能为空")){
					json.put("cwxx", "发票查询失败，请重试！"); 
					return json;
				} else {
					Elements eles = null;
					Elements test = null;
					String result1[] = null;
					if(html.contains("通用发票")) {
						eles = document.select("table[class=fptable]").select("tr").select("td");
						test = document.select("table[class=fptable]").select("tr").select("td");
						String ll = eles.text();
						result1 = ll.substring((ll.indexOf("查询码  "))+5).split(" ");
					}else{
						eles = document.select("body").select("div")
								.select("div").select("ul");
						test = document.select("body").select("div")
								.select("div").select("table").select("tbody")
								.select("tr");
					}
					int o = 0;
					if(!test.toString().contains("人民币") && !html.contains("通用发票")) {
						String key=test.get(0).text();
						String value=test.get(1).text();
						jso.put("发票内容", key+"</br>"+value);
					} else {
						o = o + 9;
					}
					int i = 0;
					if(html.contains("通用发票")) {
						for(int s = 0;s<result1.length;s++) {
							if("项目".equals(result1[s]) || "金额".equals(result1[s]) || "备注".equals(result1[s])) {
								jso.put(result1[s], result1[s+3]);
								if("206230".equals(result1[s+3])){
									s = s +3;
								}
							}else{
								if("￥".equals(result1[s])) {
									s = s +4;
									continue;
								}
								if("主管税务机关及代码".equals(result1[s])) {
									StringBuffer bu = new StringBuffer();
									bu.append(result1[s+1]);
									bu.append(" ");
									bu.append(result1[s+2]);
									jso.put(result1[s], bu);
									break;
								}
								jso.put(result1[s], result1[s+1]);
								s = s +1 ;
							}
						}
					} else {
						for (Element e : eles) {
							String[] temp = e.text().split(" ");
							if (i == 1) {
								for (int j = o; j < temp.length; j++) {
									if (j == 1) {
										continue;
									}
									String[] tempNew = temp[j].split("：");
									jso.put(tempNew[0], tempNew[1]);
								}
								break;
							}
							
							for (int j = o; j < temp.length; j++) {
								String[] tempNew = null;
								if(!test.toString().contains("人民币")) {
									tempNew = temp[j].split("：");
								}else {
									tempNew = temp[j].split(":");
								}
								if (tempNew.length <= 1) {
									if (tempNew.length == 1)
										jso.put(tempNew[0], " ");
									else
										jso.put("", "");
								} else {
									jso.put(tempNew[0], tempNew[1]);
								}
							}
							i++;
						}
					}
					json = jso;
					json.put("result", "true");
					return json;
				}
			} else {
				json.put("cwxx", "没有查询到此税控发票信息");
				return json;
			}
		} catch (Exception e) {
			try {
				logger.error("湖北地税解析错误", e);
				json.put("cwxx", "没有查询到此税控发票信息");
				return json;
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception e) {
				logger.error("湖北地税解析错误", e);
				try {
					json.put("cwxx", "没有查询到此税控发票信息");
					return json;
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
			}
		}
		return json;
	}
}
