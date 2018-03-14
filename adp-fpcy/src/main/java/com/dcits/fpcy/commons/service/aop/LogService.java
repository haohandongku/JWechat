package com.dcits.fpcy.commons.service.aop;

import java.util.HashMap;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.StringUtils;

import com.dcits.app.data.DataObject;
import com.dcits.app.service.BaseService;
import com.dcits.fpcy.commons.constant.DmSequenceName;
import com.dcits.fpcy.commons.dao.InvoiceDao;
import com.dcits.fpcy.commons.utils.PropertiesUtils;

public class LogService extends BaseService implements MethodInterceptor {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	//TODO 增加企业接口切面日志
	public Object invoke(MethodInvocation method) throws Throwable {
		long begin = System.currentTimeMillis();
		final DataObject result = (DataObject) method.proceed();
		long end = System.currentTimeMillis();
		final Map resultMap = result.getMap();
		try {
			String methodName = method.getMethod().getName();
			final DataObject argument = (DataObject) method.getArguments()[0];
			final Map argumentMap = argument.getMap();
			if ("doService".equals(methodName)) {
				resultMap.remove("cacheEntity");
				resultMap.remove("key1");
				argumentMap.remove("cacheEntity");
				argumentMap.remove("key1");
				if(resultMap != null && resultMap.get("cyjgState") != null){
					Map requestMap = new HashMap();
					requestMap.put("parameter", new DataObject(argumentMap).getJson());
					requestMap.put("result", new DataObject(resultMap).getJson());
					requestMap.put("requestTime", (end-begin));
					requestMap.put("expectTime", PropertiesUtils.getPropertiesValue("FPCY_loopSleep"));
					requestMap.put("requestId", getSequence(DmSequenceName.REQUESTID));
					requestMap.put("comeFromCode", argumentMap.get("invoiceComeFrom"));
					requestMap.put("fphm", argumentMap.get("fphm"));
					requestMap.put("fpdm", argumentMap.get("fpdm"));
					requestMap.put("invoiceType", resultMap.get("swjg_mc"));
					String requestStatus = null;
					if("1000".equals(resultMap.get("cyjgState").toString())){
						String dataFrom = String.valueOf(resultMap.get("dataFrom"));
						if("dataBase".equals(dataFrom)){
							requestStatus = "001";//数据库查询
						}else{
							requestStatus = "002";//税局接口查询
						}
					}else{
						String falseState = String.valueOf(resultMap.get("invoicefalseState"));
						String systemfalseState = String.valueOf(resultMap.get("systemfalseState"));
						if(!"".equals(systemfalseState)&&!"null".equals(systemfalseState)){
							requestStatus = systemfalseState;
						}else{
							if(StringUtils.isEmpty(falseState)&&"null".equals(systemfalseState)){
								requestStatus = "1";//系统查询失败
							}else{
								requestStatus = falseState;//系统查询失败
							}
							
						}
					}
					requestMap.put("requestStatus", requestStatus);
					InvoiceDao.saveRequestRecord(new DataObject(requestMap));
				}else if(resultMap != null && resultMap.get("resultCode") != null){
					Map requestMap = new HashMap();
					requestMap.put("parameter", new DataObject(argumentMap).getJson());
					requestMap.put("result", new DataObject(resultMap).getJson());
					requestMap.put("requestTime", (end-begin));
					requestMap.put("expectTime", PropertiesUtils.getPropertiesValue("FPCY_loopSleep"));
					requestMap.put("requestId", getSequence(DmSequenceName.REQUESTID));
					requestMap.put("comeFromCode", argumentMap.get("invoiceComeFrom"));
					requestMap.put("fphm", argumentMap.get("fphm"));
					requestMap.put("fpdm", argumentMap.get("fpdm"));
					requestMap.put("invoiceType", resultMap.get("invoiceName"));
					String requestStatus = null;
					if("1000".equals(resultMap.get("resultCode").toString())){
						String dataFrom = String.valueOf(resultMap.get("dataFrom"));
						if("dataBase".equals(dataFrom)){
							requestStatus = "001";//数据库查询
						}else{
							requestStatus = "002";//税局接口查询
						}
					}else{
						String falseState = String.valueOf(resultMap.get("invoicefalseCode"));
						String systemfalseState = String.valueOf(resultMap.get("systemfalseState"));
						if(!"".equals(systemfalseState)&&!"null".equals(systemfalseState)){
							requestStatus = systemfalseState;
						}else{
							if(StringUtils.isEmpty(falseState)&&"null".equals(systemfalseState)){
								requestStatus = "1";//系统查询失败
							}else{
								requestStatus = falseState;//系统查询失败
							}
							
						}
					}
					requestMap.put("requestStatus", requestStatus);
					InvoiceDao.saveRequestRecord(new DataObject(requestMap));
				}
			}
		} catch (Throwable t) {
			LOG.error("发票日志记录日志出现异常", t);
		}
		return result;
	}
}
