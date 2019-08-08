package at.looksy.tile;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import at.looksy.BuildConfig;
import at.looksy.R;
import at.looksy.activity.ImageViewActivity;
import at.looksy.cache.DataCache;
import at.looksy.cache.DataCache.CacheName;
import at.looksy.core.Constants;
import at.looksy.dataitem.DataItem;
import at.looksy.dataitem.ImageCaptionDataItem;
import at.looksy.manager.DimensionManager;
import at.looksy.manager.ImageManager;
import at.looksy.manager.ImageManager.ImageQuality;
import at.looksy.manager.ImageManager.RoundingEffect;
import at.looksy.service.WebService;
import at.looksy.service.consumer.IWebServiceAsyncConsumer;
import at.looksy.service.data.WebServiceRequest;
import at.looksy.service.data.WebServiceResponse;
import at.looksy.service.data.WebServiceResponse.StatusCode;
import at.looksy.util.Util;


public class ImageCaptionTile extends AbstractTile implements ITile, IWebServiceAsyncConsumer {

	private ImageCaptionDataItem data;

	private RelativeLayout rootLayout = null;
	private TextView captionView = null;
	private LinearLayout loveButtonView = null;
	private ImageView loveButtonIconView = null;
	private TextView loveButtonTextView = null;
	private LinearLayout shareButtonView = null;
	private TextView timeView = null;

	private Activity activity = null;
	
	private boolean isPreview = false;

	public ImageCaptionTile(
			Activity activity, 
			ImageCaptionDataItem data,
			boolean isPreview)
	{
		this.activity = activity;
		this.data = data;
		this.isPreview = isPreview;

		inflate();
	}

	protected void inflate() {

		// find views
		rootLayout = (RelativeLayout) activity.getLayoutInflater().inflate(R.layout.tile_mini, null);
		captionView = (TextView) rootLayout.findViewById(R.id.caption);
		setImagePayloadView((ImageView) rootLayout.findViewById(R.id.image));
		loveButtonIconView = (ImageView) rootLayout.findViewById(R.id.loveBtnIcon);
		loveButtonView = (LinearLayout) rootLayout.findViewById(R.id.loveBtn);
		loveButtonTextView = (TextView) rootLayout.findViewById(R.id.loveBtnText);
		shareButtonView = (LinearLayout) rootLayout.findViewById(R.id.shareBtn);
		timeView = (TextView) rootLayout.findViewById(R.id.textTime);

		// set width
		LinearLayout.LayoutParams layout = 
				new LinearLayout.LayoutParams(DimensionManager.getImageTileWidth(),
						LinearLayout.LayoutParams.WRAP_CONTENT);

		layout.setMargins(DimensionManager.getImageTileMargin(), 
				0, 0, DimensionManager.getImageTileMargin());
		rootLayout.setLayoutParams(layout);

		// set handlers
		updateHeartUI();
		loveButtonView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				data.setStarred(!data.isStarred());
				updateHeartUI();
				updateHeartWS();
			}
		});

		rootLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (BuildConfig.DEBUG)
					Log.d(Constants.DEBUG_TAG, "HomeTile.inflate(): inside onClick()");

				showItem();
			}
		});

		final String id = data.getId();
		shareButtonView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String url = Constants.APP_BASE_URL + Constants.APP_MINI_TILE_SUFFIX + id;
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_TEXT, url);
				activity.startActivity(Intent.createChooser(intent, "Share with..."));
			}
		});
		
		// set image
		ImageManager imageHelper = ImageManager.getInstance();
		imageHelper.loadImageView(
				data.getImagePath(), 
				DimensionManager.getImageTileWidth(),  
				isPreview ? Util.dp2px(activity, 80) : ImageManager.HEIGHT_NO_CROP,
				ImageQuality.SMALL,
				RoundingEffect.TOP_ONLY,
				getImagePayloadView());
		ImageView imageView = getImagePayloadView();
		imageView.setMinimumHeight(
				isPreview ? Util.dp2px(activity, 80) : 
					ImageManager.scaleToWidth(
							getData().getImageWidth(), 
							getData().getImageHeight(), 
							DimensionManager.getImageTileWidth()));
		imageView.setMinimumWidth(DimensionManager.getImageTileWidth());

		// set caption
		if (data.getCaption().equals(Constants.EMPTY_STR))
			captionView.setVisibility(View.GONE);
		else
			captionView.setText(
					isPreview ? Util.fitString(data.getCaption(), 35) : 
						data.getCaption());

		// update timestamp
		String humanTimeStr = 
				Util.humanTimeDifferenceMajor(new Date(), data.getCreationDate());
		timeView.setText(humanTimeStr);

		rootLayout.requestLayout();
	}

	@Override
	public View getLayout() {
		return rootLayout;
	}

	@Override
	public DataItem getData()
	{
		return data;
	}

	private void updateHeartUI()
	{
		loveButtonIconView.setImageResource(data.isStarred() ? 
				R.drawable.ico_heart_on : 
					R.drawable.ico_heart_off);
		if (data.getStarCount() != 0) {
			loveButtonTextView.setText(String.valueOf(data.getStarCount()));
			loveButtonTextView.setVisibility(View.VISIBLE);
		} else {
			loveButtonTextView.setText("Love");
			loveButtonTextView.setVisibility(View.VISIBLE);
		}
	}

	private void updateHeartWS()
	{
		new WebService(this).updateTileFavoriteStatus(
				getData().getId(), getData().isStarred());
	}

	@Override
	public void showItem()
	{
		Bundle bundle = new Bundle();

		// assemble new request object because ?
		ImageCaptionDataItem item = new ImageCaptionDataItem();
		item.setId(getData().getId());
		item.setCaption(((ImageCaptionDataItem)getData()).getCaption());
		item.setImagePath(getData().getImagePath());

		// register this visit with the web service
		WebService.registerUserTileVisit(item.getId());

		// and we're off
		bundle.putSerializable(Constants.BUNDLE_DATA, item);
		Intent intent = new Intent(activity, ImageViewActivity.class);
		intent.putExtras(bundle);
		if (activity.getParent() != null) {
			activity.getParent().startActivityForResult(intent,1);
			activity.getParent().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		} else {
			activity.startActivityForResult(intent,1);
			activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		}
	}

	@Override
	public void consumeWSExchange(WebServiceRequest wsRequest,
			WebServiceResponse wsResponse) {

		if (!receiveWSCallback())
			return;

		if (wsResponse == null)
			return;

		// get cache handle
		DataCache dataCache = DataCache.getInstance();

		String method = wsRequest.getRequestAction();

		if (method.equals(Constants.JSON_METHOD_UPDATE_TILE_FAVORITE_STATUS)) {
			try {
				if (wsResponse.getStatus() == StatusCode.OK) {

					// get new favorite count
					int favoriteCount = 
							((JSONObject)wsResponse.getJSONArray(
									Constants.WEB_SERVICE_DATA).get(0)).getInt(
											Constants.JSON_FIELD_FAVORITE_COUNT);

					// update local copy
					getData().setStarCount(favoriteCount);

					// update cached copy
					if (dataCache.containsEntry(CacheName.TILE_CACHE, getData().getId())) {
						ImageCaptionDataItem cachedDataItem = (ImageCaptionDataItem)
								dataCache.getEntry(CacheName.TILE_CACHE, getData().getId());
						cachedDataItem.setStarCount(favoriteCount);
					}

					// update UI
					updateHeartUI();					
				}

			} catch (JSONException e) {	e.printStackTrace(); }
		}
	}

	@Override
	public boolean receiveWSCallback() {
		return !activity.isFinishing();
	}


}
