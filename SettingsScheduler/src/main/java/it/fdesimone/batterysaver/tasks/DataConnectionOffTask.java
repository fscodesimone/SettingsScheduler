package it.fdesimone.batterysaver.tasks;

import android.content.Context;
import android.net.ConnectivityManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

import it.fdesimone.batterysaver.Log;
import it.fdesimone.batterysaver.R;

/**
 * Created by Francesco De Simone on 29/05/13.
 */
public class DataConnectionOffTask extends  Task {

    public DataConnectionOffTask(Context context) throws  NullPointerException{

        super(context);
    }
    @Override
    int getId() {
        return Tasks.id_DisableDataConnection;
    }

    @Override
    String getName() {
        return resoursec.getString(R.string.task_disable3G);
    }

    @Override
    boolean execute() {
        if (it.fdesimone.batterysaver.Log.LOGV) Log.v("Disable 3G connection executed @ " + new Date());
        ConnectivityManager dataManager;
        dataManager  = (ConnectivityManager)serviceContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        try{
           Method dataMtd = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
           dataMtd.setAccessible(true);
           dataMtd.invoke(dataManager, false);


        }catch (Exception e){
            Log.v("ERRORE 3G setting  : " + e);
            return  false;
        }
        showInfo();
        return true;
    }


}
