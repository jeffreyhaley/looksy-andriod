package at.looksy.manager;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.util.SparseArray;
import android.widget.ImageView;
import at.looksy.core.Constants;
import at.looksy.util.Util;


public class ImageManager {

	public static enum ImageQuality {
		LARGE, MEDIUM, SMALL
	}
	public static enum RoundingEffect {
		ON, TOP_ONLY, OFF
	}
	
	public static int HEIGHT_NO_CROP = 0;

	private final int MAX_IMAGE_CACHES = 3;
	private int DEFAULT_CACHE_SIZE = 0;
	private final int TASKS_PER_BATCH = 3;
	private final int TASK_INTERVAL = 1;

	private static ImageManager instance = null;

	private Timer bitmapQueueProcessTimer = null;
	private SparseArray<LruCache<String,Bitmap>> cacheByWidth = null;
	private Queue<BitmapWorkerTask> workerWaitQueue = null;

	private ImageManager() {
		// init cache
		cacheByWidth = new SparseArray<LruCache<String,Bitmap>>();
		workerWaitQueue = new LinkedList<BitmapWorkerTask>();

		// init cache sizes
		int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		int maxSizeForImageCaches = (maxMemory) / 3; // take up 33% of memory
		DEFAULT_CACHE_SIZE = maxSizeForImageCaches / MAX_IMAGE_CACHES;
	}

	public static ImageManager getInstance()
	{
		if (instance == null)
			instance = new ImageManager();
		return instance;
	}

	private Bitmap getImageFromCache(String imageId, int width)
	{
		Bitmap bitmap = null;

		if (cacheByWidth.get(width) != null) {
			bitmap = cacheByWidth.get(width).get(imageId);
		}

		return bitmap;
	}

	private void addImageToCache(String imagePath, Bitmap bitmap, int width)
	{
		if (cacheByWidth.get(width) == null) {
			cacheByWidth.put(width, new LruCache<String,Bitmap>(DEFAULT_CACHE_SIZE) {
				@Override
				protected int sizeOf(String key, Bitmap bitmap) {
					return ImageManager.getBitmapSize(bitmap) / 1024;
				}
			});
		}
		if (cacheByWidth.size() > 3) {
			Log.d(Constants.DEBUG_TAG, "BIG PROBLEM __ TOO MANY CACHES!!!!");
		}
		Log.d(Constants.DEBUG_TAG, "Image size: " + ImageManager.getBitmapSize(bitmap) / 1024 + "KB");
		cacheByWidth.get(width).put(imagePath, bitmap);
	}

	class ProcessBitmapWorkerQueueTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... none) {
			int tasksPerBatch = TASKS_PER_BATCH;
			while (!workerWaitQueue.isEmpty() && tasksPerBatch != 0) {
				BitmapWorkerTask task = workerWaitQueue.poll();
				if (task != null)
					task.execute();
				tasksPerBatch--;
			}
			return null;
		}
	}

	private void addToBitmapWorkerQueue(BitmapWorkerTask task) {
		workerWaitQueue.offer(task);
		activiateTimer();
	}

	private void processBitmapWorkerQueue() {
		new ProcessBitmapWorkerQueueTask().execute();
	}

	private void activiateTimer() {
		if (bitmapQueueProcessTimer == null) {
			bitmapQueueProcessTimer = new Timer(true);
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					processBitmapWorkerQueue();
				}
			};
			bitmapQueueProcessTimer.scheduleAtFixedRate(task, 
					0, 
					TimeUnit.SECONDS.toMillis(TASK_INTERVAL));
		}
	}

	public static int scaleToWidth(int origWidth, int origHeight, int scaleToWidth) {
		return (scaleToWidth * origHeight) / origWidth;
	}

	public void loadImageView(
			String imageFileName, 
			int scaleToWidth,
			int cropToHeight,
			ImageQuality imageQuality,
			RoundingEffect roundingEffect,
			ImageView imageView) 
	{
		Bitmap bitmap = 
				(cropToHeight == HEIGHT_NO_CROP) ? 
						getImageFromCache(imageFileName, scaleToWidth) : 
							null;

		if (bitmap != null && bitmap.isRecycled())
			bitmap = null;

		if (bitmap == null) { 
			// fetch it from web
			addToBitmapWorkerQueue(new BitmapWorkerTask(imageFileName, imageQuality, 
					roundingEffect,	scaleToWidth, cropToHeight, imageView));

		} else {
			// use the cached version
			if (imageView != null)
				imageView.setImageBitmap(bitmap);
		}
	}

	private class BitmapWorkerTask extends AsyncTask<Void, Void, Bitmap> {

		private final WeakReference<ImageView> imageViewReference;
		private String imageFileName;
		private ImageQuality imageSize;
		private RoundingEffect roundingEffect;
		private int scaleToWidth;
		private int cropToHeight;

		public BitmapWorkerTask(
				String imageFileName,
				ImageQuality imageSize,
				RoundingEffect roundingEffect,
				int scaleToWidth,
				int cropToHeight,
				ImageView imageView) 
		{
			// Use a WeakReference to ensure the ImageView can be garbage collected
			imageViewReference = new WeakReference<ImageView>(imageView);
			this.scaleToWidth = scaleToWidth;
			this.imageFileName = imageFileName;
			this.imageSize = imageSize;
			this.roundingEffect = roundingEffect;
			this.cropToHeight = cropToHeight;
		}

		@Override
		protected Bitmap doInBackground(Void... none) {

			Bitmap resultingBitmap = null;
			Bitmap unscaledBitmap = null;
			Bitmap scaledBitmap = null;

			// prepare the url
			String imagePathPrefix = null;
			switch (imageSize) {
			case LARGE:
				imagePathPrefix = Constants.IMG_LARGE;
				break;
			case MEDIUM:
				imagePathPrefix = Constants.IMG_MEDIUM;
				break;
			case SMALL:
				imagePathPrefix = Constants.IMG_SMALL;
				break;
			}

			// connect to retrieve image
			HttpClient httpClient = new DefaultHttpClient();
			HttpResponse response = null;
			String url = Constants.WEB_SERVICE_BASE_URI + "public/img/" + imagePathPrefix + "/" + imageFileName;;
			HttpGet request = new HttpGet(url);

			try {
				response = httpClient.execute(request);
				if (response != null) {
					// grab image from stream
					InputStream is = response.getEntity().getContent();
					unscaledBitmap = BitmapFactory.decodeStream(is);
					is.close();
				}
			} catch (Exception e) { e.printStackTrace(); }

			// create new bitmap from unscaled bitmap
			if (unscaledBitmap != null) {
				int imgWidth = unscaledBitmap.getWidth();
				int imgHeight = unscaledBitmap.getHeight();

				int newWidth = this.scaleToWidth;
				int newHeight = ImageManager.scaleToWidth(imgWidth, imgHeight, newWidth);

				scaledBitmap = Bitmap.createScaledBitmap(unscaledBitmap, newWidth, newHeight, true);
				
				if (cropToHeight != HEIGHT_NO_CROP) {
					Bitmap tmp = scaledBitmap;
					scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, 
							newWidth, Math.min(cropToHeight, scaledBitmap.getHeight()));
					tmp.recycle();
				}
				
				if (roundingEffect == RoundingEffect.OFF) {
					resultingBitmap = scaledBitmap;
					
				} else {
					
					/*
					 * Adapted from:
					 * http://stackoverflow.com/questions/1705239/how-should-i-give-images-rounded-corners-in-android
					 */
					if (imageViewReference == null || 
							imageViewReference.get() == null || 
							imageViewReference.get().getContext() == null)
						return null;
					
					int roundRadius = Util.dp2px(imageViewReference.get().getContext(), 5);

					int rounderHeight = (roundingEffect == RoundingEffect.TOP_ONLY) ? 
							newHeight / 2 : newHeight;

					Bitmap rounder = Bitmap.createBitmap(newWidth, rounderHeight, Bitmap.Config.ARGB_8888);
					Canvas canvas = new Canvas(rounder);
					Paint xferPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
					xferPaint.setColor(Color.RED);
					canvas.drawRoundRect(new RectF(0, 0, newWidth, newHeight), roundRadius, roundRadius, xferPaint);     
					xferPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

					resultingBitmap = Bitmap.createBitmap(
							scaledBitmap.getWidth(), 
							scaledBitmap.getHeight(), 
							Bitmap.Config.ARGB_8888);
					Canvas resultCanvas = new Canvas(resultingBitmap);
					resultCanvas.drawBitmap(scaledBitmap, 0, 0, null);
					resultCanvas.drawBitmap(rounder, 0, 0, xferPaint);

					// recycle unneeded scaled bitmap since caller wanted rounding
					scaledBitmap.recycle();
				}
				
				// always recycle unscaled bitmap
				unscaledBitmap.recycle();

				// save it to cache
				if (imageSize != ImageQuality.LARGE && cropToHeight == HEIGHT_NO_CROP)
					addImageToCache(imageFileName, resultingBitmap, scaleToWidth);
			}

			return resultingBitmap;
		}

		// Once complete, see if ImageView is still around and set bitmap.
		@Override
		protected void onPostExecute(Bitmap newBitmap) {
			if (imageViewReference != null && newBitmap != null) {
				final ImageView imageView = imageViewReference.get();
				if (imageView != null) {
					imageView.setImageBitmap(newBitmap);
				}
			}
		}
	}

	/**
	 * Returns the size of the provided bitmap, in bytes
	 * @param bitmap Bitmap to size
	 * @return Size of bitmap
	 */
	public static int getBitmapSize(Bitmap bitmap)
	{
		return bitmap.getRowBytes() * bitmap.getHeight();
	}

}
