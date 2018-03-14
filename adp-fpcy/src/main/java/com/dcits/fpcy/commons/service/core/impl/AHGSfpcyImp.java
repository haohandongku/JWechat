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
import com.dcits.fpcy.commons.constant.BrowerType;
import com.dcits.fpcy.commons.constant.HeaderType;
import com.dcits.fpcy.commons.constant.SysConfig;
import com.dcits.fpcy.commons.service.core.InvoiceServerBase;
import com.dcits.fpcy.commons.utils.ResultUtils;
import com.dcits.fpcy.commons.utils.SendResultRequest;
/**
 * 安徽国税分地区统一发票查验 "13407"铜陵不支持
 * 
 * @param parameter
 * @param regionNum
 * @return
 */
public class AHGSfpcyImp implements InvoiceServerBase {
	private Log logger = LogFactory.getLog(AHGSfpcyImp.class);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			parameter.put("vcode", hscEntity.getYzm());
			parameter.put("jsession", hscEntity.getCookie1());
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("安徽增值税,获取cook时出现异常", e);
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
	public void getResult(TaxOfficeBean paras, Map in_Parameter, Map out_Result,
			IpAddress ipAddress) throws JSONException {
		List<ResultBean> list;
		try {
			Map<String, String> requestHeader = new HashMap<String, String>();
			String cookie = in_Parameter.get("jsession").toString();
			requestHeader.put(HeaderType.USERAGENT, BrowerType.google);
			requestHeader.put(HeaderType.COOKIE, cookie);
			InputStream in = SendResultRequest.sendRequestIn(requestHeader,
					ipAddress, in_Parameter,paras.cy_dz,paras.cy_qqfs);
			JSONObject json = parseInvoiceResult(in);
			list = new ArrayList<ResultBean>();
			try {
				if (json == null) {
					out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
				} else if (json.get("result").toString() == "false") {
					list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
					out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
				} else {
					if(json.get("jg").equals("1")){
						String[] key = { "发票代码", "发票号码", "纳税人识别号", "发票领用人", "查询结果"};
						for (int i = 0; i < key.length; i++) {
							list.add(new ResultBean(ResultUtils.getPinYinHeadChar(key[i]), key[i], json.get(key[i])
									.toString()));
						}
					}else if(json.get("jg").equals("2")){
						String[] key = { "发票代码", "发票号码", "发票状态", "开票日期", "开票金额",
								"发票种类", "销方税号", "销方名称", "购方税号", "购方名称" };
						for (int i = 0; i < key.length; i++) {
							list.add(new ResultBean(ResultUtils.getPinYinHeadChar(key[i]), key[i], json.get(key[i])
									.toString()));
						}
					}else if(json.get("jg").equals("3")){
						String[] key = { "查询结果"};
						for (int i = 0; i < key.length; i++) {
							list.add(new ResultBean(ResultUtils.getPinYinHeadChar(key[i]), key[i], json.get(key[i])
									.toString()));
						}
					}
					out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
				}
			} catch (Exception e) {
				logger.error("安徽国税增值税解析JSONObject异常：" + e);
				logger.error(paras.swjg_mc + "解析返回参数异常", e);
				out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
				out_Result.put(SysConfig.INVOICEFALSESTATE,
						SysConfig.INVOICEFALSESTATECODE104);
				e.printStackTrace();
			}
			out_Result.put(SysConfig.INVOICEFALSESTATE,
					json.get(SysConfig.INVOICEFALSESTATE).toString());
			out_Result.put("list", list);
		} catch (Exception e) {
			logger.error(paras.swjg_mc + "解析返回参数异常", e);
		}
	}

	public JSONObject parseInvoiceResult(InputStream in) {
		JSONObject json = new JSONObject();
		try {
			json.put("result", "false");
			if (in == null) {
				json.put("cwxx", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！");
				json.put(SysConfig.INVOICEFALSESTATE, "102");
				return json;
			}
		} catch (JSONException e1) {
			logger.error("JSONObject为空", e1);
		}
		
		try {
				Document doc = (Document) SendResultRequest.iSToJSONOrDocument(
						in, "utf-8", "text");
				// ---------乱码问题解决----部分查询结果会出现乱码 此处设置UTF8格式就可以了
				if(doc.toString().contains("无此发票信息，请确认信息输入是否正确")) {
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE201);
					json.put("cwxx", "无此发票信息，请确认信息输入是否正确。");
					return json;
				}else if(doc.toString().contains("无此发票开具信息，仅能为您提供发票流向查询")){
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE201);
					Elements els = doc.select("table[id=flowDiv]").select("tr")
							.select("td");
					for (int i = 0; i < els.size(); i++) {
						String ele = els.get(i).text();
						if(ele.contains("： ")){
							String[] el1 = ele.split("： ");
							json.put(el1[0], el1[1]);	
						}else{
							json.put("查询结果", ele);
						}
					}
					json.put("jg", "1");
				}else if(doc.toString().contains("次数超过系统设定")){
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE214);
					json.put("查询结果", "该发票查询次数超过系统设定值");
					json.put("jg", "3");
				}else if(doc.toString().contains("验证码输入有误")){
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE214);
					json.put("cwxx", "查询异常请重试");
					return json;
				}else{
					Elements els = doc.select("table[id=dataDiv]").select("tr")
							.select("td");
					for (int i = 0; i < els.size(); i++) {
						String ele = els.get(i).text();
						String[] el1 = ele.split("：");
						json.put(el1[0], el1[1]);
					}
					json.put("jg", "2");
				}
				json.put("result", "true");
		} catch (Exception e) {
			try {
				json.put("cwxx", "此种票样查询发生异常，请稍后重试");
				json.put(SysConfig.INVOICEFALSESTATE,
						SysConfig.INVOICEFALSESTATECODE111);
				json.put("result", "false");
			} catch (JSONException e1) {
				logger.error("", e1);
			}
		}
		return json;
	}
}
