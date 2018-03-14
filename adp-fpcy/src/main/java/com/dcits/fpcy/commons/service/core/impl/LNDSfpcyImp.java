package com.dcits.fpcy.commons.service.core.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import com.dcits.fpcy.commons.utils.HttpUtils;
import com.dcits.fpcy.commons.utils.SendResultRequest;

public class LNDSfpcyImp implements InvoiceServerBase {
	private Log logger = LogFactory.getLog(LNDSfpcyImp.class);

	/**
	 * 第二版 辽宁地税
	 * 
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			parameter.put("rand", hscEntity.getYzm());
			parameter.put("JSESSIONID", hscEntity.getCookie());
			// 开始查验
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("辽宁地税,获取cook时出现异常", e);
			List list = new ArrayList<ResultBean>();
			list.clear();
			list.add(new ResultBean("cwxx", "", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！"));
			result.put("list", list);
			result.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE103);
			result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		}
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void getResult(TaxOfficeBean paras, Map in_Parameter, Map out_Result, IpAddress ipAddress) throws Exception {
		Map<String, String> attributeMap = new HashMap<String, String>();
		Map<String, String> requestHeader = new HashMap<String, String>();
		attributeMap.put("fpdm1", in_Parameter.get("FPDM").toString());
		attributeMap.put("fphm1", in_Parameter.get("FPHM").toString());
		attributeMap.put("TABLE_ACTION", "display");
		attributeMap.put("TABLE_NAME", "FP_ZWCX");
		attributeMap.put("checkNum", in_Parameter.get("rand").toString());
		String JSESSIONID = in_Parameter.get("JSESSIONID").toString();
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.COOKIE, JSESSIONID);
		requestHeader.put("Origin", "http://fpcx.lnsds.gov.cn");
		requestHeader.put("Upgrade-Insecure-Requests", "1");
		requestHeader.put(HeaderType.REFERER, "http://fpcx.lnsds.gov.cn/jsp/fpzwcx/FPZWCX.jsp");
		InputStream in = SendResultRequest.sendRequestPost(requestHeader, ipAddress, paras.cy_dz, attributeMap);
		JSONObject json = parseInvoiceResult(in);
		List<ResultBean> list = new ArrayList<ResultBean>();
		try {
			if (("false").equals(json.get("result").toString())) {
				list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
				out_Result.put(SysConfig.INVOICEFALSESTATE, json.getString(SysConfig.INVOICEFALSESTATE));
				out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			} else {
				list.add(new ResultBean("GPRQ", "购票日期", json.getString("购票日期")));
				list.add(new ResultBean("FPZL", "发票种类", json.getString("发票种类")));
				list.add(new ResultBean("NSRMC", "纳税人名称", json.getString("纳税人名称")));
				list.add(new ResultBean("FPMC", "发票名称", json.getString("发票名称")));
				list.add(new ResultBean("PMJE", "票面金额", json.getString("票面金额")));
				list.add(new ResultBean("FSSWJG", "发售税务机关", json.getString("发售税务机关")));
				list.add(new ResultBean("FPZT", "发票状态", json.getString("发票状态")));
				out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
			}
		} catch (Exception e) {
			logger.error("辽宁地税税解析JSONObject异常：" + e);
			logger.error(paras.swjg_mc + "解析返回参数异常", e);
			out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			out_Result.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE104);
			e.printStackTrace();
		}
		out_Result.put("list", list);
	}

	public JSONObject parseInvoiceResult(InputStream in) throws Exception {
		JSONObject jso = new JSONObject();
		Document doc = (Document) SendResultRequest.iSToJSONOrDocument(in, "GBK", "text");
		if (in == null) {
			jso.put("cwxx", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！");
			jso.put(SysConfig.INVOICEFALSESTATE, "102");
			return jso;
		}
		String html = doc.toString();
		try {
			if (html.contains("您查询的发票信息与发票开具的实际信息不一致")) {
				jso.put("result", false);
				jso.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE211);
				jso.put("cwxx",
						"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;您查询的发票信息与发票开具的实际信息不一致，请您核对录入信息是否正确！"
								+ "</br>" + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;如有疑义，请您向当地地税机关核实和举报，也可以拨打12366电话举报！");
				return jso;
			}
			if (html.contains("您输入的验证码不正确")) {
				jso.put("result", false);
				jso.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE101);
				jso.put("cwxx", "您输入的验证码不正确!");
				return jso;
			}
			if (html.contains("验证码已变化")) {
				jso.put("result", false);
				jso.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE101);
				jso.put("cwxx", "验证码已变化!请重新输入验证码！");
				return jso;
			}
			int[] indexes = { 8, 9, 10, 11, 12, 13, 14 }; // 数组里存放的是解析过后所需的组件的索引
			String[] nameOfIndexes = { "购票日期", "发票种类", "纳税人名称", "发票名称", "票面金额", "发售税务机关", "发票状态" };
			Elements eles = doc.select("form").select("tbody").select("tbody").select("tr").select("td").select("font");
			if (eles.size() > 15) { // 若第一次解析成功，则为成功的请求
				jso.put("result", "true");
				for (int i = 0; i < indexes.length; ++i) {
					jso.put(nameOfIndexes[i], eles.get(indexes[i]).text());
				}
			} else {
				jso.put("result", "false");
				jso.put("cwxx", "发票查询时出现异常，请重试！");
				return jso;
			}
		} catch (Exception e) {

			jso.put("result", "false");
			logger.error("辽宁地税获取解析异常", e);
			jso.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE111);
			jso.put("cwxx", "此种票样查询发生异常，请稍后重试");
			return jso;
		}
		return jso;
	}

	/**
	 * 辽宁地税
	 * 
	 * @param JSESSIONID
	 * @return
	 * @throws Exception
	 */
	public static String getLNDSYzm(String JSESSIONID, IpAddress ipAddress) throws Exception {
		JSONObject json = new JSONObject();
		InputStream in = null;
		BufferedReader bufferedReader = null;
		String ss = null;
		String html = "";
		try {
			URL url = new URL("http://fpcx.lnsds.gov.cn/jsp/fpzwcx/auto.jsp");
			HttpURLConnection con = HttpUtils.getConnection(url, ipAddress);
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setRequestMethod("GET");
			con.setUseCaches(false);
			con.setRequestProperty("User-Agent", BrowerType.firfox);
			con.setRequestProperty("Referer", "http://fpcx.lnsds.gov.cn/jsp/fpzwcx/FPZWCX.jsp");
			con.setRequestProperty("Cookie", JSESSIONID);
			in = con.getInputStream();
			bufferedReader = new BufferedReader(new InputStreamReader(in, "GBK"));
			while ((ss = bufferedReader.readLine()) != null) {
				html += ss;
			}
			html = html.substring(html.indexOf("xx>") + 3, html.indexOf("</yzm"));

		} catch (IOException e) {
			System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
		} finally {
			try {
				if (in != null)
					in.close();
				if (bufferedReader != null)
					bufferedReader.close();
			} catch (IOException e) {
				System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
				json.put("cwxx", "查询发生错误");
			}
		}
		return html;
	}
}
