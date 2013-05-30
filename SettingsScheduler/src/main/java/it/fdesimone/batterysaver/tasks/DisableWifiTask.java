package it.fdesimone.batterysaver.tasks;

import java.util.Date;



import android.app.Service;
import android.content.Context;
import android.net.wifi.WifiManager;

import it.fdesimone.batterysaver.Log;
import it.fdesimone.batterysaver.R;

public class DisableWifiTask extends Task {

	public DisableWifiTask(Context context) throws NullPointerException {
		super(context);
	}


	@Override
	int getId() {
		return Tasks.id_DisableWifiTask;
	}

	@Override
	String getName() {
		return resoursec.getString(R.string.task_disableWifi);
	}

	@Override
	boolean execute() {
		if (Log.LOGV) Log.v("Disable WiFi executed @ " + new Date());
		
		WifiManager wifiManager = (WifiManager) serviceContext.getSystemService(Service.WIFI_SERVICE);
		
		if (Log.LOGV) {
			Log.v("wifiManager " + wifiManager);
			Log.v("wifiManager " + wifiManager.isWifiEnabled());
		}
		
		if ( wifiManager.isWifiEnabled() )
			wifiManager.setWifiEnabled(false);
		
		if (Log.LOGV) Log.v("wifiManager " + wifiManager.isWifiEnabled());
		
		showInfo();
		
		return true;
	}

}
