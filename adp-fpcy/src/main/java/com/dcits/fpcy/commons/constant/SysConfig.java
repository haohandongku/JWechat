package com.dcits.fpcy.commons.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统错误
 * 
 * 概设：对于1xx开头是为了第一次获取获取发票信息拿不到，之后能重复去查发票才定义错误码，该错误码不能返回用户或系统
 *     对于2xx错误码直接暴露给用户或系统方便查找原因
 *     新增217，230,240，和新的常量systemfalseState
 *     改造系统错误码与用户提示分开
 * @author wuche
 *
 */
public class SysConfig {
	/* 查验页面可以输入ip */
	public static final String level = "debug";
	/* 查验地址 */
	public static final String CYADDRESS = "";
	/*初始化httpsession 1 需要初始化 2 初始化完毕不需要再次初始化*/
	public static int INITSESSION = 1;
	public static int POOLSIZE = 2;
	public static final String CODE1000 = "1000";
	public static final String CODE9000 = "9000";
	public static final String CODE1000val = "success";
	public static final String CODE20011 = "2001";
	public static final String CODE2001lval="查询异常，请重试！";
	/* _1001 用户名或密码错误 */
	public static final String CODE1001 = "1001";
	public static final String CODE1001val = "用户名或密码错误";
	public static final String CYJGSTATE="cyjgState";
	/*各种错误异常状态码(直接返给用户)*/
	public static final String INVOICEFALSESTATE="invoicefalseState";
	/*各种错误异常状态码（系统统计使用）*/
	public static final String SYSTEMFALSESTATE="systemfalseState";
	/*打码正确状态码*/
	public static final String INVOICETRUESTATE000="000";
	/*数据来源于数据库*/
	public static final String INVOICETRUESTATE001="001";
	/*数据来源于税局请求*/
	public static final String INVOICETRUESTATE002="002";
	/*验证码错误*/
	public static final String INVOICEFALSESTATECODE101="101";
	/*查验i/o流为空*/
	public static final String INVOICEFALSESTATECODE102="102";
	/*获取对象内容为空*/
	public static final String INVOICEFALSESTATECODE103="103";
	/*解析返回参数异常*/
	public static final String INVOICEFALSESTATECODE104="104";	
	/*获取验证码异常还可以在请求*/
	public static final String INVOICEFALSESTATECODE105="105";
	/*打码服务失败*/
	public static final String INVOICEFALSESTATECODE106="106";
	/*初步解析页面异常（页面更新）*/
	public static final String INVOICEFALSESTATECODE111="111";	
	/*网络超时，请重试*/
	public static final String INVOICEFALSESTATECODE113="113";
	/*由于查验行为异常，涉嫌违规，当前无法使用查验服务！*/
	public static final String INVOICEFALSESTATECODE114="114";
	/*请求不合法*/
	public static final String INVOICEFALSESTATECODE115="115";
	/*超过服务器最大请求数，请稍后访问*/
	public static final String INVOICEFALSESTATECODE116="116";
	/*空指针异常*/
	public static final String INVOICEFALSESTATECODE117="117";
	/*验证码失效*/
	public static final String INVOICEFALSESTATECODE118="118";
	/*发票查验请求太频繁，请稍后再试*/
	public static final String INVOICEFALSESTATECODE128="128";
	/*查无此票*/
	public static final String INVOICEFALSESTATECODE201="201";
	/*超过此票单日查验5次*/
	public static final String INVOICEFALSESTATECODE202="202";
	/*用户信息填写错误（传入数据错误，校验数据异常）*/
	public static final String INVOICEFALSESTATECODE203="203";
	/*此发票未显示购票单位，请到主管税务机关进行发票真伪鉴定！*/
	public static final String INVOICEFALSESTATECODE204="204";	
	/*查询码格式错误*/
	public static final String INVOICEFALSESTATECODE205="205";
	/*该发票可能不是纳税人购买的*/
	public static final String INVOICEFALSESTATECODE206="206";
	/*代码或号码格式有误*/
	public static final String INVOICEFALSESTATECODE210="210";
	/*用户填写信息错误或者为假票*/
	public static final String INVOICEFALSESTATECODE211="211";
	/*发票种类无法界定*/
	public static final String INVOICEFALSESTATECODE212="212";
	/*查询失败,服务忙（调用查询税局返回错误统一）*/
	public static final String INVOICEFALSESTATECODE213="213";
	/*查询次数上限*/
	public static final String INVOICEFALSESTATECODE214="214";
	/*发票存在，但无法查询到具体信息*/
	public static final String INVOICEFALSESTATECODE215="215";
	/*您查询的发票是当日开具的，请于次日查询！*/
	public static final String INVOICEFALSESTATECODE216="216";
	/*过了查票期*/
	public static final String INVOICEFALSESTATECODE217="217";
	/*税局查验服务暂时不可用，请稍后再试*/
	public static final String INVOICEFALSESTATECODE218="218";
	/*您输入的发票暂时不支持查询*/
	public static final String INVOICEFALSESTATECODE219="219";
	/*不一致*/
	public static final String INVOICEFALSESTATECODE220="220";
	/*您输入的发票正在查询中，请不要重复提交请求*/
	public static final String INVOICEFALSESTATECODE221="221";
	/*税局验证码调不通（不能再请求）*/
	public static final String INVOICEFALSESTATECODE230="230";
	/*税局返回验证码打码错误*/
	public static final String INVOICEFALSESTATECODE231="231";
	/*税局返回验证码打码失败*/
	public static final String INVOICEFALSESTATECODE232="232";
	/*税局查询接口调不通*/
	public static final String INVOICEFALSESTATECODE240="240";
	/*税局返回假数据*/
	public static final String INVOICEFALSESTATECODE241="241";
	/*服务错误码信息
	 * 错误码分为三类： 第一类：系统性错误码（用户不可见）【需要再次去请求】【爬虫的重试机制】
	 *            第二类：用户可见错误码【便于用户做出判断】
	 *            第三类：日志型错误码 （包含第二类，部分可见）【便于统计】
	 * */
	private static Map<String, String> map = new HashMap<String, String>();
    static {
        map.put(CODE9000, "请求成功");//舍弃
        map.put(CODE1000, "请求成功");//用户可见
        map.put(CODE1001, "用户名或密码错误");//用户可见【舍弃】-->计费所用
        map.put(INVOICETRUESTATE000, "正确状态码");//系统性错误码
        map.put(INVOICETRUESTATE001, "数据来源于数据库");//日志型（用户不可见）
        map.put(INVOICETRUESTATE002, "数据来源于税局请求");//日志型（用户不可见）
        map.put(INVOICEFALSESTATECODE101, "验证码错误");//系统性错误码
        map.put(INVOICEFALSESTATECODE102, "查验i/o流为空");//系统性错误码
        map.put(INVOICEFALSESTATECODE103, "获取对象内容为空");//系统性错误码
        map.put(INVOICEFALSESTATECODE104, "解析返回参数异常");//系统性错误码
        map.put(INVOICEFALSESTATECODE105, "获取验证码异常还可以在请求");//系统性错误码
        map.put(INVOICEFALSESTATECODE106, "第三方打码服务失败");//系统性错误码
        map.put(INVOICEFALSESTATECODE111, "税局查询初步解析页面异常（页面更新）");//系统性错误码
        map.put(INVOICEFALSESTATECODE113, "税局查询网络超时，请重试");//系统性错误码
        map.put(INVOICEFALSESTATECODE114, "税局查询由于查验行为异常，涉嫌违规，当前无法使用查验服务！");//系统性错误码
        map.put(INVOICEFALSESTATECODE115, "税局查询请求不合法");//系统性错误码
        map.put(INVOICEFALSESTATECODE116, "税局查询超过服务器最大请求数，请稍后访问");//系统性错误码
        map.put(INVOICEFALSESTATECODE117, "空指针异常");//系统性错误码
        map.put(INVOICEFALSESTATECODE118, "税局查询验证码失效");//系统性错误码
        map.put(INVOICEFALSESTATECODE128, "*税局发票查验请求太频繁，请稍后再试");//系统性错误码
        map.put(INVOICEFALSESTATECODE201, "查无此票");//用户可见
        map.put(INVOICEFALSESTATECODE202, "超过此票单日查验5次");//用户可见
        map.put(INVOICEFALSESTATECODE203, "您输入的${data}格式不正确，请你重新输入发票信息！（格式：${type}）");//用户可见
        map.put(INVOICEFALSESTATECODE204, "此发票未显示购票单位，请到主管税务机关进行发票真伪鉴定！");//用户可见
        map.put(INVOICEFALSESTATECODE205, "查询码格式错误");//用户可见
        map.put(INVOICEFALSESTATECODE206, "该发票可能不是纳税人购买的");//用户可见
        map.put(INVOICEFALSESTATECODE210, "代码或号码格式有误");//用户可见
        map.put(INVOICEFALSESTATECODE211, "用户填写信息错误或者为假票");//用户可见
        map.put(INVOICEFALSESTATECODE212, "发票种类无法界定");//用户可见
        map.put(INVOICEFALSESTATECODE213, "查询失败,服务忙");//用户可见（未知异常）
        map.put(INVOICEFALSESTATECODE214, "查询次数上限");//用户可见
        map.put(INVOICEFALSESTATECODE215, "发票存在，但无法查询到具体信息");//用户可见
        map.put(INVOICEFALSESTATECODE216, "您查询的发票是当日开具的，请于次日查询！");//用户可见
        map.put(INVOICEFALSESTATECODE217, "过了查票期");//用户可见
        map.put(INVOICEFALSESTATECODE218, "税局查验服务暂时不可用，请稍后再试");//用户可见)-->对应的日志内型（230,231,232,240）
        map.put(INVOICEFALSESTATECODE219, "您输入的发票暂时不支持查询");
        map.put(INVOICEFALSESTATECODE220, "您输入的发票信息不一致");//用户可见
        map.put(INVOICEFALSESTATECODE221, "您输入的发票正在查询中，请不要重复提交请求");//用户可见（不记录）
        map.put(INVOICEFALSESTATECODE230, "税局验证码调不通（不能再请求）");//日志型（用户不可见）
        map.put(INVOICEFALSESTATECODE231, "税局返回验证码打码错误");//日志型（用户不可见）
        map.put(INVOICEFALSESTATECODE232, "税局返回验证码打码失败");//日志型（用户不可见）
        map.put(INVOICEFALSESTATECODE240, "税局查询接口调不通");//日志型（用户不可见）
        map.put(INVOICEFALSESTATECODE241, "税局返回假数据");//日志型（用户不可见）
    }
	public static Map<String, String> getMap() {
		return map;
	}
    
}
