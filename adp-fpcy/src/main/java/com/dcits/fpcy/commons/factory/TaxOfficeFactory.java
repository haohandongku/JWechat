package com.dcits.fpcy.commons.factory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import com.dcits.app.data.DataObject;
import com.dcits.app.util.JacksonUtils;
import com.dcits.app.util.RedisUtils;
import com.dcits.fpcy.commons.bean.TaxOfficeBean;
import com.dcits.fpcy.commons.constant.FplbCode;
import com.dcits.fpcy.commons.constant.FpzlCode;
import com.dcits.fpcy.commons.dao.TaxOfficeDao;
import com.dcits.fpcy.commons.service.CommonService;
import com.dcits.fpcy.commons.utils.JavaBeanUtils;

/**
 * 组装税局信息-->对应原代码中GetFpcs
 * 
 * @author wuche
 * 
 */
public class TaxOfficeFactory {
	private static final String className = "commons.service.CommonService";
	private static final String methodName = "putTaxOfficeList";
	private static final String methodName2 = "putTORoleList";
	private TaxOfficeDao taxOfficeDao;

	public void setTaxOfficeDao(TaxOfficeDao taxOfficeDao) {
		this.taxOfficeDao = taxOfficeDao;
	}

	// 判断发票代码,获取唯一税局信息
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map queryTaxOfficeByFpdm(String fpdm, String invoiceComeFrom) throws JsonGenerationException,
			JsonMappingException, IOException {
		Map map = new HashMap();
		if (fpdm.length() == 10) {
			String swjg_dm = null;
			// 增值税截取发票代码的前4位
			String fplb = fpdm.substring(7, 8);
			// 1.增值税专用发票 fplx 01 2.货物运输业增值税专用发票fplx 02 3.增值税普通发票fplx 04
			// 5.增值税专用发票 fplx 01 7.货物运输业增值税专用发票fplx 02 6.增值税普通发票fplx 04
			swjg_dm = fpdm.substring(0, 4);
			// 判断此地区的增值税查询（现在都去发票总局去查）
			map = GenAdmin(fplb, swjg_dm, fpdm, invoiceComeFrom);
		}
		if (fpdm.length() == 12) { // 国税、地税
			// 截取发票代码的第一位，如果是0 增值税电子发票，增值税普通发票（改版），如果是 1 国税普通机打定额发票，机动车统一发票，2 地税普通发票
			String fppz = fpdm.substring(0, 1);
			// 电子发票
			if (fppz.equals("0")) {
				// 截取发票代码的2-5位，是电子发票的税务局机关代码
				String dzswjg_dm = fpdm.substring(1, 5);
				//判断发票种类  第11,12位  电子票：11 电子普票          卷式票：01是普通卷票, 04,05增值税普通
				String fpzl1 = fpdm.substring(10, 11);
				String fpzl2 = fpdm.substring(11, 12);
				String fp_zl = FpzlCode.fpzl333;
				map = queryOneTaxOffice(dzswjg_dm, fp_zl);
				if(map==null){
				    //截取三位（特殊地级市发票）
					dzswjg_dm = fpdm.substring(1, 4)+"0";
					map = queryOneTaxOffice(dzswjg_dm, fp_zl);
				}
				String swjg_mc=(String) map.get("swjg_mc");
				if("0".equals(fpzl1)&&StringUtils.isNotEmpty(swjg_mc)){
					if("6".equals(fpzl2)||"7".equals(fpzl2)){
						map.put("swjg_mc", swjg_mc.replace("电子", "卷式普通"));
					}else if("4".equals(fpzl2) ||"5".equals(fpzl2)){	
						map.put("swjg_mc", swjg_mc.replace("电子", "普通"));
					}else if("4".equals(fpzl2) ||"5".equals(fpzl2)){	
						map.put("swjg_mc", swjg_mc.replace("电子", "普通"));
					}
				}else if("1".equals(fpzl1)&&StringUtils.isNotEmpty(swjg_mc)){
					if("1".equals(fpzl2)){
					  map.put("swjg_mc", swjg_mc.replace("电子", "电子普通"));
					}else if("2".equals(fpzl2)){
						 map.put("swjg_mc", swjg_mc.replace("电子", "电子普通[通行费]"));
					}
				}
			} else {
				// 截取发票代码的前5位
				String swjg_dm = fpdm.substring(0, 5);
				map = GDScs(swjg_dm, fpdm, invoiceComeFrom);
			}
		}
		return map;
	}

	/**
	 * 获取国税地税发票配置信息，机动车发票
	 * 
	 * @param swjg_dm
	 *            税务机关代码
	 * @param fpdm
	 *            发票代码
	 * @param invoiceComeFrom
	 *            应用端编号
	 * @return 发票地区的配置信息
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 */
	@SuppressWarnings("rawtypes")
	private  Map GDScs(String swjg_dm, String fpdm, String invoiceComeFrom) throws JsonGenerationException,
			JsonMappingException, IOException {
		Map fpcs = new HashMap();
		String fp_zl = null;
		String jdfpzl = "null";
		if(!fpdm.startsWith("2")){
			jdfpzl = fpdm.substring(7, 8);
		}
		if ("2".equals(jdfpzl)) {
			fp_zl = FpzlCode.fpzl111;
			fpcs = queryOneTaxOffice(swjg_dm, fp_zl);
		} else {
			fp_zl = FpzlCode.fpzlnull;
			fpcs = queryOneTaxOffice(swjg_dm, fp_zl);
		}
		if (fpcs == null) {
			swjg_dm = fpdm.substring(0, 3) + "00";
			if(jdfpzl.equals("2")){
				fp_zl = FpzlCode.fpzl111;
			}else{
				fp_zl = FpzlCode.fpzlnull;
			}
			fpcs = queryOneTaxOffice(swjg_dm, fp_zl);
		}
		return fpcs;
	}


	/**
	 * 获取走总局接口的增值税发票地区配置信息
	 * 
	 * @param swjg_dm
	 *            税务机关代码
	 * @param fpdm
	 *            发票代码
	 * @param invoiceComeFrom
	 *            应用端编号
	 * @return 发票地区的配置信息
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 */
	@SuppressWarnings("rawtypes")
	private  Map GenAdmin(String fplb, String swjg_dm, String fpdm, String invoiceComeFrom)
			throws JsonGenerationException, JsonMappingException, IOException {
		// 1.增值税专用发票 fplx 01 2.货物运输业增值税专用发票fplx 02 3.增值税普通发票fplx 04
		// 5.增值税专用发票 fplx 01 7.货物运输业增值税专用发票fplx 02 6.增值税普通发票fplx 04
		Map fpcs = new HashMap();
		String fp_zl = null;
		if (fplb.equals(FplbCode.fplb1) || fplb.equals(FplbCode.fplb5)) {
			fp_zl = FpzlCode.fpzl222;
			fpcs = queryOneTaxOffice(swjg_dm, fp_zl);
			if (fpcs == null) {
				swjg_dm = fpdm.substring(0, 2) + "00";
				fp_zl = FpzlCode.fpzl222;
				fpcs = queryOneTaxOffice(swjg_dm, fp_zl);
			}
		} else if (fplb.equals(FplbCode.fplb3) || fplb.equals(FplbCode.fplb6)) {
			fp_zl = FpzlCode.fpzl111;
			fpcs = queryOneTaxOffice(swjg_dm, fp_zl);
			if (fpcs == null) {
				swjg_dm = fpdm.substring(0, 2) + "00";
				fp_zl = FpzlCode.fpzl111;
				fpcs = queryOneTaxOffice(swjg_dm, fp_zl);
			}
		} else if (fplb.equals(FplbCode.fplb2) || fplb.equals(FplbCode.fplb7)) {
			fp_zl = FpzlCode.fpzl111;
			fpcs = queryOneTaxOffice(swjg_dm, fp_zl);
			if (fpcs == null) {
				swjg_dm = fpdm.substring(0, 2) + "00";
				fp_zl = FpzlCode.fpzl111;
				fpcs = queryOneTaxOffice(swjg_dm, fp_zl);
			}
		}
		return fpcs;
	}

	/**
	 * 方法三:获取税局单条信息
	 * @param swjg_dm
	 * @param fp_zl
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public  Map queryOneTaxOffice(String swjg_dm, String fp_zl){
		Map resultMap = new HashMap();
		Map map = new HashMap();
		map.put("swjg_dm", swjg_dm);
		map.put("fp_zl", fp_zl);
		String result = RedisUtils.getValue(className, methodName, new DataObject(map));
		try{
			if (StringUtils.isNotEmpty(result)) {
				resultMap = JacksonUtils.getMapFromJson(result);
			} else {
				// 查询数据库
				resultMap = taxOfficeDao.getTaxOfficeOne(map);
				//刷新该地区信息到redis中
				CommonService.refreshTaxOffice2(map,resultMap);
			}
		}catch(Exception e){
			// 查询数据库
			TaxOfficeDao taxOfficeDao = new TaxOfficeDao();
			resultMap = taxOfficeDao.getTaxOfficeOne(map);
			//刷新该地区信息到redis中
			CommonService.refreshTaxOffice2(map,resultMap);
		}
		return resultMap;
	}

	// 方法三:获取税局单条信息(实现类)
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static TaxOfficeBean queryOneTaxOfficeByMap(Map map) throws Exception {
		TaxOfficeBean taxOfficeBean = null;
		if (map != null) {
			taxOfficeBean = (TaxOfficeBean) JavaBeanUtils.mapToObject(map, TaxOfficeBean.class);
		}
		return taxOfficeBean;
	}

	/**
	 * 判断是不是服务开通
	 * 
	 * @param fpdm
	 * @return
	 */
	@SuppressWarnings({ "rawtypes" })
	public boolean queryEnable(Map fpcyParas) {
		boolean flag = taxOfficeDao.queryEnable(fpcyParas);
		return flag;
	}
	
	
	/**
	 * 获取税局最新单条规则
	 * 
	 * @param fpdm
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map  queryOneRole(String swjg_mc) {
		//从redis中取
		Map map=new HashMap();
		Map resultMap=new HashMap();
		map.put("swjg_mc", swjg_mc);
		String result = RedisUtils.getValue(className, methodName2, new DataObject(map));
		try{
			if (StringUtils.isNotEmpty(result)) {
				resultMap = JacksonUtils.getMapFromJson(result);
			} else {
				// 查询数据库
				resultMap = taxOfficeDao.queryOneReg(map);
				//刷新该地区信息到redis中
				CommonService.refreshTaxOfficeRole(map,resultMap);
			}
		}catch(Exception e){
			// 查询数据库
			resultMap = taxOfficeDao.queryOneReg(map);
			//刷新该地区信息到redis中
			CommonService.refreshTaxOfficeRole(map,resultMap);
		}
		//规则
		return resultMap;
	}
	 
	/**
	 * 更新税局发票规则信息
	 * 这里不要加事务，防止影响流程
	 * @param fpdm
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked"})
	public  void  upOneRole(Map paramter) {
		if(paramter!=null){
			String swjg_mc=(String) paramter.get("swjg_mc");
			String newRole=(String) paramter.get("newRole");
			String newRoleJsName=(String) paramter.get("newRoleJsName");
			Map attrMap = new HashMap();
			attrMap.put("swjg_mc",swjg_mc);
			Map roleMap= new HashMap();
			roleMap.put("role",newRole);
			roleMap.put("swjg_mc",swjg_mc);
			roleMap.put("roleJsName",newRoleJsName);
			//刷新redis数据
			CommonService.refreshTaxOfficeRole(attrMap,roleMap);
		    //这里写的不好，对象有创建一次，但调用次数不对
			TaxOfficeDao taxOfficeDao=new TaxOfficeDao();
			taxOfficeDao.updateTaxOfficeRole(paramter);
		}
	}
	
	
	  /**
     * 获取datamodel的参数，比对用户输入的参数
     * @param dataModel
     * @return
     */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public  List getCheck(String dataModel) {// 截取datamodel	
		Pattern p = Pattern.compile("\\{.*?\\}");
		Matcher m = p.matcher(dataModel);
		List list = new ArrayList();
		try {
			while (m.find()) {
					Map map = JacksonUtils.getMapFromJson(m.group());
					Set keys = map.keySet();
					if (null != keys && !"yccs".equals(keys) && keys.size() > 1) {
						Iterator iterator = keys.iterator();
						while (iterator.hasNext()) {
							Object key = iterator.next();
							if (!"event".equals(key) && !"eventtype".equals(key)) {
								list.add(key);
							}
						}
					}
			}
		} catch (Exception e) {
			
		}
		return list;
	}
	
	
}
