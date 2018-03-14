package com.dcits.fpcy.commons.service.core.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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
import com.dcits.fpcy.commons.constant.BrowerType;
import com.dcits.fpcy.commons.constant.HeaderType;
import com.dcits.fpcy.commons.constant.SysConfig;
import com.dcits.fpcy.commons.service.core.InvoiceServerBase;
import com.dcits.fpcy.commons.utils.ResultUtils;
import com.dcits.fpcy.commons.utils.SendResultRequest;
public class NXGSfpcyImp implements InvoiceServerBase {

	private Log logger = LogFactory.getLog(NXGSfpcyImp.class);

	/**
	 * 第二版 宁夏国税
	 * 
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			parameter.put("JSESSIONID", hscEntity.getCookie());
			parameter.put("rand", hscEntity.getYzm());
			// if rand=null rand=-1 则失败
			// 开始查验
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("宁夏国税,获取cook时出现异常", e);
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

	@SuppressWarnings({ "rawtypes", "unchecked"})
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result,
			IpAddress ipAddress) throws Exception {
		Map attributeMap = new HashMap();
		String fpzl = null;
		Map<String, String> requestHeader = new HashMap<String, String>();
		if(StringUtils.isEmpty((String)in_parameter.get("fpzl"))) {
			fpzl = String.valueOf(in_parameter.get("index"));
		}else{
			fpzl = (String)in_parameter.get("fpzl");
		}
		attributeMap.put("fpdm", in_parameter.get("FPDM").toString());
		attributeMap.put("fphm", in_parameter.get("FPHM").toString());
		attributeMap.put("yzm", in_parameter.get("rand").toString()
				.toLowerCase());
		if (fpzl.equals("1")) {
			attributeMap.put("fpzl", "gmfp");
			attributeMap.put("cxm", "");
			attributeMap.put("fpje", "");
		} else if (fpzl.equals("2")) {
			attributeMap.put("fpzl", "defp");
			attributeMap.put("cxm", in_parameter.get("cxm").toString());
			attributeMap.put("fpje", "");
		} else if(fpzl.equals("zzs")) {
			attributeMap.put("fpzl", "zzsptfp");
			attributeMap.put("cxm", "");
			attributeMap.put("fpje", in_parameter.get("fpje").toString());
		} else {
			attributeMap.put("fpzl", "jdfp");
			attributeMap.put("cxm", "");
			attributeMap.put("fpje", in_parameter.get("fpje").toString());
		}
		String requestMethod = paras.cy_qqfs;
		String JSESSIONID = in_parameter.get("JSESSIONID").toString();
		String ary[] = JSESSIONID.split(";");
		// requestHeader.put("Cookie", ary[0]);
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.COOKIE, ary[0]);
		if ("post".equals(requestMethod)) {
			InputStream in = SendResultRequest.sendRequestPost(requestHeader,
					ipAddress, paras.cy_dz, attributeMap);
			JSONObject cxjg = parseInvoiceResult(in, fpzl);
			List<ResultBean> list = new ArrayList<ResultBean>();
			try {
				if (cxjg.getString("result") == "true") {
					if (fpzl.equals("2")) {
						String key[] = { "发票种类", "购票人名称", "纳税人识别号", "发票代码", "发票号码",
								"备注" };
						list = ResultUtils.getListInfoFromJson1(key, cxjg);
					} else if (fpzl.equals("1")) {
						String key[] = { "发票种类", "购票人名称", "纳税人识别号", "发票代码", "发票号码",
								"领用日期", "发票承印单位" };
						list = ResultUtils.getListInfoFromJson1(key, cxjg);
					} else if(fpzl.equals("zzs")) {
						if(!cxjg.toString().contains("领购日期")) {
							String key[] = { "发票种类", "购票人名称", "纳税人识别号", "发票代码", "发票号码",
									"开票日期","付款单位","开具金额","发票状态"};
							list = ResultUtils.getListInfoFromJson1(key, cxjg);
						}else{
							String key[] = { "发票种类", "购票人名称", "纳税人识别号", "发票代码", "发票号码",
									"领购日期","付款单位","开具金额","发票状态"};
							list = ResultUtils.getListInfoFromJson1(key, cxjg);
						}
					} else {
						list.add(new ResultBean("FPDM", "发票代码", cxjg
								.getString("FPDM")));
						list.add(new ResultBean("FPHM", "发票号码", cxjg
								.getString("FPHM")));
						list.add(new ResultBean("FPRQ", "发票日期", cxjg
								.getString("FPRQ")));
						list.add(new ResultBean("GHDW", "购货单位", cxjg.getString(
								"GHDW").replace(" ", "")));
						list.add(new ResultBean("FPJE", "发票金额", cxjg
								.getString("FPJE")));
						list.add(new ResultBean("NSRSBH", "纳税人识别号", cxjg.getString(
								"NSRSBH").replace(" ", "")));
						list.add(new ResultBean("XHDW", "销货单位", cxjg.getString(
								"XHDW").replace(" ", "")));
					}

					out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
				} else {
					list.add(new ResultBean("cwxx", "", cxjg.get("cwxx").toString()));
					out_result.put(SysConfig.INVOICEFALSESTATE,cxjg.getString(SysConfig.INVOICEFALSESTATE));
					out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
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

	public  JSONObject parseInvoiceResult(InputStream in, String fpzl)
			throws Exception {
		BufferedReader bufferedReader = null;
		JSONObject json = new JSONObject();
		json.put("result", "false");
		if (in == null) {
			json.put("cwxx", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！");
			json.put(SysConfig.INVOICEFALSESTATE, "102");
			return json;
		}
		Document document = (Document) SendResultRequest.iSToJSONOrDocument(in,
				"GBK", "");
		String html = document.toString();
		// FileUtils.writeStringToFile(new File(), data)
		try {
				if (html.contains("发票开具金额不一致")) {
					json.put("cwxx", "发票开具金额不一致，请核对发票内容再次查询或对此发票进行举报!");
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE201);
					return json;
				}
				if (html.contains("发票不存在")) {
					json.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE201);
					json.put("cwxx", "发票不存在!");
					return json;
				} else {
					Document doc = Jsoup.parse(html);
					Elements eles = doc.select("table").select("tbody")
							.select("tr").select("td").select("table")
							.select("tbody").select("tr").select("td")
							.select("div");
					if (fpzl.equals("1") || fpzl.equals("2") || fpzl.equals("zzs")) {
						Elements eles1 = doc.select("div[class=cx_table]")
								.select("table").select("tbody").select("tr")
								.select("td");
						String ary[] = eles1.text().toString().split(" ");
						for (int i = 0; i < ary.length; i++) {
							int index = ary[i].indexOf("：");
							json.put(ary[i].substring(0, index), ary[i + 1]);
							i++;
						}
					} else if (!(eles.isEmpty())) {

						Element e = eles.get(0);
						json.put("FPDM", e.getElementsByClass("fpdm").text()
								.toString());
						json.put("FPHM", e.getElementsByClass("fphm").text()
								.toString());
						String date = e.getElementsByClass("fp_item_date")
								.text().toString();
						date = date.substring(0, date.indexOf(" "));
						json.put("FPRQ", date);
						Elements eles2 = e.select("table").select("tbody")
								.select("tr");
						for (Element e2 : eles2) {
							if (e2.getElementsByClass("td1").text().toString()
									.contains("购货单位")) {
								json.put("GHDW", e2.select("td").get(2).text()
										.toString());
							} else if (e2.getElementsByClass("td1").text()
									.toString().contains("小写")) {
								json.put("FPJE", e2.select("td").get(3).text()
										.toString());
							} else if (e2.getElementsByClass("td1").text()
									.toString().contains("纳税人识别号")) {
								if (!(e2.select("td").get(1).text().toString()
										.contains(" ")))
									json.put("NSRSBH", e2.select("td").get(1)
											.text().toString());
							} else if (e2.getElementsByClass("td1").text()
									.toString().contains("销货单位")) {
								json.put("XHDW", e2.select("td").get(2).text()
										.toString());
							}
							json.put("result", "true");
						}
					} else {
						json.put("cwxx", "查询失败！");
						json.put(SysConfig.INVOICEFALSESTATE,
								SysConfig.INVOICEFALSESTATECODE111);
						return json;
					}
					json.put("result", "true");
				}
		} catch (Exception e) {
			System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
			json.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE111);
			json.put("cwxx", "您输入的发票信息不存在！");
			return json;
		} finally {
			try {
				if (in != null)
					in.close();
				if (bufferedReader != null)
					bufferedReader.close();
			} catch (Exception e) {
				System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
				json.put("cwxx", "系统异常");
				return json;
			}
		}
		return json;
	}
}
