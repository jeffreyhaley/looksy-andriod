package at.looksy.dataitem;

import java.io.Serializable;

public abstract class DataItem implements Serializable {

	private static final long serialVersionUID = 3142500682152690229L;

	private String id = null;
	private boolean isStarred = false;
	private int starCount;
	private boolean isVisible = true;
	private String imagePath = null;
	private int imageWidth;
	private int imageHeight;

	public int getImageWidth() {
		return imageWidth;
	}

	public void setImageScaledWidth(int imageWidth) {
		this.imageWidth = imageWidth;
	}

	public int getImageHeight() {
		return imageHeight;
	}

	public void setImageScaledHeight(int imageHeight) {
		this.imageHeight = imageHeight;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
	
	public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	public boolean isStarred() {
		return isStarred;
	}

	public void setStarred(boolean isStarred) {
		this.isStarred = isStarred;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public int getStarCount() {
		return starCount;
	}

	public void setStarCount(int starCount) {
		this.starCount = starCount;
	}

}
