package at.looksy.service.data;

import java.util.Date;

public class WifiAccessPointData {
	
	public static enum SignalStrength {
		UNKNOWN,
		OUT_OF_RANGE,
		ONE_BAR,
		TWO_BARS,
		THREE_BARS,
		FOUR_BARS
	}
	
	private String BSSID;
	private String SSID;
	private SignalStrength signalStrength;
	private Date dateLastSeen;
	private boolean isConnected;
	private int rawSignalStrength;
	
	public WifiAccessPointData(String bSSID, String sSID) {
		super();
		BSSID = bSSID;
		SSID = sSID;
		this.signalStrength = SignalStrength.OUT_OF_RANGE;
		dateLastSeen = new Date();
		isConnected = false;
		rawSignalStrength = 0;
	}

	public int getRawSignalStrength() {
		return rawSignalStrength;
	}

	public void setRawSignalStrength(int rawSignalStrength) {
		this.rawSignalStrength = rawSignalStrength;
	}

	public boolean isConnected() {
		return isConnected;
	}

	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}

	public SignalStrength getSignalStrength() {
		return signalStrength;
	}

	public void setSignalStrength(SignalStrength signalStrength) {
		this.signalStrength = signalStrength;
		if (signalStrength != SignalStrength.OUT_OF_RANGE)
			dateLastSeen = new Date();
	}

	public Date getDateLastSeen() {
		return dateLastSeen;
	}

	public String getBSSID() {
		return BSSID;
	}

	public String getSSID() {
		return SSID;
	}
	
	public void setDateLastSeen(Date dateLastSeen) {
		this.dateLastSeen = dateLastSeen;
	}

	@Override
	public String toString()
	{
		return "SSID:" + SSID + 
				" BSSID:" + BSSID + 
				" SignalStrength:" + signalStrength + 
				" DateLastSeen:" + dateLastSeen;
	}

	
}
