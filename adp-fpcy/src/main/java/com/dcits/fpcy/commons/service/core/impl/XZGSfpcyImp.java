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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
 * 西藏国税:15400 2017-04-11
 * **/
public class XZGSfpcyImp implements InvoiceServerBase {
	private Log logger = LogFactory.getLog(XZGSfpcyImp.class);

	/**
	 * 
	 * @param parameter
	 *            请求参数
	 * @return 其它： 1、本界面提供普通发票流向查询及参与普通发票抽奖服务。
	 *         2、本界面提供的普通发票查询范围是西藏自治区国家税务局新版发票及暂时保留的旧版发票
	 *         （不包括：机动车销售统一发票，公路、内河货物运输统一发票）。
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			parameter.put("JSESSIONID", hscEntity.getCookie1());
			parameter.put("rand", hscEntity.getYzm());
			// 开始查验
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("西藏国（地）税发票查验:" + e);
			List list = new ArrayList<ResultBean>();
			list.clear();
			list.add(new ResultBean("cwxx", "", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！"));
			result.put("list", list);
			result.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE103);
			result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		}
		return result;

	}

	@SuppressWarnings({ "rawtypes" })
	public Map FPCY(Map parameter) throws Exception {
		return null;
	}

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
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void getResult(TaxOfficeBean paras, Map in_Parameter, Map out_Result, IpAddress ipAddress)
			throws JSONException {
		Map attributeMap = new HashMap();
		Map<String, String> requestHeader = new HashMap<String, String>();
		attributeMap.put("input", in_Parameter.get("rand").toString());
		attributeMap.put("fpdm", in_Parameter.get("FPDM").toString());
		attributeMap.put("fphm", in_Parameter.get("FPHM").toString());
		attributeMap.put("je", in_Parameter.get("je").toString());
		String JSESSIONID = in_Parameter.get("JSESSIONID").toString();
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.COOKIE, JSESSIONID);
		InputStream in = SendResultRequest.sendRequestPost(requestHeader, ipAddress, paras.cy_dz, attributeMap);
		JSONObject json = parseInvoiceResult(in);
		// 新的返回方式
		List<ResultBean> list = new ArrayList<ResultBean>();
		try {
			if (json == null) {
				out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			} else if ("false".equals(json.get("result"))) {
				list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
				out_Result.put(SysConfig.INVOICEFALSESTATE, json.getString(SysConfig.INVOICEFALSESTATE));
				out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			} else {
				list.add(new ResultBean("FPDM", "发票代码", json.get("FPDM").toString()));
				list.add(new ResultBean("FPHM", "发票号码", json.get("FPHM").toString()));
				list.add(new ResultBean("HJJE", "合计金额", json.get("HJJE").toString()));
				list.add(new ResultBean("SWDJH", "税务登记号", json.get("SWDJH").toString()));
				list.add(new ResultBean("SKDWMC", "收款单位名称", json.get("SKDWMC").toString()));
				list.add(new ResultBean("CS", "查询次数", json.get("CS").toString()));
				out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
			}
		} catch (Exception e) {
			logger.error(paras.swjg_mc + "解析返回参数异常", e);
			out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			out_Result.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE104);
			e.printStackTrace();
		}
		out_Result.put("list", list);

	}

	/**
	 * 西藏国地税结果解析
	 * 
	 * @throws JSONException
	 */
	public JSONObject parseInvoiceResult(InputStream in) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("result", "false");
		if (in == null) {
			json.put("cwxx", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！");
			json.put(SysConfig.INVOICEFALSESTATE, "102");
			return json;
		}
		try {
			Document document = (Document) SendResultRequest.iSToJSONOrDocument(in, "GBK", "text");
			String result = document.toString();
			if (result.contains("验证码错误")) {
				json.put("cwxx", "发票查询错误，请重试！");
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE101);
			} else if (result.contains("发票金额不符")) {
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE213);
				json.put("cwxx",
						"您查询的下列发票金额与收款单位向税务机关报送的发票金额不符。您可以： 一、向0891-12366进一步核实相关信息。 二、向0891-96555进行涉税举报。 三、退回收款单位要求重新开具。");
			} else if (result.contains("未查询到任何记录")) {
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE213);
				json.put("cwxx", " 您查询的下列发票信息，未查询到任何记录，您可以向税务机关进行举报。联系电话：0891-6837628");
			} else if (result.contains("发票信息相符") || result.contains("发票未缴销") || result.contains("发票数据信息比对相符")) {
				json.put("result", "true");
				String bjxf = document.select(".line3").select("td[colspan=9]").text();
				bjxf = bjxf.substring(bjxf.indexOf("：") + 1);
				Element tr = document.select("#data").select("tbody").select("tr").last();
				Elements td = tr.select("td");
				String[] key = { "FPDM", "FPHM", "HJJE", "a", "b", "SWDJH", "SKDWMC", "CS" };
				for (int i = 0; i < key.length; i++) {
					json.put(key[i], td.get(i).text());
				}
				json.put("BJXF", bjxf);// 数据比对不符： 您查询的下列发票与税务机关发票数据信息比对不符。您可以：
										// 一、向0891-12366进一步核实相关信息。
										// 二、向0891-12366进行涉税举报。 三、退回收款单位要求重新开具。
			} else if (result.contains("数据比对不符")) {
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE213);
				json.put("cwxx",
						"数据比对不符： 您查询的发票信息与税务机关发票数据信息比对不符。您可以： 一、向0891-12366进一步核实相关信息。 二、向0891-12366进行涉税举报。 三、退回收款单位要求重新开具。");
			} else {
				json.put("cwxx", "发票查询失败，请稍候重试！");
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE111);
			}
			return json;
		} catch (Exception e) {
			System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
			json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE111);
			json.put("cwxx", "发票查询异常，请稍候重试！");
			e.printStackTrace();
		}
		return json;
	}
}
