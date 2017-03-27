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

/**
 * 
 */
package main.java.edu.utah.ece.async.biomodel.visitor;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;


import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.SBMLDocument;

import main.java.edu.utah.ece.async.biomodel.network.AbstractionEngine;
import main.java.edu.utah.ece.async.biomodel.network.BaseSpecies;
import main.java.edu.utah.ece.async.biomodel.network.ComplexSpecies;
import main.java.edu.utah.ece.async.biomodel.network.ConstantSpecies;
import main.java.edu.utah.ece.async.biomodel.network.DiffusibleConstitutiveSpecies;
import main.java.edu.utah.ece.async.biomodel.network.DiffusibleSpecies;
import main.java.edu.utah.ece.async.biomodel.network.Influence;
import main.java.edu.utah.ece.async.biomodel.network.NullSpecies;
import main.java.edu.utah.ece.async.biomodel.network.SpasticSpecies;
import main.java.edu.utah.ece.async.biomodel.network.SpeciesInterface;
import main.java.edu.utah.ece.async.biomodel.parser.BioModel;

/**
 * Visitor that visits species and generates the species list in the SBML file
 * 
 * @author Nam
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public abstract class AbstractPrintVisitor implements SpeciesVisitor {

	public AbstractPrintVisitor(SBMLDocument document) {
		this.document = document;
	}
	
	/**
	 * @param dimerizationAbstraction
	 *            The dimerizationAbstraction to set.
	 */
	public void setComplexAbstraction(boolean complexAbstraction) {
		this.complexAbstraction = complexAbstraction;
	}
	
	/**
	 * Returns the property if it exists, else return the default value
	 * @param property the property to check
	 * @return the property if it exists, else return the default value
	 */
	public static double getProperty(String key, Properties property, double defaultValue) {
		if (property.get(key) != null) {
			return Double.parseDouble(property.getProperty(key));
		}
		return defaultValue;
	}
	
	public static void setGCMFile(BioModel file) {
		parameters = file;
	}
	
	//Recursively breaks down repressing complex into its constituent species and complex formation equilibria
	protected String abstractComplex(String complexId, double multiplier) {
		AbstractionEngine e = new AbstractionEngine(species, complexMap, partsMap, 0, r, kl);
		String expression = e.abstractComplex(complexId, multiplier, false);
		return expression;
	}
	
	protected String sequesterSpecies(String partId, double n) {
		AbstractionEngine e = new AbstractionEngine(species, complexMap, partsMap, 0, r, kl);
		String expression = e.sequesterSpecies(partId, n, false);
		return expression;
	}
	
	protected String abstractDecay(String speciesId) {
		AbstractionEngine e = new AbstractionEngine(species, complexMap, partsMap, 0, r, kl);
		String expression = e.abstractDecay(speciesId);
		return expression;
	}
	
	@Override
	public void visitBaseSpecies(BaseSpecies specie) {}

	@Override
	public void visitComplex(ComplexSpecies specie) {}
	
	@Override
	public void visitConstantSpecies(ConstantSpecies specie) {}

	@Override
	public void visitNullSpecies(NullSpecies specie) {}

	@Override
	public void visitSpasticSpecies(SpasticSpecies specie) {}

	@Override
	public void visitSpecies(SpeciesInterface specie) {}
	
	@Override
	public void visitDiffusibleSpecies(DiffusibleSpecies species) {}

	@Override
	public void visitDiffusibleConstitutiveSpecies(DiffusibleConstitutiveSpecies species) {}
	
	protected static BioModel parameters = null;

	protected SBMLDocument document = null;
	
	protected boolean complexAbstraction = false;
	
	protected Reaction r;
	protected KineticLaw kl;
	protected HashMap<String, SpeciesInterface> species;
	protected HashMap<String, ArrayList<Influence>> complexMap;
	protected HashMap<String, ArrayList<Influence>> partsMap;
	
	
	
}
