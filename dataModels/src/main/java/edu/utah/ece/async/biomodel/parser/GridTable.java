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
package main.java.edu.utah.ece.async.biomodel.parser;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class GridTable {

  private int numRows, numCols;
  
  public GridTable()
  {
    numRows = 0;
    numCols = 0;
  }
  
  /**
   * @return the numRows
   */
  public int getNumRows() {
    return numRows;
  }

  /**
   * @return the numCols
   */
  public int getNumCols() {
    return numCols;
  }
  
  /**
   * @return the numRows
   */
  public void setNumRows(int numRows) {
    this.numRows = numRows;
  }

  /**
   * @return the numCols
   */
  public void setNumCols(int numCols) {
    this.numCols = numCols;
  }
}
