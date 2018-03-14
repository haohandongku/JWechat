package com.dcits.fpcy.interfaces;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
 * 查询发票接口
 * 
 * 可以优化心得：这个类可以和企业接口合并，因为前期直接写业务没有仔细考虑重构问题，
 * 导致很多代码冗余,这些都可以放到Factory去实现
 * 
 * @author wuche
 * 
 */
public class QueryInvoiceInfo extends BaseService implements IService {
	private static Log logger = LogFactory.getLog(QueryInvoiceInfo.class);
	private static final String className = "commons.factory.InvoiceFactory";
	private static final String methodName = "queryInvoiceInfo";
	private static final String dataType="list";
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
		//传入数据
		Map parameter = dataobject.getMap();
		//传出数据
		Map returnMap = new HashMap();
		//数据库数据
		Map dbMap = new HashMap();
		//为云南国税和云南地税做的特殊字符转换做的处理
		if (parameter.containsKey("ifHasSpecialMark")){
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
		
		List<ResultBean> list = new ArrayList<ResultBean>();
		String FPDM = StringUtils.trim((String) parameter.get("FPDM"));
		String fpdm = StringUtils.trim((String) parameter.get("fpdm"));
		String FPHM = StringUtils.trim((String) parameter.get("FPHM"));
		String fphm = StringUtils.trim((String) parameter.get("fphm"));
		String kjje =(String) parameter.get("kjje");
		if(StringUtils.isNotEmpty(kjje)&&"null"!=kjje){
			kjje=ResultUtils.getDecimalFormat(kjje);
			parameter.put("kjje", kjje);
		}		
		fpdm=(fpdm!=null?fpdm:FPDM);
		fphm=(fphm!=null?fphm:FPHM);
		parameter.put("fpdm", fpdm);
		parameter.put("fphm", fphm);
		parameter.put("FPDM", fpdm);
		parameter.put("FPHM", fphm);
		parameter.put("dataType", dataType);
		String invoiceComeFrom = StringUtils.trim((String) parameter.get("invoiceComeFrom"));
		Map fpcyParas = new HashMap();
		String swjg_mc = "";
		try {
			fpcyParas = taxOfficeFactory.queryTaxOfficeByFpdm(fpdm, invoiceComeFrom);
		} catch (Exception e) {
			list.add(new ResultBean("cwxx", "", "您输入的发票暂时不支持查询"));
			returnMap.put("list", list);
			returnMap.put("cyjgState", SysConfig.CODE20011);
			returnMap.put("invoicefalseState",SysConfig.INVOICEFALSESTATECODE219);
			returnMap.put("swjg_mc", "未能获取");
			logger.error("获取税局异常:" + e.getMessage());
			return toJson(returnMap);
		}
		//用户需要输入的参数
		List dataList = new ArrayList();
		if (fpcyParas == null||fpcyParas.size()==0) {
			list.add(new ResultBean("cwxx", "", "您输入的发票暂时不支持查询"));
			returnMap.put("list", list);
			returnMap.put("cyjgState", SysConfig.CODE20011);
			returnMap.put("invoicefalseState",SysConfig.INVOICEFALSESTATECODE219);
			return toJson(returnMap);
		} else {
			swjg_mc = (String) fpcyParas.get("swjg_mc");
			returnMap.put("swjg_mc", swjg_mc);
			boolean flag = taxOfficeFactory.queryEnable(fpcyParas);
			if (flag || swjg_mc.isEmpty()) {
				list.add(new ResultBean("cwxx", "", "您输入的发票暂时不支持查询"));
				returnMap.put("list", list);
				returnMap.put("cyjgState", SysConfig.CODE20011);
				returnMap.put("invoicefalseState",SysConfig.INVOICEFALSESTATECODE219);
				return toJson(returnMap);
			}
			if(fpdm.length() == 12 && !fpdm.startsWith("0")){
				
			}else{
				dataList = taxOfficeFactory.getCheck(String.valueOf(fpcyParas.get("datamodel")));
				int size = dataList.size();
				if (0 < size) {
					for (int i = 0; i < size; i++) {
						String data = String.valueOf(parameter.get(dataList.get(i)));
						if (ResultUtils.isNull(data)) {
							list.add(new ResultBean("cwxx", "", "您输入的发票信息不完全"));
							returnMap.put("list", list);
							returnMap.put("cyjgState", SysConfig.CODE20011);
							returnMap.put("invoicefalseState", SysConfig.INVOICEFALSESTATECODE211);
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
				list.add(new ResultBean("cwxx", "", "如您查询的发票是当日开具的，请于次日查询！"));
				returnMap.put("list", list);
				returnMap.put("cyjgState", SysConfig.CODE20011);
				returnMap.put("invoicefalseState", SysConfig.INVOICEFALSESTATECODE216);
				return toJson(returnMap);
			} else if (!kprq.contains("-")) {
				list.add(new ResultBean("cwxx", "", "您输入的发票日期格式不正确，请你重新输入发票信息！（格式：2016-01-01）"));
				returnMap.put("list", list);
				returnMap.put("cyjgState", SysConfig.CODE20011);
				returnMap.put("invoicefalseState", SysConfig.INVOICEFALSESTATECODE203);
				return toJson(returnMap);
			}
		}
		// 获取需要查询数据【这个标志之后可以作为老数据重新入库的标志】
		String isSelectDb = PropertiesUtils.getPropertiesValue("isSelectDb");
		if ("YES".equals(isSelectDb)) {
			// 判断数据是否存在该条数据
			try {
				//暂时不从redis拿
				returnMap = invoiceFactory.queryInvoiceInfoFromDb(fpdm, fphm,dataType);
				dbMap.putAll(returnMap);
			} catch (Exception e) {
				logger.error("获取发票数据库异常:" + e);
			}
			if (null != returnMap && !returnMap.isEmpty()) {
				Map cycs = ResultUtils.mapStringToMap((String) returnMap.get("cycs"));
				if(!"4".equals(invoiceComeFrom)&&!"2".equals(invoiceComeFrom)){
					if (null != cycs ) {// 判断查验参数是否和数据库存储的一致
						int size = dataList.size();
						if (0 < size) {
							for (int i = 0; i < size; i++) {
								String data = String.valueOf(parameter.get(dataList.get(i)));
								String csStr=String.valueOf(cycs.get(dataList.get(i)));
								if("kjje".equals(dataList.get(i))){
									data=ResultUtils.getDecimalFormat(data);
									csStr=ResultUtils.getDecimalFormat(csStr);
								}
								if (!data.equals(csStr)) {
									list.add(new ResultBean("cwxx", "", "您输入的发票信息不一致"));
									returnMap.put("list", list);
									returnMap.put("cyjgState", SysConfig.CODE20011);
									returnMap.put("invoicefalseState", SysConfig.INVOICEFALSESTATECODE220);
									returnMap.put("swjg_mc", fpcyParas.get("swjg_mc"));
									return toJson(returnMap);
								}
							}
						}
					}
				}
				if (!swjg_mc.contains("增值税")) {
					returnMap.put("cyjgState", SysConfig.CODE1000);
					returnMap.put("swjg_mc", fpcyParas.get("swjg_mc"));
					returnMap.put("dataFrom", "dataBase");
					returnMap.remove("invoiceResult");
					return toJson(returnMap);
				} else {
					List list1 = (List) returnMap.get("list");
					ResultBean resultbean = null;
					for (int i = 0; i < list1.size(); i++) {
						resultbean = (ResultBean) list1.get(i);
						if (resultbean.getName2().equals("开票日期")) {
							break;
						}
					}
					// 没有日期票手撕发票（国税 定额）
					if (!"开票日期".equals(resultbean.getName2())) {
						returnMap.put("swjg_mc", fpcyParas.get("swjg_mc"));
						returnMap.put("cyjgState", SysConfig.CODE1000);
						returnMap.put("dataFrom", "dataBase");
						returnMap.remove("invoiceResult");
						return toJson(returnMap);
					}
					// 判断开票日期是否是当月
					String KPRQ = resultbean.getValue().toString().substring(0, 4) + "-"
							+ resultbean.getValue().toString().substring(5, 7);
					String db_kprq = resultbean.getValue().toString().substring(0, 10);
					String yearMonth = DateUtils.getYearMonth();
					// 判断是否为当月增值税票
					String ifDel = "N";
					//判断是不是当天票（当天票只去数据库拿）
					String  cyrq=(String) returnMap.get("cyrq");
					cyrq=cyrq.substring(0, 10);
					if (!KPRQ.equals(yearMonth) || cyrq.equals(date) || swjg_mc.contains("电子发票")) {
						//开票日期不等于当前月且查验日期月份与开票日期月份不相等
						if(!DateUtils.compare_monuth(cyrq,db_kprq)||swjg_mc.contains("电子发票")||  
								cyrq.equals(date)){
							returnMap.put("cyjgState", SysConfig.CODE1000);
							returnMap.put("swjg_mc", fpcyParas.get("swjg_mc"));
							returnMap.put("dataFrom", "dataBase");
							returnMap.remove("invoiceResult");
							return toJson(returnMap);
						}
					} else {
						ifDel = "Y";
					}
					parameter.put("ifDel", ifDel);
				}
			}
		}
		 //判断是否已经过了查票期 （增值税票）
		if(swjg_mc.contains("增值税")){
			boolean isOneYear=DateUtils.getIsBetweenOneYear(kprq);
			if(!isOneYear){
				list.add(new ResultBean("cwxx", "", "只可查验最近1年内开具的发票"));
				returnMap.put("list", list);
				returnMap.put("cyjgState", SysConfig.CODE20011);
				returnMap.put("invoicefalseState", SysConfig.INVOICEFALSESTATECODE217);
				returnMap.put("swjg_mc", fpcyParas.get("swjg_mc"));
				return toJson(returnMap);
			}
		}
		//判断这张票是否查询了超过五次
		if(invoiceFactory.getFalseInvoiceInfo(fpdm,fphm)){
			list.add(new ResultBean("cwxx", "", "查验失败：失败原因，超过该张发票的单日查验次数(5次），请于24小时之后再进行查验!"));
			returnMap.put("list", list);
			returnMap.put("cyjgState", SysConfig.CODE20011);
			returnMap.put("invoicefalseState", SysConfig.INVOICEFALSESTATECODE202);
			returnMap.put("swjg_mc", fpcyParas.get("swjg_mc"));
			return toJson(returnMap);
		}
		//判断是不是有错误信息
		String invoicefalseState=invoiceFactory.getFalseToInvoiceInfo(parameter,dataList);
		if(StringUtils.isNotEmpty(invoicefalseState)){
			list.add(new ResultBean("cwxx", "",SysConfig.getMap().get(invoicefalseState)));
			returnMap.put("list", list);
			returnMap.put("cyjgState", SysConfig.CODE20011);
			returnMap.put("invoicefalseState", invoicefalseState);
			returnMap.put("swjg_mc", fpcyParas.get("swjg_mc"));
			return toJson(returnMap);
		}
		// 调用查询接口查询(这里为啥转也是历史坑请注意)
		TaxOfficeBean taxOfficeBean = new TaxOfficeBean();
		try {
			taxOfficeBean = (TaxOfficeBean) JavaBeanUtils.mapToObject(fpcyParas, TaxOfficeBean.class);
			int loop = 0;
			int loopCount = Integer.parseInt(PropertiesUtils.getPropertiesValue("FPCY_loopCount"));
			int loopSleep = Integer.parseInt(PropertiesUtils.getPropertiesValue("FPCY_loopSleep"));
			Map redisMap = new HashMap();
			redisMap.put("FPDM", fpdm);
			redisMap.put("FPHM", fphm);
            //预防再次提交			
			String redisValue = RedisUtils.getValue(className, methodName, new DataObject(redisMap));
			if (null != redisValue && !"key".equals(redisValue)) {
				returnMap = getReturnMap(redisValue);
				if ((SysConfig.CODE1000).equals(returnMap.get(SysConfig.CYJGSTATE))) {
					returnMap.put("swjg_mc", fpcyParas.get("swjg_mc"));
					return toJson(returnMap);
				} else {
					RedisUtils.delValue(className, methodName, new DataObject(redisMap));
				}
			}
			DataObject obj = invoiceFactory.queryInvoiceInfo(parameter, taxOfficeBean);
			Map rtn = obj.getMap();
			if (rtn != null) {
				String yzm = String.valueOf(rtn.get("yzm"));
				if ("YES".equals(yzm)) {
					loopCount = 21;
				}
			}
			while (true) {
				String value = RedisUtils.getValue(className, methodName, new DataObject(redisMap));
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
	            	 dbMap.put("swjg_mc", fpcyParas.get("swjg_mc"));
	            	 dbMap.put("cyjgState", SysConfig.CODE1000);
	            	 dbMap.put("dataFrom", "dataBase");
	            	 dbMap.remove("invoiceResult");
					 return toJson(dbMap);
	             }
			}
			if (returnMap.isEmpty()) {
				List<ResultBean> rList = new ArrayList<ResultBean>();
				rList.add(new ResultBean("cwxx", "", "服务忙，请稍候重试！"));
				returnMap.put("list", rList);
				returnMap.put("invoicefalseState",SysConfig.INVOICEFALSESTATECODE213);
				returnMap.put("cyjgState", SysConfig.CODE20011);
				returnMap.put("swjg_mc", fpcyParas.get("swjg_mc"));
			}
		} catch (Exception e) {
			logger.error(fpcyParas.get("swjg_mc")+fpdm+fphm+"获取发票数据异常:");
			e.printStackTrace();
			List<ResultBean> rList = new ArrayList<ResultBean>();
			rList.add(new ResultBean("cwxx", "", "服务忙，请稍候重试！"));
			returnMap.put("list", rList);
			returnMap.put("invoicefalseState", SysConfig.INVOICEFALSESTATECODE213);
			returnMap.put("cyjgState", SysConfig.CODE20011);
			returnMap.put("swjg_mc", fpcyParas.get("swjg_mc"));
		}
		returnMap.put("swjg_mc", fpcyParas.get("swjg_mc"));
		return toJson(returnMap);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Map getReturnMap(String str) throws ParseException, NoSuchElementException, JSONException {
		Map param = DataConvertUtil.StringToMap(str);
		Map resultMap = new HashMap();
		String cyrq = String.valueOf(param.get("CYRQ"));
		String cyjgState = String.valueOf(param.get("cyjgState"));
		String invoiceFalseState = String.valueOf(param.get("invoicefalseState"));
		String systemFalseState = String.valueOf(param.get("systemfalseState"));
		
		String cyjg = "";
		if (SysConfig.CODE1000.equals(cyjgState)) {
			cyjg = String.valueOf(param.get("CYJG"));
		} else {
			cyjg = String.valueOf(param.get("RZXX"));
		}
		if("list".equals(dataType)){
			if (!cyjg.isEmpty()) {
				JSONArray json = new JSONArray(cyjg);
				List<ResultBean> list = new ArrayList<ResultBean>();
				for (int i = 0; i < json.length(); i++) {
					JSONObject json1 = new JSONObject(json.get(i).toString());
					String name1 = json1.get("name1").toString();
					String name2 = json1.get("name2").toString();
					String value = json1.get("value").toString();
					//解析成有序数据
					if(!ResultUtils.isNull(name2)){
						if(name2.startsWith("jsonhw")){
							value=getOrderByObject(value);
						}
					}
					
					list.add(new ResultBean(name1, name2, value));
				}
				resultMap.put("list", list);
			}
		}
		resultMap.put("cyjgState", cyjgState);
		if (!"null".equals(invoiceFalseState) && !"".equals(invoiceFalseState)) {
			resultMap.put("invoicefalseState", invoiceFalseState);
			if(!"null".equals(systemFalseState) && !"".equals(systemFalseState)){
				resultMap.put("systemfalseState", systemFalseState);
			}else{
				resultMap.put("systemfalseState", invoiceFalseState);
			}
		}
		resultMap.put("cyrq", cyrq);
		return resultMap;

	}
	/**
	 * 排序
	 * @param str
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static  String  getOrderByObject(String str){
			Map map=DataConvertUtil.StringToMap(str);
			Map retmap = new LinkedHashMap();
			if(map!=null){
				if(map.containsKey("货物或应税劳务名称")){
					retmap.put("货物或应税劳务名称", map.get("货物或应税劳务名称"));
				}else{
					retmap.put("货物或应税劳务名称","");
				}
				if(map.containsKey("单位")){
					retmap.put("单位", map.get("单位"));
				}else{
					retmap.put("单位", "");
				}
				if(map.containsKey("规格型号")){
					retmap.put("规格型号", map.get("规格型号"));
				}else{
					retmap.put("规格型号", "");
				}
				if(map.containsKey("数量")){
					retmap.put("数量", map.get("数量"));
				}else{
					retmap.put("数量", "");
				}
				if(map.containsKey("单价")){
					retmap.put("单价", map.get("单价"));
				}else{
					retmap.put("单价", "");
				}
				if(map.containsKey("金额")){
					retmap.put("金额", map.get("金额"));
				}else{
					retmap.put("金额", "");
				}
				if(map.containsKey("税率")){
					retmap.put("税率", map.get("税率"));
				}else{
					retmap.put("税率", "");
				}
				if(map.containsKey("税额")){
					retmap.put("税额", map.get("税额"));
				}else{
					retmap.put("税额", "");
				}
				str=DataConvertUtil.MapToString(retmap);
			}
			
		return str;
	}
	@SuppressWarnings({ "rawtypes" })
	private static DataObject toJson(Map map) {
		return new DataObject(map);
	}
}
