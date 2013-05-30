package it.fdesimone.batterysaver;

import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;


/**
 * @author Francesco De Simone
 */
public class JobGroup implements Parcelable {

	static final String JOBGROUP_ID = "jobgroup_id";
	
	private int id;
    private int hour;
    private int minutes;
    private DaysOfWeek daysOfWeek;
	private boolean enabled;
	private String label;
	private List<String> jobActionIDs;
	
	public JobGroup() {
		id = -1;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        hour = c.get(Calendar.HOUR_OF_DAY);
        minutes = c.get(Calendar.MINUTE);
        daysOfWeek = new DaysOfWeek(127);
        enabled = false;
	}
	
	public JobGroup(Cursor c) {
		id = c.getInt(Columns.INDEX_ID);
		hour = c.getInt(Columns.INDEX_HOUR);
		minutes = c.getInt(Columns.INDEX_MINUTES);
		daysOfWeek = new DaysOfWeek(c.getInt(Columns.INDEX_DAYS_OF_WEEK));
		enabled = c.getInt(Columns.INDEX_ENABLED) == 1;
		label = c.getString(Columns.INDEX_LABEL);
		jobActionIDs = decodeJobActionsIDs(c.getString(Columns.INDEX_JOB_ACTION_IDS));
	}
	
//	public JobGroup(Parcel p) {
//		id = p.readInt();
//		hour = p.readInt();
//		minutes = p.readInt();
//		daysOfWeek = new DaysOfWeek(p.readInt());
//		enabled = p.readInt() == 1;
//		label = p.readString();
//		jobActionIDs = decodeJobActionsIDs(p.readString());
//	}
	
	public long getNextRunDelay() {
		Calendar now = Calendar.getInstance();
		Calendar c = Calendar.getInstance();
		
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minutes);
		c.set(Calendar.SECOND, 0);
		int days = daysOfWeek.getNextAlarm(now);
		
		if ( days > 0 ) {
			c.add(Calendar.DATE, days);
		}
		
		if ( c.before(now) ) {
			for ( ;; ) {
				c.add(Calendar.DATE, 1);
				if ( daysOfWeek.getNextAlarm(c) == 0 )
					break;
			}
		}
		
		long ret = c.getTimeInMillis() - now.getTimeInMillis();
		
		if ( ret < 1000 ) { // less than a second, schedule for next day
			for ( ;; ) {
				c.add(Calendar.DATE, 1);
				if ( daysOfWeek.getNextAlarm(c) == 0 )
					break;
			}
			ret = c.getTimeInMillis() - now.getTimeInMillis();
		}
		
		return ret;
	}
	
	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public int getMinutes() {
		return minutes;
	}

	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}

	public DaysOfWeek getDaysOfWeek() {
		return daysOfWeek;
	}

	public void setDaysOfWeek(DaysOfWeek daysOfWeek) {
		this.daysOfWeek = daysOfWeek;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public List<String> getJobActionIDs() {
		return jobActionIDs;
	}
	
	public boolean containsJobActionId(int jobActionId) {
		if ( jobActionIDs != null ) {
			return jobActionIDs.contains(String.valueOf(jobActionId));
		}
		else return false;
	}
	
	public void setJobActionIDs(List<String> jobActionIDs) {
		this.jobActionIDs = jobActionIDs;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeInt(hour);
		dest.writeInt(minutes);
		dest.writeInt(daysOfWeek.getCoded());
		dest.writeInt(enabled ? 1 : 0);
		dest.writeString(label);
		dest.writeString(encodeJobActionIDs());
	}
	
	public String encodeJobActionIDs() {
		if ( jobActionIDs == null || jobActionIDs.isEmpty() )
			return "";
		else {
			StringBuilder sb = new StringBuilder();
			for ( int i = 0; i < jobActionIDs.size(); i++ ) {
				sb.append(jobActionIDs.get(i));
				if ( i < jobActionIDs.size() - 1 )
					sb.append(",");
			}
			return sb.toString();
		}
	}
	
	private List<String> decodeJobActionsIDs(String jobActionIDs) {
		if ( jobActionIDs == null || jobActionIDs.equals("") )
			return null;
		else {
			return Arrays.asList(jobActionIDs.split(","));
			
		}
	}

	public static class Columns implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://it.fdesimone.batterysaver/jobgroups");
		public static final String HOUR = "hour";
		public static final String MINUTES = "minutes";
		public static final String DAYS_OF_WEEK = "days_of_week";
		public static final String ENABLED = "enabled";
		public static final String LABEL = "label";
		public static final String JOB_ACTION_IDS = "job_action_ids";
		
		static final String[] JOBGROUPS_QUERY_COLUMNS = { _ID, HOUR, MINUTES, DAYS_OF_WEEK, ENABLED, LABEL, JOB_ACTION_IDS };
		
		public static final int INDEX_ID = 0;
		public static final int INDEX_HOUR = 1;
		public static final int INDEX_MINUTES = 2;
		public static final int INDEX_DAYS_OF_WEEK = 3;
		public static final int INDEX_ENABLED = 4;
		public static final int INDEX_LABEL = 5;
		public static final int INDEX_JOB_ACTION_IDS = 6;
	}
	
    /*
     * Days of week code as a single int.
     * 0x00: no day
     * 0x01: Monday
     * 0x02: Tuesday
     * 0x04: Wednesday
     * 0x08: Thursday
     * 0x10: Friday
     * 0x20: Saturday
     * 0x40: Sunday
     */
    static final class DaysOfWeek {

        private static int[] DAY_MAP = new int[] {
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY,
            Calendar.SUNDAY,
        };

        // Bitmask of all repeating days
        private int mDays;

        DaysOfWeek(int days) {
            mDays = days;
        }

        public String toString(Context context) {
            StringBuilder ret = new StringBuilder();

            // no days
            if (mDays == 0) {
        		return context.getText(R.string.never).toString();
            }

            // every day
            if (mDays == 0x7f) {
                return context.getText(R.string.every_day).toString();
            }

            // count selected days
            int dayCount = 0, days = mDays;
            while (days > 0) {
                if ((days & 1) == 1) dayCount++;
                days >>= 1;
            }

            // short or long form?
            DateFormatSymbols dfs = new DateFormatSymbols();
            String[] dayList = (dayCount > 1) ?
                    dfs.getShortWeekdays() :
                    dfs.getWeekdays();

            // selected days
            for (int i = 0; i < 7; i++) {
                if ((mDays & (1 << i)) != 0) {
                    ret.append(dayList[DAY_MAP[i]]);
                    dayCount -= 1;
                    if (dayCount > 0) ret.append(
                            context.getText(R.string.day_concat));
                }
            }
            return ret.toString();
        }

        private boolean isSet(int day) {
            return ((mDays & (1 << day)) > 0);
        }

        public void set(int day, boolean set) {
            if (set) {
                mDays |= (1 << day);
            } else {
                mDays &= ~(1 << day);
            }
        }

        public void set(DaysOfWeek dow) {
            mDays = dow.mDays;
        }

        public int getCoded() {
            return mDays;
        }

        // Returns days of week encoded in an array of booleans.
        public boolean[] getBooleanArray() {
            boolean[] ret = new boolean[7];
            for (int i = 0; i < 7; i++) {
                ret[i] = isSet(i);
            }
            return ret;
        }

//        public boolean isRepeatSet() {
//            return mDays != 0;
//        }

        /**
         * returns number of days from today until next alarm
         * @param c must be set to today
         */
        public int getNextAlarm(Calendar c) {
            if (mDays == 0) {
                return -1;
            }

            int today = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;

            int day = 0;
            int dayCount = 0;
            for (; dayCount < 7; dayCount++) {
                day = (today + dayCount) % 7;
                if (isSet(day)) {
                    break;
                }
            }
            return dayCount;
        }
    }
    
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append(isEnabled() ? "": "<DISABLED> ").append("JobGroup #").append(id).append(" @ ").append(hour).append(":").append(minutes).append(" tasks count: ").append(jobActionIDs != null ? jobActionIDs.size() : "0");
    	return sb.toString();
    }
}
