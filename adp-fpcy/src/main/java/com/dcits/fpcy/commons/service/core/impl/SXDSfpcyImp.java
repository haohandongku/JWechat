package com.dcits.fpcy.commons.service.core.impl;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONObject;

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
import com.dcits.fpcy.commons.utils.TrustAllHosts;
import com.dcits.fpcy.commons.utils.thirdApi.YzmsbInterface;

/**
 * 陕西地税发票查验 陕西地税26100 20160126 修改 需要二次访问，confirmflag值无法获取，维持原有实现类
 * 
 */
public class SXDSfpcyImp implements InvoiceServerBase {
	private Log logger = LogFactory.getLog(SXDSfpcyImp.class);


	/**
	 * 发送验证码
	 * 
	 * @param yzm_dz
	 * @param yzm_qqfs
	 * @param yzm_tplx
	 * @param jessionID
	 * @return
	 */
	@SuppressWarnings("unused")
	private Map<String, Object> sendYzmRequest(TaxOfficeBean fpcyParas, String jessionID) {
		ByteArrayOutputStream out = null;
		byte[] buffer = null;
		InputStream in = null;
		Map<String, Object> backMap = null;
		String yzm_dz = fpcyParas.yzm_dz;
		String yzm_qqfs = fpcyParas.yzm_qqfs.toUpperCase();
		String yzm_tplx = fpcyParas.yzm_tplx;
		try {
			// 创建连接
			URL url = new URL(yzm_dz + "time=" + new Date().getTime());
			TrustAllHosts.trustAllHosts();
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setRequestMethod(yzm_qqfs);
			con.setUseCaches(false);
			con.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
			con.setRequestProperty("Referer", "http://fpcx.xads.gov.cn/sxlt/inv/invqueryinit.do");
			con.setRequestProperty("Cookie", jessionID);

			in = con.getInputStream();
			backMap = new HashMap<String, Object>();
			out = new ByteArrayOutputStream(1000);
			byte[] b = new byte[1000];
			int n;
			while ((n = in.read(b)) != -1) {
				out.write(b, 0, n);
			}
			buffer = out.toByteArray();
			backMap.put("filepath", buffer);

		} catch (Exception e) {
			logger.error("[ERROR] 下载验证码异常", e);
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					logger.error("[ERROR] 下载验证码异常", e);
				}
		}
		return backMap;
	}

	/**
	 * 获取cookie和发票类型
	 */
	private Map<String, String> invQueryNext(Map<String, String> in_parameter, IpAddress ipAddress) {
		InputStream in = null;
		Map<String, String> backMap = new HashMap<String, String>();
		backMap.put("result", "true");
		List<String> cookieList = null;
		BufferedReader bufferedReader = null;

		try {
			// 创建连接
			URL url = new URL("http://fpcx.xads.gov.cn/sxlt/inv/invquerynext.do?paramOne="
					+ in_parameter.get("FPDM").toString() + "&time=" + new Date().getTime());
			// Connection connection = new Connection();
			HttpURLConnection con = HttpUtils.getConnection(url, ipAddress);
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setRequestMethod("GET");
			con.setUseCaches(false);
			con.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
			con.setRequestProperty("Origin", "http://fpcx.xads.gov.cn");
			con.setRequestProperty("Referer", "http://fpcx.xads.gov.cn/sxlt/inv/invqueryinit.do");
			con.setRequestProperty("X-Requested-With", "XMLHttpRequest");
			con.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");

			// 响应头信息
			Map<String, List<String>> topheader = con.getHeaderFields();
			// 返回此映射中包含的映射关系的 Set 视图
			Set<Entry<String, List<String>>> headerSet = topheader.entrySet();
			Iterator<Entry<String, List<String>>> it = headerSet.iterator();
			while (it.hasNext()) {
				Entry<String, List<String>> en = (Entry<String, List<String>>) it.next();
				if ("set-cookie".equalsIgnoreCase(en.getKey())) {
					cookieList = en.getValue();
				}
			}
			backMap.put("JSESSIONID", cookieList.toString());

			in = con.getInputStream();// 获取发票验真返回信息
			if (in != null) {
				bufferedReader = new BufferedReader(new InputStreamReader(in, "GBK"));
				String ss = null;
				String html = "";
				while ((ss = bufferedReader.readLine()) != null) {
					html += ss;
				}
				JSONObject json = new JSONObject(html);
				backMap.put("confirmFlag", json.getString("confirmFlag"));
				in.close();
			}
		} catch (Exception e) {
			logger.error("[ERROR] 解析异常：", e);
			backMap.put("result", "false");
			return backMap;
		}
		return backMap;
	}

	/**
	 * 获取结果
	 * 
	 * @param paras
	 * @param in_parameter
	 * @param out_result
	 * @param ipAdderss
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result, IpAddress ipAddress) throws Exception {

		Map<String, String> attributeMap = new HashMap<String, String>();
		attributeMap.put("queryType", in_parameter.get("queryType").toString());
		attributeMap.put("paramOne", in_parameter.get("FPDM").toString());
		attributeMap.put("paramTwo", in_parameter.get("FPHM").toString());
		attributeMap.put("securityCode", in_parameter.get("rand").toString());

		String requestMethod = paras.cy_qqfs;
		String JSESSIONID = in_parameter.get("JSESSIONID").toString();
		attributeMap.put("JSESSIONID", JSESSIONID);

		// 20160126
		// 修改为公用接口，添加动态IP
		if ("get".equals(requestMethod)) {
			String requestAddress = "";
			requestAddress = paras.cy_dz;
			requestAddress = structureRequestAddress(requestAddress, attributeMap);
			Map requestHeader = new HashMap();
			requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
			requestHeader.put(HeaderType.COOKIE, JSESSIONID);
			requestAddress += ("&time=" + System.currentTimeMillis());
			InputStream in = SendResultRequest.sendRequestIn(requestHeader, ipAddress, attributeMap, requestAddress,
					"GET");
			JSONObject json = parseInvoiceResult(in);
			List<ResultBean> list = new ArrayList<ResultBean>();
			if ("false".equals(json.get("result"))) {
				list.add(new ResultBean("CXJG", "查询结果", json.getString("cwxx")));
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			} else {

				list.add(new ResultBean("FPZLMC", "发票种类名称", json.get("FPZLMC").toString()));
				list.add(new ResultBean("LGDWMC", "领过单位名称", json.get("LGDWMC").toString()));
				list.add(new ResultBean("LGSJ", "领购时间", json.get("LGSJ").toString()));
				list.add(new ResultBean("FSSWJG", "发售税务机关", json.get("FSSWJG").toString()));
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
			}
			out_result.put("list", list);

		}
	}

	/**
	 * 拼接参数
	 * 
	 * @param address
	 * @param attributeMap
	 * @return
	 */
	private String structureRequestAddress(String address, Map<String, String> attributeMap) {
		Set<Entry<String, String>> attributeESet = attributeMap.entrySet();
		Iterator<Entry<String, String>> it = attributeESet.iterator();
		while (it.hasNext()) {
			Entry<String, String> attributeEntry = it.next();
			address = address.replace("=" + attributeEntry.getKey(), "=" + attributeEntry.getValue());
		}
		return address;

	}

	/**
	 * 解析数据
	 * 
	 * @param in
	 * @return
	 */
	public JSONObject parseInvoiceResult(InputStream in) throws Exception {
		JSONObject jso = null;
		BufferedReader bufferedReader = null;
		JSONObject json = new JSONObject();
		json.put("result", "false");
		try {
			if (in != null) {
				bufferedReader = new BufferedReader(new InputStreamReader(in, "GBK"));
				String ss = null;
				String html = "";
				while ((ss = bufferedReader.readLine()) != null) {
					html += ss;
				}
				jso = new JSONObject(html).getJSONObject("mainMap");
				json.put("FPZLMC", jso.getString("INVNAME"));
				json.put("FPDM", jso.getString("INVCODE"));
				json.put("FPHM", jso.getString("INVNO"));
				json.put("LGDWMC", jso.getString("SALENAME"));
				json.put("LGSJ", jso.getString("SALETIME"));
				json.put("FSSWJG", jso.getString("SALEDEPT"));
				json.put("result", "true");
				json.put("cwxx", "查验成功");
			} else {
				json.put("cwxx", "您好，系统未查找到相关信息，请认真核对输入的发票代码（或号码）！");
				return json;
			}
		} catch (Exception e) {
			logger.error("[ERROR] 解析异常：", e);
			json.put("cwxx", "您好，系统未查找到相关信息，请认真核对输入的发票代码（或号码）！");
		} finally {
			try {
				if (in != null)
					in.close();
				if (bufferedReader != null)
					bufferedReader.close();
			} catch (Exception e) {
				logger.error("[ERROR] 解析异常：", e);
				json.put("cwxx", "您输入的信息有误，请重新输入！");
			}
		}
		return json;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Map FPCY(Map parameter, TaxOfficeBean fppars){
		Map<String, Object> result = new HashMap<String, Object>();
		// 这里判断数据库里是否有记录存在，若存在，直接返回结果，不在查询
		result.put("FPDM", parameter.get("FPDM").toString());
		result.put("FPHM", parameter.get("FPHM").toString());
		// 默认查询失败
		result.put("BIZ_RETURNCODE", "99");
		// 取代理IP
		HSCEntity hscEntity = (HSCEntity) parameter.get("hscEntity");
		IpAddress ipAddress = hscEntity.getIpAddress();
		if (null == ipAddress) {
			// 没有得到代理IP
			result.put("CXJG", "发票查询异常，请重试！");
			return result;
		}
		// 获取当前的税务机关代码，，并初始化发票查验相关参数实体类
		if (null == fppars) {
			result.put("CXJG", "没有找到相应的查验参数，请联系管理员！");
			return result;
		}
		if (fppars.yzm_bz.equals("1")) {
			if (fppars.yzm_dz.length() <= 0) {
				result.put("CXJG", "验证码地址为空，请联系管理员！");
				return result;
			}
			Map<String, String> map = invQueryNext(parameter, ipAddress);
			if (map.get("result").toString() != "true") {
				result.put("CXJG", "发票信息输入错误！");
				return result;
			}
			String jessionID = map.get("JSESSIONID").toString();
			parameter.put("JSESSIONID", jessionID);
			parameter.put("queryType", map.get("confirmFlag").toString());
			Map<String, Object> yzmPic;
			try {
				// 下载验证码
				yzmPic = sendYzmRequest(fppars, jessionID);
				if (yzmPic.isEmpty()) {
					result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
					return result;
				}
				// 自动识别验证码
				byte[] filepath = (byte[]) yzmPic.get("filepath");
				String yzmMap = null;
				yzmMap = YzmsbInterface.YZMSB(filepath, fppars);
				if (StringUtils.isEmpty(yzmMap)) {
					result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
				}
				parameter.put("rand", yzmMap);
				// 开始查验
				getResult(fppars, parameter, result, ipAddress);
			} catch (Exception e) {
				result.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE113);
				result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
				result.put("cwxx", "查验失败：网络超时，请重试");
			}
		} else {
			result.put("CXJG", "验证码参数有误，请联系管理员！");
			return result;
		}

		return result;
	}
}
