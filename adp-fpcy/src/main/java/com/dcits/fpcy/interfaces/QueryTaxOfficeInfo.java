package com.dcits.fpcy.interfaces;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dcits.app.data.DataObject;
import com.dcits.app.service.BaseService;
import com.dcits.app.service.IService;
import com.dcits.fpcy.commons.factory.TaxOfficeFactory;

/**
 * 查询税局信息
 * 优化：以前税局还没改版走总局还是地方国税局做了标志，现在去掉
 * @author wuche
 * 
 */
public class QueryTaxOfficeInfo extends BaseService implements IService {
	private static Log logger = LogFactory.getLog(QueryTaxOfficeInfo.class);
	private TaxOfficeFactory taxOfficeFactory;
	
	public void setTaxOfficeFactory(TaxOfficeFactory taxOfficeFactory) {
		this.taxOfficeFactory = taxOfficeFactory;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public DataObject doService(DataObject dataobject) {
		Map paramter=dataobject.getMap();
		Map  returnMap=new HashMap();
		String fpdm=StringUtils.trim(String.valueOf(paramter.get("fpdm")));
		String invoiceComeFrom=StringUtils.trim(String.valueOf(paramter.get("invoiceComeFrom")));
		try {
			Map fpcyParas=taxOfficeFactory.queryTaxOfficeByFpdm(fpdm, invoiceComeFrom);
			if(fpcyParas==null){
				returnMap.put("BIZ_RETURNCODE", "99");
			}else{
				String datamodel=String.valueOf(fpcyParas.get("datamodel"));
				String swjgmc=String.valueOf(fpcyParas.get("swjg_mc"));
				String swjgdm=String.valueOf(fpcyParas.get("swjg_dm")) ;
				returnMap.put("datamodel", datamodel);
				returnMap.put("swjgmc", swjgmc);
				returnMap.put("swjgdm", swjgdm);
				returnMap.put("BIZ_RETURNCODE", "00");
			}
		} catch (Exception e) {
			returnMap.put("BIZ_RETURNCODE", "99");
			logger.error("获取税局信息:解析数据异常");
		}
		return new DataObject(returnMap);
	}

}
