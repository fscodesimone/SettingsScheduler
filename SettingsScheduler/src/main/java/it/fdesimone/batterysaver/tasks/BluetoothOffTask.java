package it.fdesimone.batterysaver.tasks;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;

import it.fdesimone.batterysaver.Log;
import it.fdesimone.batterysaver.R;

/**
 * Created by Francesco De Simone on 30/05/13.
 */
public class BluetoothOffTask extends Task {

    public BluetoothOffTask(Context context){

        super(context);
    }

    @Override
    int getId() {
        return Tasks.id_DisableBluehtooth;
    }

    @Override
    String getName() {
        return resoursec.getString(R.string.task_BluetoothOff);
    }

    @Override
    boolean execute() {
        if(Log.LOGV) Log.v("Executing task "+resoursec.getString(R.string.task_BluetoothOn));
        boolean result = BluetoothAdapter.getDefaultAdapter().disable();
        showInfo();
        return result;
    }
}
