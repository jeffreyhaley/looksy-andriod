package at.looksy.tile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import at.looksy.R;
import at.looksy.activity.graveyard.EntityProfileActivity;
import at.looksy.core.Constants;
import at.looksy.dataitem.DataItem;
import at.looksy.dataitem.LocationDataItem;
import at.looksy.factory.ViewFactory;
import at.looksy.manager.DimensionManager;
import at.looksy.manager.ImageManager;
import at.looksy.manager.ImageManager.ImageQuality;
import at.looksy.manager.ImageManager.RoundingEffect;
import at.looksy.service.WebService;


public class SearchResultTile extends AbstractTile implements ITile {
	
	private LocationDataItem data;
	private Activity activity;
	private LinearLayout rootLayout = null;
	
	public SearchResultTile(
			Activity activity,
			LocationDataItem data)
	{
		this.data = data;
		this.activity = activity;
		
		inflate();
	}

	@Override
	public View getLayout() {
		return rootLayout;
	}

	@Override
	public DataItem getData() {
		return data;
	}

	@Override
	protected void inflate() {
		
		rootLayout = new ViewFactory(activity).getSearchResultEntry();
		
		setImagePayloadView((ImageView) rootLayout.findViewById(R.id.imgEntity));
		TextView headingTextView = (TextView) rootLayout.findViewById(R.id.textHeading);
		TextView subheadingTextView = (TextView) rootLayout.findViewById(R.id.textSubheading);
		
		// set handlers
		rootLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showItem();
			}
		});
		
		// set image
		ImageManager imageHelper = ImageManager.getInstance();
		imageHelper.loadImageView(
				data.getImagePath(), 
				DimensionManager.getLocationTileWidth(),
				ImageManager.HEIGHT_NO_CROP,
				ImageQuality.SMALL,
				RoundingEffect.ON,
				getImagePayloadView());
		
		// set heading/subheading
		headingTextView.setText(data.getName());
		subheadingTextView.setText(data.getCity() + ", " + data.getState());
	}

	@Override
	public void showItem() {
		// register this visit
		WebService.registerUserLocationVisit(getData().getId());

		// do actual visit
		Bundle bundle = new Bundle();
		bundle.putSerializable(Constants.BUNDLE_DATA, getData());
		Intent intent = new Intent(activity, EntityProfileActivity.class);
		intent.putExtras(bundle);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}

}
