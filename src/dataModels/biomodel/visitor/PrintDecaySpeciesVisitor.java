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
package dataModels.biomodel.visitor;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.sbml.jsbml.SBMLDocument;

import dataModels.biomodel.network.BaseSpecies;
import dataModels.biomodel.network.ComplexSpecies;
import dataModels.biomodel.network.ConstantSpecies;
import dataModels.biomodel.network.DiffusibleConstitutiveSpecies;
import dataModels.biomodel.network.DiffusibleSpecies;
import dataModels.biomodel.network.GeneticNetwork;
import dataModels.biomodel.network.Influence;
import dataModels.biomodel.network.SpasticSpecies;
import dataModels.biomodel.network.SpeciesInterface;
import dataModels.biomodel.parser.BioModel;
import dataModels.biomodel.util.SBMLutilities;
import dataModels.biomodel.util.Utility;
import dataModels.util.GlobalConstants;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class PrintDecaySpeciesVisitor extends AbstractPrintVisitor {

	public PrintDecaySpeciesVisitor(SBMLDocument document,
			HashMap<String, SpeciesInterface> species, HashMap<String, Properties> compartments, 
			HashMap<String, ArrayList<Influence>> complexMap, HashMap<String, ArrayList<Influence>> partsMap) {
		super(document);
		this.species = species;
		this.complexMap = complexMap;
		this.partsMap = partsMap;
		this.compartments = compartments;
		addDecayUnit();
	}
	
	private void addDecayUnit() {
		decayUnitString = GeneticNetwork.getMoleTimeParameter(1);
	}

	/**
	 * Prints out all the species to the file
	 * 
	 */
	public void run() {		
		for (SpeciesInterface s : species.values()) {
			s.accept(this);
		}
	}

	@Override
	public void visitSpecies(SpeciesInterface specie) {

	}
	
	@Override
	public void visitComplex(ComplexSpecies specie) {
		loadValues(specie);
		if (!complexAbstraction || (!specie.isAbstractable() && !specie.isSequesterAbstractable())) {	
			r = document.getModel().getReaction(GlobalConstants.DEGRADATION + "_" + specie.getId());
			if (r!=null && BioModel.isGridReaction(r)) {
				return;
			}
			String compartment = checkCompartments(specie.getId());
			r = Utility.Reaction(GlobalConstants.DEGRADATION + "_" + specie.getId());
			r.setCompartment(compartment);
			r.setReversible(false);
			r.setFast(false);
			kl = r.createKineticLaw();
			String decayExpression = "";
			if (complexAbstraction && specie.isSequesterable()) {
				decayExpression = abstractDecay(specie.getId());
				if (decayExpression.length() > 0) {
					kl.setMath(SBMLutilities.myParseFormula(decayExpression));
					Utility.addReaction(document, r);
				}
			} else if (decay > 0 || decay==-1){
				decayExpression = decayString + "*" + specie.getId();
				if (decay > 0)
					kl.addLocalParameter(Utility.Parameter(decayString, decay, decayUnitString));
				r.addReactant(Utility.SpeciesReference(specie.getId(), 1));
				kl.setMath(SBMLutilities.myParseFormula(decayExpression));
				Utility.addReaction(document, r);
			}
		}
	}

	@Override
	public void visitBaseSpecies(BaseSpecies specie) {
		
		/*
		if (specie.getId().contains("__") == false)
			return;
		*/
		
		loadValues(specie);
		r = document.getModel().getReaction(GlobalConstants.DEGRADATION + "_" + specie.getId());
		if (r!=null && BioModel.isGridReaction(r)) {
			return;
		}
		String compartment = checkCompartments(specie.getId());			
		r = Utility.Reaction(GlobalConstants.DEGRADATION + "_" + specie.getId());
		r.setCompartment(compartment);
		r.setReversible(false);
		r.setFast(false);
		kl = r.createKineticLaw();
		String decayExpression = "";
		if (complexAbstraction && specie.isSequesterable()) {
			decayExpression = abstractDecay(specie.getId());
			if (decayExpression.length() > 0) {
				kl.setMath(SBMLutilities.myParseFormula(decayExpression));
				Utility.addReaction(document, r);
			}
		} else if (decay > 0 || decay==-1){
			decayExpression = decayString + "*" + specie.getId();
			if (decay > 0)
				kl.addLocalParameter(Utility.Parameter(decayString, decay, decayUnitString));
			r.addReactant(Utility.SpeciesReference(specie.getId(), 1));
			kl.setMath(SBMLutilities.myParseFormula(decayExpression));
			Utility.addReaction(document, r);
		}
	}

	@Override
	public void visitConstantSpecies(ConstantSpecies specie) {
		//do nothing, constant species can't decay
	}

	@Override
	public void visitSpasticSpecies(SpasticSpecies specie) {
		loadValues(specie);
		r = document.getModel().getReaction(GlobalConstants.DEGRADATION + "_" + specie.getId());
		if (r!=null && BioModel.isGridReaction(r)) {
			return;
		}
		String compartment = checkCompartments(specie.getId());
		r = Utility.Reaction(GlobalConstants.DEGRADATION + "_" + specie.getId());
		r.setCompartment(compartment);
		r.setReversible(false);
		r.setFast(false);
		kl = r.createKineticLaw();
		String decayExpression = "";
		if (complexAbstraction && specie.isSequesterable()) {
			decayExpression = abstractDecay(specie.getId());
			if (decayExpression.length() > 0) {
				kl.setMath(SBMLutilities.myParseFormula(decayExpression));
				Utility.addReaction(document, r);
			}
		} else if (decay > 0 || decay==-1){
			decayExpression = decayString + "*" + specie.getId();
			if (decay > 0)
				kl.addLocalParameter(Utility.Parameter(decayString, decay, decayUnitString));
			r.addReactant(Utility.SpeciesReference(specie.getId(), 1));
			kl.setMath(SBMLutilities.myParseFormula(decayExpression));
			Utility.addReaction(document, r);
		}
	}
	
	@Override
	public void visitDiffusibleSpecies(DiffusibleSpecies species) {
		
		//this is now added during component dropping
		
		
		
//		loadValues(species);
//		
//		String ID = species.getId();
//		Reaction r = Utility.Reaction("Degradation_" + ID);
//		r.setCompartment(checkCompartments(ID));
//		r.setReversible(false);
//		r.setFast(false);
//		KineticLaw kl = r.createKineticLaw();
//		String decayExpression = "";
//		
//		if (decay > 0 || decay==-1) {
//			
//			//this is the mathematical expression for the decay
//			decayExpression = decayString + "*" + ID;
//
//			r.addReactant(Utility.SpeciesReference(ID, 1));
//
//			//parameter: id="kd" value=isDecay (usually 0.0075) units="u_1_second_n1" (inverse seconds)
//			if (decay > 0)
//				kl.addParameter(Utility.Parameter(decayString, decay, decayUnitString));
//			
//			//formula: kd * inner species
//			kl.setMath(SBMLutilities.myParseFormula(decayExpression);
//			Utility.addReaction(document, r);
//		}
	}
	
	@Override
	public void visitDiffusibleConstitutiveSpecies(DiffusibleConstitutiveSpecies specie) {
		
		loadValues(specie);
		String compartment = checkCompartments(specie.getId());
		r = document.getModel().getReaction(GlobalConstants.DEGRADATION + "_" + specie.getId());
		if (r!=null && BioModel.isGridReaction(r)) {
			return;
		}
		r = Utility.Reaction(GlobalConstants.DEGRADATION + "_" + specie.getId());
		r.setCompartment(compartment);
		r.setReversible(false);
		r.setFast(false);
		kl = r.createKineticLaw();
		String decayExpression = "";

		if (decay > 0 || decay == -1){
			decayExpression = decayString + "*" + specie.getId();
			if (decay>0)
				kl.addLocalParameter(Utility.Parameter(decayString, decay, decayUnitString));
			r.addReactant(Utility.SpeciesReference(specie.getId(), 1));
			kl.setMath(SBMLutilities.myParseFormula(decayExpression));
			Utility.addReaction(document, r);
		}
	}
	
	private void loadValues(SpeciesInterface specie) {
		decay = specie.getDecay();
	}
	
	//Checks if species belongs in a compartment other than default
	private String checkCompartments(String species) {
		
		String compartment = document.getModel().getCompartment(0).getId();
		//String[] splitted = species.split("__");
		String component = species;
		
		while (component.contains("__")) {
			
			component = component.substring(0,component.lastIndexOf("__"));
			
			for (String compartmentName : compartments.keySet()) {
				
				if (compartmentName.equals(component))
					return compartmentName;					
				else if (compartmentName.contains("__") && compartmentName.substring(0, compartmentName.lastIndexOf("__"))
						.equals(component)) {
					return compartmentName;
				}
			}
		}
		/*
		if (compartments.contains(splitted[0]))
			compartment = splitted[0];
			*/
		return compartment;
	}
	
	private double decay;
	private String decayUnitString;
	private String decayString = GlobalConstants.KDECAY_STRING;
	//private String kcompString = GlobalConstants.KCOMPLEX_STRING; 
	
	private HashMap<String, Properties> compartments;
}
