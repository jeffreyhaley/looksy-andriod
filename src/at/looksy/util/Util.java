package at.looksy.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;
import at.looksy.dataitem.HoursDataItem;

public class Util {

	public static boolean isNull(Object o)
	{
		return o == null;
	}
	
	public static void constructHourMinuteStr(StringBuffer sb, int hours, int minutes)
	{
		if (hours == 0 && minutes == 0) {
			sb.append("0 minutes");
			return;
		}
		
		if (hours != 0) {
			sb.append(hours);
			sb.append(" hour");
			if (hours > 1)
				sb.append("s ");
			else
				sb.append(" ");
		}
		if (minutes != 0) {
			sb.append(minutes);
			sb.append(" minute");
			if (minutes != 1)
				sb.append("s");
		}
	}
	
	public static String mins2HoursMins(int mins)
	{
		StringBuffer sb = new StringBuffer();
		int hours = mins / 60;
		int minutes = mins - (hours * 60);
		constructHourMinuteStr(sb, hours, minutes);
		return sb.toString();
	}
	
		
	/* Credit: http://stackoverflow.com/questions/2785485/is-there-a-unique-android-device-id */
	public static String getUniqueDeviceId(Context context)	{
		
		final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

		final String tmDevice, tmSerial, androidId;
		tmDevice = "" + tm.getDeviceId();
		tmSerial = "" + tm.getSimSerialNumber();
		androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

		UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
		String deviceId = deviceUuid.toString();
		return deviceId;
	}
	
	public static int dp2px(Context context, int dp)
	{
		float density = context.getResources().getDisplayMetrics().density;
	    return Math.round((float)dp * density);
	}
	
	public static int px2dp(Context context, int px)
	{
		float density = context.getResources().getDisplayMetrics().density;
	    return Math.round((float)px / density);
	}
	
	public static int minuteDifference(Date date1, Date date2)
	{
		return (int)Math.abs(date1.getTime() - date2.getTime()) / (1000 * 60);
	}
	
	public static long secDifference(Date date1, Date date2) {
		return TimeUnit.MILLISECONDS.toSeconds(Math.abs(date1.getTime() - date2.getTime()));
	}
	public static long minDifference(Date date1, Date date2) {
		return TimeUnit.MILLISECONDS.toMinutes(Math.abs(date1.getTime() - date2.getTime()));
	}
	public static long hourDifference(Date date1, Date date2) {
		return TimeUnit.MILLISECONDS.toHours(Math.abs(date1.getTime() - date2.getTime()));
	}
	public static long dayDifference(Date date1, Date date2) {
		return TimeUnit.MILLISECONDS.toDays(Math.abs(date1.getTime() - date2.getTime()));
	}
	public static long weekDifference(Date date1, Date date2) {
		return dayDifference(date1, date2) / 7;
	}
	public static long monthDifference(Date date1, Date date2) {
		return dayDifference(date1, date2) / 30;
	}
	public static long yearDifference(Date date1, Date date2) {
		return dayDifference(date1, date2) / 365;
	}
	
	public static String humanTimeDifferenceMajor(Date date1, Date date2) {
		String str = null;
		boolean suffix = true;
		
		long secDiff = secDifference(date1, date2);
		long minDiff = minDifference(date1, date2);
		long hourDiff = hourDifference(date1, date2);
		long dayDiff = dayDifference(date1, date2);
		long weekDiff = weekDifference(date1, date2);
		long monthDiff = monthDifference(date1, date2);
		long yearDiff = yearDifference(date1, date2);
		
		if (secDiff < 60) {
			str = "Just Now";
			suffix = false;
		} else if (minDiff == 1) {
			str = minDiff + " min";
		} else if (minDiff < 60) {
			str = minDiff + " mins";
		} else if (hourDiff == 1) {
			str = hourDiff + " hour";
		} else if (hourDiff < 24) {
			str = hourDiff + " hours";
		} else if (dayDiff == 1) {
			str = dayDiff + " day";
		} else if (dayDiff < 7) {
			str = dayDiff + " days";
		} else if (weekDiff == 1) {
			str = weekDiff + " week";
		} else if (weekDiff < 4) {
			str = weekDiff + " weeks";
		} else if (weekDiff == 4) {
			str = "1 month";
		} else if (monthDiff == 1) {
			str = monthDiff + " month";
		} else if (monthDiff < 12) {
			str = monthDiff + " months";
		} else if (monthDiff == 12) {
			str = "1 year";
		} else if (yearDiff == 1) {
			str = yearDiff + " year";
		} else {
			str = yearDiff + " years";
		}
		
		if (suffix)
			str += " ago";
		
		return str;
	}
	
	public static String humanTimeDifferenceHours(Date date1, Date date2) {
		String str = "";

		long dateDiffMs = Math.abs(date1.getTime() - date2.getTime());
		
		long hours = TimeUnit.MILLISECONDS.toHours(dateDiffMs);
		if (hours != 0) {
			dateDiffMs -= TimeUnit.HOURS.toMillis(hours);
			str += hours + " hr";
			if (hours > 1)
				str += "s";
			str += " ";
		}
		long minutes = TimeUnit.MILLISECONDS.toMinutes(dateDiffMs);
		if (minutes != 0) {
			str += minutes + " min";
			if (minutes > 1) 
				str+= "s";
		} else {
			// only show the 0th minute if hours is also 0
			if (hours == 0) {
				str = "< 1 min";
			}
		}
		
		return str;
	}
	
	public static String fitString(String str, int maxChars)
	{
		if (str.length() <= maxChars)
			return str;
		
		String result = str.substring(0, maxChars-3);
		result += "...";	
		
		return result;
	}	
	
	public static String activityClassName(Activity activity)
	{
		return activity.getClass().getName();
	}
	
	public static String urlEncode(String decodedStr) {
		try {
			return URLEncoder.encode(decodedStr, "utf-8").replace("+", "%20");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String urlDecode(String encodedStr) {
		try {
			return URLDecoder.decode(encodedStr, "utf-8").replace("%20", " ");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Date mysqlDateStringToDate(String dateStr)
	{
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
			return sdf.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
        return null;
	}
	
	public static Date dateStringToDate(String dateStr, String tzOffset)
	{
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone(tzOffset));
        try {
			return sdf.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
        return null;
	}
	
	public static String[] getBusinessHoursString(List<HoursDataItem> hoursList, String locationTz) {
		
		String [] str = new String[2];
		int daysSkipped = 0;
		
		if (hoursList.size() != 7 || locationTz == null)
			return null;
		
		Calendar calendar = Calendar.getInstance();
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		int hoursListIndex = (dayOfWeek - 2) % 7;
		if (hoursListIndex < 0)
			hoursListIndex += 7; // modulo of a negative number is neg in java
		
		// find the next day in which we're open
		HoursDataItem todayHours;
		for (; ; hoursListIndex = (hoursListIndex + 1) % 7) {
			todayHours = hoursList.get(hoursListIndex);
			if (todayHours.getTimeClose() == null || 
					todayHours.getTimeOpen() == null) {
				daysSkipped++;
				continue;
			} else {
				break;
			}
		}
		
		// construct date object for open/close hours
		String yearStr = String.valueOf(calendar.get(Calendar.YEAR));
		int month = calendar.get(Calendar.MONTH) + 1;
		String monthStr = String.valueOf(month < 10 ? "0" + month : month);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		String dayStr = String.valueOf(day < 10 ? "0" + day : day); 
		String dateCloseStr = 
				yearStr + "-" + monthStr + "-" + dayStr + " " + todayHours.getTimeClose() + ":00";
		String dateOpenStr = 
				yearStr + "-" + monthStr + "-" + dayStr + " " + todayHours.getTimeOpen() + ":00";

		// move the close date up by number of days skipped
		Date dateClose = dateStringToDate(dateCloseStr, locationTz);
		Calendar calDateClose = Calendar.getInstance();
		calDateClose.setTime(dateClose);
		calDateClose.add(Calendar.DATE, daysSkipped);
		dateClose = calDateClose.getTime();

		// move the open date up by number of days skipped		
		Calendar calDateOpen = Calendar.getInstance();
		Date dateOpen = dateStringToDate(dateOpenStr, locationTz);
		calDateOpen.setTime(dateOpen);
		calDateOpen.add(Calendar.DATE, daysSkipped);
		dateOpen = calDateOpen.getTime();
		
		Date now = new Date();
		
		if (dateOpen.equals(now) || (now.after(dateOpen) && now.before(dateClose))) {
			// we're open!
			str[0] = "Closes in";
			str[1] = Util.humanTimeDifferenceHours(now, dateClose);
			
		} else if (now.before(dateOpen)) {
			// closed now but will open today
			str[0] = "Opens in";
			str[1] = Util.humanTimeDifferenceHours(now, dateOpen);
			
		} else if (now.after(dateClose)) {
			// closed for the day - when will it open again next time?
			daysSkipped = 0;
			for (int i = (hoursListIndex+1) % 7; daysSkipped < 7; i = (i + 1) % 7) {
				daysSkipped++;
				HoursDataItem nextTimeHours = hoursList.get(i);
				if (nextTimeHours.getTimeClose() == null || 
						nextTimeHours.getTimeOpen() == null) {
					continue;
					
				} else {
					// figure out when we're opening again, taking into account
					// the days we've skipped because we're closed
					String dateNextOpenStr =
							yearStr + "-" + monthStr + "-" + dayStr + " " + nextTimeHours.getTimeOpen() + ":00";
					Date dateNextOpen = dateStringToDate(dateNextOpenStr, locationTz);					
					
					Calendar cal = Calendar.getInstance();
					cal.setTime(dateNextOpen);
					cal.add(Calendar.DATE, daysSkipped);
					dateNextOpen = cal.getTime();
					
					str[0] = "Opens in";
					str[1] = Util.humanTimeDifferenceHours(now, dateNextOpen);
					break;
				}
			}
		}
		
		return str;
	}
	
	public static String standardTo12HourTime(String standardTime) {
		
		String ampmTime = null;
		
		if (standardTime == null)
			return null;
		
		String [] timeParts = standardTime.split(":");
		if (timeParts.length != 2)
			return null;
		
		String hoursStr = timeParts[0];
		String minutesStr = timeParts[1];
		
		int hours = Integer.valueOf(hoursStr);
		
		if (hours == 0) {
			ampmTime = "12:" + minutesStr + "am";
		} else if (hours == 12) {
			ampmTime = "12:" + minutesStr + "pm";
		} else if (hours > 12) {
			ampmTime = (hours - 12) + ":" + minutesStr + "pm";
		} else {
			ampmTime = standardTime + "am";
		}
			
		return ampmTime;
	}
	
}
