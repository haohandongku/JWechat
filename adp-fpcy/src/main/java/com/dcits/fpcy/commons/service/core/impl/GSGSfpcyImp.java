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
import com.dcits.fpcy.commons.utils.ResultUtils;
import com.dcits.fpcy.commons.utils.SendResultRequest;

public class GSGSfpcyImp implements InvoiceServerBase {
	/**
	 * 甘肃国税发票查验
	 * 
	 * @param parameter
	 * @param regionNum
	 * @return
	 */
	private Log logger = LogFactory.getLog(GSGSfpcyImp.class);

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			parameter.put("rand", String.valueOf(hscEntity.getYzm()));
			parameter.put("JSESSIONID", hscEntity.getCookie());
			// 开始查验
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());

		} catch (Exception e) {
			logger.error("甘肃国税,获取cook时出现异常", e);
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result,
			IpAddress ipAddress) throws Exception {
		Map attributeMap = new HashMap();
		Map<String, String> requestHeader = new HashMap<String, String>();
		attributeMap.put("fpdm", in_parameter.get("FPDM").toString());
		attributeMap.put("fphm", in_parameter.get("FPHM").toString());
		attributeMap.put("captcha", in_parameter.get("rand").toString());
		requestHeader.put(HeaderType.COOKIE, in_parameter.get("JSESSIONID").toString() + "; path=/");
		InputStream in = SendResultRequest.sendRequestPost(requestHeader,
				ipAddress, paras.cy_dz, attributeMap);
	/*	InputStream in = SendResultRequest.sendRequestIn(requestHeader,
				ipAddress,attributeMap, paras.yz_dz, paras.cy_qqfs);*/
		JSONObject cxjg = parseInvoiceResult(in);
		List<ResultBean> list = new ArrayList<ResultBean>();
		try {
			if (cxjg.getString("result").equals("true" )) {
				String fpdm = cxjg.getString("发票代码");
				String key[] = {"发票代码","发票号码","开票时间","销售方","纳税人识别号",
						"纳税人名称"};
				list = ResultUtils.getListInfoFromJson1(key, cxjg);
				list.add(new ResultBean("CXJG", "查询结果", "查验成功"));
				if (fpdm.length() == 10) {
					list.add(new ResultBean("KPJE", "开票金额", cxjg
							.getString("开票金额")));
				}				
				
				out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE1000);
			} else if(cxjg.getString("result").equals("false")) {
				list.add(new ResultBean("cwxx", "", cxjg.get("cwxx").toString()));
				out_result.put(SysConfig.INVOICEFALSESTATE,cxjg.getString(SysConfig.INVOICEFALSESTATE));
				out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE20011);
			}else{
				list.add(new ResultBean("cwxx", "", cxjg.get("cwxx").toString()));
				out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE20011);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		out_result.put("list", list);
	}

	public JSONObject parseInvoiceResult(InputStream in) {
		JSONObject json = new JSONObject();
		/*try {
			json.put("result", "false");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}*/

		try {
			if (in != null) {
				Document doc = (Document) SendResultRequest.iSToJSONOrDocument(
						in, "UTF-8", "text");
				String html = doc.toString();
				if (html.contains("未查询到您要的发票")) {
					json.put("cwxx", "未查询到您要的发票!");
					json.put(SysConfig.INVOICEFALSESTATE,SysConfig.INVOICEFALSESTATECODE201);
					json.put("result", "false");
					return json;
				}
				if (html.contains("验证码输入错误")) {
					json.put("cwxx", "发票查询失败,请重试！");
					json.put(SysConfig.INVOICEFALSESTATE,SysConfig.INVOICEFALSESTATECODE101);
					json.put("result", "false");
					return json;
				} else {
					Elements h = doc.select("h4");
					String cxjg = h.text().toString();
					cxjg = cxjg.substring(cxjg.indexOf("你查询的"));
					if (cxjg.contains("发票代码为:")) {
						json.put(
								"发票代码",
								cxjg.substring(cxjg.indexOf("发票代码为:") + 6,
										cxjg.indexOf(",发")));
					}
					if (cxjg.contains("发票号码为:")) {
						json.put(
								"发票号码",
								cxjg.substring(cxjg.indexOf("发票号码为:") + 6,
										cxjg.indexOf("的发票,是")));
					}
					if (cxjg.contains("是") && cxjg.contains("于")
							&& cxjg.contains("销售给")) {
						json.put(
								"销售方",
								cxjg.substring(cxjg.indexOf(",是") + 2,
										cxjg.indexOf("于2")));
						json.put(
								"开票时间",
								cxjg.substring(cxjg.indexOf("于") + 1,
										cxjg.indexOf("销售")));
						json.put("纳税人识别号", cxjg.substring(
								cxjg.indexOf("纳税人识别号是:") + 8,
								cxjg.indexOf(",纳")));
					}
					if (cxjg.contains("纳税人名称是:")) {
						json.put("纳税人名称", cxjg.substring(
								cxjg.indexOf("纳税人名称是:") + 7, cxjg.indexOf("。")));
					}
					if (cxjg.contains("开票金额")) {
						json.put(
								"开票金额",
								cxjg.substring(cxjg.indexOf("开票金额是") + 5,
										cxjg.indexOf("元") + 1));
					}
					json.put("result", "true");
				}
			} else {
				json.put("cwxx", "您输入的信息有误，请重新输入！");
				return json;
			}
		} catch (Exception e) {
			System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
			try {
				json.put("cwxx", "您输入的信息有误，请重新输入！");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			return json;
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception e2) {
				System.out.println(("[ERROR] 程序执行操作出现异常：" + e2.getMessage()));
				try {
					json.put("cwxx", "您输入的信息有误，请重新输入！");
					json.put("result", "false");
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return json;
	}
}
