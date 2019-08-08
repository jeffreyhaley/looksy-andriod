package at.looksy.activity.graveyard;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import at.looksy.R;
import at.looksy.cache.DataCache;
import at.looksy.cache.DataCache.CacheName;
import at.looksy.core.Constants;
import at.looksy.dataitem.DataItem;
import at.looksy.dataitem.ImageCaptionDataItem;
import at.looksy.dataitem.LocationDataItem;
import at.looksy.manager.ActivityManager;
import at.looksy.manager.ImageCaptionTileManager;
import at.looksy.manager.ImageManager;
import at.looksy.manager.ImageManager.ImageQuality;
import at.looksy.manager.ImageManager.RoundingEffect;
import at.looksy.service.WebService;
import at.looksy.service.consumer.IWebServiceAsyncConsumer;
import at.looksy.service.data.WebServiceRequest;
import at.looksy.service.data.WebServiceResponse;
import at.looksy.service.data.WebServiceResponse.StatusCode;
import at.looksy.tile.ImageCaptionTile;
import at.looksy.util.DataItemUtil;
import at.looksy.util.UIButtonUtil;
import at.looksy.util.Util;

@Deprecated
public class TileExploreActivity extends Activity implements IWebServiceAsyncConsumer {

	public static int ENTITY_IMAGE_WIDTH;
	public static int BUCKET_MARGIN;
	public static int BUCKET_WIDTH;

	private ImageCaptionTileManager miniTileManager = null;
	
	private DataItem data;
	
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
							data.getId(), locationIdTileIdAssoc);

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
		if (dataCache.containsEntry(CacheName.LOCATION_ID_TILE_ID_CACHE, data.getId())) {
			@SuppressWarnings("unchecked")
			List<String> locationIdTileIdAssoc = (List<String>)
			dataCache.getEntry(CacheName.LOCATION_ID_TILE_ID_CACHE, data.getId());
			entityList = new ArrayList<ImageCaptionDataItem>();
			for (String tileId : locationIdTileIdAssoc) {
				entityList.add((ImageCaptionDataItem)dataCache.getEntry(
						CacheName.TILE_CACHE, tileId));
			}
			populateMiniTilesUI(entityList);

		} else {
			// not found - hit the web
			new WebService(this).getTilesForLocation(data.getId());
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
	protected void onResume() {
		// save our spot
//		ActivityManager.getInstance().pushM(getClass());
		super.onResume();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tile_explore);

		initLayoutSizes();
		initActivity();

		populateMiniTiles();
	}

	protected void initActivity() {
		
		Bundle bundle = getIntent().getExtras();
		data = (DataItem) bundle.getSerializable(Constants.BUNDLE_DATA);

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
				boolean newStarStatus = data.isStarred() ? false : true;

				// update local data structure
				data.setStarred(newStarStatus);

				// update the local panel UI
				updateStarUI(newStarStatus);

				// update the web
				new WebService(TileExploreActivity.this)
					.updateLocationFavoriteStatus(data.getId(), newStarStatus);
			}
		});

		initLayoutSizes();

		// init the managers
		if (miniTileManager == null)
			miniTileManager = new ImageCaptionTileManager(this);

		// init progress bar
		findViewById(R.id.progressText).setVisibility(View.GONE);

	}
	
	public void updateStarUI(boolean star) {
		TextView btnKeepView = (TextView) findViewById(R.id.btnFollow);
		if (star) {
			UIButtonUtil.buttonOn(btnKeepView, Constants.BTN_UNFOLLOW);
		} else {
			UIButtonUtil.buttonOff(btnKeepView, Constants.BTN_FOLLOW);
		}
	}

	private void initLayoutSizes()
	{
		// logo/pic width
		ENTITY_IMAGE_WIDTH = Util.dp2px(getApplicationContext(), 50);

		// bucket and margin sizes
		Context context = getApplicationContext();
		Display display = getWindowManager().getDefaultDisplay();
		BUCKET_MARGIN = Util.dp2px(context, 16);
		int width = display.getWidth() - (BUCKET_MARGIN * 3);		
		BUCKET_WIDTH = width / 2;
	}
	
	@Override
	public void onBackPressed()
	{
		endView();
	}
	
	protected void endView()
	{
		Class<?> callerClass = ActivityManager.getInstance().pop1();
		while (callerClass.getName().equals(EntityConnectActivity.class.getName()) ||
				callerClass.getName().equals(EntityExploreActivity.class.getName()) ||
				callerClass.getName().equals(EntityLearnActivity.class.getName())) {
			callerClass = ActivityManager.getInstance().pop1();
		}
		
		Bundle bundle = new Bundle();
		bundle.putSerializable(Constants.BUNDLE_DATA, data);

		Intent intent = new Intent(this, callerClass);
		intent.putExtras(bundle);
		startActivity(intent);

		finish();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

}
