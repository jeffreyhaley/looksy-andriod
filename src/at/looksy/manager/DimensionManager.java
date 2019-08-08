package at.looksy.manager;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Display;
import at.looksy.BuildConfig;
import at.looksy.core.Constants;
import at.looksy.util.Util;

public class DimensionManager {
	
	private static final int DP_LOCATION_TILE_MARGIN = 13;
	private static int locationTileMargin;
	private static int locationTileWidth;
	
	private static final int DP_IMAGE_TILE_MARGIN = 13;
	private static int imageTileMargin;
	private static int imageTileWidth;
	
	private static final int DP_ENTITY_IMAGE_WIDTH = 50;
	private static int entityImageWidth;
	
	private static int entityHeadingWidth;
	
	private static DimensionManager instance = null; 
	private Activity activity = null;
	
	private DimensionManager(Activity activity) {
		this.activity = activity;
		calculateSizes();
	}
	
	public static void initialize(Activity activity) {
		instance = new DimensionManager(activity);
	}
	
	private static void warnIfNotInitialized() {
		if (instance == null)
			Log.e(Constants.DEBUG_TAG, "DimensionManager has not been initalized.");
	}

	@SuppressWarnings("deprecation")
	private void calculateSizes() {

		// grab screen size
		Context context = activity.getApplicationContext();
		Display display = activity.getWindowManager().getDefaultDisplay();
		
		// location tile
		locationTileMargin = Util.dp2px(context, DP_LOCATION_TILE_MARGIN);
		locationTileWidth = (display.getWidth() - (locationTileMargin * 2));
		
		// image tile
		imageTileMargin = Util.dp2px(context, DP_IMAGE_TILE_MARGIN);
		imageTileWidth = (display.getWidth() - (imageTileMargin * 3)) / 2;
		
		// entity image width
		entityImageWidth = Util.dp2px(context, DP_ENTITY_IMAGE_WIDTH);
		
		// entity heading with
		int widthPx = activity.getApplicationContext().getResources().getDisplayMetrics().widthPixels;
		int widthDp = Util.px2dp(context, widthPx);
		entityHeadingWidth = Util.dp2px(context, ((widthDp * 53) / 100));
		
		// location heading maximum number of characters
		if (BuildConfig.DEBUG)
			Log.d(Constants.DEBUG_TAG, ">>>>> width dp=" + widthDp + " px=" + widthPx);
		
	}

	public static int getLocationTileMargin() {
		warnIfNotInitialized();
		return locationTileMargin;
	}

	public static int getLocationTileWidth() {
		warnIfNotInitialized();
		return locationTileWidth;
	}

	public static int getImageTileMargin() {
		warnIfNotInitialized();
		return imageTileMargin;
	}

	public static int getImageTileWidth() {
		warnIfNotInitialized();
		return imageTileWidth;
	}

	public static int getEntityImageWidth() {
		warnIfNotInitialized();
		return entityImageWidth;
	}
	
	public static int getEntityHeadingWidth() {
		warnIfNotInitialized();
		return entityHeadingWidth;
	}
	
}
