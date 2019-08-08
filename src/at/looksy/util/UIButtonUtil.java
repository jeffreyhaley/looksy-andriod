package at.looksy.util;

import at.looksy.R;

import android.widget.TextView;


public class UIButtonUtil {
	
	public static void buttonOff(TextView tv, String text) {
		tv.setTextAppearance(tv.getContext(), R.style.ButtonNormalOff);
		tv.setBackgroundResource(R.drawable.btn_normal_off);
		tv.setText(text);
	}
	
	public static void buttonOn(TextView tv, String text) {
		tv.setTextAppearance(tv.getContext(), R.style.ButtonNormalOn);
		tv.setBackgroundResource(R.drawable.btn_normal_on);
		tv.setText(text);
	}
	
	public static void buttonSelected(TextView tv, String text) {
		tv.setTextAppearance(tv.getContext(), R.style.ButtonNormalSelected);
		tv.setBackgroundResource(R.drawable.btn_normal_selected);
		tv.setText(text);
	}
	
	public static void buttonSmallOff(TextView tv, String text) {
		tv.setTextAppearance(tv.getContext(), R.style.ButtonSmallOff);
		tv.setBackgroundResource(R.drawable.btn_small_off);
		tv.setText(text);
	}
	
	public static void buttonSmallOn(TextView tv, String text) {
		tv.setTextAppearance(tv.getContext(), R.style.ButtonSmallOn);
		tv.setBackgroundResource(R.drawable.btn_small_on);
		tv.setText(text);
	}
	
	public static void buttonSmallSelected(TextView tv, String text) {
		tv.setTextAppearance(tv.getContext(), R.style.ButtonSmallOn);
		tv.setBackgroundResource(R.drawable.btn_small_selected);
		tv.setText(text);
	}

}
