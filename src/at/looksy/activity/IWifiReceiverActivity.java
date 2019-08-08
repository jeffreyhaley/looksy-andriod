package at.looksy.activity;

import java.util.Map;

import at.looksy.service.data.WifiAccessPointData;

public interface IWifiReceiverActivity {
	
	public void updateWifiData(Map<String,WifiAccessPointData> data);

}
