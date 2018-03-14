package com.dcits.fpcy.commons.service.core.impl;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import com.dcits.fpcy.commons.constant.HeaderType;
import com.dcits.fpcy.commons.constant.SysConfig;
import com.dcits.fpcy.commons.service.core.InvoiceServerBase;
import com.dcits.fpcy.commons.utils.HttpUtils;
import com.dcits.fpcy.commons.utils.SendResultRequest;

public class JLDSfpcyImp implements InvoiceServerBase {

	private Log logger = LogFactory.getLog(JLDSfpcyImp.class);

	/**
	 * 第二版 吉林地税
	 * 
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map FPCY(Map parameter, TaxOfficeBean fppars){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			Map map = sendType(fppars, parameter.get("FPDM").toString(), hscEntity.getIpAddress());
			parameter.put("JSESSIONID", map.get("JSESSIONID").toString());
			// 开始查验
			getResult(fppars, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("吉林地税,获取cook时出现异常", e);
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
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map sendType(TaxOfficeBean fppars, String InvCode, IpAddress ipAddress) {
		Map backMap = null;
		OutputStream out = null;
		DataOutputStream dos = null;
		String attributeString = null;
		List<String> cookieList = null;
		try {
			// 创建连接
			URL url = new URL(
					"http://125.32.16.115:8013/InvJudge/InvTypeJudge.do?timeStamp="
							+ new Date().getTime());
			HttpURLConnection con = HttpUtils.getConnection(url, ipAddress);// connection.getConnection(url);
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setUseCaches(false);
			con.setRequestProperty("Origin", "http://125.32.16.115:8013");
			con.setRequestProperty("Accept", "*/*");
			con.setRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
			con.setRequestProperty("Referer",
					"http://125.32.16.115:8013/InvJudge/");
			con.setRequestProperty(
					"User-Agent",
					"Mozilla/5.0 (Windows NT 6.3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");

			attributeString = "InvCode=" + InvCode;
			// 发送参数流
			out = con.getOutputStream();
			dos = new DataOutputStream(out);
			dos.writeBytes(attributeString);
			dos.flush();
			if (con.getResponseCode() != 200)
				return null;
			Map topheader = con.getHeaderFields();// 相应头信息
			Set headerSet = topheader.entrySet();// 返回此映射中包含的映射关系的 Set 视图
			Iterator it = headerSet.iterator();

			while (it.hasNext()) {
				Entry<String, List> en = (Entry<String, List>) it.next();
				if ("set-cookie".equalsIgnoreCase(en.getKey())) {
					cookieList = en.getValue();
				}
			}
			// 创建返回信息Map
			backMap = new HashMap();
			@SuppressWarnings("unused")
			int i = 0;
			for (String jsestr : cookieList) {
				if (jsestr.contains("JSESSION") || jsestr.contains("Session")
						|| jsestr.contains("JSID_KPGL")
						|| jsestr.contains("wsswCookie")) {
					backMap.put("JSESSIONID", jsestr);
				}
			}
		} catch (Exception e) {
			System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
		} finally {
			try {
				if (dos != null)
					dos.close();
				if (out != null)
					out.close();
			} catch (Exception e) {
				System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
			}
		}
		return backMap;
	}

	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result,
			IpAddress ipAddress) throws Exception{
		Map attributeMap = new HashMap();
		attributeMap.put("WinWidth", "800");
		attributeMap.put("InvCode", in_parameter.get("FPDM").toString());
		attributeMap.put("InvNo", in_parameter.get("FPHM").toString());
		attributeMap.put("InvAfxCode", in_parameter.get("InvAfxCode").toString());
		String fkfmc = in_parameter.get("fkfmc").toString();
		try {
			fkfmc = URLEncoder.encode(fkfmc, "utf-8");
			fkfmc = URLEncoder.encode(fkfmc, "utf-8");
			attributeMap.put("fkfmc", fkfmc);
		} catch (UnsupportedEncodingException e) {
			out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE20011);
		}
		String requestMethod = paras.cy_qqfs;
		String JSESSIONID = in_parameter.get("JSESSIONID").toString();
		Map requestHeader = new HashMap();
		requestHeader.put("Origin", "http://125.32.16.115:8013");
		requestHeader.put("Accept", "*/*");
		requestHeader.put("Referer", "http://125.32.16.115:8013/InvJudge/");
		requestHeader.put("Accept-Encoding", "gzip, deflate");
		requestHeader
				.put("User-Agent",
						"Mozilla/5.0 (Windows NT 6.3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
		requestHeader.put(HeaderType.COOKIE, JSESSIONID);
		InputStream in = SendResultRequest.sendRequestPost(requestHeader,
				ipAddress, paras.cy_dz, attributeMap);
		JSONObject cxjg = parseInvoiceResult(in);
		List<ResultBean> list = new ArrayList<ResultBean>();
		if (cxjg.getString("result") == "true") {
			list.add(new ResultBean("FPDM", "发票代码",in_parameter.get("FPDM").toString()));
			list.add(new ResultBean("FPHM", "发票号码", in_parameter.get("FPHM").toString()));
			list.add(new ResultBean("FPKJF", "发票开具方",cxjg.getString("FPKJF")));
			list.add(new ResultBean("SWJG", "税务机关",cxjg.getString("SWJG")));
			list.add(new ResultBean("FPZT", "发票状态", cxjg.getString("FPZT")));
			out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE1000);
		} else {
			list.add(new ResultBean("cwxx", "", cxjg.get("cwxx").toString()));
			out_result.put(SysConfig.INVOICEFALSESTATE,cxjg.getString(SysConfig.INVOICEFALSESTATE));
			out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE20011);
		}
		out_result.put("list", list);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public JSONObject parseInvoiceResult(InputStream in) throws Exception{
		JSONObject json = new JSONObject();
		json.put("result", "false");
		BufferedReader bufferedReader = null;
		Map map = null;
		String html = null;
		Map bmmap = new HashMap();
		bmmap.put(1, "gbk");
		bmmap.put(2, "utf-8");
		bmmap.put(3, "gb2312");
		for(int bm = 0;bm<bmmap.size();bm++){
			Document document = (Document) SendResultRequest.iSToJSONOrDocument(in,
					bmmap.get(bm+1).toString(), "text");
			html = document.toString();
			if(html.contains("<title>吉林省地方税务局发票真伪查询</title>"))break;
		}
		try {
			if (in != null) {
				// 20160126 修改代码 by wangkej
				// 原因:查询时有查询结果时返回信息为GBK编码;无结果时,返回时的信息是UTF-8的编码
				// 原有方法无法解决编码区分问题,故将该段代码进行修改
				Document doc = Jsoup.parse(html);
				Elements eles = doc.getElementsByClass("font");
				if (eles.size() > 0) {
					json.put("result", "true");
					int count = 0;
					map = new HashMap();
					for (Element e : eles) {
						map.put(count, e.text().toString());
						count++;
					}
					String fpzt = map.get(1).toString();
					fpzt = fpzt.substring(fpzt.indexOf("：") + 1);
					String fpkjf = map.get(2).toString();
					fpkjf = fpkjf.substring(fpkjf.indexOf("：") + 1);
					String swjg = map.get(3).toString();
					swjg = swjg.substring(swjg.indexOf("：") + 1);
					json.put("FPZT", fpzt);
					json.put("FPKJF", fpkjf);
					json.put("SWJG", swjg);
				} else {
					json.put("result", "false");
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE201);
					json.put("cwxx", "请联系税务机关!鉴别真伪!");
				}
			} else {
				json.put("cwxx", "您输入的信息有误，请重新输入！");
				json.put(SysConfig.INVOICEFALSESTATE,
						SysConfig.INVOICEFALSESTATECODE203);
				json.put("result", "false");
			}
		} catch (Exception e) {
			System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
			json.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE203);
			json.put("cwxx", "您输入的信息有误，请重新输入！");
			json.put("result", "false");
		} finally {
			try {
				if (in != null)
					in.close();
				if (bufferedReader != null)
					bufferedReader.close();
			} catch (Exception e) {
				System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
				json.put("cwxx", "您输入的信息有误，请重新输入！");
				json.put("result", "false");
			}
		}
		return json;
	}

	@SuppressWarnings({ "rawtypes", "unused" })
	public InputStream sendResultRequestPost(String requestAddress,
			Map attributeMap, String JSESSIONID) {
		InputStream in = null;
		OutputStream out = null;
		DataOutputStream dos = null;
		String attributeString = null;
		String code = null;
		try {
			URL url = new URL(requestAddress + "?timeStamp="
					+ new Date().getTime());
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setUseCaches(false);
			con.setRequestProperty("Origin", "http://125.32.16.115:8013");
			con.setRequestProperty("Accept", "*/*");
			con.setRequestProperty("Referer",
					"http://125.32.16.115:8013/InvJudge/");
			con.setRequestProperty("Accept-Encoding", "gzip, deflate");
			con.setRequestProperty(
					"User-Agent",
					"Mozilla/5.0 (Windows NT 6.3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
			if (attributeMap.containsKey("JSESSIONID")) {
				JSESSIONID = JSESSIONID.substring(0, JSESSIONID.indexOf(";"));
				con.setRequestProperty("Cookie", JSESSIONID);
				attributeMap.remove("JSESSIONID");
			}
			attributeString = SendResultRequest.structureAttribute(attributeMap);
			// 发送参数流
			out = con.getOutputStream();
			dos = new DataOutputStream(out);
			dos.writeBytes(attributeString);
			dos.flush();
			code = con.getResponseCode() + "";
			if (con.getResponseCode() != 200)
				return null;
			in = con.getInputStream();// 获取发票验真返回信息
			return in;
		} catch (Exception e) {
			System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
		} finally {
			try {
				if (dos != null)
					dos.close();
				if (out != null)
					out.close();
			} catch (Exception e) {
				System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
			}
		}
		return in;
	}
}
