package com.dcits.app.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dcits.app.constant.Constant;
import com.dcits.app.constant.DmSequenceName;
import com.dcits.app.constant.DmSflx;
import com.dcits.app.constant.DmYhlx;
import com.dcits.app.dao.DataWindow;
import com.dcits.app.data.DataObject;
import com.dcits.app.exception.BizRuntimeException;
import com.dcits.app.security.UserDetails;
import com.dcits.app.util.ApplicationContextUtils;

public abstract class BaseService {

	protected static final Log LOG = LogFactory.getLog(BaseService.class);
	private CommonService commonService = (CommonService) ApplicationContextUtils
			.getContext().getBean("app.service.commonService");

	@SuppressWarnings("rawtypes")
	protected int getPageNumber(Map parameter) {
		Object pageNumber = parameter.get(Constant.PAGE_NUMBER);
		return (pageNumber == null) ? Constant.DEFAULT_PAGE_NUMBER : Integer
				.valueOf(pageNumber.toString());
	}

	@SuppressWarnings("rawtypes")
	protected int getPageSize(Map parameter) {
		Object pageSize = parameter.get(Constant.PAGE_SIZE);
		return (pageSize == null) ? Constant.DEFAULT_PAGE_SIZE : Integer
				.valueOf(pageSize.toString());
	}

	@SuppressWarnings("rawtypes")
	protected String getValidCode(Map parameter) {
		return (String) parameter.get((String) parameter
				.get(Constant.SESSION_VALIDCODE_KEY));
	}

	@SuppressWarnings("rawtypes")
	protected HttpServletRequest getHttpServletRequest(Map parameter) {
		return (HttpServletRequest) parameter
				.get(Constant.HTTP_SERVLET_REQUEST);
	}

	@SuppressWarnings("rawtypes")
	protected HttpServletResponse getHttpServletResponse(Map parameter) {
		return (HttpServletResponse) parameter
				.get(Constant.HTTP_SERVLET_RESPONSE);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected List<FileItem> getFileList(Map parameter) {
		return (List<FileItem>) parameter.get(Constant.FILELIST);
	}

	protected String getSequence() {
		return getSequence(DmSequenceName.XH);
	}

	protected String getSequence(String sequenceName) {
		try {
			return DataWindow.getSequence(sequenceName);
		} catch (Exception e) {
			throw new BizRuntimeException("获取序号时出现异常", e);
		}
	}

	protected DataObject getQyjgRoot(DataObject dataObject) {
		return commonService.getQyjgRoot(dataObject);
	}

	protected DataObject getQyjgTree(DataObject dataObject) {
		return commonService.getQyjgTree(dataObject);
	}

	protected DataObject getQyjgTreeSync(DataObject dataObject) {
		return commonService.getQyjgTreeSync(dataObject);
	}

	protected DataObject getQyryTreeSync(DataObject dataObject) {
		return commonService.getQyryTreeSync(dataObject);
	}

	protected DataObject getSxjgRoot(DataObject dataObject) {
		return commonService.getSxjgRoot(dataObject);
	}

	protected DataObject getSxjgTree(DataObject dataObject) {
		return commonService.getSxjgTree(dataObject);
	}

	protected DataObject getSxjgTreeSync(DataObject dataObject) {
		return commonService.getSxjgTreeSync(dataObject);
	}

	protected DataObject initCombobox(DataObject dataObject, String sqlKey) {
		return commonService.initCombobox(dataObject, sqlKey);
	}

	protected DataObject initComboTree(DataObject dataObject, String sqlKey) {
		return commonService.initComboTree(dataObject, sqlKey);
	}

	protected DataObject initComboTreeSync(DataObject dataObject, String sqlKey) {
		return commonService.initComboTreeSync(dataObject, sqlKey);
	}

	protected DataObject initTree(DataObject dataObject, String sqlKey) {
		return commonService.initTree(dataObject, sqlKey);
	}

	protected DataObject initTreeSync(DataObject dataObject, String sqlKey) {
		return commonService.initTreeSync(dataObject, sqlKey);
	}

	protected UserDetails getUser() {
		return commonService.getUser();
	}

	protected String getUserInfoByKey(String key) {
		UserDetails user = getUser();
		return (user != null) ? (String) user.getOther().get(key) : null;
	}

	/**
	 * 获取当前用户的用户名
	 * 
	 * @return
	 */
	protected String getCurrentYhm() {
		return getUserInfoByKey("YHM");
	}

	/**
	 * 获取当前用户的用户编码
	 * 
	 * @return
	 */
	protected String getCurrentUserbm() {
		return getUserInfoByKey("USERBM");
	}

	/**
	 * 获取当前用户的名称
	 * 
	 * @return
	 */
	protected String getCurrentUserMc() {
		return getUserInfoByKey("MC");
	}

	/**
	 * 获取当前用户的用户类型代码
	 * 
	 * @return
	 */
	protected String getCurrentYhlxDm() {
		return getUserInfoByKey("YHLX_DM");
	}

	/**
	 * 获取当前用户的身份类型代码
	 * 
	 * @return
	 */
	protected String getCurrentSflxDm() {
		return getUserInfoByKey("SFLX_DM");
	}

	/**
	 * 获取当前用户的企业编码，税协用户则为企业编码
	 * 
	 * @return
	 */
	protected String getCurrentQybm() {
		return getUserInfoByKey("QYBM");
	}

	/**
	 * 获取当前用户的企业名称，税协用户则为机构名称
	 * 
	 * @return
	 */
	protected String getCurrentQyMc() {
		return getUserInfoByKey("QYMC");
	}

	/**
	 * 获取当前用户的税协会员号
	 * 
	 * @return
	 */
	protected String getCurrentSxhyh() {
		return getUserInfoByKey("SXHYH");
	}

	/**
	 * 获取当前用户所在的事务所的会员注册号（团体会员注册号）
	 * 
	 * @return
	 */
	protected String getCurrentHyzch() {
		return getUserInfoByKey("HYZCH");
	}

	/**
	 * 当前用户是否中税协用户
	 * 
	 * @return
	 */
	protected boolean isZsxYh() {
		String yhlxDm = this.getCurrentYhlxDm();
		if (DmYhlx.SXYH.equals(yhlxDm)) {
			String qybm = this.getCurrentQybm();
			if (Constant.ZSX_JGBM.equals(qybm)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 当前用户是否地方税协用户
	 * 
	 * @return
	 */
	protected boolean isDfsxYh() {
		String yhlxDm = this.getCurrentYhlxDm();
		if (DmYhlx.SXYH.equals(yhlxDm)) {
			String qybm = this.getCurrentQybm();
			if (!Constant.ZSX_JGBM.equals(qybm)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 当前用户是否企业用户
	 * 
	 * @return
	 */
	protected boolean isQyYh() {
		String yhlxDm = this.getCurrentYhlxDm();
		return DmYhlx.QYYH.equals(yhlxDm) ? true : false;
	}

	/**
	 * 当前用户是否事务所
	 * 
	 * @return
	 */
	protected boolean isSws() {
		String sflxDm = this.getCurrentSflxDm();
		return DmSflx.SWS.equals(sflxDm) ? true : false;
	}

	/**
	 * 当前用户是否注册税务师
	 * 
	 * @return
	 */
	protected boolean isZcsws() {
		String sflxDm = this.getCurrentSflxDm();
		return DmSflx.ZCSWS.equals(sflxDm) ? true : false;
	}

	/**
	 * 当前用户是否其他从业人员
	 * 
	 * @return
	 */
	protected boolean isQtcyry() {
		String sflxDm = this.getCurrentSflxDm();
		return DmSflx.QTCYRY.equals(sflxDm) ? true : false;
	}

	/**
	 * 当前用户是否系统用户
	 * 
	 * @return
	 */
	protected boolean isXtYh() {
		String yhlxDm = this.getCurrentYhlxDm();
		return DmYhlx.XTYH.equals(yhlxDm) ? true : false;
	}

}