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
package edu.utah.ece.async.ibiosim.learn.AMSModel;

import java.util.*;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class DMVCrun {
	
	private static int numDMVCruns = 0;
	
	//private int id;
	
	private ArrayList<ArrayList<Double>> valueL;
	
	private ArrayList<Integer> startPoints;
	
	private ArrayList<Integer> endPoints;
	
	private ArrayList<Double> avgVals;
	
	public DMVCrun(){
		//this.id = DMVCrun.numDMVCruns; // required?? whenever required the absolute id would be different. here it remains the same for all the runs of the same variable.
		DMVCrun.numDMVCruns = DMVCrun.numDMVCruns + 1;
		valueL = new ArrayList<ArrayList<Double>>();
		startPoints = new ArrayList<Integer>();
		endPoints = new ArrayList<Integer>();
		avgVals = new ArrayList<Double>();
	}
	
	public void addStartPoint(int i){
		startPoints.add(i);
	}

	public void addEndPoint(int i){
		endPoints.add(i);
		calcAvg();
	}
	
	public void calcAvg(){ //for the most recently added set of values by default
		//ArrayList<Double> vals = this.valueL.get(valueL.size() -1);
		Double total = 0.0;
		for (Double d:this.valueL.get(valueL.size() -1)){
			total+=d;
		}
		avgVals.add(total/this.valueL.get(valueL.size() -1).size());
	}
	
	public Double[] getAvgVals() { // think.. if entire avgVals or just an indexed one
		return avgVals.toArray(new Double[avgVals.size()]);
	}

	public int getNumPoints(){
		int numPoints = 0;
		for (int i = 0; i<startPoints.size(); i++){
			numPoints += ((endPoints.get(i) - startPoints.get(i))+1);
		}
		return numPoints;
	}
	
	public int getEndPoint(int i) {
		return endPoints.get(i);
	}

	public int getStartPoint(int i) {
		return startPoints.get(i);
	}

	public void addValue(double d){
		if (valueL.size() == startPoints.size()){//(this.valueL.isEmpty()){
			this.valueL.add(valueL.size(),new ArrayList<Double>());
		}
		this.valueL.get(valueL.size() -1).add(d);
	}
	
	public void removeValue(){ // removes the most recent set of dmv values
		if (valueL.size() > startPoints.size()){
			valueL.remove(valueL.size() -1);
		}
	}
	
	public Double getLastValue(){ // removes the most recent set of dmv values
		Double total = 0.0;
		for (Double d:this.valueL.get(valueL.size() -1)){
			total+=d;
		}
		return (total/this.valueL.get(valueL.size() -1).size());
	}
	
	public void clearAll(){
		startPoints.clear();
		endPoints.clear();
		valueL.clear();
		avgVals.clear();
	}
	
	public Double getConstVal(){ //for the most recently added set of values by default
		//ArrayList<Double> vals = this.valueL.get(valueL.size() -1);
		Double total = 0.0;
		for (Double d:this.valueL.get(valueL.size() -1)){
			total+=d;
		}
		return (total/this.valueL.get(valueL.size() -1).size());
	}
	
/*	
	public boolean belongsToRuns(int i){
		for (int j = 0; j<startPoints.size(); j++){
			if ((i>=startPoints.get(j)) && (i<=endPoints.get(j))){
				return true;
			}
		}
		return false;
	}
*/
}
