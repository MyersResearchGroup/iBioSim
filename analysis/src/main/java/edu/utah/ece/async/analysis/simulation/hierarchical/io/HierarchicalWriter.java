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
package edu.utah.ece.async.analysis.simulation.hierarchical.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.utah.ece.async.analysis.simulation.hierarchical.states.HierarchicalState;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public abstract class HierarchicalWriter {

  protected BufferedWriter bufferedWriter;
  
  protected List<HierarchicalState> listOfStates;
  
  protected FileWriter            writer;
  
  protected boolean isSet;
  public HierarchicalWriter()
  {
    listOfStates = new ArrayList<HierarchicalState>();
    isSet = false;
  }
  
  public abstract void init(String filename) throws IOException;
  
  public abstract void print() throws IOException;
  
  public abstract void addVariable(String id, HierarchicalState state);
}


