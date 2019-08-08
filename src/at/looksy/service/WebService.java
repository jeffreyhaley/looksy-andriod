package at.looksy.service;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import at.looksy.annotate.AsyncRequest;
import at.looksy.core.Constants;
import at.looksy.manager.DeviceManager;
import at.looksy.service.consumer.IWebServiceAsyncConsumer;
import at.looksy.service.data.WebServiceRequest;
import at.looksy.service.task.WebServiceConnectionTask;


public class WebService {

	private IWebServiceAsyncConsumer consumer;

	public WebService(IWebServiceAsyncConsumer consumer) {
		this.consumer = consumer;
	}

	private static void preamble(JSONObject data)
	{
		try {
			// always send out deviceId
			data.put(Constants.JSON_FIELD_DEVICE_UUID, DeviceManager.getDeviceId());

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private static WebServiceRequest assembleRequest(String action, JSONObject jsonData) {

		WebServiceRequest wsRequest = new WebServiceRequest();
		try {
			wsRequest.put(Constants.WEB_SERVICE_ACTION, action);
			wsRequest.put(Constants.WEB_SERVICE_DATA, jsonData);

			wsRequest.setRequestAction(action);
			wsRequest.setRequestData(jsonData);

		} catch (JSONException e) { e.printStackTrace(); }

		return wsRequest;
	}

	/**
	 * Send an asynchronous request to the web service.
	 * @param action
	 * @param jsonRequestData
	 */
	private static void sendAsyncRequestNoCallback(String action, JSONObject jsonRequestData)
	{
		preamble(jsonRequestData);
		WebServiceRequest wsRequest = assembleRequest(action, jsonRequestData);
		new WebServiceConnectionTask().execute(wsRequest);
	}

	private void sendAsyncRequest(String action, JSONObject jsonRequestData)
	{
		preamble(jsonRequestData);
		WebServiceRequest wsRequest = assembleRequest(action, jsonRequestData);
		WebServiceConnectionTask task = new WebServiceConnectionTask(consumer);
		task.execute((WebServiceRequest)wsRequest);
	}

	/**
	 * Get a list of registered entities for the provided BSSID list.
	 * @param bssidList List of BSSIDs
	 * @return map containing the BSSID-to-data item pairings
	 */
	@AsyncRequest
	public void getLocationsForBSSIDs(List<String> bssidList)
	{
		JSONObject jsonRequestData = new JSONObject();
		JSONArray jsonBssidRequestList = new JSONArray();

		for (String bssid : bssidList)
			jsonBssidRequestList.put(bssid);

		try {
			jsonRequestData.put(Constants.JSON_FIELD_BSSID_LIST, jsonBssidRequestList.toString());

			sendAsyncRequest(
					Constants.JSON_METHOD_GET_LOCATIONS_FOR_BSSIDS, 
					jsonRequestData);

		} catch (JSONException e) {	e.printStackTrace(); }
	}
	
	/**
	 * Find a location by name.
	 * @param searchTerm Name of location
	 */
	public void findLocations(String searchTerm) 
	{
		JSONObject jsonRequestData = new JSONObject();

		try {
			jsonRequestData.put(Constants.JSON_FIELD_LOCATION_NAME, searchTerm);

		} catch (JSONException e) {	e.printStackTrace(); }

		sendAsyncRequest(
				Constants.JSON_METHOD_GET_LOCATIONS_FOR_SEARCH_TERM, 
				jsonRequestData);
	}

	/**
	 * Get a list of registered entities for the provided BSSID list.
	 * @param bssidList List of BSSIDs
	 * @return map containing the BSSID-to-data item pairings
	 */
	@AsyncRequest
	public void getLocationCountForBSSIDs(List<String> bssidList)
	{
		JSONObject jsonRequestData = new JSONObject();
		JSONArray jsonBssidRequestList = new JSONArray();
		
		if (bssidList.isEmpty())
			return;

		for (String bssid : bssidList)
			jsonBssidRequestList.put(bssid);

		try {
			jsonRequestData.put(Constants.JSON_FIELD_BSSID_LIST, 
					jsonBssidRequestList.toString());

			sendAsyncRequest(
					Constants.JSON_METHOD_GET_LOCATION_COUNT_FOR_BSSIDS, 
					jsonRequestData);

		} catch (JSONException e) {	e.printStackTrace(); }
	}

	/**
	 * Get a list of starred tiles for the registered user.
	 * @param deviceId ID of registered user
	 * @return map containing the BSSID-to-data item pairings starred by user
	 */
	@AsyncRequest
	public void getFavoriteLocationsForDevice()
	{
		sendAsyncRequest(
				Constants.JSON_METHOD_GET_FAVORITE_LOCATIONS_FOR_DEVICE, 
				new JSONObject());
	}

	/**
	 * Update a location's star-ness.
	 * @param locationId object's ID
	 * @param starred true to star, else false
	 * @return true if starring ok, else false
	 */
	@AsyncRequest
	public void updateLocationFavoriteStatus(String locationId, boolean starred)
	{
		JSONObject jsonRequestData = new JSONObject();

		try {
			jsonRequestData.put(Constants.JSON_FIELD_LOCATION_ID, locationId);
			jsonRequestData.put(Constants.JSON_FIELD_FAVORITE, starred);

		} catch (JSONException e) {	e.printStackTrace(); }
		
		sendAsyncRequest(
				Constants.JSON_METHOD_UPDATE_LOCATION_FAVORITE_STATUS, 
				jsonRequestData);
	}

	/**
	 * Check if a location is starred
	 * @param locationId location ID
	 * @return true if starred, else false
	 */
	@AsyncRequest
	public void isLocationFavorite(String locationId) {
		JSONObject jsonRequestData = new JSONObject();

		try {
			jsonRequestData.put(Constants.JSON_FIELD_LOCATION_ID, locationId);

		} catch (JSONException e) {	e.printStackTrace(); }

		sendAsyncRequest(
				Constants.JSON_METHOD_IS_LOCATION_FAVORITE, 
				jsonRequestData);
	}

	/**
	 * Get a list of items associated with this location.
	 * @param locationId Location id
	 * @return List of images and captions
	 */
	@AsyncRequest
	public void getTilesForLocation(String locationId)
	{
		JSONObject jsonRequestData = new JSONObject();

		try {
			jsonRequestData.put(Constants.JSON_FIELD_LOCATION_ID, locationId);

		} catch (JSONException e) {	e.printStackTrace(); }

		sendAsyncRequest(
				Constants.JSON_METHOD_GET_TILES_FOR_LOCATION, 
				jsonRequestData);
	}

	/**
	 * Update a tile's star-ness.
	 * @param tileId id of tile
	 * @param starred true to star, else false
	 * @return true if starring ok, else false
	 */
	@AsyncRequest
	public void updateTileFavoriteStatus(String tileId, boolean starred)
	{
		JSONObject jsonRequestData = new JSONObject();

		try {
			jsonRequestData.put(Constants.JSON_FIELD_TILE_ID, tileId);
			jsonRequestData.put(Constants.JSON_FIELD_FAVORITE, starred);

		} catch (JSONException e) {	e.printStackTrace(); }
		
		sendAsyncRequest(
				Constants.JSON_METHOD_UPDATE_TILE_FAVORITE_STATUS, 
				jsonRequestData);
	}

	/**
	 * Register a visit to a tile (i.e. that the user clicked on a tile)
	 * @param tileId Id of tile visited
	 * @return
	 */
	@AsyncRequest
	public static void registerUserTileVisit(String tileId)	{
		JSONObject jsonRequestData = new JSONObject();

		try {
			jsonRequestData.put(Constants.JSON_FIELD_TILE_ID, tileId);

			sendAsyncRequestNoCallback(
					Constants.JSON_METHOD_REGISTER_USER_TILE_VISIT, 
					jsonRequestData);

		} catch (JSONException e) {	e.printStackTrace(); }
	}

	/**
	 * Register a visit to a location (i.e. that the user clicked on a location tile)
	 * @param locationId Id of location visited
	 * @return
	 */
	@AsyncRequest
	public static void registerUserLocationVisit(String locationId)	{
		JSONObject jsonRequestData = new JSONObject();

		try {
			jsonRequestData.put(Constants.JSON_FIELD_LOCATION_ID, locationId);

			sendAsyncRequestNoCallback(
					Constants.JSON_METHOD_REGISTER_USER_LOCATION_VISIT, 
					jsonRequestData);

		} catch (JSONException e) {	e.printStackTrace(); }
	}

	/**
	 * Register a pass by to a location (i.e. that the user passed by a location tile)
	 * @param locationId Id of location visited
	 * @return
	 */
	@AsyncRequest
	public static void registerUserLocationPassBy(String locationId)	{
		JSONObject jsonRequestData = new JSONObject();

		try {
			jsonRequestData.put(Constants.JSON_FIELD_LOCATION_ID, locationId);

			sendAsyncRequestNoCallback(
					Constants.JSON_METHOD_REGISTER_USER_LOCATION_PASS_BY, 
					jsonRequestData);

		} catch (JSONException e) {	e.printStackTrace(); }
	}

	/**
	 * Register this device. This method should be called each time the
	 * user starts the app.
	 * @return
	 */
	@AsyncRequest
	public static void registerDevice()	{
		sendAsyncRequestNoCallback(Constants.JSON_METHOD_REGISTER_DEVICE, 
				new JSONObject());
	}

}
