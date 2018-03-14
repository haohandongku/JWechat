package com.dcits.ocr.commons.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtils {
	/**
	 * 
	 * 获取开票日期是否在一年内
	 * 
	 * @return
	 */
	public static Boolean getIsBetweenOneYear(String kprq) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String str = sdf.format(date);
		boolean isOneYear = true;
		try {
			Date kprqDate = sdf.parse(kprq);
			Date nowDate = sdf.parse(str);
			Date lastDate = sdf.parse(getNowOfLastYear());
			if (kprqDate.after(lastDate) && kprqDate.before(nowDate)) {
				isOneYear = true;
			} else {
				isOneYear = false;
			}
		} catch (ParseException e) {
		}
		return isOneYear;
	}

	/**
	 * 获取当前月份
	 * 
	 * @return
	 */
	public static String getYearMonth() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
		String str = sdf.format(date);
		return str;
	}

	/**
	 * 获取当期日期
	 * 
	 * @return
	 */
	public static String getToday() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String str = sdf.format(date);
		return str;
	}

	/**
	 * 获取当期时间
	 * 
	 * @return
	 */
	public static String getCurrectTime() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String str = sdf.format(date);
		return str;
	}

	/**
	 * 获取去年的今天
	 * 
	 * @return
	 */
	public static String getNowOfLastYear() {
		SimpleDateFormat aSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		GregorianCalendar aGregorianCalendar = new GregorianCalendar();
		aGregorianCalendar.set(Calendar.YEAR,
				aGregorianCalendar.get(Calendar.YEAR) - 1);
		String currentYearAndMonth = aSimpleDateFormat
				.format(aGregorianCalendar.getTime());
		return currentYearAndMonth;
	}

	// 上个月的现在
	public static String getNowOfLastMonth() {
		SimpleDateFormat aSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		GregorianCalendar aGregorianCalendar = new GregorianCalendar();
		aGregorianCalendar.set(Calendar.MONTH,
				aGregorianCalendar.get(Calendar.MONTH) - 1);
		String nowOfLastMonth = aSimpleDateFormat.format(aGregorianCalendar
				.getTime());
		return nowOfLastMonth;
	}

	// 判断当前时间是否在某个时间段里
	public static boolean isInTime(String startTime, String endTime) throws ParseException {
		boolean isInTime = true;
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		long now = sdf.parse(sdf.format(new Date())).getTime();
		long start = sdf.parse(startTime).getTime();
		if (endTime.equals("00:00")) {
			 endTime = "24:00";
	    }
		long end = sdf.parse(endTime).getTime();
		if (end < start) {
            if (now >= end && now < start) {
            	isInTime= false;
            } else {
            	isInTime= true;
            }
        }else {
            if (now >= start && now < end) {
            	isInTime= true;
            } else {
            	isInTime= false;
            }
        } 
		return isInTime;
	}
}
