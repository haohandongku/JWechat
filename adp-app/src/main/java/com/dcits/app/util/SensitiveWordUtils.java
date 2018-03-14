package com.dcits.app.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class SensitiveWordUtils {

	private String ENCODING = "UTF-8";
	private PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
	private static Map<String, String> sensitiveWordMap;
	public static int minMatchType = 1;
	public static int maxMatchType = 2;

	public SensitiveWordUtils() {

	}

	public static String replaceSensitiveWord(String txt, int matchType,
			String replaceChar) {
		String resultTxt = txt;
		List<String> list = getSensitiveWord(txt, matchType);
		String replaceStr = "";
		if (CollectionUtils.isNotEmpty(list)) {
			for (String word : list) {
				for (int i = 0; i < word.length(); i++) {
					replaceStr = replaceStr + replaceChar;
				}
				resultTxt = resultTxt.replaceAll(word, replaceStr);
			}
		}
		return resultTxt;
	}

	private static List<String> getSensitiveWord(String txt, int matchType) {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < txt.length(); i++) {
			int matchNum = checkSensitiveWord(txt, i, matchType);
			if (matchNum > 0) {
				list.add(txt.substring(i, i + matchNum));
				i = i + matchNum - 1;
			}
		}
		return list;
	}

	@SuppressWarnings("rawtypes")
	private static int checkSensitiveWord(String txt, int beginIndex,
			int matchType) {
		boolean flag = false;
		int matchNum = 0;
		char c = 0;
		Map tempMap = sensitiveWordMap;
		for (int i = beginIndex; i < txt.length(); i++) {
			c = txt.charAt(i);
			tempMap = (Map) tempMap.get(c);
			if (tempMap != null) {
				matchNum++;
				if ("1".equals(tempMap.get("isEnd"))) {
					flag = true;
					if (minMatchType == matchType) {
						break;
					}
				}
			} else {
				break;
			}
		}
		if (matchNum < 2 || !flag) {
			matchNum = 0;
		}
		return matchNum;
	}

	public void init() throws Throwable {
		Resource resource = patternResolver
				.getResource("classpath:/sensitiveWord.txt");
		if (resource == null) {
			return;
		}
		InputStream inputStream = resource.getInputStream();
		InputStreamReader inputStreamReader = new InputStreamReader(
				inputStream, ENCODING);
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		List<String> list = new ArrayList<String>();
		String word = null;
		while ((word = bufferedReader.readLine()) != null) {
			list.add(word);
		}
		bufferedReader.close();
		addSensitiveWordToHashMap(list);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addSensitiveWordToHashMap(List<String> list) {
		if (CollectionUtils.isNotEmpty(list)) {
			HashSet<String> hashSet = new HashSet<String>(list);
			list.clear();
			list.addAll(hashSet);
			sensitiveWordMap = new HashMap<String, String>(list.size());
			Map tempMap = null;
			Map<String, String> newWordMap = null;
			for (String word : list) {
				tempMap = sensitiveWordMap;
				for (int i = 0; i < word.length(); i++) {
					char c = word.charAt(i);
					Object object = tempMap.get(c);
					if (object != null) {
						tempMap = (Map) object;
					} else {
						newWordMap = new HashMap<String, String>();
						newWordMap.put("isEnd", "0");
						tempMap.put(c, newWordMap);
						tempMap = newWordMap;
					}
					if (i == word.length() - 1) {
						tempMap.put("isEnd", "1");
					}
				}
			}
		}
	}

}
