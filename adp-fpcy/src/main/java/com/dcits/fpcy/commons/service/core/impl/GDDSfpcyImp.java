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
import com.dcits.fpcy.commons.utils.SendResultRequest;

public class GDDSfpcyImp implements InvoiceServerBase {

	/**
	 * 广东地税发票查验
	 * 
	 * @param parameter
	 * @param regionNum
	 * @return
	 * 
	 *         modify by fangys . 2016/1/22
	 */
	private Log logger = LogFactory.getLog(GDDSfpcyImp.class);
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			parameter.put("yzm", hscEntity.getYzm());
			if(hscEntity.getCookie1() == null) {
				parameter.put("JSESSIONID", hscEntity.getCookie());	
			}else{
				parameter.put("JSESSIONID", hscEntity.getCookie1());	
			}
			// 开始查验
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("广东地税,获取cook时出现异常", e);
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

	// 获取验证码验真结果
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void getResult(TaxOfficeBean paras, Map in_Parameter, Map out_result,
			IpAddress ipAddress) {
		Map attributeMap = new HashMap();
		Map<String, String> requestHeader = new HashMap<String, String>();
		attributeMap.put("yzm", in_Parameter.get("yzm").toString());
		attributeMap.put("fpdm0", in_Parameter.get("FPDM").toString());
		attributeMap.put("fphm0", in_Parameter.get("FPHM").toString());
		attributeMap.put("kpje0", in_Parameter.get("kpje0").toString());
		requestHeader.put(HeaderType.COOKIE, in_Parameter.get("JSESSIONID").toString());
     	InputStream in = SendResultRequest.sendRequestIn(requestHeader, ipAddress,
     			attributeMap, paras.cy_dz, paras.cy_qqfs); 
		JSONObject json = parseInvoiceResult(in);
		List<ResultBean> list=new ArrayList<ResultBean>();
		try {
			if(json==null||json.length()==0){
				out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE20011);
			}else if (json.get("result").toString().equals("false")) {
				list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
				out_result.put(SysConfig.INVOICEFALSESTATE,json.getString(SysConfig.INVOICEFALSESTATE));
				out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE20011);
			} else {
				list.add(new ResultBean("fpdm","发票代码", json.get("fpdm").toString()));
				list.add(new ResultBean("fphm","发票号码",json.get("fphm").toString()));
				list.add(new ResultBean("fkfMc","付款方名称",json.get("fkfMc").toString()));
				list.add(new ResultBean("skfMc","收款方名称",json.get("skfMc").toString()));
				list.add(new ResultBean("kprq","开票日期",json.get("kprq").toString()));
				list.add(new ResultBean("hjJe","合计金额",json.get("hjJe").toString()));
				list.add(new ResultBean("cyFkfSj","抽奖登记手机",json.get("cyFkfSj").toString()));
				list.add(new ResultBean("cyCs","查询次数",json.get("cyCs").toString()));
				list.add(new ResultBean("fpztMc","状态",json.get("fpztMc").toString()));
				list.add(new ResultBean("kpxmMc","行业",json.get("kpxmMc").toString()));
				list.add(new ResultBean("CXJG","查询结果","查询成功"));
				out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE1000);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		out_result.put("list", list);
	}

	/**
	 * 广东深圳地税解析结果
	 */
	public JSONObject parseInvoiceResult(InputStream in) {
		JSONObject json = new JSONObject();
		try {
			if (in != null) {
				try {
					json.put("result", "false");
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
				Document doc = (Document) SendResultRequest.iSToJSONOrDocument(in,
						"UTF-8", "text");
				String html = doc.toString();
				if (html.contains("验码不正确")
						|| html.contains("【提示信息】：您的校验码已经超时！")) {
					json.put("cwxx", "发票信息查验失败请重试");
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE101);
					return json;
				}
				if (html.contains("不能进行查询")) {
					json.put("cwxx", "该发票不能进行此类查询");
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE215);
					return json;
				}
				if (html.contains("无此发票记录")) {
					json.put("cwxx", "无此发票信息");
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE201);
					return json;
				}
				if (html.contains("验证码错误")) {
					json.put("cwxx", "验证码错误!");
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE101);
					return json;
				}
				Elements els = doc.select("input");
				int i = 0;
				String msg = "";
				while (i < els.size()) {
					if (els.get(i).toString().contains("xmlContent")) {
						msg = els
								.get(i)
								.toString()
								.substring(
										els.get(i).toString()
												.indexOf("value=\"") + 7,
										els.get(i).toString().indexOf("\" />"));
						msg = msg.replaceAll("&lt;", " ");
						msg = msg.replaceAll("&gt;", ":");
					}
					i += 1;
				}
				if (msg.contains("fpztMc:无此发票记录 ")) {
					json.put("cwxx", "无此发票记录");
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE201);
					return json;
				} else {
					String[] key = { "fpdm", "fphm", "fkfMc", "skfMc", "kprq",
							"hjJe", "cyFkfSj", "cyCs", "fpztMc", "kpxmMc" };
					for (int j = 0; j < key.length; j++) {
						if (msg.contains(key[j])) {
							String value = " ";
							int l = key[j].toString().length();
							value = msg.substring(msg.indexOf(key[j]) + l + 1,
									msg.indexOf("/" + key[j]));
							if (value.length() < 1) {
								value = " ";
							} else {
								while (value.contains("[")) {
									value = value
											.substring(value.indexOf("[") + 1);
								}
								while (value.contains("]")) {
									value = value.substring(0,
											value.indexOf("]"));
								}
							}
							json.put(key[j], value);
						}
					}
					json.put("result", "true");
					return json;
				}
			}
		} catch (Exception e) {
			System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
			try {
				json.put("cwxx", "查询发生错误");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			return json;
		}
		return json;
	}
}
