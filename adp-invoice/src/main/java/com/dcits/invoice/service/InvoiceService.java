package com.dcits.invoice.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import sun.misc.BASE64Decoder;

import com.dcits.app.dao.DataWindow;
import com.dcits.app.data.DataObject;
import com.dcits.app.service.BaseService;
import com.dcits.app.util.FileUtils;
import com.dcits.app.util.JacksonUtils;
import com.dcits.ieds.proxy.IEDSProxy;
import com.dcits.invoice.commons.constant.DmSequenceName;

public class InvoiceService extends BaseService {
	private Log logger = LogFactory.getLog(InvoiceService.class);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DataObject getDataModel(DataObject dataObject) {
		Map parameter = dataObject.getMap();
		if (!parameter.containsKey("invoiceComeFrom")) {
			parameter.put("invoiceComeFrom", "3");
		}
		DataObject rtnData = IEDSProxy.doService("newGetDataModel", new DataObject(parameter), null);
		return rtnData;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DataObject getInvoiceInfo(DataObject dataObject) throws JsonGenerationException, JsonMappingException,
			IOException {
		Map parameter = dataObject.getMap();
		parameter.put("uid", "");
		if (!parameter.containsKey("invoiceComeFrom")) {
			parameter.put("invoiceComeFrom", "3");
		}
		if (parameter.containsKey("fpdm")) {
			parameter.put("FPDM", parameter.get("fpdm").toString());
		} else {
			parameter.put("fpdm", parameter.get("FPDM").toString());
		}
		if (parameter.containsKey("fphm")) {
			parameter.put("FPHM", parameter.get("fphm").toString());
		} else {
			parameter.put("fphm", parameter.get("FPHM").toString());
		}
		DataObject rtnData = new DataObject();
		String dataString = dataObject.getJson();
		if (dataString.contains("@") && dataString.contains("rootVo.properties*fpdm")) {
			dataString = dataString.replace("@", "-");
			dataString = dataString.replace("*", "_");
			Map specialData = JacksonUtils.getMapFromJson(dataString);
			specialData.put("ifHasSpecialMark", "Y");
			rtnData = IEDSProxy.doService("newFpyzQueryInvoiceService", new DataObject(specialData), null);
		} else {
			rtnData = IEDSProxy.doService("newFpyzQueryInvoiceService", new DataObject(parameter), null);
		}
		Map dataMap = new HashMap();
		if (null != rtnData) {
			dataMap = rtnData.getMap();
			try {
				if (!"2001".endsWith(String.valueOf(dataMap.get("cyjgState")))) {
					List lista = (List) dataMap.get("list");
					if (String.valueOf(dataMap.get("swjg_mc")).contains("增值税（机动车")) {
						List listb = new ArrayList();
						String sx[] = { "票种", "name2\":\"机器编号", "查询次数", "销货单位名称", "购买方名称", "厂牌型号","金额", "税额", "价税合计", "是否作废" };
						for (int i = 0; i < sx.length; i++) {
							for (int j = 0; j < lista.size(); j++) {
								if (lista.get(j).toString().contains(sx[i])
										&& !lista.get(j).toString().contains("jsonhw")) {
									listb.add(lista.get(j));
								}
							}
						}
						dataMap.put("list", listb);
					} else if (String.valueOf(dataMap.get("swjg_mc")).contains("增值税")) {
						List listb = new ArrayList();
						String sx[] = { "票种", "name2\":\"机器编号", "查询次数", "(销售方)名称", "(购买方)名称", "金额","税额", "价税合计", "是否作废" };
						for (int i = 0; i < sx.length; i++) {
							for (int j = 0; j < lista.size(); j++) {
								if (lista.get(j).toString().contains(sx[i])
										&& !lista.get(j).toString().contains("jsonhw")) {
									listb.add(lista.get(j));
								}
							}
						}
						dataMap.put("list", listb);
					}

					if (dataMap.containsKey("invoiceResult")) {
						String json = String.valueOf(dataMap.get("invoiceResult"));
						if (!"".endsWith(json) && !"null".endsWith(json)) {
							Map jsonMap = JacksonUtils.getMapFromJson(json);
							String invoiceTypeCode = String.valueOf(jsonMap.get("invoiceTypeCode"));
							if ("04".equals(invoiceTypeCode) || "03".equals(invoiceTypeCode)) {
								Map rtnMap = new HashMap();
								rtnMap.put("invoiceTypeName", String.valueOf(jsonMap.get("invoiceTypeName")));
								rtnMap.put("taxDiskCode", String.valueOf(jsonMap.get("taxDiskCode")));
								rtnMap.put("checkNum", String.valueOf(jsonMap.get("checkNum")));
								rtnMap.put("salesName", String.valueOf(jsonMap.get("salesName")));
								rtnMap.put("purchaserName", String.valueOf(jsonMap.get("purchaserName")));
								rtnMap.put("totalTaxNum", String.valueOf(jsonMap.get("totalTaxNum")));
								rtnMap.put("totalTaxSum", String.valueOf(jsonMap.get("totalTaxSum")));
								rtnMap.put("voidMark", String.valueOf(jsonMap.get("voidMark")));
								if ("03".equals(invoiceTypeCode)) {// 机动车
									rtnMap.put("brandType", String.valueOf(jsonMap.get("brandType")));
								}
								rtnMap.put("invoiceTypeCode", String.valueOf(jsonMap.get("invoiceTypeCode")));
								json = JacksonUtils.getJsonFromMap(rtnMap);
								dataMap.put("invoiceResult", json);
							}
						}
					}
				}

			} catch (Exception e) {
				logger.error("JSON生成异常");
			}
			dataMap.remove("cycs");
			return new DataObject(dataMap);
		}
		return new DataObject(dataMap);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public DataObject getInvoiceInfoByScan(DataObject dataObject) {
		Map<String, Object> result = new HashMap<String, Object>();
		Map map = dataObject.getMap();
		Map parameter = new HashMap();
		parameter.put("invoiceComeFrom", "4");
		String value = map.get("value").toString();
		try {
			Map valueMap = JacksonUtils.getMapFromJson(value);
			value = String.valueOf(valueMap.get("resultStr"));
		} catch (Exception e) {
		}
		String key[] = value.split(",");
		parameter.put("fpdm", key[2]);
		parameter.put("fphm", key[3]);
		parameter.put("FPDM", key[2]);
		parameter.put("FPHM", key[3]);
		String fpzl = key[1];
		String kprq = key[5].contains("-") ? key[5].substring(0, 10) : key[5].substring(0, 4) + "-"
				+ key[5].substring(4, 6) + "-" + key[5].substring(6, 8);
		parameter.put("kprq", kprq);
		if (fpzl.equals("01")) {
			parameter.put("KJJE", key[4]);
			parameter.put("kjje", key[4]);
		} else if (fpzl.equals("02")) {
			parameter.put("HJJE", key[4]);
			parameter.put("kjje", key[4]);
		} else {
			parameter.put("JYM", key[6].toString().substring(key[6].length() - 6, key[6].length()));
			parameter.put("fpje", key[6].toString().substring(key[6].length() - 6, key[6].length()));
		}
		parameter.put("yzmSj", "");
		parameter.put("token", "");
		parameter.put("loginSj", "");
		parameter.put("username", "");
		parameter.put("yzm", "yzm");
		parameter.put("key2", result.get("key2"));
		parameter.put("key3", result.get("key3"));
		parameter.put("iv", result.get("iv"));
		parameter.put("salt", result.get("salt"));
		parameter.put("NEW", "1");
		DataObject rtnData = new DataObject();
		try {
			rtnData = getInvoiceInfo(new DataObject(parameter));
		} catch (Exception e) {
			logger.error("getInvoiceInfo出现异常");
		}
		return rtnData;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void uploadInvoiceSample(DataObject dataObject) throws IOException {
		Map parameter = dataObject.getMap();
		HttpServletResponse response = getHttpServletResponse(parameter);
		List<FileItem> fileList = getFileList(parameter);
		Iterator<FileItem> it = fileList.iterator();
		FileItem fileItem = null;
		//联系电话
		String sjhm = URLDecoder.decode(String.valueOf(parameter.get("sjhm")), "UTF-8");
		//联系人
		String pyzl = URLDecoder.decode(String.valueOf(parameter.get("pyzl")), "UTF-8");
		while (it.hasNext()) {
			FileItem item = it.next();
			if (item.isFormField()) {
				continue;
			} else {
				if (item.getSize() > 1024 * 1024 * 2) {
					response.setContentType("text/html;charset=UTF-8");
					response.getWriter().write("01");
				}
				fileItem = item;
			}
		}
		byte[] buffer = fileItem.get();
		DataObject dataObject1 = null;
		try {
			dataObject1 = FileUtils.upLoadFile(buffer, "FPCY/stample", System.currentTimeMillis() + "tmp.png");
			String url = dataObject1.getMap().get("URL").toString();
			Map param = new HashMap();
			param.put("STAMPLEID", getSequence(DmSequenceName.TEMP));
			param.put("IMAGEPATH", url);
			param.put("PHONENUMBER", sjhm);
			param.put("USERNAME", pyzl);
			DataWindow.insert("invoice.service.invoiceService_addStample", param);
			response.setContentType("text/html;charset=UTF-8");
			response.getWriter().write("00");
		} catch (Throwable e) {
			response.setContentType("text/html;charset=UTF-8");
			response.getWriter().write("99");
		}
	}

	@SuppressWarnings("rawtypes")
	private static Map strmap = new HashMap();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DataObject wxUploadInvoiceSample(DataObject dataObject) {
		Map<String, String> result = new HashMap<String, String>();
		try {
			Map parameter = dataObject.getMap();
			String sjhm = parameter.get("contactinformation").toString();
			String userName = String.valueOf(parameter.get("contacts"));
			String pyzl = null;
			if (parameter.get("contacts") == null) {
				pyzl = null;
			} else {
				pyzl = java.net.URLDecoder.decode(parameter.get("contacts").toString(), "UTF-8");
			}
			if (parameter.get("data").toString().equals(null)) {
				result.put("bizCode", "99");
			} else {
				String base64 = parameter.get("data").toString()/*
																 * .substring(index
																 * )
																 */;
				String step = parameter.get("step").toString();
				String fileid = parameter.get("id").toString();
				String strbase64 = "";
				if (step.equals("start")) {
					strmap.put(fileid, base64);
					result.put("bizCode", "00");
					return new DataObject(result);
				} else if (step.equals("continue")) {
					strmap.put(fileid, strmap.get(fileid) + base64);
					result.put("bizCode", "00");
					return new DataObject(result);
				} else if (step.equals("end")) {
					if (strmap.get(fileid) != null) {
						strmap.put(fileid, strmap.get(fileid) + base64);
					} else {
						strmap.put(fileid, base64);
					}
					strbase64 = (String) strmap.get(fileid);
					strmap.remove(fileid);
				}
				logger.debug("123......" + strbase64);
				String[] str = strbase64.split(",");
				BASE64Decoder decoder = new BASE64Decoder();
				byte[] b = {};
				if (str.length > 1) {
					b = decoder.decodeBuffer(str[1]);
				}
				for (int i = 0; i < b.length; ++i) {
					if (b[i] < 0) {// 调整异常数据
						b[i] += 256;
					}
				}
				byte imge[] = b;
				// 将图片上传aliyun
				DataObject dataObject1 = null;
				try {
					dataObject1 = FileUtils.upLoadFile(imge, "FPCY/stample", System.currentTimeMillis() + "tmp.png");
				} catch (Throwable e) {
					e.printStackTrace();
				}
				String url = dataObject1.getMap().get("URL").toString();
				Map param = new HashMap();
				param.put("STAMPLEID", getSequence(DmSequenceName.TEMP));
				param.put("IMAGEPATH", url);
				param.put("PHONENUMBER", sjhm);
				param.put("INVOICETYPE", pyzl);
				param.put("USERNAME", userName);
				DataWindow.insert("invoice.service.invoiceService_addStample", param);
			}

		} catch (UnsupportedEncodingException e) {
			logger.error("发票上传图片路径出错", e);
		} catch (IOException e) {
			logger.error("发票上传图片路径出错", e);
		}
		result.put("bizCode", "00");
		return new DataObject(result);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DataObject getSignature(DataObject dataObject) {// signatureUrl
		Map map = new HashMap();
		DataObject dataobj = IEDSProxy.doService("weixinGetSignatureService", dataObject, null);
		Map result1 = dataobj.getMap();
		Map result = (Map) result1.get("signatureData");
		map.put("time", result.get("timestamp"));
		map.put("appid", result.get("appid"));
		map.put("randomStr", result.get("nonceStr"));
		map.put("signature", result.get("signature"));
		return new DataObject(map);
	}
}
