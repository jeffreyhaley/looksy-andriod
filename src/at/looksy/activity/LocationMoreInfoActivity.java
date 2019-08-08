package at.looksy.activity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import at.looksy.R;
import at.looksy.activity.base.LocationBaseActivity;
import at.looksy.core.Constants;
import at.looksy.dataitem.HoursDataItem;
import at.looksy.factory.ViewFactory;
import at.looksy.util.StringHelper;
import at.looksy.util.UIUtil;
import at.looksy.util.Util;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;

public class LocationMoreInfoActivity extends LocationBaseActivity {
	
	private GoogleMap googleMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location_more_info);
		
		initActivity();
		initMap();
	}
	
	private void initMap()
	{
		try {
			MapsInitializer.initialize(this);
		} catch (GooglePlayServicesNotAvailableException e) {
			Log.e(Constants.DEBUG_TAG, "Google play service is not available.");
		}

		if (googleMap == null) {
			googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(
					R.id.mapFragment)).getMap();
		}
		
		if (googleMap != null)
			UIUtil.setupMap(this, googleMap, 
					getData().getLatitude(), getData().getLongitude(), 14);
	}

	@Override
	protected void initActivity() 
	{	
		super.initActivity();
		
		ViewFactory layoutFactory = new ViewFactory(this);
		LinearLayout infoSectionLayout = (LinearLayout) findViewById(R.id.sectionInfo);

		// closing in
		if (getData().getLocationHours() != null && 
				getData().getLocationHours().size() != 0 && 
				getData().getTimezoneOffset() != null) 
		{
			// opens/closes in header
			String hours[] = Util.getBusinessHoursString(getData().getLocationHours(), getData().getTimezoneOffset());
			((TextView) findViewById(R.id.hoursField)).setText(hours[0]);
			((TextView) findViewById(R.id.hoursContent)).setText(hours[1]);
			infoSectionLayout.addView(layoutFactory.getHorizontalLine(true));
			
			// full hours
			LinearLayout fullHoursSectionView = 
					(LinearLayout) findViewById(R.id.sectionHoursFull);
			List<HoursDataItem> hoursList = getData().getLocationHours();
			for (HoursDataItem item : hoursList) {
				// construct time range
				String timeRange = null;
				if (item.getTimeOpen() == null ||
						item.getTimeOpen().equals(Constants.NULL_STR)) 
				{
					timeRange = "Closed";
					
				} else {
					timeRange = 
							Util.standardTo12HourTime(item.getTimeOpen())
							+ " - " + 
							Util.standardTo12HourTime(item.getTimeClose());
				}
				
				// add to view
				fullHoursSectionView.addView(
						layoutFactory.getHoursEntry(item.getDay(), timeRange));
			}
		} else {
			findViewById(R.id.sectionHoursInfo).setVisibility(View.GONE);
		}

		// navigate
		if (getData() != null && 
				getData().getAddress() != null && 
				getData().getCity() != null && 
				getData().getState() != null) 
		{
			final String displayAddress = getData().getAddress() 
					+ "\n" + getData().getCity() 
					+ ", " + getData().getState();
			final String searchableAddress = getData().getAddress() 
					+ "," + getData().getCity() 
					+ "," + getData().getState();
			LinearLayout navigateButton = layoutFactory.getConnectBadgeV3(
					R.drawable.ico_nav, displayAddress);
			navigateButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String fullEncodedAddr = null;
					try {
						fullEncodedAddr = URLEncoder.encode(searchableAddress, "UTF-8");

					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					Intent navIntent = new Intent(Intent.ACTION_VIEW, 
							Uri.parse("google.navigation:q=" + fullEncodedAddr));
					startActivity(navIntent);				
				}
			});

			infoSectionLayout.addView(navigateButton);
			infoSectionLayout.addView(layoutFactory.getHorizontalLine(true));
		}

		// call button
		if (getData() != null && getData().getPhoneNumber() != null) {

			final String rawNumber = getData().getPhoneNumber().trim();
			final String displayNumber = StringHelper.prettyPhoneNumberUS(rawNumber);

			LinearLayout callButton = layoutFactory.getConnectBadgeV3(
					R.drawable.ico_call, displayNumber);
			callButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String number = "tel:" + rawNumber;
					Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(number));
					startActivity(callIntent);				
				}
			});

			infoSectionLayout.addView(callButton);
			infoSectionLayout.addView(layoutFactory.getHorizontalLine(false));
		}

		// web button
		if (getData() != null && 
				getData().getWebSite() != null && 
				!getData().getWebSite().equalsIgnoreCase(Constants.NULL_STR)) 
		{
			final String browserWebSite = getData().getWebSite();
			final String displayWebSite = getData().getWebSite().replaceFirst("https://", "").replaceFirst("http://", "");
			LinearLayout webButton = layoutFactory.getConnectBadgeV3(
					R.drawable.ico_web, displayWebSite);
			webButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
							Uri.parse(browserWebSite));
					startActivity(browserIntent);		
				}
			});
			infoSectionLayout.addView(layoutFactory.getHorizontalLine());
			infoSectionLayout.addView(webButton);
		}

		// about section
		if (getData() != null && getData().getDescription() != null) {
			TextView aboutHeading = (TextView) findViewById(R.id.headingAbout);
			aboutHeading.setText("About " + getData().getName());

			TextView aboutBody = (TextView) findViewById(R.id.bodyAbout);
			aboutBody.setText(getData().getDescription());
		}
		
		// hide progress bar
		findViewById(R.id.progressBar).setVisibility(View.GONE);
		findViewById(R.id.progressText).setVisibility(View.GONE);

	}

}
