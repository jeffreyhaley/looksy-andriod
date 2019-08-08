package at.looksy.tile;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public abstract class AbstractTile implements ITile{

	private ImageView imagePayloadView = null;

	public ImageView getImagePayloadView() {
		return imagePayloadView;
	}

	public void setImagePayloadView(ImageView imagePayloadView) {
		this.imagePayloadView = imagePayloadView;
	}

	public void recycleBitmap()
	{
		if (imagePayloadView != null && imagePayloadView.getDrawable() != null) {
			Bitmap bitmap = ((BitmapDrawable) imagePayloadView.getDrawable()).getBitmap();
			if (bitmap != null) {
				bitmap.recycle();
			}
		}
	}
	
	public int getImageHeight() {
		ImageView imageView = getImagePayloadView();
		if (imageView == null)
			return 0;
		else {
			Drawable drawable = imageView.getDrawable();
			if (drawable == null)
				return 0;
			
			Rect bounds = ((BitmapDrawable)imageView.getDrawable()).getBounds();
			if (bounds == null)
				return 0;
			
			return bounds.height();
		}
	}
	
	protected abstract void inflate();
	
	public abstract void showItem();

}
