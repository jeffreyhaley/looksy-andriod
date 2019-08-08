package at.looksy.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import at.looksy.R;


public class EnableWifiDialog extends AlertDialog {
	
	private Context context;
	
	public EnableWifiDialog(Context context) {
		super(context);
		
		this.context = context;
		
		new AlertDialog.Builder(context)
		.setTitle(R.string.dialog_enable_wifi_title)
		.setMessage(R.string.dialog_enable_wifi_message)
		.setPositiveButton(R.string.dialog_enable_wifi_button, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				enableWifi();
			}
		})
		.create().show();
	}
	
	private void enableWifi() {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		wifiManager.setWifiEnabled(true);
	}
}
