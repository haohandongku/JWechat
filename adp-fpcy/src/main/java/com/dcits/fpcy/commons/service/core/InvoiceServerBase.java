package com.dcits.fpcy.commons.service.core;

import java.util.Map;

import com.dcits.fpcy.commons.bean.TaxOfficeBean;


public interface InvoiceServerBase {
	    //查验验证码
		@SuppressWarnings("rawtypes")
		public  Map FPCY(Map parameter, TaxOfficeBean fpcyParas);
}
