package com.dcits.fpcy.commons.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dcits.app.dao.DataWindow;
import com.dcits.app.data.DataObject;

public class FilePath {
	private static Random rand = new Random(1000);
	private static Log logger = LogFactory.getLog(FilePath.class);
	private static String fileDir=System.getProperty("user.dir");
	private static String fileSep=System.getProperty("file.separator");
	private static String yzm = "yzm";
	private static boolean di = false;
	/**
	 * 生成图片名称
	 * @param tplx 图片类型
	 * @return
	 */
	public static String GetFileName(String tplx) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSS");
		String filepath = sdf.format(date)+ Integer.toString(rand.nextInt(1000));
		if (di == false) {
			CreateFileUtil.createDir(fileDir +fileSep+yzm);
			di = true;
		}
		return fileDir + fileSep + yzm + fileSep + filepath + "." + tplx;
	}
	
	public static String GetPYName(String pyname) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSS");
		String filepath = sdf.format(date)+ Integer.toString(rand.nextInt(1000));
		if (di == false) {
			CreateFileUtil.createDir(fileDir +fileSep+yzm);
			di = true;
		}
		return fileDir + fileSep + "py" + fileSep + filepath+pyname;
	}

	/**
	 * 删除从税务局网站下载的验证码图片文件
	 * @param filepath 文件路径
	 * @return
	 */
	public static boolean DelFile(String filepath) {
		try {
			File file = new File(filepath);
			return file.delete();
		} catch (Exception e) {
			logger.error("删除图片失败", e);
			return false;
		}
	}

	/**
	 * @param args
	 */
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void main(String[] args) {
		Map map = new HashMap<String, String>();
		map.put("uid", "000000000000");
		DataObject dataobject = new DataObject(map);
		doService1(dataobject);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	public static DataObject doService1(DataObject dataobject) {
		Map map = dataobject.getMap();
		//String userEmailUrl = map.get("userEmailUrl").toString();
		String uid = map.get("uid").toString();
		String sql = "fpyz.service.ls.UserQueryDetail_getResultByUid";
		Map parameter = new HashMap<String, String>();
		parameter.put("uid", uid);
		DataWindow result = DataWindow.query(sql, parameter);
		//查询信息导入excel表中
	/*	File file=new File(fileDir +fileSep+excel+fileSep+"kk.xls");
		String fileName = UUID.randomUUID().toString();
		String sheetName = "明细";
		try {
			result.getList();
			file.createNewFile();
			OutputStream os=new FileOutputStream(file);
			//ExcelUtils.createWorkbook1(os, fileName, sheetName, titles, keys, datas);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Object form = result.getList();
		//上传至阿里云服务器
		
		//链接传到用户邮箱中
		
		//插入一条记录
*/		
		return null;
	}

}
