package com.dcits.fpcy.commons.service.core.impl;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.dcits.fpcy.commons.bean.HSCEntity;
import com.dcits.fpcy.commons.bean.IpAddress;
import com.dcits.fpcy.commons.bean.ResultBean;
import com.dcits.fpcy.commons.bean.TaxOfficeBean;
import com.dcits.fpcy.commons.constant.BrowerType;
import com.dcits.fpcy.commons.constant.HeaderType;
import com.dcits.fpcy.commons.constant.SysConfig;
import com.dcits.fpcy.commons.service.core.InvoiceServerBase;
import com.dcits.fpcy.commons.utils.ResultUtils;
import com.dcits.fpcy.commons.utils.SendResultRequest;
/**
 * 海南国税发票查询,异常情况很多，分类处理 14600 poolid:6 20160315
 * 
 * @author wangkej
 * 
 */
public class HAINGSfpcyImp implements InvoiceServerBase {

	private Log logger = LogFactory.getLog(HAINGSfpcyImp.class);
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		/*ByteArrayOutputStream out = null;
		byte[] buffer = null;
		//HSCEntity hscEntity = null;
		TaxOfficeBean fppars = null;
		if(!fpcyParas.swjg_dm.equals("4600")&&!fpcyParas.swjg_dm.equals("04600")) {
			fppars = TaxOfficeBean.getFPCYCS("14600");
			if (!fppars.hasData) {
				result.put(SysConfig.CYJGSTATE,SysConfig.CODE20011);
				return result;
			}
		}else{
			fppars = fpcyParas;
		}
		Map yzmPic;*/
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			Map<String,Object> map=hscEntity.getMap();
			String cookie = hscEntity.getCookie();
			if(cookie == null) {
				cookie = hscEntity.getCookie1();
			}
			parameter.put("JSESSIONID", cookie);
			parameter.put("in", map.get("in"));
			parameter.put("rand", hscEntity.getYzm().toLowerCase());
			// 开始查验
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
			return result;
		} catch (Exception e) {
			logger.error("海南国税,获取cook时出现异常", e);
			List list = new ArrayList<ResultBean>();
			list.clear();
			list.add(new ResultBean("cwxx", "", "很抱歉，系统未能得到发票信息，请您重新进行一次查询！"));
			result.put("list", list);
			result.put(SysConfig.INVOICEFALSESTATE,
					SysConfig.INVOICEFALSESTATECODE103);
			result.put(SysConfig.CYJGSTATE,SysConfig.CODE20011);
		}
		return result;
	}

	/**
	 * 
	 * @param paras
	 *            查验参数
	 * @param in_Parameter
	 *            请求数据 [ 验证码 jessionid]
	 * @param out_Result
	 *            返回结果
	 * @throws Exception 
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result,
			IpAddress ipAddress) throws Exception {
		InputStream in = null;
		Map attributeMap = new HashMap();
		Map<String, String> requestHeader = new HashMap<String, String>();
		if(paras.swjg_dm.equals("4600")||paras.swjg_dm.equals("04600")) {
			attributeMap.put("fpdm", (String)in_parameter.get("fpcy.fpdm"));
			attributeMap.put("fphm", (String)in_parameter.get("fpcy.fphm"));
			attributeMap.put("je", (String)in_parameter.get("fpcy.jshj"));
			attributeMap.put("yzm", (String)in_parameter.get("rand"));
			String JSESSIONID = in_parameter.get("JSESSIONID").toString();
			
			requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
			requestHeader.put(HeaderType.COOKIE, JSESSIONID);
			requestHeader
					.put("Referer", "http://hitax.gov.cn:91/fpyj_v1/query.page");
			requestHeader.put("Host", "hitax.gov.cn:91");
			requestHeader.put("Origin", "http://hitax.gov.cn:91");
			requestHeader.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			in = SendResultRequest.sendRequestPost(requestHeader,
					null,paras.cy_dz , attributeMap);
		}else{
			InputStream inn=(InputStream) in_parameter.get("in");
			 String token = parseInvoiceResult111(inn);
			
			// 这里改
			attributeMap.put("search_fpdm", in_parameter.get("FPDM").toString());// 发票代码
			attributeMap.put("search_fphm", in_parameter.get("FPHM").toString());// 发票号码
			attributeMap.put("search_je", in_parameter.get("search_je").toString());// 查询码
			attributeMap.put("search_yzm", in_parameter.get("rand").toString());// 验证码
			attributeMap.put("token", token);// 隐藏字段token，随机值
			String requestMethod = paras.cy_qqfs;
			String JSESSIONID = in_parameter.get("JSESSIONID").toString();
			requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
			requestHeader.put(HeaderType.COOKIE, JSESSIONID);
			requestHeader.put("Cache-Control", "max-age=0");
			requestHeader
					.put("Referer", "http://221.176.87.246:9090/fpcy/main.jsp");
			requestHeader.put("Host", "221.176.87.246:9090");
			requestHeader.put("Origin", "http://221.176.87.246:9090");
			requestHeader.put("Content-Type", "application/x-www-form-urlencoded");
			requestHeader.put("CUpgrade-Insecure-Requests", "1");
			
			in = SendResultRequest.sendRequestPost(requestHeader,
					ipAddress, "http://221.176.87.246:9090/fpcy/netinv/callback.do?method=preQueryInvoiceFact", attributeMap);
		}
		
		JSONObject json = parseInvoiceResult(in,(String)in_parameter.get("fpcy.fpdm"));
		List<ResultBean> list = new ArrayList<ResultBean>();
		String key[] = null ;
		if (json == null) {
			out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE20011);
		} 
	 if(json.length() == 8) {
			String key1[] = {"机打发票代码", "机打号码", "发票金额",
					"开票日期", "开票方名称", "开票方识别号", "税务机构名称"};
			key = key1;
		}else if(json.length() == 5) {
			String key3[] = { "售票单位", "售票时间", "购票单位名称", "购票单位税号" };
			key = key3;
		}else if(json.length() == 10) {
			String key4[] = { "付款方名称", "收款方名称", "收款方识别号或证件号码","代开普通发票申请表号码", "品目",
					"金额", "合计人民币(大写)","合计人民币(小写)", "税额(大写)"};
			key = key4;
		}else{
			String key2[] = {"机打代码", "防伪码", "机打号码", "用户名",
					"纳税人识别号", "地址、电话", "收费项目", "金额(大、小写)", "销售方名称",
					"开票人", "用户户号", "开票日期"};
			key = key2;
		}
			try {
				if ("false".equals(json.get("result"))) {
					list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
					out_result.put(SysConfig.INVOICEFALSESTATE,json.getString(SysConfig.INVOICEFALSESTATE));
					out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE20011);
				} else {
					if(paras.getSwjg_dm().equals("4600")||paras.getSwjg_dm().equals("04600")) {
						String key3[] = { "查询次数", "开票日期", "(销售方)名称", "(销售方)纳税人识别号",
								"(销售方)地址.电话", "(销售方)开户行及账号", "(购买方)名称", "(购买方)纳税人识别号",
								"(购买方)地址.电话", "(购买方)开户行及账号", "税额", "价税合计", "机器编号",
								"金额", "是否作废" };
						if (true) {
						} else {
							list.add(new ResultBean("", "校验码", json.get("校验码")
									.toString()));
						}
						for (int i = 0; i < key3.length; i++) {
							if (!json.get(key3[i]).toString().equals("")) {
								list.add(new ResultBean("", key3[i], json.get(key3[i])
										.toString()));
							}
						}
						Iterator<String> it = json.keys();
						while (it.hasNext()) {
							String jsonkeys = (String) it.next();
							if (jsonkeys.contains("jsonhw")) {
								String jsonhw = json.getString(jsonkeys);
								list.add(new ResultBean("", jsonkeys, jsonhw));
							}
						}
						list.add(new ResultBean("CXJG", "查询结果", "成功"));
						out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
					}else{
						list = ResultUtils.getListInfoFromJson1(key, json);
						list.add(new ResultBean("CXJG", "查询结果", "查验成功"));
						out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE1000);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
				out_result.put(SysConfig.CYJGSTATE, SysConfig.CODE20011);
			}
		out_result.put("list", list);
	}
	@SuppressWarnings("unused")
	public JSONObject parseInvoiceResult(InputStream in,String fpdm) {
		JSONObject jso = null;
		BufferedReader bufferedReader = null;
		JSONObject json = new JSONObject();
		try {
			json.put("result", "false");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		Boolean flag = false;
		try {
			if (in != null) {
				jso = new JSONObject();
				bufferedReader = new BufferedReader(new InputStreamReader(in,
						"UTF-8"));
				String ss = null;
				String html = "";
				while ((ss = bufferedReader.readLine()) != null) {
					html += ss;
				}
				//FileUtils.writeStringToFile(new File("D:/CHENXY.xml"), html);
				if (html.contains("对不起，您查询的发票不是海南国税发票")) {
					json.put("cwxx", "对不起，您查询的发票不是海南国税发票，暂无法查询。");
					json.put(SysConfig.INVOICEFALSESTATE,SysConfig.INVOICEFALSESTATECODE216);
					return json;
				}
				if (html.contains("您所查询的发票无相关记录")) {
					json.put("cwxx", "您所查询的发票无相关记录，疑似问题发票，欢迎举报。");
					json.put(SysConfig.INVOICEFALSESTATE,SysConfig.INVOICEFALSESTATECODE201);
					return json;
				}
				if (html.contains("扫描查验")) {
					json.put("cwxx", "发票查询异常，请重试！");
					return json;
				} else {
					 if(html != null) {
						 if(fpdm.substring(0, 4).equals("4600")||fpdm.substring(0, 5).equals("04600")) {
							 String key[] = { "查询次数", "开票日期", "(购买方)名称", "(购买方)纳税人识别号",
										"(购买方)地址.电话", "(购买方)开户行及账号", "(销售方)名称", "(销售方)纳税人识别号",
										"(销售方)地址.电话", "(销售方)开户行及账号", "金额", "税额", "价税合计",
										"机器编号", "校验码", "是否作废" };
							 String key1[] = {"cxcs","KPRQ","SPFMC","SPFSBH","SPFDZ","","XHFMC","XHFSBH","XHFDZ","","","SE",
									 "KPHJJE","","","SSCXBZ"};
							 JSONObject kk = new JSONObject(html);
							 JSONObject kk1= new JSONObject(kk.get("data").toString());
							 for(int i=0;i<key.length;i++) {
								 if(key1[i].isEmpty()) {
									 json.put(key[i], "");
								 }else{
									 json.put(key[i], kk1.get(key1[i]));
								 }
							 }
							 String key2[] = { "货物或应税劳务名称", "规格型号", "单位", "数量", "单价", "金额",
										"税率", "税额" };
							 String key3[] = { "SPMC", "", "JLDW_MC", "SPSL", "SPDJ", "SPJE",
										"", "" };
							 String mingxi[] = kk.get("MXlist").toString().replace("[", "").replace("]", "").split("},");
							 for(int i = 0;i<mingxi.length;i++) {
								 JSONObject jsonhw = new JSONObject();
								 
									/*if (fpdm.length() == 12) {
										for (int j = 0; j < key3dz.length; j++) {
											jsonhw.put(key3dz[j], valuehw11[j]);
										}
									} else {*/
										JSONObject jj = new JSONObject(mingxi[i]+"}");
										for(int j = 0;j<key3.length;j++){
											if(key3[j].isEmpty()) {
												jsonhw.put(key2[j],"");
											}else{
												jsonhw.put(key2[j],jj.get(key3[j]));
											}
										}
									//}
									//jsonhw.put("税率", jsonhw.get("税率") + "%");
									if (i < 9) {
										json.put("jsonhw0" + (i + 1), jsonhw);
									} else {
										json.put("jsonhw" + (i + 1), jsonhw);
									}
							 }
							 json.put("result", "true");
							 //JSONObject kk2= new JSONObject();
						 }else{
						// 查询出结果
						Document doc = Jsoup.parse(html);
						Elements element1 = doc.select("div[class=normalText]");
						//通用定额
						if(element1.size() == 0) {
							Element element3 = doc.select("tbody").select("table[class=commandTable]").get(1);
							Elements element2 = element3.getAllElements();
							String str = element2.get(0).text().toString().trim();
							String str1[] = str.split(" ");
							if(str1.length>10) {
								String key[] = { "机打发票代码", "机打号码", "发票金额",
										"开票日期", "开票方名称", "开票方识别号", "税务机构名称" };
								for (int i = 2; i < str1.length; i++) {
									for(int j = 0;j<key.length;j++) {
										if(str1[i].contains(key[j])) {
											json.put(key[j], str1[i+1]);
										}
									}
									i = i+1;
								}
							}else{
								String key[] = { "售票单位", "售票时间", "购票单位名称", "购票单位税号" };
								for (int i = 1; i < str1.length; i++) {
									for(int j = 0;j<key.length;j++) {
										if(str1[i].contains(key[j])) {
											json.put(key[j], str1[i+1]);
										}
									}
									i = i+1;
								}
							}
							
							
						} else {
							int count = 0;
							String key[] = null;
							if(html.contains("代开普通发票申请表号码") || html.contains("收款方识别号或证件号码")) {
								String key1[] = { "付款方名称", "收款方名称", "收款方识别号或证件号码","代开普通发票申请表号码", "品目",
										"金额", "合计人民币(大写)","合计人民币(小写)", "税额(大写)"};
								key =  key1;
							}else{
								String key2[] = { "机打代码", "防伪码", "机打号码", "用户名",
										"纳税人识别号", "地址、电话", "收费项目", "金额(大、小写)", "销售方名称",
										"开票人", "用户户号", "开票日期"};
								key = key2;
							}
							
							if(determineInvoiceType(html)) {
								String str[] = element1.text().toString().split(" ");
								for(int i = 0; i < str.length; i++) {
									json.put(key[i], str[i]);
								}
							}else{
							for (int i = 0; i < element1.size(); i++) {
								String str = element1.get(i).text().toString();
								if (i == 0) {
									String str1 = element1.text();
										json.put(key[count++],
												str.substring(0, str.indexOf(" ")));
										json.put(
												key[count++],
												str.substring(str.indexOf(" ") + 1,
														str.length() - 1));
								} else if (i == 8 || i == 10 || i == 11 || i == 12
										|| i == 15) {
									continue;
								} else {
									json.put(key[count++], str);
								}
							}
							}
						}
						json.put("result", "true");
						 }
					 } 
					 
					if ((html.contains("作废发票") || html.contains("异常发票")) && json.length()<2) {
						Document doc = Jsoup.parse(html);
						Element script = doc.select("script").get(11);
						String scr[] = script.data().split("';|= '");
						int count = 0;
						// 返回结果需要用到的变量
						String fphjdm = null;
						String cxcs = null, gpfmc = null, swjgmc = null, fpzl_mc = null;
						String fpfsrq = null, ycrq = null;
						String fpzl_lb = null, infoerror = null, wrongje = null, yw_dm = null;
						String isOld = null, fp_yjjg_dm = null, isXpjk = null;

						for (int i = 0; i < scr.length; i = i + 2) {
							if (!scr[i].contains("txt")
									|| !scr[i].contains("if")) {
								String key = scr[i].substring(
										scr[i].indexOf("var") + 4,
										scr[i].length() - 1);
								String value = scr[i + 1];
								count++;
								if (key.equals("cxcs")) {
									cxcs = scr[i + 1];
								} else if (key.equals("gpfmc")) {
									gpfmc = scr[i + 1];
								} else if (key.equals("swjgmc")) {
									swjgmc = scr[i + 1];
								} else if (key.equals("fpzl_mc")) {
									fpzl_mc = scr[i + 1];
								} else if (key.equals("fpfsrq")) {
									fpfsrq = scr[i + 1];
								} else if (key.equals("ycrq")) {
									ycrq = scr[i + 1];
								} else if (key.equals("fphjdm")) {
									fphjdm = scr[i + 1];
								} else if (key.equals("infoerror")) {
									infoerror = scr[i + 1];
									flag = true;
								} else if (key.equals("fpzl_lb")) {
									fpzl_lb = scr[i + 1];
								} else if (key.equals("wrongje")) {
									wrongje = scr[i + 1];
								} else if (key.equals("yw_dm")) {
									yw_dm = scr[i + 1];
								} else if (key.equals("isOld")) {
									isOld = scr[i + 1];
								} else if (key.equals("fp_yjjg_dm")) {
									fp_yjjg_dm = scr[i + 1];
								} else if (key.equals("isXpjk")) {
									isXpjk = scr[i + 1];
								}
							} else {
								break;
							}
						}
						// 拼接结果
						String txt = null;
						if (!flag) {
							if (fphjdm.equals("0202") || fphjdm.equals("0203")
									|| fphjdm.equals("0302")
									|| fphjdm.equals("0303")) {
								txt = "该票是第" + cxcs + "次查询，为" + gpfmc + "从"
										+ swjgmc + "领购的" + fpzl_mc
										+ "，是作废发票，如有疑问，请致电12366-1咨询或欢迎举报。";
							} else {
								txt = "该票是第" + cxcs + "次查询，为" + gpfmc + " "
										+ fpfsrq + "从" + swjgmc + "领购的"
										+ fpzl_mc + "，" + ycrq
										+ "已被我局列为异常发票，不能作为合法的财务报销凭证，欢迎举报。";
							}
						} else {
							String text = dealWithError(yw_dm, infoerror,
									fpzl_lb, cxcs, gpfmc, fpfsrq, swjgmc,
									isOld, fp_yjjg_dm, fpzl_mc, wrongje, isXpjk);
							txt = text;
						}
						json.put("cwxx", txt);
						return json;
					}
					return json;
				}
			} else {
				json.put("cwxx", "系统繁忙，请稍后再试！");
				return json;
			}
		} catch (Exception e) {
			System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
			try {
				json.put("cwxx", "查询出现异常，请重新查询");
			} catch (JSONException e1) {
				 
				e1.printStackTrace();
			}
			return json;
		} finally {
			try {
				if (in != null)
					in.close();
				if (bufferedReader != null)
					bufferedReader.close();
			} catch (Exception e2) {
				System.out.println(("[ERROR] 程序执行操作出现异常：" + e2.getMessage()));
				try {
					json.put("cwxx", "您输入的发票信息不匹配，请检查是否输入正确！");
				} catch (JSONException e) {

					e.printStackTrace();
				}
				return json;
			}
		}
	}

	private boolean determineInvoiceType(String html) {
		if(html.contains("代开普通发票申请表号码") || html.contains("收款方识别号或证件号码")) {
			return true;
		}else{
			return false;
		}
	}

	@SuppressWarnings({ "rawtypes", "unused" })
	public InputStream sendResultRequestPost(String requestAddress,
			Map attributeMap, String JSESSIONID) {
		InputStream in = null;
		OutputStream out = null;
		DataOutputStream dos = null;
		String attributeString = null;
		String code = null;
		try {
			URL url = new URL(requestAddress);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setUseCaches(false);
			con.setRequestProperty(
					"User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
			con.setRequestProperty("Referer",
					"http://221.176.87.246:9090/fpcy/main.jsp");
			con.setRequestProperty("Host", "221.176.87.246:9090");
			con.setRequestProperty("Origin", "http://221.176.87.246:9090");
			con.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");

			/**
			 * 这里是为了得到网站JSESSIONID的后半部分的内容，其中getJsessionId()方法为新增的方法。2016-1-15
			 * 09:47:10 by王文东 若不增加后边的这块JSESSIOINID,则访问网站取不到数据
			 * 取得的abc的值为JSESSIONID_NS_Sig=Z6cvfJ55838MeLCt; path=/; HttpOnly
			 * */

			String abc = getJsessionId("http://221.176.87.246:9090/fpcy/");
			if (attributeMap.containsKey("JSESSIONID")) {
				JSESSIONID = JSESSIONID.substring(0, JSESSIONID.indexOf(";"));
				String cookie = JSESSIONID + "; " + abc;

				con.setRequestProperty("Cookie", cookie);
				attributeMap.remove("JSESSIONID");
			}
			// 拼接数据的代码，返回为拼接好的数据
			attributeString = SendResultRequest.structureAttribute(attributeMap);
			out = con.getOutputStream();// 连接问题
			dos = new DataOutputStream(out);
			dos.writeBytes(attributeString);
			dos.flush();
			code = con.getResponseCode() + "";
			if (con.getResponseCode() == 200) {
				System.out.println("responsecode is:" + con.getResponseCode());
			}
			if (con.getResponseCode() != 200)
				return null;
			in = con.getInputStream();// 获取发票验真返回信息
			return in;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (dos != null)
					dos.close();
				if (out != null)
					out.close();
			} catch (Exception w) {
				w.printStackTrace();
			}
		}
		return in;
	}

	/**
	 * 新增方法，得到jsessionid
	 * 
	 * @params: urls 传入要得到jessionid的网址
	 * @return string
	 * */
	@SuppressWarnings({ "unused", "unchecked", "rawtypes" })
	public String getJsessionId(String urls) {
		String urls1 = urls;
		URL urlss;
		List<String> cookieList = null;
		InputStream in11 = null;
		String jsessionid = null;
		String cookie = null;
		try {
			urlss = new URL(urls1);
			HttpURLConnection con;

			con = (HttpURLConnection) urlss.openConnection();
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setUseCaches(false);
			con.setRequestProperty("Upgrade-Insecure-Requests", "1");
			con.setRequestProperty(
					"User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
			con.setRequestProperty("Referer",
					"http://222.76.203.36:7002/fpcx/jsp/fpzwcx/index.jsp?type=dep");
			con.setRequestProperty("Host", "222.76.203.36:7002");
			con.setRequestProperty("Accept-Encoding", "gzip, deflate");
			con.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
			con.setRequestProperty("Origin", "http://222.76.203.36:7002");
			con.setRequestProperty("Accept",
					"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			con.setRequestMethod("GET");
			Map topheader = con.getHeaderFields();// 相应头信息
			Set headerSet = topheader.entrySet();// 返回此映射中包含的映射关系的 Set 视图
			Iterator it = headerSet.iterator();

			while (it.hasNext()) {
				Entry<String, List> en = (Entry<String, List>) it.next();
				if ("set-cookie".equalsIgnoreCase(en.getKey())) {
					cookieList = en.getValue();
				}
			}
			StringBuffer cookieStr = new StringBuffer();
			int j = 0;
			do {
				cookieStr.append(cookieList.get(j) + "：");
				j++;
			} while (j < cookieList.size());
			StringBuffer sb = new StringBuffer();
			for (String jsestr : cookieList) {
				if (jsestr.contains("JSESSIONID_NS_Sig")) {

					sb.append(jsestr);
				}
			}
			cookie = sb.toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
		}
		return cookie = cookie.substring(0, cookie.indexOf(";"));
	}

	public String dealWithError(String yw_dm, String infoerror, String fpzl_lb,
			String cxcs, String gpfmc, String fpfsrq, String swjgmc,
			String isOld, String fp_yjjg_dm, String fpzl_mc, String wrongje,
			String isXpjk) {
		String txt = "";
		if (yw_dm != null && yw_dm != "") {
			if (infoerror.equals("1")) {
				if (fpzl_lb == "1" || fpzl_lb == "2" || fpzl_lb == "3") {
					txt = "该票是第" + cxcs + "次查询，为" + gpfmc + "" + fpfsrq + "从"
							+ swjgmc + "领购的" + fpzl_mc
							+ "，该发票的开票信息需在开票方向我局报送相关开具信息的次日方能查询，如有疑问欢迎举报！";
				}
				if (fpzl_lb == "4" || fpzl_lb == "5" || fpzl_lb == "6"
						|| fpzl_lb == "7") {
					txt = "该票是第" + cxcs + "次查询，为" + gpfmc + "" + fpfsrq + "从"
							+ swjgmc + "领购的" + fpzl_mc
							+ "，该发票的开票信息需在开票方开具1小时后方能查询，如有疑问欢迎举报！";
				}
				if (fpzl_lb == "8") {
					txt = "该票是第" + cxcs + "次查询，为" + gpfmc + "" + fpfsrq + "从"
							+ swjgmc + "领购的" + fpzl_mc
							+ "，该发票的开票信息需在开票方向我局报送相关开具信息的次日方能查询，如有疑问欢迎举报！";
				}
			}
			if (infoerror.equals("2")) {
				txt = "该发票是第" + cxcs
						+ "次查询，您输入的信息与税务机关记录信息不符，如您确认输入无误则疑似问题发票，欢迎举报。";
			}
			if (infoerror.equals("3")) {
				txt = "该发票是第" + cxcs + "次查询，您输入的金额" + wrongje
						+ "元与税务机关记录信息不符，如您确认输入无误则疑似问题发票，欢迎举报。";
			}
		}
		if (isOld.equals("1")) {
			if (fp_yjjg_dm != null && fp_yjjg_dm != "") {
				if (fp_yjjg_dm.equals("10")) {
					txt = "该票是第" + cxcs + "次查询，为" + gpfmc + "" + fpfsrq + "从"
							+ swjgmc + "领购的" + fpzl_mc
							+ "，已于2011年1月1日起废止使用，如有疑问欢迎举报！";
				}
				if (fp_yjjg_dm.equals("20") || fp_yjjg_dm.equals("30")) {
					txt = "该票是第" + cxcs + "次查询，为" + gpfmc + "" + fpfsrq + "从"
							+ swjgmc + "领购的" + fpzl_mc
							+ "，是作废发票，如有疑问，请致电12366-1咨询或欢迎举报。";
				}
				if (fp_yjjg_dm.equals("90")) {
					txt = "该票是第" + cxcs + "次查询，为" + gpfmc + "" + fpfsrq + "从"
							+ swjgmc + "领购的" + fpzl_mc
							+ "，该票已被我局列为异常发票，不能作为合法的财务报销凭证，欢迎举报。";
				}
			} else {
				txt = "该票是第" + cxcs + "次查询，为" + gpfmc + "" + fpfsrq + "从"
						+ swjgmc + "领购的" + fpzl_mc
						+ "，该票已被我局列为异常发票，不能作为合法的财务报销凭证，欢迎举报。";
			}
		}
		if (isXpjk.equals("1")) {
			txt = "该票是第" + cxcs + "次查询，为" + gpfmc + "" + fpfsrq + "从" + swjgmc
					+ "领购的" + fpzl_mc
					+ "，该发票的开票信息暂不支持查询，如有疑问，请致电12366-1咨询或欢迎举报。";//
		}
		if (fpzl_lb != null && fpzl_lb != ""
				&& (yw_dm.isEmpty() || yw_dm.equals("")) && isOld != "1"
				&& isXpjk != "1") {
			if (fpzl_lb.equals("1") || fpzl_lb == "2" || fpzl_lb == "3") {
				txt = "该票是第" + cxcs + "次查询，为" + gpfmc + "" + fpfsrq + "从"
						+ swjgmc + "领购的" + fpzl_mc
						+ "，该发票的开票信息需在开票方向我局报送相关开具信息的次日方能查询，如有疑问欢迎举报！";
			}
			if (fpzl_lb == "4" || fpzl_lb == "5" || fpzl_lb == "6"
					|| fpzl_lb == "7") {
				txt = "该票是第" + cxcs + "次查询，为" + gpfmc + "" + fpfsrq + "从"
						+ swjgmc + "领购的" + fpzl_mc
						+ "，该发票的开票信息需在开票方开具1小时后方能查询，如有疑问欢迎举报！";
			}
			if (fpzl_lb == "8") {
				txt = "该票是第" + cxcs + "次查询，为" + gpfmc + "" + fpfsrq + "从"
						+ swjgmc + "领购的" + fpzl_mc
						+ "，该发票的开票信息需在开票方向我局报送相关开具信息的次日方能查询，如有疑问欢迎举报！";
			}
		}
		return txt;
	}
	public static String parseInvoiceResult111(InputStream in) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("result", "false");
		try {
			if (in != null) {
				Document doc = (Document) SendResultRequest.iSToJSONOrDocument(
						in, "UTF-8", "text");
				Elements body=doc.select("form").select("input[name=token]");
				String s=body.val();
				return s;
			}
		} catch (Exception e) {
			json.put("cwxx", "您输入的发票信息不匹配，请检查是否输入正确！");
			json.put("result", "false");
			e.printStackTrace();
		}

		return null;
	}
	@SuppressWarnings({ "rawtypes"})
	public Map FPCY(Map parameter) throws Exception {
		return null;
	}
	
	
	public static void main(String[] args) throws JSONException {
		/*IpAddress ipAddress = new IpAddress("10.126.3.112", 3128);
		Map<String, String> requestHeader = new HashMap<String, String>();
		Map<String, Object> map = new HashMap<String, Object>();
		requestHeader.put("Connection","keep-alive");
		requestHeader.put("Host", "221.176.87.246:9090");
		requestHeader.put("Cache-Control", "max-age=0");
		requestHeader.put("Upgrade-Insecure-Requests","1");
		map = SendResultRequest.sendRequestSC(requestHeader,
				ipAddress, null,"http://221.176.87.246:9090/fpcy/main.jsp","GET");
		InputStream inn=(InputStream)map.get("in");
		String token = parseInvoiceResult111(inn);
		System.out.println(token);*/
	}
}
