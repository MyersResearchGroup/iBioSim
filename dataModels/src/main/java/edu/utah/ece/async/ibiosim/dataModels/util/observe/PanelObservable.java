package edu.utah.ece.async.ibiosim.dataModels.util.observe;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import edu.utah.ece.async.ibiosim.dataModels.util.Message;

public abstract class PanelObservable extends JPanel implements BioObservable
{

  /**
   * 
   */
  private static final long serialVersionUID = -5440615594726032780L;
  private BioObservable parent;
  private List<BioObserver> listOfObservers;

  public PanelObservable(BorderLayout layout)
  {
    super(layout);
    listOfObservers = new ArrayList<BioObserver>();
  }

  public PanelObservable()
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
