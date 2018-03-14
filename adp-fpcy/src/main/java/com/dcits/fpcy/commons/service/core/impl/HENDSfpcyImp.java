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
import com.dcits.fpcy.commons.constant.BrowerType;
import com.dcits.fpcy.commons.constant.HeaderType;
import com.dcits.fpcy.commons.constant.SysConfig;
import com.dcits.fpcy.commons.service.core.InvoiceServerBase;
import com.dcits.fpcy.commons.utils.SendResultRequest;

/**
 * 河南地税24100
 * 
 * 
 */
public class HENDSfpcyImp implements InvoiceServerBase {

	private Log logger = LogFactory.getLog(HENDSfpcyImp.class);

	@SuppressWarnings({ "rawtypes" })
	@Override
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			getResult1(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("河南地税,获取cook时出现异常", e);
			List<ResultBean> list = new ArrayList<ResultBean>();
			list.clear();
			list.add(new ResultBean("cwxx", "", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！"));
			result.put("list", list);
			result.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE103);
			result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		}
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void getResult1(TaxOfficeBean paras, Map in_parameter, Map out_result, IpAddress ipAddress) throws Exception {
		String fpdm = in_parameter.get("FPDM").toString();
		String fphm = in_parameter.get("FPHM").toString();
		String je = in_parameter.get("fpje").toString();
		String mm = in_parameter.get("fpmm").toString();
		String dz = paras.cy_dz;
		String uuid2 = "fzZLBWzd0PWWnQqfEMDYkxX5ooVOtieB";
		double r2 = 0.22208601640709852;
		long date1 = System.currentTimeMillis();
		List list1 = new ArrayList();
		list1.add(getDataList("fpdm", fpdm));
		list1.add(getDataList("fphm", fphm));
		Map requestHeader = new HashMap();
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		Map param = new HashMap();
		date1 = System.currentTimeMillis();
		String dz2 = dz + "?r=" + r2 + "&sDate=" + date1 + "&sName=ajax.sword?r=" + r2 + "&sDate=" + date1 + "&rUUID="
				+ uuid2 + "&__swm=0";
		list1.add(getDataList("mm", mm));
		list1.add(getDataList("je", je));
		JSONObject map1 = new JSONObject();
		map1.put("tid", "");
		map1.put("page", "");
		map1.put("bindParam", true);
		map1.put("ctrl", ("WwSscxFpzwcxCtrl_queryFpxx?sDate=" + date1));
		map1.put("data", list1);
		param.put("postData", map1.toString());
		InputStream in2 = SendResultRequest.sendRequestPost(requestHeader, ipAddress, dz2, param);
		JSONObject json1 = parseInvoiceResult1(in2);
		JSONArray jg2 = (JSONArray) json1.getJSONArray("data");
		JSONObject m21 = (JSONObject) jg2.get(0);
		JSONObject m22 = (JSONObject) m21.get("data");
		String fpzt = ((JSONObject) m22.get("fpzt")).get("value").toString();
		List<ResultBean> list = new ArrayList<ResultBean>();
		try {
			if ("正常".equals(fpzt)) {
				list.add(new ResultBean("nsrxx", "开具单位:", ((JSONObject) m22.get("nsrxx")).get("value").toString()));
				list.add(new ResultBean("cxcs", "查询次数:", ((JSONObject) m22.get("cxcs")).get("value").toString()));
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
			}
			list.add(new ResultBean("pmje", "票面金额", ((JSONObject) m22.get("pmje")).get("value").toString()));
			list.add(new ResultBean("zjje", "中奖金额", ((JSONObject) m22.get("zjje")).get("value").toString()));
			if (!"".equals(((JSONObject) m22.get("fpzlmc")).get("value").toString())) {
				list.add(new ResultBean("fpzlmc", "发票名称", ((JSONObject) m22.get("fpzlmc")).get("value").toString()));
			}
			list.add(new ResultBean("fpzt", "发票状态", fpzt));
			list.add(new ResultBean("hzxx", "备注信息", ((JSONObject) m22.get("hzxx")).get("value").toString()));
			out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
		} catch (Exception e) {
			logger.error("河南地税解析JSONObject异常：" + e);
			logger.error(paras.swjg_mc + "解析返回参数异常", e);
			out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			out_result.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE104);
			e.printStackTrace();
		}
		out_result.put("list", list);
	}

	public JSONObject parseInvoiceResult1(InputStream in) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("result", "false");
		try {
			if (in != null) {
				json = (JSONObject) SendResultRequest.iSToJSONOrDocument(in, "utf-8", "json");
			} else {
				if (in == null) {
					json.put("cwxx", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！");
					json.put(SysConfig.INVOICEFALSESTATE, "102");
					return json;
				}
			}
		} catch (Exception e) {
			try {
				json.put("cwxx", "没有查询到此税控发票信息");
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE111);
				json.put("result", "false");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception e) {
				try {
					json.put("cwxx", "没有查询到此税控发票信息");
					json.put("result", "false");
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
			}
		}
		return json;
	}

	@SuppressWarnings({ "rawtypes" })
	public Map FPCY(Map parameter) throws Exception {
		return null;
	}

	// 编辑数据
	public JSONObject getDataList(String name, String value) throws Exception {
		JSONObject map = new JSONObject();
		map.put("name", name);
		map.put("value", value);
		map.put("sword", "attr");
		return map;
	}

}
