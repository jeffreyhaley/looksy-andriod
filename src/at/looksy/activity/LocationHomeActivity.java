package at.looksy.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import at.looksy.R;
import at.looksy.activity.base.LocationBaseActivity;
import at.looksy.cache.DataCache;
import at.looksy.cache.DataCache.CacheName;
import at.looksy.core.Constants;
import at.looksy.dataitem.ImageCaptionDataItem;
import at.looksy.manager.ImageCaptionTileManager;
import at.looksy.service.WebService;
import at.looksy.service.consumer.IWebServiceAsyncConsumer;
import at.looksy.service.data.WebServiceRequest;
import at.looksy.service.data.WebServiceResponse;
import at.looksy.service.data.WebServiceResponse.StatusCode;
import at.looksy.tile.ImageCaptionTile;
import at.looksy.util.DataItemUtil;
import at.looksy.util.Util;

public class LocationHomeActivity extends LocationBaseActivity implements IWebServiceAsyncConsumer {
	
	private ImageCaptionTileManager tileManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location_home);

		initActivity();
		populateTiles();
	}

	@Override
	protected void initActivity() 
	{	
		super.initActivity();
		
		// tile manager
		if (tileManager == null)
			tileManager = new ImageCaptionTileManager(this);

		// closing in
		if (getData().getLocationHours() == null || 
				getData().getLocationHours().size() == 0 || 
				getData().getTimezoneOffset() == null) 
		{
			findViewById(R.id.sectionHours).setVisibility(View.GONE);

		} else {
			String hours[] = Util.getBusinessHoursString(getData().getLocationHours(), getData().getTimezoneOffset());
			((TextView) findViewById(R.id.hoursField)).setText(hours[0]);
			((TextView) findViewById(R.id.hoursContent)).setText(hours[1]);
		}

		// more info button
		{
			final LinearLayout moreButtonLayout = (LinearLayout) findViewById(R.id.sectionMore);
			moreButtonLayout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Bundle bundle = new Bundle();
					bundle.putSerializable(Constants.BUNDLE_DATA, getData());
					Intent intent = new Intent(LocationHomeActivity.this, LocationMoreInfoActivity.class);
					intent.putExtras(bundle);
					startActivity(intent);
					overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
					
				}
			});
		}
	}

	@Override
	public void consumeWSExchange(WebServiceRequest wsRequest,
			WebServiceResponse wsResponse) {
		
		super.consumeWSExchange(wsRequest, wsResponse);

		if (!receiveWSCallback())
			return;

		DataCache dataCache = DataCache.getInstance();

		if (wsResponse.getStatus() == StatusCode.OK) {
			try {
				if (wsRequest.getRequestAction().equals(Constants.JSON_METHOD_GET_TILES_FOR_LOCATION)) {
					List<ImageCaptionDataItem> entityList = 
							DataItemUtil.decodeJSONTiles(
									wsResponse.getJSONArray((Constants.WEB_SERVICE_DATA)));
					List<String> locationIdTileIdAssoc = new ArrayList<String>();
					for (ImageCaptionDataItem item : entityList) {
						// add tile item to cache
						dataCache.putEntry(CacheName.TILE_CACHE, item.getId(), item);

						// add to association list
						locationIdTileIdAssoc.add(item.getId());
					}

					// save the list
					dataCache.putEntry(CacheName.LOCATION_ID_TILE_ID_CACHE, 
							getData().getId(), locationIdTileIdAssoc);

					// update the ui
					populateTilesUI(entityList);
				}
				
			} catch (JSONException e) {	e.printStackTrace(); }
		}
	}

	private void populateTiles() {

		DataCache dataCache = DataCache.getInstance();
		List<ImageCaptionDataItem> entityList = null;

		// check the cache first
		if (dataCache.containsEntry(CacheName.LOCATION_ID_TILE_ID_CACHE, getData().getId())) {
			@SuppressWarnings("unchecked")
			List<String> locationIdTileIdAssoc = (List<String>)
			dataCache.getEntry(CacheName.LOCATION_ID_TILE_ID_CACHE, getData().getId());
			entityList = new ArrayList<ImageCaptionDataItem>();
			for (String tileId : locationIdTileIdAssoc) {
				entityList.add((ImageCaptionDataItem)dataCache.getEntry(
						CacheName.TILE_CACHE, tileId));
			}
			populateTilesUI(entityList);

		} else {
			// not found - hit the web
			new WebService(this).getTilesForLocation(getData().getId());
		}
	}

	private void populateTilesUI(List<ImageCaptionDataItem> tileList) {

		if (tileList != null) {

			findViewById(R.id.progressBar).setVisibility(View.GONE);
			findViewById(R.id.progressText).setVisibility(View.GONE);

			for (ImageCaptionDataItem data : tileList)
				tileManager.addTile(new ImageCaptionTile(this, data, false));
		}
	}

}
