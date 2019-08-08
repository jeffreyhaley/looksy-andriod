package at.looksy.manager;

import java.util.Map;

import android.app.Activity;
import android.widget.LinearLayout;
import at.looksy.R;
import at.looksy.tile.ITile;
import at.looksy.tile.LocationTile;

public class LocationTileManager extends AbstractTileManager {
	
	private LinearLayout bucket1 = null;

	public LocationTileManager(Activity activity) {
		super(activity);
		
		bucket1 = (LinearLayout) activity.findViewById(R.id.feedBucket1);
	}

	public void refreshFollowedAllUI() {
		for (Map.Entry<String, ITile> entry : getTileMap().entrySet()) {
			refreshFollowedUI(entry.getValue().getData().getId());
		}
	}

	public void refreshFollowedUI(String locationId) 
	{
		LocationTile locationTile = 
				(LocationTile) getTileMap().get(locationId);
		locationTile.refreshFollowed();
	}
	
	public void refreshVisibilityOfAllTilesUI()
	{
		for (Map.Entry<String, ITile> entry : getTileMap().entrySet()) {
			LocationTile tile = (LocationTile) entry.getValue();
			if (tile.shouldRemove())
				removeTile(tile);
		}
	}

	@Override
	public void addTile(ITile tile) {
		bucket1.addView(tile.getLayout());
		getTileMap().put(tile.getData().getId(), tile);
	}

	@Override
	public void removeTile(ITile tile) {
		bucket1.removeView(tile.getLayout());
		getTileMap().remove(tile.getData().getId());
	}



}
