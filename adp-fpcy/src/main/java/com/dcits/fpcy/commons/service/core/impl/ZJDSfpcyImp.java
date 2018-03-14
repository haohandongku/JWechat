package com.dcits.fpcy.commons.service.core.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
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
public class ZJDSfpcyImp implements InvoiceServerBase {
	private Log logger = LogFactory.getLog(ZJDSfpcyImp.class);
	/**
	 * 浙江地税发票查验
	 * 
	 * @param parameter
	 * @param regionNum
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("FPDM", parameter.get("FPDM").toString());
		result.put("FPHM", parameter.get("FPHM").toString());
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			parameter.put("JSESSIONID", hscEntity.getCookie1());
			// 开始查验
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("浙江地税发票查验,获取cook时出现异常", e);
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
	
	@SuppressWarnings({ "rawtypes" })
	public Map FPCY(Map parameter) throws Exception {
		return null;
	}

	public void getResult(TaxOfficeBean paras, Map<String, String> in_Parameter,
			Map<String, Object> out_Result, IpAddress ipAddress)
			throws Exception {
		Map<String, String> attributeMap = new HashMap<String, String>();
		Map<String, String> requestHeader = new HashMap<String, String>();
		String JSESSIONID = in_Parameter.get("JSESSIONID").toString();
		requestHeader.put(HeaderType.COOKIE, JSESSIONID);
		requestHeader.put("localid", "1");
		attributeMap.put("fpdm", String.valueOf(in_Parameter.get("FPDM")));
		attributeMap.put("fphm", String.valueOf(in_Parameter.get("FPHM")));
		attributeMap.put("fpje", String.valueOf(in_Parameter.get("FPJE")));
		InputStream in = SendResultRequest.sendRequestIn(null, ipAddress,
				attributeMap, paras.cy_dz, paras.cy_qqfs);
		JSONObject json = parseInvoiceResult(in);
		try {
			List<ResultBean> list = new ArrayList<ResultBean>();
			if ("false".equals(json.get("result"))) {
				list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
				out_Result.put(SysConfig.INVOICEFALSESTATE,json.getString(SysConfig.INVOICEFALSESTATE));
				out_Result.put(SysConfig.CYJGSTATE,SysConfig.CODE20011);
			} else {
				if(json.get("biaoshi").toString().equals("1")) {
					String key1[] ={"发票代码","发票号码","税务机关是否已发放","发放税务机关","付款方","开票金额与查询金额是否相同"};
					list = ResultUtils.getListInfoFromJson1(key1, json);
				}else{
					String key[] = {"开具单位","发款方名称","开票日期","开票内容","合计金额","查询结果"};
					list = ResultUtils.getListInfoFromJson(key, json);
				}
				out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
			}
			out_Result.put("list", list);
		} catch (JSONException e) {
			e.printStackTrace();
			out_Result.put(SysConfig.CYJGSTATE,SysConfig.CODE20011);
		}
	}

	public JSONObject parseInvoiceResult(InputStream in) throws JSONException {

		String html = null;
		JSONObject json = new JSONObject();
		json.put("result", "false");
		try {
			if (in != null) {
				JSONObject result = (JSONObject) SendResultRequest
						.iSToJSONOrDocument(in, "UTF-8", "json");
				html = result.toString();
				if (html.contains("尚未发放")) {
					json.put("result", "false");
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE201);
					json.put("cwxx",
							"您所查询的发票尚未发放，请仔细核对发票代码及号码；如有疑问，可向当地地税部门申请鉴定。");
				} else {
					if (html.contains("不相同")) {
						json.put(SysConfig.INVOICEFALSESTATE,
								SysConfig.INVOICEFALSESTATECODE203);
						json.put("cwxx", "您输入的发票金额与实际金额不符合");
						json.put("result", "false");
					} else {
						json.put("CXJG", "查验成功");
						json.put("result", "true");
					}
					JSONArray ja =  new JSONArray(result.toString());
					if(html.contains("由于发票开具信息目前尚未收到等原因，尚无法进行比对")) {
						JSONObject myjObject1 = ja.getJSONObject(3);
						myjObject1 = myjObject1.getJSONObject("data");
						//JSONArray json1 = new JSONArray(result1);
						/*
						for(int i=0;i<key.length;i++) {
							//json.put(key[i], json1.get(key));
						}*/
						json.put("发票代码", myjObject1.getJSONObject("fpbm").get("value"));
						json.put("发票号码", myjObject1.getJSONObject("fphm").get("value"));
						json.put("税务机关是否已发放", myjObject1.getJSONObject("ifSell").get("value"));
						json.put("发放税务机关", myjObject1.getJSONObject("swj").get("value"));
						json.put("付款方", myjObject1.getJSONObject("qymc").get("value"));
						json.put("开票金额与查询金额是否相同", myjObject1.getJSONObject("pd").get("value"));
						json.put("biaoshi", "1");
					}else{
						JSONObject myjObject = ja.getJSONObject(2);
						myjObject = myjObject.getJSONObject("data");
						json.put("KJDW",// 开具单位
								myjObject.getJSONObject("qymc").getString("value"));
						json.put("FKFMC",// 发款方名称
								myjObject.getJSONObject("fkfmc").getString("value"));
						json.put("KPRQ",// 开票日期
								myjObject.getJSONObject("kprq").getString("value"));
						json.put("KPNR",// 开票内容
								myjObject.getJSONObject("kpnr").getString("value"));
						json.put("HJJE",// 合计金额
								myjObject.getJSONObject("hjje").getString("value"));
						json.put("biaoshi", "2");
					}
				}
			} else {
				json.put(SysConfig.INVOICEFALSESTATE,
						SysConfig.INVOICEFALSESTATECODE102);
				json.put("cwxx", "您输入的发票信息不存在！");
			}
		} catch (Exception e) {
			json.put("cwxx", "您输入的发票信息不存在！");
		}
		return json;
	}

	public String structureAttribute(Map<String, String> in_Parameter) {
		String attributeString = "{\"tid\":\"sword?tid=WsxtXtzyfpcx100BLH_search\",\"ctrl\":\"\",\"page\":\"\",\"data\":[{\"beanname\":null,\"sword\":\"SwordForm\",\"name\":\"CwFormID\",\"data\":{\"fpdm\":{\"value\":\""
				+ in_Parameter.get("FPDM")
				+ "\"},\"fphm\":{\"value\":\""
				+ in_Parameter.get("FPHM")
				+ "\"},\"fpje\":{\"value\":\""
				+ in_Parameter.get("FPJE") + "\"}}}]}";
		return attributeString;
	}
}
