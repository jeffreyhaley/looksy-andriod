package at.looksy.activity;

import java.util.Map;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import at.looksy.BuildConfig;
import at.looksy.R;
import at.looksy.activity.base.HomeBaseActivity;
import at.looksy.core.Constants;
import at.looksy.factory.ViewFactory;
import at.looksy.service.WifiService;
import at.looksy.service.data.WifiAccessPointData;
import at.looksy.service.data.WifiAccessPointData.SignalStrength;
import at.looksy.util.UIUtil;


public class WifiListingActivity extends HomeBaseActivity implements IWifiReceiverActivity {

	private WifiService wifiService = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wifi_listing);
		
		initActivity();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	private void initActivity()
	{
		// Initialize menu items
		initNavbar(this);
		
		// init wifi service
		if (wifiService == null)
			wifiService = WifiService.getInstance();
		
		if (wifiService.getLatestAccessPointData() != null)
			updateWifiData(wifiService.getLatestAccessPointData());
		
		// register with wifi service
		wifiService.attachActivity(this);
	}
	
	@Override
	public void onBackPressed()
	{
		endView();
	}
	
	private void endView()
	{
		finish();
	}

	@Override
	public void updateWifiData(Map<String, WifiAccessPointData> data) {
		
		if (BuildConfig.DEBUG)
			Log.d(Constants.DEBUG_TAG, "WifiListingActivity.updateWifiData() called");
		
		// hide the progress bar (never really shown, but that's ok)
		findViewById(R.id.progressBar).setVisibility(View.GONE);
		
		// prepare views
		ViewFactory viewFactory = new ViewFactory(this);
		LinearLayout wifiListView = (LinearLayout) findViewById(R.id.wifiListing);
		wifiListView.removeAllViews();
		
		// see if we can locate the connected wifi
		WifiAccessPointData connectedAp = null;
		for (Map.Entry<String, WifiAccessPointData> entry : data.entrySet()) {
			WifiAccessPointData wifiData = entry.getValue();
			if (wifiData.isConnected()) {
				connectedAp = wifiData;

			} else {
				if (wifiData.getSignalStrength() == SignalStrength.OUT_OF_RANGE ||
						wifiData.getSignalStrength() == SignalStrength.UNKNOWN) {
					continue;
				}
				LinearLayout wifiListingEntryView = 
						viewFactory.getWifiListingEntry();
				((TextView)wifiListingEntryView.findViewById(
						R.id.textSsid)).setText("Network name: " + wifiData.getSSID());
				((TextView)wifiListingEntryView.findViewById(
						R.id.textBssid)).setText("Unique ID: " + 
								wifiData.getBSSID().replace(':', '-'));
				((TextView)wifiListingEntryView.findViewById(
						R.id.textDbm)).setText("dBm: " + 
								wifiData.getRawSignalStrength());
				ImageView signalIndicatorView = 
						(ImageView) wifiListingEntryView.findViewById(
								R.id.icoSignalStrength);
				UIUtil.setWifiProximityIndicator(wifiData.getSignalStrength(), 
						signalIndicatorView);
				wifiListView.addView(wifiListingEntryView);
			}
		}
		
		// set onclick listener for show all networks
		findViewById(R.id.btnShowAllNetworks).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				findViewById(R.id.wifiListing).setVisibility(View.VISIBLE);
				v.setVisibility(View.GONE);
			}
		});
		
		// if we can't find it, we aren't connected to a wifi
		if (connectedAp == null || 
				connectedAp.getSignalStrength() == SignalStrength.UNKNOWN || 
				connectedAp.getSignalStrength() == SignalStrength.OUT_OF_RANGE) 
		{
			findViewById(R.id.entryNotConnected).setVisibility(View.VISIBLE);
			
		} else {
			// show that we are connected
			findViewById(R.id.entryNotConnected).setVisibility(View.GONE);
			findViewById(R.id.entryConnected).setVisibility(View.VISIBLE);
			
			// SSID
			TextView textViewSsid = (TextView) findViewById(R.id.title);
			textViewSsid.setText("Connected to " + connectedAp.getSSID());
			
			// BSSID
			TextView textViewBssid = (TextView) findViewById(R.id.textBssid);
			String displayBssid = "Unique ID: " + connectedAp.getBSSID().replace(':', '-');
			textViewBssid.setText(displayBssid);
			
			// signal strength
			ImageView signalStrengthView = (ImageView) findViewById(R.id.icoSignalStrength);
			UIUtil.setWifiProximityIndicator(connectedAp.getSignalStrength(), 
					signalStrengthView);
		}
		
	}

}
