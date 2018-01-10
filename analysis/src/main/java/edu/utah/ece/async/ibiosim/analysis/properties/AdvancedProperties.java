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

import java.util.ArrayList;
import java.util.List;

import edu.utah.ece.async.ibiosim.dataModels.util.observe.CoreObservable;

/**
 * The advanced properties contains information associated with model abstraction parameters.
 * 
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version $Rev$
 * @version %I%
 */
public class AdvancedProperties extends CoreObservable
{

  private List<String> preAbs, loopAbs, postAbs;
  
  private double        qss;
  private double        rap1;
  private double        rap2;
  private double stoichAmp;

  private int         con;

  
  /**
   * Constructor for AdvancedProperties. Not allowed to be created by classes other than {@link AnalysisProperties}.
   */
  AdvancedProperties()
  {
    rap1 = 0.1;
    rap2 = 0.1;
    qss = 0.1;
    con = 15;
    stoichAmp = 1.0;
    preAbs = new ArrayList<String>(0);
    loopAbs = new ArrayList<String>(0);
    postAbs = new ArrayList<String>(0);
    
  }
  
  /**
   * @return the con
   */
  public int getCon() {
    return con;
  }
  
  /**
   * Getter for post-abstractions methods.
   * 
   * @return list of post-abstraction methods.
   */
  public List<String> getPostAbs() {
    return postAbs;
  }
  
  /**
   * Getter for pre-abstractions methods.
   * 
   * @return list of pre-abstraction methods.
   */
  public List<String> getPreAbs() {
    return preAbs;
  }
  

  /**
   * Getter for quasi-steady state threshold.
   * 
   * @return quasi-steady state condition.
   */
  public double getQss() {
    return qss;
  }
  
  /**
   * Getter for rapid equilibrium 1 condition.
   * 
   * @return rapid equilibrium
   */
  public double getRap1() {
    return rap1;
  }
  
  /**
   * Getter for rapid equilibrium 2 condition.
   *  
   * @return rapid equilibrium
   */
  public double getRap2() {
    return rap2;
  }
  
  /**
   * Getter for stoichiometry amplication.
   * 
   * @return stoichiometry amplication
   */
  public double getStoichAmp() {
    return stoichAmp;
  }
  
  /**
   * Setter for maximum concentration threshold.
   * 
   * @param maximum concentration threshold
   */
  public void setCon(int con) {
    this.con = con;
  }

  /**
   * Getter for loop abstraction.
   * 
   * @return list of loop abstraction methods
   */
  public List<String> getLoopAbs() {
    return loopAbs;
  }
  
  /**
   * Setter for loop abstraction.
   * 
   * @param list of loop abstraction methods
   */
  public void setLoopAbs(List<String> loopAbs) {
    this.loopAbs = loopAbs;
  }
  

  /**
   * Setter for post abstraction methods.
   * 
   * @param list of post abstraction methods.
   */
  public void setPostAbs(List<String> postAbs) {
    this.postAbs = postAbs;
  }
  
  /**
   * @param preAbs the preAbs to set
   */
  public void setPreAbs(List<String> preAbs) {
    this.preAbs = preAbs;
  }
  
  /**
   * Setter for quasi-steady state threshold.
   * 
   * @param quasi-steady state condition
   */
  public void setQss(double qss) {
    this.qss = qss;
  }
  
  /**
   * Setter for rapid equilibrium 1 condition.
   * 
   * @param rapid equilibrium
   */
  public void setRap1(double rap1) {
    this.rap1 = rap1;
  }
  
  /**
   * Setter for rapid equilibrium 2 condition.
   * 
   * @param rapid equilibrium
   */
  public void setRap2(double rap2) {
    this.rap2 = rap2;
  }
  
  /**
   * Setter for stoichiometry amplication threshold.
   * 
   * @param stoichiometry amplication
   */
  public void setStoichAmp(double stoichAmp) {
    this.stoichAmp = stoichAmp;
  }

  
}
