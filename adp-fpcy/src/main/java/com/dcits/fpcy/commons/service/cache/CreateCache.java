package com.dcits.fpcy.commons.service.cache;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import com.dcits.app.asynctask.AsyncTaskService;
import com.dcits.app.asynctask.DynamicAsyncTaskService;
import com.dcits.app.data.DataObject;
import com.dcits.app.util.RedisUtils;
import com.dcits.fpcy.commons.bean.CacheEntity;
import com.dcits.fpcy.commons.bean.HSCEntity;
import com.dcits.fpcy.commons.bean.TaxOfficeBean;
import com.dcits.fpcy.commons.constant.SysConfig;
import com.dcits.fpcy.commons.factory.YzmFactory;
import com.dcits.fpcy.commons.service.CommonService;
import com.dcits.fpcy.commons.utils.DateUtils;
import com.dcits.fpcy.commons.utils.GsonUtils;
import com.dcits.fpcy.commons.utils.PropertiesUtils;
import com.dcits.fpcy.commons.utils.RedisUtil;

public class CreateCache {
	private static final String className = "commons.service.cache";
	private static final String methodName = "AsynBuildCacheObj";
	private static Log logger = LogFactory.getLog(CreateCache.class);
	//税务局增值税
	private static CacheEntity cacheEntity;
	//税务局国地税
	private static HSCEntity hscEntity;
	//最大缓存验证码个数
	private static int MAX_CACHE=4;
	private static YzmFactory yzmFactory = new YzmFactory();
	/**
	 * 增值税创建验证码
	 * @param fpdm
	 * @param invoiceComeFrom
	 * @param dataobject
	 * @param taxOfficeBean
	 */
	@SuppressWarnings("rawtypes")
	public CreateCache(final String fpdm, final String invoiceComeFrom,
			final Map dataobject, final TaxOfficeBean taxOfficeBean) {
		//校验是否需要缓冲
		if(checkIsNeedCache(taxOfficeBean.swjg_dm)){
			DynamicAsyncTaskService.getInstance().execute(new Runnable() {
				@Override
				public void run() {
					int num=(Integer) CommonService.getNum(taxOfficeBean.swjg_dm);
					num++;
					CommonService.setNum(taxOfficeBean.swjg_dm, num);
					cacheEntity = yzmFactory.getGsYzm(fpdm,
							dataobject.get("invoiceComeFrom").toString(),
							taxOfficeBean,(String)dataobject.get("requestId"));
					logger.debug("创建新的验证码开始.......");
					if(cacheEntity!=null){
						String errorCode=cacheEntity.getErrorCode();
						if(SysConfig.INVOICETRUESTATE000.equals(errorCode)){
							logger.debug(taxOfficeBean.swjg_mc+"创建新的验证码为......." + cacheEntity.getYzm());
							AsynBuildCacheObj(cacheEntity, taxOfficeBean.swjg_dm);
							logger.debug("创建新的验证码结束.......");
						}
					}
					num=0;
					CommonService.setNum(taxOfficeBean.swjg_dm, num);
				}
			},2);
		}
	}

	/**
	 * 地税和国税
	 * 
	 * @param fpcyParas
	 */
	public CreateCache(final String fpdm, final String invoiceComeFrom,final TaxOfficeBean fpcyParas) {
		//校验是否需要缓冲
	    if(checkIsNeedCache(fpcyParas.swjg_dm)){
			AsyncTaskService.getInstance().execute(new Runnable() {
				@Override
				public void run() {
					int num=(Integer) CommonService.getNum(fpcyParas.swjg_dm);
					num++;
					CommonService.setNum(fpcyParas.swjg_dm, num);
					hscEntity  =yzmFactory.getGsOrDsYzm(fpdm, invoiceComeFrom, fpcyParas);
					logger.debug("创建新的验证码开始.......");
					if(hscEntity!=null){
					   AsynBuildCacheObjToGsOrDs(fpcyParas,fpcyParas.swjg_dm);
					}  
					num--;
					CommonService.setNum(fpcyParas.swjg_dm, num);
				}
			});
	    }
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void AsynBuildCacheObj(CacheEntity cacheEntity, String swjg_dm) {
		Map jedisMap = new HashMap();
		String key = getKey();
		jedisMap.put("key", key);
		int surviveTime = Integer.parseInt(PropertiesUtils
				.getPropertiesValue("FPCY_stateTime"));
		logger.debug("缓存中放的验证码为:" + cacheEntity.toJsonString());
		RedisUtils.putValue(className, methodName, new DataObject(jedisMap),
				cacheEntity.toJsonString(), surviveTime);
		RedisUtil.rpush(swjg_dm, key);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void AsynBuildCacheObjToGsOrDs(final TaxOfficeBean fpcyParas, String swjg_dm) {
		Map jedisMap = new HashMap();
		String key = getKey();
		jedisMap.put("key", key);
		int surviveTime = Integer.parseInt(PropertiesUtils
				.getPropertiesValue("FPCY_stateTime"));
		logger.debug("缓存中放国税和地税的验证码为:" + hscEntity.getYzm());
		//将hscEntity转换成
		// 将对象编译成json  
		String hscEntityJson= GsonUtils.tojson(hscEntity);
		RedisUtils.putValue(className, methodName, new DataObject(jedisMap),
				hscEntityJson, surviveTime);
		RedisUtil.rpush(swjg_dm, key);
	}
	
	
	private static String getKey() {
		String key = UUID.randomUUID().toString() + "%"
				+ System.currentTimeMillis();
		return key;
	}
     
	/**
	 * 该判断现在可以从redis已经缓存个数，正在缓存的队列判断
	 * 之后可以加入某些地区，某些时间段等来减少缓存验证码，来控制成本
	 * @param swjg_dm
	 * @return
	 */
	private  boolean checkIsNeedCache(String swjg_dm){
		boolean isNeedCache=true;
		//判断已经缓存的个数（>2不缓存）
		long length = RedisUtil.llen(swjg_dm);
		logger.debug(swjg_dm+"缓存中验证码个数："+length);
		if(length>=MAX_CACHE){
			isNeedCache=false;
			return isNeedCache;
		}
		//判断正在队列中验证码个数（单台机器正在缓存>1不缓存）
		int num=(Integer) CommonService.getNum(swjg_dm);
		logger.debug("第二次从缓存中---"+swjg_dm+"正在缓存中验证码个数："+num);
		if(num>=1){
			isNeedCache=false;
			return isNeedCache;
		}
		//对地区进行缓存（可以暂时不做）
		//对于时间段存储（夜晚11:00--早晨5:00不缓存）
		String startTime = PropertiesUtils.getPropertiesValue("FPCY_CACHE_STATETIME");
		String endTime = PropertiesUtils.getPropertiesValue("FPCY_CACHE_ENDTIME");
		try {
			isNeedCache=DateUtils.isInTime(startTime, endTime);
		} catch (ParseException e) {
			logger.error("比较时间错误"+e.getMessage());
		}
		return isNeedCache;
	}
}
