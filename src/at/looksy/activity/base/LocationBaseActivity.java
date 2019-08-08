package at.looksy.activity.base;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import at.looksy.R;
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
import at.looksy.util.Util;

public class LocationBaseActivity extends FragmentActivity implements IWebServiceAsyncConsumer {

	private LocationDataItem data;
	
	@Override
	protected void onResume() {
		
		// update favorite
		DataCache dataCache = DataCache.getInstance();
		if (dataCache.containsEntry(CacheName.LOCATION_BY_ID_CACHE, getData().getId())) {
			LocationDataItem cachedDataItem = (LocationDataItem)
					dataCache.getEntry(CacheName.LOCATION_BY_ID_CACHE, getData().getId());
			getData().setStarCount(cachedDataItem.getStarCount());
			getData().setStarred(cachedDataItem.isStarred());

			// update UI
			updateHeartUI();
		}
		
		super.onResume();
	}
	
	protected void initActivity()
	{
		// Grab data
		Bundle bundle = getIntent().getExtras();
		data = (LocationDataItem) bundle.getSerializable(Constants.BUNDLE_DATA);

		// logo
		ImageManager imageHelper = ImageManager.getInstance();
		ImageView entityImage = (ImageView) findViewById(R.id.imgEntity);
		imageHelper.loadImageView(
				getData().getImagePath(),
				DimensionManager.getEntityImageWidth(), 
				ImageManager.HEIGHT_NO_CROP,
				ImageQuality.SMALL,
				RoundingEffect.ON,
				entityImage);

		// heading
		TextView heading = (TextView) findViewById(R.id.textTileHeading);
		if (getData() != null) {
			heading.setText(getData().getName());
			heading.setWidth(DimensionManager.getEntityHeadingWidth());
		}

		// subheading
		TextView subheading = (TextView) findViewById(R.id.textTileSubheading);
		if (getData() != null)
			subheading.setText(getData().getCity() + ", " + getData().getState());

		// star listener
		updateHeartUI();
		findViewById(R.id.btnFollow).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				// update local data structure
				getData().setStarred(!getData().isStarred());

				// update the local panel UI
				updateHeartUI();

				// update the web
				new WebService(LocationBaseActivity.this)
				.updateLocationFavoriteStatus(getData().getId(), getData().isStarred());
			}
		});

	}

	public void updateHeartUI() {
		ImageView btnKeepView = (ImageView) findViewById(R.id.btnFollow);
		TextView btnFollowTextView = (TextView) findViewById(R.id.btnFollowText);

		btnKeepView.setImageResource(data.isStarred() ? 
				R.drawable.ico_heart_on : 
					R.drawable.ico_heart_off);
		if (data.getStarCount() != 0) {
			btnFollowTextView.setText(String.valueOf(data.getStarCount()));
			btnFollowTextView.setVisibility(View.VISIBLE);
		} else {
			btnFollowTextView.setVisibility(View.GONE);
		}
	}

	protected LocationDataItem getData() {
		return data;
	}

	protected void endView()
	{
//		Class<?> callerClass = ActivityManager.getInstance().pop2();
//
//		Intent intent = new Intent(this, callerClass);
//		startActivity(intent);

		finish();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	@Override
	public void onBackPressed()
	{
		endView();
	}

	@Override
	public void consumeWSExchange(WebServiceRequest wsRequest,
			WebServiceResponse wsResponse) {

		if (!receiveWSCallback())
			return;

		DataCache dataCache = DataCache.getInstance();

		if (wsResponse.getStatus() == StatusCode.OK) {
			try {
				if (wsRequest.getRequestAction().equals(Constants.JSON_METHOD_UPDATE_LOCATION_FAVORITE_STATUS)) {

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
			} catch (JSONException e) {	e.printStackTrace(); }
		}
	}

	@Override
	public boolean receiveWSCallback() {
		return !isFinishing();
	}

}
