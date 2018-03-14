package com.dcits.fpcy.interfaces;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.json.JSONArray;

import com.dcits.app.data.DataObject;
import com.dcits.app.service.BaseService;
import com.dcits.app.service.IService;
import com.dcits.app.util.RedisUtils;
import com.dcits.fpcy.commons.bean.ResultBean;
import com.dcits.fpcy.commons.bean.TaxOfficeBean;
import com.dcits.fpcy.commons.constant.SysConfig;
import com.dcits.fpcy.commons.factory.InvoiceFactory;
import com.dcits.fpcy.commons.factory.TaxOfficeFactory;
import com.dcits.fpcy.commons.utils.DataConvertUtil;
import com.dcits.fpcy.commons.utils.DateUtils;
import com.dcits.fpcy.commons.utils.JavaBeanUtils;
import com.dcits.fpcy.commons.utils.PropertiesUtils;
import com.dcits.fpcy.commons.utils.ResultUtils;

/**
 * 查询发票接口 （企业接口与系统接口不同是应为返回数据格式不用， 所以两接口不一样）这是一个历史性坑，一定小心
 * 
 * @author wuche
 * 
 */
public class QueryOpenInvoiceInfo extends BaseService implements IService {
	private static Log logger = LogFactory.getLog(QueryInvoiceInfo.class);
	private static final String className = "commons.factory.InvoiceFactory";
	private static final String methodName = "queryInvoiceInfo";
	private static final String dataType = "json";
	private TaxOfficeFactory taxOfficeFactory;
	private InvoiceFactory invoiceFactory;

	public void setTaxOfficeFactory(TaxOfficeFactory taxOfficeFactory) {
		this.taxOfficeFactory = taxOfficeFactory;
	}

	public void setInvoiceFactory(InvoiceFactory invoiceFactory) {
		this.invoiceFactory = invoiceFactory;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObject doService(DataObject dataobject) {
		// 判断是不是该服务停止
		Map parameter = dataobject.getMap();
		if (parameter.containsKey("ifHasSpecialMark")) {
			Iterator entries = parameter.entrySet().iterator();
			Map tempMap = new HashMap();
			while (entries.hasNext()) {
				Map.Entry entry = (Map.Entry) entries.next();
				String key = String.valueOf(entry.getKey());
				String value = String.valueOf(entry.getValue());
				String newKey = key.replace("-", "@").replace("_", "*");
				tempMap.put(newKey, value);
			}
			parameter.clear();
			parameter = tempMap;
		}
		Map returnMap = new HashMap();
		//数据库数据
		Map dbMap = new HashMap();
		String FPDM = StringUtils.trim((String) parameter.get("FPDM"));
		String fpdm = StringUtils.trim((String) parameter.get("fpdm"));
		String FPHM = StringUtils.trim((String) parameter.get("FPHM"));
		String fphm = StringUtils.trim((String) parameter.get("fphm"));
		String kjje = (String) parameter.get("kjje");
		if (StringUtils.isNotEmpty(kjje) && "null" != kjje) {
			kjje = ResultUtils.getDecimalFormat(kjje);
			parameter.put("kjje", kjje);
		}
		fpdm = (fpdm != null ? fpdm : FPDM);
		fphm = (fphm != null ? fphm : FPHM);
		parameter.put("fpdm", fpdm);
		parameter.put("fphm", fphm);
		parameter.put("FPDM", fpdm);
		parameter.put("FPHM", fphm);
		parameter.put("dataType", dataType);
		String invoiceComeFrom = StringUtils.trim((String) parameter
				.get("invoiceComeFrom"));
		Map fpcyParas = new HashMap();
		String swjg_mc = "";
		try {
			fpcyParas = taxOfficeFactory.queryTaxOfficeByFpdm(fpdm,
					invoiceComeFrom);
		} catch (Exception e) {
			returnMap.put("resultMsg", "您输入的发票地区暂时不支持查询");
			returnMap.put("resultCode", SysConfig.CODE20011);
			returnMap.put("invoicefalseCode",
					SysConfig.INVOICEFALSESTATECODE219);
			returnMap.put("invoiceName", "未能获取");
			logger.error("获取税局异常:" + e.getMessage());
			return toJson(returnMap);
		}
		List dataList = new ArrayList();
		if (fpcyParas == null || fpcyParas.size() == 0) {
			returnMap.put("resultMsg", "您输入的发票地区暂时不支持查询");
			returnMap.put("resultCode", SysConfig.CODE20011);
			returnMap.put("invoiceName", "未能获取");
			returnMap.put("invoicefalseCode",
					SysConfig.INVOICEFALSESTATECODE219);
			return toJson(returnMap);
		} else {
			swjg_mc = (String) fpcyParas.get("swjg_mc");
			returnMap.put("invoiceName", swjg_mc);
			boolean flag = taxOfficeFactory.queryEnable(fpcyParas);
			if (flag && swjg_mc.isEmpty()) {
				returnMap.put("resultMsg", "您输入的发票地区暂时不支持查询");
				returnMap.put("resultCode", SysConfig.CODE20011);
				returnMap.put("invoicefalseCode",
						SysConfig.INVOICEFALSESTATECODE219);
				returnMap.put("invoiceName", swjg_mc);
				return toJson(returnMap);
			}
			//比对入库增值税输入参数是否一致
			if (fpdm.length() == 12 && !fpdm.startsWith("0")) {} else {
				dataList = taxOfficeFactory.getCheck(String.valueOf(fpcyParas.get("datamodel")));
				int size = dataList.size();
				if (0 < size) {
					for (int i = 0; i < size; i++) {
						String data = String.valueOf(parameter.get(dataList
								.get(i)));
						if (ResultUtils.isNull(data)) {
							returnMap.put("resultMsg", "您输入的发票信息不完全");
							returnMap.put("resultCode", SysConfig.CODE20011);
							returnMap.put("invoicefalseCode",
									SysConfig.INVOICEFALSESTATECODE211);
							returnMap.put("invoiceName", swjg_mc);
							return toJson(returnMap);
						}
					}
				}
			}
		}
		// 判断开票日期是否是当日及开票日期是否符合格式
		String date = DateUtils.getToday();
		String kprq = (String) parameter.get("kprq");
		if (kprq != null) {
			if (kprq.equals(date)) {
				returnMap.put("resultMsg", "如您查询的发票是当日开具的，请于次日查询！");
				returnMap.put("resultCode", SysConfig.CODE20011);
				returnMap.put("invoicefalseCode",
						SysConfig.INVOICEFALSESTATECODE216);
				returnMap.put("invoiceName", swjg_mc);
				return toJson(returnMap);
			} else if (!kprq.contains("-")) {
				returnMap.put("resultMsg",
						"您输入的发票日期格式不正确，请你重新输入发票信息！（格式：2016-01-01）！");
				returnMap.put("resultCode", SysConfig.CODE20011);
				returnMap.put("invoicefalseCode",
						SysConfig.INVOICEFALSESTATECODE203);
				returnMap.put("invoiceName", swjg_mc);
				return toJson(returnMap);
			}
		}
		// 获取是否查询数据库的标志
		String isSelectDb = PropertiesUtils.getPropertiesValue("isSelectDb");
		if ("YES".equals(isSelectDb)) {
			// 判断数据是否存在该条数据
			try {
				returnMap = invoiceFactory.queryInvoiceInfoFromDb(fpdm, fphm,
						dataType);
			} catch (Exception e) {
				logger.error("获取发票数据异常:" + e.getMessage());
			}
			if (null != returnMap && !returnMap.isEmpty()) {
				Map cycs = ResultUtils.mapStringToMap((String) returnMap.get("cycs"));
				if (null != cycs) {// 判断查验参数是否和数据库存储的一致
					int size = dataList.size();
					if (0 < size) {
						for (int i = 0; i < size; i++) {
							String data = String.valueOf(parameter.get(dataList
									.get(i)));
							String csStr = String.valueOf(cycs.get(dataList
									.get(i)));
							if ("kjje".equals(dataList.get(i))) {
								data = ResultUtils.getDecimalFormat(data);
								csStr = ResultUtils.getDecimalFormat(csStr);
							}
							if (!data.equals(csStr)) {
								returnMap.put("resultMsg", "您输入的发票信息不一致");
								returnMap
										.put("resultCode", SysConfig.CODE20011);
								returnMap.put("invoicefalseCode",
										SysConfig.INVOICEFALSESTATECODE220);
								returnMap.remove("list");
								returnMap.remove("invoiceResult");
								returnMap.remove("dateResultbean");
								returnMap.put("invoiceName",
										fpcyParas.get("swjg_mc"));
								return toJson(returnMap);
							}
						}
					}
				}
				if (!swjg_mc.contains("增值税")) {
					returnMap.put("resultCode", SysConfig.CODE1000);
					returnMap.put("invoiceName", fpcyParas.get("swjg_mc"));
					returnMap.put("dataFrom", "dataBase");
					returnMap.put("resultMsg", "查验结果成功");
					returnMap.remove("dateResultbean");
					returnMap.remove("list");
					return toJson(returnMap);
				} else {
					ResultBean resultbean = null;
					if ("list".equals(dataType)) {
						List list1 = (List) returnMap.get("list");
						for (int i = 0; i < list1.size(); i++) {
							resultbean = (ResultBean) list1.get(i);
							if (resultbean.getName2().equals("开票日期")) {
								break;
							}
						}
					} else if ("json".equals(dataType)) {
						resultbean = (ResultBean) returnMap
								.get("dateResultbean");
					}
					// 没有日期票手撕增值发票
					if (!resultbean.getName2().equals("开票日期")) {
						returnMap.put("invoiceName", fpcyParas.get("swjg_mc"));
						returnMap.put("resultCode", SysConfig.CODE1000);
						returnMap.put("dataFrom", "dataBase");
						returnMap.put("resultMsg", "查验结果成功");
						returnMap.remove("dateResultbean");
						return toJson(returnMap);
					}
					// 判断开票日期是否是当月
					String KPRQ = resultbean.getValue().toString()
							.substring(0, 4)
							+ "-"
							+ resultbean.getValue().toString().substring(5, 7);
					String db_kprq = resultbean.getValue().toString().substring(0, 10);
					String yearMonth = DateUtils.getYearMonth();
					// 判断是否为当月增值税票
					String ifDel = "N";
					// 判断是不是当天票（当天票只去数据库拿）
					String cyrq = (String) returnMap.get("cyrq");
					cyrq = cyrq.substring(0, 10);
					if (!KPRQ.equals(yearMonth) || cyrq.equals(date) || swjg_mc.contains("电子发票")) {
						//开票日期不等于当前月且查验日期月份与开票日期月份不相等
						if(!DateUtils.compare_monuth(cyrq,db_kprq)||swjg_mc.contains("电子发票")||  
								cyrq.equals(date)){
						returnMap.put("resultCode", SysConfig.CODE1000);
						returnMap.put("invoiceName", fpcyParas.get("swjg_mc"));
						returnMap.put("dataFrom", "dataBase");
						returnMap.remove("dateResultbean");
						returnMap.put("resultMsg", "查验结果成功");
						return toJson(returnMap);
					  }
					} else {
						ifDel = "Y";
					}
					parameter.put("ifDel", ifDel);
				}
			}
		}
		// 判断是否已经过了查票期 （增值税票）
		if (swjg_mc.contains("增值税")) {
			boolean isOneYear = DateUtils.getIsBetweenOneYear(kprq);
			if (!isOneYear) {
				returnMap.put("resultCode", SysConfig.CODE20011);
				returnMap.put("invoiceName", fpcyParas.get("swjg_mc"));
				returnMap.put("invoicefalseCode",
						SysConfig.INVOICEFALSESTATECODE217);
				returnMap.put("resultMsg", "只可查验最近1年内开具的发票");
				return toJson(returnMap);
			}
		}
		// 判断这张票是否查询了超过五次
		if (invoiceFactory.getFalseInvoiceInfo(fpdm, fphm)) {
			returnMap.put("resultCode", SysConfig.CODE20011);
			returnMap.put("invoiceName", fpcyParas.get("swjg_mc"));
			returnMap.put("invoicefalseCode",
					SysConfig.INVOICEFALSESTATECODE220);
			returnMap.put("resultMsg",
					"查验失败：失败原因，超过该张发票的单日查验次数(5次），请于24小时之后再进行查验!");
			return toJson(returnMap);
		}
		//判断是不是有错误信息
		String invoicefalseState=invoiceFactory.getFalseToInvoiceInfo(parameter,dataList);
		if(StringUtils.isNotEmpty(invoicefalseState)){
			returnMap.put("resultMsg",
					SysConfig.getMap().get(invoicefalseState));
			returnMap.put("resultCode", SysConfig.CODE20011);
			returnMap.put("invoicefalseCode", invoicefalseState);
			returnMap.put("invoiceName", fpcyParas.get("swjg_mc"));
			return toJson(returnMap);
		}
		
		returnMap.remove("dateResultbean");
		// 调用查询接口查询
		TaxOfficeBean taxOfficeBean = new TaxOfficeBean();
		try {
			taxOfficeBean = (TaxOfficeBean) JavaBeanUtils.mapToObject(
					fpcyParas, TaxOfficeBean.class);
			int loop = 0;
			int loopCount = Integer.parseInt(PropertiesUtils
					.getPropertiesValue("FPCY_loopCount"));
			int loopSleep = Integer.parseInt(PropertiesUtils
					.getPropertiesValue("FPCY_loopSleep"));
			Map redisMap = new HashMap();
			redisMap.put("FPDM", fpdm);
			redisMap.put("FPHM", fphm);
			String redisValue = RedisUtils.getValue(className, methodName,
					new DataObject(redisMap));
			if (null != redisValue && !"key".equals(redisValue)) {
				returnMap = getReturnMap(redisValue);
				if ((SysConfig.CODE1000).equals(returnMap
						.get(SysConfig.CYJGSTATE))) {
					returnMap.put("invoiceName", fpcyParas.get("swjg_mc"));
					return toJson(returnMap);
				} else {
					RedisUtils.delValue(className, methodName, new DataObject(
							redisMap));
				}
			}else{
				// 发票正在查询中不能重复提交
				if(null !=redisValue && "key".equals(redisValue)){
					returnMap.put("resultMsg", "*您输入的发票正在查询中，请不要重复提交请求");
					returnMap.put("invoiceName", fpcyParas.get("swjg_mc"));
					returnMap.put("invoicefalseCode",
							SysConfig.INVOICEFALSESTATECODE221);
					returnMap.put("resultCode", SysConfig.CODE20011);
					return toJson(returnMap);
				}
			}
			DataObject obj = invoiceFactory.queryInvoiceInfo(parameter,
					taxOfficeBean);
			Map rtn = obj.getMap();
			if (rtn != null) {
				String yzm = String.valueOf(rtn.get("yzm"));
				if ("YES".equals(yzm)) {
					loopCount = 22;
				}
			}
			while (true) {
				String value = RedisUtils.getValue(className, methodName,
						new DataObject(redisMap));
				if (null != value && !"key".equals(value)) {
					returnMap = getReturnMap(value);
					break;
				} else if (loop == loopCount) {
					break;
				}
				loop++;
				Thread.sleep(loopSleep);
			}
			logger.debug("获取到结果数据:" + new DataObject(returnMap).getJson());
			 //判断是不是数据库有
			if(returnMap==null){
	             if(dbMap!=null){
	            	 dbMap.put("resultCode", SysConfig.CODE1000);
	            	 dbMap.put("invoiceName", fpcyParas.get("swjg_mc"));
	            	 dbMap.put("dataFrom", "dataBase");
	            	 dbMap.put("resultMsg", "查验结果成功");
	            	 dbMap.remove("dateResultbean");
	            	 dbMap.remove("list");
					 return toJson(dbMap);
	             }
			}
			//15s
			if (returnMap.isEmpty()) {
				returnMap.put("resultMsg", "服务忙，请稍候重试");
				returnMap.put("invoicefalseCode",
						SysConfig.INVOICEFALSESTATECODE213);
				returnMap.put("resultCode", SysConfig.CODE20011);
			}
		} catch (Exception e) {
			logger.error(fpcyParas.get("swjg_mc")+fpdm+"-"+fphm+"-"+ e.getMessage());
			logger.error(fpcyParas.get("swjg_mc")+"获取发票数据异常:");
			e.printStackTrace();
			returnMap.put("resultMsg", "服务忙，请稍候重试");
			returnMap.put("invoicefalseCode",
					SysConfig.INVOICEFALSESTATECODE213);
			returnMap.put("resultCode", SysConfig.CODE20011);
		}
		returnMap.put("invoiceName", fpcyParas.get("swjg_mc"));
		return toJson(returnMap);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Map getReturnMap(String str) throws ParseException,
			NoSuchElementException, JSONException {
		Map param = DataConvertUtil.StringToMap(str);
		String cyrq = String.valueOf(param.get("CYRQ"));
		String cyjgState = String.valueOf(param.get("cyjgState"));
		String invoiceFalseState = String.valueOf(param
				.get("invoicefalseState"));
		String systemFalseState = String.valueOf(param
				.get("systemfalseState"));
		Map resultMap = new HashMap();
		String cyjg = "";
		if (SysConfig.CODE1000.equals(cyjgState)) {
			cyjg = String.valueOf(param.get("CYJG"));
			resultMap.put("resultMsg", "查询发票信息成功");
		} else {
			cyjg = String.valueOf(param.get("RZXX"));
			if (!cyjg.isEmpty()) {
				JSONArray json = new JSONArray(cyjg);
				for (int i = 0; i < json.length(); i++) {
					JSONObject json1 = new JSONObject(json.get(i).toString());
					String name1 = json1.get("name1").toString();
					String value = json1.get("value").toString();
					if ("cwxx".equals(name1)) {
						resultMap.put("resultMsg", value);
						break;
					}
				}
			}
		}
		if (!cyjg.isEmpty()) {
			if ("list".equals(dataType)) {
				JSONArray json = new JSONArray(cyjg);
				List<ResultBean> list = new ArrayList<ResultBean>();
				for (int i = 0; i < json.length(); i++) {
					JSONObject json1 = new JSONObject(json.get(i).toString());
					String name1 = json1.get("name1").toString();
					String name2 = json1.get("name2").toString();
					String value = json1.get("value").toString();
					list.add(new ResultBean(name1, name2, value));
				}
				resultMap.put("list", list);
			} else if ("json".equals(dataType)) {
				String invoiceResult = (String) param.get("invoiceResult");
				resultMap.put("invoiceResult", invoiceResult);
			}
		}
		resultMap.put("resultCode", cyjgState);
		if (!"null".equals(invoiceFalseState) && !"".equals(invoiceFalseState)) {
			resultMap.put("invoicefalseCode", invoiceFalseState);
			if(!"null".equals(systemFalseState) && !"".equals(invoiceFalseState)){
				resultMap.put("systemfalseState", systemFalseState);
			}else{
				resultMap.put("systemfalseState", invoiceFalseState);
			}
		}
		resultMap.put("cyrq", cyrq);
		return resultMap;

	}

	@SuppressWarnings({ "rawtypes" })
	private static DataObject toJson(Map map) {
		return new DataObject(map);
	}
}
