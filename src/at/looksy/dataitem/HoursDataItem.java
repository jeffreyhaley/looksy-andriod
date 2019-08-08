package at.looksy.dataitem;

import java.io.Serializable;

public class HoursDataItem implements Serializable {
	
	private static final long serialVersionUID = 381147044519713118L;
	
	private String day;
	private String timeOpen;
	private String timeClose;
	
	public String getDay() {
		return day;
	}
	public void setDay(String day) {
		this.day = day;
	}
	public String getTimeOpen() {
		return timeOpen;
	}
	public void setTimeOpen(String timeOpen) {
		this.timeOpen = timeOpen;
	}
	public String getTimeClose() {
		return timeClose;
	}
	public void setTimeClose(String timeClose) {
		this.timeClose = timeClose;
	}
	
}
