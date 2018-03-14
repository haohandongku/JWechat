package com.dcits.fpcy.commons.utils.thirdApi;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baiwang.bop.client.BopException;
import com.baiwang.bop.client.IBopClient;
import com.baiwang.bop.client.ILoginClient;
import com.baiwang.bop.client.impl.BopRestClient;
import com.baiwang.bop.client.impl.PostLogin;
import com.baiwang.bop.request.impl.LoginRequest;
import com.baiwang.bop.request.impl.input.CollectRequest;
import com.baiwang.bop.respose.entity.LoginResponse;
import com.baiwang.bop.respose.entity.input.CollectResponse;
import com.baiwang.bop.utils.JacksonUtil;
import com.dcits.app.data.DataObject;
import com.dcits.app.util.JacksonUtils;
import com.dcits.app.util.RedisUtils;
import com.dcits.fpcy.commons.dao.InvoiceDao;
import com.dcits.fpcy.commons.utils.DataConvertUtil;
import com.dcits.fpcy.commons.utils.PropertiesUtils;

public class BaiWangApi {
	private static String url = PropertiesUtils
			.getPropertiesValue("baiwang_api_url");
	private static String appKey = PropertiesUtils
			.getPropertiesValue("baiwang_appKey");
	private static String appSecret = PropertiesUtils
			.getPropertiesValue("baiwang_appSecret");
	private static String username = PropertiesUtils
			.getPropertiesValue("baiwang_username");
	private static String password = PropertiesUtils
			.getPropertiesValue("baiwang_password");
	private static String UUID = PropertiesUtils
			.getPropertiesValue("baiwang_UUID");
	private static String className = "fpcy_BaiWangApi";
	private static Log logger = LogFactory.getLog(BaiWangApi.class);
	private static long accessTokenExpiresTime = 3600;
    
	/**
	 * 获取发票信息
	 * @param paramMap
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map getInvoice(Map paramMap) {
		Map resultMap=new HashMap();
		String fplx = (String) paramMap.get("fplx");
		String fpdm=(String) paramMap.get("fpdm");
		String fphm=(String) paramMap.get("fphm");
		String kprq=(String) paramMap.get("kprq");
		String fpje=(String) paramMap.get("fpje");
		String resultCode="";
		String resultMsg="";
		String isSuccess="N";
		String token=getToken();
		if(StringUtils.isEmpty(token)){
			resultCode="-1";
			resultMsg="token调用失败";
			return resultMap;
		}
		long startTime = System.currentTimeMillis();
		IBopClient client = new BopRestClient(url, appKey, appSecret);
		CollectRequest request = new CollectRequest();
		request.setVersion("1.0");
		request.setTaxCheck("0");
		request.setInvoiceCode(fpdm);
		request.setInvoiceNumber(fphm);
		request.setBillingDate(kprq);
		if ("01".equals(fplx) || "03".equals(fplx)) {
			request.setTotalAmount(fpje);
		} else {
			request.setCheckCode(fpje);
		}
		try {
			  CollectResponse response = client.execute(request,token,CollectResponse.class);  
			  resultCode="0";
			  resultMsg=JacksonUtil.beanToString(response);
			  logger.error(resultMsg);
			  isSuccess="Y";
		} catch (BopException e) {
			resultCode=e.getSubCode();
			resultMsg=e.getSubMessage();
			if(StringUtils.isEmpty(resultCode)){
				resultCode=e.getErrCode();
				resultMsg=e.getErrMsg();
			}
			isSuccess="Y";
		}
		long endTime = System.currentTimeMillis();
		resultMap.put("resultCode", resultCode);
		resultMap.put("resultMsg", resultMsg);
		resultMap.put("isSuccess", isSuccess);
		Map logMap=new HashMap();
		logMap.put("invoiceCode",fpdm);
		logMap.put("invoiceNum", fphm);
		logMap.put("invoiceName", paramMap.get("swjg_mc"));
		logMap.put("requestType", "cy");
		logMap.put("requestId", paramMap.get("requestId"));
		logMap.put("requestConent",
				DataConvertUtil.MapToString(paramMap));
		logMap.put("errorMsg", resultMsg);
		logMap.put("errorCode", resultCode);
		logMap.put("isSuccess", isSuccess);
		logMap.put("requestTime", String.valueOf(endTime - startTime));
		try {
			InvoiceDao.saveBaiWangRequestLog(logMap);
		} catch (Exception e) {
			logger.error("获取百望请求获取请求保存异常：" + e.getMessage());
		}
        return resultMap;
	}

	/**
	 * 获取百望token
	 * 
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String getToken() {
		String token = "";
		boolean falg = false;
		Map map = new HashMap();
		map.put("appKey", appKey);
		String result = RedisUtils.getValue(className, "getAccessToken",
				new DataObject(map));
		try {
			if (StringUtils.isNotEmpty(result)) {
				Map resultMap = JacksonUtils.getMapFromJson(result);
				if(resultMap.containsKey("lastAccessTokenTime")){
					int lastAccessTokenTime = (int) resultMap
							.get("lastAccessTokenTime");
					long now = new Date().getTime();
					if (now / 1000 - lastAccessTokenTime < accessTokenExpiresTime) {
						token = (String) resultMap.get("access_token");
						falg = true;
					}
				}
			}
			if (!falg) {
				String resultCode="";
				String resultMsg="";
				String isSuccess="N";
				long startTime = System.currentTimeMillis();
				try{
					ILoginClient loginClient = new PostLogin(url);
					LoginRequest loginRequest = new LoginRequest();
					loginRequest.setAppkey(appKey);
					loginRequest.setAppSecret(appSecret);
					loginRequest.setUserName(username);
					loginRequest.setPasswordMd5(password);
					loginRequest.setUserSalt(UUID);
					LoginResponse loginResponse = loginClient.login(loginRequest);
					Map loginMap = new HashMap();
					token = loginResponse.getAccess_token();
					loginMap.put("access_token", token);
					long now = new Date().getTime();
					long lastAccessTokenTime = now / 1000;
					loginMap.put("lastAccessTokenTime", lastAccessTokenTime);
					result = JacksonUtils.getJsonFromMap(loginMap);
					RedisUtils.putValue(className, "getAccessToken",
							new DataObject(map), result, 3600);
					isSuccess="Y";
				}catch (BopException e) {
					logger.error("获取百望token异常" + e.getErrCode() + "," + e.getErrMsg());
					resultCode=e.getSubCode();
					resultMsg=e.getSubMessage();
					if(StringUtils.isEmpty(resultCode)){
						resultCode=e.getErrCode();
						resultMsg=e.getErrMsg();
					}
					isSuccess="Y";
				}
				long endTime = System.currentTimeMillis();
				Map logMap=new HashMap();
				logMap.put("invoiceCode","");
				logMap.put("invoiceNum", "");
				logMap.put("invoiceName","");
				logMap.put("requestType", "ct");
				logMap.put("requestId", "");
				logMap.put("requestConent",
						"");
				logMap.put("errorMsg", resultMsg);
				logMap.put("errorCode", resultCode);
				logMap.put("isSuccess", isSuccess);
				logMap.put("requestTime", String.valueOf(endTime - startTime));
				try {
					InvoiceDao.saveBaiWangRequestLog(logMap);
				} catch (Exception e) {
					logger.error("获取百望请求获取请求保存异常：" + e.getMessage());
				}
			}
		} catch (Exception e) {
			logger.error("解析错误");
		}
		return token;
	}

}
