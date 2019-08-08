package at.looksy.activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import at.looksy.BuildConfig;
import at.looksy.R;
import at.looksy.activity.base.HomeBaseActivity;
import at.looksy.cache.DataCache;
import at.looksy.cache.DataCache.CacheName;
import at.looksy.core.Constants;
import at.looksy.dataitem.LocationDataItem;
import at.looksy.manager.LocationTileManager;
import at.looksy.service.WebService;
import at.looksy.service.consumer.IWebServiceAsyncConsumer;
import at.looksy.service.data.WebServiceRequest;
import at.looksy.service.data.WebServiceResponse;
import at.looksy.tile.LocationTile;
import at.looksy.util.DataItemUtil;


public class FavoriteLocationsActivity extends HomeBaseActivity implements IWebServiceAsyncConsumer {

	private LocationTileManager locationTileManager = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_favorite_locations);

		if (BuildConfig.DEBUG) {
			Log.d(Constants.DEBUG_TAG, "StarredTilesActivity: onCreate() called");
		}

		initActivity();

		// grab data
		new WebService(this).getFavoriteLocationsForDevice();
	}

	protected void initActivity()
	{
		// Initialize services and managers
		if (locationTileManager == null)
			locationTileManager = new LocationTileManager(this);

		// Initialize menu items
		initNavbar(this);
	}

	protected void endView()
	{
		finish();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	@Override
	public void onResume()
	{
		if (BuildConfig.DEBUG) {
			Log.d(Constants.DEBUG_TAG, "StarredTilesActivity: onResume() called");
		}

		if (locationTileManager != null)
			locationTileManager.refreshFollowedAllUI();
		
		super.onResume();
	}


	@Override
	public void finish()
	{
		if (BuildConfig.DEBUG) {
			Log.d(Constants.DEBUG_TAG, "StarredTilesActivity: finish() called");
		}

		// clean up bitmaps
		if (locationTileManager != null) {
			locationTileManager = null;
		}

		super.finish();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (BuildConfig.DEBUG)
			Log.d(Constants.DEBUG_TAG, "StarredTilesActivity: onNewIntent() called");
		super.onNewIntent(intent);
		setIntent(intent);
	}

	private void populateStarredTilesUI(Map<String,LocationDataItem> dataMap)	{
		
		// hide progress bar
		findViewById(R.id.progress).setVisibility(View.GONE);

		if (dataMap != null && !dataMap.isEmpty()) {
			for (Map.Entry<String, LocationDataItem> entry : dataMap.entrySet()) {
				LocationDataItem item = entry.getValue();
				LocationTile homeTile = new LocationTile(this, item, null);
				locationTileManager.addTile(homeTile);
			}

		} else {

			// update or hide the notification
			((TextView)findViewById(R.id.inlineNotificationHeading)).setText(
					R.string.activity_favorite_locations_no_loved_locations);
			((TextView)findViewById(R.id.inlineNotificationSubheading)).setText(
					R.string.activity_favorite_locations_no_loved_locations_more);
			findViewById(R.id.notification).setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onBackPressed() {
		endView();
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

		if (method.equals(Constants.JSON_METHOD_GET_FAVORITE_LOCATIONS_FOR_DEVICE)) {
			try {
				Map<String,LocationDataItem> map = new HashMap<String,LocationDataItem>();
				List<LocationDataItem> entityList = 
						DataItemUtil.decodeJSONLocations(
								wsResponse.getJSONArray((Constants.WEB_SERVICE_DATA)));
				for (LocationDataItem item : entityList) {
					// return data
					map.put(item.getBSSIDList().get(0), item);

					// cache the return data
					dataCache.putEntry(CacheName.LOCATION_BY_ID_CACHE, item.getId(), item);
					dataCache.putEntry(CacheName.LOCATION_BY_BSSID_CACHE, item.getBSSIDList().get(0), item);
				}
				
				// add the tiles
				populateStarredTilesUI(map);
				
			} catch (Exception e) { e.printStackTrace(); }
		}

	}

	@Override
	public boolean receiveWSCallback() {
		return !isFinishing();
	}

}

