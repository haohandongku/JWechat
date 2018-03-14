package com.dcits.fpcy.commons.dao;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.json.JSONArray;

import com.dcits.app.dao.DataWindow;
import com.dcits.app.data.DataObject;
import com.dcits.fpcy.commons.bean.ResultBean;
import com.dcits.fpcy.commons.constant.SysConfig;
import com.dcits.fpcy.commons.utils.DataConvertUtil;

/**
 * 发票
 * 
 * @author wuche
 * 
 */
public class InvoiceDao {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map queryInvoiceInfo(String fpdm, String fphm,String dataType) throws ParseException, NoSuchElementException, JSONException {
		Map<String, Object> parameter = new HashMap<String, Object>();
		Map resultMap = new HashMap();
		parameter.put("fpdm", fpdm);
		parameter.put("fphm", fphm);
		DataWindow dataWindow = DataWindow.query("fpcy.common.dao.InvoiceDao_QueryInvoiceInfo", parameter);
		String cxjg = (String) dataWindow.getItemAny(0, "CYJG");
		String cycs = (String) dataWindow.getItemAny(0, "CYCS");
		String result = (String) dataWindow.getItemAny(0, "invoiceResult");
		Date cyrq = (Date) dataWindow.getItemAny(0, "CYRQ");
		if (cxjg != null) {
			if("list".equals(dataType)){
					JSONArray json = new JSONArray(cxjg);
					List<ResultBean> list = new ArrayList<ResultBean>();
					for (int i = 0; i < json.length(); i++) {
						JSONObject json1 = new JSONObject(json.get(i).toString());
						String name1 = json1.get("name1").toString();
						String name2 = json1.get("name2").toString();
						String value = json1.get("value").toString();
						//解析成有序数据
						if(!isNull(name2)){
							if(name2.startsWith("jsonhw")){
								value=getOrderByObject(value);
							}
						}
						list.add(new ResultBean(name1, name2, value));
					}
					resultMap.put("list", list);
					resultMap.put("cyjgState", SysConfig.CODE1000);
			}else if("json".equals(dataType)){
				JSONArray json = new JSONArray(cxjg);
				ResultBean resultbean = null;
				for (int i = 0; i < json.length(); i++) {
					JSONObject json1 = new JSONObject(json.get(i).toString());
					String name1 = json1.get("name1").toString();
					String name2 = json1.get("name2").toString();
					String value = json1.get("value").toString();
					if("开票日期".equals(name2)){
						resultbean = new ResultBean(name1, name2, value);
						break;
					}
				}
				resultMap.put("dateResultbean", resultbean);
				resultMap.put("invoiceResult", String.valueOf(result));
				resultMap.put("resultCode", SysConfig.CODE1000);
			}
			resultMap.put("cyrq", String.valueOf(cyrq));
			resultMap.put("cycs", String.valueOf(cycs));
		}
		return resultMap;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map queryInvoiceErrorInfo(String ID) throws ParseException, NoSuchElementException, JSONException {
		Map parameter = new HashMap();
		parameter.put("ID", ID);
		Object obj = DataWindow.queryOne("fpcy.common.dao.InvoiceDao_QueryInvoiceErrorInfo", parameter);
		Map resultMap = new HashMap();
		if (null != obj) {
			Map result = (Map) obj;
			String rzxx = String.valueOf(result.get("RZXX"));
			String cyrq = String.valueOf(result.get("CYRQ"));
			if (rzxx != null) {
				JSONArray json = new JSONArray(rzxx);
				List<ResultBean> list = new ArrayList<ResultBean>();
				for (int i = 0; i < json.length(); i++) {
					JSONObject json1 = new JSONObject(json.get(i).toString());
					String name1 = json1.get("name1").toString();
					String name2 = json1.get("name2").toString();
					String value = json1.get("value").toString();
					list.add(new ResultBean(name1, name2, value));
				}
				resultMap.put("list", list);
			}
			resultMap.put("cyjgState", SysConfig.CODE20011);
			resultMap.put("cyrq", cyrq);
		}
		return resultMap;
	}

	/**
	 * 验真请求记录 `id`'验真请求ID', `key1`'请求通知（如果等于""则没有通知，验证码请求成功 如果不为空则key1和key4为空）',
	 * `prompt`'返回的提示信息，如果等于"",则查验成功', `pic_id`'识别验证码的ID', `serverIP`'服务器IP',
	 * **/
	@SuppressWarnings("rawtypes")
	public static DataObject saveRequestLog(DataObject dataObject) {
		Map parameter = dataObject.getMap();
		DataWindow.insert("fpcy.common.dao.InvoiceDao_saveRequestLog", parameter);
		return new DataObject();
	}

	/**
	 * 保存查询记录
	 * **/
	@SuppressWarnings("rawtypes")
	public static DataObject saveQueryRecord(DataObject dataObject) {
		Map parameter = dataObject.getMap();
		DataWindow.insert("fpcy.common.dao.InvoiceDao_saveQueryRecord", parameter);
		return new DataObject();
	}

	/**
	 * 保存查询错误记录
	 * **/
	@SuppressWarnings("rawtypes")
	public static DataObject saveErrorRecord(DataObject dataObject) {
		Map parameter = dataObject.getMap();
		DataWindow.insert("fpcy.common.dao.InvoiceDao_saveErrorRecord", parameter);
		return new DataObject();
	}

	/**
	 * 删除发票信息记录(用于更新增值税当月发票的查询)
	 * **/
	@SuppressWarnings("rawtypes")
	public static DataObject deleteQueryRecord(DataObject dataObject) {
		Map parameter = dataObject.getMap();
		DataWindow.delete("fpcy.common.dao.InvoiceDao_deleteQueryRecord", parameter);
		return new DataObject();
	}

	/**
	 * 保存请求成功失败
	 * 
	 * @param dataObject
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static DataObject saveRequestRecord(DataObject dataObject) {
		Map parameter = dataObject.getMap();
		DataWindow.insert("fpcy.common.dao.InvoiceDao_saveRequestRecord", parameter);
		return new DataObject();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static DataObject updateErrorInvoiceInfo(DataObject dataObject) throws ParseException, NoSuchElementException,
			JSONException, JsonGenerationException, JsonMappingException, IOException {
		Map<String, Object> parameter = new HashMap<String, Object>();
		DataWindow dataWindow = DataWindow.query("fpcy.common.dao.InvoiceDao_QueryInvoiceInfoByDate", parameter);
		List dataList = dataWindow.getList();
		for (int i = 0, j = dataList.size(); i < j; i++) {
			String cxjg = (String) dataWindow.getItemAny(i, "CYJG");
			String fphm = (String) dataWindow.getItemAny(i, "FPHM");
			String fpdm = (String) dataWindow.getItemAny(i, "FPDM");
			if (cxjg != null) {
				JSONArray json = new JSONArray(cxjg);
				List list = new ArrayList();
				for (int k = 0; k < json.length(); k++) {
					JSONObject json1 = new JSONObject(json.get(k).toString());
					String name1 = json1.get("name1").toString();
					String name2 = json1.get("name2").toString();
					String value = json1.get("value").toString();
					if ("(销售方)纳税人识别号".equals(name2)) {
						name2 = formatStr(value, "");
					}
					Map correctMap = new HashMap();
					correctMap.put("name1", name1);
					correctMap.put("name2", name2);
					correctMap.put("value", value);
					list.add(correctMap);
				}
				String cyjg = new DataObject(list).getJson();
				Map param = new HashMap();
				param.put("cyjg", cyjg);
				param.put("fphm", fphm);
				param.put("fpdm", fpdm);
				DataWindow.update("", param);
			}
		}
		return new DataObject();
	}

	private static String formatStr(String nsrsbh, String reg) {
		String[] first = reg.split("☺");
		String[] regular = first[1].split("_");
		for (int i = 0; i < regular.length; i++) {
			nsrsbh = changeStr(nsrsbh, regular[i]);
		}
		return nsrsbh;
	}

	private static String changeStr(String nsrsbh, String regular) {
		String a = String.valueOf(regular.charAt(2));
		String b = String.valueOf(regular.charAt(0));
		nsrsbh = nsrsbh.replace(a, "#");
		nsrsbh = nsrsbh.replace(b, "%");
		nsrsbh = nsrsbh.replace("#", b);
		nsrsbh = nsrsbh.replace("%", a);
		return nsrsbh;
	}
	
	/**
	 * 获取是否查询五次
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Boolean getFalseInvoiceInfo(String fpdm,String fphm){
		boolean isCheckFireNum=false;
		Map map=new HashMap();
		map.put("fpdm", fpdm);
		map.put("fphm", fphm);
		//判断是否有错误码(202)
		int total=DataWindow.getTotal("fpcy.common.dao.InvoiceDao_getFalseInvoiceInfo", map);
		if(total>0){
			isCheckFireNum=true;
			return isCheckFireNum;
		}
		//之后添加（是否查询五次）
		return isCheckFireNum;
	}
	

	/**
	 * 获取201,202的错误结果
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List getFalseInvoiceInfoList(String fpdm,String fphm){
		Map map=new HashMap();
		map.put("fpdm", fpdm);
		map.put("fphm", fphm);
		DataWindow dw=DataWindow.query("fpcy.common.dao.InvoiceDao_getFalseInvoiceInfs", map);
		//之后添加（是否查询五次）
		return dw.getList();
	}
	/**
	 * 排序
	 * @param str
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private   String  getOrderByObject(String str){
			Map map=DataConvertUtil.StringToMap(str);
			Map retmap = new LinkedHashMap();
			if(map!=null){
				if(map.containsKey("货物或应税劳务名称")){
					retmap.put("货物或应税劳务名称", map.get("货物或应税劳务名称"));
				}else{
					retmap.put("货物或应税劳务名称","");
				}
				if(map.containsKey("规格型号")){
					retmap.put("规格型号", map.get("规格型号"));
				}else{
					retmap.put("规格型号", "");
				}
				if(map.containsKey("单位")){
					retmap.put("单位", map.get("单位"));
				}else{
					retmap.put("单位", "");
				}
				if(map.containsKey("数量")){
					retmap.put("数量", map.get("数量"));
				}else{
					retmap.put("数量", "");
				}
				if(map.containsKey("单价")){
					retmap.put("单价", map.get("单价"));
				}else{
					retmap.put("单价", "");
				}
				if(map.containsKey("金额")){
					retmap.put("金额", map.get("金额"));
				}else{
					retmap.put("金额", "");
				}
				if(map.containsKey("税率")){
					retmap.put("税率", map.get("税率"));
				}else{
					retmap.put("税率", "");
				}
				if(map.containsKey("税额")){
					retmap.put("税额", map.get("税额"));
				}else{
					retmap.put("税额", "");
				}
				str=DataConvertUtil.MapToString(retmap);
			}
			
		return str;
	}
	private static boolean isNull(String str) {// 判断字符串是否为空
		if ("".endsWith(str) || "null".equals(str)) {
			return true;
		} else {
			return false;
		}
	}
	/**
	 * 保存百望请求日志
	 * 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Map saveBaiWangRequestLog(Map paramter) {
	   Map resMap=new HashMap();	
	   DataWindow.insert(
				"fpcy.common.dao.InvoiceDao_saveBwRequestLog", paramter);
		return resMap;
	} 
}
