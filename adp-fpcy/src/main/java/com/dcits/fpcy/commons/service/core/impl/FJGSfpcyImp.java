package com.dcits.fpcy.commons.service.core.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.ClientProtocolException;
import org.codehaus.jettison.json.JSONException;
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

public class FJGSfpcyImp implements InvoiceServerBase {
	/**
	 * 福建国税发票查验
	 * 
	 * @param parameter
	 * @param regionNum
	 * @return 增值税专用发票仅提供该号码是否是该开票纳税人购买的流向查询，真伪的识别请直接进行发票认证。 发票认证需要纳税人个人信息登录
	 *         http://wssw.fj-n-tax.gov.cn/wssw/jsp/index/common/onlylogin.jsp?
	 *         styleName=blue&siteName=fj
	 */
	private Log logger = LogFactory.getLog(FJGSfpcyImp.class);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			// 开始查验
			parameter.put("veryCode", hscEntity.getYzm());
			if(hscEntity.getCookie() == null) {
				parameter.put("JSESSIONID", hscEntity.getCookie1());	
			}else{
				parameter.put("JSESSIONID", hscEntity.getCookie());	
			}
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("福建国税,获取cook时出现异常", e);
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result,
			IpAddress ipAddress) throws JSONException {
		Map<String, String> requestHeader = new HashMap<String, String>();
		String cookie[] = in_parameter.get("JSESSIONID").toString().split(";");
		requestHeader.put(HeaderType.COOKIE, cookie[0]);
		requestHeader.put("Accept", "text/plain;charset=utf-8");
		requestHeader.put("Accept-Encoding", "gzip, deflate");
		requestHeader.put("Accept-Language", "zh-CN,zh;q=0.8");
		requestHeader.put("Connection", "keep-alive");
		requestHeader.put("Content-Length", "214");
		requestHeader.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		requestHeader.put("X-Requested-With", "XMLHttpRequest");
		requestHeader.put("Host", "wssw.fj-n-tax.gov.cn");
		requestHeader.put("Origin", "http://wssw.fj-n-tax.gov.cn");
		requestHeader.put("Referer", "http://wssw.fj-n-tax.gov.cn/etax/135/sscx/fpcy.jsp");
		requestHeader.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
		Map attributeMap = new HashMap();
		attributeMap.put("fpdm",in_parameter.get("fpdm0").toString());
		attributeMap.put("fphm",in_parameter.get("fphm0").toString());
		attributeMap.put("xfswdjh",in_parameter.get("xfswdjh0").toString());
		attributeMap.put("kprq",in_parameter.get("kpsj0").toString());
		attributeMap.put("kpje",in_parameter.get("hjje0").toString());
		try {
			attributeMap.put("xhfmc",java.net.URLEncoder.encode("请输入销货方（收款方）名称", "utf-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		attributeMap.put("fpyzm",in_parameter.get("veryCode").toString() );
		InputStream inn=SendResultRequest.sendRequestPost(requestHeader, null, paras.cy_dz, attributeMap);
		String mm = null;
		try {
			mm = SendResultRequest.sent(paras.cy_dz, attributeMap, "UTF-8");
		} catch (ClientProtocolException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		JSONObject cxjg = parseInvoiceResult(inn);
		List<ResultBean> list = new ArrayList<ResultBean>();
		try {
			if(mm.contains("??????")) {
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
				out_result.put(SysConfig.INVOICEFALSESTATE,SysConfig.INVOICEFALSESTATECODE203);
				out_result.put("cxxx", "发票信息填写有误");
			}else{
			if (cxjg == null) {
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			}
			if (cxjg.getString("result").equals("true")) {
				list.add(new ResultBean("FPDM", "发票代码", cxjg.getString("0")));
				list.add(new ResultBean("FPHM", "发票号码", cxjg.getString("1")));
				list.add(new ResultBean("XHFSWDJH", "销货方税务登记号", cxjg
						.getString("2")));
				list.add(new ResultBean("XHFMC", "销货方名称", cxjg.getString("3")));
				list.add(new ResultBean("FPJE", "发票金额", cxjg.getString("4")));
				list.add(new ResultBean("KPYF", "开票月份", cxjg.getString("5")));
				if (cxjg.has("XXXX")) {
					list.add(new ResultBean("XXXX", "查询详细", cxjg.getString("XXXX")));
				}
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
			} else {
				list.add(new ResultBean("cwxx", "", cxjg.get("cwxx").toString()));
				out_result.put(SysConfig.INVOICEFALSESTATE,cxjg.getString(SysConfig.INVOICEFALSESTATE));
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		out_result.put("list", list);
	}

	public JSONObject parseInvoiceResult(InputStream in) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("result", "false");
		try {
			if (in != null) {
				Document doc = (Document) SendResultRequest.iSToJSONOrDocument(
						in, "utf-8", "text");
				String html = doc.toString();
				// 没有能输出正确信息的福建国税发票，所以此处无法判断
				if (html.contains("尚未发放")) {
					json.put("cwxx",
							"您所查询的发票尚未发放，请仔细核对发票代码及号码；如有疑问，可向当地地税部门申请鉴定。");
					json.put(SysConfig.INVOICEFALSESTATE,SysConfig.INVOICEFALSESTATECODE201);
					return json;
				}else if(html.contains("????????????") || html.contains("java.lang.NullPointerException")) {
					json.put("cwxx",
							"发票信息填写有误。");
					json.put(SysConfig.INVOICEFALSESTATE,SysConfig.INVOICEFALSESTATECODE203);
					return json;
				} else {
					Elements eles2 = doc.select("table").select("tbody")
							.select("tr").select("td").select("table")
							.select("tbody").select("tr").select("td")
							.select("table").select("tbody").select("tr")
							.select("td").select("table").select("tbody")
							.select("tr");
					Elements ele = eles2.select("td[colspan=7]").select("span");
					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < ele.size(); i++) {
						if (ele.get(i).toString().contains("red")) {
							sb.append((i + 1) + "、" + ele.get(i).text());
						}
					}
					json.put("XXXX", sb.toString());
					int ii = 0;
					for (Element e2 : eles2) {
						if (ii == 1) {
							Elements eles3 = e2.select("td");
							int index = 0;
							for (Element e3 : eles3) {
								String indexs = index + "";
								json.put(indexs, e3.text().toString());
								index++;
							}
						}
						ii++;
					}
					json.put("result", "true");
					return json;
				}
			} else {
				json.put("cwxx", "发票查询时，出现异常，请重试！");
				json.put("result", "false");
				return json;
			}
		} catch (Exception e) {
			System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
			try {
				json.put("cwxx", "您输入的信息有误，请重新输入！");
				json.put("result", "false");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			return json;
		}
	}
}
