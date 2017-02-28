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

import dataModels.biomodel.network.ComplexSpecies;
import dataModels.biomodel.network.GeneticNetwork;
import dataModels.biomodel.network.Influence;
import dataModels.biomodel.network.SpeciesInterface;
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
public class PrintComplexVisitor extends AbstractPrintVisitor {
	
	public PrintComplexVisitor(SBMLDocument document, HashMap<String, SpeciesInterface> species,
			HashMap<String, Properties> compartments, HashMap<String, ArrayList<Influence>> complexMap, 
			HashMap<String, ArrayList<Influence>> partsMap) {
		super(document);
		this.species = species;
		this.complexMap = complexMap;
		this.partsMap = partsMap;
		this.compartments = compartments;
	}

	/**
	 * Prints out all the species to the file
	 * 
	 */
	public void run() {
		for (SpeciesInterface s : species.values()) {
			if (!complexAbstraction || (!s.isAbstractable() && !s.isSequesterAbstractable()))
				s.accept(this);
		}
	}

	@Override
	public void visitComplex(ComplexSpecies specie) {
		loadValues(specie);
		String compartment = checkCompartments(specie.getId());
		r = Utility.Reaction(GlobalConstants.COMPLEXATION + "_" + specie.getId());
		r.setCompartment(compartment);
		r.setReversible(true);
		r.setFast(false);
		r.addProduct(Utility.SpeciesReference(specie.getId(), 1));
		kl = r.createKineticLaw();
		String kcompId = kcompString;
		String kcompIdf = "kf_c";
		String kcompIdr = "kr_c";
		String compExpression = "";
		String boundExpression = specie.getId();
		String ncSum = ""; 
		double stoich = 0;
//		if (complexAbstraction) {
//			kcompId = kcompId + "__" + specie.getId();
//			kcompIdf = kcompIdf + "__" + specie.getId();
//			kcompIdr = kcompIdr + "__" + specie.getId();
//			compExpression = abstractComplex(specie.getId(), 1);
//			int index = compExpression.indexOf('*');
//			compExpression = compExpression.substring(index, compExpression.length());
//			for (Influence infl : complexMap.get(specie.getId())) {
//				stoich += infl.getCoop();
//				String partId = infl.getInput();
//				String nId = coopString + "__" + partId + "_" + specie.getId();
//				ncSum = ncSum + nId + "+";
//			}
		if (complexAbstraction && specie.isSequesterable())
			boundExpression = sequesterSpecies(specie.getId(), 0);
//		} else {
		for (Influence infl : complexMap.get(specie.getId())) {
			String partId = infl.getInput();
			stoich += infl.getCoop();
			r.addReactant(Utility.SpeciesReference(partId, infl.getCoop()));
			String nId = coopString + "__" + partId + "_" + specie.getId();
			kl.addLocalParameter(Utility.Parameter(nId, infl.getCoop(), "dimensionless"));
			ncSum = ncSum + nId + "+";
			compExpression = compExpression + "*" + "(" + partId + ")^" + nId;
		}
//		}	
		if (stoich == 1)
			kl.addLocalParameter(Utility.Parameter(kcompIdf, kf, GeneticNetwork.getMoleTimeParameter(1)));
		else if (stoich >= 2) {
			kl.addLocalParameter(Utility.Parameter(kcompIdf, kf, GeneticNetwork.getMoleTimeParameter(2)));
			if (stoich > 2)
				kl.addLocalParameter(Utility.Parameter(kcompId, kcomp, GeneticNetwork.getMoleParameter(2)));
		}
		kl.addLocalParameter(Utility.Parameter(kcompIdr, kr, GeneticNetwork.getMoleTimeParameter(1)));
		kl.setMath(SBMLutilities.myParseFormula(generateLaw(compExpression, boundExpression, kcompId, kcompIdf, kcompIdr, ncSum, stoich)));
		Utility.addReaction(document, r);
	}
	
	private static String generateLaw(String compExpression, String boundExpression, String kcompId, String kcompIdf, 
			String kcompIdr, String ncSum, double stoich) {
		String law = "";
		if (stoich == 1 || stoich == 2)
			law = kcompIdf + compExpression + "-" + kcompIdr + "*" + boundExpression;
		else if (stoich > 2)
			law = kcompIdf + "*" + kcompId + "^" + "(" + ncSum.substring(0, ncSum.length() - 1) + "-2)" + compExpression 
				+ "-" + kcompIdr + "*" + boundExpression;
		return law;
	}
	
	//Checks whether equilibrium constants are given as forward and reverse rate constants before loading values
	private void loadValues(SpeciesInterface s) {
		double[] kcompArray = s.getKc();
		kf = kcompArray[0];
		if (kcompArray.length == 2) {
			kcomp = kcompArray[0]/kcompArray[1];
			kr = kcompArray[1];
		} else {
			kcomp = kcompArray[0];
			kr = 1;
		}
	}
	
	//Checks if species belongs in a compartment other than default
	private String checkCompartments(String species) {
		String compartment = document.getModel().getCompartment(0).getId();
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
		return compartment;
	}
	
	private HashMap<String, Properties> compartments;
	
	private double kf;
	private double kcomp;
	private double kr;

	
	private String kcompString = GlobalConstants.KCOMPLEX_STRING;
	private String coopString = GlobalConstants.COOPERATIVITY_STRING;
	
}
