package it.fdesimone.batterysaver.tasks;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;

import it.fdesimone.batterysaver.Log;
import it.fdesimone.batterysaver.R;

/**
 * Created by Francesco De Simone on 30/05/13.
 */
public class BluetoothOnTask extends  Task {

    public BluetoothOnTask(Context context){
        super(context);
    }
    @Override
    int getId() {
        return Tasks.id_EnableBluehtooth;
    }

    @Override
    String getName() {
        return resoursec.getString(R.string.task_BluetoothOn);
    }

    @Override
    boolean execute() {
        if(Log.LOGV) Log.v("Executing task "+resoursec.getString(R.string.task_BluetoothOn));
        boolean result = BluetoothAdapter.getDefaultAdapter().enable();
        showInfo();
        return result;
    }
}
