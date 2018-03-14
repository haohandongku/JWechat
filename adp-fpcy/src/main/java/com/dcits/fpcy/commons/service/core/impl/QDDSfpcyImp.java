package com.dcits.fpcy.commons.service.core.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

public class QDDSfpcyImp implements InvoiceServerBase {
	private Log logger = LogFactory.getLog(QDDSfpcyImp.class);
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			parameter.put("yzm", hscEntity.getYzm());
			parameter.put("JSESSIONID", hscEntity.getCookie1());		// 开始查验
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("青岛地税,获取cook时出现异常", e);
			List<ResultBean> list = new ArrayList<ResultBean>();
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
			IpAddress ipAddress) throws Exception {
		Map<String, String> requestHeader = new HashMap<String, String>();
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		String JSESSIONID =null;
		if(in_parameter.get("JSESSIONID")!=null){
		JSESSIONID = in_parameter.get("JSESSIONID").toString();
		}
		requestHeader.put(HeaderType.COOKIE, JSESSIONID);
		InputStream in = SendResultRequest.sendRequestPost(requestHeader,
				ipAddress, paras.cy_dz, in_parameter);
		JSONObject json = parseInvoiceResult(in);
		List<ResultBean> list = new ArrayList<ResultBean>();
		try {
			if (json == null||("false").equals(json.get("result").toString())) {
				list.add(new ResultBean("cwjg", "", json.get("cwjg").toString()));
				out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE20011);
				out_result.put(SysConfig.INVOICEFALSESTATE,json.getString(SysConfig.INVOICEFALSESTATE));
			} else {		
				list.add(new ResultBean("FPDM", "发票代码", json.get("fpdm").toString()));
				list.add(new ResultBean("FPHM", "发票号码", json.get("fphm").toString()));
				list.add(new ResultBean("FPCXJG","发票查询结果",json.get("fpcxjg").toString()));
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
			}
		} catch (Exception e) {
			logger.error("青岛地税解析JSONObject异常：" + e);
			logger.error(paras.swjg_mc + "解析返回参数异常", e);
			out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			out_result.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE104);
			e.printStackTrace();
		}
		out_result.put("list", list);
	}

	/**
	 * 青岛地税发票解析
	 */
	public JSONObject parseInvoiceResult(InputStream in) throws Exception {
		JSONObject jso = new JSONObject();
		BufferedReader bufferedReader = null;
		jso.put("result", "false");
		if (in == null) {
			jso.put("cwxx", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！");
			jso.put(SysConfig.INVOICEFALSESTATE, "102");
			return jso;
		}
		Document document = (Document) SendResultRequest.iSToJSONOrDocument(in,
				"GBK", "text");
		String html = document.toString();
		try {
				if (!html.contains("请输入正确验证码")) {
					Document doc = Jsoup.parse(html);
					Elements eles = doc.select("input");
					String msg = "";//不能找到此类型的发票
					if (html.contains("不能找到此类型的发票")) {
						jso.put(SysConfig.INVOICEFALSESTATE,
								SysConfig.INVOICEFALSESTATECODE211);
						jso.put("cwjg", "不能找到此类型的发票,请检查你的发票代码和号码是否正确！");
						return jso;
					}
					if (html.contains("没有找到")) { // 发票信息错误
						jso.put(SysConfig.INVOICEFALSESTATE,
								SysConfig.INVOICEFALSESTATECODE211);
						for (Element e : eles) {
							if (e.attr("name").compareTo("ycxx") == 0
									|| e.attr("name").compareTo("cxxx") == 0) {
								msg += e.attr("value");
							}
							jso.put("cwjg", msg);
						}
						jso.put("cwjg", "查询失败");
						return jso;
					} else if (html.contains("cxjg")) {
						if (html.contains("次数")) { // 查询频繁
							jso.put("cwjg", "查询过于频繁");
							jso.put(SysConfig.INVOICEFALSESTATE,
									SysConfig.INVOICEFALSESTATECODE214);
						}
						for (Element e : eles) {
							if (e.attr("name").compareTo("fpdm") == 0
									|| e.attr("name").compareTo("fphm") == 0) {
								jso.put(e.attr("name"), e.attr("value"));
							} else if (e.attr("name").compareTo("cxjg") == 0
									|| e.attr("name").compareTo("cxxx") == 0) {
								msg += e.attr("value");
							}
							if (html.contains("次数")) {// 查询频繁
								jso.put("fpcxjg", "发票为真票！" + msg);
							} else {
								jso.put("fpcxjg", msg);
							}
							if (html.contains("您的操作有异常")) {// 查询频繁
								jso.put("cwjg", "您的操作有异常，请重新查询！");
								return jso;
							}
						}
						jso.put("cxjg", "查询成功");
						jso.put("result", "true");
						return jso;
					}
				} else {
					jso = new JSONObject();
					jso.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE101);
					jso.put("cwjg", "查询失败,请重新查询！");
					return jso;
				}
		} catch (Exception e) {
			jso.put("cwjg",
					"没有找到该发票类型。请确认您输入的信息是否正确，如果无误，则此票可能为假票，请拨打青岛地税纳税服务热线(0532)12366-2举报!");
			jso.put("result", "false");
			jso.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE111);
			return jso;
		} finally {
			try {
				if (bufferedReader != null)
					bufferedReader.close();
				if (in != null)
					in.close();
			} catch (Exception e) {
				jso.put("cwjg",
						"没有找到该发票类型。请确认您输入的信息是否正确，如果无误，则此票可能为假票，请拨打青岛地税纳税服务热线(0532)12366-2举报!");
				jso.put("result", "false");
				return jso;
			}
		}
		return jso;
	}
}
