package com.dcits.fpcy.commons.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.apache.commons.jxpath.ri.compiler.Path;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONObject;

import com.dcits.fpcy.commons.bean.ResultBean;
import com.dcits.fpcy.commons.constant.SysConfig;

public class ResultUtils {
	/**
	 * 获取汉字首字母
	 * 
	 * @param str
	 * @return
	 * @throws BadHanyuPinyinOutputFormatCombination
	 */
	public static String getPinYinHeadChar(String str)
			throws BadHanyuPinyinOutputFormatCombination {
		String convert = "";
		for (int j = 0; j < str.length(); j++) {
			char word = str.charAt(j);
			String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word,
					new HanyuPinyinOutputFormat());
			if (pinyinArray != null) {
				convert += pinyinArray[0].charAt(0);
			} else {
				convert += word;
			}
		}
		return convert;
	}
	
	/**
	 * 
	 * @param key
	 * @param cxjg{拼音，value}
	 * @return
	 * @throws Exception
	 */
	public static List<ResultBean> getListInfoFromJson(String[] key, JSONObject cxjg) throws Exception{
		List<ResultBean> list = new ArrayList<ResultBean>();
		for(int i = 0;i<key.length;i++) {
			if(StringUtils.isNotBlank(cxjg.getString(ResultUtils.getPinYinHeadChar(key[i]).toUpperCase()))) {
				list.add(new ResultBean(ResultUtils.getPinYinHeadChar(key[i]).toUpperCase(), key[i], cxjg.getString(ResultUtils.getPinYinHeadChar(key[i]).toUpperCase())));
			}
		}
		return list;
	}
	/**
	 * 
	 * @param key
	 * @param cxjg{汉字，value}
	 * @return
	 * @throws Exception
	 */
	public static List<ResultBean> getListInfoFromJson1(String[] key, JSONObject cxjg) throws Exception{
		List<ResultBean> list = new ArrayList<ResultBean>();
		for(int i = 0;i<key.length;i++) {
			if(StringUtils.isNotBlank(cxjg.getString(key[i]))) {
				list.add(new ResultBean(ResultUtils.getPinYinHeadChar(key[i]).toUpperCase(), key[i], cxjg.getString(key[i])));
			}
		}
		return list;
	}
	@SuppressWarnings("rawtypes")
	public static List<ResultBean> getInfoFromMap(String[] key, Map map) throws Exception{
		List<ResultBean> list = new ArrayList<ResultBean>();
		for(int i = 0;i<key.length;i++) {
			if(StringUtils.isNotBlank((String) map.get(ResultUtils.getPinYinHeadChar(key[i]).toUpperCase()))) {
				list.add(new ResultBean(ResultUtils.getPinYinHeadChar(key[i]).toUpperCase(), key[i], map.get(ResultUtils.getPinYinHeadChar(key[i]).toUpperCase()).toString()));
			}
		}
		return list;
	}
	
	/**
	 * @Description:图片拼接 （注意：必须两张图片长宽一致哦）
	 * @param files
	 *            要拼接的文件列表
	 * @param type1
	 *            横向拼接， 2 纵向拼接
	 * @return
	 */
	@SuppressWarnings("unused")
	public static byte[] mergeImage(BufferedImage bi, BufferedImage bi1,
			int type) {
		int[][] ImageArrays = new int[2][];
		for (int i = 0; i < ImageArrays.length; i++) {
			if (i == 0) {
				int width = bi.getWidth();
				int height = bi.getHeight();
				ImageArrays[i] = new int[width * height];
				ImageArrays[i] = bi.getRGB(0, 0, width, height, ImageArrays[i],
						0, width);
			} else {
				int width = bi1.getWidth();
				int height = bi1.getHeight();
				ImageArrays[i] = new int[width * height];
				ImageArrays[i] = bi1.getRGB(0, 0, width, height,
						ImageArrays[i], 0, width);
			}

		}
		int newHeight = 0;
		int newWidth = 0;
		for (int i = 0; i < ImageArrays.length; i++) {
			if (i == 0) {
				newWidth = newWidth > bi.getWidth() ? newWidth : bi.getWidth();
				newHeight += bi.getHeight();
			} else {
				newWidth = newWidth > bi1.getWidth() ? newWidth : bi1
						.getWidth();
				newHeight += bi1.getHeight();

			}
		}
		if (type == 1 && newWidth < 1) {
			return null;
		}
		if (type == 2 && newHeight < 1) {
			return null;
		}
		ByteArrayOutputStream out = null;
		// 生成新图片
		try {
			BufferedImage ImageNew = new BufferedImage(newWidth, newHeight,
					BufferedImage.TYPE_INT_RGB);
			int height_i = 0;
			int width_i = 0;
			for (int i = 0; i < ImageArrays.length; i++) {
				if (i == 0) {
					ImageNew.setRGB(0, height_i, newWidth, bi.getHeight(),
							ImageArrays[i], 0, newWidth);
					height_i += bi.getHeight();
				} else {
					ImageNew.setRGB(0, height_i, newWidth, bi1.getHeight(),
							ImageArrays[i], 0, newWidth);
					height_i += bi1.getHeight();
				}

			}
			// 输出想要的图片
			//ImageIO.write(ImageNew, "png", new File("D:/image/" + "06.png"));
			out = new ByteArrayOutputStream();
			boolean flag = ImageIO.write(ImageNew, "gif", out);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return out.toByteArray();
	}
	
	public static File getFile(String index) {
		File file = null;
		String path = null;
		String folderPath = Path.class.getProtectionDomain()
				.getCodeSource().getLocation().getPath();
		if (folderPath.indexOf("WEB-INF") > 0) {
			path = folderPath.substring(0, folderPath.indexOf("WEB-INF"));
		}
		String filepath = path + "static/images/pyimage/";
		if (index.equals("00")) {
			file = new File(filepath+"00.png");
		} else if (index.equals("01")) {
			file = new File(filepath+"01.png");
		} else if (index.equals("02")) {
			file = new File(filepath+"02.png");
		} else {
			file = new File(filepath+"03.png");
		}
		return file;
	}
    
	/**
	 * 拼装返回结果
	 * @param result
	 * @param invoicefalseState
	 * @param systemfalseState
	 * @param cyjgState
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static  Map getResult(Map result, String invoicefalseState,
			String systemfalseState, String cyjgState,String msg) {
		List list = new ArrayList();
		result.put(SysConfig.INVOICEFALSESTATE,
				invoicefalseState);
		result.put(SysConfig.SYSTEMFALSESTATE, systemfalseState);
		result.put(SysConfig.CYJGSTATE, cyjgState);
		result.put("cwxx",msg);
		list.add(new ResultBean("cwxx", "", result.get("cwxx").toString()));
		result.put("list", list);
		return result;
	}
	
	/**
	 * 判读字符串是否为空
	 * @param str
	 * @return
	 */
	public static boolean isNull(String str) {// 判断字符串是否为空
		if ("".endsWith(str) || "null".equals(str)) {
			return true;
		} else {
			return false;
		}
	}
	
	 /**
	  * 将map的字符串转成正规的map对象 
	  * @param str
	  * @return
	  */
	public static Map<String, String> mapStringToMap(String str) {// 数据库取出的MAP型字符串转换回MAP
		Map<String, String> map = new HashMap<String, String>();
		if(isNull(str)){
			return map;
		}
		str = str.substring(1, str.length() - 1);
		String[] strs = str.split(",");
		
		for (String string : strs) {
			String[] m = string.split("=");
			if (m.length > 1) {
				String key = m[0].replace(" ", "");
				String value = m[1];
				map.put(key, value);
			} else {
				String key = m[0].replace(" ", "");
				String value = "";
				map.put(key, value);
			}
		}
		return map;
	}
	//格式化开票金额
	public static String getDecimalFormat(String  data){
		if(StringUtils.isNotEmpty(data) && "null"!=data){
			DecimalFormat df = new DecimalFormat("0.00");
			BigDecimal amount = new BigDecimal(Double.parseDouble(data));
			 return df.format(amount);
		}
		if( "null"!=data){
			data="";
		}
	    return data;
	}
}
