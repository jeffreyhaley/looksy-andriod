package at.looksy.manager;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import at.looksy.tile.ITile;


public abstract class AbstractTileManager {

	private Map<String,ITile> tileMap;

	public AbstractTileManager(Activity activity) {
		tileMap = new HashMap<String,ITile>();
	}

	public Map<String, ITile> getTileMap() {
		return tileMap;
	}

	public boolean hasTiles() {
		return !tileMap.isEmpty();
	}

	public void recycleBitmaps() {
		for (Map.Entry<String,ITile> entry : tileMap.entrySet())
			entry.getValue().recycleBitmap();
	}
	
	public abstract void addTile(ITile tile);
	public abstract void removeTile(ITile tile);

}
