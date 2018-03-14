package com.dcits.ocr.interfaces;

import java.util.HashMap;
import java.util.Map;

import com.dcits.app.data.DataObject;
import com.dcits.app.service.BaseService;
import com.dcits.app.service.IService;
import com.dcits.ocr.commons.constant.Constant;
import com.dcits.ocr.commons.dao.OCRDao;
import com.dcits.ocr.commons.service.OCRService;

public class OCRInterface extends BaseService implements IService {
	private OCRDao ocrDao;
	private OCRService ocrService;

	@SuppressWarnings({ "static-access", "rawtypes", "unchecked" })
	@Override
	public DataObject doService(DataObject dataObject) {
		Map parameter = dataObject.getMap();
		DataObject object = ocrDao.selectOcrSetting(parameter);
		if (null != object) {
			Map objMap = object.getMap();
			parameter.put("thirdCodeType", objMap.get("thirdCodeType"));
			if ("1".equals(String.valueOf(objMap.get("thirdCom")))) {// 超级鹰
				DataObject rtnData = ocrService.chaoJiYingOCRPic(dataObject);
				return rtnData;
			} else if ("2".equals(String.valueOf(objMap.get("thirdCom")))) {// 联众
				DataObject rtnData = ocrService.lianZhongOCRPic(dataObject);
				return rtnData;
			}
		} else {
			Map rtnMap = new HashMap();
			rtnMap.put(Constant.BIZ_CODE, Constant.RTN_CODE_FAILURE);
			rtnMap.put(Constant.BIZ_MSG, "当前验证码类型没有对应的服务");
			rtnMap.put("picId", "");
			rtnMap.put("ocrStr", "");
			return new DataObject(rtnMap);
		}
		return new DataObject();
	}

	public void setOcrService(OCRService ocrService) {
		this.ocrService = ocrService;
	}

	public void setOcrDao(OCRDao ocrDao) {
		this.ocrDao = ocrDao;
	}

}
