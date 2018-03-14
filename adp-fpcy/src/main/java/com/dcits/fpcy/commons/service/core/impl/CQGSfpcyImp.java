package com.dcits.fpcy.commons.service.core.impl;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.dcits.fpcy.commons.bean.CookieList;
import com.dcits.fpcy.commons.bean.IpAddress;
import com.dcits.fpcy.commons.bean.ResultBean;
import com.dcits.fpcy.commons.bean.TaxOfficeBean;
import com.dcits.fpcy.commons.constant.SysConfig;
import com.dcits.fpcy.commons.factory.utils.YzmRequestUtils;
import com.dcits.fpcy.commons.service.core.InvoiceServerBase;
import com.dcits.fpcy.commons.utils.ResultUtils;
import com.dcits.fpcy.commons.utils.SendResultRequest;
import com.dcits.fpcy.commons.utils.thirdApi.YzmsbInterface;
public class CQGSfpcyImp implements InvoiceServerBase {

	@SuppressWarnings("unused")
	private static Log logger = LogFactory.getLog(CQGSfpcyImp.class);

	/**
	 * 重庆国税发票查验
	 * 
	 * 验证码为4位汉字成语
	 * 
	 * @param parameter
	 * @param regionNum
	 * @return
	 */
	@SuppressWarnings({"rawtypes" })
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		List<ResultBean> list = new ArrayList<ResultBean>();
		try {
			getResult(fpcyParas, parameter, result, null);
		} catch (Exception e) {
			list.clear();
			list.add(new ResultBean("cwxx", "", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！"));
			result.put("list", list);
			result.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE117);
			result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		} 
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result,
			IpAddress ipAddress) throws Exception {
		Map parament = new HashMap<String, String>();
		Map parament1 = new HashMap<String, String>();
		Map parament2 = new HashMap<String, Object>();
		parament2
				.put("Accept",
						"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		parament2.put("Accept-Encoding", "gzip, deflate, sdch");
		parament2.put("Accept-Language", "zh-CN,zh;q=0.8");
		parament2.put("Cache-Control", "max-age=0");
		parament2.put("Connection", "keep-alive");
		parament2.put("Upgrade-Insecure-Requests", "1");
		parament2.put("Host", "12366.cqsw.gov.cn:5000");
		parament2
				.put("User-Agent",
						"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36");
		String address1 = "http://12366.cqsw.gov.cn:5000/captcha.jpg";
		String address2 = "http://12366.cqsw.gov.cn:5000/PortalWeb/pages/sscx/cx_fpmx.html";
		String cookie = null;
		List list = SendResultRequest.sendRequestCookie(parament2, null, null,
				address2, "get");
		cookie = CookieList.getItem(list);
		paras.setYzm_dz(address1);
		paras.setYzm_qqfs("get");
		paras.setCookie(2);
		Map map = YzmRequestUtils.sendYzmRequest(null, paras, parament2, cookie);
		String cookieList = map.get("cookieList").toString();
		if (cookieList.contains("JSESSIONID")) {
			String[] cookies = cookieList.split(";");
			cookie = cookies[1].substring(cookies[1].indexOf(",") + 1) + ";"
					+ cookies[0].substring(1);
		}
		String result = null;
		byte[] filePath = null;
		System.out.println(cookie);
		paras.setCy_yzmcs("02");
		try {
			filePath = (byte[]) map.get("filepath");
			if (filePath == null) {
				// hscEntity.setYzm(null);
			} else {
				result = YzmsbInterface.YZMSB(filePath, paras);
			}
		} catch (Exception e) {

		}
		//{fpdm=5000161271, fphm=42343333, FPDM=5000161271, FPHM=42343333, kpfmc=4234, kpje=33333333333, new=1, uid=null, invoiceComeFrom=3}
		String jsonStr0 = "{\"fpdm\":\"" +in_parameter.get("fpdm").toString() + "\",\"fphm\":\""
				+ in_parameter.get("fphm").toString() + "\",\"fkfmc\":\"" + in_parameter.get("kpfmc").toString()
				+ "\",\"kpje\":\"" + in_parameter.get("kpje").toString() + "\",\"code\":\""
				+ result.toLowerCase() + "\"}";
		String jsonStr = URLEncoder.encode(jsonStr0, "UTF-8");
		parament.put("jsonStr", jsonStr);
		parament1.put("Cookie", cookie);
		parament1.put("Accept", "*/*");
		parament1.put("Accept-Encoding", "gzip, deflate");
		parament1.put("Accept-Language", "zh-CN,zh;q= 0.8");
		parament1.put("Connection", "keep-alive");
		parament1.put("Content-Length", "239");
		parament1.put("Content-Type",
				"application/x-www-form-urlencoded; charset=UTF-8");
		parament1.put("Host", "12366.cqsw.gov.cn:5000");
		parament1.put("Origin", "http://12366.cqsw.gov.cn:5000");
		parament1
				.put("Referer",
						"http://12366.cqsw.gov.cn:5000/PortalWeb/pages/sscx/cx_fpmx.html");
		parament1
				.put("User-Agent",
						"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36");
		parament1.put("X-Requested-With", "XMLHttpRequest");
		String address = "http://12366.cqsw.gov.cn:5000/api/sscx/fpmx";
		InputStream in = SendResultRequest.sendRequestPost(parament1, null,
				address, parament);
		JSONObject json = null;
		try {

			json = parseInvoiceResult(in);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		List<ResultBean> list1 = new ArrayList<ResultBean>();
		if (json == null) {
			out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		} else
			try {
				if (json.get("result").toString().equals("false")) {
					list1.add(new ResultBean("cwxx", "", json.get("cwxx")
							.toString()));
					out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
				} else {
					list1.add(new ResultBean("pz", "票种",paras.swjg_mc));
					String key[] = { "发票代码", "发票号码", "销方纳税人名称", "购方纳税人名称",
							"发票名称", "销方纳税人识别号", "金额", "开票日期", "购方纳税人识别号" };
					for(int i = 0;i<key.length;i++) {
						System.out.println(ResultUtils.getPinYinHeadChar(key[i]).toLowerCase());
						if(StringUtils.isNotBlank(json.getString(ResultUtils.getPinYinHeadChar(key[i]).toLowerCase()))) {
							list1.add(new ResultBean(ResultUtils.getPinYinHeadChar(key[i]).toLowerCase(), key[i], json.getString(ResultUtils.getPinYinHeadChar(key[i]).toLowerCase())));
						}
					}
					out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
				}
			} catch (JSONException e) {
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
				out_result.put(SysConfig.INVOICEFALSESTATE,
						SysConfig.INVOICEFALSESTATECODE104);
			}
		out_result.put(SysConfig.INVOICEFALSESTATE,
				json.get(SysConfig.INVOICEFALSESTATE).toString());
		out_result.put("list", list1);
	}

	public String toUtf8String(String s) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c >= 0 && c <= 255) {
				sb.append(c);
			} else {
				byte[] b;
				try {
					b = String.valueOf(c).getBytes("utf-8");
				} catch (Exception ex) {
					b = new byte[0];
				}
				for (int j = 0; j < b.length; j++) {
					int k = b[j];
					if (k < 0)
						k += 256;
					sb.append("%" + Integer.toHexString(k).toUpperCase());
				}
			}
		}
		return sb.toString();
	}

	/**
	 * 重庆国税结果解析
	 * 
	 * @throws JSONException
	 */

	public  JSONObject parseInvoiceResult(InputStream in) throws JSONException {

		JSONObject json = new JSONObject();
		json.put("result", "false");
		if (in == null) {
			json.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE101);
			json.put("cwxx", "您输入的发票信息不匹配，请检查是否输入正确！");
			return json;
		}
		JSONObject doc = (JSONObject) SendResultRequest.iSToJSONOrDocument(in,
				"UTF-8", "json");
		if (doc.get("success").equals(true)) {
			JSONArray data0 = (JSONArray) doc.get("data");
			json = data0.getJSONObject(0);
			json.put("result", "true");
			json.put(SysConfig.INVOICEFALSESTATE, "000");
		} else {
			//{"success":false,"jylsh":null,"message":null,"messageCode":"80480403","paramList":["未查询到该发票信息,请仔细核对发票代码和号码，并注意数据更新延迟时间"],"data":null,"total":null,"otherParams":null}
			json.put("cwxx", "未查询到该发票信息,请仔细核对发票代码和号码，并注意数据更新延迟时间!");
			json.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE203);
		}
		return json;
	}

	@SuppressWarnings("rawtypes")
	public Map FPCY(Map parameter) throws Exception {
		return null;
	}
}
