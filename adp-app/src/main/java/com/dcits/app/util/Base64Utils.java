package com.dcits.app.util;

import java.io.IOException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Base64Utils {

	private static final Log LOG = LogFactory.getLog(Base64Utils.class);

	public static byte[] encode(byte[] src) {
		if (ArrayUtils.isEmpty(src)) {
			return null;
		}
		BASE64Encoder base64Encoder = new BASE64Encoder();
		try {
			return base64Encoder.encode(src).getBytes();
		} catch (Throwable e) {
			LOG.error(e);
		}
		return null;
	}

	public static byte[] deEncode(String src) {
		BASE64Decoder base64Decoder = new BASE64Decoder();
		try {
			return base64Decoder.decodeBuffer(src);
		} catch (IOException e) {
			LOG.error(e);
		}
		return null;
	}

}