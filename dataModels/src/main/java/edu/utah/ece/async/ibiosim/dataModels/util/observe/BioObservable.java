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
      REQUEST_PROGRESS,
      ADD_FILE
  };
  
  public abstract void addObservable(BioObservable bioObservable);
  public abstract void addObserver(BioObserver bioObserver);
  public abstract void notifyObservers(Message message);
  public abstract boolean request(RequestType type, Message message);
  public abstract boolean send(RequestType type, Message message);
}
