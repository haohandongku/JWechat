package com.dcits.ocr.commons.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.alibaba.druid.util.Base64;
import com.dcits.app.data.DataObject;
import com.dcits.app.util.JacksonUtils;
import com.dcits.ocr.commons.constant.Constant;
import com.dcits.ocr.commons.dao.OCRDao;
import com.dcits.ocr.commons.service.impl.ChaoJiYing;
import com.dcits.ocr.commons.service.impl.LianZhong;
import com.dcits.ocr.commons.utils.DateUtils;

public class OCRService {
	private static Log LOG = LogFactory.getLog(OCRService.class);
	private OCRDao ocrDao;
	private ChaoJiYing chaoJiYing;
	private LianZhong lianZhong;

	/**
	 * 超级鹰解析验证码 codeType 验证码类型 minLen 验证码最小长度 maxLen 验证码最大长度 fileType 文件类型
	 * codeFileStream 验证码文件流，与base64二选一 codeBase64 Base64类型的文件，与验证码文件流二选一
	 ***/
	@SuppressWarnings({ "static-access", "rawtypes", "unchecked" })
	public DataObject chaoJiYingOCRPic(DataObject dataObject) {
		Map parameter = dataObject.getMap();
		byte[] fileByte = null;
		if (parameter.get("codeFileStream") instanceof byte[]) {
			fileByte = (byte[]) parameter.get("codeFileStream");
		}
		if (null == fileByte) {
			fileByte = Base64.base64ToByteArray(String.valueOf(parameter.get("codeBase64")));// 文件流
		}
		String codeType = String.valueOf(parameter.get("codeType"));// 验证码类型
		String thirdCodeType = String.valueOf(parameter.get("thirdCodeType"));// 第三方验证码类型
		String minLen = String.valueOf(parameter.get("minLen"));// 最小长度
		if (ifNeedDefault(parameter.get("minLen"))) {
			minLen = "0";
		}
		String fileType = String.valueOf(parameter.get("fileType"));// 图片格式
		LOG.debug("codeType="+codeType+" fileType="+fileType);
		String reviceTime = DateUtils.getCurrectTime();
		long startTime = System.currentTimeMillis();
		String result = chaoJiYing.PostPic(fileByte, thirdCodeType, minLen, fileType);
		long endTime = System.currentTimeMillis();
		LOG.debug("超级鹰结果=" + result);
		Map resultMap = new HashMap();
		try {
			if (!"未知问题".equals(result)) {
				resultMap = JacksonUtils.getMapFromJson(result);
			}else{
				resultMap.put("err_no", "-1");
				resultMap.put("err_str", "打码服务异常");
			}
		} catch (Exception e) {
			LOG.error("超级鹰结果解析异常" + e.getMessage());
		}
		Map rtnMap = new HashMap();
		if ("0".equals(String.valueOf(resultMap.get("err_no")))) {
			Map logMap = new HashMap();
			logMap.put("codeType", codeType);
			logMap.put("reviceTime", reviceTime);
			logMap.put("requestId", parameter.get("requestId"));
			logMap.put("picId", resultMap.get("pic_id"));
			logMap.put("yzm", resultMap.get("pic_str"));
			logMap.put("recogTime", DateUtils.getCurrectTime());
			logMap.put("dateLength", (endTime - startTime));
			logMap.put("company", "超级鹰");
			ocrDao.saveOcrLog(logMap);
			rtnMap.put(Constant.BIZ_CODE, Constant.RTN_CODE_SUCCESS);
			rtnMap.put(Constant.BIZ_MSG, "");
			rtnMap.put("picId", resultMap.get("pic_id"));
			rtnMap.put("ocrStr", resultMap.get("pic_str"));
		} else {
			rtnMap.put(Constant.BIZ_CODE, Constant.RTN_CODE_FAILURE);
			rtnMap.put(Constant.BIZ_MSG, resultMap.get("err_str"));
			rtnMap.put("picId", "");
			rtnMap.put("ocrStr", "");
		}
		return new DataObject(rtnMap);
	}

	/**
	 * 超级鹰打码回填
	 **/
	@SuppressWarnings({ "rawtypes", "static-access", "unchecked" })
	public DataObject chaoJiYingReport(DataObject dataObject) {
		Map parameter = dataObject.getMap();
		Map rtnMap = new HashMap();
		if ("Y".endsWith(String.valueOf(parameter.get("isRight")))) {// 正确
			rtnMap.put(Constant.BIZ_CODE, Constant.RTN_CODE_SUCCESS);
			rtnMap.put(Constant.BIZ_MSG, "成功");
		} else if ("N".endsWith(String.valueOf(parameter.get("isRight")))) {
			String id = String.valueOf(parameter.get("picId"));
			String result = chaoJiYing.ReportError(id);
			Map resultMap;
			try {
				resultMap = JacksonUtils.getMapFromJson(result);
				if ("0".equals(String.valueOf(resultMap.get("err_no")))) {
					rtnMap.put(Constant.BIZ_CODE, Constant.RTN_CODE_SUCCESS);
					rtnMap.put(Constant.BIZ_MSG, "成功");
				} else {
					LOG.error("超级鹰回填返回="+result);
					rtnMap.put(Constant.BIZ_CODE, Constant.RTN_CODE_FAILURE);
					rtnMap.put(Constant.BIZ_MSG, resultMap.get("err_str"));
				}
			} catch (Exception e) {
				LOG.error("报文解析异常" + e.getMessage()+"超级鹰返回="+result);
				rtnMap.put(Constant.BIZ_CODE, Constant.RTN_CODE_FAILURE);
				rtnMap.put(Constant.BIZ_MSG, "");
			}
		}
		ocrDao.updateOcrLog(parameter);
		return new DataObject(rtnMap);
	}

	/**
	 * 联众打码 识别
	 **/
	@SuppressWarnings({ "static-access", "rawtypes", "unchecked" })
	public DataObject lianZhongOCRPic(DataObject dataObject) {
		String reviceTime = DateUtils.getCurrectTime();
		Map parameter = dataObject.getMap();
		byte[] fileByte = null;
		if (parameter.get("codeFileStream") instanceof byte[]) {
			fileByte = (byte[]) parameter.get("codeFileStream");
		}
		if (null == fileByte) {
			fileByte = Base64.base64ToByteArray(String.valueOf(parameter.get("codeBase64")));// 文件流
		}
		String codeType = String.valueOf(parameter.get("codeType"));
		String thirdCodeType = String.valueOf(parameter.get("thirdCodeType"));
		String minLen = String.valueOf(parameter.get("minLen"));
		if (ifNeedDefault(parameter.get("minLen"))) {
			minLen = "0";
		}
		String maxLen = String.valueOf(parameter.get("maxLen"));
		if (ifNeedDefault(parameter.get("maxLen"))) {
			maxLen = "10";
		}
		String fileType = String.valueOf(parameter.get("fileType"));
		String result;
		try {
			long startTime = System.currentTimeMillis();
			result = lianZhong.YZMSB(fileByte, minLen, maxLen, thirdCodeType, fileType);
			long endTime = System.currentTimeMillis();
			Map resultMap = JacksonUtils.getMapFromJson(result);
			Map rtnMap = new HashMap();
			if ("true".equals(String.valueOf(resultMap.get("result")))) {
				Map dataMap = (Map) resultMap.get("data");
				Map logMap = new HashMap();
				logMap.put("codeType", codeType);
				logMap.put("reviceTime", reviceTime);
				logMap.put("requestId", parameter.get("requestId"));
				logMap.put("picId", dataMap.get("id"));
				logMap.put("yzm", dataMap.get("val"));
				logMap.put("recogTime", DateUtils.getCurrectTime());
				logMap.put("dateLength", (endTime - startTime));
				logMap.put("company", "联众");
				ocrDao.saveOcrLog(logMap);
				rtnMap.put(Constant.BIZ_CODE, Constant.RTN_CODE_SUCCESS);
				rtnMap.put(Constant.BIZ_MSG, "");
				rtnMap.put("picId", dataMap.get("id"));
				rtnMap.put("ocrStr", dataMap.get("val"));
			} else {
				rtnMap.put(Constant.BIZ_CODE, Constant.RTN_CODE_FAILURE);
				rtnMap.put(Constant.BIZ_MSG, resultMap.get("data"));
				rtnMap.put("picId", "");
				rtnMap.put("ocrStr", "");
			}
			return new DataObject(rtnMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new DataObject();
	}

	/**
	 * 联众打码回填
	 **/
	@SuppressWarnings({ "rawtypes", "static-access", "unchecked" })
	public DataObject lianZhongReport(DataObject dataObject) {
		Map parameter = dataObject.getMap();
		Map rtnMap = new HashMap();
		if ("Y".endsWith(String.valueOf(parameter.get("isRight")))) {// 正确
			rtnMap.put(Constant.BIZ_CODE, Constant.RTN_CODE_SUCCESS);
			rtnMap.put(Constant.BIZ_MSG, "成功");
		} else if ("N".endsWith(String.valueOf(parameter.get("isRight")))) {
			String id = String.valueOf(parameter.get("picId"));
			String result = lianZhong.ReportError(id);
			Map resultMap;
			try {
				resultMap = JacksonUtils.getMapFromJson(result);
				if ("true".equals(String.valueOf(resultMap.get("result")))) {
					rtnMap.put(Constant.BIZ_CODE, Constant.RTN_CODE_SUCCESS);
					rtnMap.put(Constant.BIZ_MSG, "成功");
				} else if ("false".equals(String.valueOf(resultMap.get("result")))) {
					rtnMap.put(Constant.BIZ_CODE, Constant.RTN_CODE_FAILURE);
					rtnMap.put(Constant.BIZ_MSG, resultMap.get("data"));
				}
			} catch (Exception e) {
				LOG.error("报文解析异常" + e.getMessage()+"联众返回="+result);
				rtnMap.put(Constant.BIZ_CODE, Constant.RTN_CODE_FAILURE);
				rtnMap.put(Constant.BIZ_MSG, "");
			}
		}
		ocrDao.updateOcrLog(parameter);
		return new DataObject(rtnMap);
	}

	private boolean ifNeedDefault(Object object) {
		try {
			int obj = Integer.valueOf(String.valueOf(object));
			if (obj > 0) {
				return false;
			}
		} catch (Exception e) {
		}
		return true;
	}

	public void setChaoJiYing(ChaoJiYing chaoJiYing) {
		this.chaoJiYing = chaoJiYing;
	}

	public void setOcrDao(OCRDao ocrDao) {
		this.ocrDao = ocrDao;
	}

	public void setLianZhong(LianZhong lianZhong) {
		this.lianZhong = lianZhong;
	}
}
