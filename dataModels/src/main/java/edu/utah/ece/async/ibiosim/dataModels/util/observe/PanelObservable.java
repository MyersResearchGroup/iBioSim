/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package edu.utah.ece.async.ibiosim.dataModels.util.observe;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import edu.utah.ece.async.ibiosim.dataModels.util.Message;
import edu.utah.ece.async.ibiosim.dataModels.util.observe.BioObservable.RequestType;

public abstract class PanelObservable extends JPanel implements BioObservable
{

  /**
   * 
   */
  private static final long serialVersionUID = -5440615594726032780L;
  protected BioObservable parent;
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
  
  public boolean request(RequestType type,Message message){return false;}

  public boolean send(RequestType type, Message message){return false;}

}
