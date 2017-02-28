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
package backend.verification.platu.expression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ArrayElement extends VarNode{
	List<ExpressionNode> indexVariables = null;
	ArrayNode array = null;
	
	public ArrayElement(ArrayNode array, List<ExpressionNode> indexVars){
		super(array.getName());
		this.array = array;
		this.indexVariables = indexVars;
	}
	
	public ArrayNode getArray(){
		return this.array;
	}
	
	@Override
	public int evaluate(int[] statevector){
		List<Integer> indexValues = new ArrayList<Integer>(this.indexVariables.size());
		for(ExpressionNode n : this.indexVariables){
			indexValues.add(n.evaluate(statevector));
		}
		
		return this.array.getElement(indexValues).evaluate(statevector);
	}
	
	@Override
	public void getVariables(HashSet<VarNode> variables){
		variables.add(this.array);
	}
	
	@Override
	public String toString(){
		String s = array.getName();
		for(ExpressionNode n : this.indexVariables){
			s += "[" + n.toString() + "]";
		}
		
		return s;
	}
	
	@Override
	public int getIndex(int[] stateVector){
		List<Integer> indexValues = new ArrayList<Integer>(this.indexVariables.size());
		for(ExpressionNode n : this.indexVariables){
			indexValues.add(n.evaluate(stateVector));
		}
		
		return this.array.getElement(indexValues).getIndex(stateVector);
	}
	
	@Override
	public ExpressionNode copy(HashMap<String, VarNode> variables){
		List<ExpressionNode> copyList = new ArrayList<ExpressionNode>(this.indexVariables.size());
		for(ExpressionNode n : this.indexVariables){
			copyList.add(n.copy(variables));
		}
		
		return new ArrayElement((ArrayNode) this.array.copy(variables), copyList);
	}
}
