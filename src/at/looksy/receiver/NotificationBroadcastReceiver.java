package at.looksy.receiver;

import java.util.Date;

import at.looksy.BuildConfig;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import at.looksy.core.Constants;
import at.looksy.manager.PreferenceManager;


public class NotificationBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent incomingIntent) {
		if (Constants.BROADCAST_NOTIFICATION_CANCELLED
				.equals(incomingIntent.getAction())) 
		{
			if (BuildConfig.DEBUG)
				Log.d(Constants.DEBUG_TAG, "NotificationBroadcastReceiver.onReceive called");
			
			new PreferenceManager(context).setPreference(
					Constants.PREF_NOTIF_LAST_DISMISSED, 
					String.valueOf(new Date().getTime()));
		}  

	}

}
