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
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.HierarchicalState;

/**
 * Base class for writing hierarchical simulator results.
 *
 * @author Leandro Watanabe
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public abstract class HierarchicalWriter {

  protected BufferedWriter bufferedWriter;
  protected List<WriterNode> listOfStates;
  protected FileWriter writer;
  protected boolean isSet;

  public HierarchicalWriter() {
    listOfStates = new ArrayList<>();
    isSet = false;
  }

  /**
   * Initializes the writer.
   *
   * @param filename
   *          - the name of the file that the writer writes to.
   * @throws IOException
   *           - if there is any problem with the output file.
   */
  public abstract void init(String filename) throws IOException;

  /**
   * Writes out the simulation results.
   *
   *
   * @param currentTime
   *          - simulation time.
   * @throws IOException
   *           - if there is any problem with the output file.
   */
  public abstract void print(double currentTime) throws IOException;

  /**
   * Adds a variable that should have the values printed out.
   *
   * @param id
   *          - the id of the variable.
   * @param node
   *          - the node with the state variable.
   * @param index
   *          - the index of the submodel the node corresponds to.
   */
  public abstract void addVariable(String id, HierarchicalNode node, HierarchicalNode compartment, int index);

  /**
   * Closes the writer.
   *
   * @throws IOException
   *           - if a problem occurs when closing the writer.
   */
  public abstract void close() throws IOException;

  /**
   * Adds the state variable to the list of variables that will be printed out.
   *
   */
  protected void addNode(HierarchicalState state) {
    addNode(state, null);
  }

  /**
   * Adds the state variable to the list of variables that will be printed out.
   *
   */
  protected void addNode(HierarchicalState state, HierarchicalState compartment) {
    listOfStates.add(new WriterNode(state, compartment));
  }

  /**
   * Node class for printing out the results of a variable.
   */
  protected class WriterNode {
    private final HierarchicalState state, compartmentState;

    /**
     * Creates a WriterNode object.
     */
    public WriterNode(HierarchicalState state, int index) {
      this.state = state;
      this.compartmentState = null;
    }

    /**
     *
     * @param state
     * @param index
     * @param isConcentration
     */
    public WriterNode(HierarchicalState state, HierarchicalState compartmentState) {
      this.state = state;
      this.compartmentState = compartmentState;
    }

    @Override
    public String toString() {
      double value = state.getValue();

      if (compartmentState != null) {
        value = value / compartmentState.getValue();
      }

      return String.valueOf(value);
    }
  }

}
