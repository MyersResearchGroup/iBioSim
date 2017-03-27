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
package main.java.edu.utah.ece.async.verification.platu.expression;

import java.util.HashMap;
import java.util.HashSet;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class ConstNode implements ExpressionNode {
	int Value = 0;
	String Name;
	
	public ConstNode(String name, int value){
		this.Name = name;
		this.Value = value; 
	}

	@Override
	public int evaluate(int[] stateVector){
		return this.Value;
	}
	
	@Override
	public void getVariables(HashSet<VarNode> variables){

	}
	
	@Override
	public String toString(){
		return "" + Value;
	}
	
	@Override
	public ExpressionNode copy(HashMap<String, VarNode> variables){
		return this;
	}
}
