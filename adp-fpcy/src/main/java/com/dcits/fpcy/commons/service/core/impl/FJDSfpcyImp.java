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
import com.dcits.fpcy.commons.constant.SysConfig;
import com.dcits.fpcy.commons.service.core.InvoiceServerBase;
import com.dcits.fpcy.commons.utils.SendResultRequest;

public class FJDSfpcyImp implements InvoiceServerBase {

	private Log logger = LogFactory.getLog(FJDSfpcyImp.class);

	/**
	 * 福建发票查验
	 * 
	 */
	@SuppressWarnings({ "rawtypes" })
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			// 开始查验
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("福建地税,获取cook时出现异常", e);
			List<ResultBean> list = new ArrayList<ResultBean>();
			list.clear();
			list.add(new ResultBean("cwxx", "", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！"));
			result.put("list", list);
			result.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE103);
			result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		}
		return result;
	}

	// 进行验真方法
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void getResult(TaxOfficeBean paras, Map in_Parameter,
			Map<String, Object> out_result, IpAddress ipAddress)
			throws Exception {
		InputStream in = SendResultRequest.sendRequestIn(null, ipAddress,
				in_Parameter, paras.cy_dz, paras.cy_qqfs);
		JSONObject json = parseInvoiceResult(in);
		List<ResultBean> list = new ArrayList<ResultBean>();
		try {
			if (json == null) {
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			} else if (json.get("result").toString() == "false") {
				list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
				out_result.put(SysConfig.INVOICEFALSESTATE,json.getString(SysConfig.INVOICEFALSESTATE));
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			} else {
				JSONObject cxjg = new JSONObject(json.get("cxjg").toString());
				list.add(new ResultBean("FPDM", "发票代码", cxjg.getString("发票代码")));
				list.add(new ResultBean("FPHM", "发票号码", cxjg.getString("发票号码")));
				list.add(new ResultBean("LGSJ", "领购时间", cxjg.getString("领购时间")));
				list.add(new ResultBean("LGDW", "领购单位", cxjg.getString("领购单位")));
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
			}
		} catch (JSONException e) {
			logger.error(paras.swjg_mc + "解析返回参数异常", e);
			out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			out_result.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE104);
			e.printStackTrace();
		}
		out_result.put("list", list);
	}

	public JSONObject parseInvoiceResult(InputStream in) throws Exception {
		JSONObject jso = null;
		String html = null;
		JSONObject json = new JSONObject();
		json.put("result", "false");
		if (in == null) {
			json.put("cwxx", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！");
			json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE102);
			return jso;
		}
		try {
				jso = new JSONObject();
				Document doc = (Document) SendResultRequest.iSToJSONOrDocument(
						in, "GBK", "text");
				html = doc.toString();
				String[] titles = { "fpdm", "fphm", "fpmc", "jeb", "lgdw",
						"time", "kjje", "jmje", "tsh" }, values = null;
				Elements eles = doc.select("table.SBB").select("tbody")
						.select("tr");
				for (Element e : eles) {
					if (e.attr("align").compareTo("center") == 0) {
						if (!e.hasAttr("bgcolor"))
							values = e.text().split(" ");
					}
				}
				for (int i = 0; i < titles.length; ++i)
					jso.put(titles[i],
							(String) values[i].subSequence(0,
									values[i].length() - 1));

				if (html.contains("无效发票")) {
					json.put("cwxx", "您输入的发票信息不存在！");
					// 无效发票分为两种，一种是假发票（后台无数据），
					// 另一种是有数据的无效发票，系统需要处理并返回结果
					// values3.4.5.6-->金额版，领购单位，时间，金额
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE211);
					if (values[4] == null || values[4] == ""
							|| values[4].length() <= 1)
						return json;
				}
				json.put("result", "true");
				json.put("cxjg", "{\"发票代码\":\"" + jso.getString("fpdm")
						+ "\",\"发票号码\":\"" + jso.getString("fphm")
						+ "\",\"发票名称\":\"" + jso.getString("fpmc")
						+ "\",\"金额版\":\"" + jso.getString("jeb")
						+ "\",\"领购单位\":\"" + jso.getString("lgdw")
						+ "\",\"领购时间\":\"" + jso.getString("time")
						+ "\",\"开具金额\":\"" + jso.getString("kjje")
						+ "\",\"奖面金额\":\"" + jso.getString("jmje")
						+ "\",\"提示\":\"" + jso.getString("tsh") + "\"}");
		} catch (Exception e) {
			System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
			try {
				json.put(SysConfig.INVOICEFALSESTATE,
						SysConfig.INVOICEFALSESTATECODE111);
				json.put("cwxx", "此类发票查询发生异常，请重试");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			return json;
		}
		return json;
	}
}
