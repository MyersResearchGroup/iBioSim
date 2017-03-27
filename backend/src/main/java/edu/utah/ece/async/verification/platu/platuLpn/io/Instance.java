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
package main.java.edu.utah.ece.async.verification.platu.platuLpn.io;

import java.util.List;


public class Instance {
	private String name = "";
	private String lpnLabel = null;
	private List<String> variableList = null;
	private List<String> moduleList = null;
	
	public Instance(String lpnLabel, String name, List<String> varList, List<String> modList){
		this.name = name;
		this.lpnLabel = lpnLabel;
		this.variableList = varList;
		this.moduleList = modList;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getLpnLabel(){
		return this.lpnLabel;
	}
	
	public List<String> getVariableList(){
		return this.variableList;
	}
	
	public List<String> getModuleList(){
		return this.moduleList;
	}
}
