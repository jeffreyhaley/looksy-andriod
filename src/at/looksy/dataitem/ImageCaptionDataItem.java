package at.looksy.dataitem;

import java.util.Date;

public class ImageCaptionDataItem extends DataItem {

	private static final long serialVersionUID = -3660900034895440781L;

	private String caption;
	private Date creationDate;

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	
	
}
