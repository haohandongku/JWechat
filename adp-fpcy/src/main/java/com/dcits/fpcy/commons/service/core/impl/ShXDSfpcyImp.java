package com.dcits.fpcy.commons.service.core.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONException;
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


public class ShXDSfpcyImp implements InvoiceServerBase {

	private Log logger = LogFactory.getLog(ShXDSfpcyImp.class);

	/**
	 * 山西地税
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas) {
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			parameter.put("JSESSIONID", hscEntity.getCookie());
			parameter.put("rand", hscEntity.getYzm());
			// 开始查验
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("山西地税" + e);
			result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
		} finally {
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
	 * @throws JSONException
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void getResult(TaxOfficeBean paras, Map in_Parameter, Map out_Result,
			IpAddress ipAddress) throws JSONException {
		// 开始查验(山西地税查验前需要1.先判断验证码是否正确2.验证码正确后查验)
		//判断验证码识别是否正确
		boolean flag=validateYZM(in_Parameter,ipAddress);
		if(flag){
			//验证码识别正确
			getResult(in_Parameter,out_Result,ipAddress);
		}else{
			//验证码识别错误
			List<ResultBean> list = new ArrayList<ResultBean>();
			list.add(new ResultBean("cwxx", "", "查询错误，请重新查询"));
			out_Result.put(SysConfig.CYJGSTATE,SysConfig.CODE20011);
		}
	}
	
	/**
	 * 发送请求查验验证码识别结果是否正确
	 * @param identifyResult
	 * @param ipAddress
	 * @return
	 */
	@SuppressWarnings({ "unchecked"})
	public static boolean validateYZM(Map<String, String> identifyResult,
			IpAddress ipAddress) {
		// 请求头
		Map<String, String> requestHeader = new HashMap<String, String>();
		requestHeader.put(HeaderType.HOST, "218.26.133.58:85");
		requestHeader.put(HeaderType.REFERER,
				"http://218.26.133.58:85/Sehup/invseek/web.jsp");
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.ACCEPTENCODING, "gzip, deflate");
		requestHeader.put(HeaderType.ACCEPT,"*/*");
		requestHeader.put(HeaderType.ACCEPTLANGUAGE,"zh-CN,zh;q=0.8");
		requestHeader.put(HeaderType.CONNECTION,"keep-alive");
		requestHeader.put(HeaderType.COOKIE, identifyResult.get("JSESSIONID").toString());
		requestHeader.put(HeaderType.REFERER,"http://218.26.133.58:85/Sehup/invseek/web.jsp");
		// 请求所需数据
		@SuppressWarnings("rawtypes")
		Map checkcodeAttributeMap = new HashMap();
		checkcodeAttributeMap.put("checkcode", identifyResult.get("rand"));
		// 判断验证码是否正确地址
		String checkcodeRequestAddress = "http://218.26.133.58:85/Sehup/invseek/CheckValidate";
		// 先判断验证码是否正确
		InputStream chekcodeIn = SendResultRequest.sendRequestIn(requestHeader,
				null, checkcodeAttributeMap, checkcodeRequestAddress,
				"get");
		// 后台接收收请求后解析并返回查验结果
		Document checkcodeDocument = (Document) SendResultRequest
				.iSToJSONOrDocument(chekcodeIn, "UTF-8", "text");
		Elements elements = checkcodeDocument.select("body");
		String flag=elements.get(0).text();
		// 若验证错误返回false
		if (flag.equals("Y")) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 获取验证结果并打印
	 * @param identifyResult
	 * @param ipAddress
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void getResult(Map<String, String> identifyResult,Map out_Result,
			IpAddress ipAddress) {
		// 请求头
		Map<String, String> requestHeader = new HashMap<String, String>();
		requestHeader.put(HeaderType.HOST, "218.26.133.58:85");
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.ACCEPTENCODING, "gzip, deflate");
		requestHeader.put(HeaderType.ACCEPT,"*/*");
		requestHeader.put(HeaderType.ACCEPTLANGUAGE,"zh-CN,zh;q=0.8");
		requestHeader.put(HeaderType.CONNECTION,"keep-alive");
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.COOKIE, identifyResult.get("JSESSIONID"));
		requestHeader.put(HeaderType.REFERER,"http://218.26.133.58:85/Sehup/invseek/web.jsp");
		// 发票验真地址
		String requestAddress = "http://218.26.133.58:85/Sehup/invseek/CheckInvoiceServlet";
		// 请求所需数据
		Map attributeMap = new HashMap();
		attributeMap.put("fphm", identifyResult.get("FPDM")+identifyResult.get("FPHM"));
		attributeMap.put("checkcode", identifyResult.get("rand"));
		// 2.验证码正确后查验
		InputStream in = SendResultRequest.sendRequestIn(requestHeader,
				null, attributeMap, requestAddress, "get");
		// 后台接收收请求后解析并返回查验结果
		Document document = (Document) SendResultRequest.iSToJSONOrDocument(in,
				"UTF-8", "text");
		String parseResult = document.toString();
		System.out.println(parseResult);
		Elements elements = document.select("td");
		//返回查询结果
		List<ResultBean> list = new ArrayList<ResultBean>();
		list.add(new ResultBean("FPDM", "发票代码", elements.get(1).text()));
		list.add(new ResultBean("FPHM", "发票号码", elements.get(2).text()));
		list.add(new ResultBean("FPZL", "发票种类", elements.get(3).text()));
		list.add(new ResultBean("SWJG", "发售税务机关", elements.get(4).text()));
		list.add(new ResultBean("DW", "领购单位", elements.get(5).text()));
		list.add(new ResultBean("RQ", "领购日期", elements.get(6).text()));
		out_Result.put("list", list);
		out_Result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
	}

}
