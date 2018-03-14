package com.dcits.fpcy.commons.service.core.impl;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.dcits.fpcy.commons.constant.BrowerType;
import com.dcits.fpcy.commons.constant.HeaderType;
import com.dcits.fpcy.commons.constant.SysConfig;
import com.dcits.fpcy.commons.factory.TaxOfficeFactory;
import com.dcits.fpcy.commons.service.core.InvoiceServerBase;
import com.dcits.fpcy.commons.utils.JavaBeanUtils;
import com.dcits.fpcy.commons.utils.ResultUtils;
import com.dcits.fpcy.commons.utils.SendResultRequest;

public class CQDSfpcyImp implements InvoiceServerBase {

	/**
	 * 重庆发票查验 包含机打，定额两类（货运无票）
	 * 
	 * @param parameter
	 * @param regionNum
	 * @return
	 */
	private Log logger = LogFactory.getLog(CQDSfpcyImp.class);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			if(parameter.get("fpzl").equals("2")){
				TaxOfficeFactory  taxOfficeFactory=new TaxOfficeFactory();
				Map param=taxOfficeFactory.queryOneTaxOffice(fpcyParas.getSwjg_dm(),"2");
				fpcyParas=(TaxOfficeBean) JavaBeanUtils.mapToObject(param, TaxOfficeBean.class);
			}
			parameter.put("sjm", hscEntity.getYzm());
			if(hscEntity.getCookie1() == null) {
				parameter.put("JSESSIONID", hscEntity.getCookie());	
			}else{
				parameter.put("JSESSIONID", hscEntity.getCookie1());	
			}
			// 开始查验
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());

		} catch (Exception e) {
			logger.error("重庆地税,获取cook时出现异常", e);
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

	/**
	 * 
	 * @param paras
	 *            查验参数
	 * @param in_Parameter
	 *            请求数据 [ 验证码 jessionid]
	 * @param out_Result
	 *            返回结果
	 * @param IpAddress
	 *            ip
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result,
			IpAddress ipAddress) throws Exception {
		Map attributeMap = new HashMap();
		Map requestHeader = new HashMap();
		String fpzl = in_parameter.get("fpzl").toString();
		// "1"机打发票
		if (fpzl.equals("1")) {
			attributeMap.put("sjm", in_parameter.get("sjm").toString());
			attributeMap.put("fpdm", in_parameter.get("FPDM").toString());
			attributeMap.put("fkfmc", URLEncoder.encode(in_parameter.get("fkfmc").toString(), "UTF-8"));
			attributeMap.put("fphm", in_parameter.get("FPHM").toString());
			attributeMap.put("kpje", in_parameter.get("kpje").toString());
		}
		// "2"定额发票
		if (fpzl.equals("2")) {
			attributeMap.put("sjm", in_parameter.get("sjm").toString());
			attributeMap.put("fpdm", in_parameter.get("FPDM").toString());
			attributeMap.put("fphm", in_parameter.get("FPHM").toString());
		}
		String JSESSIONID = in_parameter.get("JSESSIONID").toString();
		requestHeader.put(HeaderType.USERAGENT, BrowerType.google);
		requestHeader.put(HeaderType.COOKIE, JSESSIONID);
		in_parameter.remove("JSESSIONID");
		InputStream in = SendResultRequest.sendRequestIn(requestHeader,
				ipAddress, attributeMap,paras.cy_dz, paras.cy_qqfs);
		// 重庆地税机打结果解析
		List<ResultBean> list = new ArrayList<ResultBean>();
		if (fpzl.equals("1")) {
			JSONObject json = parseInvoiceResult(in);
			if (json == null) {
				out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE20011);
			} else if ("false".equals(json.get("result").toString())) {
				list.add(new ResultBean("cwxx","",json.get("cwxx").toString()));
				out_result.put(SysConfig.INVOICEFALSESTATE,json.getString(SysConfig.INVOICEFALSESTATE));
				out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE20011);
			} else {
				String key[] = {"发票名称","纳税人名称","发票发售机关","付款方名称","金额"};
				list = ResultUtils.getListInfoFromJson1(key, json);
				list.add(new ResultBean("FPDM","发票代码",in_parameter.get("FPDM").toString()));
				list.add(new ResultBean("FPHM","发票号码",in_parameter.get("FPHM").toString()));
				/*list.add(new ResultBean("FPMC", "发票名称", json.getString("发票名称")));
				list.add(new ResultBean("NSRMC", "纳税人名称", json.get("纳税人名称")
						.toString()));
				list.add(new ResultBean("FSJG", "发票发售机关", json.get("发票发售机关")
						.toString()));
				list.add(new ResultBean("FKFMC", "付款方名称", json.get("付款方名称")
						.toString()));
				list.add(new ResultBean("JE", "金额", json.get("金额").toString()));*/
				list.add(new ResultBean("CXJG", "查询结果", "查询成功"));
				out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE1000);
			}
		}
		// 重庆地税定额结果解析
		if (fpzl.equals("2")) {
			JSONObject json = parseInvoiceResult2(in);
			if (json == null) {
				out_result.put("CXJG", "发票查询异常，请重试！");
			} else
				try {
					if (json.get("result").toString().equals("false")) {
						list.add(new ResultBean("cwxx","",json.get("cwxx").toString()));
						out_result.put(SysConfig.INVOICEFALSESTATE,json.getString(SysConfig.INVOICEFALSESTATE));
						out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE20011);
					} else {
						list.add(new ResultBean("FPDM","发票代码",in_parameter.get("FPDM").toString()));
						list.add(new ResultBean("FPHM","发票号码",in_parameter.get("FPHM").toString()));
						list.add(new ResultBean("FPLX", "发票类型", json.get("发票类型").toString()));
						list.add(new ResultBean("BZ", "本票发由", json.getString("领用")));
						list.add(new ResultBean("LYDW", "领用单位", json.getString("本发票由")));
						list.add(new ResultBean("CXJG", "查询结果", "查询成功"));					
						out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE1000);
					}
				} catch (JSONException e) {
					logger.error("", e);
				}
		}
		out_result.put("list", list);
	}

	/**
	 * 重庆地税机打结果解析
	 * 
	 * @throws JSONException
	 */
	public JSONObject parseInvoiceResult(InputStream in) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("result", "false");
		Document document = (Document) SendResultRequest.iSToJSONOrDocument(in,
				"UTF-8", "text");
		String result = document.toString();
		try {
			if (in != null) {
				if (result.contains("随机码输入有误")) {
					// 随机码错误
					json.put(SysConfig.INVOICEFALSESTATE,SysConfig.INVOICEFALSESTATECODE205);
					json.put("cwxx", "发票查询异常，请重试！");
					return json;
				}
				if (result.contains("不符")) {
					json.put("cwxx", "此票可能为克隆发票或头大尾小发票，请到税务机关鉴别或拨打12366-2详询。");
					json.put(SysConfig.INVOICEFALSESTATE,SysConfig.INVOICEFALSESTATECODE211);
					return json;
				}
				if (result.contains("没有找到对应发票")) {
					json.put("cwxx", "没有找到对应发票！请联系税务机关进行查询。");
					json.put(SysConfig.INVOICEFALSESTATE,SysConfig.INVOICEFALSESTATECODE201);
					return json;
				} else if(result.contains("相符")){
					json.put("result", "true");
					Elements elements = document.select("br");
					for (Element element : elements) {
						String ele = element.nextSibling().toString();
						String[] news = ele.split("：");
						if (news.length > 1) {
							json.put(news[0], news[1]);
						}
					}
				}else{
					json.put("cwxx","发票查询失败，请重试！");
				}
			}
		} catch (Exception e) {
			try {
				logger.error("", e);
				json.put("cwxx", "您输入的发票信息不匹配，请检查是否输入正确！");
			} catch (JSONException e1) {
				logger.error("", e1);
			}
		}
		return json;

	}

	/**
	 * 重庆地税定额结果解析
	 * 
	 * @throws JSONException
	 */
	public JSONObject parseInvoiceResult2(InputStream in) throws JSONException {
		JSONObject json = new JSONObject();
		try {
			json.put("result", "false");
		} catch (JSONException e1) {
			logger.error("", e1);
		}
		Document document = (Document) SendResultRequest.iSToJSONOrDocument(in,
				"UTF-8", "text");
		String result = document.toString();
		try {
			if (in != null) {
				if (result.contains("\"status\":\"0\"")) {
					json.put("result", "false");
					// 随机码错误
					json.put(SysConfig.INVOICEFALSESTATE,SysConfig.INVOICEFALSESTATECODE205);
					json.put("cwxx", "发票查询异常，请重试！");
					return json;
				} else if (result.contains("无效发票")) {
					json.put(SysConfig.INVOICEFALSESTATE,SysConfig.INVOICEFALSESTATECODE211);
					json.put("cwxx", "该发票为无效发票，请及时向税务机关举报！电话举报请拨12366-2。");
					return json;
				}else if(result.contains("随机码输入有误")){
					json.put(SysConfig.INVOICEFALSESTATE,SysConfig.INVOICEFALSESTATECODE205);
					json.put("cwxx", "发票查询失败，请稍候重试！");
				} else {
					Elements elements = document.select("br");
					String jg = elements.select("br").get(0).nextSibling()
							.toString();
					String jg2 = elements.select("br").get(1).nextSibling()
							.toString();
					jg2 += elements.select("br").get(1).nextElementSibling()
							.text();
					String jg3 = elements.select("br").get(1)
							.nextElementSibling().nextSibling().toString();
					if ("&middot;".equals("&middot;")) {
						jg = jg.replace("&middot;", "·");
					}
					if ("&middot;".equals("&middot;")) {
						jg = jg.replace("&times;", "×");
					}
					jg3 = jg3.replace("&quot;}", "");
					String arr[] = jg.split(": ");
					String arr2[] = jg2.split(" ");
					String arr3[] = jg3.split("，");
					String[] lx = { "发票类型", "本发票由", "领用" };
					json.put(lx[0], arr[1]);
					json.put(lx[1], arr2[1]);
					json.put(lx[2], arr3[1]);
					json.put("result", "true");
				}
			} else {
				json.put("cwxx", "您输入的发票信息不匹配，请检查是否输入正确！");
				return json;
			}
		} catch (Exception e) {
			json.put("cwxx", "您输入的发票信息不匹配，请检查是否输入正确！");
			logger.error("", e);
			return json;
		}
		return json;
	}

}
