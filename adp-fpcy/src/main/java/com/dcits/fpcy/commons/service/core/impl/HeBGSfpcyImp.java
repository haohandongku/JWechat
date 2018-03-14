package com.dcits.fpcy.commons.service.core.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.apache.commons.lang.StringUtils;
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
 * 河北国税 11300
 * 
 * 2017-04-11
 */
public class HeBGSfpcyImp implements InvoiceServerBase {
	private Log logger = LogFactory.getLog(HeBGSfpcyImp.class);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			String rand = hscEntity.getYzm();
			parameter.put("rand", rand);
			parameter.put("JSESSIONID", hscEntity.getCookie());
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("河北国税,获取cook时出现异常", e);
			List<ResultBean> list = new ArrayList<ResultBean>();
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result, IpAddress ipAddress)
			throws JSONException {
		Map requestHeader = new HashMap();
		Map attributeMap = new HashMap();
		String fpdm = in_parameter.get("FPDM").toString();
		attributeMap.put("yzm", in_parameter.get("rand").toString());
		attributeMap.put("fpdm", fpdm);
		attributeMap.put("fphm", in_parameter.get("FPHM").toString());
		String fplb = in_parameter.get("fplb").toString();
		if (fpdm.length() == 12) {
			if (fplb.equals("1")) {// 通用机打
				attributeMap.put("sbm", in_parameter.get("sbm1").toString());
				attributeMap.put("fpje", in_parameter.get("fpje1").toString());
				attributeMap.put("kpfsh", in_parameter.get("kpfsh1").toString());
			} else if (fplb.equals("2")) {// 手工
				attributeMap.put("sbm", in_parameter.get("sbm2").toString());
				attributeMap.put("kpfsh", in_parameter.get("kpfsh2").toString());
			} else if (fplb.equals("3")) {// 增值税普通发票
				attributeMap.put("fpje", in_parameter.get("fpje3").toString());
				attributeMap.put("kpfsh", in_parameter.get("kpfsh3").toString());
			} else if (fplb.equals("4")) {// 客运发票
				attributeMap.put("sbm", in_parameter.get("sbm4").toString());
			} else if (fplb.equals("5")) {// 二手车和机动车发票
				attributeMap.put("kpfsh", in_parameter.get("kpfsh5").toString());
			}
		}
		attributeMap.put("taskId", in_parameter.get("taskId").toString());
		attributeMap.put("fplb", in_parameter.get("fplb").toString());
		attributeMap.put("s_fplb", in_parameter.get("fplb").toString());
		String JSESSIONID = in_parameter.get("JSESSIONID").toString();
		int i = JSESSIONID.indexOf(';');
		JSESSIONID = JSESSIONID.substring(11, i);
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.COOKIE, "JSESSIONID=" + JSESSIONID);
		List<ResultBean> list = new ArrayList<ResultBean>();
		InputStream in = SendResultRequest.sendRequestPost(requestHeader, ipAddress, paras.cy_dz, attributeMap);
		JSONObject cxjg = parseInvoiceResult(in, fplb);
		try {
			if ("false".equals(cxjg.get("result"))) {
				if (cxjg.get("cwxx").toString().contains("请改日再查")) {
					list.add(new ResultBean("cwxx", "", cxjg.get("cwxx").toString()));
					out_result.put(SysConfig.INVOICEFALSESTATE, cxjg.getString(SysConfig.INVOICEFALSESTATE));
					out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
				} else {
					list.add(new ResultBean("cwxx", "", cxjg.get("cwxx").toString()));
					out_result.put(SysConfig.INVOICEFALSESTATE, cxjg.getString(SysConfig.INVOICEFALSESTATE));
					out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
				}
			} else {
				if (fplb.equals("1")) {
					String key[] = { "发票代码", "发票号码", "开票日期", "开票金额", "开票方名称", "开票方税号", "发票状态", "收票方名称", "收票方识别号",
							"货物名称", "单位", "数量", "单价", "金额" };
					for (String str : key) {
						if (!StringUtils.isBlank(cxjg.getString(str))) {
							list.add(new ResultBean(getPinYinHeadChar(str).toUpperCase(), str, cxjg.getString(str)));
						}
					}
				} else if (fplb.equals("2")) {
					attributeMap.put("sbm", in_parameter.get("sbm2").toString());
					attributeMap.put("kpfsh", in_parameter.get("kpfsh2").toString());
				} else if (fplb.equals("3")) {
					list.add(new ResultBean("", "查验结果", cxjg.getString("hy")));
				} else if (fplb.equals("4")) {
					list.add(new ResultBean("", "查验结果", cxjg.getString("hy")));
				} else if (fplb.equals("5")) {
					attributeMap.put("kpfsh", in_parameter.get("kpfsh5").toString());
				}

				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
			}
		} catch (Exception e) {
			out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			logger.error("河北国税解析JSONObject异常：" + e);
			logger.error(paras.swjg_mc + "解析返回参数异常", e);
			out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			out_result.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE104);
			e.printStackTrace();
		}
		out_result.put("list", list);
	}

	/**
	 * 河北国税发票解析
	 * 
	 * @throws JSONException
	 */
	public JSONObject parseInvoiceResult(InputStream in, String fplb) throws JSONException {
		JSONObject json = new JSONObject();
		try {
			json.put("result", "false");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		if (in == null) {
			json.put("cwxx", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！");
			json.put(SysConfig.INVOICEFALSESTATE, "102");
			return json;
		}
		Document doc = (Document) SendResultRequest.iSToJSONOrDocument(in, "GBK", "");
		String result = doc.toString();
		try {
			if (result.contains("验证码不正确")) {
				json.put("cwxx", "发票查询异常，请重试！");
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE101);
			} else if (result.contains("请改日再查")) {
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE214);// 10次
				json.put("cwxx", "此张发票已达每日查询上限（每日10次），请改日再查。");
			} else if (result.contains("系统繁忙,请稍后再试")) {
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE213);
				json.put("cwxx", "系统繁忙,请稍后再试!");
			} else if (result.contains("查验成功")) {
				if (fplb.equals("1")) {
					Elements ele = doc.select("table[id=submenu11]").select("tr");
					Elements eltd = ele.select("td");
					String key[] = { "发票代码", "发票号码", "开票日期", "开票金额", "开票方名称", "开票方税号", "发票状态", "收票方名称", "收票方识别号",
							"货物名称", "单位", "数量", "单价", "金额" };
					for (int i = 0; i < key.length; i++) {
						json.put(key[i], eltd.get(i).text());
					}
				} else if (fplb.equals("4")) {
					Elements ele = doc.select("span[class=hint]");
					String eles = ele.text();
					json.put("hy", eles);
				}
				json.put("result", "true");
			} else if (result.contains("该发票为正常发票")) {
				Elements elements = doc.select("#submenu11").select("tbody").select("tr").select("td");
				String[] keys = { "fpdm", "fphm", "kprq", "kpje", "kpfmc", "kpfsh", "fpzt", "spfmc", "spfsbh", "hwmc",
						"dw", "sl", "dj", "je" };
				for (int i = 0; i < elements.size(); i++) {
					json.put(keys[i], elements.get(i).text());
				}
				json.put("result", "true");
			} else if (result.contains("查验失败")) {
				String ele = doc.select(".hint").get(0).text();
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE213);
				json.put("cwxx", ele);

			} else {
				json.put("cwxx", "发票查询异常,请稍候重试!");
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE111);
			}

		} catch (Exception e) {
			try {
				json.put("cwxx", "发票查询异常,请稍候重试!");
				json.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICEFALSESTATECODE111);
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
					json.put("cwxx", "发票查询异常,请稍候重试!");
					json.put("result", "false");
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
			}
		}
		return json;
	}

	/**
	 * 获取汉字首字母
	 * 
	 * @param str
	 * @return
	 * @throws BadHanyuPinyinOutputFormatCombination
	 */
	public static String getPinYinHeadChar(String str) throws BadHanyuPinyinOutputFormatCombination {
		String convert = "";
		for (int j = 0; j < str.length(); j++) {
			char word = str.charAt(j);
			String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word, new HanyuPinyinOutputFormat());
			if (pinyinArray != null) {
				convert += pinyinArray[0].charAt(0);
			} else {
				convert += word;
			}
		}
		return convert;
	}

}