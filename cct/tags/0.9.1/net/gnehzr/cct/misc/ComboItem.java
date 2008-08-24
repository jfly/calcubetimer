package net.gnehzr.cct.misc;

public class ComboItem {
	private Object obj;
	private boolean isEnabled;
	private boolean isInUse;

	public ComboItem(Object obj, boolean isEnabled){
		this.obj = obj;
		this.isEnabled = isEnabled;
		isInUse = false;
	}

	public boolean isEnabled(){
		return isEnabled;
	}

	public boolean isInUse(){
		return isInUse;
	}

	public void setEnabled(boolean isEnabled){
		this.isEnabled = isEnabled;
	}

	public void setInUse(boolean isInUse){
		this.isInUse = isInUse;
	}

	public String toString() {
		return obj.toString();
	}
}
