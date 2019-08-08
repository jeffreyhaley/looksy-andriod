package at.looksy.core;

public class Constants {
	
	public static final String DEBUG_TAG = "debug";
	public static final String EMPTY_STR = "";
	public static final String NULL_STR = "null";
	
	/**
	 * Web service constants
	 */
//	public static final String WEB_SERVICE_BASE_URI = "http://mirans-mbp.home/";
	public static final String WEB_SERVICE_BASE_URI = "http://looksy.at/";
	public static final String WEB_SERVICE_HEADER_CONTENT_TYPE = "Content-Type";
	public static final String WEB_SERVICE_HEADER_FORM = "application/x-www-form-urlencoded";
	public static final String WEB_SERVICE_JSON_REQUEST = "req";
	public static final String WEB_SERVICE_MODULE = "module";
	public static final String WEB_SERVICE_ACTION = "action";
	public static final String WEB_SERVICE_DATA = "data";
	public static final String WEB_SERVICE_IMAGE = "image";
	public static final String WEB_SERVICE_MODULE_API = "api";
	
	/**
	 * Bundle constants
	 */
	public static final String BUNDLE_DATA = "bundle.data";
	public static final String BUNDLE_CA2LLING_ACTIVITY = "bundle.calling_activity";
	public static final String BUNDLE_STAR_CHANGE_LIST = "bundle.star_change_list";
	public static final String BUNDLE_LOCATION_ID = "bundle.location_id";
	
	/**
	 * JSON fields
	 */
	public static final String JSON_FIELD_DEVICE_UUID = "DeviceUUID";
	public static final String JSON_FIELD_LOCATION_ID = "LocationId";
	public static final String JSON_FIELD_TILE_ID = "TileId";
	public static final String JSON_FIELD_TILE_CAPTION = "TileCaption";
	public static final String JSON_FIELD_LOCATION_IMAGE_PATH = "ImageName";
	public static final String JSON_FIELD_TILE_IMAGE_PATH = "ImageName";
	public static final String JSON_FIELD_TILE_CREATION_TIME = "TileCreationTime";
	public static final String JSON_FIELD_BSSID_LIST = "BssidList";
	public static final String JSON_FIELD_LOCATION_NAME = "LocationName";
	public static final String JSON_FIELD_LOCATION_COUNT = "LocationCount";
	public static final String JSON_FIELD_LOCATION_DESCRIPTION = "LocationDescription";
	public static final String JSON_FIELD_LOCATION_PHONE_NUMBER = "LocationPhoneNumber";
	public static final String JSON_FIELD_LOCATION_ADDRESS = "LocationAddress";
	public static final String JSON_FIELD_LOCATION_CITY = "LocationCity";
	public static final String JSON_FIELD_STATE = "USStateAbbr";
	public static final String JSON_FIELD_LOCATION_ZIP = "LocationZip";
	public static final String JSON_FIELD_LOCATION_TWITTER_USER_NAME = "LocationTwitterUserName";
	public static final String JSON_FIELD_LOCATION_FACEBOOK_USER_NAME = "LocationFacebookUserName";
	public static final String JSON_FIELD_LOCATION_INSTAGRAM_USER_NAME = "LocationInstagramUserName";
	public static final String JSON_FIELD_TRANSMITTER_BSSID = "TransmitterBSSID";
	public static final String JSON_FIELD_FAVORITE_COUNT = "FavoriteCount";
	public static final String JSON_FIELD_FAVORITE = "Favorite";
	public static final String JSON_FIELD_DEVICE_TYPE_ID = "DeviceTypeId";
	public static final String JSON_FIELD_LOCATION_WEB_SITE = "LocationWebSite";
	public static final String JSON_FIELD_IMAGE_WIDTH = "ImageOrigWidth";
	public static final String JSON_FIELD_IMAGE_HEIGHT = "ImageOrigHeight";
	public static final String JSON_FIELD_DAY_NAME = "DayName";
	public static final String JSON_FIELD_OPEN_TIME = "OpenTime";
	public static final String JSON_FIELD_CLOSE_TIME = "CloseTime";
	public static final String JSON_FIELD_TIMEZONE = "LocationTzOffset";
	public static final String JSON_FIELD_LAT = "LocationLat";
	public static final String JSON_FIELD_LNG = "LocationLng";
	public static final String JSON_FIELD_HOURS = "Hours";
	
	/**
	 * Method names
	 */
	public static final String JSON_METHOD_GET_LOCATIONS_FOR_BSSIDS = "getLocationsForBSSIDs";
	public static final String JSON_METHOD_GET_LOCATION_COUNT_FOR_BSSIDS = "getLocationCountForBSSIDs";
	public static final String JSON_METHOD_GET_FAVORITE_LOCATIONS_FOR_DEVICE = "getFavoriteLocationsForDevice";
	public static final String JSON_METHOD_GET_TILES_FOR_LOCATION = "getTilesForLocation";
	public static final String JSON_METHOD_GET_TILE_FAVORITE_COUNT = "getTileFavoriteCount";
	public static final String JSON_METHOD_UPDATE_LOCATION_FAVORITE_STATUS = "updateLocationFavoriteStatus";
	public static final String JSON_METHOD_GET_LOCATION_FAVORITE_COUNT = "getLocationFavoriteCount";
	public static final String JSON_METHOD_IS_LOCATION_FAVORITE = "isLocationFavorite";
	public static final String JSON_METHOD_UPDATE_TILE_FAVORITE_STATUS = "updateTileFavoriteStatus";
	public static final String JSON_METHOD_IS_TILE_FAVORITE = "isTileFavorite";
	public static final String JSON_METHOD_REGISTER_DEVICE = "registerDevice";
	public static final String JSON_METHOD_REGISTER_USER_TILE_VISIT = "registerUserTileVisit";
	public static final String JSON_METHOD_REGISTER_USER_LOCATION_VISIT = "registerUserLocationVisit";
	public static final String JSON_METHOD_REGISTER_USER_LOCATION_PASS_BY = "registerUserLocationPassBy";
	public static final String JSON_METHOD_GET_LOCATIONS_FOR_SEARCH_TERM = "getLocationsForSearchTerm";
	
	/**
	 * Image size folders
	 */
	public static final String IMG_SMALL = "small";
	public static final String IMG_MEDIUM = "medium";
	public static final String IMG_LARGE = "large";
	
	/**
	 * JSON Status
	 */
	public static final String JSON_STATUS = "status";
	public static final String JSON_STATUS_OK = "success";

	/**
	 * Public URLs
	 */
	public static final String APP_BASE_URL = "http://looksy.at/";
	public static final String APP_MINI_TILE_SUFFIX = "t/";
	
	/**
	 * Notifications
	 */
	public static final int NOTIFICATION_ID = 1;
	
	/**
	 * Custom broadcast actions
	 */
	public static final String BROADCAST_NOTIFICATION_CANCELLED = "app.action.notification_deleted";
	
	/**
	 * Timers
	 */
	public static final int LOCATION_TILE_TIME_TO_LIVE = 30; // in mins
	public static final int WIFI_SCAN_INTERVAL = 60; // in secs
	public static final int SHOW_NOTIFICATION_TIME_SPREAD = 24; // in hours
	public static final int VIBRATE_NOTIFICATION_TIME_SPREAD = 2; // in hours
	
	/**
	 * Display pagers
	 */
	public static final int REEL_MAX_ITEMS = 4;
	
	/**
	 * Button labels
	 */
	public static final String BTN_FOLLOW = "Follow";
	public static final String BTN_FOLLOWED = "Followed";
	public static final String BTN_UNFOLLOW = "Unfollow";
	
	/**
	 * Preferences
	 */
	public static final String PREF_PROCESS_STATE = "pref.process_state";
	public static final String PREF_NOTIF_LAST_DISMISSED = "pref.notif_last_dismissed";
}
