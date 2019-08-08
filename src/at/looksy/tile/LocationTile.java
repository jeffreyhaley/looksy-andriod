package at.looksy.tile;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import at.looksy.R;
import at.looksy.activity.LocationHomeActivity;
import at.looksy.cache.DataCache;
import at.looksy.cache.DataCache.CacheName;
import at.looksy.core.Constants;
import at.looksy.dataitem.LocationDataItem;
import at.looksy.manager.DimensionManager;
import at.looksy.manager.ImageManager;
import at.looksy.manager.ImageManager.ImageQuality;
import at.looksy.manager.ImageManager.RoundingEffect;
import at.looksy.service.WebService;
import at.looksy.service.consumer.IWebServiceAsyncConsumer;
import at.looksy.service.data.WebServiceRequest;
import at.looksy.service.data.WebServiceResponse;
import at.looksy.service.data.WebServiceResponse.StatusCode;
import at.looksy.service.data.WifiAccessPointData;
import at.looksy.service.data.WifiAccessPointData.SignalStrength;
import at.looksy.util.UIUtil;
import at.looksy.util.Util;


public class LocationTile extends AbstractTile implements ITile, IWebServiceAsyncConsumer {

	private LocationDataItem data;
	private WifiAccessPointData wifiData;

	private RelativeLayout rootLayout = null;
	private TextView headingView = null;
	private TextView subheadingView = null;
	private ImageView btnFollowView = null;
	private LinearLayout btnFollowGroupView = null;
	private TextView lastSeenView = null;
	private ImageView signalStrengthView = null;
	private TextView btnFollowTextView = null;
	
	private Activity activity = null;
	
	public LocationTile(
			Activity activity, 
			LocationDataItem entityData,
			WifiAccessPointData wifiData)
	{
		this.activity = activity;
		this.data = entityData;
		this.wifiData = wifiData;

		inflate();
		updateProximityIndicator(wifiData == null ? 
				SignalStrength.UNKNOWN : 
					wifiData.getSignalStrength());
	}

	@Override
	protected void inflate() {

		// determine type of tile
		LocationDataItem entityDataItem = (LocationDataItem) data;

		// find views
		rootLayout = (RelativeLayout) activity.getLayoutInflater().inflate(R.layout.tile_location, null);
		headingView = (TextView) rootLayout.findViewById(R.id.textTileHeading);
		subheadingView = (TextView) rootLayout.findViewById(R.id.textTileSubheading);
		setImagePayloadView((ImageView) rootLayout.findViewById(R.id.imgEntity));
		btnFollowView = (ImageView) rootLayout.findViewById(R.id.btnFollow);
		btnFollowTextView = (TextView) rootLayout.findViewById(R.id.btnFollowText);
		btnFollowGroupView = (LinearLayout) rootLayout.findViewById(R.id.btnFollowGroup);
		
		lastSeenView = (TextView) rootLayout.findViewById(R.id.textLastSeen);
		signalStrengthView = (ImageView) rootLayout.findViewById(R.id.imgSignalStrength);

		// animations (first, of course!)
		animateViewChange(rootLayout);

		// set width
		LinearLayout.LayoutParams layout = 
				new LinearLayout.LayoutParams(DimensionManager.getLocationTileWidth(), 
						LinearLayout.LayoutParams.WRAP_CONTENT);

		layout.setMargins(DimensionManager.getLocationTileMargin(), 
				0, 0, DimensionManager.getLocationTileMargin());
		rootLayout.setLayoutParams(layout);

		// set handlers
		updateHeartUI();
		btnFollowGroupView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				data.setStarred(!data.isStarred());
				updateHeartUI();
				
				// update the web
				new WebService(LocationTile.this)
				.updateLocationFavoriteStatus(
						getData().getId(), getData().isStarred());
			}
		});

		rootLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showItem();
			}
		});

		// set info
		ImageManager imageHelper = ImageManager.getInstance();
		imageHelper.loadImageView(
				data.getImagePath(), 
				DimensionManager.getLocationTileWidth(),
				ImageManager.HEIGHT_NO_CROP,
				ImageQuality.MEDIUM,
				RoundingEffect.TOP_ONLY,
				getImagePayloadView());
		getImagePayloadView().setMinimumWidth(DimensionManager.getLocationTileWidth());
		
		if (entityDataItem != null) {
			headingView.setText(entityDataItem.getName());
			subheadingView.setText(entityDataItem.getCity() + ", " + entityDataItem.getState());
		}

		rootLayout.requestLayout();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	private void animateViewChange(View animatedView)
	{
		animatedView.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {

			@Override
			public void onViewDetachedFromWindow(View v) { }

			@Override
			public void onViewAttachedToWindow(View v) {
				Animation anim = AnimationUtils.loadAnimation(activity, android.R.anim.fade_in);
				anim.setDuration(1000);
				v.startAnimation(anim);
			}
		});
	}

	/**
	 * Update proximity indicator for this tile.
	 * @param apData Proximity data or null to hide indicator
	 */
	public void updateProximityIndicator(SignalStrength signalStrength)
	{
		lastSeenView.setVisibility(View.GONE);
		signalStrengthView.setVisibility(View.GONE);
		
		// update local wifi data
		if (wifiData != null)
			wifiData.setSignalStrength(signalStrength);
		
		if (signalStrength == SignalStrength.OUT_OF_RANGE) {
			lastSeenView.setVisibility(View.VISIBLE);
			String humanDateStr = 
					Util.humanTimeDifferenceMajor(new Date(), wifiData.getDateLastSeen());
			lastSeenView.setText(humanDateStr);
			
		} else if (signalStrength == SignalStrength.UNKNOWN) {
			// do nothing (for starred-only tiles)
			
		} else {
			UIUtil.setWifiProximityIndicator(signalStrength, signalStrengthView);
			wifiData.setDateLastSeen(new Date());
		}
		
	}
	
	public boolean shouldRemove() {
		if (wifiData == null)
			return true;
		else 
			return Util.minuteDifference(
					new Date(), wifiData.getDateLastSeen()) 
					>= Constants.LOCATION_TILE_TIME_TO_LIVE;
	}

	public WifiAccessPointData getWifiData() {
		return wifiData;
	}

	@Override
	public View getLayout() {
		return rootLayout;
	}

	public LocationDataItem getData() {
		return data;
	}
	
	public void refreshFollowed()
	{
		DataCache dataCache = DataCache.getInstance();
		if (dataCache.containsEntry(CacheName.LOCATION_BY_ID_CACHE, getData().getId())) {
			LocationDataItem dataItem = (LocationDataItem) dataCache.getEntry(CacheName.LOCATION_BY_ID_CACHE, getData().getId());
			getData().setStarred(dataItem.isStarred());
			getData().setStarCount(dataItem.getStarCount());
			updateHeartUI();
			
		} else {
			new WebService(this).isLocationFavorite(getData().getId());
		}
	}
	
	private void updateHeartUI()
	{
		btnFollowView.setImageResource(data.isStarred() ? 
				R.drawable.ico_heart_on : 
					R.drawable.ico_heart_off);
		if (data.getStarCount() != 0) {
			btnFollowTextView.setText(String.valueOf(data.getStarCount()));
			btnFollowTextView.setVisibility(View.VISIBLE);
		} else {
			btnFollowTextView.setText("Love");
			btnFollowTextView.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public void showItem()
	{
		// register this visit
		WebService.registerUserLocationVisit(getData().getId());

		// do actual visit
		Bundle bundle = new Bundle();
		bundle.putSerializable(Constants.BUNDLE_DATA, getData());
		Intent intent = new Intent(activity, LocationHomeActivity.class);
		intent.putExtras(bundle);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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

		try {
			if (wsResponse.getStatus() == StatusCode.OK) {
				if (method.equals(Constants.JSON_METHOD_IS_LOCATION_FAVORITE)) {
					// get new favorite count
					boolean isFavorite = 
							((JSONObject)wsResponse.getJSONArray(
									Constants.WEB_SERVICE_DATA).get(0)).getInt(
											Constants.JSON_FIELD_FAVORITE) >= 1;
			
					// update local copy
					getData().setStarred(isFavorite);
					
					// update cached copy
					if (dataCache.containsEntry(CacheName.LOCATION_BY_ID_CACHE, getData().getId())) {
						LocationDataItem cachedDataItem = (LocationDataItem)
								dataCache.getEntry(CacheName.LOCATION_BY_ID_CACHE, getData().getId());
						cachedDataItem.setStarred(isFavorite);
					}
					
					// update UI
					updateHeartUI();
					
				} else if (method.equals(Constants.JSON_METHOD_UPDATE_LOCATION_FAVORITE_STATUS)) {
					// get new favorite count
					int favoriteCount = 
							((JSONObject)wsResponse.getJSONArray(
									Constants.WEB_SERVICE_DATA).get(0)).getInt(
											Constants.JSON_FIELD_FAVORITE_COUNT);
			
					// update local copy
					getData().setStarCount(favoriteCount);
					
					// update cached copy
					if (dataCache.containsEntry(CacheName.LOCATION_BY_ID_CACHE, getData().getId())) {
						LocationDataItem cachedDataItem = (LocationDataItem)
								dataCache.getEntry(CacheName.LOCATION_BY_ID_CACHE, getData().getId());
						cachedDataItem.setStarCount(favoriteCount);
						cachedDataItem.setStarred(getData().isStarred());
					}
					
					// update UI
					updateHeartUI();
				}
			}
		} catch (JSONException e) {	e.printStackTrace(); }
		
	}

	@Override
	public boolean receiveWSCallback() {
		return !activity.isFinishing();
	}

}
