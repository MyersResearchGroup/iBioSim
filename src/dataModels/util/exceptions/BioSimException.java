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
package dataModels.util.exceptions;

/**
 * 
 *
 * @author Tramy Nguyen
 * @author Leandro Watanabe 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class BioSimException extends Exception{

  private static final long serialVersionUID = 889807138206436229L;
  private String title;
  
  public BioSimException(String message, String messageTitle){
    super(message);
    this.title = messageTitle;
  }
  
  public String getTitle(){
    return this.title;
  }
}
