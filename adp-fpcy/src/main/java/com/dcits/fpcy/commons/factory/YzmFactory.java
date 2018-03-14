package com.dcits.fpcy.commons.factory;

import java.util.HashMap;
import java.util.Map;


import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import com.dcits.app.data.DataObject;
import com.dcits.app.util.JacksonUtils;
import com.dcits.app.util.RedisUtils;
import com.dcits.fpcy.commons.bean.CacheEntity;
import com.dcits.fpcy.commons.bean.HSCEntity;
import com.dcits.fpcy.commons.bean.TaxOfficeBean;
import com.dcits.fpcy.commons.constant.SysConfig;
import com.dcits.fpcy.commons.dao.TaxOfficeDao;
import com.dcits.fpcy.commons.service.core.YzmApi;
import com.dcits.fpcy.commons.service.cache.CreateCache;
import com.dcits.fpcy.commons.utils.GsonUtils;
import com.dcits.fpcy.commons.utils.JavaBeanUtils;
import com.dcits.fpcy.commons.utils.RedisUtil;

/**
 * 对应--原始代码中GetYZM(方法都考到这里)
 * 
 * @author wuche
 * 
 */
public class YzmFactory {
	private static final String className = "commons.service.cache";
	private static final String methodName = "AsynBuildCacheObj";
	private static Log logger = LogFactory.getLog(YzmFactory.class);
	@SuppressWarnings("unused")
	private TaxOfficeDao taxOfficeDao;

	public void setTaxOfficeDao(TaxOfficeDao taxOfficeDao) {
		this.taxOfficeDao = taxOfficeDao;
	}

	/**
	 * 组装生成国税总局发票验证码
	 * 
	 * @param fpdm
	 * @param fpcyParas
	 * @return
	 */
	public CacheEntity getGsYzm(String fpdm, String invoiceComeFrom,
			TaxOfficeBean fpcyParas,String requestId) {
		CacheEntity cacheEntity = new CacheEntity();
		if (fpcyParas.swjg_mc.contains("增值税")) {
			int countNum=0;
			cacheEntity = YzmApi.queryGsYzm(fpdm, fpcyParas,++countNum,requestId);
			String errorCode=cacheEntity.getErrorCode();
			if(SysConfig.INVOICEFALSESTATECODE105.equals(errorCode)||SysConfig.INVOICEFALSESTATECODE106.equals(errorCode)){
				cacheEntity = YzmApi.queryGsYzm(fpdm, fpcyParas,countNum,requestId);
				String errorCode1=cacheEntity.getErrorCode();
				if(SysConfig.INVOICEFALSESTATECODE105.equals(errorCode1)||SysConfig.INVOICEFALSESTATECODE106.equals(errorCode)){
					cacheEntity = YzmApi.queryGsYzm(fpdm, fpcyParas,countNum,requestId);
					String errorCode2=cacheEntity.getErrorCode();
					if(SysConfig.INVOICEFALSESTATECODE105.equals(errorCode2)){
					   cacheEntity.setErrorCode(SysConfig.INVOICEFALSESTATECODE230);
					}else if(SysConfig.INVOICEFALSESTATECODE106.equals(errorCode)){
						 cacheEntity.setErrorCode(SysConfig.INVOICEFALSESTATECODE232);
					}
				}
			}
			
		}
		return cacheEntity;
	}

	/**
	 * 组装国税和地税验证码对象
	 * 
	 * @param fpdm
	 * @param fpcyParas
	 * @return
	 */
	public HSCEntity getGsOrDsYzm(String fpdm, String invoiceComeFrom,
			TaxOfficeBean fpcyParas) {
		HSCEntity hscEntity = new HSCEntity();
		hscEntity = YzmApi.queryGsOrDsYzm(fpdm, fpcyParas);
		return hscEntity;
	}

	/**
	 * 获取、缓存验证码
	 * 
	 * @param dataobject
	 * @param taxOfficeBean
	 */
	@SuppressWarnings({"unchecked", "rawtypes" })
	public void getYzmObject(Map dataobject, TaxOfficeBean taxOfficeBean) {
		CacheEntity cacheEntity = null;
		HSCEntity hscEntity = null;
		String fpdm = dataobject.get("fpdm").toString();
		if (taxOfficeBean.swjg_mc.contains("增值税")) {
			try {
				cacheEntity = getRedisYzm(dataobject,taxOfficeBean,false);
				if (cacheEntity == null) {
					cacheEntity = getGsYzm(fpdm,
							dataobject.get("invoiceComeFrom").toString(),
							taxOfficeBean,(String)dataobject.get("requestId"));
				}
				dataobject.put("cacheEntity", cacheEntity);
			} catch (Exception e) {
				logger.error("---"+fpdm+"获取增值税验证码出错："+e.getMessage());
			} finally {
				// 创建缓存
				 new CreateCache(fpdm, dataobject.get(
						"invoiceComeFrom").toString(), dataobject,
						taxOfficeBean);
			}
			// 国税和地税
		} else {
			try {
				hscEntity = getRedisDsYzm(taxOfficeBean.swjg_dm);
				if (hscEntity == null) {
					hscEntity = getGsOrDsYzm(fpdm,
							dataobject.get("invoiceComeFrom").toString(),
							taxOfficeBean);
				}
				dataobject.put("hscEntity", hscEntity);
			} catch (Exception e) {
				logger.error("获取地国增值税验证码出错："+e.getMessage());
			} finally {
				// 创建缓存
				 new CreateCache(fpdm, dataobject.get(
						"invoiceComeFrom").toString(), taxOfficeBean);
			}

		}
	}
    /**
     * 获取redis中的验证码数据
     * @param dataobject
     * @param taxOfficeBean
     * @param isCache  是否需要缓存
     * @return
     */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public  CacheEntity getRedisYzm(Map dataobject, TaxOfficeBean taxOfficeBean,boolean isCache) {
		CacheEntity cacheEntity = null;
		String fpdm = dataobject.get("fpdm").toString();
		String res = getAndDelQueueFirstElement(taxOfficeBean.swjg_dm);
		if (res != null) {
			try {
				cacheEntity = (CacheEntity) JavaBeanUtils.mapToObject(
						JacksonUtils.getMapFromJson(res), CacheEntity.class);
			} catch (Exception e) {
				logger.error("获取Redis验证码出错：解析异常"+e.getMessage());
			}finally{
				if(isCache){
					// 创建缓存
					 new CreateCache(fpdm, dataobject.get(
							"invoiceComeFrom").toString(), dataobject,
							taxOfficeBean);
				}
			}
		}
		return cacheEntity;
	}

	public  HSCEntity getRedisDsYzm(String swjg_dm) {
		HSCEntity hscEntity = null;
		String res = getAndDelQueueFirstElement(swjg_dm);
		if (res != null) {
			try {
				hscEntity = (HSCEntity)GsonUtils.toObject(res, HSCEntity.class);
			} catch (Exception e) {
				logger.error("获取Redis地税验证码出错"+e.getMessage());
			}
		}
		return hscEntity;
	}

	/**
	 * 获取队首元素对应的值并删除队首元素
	 * 
	 * @param swjg_dm
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static String getAndDelQueueFirstElement(String swjg_dm) {
		Map jedisMap = new HashMap();
		long length = RedisUtil.llen(swjg_dm);
		logger.debug("第一次从缓存中---"+swjg_dm+"缓存中验证码个数："+length);
		String value = null;
		int i = 0;
		if (0 != length) {
			while (value == null && i < length) {
				String key = RedisUtil.lpop(swjg_dm);
				if(key.contains("%")){
					String[] str = key.split("%");
					long time = Long.parseLong(str[1]);
					//TODO 需要修改这里时间
					if ((System.currentTimeMillis() - time) <= 150000) {
						jedisMap.put("key", key);
						value = RedisUtils.getValue(className, methodName,
								new DataObject(jedisMap));
						RedisUtils.delValue(className, methodName, new DataObject(
								jedisMap));
					}
				}
				i++;
			}
		}
		if (StringUtils.isNotBlank(value)) {
			return value;
		} else {
			return null;
		}
	}

}
