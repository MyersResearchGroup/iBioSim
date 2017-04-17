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
package edu.utah.ece.async.biosim.dataModels.biomodel.parser;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.prefs.Preferences;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

import edu.utah.ece.async.biosim.dataModels.biomodel.network.BaseSpecies;
import edu.utah.ece.async.biosim.dataModels.biomodel.network.DiffusibleConstitutiveSpecies;
import edu.utah.ece.async.biosim.dataModels.biomodel.network.DiffusibleSpecies;
import edu.utah.ece.async.biosim.dataModels.biomodel.network.GeneticNetwork;
import edu.utah.ece.async.biosim.dataModels.biomodel.network.Influence;
import edu.utah.ece.async.biosim.dataModels.biomodel.network.Promoter;
import edu.utah.ece.async.biosim.dataModels.biomodel.network.SpasticSpecies;
import edu.utah.ece.async.biosim.dataModels.biomodel.network.SpeciesInterface;
import edu.utah.ece.async.biosim.dataModels.util.GlobalConstants;

/**
 * This class parses a genetic circuit model.
 * 
 * @author Nam Nguyen
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class GCMParser {
	
	private String separator;

	public GCMParser(String filename) throws XMLStreamException, IOException {
		separator = GlobalConstants.separator;
		//this.debug = debug;
		biomodel = new BioModel(filename.substring(0, filename.length()
				- filename.split(separator)[filename.split(separator).length - 1]
						.length()));
		biomodel.load(filename);
		
		data = new StringBuffer();
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String str;
			while ((str = in.readLine()) != null) {
				data.append(str + "\n");
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Error opening file");
		}
	}
	
	public GCMParser(BioModel gcm) {
		separator = GlobalConstants.separator;
		//this.debug = debug;
		this.biomodel = gcm;
	}
	
	public GeneticNetwork buildNetwork(SBMLDocument sbml) {		
		
		if (sbml == null) return null;

		speciesList = new HashMap<String, SpeciesInterface>();
		promoterList = new HashMap<String, Promoter>();
		complexMap = new HashMap<String, ArrayList<Influence>>();
		partsMap = new HashMap<String, ArrayList<Influence>>();

		// Need to first parse all species that aren't promoters before parsing promoters
		// since latter process refers to the species list
		for (int i=0; i<sbml.getModel().getSpeciesCount(); i++) {
			Species species = sbml.getModel().getSpecies(i);
			if (!BioModel.isPromoterSpecies(species)) 
				parseSpeciesData(sbml,species);
		}
		for (int i=0; i<sbml.getModel().getSpeciesCount(); i++) {
			Species species = sbml.getModel().getSpecies(i);
			if (BioModel.isPromoterSpecies(species))
				parsePromoterData(sbml,species);
		}
		
		GeneticNetwork network = new GeneticNetwork(speciesList, complexMap, partsMap, promoterList, biomodel);
		
		network.setSBMLFile(biomodel.getSBMLFile());
		network.setSBML(sbml);
		
		return network;		
	}

	public HashMap<String, SpeciesInterface> getSpecies() {
		return speciesList;
	}

	public void setSpecies(HashMap<String, SpeciesInterface> speciesList) {
		this.speciesList = speciesList;
	}

	public HashMap<String, Promoter> getPromoters() {
		return promoterList;
	}

	public void setPromoters(HashMap<String, Promoter> promoterList) {
		this.promoterList = promoterList;
	}

	private void parsePromoterData(SBMLDocument sbml, Species promoter) {
		Preferences biosimrc = Preferences.userRoot();
		Promoter p = new Promoter();
		p.setId(promoter.getId());
		promoterList.put(promoter.getId(), p);
		p.setInitialAmount(promoter.getInitialAmount());
		Reaction production = BioModel.getProductionReaction(promoter.getId(), sbml.getModel());
		if (production != null) {
			p.setCompartment(production.getCompartment());
			if (production.getKineticLaw().getLocalParameter(GlobalConstants.ACTIVATED_STRING) != null) {
				p.setKact(production.getKineticLaw().getLocalParameter(GlobalConstants.ACTIVATED_STRING).getValue());
			} else if (sbml.getModel().getParameter(GlobalConstants.ACTIVATED_STRING) != null) {
				p.setKact(sbml.getModel().getParameter(GlobalConstants.ACTIVATED_STRING).getValue());
			} else {
				p.setKact(Double.parseDouble(biosimrc.get("biosim.gcm.ACTIVED_VALUE", "")));
			}
			if (production.getKineticLaw().getLocalParameter(GlobalConstants.KBASAL_STRING) != null) {
				p.setKbasal(production.getKineticLaw().getLocalParameter(GlobalConstants.KBASAL_STRING).getValue());
			} else if (sbml.getModel().getParameter(GlobalConstants.KBASAL_STRING) != null) {
				p.setKbasal(sbml.getModel().getParameter(GlobalConstants.KBASAL_STRING).getValue());
			} else {
				p.setKbasal(Double.parseDouble(biosimrc.get("biosim.gcm.KBASAL_VALUE", "")));
			}
			if (production.getKineticLaw().getLocalParameter(GlobalConstants.OCR_STRING) != null) {
				p.setKoc(production.getKineticLaw().getLocalParameter(GlobalConstants.OCR_STRING).getValue());
			} else if (sbml.getModel().getParameter(GlobalConstants.OCR_STRING) != null) {
				p.setKoc(sbml.getModel().getParameter(GlobalConstants.OCR_STRING).getValue());
			} else {
				p.setKoc(Double.parseDouble(biosimrc.get("biosim.gcm.OCR_VALUE", "")));
			}
			if (production.getKineticLaw().getLocalParameter(GlobalConstants.STOICHIOMETRY_STRING) != null) {
				p.setStoich(production.getKineticLaw().getLocalParameter(GlobalConstants.STOICHIOMETRY_STRING).getValue());
			} else if (sbml.getModel().getParameter(GlobalConstants.STOICHIOMETRY_STRING) != null) {
				p.setStoich(sbml.getModel().getParameter(GlobalConstants.STOICHIOMETRY_STRING).getValue());
			} else {
				p.setStoich(Double.parseDouble(biosimrc.get("biosim.gcm.STOICHIOMETRY_VALUE", "")));
			}
			if (production.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_RNAP_BINDING_STRING)!=null &&
				production.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_RNAP_BINDING_STRING)!=null) {
				p.setKrnap(production.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_RNAP_BINDING_STRING).getValue(),
						   production.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_RNAP_BINDING_STRING).getValue());
			} else if ((sbml.getModel().getParameter(GlobalConstants.FORWARD_RNAP_BINDING_STRING) != null)  && 
					(sbml.getModel().getParameter(GlobalConstants.REVERSE_RNAP_BINDING_STRING) != null)) {
				p.setKrnap(sbml.getModel().getParameter(GlobalConstants.FORWARD_RNAP_BINDING_STRING).getValue(),
						   sbml.getModel().getParameter(GlobalConstants.REVERSE_RNAP_BINDING_STRING).getValue());
			} else {
				p.setKrnap(Double.parseDouble(biosimrc.get("biosim.gcm.RNAP_BINDING_VALUE", "")),1.0);
			}
			if (production.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING)!=null &&
					production.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING)!=null) {
				p.setKArnap(production.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING).getValue(),
						production.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING).getValue());
			} else if ((sbml.getModel().getParameter(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING) != null)  && 
					(sbml.getModel().getParameter(GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING) != null))  {
				p.setKArnap(sbml.getModel().getParameter(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING).getValue(),
						sbml.getModel().getParameter(GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING).getValue());
			} else {
				p.setKArnap(Double.parseDouble(biosimrc.get("biosim.gcm.ACTIVATED_RNAP_BINDING_VALUE", "")),1.0);
			}
			for (int j = 0; j < production.getProductCount(); j++) {
				SpeciesReference product = production.getProduct(j);
				if (!BioModel.isMRNASpecies(sbml,sbml.getModel().getSpecies(product.getSpecies())))
					p.addOutput(product.getSpecies(),speciesList.get(product.getSpecies()));
			}
			for (int i = 0; i < production.getModifierCount(); i++) {
				ModifierSpeciesReference modifier = production.getModifier(i);
				if (BioModel.isRepressor(modifier) || BioModel.isRegulator(modifier)) {
					for (int j = 0; j < production.getProductCount(); j++) {
						SpeciesReference product = production.getProduct(j);
						Influence infl = new Influence();		
						infl.generateName();
						infl.setType("tee");
						infl.setInput(modifier.getSpecies());
						if (BioModel.isMRNASpecies(sbml,sbml.getModel().getSpecies(product.getSpecies()))) {
							infl.setOutput("none");
						} else {
							infl.setOutput(product.getSpecies());
//							p.addOutput(product.getSpecies(),speciesList.get(product.getSpecies()));
						}
						p.addToReactionMap(modifier.getSpecies(), infl);
						p.addRepressor(modifier.getSpecies(), speciesList.get(modifier.getSpecies()));
						speciesList.get(modifier.getSpecies()).setRepressor(true);
						if (production.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_KREP_STRING.replace("_","_"+modifier.getSpecies()+"_"))!=null &&
							production.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_KREP_STRING.replace("_","_"+modifier.getSpecies()+"_"))!=null) {
							infl.setRep(production.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_KREP_STRING.replace("_","_"+modifier.getSpecies()+"_")).getValue(),
									production.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_KREP_STRING.replace("_","_"+modifier.getSpecies()+"_")).getValue());
						} else if ((sbml.getModel().getParameter(GlobalConstants.FORWARD_KREP_STRING) != null)  && 
								(sbml.getModel().getParameter(GlobalConstants.REVERSE_KREP_STRING) != null))  {
							infl.setRep(sbml.getModel().getParameter(GlobalConstants.FORWARD_KREP_STRING).getValue(),
									sbml.getModel().getParameter(GlobalConstants.REVERSE_KREP_STRING).getValue());
						} else {
							infl.setRep(Double.parseDouble(biosimrc.get("biosim.gcm.KREP_VALUE", "")),1.0);
						}
						if (production.getKineticLaw().getLocalParameter(GlobalConstants.COOPERATIVITY_STRING + "_" + modifier.getSpecies() + "_r") != null) {
							infl.setCoop(production.getKineticLaw().getLocalParameter(GlobalConstants.COOPERATIVITY_STRING + "_" + modifier.getSpecies() + "_r").getValue());
						} else if (sbml.getModel().getParameter(GlobalConstants.COOPERATIVITY_STRING) != null) {
							infl.setCoop(sbml.getModel().getParameter(GlobalConstants.COOPERATIVITY_STRING).getValue());
						} else {
							infl.setCoop(Double.parseDouble(biosimrc.get("biosim.gcm.COOPERATIVITY_VALUE", "")));
						}
					}
				} 
				if (BioModel.isActivator(modifier) || BioModel.isRegulator(modifier)) {
					for (int j = 0; j < production.getProductCount(); j++) {
						SpeciesReference product = production.getProduct(j);
						Influence infl = new Influence();		
						infl.generateName();
						infl.setType("vee");
						infl.setInput(modifier.getSpecies());
						if (BioModel.isMRNASpecies(sbml,sbml.getModel().getSpecies(product.getSpecies()))) {
							infl.setOutput("none");
						} else {
							infl.setOutput(product.getSpecies());
//							p.addOutput(product.getSpecies(),speciesList.get(product.getSpecies()));
						}
						p.addToReactionMap(modifier.getSpecies(), infl);
						p.addActivator(modifier.getSpecies(), speciesList.get(modifier.getSpecies()));
						speciesList.get(modifier.getSpecies()).setActivator(true);
						if (production.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_KACT_STRING.replace("_","_"+modifier.getSpecies()+"_"))!=null &&
								production.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_KACT_STRING.replace("_","_"+modifier.getSpecies()+"_"))!=null) {
							infl.setAct(production.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_KACT_STRING.replace("_","_"+modifier.getSpecies()+"_")).getValue(),
									production.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_KACT_STRING.replace("_","_"+modifier.getSpecies()+"_")).getValue());
						} else if ((sbml.getModel().getParameter(GlobalConstants.FORWARD_KACT_STRING) != null)  && 
								(sbml.getModel().getParameter(GlobalConstants.REVERSE_KACT_STRING) != null))   {
							infl.setAct(sbml.getModel().getParameter(GlobalConstants.FORWARD_KACT_STRING).getValue(),
									sbml.getModel().getParameter(GlobalConstants.REVERSE_KACT_STRING).getValue());
						} else {
							infl.setAct(Double.parseDouble(biosimrc.get("biosim.gcm.KACT_VALUE", "")),1.0);
						}
						if (production.getKineticLaw().getLocalParameter(GlobalConstants.COOPERATIVITY_STRING + "_" + modifier.getSpecies() + "_a") != null) {
							infl.setCoop(production.getKineticLaw().getLocalParameter(GlobalConstants.COOPERATIVITY_STRING + "_" + modifier.getSpecies() + "_a").getValue());
						} else if (sbml.getModel().getParameter(GlobalConstants.COOPERATIVITY_STRING) != null) {
							infl.setCoop(sbml.getModel().getParameter(GlobalConstants.COOPERATIVITY_STRING).getValue());
						} else {
							infl.setCoop(Double.parseDouble(biosimrc.get("biosim.gcm.COOPERATIVITY_VALUE", "")));
						}
					}
				} 
			}
			sbml.getModel().removeReaction(production.getId());
		}
	}

	/**
	 * Parses the data and put it into the species
	 * 
	 * @param name
	 *            the name of the species
	 * @param properties
	 *            the properties of the species
	 */
	private void parseSpeciesData(SBMLDocument sbml,Species species) {
		Preferences biosimrc = Preferences.userRoot();
		
		SpeciesInterface speciesIF = null;

		String component = "";
		String speciesId = species.getId();
		if (species.getId().contains("__")) {
			component = species.getId().substring(0,species.getId().lastIndexOf("__")+2);
			speciesId = speciesId.substring(speciesId.lastIndexOf("__")+2);
		}
		Reaction degradation = BioModel.getDegradationReaction(species.getId(), sbml.getModel());
		Reaction diffusion = BioModel.getDiffusionReaction(species.getId(), sbml.getModel());
		// TODO: Need to change to getConstitutive reaction
		Reaction constitutive = sbml.getModel().getReaction(component+"Constitutive_"+speciesId);
		Reaction complex = BioModel.getComplexReaction(species.getId(), sbml.getModel());
		
		/*if (property.getProperty(GlobalConstants.TYPE).contains(GlobalConstants.CONSTANT)) {
			speciesIF = new ConstantSpecies();
		} 
		else*/ 
		if (diffusion != null && constitutive != null) {
			speciesIF = new DiffusibleConstitutiveSpecies();
			speciesIF.setDiffusible(true);
		} 
		else if (diffusion != null) {
			speciesIF = new DiffusibleSpecies();
			speciesIF.setDiffusible(true);
		} 
		else if (constitutive != null) {
			speciesIF = new SpasticSpecies();
			speciesIF.setDiffusible(false);
		}
		else {
			speciesIF = new BaseSpecies();
			speciesIF.setDiffusible(false);
		}
		speciesList.put(species.getId(), speciesIF);
		if (species.isSetInitialAmount()) {
			speciesIF.setInitialAmount(species.getInitialAmount());
		} else if (species.isSetInitialConcentration()) {
			speciesIF.setInitialConcentration(species.getInitialConcentration());
		}  
		
		if (constitutive != null) {
			if (constitutive.getKineticLaw().getLocalParameter(GlobalConstants.OCR_STRING)!=null) {
				speciesIF.setKo(constitutive.getKineticLaw().getLocalParameter(GlobalConstants.OCR_STRING).getValue());
			} else if (sbml.getModel().getParameter(GlobalConstants.OCR_STRING)!=null){
				speciesIF.setKo(sbml.getModel().getParameter(GlobalConstants.OCR_STRING).getValue());
			} else {
				speciesIF.setKo(Double.parseDouble(biosimrc.get("biosim.gcm.OCR_VALUE", "")));
			}
			if (constitutive.getKineticLaw().getLocalParameter(GlobalConstants.STOICHIOMETRY_STRING)!=null) {
				speciesIF.setnp(constitutive.getKineticLaw().getLocalParameter(GlobalConstants.STOICHIOMETRY_STRING).getValue());
			} else if (sbml.getModel().getParameter(GlobalConstants.STOICHIOMETRY_STRING)!=null) {
				speciesIF.setnp(sbml.getModel().getParameter(GlobalConstants.STOICHIOMETRY_STRING).getValue());
			} else {
				speciesIF.setnp(Double.parseDouble(biosimrc.get("biosim.gcm.STOICHIOMETRY_VALUE", "")));
			}
			sbml.getModel().removeReaction(constitutive.getId());
		} else {
			speciesIF.setKo(0.0);
			speciesIF.setnp(0.0);
		}
		
		if (degradation != null) {
			if (degradation.getKineticLaw().getLocalParameter(GlobalConstants.KDECAY_STRING)!=null) {
				speciesIF.setDecay(degradation.getKineticLaw().getLocalParameter(GlobalConstants.KDECAY_STRING).getValue());
			} else if (sbml.getModel().getParameter(GlobalConstants.KDECAY_STRING)!=null){
				speciesIF.setDecay(sbml.getModel().getParameter(GlobalConstants.KDECAY_STRING).getValue());
			} else {
				speciesIF.setDecay(Double.parseDouble(biosimrc.get("biosim.gcm.KDECAY_VALUE", "")));
			}
			
			if (!BioModel.isGridReaction(degradation)) {
				sbml.getModel().removeReaction(degradation.getId());
			}
		} else {
			speciesIF.setDecay(0.0);
		}
		
		if (complex != null) {
			if (complex.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_KCOMPLEX_STRING)!=null &&
					complex.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_KCOMPLEX_STRING)!=null) {
				speciesIF.setKc(complex.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_KCOMPLEX_STRING).getValue(),
						complex.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_KCOMPLEX_STRING).getValue());
			} else if (sbml.getModel().getParameter(GlobalConstants.FORWARD_KCOMPLEX_STRING)!=null &&
						sbml.getModel().getParameter(GlobalConstants.REVERSE_KCOMPLEX_STRING)!=null){
				speciesIF.setKc(sbml.getModel().getParameter(GlobalConstants.FORWARD_KCOMPLEX_STRING).getValue(),
						sbml.getModel().getParameter(GlobalConstants.REVERSE_KCOMPLEX_STRING).getValue());
			} else {
				speciesIF.setKc(Double.parseDouble(biosimrc.get("biosim.gcm.KCOMPLEX_VALUE", "")),1.0);
			}
		} else {
			speciesIF.setKc(0.0,1.0);
		}
		
		if (diffusion != null) {
			if (diffusion.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_MEMDIFF_STRING)!=null &&
				diffusion.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_MEMDIFF_STRING)!=null) {
				speciesIF.setKmdiff(diffusion.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_MEMDIFF_STRING).getValue(),
						diffusion.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_MEMDIFF_STRING).getValue());
			} else if (sbml.getModel().getParameter(GlobalConstants.FORWARD_MEMDIFF_STRING)!=null &&
					sbml.getModel().getParameter(GlobalConstants.REVERSE_MEMDIFF_STRING)!=null) {
				speciesIF.setKmdiff(sbml.getModel().getParameter(GlobalConstants.FORWARD_MEMDIFF_STRING).getValue(),
						sbml.getModel().getParameter(GlobalConstants.REVERSE_MEMDIFF_STRING).getValue());
			} else {
				speciesIF.setKmdiff(Double.parseDouble(biosimrc.get("biosim.gcm.FORWARD_MEMDIFF_VALUE", "")),
						Double.parseDouble(biosimrc.get("biosim.gcm.REVERSE_MEMDIFF_VALUE", "")));
			}
			//sbml.getModel().removeReaction(diffusion.getId());
		} else {
			speciesIF.setKmdiff(0.0,1.0);
		}
		/*
		if (property.containsKey(GlobalConstants.KASSOCIATION_STRING)) {
			specie.addProperty(GlobalConstants.KASSOCIATION_STRING, property.getProperty(GlobalConstants.KASSOCIATION_STRING));
		} else {
			specie.addProperty(GlobalConstants.KASSOCIATION_STRING, gcm.getParameter(GlobalConstants.KASSOCIATION_STRING));
		}
		
		if (property.containsKey(GlobalConstants.KECDIFF_STRING)) {
			specie.addProperty(GlobalConstants.KECDIFF_STRING, property.getProperty(GlobalConstants.KECDIFF_STRING));
		} else {
			specie.addProperty(GlobalConstants.KECDIFF_STRING, gcm.getParameter(GlobalConstants.KECDIFF_STRING));
		}
		
		if (property.containsKey(GlobalConstants.KECDECAY_STRING)) {
			specie.addProperty(GlobalConstants.KECDECAY_STRING, property.getProperty(GlobalConstants.KECDECAY_STRING));
		} else {
			specie.addProperty(GlobalConstants.KECDECAY_STRING, gcm.getParameter(GlobalConstants.KECDECAY_STRING));
		}
		*/
		
		speciesIF.setId(species.getId());
		speciesIF.setName(species.getName());
		speciesIF.setStateName(species.getId());
		
		if (complex != null) {
			for (int i = 0; i < complex.getReactantCount(); i++) {
				Influence infl = new Influence();		
				infl.generateName();		
				infl.setType("plus");
				infl.setCoop(complex.getReactant(i).getStoichiometry());
				String input = complex.getReactant(i).getSpecies();
				String output = complex.getProduct(0).getSpecies();
				infl.setInput(input);
				infl.setOutput(output);
				//Maps complex species to complex formation influences of which they're outputs
				ArrayList<Influence> complexInfluences = null;
				if (complexMap.containsKey(output)) {
					complexInfluences = complexMap.get(output);
				} else { 
					complexInfluences = new ArrayList<Influence>();
					complexMap.put(output, complexInfluences);
				}
				complexInfluences.add(infl);
				//Maps part species to complex formation influences of which they're inputs
				complexInfluences = null;
				if (partsMap.containsKey(input)) {
					complexInfluences = partsMap.get(input);
				} else { 
					complexInfluences = new ArrayList<Influence>();
					partsMap.put(input, complexInfluences);
				}
				complexInfluences.add(infl);
			} 
			sbml.getModel().removeReaction(complex.getId());
		}
	}
	
	// Holds the text of the GCM
	private StringBuffer data = null;

	private HashMap<String, SpeciesInterface> speciesList;
	private HashMap<String, Promoter> promoterList;
	private HashMap<String, ArrayList<Influence>> complexMap;
	private HashMap<String, ArrayList<Influence>> partsMap;

	private BioModel biomodel = null;

	// A regex that matches information
	//private static final String STATE = "(^|\\n) *([^- \\n]*) *\\[(.*)\\]";

	//private static final String REACTION = "(^|\\n) *([^ \\n]*) *\\-\\> *([^ \n]*) *\\[(.*)arrowhead=([^,\\]]*)(.*)";

	//private static final String PROPERTY_NUMBER = "([a-zA-Z]+)=\"([\\d]*[\\.\\d]?\\d+)\"";

	// private static final String PROPERTY_STATE = "([a-zA-Z]+)=([^\\s,.\"]+)";

	// private static final String PROPERTY_QUOTE =
	// "([a-zA-Z]+)=\"([^\\s,.\"]+)\"";

	//private static final String PROPERTY_STATE = "([a-zA-Z\\s\\-]+)=([^\\s,]+)";

	// Debug level
	//private boolean debug = false;
}
