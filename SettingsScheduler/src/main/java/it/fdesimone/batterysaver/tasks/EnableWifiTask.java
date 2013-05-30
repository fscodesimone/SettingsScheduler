package it.fdesimone.batterysaver.tasks;

import java.util.Date;


import android.app.Service;
import android.content.Context;
import android.net.wifi.WifiManager;

import it.fdesimone.batterysaver.Log;
import it.fdesimone.batterysaver.R;

public class EnableWifiTask extends Task {

	public EnableWifiTask(Context context) throws NullPointerException {
		super(context);
	}

	@Override
	int getId() {
		return Tasks.id_EnableWifiTask;
	}

	@Override
	String getName() {
		return resoursec.getString(R.string.task_enableWifi);
	}

	@Override
	boolean execute() {
		if (Log.LOGV) Log.v("Enable WiFi executed @ " + new Date());
		
		WifiManager wifiManager = (WifiManager) serviceContext.getSystemService(Service.WIFI_SERVICE);
		
		if (Log.LOGV) {
			Log.v("wifiManager " + wifiManager);
			Log.v("wifiManager " + wifiManager.isWifiEnabled());
		}
		
		if ( !wifiManager.isWifiEnabled() )
			wifiManager.setWifiEnabled(true);
		
		if (Log.LOGV) Log.v("wifiManager " + wifiManager.isWifiEnabled());
		
		showInfo();
		
		return true;
	}

}
