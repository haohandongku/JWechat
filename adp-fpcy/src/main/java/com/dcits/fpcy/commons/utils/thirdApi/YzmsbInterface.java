package com.dcits.fpcy.commons.utils.thirdApi;


import org.apache.commons.lang.StringUtils;

import com.dcits.fpcy.commons.bean.TaxOfficeBean;


//多验证系统支持，更换验证码厂商时，需要修改数据库验证码厂商字段(yzmcs)
//另外，每个查验网址对应一个查验厂商，目的为了将来扩展方便。
public class YzmsbInterface {
	/**
	 * 如果第一次识别失败会重新识别 然后返回结果
	 * 
	 * @param filePath
	 * @param swjg_dm
	 * @param result
	 * @return
	 * @throws Exception
	 */
	public static String YZMSB(byte[] filePath, TaxOfficeBean fpcyParas)
			throws Exception {
		String returnVal = "";
		for (int i = 0; i < 3; i++) {
		 	if(fpcyParas.cy_yzmcs.equals("02")){//先走联众
		        returnVal = YZMSB_LZ.YZMSB(filePath, fpcyParas);
				if (StringUtils.isEmpty(returnVal)) {
					//returnVal = YZMSB_UU.YZMSB(filePath, fpcyParas);
					System.out.println("解析io流时出错");
				}
				if (!StringUtils.isEmpty(returnVal)) {
					break;
				}
		 	}
		}
		return returnVal;
	}
}
