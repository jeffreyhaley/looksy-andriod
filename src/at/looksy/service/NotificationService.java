package at.looksy.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import at.looksy.BuildConfig;
import at.looksy.R;
import at.looksy.activity.HomeActivity;
import at.looksy.cache.DataCache;
import at.looksy.cache.DataCache.CacheName;
import at.looksy.core.Constants;
import at.looksy.manager.DeviceManager;
import at.looksy.manager.ImageManager;
import at.looksy.manager.PreferenceManager;
import at.looksy.receiver.NotificationBroadcastReceiver;
import at.looksy.service.consumer.IWebServiceAsyncConsumer;
import at.looksy.service.consumer.IWifiDataConsumer;
import at.looksy.service.data.WebServiceRequest;
import at.looksy.service.data.WebServiceResponse;
import at.looksy.service.data.WebServiceResponse.StatusCode;
import at.looksy.service.data.WifiAccessPointData;
import at.looksy.util.Util;


public class NotificationService extends Service implements IWifiDataConsumer, IWebServiceAsyncConsumer {

	private IBinder notificationServiceBinder = new NotificationServiceBinder();
	private WifiService wifiService = null;
	
	private Date notificationLastDismissed;
	private Date lastVibrateEvent;
	
	private Map<String, WifiAccessPointData> wifiDataCache = null;
	
	// cache references - so we don't lose them when the main app exits
	private DataCache dataCache = null;
	private ImageManager imageManager = null;
	
	
	private void initialize() {
		
		if (BuildConfig.DEBUG)
			Log.d(Constants.DEBUG_TAG, "NotificationService.initialize() called");
		
		// init the DeviceManager, if needed
		if (!DeviceManager.isInitialized())
			DeviceManager.initialize(getApplicationContext());
		
		wifiService = WifiService.getInstance();
		wifiService.attachService(this);
		wifiService.requestWifiScan();
		
		// reference the image and data caches so we don't lose the data
		if (dataCache == null)
			dataCache = DataCache.getInstance();
		if (imageManager == null)
			imageManager = ImageManager.getInstance();
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return notificationServiceBinder;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		if (BuildConfig.DEBUG) 
			Log.d(Constants.DEBUG_TAG, "NotificationService.onUnbind() called");
		return true;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		initialize();
		return START_STICKY;
	}
	
	@Override
	public void onDestroy()
	{
		if (BuildConfig.DEBUG) 
			Log.d(Constants.DEBUG_TAG, "NotificationService.onDestroy() called");
		stopForeground(true);
		super.onDestroy();
	}
	
	public class NotificationServiceBinder extends Binder {
		public NotificationService getService() {
			return NotificationService.this;
		}
	}
	
	private void cancelNotification() {
		((NotificationManager) getApplicationContext().
				getSystemService(Context.NOTIFICATION_SERVICE)).
				cancel(Constants.NOTIFICATION_ID);
	}
	
	public void updateNotification(int locationsDiscovered) {
		
		String text = null;
		if (locationsDiscovered == 0) {
			cancelNotification();
			return;
		}
		
		if (!shouldNotify())
			return;
		
		// vibrate
		if (shouldVibrate()) {
			Vibrator vibrator = (Vibrator) getApplicationContext().
					getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(1000);
			lastVibrateEvent = new Date();
			if (BuildConfig.DEBUG)
				Log.d(Constants.DEBUG_TAG, "NotificationService sending out a smooth vibration.");
		}
		
		// text notification		
		text = locationsDiscovered + 
				" location" + (locationsDiscovered > 1 ? "s" : "");
		
		Context context = this.getApplicationContext();
		Intent notificationIntent = new Intent(context, HomeActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(context,
		        Constants.NOTIFICATION_ID, notificationIntent,
		        PendingIntent.FLAG_CANCEL_CURRENT);

		NotificationManager nm = (NotificationManager) context
		        .getSystemService(Context.NOTIFICATION_SERVICE);

		Resources res = context.getResources();
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

		builder.setContentIntent(contentIntent)
		            .setSmallIcon(R.drawable.app_notification_icon)
		            .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.app_notification_icon))
		            .setWhen(System.currentTimeMillis())
		            .setAutoCancel(false)
		            .setContentTitle(res.getString(R.string.app_name))
		            .setContentText(text)
		            .setDeleteIntent(createDeleteIntent());
		
		@SuppressWarnings("deprecation")
		Notification notification = builder.getNotification();
		
		nm.notify(Constants.NOTIFICATION_ID, notification);
	}
	
	private boolean shouldVibrate() {
		if (lastVibrateEvent == null) {
			lastVibrateEvent = new Date();
			return true;
			
		} else {
			return Util.hourDifference(new Date(), lastVibrateEvent) 
					>= Constants.VIBRATE_NOTIFICATION_TIME_SPREAD;
		}
	}

	private PendingIntent createDeleteIntent()
	{
		Intent intent = new Intent(getApplicationContext(), NotificationBroadcastReceiver.class);
	    intent.setAction(Constants.BROADCAST_NOTIFICATION_CANCELLED);
	    return PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	}
	
	private boolean shouldNotify() {
		
		String notificationLastDismissedStr = 
				new PreferenceManager(getApplicationContext()).getPreference(
						Constants.PREF_NOTIF_LAST_DISMISSED);
		notificationLastDismissed = notificationLastDismissedStr == null ? 
				null : new Date(Long.parseLong(notificationLastDismissedStr));
		
		// first time notifying -- ever?
		if (notificationLastDismissed == null) {
			return true;
			
		} else {
			// has enough time gone by that we should show it again?
			if (Util.hourDifference(notificationLastDismissed, new Date()) 
					>= Constants.SHOW_NOTIFICATION_TIME_SPREAD) {
				// Note: notificationLastDismissed is updated in the
				// notification swipe listener
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public void updateWifiData(Map<String, WifiAccessPointData> data) {
		
		if (BuildConfig.DEBUG)
			Log.d(Constants.DEBUG_TAG, "NotificationService.updateWifiData() called");
		
		DataCache dataCache = DataCache.getInstance();
		
		List<String> bssidList = new ArrayList<String>();
		for (Map.Entry<String,WifiAccessPointData> entry : data.entrySet()) {
			WifiAccessPointData wifiData = entry.getValue();
			if (dataCache.containsEntry(
							CacheName.BSSID_NOT_FOUND_CACHE, wifiData.getBSSID())
				|| dataCache.containsEntry(
							CacheName.BSSID_FOUND_CACHE, wifiData.getBSSID()))
			{
				// Skip ones we've confirmed either do or do not exist
				continue;
			}
			
			bssidList.add(wifiData.getBSSID());
		}
		
		int locationsDiscovered = dataCache.getEntryCount(
				CacheName.BSSID_FOUND_CACHE);
		updateNotification(locationsDiscovered);
		
		this.wifiDataCache = data;
		
		if (!bssidList.isEmpty())
			new WebService(this).getLocationCountForBSSIDs(bssidList);
	}

	public Map<String, WifiAccessPointData> getWifiDataCache() {
		return wifiDataCache;
	}
	
	@Override
	public void consumeWSExchange(WebServiceRequest wsRequest,
			WebServiceResponse wsResponse) {
		
		if (!receiveWSCallback())
			return;
		
		if (wsResponse == null)
			return;
		
		// get cache handle
		DataCache dataCache = DataCache.getInstance();
		String method = wsRequest.getRequestAction();

		if (method.equals(Constants.JSON_METHOD_GET_LOCATION_COUNT_FOR_BSSIDS)) {
			try {
				if (wsResponse.getStatus() == StatusCode.OK) {
					JSONArray dataArray = wsResponse.getJSONArray(
							Constants.WEB_SERVICE_DATA);
					for (int i = 0; i < dataArray.length(); i++) {
						String bssid = ((JSONObject)dataArray.get(i)).getString(
								Constants.JSON_FIELD_TRANSMITTER_BSSID);
						dataCache.putEntry(CacheName.BSSID_FOUND_CACHE, 
								bssid, Boolean.valueOf(true));
					}
				}
			} catch (JSONException e) {	e.printStackTrace(); }
			
			// compare the bssid request/return set - add the not found bssids to the cache
			try {
				String str = wsRequest.getRequestData().getString(
						Constants.JSON_FIELD_BSSID_LIST);
				JSONArray jsonArray = new JSONArray(str);
				for (int i = 0; i < jsonArray.length(); i++) {
					// add to not found cache if the bssid is not a location
					String bssid = jsonArray.getString(i);
					if (!dataCache.containsEntry(CacheName.BSSID_FOUND_CACHE, bssid))
						dataCache.putEntry(CacheName.BSSID_NOT_FOUND_CACHE, 
								bssid, Boolean.valueOf(true));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			int locationsDiscovered = dataCache.getEntryCount(
					CacheName.BSSID_FOUND_CACHE);
			updateNotification(locationsDiscovered);
		}
	}

	@Override
	public boolean receiveWSCallback() {
		// Always consume callback
		return true;
	}

	@Override
	public Context getContext() {
		return this.getApplicationContext();
	}

}
