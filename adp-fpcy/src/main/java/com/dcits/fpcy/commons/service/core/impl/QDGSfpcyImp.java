package com.dcits.fpcy.commons.service.core.impl;

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
 * 青岛国税 13702
 * 
 * 2017-04-12
 */
public class QDGSfpcyImp implements InvoiceServerBase {
	
	private Log logger = LogFactory.getLog(QDGSfpcyImp.class);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map FPCY(Map parameter,TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			parameter.put("JSESSIONID", hscEntity.getCookie1());		// 开始查验
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("青岛国税,获取cook时出现异常", e);
			result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		}
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result,
			IpAddress ipAddress) throws Exception {
		String skm=null ;
		String fphm = in_parameter.get("fphm").toString();
		String fpdm = in_parameter.get("fpdm").toString();
		String isSkm = in_parameter.get("isSkm").toString();
	 	String fplx = in_parameter.get("fplx").toString();
		if ("wlfp".equals(fplx)) {
			 skm = in_parameter.get("skm").toString();
		}
      
		String JSESSIONID = in_parameter.get("JSESSIONID").toString();
	//	String kjfnsrsbh = in_parameter.get("kjfnsrsbh").toString();
	//	String kjfnsrmc = in_parameter.get("kjfnsrmc").toString();
	//	String kprq = in_parameter.get("kprq").toString();
	//	String skskjbh = in_parameter.get("skskjbh").toString();
	//	String kpje = in_parameter.get("kpje").toString();
		Map requestHeader = new HashMap();
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.COOKIE, JSESSIONID);
		// 发送验证码 青岛国税验证码校验一步可以跳过不做，因而该处只是作为一个简单的连接，以备将来使用。
		Map attributeMap = new HashMap();
		//String address1 = paras.yz_dz + validateCode;
		InputStream in = SendResultRequest.sendRequestIn(requestHeader,
				ipAddress, attributeMap, paras.cyym, "POST");
		JSONObject json = parseInvoiceResultQDGS(in, null);
		// 第二次请求,没有实质作用，跳过
		// String address2 = "http://www.qd-n-tax.gov.cn/sst/fpcxWizard.do";
		// 第三次请求
		String address3 = "";
		attributeMap.put("fpdm", fpdm);
		attributeMap.put("fphm", fphm);
		// 1:是，2：否
		if ("1".equals(isSkm)) {
			if ("wlfp".equals(fplx)) {
				attributeMap.put("skm", skm);
				address3 = "http://sst.qd-n-tax.gov.cn:8001/sst/jsjKjPtFp.do";
			} else if ("skfp".equals(fplx)) {
				address3 = "http://sst.qd-n-tax.gov.cn:8001/sst/skskjKjPtFp.do";
				attributeMap.put("kpje", in_parameter.get("kpje").toString());
				attributeMap.put("kprq", in_parameter.get("kprq").toString());
				attributeMap.put("skskjbh", in_parameter.get("skskjbh").toString());
				String str = "";
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < 5; i++) {
					int s = i * 4;
					int e = s + 4;
					str = skm.substring(s, e);
					sb.append("skm=");
					sb.append(str);
					sb.append("&");
				}
				skm = sb.substring(3, sb.length() - 1);
				attributeMap.put("skm", skm);
			}
		} else if ("2".equals(isSkm)) {
			if ("czcfp".equals(fplx)) {
				address3 = "http://sst.qd-n-tax.gov.cn:8001/sst/czcFp.do";
			} else if ("sgfp".equals(fplx)) {
				address3 = "http://sst.qd-n-tax.gov.cn:8001/sst/sgFp.do";
				attributeMap.put("kjfnsrsbh", in_parameter.get("kjfnsrsbh").toString());
				attributeMap.put("kjfnsrmc", in_parameter.get("kjfnsrmc").toString());
			}
		}
		in = SendResultRequest.sendRequestPost(requestHeader, ipAddress,
				address3, attributeMap);
		json = parseInvoiceResultQDGS(in, fplx);
		List<ResultBean> list=new ArrayList<ResultBean>();
		if (json == null||("false").equals(json.get("result").toString())) {
			list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
			out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);// 解析返回结果有误
		} else {
			list.add(new ResultBean("CXJG","查询结果",json.get("cwxx").toString()));
			//out_result.put(SysConfig.INVOICEFALSESTATE,json.getString(SysConfig.INVOICEFALSESTATE));
			out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
		}
		out_result.put("list", list);
	}

	public JSONObject parseInvoiceResultQDGS(InputStream in, String fplx) throws Exception {
		JSONObject json = new JSONObject();
		try {
			if (in != null) {
				json.put("result", "false");
				Document document = (Document) SendResultRequest.iSToJSONOrDocument(in,
						"UTF-8", "text");
				String html = document.toString();
				if (StringUtils.isNotBlank(fplx)) {
					if (html.contains("0")) {
						json.put("cwxx", "您查询的发票信息不存在，可能为假发票，请向青岛市国税局举报。");
						return json;
					} else if (html.contains("1")) {
						if ("sgfp".equals(fplx) || "skfp".equals(fplx)) {
							json.put("cwxx", "您查询的发票信息与我局保存的信息不一致，可能为假发票或存在违章行为的发票，请向青岛市国税局举报。");
							return json;
						} else if ("wlfp".equals(fplx)) {
							json.put("cwxx", "您查询的发票信息开票人尚未上传，请稍候再查。");
							return json;
						}
					} else if (html.contains("2")) {
						json.put("cwxx", "您所查询发票的销货单位与我局保存的购领发票单位一致。");
						return json;
					} else if (html.contains("3")) {
						json.put("cwxx", "该张发票已作废，请向青岛市国税局举报。");
						return json;
					} else if (html.contains("4")) {
						if ("skfp".equals(fplx)) {
							json.put("cwxx", "您查询的发票信息需要向青岛市国税局进一步核实。");
							return json;
						} else if ("wlfp".equals(fplx)) {
							json.put("cwxx", "您查询的发票已经被冲红。");
							return json;
						}
					} else {
						if ("czcfp".equals(fplx)) {
							json.put("result", true);
							json.put("cwxx",  "发票由" + html + "开具，此种发票只提供流向查询，不能保证信息的真伪。");
							return json;
						} else if ("wlfp".equals(fplx)) {
							json.put("cwxx", "您查询的内容与开票人开具的发票信息一致。");
							return json;
						} else {
							json.put("cwxx", "系统正忙,请稍后再试");
							return json;
						}
					}
				} else {
					if (html.contains("验证码")) {
						json.put("cwxx", "系统繁忙请稍后再试！");
					} else if ("inputCorrect".equals(html)) {
						json.put("cwxx", "验证码正确！");
					} else if (html.contains("<body>")) {
						// 第二次请求时会返回html
					} else if ("".equals(html)) {
						json.put("cwxx", "系统正忙,请稍后再试！");
					}
				}
			} else {
				json.put("cwxx", "没有查询到此税控发票信息");
			}
		} catch (Exception e) {
			json.put("cwxx", "没有查询到此税控发票信息");
			logger.error("青岛国税解析异常", e);
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception e) {
				logger.error("青岛国税解析异常", e);
				json.put("cwxx", "没有查询到此税控发票信息");
			}
		}
		return json;
	}
}
