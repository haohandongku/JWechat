package com.dcits.fpcy.commons.service.core.impl;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
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
import com.dcits.fpcy.commons.utils.ResultUtils;
import com.dcits.fpcy.commons.utils.SendResultRequest;
public class XMGSfpcyImp implements InvoiceServerBase {

	private Log logger = LogFactory.getLog(XMGSfpcyImp.class);

	/**
	 * 厦门国税定额（手工）票、出租车发票流向查询 说明： 1.厦门国税由于正常流程得不到结果，显示验证码错误，应该是由于验证码过期了
	 * 所以每次在得验证码的时候将JSESSIONID当做一个参数传到验证码的地址，以来让浏览器确认这是同一次请求
	 * 流程改成，先在查询页面得到Jsessionid,然后拼接到验证码地址页面中
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			parameter.put("JSESSIONID", hscEntity.getCookie1());
			parameter.put("checkcode_dep", hscEntity.getYzm());
			// 开始查验
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("厦门国税:", e);
			result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		} 
		return result;
	}

	// 获取验证码验真结果
	/**
	 * 
	 * @param paras
	 *            查验参数
	 * @param in_Parameter
	 *            请求数据 [ 验证码 jessionid]
	 * @param out_Result
	 *            返回结果
	 * @throws JSONException
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes"})
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result,
			IpAddress ipAddress) throws Exception {
		Map parameter1 = new HashMap<Object, Object>();
		Map<String, String> requestHeader = new HashMap<String, String>();
		// 这里改成网址传参数时需要的名称
		if(paras.getPoolid()==90){
			in_parameter.put("checkcode_skp", in_parameter.get("checkcode_dep").toString());
		}
		if(paras.swjg_dm.equals("3502")) {
			parameter1.put("khmc", "");
			parameter1.put("fpdm", in_parameter.get("fpdm0").toString());
			parameter1.put("fphm", in_parameter.get("fphm0").toString());
			parameter1.put("nsrbm", in_parameter.get("xfswdjh0").toString());
			parameter1.put("je", in_parameter.get("hjje0").toString());
			parameter1.put("kprq", in_parameter.get("kprq").toString());
			
			parameter1.put("checkcode_skp", in_parameter.get("checkcode_dep").toString());
		}
		requestHeader.put(HeaderType.COOKIE,in_parameter.get("JSESSIONID").toString());
		//requestHeader.put("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
		//requestHeader.put("localid", "1");
		InputStream in = null;
		if(paras.swjg_dm.equals("3502")) {
			in = SendResultRequest.sendRequestIn(requestHeader, ipAddress,
					parameter1, paras.cy_dz, paras.cy_qqfs);
		}else{
			in = SendResultRequest.sendRequestIn(requestHeader, ipAddress,
					in_parameter, paras.cy_dz, paras.cy_qqfs);
		}
		JSONObject json = parseInvoiceResult(in);
		List<ResultBean> list = new ArrayList<ResultBean>();
		if (json == null) {
			list.add(new ResultBean("CXJG", "查询结果", "发票查询异常，请重试！"));
		} else if ("false".equals(json.get("result").toString())) {
			list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
			out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		} else {
			json.remove("result");
			Iterator it=json.keys();
			while(it.hasNext()){
				String key=it.next().toString();
				String value=json.getString(key);
				if(!StringUtils.isBlank(value)){
					list.add(new ResultBean(ResultUtils.getPinYinHeadChar(key),key,value));
				}
			}
			out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
		}
		out_result.put("list", list);
	}

	/**
	 * 厦门国税查询结果解析
	 * 
	 * @throws JSONException
	 */
	public JSONObject parseInvoiceResult(InputStream in) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("result", "false");
		Document doc = (Document) SendResultRequest.iSToJSONOrDocument(in,
				"UTF-8", "text");
		String html = doc.toString();
		try {
			if (in != null) {
				if (html.contains("\"status\":\"0\"")) { // 随机码错误
					json.put("cwxx", "发票查询异常，请重试！");
					return json;
				} else {
					if (html.contains("未查询到相符发票信息")) {
						json.put("cwxx",
								"未查询到相符发票信息<br/>发票真伪以到主管税务机关鉴定为准！查询信息提示（查询结果仅供参考）！");
						return json;
					} else if (html.contains("history.go(-1)")) {
						json.put("cwxx", "发票查询失败，请稍候重试!");
						return json;
					} else if (html.contains("该发票已经售出")) {
						Elements ele = doc.select("table[cellspacing=2]")
								.select("tbody").select("tr");// .select("td");
						for (Element e : ele) {
							String s = e.text().toString();
							json.put(s.substring(0, s.indexOf(" ") - 1),
									s.substring(s.indexOf(" ") + 1, s.length()));
						}
						json.put("发票查询提示", "发票真伪以到主管税务机关鉴定为准！查询信息提示（查询结果仅供参考）！");
						json.put("result", "true");
					}else if(html.contains("有查询到相符发票信息")){
						Elements ele=doc.select(".td01");
						for(int i=0;i<ele.size();i=i+2){
							String key=ele.get(i).text();
							String value=ele.get(i+1).text();
							if(key.contains("*")){
								key=key.substring(1, key.lastIndexOf("："));
							}else{
								key=key.substring(0, key.lastIndexOf("："));
							}
							json.put(key, value);
						}
						json.put("发票查询提示", "发票真伪以到主管税务机关鉴定为准！查询信息提示（查询结果仅供参考）！");
						json.put("result", "true");
					} else {
						json.put("cwxx", "发票查询失败，请稍候重试!");
					}
				}
			} else {
				json.put("cwxx", "发票查询失败，请稍候重试!");
			}
		} catch (Exception e) {
			logger.error("解析错误", e);
			json.put("cwxx", "发票查询失败，请稍候重试!");
		} finally {
		}
		return json;
	}
	
	public static String cookieUtil(List<String> firstList,TaxOfficeBean fpcyParas){
		StringBuffer sb = new StringBuffer();
		for (int i = firstList.size() - 1; i >= 0; i--) {
			String jess = firstList.get(i).toString();
			jess = jess.substring(0, jess.indexOf(";"));
			sb.append(jess);
			sb.append("; ");
		}
		String firstJess = sb.toString();
		firstJess = firstJess.substring(0, firstJess.length() - 2);
		// 重新拼接一下JSESSIONID
		String checkUrl = fpcyParas.yzm_dz
				+ "&a="
				+ new SimpleDateFormat("EE MMM dd yyyy HH:mm:ss zZZ ",
						Locale.ENGLISH).format(new Date())
						.replace("CST", "GMT").replace(" ", "%20");
		return checkUrl + "&" + firstJess;
	}
	
	@SuppressWarnings({ "rawtypes" })
	public Map FPCY(Map parameter) throws Exception {
		return null;
	}
}
