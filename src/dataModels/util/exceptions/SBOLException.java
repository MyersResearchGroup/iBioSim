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

public class SBOLException extends BioSimException{
	
	private String title;
	
	public SBOLException(String message, String messageTitle){
		super(message, messageTitle);
		this.title = messageTitle;
	}
	
	public String getTitle(){
		return this.title;
	}

}
