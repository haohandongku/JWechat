package com.dcits.fpcy.commons.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.jxpath.ri.compiler.Path;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import com.dcits.fpcy.commons.dao.YzmDao;

public class AnalysisJS {
	private static Log logger = LogFactory.getLog(AnalysisJS.class);
	private final static ScriptEngineManager engineManager = new ScriptEngineManager();
	private final static ScriptEngine engine = engineManager
			.getEngineByName("javascript");
	private final static ScriptEngine engine2 = engineManager
			.getEngineByName("javascript");
	static {
		evalFile("base", "key");
		evalFile("m.q.d.min", "key");
		evalFile("q.b.a.min", "key");
		evalFile("s.d.b.min", "key");
		evalFile("t.q.b.min", "key");
		evalFile("t.q.d.min", "key");
		evalFile("t.q.e.min", "key");
		evalFile("t.q.z.min", "key");
		evalFile("aes", "role");
		evalFile("AesUtil", "role");
		evalFile("pbkdf2", "role");
	}

	private static void evalFile(String fileName, String type) {
		try {
			String path = "";
			String folderPath = Path.class.getProtectionDomain()
					.getCodeSource().getLocation().getPath();
			if (folderPath.indexOf("WEB-INF") > 0) {
				path = folderPath.substring(0, folderPath.indexOf("WEB-INF"));
			}
			String filepath = path + "static/js/";
			if ("role".equals(type)) {
				engine.eval(new FileReader(
						new File(filepath + fileName + ".js")));
			} else if ("key".equals(type)) {
				engine2.eval(new FileReader(new File(filepath + fileName
						+ ".js")));
			}
		} catch (Exception e) {
			logger.error("scrpitEngine调用出错。。。。。" + e.getMessage());
		}
	}

	/**
	 * 获取publicKey
	 * 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static String analysisJsKey(Map keyMap, String type,
			String isCoreTransformation) {
		String publicKey = "";
		String publickeyString = "";
		if ("yzm".equals(type)) {
			String fpdmyzm = (String) keyMap.get("fpdm");
			String nowtime = (String) keyMap.get("nowtime");
			publickeyString = "jQuery.ckcode('" + fpdmyzm + "','" + nowtime
					+ "');";
		} else if ("cy".equals(type)) {
			String fpdm = (String) keyMap.get("fpdm");
			String fphm = (String) keyMap.get("fphm");
			String kjje = (String) keyMap.get("fpje");
			String kprq = (String) keyMap.get("kprq");
			String yzmSj = (String) keyMap.get("yzmSj");
			String yzm = (String) keyMap.get("yzm");
			if ("0".equals(isCoreTransformation)) {
				publickeyString = "jQuery.ck('" + fpdm + "','" + fphm + "','"
						+ kprq + "','" + kjje + "','" + yzmSj + "','" + yzm
						+ "');";
			} else {
				publickeyString = "jQuery.ck('" + fpdm + "','" + fphm + "','"
						+ kjje + "','" + kprq + "','" + yzmSj + "','" + yzm
						+ "');";
			}

		}
		try {
			Object a = engine2.eval(publickeyString);
			publicKey = a.toString();
		} catch (Exception e) {
			logger.error("scrpitEngine调用出错。。。。。" + e.getMessage());
			e.printStackTrace();
		}
		return publicKey;
	}

	/**
	 * 新增接口
	 * 
	 * @param jsonData
	 * @param fplx
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map analysisNewJsAfter(String jsonData, Map parmterMap,
			String fplx) {
		String resultCode = "SUCCESS";
		Map resMap = new LinkedHashMap();
		Map retMap = new LinkedHashMap();
		long startTime = System.currentTimeMillis();
		try {
			Map paramMap = DataConvertUtil.StringToMap(jsonData);
			String fpxxs = (String) paramMap.get("key2");
			String hwxxs = (String) paramMap.get("key3");
			String key4 = (String) paramMap.get("key4");
			String fpxx[] = fpxxs.split("≡");
			resMap.put("invoiceDataCode", parmterMap.get("fpdm"));
			resMap.put("invoiceNumber", parmterMap.get("fphm"));
			resMap.put("invoiceTypeName", parmterMap.get("swjgmc"));
			resMap.put("invoiceTypeCode", fplx);
			resMap.put("checkDate", DateUtils.getTime());
			if ("01".equals(fplx)) {// 增值税专票
				/**
				 * 3700163130,01697380, 2017-02-22,95008.54 [4, 20171030,
				 * 北京恒昌利通投资管理有限公司, 911101135769049507,
				 * 北京市顺义区李桥镇顺通路李家桥段7号、010-57409174, 中国工商银行股份有限公司北京知春路支行
				 * 0200207919200070036, 上海吴卓资产管理有限公司, 91310113087932424R,
				 * 上海市宝山区顾太路380号223-25室 021-80376436, 工商银行上海宝山支行
				 * 1001233309005621277, 1567.76, 94.07, 1661.83, , 661505756039,
				 * 0, 72373720453179389178, N]
				 */
				String invs[] = { "checkNum", "billingTime", "purchaserName",
						"taxpayerNumber", "taxpayerAddressOrId",
						"taxpayerBankAccount", "salesName", "salesTaxpayerNum",
						"salesTaxpayerAddress", "salesTaxpayerBankAccount",
						"totalAmount", "totalTaxNum", "totalTaxSum",
						"invoiceRemarks", "taxDiskCode", "", "checkCode",
						"voidMark" };
				getAmap(invs, resMap, fpxx, fplx, parmterMap);
			} else if ("03".equals(fplx)) {// 增值税机动车统一票
				/**
				 * [2, 20171222, 499905800083, 傅露珍, 330724198912272626, ,
				 * 多用途乘用车, 哈弗牌CC6462RM01C, 保定, WAN047171007385, 88717.95, 无,
				 * 1725251475, LGWEF4A59HF598983, 无, 金华元盛长城汽车有限公司东阳分公司,
				 * 0579-86090853, 91330783096098339N, 0188992206000086,
				 * 浙江省东阳市东义路760号, 金华银行股份有限公司东阳世贸城支行, 17, 15082.05, 133078300,
				 * 103800, , 无, 5, N, 浙江省东阳市国家税务局, 1]
				 * 
				 * {"checkNum","billingTime","taxDiskCode","purchaserName",
				 * "taxpayerIdOrOrginCode",
				 * "taxpayerNumber","vehicleType","brandType"
				 * ,"producingArea","certifNumber","totalAmount",
				 * "inspectionOrder",
				 * "engineNumber","frameNumbr","importCertif",
				 * "salesName","salesTaxpayerTel","salesTaxpayerNum",
				 * "salesTaxpayerAccount"
				 * ,"salesTaxpayerAddress","salesTaxpayerBank"
				 * ,"taxRate","totalTaxNum",
				 * "taxOfficeName1","totalTaxSum","taxReceiptCode"
				 * ,"tonnage","limitNum","voidMark","taxOfficeName2",""} }
				 */
				String invs[] = { "checkNum", "billingTime", "taxDiskCode",
						"purchaserName", "taxpayerIdOrOrginCode",
						"taxpayerNumber", "vehicleType", "brandType",
						"producingArea", "certifNumber", "totalAmount",
						"inspectionOrder", "engineNumber", "frameNumbr",
						"importCertif", "salesName", "salesTaxpayerTel",
						"salesTaxpayerNum", "salesTaxpayerAccount",
						"salesTaxpayerAddress", "salesTaxpayerBank", "taxRate",
						"totalTaxNum", "taxOfficeName1", "totalTaxSum",
						"taxReceiptCode", "tonnage", "limitNum", "voidMark",
						"taxOfficeName2", "" };
				getAmap(invs, resMap, fpxx, fplx, parmterMap);
			} else if ("04".equals(fplx)) {// 增值税普通票
				/***
				 * 3300171320,39424510,2018-01-02,079363 [1, 20180102,
				 * 杭州及西贸易有限公司, 91330106MA28007TXG, 杭州市西湖区文三路553号浙江中小企业大厦2019室 .,
				 * 工商银行文三路支行1202012638500036277, 台州精筑建筑劳务分包有限公司,
				 * 91331001MA2AL21G7D, 浙江省台州市聚海大道4298号6号楼众创空间141号0576-88223250,
				 * , 80728661621584079363, 622.7, 16300, , 661618755374, ,
				 * 15677.3, N, 1] {
				 * "checkNum","billingTime","salesName","salesTaxpayerNum"
				 * ,"salesTaxpayerAddress",
				 * "salesTaxpayerBankAccount","purchaserName"
				 * ,"taxpayerNumber","taxpayerAddressOrId",
				 * "checkCode","totalTaxNum"
				 * ,"totalTaxSum","invoiceRemarks","taxDiskCode"
				 * ,"taxpayerBankAccount","totalAmount","voidMark",""}
				 * 
				 */
				String invs[] = { "checkNum", "billingTime", "salesName",
						"salesTaxpayerNum", "salesTaxpayerAddress",
						"salesTaxpayerBankAccount", "purchaserName",
						"taxpayerNumber", "taxpayerAddressOrId",
						"taxpayerBankAccount", "checkCode", "totalTaxNum",
						"totalTaxSum", "invoiceRemarks", "taxDiskCode", "",
						"totalAmount", "voidMark", "" };
				getAmap(invs, resMap, fpxx, fplx, parmterMap);
			} else if ("10".equals(fplx)) {// 增值税电子票
				/**
				 * 033001700111，29923381，2017-12-25，942635 [1, 20171225,
				 * 中国平安财产保险股份有限公司绍兴中心支公司, 91330600843010311M,
				 * 浙江省绍兴市延安东路328号天翼大厦1楼、3楼 0575-85222964,
				 * 工行牡丹支行1211016319200003278, 上海日铸实业有限公司, , , ,
				 * 10664322412520942635, 100.58, 1777, , 499099431227, 1676.42,
				 * N]
				 * 
				 */
				String invs[] = { "checkNum", "billingTime", "salesName",
						"salesTaxpayerNum", "salesTaxpayerAddress",
						"salesTaxpayerBankAccount", "purchaserName",
						"taxpayerNumber", "taxpayerAddressOrId",
						"taxpayerBankAccount", "checkCode", "totalTaxNum",
						"totalTaxSum", "invoiceRemarks", "taxDiskCode",
						"totalAmount", "voidMark" };
				getAmap(invs, resMap, fpxx, fplx, parmterMap);
			} else if ("11".equals(fplx)) {// 增值税卷票票
				/**
				 * 参数：061001700107，13991932，2017-12-29，510306 [1, 20171229,
				 * 中国石油天然气股份有限公司陕西咸阳销售分公司, 91610400221705805Q,
				 * ·陕西送变电工程公司西安电力安装工程处, 9161013322080211XD, 661712399708, 29.06,
				 * 200, , 62654378974169510306, 170.94, 冯建宏, N]
				 */
				String invs[] = { "checkNum", "billingTime", "salesName",
						"salesTaxpayerNum", "purchaserName", "taxpayerNumber",
						"taxDiskCode", "totalTaxNum", "totalTaxSum",
						"invoiceRemarks", "checkCode", "totalAmount",
						"goodsClerk", "voidMark" };
				getAmap(invs, resMap, fpxx, fplx, parmterMap);
			}
			// 发票代码
			// 处理发票详情信息
			if (StringUtils.isNotEmpty(StringUtils.trim(hwxxs))) {
				getNewHwxx(resMap, hwxxs, fplx);
			}
			if (StringUtils.isNotEmpty(key4)) {
				resMap.put("invoiceRemarks", key4.replace("<br/>", " ")
						.replace("<br/>", " "));
			}
			retMap.put("invoiceData", resMap);
		} catch (Exception e) {
			resultCode = "解析出错" + e.getMessage();
		}
		Map logMap = new HashMap();
		long endTime = System.currentTimeMillis();
		logMap.put("requestTime", String.valueOf(endTime - startTime));
		if ("SUCCESS".equals(resultCode)) {
			logMap.put("isSuccess", "Y");
		} else {
			logMap.put("isSuccess", "N");
		}
		logMap.put("invoiceCode", parmterMap.get("fpdm"));
		logMap.put("invoiceNum", parmterMap.get("fphm"));
		logMap.put("invoiceName", parmterMap.get("swjgmc"));
		logMap.put("requestType", "xjx");
		logMap.put("requestId", parmterMap.get("requestId"));
		logMap.put("requestConent", DataConvertUtil.MapToString(parmterMap));
		logMap.put("errorMsg", DataConvertUtil.MapToString(retMap));
		logMap.put("errorCode", resultCode);
	    YzmDao.saveRequestLog(logMap);
		return retMap;
	}

	/**
	 * 格式化数据
	 * 
	 * @param invs
	 * @param resMap
	 * @param fpxx
	 * @param fplx
	 * @param paramMap
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void getAmap(String invs[], Map resMap, String fpxx[],
			String fplx, Map paramMap) {
		for (int i = 0; i < invs.length; i++) {
			if ("voidMark".equals(invs[i])) {
				String zf = fpxx[i];
				if ("Y".equals(zf)) {
					zf = "1";
				} else {
					zf = "0";
				}
				resMap.put(invs[i], zf);
			} else if ("billingTime".equals(invs[i])) {
				String billingTime = fpxx[i].substring(0, 4) + "-"
						+ fpxx[i].substring(4, 6) + "-"
						+ fpxx[i].substring(6, 8);
				resMap.put(invs[i], billingTime);
			} else if ("taxRate".equals(invs[i])) {
				String taxRate = FormatSl(fpxx[i]);
				resMap.put(invs[i], taxRate);
			} else if ("totalTaxNum".equals(invs[i])
					|| "totalTaxSum".equals(invs[i])
					|| "totalAmount".equals(invs[i])) {
				String num = getJeToDot(fpxx[i]);
				resMap.put(invs[i], num);
			} else {
				if (!invs[i].isEmpty()) {
					resMap.put(invs[i], fpxx[i]);
				}
			}
		}
		if ("03".equals(fplx)) {
			resMap.put("taxOfficeName", resMap.get("taxOfficeName2") + " "
					+ resMap.get("taxOfficeName1"));
			resMap.remove("taxOfficeName1");
			resMap.remove("taxOfficeName2");
			resMap.put("machineCode", paramMap.get("fpdm"));
			resMap.put("machineNumber", paramMap.get("fphm"));
		} else if ("11".equals(fplx)) {
			resMap.put("machineNumber", paramMap.get("fphm"));
		}
	}

	/**
	 * 新增
	 * 
	 * @param xfsbh
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void getNewHwxx(Map map, String hwxxs, String fplx) {
		List list = new ArrayList();
		String hwii[] = hwxxs.split("▄");
		String qdFlog = "1";
		// 判断是不是清单票
		if (hwii.length > 1) {
			// 清单票
			qdFlog = "0";
		}
		// 清单票处理(大于8行)
		if ("0".equals(qdFlog)) {
			// 清单行
			String billLine = hwii[0];
			String fpxxw[] = billLine.split("≡");
			// 有折扣行发票
			if (fpxxw.length > 1) {
				billLine = fpxxw[0];
				// 清单行
				Map billLineMap = getNewFpxx(billLine, "0", fplx, 0);
				list.add(billLineMap);
			} else {
				// 清单行
				Map billLineMap = getNewFpxx(billLine, "0", fplx, 0);
				list.add(billLineMap);
			}
			// 非清单票(小于8行)
			String notBillLine = hwii[1];
			String notBillLines[] = notBillLine.split("▎");
			for (int i = 0; i < notBillLines.length; i++) {
				String oneNotBillLine = notBillLines[i];
				// 非清单行数据
				Map notBillLineMap = getNewFpxx(oneNotBillLine, "1", fplx,
						(i + 1));
				list.add(notBillLineMap);
			}
		} else {
			String hwinfo[] = hwxxs.split("≡");
			for (int i = 0; i < hwinfo.length; i++) {
				String oneNotBill = hwinfo[i];
				Map oneNotBillMap = getNewFpxx(oneNotBill, "0", fplx, (i + 1));
				list.add(oneNotBillMap);
			}
		}
		if ("0".equals(qdFlog)) {
			map.put("isBillMark", "Y");
		} else {
			map.put("isBillMark", "N");
		}
		map.put("invoiceDetailData", list);
	}

	/**
	 * 
	 * @param fpxx
	 * @param roleString1
	 *            //格式化货物名称
	 * @param roleString2
	 *            //该规则计算金额税额
	 * @param qdFlog
	 *            判断是否为清单列表
	 * @param fplx
	 *            发票类型
	 * @param lineNum
	 *            行数 有清单默认该行为 0
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Map getNewFpxx(String fpxx, String qdFlog, String fplx,
			int lineNum) {
		Map map = new LinkedHashMap();
		if (StringUtils.isNotEmpty(fpxx)) {
			map.put("lineNum", lineNum + "");
			String fpxxs[] = fpxx.split("█");
			// 货劳务名称
			String goodserviceName = "";
			// 型号
			String model = "";
			// 单位
			String unit = "";
			// 数量
			String number = "";
			// 单价
			String price = "";
			// 金额
			String sum = "";
			// 税率
			String taxRate = "";
			// 税额
			String tax = "";

			goodserviceName = fpxxs[0];

			/**
			 * 这边怕税局会单独处理，故这个分开处理 请注意
			 * getJeToDot(),一个参数是用来格式化数据，两个参数是用来补差规则（税局这里暂时处理价税合计，税额，金额）
			 */
			if ("0".equals(qdFlog)) {
				if ("01".equals(fplx)) {// 专用
					model = fpxxs[1];
					unit = fpxxs[2];
					number = getzeroDot(fpxxs[3]);
					price = fpxxs[4];
					sum = fpxxs[5];
					taxRate = FormatSl(fpxxs[6]);
					tax = getJeToDot(fpxxs[7]);
				} else if ("04".equals(fplx)) {// 普通
					model = fpxxs[1];
					unit = fpxxs[2];
					number = getzeroDot(fpxxs[3]);
					price = getJeToDot(fpxxs[4]);
					sum = getJeToDot(fpxxs[5]);
					taxRate = FormatSl(fpxxs[6]);
					tax = getJeToDot(fpxxs[7]);
				} else if ("10".equals(fplx)) {// 电子票
					model = fpxxs[1];
					unit = fpxxs[2];
					number = fpxxs[6];
					price = getJeToDot(fpxxs[4]);
					sum = getJeToDot(fpxxs[5]);
					if (fpxxs.length > 8) {
						if ("1".equals(fpxxs[8])) {
							taxRate = "免税";
						} else if ("2".equals(fpxxs[8])) {
							taxRate = "不征税";
						} else {
							taxRate = FormatSl(fpxxs[3]);
						}
						if ("3".equals(fpxxs[8]) || "1".equals(fpxxs[8])
								|| "2".equals(fpxxs[8])) {
							tax = "***";
						} else {
							tax = getzeroDot(getJeToDot(fpxxs[7].trim()));
						}
					} else {
						taxRate = FormatSl(fpxxs[3]);
						tax = getzeroDot(getJeToDot(fpxxs[7].trim()));
					}
				} else if ("11".equals(fplx)) {// 卷式票
					number = fpxxs[1];
					price = fpxxs[2];
					sum = fpxxs[3];
					taxRate = FormatSl(fpxxs[6]);
					tax = getJeToDot(fpxxs[7]);
				} else if ("14".equals(fplx)) {
					model = fpxxs[1];
					unit = fpxxs[2];
					number = getzeroDot(fpxxs[3]);
					price = fpxxs[4];
					sum = fpxxs[5];
					if (fpxxs.length > 8) {
						if ("1".equals(fpxxs[8])) {
							taxRate = "免税";
						} else if ("2".equals(fpxxs[8])) {
							taxRate = "不征税";
						} else {
							taxRate = FormatSl(fpxxs[6]);
						}
						if ("3".equals(fpxxs[8]) || "1".equals(fpxxs[8])
								|| "2".equals(fpxxs[8])) {
							tax = "***";
						} else {
							tax = getzeroDot(getJeToDot(fpxxs[7].trim()));
						}
					} else {
						taxRate = FormatSl(fpxxs[6]);
						tax = getzeroDot(getJeToDot(fpxxs[7].trim()));
					}
				}
				// 清单计算
			} else if ("1".equals(qdFlog)) {
				model = fpxxs[1];
				unit = fpxxs[2];
				number = getzeroDot(fpxxs[3]);
				price = getJeToDot(fpxxs[4]);
				sum = getJeToDot(fpxxs[5]);
				if ("04".equals(fplx)) {
					if (fpxxs.length > 8) {
						if ("1".equals(fpxxs[8])) {
							taxRate = "免税";
						} else if ("2".equals(fpxxs[8])) {
							taxRate = "不征税";
						} else {
							taxRate = FormatSl(fpxxs[6]);
						}
						if ("3".equals(fpxxs[8]) || "1".equals(fpxxs[8])
								|| "2".equals(fpxxs[8])) {
							tax = "***";
						} else {
							tax = getzeroDot(getJeToDot(fpxxs[7].trim()));
						}
					} else {
						taxRate = FormatSl(fpxxs[6]);
						tax = getzeroDot(getJeToDot(fpxxs[7].trim()));
					}
				}else{
					taxRate = FormatSl(fpxxs[6]);
					tax = getzeroDot(getJeToDot(fpxxs[7].trim()));
				}
			}
			map.put("goodserviceName", goodserviceName);
			map.put("model", model);
			map.put("unit", unit);
			map.put("number", number);
			map.put("price", price);
			map.put("sum", sum);
			map.put("taxRate", taxRate);
			map.put("tax", tax);
			if (0 == lineNum) {
				map.put("isBillLine", "Y");
			} else {
				map.put("isBillLine", "N");
			}
		}
		return map;
	}

	/**
	 * 请求后的处理
	 * 
	 * @param key8
	 * @param key7
	 * @param key9
	 * @param key4
	 * @return
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map analysisJsAfter(String stringsNeed, String jsonData,
			String rule, String fplx) {
		Map retMap = new HashMap();
		Map resultMap = new HashMap();
		String stringPrint = "var hwxxs='';var fplx='" + fplx
				+ "';var fpxxs='';var sort='';var bz='';";
		try {
			engine.eval("function Hello(){};"
					+ stringPrint
					+ " Hello.prototype.sayHello = function(){"
					+ "var aesUtil = new AesUtil(128, 100);"
					+ stringsNeed
					+ " var jsonData="
					+ jsonData
					+ ";"
					+ "var t = jsonData.key5;eval(t);var hwxx = jsonData.key3;"
					+ " var jmbz = '';if (jsonData.key4 != ' '|| jsonData.key4 != '') {jmbz = aesUtil.decrypt(jsonData.key8, jsonData.key7, jsonData.key9, jsonData.key4);} "
					+ "var jmsort = aesUtil.decrypt(jsonData.key8, jsonData.key7, jsonData.key9, jsonData.key10);"
					+ "var tt = jsonData.key6; eval(tt);"
					+ "var data=result;var tempno = data.template;"
					+ "if (tempno == 0) {fplx=data.fplx;hwxxs=data.hwxx;fpxxs=data.fpxx;}"
					+ "else if (tempno == 1) {fplx=data.f3ld;hwxxs=data.fdzx;fpxxs=data.h2gx;}"
					+ "else if (tempno == 2) {fplx=data.a3b0;hwxxs=data.eb2a;fpxxs=data.f8d7;}"
					+ "else if (tempno == 3) {fplx=data.c342;hwxxs=data.dbd2;fpxxs=data.d64b;}"
					+ "else if (tempno == 4) {fplx=data.af0b;hwxxs=data.c32a;fpxxs=data.a22a;}"
					+ "else if (tempno == 5) {fplx=data.ecae;hwxxs=data.c3c0;fpxxs=data.cb20;}"
					+ "else if (tempno == 6) {fplx=data.c3c8;hwxxs=data.a574;fpxxs=data.da20;}"
					+ "else if (tempno == 7) {fplx=data.dc02;hwxxs=data.cc66;fpxxs=data.ddbb;}"
					+ "else if (tempno == 8) {fplx=data.b3dd;hwxxs=data.c2b9;fpxxs=data.e72d;}"
					+ "else if (tempno == 9) {fplx=data.f16a;hwxxs=data.ceb5;fpxxs=data.a83e;}"
					+ "sort = data.sort;bz=jmbz;"
					+ " };var hello = new Hello();  hello.sayHello();");
			List list = new ArrayList();
			list.add("fplx");
			list.add("hwxxs");
			list.add("fpxxs");
			list.add("sort");
			list.add("bz");
			// 基本数据处理
			resultMap = getObjectToEngine(engine, list, rule);
			logger.debug("--传入的发票类型：" + fplx + ",----引擎输出的fplx:"
					+ resultMap.get("fplx"));
			// 再次处理
			Map resMap = checkMapFromRole(resultMap, rule, fplx);
			retMap.put("invoiceData", resMap);
		} catch (ScriptException e) {
			e.printStackTrace();
			logger.error("scrpitEngine调用出错。。。。。" + e.getMessage());
		}
		return retMap;
	}

	/**
	 * hu
	 * 
	 * @param engine
	 * @param list
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Map getObjectToEngine(ScriptEngine engine, List list,
			String rule) {
		Map map = new HashMap();
		for (int i = 0; i < list.size(); i++) {
			String object = (String) list.get(i);
			Object obj = engine.get(object);
			String str = getStringToObject(rule, object, obj);
			map.put(object, str);
		}
		return map;
	}

	/**
	 * 处理发票特殊字段
	 * 
	 * @param engine
	 * @param list
	 * @return
	 */
	private static String getStringToObject(String rule, String key,
			Object object) {
		String str = (String) object;
		String[] rules = rule.split("☺");
		String splitstr = rules[0];
		if ("fpxxs".equals(key)) {
			String fpxxs = str.replaceAll(splitstr, "≡");
			return fpxxs;
		} else if ("hwxxs".equals(key)) {
			String hwxxs = str.replaceAll(splitstr, "≡");
			return hwxxs;
		} else if ("bz".equals(key)) {
			String bz = str.replace("/\r\n/g", "<br/>").replace("/\n/g",
					"<br/>");
			return bz;
		}
		return str;
	}

	/**
	 * 处理发票中不同字段
	 * 
	 * @param engine
	 * @param list
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Map checkMapFromRole(Map map, String rule, String fplx) {
		Map resMap = new HashMap();
		// 规则循序
		String sort = (String) map.get("sort");
		// 发票基本信息
		String fpxxs = (String) map.get("fpxxs");
		logger.debug("获取的发票基本信息：" + fpxxs);
		// 发票详情
		String hwxxs = (String) map.get("hwxxs");
		logger.debug("获取的发票详情：" + hwxxs);
		// 备注
		String bz = (String) map.get("bz");
		String[] rules = rule.split("☺");
		String sortarray[] = sort.split("_");
		String tmpfpxx[] = fpxxs.split("≡");
		String cysj = tmpfpxx[tmpfpxx.length - 1];
		String tmpfp[] = new String[tmpfpxx.length - 4];
		for (int i = 3; i < tmpfpxx.length - 1; i++) {
			tmpfp[i - 3] = tmpfpxx[i];
		}
		String newfpxx[] = new String[tmpfpxx.length - 4];
		for (int i = 0; i < tmpfpxx.length - 4; i++) {
			newfpxx[i] = tmpfp[Integer.parseInt(sortarray[i])];
		}
		String newfpxxstr = tmpfpxx[0] + "≡" + tmpfpxx[1] + "≡" + tmpfpxx[2]
				+ "≡";
		for (int i = 0; i < newfpxx.length; i++) {
			newfpxxstr = newfpxxstr + newfpxx[i] + "≡";
		}
		fpxxs = newfpxxstr + cysj;
		String fpxx[] = fpxxs.split("≡");
		// 发票代码
		String fpdm = fpxx[0];
		resMap.put("invoiceDataCode", fpdm);
		// 发票号码
		String fphm = fpxx[1];
		resMap.put("invoiceNumber", fphm);
		// 发票种类(xx增值税专用发票)
		String fpcc = fpxx[2];
		resMap.put("invoiceTypeName", fpcc);
		resMap.put("invoiceTypeCode", fplx);
		// 查询次数
		String cycs = fpxx[3];
		resMap.put("checkNum", cycs);
		// 开票日期
		String kprq = fpxx[4];
		kprq = formatDate(kprq, rules[3]);
		resMap.put("billingTime", formatDate(kprq));
		if ("01".equals(fplx)) {// 增值税专票
			// 机器编号
			String jqbh = fpxx[17];
			resMap.put("taxDiskCode", jqbh);
			// 查验时间
			cysj = fpxx[21];
			resMap.put("checkDate", cysj);
			// 购方名称
			String gfmc = fpxx[5];
			resMap.put("purchaserName", gfmc);
			// 购方识别号
			String gfsbh = fpxx[6];
			resMap.put("taxpayerNumber", gfsbh);
			// 购方地址.电话
			String gfdzdh = fpxx[7];
			resMap.put("taxpayerAddressOrId", gfdzdh);
			// 购方开户行及账户
			String gfyhzh = fpxx[8];
			resMap.put("taxpayerBankAccount", gfyhzh);
			// 销方名称
			String xfmc = fpxx[9];
			resMap.put("salesName", xfmc);
			// 销方识别号
			String xfsbh = fpxx[10];
			xfsbh = formatSBH(xfsbh, rules[1]);
			resMap.put("salesTaxpayerNum", xfsbh);
			// 销方地址.电话
			String xfdzdh = fpxx[11];
			resMap.put("salesTaxpayerAddress", xfdzdh);
			// 销方开户行及账户
			String xfyhzh = fpxx[12];
			resMap.put("salesTaxpayerBankAccount", xfyhzh);
			// 金额
			String je = fpxx[13];
			je = getJeToDot(je, rules[2]);
			resMap.put("totalAmount", je);
			// 校验码
			String jym = fpxx[19];
			resMap.put("checkCode", jym);
			// 税额
			String se = fpxx[14];
			se = getJeToDot(se, rules[2]);
			resMap.put("totalTaxNum", se);
			// 价税合计
			String jshjxx = fpxx[15];
			jshjxx = getJeToDot(jshjxx, rules[2]);
			resMap.put("totalTaxSum", jshjxx);
			// 作废
			String zf = fpxx[20];
			if ("Y".equals(zf)) {
				zf = "1";
			} else {
				zf = "0";
			}
			resMap.put("voidMark", zf);

		} else if ("04".equals(fplx) || "02".equals(fplx)) {// 增值普通税票对应cygj04--321行货运票
			// 机器编号
			String jqbh = fpxx[17];
			resMap.put("taxDiskCode", jqbh);
			// 查验时间
			cysj = fpxx[21];
			resMap.put("checkDate", cysj);
			// 销方名称
			String xfmc = fpxx[5];
			resMap.put("salesName", xfmc);
			// 销方识别号
			String xfsbh = fpxx[6];
			xfsbh = formatSBH(xfsbh, rules[1]);
			resMap.put("salesTaxpayerNum", xfsbh);
			// 销方地址.电话
			String xfdzdh = fpxx[7];
			resMap.put("salesTaxpayerAddress", xfdzdh);
			// 销方开户行及账户
			String xfyhzh = fpxx[8];
			resMap.put("salesTaxpayerBankAccount", xfyhzh);
			// 购方名称
			String gfmc = fpxx[9];
			resMap.put("purchaserName", gfmc);
			// 购方识别号
			String gfsbh = fpxx[10];
			resMap.put("taxpayerNumber", gfsbh);
			// 购方地址.电话
			String gfdzdh = fpxx[11];
			resMap.put("taxpayerAddressOrId", gfdzdh);
			// 购方开户行及账户
			String gfyhzh = fpxx[12];
			resMap.put("taxpayerBankAccount", gfyhzh);
			// 金额
			String je = fpxx[19];
			je = getJeToDot(je, rules[2]);
			resMap.put("totalAmount", je);
			// 校验码
			String jym = fpxx[13];
			resMap.put("checkCode", jym);
			// 税额
			String se = fpxx[14];
			se = getJeToDot(se, rules[2]);
			resMap.put("totalTaxNum", se);
			// 价税合计
			String jshjxx = fpxx[15];
			jshjxx = getJeToDot(jshjxx, rules[2]);
			resMap.put("totalTaxSum", jshjxx);
			// 作废
			String zf = fpxx[20];
			if ("Y".equals(zf)) {
				zf = "1";
			} else {
				zf = "0";
			}
			resMap.put("voidMark", zf);
		} else if ("10".equals(fplx)) {// 电子发票税票对应cygj10--296行
			// 机器编号
			String jqbh = fpxx[17];
			resMap.put("taxDiskCode", jqbh);
			// 查验时间
			cysj = fpxx[20];
			resMap.put("checkDate", cysj);
			// 销方名称
			String xfmc = fpxx[5];
			resMap.put("salesName", xfmc);
			// 销方识别号
			String xfsbh = fpxx[6];
			xfsbh = formatSBH(xfsbh, rules[1]);
			resMap.put("salesTaxpayerNum", xfsbh);
			// 销方地址.电话
			String xfdzdh = fpxx[7];
			resMap.put("salesTaxpayerAddress", xfdzdh);
			// 销方开户行及账户
			String xfyhzh = fpxx[8];
			resMap.put("salesTaxpayerBankAccount", xfyhzh);
			// 购方名称
			String gfmc = fpxx[9];
			resMap.put("purchaserName", gfmc);
			// 购方识别号
			String gfsbh = fpxx[10];
			resMap.put("taxpayerNumber", gfsbh);
			// 购方地址.电话
			String gfdzdh = fpxx[11];
			resMap.put("taxpayerAddressOrId", gfdzdh);
			// 购方开户行及账户
			String gfyhzh = fpxx[12];
			resMap.put("taxpayerBankAccount", gfyhzh);
			// 金额
			String je = fpxx[18];
			je = getJeToDot(je, rules[2]);
			resMap.put("totalAmount", je);
			// 校验码
			String jym = fpxx[13];
			resMap.put("checkCode", jym);
			// 税额
			String se = fpxx[14];
			se = getJeToDot(se, rules[2]);
			resMap.put("totalTaxNum", se);
			// 价税合计
			String jshjxx = fpxx[15];
			jshjxx = getJeToDot(jshjxx, rules[2]);
			resMap.put("totalTaxSum", jshjxx);
			// 作废
			String zf = fpxx[19];
			if ("Y".equals(zf)) {
				zf = "1";
			} else {
				zf = "0";
			}
			resMap.put("voidMark", zf);
		} else if ("03".equals(fplx)) {// 机动车发票
			// 查验时间
			cysj = fpxx[33];
			resMap.put("checkData", cysj);
			// 机打代码
			String jddm = fpxx[0];
			resMap.put("machineCode", jddm);
			// 机打号码
			String jdhm = fpxx[1];
			resMap.put("machineNumber", jdhm);
			// 机器编号
			String jqbm = fpxx[5];
			resMap.put("taxDiskCode", jqbm);
			// 购方单位
			String ghdw = fpxx[6];
			resMap.put("purchaserName", ghdw);
			// 身份证号码/组织机构代码
			String sfzhm = fpxx[7];
			resMap.put("taxpayerIdOrOrginCode", sfzhm);
			// 购方纳税人识别号
			String gfsbh = fpxx[8];
			resMap.put("taxpayerNumber", gfsbh);
			// 车辆类型
			String cllx = fpxx[9];
			resMap.put("vehicleType", cllx);
			// 厂牌类型
			String cpxh = fpxx[10];
			resMap.put("brandType", cpxh);
			// 产地
			String cd = fpxx[11];
			resMap.put("producingArea", cd);
			// 合格证号
			String hgzs = fpxx[12];
			resMap.put("certifNumber", hgzs);
			// 价 税 合 计
			String jshjxx = fpxx[27];
			jshjxx = getJeToDot(jshjxx, rules[2]);
			resMap.put("totalTaxSum", jshjxx);
			// 商检单号
			String sjdh = fpxx[14];
			resMap.put("inspectionOrder", sjdh);
			// 发动机号码
			String fdjhm = fpxx[15];
			resMap.put("engineNumber", fdjhm);
			// 车辆识别代号/车架号码
			String cjhm = fpxx[16];
			resMap.put("frameNumbr", cjhm);
			// 进口证明书号
			String jkzmsh = fpxx[17];
			resMap.put("importCertif", jkzmsh);
			// 销货单位名称
			String xhdwmc = fpxx[18];
			resMap.put("salesName", xhdwmc);
			// 电话
			String dh = fpxx[19];
			resMap.put("salesTaxpayerTel", dh);
			// 纳税人识别号
			String nsrsbh = fpxx[20];
			nsrsbh = formatSBH(nsrsbh, rules[1]);
			resMap.put("salesTaxpayerNum", nsrsbh);
			// 账号
			String zh = fpxx[21];
			resMap.put("salesTaxpayerAccount", zh);
			// 地 址
			String dz = fpxx[22];
			resMap.put("salesTaxpayerAddress", dz);
			// 开户银行
			String khyh = fpxx[23];
			resMap.put("salesTaxpayerBank", khyh);
			// 增值税税率/或 征 收 率
			String zzssl = fpxx[24] + "%";
			resMap.put("taxRate", zzssl);
			// 增值税/税额
			String zzsse = fpxx[25];
			zzsse = getJeToDot(zzsse, rules[2]);
			resMap.put("totalTaxNum", zzsse);
			// 主管税务机关及代码
			String swjg = fpxx[32] + " " + fpxx[26];
			resMap.put("taxOfficeName", swjg);
			// 不 含 税 价(金额)
			String cjfy = fpxx[13];
			cjfy = getJeToDot(cjfy, rules[2]);
			resMap.put("totalAmount", cjfy);
			// 完税凭证号码
			String wspzhm = fpxx[28];
			resMap.put("taxReceiptCode", wspzhm);
			// 吨位
			String dw = fpxx[29];
			resMap.put("tonnage", dw);
			// 限乘人数
			String xcrs = fpxx[30];
			resMap.put("limitNum", xcrs);
			// 是否作废
			String zfbz = fpxx[31];
			if ("Y".equals(zfbz)) {
				zfbz = "1";
			} else {
				zfbz = "0";
			}
			resMap.put("voidMark", zfbz);
		} else if ("11".equals(fplx)) { // 卷式普票
			// 机打号码(同发票号码相同)
			resMap.put("machineNumber", fphm);
			// 查验时间
			cysj = fpxx[17];
			resMap.put("checkDate", cysj);
			// 销方名称
			String xfmc = fpxx[5];
			resMap.put("salesName", xfmc);
			// 销方识别号
			String xfsbh = fpxx[6];
			xfsbh = formatSBH(xfsbh, rules[1]);
			resMap.put("salesTaxpayerNum", xfsbh);
			// 购方名称
			String gfmc = fpxx[7];
			resMap.put("purchaserName", gfmc);
			// 购方识别号
			String gfsbh = fpxx[8];
			resMap.put("taxpayerNumber", gfsbh);
			// 机器编号
			String jqbm = fpxx[9];
			resMap.put("taxDiskCode", jqbm);
			// 价税合计
			String jshjxx = fpxx[11];
			jshjxx = getJeToDot(jshjxx, rules[2]);
			resMap.put("totalTaxSum", jshjxx);
			// 校验码
			String jym = fpxx[13];
			resMap.put("checkCode", jym);
			// 收货员
			String shy = fpxx[15];
			resMap.put("goodsClerk", shy);
			// 作废标志
			String zf = fpxx[16];
			if ("Y".equals(zf)) {
				zf = "1";
			} else {
				zf = "0";
			}
			resMap.put("voidMark", zf);
		}
		resMap.put("invoiceRemarks", bz);
		// 处理发票详情信息
		if (StringUtils.isNotEmpty(StringUtils.trim(hwxxs))) {
			getHwxx(resMap, hwxxs, rules[4], rules[2], fplx);
		}
		return resMap;
	}

	/**
	 * 请求前准备的值
	 * 
	 * @param key8
	 * @param key7
	 * @param key9
	 * @param key4
	 * @return
	 * @throws ScriptException
	 */
	public static String getJsSaltAndIv() throws ScriptException {
		String tojs = "";
		tojs = (String) engine
				.eval("CryptoJS.lib.WordArray.random(128 / 8).toString(CryptoJS.enc.Hex);");
		return tojs;
	}

	/**
	 * 格式开票日期
	 * 
	 * @param kprq
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private static String formatDate(String time, String roleString) {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		Date date = null;
		try {
			date = format.parse(time);
			date.setDate(date.getDate() + (0 - Integer.parseInt(roleString)));
			time = format.format(date);
		} catch (ParseException e) {
		}
		return time;
	}

	/**
	 * 格式识别号
	 * 
	 * @param xfsbh
	 * @return
	 */
	private static String formatSBH(String xfsbh, String roleString) {
		String str[] = roleString.split("_");
		for (int i = 0; i < str.length; i++) {
			xfsbh = chgchar(xfsbh, str[i]);
		}
		return xfsbh;
	}

	private static String chgchar(String xfsbh, String string) {
		String a = string.charAt(2) + "";
		String b = string.charAt(0) + ""; // 反向替换，所以和java中是相反的
		xfsbh = xfsbh.replaceAll(a, "#");
		xfsbh = xfsbh.replaceAll(b, "%");
		xfsbh = xfsbh.replaceAll("#", b);
		xfsbh = xfsbh.replaceAll("%", a);
		return xfsbh;
	}

	/**
	 * 格式化money(不加规则)
	 * 
	 * @param xfsbh
	 * @return
	 */
	private static String getJeToDot(String je, String roleString) {
		if (je != "undefined" && je != "" && je != null && je != " ") {
			return getJeToDot(accAdd(je, roleString));
		} else {
			je = "0";
			return je;
		}
	}

	/**
	 * 格式化money(不加规则)
	 * 
	 * @param xfsbh
	 * @return
	 */
	private static String getJeToDot(String je) {
		if (StringUtils.isNotBlank(je)) {
			if (je.trim().equals("-")) {
				return je;
			}
			je = je.trim() + "";
			if (je.length() > 1) {
				if (je.substring(0, 1).equals(".")) {
					je = '0' + '.' + je.substring(1, je.length());
					return je;
				}
			}
			int index = je.indexOf(".");
			if (index < 0) {
				je += ".00";
				return je;
			}
			String jes[] = je.split("\\.");
			if (jes.length == 2 && jes[1].length() == 1) {
				je += "0";
				return je;
			}
			if (je.contains("-.")) {
				je = "-0." + je.substring(2, je.length());
				return je;
			}
			return je;
		} else {
			return je;
		}
	}

	/**
	 * 格式单价
	 * 
	 * @param je
	 * @return
	 */
	private static String getzeroDot(String je) {
		if (StringUtils.isNotBlank(je)) {
			je = je.trim() + "";
			if (je.length() > 1) {
				if (je.substring(0, 1).equals(".")) {
					je = '0' + '.' + je.substring(1, je.length());
					return je;
				}
			}
			if (je.contains("-.")) {
				je = "-0." + je.substring(2, je.length());
				return je;
			}
		}
		return je;
	}

	/**
	 * 计算金额
	 * 
	 * @param key8
	 * @param key7
	 * @param key9
	 * @param key4
	 * @return
	 */
	public static String accAdd(String je, String roleString) {
		if (StringUtils.isBlank(roleString)) {
			roleString = "0";
		}
		BigDecimal je1 = new BigDecimal(je);
		BigDecimal roleString1 = new BigDecimal(roleString);
		return String.valueOf(je1.add(roleString1));
	}

	/**
	 * 处理发票信息详情
	 * 
	 * @param xfsbh
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void getHwxx(Map map, String hwxxs, String roleString1,
			String roleString2, String fplx) {
		List list = new ArrayList();
		String hwii[] = hwxxs.split("▄");
		String qdFlog = "1";
		// 判断是不是清单票
		if (hwii.length > 1) {
			// 清单票
			qdFlog = "0";
		}
		// 清单票处理(大于8行)
		if ("0".equals(qdFlog)) {
			// 清单行
			String billLine = hwii[0];
			String fpxxw[] = billLine.split("≡");
			// 有折扣行发票
			if (fpxxw.length > 1) {
				billLine = fpxxw[0];
				// 清单行
				Map billLineMap = getFpxx(billLine, roleString1, null, "0",
						fplx, 0);
				list.add(billLineMap);
			} else {
				// 清单行
				Map billLineMap = getFpxx(billLine, roleString1, null, "0",
						fplx, 0);
				list.add(billLineMap);
			}
			// 非清单票(小于8行)
			String notBillLine = hwii[1];
			String notBillLines[] = notBillLine.split("▎");
			for (int i = 0; i < notBillLines.length; i++) {
				String oneNotBillLine = notBillLines[i];
				// 非清单行数据
				Map notBillLineMap = getFpxx(oneNotBillLine, roleString1,
						roleString2, "1", fplx, (i + 1));
				list.add(notBillLineMap);
			}
		} else {
			String hwinfo[] = hwxxs.split("≡");
			for (int i = 0; i < hwinfo.length; i++) {
				String oneNotBill = hwinfo[i];
				Map oneNotBillMap = getFpxx(oneNotBill, roleString1,
						roleString2, "0", fplx, (i + 1));
				list.add(oneNotBillMap);
			}
		}
		if ("0".equals(qdFlog)) {
			map.put("isBillMark", "Y");
		} else {
			map.put("isBillMark", "N");
		}
		map.put("invoiceDetailData", list);
	}

	/**
	 * 
	 * @param fpxx
	 * @param roleString1
	 *            //格式化货物名称
	 * @param roleString2
	 *            //该规则计算金额税额
	 * @param qdFlog
	 *            判断是否为清单列表
	 * @param fplx
	 *            发票类型
	 * @param lineNum
	 *            行数 有清单默认该行为 0
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Map getFpxx(String fpxx, String roleString1,
			String roleString2, String qdFlog, String fplx, int lineNum) {
		Map map = new LinkedHashMap();
		if (StringUtils.isNotEmpty(fpxx)) {
			map.put("lineNum", lineNum + "");
			String fpxxs[] = fpxx.split("█");
			if (fpxxs.length == 8) {
				// 货劳务名称
				String goodserviceName = "";
				// 型号
				String model = "";
				// 单位
				String unit = "";
				// 数量
				String number = "";
				// 单价
				String price = "";
				// 金额
				String sum = "";
				// 税率
				String taxRate = "";
				// 税额
				String tax = "";
				goodserviceName = fpxxs[0];
				goodserviceName = goodserviceName.replaceAll(roleString1, "");
				/**
				 * 这边怕税局会单独处理，故这个分开处理 请注意
				 * getJeToDot(),一个参数是用来格式化数据，两个参数是用来补差规则（税局这里暂时处理价税合计，税额，金额）
				 */
				if ("0".equals(qdFlog)) {
					if ("01".equals(fplx)) {// 专用
						model = fpxxs[1];
						unit = fpxxs[2];
						number = getzeroDot(fpxxs[3]);
						price = fpxxs[4];
						sum = fpxxs[5];
						taxRate = FormatSl(fpxxs[6]);
						tax = getJeToDot(fpxxs[7]);
					} else if ("04".equals(fplx)) {// 普通
						model = fpxxs[1];
						unit = fpxxs[2];
						number = getzeroDot(fpxxs[3]);
						price = getJeToDot(fpxxs[4]);
						sum = getJeToDot(fpxxs[5]);
						taxRate = FormatSl(fpxxs[6]);
						tax = getJeToDot(fpxxs[7]);
					} else if ("10".equals(fplx)) {// 电子票
						model = fpxxs[1];
						unit = fpxxs[2];
						number = fpxxs[6];
						price = getJeToDot(fpxxs[4]);
						sum = getJeToDot(fpxxs[5]);
						taxRate = FormatSl(fpxxs[3]);
						tax = getJeToDot(fpxxs[7]);
					} else if ("11".equals(fplx)) {// 卷式票
						number = fpxxs[1];
						price = fpxxs[2];
						sum = fpxxs[3];
						taxRate = FormatSl(fpxxs[6]);
						tax = getJeToDot(fpxxs[7]);
					}
					// 清单计算
				} else if ("1".equals(qdFlog)) {
					model = fpxxs[1];
					unit = fpxxs[2];
					number = getzeroDot(fpxxs[3]);
					price = getJeToDot(fpxxs[4]);
					sum = getJeToDot(fpxxs[5]);
					taxRate = FormatSl(fpxxs[6]);
					tax = getJeToDot(fpxxs[7]);
				}
				map.put("goodserviceName", goodserviceName);
				map.put("model", model);
				map.put("unit", unit);
				map.put("number", number);
				map.put("price", price);
				map.put("sum", sum);
				map.put("taxRate", taxRate);
				map.put("tax", tax);
				if (0 == lineNum) {
					map.put("isBillLine", "Y");
				} else {
					map.put("isBillLine", "N");
				}
			}
		}
		return map;
	}

	private static String formatDate(String date) {
		if (!date.contains("-")) {
			String year = date.substring(0, 4);
			String month = date.substring(4, 6);
			String day = date.substring(6, 8);
			date = year + "-" + month + "-" + day;
		}
		return date;
	}

	/**
	 * 格式化税率
	 * 
	 * @param date
	 * @return
	 */
	private static String FormatSl(String data) {
		data = StringUtils.trim(data);
		if (data.contains(".")) {
			BigDecimal je1 = new BigDecimal(data);
			BigDecimal je2 = new BigDecimal("100");
			data = je1.multiply(je2) + "";
		}
		if (data.length() > 0) {
			data = data + "%";
		} else {
			data = "";
		}
		return data;
	}

	/*public static void main(String[] args) {
		String jsonData = "{\"key1\":\"001\",\"key2\":\"3≡20180104≡北京京东世纪信息技术有限公司≡91110302562134916R≡北京市北京经济技术开发区科创十四街99号2幢B178室 62648622≡交行北京海淀支行 110060576018150093527≡61001部队≡ ≡ ≡ ≡76878999940608285559≡19.12≡131.6≡≡661616325258≡112.48≡N\",\"key3\":\"清风（app）抽纸 黑曜系列 3层130抽3盒█无█ █17█33.24786325█199.49█6█33.91≡清风（app）抽纸 黑曜系列 3层130抽3盒█ █ █17█0█-87.01█0█-14.79\",\"key4\":\"订单号:70171579967,\",\"key5\":\"1\"}";
		Map parmterMap = new HashMap();
		parmterMap.put("fpdm", "012001700211");
		parmterMap.put("fphm", "09669640");
		parmterMap.put("swjgmc", "天津增值税（电子普通发票）");
		System.out.println(new DataObject(analysisNewJsAfter(jsonData,
				parmterMap, "10")));
	}*/
}
