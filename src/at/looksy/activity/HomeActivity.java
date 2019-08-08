package at.looksy.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import at.looksy.BuildConfig;
import at.looksy.R;
import at.looksy.activity.base.HomeBaseActivity;
import at.looksy.cache.DataCache;
import at.looksy.cache.DataCache.CacheName;
import at.looksy.core.Constants;
import at.looksy.dataitem.LocationDataItem;
import at.looksy.dialog.EnableWifiDialog;
import at.looksy.manager.DeviceManager;
import at.looksy.manager.DimensionManager;
import at.looksy.manager.LocationTileManager;
import at.looksy.service.NotificationService;
import at.looksy.service.WebService;
import at.looksy.service.WifiService;
import at.looksy.service.consumer.IWebServiceAsyncConsumer;
import at.looksy.service.data.WebServiceRequest;
import at.looksy.service.data.WebServiceResponse;
import at.looksy.service.data.WebServiceResponse.StatusCode;
import at.looksy.service.data.WifiAccessPointData;
import at.looksy.service.data.WifiAccessPointData.SignalStrength;
import at.looksy.tile.ITile;
import at.looksy.tile.LocationTile;
import at.looksy.util.DataItemUtil;

public class HomeActivity extends HomeBaseActivity implements IWifiReceiverActivity, IWebServiceAsyncConsumer{

	private LocationTileManager locationTileManager = null;
	private WifiService wifiService = null;
	
	private Map<String, WifiAccessPointData> latestWifiDataMap;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		if (BuildConfig.DEBUG) {
			Log.d(Constants.DEBUG_TAG, "HomeActivity: onCreate() called");
		}

		initActivity();
		
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_show_wifi:
			Intent intent = new Intent(this, WifiListingActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_home, menu);
		return true;
	}
	
	private void wifiEnabledCheck()
	{
		// enable wifi, if not already enabled
		WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		if (!wifiManager.isWifiEnabled()) {
			new EnableWifiDialog(this);
		}
	}

	private void initActivity()
	{
		// enable wifi, if not already enabled
		wifiEnabledCheck();

		// Initialize menu items
		initNavbar(this);

		// Tile manager
		if (locationTileManager == null)
			locationTileManager = new LocationTileManager(this);

		// Device manager
		if (!DeviceManager.isInitialized())
			DeviceManager.initialize(getApplicationContext());
		
		// Dimension manager (must be called in the launcher activity)
		DimensionManager.initialize(this);
		
		// Notification service
		Intent intent = new Intent(getApplicationContext(), NotificationService.class);
		PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent, 0);
		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0,
		    TimeUnit.SECONDS.toMillis(Constants.WIFI_SCAN_INTERVAL), pendingIntent);
		
		// Register device
		WebService.registerDevice();
		
		// Recieve wifi service callbacks
		if (wifiService == null)
			wifiService = WifiService.getInstance();
		wifiService.attachActivity(this);
		wifiService.requestWifiScan();
		
		// Grab cached data immediately, if exists
		if (wifiService.getLatestAccessPointData() != null)
			updateWifiData(wifiService.getLatestAccessPointData());
		
		// Set inline notification text
		((TextView)findViewById(R.id.inlineNotificationHeading)).setText(
				R.string.activity_home_locations_appear_here);
		((TextView)findViewById(R.id.inlineNotificationSubheading)).setText(
				R.string.activity_home_locations_appear_here_more);
		findViewById(R.id.notification).setVisibility(View.VISIBLE);
	}

	@Override
	protected void onResume()
	{
		if (BuildConfig.DEBUG) {
			Log.d(Constants.DEBUG_TAG, "HomeActivity: onResume() called");
		}

		locationTileManager.refreshFollowedAllUI();
		super.onResume();
	}
	

	@Override
	public void onBackPressed()	{
		finish();
	}

	@Override
	public void finish()
	{
		if (BuildConfig.DEBUG) {
			Log.d(Constants.DEBUG_TAG, "HomeActivity: finish() called");
		}

		// detach from wifi service
		if (wifiService != null)
			wifiService.detachActivity(this);

		// clean up images
		if (locationTileManager != null)
			locationTileManager = null;

		// flush select caches
		DataCache dataCache = DataCache.getInstance();
		dataCache.flushCache(CacheName.LOCATION_BY_BSSID_CACHE);
		dataCache.flushCache(CacheName.LOCATION_BY_ID_CACHE);
		dataCache.flushCache(CacheName.LOCATION_ID_TILE_ID_CACHE);
		dataCache.flushCache(CacheName.TILE_CACHE);

		super.finish();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (BuildConfig.DEBUG)
			Log.d(Constants.DEBUG_TAG, "HomeActivity: onNewIntent() called");
		super.onNewIntent(intent);
		setIntent(intent);
	}

	@Override
	public void updateWifiData(Map<String, WifiAccessPointData> wifiDataMap) {

		// check if we are still active
		if (isFinishing())
			return;

		this.latestWifiDataMap = wifiDataMap;

		if (BuildConfig.DEBUG)
			Log.d(Constants.DEBUG_TAG, "HomeActivity.updateWifiData() called");

		// UPDATE existing tiles, kicking out expired ones
		Map<String,String> knownBssidMap = new HashMap<String,String>();
		Map<String,ITile> tiles = locationTileManager.getTileMap();
		Iterator<Map.Entry<String, ITile>> tileIterator = tiles.entrySet().iterator();
		while (tileIterator.hasNext()) {
			Map.Entry<String, ITile> entry = tileIterator.next();
			LocationTile tile = (LocationTile)entry.getValue();
			String bssid = tile.getData().getBSSIDList().get(0);
			
			// grab the latest wifi data and update the proximity indicator
			WifiAccessPointData wifiData = wifiDataMap.get(bssid);
			tile.updateProximityIndicator(wifiData == null ? 
					SignalStrength.OUT_OF_RANGE : 
						wifiData.getSignalStrength());
				
			if (tile.shouldRemove()) {
				tileIterator.remove();
				locationTileManager.removeTile(tile);

			} else {
				knownBssidMap.put(bssid, bssid);
			}
		}

		// FIND difference between what we've already seen and what we just saw
		// and prepare the lookup request
		DataCache dataCache = DataCache.getInstance();
		List<String> newBssidList = new ArrayList<String>();
		for (Map.Entry<String,WifiAccessPointData> entry : wifiDataMap.entrySet()) {
			String bssid = entry.getKey();
			if (!knownBssidMap.containsKey(bssid) && 
					!dataCache.containsEntry(CacheName.BSSID_NOT_FOUND_CACHE, bssid)) 
			{
				newBssidList.add(bssid);
			}
		}

		// LOOKUP new locations, if any
		// start with the cache
		Map<String,LocationDataItem> entityMap = null;
		for (int i = 0; i < newBssidList.size(); i++) {
			if (dataCache.containsEntry(CacheName.LOCATION_BY_BSSID_CACHE, newBssidList.get(i))) {
				if (entityMap == null) 
					entityMap = new HashMap<String,LocationDataItem>();
				String bssid = newBssidList.get(i);
				LocationDataItem item = 
						(LocationDataItem) dataCache.getEntry(
								CacheName.LOCATION_BY_BSSID_CACHE, bssid);
				entityMap.put(bssid, item);
				newBssidList.remove(i);
			}
		}
		
		updateLocationTilesUI(entityMap);

		// then hit the web for more
		if (!newBssidList.isEmpty())
			new WebService(this).getLocationsForBSSIDs(newBssidList);
	}


	@Override
	public void consumeWSExchange(WebServiceRequest wsRequest, WebServiceResponse wsResponse) {

		if (!receiveWSCallback())
			return;
		
		if (wsResponse == null)
			return;
		
		// get cache handle
		DataCache dataCache = DataCache.getInstance();
		
		String method = wsRequest.getRequestAction();

		if (method.equals(Constants.JSON_METHOD_GET_LOCATIONS_FOR_BSSIDS)) {
			Map<String,LocationDataItem> map = new HashMap<String,LocationDataItem>();
			try {
				if (wsResponse.getStatus() == StatusCode.OK) {
					List<LocationDataItem> entityList = 
							DataItemUtil.decodeJSONLocations(
									wsResponse.getJSONArray(Constants.WEB_SERVICE_DATA));
					for (LocationDataItem item : entityList) {
						// prepare return map
						map.put(item.getBSSIDList().get(0), item);

						// cache the return data
						dataCache.putEntry(CacheName.LOCATION_BY_ID_CACHE, item.getId(), item);
						dataCache.putEntry(CacheName.LOCATION_BY_BSSID_CACHE, item.getBSSIDList().get(0), item);
					}
				}
			} catch (JSONException e) {	e.printStackTrace(); }

			// add the new tiles
			updateLocationTilesUI(map);

			// compare the bssid request/return set - add the not found bssids to the cache
			try {
				String str = wsRequest.getRequestData().getString(Constants.JSON_FIELD_BSSID_LIST);
				JSONArray jsonArray = new JSONArray(str);
				for (int i = 0; i < jsonArray.length(); i++) {
					// add to not found cache if we didn't return a valid result
					String bssid = jsonArray.getString(i);
					if (!map.containsKey(bssid))
						dataCache.putEntry(CacheName.BSSID_NOT_FOUND_CACHE, 
								bssid, Boolean.valueOf(true));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private void updateLocationTilesUI(Map<String,LocationDataItem> dataMap)
	{
		if (dataMap != null) {
			// FOUND new stuff - update UI
			for (Map.Entry<String, LocationDataItem> entry : dataMap.entrySet()) {
				LocationDataItem item = entry.getValue();
				if (locationTileManager.getTileMap().containsKey(item.getId()))
					continue;
				LocationTile homeTile = new LocationTile(
						this, item,	latestWifiDataMap.get(item.getBSSIDList().get(0)));
				locationTileManager.addTile(homeTile);

				// register this pass by
				WebService.registerUserLocationPassBy(item.getId());
			}
		}
		
		// update or hide the notification
		View inlineNotification = findViewById(R.id.notification);
		
		if (locationTileManager.hasTiles()) {
			inlineNotification.setVisibility(View.GONE);
			
		} else {
			inlineNotification.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public boolean receiveWSCallback() {
		return !isFinishing();
	}

}
