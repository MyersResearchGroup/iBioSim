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
package main.java.edu.utah.ece.async.biomodel.network;


import main.java.edu.utah.ece.async.biomodel.visitor.SpeciesVisitor;



/**
 * This represents a spastic species.
 * @author Nam
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SpasticSpecies extends AbstractSpecies {
	public SpasticSpecies(String name, String stateName) {
		this.id = name;
		this.stateName = stateName;
	}
	
	public SpasticSpecies() {
		super();
	}
	
	
	@Override
	public void accept(SpeciesVisitor visitor) {
		visitor.visitSpasticSpecies(this);
	}
}

