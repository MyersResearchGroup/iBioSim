package edu.utah.ece.async.ibiosim.dataModels.util.observe;

import edu.utah.ece.async.ibiosim.dataModels.util.Message;
import edu.utah.ece.async.ibiosim.dataModels.util.observe.BioObservable.RequestType;

public interface BioObservable 
{
  
  public static enum RequestType
  {
      REQUEST_INTEGER,
      REQUEST_DOUBLE,
      REQUEST_STRING,
      REQUEST_OVERWRITE,
      ADD_FILE
  };
  
  public abstract void addObservable(BioObservable bioObservable);
  public abstract void addObserver(BioObserver bioObserver);
  public abstract void notifyObservers(Message message);
  public abstract boolean request(RequestType type, Message message);
  public abstract boolean send(RequestType type, Message message);
}
