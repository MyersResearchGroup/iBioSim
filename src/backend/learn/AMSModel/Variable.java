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
package backend.learn.AMSModel;


//import java.util.*;

public class Variable implements Comparable<Variable>{

	private String name;
	
	private boolean dmvc;
	
	private boolean input;
	
	private boolean output;
	
	private boolean care;
	
	private boolean interpolate;
	
	private DMVCrun runs; // tmp
	
	private Double initValue_vMax,initRate_rMax;
	
	private Double initValue_vMin,initRate_rMin;
	
	private String forceType;
	
	private Double epsilon;
	
	private boolean destab;
	
	public Variable(String name){
		this.name = name;
		this.runs = new DMVCrun();
		this.forceType = null;
		this.care = true;
		this.epsilon = null; //default
		this.destab = false;
		this.interpolate = false;
	}

	public boolean isDmvc() {
		return dmvc;
	}

	public void setDmvc(boolean dmvc) {
		this.dmvc = dmvc;
	}

	public boolean isInput() {
		return input;
	}
	public void setInput(boolean input) {
		this.input = input;
	}
	
	public boolean isCare() {
		return care;
	}

	public void setCare(boolean care) {
		this.care = care;
	}
	
	public boolean isInterpolate() {
		return interpolate;
	}

	public void setInterpolate(boolean interpolate) {
		this.interpolate = interpolate;
	}

	public String getName() {
		return name;
	}

//	public void setName(String name) {
//		this.name = name;
//	}

	public boolean isOutput() {
		return output;
	}

	public void setOutput(boolean output) {
		this.output = output;
	}

	public DMVCrun getRuns() {
		return runs;
	}
	
	public void addInitValues(Double v){
		if ((initValue_vMin == null) && (initValue_vMax == null)){
			initValue_vMin = v;
			initValue_vMax = v;
		}
		else{
			if (v < initValue_vMin){
				initValue_vMin = v;
			}
			else if (v > initValue_vMax){
				initValue_vMax = v;
			}
		}
	}
	
	public void addInitRates(Double r){
		if ((initRate_rMin == null) && (initRate_rMax == null)){
			initRate_rMin = r;
			initRate_rMax = r;
		}
		else{
			if (r < initRate_rMin){
				initRate_rMin = r;
			}
			else if (r > initRate_rMax){
				initRate_rMax = r;
			}
		}
	}
	
	public void reset(){
		initRate_rMax = null;
		initRate_rMin = null;
		initValue_vMin = null;
		initValue_vMax = null;
		this.runs = new DMVCrun();
		//this.epsilon = null ??
		//this.care = true ???
	}
	
	public String getInitValue(){
		return ("["+(int)Math.floor(initValue_vMin)+","+(int)Math.ceil(initValue_vMax)+"]");
	}
	
	public String getInitRate(){
		return ("["+(int)Math.floor(initRate_rMin)+","+(int)Math.ceil(initRate_rMax)+"]");
	}
	
	public void scaleInitByDelay(Double dScaleFactor){
		if (!dmvc){
			initRate_rMin /=  dScaleFactor;
			initRate_rMax /=  dScaleFactor;
		}
	}
	
	public void scaleInitByVar(Double vScaleFactor){
		if (!dmvc){
			initRate_rMin *=  vScaleFactor;
			initRate_rMax *=  vScaleFactor;
		}
		initValue_vMax *=  vScaleFactor;
		initValue_vMin *=  vScaleFactor;
	}
	
	@Override
	public int compareTo(Variable o) {
		return (this.getName().compareToIgnoreCase(o.getName()));
	}
	
	public void copy(Variable a){
		//Private fields in a. We shouldn't access like this?
		this.name = a.name;
		this.runs = a.runs;
		this.dmvc = a.dmvc;
		this.input = a.input;
		this.output = a.output;
		this.forceType = a.forceType;
		this.care = a.care;
		this.epsilon = a.epsilon;
		this.destab = a.destab;
		this.interpolate = a.interpolate;
		// NOT Copying the init values and rates bcoz they get normalized every time a learn is performed on them. 
		// So the values get multiplied by the same factor multiple times which is wrong.
	//	this.initValue_vMax = a.initValue_vMax;
	//	this.initValue_vMin = a.initValue_vMin;
	//	this.initRate_rMax = a.initRate_rMax;
	//	this.initRate_rMin = a.initRate_rMin;
	}
	
	public void forceDmvc(Boolean dmvc){
		if (dmvc == null){
			this.forceType = null;
		//	this.dmvc = false;
		}
		else if (!dmvc){
			this.forceType = "Cont";
			this.dmvc = false;
		}
		else if (dmvc){
			this.forceType = "DMV";
			this.dmvc = true;
		}
	}
	
	public boolean isForcedDmv(){
		if (forceType == null)
			return false;
		else if (forceType.equalsIgnoreCase("DMV"))
			return true;
		else
			return false;
	}
	
	public boolean isForcedCont(){
		if (forceType == null)
			return false;
		else if (forceType.equalsIgnoreCase("Cont"))
			return true;
		else
			return false;
	}

	public void setEpsilon(Double v) {
		this.epsilon = v;
	}
	
	public Double getEpsilon() {
		return(epsilon);
	}
	
	public boolean isDestab() {
		return destab;
	}
	
	public void setDestab(boolean b) {
		destab = b;
	}
	
	/*
	public void addInitValues(Double d, int i){
		if (initValues.isEmpty()){
			for (int j = 0; j < vars.length; j++){
				initValues.add(j, new ArrayList<Double>());
			}
			initValues.get(i).add(d);
		}
		else{
			initValues.get(i).add(d);
		}
		//this.initValues[j] = d;
	}
	
	public void addInitRates(Double d, int i){
		if (initRates.isEmpty()){
			for (int j = 0; j < vars.length; j++){
				initRates.add(j, new ArrayList<Double>());
			}
			initRates.get(i).add(d);
		}
		else{
			initRates.get(i).add(d);
		}
		//this.initRates[j] = d;
	}
	*/

	
}