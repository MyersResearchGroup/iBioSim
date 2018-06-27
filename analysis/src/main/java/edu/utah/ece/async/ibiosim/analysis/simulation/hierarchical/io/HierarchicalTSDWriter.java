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

/**
 * Writes simulation results to time-series data format.
 *
 * @author Leandro Watanabe
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class HierarchicalTSDWriter extends HierarchicalWriter {

  private final StringBuilder header;

  public HierarchicalTSDWriter() {
    super();
    header = new StringBuilder();
    header.append("(\"time\"");
  }

  @Override
  public void print(double currentTime) throws IOException {
    bufferedWriter.write(",\n(");
    bufferedWriter.write(String.valueOf(currentTime));
    for (int i = 0; i < this.listOfStates.size(); ++i) {
      bufferedWriter.write("," + listOfStates.get(i).toString());
    }

    bufferedWriter.write(")");
    bufferedWriter.flush();
  }

  @Override
  public void addVariable(String id, HierarchicalNode node, int index, boolean isConcentration) {
    header.append(",\"" + id + "\"");
    addNode(node, index, isConcentration);
  }

  @Override
  public void init(String filename) throws IOException {
    if (!isSet && header.length() > 0) {
      header.append(")");
      isSet = true;
    }
    if (isSet) {
      writer = new FileWriter(filename);
      bufferedWriter = new BufferedWriter(writer);
      bufferedWriter.write('(');
      bufferedWriter.write(header.toString());
      bufferedWriter.flush();
    }
  }

  @Override
  public void close() throws IOException {
    bufferedWriter.write(")");
    bufferedWriter.close();
  }

}
