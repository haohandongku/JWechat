package com.dcits.fpcy.commons.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dcits.app.dao.DataWindow;

/**
 * 税局
 * 
 * @author wuche
 * 
 */
public class TaxOfficeDao {
	/**
	 * 获取所有税局配置信息
	 * 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public List getTaxOfficeList() {
		DataWindow dataWindow = DataWindow.query(
				"fpcy.common.dao.TaxOfficeDao_queryAllTaxOffice", null);
		return dataWindow.getList();
	}

	/**
	 * 根据swjg_dm和fp_zl查询单条数据
	 * 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public  Map getTaxOfficeOne(Map paramter) {
		Map map = (Map) DataWindow.queryOne(
				"fpcy.common.dao.TaxOfficeDao_queryOneTaxOffice", paramter);
		return map;
	}

	/**
	 * 获取是否走总局接口的参数
	 * 
	 * @param swjg_dm
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String isGenAdmin(String swjg_dm) {
		String SQL = "fpcy.common.dao.TaxOfficeDao_isGenAdmin";
		Map parameter = new HashMap();
		if (swjg_dm.length() == 4) {
			parameter.put("swjg_dm", "%" + swjg_dm + "%");
			parameter.put("swjg_mc_like", "%增值税%");
		} else {
			parameter.put("swjg_dm", "%" + swjg_dm + "%");
			parameter.put("swjg_mc_like", "%增值税%");
		}
		DataWindow dataWindow = DataWindow.query(SQL, parameter);
		String isGenAdmin = null;
		if (dataWindow.getList().size() > 0) {
			isGenAdmin = dataWindow.getItemAny(0, "isGenAdmin").toString();
		}
		return isGenAdmin;

	}
	
	/**
	 * 控制服务的停止表
	 * @param fpdm
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean queryEnable(Map fpcyParas) {
		Map parameter = new HashMap<String, String>();
		parameter.put("swjg_dm", fpcyParas.get("swjg_dm"));
		parameter.put("fp_zl", fpcyParas.get("fp_zl"));
		//parameter.put("poolid",fpcyParas.get("poolid"));
		parameter.put("query", "gb");
		String SQL = "fpcy.common.dao.TaxOfficeDao_selectenablequery";
		int total = DataWindow.getTotal(SQL, parameter);
		if (total >= 1) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 修改税局规则，并新增税局变化日志
	 */
	@SuppressWarnings("rawtypes")
	public  void  updateTaxOfficeRole(Map paramter) {
		if(paramter!=null){
			DataWindow.update("fpcy.common.dao.TaxOfficeDao_updateRule", paramter);
		    DataWindow.insert("fpcy.common.dao.TaxOfficeDao_insertRuleLog", paramter);
		}
	}
	
	/**
	 * 查询所有税局总局发票解析规则
	 * @param map
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public  List queryAllReg(){
		Map map=new HashMap();
		DataWindow  dw = DataWindow.query("fpcy.common.dao.TaxOfficeDao_QueryRegAll", map);
		return dw.getList();
	}
	
	/**
	 * 查询单条税局总局发票解析规则
	 * @param map
	 * @return
	 */
	@SuppressWarnings({ "rawtypes"})
	public  Map queryOneReg(Map map){
		return (Map) DataWindow.queryOne("fpcy.common.dao.TaxOfficeDao_QueryOneReg", map);
	}
}
