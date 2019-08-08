package at.looksy.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import at.looksy.R;
import at.looksy.core.Constants;
import at.looksy.dataitem.DataItem;
import at.looksy.dataitem.ImageCaptionDataItem;
import at.looksy.listener.OnSwipeTouchListener;
import at.looksy.manager.ImageManager;
import at.looksy.manager.ImageManager.ImageQuality;
import at.looksy.manager.ImageManager.RoundingEffect;
import at.looksy.util.SystemUiHider;


public class ImageViewActivity extends Activity {
	private static final boolean AUTO_HIDE = false;
	private static final int AUTO_HIDE_DELAY_MILLIS = 10000;
	private static final boolean TOGGLE_ON_CLICK = true;
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	private SystemUiHider mSystemUiHider;

	private DataItem data = null;

	private int IMAGE_FULL_WIDTH = 0;
	
	private ImageView contentView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_image_view);

		initActivity();

		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		if (contentView == null) {
			contentView = (ImageView) findViewById(R.id.fullscreen_content);
			ImageManager imageHelper = ImageManager.getInstance();
			imageHelper.loadImageView(
					data.getImagePath(),
					IMAGE_FULL_WIDTH, 
					ImageManager.HEIGHT_NO_CROP,
					ImageQuality.LARGE,
					RoundingEffect.OFF,
					contentView);
		}

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
		.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
			// Cached values.
			int mControlsHeight;
			int mShortAnimTime;

			@SuppressWarnings("unused")
			@Override
			@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
			public void onVisibilityChange(boolean visible) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
					// If the ViewPropertyAnimator API is available
					// (Honeycomb MR2 and later), use it to animate the
					// in-layout UI controls at the bottom of the
					// screen.
					if (mControlsHeight == 0) {
						mControlsHeight = controlsView.getHeight();
					}
					if (mShortAnimTime == 0) {
						mShortAnimTime = getResources().getInteger(
								android.R.integer.config_shortAnimTime);
					}
					controlsView
					.animate()
					.translationY(visible ? 0 : mControlsHeight)
					.setDuration(mShortAnimTime);
				} else {
					// If the ViewPropertyAnimator APIs aren't
					// available, simply show or hide the in-layout UI
					// controls.
					controlsView.setVisibility(visible ? View.VISIBLE
							: View.GONE);
				}

				if (visible && AUTO_HIDE) {
					// Schedule a hide().
					delayedHide(AUTO_HIDE_DELAY_MILLIS);
				}
			}
		});

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		TextView captionText = (TextView) findViewById(R.id.caption);
		captionText.setOnTouchListener(mDelayHideTouchListener);
		String caption = "";
		if (data instanceof ImageCaptionDataItem) {
			caption = ((ImageCaptionDataItem) data).getCaption();
		}
		captionText.setText(caption);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}
	

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void initActivity() 
	{	
		initLayoutSizes();

		// prepare bundle
		Bundle bundle = getIntent().getExtras();
		data = (DataItem) bundle.getSerializable(Constants.BUNDLE_DATA);

		// back swipe listener
		ImageView root = (ImageView) findViewById(R.id.fullscreen_content);
		root.setOnTouchListener(new OnSwipeTouchListener() {
			public void onSwipeRight() {
				endView();
			}
		});	
	}

	@Override
	public void onBackPressed()
	{
		endView();
	}

	private void endView()
	{
		if (contentView != null) {
			Drawable drawable = contentView.getDrawable();
			if (drawable != null) {
				Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
				if (bitmap != null) {
					bitmap.recycle();
				}
			}
		}
		
		finish();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	@SuppressWarnings("deprecation")
	private void initLayoutSizes()
	{
		Display display = getWindowManager().getDefaultDisplay();
		IMAGE_FULL_WIDTH = display.getWidth();
	}
}
