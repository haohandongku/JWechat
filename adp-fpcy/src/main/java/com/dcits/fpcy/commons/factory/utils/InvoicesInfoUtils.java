package com.dcits.fpcy.commons.factory.utils;

import java.util.HashMap;
import java.util.Map;

public class InvoicesInfoUtils {
	private static Map<String, String> map;
	static {
		map = new HashMap<String, String>();
		
		map.put("cwxx", "errMsg");

		map.put("票种", "invoiceType");//加“发票”两个字
		map.put("查询次数", "times");
		map.put("(销售方)名称", "salesName");
		map.put("(销售方)纳税人识别号", "salerTaxNum");
		map.put("(销售方)地址.电话", "salerAddrAndTel");
		map.put("(销售方)开户行及账号", "salerAccount");
		map.put("(购买方)名称", "purchaserName");
		map.put("(购买方)纳税人识别号", "purchaserTaxNum");
		map.put("(购买方)地址.电话", "purchaserAddrAndTel");
		map.put("(购买方)开户行及账号", "purchaserAccount");
		map.put("税额", "tax");
		map.put("价税合计", "total");
		map.put("金额", "money");
		map.put("是否作废", "isVoid");
		map.put("备注", "remark");
		map.put("机器编号", "machineNum");
		map.put("校验码", "checkCode");
		map.put("开票日期", "billingTime");
		map.put("查询结果", "result");
		
		map.put("主管税务机关", "taxAuthority");
		map.put("不含税价", "money");
		map.put("购买方名称", "purchaseName");
		map.put("产地", "producingArea");
		map.put("车辆识别代号/车架号码", "carframeNum");
		map.put("销方纳税人识别号", "salerTaxNum");
		map.put("电话", "tel");
		map.put("增值税税率或征收率", "vatRate");
		map.put("增值税税额", "vatTax");
		map.put("限乘人数", "limitNum");
		map.put("发动机号码", "engineNum");
		map.put("厂牌型号", "factoryModel");
		map.put("开户银行", "bank");
		map.put("地址", "address");
		map.put("购买方身份证号码", "purchaserID");
		map.put("账号", "account");
		map.put("销货单位名称", "salerName");
		map.put("合格证号", "certificateNum");
		map.put("车辆类型", "vehicleType");
	}

	public static String getKey(String value) {
		return map.get(value);
	}
}
