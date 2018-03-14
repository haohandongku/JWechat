package com.dcits.fpcy.commons.service.core;

import java.util.HashMap;
import java.util.Map;

import com.dcits.fpcy.commons.service.core.impl.AHGSfpcyImp;
import com.dcits.fpcy.commons.service.core.impl.BJDSfpcyImp;
import com.dcits.fpcy.commons.service.core.impl.BJGSfpcyImp;
import com.dcits.fpcy.commons.service.core.impl.DLDSfpcyImp;
import com.dcits.fpcy.commons.service.core.impl.GDDSfpcyImp;
import com.dcits.fpcy.commons.service.core.impl.GSDSfpcyImp;
import com.dcits.fpcy.commons.service.core.impl.GXDSfpcyImp;
import com.dcits.fpcy.commons.service.core.impl.GXGSfpcyImp;
import com.dcits.fpcy.commons.service.core.impl.GZDSfpcyImp;
import com.dcits.fpcy.commons.service.core.impl.HENDSfpcyImp;
import com.dcits.fpcy.commons.service.core.impl.HLJGSfpcyImp;
import com.dcits.fpcy.commons.service.core.impl.HaiNDSfpcyImp;
import com.dcits.fpcy.commons.service.core.impl.HeBGSfpcyImp;
import com.dcits.fpcy.commons.service.core.impl.HeNGSfpcyImp;
import com.dcits.fpcy.commons.service.core.impl.JLDSfpcyImp;
import com.dcits.fpcy.commons.service.core.impl.JLGSfpcyImp;
import com.dcits.fpcy.commons.service.core.impl.LNDSfpcyImp;
import com.dcits.fpcy.commons.service.core.impl.NMGDSfpcyImp;
import com.dcits.fpcy.commons.service.core.impl.NXDSfpcyImp;
import com.dcits.fpcy.commons.service.core.impl.NewVatInvoiceImp;
import com.dcits.fpcy.commons.service.core.impl.SCDSfpcyImp;
import com.dcits.fpcy.commons.service.core.impl.SCGSfpcyImp;
import com.dcits.fpcy.commons.service.core.impl.SDDSfpcyImp;
import com.dcits.fpcy.commons.service.core.impl.SDGSfpcyImp;
import com.dcits.fpcy.commons.service.core.impl.SHGSfpcyImp;
import com.dcits.fpcy.commons.service.core.impl.SXDSfpcyImp;
import com.dcits.fpcy.commons.service.core.impl.ShXDSfpcyImp;
import com.dcits.fpcy.commons.service.core.impl.ShXGSfpcyImp;
import com.dcits.fpcy.commons.service.core.impl.XJDSfpcyImp;
import com.dcits.fpcy.commons.service.core.impl.XJGSfpcyImp;
import com.dcits.fpcy.commons.service.core.impl.XZGSfpcyImp;
import com.dcits.fpcy.commons.service.core.impl.YNDSfpcyImp;
import com.dcits.fpcy.commons.service.core.impl.YNGSfpcyImp;
import com.dcits.fpcy.commons.service.core.impl.HNDSfpcyImp;

/***
 * 如果有能力的话，可以将其转写成spring 代理类方式，
 * 原因：这种方法，每次都会创建新对象，对象的销毁不利于GC
 * @author wuche
 *
 */
public class InvoiceServerManger {
	private static Map<String, InvoiceServerBase> map;
	static {
		map = new HashMap<String, InvoiceServerBase>();
		// 北京增值税（新票）发票校验(国税局)
		map.put("BJGSfpcyImpNew", new NewVatInvoiceImp());
		map.put("ZZSfpcyImpNew1", new NewVatInvoiceImp());
		map.put("SZGSfpcyImpNew", new NewVatInvoiceImp());
		map.put("SHaiGSfpcyImpNew", new NewVatInvoiceImp());
		// 北京国税
		map.put("AHZZSfpcyImp", new AHGSfpcyImp());// 安徽国税
		map.put("BJGSfpcyImp", new BJGSfpcyImp());// 北京国税
		map.put("HeBGSfpcyImp", new HeBGSfpcyImp());// 河北国税
		map.put("HeNGSfpcyImp", new HeNGSfpcyImp());// 河南国税
		map.put("JLGSfpcyImp", new JLGSfpcyImp());// 吉林国税
		map.put("SCGSfpcyImp", new SCGSfpcyImp());// 四川国税
		map.put("ShXGSfpcyImp", new ShXGSfpcyImp());// 山西国税
		map.put("SHGSfpcyImp", new SHGSfpcyImp());// 上海国税
		map.put("SDGSfpcyImp", new SDGSfpcyImp());// 山东国税
		map.put("XZGSfpcyImp", new XZGSfpcyImp());// 西藏国税
		map.put("XJGSfpcyImp", new XJGSfpcyImp());// 新疆国税
		map.put("YNGSfpcyImp", new YNGSfpcyImp());// 云南国税

		map.put("BJDSfpcyImp", new BJDSfpcyImp());// 北京地税
		map.put("NMGDSfpcyImp", new NMGDSfpcyImp());// 内蒙古地税
		map.put("LNDSfpcyImp", new LNDSfpcyImp());// 辽宁地税
		map.put("DLDSfpcyImp", new DLDSfpcyImp());// 大连地税
		map.put("JLDSfpcyImp", new JLDSfpcyImp());// 吉林地税
		map.put("HENDSfpcyImp", new HENDSfpcyImp());// 河南地税
		map.put("GDDSfpcyImp", new GDDSfpcyImp());// 广东地税
		map.put("GXDSfpcyImp", new GXDSfpcyImp());// 广西地税
		map.put("HaiNDSfpcyImp", new HaiNDSfpcyImp());// 海南地税
		map.put("SCDSfpcyImp", new SCDSfpcyImp());// 四川地税
		map.put("GZDSfpcyImp", new GZDSfpcyImp());// 贵州地税
		map.put("YNDSfpcyImp", new YNDSfpcyImp());// 云南地税
		map.put("SXDSfpcyImp", new SXDSfpcyImp());// 陕西地税
		map.put("GSDSfpcyImp", new GSDSfpcyImp());// 甘肃地税
		map.put("NXDSfpcyImp", new NXDSfpcyImp());// 宁夏地税
		map.put("XJDSfpcyImp", new XJDSfpcyImp());// 新疆地税
		map.put("HNDSfpcyImp", new HNDSfpcyImp());//湖南地税
		map.put("SHXDSfpcyImp", new ShXDSfpcyImp());//山西地税
		
		
		// map.put("CQDSfpcyImp", new CQDSfpcyImp());//重庆地税
		
		// map.put("HBDSfpcyImp", new HBDSfpcyImp());//湖北地税
		// map.put("QDDSfpcyImp", new QDDSfpcyImp());//青岛地税
		 map.put("SDDSfpcyImp", new SDDSfpcyImp());//山东地税
		// map.put("JSDSfpcyImp", new JSDSfpcyImp());//江苏地税
		// map.put("JXGSfpcyImp", new JXGSfpcyImp());//江西地税
		// map.put("HLJDSfpcyImp", new HLJDSfpcyImp());//黑龙江地税
		// map.put("ZJDSfpcyImp", new ZJDSfpcyImp());//浙江地税
		// map.put("NBDSfpcyImp", new NBDSfpcyImp());//宁波地税
		// map.put("FJDSfpcyImp", new FJDSfpcyImp());//福建地税
		// map.put("XMDSfpcyImp", new XMDSfpcyImp());//厦门地税

		// map.put("SZGSfpcyImp", new SZGSfpcyImp());// 深圳国税
	    //   map.put("GZGSfpcyImp", new GZGSfpcyImp());// 贵州国税
		// map.put("JSNJGSfpcyImp", new JSNJGSfpcyImp());//江苏南京国税 无票
		// map.put("NXGSfpcyImp", new NXGSfpcyImp());//宁夏国税 官网服务挂了
		// map.put("QDGSfpcyImp", new QDGSfpcyImp());//青岛国税
		// map.put("SXGSfpcyImp", new SXGSfpcyImp());//陕西国税，原功能未实现
		// map.put("CQGSfpcyImp", new CQGSfpcyImp());//重庆国税 官网变更
		// map.put("LNSYGSfpcyImp", new LNGSfpcyImp());//辽宁国税 官网变更，需要重做
		// map.put("FJGSfpcyImp", new FJGSfpcyImp());//福建国税 已关闭
		// map.put("GSGSfpcyImp", new GSGSfpcyImp());//甘肃国税 已关闭，验证码有问题
		// map.put("HBGSfpcyImp", new HBGSfpcyImp());//湖北国税 没有票
		// map.put("ZJGSfpcyImp", new ZJGSfpcyImp());//浙江国税 没有国税
		// map.put("JXGSfpcyImp", new JXGSfpcyImp());//江西国税 验证码有问题
		// map.put("HAINGSfpcyImp", new HAINGSfpcyImp());//海南国税 验证码有问题，逻辑需要修改
		// map.put("GDGSfpcyImp", new GDGSfpcyImp());//广东国税 验证码有问题
		// map.put("XMGSfpcyImp", new XMGSfpcyImp());//厦门国税 暂不支持
		// map.put("NBGSfpcyImp", new NBGSfpcyImp());//宁波国税 验证码有问题
		// map.put("NMGGSfpcyImp", new NMGGSfpcyImp());//内蒙古没有国税
		 map.put("GXGSfpcyImp", new GXGSfpcyImp());//广西国税（需要重新处理验证码）
		 map.put("HLJGSfpcyImp", new HLJGSfpcyImp());//黑龙江国税 查验地址变更
		// map.put("HUNGSfpcyImp", new HUNGSfpcyImp());//湖南国税 暂不支持
	}

	public static InvoiceServerBase getFpcyBaseImpl(String fpimpclass) {
		return map.get(fpimpclass);
	}
}
