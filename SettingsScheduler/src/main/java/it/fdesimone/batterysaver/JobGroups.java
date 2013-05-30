package it.fdesimone.batterysaver;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.DateFormat;
/**
 * @author Francesco De Simone
 */
public class JobGroups {

	private static final Intent crontabRestart = new Intent("crontab_restart");
	
	public static void deleteJobGroup(Context context, int jobGroupId) {
		ContentResolver contentResolver = context.getContentResolver();
		Uri uri = ContentUris.withAppendedId(JobGroup.Columns.CONTENT_URI, jobGroupId);
		contentResolver.delete(uri, "", null);
		
		context.sendBroadcast(crontabRestart);
		
	}
	
	public static void addJobGroup(Context context, JobGroup jobGroup) {
		Uri uri = context.getContentResolver().insert(
                JobGroup.Columns.CONTENT_URI, buildContentValues(jobGroup));
		jobGroup.setId((int) ContentUris.parseId(uri));
		
		context.sendBroadcast(crontabRestart);
	}
	
	public static void updateJobGroup(Context context, int jobGroupId, ContentValues cv) {
		context.getContentResolver().update(
                ContentUris.withAppendedId(JobGroup.Columns.CONTENT_URI, jobGroupId),
                cv, null, null);
		
		context.sendBroadcast(crontabRestart);
	}
	
	public static void updateJobGroup(Context context, JobGroup jobGroup) {
		context.getContentResolver().update(
                ContentUris.withAppendedId(JobGroup.Columns.CONTENT_URI, jobGroup.getId()),
                buildContentValues(jobGroup), null, null);
		
		context.sendBroadcast(crontabRestart);
	}
	
	public static List<JobGroup> getAllJobGroups(Context context) {
		Cursor c = getJobGroupsCursor(context.getContentResolver());
		
		if ( c != null ) {
			List<JobGroup> result = new ArrayList<JobGroup>(c.getCount());
			if ( c.moveToFirst() ) {
				for ( ;; ) {
					result.add(new JobGroup(c));
					
					if ( !c.moveToNext() )
						break;
				}
			}
			
			c.close();
			
			return result;
		}
		else
			return new ArrayList<JobGroup>(0);
		
	}
	
	private static ContentValues buildContentValues(JobGroup jobGroup) {
		ContentValues cv = new ContentValues();
		cv.put(JobGroup.Columns.HOUR, jobGroup.getHour());
		cv.put(JobGroup.Columns.MINUTES, jobGroup.getMinutes());
		cv.put(JobGroup.Columns.ENABLED, jobGroup.isEnabled());
		cv.put(JobGroup.Columns.DAYS_OF_WEEK, jobGroup.getDaysOfWeek().getCoded());
		cv.put(JobGroup.Columns.JOB_ACTION_IDS, jobGroup.encodeJobActionIDs());
		cv.put(JobGroup.Columns.LABEL, jobGroup.getLabel());
		return cv;
	}
	
	public static JobGroup getJobGroup(ContentResolver contentResolver, int jobGroupId) {
        Cursor cursor = contentResolver.query(
                ContentUris.withAppendedId(JobGroup.Columns.CONTENT_URI, jobGroupId),
                JobGroup.Columns.JOBGROUPS_QUERY_COLUMNS,
                null, null, null);
        JobGroup jobGroup = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
            	jobGroup = new JobGroup(cursor);
            }
            cursor.close();
        }
        return jobGroup;
    }
	
	public static Cursor getJobGroupsCursor(ContentResolver contentResolver) {
		return contentResolver.query(
                JobGroup.Columns.CONTENT_URI, JobGroup.Columns.JOBGROUPS_QUERY_COLUMNS,
                null, null, null);
	}
	
	private final static String M12 = "h:mm aa";
    // Shared with DigitalClock
    final static String M24 = "kk:mm";
	
    static String formatTime(final Context context, int hour, int minute,
            JobGroup.DaysOfWeek daysOfWeek) {
		Calendar c = calculateAlarm(hour, minute, daysOfWeek);
		return formatTime(context, c);
	}
    
	static String formatTime(final Context context, Calendar c) {
        String format = get24HourMode(context) ? M24 : M12;
        return (c == null) ? "" : (String)DateFormat.format(format, c);
    }
	
	static boolean get24HourMode(final Context context) {
        return DateFormat.is24HourFormat(context);
    }
	
	/**
     * Given an alarm in hours and minutes, return a time suitable for
     * setting in AlarmManager.
     */
    static Calendar calculateAlarm(int hour, int minute,
            JobGroup.DaysOfWeek daysOfWeek) {

        // start with now
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());

        int nowHour = c.get(Calendar.HOUR_OF_DAY);
        int nowMinute = c.get(Calendar.MINUTE);

        // if alarm is behind current time, advance one day
        if (hour < nowHour  ||
            hour == nowHour && minute <= nowMinute) {
            c.add(Calendar.DAY_OF_YEAR, 1);
        }
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        int addDays = daysOfWeek.getNextAlarm(c);
        if (addDays > 0) c.add(Calendar.DAY_OF_WEEK, addDays);
        return c;
    }
	
}
