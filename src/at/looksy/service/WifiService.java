package at.looksy.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import at.looksy.BuildConfig;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import at.looksy.activity.IWifiReceiverActivity;
import at.looksy.core.Constants;
import at.looksy.service.consumer.IWifiDataConsumer;
import at.looksy.service.data.WifiAccessPointData;
import at.looksy.service.data.WifiAccessPointData.SignalStrength;
import at.looksy.util.Util;


public class WifiService {
	
	private WifiScanReciever wifiScanReceiver = null;
	private WifiManager wifiManager = null;
	private IWifiDataConsumer wifiReceiverService = null;
	
	private static WifiService instance = null;
	
	private Map<Activity,Activity> wifiReceiverActivityMap = null;
	private Map<String,WifiAccessPointData> accessPointData = null;
	
	private Context context = null;
	
	private WifiService()	{
		if (accessPointData == null)
			accessPointData = new HashMap<String, WifiAccessPointData>();
		if (wifiReceiverActivityMap == null)
			wifiReceiverActivityMap = new HashMap<Activity,Activity>();
		if (wifiScanReceiver == null)
			wifiScanReceiver = new WifiScanReciever();
	}
	
	public static WifiService getInstance() {
		if (instance == null)
			instance = new WifiService();
		return instance;
	}
	
	private class WifiScanReciever extends BroadcastReceiver {

		// TODO:
//		private boolean wasAdded = false;
		
		@Override
		public void onReceive(Context arg0, Intent arg1) {
	
			// grab latest results
			List<ScanResult> scanResults = wifiManager.getScanResults();
			
			// update existing/known access point info
			// this is needed in case the receiver activity exits and forces
			// us to get it data for the last LOCATION_TILE_TIME_TO_LIVE
			// minutes
			Iterator<Map.Entry<String, WifiAccessPointData>> dataIterator = 
					accessPointData.entrySet().iterator();
			while (dataIterator.hasNext()) {
				Map.Entry<String, WifiAccessPointData> entry = dataIterator.next();
				WifiAccessPointData data = entry.getValue();
				
				// quit looking at it if the location tile has died off
				if (Util.minuteDifference(new Date(), data.getDateLastSeen()) 
						>= Constants.LOCATION_TILE_TIME_TO_LIVE)
				{
					dataIterator.remove();
					continue;
					
				} else {
					data.setSignalStrength(SignalStrength.OUT_OF_RANGE);
					data.setConnected(false);
				}
			}
			// TODO:
//			if (accessPointData.containsKey("f8:e4:fb:06:ab:23"))
//				wasAdded = true;
			
			// add newly found access points
			for (ScanResult result : scanResults) {
				// TODO
//				if (wasAdded && 
//						result.BSSID.equals("f8:e4:fb:06:ab:23"))
//					continue;
				
				WifiAccessPointData data = new WifiAccessPointData(result.BSSID, result.SSID);
				data.setSignalStrength(signalLevel2SignalStrength(result.level));
				data.setRawSignalStrength(result.level);
				data.setConnected(
						result.BSSID.equalsIgnoreCase(
								wifiManager.getConnectionInfo().getBSSID()));
				accessPointData.put(result.BSSID, data);
			}
			
			if (BuildConfig.DEBUG)
				Log.d(Constants.DEBUG_TAG, "Found " + accessPointData.size() + " BSSIDs, sending to listeners");
			
			// activity consumers
			if (wifiReceiverActivityMap != null) {
				for (Map.Entry<Activity, Activity> entry : wifiReceiverActivityMap.entrySet()) {
					((IWifiReceiverActivity) entry.getKey()).updateWifiData(accessPointData);
				}
			}
			
			// service consumers
			if (wifiReceiverService != null)
				wifiReceiverService.updateWifiData(accessPointData);
		}	
	}
	
	private SignalStrength signalLevel2SignalStrength(int level)
	{
		switch (WifiManager.calculateSignalLevel(level, 4)) {
		case 0:
			return SignalStrength.ONE_BAR;
		case 1:
			return SignalStrength.TWO_BARS;
		case 2:
			return SignalStrength.THREE_BARS;
		case 3:
			return SignalStrength.FOUR_BARS;
		default:
			return SignalStrength.UNKNOWN;
		}
	}
	
	public void requestWifiScan() {
		if (wifiManager == null)
			wifiManager	= (WifiManager) context.getSystemService(
					Context.WIFI_SERVICE);

		context.registerReceiver(wifiScanReceiver, 
				new IntentFilter(
						WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

		wifiManager.startScan();
	}
	
	public void attachActivity(Activity activity) {
		if (context == null)
			context = activity.getApplicationContext();
		wifiReceiverActivityMap.put(activity, activity);
	}
	public void detachActivity(Activity activity) {
		wifiReceiverActivityMap.remove(activity);
	}
	
	public void attachService(IWifiDataConsumer service) {
		if (context == null)
			this.context = service.getContext();
		this.wifiReceiverService = service;
	}
	public void detachService() {
		this.wifiReceiverService = null;
	}

	public Map<String, WifiAccessPointData> getLatestAccessPointData() {
		return accessPointData;
	}

}
