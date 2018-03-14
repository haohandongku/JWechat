package com.dcits.ocr.interfaces;

import java.util.HashMap;
import java.util.Map;

import com.dcits.app.data.DataObject;
import com.dcits.app.service.BaseService;
import com.dcits.app.service.IService;
import com.dcits.ocr.commons.constant.Constant;
import com.dcits.ocr.commons.dao.OCRDao;
import com.dcits.ocr.commons.service.OCRService;

public class OCRReportInterface extends BaseService implements IService {
	private OCRDao ocrDao;
	private OCRService ocrService;

	@SuppressWarnings({ "static-access", "rawtypes", "unchecked" })
	@Override
	public DataObject doService(DataObject dataObject) {
		Map parameter = dataObject.getMap();
		DataObject picObject = ocrDao.selectOcrPic(parameter);
		Map param = new HashMap();
		param.put("picId", parameter.get("picId"));
		param.put("isRight", parameter.get("isRight"));
		if (null != picObject) {
			Map picMap = picObject.getMap();
			String thirdCom = String.valueOf(picMap.get("thirdCom"));
			if ("1".equals(thirdCom)) {// 超级鹰
				return ocrService.chaoJiYingReport(new DataObject(param));
			} else if ("2".equals(thirdCom)) {// 联众
				return ocrService.lianZhongReport(new DataObject(param));
			}
		} else {
			Map rtnMap = new HashMap();
			rtnMap.put(Constant.BIZ_CODE, Constant.RTN_CODE_FAILURE);
			rtnMap.put(Constant.BIZ_MSG, "未能获取到当前ID的服务商");
			return new DataObject(rtnMap);
		}
		return dataObject;
	}

	public void setOcrService(OCRService ocrService) {
		this.ocrService = ocrService;
	}

	public void setOcrDao(OCRDao ocrDao) {
		this.ocrDao = ocrDao;
	}
}
