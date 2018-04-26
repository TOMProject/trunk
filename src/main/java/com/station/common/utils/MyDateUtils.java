package com.station.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

public class MyDateUtils {
	private static String[] pattern = new String[] { "yy.MM.dd", "yyyy.MM.dd", "yyyy-MM", "yyyyMM", "yyyy/MM",
			"yyyyMMdd", "yyyy-MM-dd", "yyyy/MM/dd", "yyyyMMddHHmmss", "yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss" };

	public static Date parseDate(String dateStr) {
		try {
			if (dateStr == null || dateStr.trim().length() == 0) {
				return null;
			}
			return DateUtils.parseDate(dateStr, pattern);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getDateString(Date d) {
		if (d == null) {
			return null;
		}
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(d);
		return dateString;
	}

	public static String getDateString(Date d, String formatStr) {
		SimpleDateFormat formatter = new SimpleDateFormat(formatStr);
		String dateString = formatter.format(d);
		return dateString;
	}

	public static String diffDays(Date d1, Date d2) {
		if (d1 == null || d2 == null) {
			return "";
		}
		String result = "";
		long diffTime = Math.abs(d1.getTime() - d2.getTime()) / 1000;
		if (diffTime < 24 * 60 * 60) {
			result = diffTime / (24 * 60 * 60) + "天";
		} else {
			long diffDays = diffTime / (24 * 60 * 60);
			if (diffDays < 31) {
				result = diffDays + "天";
			} else {
				if (diffDays / 365 > 0) {
					result = diffDays / 365 + "年" + (diffDays % 365) / 30 + "月";
				} else {
					result = (diffDays % 365) / 30 + "月";
				}
			}
		}
		return result;
	}

	public static Date getDiffTime(long diffTime) {
		Date n = new Date();
		return new Date(n.getTime() + diffTime);
	}

	/**
	 * 获取指定日期当月第一天
	 * 
	 * @param d
	 * @return
	 */
	public static Date getFirstDay(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.set(Calendar.DAY_OF_MONTH, 1);
		return c.getTime();
	}

	/**
	 * 得到给定时间相差 diffMonth 个月的1号0点0分0秒
	 * 
	 * @param currentDate
	 * @param diffMonth
	 * @return
	 */
	public static Date getFirstDayDiffMonth(Date d, int diffMonth) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.MONTH, diffMonth);
		c.set(Calendar.DAY_OF_MONTH, 1);
		// 将小时至0
		c.set(Calendar.HOUR_OF_DAY, 0);
		// 将分钟至0
		c.set(Calendar.MINUTE, 0);
		// 将秒至0
		c.set(Calendar.SECOND, 0);
		// 将毫秒至0
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	public static Date add(Date d, int field, int amount) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(d);
		calendar.add(field, amount);
		return calendar.getTime();
	}

	public static int diffMonths(Date startDate, Date endDate) {
		Calendar startCalendar = Calendar.getInstance();
		startCalendar.setTime(startDate);

		Calendar endCalendar = Calendar.getInstance();
		endCalendar.setTime(endDate);

		int diffYear = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
		int diffMonth = diffYear * 12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);

		return diffMonth;
	}

	public static long diffTime(Date startDate, Date endDate, int field) {
		long nd = 1000 * 24 * 60 * 60;
		long nh = 1000 * 60 * 60;
		long nm = 1000 * 60;
		// long ns = 1000;
		// 获得两个时间的毫秒时间差异
		long diff = endDate.getTime() - startDate.getTime();
		// 计算差多少天
		long day = diff / nd;
		// 计算差多少小时
		long hour = diff % nd / nh;
		// 计算差多少分钟
		long min = diff % nd % nh / nm;
		switch (field) {
		case Calendar.DATE:
			return day;
		case Calendar.HOUR:
			return hour;
		case Calendar.MINUTE:
			return min;
		}
		return 0;

	}

}
