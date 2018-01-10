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

import edu.utah.ece.async.ibiosim.dataModels.util.observe.CoreObservable;

/**
 * The incremental properties contains information used for running iSSA.
 * 
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version $Rev$
 * @version %I%
 */
public final class IncrementalProperties extends CoreObservable
{

  private boolean mpde, meanPath, medianPath, adaptive;

  private int numPaths;
  
  /**
   * Empty constructor. Not allowed to be created by classes other than {@link AnalysisProperties}.
   */
  IncrementalProperties()
  {
    
  }
  
  /**
   * Getter for number of paths that branches off at each increment.
   * 
   * @return the number of paths
   */
  public int getNumPaths() {
    return numPaths;
  }
  
  /**
   * Check whether the simulation has adaptive time-step.
   * 
   * @return if simulation uses adaptive time-step.
   */
  public boolean isAdaptive() {
    return adaptive;
  }
  
  
  /**
   * Check if the selected simulation uses the median path method.
   * 
   * @return if simulation uses median path.
   */
  public boolean isMedianPath() 
  {
    return medianPath;
  }
  
  /**
   * Check if the selected simulation uses the mean path method.
   * 
   * @return if simulation uses mean path.
   */
  public boolean isMeanPath() {
    return meanPath;
  }
  
  /**
   * Check if the simulation uses probability density function.
   * 
   * @return if simulation uses probability density function.
   */
  public boolean isMpde() {
    return mpde;
  }
  

  /**
   * Setter for the adaptive flag.
   * 
   * @param true if simulation should have adaptive time-step.
   */
  public void setAdaptive(boolean adaptive) {
    this.adaptive = adaptive;
  }
  
  /**
   * Setter for the mean path flag.
   * 
   * @param true if simulation uses the mean path method.
   */
  public void setMeanPath(boolean meanPath) {
    this.meanPath = meanPath;
  }
  
  /**
   * Setter for the median path flag.
   * 
   * @param true if simulation uses the median path method.
   */
  public void setMedianPath(boolean medianPath) {
    this.medianPath = medianPath;
  }
  
  /**
   * Setter for the MPDE flag.
   * 
   * @param true if simulation uses probability density function.
   */
  public void setMpde(boolean mpde) {
    this.mpde = mpde;
  }
  
  /**
   * Setter for number of paths.
   * 
   * @param non-negative integer for the number of paths.
   */
  public void setNumPaths(int numPaths) {
    this.numPaths = numPaths;
  }
}
