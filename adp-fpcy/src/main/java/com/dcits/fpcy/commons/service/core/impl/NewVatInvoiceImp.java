package com.dcits.fpcy.commons.service.core.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.dcits.app.data.DataObject;
import com.dcits.fpcy.commons.bean.CacheEntity;
import com.dcits.fpcy.commons.bean.ResultBean;
import com.dcits.fpcy.commons.bean.TaxOfficeBean;
import com.dcits.fpcy.commons.constant.BrowerType;
import com.dcits.fpcy.commons.constant.HeaderType;
import com.dcits.fpcy.commons.constant.SysConfig;
import com.dcits.fpcy.commons.factory.TaxOfficeFactory;
import com.dcits.fpcy.commons.service.CommonService;
import com.dcits.fpcy.commons.service.core.InvoiceServerBase;
import com.dcits.fpcy.commons.utils.AnalysisHttpClientUtils;
import com.dcits.fpcy.commons.utils.AnalysisJS;
import com.dcits.fpcy.commons.utils.DataConvertUtil;
import com.dcits.fpcy.commons.utils.HttpClient;
import com.dcits.fpcy.commons.utils.PropertiesUtils;
import com.dcits.ieds.proxy.IEDSProxy;

public class NewVatInvoiceImp implements InvoiceServerBase {
	private  static HttpClient  httpClient=new HttpClient();
	//private  static HttpClient2  httpClient2=new HttpClient2();
	private  static String iv = "6a095635fcd841b7b7169998a1f68b0c";
	private  static String salt = "13ce594c8589a22d31fe7878b38217b0";
	private final static String jsRuleURL="https://inv-veri.chinatax.gov.cn/js/";
	private  static AnalysisHttpClientUtils  analysisHttpClient=new AnalysisHttpClientUtils();
	/**
	 * 增值税（新票）发票校验
	 */
	private static Log logger = LogFactory.getLog(NewVatInvoiceImp.class);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas) {
		Map result = new HashMap();
		CacheEntity cache = (CacheEntity) parameter.get("cacheEntity");
		if(cache==null){
			result.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE105);
			result.put("cwxx", "该税局服务器非正常状态，请稍后再试！");
			result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			List list = new ArrayList();
			list.add(new ResultBean("cwxx", "", result.get("cwxx").toString()));
			result.put("list", list);
			return result;
		}
		String yzm = cache.getYzm();
		if (StringUtils.isEmpty(yzm)) {
			result.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE105);
			result.put("cwxx", "该税局服务器非正常状态，请稍后再试！");
			result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			List list = new ArrayList();
			list.add(new ResultBean("cwxx", "", result.get("cwxx").toString()));
			result.put("list", list);
			return result;
		}
		parameter.put("pic_id", cache.getImageId());
		try {
			String index = cache.getKey3();
			parameter.put("yzm", yzm.toLowerCase());
			parameter.put("yzmSj", cache.getKey2());
			parameter.put("index", index);
			// 开始查验
			getResult(fpcyParas, parameter, result);
			//同一个验证码，查验失败后，可以被重复使用
            if(SysConfig.INVOICEFALSESTATECODE113.equals(result.get("invoicefalseState"))){
            	getResult(fpcyParas, parameter, result);
            	if(SysConfig.INVOICEFALSESTATECODE113.equals(result.get("invoicefalseState"))){
            		getResult(fpcyParas, parameter, result);
            	}
            }
		} catch (Exception e) {
			e.printStackTrace();
			result.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE113);
			result.put("cwxx", "查验失败：网络超时，请重试！");
			result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			List list = new ArrayList();
			list.add(new ResultBean("cwxx", "", result.get("cwxx").toString()));
			result.put("list", list);
		}
		return result;
	}
	/**
	 * 获取参数
	 * 
	 * @param parmaterMap
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static List<NameValuePair> getParamList(Map parmaterMap) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		if (null != parmaterMap) {
			Iterator<String> iterator = parmaterMap.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				String value = (String) parmaterMap.get(key);
				params.add(new BasicNameValuePair(key, value));
			}
		}
		return params;
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void getResult(TaxOfficeBean fpcyParas, Map parameter, Map result) {
		Map invoMap=getInvoiceType(parameter);
		String isCoreTransformation=fpcyParas.isCoreTransformation;
		// 获取盐和密钥
		String ivString="";
		String saltString="";
		try{
			ivString =AnalysisJS.getJsSaltAndIv();
			saltString =AnalysisJS.getJsSaltAndIv();
		}catch(Exception e){}
		if (StringUtils.isEmpty(ivString)) {
			ivString = iv;
		}
		if (StringUtils.isEmpty(saltString)) {
			saltString = salt;
		}
		String fpje = String.valueOf(invoMap.get("fpje"));
		String fplx = String.valueOf(invoMap.get("fplx"));
		String fpdm = String.valueOf(parameter.get("fpdm"));
		String fphm = String.valueOf(parameter.get("fphm"));
		String swjg_mc= fpcyParas.swjg_mc;
		Map attributeMap = new HashMap();
		long nowTime=System.currentTimeMillis();
		attributeMap.put("callback",
				"jQuery110206227320923474144_" +String.valueOf(nowTime));
		attributeMap.put("fpdm", fpdm);
		attributeMap.put("fphm", fphm);
		attributeMap.put("kprq",
				String.valueOf(parameter.get("kprq")).replace("-", ""));
		attributeMap.put("fpje", fpje);
		attributeMap.put("fplx", fplx);
		attributeMap.put("yzm", String.valueOf(parameter.get("yzm")));
		attributeMap.put("yzmSj", String.valueOf(parameter.get("yzmSj")));
		if(!"0".equals(isCoreTransformation)){
			attributeMap.put("iv", ivString);
			attributeMap.put("salt", saltString);
		}
		attributeMap.put("_",String.valueOf(nowTime));
		attributeMap.put("index", String.valueOf(parameter.get("index")));
		String publickey=AnalysisJS.analysisJsKey(attributeMap,"cy",isCoreTransformation);
		attributeMap.put("publickey", publickey);
		Map map1=new HashMap();
		map1.put("yzmSj", String.valueOf(parameter.get("yzmSj")));
		List<NameValuePair> nvps = getParamList(map1);
		// 转换为键值对
		String str ="";
		try {
			str = EntityUtils.toString(new UrlEncodedFormEntity(nvps,
					"utf-8"));
		} catch (Exception e1) {
			e1.printStackTrace();
			
		}
		String urlParam="";
		if(!"0".equals(isCoreTransformation)){
			 urlParam="callback="+attributeMap.get("callback")+"&fpdm="+fpdm
						+"&fphm="+attributeMap.get("fphm")+"&kprq="+attributeMap.get("kprq")
						+"&fpje="+attributeMap.get("fpje")+"&fplx="+attributeMap.get("fplx")
						+"&yzm="+attributeMap.get("yzm")+"&"+str
						+"&index="+attributeMap.get("index")
						+"&iv="+attributeMap.get("iv")+"&salt="+attributeMap.get("salt")
						+"&area="+fpdm.substring(0, 4)+"&publickey="+publickey
						+"&_="+String.valueOf(System.currentTimeMillis());
		}else{
			 urlParam="callback="+attributeMap.get("callback")+"&fpdm="+fpdm
						+"&fphm="+attributeMap.get("fphm")+"&kprq="+attributeMap.get("kprq")
						+"&fpje="+attributeMap.get("fpje")+"&fplx="+attributeMap.get("fplx")
						+"&yzm="+attributeMap.get("yzm")+"&"+str
						+"&index="+attributeMap.get("index")
						+"&area="+fpdm.substring(0, 4)+"&publickey="+publickey
						+"&_="+String.valueOf(System.currentTimeMillis());
		}
		logger.debug("发票查验请求路径："+urlParam);
		Map attrMap = new HashMap<String, String>();
		String cy_dz = fpcyParas.cy_dz;
		String host = cy_dz.substring(cy_dz.indexOf("/") + 2,
				cy_dz.indexOf("/WebQuery"));
		attrMap.put("Accept", "*/*");
		attrMap.put("Accept-Encoding", "gzip, deflate, sdch, br");
		attrMap.put("Referer", "https://inv-veri.chinatax.gov.cn/");
		attrMap.put("Host", host);
		attrMap.put(HeaderType.USERAGENT, BrowerType.google);
		Map logMap = new HashMap();
		logMap.put("invoiceCode",fpdm);
		logMap.put("invoiceNum", fphm);
		logMap.put("invoiceName", swjg_mc);
		logMap.put("requestType", "cy");
		logMap.put("requestId", parameter.get("requestId").toString());
		Map map = httpClient.callGetService(fpcyParas.cy_dz,urlParam, logMap, attributeMap,
				attrMap,2);
		/*Map map =new HashMap();
		map.put("resultData", "jQuery1102010651152172636147_1517402995213({\"key1\":\"001\",\"key2\":\"1≡20171229≡中国石油天然气股份有限公司陕西咸阳销售分公司≡91610400221705805Q≡·陕西送变电工程公司西安电力安装工程处≡9161013322080211XD≡661712399708≡29.06≡200≡≡62654378974169510306≡170.94≡冯建宏≡N\",\"key3\":\"92号 车用汽油(Ⅴ)█29.72000000█6.73000000█200█6.73█170.94█17█29.06\",\"key4\":\" \",\"key5\":\"1\"})");
		map.put("resultCode", "SUCCESS");*/
		List list = new ArrayList();
		String isRight="Y";
		if (map != null) {
			String resultData = (String) map.get("resultData");
			String resultCode = (String) map.get("resultCode");
			if (StringUtils.isNotEmpty(resultData)) {
				resultData=getCacheSjxxJson(resultData);
				Map invoiceInfoMap = getSjxxMap(resultData);
				if (invoiceInfoMap != null) {
					// 返回结果数据
					String key1 = (String) invoiceInfoMap.get("key1");
					String ifSuccess="Y";
					logger.error("---"+swjg_mc+"_"+fpdm+"_"+fphm+"--返回结果的值：" + key1);
					if ("001".equals(key1)) {
						//获取key3(判断税局是不是返回假数据)
						String key3 = (String) invoiceInfoMap.get("key3");
						if(key3.contains("▽█ █ █ █ █ █ █")){
							ifSuccess="N";
							result.put(SysConfig.INVOICEFALSESTATE,
									SysConfig.INVOICEFALSESTATECODE241);
							result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
							result.put("cwxx", "查验失败：网络超时，请重试");
							list.add(new ResultBean("cwxx", "", result.get("cwxx").toString()));
							result.put("list", list);
						}else{
							parameter.put("swjgmc", swjg_mc);
							try{
								Map returnMap=new HashMap();
								if("0".equals(isCoreTransformation)){
									returnMap= AnalysisJS.analysisNewJsAfter(resultData,parameter ,fplx);
								}else{
									String newRoleJsName=(String) invoiceInfoMap.get("key11");
									String role=getInvoiceRule(newRoleJsName,fplx,parameter);
									String stringsNeed = getStringsNeed(parameter);
									returnMap =getAnalysisMap(parameter,resultData, role, fplx);
									if(returnMap.isEmpty()){
									returnMap = AnalysisJS.analysisJsAfter(stringsNeed,
												resultData, role, fplx);
									}
								}
								//两种方式，如果nodejs解析不行，就用本地的
								list = getOldInvoiceMap(returnMap);
								result.put(SysConfig.INVOICEFALSESTATE, SysConfig.INVOICETRUESTATE000);
								result.put(SysConfig.SYSTEMFALSESTATE,
										SysConfig.INVOICETRUESTATE000);
								result.put("list", list);
								result.put("invoiceMap", returnMap.get("invoiceData"));
							}catch(Exception e){
								e.printStackTrace();
								logger.error(swjg_mc+"税局异常：税局规则发生变化---" + e.getMessage());
								result.put(SysConfig.INVOICEFALSESTATE,
										SysConfig.INVOICEFALSESTATECODE113);
								result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
								result.put("cwxx", "查验失败：网络超时，请重试");
								list.add(new ResultBean("cwxx", "", result.get("cwxx").toString()));
								result.put("list", list);
							}
						}
					} else if ("008".equals(key1)) {
						result.put(SysConfig.INVOICEFALSESTATE,
								SysConfig.INVOICEFALSESTATECODE101);
						isRight="N";
						result.put("cwxx", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！");
						result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
					} else if ("009".equals(key1)) {
						result.put(SysConfig.INVOICEFALSESTATE,
								SysConfig.INVOICEFALSESTATECODE201);
						result.put("cwxx", "查无此票！");
					} else if ("002".equals(key1)) {
						result.put(SysConfig.INVOICEFALSESTATE,
								SysConfig.INVOICEFALSESTATECODE202);
						result.put("cwxx",
								"查验失败：失败原因，超过该张发票的单日查验次数(5次），请于24小时之后再进行查验!");
					} else if ("003".equals(key1)) {
						result.put(SysConfig.INVOICEFALSESTATE,
								SysConfig.INVOICEFALSESTATECODE128);
						result.put("cwxx", "查验失败：发票查验请求太频繁，请稍后再试！");
					} else if ("004".equals(key1)) {
						result.put(SysConfig.INVOICEFALSESTATE,
								SysConfig.INVOICEFALSESTATECODE116);
						result.put("cwxx", "查验失败：超过服务器最大请求数，请稍后访问！");
					} else if ("005".equals(key1)) {
						result.put(SysConfig.INVOICEFALSESTATE,
								SysConfig.INVOICEFALSESTATECODE115);
						result.put("cwxx", "查验失败：请求不合法!");
					} else if ("020".equals(key1)) {
						result.put(SysConfig.INVOICEFALSESTATE,
								SysConfig.INVOICEFALSESTATECODE114);
						result.put("cwxx", "查验失败：由于查验行为异常，涉嫌违规，当前无法使用查验服务！");
					} else if ("006".equals(key1)) {
						result.put(SysConfig.INVOICEFALSESTATE,
								SysConfig.INVOICEFALSESTATECODE220);
						result.put("cwxx", "查验失败：不一致!");
					} else if ("007".equals(key1)) {
						result.put(SysConfig.INVOICEFALSESTATE,
								SysConfig.INVOICEFALSESTATECODE118);
						result.put("cwxx", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！");
					} else if ("010".equals(key1) || "010_".equals(key1)) {
						result.put(SysConfig.INVOICEFALSESTATE,
								SysConfig.INVOICEFALSESTATECODE113);
						result.put("cwxx", "查验失败：网络超时，请重试");
					} else if (!"002".equals(key1) && !"001".equals(key1)) {
						isRight="N";
						result.put(SysConfig.INVOICEFALSESTATE,
								SysConfig.INVOICEFALSESTATECODE113);
						result.put("cwxx", "查验失败：网络超时，请重试！");
					}
					if ("001".equals(key1)&&"Y".equals(ifSuccess)) {
						result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
					} else {
						result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
						result.put(SysConfig.SYSTEMFALSESTATE,
								result.get(SysConfig.INVOICEFALSESTATE));
						list.add(new ResultBean("cwxx", "", result.get("cwxx")
								.toString()));
						result.put("list", list);
					}
				} else {
					result.put(SysConfig.INVOICEFALSESTATE,
							SysConfig.INVOICEFALSESTATECODE113);
					result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
					result.put("cwxx", "查验失败：网络超时，请重试");
					list.add(new ResultBean("cwxx", "", result.get("cwxx").toString()));
					result.put("list", list);
				}
			} else {
				if (resultCode.contains("Read timed out")
						|| resultCode.contains("connect timed out")
						|| resultCode.contains("Connection reset")||
						resultCode.contains("Service Unavailable")
						||resultCode.contains("400 Bad Request")
                        ||resultCode.contains("server certificate change is restrictedduring renegotiation")
						) {
					resultCode = SysConfig.INVOICEFALSESTATECODE113;
				} else {
					resultCode = SysConfig.INVOICEFALSESTATECODE240;
				}
				result.put(SysConfig.INVOICEFALSESTATE,
						resultCode);
				result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
				result.put("cwxx", "税局查验服务暂时不可用，请稍后再试");
				list.add(new ResultBean("cwxx", "", result.get("cwxx")
						.toString()));
				result.put("list", list);
			}
		} else {
			result.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE113);
			result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			result.put("cwxx", "查验失败：网络超时，请重试");
			list.add(new ResultBean("cwxx", "", result.get("cwxx").toString()));
			result.put("list", list);
		}
		//更新ocr 请求日志
		Map ocrMap=new HashMap();
		ocrMap.put("isRight", isRight);
		ocrMap.put("picId", parameter.get("pic_id"));
		try {
			IEDSProxy.doService("OCRReport", new DataObject(ocrMap), null);
			}catch(Exception e){
				logger.error("保存ocr异常：" + e.getMessage());
			}		
	}

	/**
	 * 判断发票种类
	 * 
	 * @param parameter
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Map getInvoiceType(Map parameter) {
		Map map=new HashMap();
		String fpdm = String.valueOf(parameter.get("fpdm"));
		String fplb = fpdm.substring(7, 8);
		String fppz = fpdm.substring(0, 1);
		String fplx = "";
		String fpje = "";
		if (fpdm.length() == 10) {
			// 1.增值税专用发票 fplx 01 2.货物运输业增值税专用发票fplx 02 3.增值税普通发票fplx 04
			// 5.增值税专用发票 fplx 01 7.货物运输业增值税专用发票fplx 02 6.增值税普通发票fplx 04
			if (fplb.equals("1") || fplb.equals("5")) {// 专票
				fpje = parameter.get("kjje").toString();
				fplx = "01";
			} else if (fplb.equals("3") || fplb.equals("6")) {// 普通票
				fpje = parameter.get("fpje").toString();
				fplx = "04";
			} else if (fplb.equals("2") || fplb.equals("7")) {// 货运票（废弃）
				fpje = parameter.get("hjje").toString();
				fplx = "02";
			}
		} else if (fpdm.length() == 12) {
			if (!fppz.equals("0") && fplb.equals("2")) {// 机动车销售统一发票
				fpje = parameter.get("kjje").toString();
				fplx = "03";
			} else if (fppz.equals("0")) {// 电子票和卷式票，普通（叠票）
				// 判断发票种类 第11,12位 电子票：11 电子普票  卷式票：01  是普通卷票，04,05是普通（叠票）
				String fpzl1 = fpdm.substring(10, 11);
				String fpzl2 = fpdm.substring(11, 12);
				if ("1".equals(fpzl1)) {
					if ("1".equals(fpzl2)) {
						fpje = parameter.get("fpje").toString();
						fplx = "10";
					}else if("2".equals(fpzl2)){
						fpje = parameter.get("fpje").toString();
						fplx = "14";
					}
				} else if ("0".equals(fpzl1)) {
					if ("6".equals(fpzl2)||"7".equals(fpzl2)) {
						fpje = parameter.get("fpje").toString();
						fplx = "11";
					}else if ("4".equals(fpzl2)||"5".equals(fpzl2)) {
						fpje = parameter.get("fpje").toString();
						fplx = "04";
					}
					
				}

			}
		}
		map.put("fpje", fpje);
		map.put("fplx", fplx);
		return map;
	}


	/**
	 * 获取传入参数
	 * 
	 * @param nsrsbh
	 * @param swjg_mc
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static String getStringsNeed(Map paramter) {
		if (paramter.containsKey("cacheEntity")) {
			paramter.remove("cacheEntity");
		}
		String reg = "";
		if (null != paramter) {
			Iterator<String> iterator = paramter.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				String value = (String) paramter.get(key);
				if ("fpdm".equals(key) || "fphm".equals(key)
						|| "swjgmc".equals(key) || "hjje".equals(key)
						|| "yzmSj".equals(key) || "kprq".equals(key)
						|| "fpje".equals(key) || "kjje".equals(key)) {
					reg += "var " + key + "='" + value + "'; ";
				}
			}
		}
		return reg;
	}
    /**
     * 获取税局正常json
     * @param data
     * @return
     */
	private static String getCacheSjxxJson(String  data) {
		if (StringUtils.isNotEmpty(data)) {
			// 清单中括号会影响数据
			data = data.toString().substring(data.indexOf("(") + 1,
					data.length()-1);
		}
		return data;
	}
	/**
	 * 获取map
	 * @param data
	 * @return
	 */
	@SuppressWarnings({ "rawtypes" })
	private static Map getSjxxMap(String  data) {
		Map returnMap=new HashMap();
		if (StringUtils.isNotEmpty(data)) {
			returnMap = DataConvertUtil.StringToMap(data);
		}
		
		return returnMap;
	}
	/**
	 * 将获取到数据格式化成之前伙伴的那种格式
	 * （这里是坑）
	 * @param map
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List getOldInvoiceMap(Map map) {
		List list = new ArrayList();
		final Map initInoiceMap = CommonService.getInitInvoiceMap();
		Map initInoicesMap = initInoiceMap;
		if (map != null) {
			Map invoiceData = (Map) map.get("invoiceData");
			String invoiceTypeCode = (String) invoiceData
					.get("invoiceTypeCode");
			if ("01".equals(invoiceTypeCode) || "10".equals(invoiceTypeCode)
					|| "04".equals(invoiceTypeCode) || "14".equals(invoiceTypeCode)) {
				Map retMap = new HashMap();
				retMap.putAll((Map) initInoicesMap.get("10"));
				List invoiceDetailData = (List) invoiceData
						.get("invoiceDetailData");
				int i = 0;
				List resList = new ArrayList();
				List hwList = new ArrayList();
				for (Object object : invoiceDetailData) {
					Map redMap = new LinkedHashMap();
					Map invoiceDetailMap = (Map) object;
					Iterator<String> iterator = invoiceDetailMap.keySet()
							.iterator();
					while (iterator.hasNext()) {
						String key = iterator.next();
						if (!"isBillLine".equals(key) && !"lineNum".equals(key)) {
							String value = (String) invoiceDetailMap.get(key);
							value = (value != null) ? value : " ";
							value = value.replaceAll("[\"]", "≡")
									.replaceAll("[{]", "▄")
									.replaceAll("[}]", "☺");
							redMap.put(retMap.get(key), value);
						}
						resList.add(key);
					}
					i++;
					if (i > 9) {
						hwList.add(new ResultBean("", "jsonhw" + i,
								DataConvertUtil.MapToString(redMap)));
					} else {
						hwList.add(new ResultBean("", "jsonhw0" + i,
								DataConvertUtil.MapToString(redMap)));
					}
				}
				// 删除发票详情
				for (Object object : resList) {
					String key = (String) object;
					retMap.remove(key);
				}
				Iterator<String> iterator = retMap.keySet().iterator();
				while (iterator.hasNext()) {
					String key = iterator.next();
					String value = (String) retMap.get(key);
					if ("开票日期".equals(value)) {
						list.addAll(hwList);
						list.add(new ResultBean("", value,
								formatDate(invoiceData.get(key).toString())));
						continue;
					} else if ("voidMark".equals(key)) {
						String voidMark = (String) invoiceData.get(key);
						String iszf = "否";
						if ("0".equals(voidMark)) {
							iszf = "否";
						} else {
							iszf = "是";
						}
						list.add(new ResultBean("", value, iszf));
						continue;
					} else if ("isBillMark".equals(key)) {
						String isBillMark = (String) invoiceData.get(key);
						String isqd = "否";
						if ("N".equals(isBillMark)) {
							isqd = "否";
						} else {
							isqd = "是";
						}
						list.add(new ResultBean("", value, isqd));
						continue;
					}else if("invoiceRemarks".equals(key)){
						String invoiceRemarks = (String) invoiceData.get(key);
						invoiceRemarks = (invoiceRemarks != null) ? invoiceRemarks : " ";
						invoiceRemarks = invoiceRemarks.replaceAll("[\n]", "▎").replaceAll("[\"]", "≡")
								.replaceAll("[{]", "▄")
								.replaceAll("[}]", "☺");
						list.add(new ResultBean("", value, invoiceRemarks));
						continue;
					}
					list.add(new ResultBean("", value, invoiceData.get(key)
							.toString()));
				}
			} else if ("03".equals(invoiceTypeCode)) {
				Map retMap = (Map) initInoicesMap.get("03");
				Iterator<String> iterator = retMap.keySet().iterator();
				while (iterator.hasNext()) {
					String key = iterator.next();
					String value = (String) retMap.get(key);
					if ("invoiceTypeName".equals(key)) {
						list.add(new ResultBean("pz", value, invoiceData.get(
								key).toString()));
					} else if ("voidMark".equals(key)) {
						String voidMark = invoiceData.get(key).toString();
						String iszf = "否";
						if ("0".equals(voidMark)) {
							iszf = "否";
						} else {
							iszf = "是";
						}
						list.add(new ResultBean("", value, iszf));
					}else if("invoiceRemarks".equals(key)){
						String invoiceRemarks = (String) invoiceData.get(key);
						invoiceRemarks = (invoiceRemarks != null) ? invoiceRemarks : " ";
						invoiceRemarks = value.replaceAll("[\n]", "").replaceAll("[\"]", "≡")
								.replaceAll("[{]", "▄")
								.replaceAll("[}]", "☺");
						list.add(new ResultBean("", value, invoiceRemarks));
						continue;
					} else {
						list.add(new ResultBean("", value, invoiceData.get(key)
								.toString()));
					}
				}
			} else if ("11".equals(invoiceTypeCode)) {
				Map retMap = new HashMap();
				retMap.putAll((Map) initInoicesMap.get("11"));
				List invoiceDetailData = (List) invoiceData
						.get("invoiceDetailData");
				int i = 0;
				List resList = new ArrayList();
				List hwList = new ArrayList();
				for (Object object : invoiceDetailData) {
					Map redMap = new HashMap();
					Map invoiceDetailMap = (Map) object;
					Iterator<String> iterator = invoiceDetailMap.keySet()
							.iterator();
					while (iterator.hasNext()) {
						String key = iterator.next();
						if (!"isBillLine".equals(key) && !"lineNum".equals(key)) {
							String value = (String) invoiceDetailMap.get(key);
							value = (value != null) ? value : " ";
							value = value.replaceAll("[\"]", "≡")
									.replaceAll("[{]", "▄")
									.replaceAll("[}]", "☺");
							redMap.put(retMap.get(key), value);
						}
						resList.add(key);
					}
					i++;
					if (i > 9) {
						hwList.add(new ResultBean("", "jsonhw" + i,
								DataConvertUtil.MapToString(redMap)));
					} else {
						hwList.add(new ResultBean("", "jsonhw0" + i,
								DataConvertUtil.MapToString(redMap)));
					}
				}
				// 删除发票详情
				for (Object object : resList) {
					String key = (String) object;
					retMap.remove(key);
				}
				Iterator<String> iterator = retMap.keySet().iterator();
				while (iterator.hasNext()) {
					String key = iterator.next();
					String value = (String) retMap.get(key);
					if ("开票日期".equals(value)) {
						list.addAll(hwList);
						list.add(new ResultBean("", value,
								formatDate(invoiceData.get(key).toString())));
						continue;
					} else if ("voidMark".equals(key)) {
						String voidMark = invoiceData.get(key).toString();
						String iszf = "否";
						if ("0".equals(voidMark)) {
							iszf = "否";
						} else {
							iszf = "是";
						}
						list.add(new ResultBean("", value, iszf));
						continue;
					}else if("invoiceRemarks".equals(key)){
						String invoiceRemarks = invoiceData.get(key).toString();
						invoiceRemarks = (invoiceRemarks != null) ? invoiceRemarks : " ";
						invoiceRemarks = value.replaceAll("[\n]", "").replaceAll("[\"]", "≡")
								.replaceAll("[{]", "▄")
								.replaceAll("[}]", "☺");
						list.add(new ResultBean("", value, invoiceRemarks));
						continue;
					}
					list.add(new ResultBean("", value, invoiceData.get(key)
							.toString()));
				}
			}
			list.add(new ResultBean("CXJG", "查询结果", "成功"));
		}
		return list;
	}

	private static String formatDate(String date) {
		if (!date.contains("-")) {
			String year = date.substring(0, 4);
			String month = date.substring(4, 6);
			String day = date.substring(6, 8);
			date = year + "-" + month + "-" + day;
		}
		return date;
	}
	
	/**
	 * 获取国税总局的数据处理规则
	 * 思考服务不可用
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static String getInvoiceRule(String newRoleJsName,String fplx,Map paramterMap){
		//先比较新的roleName与老的roleName是否相同（从redis获取）
		String role="";
		String swjg_mc=paramterMap.get("swjgmc").toString();
		if ("11".equals(fplx)) {
			swjg_mc=swjg_mc.replace("卷式普通", "电子");
		}else{
			if("14".equals(fplx)){
				swjg_mc=swjg_mc.replace("电子普通[通行费]", "电子");
			}else{
				swjg_mc=swjg_mc.replace("电子普通", "电子");
			}
		}
		TaxOfficeFactory  taxOfficeFactory=new TaxOfficeFactory();
		Map roleMap=taxOfficeFactory.queryOneRole(swjg_mc);
		if(roleMap!=null){
			String oldRoleJsName=(String) roleMap.get("roleJsName");
			String oldRole=(String) roleMap.get("role");
			if(StringUtils.isEmpty(newRoleJsName)){
				return oldRole;
			}else{
				if(StringUtils.isNotEmpty(oldRoleJsName)&& newRoleJsName.equals(oldRoleJsName)){
				  return oldRole;	
				}
				//这里抓一下异常，是因为框架在保存时事务的会抛异常，不要把异常往上抛
				try{
					String url = jsRuleURL+newRoleJsName+".js";
					Map attrMap = new HashMap<String, String>();
					attrMap.put("Referer", "https://inv-veri.chinatax.gov.cn/");
					attrMap.put("Host","inv-veri.chinatax.gov.cn");
					attrMap.put(HeaderType.USERAGENT, BrowerType.firfox);
					Map<String, String> mar = new HashMap<String, String>();
					mar.put("_", String.valueOf(System.currentTimeMillis()));
					Map logMap = new HashMap();
					logMap.put("requestType", "rule");
					logMap.put("invoiceName",swjg_mc);
					logMap.put("requestId", paramterMap.get("requestId").toString());
					Map map = httpClient.callGetService(url,"",logMap, mar, attrMap,2);
					String newRole="";
					if(map!=null){
						String resultData = (String) map.get("resultData");
						String resultCode = (String) map.get("resultCode");
						if (StringUtils.isNotEmpty(resultData)&& "SUCCESS".equals(resultCode)) {
							String s[]=resultData.split("\"");
							if(s.length>2){
								newRole=s[1];
								role=newRole;
								Map retMap=new HashMap();
								retMap.put("oldRole",oldRole);
								retMap.put("oldRoleJsName",oldRoleJsName);
								retMap.put("swjg_mc",swjg_mc);
								retMap.put("newRole", newRole);
								retMap.put("newRoleJsName", newRoleJsName);
								//更新规则
								taxOfficeFactory.upOneRole(retMap);
							}else{
								logger.error("获取规则错误"+resultData);
							}
						}
					}
				}catch(Exception e){
					logger.error("获取规则错误"+e.getMessage());
					e.printStackTrace();
					return oldRole;
				}
				
			}
		}
		return role;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private  static Map getAnalysisMap(Map paramter,String resultData,String rule,String fplx){
		Map retMap=new HashMap();
		Map dataMap=new HashMap();
		String fpdm=(String) paramter.get("fpdm");
  	    String fphm=(String) paramter.get("fphm");
  	   String swjgmc=(String) paramter.get("swjgmc");
        try{
     	    String yzmSj=(String) paramter.get("yzmSj");
     	    String requestId=(String) paramter.get("requestId");
     	    retMap.put("fpdm", fpdm);
     	    retMap.put("fphm", fphm);
     	    retMap.put("swjgmc", swjgmc);
     	    retMap.put("yzmSj", yzmSj);
     	    retMap.put("jsonData", resultData);
     	    retMap.put("fplx", fplx);
     	    retMap.put("rule", rule);
     	    Map logMap=new HashMap();
     	    logMap.put("invoiceCode",fpdm);
     		logMap.put("invoiceNum", fphm);
     		logMap.put("invoiceName", swjgmc);
     		logMap.put("requestType", "jx");
     	    logMap.put("requestId", requestId);
     	    String analysisUrl=PropertiesUtils.getPropertiesValue("invoice_analysis_url");
     	    Map map=analysisHttpClient.callPostService(analysisUrl, logMap, retMap, null);
     	    if(map!=null){
     	    	String resultDataStr=(String) map.get("resultData");
     	    	if(resultDataStr.length()>0){
     	    		Map asMap=DataConvertUtil.StringToMap(resultDataStr);
     	    		if(null!=asMap){
     	    			String resultCode=(String) asMap.get("resultCode");
     	    			if("1000".equals(resultCode)){
     	    				dataMap.put("invoiceData", (Map) asMap.get("invoiceResult"));
     	    			}
     	    		}
     	    	}
     	    }
		}catch(Exception e){
			logger.error(swjgmc+"-"+fpdm+"-"+fphm+"调用nodejs解析出错");
		}
		return dataMap;
	}
}
