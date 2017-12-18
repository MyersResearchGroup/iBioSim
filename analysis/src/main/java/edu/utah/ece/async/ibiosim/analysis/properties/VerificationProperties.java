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

import edu.utah.ece.async.lema.verification.lpn.properties.AbstractionProperty;

/**
 * 
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version $Rev$
 * @version %I%
 */
public class VerificationProperties 
{

  private AbstractionProperty absProperty;
  
  private String lpnProperty, constraintProperty, factorField, iterField;
  
  private ArrayList<String> abstractInteresting;

  /**
   * Empty constructor. Not allowed to be created by classes other than {@link AnalysisProperties}.
   */
  VerificationProperties()
  {
    
  }
  
  /**
   * @return the constraintProperty
   */
  public String getConstraintProperty() {
    return constraintProperty;
  }
  
  /**
   * @return the lpnProperty
   */
  public String getLpnProperty() {
    return lpnProperty;
  }
  

  /**
   * @param constraintProperty the constraintProperty to set
   */
  public void setConstraintProperty(String constraintProperty) {
    this.constraintProperty = constraintProperty;
  }
  

  
  /**
   * @param lpnProperty the lpnProperty to set
   */
  public void setLpnProperty(String lpnProperty) {
    this.lpnProperty = lpnProperty;
  }
  
  public void addAbstractInteresting(String interesting)
  {
    if(abstractInteresting == null)
    {
      abstractInteresting = new ArrayList<String>();
    }
    
    abstractInteresting.add(interesting);
  }

  
  public AbstractionProperty getAbsProperty() {
    return absProperty;
  }

  
  public void setAbsProperty(AbstractionProperty absProperty) {
    this.absProperty = absProperty;
  }

  
  public String getFactorField() {
    return factorField;
  }

  
  public void setFactorField(String factorField) {
    this.factorField = factorField;
  }

  
  public ArrayList<String> getAbstractInteresting() {
    return abstractInteresting;
  }

  
  public void setAbstractInteresting(ArrayList<String> abstractInteresting) {
    this.abstractInteresting = abstractInteresting;
  }

  
  public String getIterField() {
    return iterField;
  }

  
  public void setIterField(String iterField) {
    this.iterField = iterField;
  }
  
}
