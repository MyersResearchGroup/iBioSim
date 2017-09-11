package edu.utah.ece.async.ibiosim.dataModels.util.observe;

import java.util.ArrayList;
import java.util.List;

import edu.utah.ece.async.ibiosim.dataModels.util.Message;

public abstract class CoreObservable implements BioObservable
{

  private BioObservable parent;
  private List<BioObserver> listOfObservers;

  public CoreObservable()
  {
    listOfObservers = new ArrayList<BioObserver>();
  }
  
  public void addObservable(BioObservable bioObservable)
  {
    parent = bioObservable;
  }
  
  public void addObserver(BioObserver bioObserver)
  {
    this.listOfObservers.add(bioObserver);
  }

  public void notifyObservers(Message message)
  {
    if(parent != null)
    {
      parent.notifyObservers(message);
    }
    for(BioObserver bioObserver : listOfObservers)
    {
      bioObserver.update(message);
    }
  }

}
