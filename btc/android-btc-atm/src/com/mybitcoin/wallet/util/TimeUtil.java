package com.mybitcoin.wallet.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TimeUtil {
	public static String getDate() {
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
				Locale.CHINA);
		long now = System.currentTimeMillis();
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(now);
		return formatter.format(calendar.getTime());
	}

	public static double getCurrentSeconds() {
		return System.currentTimeMillis() * 1.0 / 1000.0;
	}
	
	public static long getTimeInMillis() {
		return System.currentTimeMillis();
	}

}
