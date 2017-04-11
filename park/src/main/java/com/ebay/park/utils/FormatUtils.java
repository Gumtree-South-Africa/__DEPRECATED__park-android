package com.ebay.park.utils;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.text.*;
import java.util.*;

/**
 * Util class for formatting data.
 * 
 * @author federico.perez
 * 
 */
@SuppressLint("SimpleDateFormat")
public class FormatUtils {

	/**
	 * Format price to show to user.
	 * 
	 * @param price
	 *            Number to format.
	 * @return A string formatted as price.
	 */
	public static String formatPrice(Number price) {
		NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
		return currencyFormatter.format(price).replaceAll("\\.00", "");
	}

	public static String formatPriceWithoutCurrency(Number price) {
		String strPrice = String.valueOf(price);
		if (strPrice.endsWith(".0")) {
			strPrice = strPrice.replace(".0", "");
		}
		return strPrice;
	}

	public static String formatDoubleTwoDecimals(Double value) {
		DecimalFormat df = new DecimalFormat("#.##");
		return df.format(value);
	}

	public static String timeAgo(String date) {
		if (!TextUtils.isEmpty(date)) {
			try {
				Calendar aMinuteAfter = Calendar.getInstance();
				aMinuteAfter.add(Calendar.MINUTE, -1);
				Calendar calendar = iso8601ToCalendar(date);
				if (!calendar.after(aMinuteAfter)) {
					Configuration configuration = new Configuration(Resources.getSystem().getConfiguration());
					configuration.locale = new Locale("es", "ES");
					Resources.getSystem().updateConfiguration(configuration, null);
					String finalDate = DateUtils.getRelativeTimeSpanString(calendar.getTimeInMillis()).toString();
					if (finalDate.equals("Yesterday")) {
						finalDate = "Ayer";
					}

					return finalDate;
				}
			} catch (ParseException e) {
				Logger.error(e.getMessage());
			}
		}
		return ParkApplication.getInstance().getString(R.string.time_now);
	}

	public static String timeAgo(long date) {
		Calendar aMinuteAfter = Calendar.getInstance();
		aMinuteAfter.add(Calendar.MINUTE, -1);
		if ((date * 1000) > aMinuteAfter.getTimeInMillis()) {
			return ParkApplication.getInstance().getString(R.string.time_now);
		} else {
			Configuration configuration = new Configuration(Resources.getSystem().getConfiguration());
			configuration.locale = new Locale("es", "ES");
			Resources.getSystem().updateConfiguration(configuration, null);

			String finalDate = DateUtils.getRelativeTimeSpanString(date * 1000).toString();
			if (finalDate.equals("Yesterday")) {
				finalDate = "Ayer";
			}
			if (finalDate.contains(",")){ // i.e.: Apr 20, 2016
				finalDate = dateBarsFormat(date);
			}

			return finalDate;
		}
	}

	public static String dateBarsFormat(long longDate){
		Date date = new Date(longDate * 1000);
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		return dateFormat.format(date);
	}

	public static String formatAsISO8601(final Date date) {
		return DateFormatUtils.ISO_DATE_TIME_ZONE_FORMAT.format(date);
	}

	public static Calendar iso8601ToCalendar(final String iso8601string) throws ParseException {
		Calendar calendar = GregorianCalendar.getInstance();
		String s = iso8601string.replace("Z", "+00:00");
		try {
			s = s.substring(0, 22) + s.substring(23);
		} catch (IndexOutOfBoundsException e) {
			throw new ParseException("Invalid length", 0);
		}
		Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(s);
		calendar.setTime(date);
		return calendar;
	}
}
