package in.teacher.adapter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

import in.teacher.util.NetworkUtils;

public class WifiConnect extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		boolean isNetworkConnected = NetworkUtils.isNetworkConnected(context);
		context.sendBroadcast(new Intent("WIFI_STATUS"));
		if(!isNetworkConnected){
			WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
			wifiManager.setWifiEnabled(true);
		}
	}
}
