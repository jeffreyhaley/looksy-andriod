package at.looksy.activity.graveyard;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import at.looksy.R;
import at.looksy.cache.DataCache;
import at.looksy.cache.DataCache.CacheName;
import at.looksy.core.Constants;
import at.looksy.dataitem.DataItem;
import at.looksy.dataitem.LocationDataItem;
import at.looksy.manager.ImageManager;
import at.looksy.manager.ImageManager.ImageQuality;
import at.looksy.manager.ImageManager.RoundingEffect;
import at.looksy.service.WebService;
import at.looksy.service.consumer.IWebServiceAsyncConsumer;
import at.looksy.service.data.WebServiceRequest;
import at.looksy.service.data.WebServiceResponse;
import at.looksy.util.UIButtonUtil;
import at.looksy.util.Util;

@Deprecated
public class EntityProfileActivity extends TabActivity implements IWebServiceAsyncConsumer {

	public static int ENTITY_IMAGE_WIDTH = 0;
	private DataItem data = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_entity_profile);

		initActivity();
		
		// prepare the bundle
		Bundle bundle = new Bundle();
		bundle.putSerializable(Constants.BUNDLE_DATA, data);
		
		// init the tabs
		final TabHost tabHost = getTabHost();
		tabHost.getTabWidget().setDividerDrawable(null);
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			
			@Override
			public void onTabChanged(String tabId) {
				renderTabBackgrounds(tabHost);
			}
		});
		
		// "Reel" tab
		TabSpec exploreTab = tabHost.newTabSpec("Explore");
		exploreTab.setIndicator("Explore");
		Intent reelIntent = new Intent(this, EntityExploreActivity.class);
		reelIntent.putExtras(bundle);
		exploreTab.setContent(reelIntent);
		
		// "Social" tab
		TabSpec learnTab = tabHost.newTabSpec("Learn");
		learnTab.setIndicator("Love");
		Intent aboutIntent = new Intent(this, EntityLearnActivity.class);
		aboutIntent.putExtras(bundle);
		learnTab.setContent(aboutIntent);
		
		// "Connect" tab
		TabSpec connectTab = tabHost.newTabSpec("Connect");
		connectTab.setIndicator("Connect");
		Intent connectIntent = new Intent(this, EntityConnectActivity.class);
		connectIntent.putExtras(bundle);
		connectTab.setContent(connectIntent);

		// Adding all TabSpec to TabHost
		tabHost.addTab(exploreTab);
		tabHost.addTab(connectTab);
//		tabHost.addTab(learnTab); TODO
		
		renderTabBackgrounds(tabHost);

	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		
		if (backButtonPressed) {
			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
		} else {
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		}
	}

	private boolean backButtonPressed = false;
	public void setBackButtonPressed()
	{
		backButtonPressed = true;
	}
	
	private void renderTabBackgrounds(TabHost tabHost)
	{
		tabHost.getTabWidget().setDividerDrawable(R.drawable.tab_divider);
		for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
			tabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.panel_tab_inactive);
			TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
			if (tv != null) {
				tv.setTextColor(getResources().getColor(R.color.dark_gray));
				tv.setTextAppearance(getApplicationContext(), R.style.BigButtonTextBold);
				titleCaseText(tv);
			}
			
		}
		tabHost.getCurrentTabView().setBackgroundResource(R.drawable.panel_tab_active);
		TextView tv = (TextView) tabHost.getCurrentTabView().findViewById(android.R.id.title);
		tv.setTextColor(getResources().getColor(R.color.red));
	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void titleCaseText(TextView tv) {
		tv.setAllCaps(false);
	}

	private void initActivity() 
	{	
		Bundle bundle = getIntent().getExtras();
		data = (DataItem) bundle.getSerializable(Constants.BUNDLE_DATA);

		initLayoutSizes();
		
		// logo
		ImageManager imageHelper = ImageManager.getInstance();
		ImageView entityImage = (ImageView) findViewById(R.id.imgEntity);
		imageHelper.loadImageView(
				data.getImagePath(),
				ENTITY_IMAGE_WIDTH, 
				ImageManager.HEIGHT_NO_CROP,
				ImageQuality.SMALL,
				RoundingEffect.ON,
				entityImage);

		// figure out type for all future calls
		LocationDataItem entityDataItem = (LocationDataItem) data;
		
		// heading
		TextView heading = (TextView) findViewById(R.id.textTileHeading);
		if (entityDataItem != null)
			heading.setText(entityDataItem.getName());

		// subheading
		TextView subheading = (TextView) findViewById(R.id.textTileSubheading);
		if (entityDataItem != null)
			subheading.setText(entityDataItem.getCity() + ", " + entityDataItem.getState());

		// star listener
		updateStarUI(data.isStarred());
		findViewById(R.id.btnFollow).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Activity currentActivity = getLocalActivityManager().getCurrentActivity();
				boolean newStarStatus = data.isStarred() ? false : true;
				
				// update local data structure
				data.setStarred(newStarStatus);
				
				// update the local panel UI
				updateStarUI(newStarStatus);
				
				// check if we are on the Connect tab
				if (currentActivity != null && 
						currentActivity instanceof EntityConnectActivity) 
				{
					// if we are, update its star
					((EntityConnectActivity) currentActivity).getData().setStarred(newStarStatus);
					((EntityConnectActivity) currentActivity).updateFollowedWS();
					((EntityConnectActivity) currentActivity).updateFollowedTextUI();
					
				} else {
					// otherwise just call the web service ourselves
					new WebService(EntityProfileActivity.this)
						.updateLocationFavoriteStatus(data.getId(), newStarStatus);
				}
			}
		});
	}
	
	private void initLayoutSizes()
	{
		// logo/pic width
		ENTITY_IMAGE_WIDTH = Util.dp2px(getApplicationContext(), 50);
	}
	
	public void updateStarUI(boolean star)
	{
		TextView btnKeepView = (TextView) findViewById(R.id.btnFollow);
		if (star) {
			UIButtonUtil.buttonOn(btnKeepView, Constants.BTN_FOLLOWED);
		} else {
			UIButtonUtil.buttonOff(btnKeepView, Constants.BTN_FOLLOW);
		}
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
			if (method.equals(Constants.JSON_METHOD_UPDATE_LOCATION_FAVORITE_STATUS)) {
				// get new favorite count
				int favoriteCount = 
						((JSONObject)wsResponse.getJSONArray(
								Constants.WEB_SERVICE_DATA).get(0)).getInt(
										Constants.JSON_FIELD_FAVORITE_COUNT);

				// update local copy
				data.setStarCount(favoriteCount);

				// update cached copy
				if (dataCache.containsEntry(CacheName.LOCATION_BY_ID_CACHE, data.getId())) {
					LocationDataItem cachedDataItem = (LocationDataItem)
							dataCache.getEntry(CacheName.LOCATION_BY_ID_CACHE, data.getId());
					cachedDataItem.setStarCount(favoriteCount);
					cachedDataItem.setStarred(data.isStarred());
				}
			}
			
		} catch (JSONException e) {	e.printStackTrace(); }
	}

	@Override
	public boolean receiveWSCallback() {
		return !isFinishing();
	}
	
}
