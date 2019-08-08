package at.looksy.manager;

import android.content.Context;
import at.looksy.util.Util;


public class DeviceManager {
	
	private static String deviceId = null;
	private static boolean isInitialized = false;

	public static void initialize(Context context) {
		if (deviceId == null) {
			deviceId = Util.getUniqueDeviceId(context);
		}
		isInitialized = true;
	}
	
	public static String getDeviceId() {
		return deviceId;
	}
	
	public static boolean isInitialized() {
		return isInitialized;
	}
}
