package at.looksy.activity.graveyard;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import at.looksy.BuildConfig;
import at.looksy.R;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import at.looksy.activity.base.EntityBaseActivity;
import at.looksy.cache.DataCache;
import at.looksy.cache.DataCache.CacheName;
import at.looksy.core.Constants;
import at.looksy.dataitem.LocationDataItem;
import at.looksy.factory.ViewFactory;
import at.looksy.listener.OnSwipeTouchListener;
import at.looksy.manager.ActivityManager;
import at.looksy.service.WebService;
import at.looksy.service.consumer.IWebServiceAsyncConsumer;
import at.looksy.service.data.WebServiceRequest;
import at.looksy.service.data.WebServiceResponse;
import at.looksy.service.data.WebServiceResponse.StatusCode;
import at.looksy.util.StringHelper;

@Deprecated
public class EntityConnectActivity extends EntityBaseActivity implements IWebServiceAsyncConsumer {

	private LinearLayout followButtonGroup = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_entity_connect);
		
		if (BuildConfig.DEBUG) {
			Log.d(Constants.DEBUG_TAG, "EntityConnectActivity onCreate() called");
		}
		
		initActivity();
	}
	
	@Override
	protected void onResume()
	{
		if (BuildConfig.DEBUG) {
			Log.d(Constants.DEBUG_TAG, "EntityConnectActivity onResume() called");
		}
		
		super.onResume();
		if (getData() != null) {
			updateFollowedTextUI();
		}
		
		// save our spot
//		ActivityManager.getInstance().pushM(getClass());
	}
	
	public void updateFollowedTextUI()
	{
		// update text
		String timesSuffix = getData().getStarCount() == 1 ? "person" : "people";
		TextView detailText = (TextView) followButtonGroup.findViewById(R.id.detail);
		detailText.setText("by " + getData().getStarCount() + " " + timesSuffix);
	}
	
	public void updateFollowedWS()
	{
		new WebService(this).updateLocationFavoriteStatus(
				getData().getId(), getData().isStarred());
	}
		
	@Override
	protected void initActivity()
	{
		super.initActivity();

		// figure out type for all future calls
		final LocationDataItem entityDataItem = (LocationDataItem) getData();
		
		ViewFactory layoutFactory = new ViewFactory(this);
		
		LinearLayout connectStarLayout = (LinearLayout) findViewById(R.id.connectStarBadges);
		LinearLayout connectMainLayout = (LinearLayout) findViewById(R.id.connectPhysicalBadges);
		LinearLayout connectSocialLayout = (LinearLayout) findViewById(R.id.connectSocialBadges);
		
		// follow button
		{
			followButtonGroup = layoutFactory.getConnectBadge();
			updateFollowedTextUI();
			followButtonGroup.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (entityDataItem.isStarred()) {
						entityDataItem.setStarred(false);
						((EntityProfileActivity)getParent()).updateStarUI(false);
						
					} else {
						entityDataItem.setStarred(true);
						((EntityProfileActivity)getParent()).updateStarUI(true);
					}
					
					updateFollowedWS();
					updateFollowedTextUI();
				}
			});
			
			TextView primaryText = (TextView) followButtonGroup.findViewById(R.id.primaryPurpose);
			primaryText.setText("Followed");
			
			connectStarLayout.addView(followButtonGroup);
		}	
		
		// call button
		if (entityDataItem != null && entityDataItem.getPhoneNumber() != null) {
			LinearLayout callButtonGroup = layoutFactory.getConnectBadge();
			final String phoneNumber = entityDataItem.getPhoneNumber().trim();
			callButtonGroup.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String number = "tel:" + phoneNumber;
					Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(number));
					startActivity(callIntent);				
				}
			});
			ImageView image = (ImageView) callButtonGroup.findViewById(R.id.icon);
			image.setImageResource(R.drawable.ico_call);
			
			TextView primaryText = (TextView) callButtonGroup.findViewById(R.id.primaryPurpose);
			primaryText.setText("Call");
			
			TextView detailText = (TextView) callButtonGroup.findViewById(R.id.detail);
			detailText.setText(StringHelper.prettyPhoneNumberUS(phoneNumber));
			
			if (connectMainLayout.getChildCount() != 0)
				connectMainLayout.addView(layoutFactory.getHorizontalLine());
			connectMainLayout.addView(callButtonGroup);
		}

		// navigate button
		if (entityDataItem != null && 
				entityDataItem.getAddress() != null && 
				entityDataItem.getCity() != null && 
				entityDataItem.getState() != null) 
		{
			LinearLayout navButtonGroup = layoutFactory.getConnectBadge();
			final String fullDecodedAddr = entityDataItem.getAddress() 
					+ "," + entityDataItem.getCity() 
					+ "," + entityDataItem.getState();
			navButtonGroup.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String fullEncodedAddr = null;
					try {
						fullEncodedAddr = URLEncoder.encode(fullDecodedAddr, "UTF-8");

					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					Intent navIntent = new Intent(Intent.ACTION_VIEW, 
							Uri.parse("google.navigation:q=" + fullEncodedAddr));
					startActivity(navIntent);				
				}
			});
			
			ImageView image = (ImageView) navButtonGroup.findViewById(R.id.icon);
			image.setImageResource(R.drawable.ico_nav);
			
			TextView primaryText = (TextView) navButtonGroup.findViewById(R.id.primaryPurpose);
			primaryText.setText("Navigate");
			
			TextView detailText = (TextView) navButtonGroup.findViewById(R.id.detail);
			String formattedAddress = entityDataItem.getAddress() 
					+ "\n" + entityDataItem.getCity() 
					+ ", " + entityDataItem.getState();
			detailText.setText(formattedAddress);
			
			if (connectMainLayout.getChildCount() != 0)
				connectMainLayout.addView(layoutFactory.getHorizontalLine());
			connectMainLayout.addView(navButtonGroup);
		}

		// web button
		if (entityDataItem != null && entityDataItem.getWebSite() != null) {
			LinearLayout webButtonGroup = layoutFactory.getConnectBadge();
			final String webSite = entityDataItem.getWebSite();
			webButtonGroup.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
							Uri.parse(webSite));
					startActivity(browserIntent);				
				}
			});
			
			ImageView image = (ImageView) webButtonGroup.findViewById(R.id.icon);
			image.setImageResource(R.drawable.ico_web);
			
			TextView primaryText = (TextView) webButtonGroup.findViewById(R.id.primaryPurpose);
			primaryText.setText("Web");
			
			TextView detailText = (TextView) webButtonGroup.findViewById(R.id.detail);
			detailText.setText(StringHelper.prettyUrl(webSite));
			
			if (connectMainLayout.getChildCount() != 0)
				connectMainLayout.addView(layoutFactory.getHorizontalLine());
			connectMainLayout.addView(webButtonGroup);
		}
		
		// fb button
		if (entityDataItem != null && entityDataItem.getFacebookUsername() != null) {
			LinearLayout facebookButtonGroup = layoutFactory.getConnectBadge();
			final String fbUsername = entityDataItem.getFacebookUsername();
			facebookButtonGroup.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
							Uri.parse("https://www.facebook.com/" + fbUsername));
					startActivity(browserIntent);				
				}
			});
			
			ImageView image = (ImageView) facebookButtonGroup.findViewById(R.id.icon);
			image.setImageResource(R.drawable.ico_fb);
			
			TextView primaryText = (TextView) facebookButtonGroup.findViewById(R.id.primaryPurpose);
			primaryText.setText("Facebook");
			
			TextView detailText = (TextView) facebookButtonGroup.findViewById(R.id.detail);
			detailText.setText("fb.com/" + fbUsername);
			
			if (connectSocialLayout.getChildCount() != 0)
				connectSocialLayout.addView(layoutFactory.getHorizontalLine());
			connectSocialLayout.addView(facebookButtonGroup);
		}
		
		// twitter button
		if (entityDataItem != null && entityDataItem.getTwitterUsername() != null) {
			LinearLayout twitterButtonGroup = layoutFactory.getConnectBadge();
			final String twitterUsername = entityDataItem.getTwitterUsername();
			twitterButtonGroup.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
							Uri.parse("https://www.twitter.com/" + twitterUsername));
					startActivity(browserIntent);				
				}
			});
			
			ImageView image = (ImageView) twitterButtonGroup.findViewById(R.id.icon);
			image.setImageResource(R.drawable.ico_twitter);
			
			TextView primaryText = (TextView) twitterButtonGroup.findViewById(R.id.primaryPurpose);
			primaryText.setText("Twitter");
			
			TextView detailText = (TextView) twitterButtonGroup.findViewById(R.id.detail);
			detailText.setText("twitter.com/" + twitterUsername);
			
			if (connectSocialLayout.getChildCount() != 0)
				connectSocialLayout.addView(layoutFactory.getHorizontalLine());
			connectSocialLayout.addView(twitterButtonGroup);
		}
		
		// instagram button
		if (entityDataItem != null && entityDataItem.getInstagramUsername() != null) {
			LinearLayout instagramButtonGroup = layoutFactory.getConnectBadge();
			final String instagramUsername = entityDataItem.getInstagramUsername();
			instagramButtonGroup.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
							Uri.parse("http://www.instagram.com/" + instagramUsername));
					startActivity(browserIntent);				
				}
			});
			
			ImageView image = (ImageView) instagramButtonGroup.findViewById(R.id.icon);
			image.setImageResource(R.drawable.ico_instagram);
			
			TextView primaryText = (TextView) instagramButtonGroup.findViewById(R.id.primaryPurpose);
			primaryText.setText("Instagram");
			
			TextView detailText = (TextView) instagramButtonGroup.findViewById(R.id.detail);
			detailText.setText("instagram.com/" + instagramUsername);
			
			if (connectSocialLayout.getChildCount() != 0)
				connectSocialLayout.addView(layoutFactory.getHorizontalLine());
			connectSocialLayout.addView(instagramButtonGroup);
		}
		
		// back swipe listener
		ScrollView root = (ScrollView) findViewById(R.id.viewScroller);
		root.setOnTouchListener(new OnSwipeTouchListener() {
			public void onSwipeRight() {
				endView();
			}
		});
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
				if (method.equals(Constants.JSON_METHOD_UPDATE_LOCATION_FAVORITE_STATUS)) {
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
					
					// update ui
					updateFollowedTextUI();
				}
			}
		} catch (JSONException e) {	e.printStackTrace(); }
		
		
	}

	@Override
	public boolean receiveWSCallback() {
		return !isFinishing();
	}

}
