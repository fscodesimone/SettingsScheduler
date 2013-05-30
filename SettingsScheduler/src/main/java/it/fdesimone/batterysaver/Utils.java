package it.fdesimone.batterysaver;

import java.io.File;
import java.io.IOException;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * @author Francesco De Simone
 */
public class Utils {

	private static Handler handler = new Handler(Looper.getMainLooper());
	
	public static void showInfoShort(final Context context, final String msg) {
		handler.post(new Runnable() {
			public void run() {
				Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	public static void showInfoLong(final Context context, final String msg) {
		handler.post(new Runnable() {
			public void run() {

                //Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                Notification noti = new Notification.Builder(context)
                        .setContentTitle("Eseguito il Task")
                        .setContentText(msg)
                        .setSmallIcon(R.drawable.ic_launcher).build();


                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

// Hide the notification after its selected
                noti.flags |= Notification.FLAG_AUTO_CANCEL;

                notificationManager.notify(0, noti);
            }
		});
	}
	
	private static File enabled_flag;
	
	private static void cacheEnabledFlagFile(Context context) {
		if ( enabled_flag == null ) {
			enabled_flag = new File(context.getDir("data", Context.MODE_PRIVATE), ".enabled");
		}
	}
	
	public static boolean isServiceEnabledOnBoot(Context context) {
		boolean ret = false;
		
		cacheEnabledFlagFile(context);
		
		ret = enabled_flag.exists();
		
		return ret;
	}
	
	public static void setServiceEnabledOnBoot(Context context) {
		cacheEnabledFlagFile(context);
		try {
			enabled_flag.createNewFile();
		} catch (IOException e) {}
	}
	
	public static void setServiceDisabledOnBoot(Context context) {
		cacheEnabledFlagFile(context);
		enabled_flag.delete();
	}
	
}
