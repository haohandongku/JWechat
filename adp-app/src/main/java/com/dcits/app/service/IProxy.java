package com.dcits.app.service;

import java.io.File;
import java.util.Map;

import com.dcits.app.data.DataObject;

public interface IProxy {

	@SuppressWarnings("rawtypes")
	public DataObject callService(String serviceId, DataObject dataObject,
			Map router);

	@SuppressWarnings("rawtypes")
	public DataObject callFileService(String serviceId, String serviceName,
			File file, DataObject dataObject, Map router);

}
