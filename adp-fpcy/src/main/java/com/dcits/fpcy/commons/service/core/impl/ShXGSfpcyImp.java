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

public class ShXGSfpcyImp implements InvoiceServerBase {
	private Log log = LogFactory.getLog(ShXGSfpcyImp.class);

	/**
	 * 山西国税发票查验
	 * 
	 * 2017-04-11
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){

		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			parameter.put("rand", hscEntity.getYzm());
			if (hscEntity.getCookie1() == null) {
				parameter.put("JSESSIONID", hscEntity.getCookie());
			} else {
				parameter.put("JSESSIONID", hscEntity.getCookie1());
			}
			// 开始查验
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			log.error("山西国税发票查验" + e);
			List<ResultBean> list = new ArrayList<ResultBean>();
			list.clear();
			list.add(new ResultBean("cwxx", "", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！"));
			result.put("list", list);
			result.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE103);
			result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		}
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result, IpAddress ipAddress)
			throws JSONException {
		Map attributeMap = new HashMap();
		Map<String, String> requestHeader = new HashMap<String, String>();
		attributeMap.put("fplx", "1");
		attributeMap.put("fpdm", in_parameter.get("fpdm").toString());
		attributeMap.put("fphm", in_parameter.get("fphm").toString());
		attributeMap.put("xfsbh", in_parameter.get("xfsbh").toString());
		attributeMap.put("kpje", in_parameter.get("kpje").toString());
		attributeMap.put("code", in_parameter.get("rand").toString());
		String requestMethod = paras.cy_qqfs;
		String JSESSIONID = in_parameter.get("JSESSIONID").toString();
		attributeMap.put("JSESSIONID", JSESSIONID);
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.COOKIE, JSESSIONID + "; path=/");
		requestHeader.put(HeaderType.REFERER, "http://202.99.207.241:7005/fpcx/");
		List<ResultBean> list = new ArrayList<ResultBean>();
		if ("post".equals(requestMethod)) {
			InputStream in = SendResultRequest.sendRequestPost(requestHeader, ipAddress, paras.cy_dz, attributeMap);
			JSONObject cxjg = parseInvoiceResult(in);
			if (cxjg == null) {
				list.add(new ResultBean("CXJG", "查询结果", "发票查询异常，请重试！"));
			} else if ("true".equals(cxjg.getString("result"))) {
				String fpdm = cxjg.getString("发票代码").replace(" ", "");
				list.add(new ResultBean("FPDM", "发票代码", fpdm));
				list.add(new ResultBean("FPHM", "发票号码", cxjg.getString("发票号码")));
				if (fpdm.length() == 12) {
					list.add(new ResultBean("NSRSBH", "销售方纳税人识别号", cxjg.getString("销售方纳税人识别号")));
					list.add(new ResultBean("NSRMC", "销售方纳税人名称", cxjg.getString("销售方纳税人名称")));
					list.add(new ResultBean("LGRQ", "领购日期", cxjg.getString("领购日期")));
				} else {
					list.add(new ResultBean("NSRSBH", "销售方纳税人识别号", cxjg.getString("销售方识别号")));
					list.add(new ResultBean("NSRMC", "销售方纳税人名称", cxjg.getString("销售方名称")));
					list.add(new ResultBean("LGRQ", "领购日期", cxjg.getString("开票日期")));
					list.add(new ResultBean("FPJE", "发票金额", cxjg.getString("发票金额")));
					list.add(new ResultBean("FPSE", "发票税额", cxjg.getString("发票税额")));
					list.add(new ResultBean("CXJG", "查询结果", cxjg.getString("cwxx")));
				}
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
			} else {
				out_result.put(SysConfig.INVOICEFALSESTATE, cxjg.getString(SysConfig.INVOICEFALSESTATE));
				list.add(new ResultBean("cwxx", "", cxjg.get("cwxx").toString()));
			}
			out_result.put("list", list);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public JSONObject parseInvoiceResult(InputStream in) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("result", "false");
		Document doc = (Document) SendResultRequest.iSToJSONOrDocument(in, "UTF-8", "text");
		String html = doc.toString();
		try {
			if (in != null) {
				if (html.contains("查无此发票开具信息")) {
					json.put("result", "false");
					json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE201);
					json.put("cwxx", "提示信息： 查无此发票开具信息 , 请仔细核对后，再进行查询。");
				} else if (html.contains("此发票信息真实")) {
					json.put("result", "true");
					json.put("cwxx", "此发票信息真实");
					Elements eles = doc.getElementsByClass("jgk");
					Element element = eles.get(0);
					Elements elements = element.select("table").select("tbody").select("tr").select("td");
					Map map = new HashMap();
					int count = 0;
					for (Element e : elements) {
						if (!(e.text().toString().isEmpty())) {
							map.put(count, e.text().toString());
							count++;
						}
					}
					for (int i = 0; i < count; i += 2) {
						String value = map.get(i + 1).toString();
						json.put(map.get(i).toString(), value.substring(0, value.length() - 1));
					}
				} else if (html.contains("此发票为假票或非法开具发票")) {
					json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE211);
					json.put("cwxx", "此发票为假票或非法开具发票，请向当地主管税务机关进一步核实发票真伪！");
				} else {
					json.put("cwxx", "发票查询失败，请稍后重试！");
				}
			}
		} catch (Exception e) {
			json.put("cwxx", "发票查询异常，请稍后重试！");
			json.put("result", "false");
			e.printStackTrace();
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception e2) {
				json.put("cwxx", "您输入的信息有误，请重新输入！");
				json.put("result", "false");
			}
		}
		return json;
	}
}
