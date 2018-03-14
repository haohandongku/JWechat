package com.dcits.fpcy.commons.service.core.impl;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.codehaus.jettison.json.JSONException;
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
import com.dcits.fpcy.commons.utils.SendResultRequest;

/**
 * 广东国税
 * 
 * 
 */
public class GDGSfpcyImp implements InvoiceServerBase {
	private Log logger = LogFactory.getLog(GDGSfpcyImp.class);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			parameter.put("rand", hscEntity.getYzm());
			parameter.put("JSESSIONID", hscEntity.getCookie1());
			// 开始查验
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("广东国税,获取cook时出现异常", e);
			result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		}
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result, IpAddress ipAddress)
			throws UnsupportedEncodingException {
		Map requestHeader = new HashMap();
		String kpje = "";
		String kprq = "";
		String fpdm = in_parameter.get("FPDM").toString();
		String fphm = in_parameter.get("FPHM").toString();
		String xhfswdjh = in_parameter.get("xhfswdjh").toString();
		if (fpdm.length() == 12) {
			kpje = in_parameter.get("kpje").toString();
			kprq = in_parameter.get("kprq").toString();
		}
		String xhfmc = toUtf8String(in_parameter.get("xhfmc").toString());
		String yzm1 = in_parameter.get("rand").toString();
		String JSESSIONID = in_parameter.get("JSESSIONID").toString();
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.COOKIE, JSESSIONID);
		requestHeader.put("Accept", "text/plain;charset=UTF-8");
		String yzm = new String(java.net.URLEncoder.encode(yzm1, "utf-8").getBytes());
		String bizXml = "%3CtaxML%3E%3Cfpdm%3E" + fpdm + "%3C%2Ffpdm%3E%3Cfphm%3E" + fphm
				+ "%3C%2Ffphm%3E%3Cxhfswdjh%3E" + xhfswdjh + "%3C%2Fxhfswdjh%3E%3Cxhfmc%3E" + xhfmc
				+ "%3C%2Fxhfmc%3E%3Ckpje%3E" + kpje + "%3C%2Fkpje%3E%3Ckprq%3E" + kprq
				+ "%3C%2Fkprq%3E%3Ckpse%3E%3C%2Fkpse%3E%3Cghfswdjh%3E%3C%2Fghfswdjh%3E%3Cyzm%3E" + yzm
				+ "%3C%2Fyzm%3E%3C%2FtaxML%3E";
		Map attributeMap1 = new HashMap();
		Map attributeMap2 = new HashMap();
		Map attributeMap3 = new HashMap();
		Map attributeMap4 = new HashMap();
		Map attributeMap5 = new HashMap();
		Map attributeMap6 = new HashMap();
		Map attributeMap7 = new HashMap();
		attributeMap1.put("requestXml", "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?><taxML><nsrsbh>"
				+ xhfswdjh + "</nsrsbh></taxML>");
		attributeMap1.put("sid", "ETax.WS.GetSwjgxx.Fpcy");
		String requestMethod = paras.cy_qqfs;
		if ("post".equals(requestMethod)) {
			InputStream in1 = SendResultRequest.sendRequestPost(requestHeader, ipAddress, paras.cy_dz, attributeMap1);
			JSONObject json1 = parseInvoiceResult(in1);
			List<ResultBean> list = new ArrayList<ResultBean>();
			if (json1 == null) {
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			} else
				try {
					if (json1.get("result").toString().equals("false")) {
						list.add(new ResultBean("cwxx", "", json1.get("cwxx").toString()));
						out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
					} else {
						attributeMap2.put("sid", json1.get("sid").toString());
						attributeMap2.put("tid", json1.get("tid").toString());
						attributeMap2.put("action", "queryXml");
						InputStream in2 = SendResultRequest.sendRequestPost(requestHeader, ipAddress,
								"http://app.gd-n-tax.gov.cn/etax/bizfront/rejoinQuery.do", attributeMap2);
						JSONObject json2 = parseInvoiceResult(in2);// 税务机关地址、名称
						if (json2 == null) {
							out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
						} else if (json2.get("result").toString().equals("false")) {
							list.add(new ResultBean("cwxx", "", json1.get("cwxx").toString()));
							out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
						} else {
							attributeMap3.put("requestXml",
									"<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?><taxML><fpdm>" + fpdm
											+ "</fpdm></taxML>");
							attributeMap3.put("sid", "ETax.WS.IsZzszyfp.Fpcy");
							InputStream in3 = SendResultRequest.sendRequestPost(requestHeader, ipAddress, paras.cy_dz,
									attributeMap3);
							JSONObject json3 = parseInvoiceResult(in3);
							if (json3 == null) {
								out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
							} else if (json3.get("result").toString().equals("false")) {
								list.add(new ResultBean("cwxx", "", json1.get("cwxx").toString()));
								out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
							} else {
								attributeMap4.put("sid", json3.get("sid").toString());
								attributeMap4.put("tid", json3.get("tid").toString());
								attributeMap4.put("action", "queryXml");
								InputStream in4 = SendResultRequest.sendRequestPost(requestHeader, ipAddress,
										"http://app.gd-n-tax.gov.cn/etax/bizfront/rejoinQuery.do", attributeMap4);
								JSONObject json4 = parseInvoiceResult(in4);// flag
								if (json4 == null) {
									out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
								} else if (json4.get("result").toString().equals("false")) {
									list.add(new ResultBean("cwxx", "", json1.get("cwxx").toString()));
									out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
								} else {
									attributeMap5.put("action", "cx");
									attributeMap5.put("sid", "ETax.WS.FpcySubmit.Fpcy");
									attributeMap5.put("bizXml", bizXml);
									InputStream in5 = SendResultRequest.sendRequestPost(requestHeader, ipAddress,
											"http://app.gd-n-tax.gov.cn/etax/gdgs/mh/query_y.do", attributeMap5);
									JSONObject json5 = parseInvoiceResult(in5);
									if (json5 == null) {
										out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
									} else if (json5.get("result").toString().equals("false")) {
										list.add(new ResultBean("cwxx", "", json1.get("cwxx").toString()));
										out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
									} else {
										attributeMap6.put("sid", json5.get("sid").toString());
										attributeMap6.put("tid", json5.get("tid").toString());
										attributeMap6.put("action", "jgcx");
										InputStream in6 = SendResultRequest.sendRequestPost(requestHeader, ipAddress,
												"http://app.gd-n-tax.gov.cn/etax/gdgs/mh/query_y.do", attributeMap6);
										JSONObject json6 = parseInvoiceResult(in6);
										if (json6 == null) {
											out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
										} else if (json6.get("result").toString().equals("false")) {
											list.add(new ResultBean("cwxx", "", json1.get("cwxx").toString()));
											out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
										} else {
											String retMsg = toUtf8String(json6.get("retMsg").toString());
											attributeMap7.put("retMsg", retMsg);
											attributeMap7.put("nsrmcBz", json6.get("nsrmcBz").toString());
											attributeMap7.put("check_code", yzm);
											attributeMap7.put("flag", json4.get("flag").toString());
											attributeMap7.put("xhfmc", xhfmc);
											if (fpdm.length() == 12) {
												attributeMap7.put("FP_LB", "PT");
											} else {
												attributeMap7.put("FP_LB", "ZY");
											}
											attributeMap7.put("kpje", kpje);
											attributeMap7.put("xhfswdjh", xhfswdjh);
											attributeMap7.put("kprq", kprq);
											attributeMap7.put("fpdm", fpdm);
											attributeMap7.put("fphm", fphm);

											InputStream in7 = SendResultRequest
													.sendRequestPost(
															requestHeader,
															ipAddress,
															"http://app.gd-n-tax.gov.cn/etax/gdgs/jsp/common/query/invoice_checking/invoice_checking_result_new.jsp",
															attributeMap7);
											JSONObject json = parseInvoiceResult(in7);

											if (json == null) {
												out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
											} else
												try {
													if (json.get("result").toString().equals("false")) {
														list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
														out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
													} else {
														list.add(new ResultBean("FPDM", "发票代码", fpdm));
														list.add(new ResultBean("FPHM", "发票号码", fphm));
														// json 转map
														HashMap<String, String> jsone = new HashMap<String, String>();
														Iterator it = json.keys();
														// 遍历jsonObject数据，添加到Map对象
														while (it.hasNext()) {
															String key = String.valueOf(it.next());
															String value = (String) json.get(key);
															jsone.put(key, value);
														}
														if ((jsone).containsKey("mc")) {
															list.add(new ResultBean("MC", "持有该发票的纳税人名称", json.get("mc")
																	.toString()));
														}
														list.add(new ResultBean("swjgmc", "税务机关名称", json2.get("swjgmc")
																.toString()));
														list.add(new ResultBean("swjgdz", "税务机关地址", json2.get("swjgdz")
																.toString()));
														list.add(new ResultBean("CXJG", "查询结果", json.getString("jg")));
														out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
													}
												} catch (JSONException e) {
													e.printStackTrace();
												}
										}
									}
								}
							}
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			out_result.put("list", list);
		}

	}

	public JSONObject parseInvoiceResult(InputStream in) {
		JSONObject json = new JSONObject();
		try {
			json.put("result", "false");
		} catch (JSONException e2) {
			e2.printStackTrace();
		}
		Document doc = (Document) SendResultRequest.iSToJSONOrDocument(in, "UTF-8", "text");
		String result = doc.toString();
		try {
			if (in != null) {
				if (result.contains("sid")) {
					String sid = doc.select("sid").get(0).text();
					String tid = doc.select("tid").get(0).text();
					json.put("sid", sid);
					json.put("tid", tid);
					json.put("result", "true");
				} else if (result.contains("swjgmc")) {
					String swjgmc = doc.select("swjgmc").get(0).text();
					String swjgdz = doc.select("swjgdz").get(0).text();
					json.put("swjgmc", swjgmc);
					json.put("swjgdz", swjgdz);
					json.put("result", "true");
				} else if (result.contains("flag")) {
					String flag = doc.select("flag").get(0).text();
					json.put("flag", flag);
					json.put("result", "true");
				} else if (result.contains("retmsg")) {
					String nsrmcBz = doc.select("nsrmcBz").get(0).text();
					String retMsg = doc.select("retMsg").get(0).text();
					json.put("nsrmcBz", nsrmcBz);
					json.put("retMsg", retMsg);
					json.put("result", "true");
				} else if (result.contains("errorbox")) {
					json.put("result", "false");
					json.put("cwxx", "发票查询异常，请稍后重试！");
				} else if (result.contains("开票信息一致")) {
					json.put("result", "true");
					Elements els = doc.select("span");
					String[] mcs = els.get(4).text().split("：");
					if (mcs.length == 2) {
						json.put("mc", mcs[1]);// 持有该发票的纳税人名称：华润万家生活超市（珠海）有限公司
					}
					json.put("jg", els.get(5).text());// 您所输入的发票信息与税务机关采集的开票信息一致。
				} else if (result.contains("该号码发票尚未发售给纳税人使用")) {
					json.put("result", "false");
					json.put("cwxx", "该号码发票尚未发售给纳税人使用。<br/>提示:请检查发票号码、发票代码是否输入正确。若确定信息输入无误，则此票疑为假票!");
				} else if (result.contains("税务登记号有误")) {
					json.put("result", "false");
					json.put("cwxx", "税务登记号有误。<br/>提示:请检查销货方（收款方）税务登记号是否输入正确。若确定信息输入无误，则此票疑为假票!");
				} else if (result.contains("开票金额与税务机关采集的不一致")) {
					json.put("result", "false");
					json.put("cwxx", "该号码发票是发售给您所输入的销货方（收款方）使用，但开票金额与税务机关采集的不一致。<br/>请检查开票金额是否输入正确。若确定信息输入无误，则此票疑为假票!");
				} else if (result.contains("销货方名称有误")) {
					json.put("result", "false");
					json.put("cwxx", "销货方名称有误。<br/>请检查开销货方名称是否输入正确。若确定信息输入无误，则此票疑为假票!");
				} else if (result.contains("开票日期与税务机关采集的不一致")) {
					json.put("result", "false");
					json.put("cwxx", "该号码发票是发售给您所输入的销货方（收款方）使用，但开票日期与税务机关采集的不一致。<br/>请检查开票日期是否输入正确。若确定信息输入无误，则此票疑为假票!");
				} else if (result.contains("税务机关未采集开票信息")) {
					json.put("result", "false");
					json.put("cwxx", "该号码发票是发售给您所输入的销货方（收款方）使用，但税务机关未采集开票信息。<br/>请检查信息是否输入正确。若确定信息输入无误，此票疑为假票!");
				} else {
					json.put("cwxx", "发票查询失败，请稍候重试!");
				}
				return json;
			} else {
				json.put("cwxx", "发票查询失败，请稍后重试！");
				json.put("result", "false");
			}

		} catch (Exception e) {
			try {
				json.put("cwxx", "发票查询失败，请稍后重试！");
				json.put("result", "false");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception e) {
				try {
					json.put("cwxx", "您输入的发票信息不匹配，请检查是否输入正确！");
					json.put("result", "false");
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
			}
		}
		return json;
	}

	/**
	 * 汉字转URL编码
	 * 
	 * @param 字符串str
	 * @return
	 */
	public static String toUtf8String(String str) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c >= 0 && c <= 255) {
				sb.append(c);
			} else {
				byte[] b;
				try {
					b = String.valueOf(c).getBytes("utf-8");
				} catch (Exception ex) {
					System.out.println(ex);
					b = new byte[0];
				}
				for (int j = 0; j < b.length; j++) {
					int k = b[j];
					if (k < 0)
						k += 256;
					sb.append("%" + Integer.toHexString(k).toUpperCase());
				}
			}
		}
		return sb.toString();

	}
}
