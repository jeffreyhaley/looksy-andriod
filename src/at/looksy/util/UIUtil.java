package at.looksy.util;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import at.looksy.R;
import at.looksy.service.data.WifiAccessPointData.SignalStrength;

public class UIUtil {
	
	public static void setWifiProximityIndicator(SignalStrength signalStrength, 
			ImageView imageView) 
	{
		switch (signalStrength) {
		case FOUR_BARS:
			imageView.setVisibility(View.VISIBLE);
			imageView.setImageResource(R.drawable.ico_signal_4_bars);
			break;
		case THREE_BARS:
			imageView.setVisibility(View.VISIBLE);
			imageView.setImageResource(R.drawable.ico_signal_3_bars);
			break;
		case TWO_BARS:
			imageView.setVisibility(View.VISIBLE);
			imageView.setImageResource(R.drawable.ico_signal_2_bars);
			break;
		case ONE_BAR:
			imageView.setVisibility(View.VISIBLE);
			imageView.setImageResource(R.drawable.ico_signal_1_bars);
			break;
		case OUT_OF_RANGE:
		case UNKNOWN:
			// don't show
			break;
		}
	}
	
	public static void setupMap(Activity activity, GoogleMap googleMap, double lat, double lng, int zoom) {
		if (lat == 0 && lng == 0) {
			activity.findViewById(R.id.sectionMap).setVisibility(View.GONE);

		} else {
			LatLng loc = new LatLng(lat, lng);

			CameraPosition cameraPosition = 
					new CameraPosition.Builder()
			.target(loc)
			.zoom(zoom)
			.build();

			googleMap.clear();
			googleMap.addMarker(new MarkerOptions()
								.position(loc)
								.icon(BitmapDescriptorFactory.fromResource(
										zoom >= 11 ? 
												R.drawable.ico_circle_red : 
													R.drawable.ico_circle_red_small)));
			googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			googleMap.getUiSettings().setCompassEnabled(false);
			googleMap.getUiSettings().setRotateGesturesEnabled(false);
			googleMap.getUiSettings().setZoomControlsEnabled(false);
			googleMap.getUiSettings().setTiltGesturesEnabled(false);
			googleMap.getUiSettings().setScrollGesturesEnabled(false);
			googleMap.getUiSettings().setZoomGesturesEnabled(false);
			googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
		}
	}

}
