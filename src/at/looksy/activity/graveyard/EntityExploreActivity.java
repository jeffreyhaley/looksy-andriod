package at.looksy.activity.graveyard;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import at.looksy.R;

import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.TextView;
import at.looksy.activity.base.EntityBaseActivity;
import at.looksy.cache.DataCache;
import at.looksy.cache.DataCache.CacheName;
import at.looksy.core.Constants;
import at.looksy.dataitem.ImageCaptionDataItem;
import at.looksy.manager.ActivityManager;
import at.looksy.manager.ImageCaptionTileManager;
import at.looksy.service.WebService;
import at.looksy.service.consumer.IWebServiceAsyncConsumer;
import at.looksy.service.data.WebServiceRequest;
import at.looksy.service.data.WebServiceResponse;
import at.looksy.service.data.WebServiceResponse.StatusCode;
import at.looksy.tile.ImageCaptionTile;
import at.looksy.util.DataItemUtil;
import at.looksy.util.Util;

@Deprecated
public class EntityExploreActivity extends EntityBaseActivity implements IWebServiceAsyncConsumer {

	public static int ENTITY_IMAGE_WIDTH;
	public static int BUCKET_MARGIN;
	public static int BUCKET_WIDTH;

	private ImageCaptionTileManager miniTileManager = null;

	@Override
	public void consumeWSExchange(WebServiceRequest wsRequest,
			WebServiceResponse wsResponse) {

		if (!receiveWSCallback())
			return;

		DataCache dataCache = DataCache.getInstance();

		if (wsRequest.getRequestAction().equals(Constants.JSON_METHOD_GET_TILES_FOR_LOCATION)) {
			List<ImageCaptionDataItem> entityList = null;
			try {
				if (wsResponse.getStatus() == StatusCode.OK) {
					entityList = 
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
					populateMiniTilesUI(entityList);
				}
			} catch (JSONException e) {	e.printStackTrace(); }

		}
	}

	@Override
	public boolean receiveWSCallback() {
		return !isFinishing();
	}

	private void populateMiniTiles() {

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
			populateMiniTilesUI(entityList);

		} else {
			// not found - hit the web
			new WebService(this).getTilesForLocation(getData().getId());
		}

	}

	private void populateMiniTilesUI(List<ImageCaptionDataItem> tileList) {

		if (tileList != null) {
			if (tileList.isEmpty()) {
				findViewById(R.id.progressText).setVisibility(View.VISIBLE);
				((TextView)findViewById(R.id.progressText))
					.setText("No tiles have been created, yet");
				findViewById(R.id.progressBar).setVisibility(View.GONE);
			} else {

				findViewById(R.id.progressBar).setVisibility(View.GONE);
				findViewById(R.id.progressText).setVisibility(View.GONE);
			}

			for (ImageCaptionDataItem data : tileList)
				miniTileManager.addTile(new ImageCaptionTile(this, data, false));
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_entity_explore);

		initActivity();

		populateMiniTiles();
	}
	
	@Override
	protected void onResume() {
		// save our spot
//		ActivityManager.getInstance().pushM(getClass());
		super.onResume();
	}

	@Override
	protected void initActivity() {
		super.initActivity();
		
		initLayoutSizes();

		// init the managers
		if (miniTileManager == null)
			miniTileManager = new ImageCaptionTileManager(this);

		// init progress bar
		findViewById(R.id.progressText).setVisibility(View.GONE);

	}

	private void initLayoutSizes()
	{
		// logo/pic width
		ENTITY_IMAGE_WIDTH = Util.dp2px(getApplicationContext(), 50);

		// bucket and margin sizes
		Context context = getApplicationContext();
		Display display = getWindowManager().getDefaultDisplay();
		BUCKET_MARGIN = Util.dp2px(context, 16);
		@SuppressWarnings("deprecation")
		int width = display.getWidth() - (BUCKET_MARGIN * 3);		
		BUCKET_WIDTH = width / 2;
	}

}
