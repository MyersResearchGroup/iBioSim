package dataModels.observable;

public interface Observable {

	public abstract void register();
	
	public abstract void unregister();
	
	public abstract void notifyObservers();
	
}
