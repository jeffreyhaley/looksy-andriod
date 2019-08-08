package at.looksy.manager;

import android.content.Context;
import android.content.SharedPreferences;
import at.looksy.core.Constants;



public class PreferenceManager {
	
	private Context context;
	
	public PreferenceManager(Context context) {
		this.context = context;
	}
	
	public void setPreference(String pref, String value) {
		SharedPreferences contactPrefs = context.getSharedPreferences(
				Constants.PREF_PROCESS_STATE, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = contactPrefs.edit();
		editor.putString(pref, value);	
		editor.commit();
	}
	
	public String getPreference(String pref) {
		SharedPreferences contactPrefs = context.getSharedPreferences(
				Constants.PREF_PROCESS_STATE, Context.MODE_PRIVATE);
		return contactPrefs.getString(pref, null);
	}

}
