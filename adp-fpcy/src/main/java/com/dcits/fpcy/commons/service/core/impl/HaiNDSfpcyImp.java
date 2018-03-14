package com.dcits.fpcy.commons.service.core.impl;

import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jsoup.nodes.Document;

import com.dcits.fpcy.commons.bean.HSCEntity;
import com.dcits.fpcy.commons.bean.IpAddress;
import com.dcits.fpcy.commons.bean.ResultBean;
import com.dcits.fpcy.commons.bean.TaxOfficeBean;
import com.dcits.fpcy.commons.constant.BrowerType;
import com.dcits.fpcy.commons.constant.HeaderType;
import com.dcits.fpcy.commons.constant.SysConfig;
import com.dcits.fpcy.commons.service.core.InvoiceServerBase;
import com.dcits.fpcy.commons.utils.SendResultRequest;

public class HaiNDSfpcyImp implements InvoiceServerBase {
	/**
	 * 海南地税发票查验
	 * 
	 * @param parameter
	 * @param regionNum
	 * @return
	 */
	private Log logger = LogFactory.getLog(HaiNDSfpcyImp.class);
	

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			IpAddress ipAddress = hscEntity.getIpAddress();
			getSec(parameter, ipAddress);
			getResult(fpcyParas, parameter, result, ipAddress);
		} catch (Exception e) {
			logger.error("海南地税,获取cook时出现异常", e);
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

	@SuppressWarnings("rawtypes")
	public Map FPCY(Map parameter) throws Exception {
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result,
			IpAddress ipAddress) throws JSONException {
		Map attributeMap = new HashMap();
		Map requestHeader = new HashMap();
		String JSESSIONID = in_parameter.get("cookie").toString();
		String requestMethod = paras.cy_qqfs;
		String msg = in_parameter.get("Msg").toString().replace("%", "%25");
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.COOKIE, JSESSIONID);
		attributeMap.put("data", msg);
		if ("post".equals(requestMethod)) {
			InputStream in = SendResultRequest.sendRequestPost(requestHeader,
					ipAddress, paras.cy_dz, attributeMap);
			JSONObject json = parseInvoiceResult(in);
			List<ResultBean> list = new ArrayList<ResultBean>();
			try {
				if (json == null) {
					out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE20011);
				} else if ("false".equals(json.get("result"))) {
					out_result.put(SysConfig.INVOICEFALSESTATE,json.getString(SysConfig.INVOICEFALSESTATE));
					list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
					out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE20011);
				} else {
					String[] key = new String[20];
					 Iterator it = json.keys();  
				        int i = 0;
				        while(it.hasNext()){  
				            key[i]=it.next().toString();
				            i++;
				        }
					for(int j = 0 ; j < i ; j++){
						if(!json.isNull(key[j])){
							if(!key[j].equals("result"))
							list.add(new ResultBean("RES", key[j], json.get(key[j]).toString()));
						}
					}
//					list.add(new ResultBean("RES", "比对结果", json.get("msg").toString()));
					list.add(new ResultBean("CXJG", "查询结果", "查验成功"));
					out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE1000);
				}
			} catch (Exception e) {
				logger.error(paras.swjg_mc + "解析返回参数异常", e);
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
				out_result.put(SysConfig.INVOICEFALSESTATE,
						SysConfig.INVOICEFALSESTATECODE104);
				e.printStackTrace();
			}
			out_result.put("list", list);
		}
	}

	/**
	 * 海南地税结果解析
	 * 
	 * @throws JSONException
	 */
	public JSONObject parseInvoiceResult(InputStream in) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("result", "false");
		if (in == null) {
			json.put("result", "false");
			json.put("cwxx", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！");
			json.put(SysConfig.INVOICEFALSESTATE, "102");
			return json;
		}
		try {
				Document doc = (Document) SendResultRequest.iSToJSONOrDocument(
						in, "UTF-8", "");
				String result = doc.toString();
				if (result.contains("seckey")) {
					String body = doc.select("body").text();
					JSONArray json1 = new JSONArray(body);
					String seckey = new JSONObject(json1.getString(0))
							.getString("seckey");
					String seciv = new JSONObject(json1.getString(0))
							.getString("seciv");
					json.put("result", "true");//1
					json.put("seckey", seckey);
					json.put("seciv", seciv);
				} else if (result
						.contains("<input type=\"hidden\" id=\"jmm\" name=\"jmm\"")) {
					String jmm = doc.select("#jmm").val();
					json.put("result", "true");//2
					json.put("jmm", jmm);
				} else if (result.contains("2001")) {
					json.put("cwxx", "发票查询失败,请稍后重试！");
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE213);
					json.put("result", "false");
				} else if (result.contains("2000")) {
					String body = doc.select("body").text();
					JSONArray json1 = new JSONArray(body);
					String msg = URLDecoder
							.decode(new JSONObject(json1.getString(0))
									.getString("msg"), "UTF-8");
					msg=msg.substring(msg.indexOf("0")+2, msg.indexOf("</"));
					String[] msg1 = msg.split(",");
					if(msg.contains("通用机打发票3（税务机关代开）")){
						json.put("查验结果", msg);
					}else{
						for(String msgs : msg1){
							if(msgs.contains(":")||msgs.contains("：")){
								int index = msgs.indexOf(":");
								json.put(msgs.substring(0,index), msgs.substring(index+1));
							}
						}
					}
					json.put("result", "true");//3
				} else {
					json.put("cwxx", "发票查询失败，请稍候重试！");
				}
		} catch (Exception e) {
			json.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE111);
			json.put("cwxx", "发票查询异常，请稍后重试！");
			json.put("result", "false");
			e.printStackTrace();
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception e2) {
				json.put("cwxx", "您输入的发票信息不匹配，请检查是否输入正确！");
				json.put("result", "false");
				e2.printStackTrace();
			}
		}
		return json;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map getSec(Map parameter, IpAddress ipAddress) throws JSONException {
		Map map = new HashMap();
		Map requestHeader = new HashMap();
		List cookieList = SendResultRequest.sendRequest(null, ipAddress,
				"http://202.100.197.38:7009/etax2006/WssbServlet");
		String sto = cookieList.get(0).toString();
		String ts = cookieList.get(1).toString();
		sto = sto.substring(0, sto.indexOf(";"));
		ts = ts.substring(0, ts.indexOf(";"));
		String cookie = sto + ";" + ts;
		map.put("cookie", cookie);
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.COOKIE, cookie);
		InputStream in = SendResultRequest.sendRequestIn(requestHeader,
				ipAddress, map,
				"http://202.100.197.38:7009/etax2006/WssbServlet", "GET");
		JSONObject json = parseInvoiceResult(in);
		InputStream in1 = SendResultRequest.sendRequestIn(requestHeader,
				ipAddress, map,
				"http://202.100.197.38:7009/etax2006/jaxm/fpzwcx.jsp", "GET");
		JSONObject json1 = parseInvoiceResult(in1);
		String fphm = parameter.get("FPHM").toString();
		String fpdm = parameter.get("FPDM").toString();
		String fpmm = parameter.get("fpmm").toString();
		String jmm = json1.get("jmm").toString();
		String sKey = json.get("seckey").toString();
		String vi = json.get("seciv").toString();
		String sSrc = "<?xml version=\"1.0\" encoding=\"GBK\"?>\n<ROOT>\n"
				+ "  <PATH>etax.web.sBean.service.fpyb.FpybSaveAction</PATH>\n"
				+ "   <ACTION>fpzwcx</ACTION>\n" + "    <DATA><FPHM>" + fphm
				+ "</FPHM><FPDM>" + fpdm + "</FPDM><FPMM>" + fpmm + "</FPMM><JMM>"
				+ jmm + "</JMM></DATA>\n" + "</ROOT>";
		
		try {
			String msg = Encrypt(sSrc, sKey, vi);
			parameter.put("Msg", msg);
		} catch (Exception e) {
			logger.error("加密异常：" + e);
		}
		parameter.put("cookie", cookie);
		return parameter;
	}
	//将原JavaScript的AES加密，改用java的AES加密实现
	public String Encrypt(String sSrc, String sKey, String vi) throws Exception {
		byte[] raw = sKey.getBytes();
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// "算法/模式/补码方式"
		IvParameterSpec iv = new IvParameterSpec(vi.getBytes());// 使用CBC模式，需要一个向量iv，可增加加密算法的强度
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
		byte[] encrypted = cipher.doFinal(sSrc.getBytes());
		String str = new String(Base64.encodeBase64(encrypted));
		String utf = URLEncoder.encode(str, "utf-8");
		return utf;
	}
}
