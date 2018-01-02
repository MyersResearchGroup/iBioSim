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
package edu.utah.ece.async.lema.verification.platu.expression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class ArrayNode extends VarNode{
	private int dimensions = 0;
	private List<Object> array = null;
	private List<Integer> dimensionList = null;
	private List<VarNode> variableList = null;
	
	public ArrayNode(String name, List<Object> array, int dimensions, List<Integer> dimensionList, List<VarNode> variableList){
		super(name);
		this.array = array;
		this.dimensions = dimensions;
		this.dimensionList = dimensionList;
		this.variableList = variableList;
	}
	
	public List<VarNode> getVariableList(){
		return this.variableList;
	}
	
	public int getDimensions(){
		return this.dimensions;
	}
	
	public VarNode getElement(int index){
		if(this.dimensions != 1 || index < 0 || index > (this.dimensionList.get(0) - 1)){
			System.err.println("error: out of bounds array index");
			System.exit(1);
		}
		
		return (VarNode) this.array.get(index);
	}
	
	@SuppressWarnings("unchecked")
	public VarNode getElement(List<Integer> indexList){
		if(this.dimensions != indexList.size()){
			System.err.println("error: incorrect dimensions for array " + this.name);
			System.exit(1);
		}
		
		int lastIndex = indexList.size() - 1;
		lastIndex = indexList.get(lastIndex).intValue();
		
		Object currentArray = this.array;
		for(int i = 0; i < indexList.size() - 1; i++){
			int index = indexList.get(i);
			if(index < 0 || index > (this.dimensionList.get(i) - 1)){
				System.err.println("error: out of bounds array index");
				System.exit(1);
			}
			
			currentArray = ((List<Object>)currentArray).get(index);
		}
		
		if(lastIndex < 0 || lastIndex > (this.dimensionList.get(indexList.size() - 1) - 1)){
			System.err.println("error: out of bounds array index");
			System.exit(1);
		}
		
		return (VarNode) ((List<Object>)currentArray).get(lastIndex);
	}
	
	@Override
	public void getVariables(HashSet<VarNode> variables){
		variables.add(this);
	}
	
	public List<Integer> getDimensionList(){
		return this.dimensionList;
	}
	
	@Override
	public VarNode clone(){
		return new ArrayNode(this.name, this.array, this.dimensions, this.dimensionList, this.variableList);
	}
	
	@Override
	public ExpressionNode copy(HashMap<String, VarNode> variables){
		VarNode varNode = variables.get(this.name);
		if(varNode != null){
			 return varNode;
		}
		int iter = dimensionList.size() - 1;
		int dIndex = 0;
		int arraySize = dimensionList.get(dIndex++);
		int lastSize = 0;
		List<Object> topLevelArray = new ArrayList<Object>(arraySize);
		
		Queue<List<Object>> arrayQueue = new LinkedList<List<Object>>();
		arrayQueue.offer(topLevelArray);
				
		while(iter > 0){
			lastSize = arraySize;
			arraySize = dimensionList.get(dIndex++);
			int qSize = arrayQueue.size();
			for(int i = 0; i < qSize; i++){
				List<Object> array = arrayQueue.poll();
				for(int j = 0 ; j < lastSize; j++){
					List<Object> newArray = new ArrayList<Object>(arraySize);
					array.add(j, newArray);
					arrayQueue.offer(newArray);
				}
			}
			
			iter--;
		}
		
		dIndex--;
		arraySize = dimensionList.get(dIndex);
		
		int nodeIndex = 0;
		List<VarNode> varList = new ArrayList<VarNode>();
		while(!arrayQueue.isEmpty()){
			List<Object> array = arrayQueue.poll();
			for(int i = 0; i < arraySize; i++){
				VarNode node = this.variableList.get(nodeIndex++);
				
				VarNode newNode = variables.get(node.getName());
				if(newNode == null){
					newNode = node.clone();
				}
				
				array.add(i, newNode);
				varList.add(newNode);
			}
		}

		ArrayNode newArray = new ArrayNode(this.name, topLevelArray, this.dimensionList.size(), this.dimensionList, varList);
		newArray.setType(this.type);
		
		return newArray;
	}
}
