package com.dcits.fpcy.commons.service.core.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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

public class GZDSfpcyImp implements InvoiceServerBase {
	/**
	 * 贵州地税发票校验
	 */
	private Log logger = LogFactory.getLog(GZDSfpcyImp.class);
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map FPCY(Map parameter, TaxOfficeBean fpcyParas){
		Map<String, Object> result = new HashMap<String, Object>();
		HSCEntity hscEntity = null;
		try {
			hscEntity = (HSCEntity) parameter.get("hscEntity");
			parameter.put("rand", hscEntity.getYzm());
			parameter.put("JSESSIONID", hscEntity.getCookie1());
			// 开始查验
			getResult(fpcyParas, parameter, result, hscEntity.getIpAddress());
		} catch (Exception e) {
			logger.error("贵州地税,获取cook时出现异常", e);
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

	// 获取验证码验真结果
	/**
	 * 
	 * @param paras
	 *            查验参数
	 * @param in_Parameter
	 *            请求数据 [ 验证码 jessionid]
	 * @param out_Result
	 *            返回结果
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void getResult(TaxOfficeBean paras, Map in_parameter, Map out_result,
			IpAddress ipAddress) throws Exception{
		Map attributeMap = new HashMap();
		Map<String, String> requestHeader = new HashMap<String, String>();
		// 得到当前代理ip，在拼接参数的时候需要用
		String localip = ipAddress.getIp().toString();
		// 字符串的拼接，将所有数据拼接起来组成一个数据，与其他数据一起传送
		String jsonStr = "{\"fp_dm\":\""
				+ in_parameter.get("FPDM").toString()
				+ "\",\"fphm\":\""
				+ in_parameter.get("FPHM").toString()
				+ "\",\"je\":\""
				+ in_parameter.get("KPJE").toString()
				+ "\",\"ip\":\""
				+ localip
				+ "\",\"kprq\":\""
				+ in_parameter.get("KPRQ").toString()
				+ "\",\"rand\":\""
				+ in_parameter.get("rand").toString()
				+ "\",\"pageNo\":1,\"pageSize\":20,\"dealMethod\":\"doService\"}";
		String requestMethod = paras.cy_qqfs;
		String JSESSIONID = in_parameter.get("JSESSIONID").toString();
		attributeMap.put("parameters", jsonStr);
		attributeMap.put("service", "S_WLFPCY");
		attributeMap.put("serviceMethod", "doService");
		requestHeader.put(HeaderType.USERAGENT, BrowerType.firfox);
		requestHeader.put(HeaderType.COOKIE, JSESSIONID);
		InputStream in = SendResultRequest.sendRequestPost(requestHeader,
				ipAddress, paras.cy_dz, attributeMap);// 开始发送请求
		JSONObject json = parseInvoiceResult(in);
		List<ResultBean> list=new ArrayList<ResultBean>();
		if (json == null) {
			out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE20011);
		} else if (json.get("result").toString().equals("false")) {
			list.add(new ResultBean("cwxx", "", json.get("cwxx").toString()));
			out_result.put(SysConfig.INVOICEFALSESTATE,json.getString(SysConfig.INVOICEFALSESTATE));
			out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE20011);
		} else {
			JSONObject cxjg = new JSONObject(json.get("cxjg").toString());
			String key[] = {"开票日期","开票方名称","收票方名称","金额","收费种类"};
			list = ResultUtils.getListInfoFromJson(key, json);
			list.add(new ResultBean("CXJG","查询结果", cxjg.get("rtnMsg").toString()));
			out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE1000);
		}
		if ("get".equals(requestMethod)) {
			out_result.put(SysConfig.CYJGSTATE,SysConfig.CODE20011);
		}
		out_result.put("list", list);
	}

	/**
	 * 贵州地税结果解析
	 */
	public JSONObject parseInvoiceResult(InputStream in) throws Exception{
		JSONObject json = new JSONObject();
		json.put("result", "false");
		Document document = (Document) SendResultRequest.iSToJSONOrDocument(in,
				"utf-8", "text");
		String html = document.toString().replace("&quot;", "");
		try {
			Document doc = Jsoup.parse(html);
			System.out.println(doc.toString());
			if (html.contains("校验码不符")) {
				json.put("result", "false");
				json.put(SysConfig.INVOICEFALSESTATE,
						SysConfig.INVOICEFALSESTATECODE205);
				json.put("cwxx", "发票查询错误，请重试！");
				return json;

			}
			if (html.contains("查无此票")) {
				json.put("result", "false");
				json.put(SysConfig.INVOICEFALSESTATE,
						SysConfig.INVOICEFALSESTATECODE201);
				json.put("cwxx", "查无此票，请核对并向税务机关报告 请仔细核对后，再进行查询。 ");
				return json;
			} else {
				Elements els = doc.select("body");
				String rtnResult = els.get(0).text();
				String ll = "data";
				int l = rtnResult.lastIndexOf(ll)+5;
				String kk[] = rtnResult.substring(l).split(",");
				json.put("KPRQ", kk[13].toString().split(":")[1]);
				json.put("KPFMC", kk[16].toString().split(":")[1]);
				json.put("SPFMC", kk[7].toString().split(":")[1]);
				json.put("JE", kk[1].toString().split(":")[1]);
				json.put("SFZL", kk[5].toString().split(":")[1]);
				/*FileUtils.writeStringToFile(new File("D:/AA.txt"), rtnResult);
				JSONObject jsonStr = new JSONObject(rtnResult);
				String rtnMsg = jsonStr.getString("rtnMsg");
				jso.put("rtnMsg", rtnMsg);*/
				json.put("result", "true");
				json.put("cxjg", "{\"rtnMsg\":\"" + "此票为真票"
						+ "\"}");
			}
		} catch (Exception e) {
			json.put("result", "false");
			json.put("cwxx", "发票查询异常，请稍候重试！");
			System.out.println(("[ERROR] 程序执行操作出现异常：" + e.getMessage()));
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception e2) {
				json.put("cwxx", "发票查询异常，请稍候重试！");
				json.put("result", "false");
				System.out.println(("[ERROR] 程序执行操作出现异常：" + e2.getMessage()));
			}
		}
		return json;
	}
}
