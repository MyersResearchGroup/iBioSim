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
package edu.utah.ece.async.biosim.dataModels.biomodel.network;


import edu.utah.ece.async.biosim.dataModels.biomodel.visitor.SpeciesVisitor;



/**
 * This represents a constant species.
 * @author Nam
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class ConstantSpecies extends AbstractSpecies {
	public ConstantSpecies(String name, String stateName) {
		//this.properties = properties;
		this.id = name;
		this.stateName = stateName;
	}
	
	public ConstantSpecies() {
		super();
	}
	
	
	@Override
	public void accept(SpeciesVisitor visitor) {
		visitor.visitConstantSpecies(this);
	}
}

