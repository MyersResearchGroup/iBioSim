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
package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 *
 * @author Tramy Nguyen
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class StringNode
{
	private List<StringNode> children;
	private int stateNum; 
	private String weight; 
	private String nextState;
	
	public StringNode(int stateNum)
	{
		this.children = new ArrayList<StringNode>();
		this.stateNum = stateNum;
		this.weight = null; 
	}
	
	public void setWeight(String c)
	{
		
	}

}
