package it.fdesimone.batterysaver.tasks;



import android.content.Context;
import android.content.res.Resources;

import it.fdesimone.batterysaver.Utils;

/**
 * @author De Simone Francesco
 */
public abstract class Task {

	protected Context serviceContext;
	protected Resources resoursec;
	
	public Task(Context serviceContext) throws NullPointerException {
		if ( serviceContext == null )
			throw new NullPointerException();
		
		this.serviceContext = serviceContext;
		this.resoursec = serviceContext.getResources();
	}
	
	abstract int getId();
	abstract String getName();
	abstract boolean execute();
	
	protected void showInfo() {
		Utils.showInfoLong(serviceContext, getName());
	}
}
