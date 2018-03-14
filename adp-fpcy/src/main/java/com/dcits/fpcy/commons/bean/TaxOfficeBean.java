package com.dcits.fpcy.commons.bean;

import java.io.Serializable;

/**
 * 税局实体兼容老版本
 * @author wuche
 *
 */
public class TaxOfficeBean implements Serializable {
	public static final long serialVersionUID = 1L;
	public String swjg_dm ; // 税务机关代码
	public String fp_zl;
	public String swjg_mc ; //税务机关名称
	public String yzm_bz;//验证码备注
	public String yzm_lx; // 验证码类型
	public String yzm_dz ; // 验证码地址
	public String yzm_qqfs; // 验证码请求方式
	public String yzm_tplx; // 图片类型
	public String yzm_zdcd; // 验证码最大长度
	public String yzm_zxcd ; // 验证码最小长度
	public String cy_dz; // 验真地址
	public String cy_yzmcs; // 01 联众 02 UU 默认联众
	public String cy_qqfs; // 查验请求方式
	public String JS; // 查验请求方式
	public String fpimpclass ;
	public String datamodel;//渲染前台界面模板
	public int poolid ;
	public String cyym ;
	//0 不要验证码		1 需要将cookie信息添加到请求头里	2 需要将cookie信息添加到URL中
	public int cookie ;
	public int yzm_lx_uu; // UU对应的验证码类型
	public String isopenservice ;
	public String isGenAdmin ;
	//是否升级
	public String isCoreTransformation ;
	public String getSwjg_dm() {
		return swjg_dm;
	}
	public void setSwjg_dm(String swjg_dm) {
		this.swjg_dm = swjg_dm;
	}
	public String getFp_zl() {
		return fp_zl;
	}
	public void setFp_zl(String fp_zl) {
		this.fp_zl = fp_zl;
	}
	public String getSwjg_mc() {
		return swjg_mc;
	}
	public void setSwjg_mc(String swjg_mc) {
		this.swjg_mc = swjg_mc;
	}
	public String getYzm_bz() {
		return yzm_bz;
	}
	public void setYzm_bz(String yzm_bz) {
		this.yzm_bz = yzm_bz;
	}
	public String getYzm_lx() {
		return yzm_lx;
	}
	public void setYzm_lx(String yzm_lx) {
		this.yzm_lx = yzm_lx;
	}
	public String getYzm_dz() {
		return yzm_dz;
	}
	public void setYzm_dz(String yzm_dz) {
		this.yzm_dz = yzm_dz;
	}
	public String getYzm_qqfs() {
		return yzm_qqfs;
	}
	public void setYzm_qqfs(String yzm_qqfs) {
		this.yzm_qqfs = yzm_qqfs;
	}
	public String getYzm_tplx() {
		return yzm_tplx;
	}
	public void setYzm_tplx(String yzm_tplx) {
		this.yzm_tplx = yzm_tplx;
	}
	public String getYzm_zdcd() {
		return yzm_zdcd;
	}
	public void setYzm_zdcd(String yzm_zdcd) {
		this.yzm_zdcd = yzm_zdcd;
	}
	public String getYzm_zxcd() {
		return yzm_zxcd;
	}
	public void setYzm_zxcd(String yzm_zxcd) {
		this.yzm_zxcd = yzm_zxcd;
	}
	public String getCy_dz() {
		return cy_dz;
	}
	public void setCy_dz(String cy_dz) {
		this.cy_dz = cy_dz;
	}
	public String getCy_yzmcs() {
		return cy_yzmcs;
	}
	public void setCy_yzmcs(String cy_yzmcs) {
		this.cy_yzmcs = cy_yzmcs;
	}
	public String getCy_qqfs() {
		return cy_qqfs;
	}
	public void setCy_qqfs(String cy_qqfs) {
		this.cy_qqfs = cy_qqfs;
	}
	public String getJS() {
		return JS;
	}
	public void setJS(String jS) {
		JS = jS;
	}
	public String getFpimpclass() {
		return fpimpclass;
	}
	public void setFpimpclass(String fpimpclass) {
		this.fpimpclass = fpimpclass;
	}
	public String getDatamodel() {
		return datamodel;
	}
	public void setDatamodel(String datamodel) {
		this.datamodel = datamodel;
	}
	public int getPoolid() {
		return poolid;
	}
	public void setPoolid(int poolid) {
		this.poolid = poolid;
	}
	public String getCyym() {
		return cyym;
	}
	public void setCyym(String cyym) {
		this.cyym = cyym;
	}
	public int getCookie() {
		return cookie;
	}
	public void setCookie(int cookie) {
		this.cookie = cookie;
	}
	public int getYzm_lx_uu() {
		return yzm_lx_uu;
	}
	public void setYzm_lx_uu(int yzm_lx_uu) {
		this.yzm_lx_uu = yzm_lx_uu;
	}
	public String getIsopenservice() {
		return isopenservice;
	}
	public void setIsopenservice(String isopenservice) {
		this.isopenservice = isopenservice;
	}
	
	
	public String getIsCoreTransformation() {
		return isCoreTransformation;
	}
	public void setIsCoreTransformation(String isCoreTransformation) {
		this.isCoreTransformation = isCoreTransformation;
	}
	public String getIsGenAdmin() {
		return isGenAdmin;
	}
	public void setIsGenAdmin(String isGenAdmin) {
		this.isGenAdmin = isGenAdmin;
	}
	@Override
	public String toString() {
		return "TaxOfficeBean [swjg_dm=" + swjg_dm + ", fp_zl=" + fp_zl
				+ ", swjg_mc=" + swjg_mc + ", yzm_bz=" + yzm_bz + ", yzm_lx="
				+ yzm_lx + ", yzm_dz=" + yzm_dz + ", yzm_qqfs=" + yzm_qqfs
				+ ", yzm_tplx=" + yzm_tplx + ", yzm_zdcd=" + yzm_zdcd
				+ ", yzm_zxcd=" + yzm_zxcd + ", cy_dz=" + cy_dz + ", cy_yzmcs="
				+ cy_yzmcs + ", cy_qqfs=" + cy_qqfs + ", JS=" + JS
				+ ", fpimpclass=" + fpimpclass + ", datamodel=" + datamodel
				+ ", poolid=" + poolid + ", cyym=" + cyym + ", cookie="
				+ cookie + ", yzm_lx_uu=" + yzm_lx_uu + ", isopenservice="
				+ isopenservice + ", isCoreTransformation="
						+ isCoreTransformation + ", isGenAdmin=" + isGenAdmin + "]";
	}
}
