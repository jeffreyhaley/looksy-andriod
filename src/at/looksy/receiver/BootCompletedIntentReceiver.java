package at.looksy.receiver;

import java.util.concurrent.TimeUnit;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import at.looksy.core.Constants;
import at.looksy.service.NotificationService;

public class BootCompletedIntentReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent incomingIntent) {
		if ("android.intent.action.BOOT_COMPLETED".equals(incomingIntent.getAction())) {
			
			Intent intent = new Intent(context, NotificationService.class);
			PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0,
			    TimeUnit.SECONDS.toMillis(Constants.WIFI_SCAN_INTERVAL), pendingIntent);
		}  

	}

}
