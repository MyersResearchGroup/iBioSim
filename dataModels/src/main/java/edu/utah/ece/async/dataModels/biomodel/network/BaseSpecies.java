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
package edu.utah.ece.async.dataModels.biomodel.network;

import edu.utah.ece.async.dataModels.biomodel.visitor.SpeciesVisitor;

/**
 * This is the most basic implementation of the species class.  
 * @author Nam
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class BaseSpecies extends AbstractSpecies {
	/**
	 * Constructor with parameters
	 * 
	 * @param name
	 *            the name of the species.
	 * @param stateName
	 *            the state name of the species.
	 * @param dimerizationConstant
	 *            the dimerization constants associated with the species.
	 * @param decayRate
	 *            the decay rates associated with the species.
	 * @param maxDimer
	 * 			  the maximum monomers can combine to form dimer
	 */
	public BaseSpecies(String name, String stateName) {
		this.id = name;
		this.stateName = stateName;
	}
	
	/**
	 * Empty constructor
	 *
	 */
	public BaseSpecies() {
		super();
	}
	
	
	@Override
	public void accept(SpeciesVisitor visitor) {
		visitor.visitBaseSpecies(this);
	}
}

