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
package dataModels.lpn.parser;

import java.util.ArrayList;
import java.util.Properties;

import backend.verification.timed_state_exploration.zoneProject.InequalityVariable;

public class Variable {
	
	protected String name;
	
	private String type;
	
	protected String initValue;
	
	private String initRate;
	
	private String port;
	
	// For continuous variables, a list of inequality variables
	// that contain this continuous variable.
	protected ArrayList<InequalityVariable> inequalities;
	
	public Variable(String name, String type) {
		this.name = name;
		this.type = type;
	}
	
	public Variable(String name, String type, String initValue) {
		this.name = name;
		this.type = type;
		this.initValue = initValue;
	}
	
	public Variable(String name, String type, Properties initCond) {
		if (type.equals(CONTINUOUS)) {
			this.name = name;
			this.type = type;
			this.initValue = initCond.getProperty("value");
			this.initRate = initCond.getProperty("rate");
		}
	}
	
	public Variable(String name, String type, Properties initCond, String port) {
		if (type.equals(CONTINUOUS)) {
			this.name = name;
			this.type = type;
			this.initValue = initCond.getProperty("value");
			this.initRate = initCond.getProperty("rate");
			this.port = port;
		}
	}
	
	public Variable(String name, String type, String initValue, String port) {
		this.name = name;
		this.type = type;
		this.initValue = initValue;
		this.port = port;
	}
	
	public void addInitValue(String initValue) {
		this.initValue = initValue;
	}
	
	public void addInitRate(String initRate) {
		this.initRate = initRate;
	}
	
	public void addInitCond(Properties initCond) {
		if (type.equals(CONTINUOUS)) {
			this.initValue = initCond.getProperty("value");
			this.initRate = initCond.getProperty("rate");
		}
	}
	
	public String getName() {
		return name;
	}
	
	public String getInitValue() {
		return initValue;
	}
	
	public String getInitRate() {
		return initRate;
	}
	
	public boolean isInput() {
		if (port != null) {
		return port.equals(INPUT);
		}
		return false;
	}
	
	public boolean isOutput() {
		if (port != null) {
		return port.equals(OUTPUT);
		}
		return false;
	}

	public boolean isInternal() {
		if (port != null) {
		return port.equals(INTERNAL);
		}
		return true;
	}
	
	public String getPort() {
		return port;
	}
	
	public String getType() {
		return type;
	}
	
	public void setPort(String newPort) {
		port = newPort;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public boolean equals(Object var) {
		return name.equals(var.toString());
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((initRate == null) ? 0 : initRate.hashCode());
		result = prime * result
				+ ((initValue == null) ? 0 : initValue.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((port == null) ? 0 : port.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}
	
	/**
	 * Get the list of inequality variables that reference this continuous
	 * variable.
	 * @return
	 * 		A list of all the inequality variables that reference this continuous
	 * 		variable.
	 */
	public ArrayList<InequalityVariable> getInequalities(){
		return inequalities;
	}
	
	/**
	 * Add an inequality variable that references this continuous variable.
	 * @param inVar
	 * 		The InequalityVariable to add.
	 */
	public void addInequalityVariable(InequalityVariable inVar){
		if(inequalities == null){
			inequalities = new ArrayList<InequalityVariable>();
		}
		inequalities.add(inVar);
	}
	
	public static final String BOOLEAN = "boolean";
	
	public static final String INTEGER = "integer";
	
	public static final String CONTINUOUS = "continuous";
	
	public static final String INPUT = "input";
	
	public static final String OUTPUT = "output";
	
	public static final String INTERNAL = "internal";
	
}