package com.dcits.fpcy.commons.factory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

import com.dcits.app.asynctask.DynamicAsyncTaskService;
import com.dcits.app.data.DataObject;
import com.dcits.app.util.RedisUtils;
import com.dcits.fpcy.commons.bean.CacheEntity;
import com.dcits.fpcy.commons.bean.HSCEntity;
import com.dcits.fpcy.commons.bean.TaxOfficeBean;
import com.dcits.fpcy.commons.constant.SysConfig;
import com.dcits.fpcy.commons.dao.InvoiceDao;
import com.dcits.fpcy.commons.dao.TaxOfficeDao;
import com.dcits.fpcy.commons.service.core.InvoiceServerBase;
import com.dcits.fpcy.commons.service.core.InvoiceServerManger;
import com.dcits.fpcy.commons.utils.CommonUtils;
import com.dcits.fpcy.commons.utils.DataConvertUtil;
import com.dcits.fpcy.commons.utils.IpUtils;
import com.dcits.fpcy.commons.utils.JavaBeanUtils;
import com.dcits.fpcy.commons.utils.PropertiesUtils;
import com.dcits.fpcy.commons.utils.ResultUtils;
import com.dcits.fpcy.commons.utils.thirdApi.BaiWangApi;

/**
 * 组装发票信息
 * 
 * @author wuche
 * 
 */
public class InvoiceFactory {

	private static Log logger = LogFactory.getLog(InvoiceFactory.class);
	private static final String className = "commons.factory.InvoiceFactory";
	private static final String methodName = "queryInvoiceInfo";
	private InvoiceDao invoiceDao;
	@SuppressWarnings("unused")
	private TaxOfficeDao taxOfficeDao;
	private YzmFactory yzmFactory;

	public void setInvoiceDao(InvoiceDao invoiceDao) {
		this.invoiceDao = invoiceDao;
	}

	public void setTaxOfficeDao(TaxOfficeDao taxOfficeDao) {
		this.taxOfficeDao = taxOfficeDao;
	}

	public void setYzmFactory(YzmFactory yzmFactory) {
		this.yzmFactory = yzmFactory;
	}

	/**
	 * 获取数据数据
	 * 
	 * @param fpdm
	 * @return
	 * @throws JSONException
	 * @throws ParseException
	 * @throws NoSuchElementException
	 */
	@SuppressWarnings({ "rawtypes" })
	public Map queryInvoiceInfoFromDb(String fpdm, String fphm, String dataType)
			throws NoSuchElementException, ParseException, JSONException {
	    //从redis(排除错误的和key)
		Map map = invoiceDao.queryInvoiceInfo(fpdm, fphm, dataType);
		return map;
	}

	@SuppressWarnings({ "rawtypes" })
	public Map queryInvoiceErrorInfoFromDb(String ID)
			throws NoSuchElementException, ParseException, JSONException {
		Map map = invoiceDao.queryInvoiceErrorInfo(ID);
		return map;
	}

	/**
	 * 获取已经查询五次
	 * 
	 * @param fpdm
	 * @param fphm
	 * @return
	 */
	public Boolean getFalseInvoiceInfo(String fpdm, String fphm) {
		return invoiceDao.getFalseInvoiceInfo(fpdm, fphm);
	}
	/**
	 * 获取是否有重复提交错误结果
	 * 201,220
	 * @param fpdm
	 * @param fphm
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public String getFalseToInvoiceInfo(Map paramMap,List dataList) {
		String fpdm=(String) paramMap.get("fpdm");
		String fphm=(String) paramMap.get("fphm");
		List list=invoiceDao.getFalseInvoiceInfoList(fpdm, fphm);
		String invoicefalseState="";
		if(!list.isEmpty()){
			for (int i = 0; i < list.size(); i++) {
				Object obj=list.get(i);
				if(obj instanceof Map){
					Map map=(Map) obj;
					if(map!=null){
						String cycs=(String) map.get("cycs");
						String invoicefalseStateDb=(String) map.get("invoicefalseState");
						Map cycsMap=ResultUtils.mapStringToMap(cycs);
						if(cycsMap!=null){
							int size = dataList.size();
							if (0 < size) {
								boolean ifHave =true;
								for (int j = 0; j < size; j++) {
									String data = String.valueOf(paramMap.get(dataList.get(j)));
									String csStr=String.valueOf(cycsMap.get(dataList.get(j)));
									if("kjje".equals(dataList.get(j))){
										data=ResultUtils.getDecimalFormat(data);
										csStr=ResultUtils.getDecimalFormat(csStr);
									}
									if (!data.equals(csStr)) {
										ifHave=false;
										break;
									}
								}
								if(true==ifHave){
									invoicefalseState=invoicefalseStateDb;
									break;
								}
								
							}
						} 
					}
				}
			}
		}
		return invoicefalseState;
	}
	/**
	 * 获取并组装发票
	 * 
	 * @param fpdm
	 * @return
	 * @throws Exception
	 * @throws JSONException
	 * @throws ParseException
	 * @throws NoSuchElementException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DataObject queryInvoiceInfo(final Map paramterMap,
			final TaxOfficeBean taxOfficeBean) throws Exception {
		Map rtnMap = new HashMap();
		final String fpdm = String.valueOf(paramterMap.get("fpdm"));
		final String fphm = String.valueOf(paramterMap.get("fphm"));
		final Map keyMap = new HashMap();
		keyMap.put("FPDM", fpdm);
		keyMap.put("FPHM", fphm);
		String key = RedisUtils.getValue(className, methodName, new DataObject(
				keyMap));
		if (!"key".equals(key)) {
			RedisUtils.putValue(className, methodName, new DataObject(keyMap),
					"key", 60);
			CacheEntity cacheEntityNew = null;
			HSCEntity hscEntityNew = null;
			if (taxOfficeBean.swjg_mc.contains("增值税")) {
				hscEntityNew = null;
				// 这是提前缓存机制加大
				cacheEntityNew = yzmFactory.getRedisYzm(paramterMap,
						taxOfficeBean, true);
				// 国税和地税
			} else {
				cacheEntityNew = null;
				hscEntityNew = yzmFactory.getRedisDsYzm(taxOfficeBean
						.getSwjg_dm());
			}
			final CacheEntity cacheEntity = cacheEntityNew;
			final HSCEntity hscEntity = hscEntityNew;
			if (cacheEntity != null || hscEntity != null) {
				rtnMap.put("yzm", "YES");
			} else {
				rtnMap.put("yzm", "NO");
			}
			String threadId=IpUtils.getLocalIp().split("\\.")[3]+CommonUtils.getNextId();
			paramterMap.put("requestId", threadId);
			DynamicAsyncTaskService.getInstance().execute(new Runnable() {
				@Override
				public void run() {
					long startTime = System.currentTimeMillis();
					try {
						Map taxOffice = JavaBeanUtils.objectToMap(taxOfficeBean);
						Map result = singleOrQuery(paramterMap, taxOfficeBean,
								cacheEntity, hscEntity);
						if (!(SysConfig.CODE1000).equals(result
								.get(SysConfig.CYJGSTATE))) {
							if (result.get(SysConfig.INVOICEFALSESTATE) == null) {
								if (result.get("cyjgState").equals("2001")) {
									result = singleOrQuery(paramterMap,
											taxOfficeBean, null, null);
									if (result.get("cyjgState").equals("2001")) {
										result = singleOrQuery(paramterMap,
												taxOfficeBean, null, null);
									}
								}
							} else if (ifNendToAgainToFalseCode(result
									.get(SysConfig.INVOICEFALSESTATE))) {
								result = singleOrQuery(paramterMap,
										taxOfficeBean, null, null);
								if (ifNendToAgainToFalseCode(result
										.get(SysConfig.INVOICEFALSESTATE))) {
									result = singleOrQuery(paramterMap,
											taxOfficeBean, null, null);
								}
							}
						}
						// 替换用户不可见代码
						changeFalseCode(result);
						paramterMap.remove("cacheEntity");
						paramterMap.remove("key1");
						paramterMap.remove("index");
						paramterMap.remove("pic_id");
						paramterMap.remove("yzmSj");
						paramterMap.remove("yzm");
						paramterMap.remove("FPHM");
						paramterMap.remove("FPDM");
						paramterMap.remove("loginSj");
						paramterMap.remove("token");
						paramterMap.remove("username");
						paramterMap.remove("new");
						paramterMap.remove("key2");
						paramterMap.remove("key3");
						long endTime = System.currentTimeMillis();
						SimpleDateFormat df = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss");// 设置日期格式
						if ((SysConfig.CODE1000).equals(result
								.get(SysConfig.CYJGSTATE))) {
							Map parameter = new HashMap();
							parameter.put("FPDM", fpdm);
							parameter.put("FPHM", fphm);
							parameter.put("UID",
									String.valueOf(paramterMap.get("UID")));
							parameter.put(
									"CYJG",
									// 替换特殊字符：如",{,}
									new JSONArray((List) result.get("list"))
											.toString().replace("\\", "")
											.replace("}\"", "}")
											.replace("\"{", "{")
											.replaceAll("[≡]", "\\\\\"")
											.replaceAll("[▎]", " ")
											.replaceAll("[▄]", "\\\\{")
											.replaceAll("[☺]", "\\\\}"));
							parameter.put("CYCS", paramterMap.toString());
							parameter.put("USER_BM", "system");
							parameter.put("QY",
									String.valueOf(taxOffice.get("swjg_mc")));
							parameter.put("IP", "127.0.0.2");
							//
							parameter.put("requestId",
									String.valueOf(paramterMap.get("requestId")));
							parameter.put("INVOICECOMEFROM",
									String.valueOf(paramterMap
											.get("invoiceComeFrom")));
							parameter.put("useTime",
									String.valueOf(endTime - startTime));
							parameter.put("cyjgState", SysConfig.CODE1000);
							parameter.put("CYRQ", df.format(new Date()));
							if (result.containsKey("invoiceData")) {
								parameter.put("invoiceResult", String
										.valueOf(result.get("invoiceData")));
							} else {
								parameter.put("invoiceResult", new DataObject(
										result.get("invoiceMap")).getJson());
							}
							Map  parMap=new HashMap();
							parMap.put("CYCS",parameter.get("CYCS"));
							parMap.put("CYJG",parameter.get("CYJG"));
							parMap.put("CYRQ",parameter.get("CYRQ"));
							parMap.put("cyjgState",parameter.get("cyjgState"));
							parMap.put("invoiceResult",parameter.get("invoiceResult"));
							RedisUtils.putValue(className, methodName,
									new DataObject(keyMap),
									DataConvertUtil.MapToString(parMap), 120);
							String ifDel = String.valueOf(paramterMap
									.get("ifDel"));
							if ("Y".equals(ifDel)) {
								Map delete = new HashMap();
								delete.put("FPDM", fpdm);
								delete.put("FPHM", fphm);
								InvoiceDao.deleteQueryRecord(new DataObject(
										delete));
							}
							InvoiceDao
									.saveQueryRecord(new DataObject(parameter));
						} else {
							Map parameter = new HashMap();
							parameter.put("FPDM", fpdm);
							parameter.put("FPHM", fphm);
							parameter.put(
									"RZXX",
									new JSONArray((List) result.get("list"))
											.toString().replace("\\", "")
											.replace("}\"", "}")
											.replace("\"{", "{")
											.replaceAll("[≡]", "\\\\\"")
											.replaceAll("[▄]", "\\\\{")
											.replaceAll("[☺]", "\\\\}"));
							parameter.put("UID",
									String.valueOf(paramterMap.get("uid")));// 用户ID
							parameter.put("requestId",
									String.valueOf(paramterMap.get("requestId")));// 请求Id
							parameter.put("CYCS", paramterMap.toString());
							parameter.put("QY",
									String.valueOf(taxOffice.get("swjg_mc"))); // 获取当前所属地域
							parameter.put("IP", "127.0.0.2"); // 当前登录人ip
							parameter.put("useTime",
									String.valueOf(endTime - startTime));
							parameter.put("invoiceComeFrom",
									String.valueOf(paramterMap
											.get("invoiceComeFrom")));
							parameter.put("invoicefalseState", String
									.valueOf(result
											.get(SysConfig.INVOICEFALSESTATE)));
							parameter.put("systemfalseState",
									result.get(SysConfig.SYSTEMFALSESTATE));
							parameter.put("cyjgState", SysConfig.CODE20011);
							parameter.put("CYRQ", df.format(new Date()));
							Map  parMap=new HashMap();
							parMap.put("CYCS",parameter.get("CYCS"));
							parMap.put("RZXX",parameter.get("RZXX"));
							parMap.put("CYRQ",parameter.get("CYRQ"));
							parMap.put("cyjgState",parameter.get("cyjgState"));
							parMap.put("invoicefalseState",parameter.get("invoicefalseState"));
							parMap.put("systemfalseState",parameter.get("systemfalseState"));
							RedisUtils.putValue(className, methodName,
									new DataObject(keyMap),
									DataConvertUtil.MapToString(parMap), 30);
							InvoiceDao
									.saveErrorRecord(new DataObject(parameter));
						}
					} catch (Exception e) {
						logger.debug("线程出现异常:" + e);
					}
				}
			}, 1);
		}
		String isOpenBW = PropertiesUtils.getPropertiesValue("isOpenBW");
		if("Y".equals(isOpenBW)){
			/**获取百望数据*/
			DynamicAsyncTaskService.getInstance().execute(new Runnable() {
				@Override
				public void run() {
					Map paramMap=getInvoiceType(paramterMap);	
					paramMap.put("fpdm", fpdm);
					paramMap.put("fphm", fphm);
					paramMap.put("swjg_mc",taxOfficeBean.swjg_mc);
					paramMap.put("kprq",paramterMap.get("kprq"));
					paramMap.put("requestId",paramterMap.get("requestId"));
					BaiWangApi.getInvoice(paramMap);
				}
			},2);
		}
		return new DataObject(rtnMap);
	}

	/**
	 * 开启发票调用 （组装验证码） 1，国地税（需要cookie等信息）--建HSCEntity对象
	 * 2，增值税（不需session，依赖于js加密解密）--->CacheEntity
	 * 
	 * @param parameter
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map singleOrQuery(Map paramterMap, TaxOfficeBean taxOfficeBean,
			CacheEntity cacheEntity, HSCEntity hscEntity) {
		Map result = new HashMap();
		String fpdm = (String) paramterMap.get("FPDM");
		String invoiceComeFrom = (String) paramterMap.get("invoiceComeFrom");
		String swjg_mc = taxOfficeBean.swjg_mc;
		String errorCode = "";
		if (swjg_mc.contains("增值税")) {
			if (null == cacheEntity) {
				Map yzmMap = new HashMap();
				yzmMap.put("invoiceComeFrom", invoiceComeFrom);
				yzmMap.put("fpdm", fpdm);
				yzmMap.put("requestId", paramterMap.get("requestId"));
				long startTime = System.currentTimeMillis();
				// 从接口中拿
				yzmFactory.getYzmObject(yzmMap, taxOfficeBean);
				cacheEntity = (CacheEntity) yzmMap.get("cacheEntity");
				long endTime = System.currentTimeMillis();
				logger.debug(swjg_mc + "从接口中拿验证码的时间" + (endTime - startTime));
			}
			if (null == cacheEntity) {
				logger.error(swjg_mc + "redis获取不到验证码");
				if (StringUtils.isEmpty(errorCode)) {
					errorCode = SysConfig.INVOICEFALSESTATECODE230;
				}
				//税局查验服务暂时不可用，请稍后再试
				return ResultUtils.getResult(result,SysConfig.INVOICEFALSESTATECODE218,errorCode,
						SysConfig.CODE20011,SysConfig.getMap().get(SysConfig.INVOICEFALSESTATECODE218));
			}
			errorCode = cacheEntity.getErrorCode();
			String yzm = cacheEntity.getYzm();
			if (SysConfig.INVOICETRUESTATE000.equals(errorCode)
					&& StringUtils.isNotEmpty(yzm)) {
				paramterMap.put("cacheEntity", cacheEntity);
			} else {
				logger.error(swjg_mc + "redis获取不到验证码");
				//这里调3次可以再
				if (StringUtils.isEmpty(errorCode)) {
					errorCode = SysConfig.INVOICEFALSESTATECODE230;
				}
				//税局查验服务暂时不可用，请稍后再试
				return ResultUtils.getResult(result,SysConfig.INVOICEFALSESTATECODE218,errorCode,
						SysConfig.CODE20011,SysConfig.getMap().get(SysConfig.INVOICEFALSESTATECODE218));
			}
			// 国地税
		} else {
			Map yzmMap = new HashMap();
			yzmMap.put("invoiceComeFrom", invoiceComeFrom);
			yzmMap.put("fpdm", fpdm);
			if (null == hscEntity) {
				yzmFactory.getYzmObject(yzmMap, taxOfficeBean);
			} else {
				yzmMap.put("hscEntity", hscEntity);
			}
			logger.debug("从redis获取到国地税验证码 :"
					+ ((HSCEntity) yzmMap.get("hscEntity")).getYzm());
			paramterMap.put("hscEntity", yzmMap.get("hscEntity"));
		}
		result = singleQuery(paramterMap, taxOfficeBean);
		return result;
	}

	/**
	 * 开启发票调用
	 * 
	 * @param parameter
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map<String, Object> singleQuery(Map dataObject,
			TaxOfficeBean taxOfficeBean) {
		InvoiceServerBase invoiceServerBase = InvoiceServerManger
				.getFpcyBaseImpl(taxOfficeBean.fpimpclass);
		if (taxOfficeBean.swjg_mc.contains("增值")
				|| taxOfficeBean.swjg_mc.contains("机动车")) {
			return invoiceServerBase.FPCY(dataObject, taxOfficeBean);
		} else {
			return getGDSJsonData(
					invoiceServerBase.FPCY(dataObject, taxOfficeBean),
					taxOfficeBean.swjg_mc);
		}
	}

	/***
	 * 删除发票
	 * 
	 * @param fphm
	 * @param fpdm
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void deleteInvoice(String fphm, String fpdm) {
		Map param = new HashMap();
		param.put("FPDM", fpdm);
		param.put("FPHM", fphm);
		InvoiceDao.deleteQueryRecord(new DataObject(param));
	}

	/**
	 * 格式化国税发票格式
	 * 
	 * @param invoiceMap
	 * @param lx
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map getGDSJsonData(Map invoiceMap, String lx) {
		try {
			Map jsonMap = new HashMap();
			List list = new ArrayList();
			int i = 0;
			for (Map<String, String> map : (List<Map<String, String>>) invoiceMap
					.get("list")) {
				String key = String.valueOf(map.get("name1"));
				String value = String.valueOf(map.get("value"));
				if ("".equals(key) && !"".equals(value)) {
					list.add("cyjg");
					jsonMap.put("cyjg", String.valueOf(map.get("value")));
				} else {
					if (jsonMap.containsKey(key)) {
						key = key + String.valueOf(i);
						i++;
					}
					list.add(key);
					jsonMap.put(key, String.valueOf(map.get("value")));
				}

			}
			String resultJson = new DataObject(jsonMap).getJson();
			String code = lx.contains("国税") ? "20" : "30";
			String keys = list.toString();
			Map resultMap = new HashMap();
			resultMap.put("invoiceTypeCode", code);
			resultMap.put("invoiceTypeName", lx);
			resultMap.put("resultJson", resultJson);
			resultMap.put("resultKeys", keys);
			invoiceMap.put("invoiceData", new DataObject(resultMap).getJson());
		} catch (Exception e) {
		}
		return invoiceMap;
	}

	/**
	 * 判断是否需要再次去税局请求
	 * @param stateCode
	 */
	private static boolean ifNendToAgainToFalseCode(Object stateCode) {
		boolean flag = false;
		if (!stateCode.toString().isEmpty()) {
			flag = stateCode.equals("101")
					|| stateCode.equals("105")
					|| stateCode.equals("106") || stateCode.equals("113")
					|| stateCode.equals("118");
		}
		return flag;
	}

	/**
	 * 格式化税局异常错误码，使错误码统一
	 * （让其变成用户可见的错误码）
	 * @param stateCode
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void changeFalseCode(Map result) {
		// 替换掉用户不可见代码
		 if ("101".equals(result.get(SysConfig.INVOICEFALSESTATE))) {
			result.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE213);
			result.put(SysConfig.SYSTEMFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE231);
		} else if ("102".equals(result.get(SysConfig.INVOICEFALSESTATE))) {
			result.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE213);
			result.put(SysConfig.SYSTEMFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE213);
		} else if ("103".equals(result.get(SysConfig.INVOICEFALSESTATE))) {
			result.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE213);
			result.put(SysConfig.SYSTEMFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE213);
		} else if ("104".equals(result.get(SysConfig.INVOICEFALSESTATE))) {
			result.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE213);
			result.put(SysConfig.SYSTEMFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE213);
		} else if ("105".equals(result.get(SysConfig.INVOICEFALSESTATE))) {
			result.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE213);
			result.put(SysConfig.SYSTEMFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE213);
		} else if ("106".equals(result.get(SysConfig.INVOICEFALSESTATE))) {
			result.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE213);
			result.put(SysConfig.SYSTEMFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE232);
		}else if ("111".equals(result.get(SysConfig.INVOICEFALSESTATE))) {
			result.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE213);
			result.put(SysConfig.SYSTEMFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE232);
		}else if ("113".equals(result.get(SysConfig.INVOICEFALSESTATE))) {
			result.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE213);
			result.put(SysConfig.SYSTEMFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE213);
		}else if ("118".equals(result.get(SysConfig.INVOICEFALSESTATE))) {
				result.put(SysConfig.INVOICEFALSESTATE,
						SysConfig.INVOICEFALSESTATECODE213);
				result.put(SysConfig.SYSTEMFALSESTATE,
						SysConfig.INVOICEFALSESTATECODE231);
		}else if ("231".equals(result.get(SysConfig.INVOICEFALSESTATE))
				||"232".equals(result.get(SysConfig.INVOICEFALSESTATE))
				||"241".equals(result.get(SysConfig.INVOICEFALSESTATE))) {
			result.put(SysConfig.SYSTEMFALSESTATE,
					result.get(SysConfig.INVOICEFALSESTATE));
			result.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE213);
	     }else if ("230".equals(result.get(SysConfig.INVOICEFALSESTATE))
	    		 ||"240".equals(result.get(SysConfig.INVOICEFALSESTATE))) {
	    	    result.put(SysConfig.SYSTEMFALSESTATE,
						result.get(SysConfig.INVOICEFALSESTATE));
				result.put(SysConfig.INVOICEFALSESTATE,
						SysConfig.INVOICEFALSESTATECODE218);
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
}
