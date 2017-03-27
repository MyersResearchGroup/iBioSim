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
package main.java.edu.utah.ece.async.verification.platu.logicAnalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dataModels.lpn.parser.LPN;
import dataModels.lpn.parser.Transition;
import main.java.edu.utah.ece.async.verification.platu.platuLpn.DualHashMap;
import main.java.edu.utah.ece.async.verification.platu.stategraph.State;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Constraint{
	private LPN lpn; // lpn that generates the constraint
	final private int[] interfaceValues;
	final private Transition lpnTransition;
	final private int[] vector;
	//List<VarNode> variableList = new ArrayList<VarNode>(1);
	List<Integer> variableList = new ArrayList<Integer>(1); // variableList stores the index of interface variables in the other lpn. 
	List<Integer> valueList = new ArrayList<Integer>(1);
	private int hashVal = -1;

	public Constraint(State start, State end, Transition firedTran, LPN lpn2) {
	    this.lpnTransition = firedTran;
	    this.lpn = firedTran.getLpn();		
		this.vector = start.getVariableVector();

		int[] endVector = end.getVariableVector();
//		int index = dstLpn.getInterfaceIndex(this.lpn.getLabel());
		//int[] thisIndex = lpn2.getOtherIndexArray(this.lpn.ID-1);
		int[] thisIndex = lpn2.getOtherIndexArray(this.lpn.getLpnIndex());
		DualHashMap<String, Integer> varIndexMap = this.lpn.getVarIndexMap();

		this.interfaceValues = new int[thisIndex.length];
		for(int i = 0; i < thisIndex.length; i++){
			int varIndex = thisIndex[i];
			this.interfaceValues[i] = this.vector[varIndex];
			if(this.vector[varIndex] != endVector[varIndex]){
				String variable = varIndexMap.getKey(varIndex);
				this.valueList.add(endVector[varIndex]);				
				this.variableList.add(lpn2.getVarIndexMap().get(variable));
				//this.variableList.add(lpn2.getVarNodeMap().get(variable));				
			}
		}
		if(this.variableList.size() == 0){
			System.out.println(this.lpnTransition.getFullLabel());
			System.err.println("error: invalid constraint");
			System.exit(1);
		}
	}

	/**
     * @return List of modified variables.
     */
	public List<Integer> getVariableList(){
		return this.variableList;
	}
	
	/**
     * @return List of new variable values.
     */
	public List<Integer> getValueList(){
		return this.valueList;
	}
	
	/**
     * @return State vector.
     */
	public int[] getVector(){
		return this.vector;
	}
	
	/**
     * @return LPN where the constraint was generated.
     */
	public LPN getLpn(){
		return this.lpn;
	}

	/**
     * @return Values of the interface variables.
     */
	public int[] getInterfaceValue(){
		return this.interfaceValues;
	}
	
	/**
     * @return LPNTran applied.
     */
    public Transition getLpnTransition(){
    	return this.lpnTransition;
    }

	@Override
	public int hashCode() {
		if(this.hashVal == -1){
			final int prime = 31;
			this.hashVal = 1;
			this.hashVal = prime * this.hashVal + Arrays.hashCode(interfaceValues);
			this.hashVal = prime * this.hashVal + ((this.lpn == null) ? 0 : this.lpn.getLabel().hashCode());
			this.hashVal = prime * this.hashVal + ((this.lpnTransition == null) ? 0 : this.lpnTransition.hashCode());
		}
		
		return this.hashVal;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (getClass() != obj.getClass())
			return false;
		
		Constraint other = (Constraint) obj;
		if (!Arrays.equals(this.interfaceValues, other.interfaceValues))
			return false;
		
		if (this.lpn == null) {
			if (other.lpn != null)
				return false;
		} 
		else if (!this.lpn.equals(other.lpn))
			return false;
		
		if (this.lpnTransition == null) {
			if (other.lpnTransition != null)
				return false;
		} 
		else if (!this.lpnTransition.equals(other.lpnTransition))
			return false;
		
		return true;
	}

	@Override
	public String toString() {
		return "Constraint [lpn=" + lpn.getLabel() + ",\n interfaceValues="
				+ Arrays.toString(interfaceValues) + ",\n lpnTransition="
				+ lpnTransition + ",\n vector=" + Arrays.toString(vector)
				+ ",\n variableList=" + variableList + ",\n valueList=" + valueList
				+ "]";
	}
	
	
}
