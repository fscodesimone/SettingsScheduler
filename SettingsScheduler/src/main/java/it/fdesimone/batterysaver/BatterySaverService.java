package it.fdesimone.batterysaver;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;

import it.fdesimone.batterysaver.tasks.Tasks;


/**
 * @author Francesco De Simone
 */
public class
        BatterySaverService extends Service {

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	private Map<Integer, ScheduledFuture<?>> handles;
	
	private String jobGroupCompleted = "jobgroup_completed";
	
	private final Intent jobGroupCompletedIntent = new Intent(jobGroupCompleted);
	private final Handler handler = new Handler();
	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, final Intent intent) {
			handler.post(new Runnable() {
				
				public void run() {
					
					if ( intent.getAction().equals(jobGroupCompleted) ) {
						
						int jobGroupId = intent.getIntExtra("id", -1);
						
						Log.v("jobGroupCompleted id = " + jobGroupId);
						
						if ( jobGroupId != -1 ) {
							
							JobGroup jobGroup = JobGroups.getJobGroup(getContentResolver(), jobGroupId);
							
							Log.v(jobGroup.toString());
							
							
							List<String> tasks = jobGroup.getJobActionIDs();
							
							if ( jobGroup.isEnabled() && 
									tasks != null &&
									tasks.size() > 0 &&
									jobGroup.getDaysOfWeek().getCoded() > 0 ) {
								
								long delay = jobGroup.getNextRunDelay();
								Log.v("delay(" + delay + ") = " + formatMillis(delay));
								handles.remove(jobGroupId);
								handles.put(jobGroupId, scheduler.schedule(createTask(getApplicationContext(), tasks, jobGroupId), delay, TimeUnit.MILLISECONDS));
								
							}
						}
						
					}
					else {
						Log.v("broadcast: restart");
						restart();
					}
				}
			});
		}
	};
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.v("onCreate");
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_TIME_CHANGED);
		filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		filter.addAction("crontab_restart");
		filter.addAction(jobGroupCompleted);
		registerReceiver(receiver, filter);
		
		handles = new HashMap<Integer, ScheduledFuture<?>>();
		
		for ( JobGroup jobGroup : JobGroups.getAllJobGroups(getApplicationContext()) ) {
			
			Log.v(jobGroup.toString());
			
			List<String> tasks = jobGroup.getJobActionIDs();
			
			if ( jobGroup.isEnabled() && 
					tasks != null &&
					tasks.size() > 0 &&
					jobGroup.getDaysOfWeek().getCoded() > 0 ) {
				
				long delay = jobGroup.getNextRunDelay();
				Log.v("delay(" + delay + ") = " + formatMillis(delay));
				int jobGroupId = jobGroup.getId();
				
				handles.put(jobGroupId, scheduler.schedule(createTask(getApplicationContext(), tasks, jobGroupId), delay, TimeUnit.MILLISECONDS));
				
			}
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		Log.v("onDestroy");
		
		unregisterReceiver(receiver);
		
		if ( !handles.isEmpty() ) {
			
			for ( Iterator<ScheduledFuture<?>> iter = handles.values().iterator(); iter.hasNext(); ) {
				iter.next().cancel(true);
			}
			
			handles.clear();
			handles = null;
		}
		
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void restart() {
		Log.v("restart service!");
		stopSelf();
		startService(new Intent(this, BatterySaverService.class));
	}
	
	private Runnable createTask(final Context context, final List<String> tasks, final int jobGroupId) {
		return new Runnable() {
			public void run() {
				for ( String taskId : tasks ) {
					Tasks.executeTaskId(Integer.valueOf(taskId), context);
				}
				
				jobGroupCompletedIntent.putExtra("id", jobGroupId);
				context.sendBroadcast(jobGroupCompletedIntent);
			}
		};
	}
	
	private String formatMillis(long aMillis ) {
		int days = (int) ( aMillis / (1000 * 60 * 60 * 24 ));
		long rest = aMillis  % ( 1000 * 60 * 60 * 24 );
		int hours = (int) ( rest  / ( 1000 * 60 * 60 ) );
		rest = rest % ( 1000 * 60 * 60 );
		int minutes = (int) ( rest / ( 1000 * 60 ) );
		rest = rest % ( 1000 * 60 );
		int seconds = (int) ( rest / 1000 );
		int millis = (int) ( rest % 1000 );
		String format = "%2ddays %02dh %02dm %02ds %02dms";
		return String.format(format, days, hours, minutes, seconds, millis);
	}
}
