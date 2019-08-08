package at.looksy.dataitem;

import java.util.ArrayList;
import java.util.List;

public class LocationDataItem extends DataItem {

	private static final long serialVersionUID = 3142500682152690229L;
	private String name;
	private String description;
	private String address;
	private String city;
	private String state;
	private String zip;
	private String phoneNumber;
	private String webSite;
	private String facebookUsername;
	private String twitterUsername;
	private String instagramUsername;

	private String timezoneOffset;
	private double latitude;
	private double longitude;
	
	private List<String> BSSIDList;
	private List<HoursDataItem> locationHours;
	
	public List<HoursDataItem> getLocationHours() {
		return locationHours;
	}

	public void setLocationHours(List<HoursDataItem> locationHours) {
		this.locationHours = locationHours;
	}

	public String getTimezoneOffset() {
		return timezoneOffset;
	}

	public void setTimezoneOffset(String timezoneOffset) {
		this.timezoneOffset = timezoneOffset;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public LocationDataItem() {
		super();
		this.BSSIDList = new ArrayList<String>();
	}
	
	public void associateBSSID(String bssid) {
		BSSIDList.add(bssid);
	}
	
	public List<String> getBSSIDList() {
		return BSSIDList;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getWebSite() {
		return webSite;
	}

	public void setWebSite(String webSite) {
		this.webSite = webSite;
	}

	public String getTwitterUsername() {
		return twitterUsername;
	}

	public void setTwitterUsername(String twitterUsername) {
		this.twitterUsername = twitterUsername;
	}

	public String getInstagramUsername() {
		return instagramUsername;
	}

	public void setInstagramUsername(String instagramUsername) {
		this.instagramUsername = instagramUsername;
	}

	public String getFacebookUsername() {
		return facebookUsername;
	}

	public void setFacebookUsername(String facebookUsername) {
		this.facebookUsername = facebookUsername;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	

}
