package it.fdesimone.batterysaver;

import android.util.Config;
/**
 * @author Francesco De Simone
 */
public class Log {
	
    public final static String LOGTAG = "SettingsScheduler";

    public static final boolean LOGV = true;

    public static void v(String logMe) {
        android.util.Log.v(LOGTAG, logMe);
    }
}
