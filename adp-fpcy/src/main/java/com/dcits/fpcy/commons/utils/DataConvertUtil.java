package com.dcits.fpcy.commons.utils;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser.Feature;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class DataConvertUtil {
	private static ObjectMapper objectMapper = new ObjectMapper();

	@SuppressWarnings("rawtypes")
	public static String MapToString(Map map) {
		if (map == null) {
			return "";
		}
		try {
			objectMapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true) ;
			return objectMapper.writeValueAsString(map);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	@SuppressWarnings("rawtypes")
	public static Map StringToMap(String msg) {
		try {
			objectMapper.configure(Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true) ;
			objectMapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true) ;
			return objectMapper.readValue(msg, Map.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new HashMap();
	}
}
