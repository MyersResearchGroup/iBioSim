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
 * The verification properties is mostly used in the LEMA tool.
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
   * Getter for the constraint property.
   * 
   * @return the constraint property.
   */
  public String getConstraintProperty() {
    return constraintProperty;
  }
  
  /**
   * Getter for lpn property.
   * 
   * @return the lpn property.
   */
  public String getLpnProperty() {
    return lpnProperty;
  }
  

  /**
   * Setter for the constraint property.
   * 
   * @param the constraint property.
   */
  public void setConstraintProperty(String constraintProperty) {
    this.constraintProperty = constraintProperty;
  }
  

  
  /**
   * Setter for the lpn property.
   * 
   * @param the lpn property.
   */
  public void setLpnProperty(String lpnProperty) {
    this.lpnProperty = lpnProperty;
  }
  
  /**
   * Add an abstract property.
   * 
   * @param the list of abstract properties.
   */
  public void addAbstractInteresting(String interesting)
  {
    if(abstractInteresting == null)
    {
      abstractInteresting = new ArrayList<String>();
    }
    
    abstractInteresting.add(interesting);
  }

  /**
   * Getter for the abstraction property.
   * 
   * @return the abstraction property object.
   */
  public AbstractionProperty getAbsProperty() {
    return absProperty;
  }

  /**
   * Setter for the abstraction property object.
   * @param the abstraction property object.
   */
  public void setAbsProperty(AbstractionProperty absProperty) {
    this.absProperty = absProperty;
  }

  /**
   * Getter for factor field.
   * 
   * @return the factor field.
   */
  public String getFactorField() {
    return factorField;
  }

  /**
   * Setter for factor field.
   * 
   * @param the factor field.
   */
  public void setFactorField(String factorField) {
    this.factorField = factorField;
  }

  /**
   * Getter for the list of abstraction properties.
   * 
   * @return the list of abstraction properties.
   */
  public ArrayList<String> getAbstractInteresting() {
    return abstractInteresting;
  }


  /**
   * Setter for list of abstraction properties.
   * 
   * @param the new list of abstraction properties.
   */
  public void setAbstractInteresting(ArrayList<String> abstractInteresting) {
    this.abstractInteresting = abstractInteresting;
  }

  
  /**
   * Getter iteration field.
   * 
   * @return the iteration field.
   */
  public String getIterField() {
    return iterField;
  }


  /**
   * Setter for iteration field.
   * 
   * @param the iteration field
   */
  public void setIterField(String iterField) {
    this.iterField = iterField;
  }
  
}
