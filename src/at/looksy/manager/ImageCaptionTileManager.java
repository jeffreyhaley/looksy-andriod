package at.looksy.manager;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;
import at.looksy.R;
import at.looksy.dataitem.ImageCaptionDataItem;
import at.looksy.tile.ITile;

public class ImageCaptionTileManager extends AbstractTileManager {

	private LinearLayout bucket1 = null;
	private LinearLayout bucket2 = null;

	int bucket1Height;
	int bucket2Height;

	public ImageCaptionTileManager(Activity activity) {
		super(activity);

		bucket1 = (LinearLayout) activity.findViewById(R.id.feedBucket1);
		bucket2 = (LinearLayout) activity.findViewById(R.id.feedBucket2);

		bucket1Height = 0;
		bucket2Height = 0;
	}

	private int calculateTileHeight(ITile tile) {
		int height = tile.getData().getImageHeight();
		if (tile.getData() instanceof ImageCaptionDataItem) {
			height += ((ImageCaptionDataItem)tile.getData())
					.getCaption().length();
		}
		return height;
	}

	@Override
	public void addTile(ITile tile) {
		int tileHeight = calculateTileHeight(tile);

		if (bucket1Height <= bucket2Height) {
			bucket1.addView(tile.getLayout());
			bucket1Height += tileHeight;
		} else {
			bucket2.addView(tile.getLayout());
			bucket2Height += tileHeight;
		}

		getTileMap().put(tile.getData().getId(), tile);
	}

	@Override
	public void removeTile(ITile tile) {
		int bucket1ChildCount = bucket1.getChildCount();

		bucket1.removeView(tile.getLayout());
		bucket2.removeView(tile.getLayout());

		// figure out which column we removed from
		if (bucket1.getChildCount() != bucket1ChildCount) {
			// must have removed from bucket1
			bucket1Height -= calculateTileHeight(tile);
		} else {
			// must have removed from bucket2
			bucket2Height -= calculateTileHeight(tile);
		}

		getTileMap().remove(tile.getData().getId());
		rebalance();
	}

	private void rebalance() {

		while (tilesAreUnbalanced()) {
			if (bucket1.getChildCount() > bucket2.getChildCount()) {
				View viewToMove = bucket1.getChildAt(bucket1.getChildCount()-1);
				bucket1.removeView(viewToMove);
				bucket2.addView(viewToMove);
			} else {
				View viewToMove = bucket2.getChildAt(bucket2.getChildCount()-1);
				bucket2.removeView(viewToMove);
				bucket1.addView(viewToMove);
			}
		}
	}

	private boolean tilesAreUnbalanced() {
		return (Math.abs(bucket1.getChildCount() - bucket2.getChildCount()) > 1) 
				|| (bucket2.getChildCount() > bucket1.getChildCount());
	}




}
