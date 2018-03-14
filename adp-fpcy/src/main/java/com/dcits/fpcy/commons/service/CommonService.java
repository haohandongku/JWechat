package com.dcits.fpcy.commons.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.util.CollectionUtils;

import com.dcits.app.data.DataObject;
import com.dcits.app.service.BaseService;
import com.dcits.app.util.JacksonUtils;
import com.dcits.app.util.RedisUtils;
import com.dcits.fpcy.commons.dao.TaxOfficeDao;
import com.dcits.fpcy.commons.utils.PropertiesUtils;

/**
 * 1.初始化所有税局配置信息【redis缓存】 (dataObject(swjg_dm,fp_zl)) 2.统计队列中缓存各个地区验证码个数【应用缓存】
 * 3.转老数据需要参数【应用缓存】-->历史业务改版留下的坑 4.缓存解析数据规则【redis缓存】-->解析税局发票结果
 * (建议那种不变数据，应放到缓存中去，减少对象创建)
 * 
 * @author wuche
 * 
 */
public class CommonService extends BaseService {
	private static Log logger = LogFactory.getLog(CommonService.class);
	private static final String className = "commons.service.CommonService";
	private static final String methodName = "putTaxOfficeList";
	private static final String methodName2 = "putTORoleList";
	// 这个是解析发票数据结构
	private static Map<String, Map<String, String>> initInvoiceMap;
	// 队列中验证码现在有的个数
	private static Map<String, Integer> initSwjgDmCacheMap;
	// 其他缓存数据
	@SuppressWarnings("rawtypes")
	private static Map dmTableData;

	private TaxOfficeDao taxOfficeDao;

	public void setTaxOfficeDao(TaxOfficeDao taxOfficeDao) {
		this.taxOfficeDao = taxOfficeDao;
	}

	public static Map<String, Map<String, String>> getInitInvoiceMap() {
		return initInvoiceMap;
	}

	public static Map<String, Integer> initSwjgDmCacheMap() {
		return CommonService.initSwjgDmCacheMap;
	}

	public static int getNum(String swjg_dm) {
		int num = 0;
		Object numm = initSwjgDmCacheMap.get("cache_" + swjg_dm);
		if (numm instanceof Integer) {
			num = (Integer) numm;
		}
		return num;
	}

	public static void setNum(String swjg_dm, int num) {
		initSwjgDmCacheMap.put("cache_" + swjg_dm, num);
	}

	/**
	 * scriptEngine(引擎) 放到缓存中便于快速解析js文件
	 * 
	 * @param map
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void putScriptEngine(Map map) {
		dmTableData.put("scriptEngine", map);
	}

	@SuppressWarnings("rawtypes")
	public static Map getScriptEngine() {
		Map map = new HashMap();
		Object obj = dmTableData.get("scriptEngine");
		if (obj instanceof Map) {
			map = (Map) obj;
		}
		return map;
	}

	public static void setInitInvoiceMap(
			Map<String, Map<String, String>> initInvoiceMap) {
		CommonService.initInvoiceMap = initInvoiceMap;
	}

	// 方法一:将税局信息初始化到redis中
	@SuppressWarnings({ "rawtypes"})
	public void init() throws JsonGenerationException, JsonMappingException,
			IOException {
		// 初始化税局信息
		intoTaxOffice();
		// 初始化配置文件信息
		PropertiesUtils.init();
		dmTableData = new HashMap();
		// 初始化税务总局发票规则
		intoTaxOfficeRole();
	}

	/**
	 * 
	 * 同步税局相关datamodel信息
	 * 
	 * @param dataObject
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void intoTaxOffice() {
		try {
			// 获取所有数据信息
			List taxOfficeList = taxOfficeDao.getTaxOfficeList();
			if (!CollectionUtils.isEmpty(taxOfficeList)) {
				for (int i=0;i<taxOfficeList.size();i++) {
					Map taxOfficeMap = (Map) taxOfficeList.get(i);
					if (taxOfficeMap != null) {
						Integer swjg_dm = (Integer) taxOfficeMap.get("swjg_dm");
						String fp_zl = (String) taxOfficeMap.get("fp_zl");
						Map map = new HashMap();
						map.put("swjg_dm", swjg_dm.toString());
						int num = 0;
						initSwjgDmCacheMap = new HashMap();
						initSwjgDmCacheMap.put("cache_" + swjg_dm, num);
						map.put("fp_zl", fp_zl);
						String result = JacksonUtils
								.getJsonFromMap(taxOfficeMap);
						RedisUtils.putValue(className, methodName,
								new DataObject(map), result);
					}
				}
			}
		} catch (Exception e) {
			logger.error("初始化税局datamodel报错------");
		}
	}
    
	
	/**
	 * 
	 * 同步税局相关datamodel信息
	 * 
	 * @param dataObject
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void intoTaxOfficeRole() {
		try {
			// 获取所有数据信息
			List taxOfficeRoleList = taxOfficeDao.queryAllReg();
			if (!CollectionUtils.isEmpty(taxOfficeRoleList)) {
				for (int i=0;i<taxOfficeRoleList.size();i++) {
					Map taxOfficeRoleMap = (Map) taxOfficeRoleList.get(i);
					if (taxOfficeRoleMap != null) {
						String swjg_mc=(String) taxOfficeRoleMap.get("swjg_mc");
						Map map = new HashMap();
						map.put("swjg_mc",swjg_mc);
						String result = JacksonUtils
								.getJsonFromMap(taxOfficeRoleMap);
						RedisUtils.putValue(className, methodName2,
								new DataObject(map), result);
					}
				}
			}
		} catch (Exception e) {
			logger.error("初始化税局规则报错------");
		}
	}
	/**
	 * 根据swjg_dm,fp_zl,同步税局信息(后台管理页面刷，暂时没有使用)
	 * 
	 * @param dataObject
	 * @return
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public DataObject refreshTaxOffice(DataObject dataObject){
		try {
			Map paramter = dataObject.getMap();
			Map taxOfficeMap = taxOfficeDao.getTaxOfficeOne(paramter);
			if (taxOfficeMap != null) {
				Integer swjg_dm = (Integer) taxOfficeMap.get("swjg_dm");
				String fp_zl = (String) taxOfficeMap.get("fp_zl");
				Map map = new HashMap();
				map.put("swjg_dm", swjg_dm);
				map.put("fp_zl", fp_zl);
				String result = JacksonUtils.getJsonFromMap(taxOfficeMap);
				RedisUtils.putValue(className, methodName, new DataObject(map),
						result);
			}
		}catch (Exception e) {
			logger.error("缓存到redis税局datamodel报错------");
		}
		return new DataObject();
	}
    
	/**
	 * 通过程序去同步该地区信息
	 * 
	 * @param dataObject
	 * @return
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@SuppressWarnings({ "rawtypes" })
	public static void refreshTaxOffice2(Map attrMap, Map taxOfficeMap) {
		try {
			if (taxOfficeMap != null) {
				String result = JacksonUtils.getJsonFromMap(taxOfficeMap);
				RedisUtils.putValue(className, methodName, new DataObject(
						attrMap), result);
			}
		} catch (Exception e) {
			logger.error("缓存到redis税局datamodel报错------");
		}
	}
	
	/**
	 * 通过程序去同步该地区信息
	 * 
	 * @param dataObject
	 * @return
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@SuppressWarnings({ "rawtypes" })
	public static void refreshTaxOfficeRole(Map attrMap, Map taxOfficeMap) {
		try {
			if (taxOfficeMap != null) {
				String result = JacksonUtils.getJsonFromMap(taxOfficeMap);
				RedisUtils.putValue(className, methodName2, new DataObject(
						attrMap), result);
			}
		} catch (Exception e) {
			logger.error("缓存到redis税局规则报错------");
		}
	}
}
