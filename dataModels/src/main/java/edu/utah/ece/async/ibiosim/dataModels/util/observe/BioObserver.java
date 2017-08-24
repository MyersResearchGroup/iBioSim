package edu.utah.ece.async.ibiosim.dataModels.util.observe;

import edu.utah.ece.async.ibiosim.dataModels.util.Message;

public interface BioObserver 
{

  public abstract void update(Message message);
  
}
