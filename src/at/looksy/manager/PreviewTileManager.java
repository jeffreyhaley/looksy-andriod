package at.looksy.manager;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.widget.LinearLayout;
import at.looksy.dataitem.DataItem;
import at.looksy.tile.ITile;

@Deprecated
public class PreviewTileManager {

	private LinearLayout tilePreviewBucket = null;
	
	private Map<String,ITile> tileMap;

	public PreviewTileManager(Activity activity) {
//		tilePreviewBucket = (LinearLayout) activity.findViewById(R.id.bucket);
		tileMap = new HashMap<String,ITile>();
		
	}

	public void addTile(ITile tile) {
		tilePreviewBucket.addView(tile.getLayout());
		tileMap.put(tile.getData().getId(), tile);
	}

	public void removeTile(ITile tile) {
		tilePreviewBucket.removeView(tile.getLayout());
		tileMap.remove(tile.getData().getId());
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
	
	public void addBookEnd(int remaining, final DataItem data) {
		if (remaining <= 0)
			return;
		
//		TextViewRobotoLight view = new ViewFactory(activity).getReelBookEnd();
//		view.setText("+\n" + remaining + " more tiles");
//		view.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//
//				// remember where you came from
//				ActivityManager.getInstance().push(activity.getClass());
//				
//				// do actual visit
//				Bundle bundle = new Bundle();
//				bundle.putSerializable(Constants.BUNDLE_DATA, data);
//				Intent intent = new Intent(activity, TileExploreActivity.class);
//				intent.putExtras(bundle);
//				activity.startActivity(intent);
//				activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
//				
//			}
//		});
//		
//		
//		tilePreviewBucket.addView(view);
	}

}
