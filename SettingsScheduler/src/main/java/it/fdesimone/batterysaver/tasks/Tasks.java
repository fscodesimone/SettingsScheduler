package it.fdesimone.batterysaver.tasks;

import android.content.Context;

import it.fdesimone.batterysaver.Log;

public final class Tasks {

	public static final int id_EnableWifiTask = 0;
	public static final int id_DisableWifiTask = 1;
    public static final int id_DisableDataConnection = 2;
    public static final int id_EnableDataConnection = 3;
    public static final int id_EnableBluehtooth = 4;
    public static final int id_DisableBluehtooth = 5;
	
	public static boolean executeTaskId(int taskId, Context context) {
        if (Log.LOGV) Log.v("Executing job "+taskId);
		boolean result = false;
		
		switch (taskId) {
			case id_EnableWifiTask:
				result = new EnableWifiTask(context).execute();
				break;
			case id_DisableWifiTask:
				result = new DisableWifiTask(context).execute();
				break;
            case id_DisableDataConnection:
                result = new DataConnectionOffTask(context).execute();
                break;
            case id_EnableDataConnection:
                result = new DataConnectionOnTask(context).execute();
                break;
            case id_DisableBluehtooth:
                result = new BluetoothOffTask(context).execute();
                break;
            case id_EnableBluehtooth:
                result = new BluetoothOnTask(context).execute();
                break;

			}
		
		return result;
			
	}
	
}
