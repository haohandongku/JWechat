package com.dcits.app.dao;

import java.util.List;
import java.util.Map;

import com.dcits.app.sequence.SequenceFactory;
import com.dcits.app.util.ApplicationContextUtils;
import com.dcits.db.mybatis.dao.MybatisDao;

public class DataWindowBasic {

	private static MybatisDao mybatisDao = (MybatisDao) ApplicationContextUtils
			.getContext().getBean("mybatisDao");
	protected Object data;

	public String getSequence(String sequenceName) throws Exception {
		return SequenceFactory.getInstance().getSequence(sequenceName);
	}

	public void query(String key, Object parameter) {
		data = mybatisDao.list(key, parameter);
	}

	public void query(String key, Object parameter, int pageNumber, int pageSize) {
		data = mybatisDao.list(key, parameter, pageNumber, pageSize);
	}

	public int getTotal(String key, Object parameter) {
		return mybatisDao.count(key, parameter);
	}

	public Object queryOne(String key, Object parameter) {
		return mybatisDao.get(key, parameter);
	}

	public void insert(String key, Object parameter) {
		mybatisDao.insert(key, parameter);
	}

	public void update(String key, Object parameter) {
		mybatisDao.update(key, parameter);
	}

	public void delete(String key, Object parameter) {
		mybatisDao.delete(key, parameter);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Object> getList() {
		if (data instanceof List) {
			return (List) data;
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Object getItemAny(int row, String colName) {
		if (data instanceof List) {
			List list = (List) data;
			if (list.size() > 0) {
				Object object = list.get(row);
				if (object instanceof Map) {
					Map map = (Map) object;
					return map.get(colName);
				}
			}
		}
		return null;
	}

	public String getSql(String key) {
		return mybatisDao.getSql(key);
	}

}