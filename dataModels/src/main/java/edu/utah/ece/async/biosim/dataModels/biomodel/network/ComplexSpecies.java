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
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class ComplexSpecies extends AbstractSpecies {
	

	public ComplexSpecies(SpeciesInterface s) {
		id = s.getId();
		
		name = s.getName();
		
		type = s.getType();
		
		diffusible = s.isDiffusible();
		
		amount = s.getInitialAmount();
		
		concentration = s.getInitialConcentration();
		
//		RBS = s.getRBS();
//		
//		ORF = s.getORF();
		
		Kc = s.getKc();
		
		kd = s.getDecay();
		
		Kmdiff = s.getKmdiff();

		stateName = s.getStateName();
		
		isActivator = s.isActivator();
		
		isRepressor = s.isRepressor();
		
		isAbstractable = s.isAbstractable();
		
		isSequesterAbstractable = s.isSequesterAbstractable();
		
		isSequesterable = s.isSequesterable();
		
		isConvergent = s.isConvergent();
	}
	
	/**
	 * Empty constructor
	 *
	 */
	public ComplexSpecies() {
		super();
	}
	
	@Override
	public void accept(SpeciesVisitor visitor) {
		visitor.visitComplex(this);
	}
	
}