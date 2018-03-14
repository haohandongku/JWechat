package com.dcits.app.util;

import java.util.HashMap;
import java.util.Map;

import com.dcits.app.dao.DataWindow;

public class XtSvcUtils {

	public static final String PUBLIC = "PUBLIC";

	public static String getXtcs(String csxh) {
		return getXtcs(csxh, PUBLIC);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static String getXtcs(String csxh, String jgbm) {
		Map parameter = new HashMap();
		parameter.put("CSXH", csxh);
		parameter.put("JGBM", jgbm);
		DataWindow dw = DataWindow.query("app.XtSvcUtils_getXtcs", parameter);
		return dw.getItemAny(0, "CSNR").toString();
	}

}