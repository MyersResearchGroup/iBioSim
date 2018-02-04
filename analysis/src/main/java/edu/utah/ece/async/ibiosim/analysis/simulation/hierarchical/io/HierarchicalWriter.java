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
import java.util.ArrayList;
import java.util.List;

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.HierarchicalNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.SpeciesNode;

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
  
  protected List<WriterNode> listOfStates;
  
  protected FileWriter            writer;
  
  protected boolean isSet;
  
  public HierarchicalWriter()
  {
    listOfStates = new ArrayList<WriterNode>();
    isSet = false;
  }
  
  public abstract void init(String filename) throws IOException;
  
  public abstract void print() throws IOException;
  
  public abstract void addVariable(String id, HierarchicalNode node, int index,  boolean isConcentration);
  
  /**
   * 
   * @throws IOException
   */
  public abstract void close() throws IOException;
  
  /**
   * 
   * @param node
   * @param index
   * @param isConcentration
   */
  protected void addNode(HierarchicalNode node, int index, boolean isConcentration)
  {
    listOfStates.add(new WriterNode(node, index, isConcentration));
  }
  
  protected class WriterNode
  {
    private HierarchicalNode node;
    private int index;
    private boolean isConcentration;
    
    /**
     * 
     * @param node
     * @param index
     * @param isConcentration
     */
    public WriterNode(HierarchicalNode node, int index, boolean isConcentration)
    {
      this.node = node;
      this.index = index;
      this.isConcentration = isConcentration;
    }
    
    /**
     * 
     */
    public String toString()
    {
      double value = 0;
      if(isConcentration && node.isSpecies())
      {
        SpeciesNode species = (SpeciesNode) node;
        value = species.getConcentration(index);
      }
      else
      {
        value = node.getState().getState(index).getStateValue();
      }
      
      return String.valueOf(value);
    }
  }

}


