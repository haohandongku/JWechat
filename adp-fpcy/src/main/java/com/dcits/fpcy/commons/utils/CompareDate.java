package com.dcits.fpcy.commons.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CompareDate {
	public static boolean compare(String date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date kp = sdf.parse(date);
			Calendar ca = Calendar.getInstance();
			ca.setTime(kp);
			Calendar now = Calendar.getInstance();
			if (now.get(Calendar.YEAR) == ca.get(Calendar.YEAR) && now.get(Calendar.MONTH) == ca.get(Calendar.MONTH)) {
				return true;
			} else {
				return false;
			}
		} catch (ParseException e) {
			return false;
		}
		
	}
}
