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


public class BJGSfpcyImp implements InvoiceServerBase {

	/**
	 * 北京国税发票校验
	 */
	private static Log logger = LogFactory.getLog(BJGSfpcyImp.class);
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity  hscEntity=null;
		try {
			hscEntity=(HSCEntity) parameter.get("hscEntity");
			parameter.put("rand", hscEntity.getYzm());
			String lastSession=hscEntity.getCookie();
			String jsessionid = lastSession.substring(0,
					lastSession.lastIndexOf("!"));
			parameter.put("lastSession", lastSession);
			parameter.put("JSESSIONID", jsessionid);
			// 开始查验
			getResult(fpcyParas, parameter, result,hscEntity.getIpAddress());

		} catch (Exception e) {
			logger.error("北京国税,获取cook时出现异常", e);
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result,
			IpAddress ipAddress) throws JSONException {
		Map requestHeader = new HashMap();
		Map attributeMap=new HashMap();
		attributeMap.put("fpdm", in_parameter.get("fpdm").toString());
		attributeMap.put("fphm", in_parameter.get("fphm").toString());
		attributeMap.put("fpmm", in_parameter.get("fpmm").toString());
		attributeMap.put("valiNum", in_parameter.get("rand").toString());
//		attributeMap.put("ip", ipAddress.getIp());
		attributeMap.put("isFrist", in_parameter.get("isFrist").toString());
		attributeMap.put("sfzh", in_parameter.get("sfzh").toString());
		attributeMap.put("kpri", in_parameter.get("kpri").toString());
		attributeMap.put("nsr", in_parameter.get("nsr").toString());
		attributeMap.put("kjje", in_parameter.get("kjje").toString());
		String JSESSIONID = in_parameter.get("JSESSIONID").toString();
		String lastSession = in_parameter.get("lastSession").toString();
		attributeMap.put("lastSession", lastSession);
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.COOKIE, JSESSIONID);
		requestHeader.put("localid", "1");
		InputStream in = SendResultRequest.sendRequestIn(requestHeader,
				ipAddress, attributeMap, paras.cy_dz, paras.cy_qqfs);
		JSONObject json = parseInvoiceResult(in);
		List<ResultBean> list =new ArrayList<ResultBean>();
		try {
			if (json == null) {
				out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE20011);
			} else if (json.get("result").toString() == "false") {
				list.add(new ResultBean("cwxx","",json.get("cwxx").toString()));
				if(json.has(SysConfig.INVOICEFALSESTATE)){
					out_result.put(SysConfig.INVOICEFALSESTATE,json.getString(SysConfig.INVOICEFALSESTATE));
				}else{
					out_result.put(SysConfig.INVOICEFALSESTATE,SysConfig.INVOICEFALSESTATECODE215);
				}
				out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE20011);
			} else {
				String fpdm=json.getString("FPDM");
				if(fpdm.length()==12) {
					String key[] = {"发票代码","发票号码","发票密码","单位名称","查询结果"};
					list = ResultUtils.getListInfoFromJson(key, json);
				}else if(fpdm.length()==10) {
					String key[] = {"发票代码","发票号码","纳税人识别号","开票金额","开票日期","购票单位","查询结果"};
					list = ResultUtils.getListInfoFromJson(key, json);
				}		
				out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE1000);
			}
		} catch (Exception e) {
			logger.error("", e);
		}
		out_result.put("list", list);
	}

	/**
	 * 北京国税结果解析
	 * 
	 * @throws JSONException
	 */
	public static JSONObject parseInvoiceResult(InputStream in) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("result", "false");
		Document document = (Document) SendResultRequest.iSToJSONOrDocument(in,
				"UTF-8", "text");
		String result = document.toString();
		try {
			if (result.contains("meta http-equiv")) {
				Elements els = document.select("script");
				String script = els.get(6).toString();
				int i = script.indexOf("lastSession");
				int j = script
						.indexOf("document.getElementById(\"yzmIMG\").src = \"getVFImage?sessionrandom=\" ");
				String lastSession = script.substring(i, j);
				lastSession = lastSession.substring(
						lastSession.indexOf("'") + 1,
						lastSession.lastIndexOf("'"));
				json.put("lastSession", lastSession);
			} else if (result.contains("FPDM")) {
				Elements els = document.select("n");
				String[] key = { "YZM", "FPDM", "FPHM", "FPMM", "DWMC", "kpri",
						"nsr1", "CXJG", "KPJE", "ZJ" };
				for (int i = 0; i < els.size(); i++) {
					json.put(key[i], els.get(i).text());
				}
				String CXJG = json.get("CXJG").toString();
				if (CXJG == null) {
					json.put("cwxx", "发票查询失败，请稍候重试！");
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE103);
				} else if (CXJG.contains("正确查询")) {
					CXJG = CXJG.replace("tz真", "相符");
					json.put("CXJG", CXJG);
					json.put("result", "true");
					json.put(SysConfig.INVOICEFALSESTATE, "000");
				} else if (CXJG.contains("税控后台校验比对结果")
						&& CXJG.contains("北京市国家税务局")) {
					CXJG = CXJG.replace("fj假", "不符");
					json.put("cwxx", CXJG);
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE201);
				} else if (CXJG.contains("代码、号码、密码及税控后台校验比对结果")) {
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE201);
					json.put("cwxx", "无此发票信息，请向相关部门举报。");
				}else if (CXJG.contains("税控后台未接收到该发票的明细上传信息")) {
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE201);
					json.put("cwxx", "税控后台未接收到该发票的明细上传信息。");
				}
				else if (CXJG.contains("不是第一次查询")) {
					json.put("CXJG", CXJG);
					json.put("result", "true");
					json.put(SysConfig.INVOICEFALSESTATE, "000");
				}
			} else {
				json.put("cwxx", "发票查询失败，请稍候重试！");
				json.put(SysConfig.INVOICEFALSESTATE,
						SysConfig.INVOICEFALSESTATECODE215);
			}
			return json;
		} catch (Exception e) {
			json.put("cwxx", "发票查询异常，请稍候重试！");
			json.put("result", "false");
			logger.error("", e);
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception e) {
				json.put("cwxx", "发票查询异常，请稍候重试！");
				json.put("result", "false");
				logger.error("", e);
			}
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static String getLastSession(IpAddress ipAddress)
			throws JSONException {
		Map map = new HashMap();
		InputStream in = SendResultRequest.sendRequestIn(null, ipAddress, map,
				"http://www.bjtax.gov.cn/ptfp/fpindex.jsp", "GET");
		JSONObject json = parseInvoiceResult(in);
		String lastSession = json.get("lastSession").toString();
		return lastSession;
	}
}
