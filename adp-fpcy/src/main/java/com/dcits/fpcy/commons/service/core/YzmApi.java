package com.dcits.fpcy.commons.service.core;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jsoup.nodes.Document;

import com.dcits.fpcy.commons.bean.CacheEntity;
import com.dcits.fpcy.commons.bean.CookieList;
import com.dcits.fpcy.commons.bean.HSCEntity;
import com.dcits.fpcy.commons.bean.IpAddress;
import com.dcits.fpcy.commons.bean.TaxOfficeBean;
import com.dcits.fpcy.commons.constant.BrowerType;
import com.dcits.fpcy.commons.constant.HeaderType;
import com.dcits.fpcy.commons.constant.SysConfig;
import com.dcits.fpcy.commons.factory.utils.YzmRequestUtils;
import com.dcits.fpcy.commons.service.core.impl.BJGSfpcyImp;
import com.dcits.fpcy.commons.utils.AnalysisJS;
import com.dcits.fpcy.commons.utils.GsonUtils;
import com.dcits.fpcy.commons.utils.HttpClient;
import com.dcits.fpcy.commons.utils.HttpUtils;
import com.dcits.fpcy.commons.utils.IpUtils;
import com.dcits.fpcy.commons.utils.SendResultRequest;
import com.dcits.fpcy.commons.utils.TrustAllHosts;
import com.dcits.fpcy.commons.utils.thirdApi.YzmsbInterface;

/**
 * 获取验证码
 * 
 * @author wuche
 * 
 */
public class YzmApi {
	private static Log logger = LogFactory.getLog(YzmApi.class);
	private static final String YZMSBYC = "验证码识别异常！！";
    private  static HttpClient  httpClient=new HttpClient();
   
	
    /**
	 * 该方法去总局拿验证码（errorCode:000[成功拿到验证码],105[拿码失败可以在拿]，230[拿码失败不能再拿]）
	 * @param fpdm
	 * @param fpcyParas
	 * @return
	 */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public static CacheEntity queryGsYzm(String fpdm, TaxOfficeBean fpcyParas,int countNum,String requestId) {
    	String resultCode="";
    	String resultCodeName="";
    	logger.debug("----获取验证码请求开始---");
    	Map attrMap = new HashMap<String, String>();
		String yzm_dz = fpcyParas.getYzm_dz();
		String host = yzm_dz.substring(yzm_dz.indexOf("/") + 2,
				yzm_dz.indexOf("/WebQuery"));
		attrMap.put("Referer", "https://inv-veri.chinatax.gov.cn");
		attrMap.put("Host", host);
		attrMap.put("Connection", "keep-alive");
		attrMap.put("Accept",
				"*/*");
		attrMap.put("Accept-Encoding", "gzip, deflate, sdch");
		attrMap.put("Accept-Language", "zh-CN,zh;q=0.8");
		attrMap.put(HeaderType.USERAGENT, BrowerType.google);
		Map mar = new HashMap();
		String nowtime=String.valueOf(System.currentTimeMillis());
		mar.put("fpdm", fpdm);
		mar.put("callback",
				"jQuery110205222222650445494_" +nowtime);
		double rad = Math.random();
		mar.put("r", String.valueOf(rad));
		mar.put("nowtime",nowtime);
		//获取publicKey
		String publickey=AnalysisJS.analysisJsKey(mar,"yzm","0");
		mar.put("publickey", publickey);
		mar.put("v","V1.0.04_001");
		mar.put("area","4200");
		String  urlparam="callback="+mar.get("callback")+"&fpdm="+fpdm
				+"&r="+mar.get("r")+"&v=V1.0.04_001&nowtime="+mar.get("nowtime")+"&area="+fpdm.substring(0, 4)+"&publickey="+publickey
				+"&_="+String.valueOf(System.currentTimeMillis());
		Map logMap=new HashMap();
		logMap.put("invoiceCode", fpdm);
		logMap.put("invoiceName", fpcyParas.swjg_mc);
		logMap.put("requestType", "yzm");
		logMap.put("requestId", requestId);
		Map map=httpClient.callGetService(fpcyParas.cyym,urlparam,logMap, mar, attrMap,1);
		CacheEntity cacheEntity=null;
		if(map!=null){
			String resultData=(String) map.get("resultData");
			resultCode=(String) map.get("resultCode");
			resultCodeName=(String) map.get("resultCodeName");
			if(StringUtils.isNotEmpty(resultData)){
				resultData=getCacheSjxxJson(resultData);
				cacheEntity = (CacheEntity) GsonUtils.toObject(resultData,
						CacheEntity.class);
				logger.debug("---获取验证码地区：" +fpcyParas.getSwjg_mc()+"："+resultCodeName);
			}else{
				logger.error("获取验证码地区：" +fpcyParas.getSwjg_mc()+"，错误信息："+resultCodeName);
				if(resultCode.contains("Read timed out")||resultCode.contains("connect timed out")
						|| resultCode.contains("Connection reset")||
						resultCode.contains("Service Unavailable")
						){
					resultCode=SysConfig.INVOICEFALSESTATECODE105;
				}else{
					resultCode=SysConfig.INVOICEFALSESTATECODE230;
				}
			}
		}
		if(cacheEntity!=null){
			String key4 = cacheEntity.getKey4();
			String key1=cacheEntity.getKey1();
			if (key1.equals("003")) {
				resultCode=SysConfig.INVOICEFALSESTATECODE230;
				resultCodeName = "验证码请求次数过于频繁，请1分钟后再试！ 警告";
			} else if (key1.equals("005")) {
				resultCode=SysConfig.INVOICEFALSESTATECODE230;
				resultCodeName = "非法请求! 警告";
			} else if (key1.equals("010")) {
				resultCode=SysConfig.INVOICEFALSESTATECODE105;
				resultCodeName = "网络超时，请重试！(01)！警告";
			} else if (key1.equals("fpdmerr")) {
				resultCode=SysConfig.INVOICEFALSESTATECODE230;
				resultCodeName = "请输入合法发票代码 ！ 警告";
			} else if (key1.equals("024")) {
				resultCode=SysConfig.INVOICEFALSESTATECODE230;
				resultCodeName = "24小时内验证码请求太频繁，请稍后再试！ 警告";
			} else if (key1.equals("016")) {
				resultCode=SysConfig.INVOICEFALSESTATECODE230;
				resultCodeName = "服务器接收的请求太频繁，请稍后再试！ 警告";
			} else if (key1.equals("020")) {
				resultCode=SysConfig.INVOICEFALSESTATECODE230;
				resultCodeName = "由于查验行为异常，涉嫌违规，当前无法使用查验服务！ 提示";
			} else if (key1.equals("010")) {
				resultCode=SysConfig.INVOICEFALSESTATECODE230;
				resultCodeName = "非法请求! 警告";
			} else if (key1 != "") {
				// 成功请求到验证码
				if (key4.equals("02") || key4.equals("00")) {
					if(countNum>=4){
						resultCode=SysConfig.INVOICEFALSESTATECODE230;
						String errorMsg="拿到00和02次数过多";
						cacheEntity.setErrorMsg(errorMsg);
						cacheEntity.setErrorCode(resultCode);
						cacheEntity.setThreadId(requestId);
						return cacheEntity;
					}else{
						cacheEntity = queryGsYzm(fpdm, fpcyParas,++countNum,requestId);
					}
				} else {
					    Map parameter = new HashMap<Object, Object>();
						parameter.put("key1", cacheEntity.getKey1());
						parameter.put("key4", cacheEntity.getKey4());
						parameter.put("reviceTime", cacheEntity.getKey2());
						parameter.put("requestId", requestId);
						String yzm = YzmRequestUtils.getYZM(parameter);
						logger.debug(fpdm+"打码服务结束---验证码为："+yzm);
						//断网,或超级鹰服务宕机,解析网上验证码出错
						String errorMsg=(String) parameter.get("errorMsg");
						if(StringUtils.isEmpty(yzm)){
							resultCode=SysConfig.INVOICEFALSESTATECODE106;
							cacheEntity.setErrorMsg(errorMsg);
						}else{
							resultCode=SysConfig.INVOICETRUESTATE000;
							cacheEntity.setYzm(yzm);
							cacheEntity.setIp(IpUtils.getIp());
							String pic_id = (String) parameter.get("pic_id");
							cacheEntity.setImageId(pic_id);
							cacheEntity.setKey1("");
						}
						cacheEntity.setErrorCode(resultCode);
						cacheEntity.setThreadId(requestId);
						return cacheEntity;
				}
			}
		}else{
			cacheEntity=new CacheEntity();
			cacheEntity.setErrorCode(resultCode);
			cacheEntity.setThreadId(requestId);
		}
		return cacheEntity;
	}
    /**
     * 获取税局正常json
     * @param data
     * @return
     */
	private static String getCacheSjxxJson(String  data) {
		if (StringUtils.isNotEmpty(data)) {
			if(data.indexOf("(")>0){
				// 清单中括号会影响数据
				data = data.toString().substring(data.indexOf("(") + 1,
						data.length()-1);
			}
		}
		return data;
	}
	/**
	 * 
	 * @param fpdm
	 * @param fpcyParas
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	public static HSCEntity queryGsOrDsYzm(String fpdm, TaxOfficeBean fpcyParas) {
		String result = null;
		Map<String, Object> map1 = null;
		String cookie = null;
		String cookie1 = null;
		byte[] filePath = null;
		Map<String, Object> map = null;
		List<String> list = null;
		List<String> list1 = null;
		IpAddress ipAddress = IpUtils.get();
		HSCEntity hscEntity = new HSCEntity();
		/*
		 * 通过参数判断下载验证码是否需要cookie
		 * fpcyParas.cookie==1是需要验证码，fpcyParas.cookie==0是不需要验证码
		 * list中存的是从cyymurl拿到的jsessionid
		 * cookie中放的是list中的jsessionid字符串，但是有些省份需要特殊处理，所以就没走
		 */
		if (fpcyParas.cookie != 0) {
			if ("23502".equals(fpcyParas.getSwjg_dm())) {
				// 厦门地税需要的请求信息
				TrustAllHosts.trustAllHosts();// https
				list = SendResultRequest.sendRequestCookie(null, ipAddress,
						null, fpcyParas.cyym, fpcyParas.yzm_qqfs);
			} else if ("21200".equals(fpcyParas.getSwjg_dm())) {
				// 天津地税需要的请求信息
				// 1获取cookie
				list = SendResultRequest
						.sendRequestCookie(
								null,
								null,
								null,
								"http://fpcx.tjcs.gov.cn/TJTAX_NET/NetLevy/NetQuery/TicketUse/index.jsp",
								"get");
				// 废弃
			} else if ("13502".equals(fpcyParas.getSwjg_dm())
					|| "3502".equals(fpcyParas.getSwjg_dm())) {
				// 厦门国税及厦门增值税需要的请求信息
				List<String> firstList = SendResultRequest.sendRequestCookie(
						null, ipAddress, null, fpcyParas.cyym,
						fpcyParas.yzm_qqfs);
				// 对jsessionid做了特殊处理
				cookie = cookieUtil(firstList, fpcyParas);
			} else if ("13100".equals(fpcyParas.getSwjg_dm())
					|| "3100".equals(fpcyParas.swjg_dm)
					|| "23100".equals(fpcyParas.swjg_dm)
			/* || "24200".equals(fpcyParas.getSwjg_dm()) */) {
				// 上海国税（https）、地税、增值税、湖北地税
				list = YzmRequestUtils.sendSHGSHttpsRequest(null, ipAddress,
						fpcyParas.cyym);
			} else if ("23700".equals(fpcyParas.getSwjg_dm())) {
				// 山东地税需要的请求信息
				Map requestHeader = new HashMap<String, String>();
				requestHeader.put(HeaderType.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
				requestHeader.put(HeaderType.ACCEPTENCODING,"gzip, deflate");
				requestHeader.put(HeaderType.ACCEPTLANGUAGE,"zh-CN,zh;q=0.8");
				requestHeader.put(HeaderType.CACHECONTROL,"max-age=0");
				requestHeader.put(HeaderType.CONNECTION,"keep-alive");
				requestHeader.put(HeaderType.HOST,"wsbs.sdds.gov.cn");
				requestHeader.put(HeaderType.UPGRADEINSECUREREQUESTS,"1");
				requestHeader.put(HeaderType.COOKIE,"COLLCK="+System.currentTimeMillis()/1000L);
				requestHeader.put(HeaderType.REFERER, "http://wsbs.sdds.gov.cn/etax/fpcy/jsp/fpzk.jsp");
				list = SendResultRequest.sendRequestCookie(requestHeader,
					null, null, fpcyParas.cyym, fpcyParas.yzm_qqfs);
			} else if ("4600".equals(fpcyParas.getSwjg_dm())
					|| "14600".equals(fpcyParas.getSwjg_dm())
					|| "04600".equals(fpcyParas.getSwjg_dm())) {
				// 海南国税
				Map<String, String> requestHeader = new HashMap<String, String>();
				requestHeader
						.put("Accept",
								"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
				requestHeader.put("Accept-Encoding", "gzip, deflate, sdch");
				requestHeader.put("Accept-Language", "zh-CN,zh;q=0.8");
				requestHeader.put("Connection", "keep-alive");
				requestHeader.put("Host", "hitax.gov.cn:91");
				requestHeader.put("Cache-Control", "max-age=0");
				requestHeader.put("Upgrade-Insecure-Requests", "1");
				requestHeader
						.put("User-Agent",
								"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
				map1 = SendResultRequest.sendRequestMap(requestHeader, null,
						null, fpcyParas.cyym, "GET");
				Object a = map1.get("set-cookie");
				list = (List<String>) a;
			} else if ("21100".equals(fpcyParas.getSwjg_dm())) {
				list = SendResultRequest.sendRequestCookie(null, ipAddress,
						null, fpcyParas.cyym, fpcyParas.yzm_qqfs);
			} else if ("3702".equals(fpcyParas.getSwjg_dm())
					|| "4200".equals(fpcyParas.getSwjg_dm())) {
				TrustAllHosts.trustAllHosts();
				list = SendResultRequest.sendRequestCookie(null, ipAddress,
						null, fpcyParas.cyym, fpcyParas.yzm_qqfs);
			} else if ("5100".equals(fpcyParas.getSwjg_dm())
					|| "05100".equals(fpcyParas.getSwjg_dm())
					|| "2200".equals(fpcyParas.getSwjg_dm())
					|| !"5200".equals(fpcyParas.getSwjg_dm())) {
				list = SendResultRequest.sendRequestCookie(null, null, null,
						fpcyParas.cyym, fpcyParas.yzm_qqfs);
			} else if ((!"259".equals(fpcyParas.getPoolid()) && !"6400"
					.equals(fpcyParas.getSwjg_dm()))) {
				// 默认通用请求，除了贵州增值税。
				list = SendResultRequest.sendRequestCookie(null, ipAddress,
						null, fpcyParas.cyym, fpcyParas.yzm_qqfs);
			}
			// ---------------------cookie是处理过后的list中的jsessionid字符串------------------------------------------------------------
			if (list != null) {
				cookie = CookieList.getItem(list);
			}
			// -----二次请求的地区
			if ("21200".equals(fpcyParas.getSwjg_dm())) {
				// 天津地税需要的请求信息
				// 2
				Map map2 = new HashMap<String, String>();
				map2.put("_INIT",
						"tjtax.declevy.NetQuery.TicketQuery.si.C_UseTicketQuerySI");
				map2.put("_SIVO",
						"tjtax.declevy.NetQuery.TicketQuery.vo.C_UseTicketQueryVO");
				map2.put("_TaskID",
						"tjtax.declevy.NetQuery.TicketQuery.si.C_UseTicketQuerySI_init");
				map2.put("_ReturnPage",
						"/TJTAX_NET/NetLevy/NetQuery/TicketUse/edit.jsp");
				map2.put("_ReturnArea", "editArea");
				map2.put("_SessionName",
						"/TJTAX_NET/NetLevy/NetQuery/TicketUse/index.jsp");
				Map map3 = new HashMap<String, String>();
				map3.put("Referer",
						"http://fpcx.tjcs.gov.cn/TJTAX_NET/NetLevy/NetQuery/TicketUse/index.jsp");
				map3.put(HeaderType.COOKIE, cookie);
				InputStream in1 = SendResultRequest
						.sendRequestGet(
								map3,
								null,
								"http://fpcx.tjcs.gov.cn/servlet/com.appinf.bus.TaskBus",
								map2);
				// 3
				Map map4 = new HashMap<String, String>();
				Map map5 = new HashMap<String, String>();
				map4.put("_SessionName",
						"/TJTAX_NET/NetLevy/NetQuery/TicketUse/index.jsp");
				map4.put("_ReturnArea", "editArea");
				map4.put("_ReturnPage",
						"/TJTAX_NET/NetLevy/NetQuery/TicketUse/edit.jsp");
				map4.put("_FIELDS", "null");
				map5.put(HeaderType.USERAGENT, BrowerType.firfox);
				map5.put(HeaderType.COOKIE, cookie);
				map5.put(
						"Referer",
						"http://fpcx.tjcs.gov.cn/servlet/com.appinf.bus.TaskBus?_INIT=tjtax.declevy.NetQuery.TicketQuery.si.C_UseTicketQuerySI&_SIVO=tjtax.declevy.NetQuery.TicketQuery.vo.C_UseTicketQueryVO&_TaskID=tjtax.declevy.NetQuery.TicketQuery.si.C_UseTicketQuerySI_init&_ReturnPage=/TJTAX_NET/NetLevy/NetQuery/TicketUse/edit.jsp&_ReturnArea=editArea&_SessionName=/TJTAX_NET/NetLevy/NetQuery/TicketUse/index.jsp");
				InputStream in2 = SendResultRequest
						.sendRequestPost(
								map5,
								null,
								"http://fpcx.tjcs.gov.cn/TJTAX_NET/NetLevy/NetQuery/TicketUse/edit.jsp",
								map4);
			}
			// map中存放的是验证码图片字节filepath&&下载完验证码得到的新的jsessionid（cookieList），如果不为空则放入list1中，
			if ("11400".equals(fpcyParas.getSwjg_dm())
					|| "23702".equals(fpcyParas.getSwjg_dm())) {
				// 山西国税&青岛地税
				map = YzmRequestUtils.sendYzmRequest(null, fpcyParas,
						null, null);
			} else if (42 == fpcyParas.getPoolid()
					|| 50 == fpcyParas.getPoolid()) {
				// 北京国税&北京增值税
				try {
					cookie = BJGSfpcyImp.getLastSession(null);// lastSession
					String adress = fpcyParas.yzm_dz + "?sessionrandom="
							+ Math.random() + "&sessionId=" + cookie;
					map = YzmRequestUtils.sendYzmRequest(null, null, adress,
							fpcyParas);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else if ("22102".equals(fpcyParas.getSwjg_dm())) {
				// 大连地税
				String jess = cookie.substring(0, cookie.indexOf(";"));
				String checkUrl = fpcyParas.yzm_dz
						+ "?sessionKey=YZMWA111001&t="
						+ new SimpleDateFormat("EE MMM dd yyyy HH:mm:ss zZZ ",
								Locale.ENGLISH).format(new Date())
								.replace("CST", "GMT").replace(" ", "%20");
				// 得到最后的验证码地址
				checkUrl = checkUrl + "(%D6%D0%B9%FA%B1%EA%D7%BC%CA%B1%BC%E4)&"
						+ jess;
				// 下载验证码
				map = YzmRequestUtils.sendYzmRequest(null, ipAddress, checkUrl,
						fpcyParas);
			} else if ("24400".equals(fpcyParas.getSwjg_dm())) {
				// 广东地税
				map = YzmRequestUtils.sendGuangDDSYzmRequest(null, ipAddress,
						fpcyParas.yzm_dz, fpcyParas.yzm_qqfs,
						fpcyParas.yzm_tplx);
			} else if ("26100".equals(fpcyParas.getSwjg_dm())) {
				// 陕西地税 需两次访问获取验证码 第一次需要时间戳 第二次不需要
				map = YzmRequestUtils.sendSXDSYzmRequest(null, ipAddress,
						fpcyParas, cookie);
			} else if ("14600".equals(fpcyParas.getSwjg_dm())
					|| "4600".equals(fpcyParas.getSwjg_dm())
					|| "04600".equals(fpcyParas.getSwjg_dm())) {
				// 海南国税 &海南增值税
				Map<String, String> requestHeader = new HashMap<String, String>();
				requestHeader
						.put("Accept",
								"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
				requestHeader.put("Accept-Encoding", "gzip, deflate, sdch");
				requestHeader.put("Accept-Language", "zh-CN,zh;q=0.8");
				requestHeader.put("Connection", "keep-alive");
				requestHeader.put("Host", "hitax.gov.cn:91");
				requestHeader.put("Cache-Control", "max-age=0");
				requestHeader.put("Upgrade-Insecure-Requests", "1");
				requestHeader
						.put("User-Agent",
								"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
				try {
					map = YzmRequestUtils.sendYzmRequest(null, fpcyParas,
							requestHeader, null);
					List<String> hnlist = (List<String>) map.get("cookieList");
					StringBuffer sb1 = new StringBuffer();
					for (int i = hnlist.size() - 1; i >= 0; i--) {
						String jess = hnlist.get(i).toString();
						jess = jess.substring(0, jess.indexOf(";"));
						sb1.append(jess);
						sb1.append("; ");
					}
					String secJess = sb1.toString();
					cookie = secJess.substring(0, secJess.length() - 2);
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("海南国税初始化异常:" + e);
				}
			} else if ("13502".equals(fpcyParas.getSwjg_dm())
					|| "3502".equals(fpcyParas.getSwjg_dm())) {
				// 厦门国税
				for (int i = 0; i < 3; i++) {
					map = YzmRequestUtils.sendYzmRequest(null, ipAddress,
							cookie, fpcyParas);
					if (map == null) {
					} else {
						String cookieList = map.get("cookieList").toString();
						if (cookieList.contains("JSESSIONID")) {
							String[] cookies = cookieList.split(";");
							cookie1 = cookies[1].substring(cookies[1]
									.indexOf(",") + 1)
									+ ";"
									+ cookies[0].substring(1);
							;
						}
						if (!StringUtils.isEmpty(cookie1)) {
							break;
						}
					}
				}
			} else if ("13100".equals(fpcyParas.getSwjg_dm())
					|| "3100".equals(fpcyParas.swjg_dm)
					|| "23100".equals(fpcyParas.swjg_dm)
			/* || "24200".equals(fpcyParas.getSwjg_dm()) */) {
				// 上海国税、地税、增值税
				map = YzmRequestUtils.sendSHGYzmRequest(ipAddress, fpcyParas,
						null, cookie);
			} else if ("23700".equals(fpcyParas.getSwjg_dm())) {
				System.out.println(cookie);
				System.out.println(list);
				// 山东地税
				Map requestHeader = new HashMap<String, String>();
				requestHeader.put(HeaderType.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
				requestHeader.put(HeaderType.ACCEPTENCODING,"gzip, deflate");
				requestHeader.put(HeaderType.ACCEPTLANGUAGE,"zh-CN,zh;q=0.8");
				requestHeader.put(HeaderType.CACHECONTROL,"max-age=0");
				requestHeader.put(HeaderType.CONNECTION,"keep-alive");
				requestHeader.put(HeaderType.HOST,"wsbs.sdds.gov.cn");
				requestHeader.put(HeaderType.UPGRADEINSECUREREQUESTS,"1");
				requestHeader.put(HeaderType.COOKIE,"COLLCK=1870466999");
				requestHeader.put(HeaderType.REFERER, "http://wsbs.sdds.gov.cn/etax/fpcy/jsp/fpzk.jsp");
				map = YzmRequestUtils.sendYzmRequest(null, fpcyParas,
						null, null);
			}else if("15201".equals(fpcyParas.getSwjg_dm())) {
				// 贵州国税
				Map<String, String> requestHeader = new HashMap<String, String>();
				requestHeader
						.put(HeaderType.HOST, "yun.gzgs12366.gov.cn");
				requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
				map1 = SendResultRequest.sendRequestMap(requestHeader,
						null, null,
						"http://yun.gzgs12366.gov.cn/Service/Invoice",
						"get");
				requestHeader.put(HeaderType.REFERER,
						"http://yun.gzgs12366.gov.cn/Service/Invoice");
				List list2 = (List) map1.get("set-cookie");
				String cookie0 = (String) list2.get(0);
				String cookie2 = (String) list2.get(2);
				cookie = cookie0 + ";" + cookie2;
				requestHeader.put(HeaderType.COOKIE, cookie);
				InputStream inn = (InputStream) map1.get("in");
				JSONObject json1 = parseInvoiceResult(inn);
				map1.remove("in");
				InputStream in = null;
				Map<String, String> attributeMap = new HashMap<String, String>();
				try {
					map1.put("CaptchaDeText1", json1.get("CaptchaDeText1")
							.toString());
					map1.put("__RequestVerificationToken",
							json1.get("__RequestVerificationToken").toString());
					attributeMap.put("t", json1.getString("CaptchaDeText1"));
					attributeMap.put("_multiple_", "1");
					in = SendResultRequest.sendRequestIn(requestHeader, null,
							attributeMap, fpcyParas.yzm_dz, "get");
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
				map = getMap(in, list);
			}else if ("12300".equals(fpcyParas.getSwjg_dm())
					|| ("2300").equals(fpcyParas.getSwjg_dm())) {
				// 黑龙江国税&黑龙江增值税
				Map<String, String> requestHeader = new HashMap<String, String>();
				requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
				requestHeader.put(HeaderType.ACCEPT, "image/webp,image/*,*/*;q=0.8");
				requestHeader.put(HeaderType.COOKIE, cookie);
				requestHeader.put(HeaderType.REFERER, "http://221.212.153.202:82/fpcx/qtptfpcxjs.jsp");
				long now = System.currentTimeMillis();
				String address = fpcyParas.yzm_dz+"?now="
						+ now;
				map = YzmRequestUtils.sendYzmRequest(requestHeader, null, address,
						fpcyParas);
				if (!map.isEmpty()) {
					Object cookielist = (List) map.get("cookieList");
					if(cookielist instanceof List){
						List cookielist1=(List) cookielist;
						cookie = (String) cookielist1.get(0);
					}
					
				}
				cookie1 = now + "";
				getHLJGSYzm(cookie, cookie1);
				map.remove("cookieList");
			} else if ("22100".equals(fpcyParas.getSwjg_dm())) {
				// 辽宁地税
				String address = fpcyParas.yzm_dz + new Date().getTime()
						+ ".jpg";
				map = YzmRequestUtils.sendYzmRequest(null, ipAddress, address,
						fpcyParas);
				List<String> cookieList = (List<String>) map.get("cookieList");
				StringBuffer cookieStr = new StringBuffer();
				int j = 0;
				do {
					cookieStr.append(cookieList.get(j) + "：");
					j++;
				} while (j < cookieList.size());
				String str = cookieStr.substring(0, cookieStr.indexOf(";") + 1);
				if (j > 1) {
					str = str
							+ " "
							+ cookieStr.substring(cookieStr.indexOf("：") + 1,
									cookieStr.lastIndexOf("："));
				}
				// 重新处理jsessionid
				cookie = str;
			} else if ("25300".equals(fpcyParas.getSwjg_dm())) {
				// 云南地税
				map = YzmRequestUtils.sendYzmRequest(null, ipAddress,
						fpcyParas.yzm_dz, fpcyParas);
			} else if ("21400".equals(fpcyParas.getSwjg_dm())) {
				// 山西地税,需要referer
				Map<String, String> requestHeader = new HashMap<String, String>();
				requestHeader.put(HeaderType.HOST, "218.26.133.58:85");
				requestHeader.put(HeaderType.REFERER,
						"http://218.26.133.58:85/Sehup/invseek/web.jsp");
				requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
				requestHeader.put(HeaderType.ACCEPTENCODING, "gzip, deflate");
				requestHeader.put(HeaderType.ACCEPT,"image/webp,image/apng,image/*,*/*;q=0.8");
				requestHeader.put(HeaderType.ACCEPTLANGUAGE,"zh-CN,zh;q=0.8");
				requestHeader.put(HeaderType.CONNECTION,"keep-alive");
				requestHeader.put(HeaderType.COOKIE, cookie);
				String address = "http://218.26.133.58:85/Sehup/invseek/CheckCode?"
						+ Math.random();
				map = YzmRequestUtils.sendYzmRequest(requestHeader, null,
						address, fpcyParas);
			} else if ("26500".equals(fpcyParas.getSwjg_dm())) {
				// 新疆地税
				map = YzmRequestUtils.sendYzmRequest(null, ipAddress,
						(fpcyParas.yzm_dz + "?" + cookie), fpcyParas);
			} else if ("23302".equals(fpcyParas.getSwjg_dm())) {
				// 宁波地税
				String address = "http://taxapp.nbcs.gov.cn/aWeb/invoiceJsp/image.jsp?rand="
						+ getBASE64();
				map = YzmRequestUtils.sendYzmRequest(null, ipAddress, address,
						fpcyParas);
			} else if ("13702".equals(fpcyParas.getSwjg_dm())
					|| ("13310").equals(fpcyParas.getSwjg_dm())
					|| ("13301").equals(fpcyParas.getSwjg_dm())
					|| ("3300").equals(fpcyParas.getSwjg_dm())) {
				// 青岛国税、浙江国税网络发票、浙江国税客运发票、浙江增值税（只要jsessionid,不需要验证码）
			} else if ("4100".equals(fpcyParas.getSwjg_dm())
					|| "14100".equals(fpcyParas.getSwjg_dm())) {
				// 河南国税（不需要页面的cookie直接得到新的jsession和验证码字节）
				map = YzmRequestUtils.sendYzmRequest(null, fpcyParas,
						null, cookie);
			} else if ("5200".equals(fpcyParas.getSwjg_dm())
					|| "15200".equals(fpcyParas.getSwjg_dm())) {
				Map<String, String> requestHeader = new HashMap<String, String>();
				requestHeader
						.put(HeaderType.HOST, "etax.gzgs12366.gov.cn:8080");
				requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
				map1 = SendResultRequest.sendRequestMap(requestHeader,
						ipAddress, null,
						"http://etax.gzgs12366.gov.cn:8080/TaxInquiry/Invoice",
						"get");
				requestHeader.put(HeaderType.REFERER,
						"http://etax.gzgs12366.gov.cn:8080/TaxInquiry/Invoice");
				List list2 = (List) map1.get("set-cookie");
				String cookie0 = (String) list2.get(0);
				String cookie2 = (String) list2.get(2);
				cookie = cookie0 + ";" + cookie2;
				requestHeader.put(HeaderType.COOKIE, cookie);
				InputStream inn = (InputStream) map1.get("in");
				JSONObject json1 = parseInvoiceResult(inn);
				InputStream in = null;
				Map<String, String> attributeMap = new HashMap<String, String>();
				try {
					map1.put("CaptchaDeText1", json1.get("CaptchaDeText1")
							.toString());
					map1.put("__RequestVerificationToken",
							json1.get("__RequestVerificationToken").toString());
					attributeMap.put("t", json1.getString("CaptchaDeText1"));
					attributeMap.put("_multiple_", "1");
					in = SendResultRequest.sendRequestIn(requestHeader, null,
							attributeMap, fpcyParas.yzm_dz, "get");
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
				map = getMap(in, list);
				// 江苏各个省份
			} else if ("21100".equals(fpcyParas.getSwjg_dm())) {
				/*
				 * map1 = SendResultRequest.sendRequestMap(null, ipAddress,null,
				 * "http://www.jsnj-n-tax.gov.cn/fpyzcxInternet/lgxxyzAction.do"
				 * ,"get"); InputStream inn = (InputStream) map1.get("in");
				 * JSONObject json1=parseInvoiceResult1(inn); try { String ll =
				 * json1.get("image").toString(); } catch (JSONException e) {
				 * e.printStackTrace(); }
				 */
				map = YzmRequestUtils.sendYzmRequest(null, fpcyParas,
						null, cookie);

			} else if ("15300".equals(fpcyParas.getSwjg_dm())
					|| "14300".equals(fpcyParas.getSwjg_dm())
					|| "2200".equals(fpcyParas.getSwjg_dm())
					|| "4300".equals(fpcyParas.getSwjg_dm())
					|| "4200".equals(fpcyParas.getSwjg_dm())) {
				// 云南国税和湖南国税不走代理
				map = YzmRequestUtils.sendYzmRequest(null, fpcyParas, null,
						cookie);
			}else if ("259".equals(fpcyParas.getPoolid())
					|| !"259".equals(fpcyParas.getPoolid())) {
				// 其他省份下载验证码
				map = YzmRequestUtils.sendYzmRequest(null, fpcyParas,
						null, cookie);
			}
			// map中存放的是验证码图片地址filepath&&下载完验证码得到的新的jsessionid（cookieList），如果不为空则放入list1中，
			if (map != null) {
				if (map.containsKey("cookieList")
						&& map.get("cookieList") != null) {
					// 为有需要list1的取list1
					list1 = (List<String>) map.get("cookieList");
					if ("13502".equals(fpcyParas.getSwjg_dm())
							|| "3502".equals(fpcyParas.getSwjg_dm())) {
					} else {
						cookie1 = CookieList.getItem((List<String>) map
								.get("cookieList"));
					}
				}
				if ("22100".equals(fpcyParas.getSwjg_dm())) {// 辽宁地税验证码识别不需要第三方服务打码识别
					try {
						hscEntity.setYzm(getLNDSYzm(cookie, ipAddress));
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					// 验证码识别需要第三方打码识别
					try {
						filePath = (byte[]) map.get("filepath");
						if (filePath == null) {
							hscEntity.setYzm(null);
						} else {
							result = YzmsbInterface.YZMSB(filePath, fpcyParas);
							hscEntity.setYzm(result);
						}
					} catch (Exception e) {
						logger.error(YZMSBYC, e);
					}
					if (StringUtils.isEmpty(result)) {
						logger.error(YZMSBYC);
					}
				}
			}
		} else {
			// 四川国税和海南国税需要cyym的jsessionid和界面，都存放在集合map1中
			if ("15100".equals(fpcyParas.getSwjg_dm())
					|| "5100".equals(fpcyParas.getSwjg_dm())
					|| "05100".equals(fpcyParas.getSwjg_dm())) {
				// 四川国税
				map1 = SendResultRequest.sendRequestMap(null, ipAddress, null,
						"http://wsbs.sc-n-tax.gov.cn/fpcy/index.htm", "GET");
			}
		}
		hscEntity.setMap(map1);
		hscEntity.setFpcyParas(fpcyParas);
		/*
		 * for(int i=0;i<5;i++){
		 * if(!org.apache.commons.lang3.StringUtils.isEmpty(cookie)) {
		 * this.createNew2(); }else{
		 */
		hscEntity.setCookie(cookie);
		/*
		 * } }
		 */
		hscEntity.setList(list);
		hscEntity.setCookie1(cookie1);
		hscEntity.setList1(list1);
		hscEntity.setIpAddress(ipAddress);
		return hscEntity;

	}


	/**
	 * 对cookie特殊处理 厦门国税定额（手工）票、出租车发票流向查询 说明：
	 * 1.厦门国税由于正常流程得不到结果，显示验证码错误，应该是由于验证码过期了
	 * 所以每次在得验证码的时候将JSESSIONID当做一个参数传到验证码的地址，以来让浏览器确认这是同一次请求
	 * 流程改成，先在查询页面得到Jsessionid,然后拼接到验证码地址页面中
	 * 
	 * @param firstList
	 * @param fpcyParas
	 * @return
	 */
	private static String cookieUtil(List<String> firstList,
			TaxOfficeBean fpcyParas) {
		StringBuffer sb = new StringBuffer();
		for (int i = firstList.size() - 1; i >= 0; i--) {
			String jess = firstList.get(i).toString();
			jess = jess.substring(0, jess.indexOf(";"));
			sb.append(jess);
			sb.append("; ");
		}
		String firstJess = sb.toString();
		firstJess = firstJess.substring(0, firstJess.length() - 2);
		// 重新拼接一下JSESSIONID
		String checkUrl = fpcyParas.yzm_dz
				+ "&a="
				+ new SimpleDateFormat("EE MMM dd yyyy HH:mm:ss zZZ ",
						Locale.ENGLISH).format(new Date())
						.replace("CST", "GMT").replace(" ", "%20");
		return checkUrl + "&" + firstJess;
	}

	/**
	 * 黑龙江国税
	 * 
	 * @param JSESSIONID
	 * @param snow
	 * @return
	 */
	@SuppressWarnings("unused")
	public static String getHLJGSYzm(String JSESSIONID, String snow) {
		InputStream in = null;
		BufferedReader bufferedReader = null;
		String html = "";
		try {
			URL url = new URL("http://221.212.153.202:82/fpcx/YbSession.do?noww="
					+ snow);
			HttpURLConnection con = HttpUtils.getConnection(url, null);
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setRequestMethod("GET");
			con.setUseCaches(false);
			con.setRequestProperty(HeaderType.USERAGENT, BrowerType.firfox);
			con.setRequestProperty("Referer",
					"http://221.212.153.202:82/fpcx/qtptfpcxjs.jsp");
			con.setRequestProperty("Cookie", JSESSIONID);
			in = con.getInputStream();// 获取发票验真返回信息
			return html;
		} catch (Exception w) {
		}
		return html;
	}

	/**
	 * 宁波地税BASE64编码
	 * 
	 * @return
	 */
	private static String getBASE64() {
		String s = createCode();
		s = (new sun.misc.BASE64Encoder()).encode(s.getBytes());
		s = s.replace("=", "%3D");
		return s;
	}

	private static String createCode() {
		String code = "";
		String[] random = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
				"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l",
				"m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x",
				"y", "z" };
		for (int i = 0; i < 4; i++) {// 循环操作
			int index = (int) Math.floor(Math.random() * 36); // 取得随机数的索引（0~35）
			code += random[index];// 根据索引取得随机数加到code上
		}
		return code;
	}

	private static Map<String, Object> getMap(InputStream in, List<String> list) {
		Map<String, Object> map = new HashMap<String, Object>();
		ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
		byte[] b = new byte[1000];
		int n;
		try {
			while ((n = in.read(b)) != -1) {
				out.write(b, 0, n);
			}
			byte[] buffer= out.toByteArray();
			map.put("filepath", buffer);
			map.put("cookieList", list);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (Exception e) {
				logger.error("[ERROR] 下载验证码异常：" + e);
			}
		}
		return map;
	}

	private static JSONObject parseInvoiceResult(InputStream in) {
		JSONObject json = new JSONObject();
		if (in != null) {
			Document doc = (Document) SendResultRequest.iSToJSONOrDocument(in,
					"utf-8", "text");
			org.jsoup.select.Elements ele = doc
					.select("input[id=CaptchaDeText1]");
			String eles = ele.val();
			org.jsoup.select.Elements ele1 = doc
					.select("input[name=__RequestVerificationToken]");
			String hid = ele1.val();
			try {
				json.put("CaptchaDeText1", eles);
				json.put("__RequestVerificationToken", hid);
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

		return json;
	}

	public static byte[] toByteArray(File imageFile) throws Exception {
		BufferedImage img = ImageIO.read(imageFile);
		ByteArrayOutputStream buf = new ByteArrayOutputStream(
				(int) imageFile.length());
		try {
			ImageIO.write(img, "jpg", buf);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return buf.toByteArray();
	}

	/**
	 * 辽宁地税
	 * 
	 * @param JSESSIONID
	 * @return
	 * @throws Exception
	 */
	public static String getLNDSYzm(String JSESSIONID, IpAddress ipAddress)
			throws Exception {
		JSONObject json = new JSONObject();
		InputStream in = null;
		BufferedReader bufferedReader = null;
		String ss = null;
		String html = "";
		try {
			URL url = new URL("http://fpcx.lnsds.gov.cn/jsp/fpzwcx/auto.jsp");
			HttpURLConnection con = HttpUtils.getConnection(url, ipAddress);
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setRequestMethod("GET");
			con.setUseCaches(false);
			con.setRequestProperty("User-Agent", BrowerType.firfox);
			con.setRequestProperty("Referer",
					"http://fpcx.lnsds.gov.cn/jsp/fpzwcx/FPZWCX.jsp");
			con.setRequestProperty("Cookie", JSESSIONID);
			in = con.getInputStream();
			bufferedReader = new BufferedReader(
					new InputStreamReader(in, "GBK"));
			while ((ss = bufferedReader.readLine()) != null) {
				html += ss;
			}
			html = html.substring(html.indexOf("xx>") + 3,
					html.indexOf("</yzm"));

		} catch (IOException e) {
			System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
		} finally {
			try {
				if (in != null)
					in.close();
				if (bufferedReader != null)
					bufferedReader.close();
			} catch (IOException e) {
				System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
				json.put("cwxx", "查询发生错误");
			}
		}
		return html;
	}
}
