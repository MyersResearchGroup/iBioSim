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
package edu.utah.ece.async.ibiosim.analysis.properties;


public class IncrementalProperties {

  private boolean mpde, meanPath, medianPath, adaptive;

  private int numPaths;
  

  /**
   * @return the numPaths
   */
  public int getNumPaths() {
    return numPaths;
  }
  
  /**
   * @return the adaptive
   */
  public boolean isAdaptive() {
    return adaptive;
  }
  
  
  /**
   * @return the medianPath
   */
  public boolean isMedianPath() {
    return medianPath;
  }
  /**
   * @return the meanPath
   */
  public boolean isMeanPath() {
    return meanPath;
  }
  
  /**
   * @return the mpde
   */
  public boolean isMpde() {
    return mpde;
  }
  

  /**
   * @param adaptive the adaptive to set
   */
  public void setAdaptive(boolean adaptive) {
    this.adaptive = adaptive;
  }
  
  /**
   * @param meanPath the meanPath to set
   */
  public void setMeanPath(boolean meanPath) {
    this.meanPath = meanPath;
  }
  
  /**
   * @param medianPath the medianPath to set
   */
  public void setMedianPath(boolean medianPath) {
    this.medianPath = medianPath;
  }
  
  /**
   * @param mpde the mpde to set
   */
  public void setMpde(boolean mpde) {
    this.mpde = mpde;
  }
  
  /**
   * @param numPaths the numPaths to set
   */
  public void setNumPaths(int numPaths) {
    this.numPaths = numPaths;
  }
}
