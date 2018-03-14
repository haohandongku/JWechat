package com.dcits.fpcy.commons.service.core.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jsoup.Jsoup;
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


/**
 * 厦门地税23502 20160126修改，添加动态IP代理
 * 
 * @author wangkej
 * 
 */
public class XMDSfpcyImp implements InvoiceServerBase {
	private Log logger = LogFactory.getLog(XMDSfpcyImp.class);

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			parameter.put("JSESSIONID", hscEntity.getCookie());
			parameter.put("checkCode", hscEntity.getYzm());
			// 开始查验
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("厦门地税:", e);
			result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		} 
		return result;

	}

	/**
	 * 解析返回结果
	 * 
	 * @param paras
	 *        参数类
	 * @param in_parameter
	 * @param out_result
	 * @param ipAddress
	 * @throws JSONException
	 * @throws BadHanyuPinyinOutputFormatCombination 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result,
			IpAddress ipAddress) throws JSONException, BadHanyuPinyinOutputFormatCombination {
		Map<String, String> attributeMap = new HashMap<String, String>();
		//厦门地税验真是get方法提交
		logger.error("qwer"+in_parameter.get("checkCode").toString());
		attributeMap.put("fpdm", in_parameter.get("fpdm").toString());
		attributeMap.put("je", in_parameter.get("je").toString());
		attributeMap.put("fphm", in_parameter.get("fphm").toString());
		attributeMap.put("checkCode", in_parameter.get("checkCode").toString());
		Map requestHeader = new HashMap();
		requestHeader.put(HeaderType.COOKIE,in_parameter.get("JSESSIONID").toString());
		InputStream in = SendResultRequest.sendRequestIn(requestHeader,
				ipAddress, attributeMap, paras.cy_dz, paras.cy_qqfs);
		JSONObject json = parseInvoiceResult(in);
		// 新的返回方式
		List<ResultBean> list = new ArrayList<ResultBean>();
		logger.error("asdfgh"+json.toString());
		if ("false".equals(json.get("result"))) {
			list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
			out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		} else {
			String[] key={"机打代码","机打号码","付款方名称","收款方纳税人代码","收款方名称","开票日期"};
			for(int i=0;i<key.length;i++){
				if(!StringUtils.isBlank(json.getString(key[i]))){
					list.add(new ResultBean(ResultUtils.getPinYinHeadChar(key[i]).toUpperCase(), key[i], json.getString(key[i])));
				}
			}
			out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
		}
		out_result.put("list", list);
	}
	
	/**
	 * 解析返回值
	 * 
	 * @param in
	 * @return
	 * @throws JSONException
	 */
	public JSONObject parseInvoiceResult(InputStream in) throws JSONException {
		BufferedReader bufferedReader = null;
		JSONObject json = new JSONObject();
		json.put("result", "false");
		Document document = (Document) SendResultRequest.iSToJSONOrDocument(in,
				"GBK", "text");
		String html = document.toString();
		logger.error("zxcv"+html);
		try {
			if (in != null) {
				if (html.contains("未找到该发票")) {
					json.put("cwxx", "未找到该发票。请核对发票代码和号码（机打代码和号码）是否录入准确。");
					return json;
				}
				if (html.contains("发票金额和系统中不一致")) {
					json.put("cwxx",
							"发票金额和系统中不一致，请重新核对发票代码和号码（机打代码和号码）、金额是否录入准确。</br>如确认无误，该发票可能存在问题，请向税务机关反映或核实。");
					return json;
				}
				Document doc = Jsoup.parse(html);
				Elements eles = doc.getElementsByClass("invoiceColumn");
				if (eles.size() > 0) {
					for (Element e : eles) {
						String str = e.text().toString();
						if ("".equals(str)) {
							continue;
						}
						if (str.contains("机打代码")) {
							json.put("机打代码",
									str.substring(str.indexOf(":") + 1));
						}
						if (str.contains("机打号码")) {
							json.put("机打号码",
									str.substring(str.indexOf(":") + 1));
						}
						if (str.contains("付款方名称")) {
							json.put("付款方名称",
									str.substring(str.indexOf(":") + 1));
						}
						if (str.contains("收款方纳税人代码")) {
							json.put("收款方纳税人代码", str.substring(str.indexOf(" ")));
						}
						if (str.contains("收款方名称")) {
							json.put("收款方名称",
									str.substring(str.indexOf("）") + 1));
						}
						if (str.contains("开票日期")) {
							json.put("开票日期",
									str.substring(str.indexOf(":") + 1));
						} 
					}
					json.put("result", "true");
				}else{
					json.put("cwxx", "您输入的发票信息不存在！");
				}
			} else {
				json.put("cwxx", "您输入的发票信息不存在！");
			}
		} catch (Exception e) {
			System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
			json.put("cwxx", "您输入的发票信息不存在！");
		} finally {
			try {
				if (in != null)
					in.close();
				if (bufferedReader != null)
					bufferedReader.close();
			} catch (Exception e) {
				System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
				json.put("cwxx", "您输入的发票信息不存在！");
			}
		}
		return json;
	}
}
