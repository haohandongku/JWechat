package com.dcits.ocr.commons.dao;

import java.util.Map;

import com.dcits.app.dao.DataWindow;
import com.dcits.app.data.DataObject;

public class OCRDao {
	// 验证码日志添加
	@SuppressWarnings("rawtypes")
	public static DataObject saveOcrLog(Map parameter) {
		DataWindow.insert("ocr.service.OcrDao_saveOcrRequestLog", parameter);
		return new DataObject();
	}

	// 验证码日志修改
	@SuppressWarnings("rawtypes")
	public static DataObject updateOcrLog(Map parameter) {
		DataWindow.update("ocr.service.OcrDao_updateOcrRequestLog", parameter);
		return new DataObject();
	}

	// 验证码日志删除
	@SuppressWarnings("rawtypes")
	public static DataObject deleteOcrLog(Map parameter) {
		DataWindow.delete("ocr.service.OcrDao_deleteOcrRequestLog", parameter);
		return new DataObject();
	}

	// 验证码日志获取
	@SuppressWarnings("rawtypes")
	public static DataObject selectOcrLog(Map parameter) {
		Object object = DataWindow.queryOne("ocr.service.OcrDao_queryOcrRequestLog", parameter);
		return new DataObject(object);
	}

	// 获取打码服务设置
	@SuppressWarnings("rawtypes")
	public static DataObject selectOcrSetting(Map parameter) {
		Object object = DataWindow.queryOne("ocr.service.OcrDao_queryOcrSetting", parameter);
		return new DataObject(object);
	}
	
	@SuppressWarnings("rawtypes")
	public static DataObject selectOcrPic(Map parameter){
		Object object = DataWindow.queryOne("ocr.service.OcrDao_queryOcrPic", parameter);
		return new DataObject(object);
	}
}
