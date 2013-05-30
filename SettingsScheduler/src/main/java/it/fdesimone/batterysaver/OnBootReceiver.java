package it.fdesimone.batterysaver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OnBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			if ( Utils.isServiceEnabledOnBoot(context) ) {
				context.startService(new Intent(context, BatterySaverService.class));
			}
		}
	}

}
