package at.looksy.util;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import at.looksy.core.Constants;
import at.looksy.dataitem.HoursDataItem;
import at.looksy.dataitem.ImageCaptionDataItem;
import at.looksy.dataitem.LocationDataItem;
import at.looksy.manager.DimensionManager;
import at.looksy.manager.ImageManager;


public class DataItemUtil {

	public static List<ImageCaptionDataItem> decodeJSONTiles(JSONArray jsonTilesArray) {
		
		List<ImageCaptionDataItem> tileList = new ArrayList<ImageCaptionDataItem>();
		
		try {
			for (int j = 0; j < jsonTilesArray.length(); j++) {
				ImageCaptionDataItem dataItem = new ImageCaptionDataItem();
	
				JSONObject jsonTile = (JSONObject) jsonTilesArray.get(j);
				dataItem.setId(jsonTile.getString(Constants.JSON_FIELD_TILE_ID));
				dataItem.setCaption(jsonTile.getString(Constants.JSON_FIELD_TILE_CAPTION));
				dataItem.setStarred(jsonTile.getInt(Constants.JSON_FIELD_FAVORITE) >= 1);
				dataItem.setStarCount(jsonTile.getInt(Constants.JSON_FIELD_FAVORITE_COUNT));
				dataItem.setImagePath(jsonTile.getString(Constants.JSON_FIELD_TILE_IMAGE_PATH));
				dataItem.setCreationDate(Util.mysqlDateStringToDate((String) jsonTile.get(Constants.JSON_FIELD_TILE_CREATION_TIME)));
				
				int imageWidth = jsonTile.getInt(Constants.JSON_FIELD_IMAGE_WIDTH);
				int imageHeight = jsonTile.getInt(Constants.JSON_FIELD_IMAGE_HEIGHT);
				int scaledWidth = DimensionManager.getImageTileWidth();
				int scaledHeight = ImageManager.scaleToWidth(imageWidth, imageHeight, scaledWidth);
				
				dataItem.setImageScaledWidth(scaledWidth);
				dataItem.setImageScaledHeight(scaledHeight);
				
				tileList.add(dataItem);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return tileList;
	}
	
	public static List<LocationDataItem> decodeJSONLocations(JSONArray jsonLocationsArray) {
		
		List<LocationDataItem> dataItemList = new ArrayList<LocationDataItem>();
		try {
			for (int i = 0; i < jsonLocationsArray.length(); i++) {
				JSONObject location = jsonLocationsArray.getJSONObject(i);

				LocationDataItem locationDataItem = new LocationDataItem();
				locationDataItem.setId(location.getString(Constants.JSON_FIELD_LOCATION_ID));
				locationDataItem.setImagePath(location.getString(Constants.JSON_FIELD_LOCATION_IMAGE_PATH));
				locationDataItem.setName(location.getString(Constants.JSON_FIELD_LOCATION_NAME));
				locationDataItem.setDescription(location.getString(Constants.JSON_FIELD_LOCATION_DESCRIPTION));
				locationDataItem.setPhoneNumber(location.getString(Constants.JSON_FIELD_LOCATION_PHONE_NUMBER));
				locationDataItem.setAddress(location.getString(Constants.JSON_FIELD_LOCATION_ADDRESS));
				locationDataItem.setCity(location.getString(Constants.JSON_FIELD_LOCATION_CITY));
				locationDataItem.setState(location.getString(Constants.JSON_FIELD_STATE));
				locationDataItem.setZip(location.getString(Constants.JSON_FIELD_LOCATION_ZIP));
				locationDataItem.setWebSite(location.getString(Constants.JSON_FIELD_LOCATION_WEB_SITE));
				locationDataItem.setStarCount(location.getInt(Constants.JSON_FIELD_FAVORITE_COUNT));
				locationDataItem.setFacebookUsername(location.getString(Constants.JSON_FIELD_LOCATION_FACEBOOK_USER_NAME));
				locationDataItem.setTwitterUsername(location.getString(Constants.JSON_FIELD_LOCATION_TWITTER_USER_NAME));
				locationDataItem.setInstagramUsername(location.getString(Constants.JSON_FIELD_LOCATION_INSTAGRAM_USER_NAME));
				locationDataItem.associateBSSID(location.getString(Constants.JSON_FIELD_TRANSMITTER_BSSID));
				locationDataItem.setStarred(location.getInt(Constants.JSON_FIELD_FAVORITE) >= 1);
				
				int imageWidth = location.getInt(Constants.JSON_FIELD_IMAGE_WIDTH);
				int imageHeight = location.getInt(Constants.JSON_FIELD_IMAGE_HEIGHT);
				int scaledWidth = DimensionManager.getLocationTileWidth();
				int scaledHeight = ImageManager.scaleToWidth(imageWidth, imageHeight, scaledWidth);
				
				locationDataItem.setImageScaledWidth(scaledWidth);
				locationDataItem.setImageScaledHeight(scaledHeight);
				
				if (!location.getString(Constants.JSON_FIELD_TIMEZONE).equals(Constants.NULL_STR))
					locationDataItem.setTimezoneOffset(location.getString(Constants.JSON_FIELD_TIMEZONE));
				if (!location.getString(Constants.JSON_FIELD_LAT).equals(Constants.NULL_STR))
					locationDataItem.setLatitude(location.getDouble(Constants.JSON_FIELD_LAT));
				if (!location.getString(Constants.JSON_FIELD_LNG).equals(Constants.NULL_STR))
					locationDataItem.setLongitude(location.getDouble(Constants.JSON_FIELD_LNG));
				
				locationDataItem.setLocationHours(
						DataItemUtil.decodeJSONLocationHours(
								location.getJSONArray(
										Constants.JSON_FIELD_HOURS)));
				
				dataItemList.add(locationDataItem);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return dataItemList;
	}
	
	private static List<HoursDataItem> decodeJSONLocationHours(JSONArray jsonLocationHours) {
		
		List<HoursDataItem> hoursList = new ArrayList<HoursDataItem>();
		
		try {
			for (int i = 0; i < jsonLocationHours.length(); i++) {
				HoursDataItem dataItem = new HoursDataItem();
	
				JSONObject jsonHour = (JSONObject) jsonLocationHours.get(i);
				dataItem.setDay(jsonHour.getString(Constants.JSON_FIELD_DAY_NAME));
				
				String entry = jsonHour.getString(Constants.JSON_FIELD_OPEN_TIME);
				dataItem.setTimeOpen(entry.equals(Constants.NULL_STR) ? null : entry);
				
				entry = jsonHour.getString(Constants.JSON_FIELD_CLOSE_TIME);
				dataItem.setTimeClose(entry.equals(Constants.NULL_STR) ? null : entry);
				
				hoursList.add(dataItem);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return hoursList;
	}

}
