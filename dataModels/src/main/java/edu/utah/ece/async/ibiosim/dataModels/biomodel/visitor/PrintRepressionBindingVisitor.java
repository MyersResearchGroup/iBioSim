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
package edu.utah.ece.async.ibiosim.dataModels.biomodel.visitor;

import java.util.ArrayList;
import java.util.HashMap;

import org.sbml.jsbml.SBMLDocument;

import edu.utah.ece.async.ibiosim.dataModels.biomodel.network.BaseSpecies;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.network.ComplexSpecies;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.network.ConstantSpecies;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.network.DiffusibleConstitutiveSpecies;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.network.DiffusibleSpecies;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.network.GeneticNetwork;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.network.Influence;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.network.Promoter;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.network.SpasticSpecies;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.network.SpeciesInterface;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.util.SBMLutilities;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.util.Utility;
import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class PrintRepressionBindingVisitor extends AbstractPrintVisitor {

	public PrintRepressionBindingVisitor(SBMLDocument document, Promoter p, HashMap<String, SpeciesInterface> species, 
			HashMap<String, ArrayList<Influence>> complexMap, 
			HashMap<String, ArrayList<Influence>> partsMap) {
		super(document);
		this.promoter = p;
		this.species = species;
		this.complexMap = complexMap;
		this.partsMap = partsMap;
		//this.compartment = compartment;
	}

	/**
	 * Prints out all the species to the file
	 * 
	 */
	public void run() {
		for (SpeciesInterface specie : promoter.getRepressors()) {
			String repressor = specie.getId();
			String[] splitted = repressor.split("__");
			if (splitted.length > 1)
				repressor = splitted[1];
			boundId = promoter.getId() + "_" + repressor;
			reactionId = "R_repression_binding_" + promoter.getId() + "_" + repressor;
			specie.accept(this);
		}
	}

	@Override
	public void visitSpecies(SpeciesInterface specie) {

	}

	@Override
	public void visitComplex(ComplexSpecies specie) {
		loadValues(specie);
		r = Utility.Reaction(reactionId);
		r.setCompartment(promoter.getCompartment());
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		r.addProduct(Utility.SpeciesReference(boundId, 1));
		r.setReversible(true);
		r.setFast(false);
		kl = r.createKineticLaw();
		if (kf >= 0) {
			Utility.Parameter(kl, GlobalConstants.FORWARD_KREP_STRING, kf, GeneticNetwork.getMoleTimeParameter(2));
			Utility.Parameter(kl, GlobalConstants.REVERSE_KREP_STRING, kr, GeneticNetwork.getMoleTimeParameter(1));
		}
		if ((coop > 1)&&(krep >= 0))
			Utility.Parameter(kl, krepString, krep,	GeneticNetwork.getMoleParameter(2));
		if (coop >= 0)
			Utility.Parameter(kl, coopString, coop, "dimensionless");
		else 
			coop = document.getModel().getParameter(coopString).getValue();
		String repExpression = "";
		if (complexAbstraction && specie.isAbstractable()) {
			repExpression = abstractComplex(specie.getId(), coop);
		} else if (complexAbstraction && specie.isSequesterable()) {
			repExpression = sequesterSpecies(specie.getId(), coop);
		} else {
			repExpression = specie.getId();
			r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
		}
		kl.setMath(SBMLutilities.myParseFormula(generateLaw(repExpression)));
		Utility.addReaction(document, r);
	}
	
	@Override
	public void visitBaseSpecies(BaseSpecies specie) {
		loadValues(specie);
		r = Utility.Reaction(reactionId);
		r.setCompartment(promoter.getCompartment());
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		r.addProduct(Utility.SpeciesReference(boundId, 1));
		r.setReversible(true);
		r.setFast(false);
		kl = r.createKineticLaw();
		if (kf >= 0) {
			Utility.Parameter(kl, GlobalConstants.FORWARD_KREP_STRING, kf, GeneticNetwork.getMoleTimeParameter(2));
			Utility.Parameter(kl, GlobalConstants.REVERSE_KREP_STRING, kr, GeneticNetwork.getMoleTimeParameter(1));
		}
		if ((coop > 1)&&(krep >= 0))
			Utility.Parameter(kl, krepString, krep,	GeneticNetwork.getMoleParameter(2));
		if (coop >= 0)
			Utility.Parameter(kl, coopString, coop, "dimensionless");
		else 
			coop = document.getModel().getParameter(coopString).getValue();
		String repExpression = "";
		//Checks for valid complex sequestering of repressing species if complex abstraction is selected
		if (complexAbstraction && specie.isSequesterable()) {
			repExpression = repExpression + sequesterSpecies(specie.getId(), coop);
		} else {
			repExpression = specie.getId();
			r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
		}
		kl.setMath(SBMLutilities.myParseFormula(generateLaw(repExpression)));
		Utility.addReaction(document, r);
	}

	@Override
	public void visitConstantSpecies(ConstantSpecies specie) {
		loadValues(specie);
		r = Utility.Reaction(reactionId);
		r.setCompartment(promoter.getCompartment());
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		r.addProduct(Utility.SpeciesReference(boundId, 1));
		r.setReversible(true);
		r.setFast(false);
		kl = r.createKineticLaw();
		if (kf >= 0) {
			Utility.Parameter(kl, GlobalConstants.FORWARD_KREP_STRING, kf, GeneticNetwork.getMoleTimeParameter(2));
			Utility.Parameter(kl, GlobalConstants.REVERSE_KREP_STRING, kr, GeneticNetwork.getMoleTimeParameter(1));
		}
		if ((coop > 1)&&(krep >= 0))
			Utility.Parameter(kl, krepString, krep,	GeneticNetwork.getMoleParameter(2));
		if (coop >= 0)
			Utility.Parameter(kl, coopString, coop, "dimensionless");
		else 
			coop = document.getModel().getParameter(coopString).getValue();
		String repExpression = "";
		//Checks for valid complex sequestering of repressing species if complex abstraction is selected
		if (complexAbstraction && specie.isSequesterable()) {
			repExpression = repExpression + sequesterSpecies(specie.getId(), coop);
		} else {
			repExpression = specie.getId();
			r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
		}
		kl.setMath(SBMLutilities.myParseFormula(generateLaw(repExpression)));
		Utility.addReaction(document, r);
	}

	@Override
	public void visitSpasticSpecies(SpasticSpecies specie) {
		loadValues(specie);
		r = Utility.Reaction(reactionId);
		r.setCompartment(promoter.getCompartment());
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		r.addProduct(Utility.SpeciesReference(boundId, 1));
		r.setReversible(true);
		r.setFast(false);
		kl = r.createKineticLaw();
		if (kf >= 0) {
			Utility.Parameter(kl, GlobalConstants.FORWARD_KREP_STRING, kf, GeneticNetwork.getMoleTimeParameter(2));
			Utility.Parameter(kl, GlobalConstants.REVERSE_KREP_STRING, kr, GeneticNetwork.getMoleTimeParameter(1));
		}
		if ((coop > 1)&&(krep >= 0))
			Utility.Parameter(kl, krepString, krep,	GeneticNetwork.getMoleParameter(2));
		if (coop >= 0)
			Utility.Parameter(kl, coopString, coop, "dimensionless");
		else 
			coop = document.getModel().getParameter(coopString).getValue();
		String repExpression = "";
		//Checks for valid complex sequestering of repressing species if complex abstraction is selected
		if (complexAbstraction && specie.isSequesterable()) {
			repExpression = repExpression + sequesterSpecies(specie.getId(), coop);
		} else {
			repExpression = specie.getId();
			r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
		}
		kl.setMath(SBMLutilities.myParseFormula(generateLaw(repExpression)));
		Utility.addReaction(document, r);
	}

	@Override
	public void visitDiffusibleSpecies(DiffusibleSpecies specie) {
		
		loadValues(specie);
		r = Utility.Reaction(reactionId);
		r.setCompartment(promoter.getCompartment());
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		r.addProduct(Utility.SpeciesReference(boundId, 1));
		r.setReversible(true);
		r.setFast(false);
		kl = r.createKineticLaw();
		if (kf >= 0) {
			Utility.Parameter(kl, GlobalConstants.FORWARD_KREP_STRING, kf, GeneticNetwork.getMoleTimeParameter(2));
			Utility.Parameter(kl, GlobalConstants.REVERSE_KREP_STRING, kr, GeneticNetwork.getMoleTimeParameter(1));
		}
		if ((coop > 1)&&(krep >= 0))
			Utility.Parameter(kl, krepString, krep,	GeneticNetwork.getMoleParameter(2));
		if (coop >= 0)
			Utility.Parameter(kl, coopString, coop, "dimensionless");
		else 
			coop = document.getModel().getParameter(coopString).getValue();
		String repExpression = "";
		//Checks for valid complex sequestering of repressing species if complex abstraction is selected
		if (complexAbstraction && specie.isSequesterable()) {
			repExpression = repExpression + sequesterSpecies(specie.getId(), coop);
		} else {
			repExpression = specie.getId();
			r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
		}
		kl.setMath(SBMLutilities.myParseFormula(generateLaw(repExpression)));
		Utility.addReaction(document, r);
	}
	
	@Override
	public void visitDiffusibleConstitutiveSpecies(DiffusibleConstitutiveSpecies specie) {
		
		loadValues(specie);
		r = Utility.Reaction(reactionId);
		r.setCompartment(promoter.getCompartment());
		r.addReactant(Utility.SpeciesReference(promoter.getId(), 1));
		r.addProduct(Utility.SpeciesReference(boundId, 1));
		r.setReversible(true);
		r.setFast(false);
		kl = r.createKineticLaw();
		if (kf >= 0) {
			Utility.Parameter(kl, GlobalConstants.FORWARD_KREP_STRING, kf, GeneticNetwork.getMoleTimeParameter(2));
			Utility.Parameter(kl, GlobalConstants.REVERSE_KREP_STRING, kr, GeneticNetwork.getMoleTimeParameter(1));
		}
		if ((coop > 1)&&(krep >= 0))
			Utility.Parameter(kl, krepString, krep,	GeneticNetwork.getMoleParameter(2));
		if (coop >= 0)
			Utility.Parameter(kl, coopString, coop, "dimensionless");
		else 
			coop = document.getModel().getParameter(coopString).getValue();
		String repExpression = "";
		//Checks for valid complex sequestering of repressing species if complex abstraction is selected
		if (complexAbstraction && specie.isSequesterable()) {
			repExpression = repExpression + sequesterSpecies(specie.getId(), coop);
		} else {
			repExpression = specie.getId();
			r.addReactant(Utility.SpeciesReference(specie.getId(), coop));
		}
		kl.setMath(SBMLutilities.myParseFormula(generateLaw(repExpression)));
		Utility.addReaction(document, r);
	}	
	
	/**
	 * Generates a kinetic law
	 * 
	 * @param specieName
	 *            specie name
	 * @param repExpression
	 *            repressor molecule
	 * @return
	 */
	private String generateLaw(String repExpression) {
		String law = "";
		if (coop == 1)
			law = GlobalConstants.FORWARD_KREP_STRING + "*" + "(" + repExpression + ")" + "^" + coopString + "*" + 
					promoter.getId() + "-" + GlobalConstants.REVERSE_KREP_STRING + "*" + boundId;
		else if (coop > 1)
			law = GlobalConstants.FORWARD_KREP_STRING + "*" + "(" + krepString + ")" + "^" + "(" + coopString + 
			"-1" + ")" + "*" + "(" + repExpression + ")" + "^" + coopString + "*" + promoter.getId() + 
			"-" + GlobalConstants.REVERSE_KREP_STRING + "*" + boundId;
		return law;
	}

	// Checks if binding parameters are specified as forward and reverse rate constants or
	// as equilibrium binding constants before loading values
	private void loadValues(SpeciesInterface s) {
		Influence r = promoter.getRepressionMap().get(s.getId());
		coop = r.getCoop();
		double[] krepArray = r.getRep();
		kf = krepArray[0];
		if (krepArray.length == 2) {
			krep = krepArray[0]/krepArray[1];
			kr = krepArray[1];
		} else {
			krep = krepArray[0];
			kr = 1;
		}
	}
		

	private Promoter promoter;
	
	private double coop;
	private double kf;
	private double krep;
	private double kr;

	private String krepString = GlobalConstants.KREP_STRING;
	private String coopString = GlobalConstants.COOPERATIVITY_STRING;

	private String boundId;
	private String reactionId;
	//private String compartment;
}

