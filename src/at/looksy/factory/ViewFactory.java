package at.looksy.factory;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import at.looksy.R;
import at.looksy.util.Util;


public class ViewFactory {
	
	private Activity activity;
	
	public ViewFactory(Activity activity) {
		this.activity = activity;
	}
	
	public View getHorizontalLine() {
		View line = activity.getLayoutInflater().inflate(R.layout.snippet_line, null);
		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, 
				Util.dp2px(activity.getApplicationContext(), 1));
		
		line.setLayoutParams(layoutParams);
		
		return line;
	}
	
	public View getHorizontalLine(boolean isVisible) {
		int visibility = isVisible ? View.VISIBLE : View.GONE;
		View view = getHorizontalLine();
		view.setVisibility(visibility);
		return view;
	}
	
	public View getVerticalLine()
	{
		LinearLayout layout =
				(LinearLayout) activity.getLayoutInflater().inflate(
						R.layout.snippet_line_vertical, null);
		
		LinearLayout.LayoutParams params = 
				new LinearLayout.LayoutParams(
						0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
		layout.setLayoutParams(params);
		
		return layout;
	}
	
	public LinearLayout getConnectBadge()
	{
		return (LinearLayout) activity.getLayoutInflater().inflate(R.layout.snippet_connect_badge, null);
	}
	
	public LinearLayout getConnectBadgeV2()
	{
		LinearLayout layout =
				(LinearLayout) activity.getLayoutInflater().inflate(
						R.layout.snippet_connect_badge_v2, null);
		
		LinearLayout.LayoutParams params = 
				new LinearLayout.LayoutParams(
						0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
		layout.setLayoutParams(params);
		
		return layout;
	}
	
	public LinearLayout getConnectBadgeV3(int iconResId, String text)
	{
		LinearLayout layout = 
				(LinearLayout) activity.getLayoutInflater().
					inflate(R.layout.snippet_connect_badge_v3, null);
		((ImageView)layout.findViewById(R.id.icon)).setImageResource(iconResId);
		((TextView)layout.findViewById(R.id.text)).setText(text);
		return layout;
	}
	
	public LinearLayout getAboutSection()
	{
		LinearLayout layout = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.snippet_about_section, null);
		
		int marginSide = Util.dp2px(activity.getApplicationContext(), 20);
		int marginTop = Util.dp2px(activity.getApplicationContext(), 10);
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(marginSide, marginTop, marginSide, 0);
		
		layout.setLayoutParams(params);
		
		return layout;
	}
	
	public LinearLayout getSearchResultEntry()
	{
		LinearLayout layout = 
				(LinearLayout) 
				activity.getLayoutInflater().inflate(
						R.layout.snippet_loved_location_entry, null);
		
		int marginSide = Util.dp2px(activity.getApplicationContext(), 20);
		int marginTop = Util.dp2px(activity.getApplicationContext(), 5);
		
		LinearLayout.LayoutParams params = 
				new LinearLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(marginSide, marginTop, marginSide, 0);
		
		layout.setLayoutParams(params);
		
		return layout;
	}
	
	public LinearLayout getWifiListingEntry()
	{
		LinearLayout view = 
				(LinearLayout) 
				activity.getLayoutInflater().inflate(
						R.layout.snippet_wifi_listing_entry, null);
		
		// set width
		LinearLayout.LayoutParams layout = 
				new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT, 
						LinearLayout.LayoutParams.WRAP_CONTENT);

		layout.setMargins(0, Util.dp2px(activity, 15), 0, 0);

		view.setLayoutParams(layout);
				
		return view;
	}
	
	public ImageView getHeartView()
	{
		ImageView imageView = new ImageView(activity.getApplicationContext());
		imageView.setImageResource(R.drawable.ico_heart_on);
		return imageView;
	}
	
	public LinearLayout getHoursEntry(String day, String hoursRange)
	{
		LinearLayout view = 
				(LinearLayout) 
				activity.getLayoutInflater().inflate(
						R.layout.snippet_hours_entry, null);
		
		// set width/height
		LinearLayout.LayoutParams layout = 
				new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT, 
						LinearLayout.LayoutParams.WRAP_CONTENT);
		view.setLayoutParams(layout);
		
		// set content
		((TextView) view.findViewById(R.id.day)).setText(day);
		((TextView) view.findViewById(R.id.hoursRange)).setText(hoursRange);
				
		return view;
	}

}
