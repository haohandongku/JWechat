package com.dcits.app.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MathUtils {

	/**
	 * 求指定数据的合计
	 * 
	 * @param cols
	 * @return
	 */
	public static BigDecimal sum(BigDecimal[] cols) {
		BigDecimal value = new BigDecimal(0.00);
		for (BigDecimal col : cols) {
			if (col != null) {
				if (value == null) {
					value = col;
				} else {
					value = value.add(col);
				}
			}
		}
		if (value != null) {
			value = value.setScale(2, BigDecimal.ROUND_HALF_UP);
		}
		return value;
	}

	/**
	 * 获得指定数据项的合计数
	 * 
	 * @param data
	 * @param addcols
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static BigDecimal sum(Map data, String addcols) {
		BigDecimal value = new BigDecimal(0.00);
		String[] cols = addcols.split(",");
		for (String col : cols) {
			if (data.get(col) != null) {
				if (value == null) {
					value = (BigDecimal) data.get(col);
				} else {
					value = value.add((BigDecimal) data.get(col));
				}
			}
		}
		if (value != null) {
			value = value.setScale(2, BigDecimal.ROUND_HALF_UP);
		}
		return value;
	}

	/**
	 * 获得指定数据项的合计数
	 * 
	 * @param data
	 * @param addcols
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static BigDecimal sum(Map data, String addcols, int Scale) {
		BigDecimal value = new BigDecimal(0.00);
		String[] cols = addcols.split(",");
		for (String col : cols) {
			if (data.get(col) != null) {
				if (value == null) {
					value = (BigDecimal) data.get(col);
				} else {
					value = value.add((BigDecimal) data.get(col));
				}
			}
		}
		if (value != null) {
			value = value.setScale(Scale, BigDecimal.ROUND_HALF_UP);
		}
		return value;
	}

	/**
	 * 对指定的数据进行加减操作
	 * 
	 * @param data
	 * @param addcols
	 * @param delcols
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static BigDecimal sum(Map data, String addcols, String delcols) {
		BigDecimal addvalue = MathUtils.sum(data, addcols);
		BigDecimal delvalue = MathUtils.sum(data, delcols);
		BigDecimal value = new BigDecimal(0.00);
		if (addvalue != null && delvalue != null) {
			value = addvalue.subtract(delvalue);
		} else if (addvalue == null && delvalue != null) {
			value = BigDecimal.ZERO.subtract(delvalue);
		} else if (addvalue != null && delvalue == null) {
			value = addvalue;
		} else {
			// 为空
		}
		if (value != null) {
			value = value.setScale(2, BigDecimal.ROUND_HALF_UP);
		}
		return value;
	}

	/**
	 * 对list对象中指定条件的列求和
	 * 
	 * @param datas
	 * @param sumcol
	 * @param filtercol
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static BigDecimal sum(List datas, String sumcol, String filtercol,
			String filter) {
		String[] items = filter.split(",");
		Map sumMap = new HashMap();
		for (String item : items) {
			Map iteml = MapUtils.filter(datas, filtercol, item);
			sumMap.put(item, iteml.get(sumcol));
		}
		return MathUtils.sum(sumMap, filter);
	}

	/**
	 * 对list对象中指定条件的列求和
	 * 
	 * @param datas
	 * @param sumcol
	 * @param filtercol
	 * @return
	 */
	@SuppressWarnings({ "rawtypes" })
	public static BigDecimal sum(List datas, String sumcol) {
		BigDecimal[] cols = new BigDecimal[datas.size()];
		for (int i = 0; i < datas.size(); i++) {
			Map iteml = (Map) datas.get(i);
			cols[i] = (BigDecimal) iteml.get(sumcol);
		}
		return MathUtils.sum(cols);
	}

	/**
	 * 乘法
	 * 
	 * @param cols
	 * @return
	 */
	public static BigDecimal multiply(BigDecimal[] cols) {
		BigDecimal value = null;
		for (BigDecimal col : cols) {
			if (col == null) {
				col = new BigDecimal(0.00);
			}
			if (value == null) {
				value = col;
			} else {
				value = value.multiply(col);
			}

		}
		if (value != null) {
			value = value.setScale(2, BigDecimal.ROUND_HALF_UP);
		} else {
			value = new BigDecimal(0.00);
		}
		return value;
	}

	/**
	 * 乘法
	 * 
	 * @param cols
	 * @return
	 */
	public static BigDecimal multiply(BigDecimal[] cols, int Scale) {
		BigDecimal value = null;
		for (BigDecimal col : cols) {
			if (col != null) {
				if (value == null) {
					value = col;
				} else {
					value = value.multiply(col);
				}
			}
		}
		if (value != null) {
			value = value.setScale(Scale, BigDecimal.ROUND_HALF_UP);
		} else {
			value = new BigDecimal(0.00);
		}
		return value;
	}

	/**
	 * 乘法
	 * 
	 * @param cols
	 * @return
	 */
	public static BigDecimal divide(BigDecimal dividend, BigDecimal divisor) {
		BigDecimal value = null;
		if (divisor != null) {
			if (dividend != null) {
				if (divisor.compareTo(BigDecimal.ZERO) == 0) {
					return new BigDecimal(0.0000);
				}
				value = dividend.divide(divisor, 4, BigDecimal.ROUND_HALF_EVEN);
			}
		}

		if (value != null) {
			value = value.setScale(4, BigDecimal.ROUND_HALF_UP);
		} else {
			value = new BigDecimal(0.0000);
		}
		return value;
	}

	/**
	 * 条件判断，如果x>=0 返回y 否则为z；如果x为空，返回null
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static BigDecimal gtOrEqZero(BigDecimal x, BigDecimal y, BigDecimal z) {
		if (x != null) {
			if (x.compareTo(BigDecimal.ZERO) >= 0) {
				return y;
			} else {
				return z;
			}
		}
		return null;
	}

	/**
	 * 条件判断，如果x>=0 返回y 否则为z；如果x为空，返回null
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static BigDecimal gtZero(BigDecimal x, BigDecimal y, BigDecimal z) {
		if (x != null) {
			if (x.compareTo(BigDecimal.ZERO) > 0) {
				return y;
			} else {
				return z;
			}
		}
		return null;
	}

	/**
	 * 条件判断，如果x<0 返回y 否则为z；如果x为空，返回null
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static BigDecimal ltZero(BigDecimal x, BigDecimal y, BigDecimal z) {
		if (x != null) {
			if (x.compareTo(BigDecimal.ZERO) < 0) {
				return y;
			} else {
				return z;
			}
		}
		return null;
	}

	/**
	 * 条件判断，如果x<0 返回y 否则为z；如果x为空，返回null
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static BigDecimal ltOrEqZero(BigDecimal x, BigDecimal y, BigDecimal z) {
		if (x != null) {
			if (x.compareTo(BigDecimal.ZERO) <= 0) {
				return y;
			} else {
				return z;
			}
		}
		return null;
	}

	/**
	 * 求绝对值 如果x为null返回null，否则返回x的绝对值
	 * 
	 * @param x
	 * @return
	 */
	public static BigDecimal abs(BigDecimal x) {
		if (x != null) {
			return x.abs();
		}
		return null;
	}

	/**
	 * 求最大数 如果x为null返回null，否则返回x中的最大值
	 * 
	 * @param x
	 * @return
	 */
	public static BigDecimal max(BigDecimal[] x) {
		BigDecimal value = null;
		for (BigDecimal i : x) {
			if (i != null) {
				if (value == null) {
					value = i;
				} else {
					if (value.compareTo(i) < 0) {
						value = i;
					}
				}
			}
		}
		if (value != null) {
			value = value.setScale(2, BigDecimal.ROUND_HALF_UP);
		}
		return value;
	}

	/**
	 * 求最小数 如果x为null返回null，否则返回x中的最小值
	 * 
	 * @param x
	 * @return
	 */
	public static BigDecimal min(BigDecimal[] x) {
		BigDecimal value = null;
		for (BigDecimal i : x) {
			if (i != null) {
				if (value == null) {
					value = i;
				} else {
					if (value.compareTo(i) > 0) {
						value = i;
					}
				}
			}
		}
		if (value != null) {
			value = value.setScale(2, BigDecimal.ROUND_HALF_UP);
		}
		return value;
	}

	/**
	 * 金额按千位逗号分割
	 */
	public static String separateMoneyByComma(BigDecimal money, Integer accuracy) {
		if(money ==null){
			return "0.00";
		}
		int n = 2;
		if (accuracy != null) {
			n = accuracy;
		}
		StringBuffer sb = new StringBuffer();
		String prefix = "#,###";		
		if(money.compareTo(BigDecimal.ZERO)==0){
			prefix = "0";
		}
		sb.append(prefix);		
		if (n > 0) {
			sb.append(".");
		}
		for (int i = 0; i < n; i++) {
			sb.append("0");
		}
		DecimalFormat decimalFormat = new DecimalFormat(sb.toString());
		return decimalFormat.format(money);
	}

	/**
	 * 将按千位逗号分割的数字字符串转换为浮点数
	 */
	public static BigDecimal unSeparateMoneyByComma(String val) {
		return new BigDecimal(val.replaceAll(",", ""));
	}

	/**
	 * 将浮点数转换为百分比
	 */
	public static String numberToPercent(BigDecimal number, Integer accuracy) {
		if(number ==null){
			return "0%";
		}
		int n = 0;
		if (accuracy != null) {
			n = accuracy;
		}
		StringBuffer sb = new StringBuffer();
		sb.append("#");
		if (n > 0) {
			sb.append(".");
		}
		for (int i = 0; i < n; i++) {
			sb.append("0");
		}
		sb.append("%");
		DecimalFormat decimalFormat = new DecimalFormat(sb.toString());
		return decimalFormat.format(number);
	}

}