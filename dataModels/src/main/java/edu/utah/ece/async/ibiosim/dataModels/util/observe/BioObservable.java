package edu.utah.ece.async.ibiosim.dataModels.util.observe;

import edu.utah.ece.async.ibiosim.dataModels.util.Message;

public interface BioObservable 
{
  public abstract void addObservable(BioObservable bioObservable);
  public abstract void addObserver(BioObserver bioObserver);
  public abstract void notifyObservers(Message message);
}
