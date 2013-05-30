package it.fdesimone.batterysaver.tasks;

import android.content.Context;
import android.net.ConnectivityManager;
import java.lang.reflect.Method;
import java.util.Date;

import it.fdesimone.batterysaver.Log;
import it.fdesimone.batterysaver.R;

/**
 * Created by Francesco De Simone on 29/05/13.
 */
public class DataConnectionOnTask extends Task {



    public DataConnectionOnTask(Context context) throws NullPointerException {
        super(context);
    }

    @Override
    int getId() {
        return Tasks.id_EnableDataConnection;
    }

    @Override
    String getName() {
        return resoursec.getString(R.string.task_enable3G);
    }

    @Override
    boolean execute() {
        if (Log.LOGV) Log.v("ENABLE 3G connection executed @ " + new Date());
        ConnectivityManager dataManager;
        dataManager  = (ConnectivityManager)serviceContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        try{
            Method dataMtd = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
            dataMtd.setAccessible(true);
            dataMtd.invoke(dataManager, true);

        }catch (Exception e){
            Log.v("Error : "+ e.getMessage());
            return  false;
        }
        showInfo();
        return true;
    }
}
