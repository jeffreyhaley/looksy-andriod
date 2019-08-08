package at.looksy.cache;

import java.util.Date;
import java.util.Map;

import at.looksy.BuildConfig;

import android.support.v4.util.LruCache;
import android.util.Log;
import at.looksy.core.Constants;
import at.looksy.util.Util;


public class DataCache {

	private static boolean CACHE_ENABLED = true;

	private static DataCache instance = null;

	public static enum CacheName {
		LOCATION_BY_BSSID_CACHE,	// key: bssid, value: EntityDataItem
		LOCATION_BY_ID_CACHE,		// key: locationId, value: EntityDataItem
		TILE_CACHE,					// key: tileId, value: ImageCaptionDataItem
		BSSID_NOT_FOUND_CACHE,		// key: bssid, value: Boolean
		BSSID_FOUND_CACHE,			// key: bssid, value: Boolean
		LOCATION_ID_TILE_ID_CACHE	// key: locationId, value: List<tileId>
	}

	private LruCache<String,DataCacheEntry> locationByBSSIDCache = null;
	private LruCache<String,DataCacheEntry> locationByIdCache = null;
	private LruCache<String,DataCacheEntry> tileCache = null;
	private LruCache<String,DataCacheEntry> bssidNotFoundCache = null;
	private LruCache<String,DataCacheEntry> bssidFoundCache = null;
	private LruCache<String,DataCacheEntry> locationIdTileIdCache = null;

	public static DataCache getInstance() {
		if (instance == null) {
			if (BuildConfig.DEBUG)
				Log.d(Constants.DEBUG_TAG, "DataCache creating new instance");
			instance = new DataCache();
		}
		return instance;
	}

	private DataCache() {

		if (BuildConfig.DEBUG) {
			Log.d(Constants.DEBUG_TAG, "DataCache() constructor called");
		}

		// init cache sizes
		int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		int maxCache = (maxMemory) / 10; // take up 10% of memory for overall caches
		int sizePerCache = maxCache / CacheName.values().length;

		if (locationByBSSIDCache == null)
			locationByBSSIDCache = new LruCache<String, DataCacheEntry>(sizePerCache);
		if (locationByIdCache == null)
			locationByIdCache = new LruCache<String, DataCacheEntry>(sizePerCache);
		if (tileCache == null)
			tileCache = new LruCache<String, DataCacheEntry>(sizePerCache);
		if (bssidNotFoundCache == null)
			bssidNotFoundCache = new LruCache<String, DataCacheEntry>(sizePerCache);
		if (bssidFoundCache == null)
			bssidFoundCache = new LruCache<String, DataCacheEntry>(sizePerCache);
		if (locationIdTileIdCache == null)
			locationIdTileIdCache = new LruCache<String,DataCacheEntry>(sizePerCache);
	}

	public boolean containsEntry(CacheName cacheType, String key) {
		if  (!CACHE_ENABLED)
			return false;

		return getEntry(cacheType, key) != null;
	}

	public Object getEntry(CacheName cacheType, String key) {
		if (!CACHE_ENABLED)
			return null;

		DataCacheEntry entry = null;

		switch (cacheType) {
		case LOCATION_BY_BSSID_CACHE:
			entry = locationByBSSIDCache.get(key);
			break;
		case BSSID_NOT_FOUND_CACHE:
			entry = bssidNotFoundCache.get(key);
			break;
		case TILE_CACHE:
			entry = tileCache.get(key);
			break;
		case LOCATION_BY_ID_CACHE:
			entry = locationByIdCache.get(key);
			break;
		case LOCATION_ID_TILE_ID_CACHE:
			entry = locationIdTileIdCache.get(key);
			break;
		case BSSID_FOUND_CACHE:
			entry = bssidFoundCache.get(key);
			break;
		default:
			break;
		}

		// check if entry expired -- if it did return null
		if (entry != null && cacheEntryExpired(cacheType, entry)) {
			if (BuildConfig.DEBUG) {
				Log.d(Constants.DEBUG_TAG, "Cache entry expired: " + cacheType + " id: " + key);
			}
			removeEntry(cacheType, key);
			return null;

		} else if (entry != null) {
			entry.touch();
			return entry.getPayload();

		} else {
			return null;
		}
	}

	public void putEntry(CacheName cacheType, String key, Object data) {
		if (!CACHE_ENABLED)
			return;

		DataCacheEntry entry = new DataCacheEntry(data);

		switch (cacheType) {
		case LOCATION_BY_BSSID_CACHE:
			locationByBSSIDCache.put(key, entry);
			break;
		case BSSID_NOT_FOUND_CACHE:
			bssidNotFoundCache.put(key, entry);
			break;
		case BSSID_FOUND_CACHE:
			bssidFoundCache.put(key, entry);
			break;
		case TILE_CACHE:
			tileCache.put(key, entry);
			break;
		case LOCATION_BY_ID_CACHE:
			locationByIdCache.put(key, entry);
			break;
		case LOCATION_ID_TILE_ID_CACHE:
			locationIdTileIdCache.put(key, entry);
			break;
		}
	}

	public void removeEntry(CacheName cacheType, String key) {
		if (!CACHE_ENABLED)
			return;

		switch (cacheType) {
		case LOCATION_BY_BSSID_CACHE:
			locationByBSSIDCache.remove(key);
			break;
		case BSSID_NOT_FOUND_CACHE:
			bssidNotFoundCache.remove(key);
			break;
		case BSSID_FOUND_CACHE:
			bssidFoundCache.remove(key);
			break;
		case TILE_CACHE:
			tileCache.remove(key);
			break;
		case LOCATION_BY_ID_CACHE:
			locationByIdCache.remove(key);
			break;
		case LOCATION_ID_TILE_ID_CACHE:
			locationIdTileIdCache.remove(key);
			break;
		}
	}

	public void flushCache(CacheName cacheType) {
		if (!CACHE_ENABLED)
			return;

		switch (cacheType) {
		case LOCATION_BY_BSSID_CACHE:
			locationByBSSIDCache.evictAll();
			break;
		case BSSID_NOT_FOUND_CACHE:
			bssidNotFoundCache.evictAll();
			break;
		case BSSID_FOUND_CACHE:
			bssidFoundCache.evictAll();
			break;
		case TILE_CACHE:
			tileCache.evictAll();
			break;
		case LOCATION_BY_ID_CACHE:
			locationByIdCache.evictAll();
			break;
		case LOCATION_ID_TILE_ID_CACHE:
			locationIdTileIdCache.evictAll();
			break;
		}
	}

	private boolean cacheEntryExpired(CacheName cacheType, DataCacheEntry entry) {
		if (!CACHE_ENABLED)
			return true;
		
		switch (cacheType) {
		case BSSID_NOT_FOUND_CACHE:
			return Util.minDifference(new Date(), entry.getCreationTime()) 
					>= 5;
		case BSSID_FOUND_CACHE:
			return Util.minDifference(new Date(), entry.getCreationTime()) 
					>= Constants.LOCATION_TILE_TIME_TO_LIVE;
		case TILE_CACHE:
		case LOCATION_ID_TILE_ID_CACHE:
		case LOCATION_BY_BSSID_CACHE:
		case LOCATION_BY_ID_CACHE:
			return false;
			
		default:
			return true;
		}
	}

	public int getEntryCount(CacheName cacheType) {
		
		if (!CACHE_ENABLED)
			return 0;
		
		LruCache<String,DataCacheEntry> cache = null;
		switch (cacheType) {
		case BSSID_NOT_FOUND_CACHE:
			cache = bssidNotFoundCache;
			break;
		case BSSID_FOUND_CACHE:
			cache = bssidFoundCache;
			break;
		case TILE_CACHE:
			cache = tileCache;
			break;
		case LOCATION_BY_BSSID_CACHE:
			cache = locationByBSSIDCache;
			break;
		case LOCATION_BY_ID_CACHE:
			cache = locationByIdCache;
			break;
		case LOCATION_ID_TILE_ID_CACHE:
			cache = locationIdTileIdCache;
			break;
		}

		// cycle through the cache, cleaning up entries
		Map<String,DataCacheEntry> mapSnapshot = cache.snapshot();
		for (Map.Entry<String, DataCacheEntry> entry : mapSnapshot.entrySet()) {
			String key = entry.getKey();
			DataCacheEntry value = entry.getValue();
			if (cacheEntryExpired(cacheType, value)) {
				removeEntry(cacheType, key);
			}
		}
		
		return cache.size();
	}

	public String stats()
	{
		StringBuilder sb = new StringBuilder();

		sb.append(getStatsForCache(CacheName.LOCATION_BY_BSSID_CACHE));
		sb.append(getStatsForCache(CacheName.LOCATION_BY_ID_CACHE));
		sb.append(getStatsForCache(CacheName.LOCATION_ID_TILE_ID_CACHE));
		sb.append(getStatsForCache(CacheName.TILE_CACHE));
		sb.append(getStatsForCache(CacheName.BSSID_NOT_FOUND_CACHE));
		sb.append(getStatsForCache(CacheName.BSSID_FOUND_CACHE));

		return sb.toString();
	}

	private String getStatsForCache(CacheName cacheType) 
	{
		LruCache<?,?> cache = null;
		switch (cacheType) {
		case BSSID_NOT_FOUND_CACHE:
			cache = bssidNotFoundCache;
			break;
		case BSSID_FOUND_CACHE:
			cache = bssidFoundCache;
			break;
		case TILE_CACHE:
			cache = tileCache;
			break;
		case LOCATION_BY_BSSID_CACHE:
			cache = locationByBSSIDCache;
			break;
		case LOCATION_BY_ID_CACHE:
			cache = locationByIdCache;
			break;
		case LOCATION_ID_TILE_ID_CACHE:
			cache = locationIdTileIdCache;
			break;
		}

		StringBuilder sb = new StringBuilder();

		sb.append(cacheType + "\n");
		sb.append("\tSize: " + cache.size() + "\n");
		sb.append("\tMax size: " + cache.maxSize() + "\n");
		sb.append("\tHit count: " + cache.hitCount() + "\n");
		sb.append("\tMiss count: " + cache.missCount() + "\n");
		sb.append("\tPut count: " + cache.putCount() + "\n");
		sb.append("\tEviction count: " + cache.evictionCount() + "\n");

		return sb.toString();		
	}

}
