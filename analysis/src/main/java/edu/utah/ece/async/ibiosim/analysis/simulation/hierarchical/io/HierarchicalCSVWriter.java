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
package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.HierarchicalNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.HierarchicalState;

public class HierarchicalCSVWriter extends HierarchicalWriter{

  private StringBuilder header;
  private char separator;
  
  public HierarchicalCSVWriter()
  {
    super();
    this.separator = ',';
    this.header = new StringBuilder();
  }
  
  @Override
  public void print() throws IOException {
    bufferedWriter.write("\n");
    if(listOfStates.size() > 0)
    {
      bufferedWriter.write(String.valueOf(listOfStates.get(0).toString()));
      
      for(int i = 1; i < this.listOfStates.size(); ++i)
      {
        bufferedWriter.write(String.valueOf(separator) + listOfStates.get(i).toString());
      }
    }
    
    bufferedWriter.flush();
  }

  @Override
  public void addVariable(String id, HierarchicalNode node, int index, boolean isConcentration) {
    if(header.length() == 0)
    {
      header.append(id);
    }
    else
    {
      header.append(separator + id);
    }
    
    addNode(node, index, isConcentration);
  }
  @Override
  public void init(String filename) throws IOException {
    if(!isSet && header.length() > 0)
    {
      isSet = true;
    }
    if(isSet)
    {
      writer = new FileWriter(filename);
      bufferedWriter = new BufferedWriter(writer);
      bufferedWriter.write(header.toString());
      bufferedWriter.flush();
    }
  }
  
  @Override
  public void close() throws IOException 
  {
    bufferedWriter.write(")");
    bufferedWriter.close();
  }
}
