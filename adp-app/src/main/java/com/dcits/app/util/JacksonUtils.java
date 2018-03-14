package com.dcits.app.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser.Feature;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;

public class JacksonUtils {

	private static ObjectMapper objectMapper = new ObjectMapper();

	static {
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
		objectMapper.configure(Feature.ALLOW_SINGLE_QUOTES, true);
		objectMapper.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		objectMapper.getSerializerProvider().setNullValueSerializer(
				new JsonSerializer<Object>() {
					public void serialize(Object value,
							JsonGenerator jsonGenerator,
							SerializerProvider provider) throws IOException,
							JsonProcessingException {
						jsonGenerator.writeString("");
					}
				});
	}

	@SuppressWarnings("rawtypes")
	public static Map getMapFromJson(String json) throws JsonParseException,
			JsonMappingException, IOException {
		return objectMapper.readValue(json, Map.class);
	}

	@SuppressWarnings("rawtypes")
	public static String getJsonFromMap(Map map)
			throws JsonGenerationException, JsonMappingException, IOException {
		String jsonString = objectMapper.writeValueAsString(map);
		return jsonString;
	}

}