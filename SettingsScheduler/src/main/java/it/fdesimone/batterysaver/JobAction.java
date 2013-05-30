package it.fdesimone.batterysaver;


/**
 * @author Francesco De Simone
 */
public abstract class JobAction implements Runnable {

	public int id;
	public String name;
	public boolean enabled;
	
}
