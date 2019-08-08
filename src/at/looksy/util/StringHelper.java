package at.looksy.util;

public class StringHelper {
	
	
	
	public static String prettyUrl(String url)
	{
		return url.replaceFirst("http://", "").replaceFirst("https://", "").replaceFirst("www.", "");
	}
	
	public static String prettyPhoneNumberUS(String phoneNumber)
	{
		return String.format("(%s) %s-%s", phoneNumber.substring(0, 3), phoneNumber.substring(3, 6), 
				phoneNumber.substring(6, 10));
	}

}
