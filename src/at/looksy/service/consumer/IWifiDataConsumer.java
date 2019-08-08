package at.looksy.service.consumer;

import java.util.Map;

import android.content.Context;
import at.looksy.service.data.WifiAccessPointData;


public interface IWifiDataConsumer {
	
	public void updateWifiData(Map<String,WifiAccessPointData> data);
	public Context getContext();
}
