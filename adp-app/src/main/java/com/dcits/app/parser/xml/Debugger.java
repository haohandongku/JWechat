package com.dcits.app.parser.xml;

import com.dcits.app.exception.BizRuntimeException;

public class Debugger {

	public static void ASSERT(boolean c) {
		ASSERT(c, "断言出错");
	}

	public static void ASSERT(boolean c, String errMsg) {
		if (!c) {
			throw new BizRuntimeException(errMsg);
		}
	}

}