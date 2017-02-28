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
package conversion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.comp.CompSBasePlugin;
import org.sbml.jsbml.ext.comp.Port;
import org.sbml.jsbml.ext.comp.ReplacedBy;
import org.sbml.jsbml.ext.comp.ReplacedElement;
import org.sbml.jsbml.ext.comp.Submodel;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.MapsTo;
import org.sbolstandard.core2.Module;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.Participation;
import org.sbolstandard.core2.RefinementType;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.Identified;
import org.sbolstandard.core2.SequenceOntology;
import org.sbolstandard.core2.SystemsBiologyOntology;

import dataModels.biomodel.annotation.AnnotationUtility;
import dataModels.biomodel.annotation.SBOLAnnotation;
import dataModels.biomodel.parser.BioModel;
import dataModels.biomodel.util.SBMLutilities;
import dataModels.biomodel.util.Utility;
import dataModels.util.GlobalConstants;


/**
 * Perform conversion from SBOL to SBML. 
 * 
 * @author Nicholas Roehner 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SBOL2SBML {

	public SBOL2SBML() {
	}

	/**
	 * Returns the specified SBOL object's displayId. This means the prefix of the SBOL element's displayId will be cut off.
	 * @param sbolElement - The SBOL element to retrieve its displayId
	 * @return The displayId of the given SBOL element.
	 */
	private static String getDisplayID(Identified sbolElement) { 
		if (sbolElement.isSetDisplayId()) {
			return sbolElement.getDisplayId();
		}
		String identity = sbolElement.getIdentity().toString();
		return identity.substring(identity.lastIndexOf("/") + 1);
	}


	/**
	 * Perform conversion from SBOL to SBML. 
	 * All SBOL Interactions are converted to SBML degradation, complex formation, and production reactions. Depending
	 * on what SBML reactions are created, SBO terms are assigned to each reaction's role to preserve SBOL participant
	 * and interaction type. 
	 * All SBOL FunctionalComponents found in the given ModuleDefinition will be converted to SBML species.
	 * All SBOL Modules are converted to SBML submodels that are encased within iBioSim's BioModel objects.
	 * 
	 * @param projectDirectory - The location to generate the SBML model
	 * @param moduleDef - the current ModuleDefinition to convert all SBML object within the ModuleDefinition to its equivalent SBML component.
	 * @param sbolDoc - The SBOL document to be converted to its equivalent SBML model.
	 * @return The list of SBML models converted from SBOL ModuleDefinition. Each of the converted SBML model are stored within iBioSim's BioModel object.
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public static List<BioModel> generateModel(String projectDirectory, ModuleDefinition moduleDef, SBOLDocument sbolDoc) throws XMLStreamException, IOException {

		List<BioModel> models = new LinkedList<BioModel>();

		BioModel targetModel = new BioModel(projectDirectory);
		targetModel.createSBMLDocument(getDisplayID(moduleDef), false, false);

		// Annotate SBML model with SBOL module definition
		Model sbmlModel = targetModel.getSBMLDocument().getModel();
		SBOLAnnotation modelAnno = new SBOLAnnotation(sbmlModel.getMetaId(), 
				moduleDef.getClass().getSimpleName(), moduleDef.getIdentity()); 
		AnnotationUtility.setSBOLAnnotation(sbmlModel, modelAnno);

		for (FunctionalComponent comp : moduleDef.getFunctionalComponents()) { 
			if (isSpeciesComponent(comp, sbolDoc)) {
				generateSpecies(comp, sbolDoc, targetModel);
				if (isInputComponent(comp)) {
					generateInputPort(comp, targetModel);
				} else if (isOutputComponent(comp)){
					generateOutputPort(comp, targetModel);
				}
			} else if (isPromoterComponent(comp, sbolDoc)) {
				generatePromoterSpecies(comp, sbolDoc, targetModel);
				if (isInputComponent(comp)) {
					generateInputPort(comp, targetModel);
				} else if (isOutputComponent(comp)){
					generateOutputPort(comp, targetModel);
				}
			} else {
				//System.out.println("Dropping "+comp.getIdentity());
			}
		}

		HashMap<FunctionalComponent, List<Interaction>> promoterToProductions = new HashMap<FunctionalComponent, List<Interaction>>();
		HashMap<FunctionalComponent, List<Interaction>> promoterToActivations = new HashMap<FunctionalComponent, List<Interaction>>();
		HashMap<FunctionalComponent, List<Interaction>> promoterToRepressions = new HashMap<FunctionalComponent, List<Interaction>>();
		HashMap<FunctionalComponent, List<Participation>> promoterToProducts = new HashMap<FunctionalComponent, List<Participation>>();
		HashMap<FunctionalComponent, List<Participation>> promoterToTranscribed = new HashMap<FunctionalComponent, List<Participation>>();
		HashMap<FunctionalComponent, List<Participation>> promoterToActivators = new HashMap<FunctionalComponent, List<Participation>>();
		HashMap<FunctionalComponent, List<Participation>> promoterToRepressors = new HashMap<FunctionalComponent, List<Participation>>();
		HashMap<FunctionalComponent, List<Participation>> promoterToPartici = new HashMap<FunctionalComponent, List<Participation>>();
		for (Interaction interact : moduleDef.getInteractions()) {
			if (isDegradationInteraction(interact, moduleDef, sbolDoc)) {
				generateDegradationRxn(interact, moduleDef, targetModel);
			} else if (isComplexFormationInteraction(interact, moduleDef, sbolDoc)) {
				Participation complex = null;
				List<Participation> ligands = new LinkedList<Participation>();
				for (Participation partici: interact.getParticipations()) {
					// COMPLEX
					if (partici.containsRole(SystemsBiologyOntology.PRODUCT) ||
							partici.containsRole(URI.create("http://identifiers.org/biomodels.sbo/SBO:0000253"))) {
						complex = partici;
					} else if (partici.containsRole(SystemsBiologyOntology.REACTANT) ||
							partici.containsRole(URI.create("http://identifiers.org/biomodels.sbo/SBO:0000280"))) {
						ligands.add(partici);
					}
				}
				generateComplexFormationRxn(interact, complex, ligands, moduleDef, targetModel);
			} else if (isProductionInteraction(interact, moduleDef, sbolDoc)) {
				FunctionalComponent promoter = null;
				for (Participation partici : interact.getParticipations())
					if (partici.containsRole(SystemsBiologyOntology.PROMOTER)||
							partici.containsRole(SystemsBiologyOntology.TEMPLATE)) {
						promoter = moduleDef.getFunctionalComponent(partici.getParticipantURI());
						if (!promoterToPartici.containsKey(promoter))
							promoterToPartici.put(promoter, new LinkedList<Participation>());
						promoterToPartici.get(promoter).add(partici);
						if (!promoterToProductions.containsKey(promoter))
							promoterToProductions.put(promoter, new LinkedList<Interaction>());
						promoterToProductions.get(promoter).add(interact);
					} 
				for (Participation partici : interact.getParticipations())
					if (partici.containsRole(SystemsBiologyOntology.PRODUCT)) {
						if (!promoterToProducts.containsKey(promoter))
							promoterToProducts.put(promoter, new LinkedList<Participation>());
						promoterToProducts.get(promoter).add(partici);
						// TRANSCRIBED
					} else if (partici.containsRole(SystemsBiologyOntology.PROMOTER)||
							partici.containsRole(SystemsBiologyOntology.TEMPLATE)) {
						if (!promoterToTranscribed.containsKey(promoter))
							promoterToTranscribed.put(promoter, new LinkedList<Participation>());
						promoterToTranscribed.get(promoter).add(partici);
					}
				if (!promoterToActivators.containsKey(promoter))
					promoterToActivators.put(promoter, new LinkedList<Participation>());
				if (!promoterToRepressors.containsKey(promoter))
					promoterToRepressors.put(promoter, new LinkedList<Participation>());
				if (!promoterToActivations.containsKey(promoter))
					promoterToActivations.put(promoter, new LinkedList<Interaction>());
				if (!promoterToRepressions.containsKey(promoter))
					promoterToRepressions.put(promoter, new LinkedList<Interaction>());
			} else if (isActivationInteraction(interact, moduleDef, sbolDoc)) {
				FunctionalComponent promoter = null;
				for (Participation partici : interact.getParticipations())
					if (partici.containsRole(SystemsBiologyOntology.PROMOTER)||
							partici.containsRole(SystemsBiologyOntology.STIMULATED)) {
						promoter = moduleDef.getFunctionalComponent(partici.getParticipantURI());
						if (!promoterToPartici.containsKey(promoter))
							promoterToPartici.put(promoter, new LinkedList<Participation>());
						promoterToPartici.get(promoter).add(partici);
						if (!promoterToActivators.containsKey(promoter))
							promoterToActivators.put(promoter, new LinkedList<Participation>());
					} 
				for (Participation partici : interact.getParticipations())
					if (partici.containsRole(SystemsBiologyOntology.STIMULATOR))
						promoterToActivators.get(promoter).add(partici);
				if (!promoterToActivations.containsKey(promoter))
					promoterToActivations.put(promoter, new LinkedList<Interaction>());
				promoterToActivations.get(promoter).add(interact);
			} else if (isRepressionInteraction(interact, moduleDef, sbolDoc)) {
				FunctionalComponent promoter = null;
				for (Participation partici : interact.getParticipations())
					if (partici.containsRole(SystemsBiologyOntology.PROMOTER)||
							partici.containsRole(SystemsBiologyOntology.INHIBITED)) {
						promoter = moduleDef.getFunctionalComponent(partici.getParticipantURI());
						if (!promoterToPartici.containsKey(promoter))
							promoterToPartici.put(promoter, new LinkedList<Participation>());
						promoterToPartici.get(promoter).add(partici);
						if (!promoterToRepressors.containsKey(promoter))
							promoterToRepressors.put(promoter, new LinkedList<Participation>());
					} 
				for (Participation partici : interact.getParticipations())
					if (partici.containsRole(SystemsBiologyOntology.INHIBITOR))
						promoterToRepressors.get(promoter).add(partici);
				if (!promoterToRepressions.containsKey(promoter))
					promoterToRepressions.put(promoter, new LinkedList<Interaction>());
				promoterToRepressions.get(promoter).add(interact);
			} else {
				generateBiochemicalRxn(interact, moduleDef, targetModel);
			}
		}

		for (FunctionalComponent promoter : moduleDef.getFunctionalComponents()) { 
			if (isPromoterComponent(promoter, sbolDoc)) {
				if (!promoterToActivators.containsKey(promoter))
					promoterToActivators.put(promoter, new LinkedList<Participation>());
				if (!promoterToRepressors.containsKey(promoter))
					promoterToRepressors.put(promoter, new LinkedList<Participation>());
				if (!promoterToActivations.containsKey(promoter))
					promoterToActivations.put(promoter, new LinkedList<Interaction>());
				if (!promoterToRepressions.containsKey(promoter))
					promoterToRepressions.put(promoter, new LinkedList<Interaction>());
				if (!promoterToProducts.containsKey(promoter))
					promoterToProducts.put(promoter, new LinkedList<Participation>());
				if (!promoterToTranscribed.containsKey(promoter))
					promoterToTranscribed.put(promoter, new LinkedList<Participation>());
				if (!promoterToPartici.containsKey(promoter))
					promoterToPartici.put(promoter, new LinkedList<Participation>());
				generateProductionRxn(promoter, promoterToPartici.get(promoter), promoterToProductions.get(promoter), 
						promoterToActivations.get(promoter), promoterToRepressions.get(promoter), promoterToProducts.get(promoter),
						promoterToTranscribed.get(promoter), promoterToActivators.get(promoter),
						promoterToRepressors.get(promoter), moduleDef, sbolDoc, targetModel);
			}
		}

		//		for (FunctionalComponent promoter : promoterToProductions.keySet()) {
		//			generateProductionRxn(promoter, promoterToPartici.get(promoter), promoterToProductions.get(promoter), 
		//					promoterToActivations.get(promoter), promoterToRepressions.get(promoter), promoterToProducts.get(promoter),
		//					promoterToTranscribed.get(promoter), promoterToActivators.get(promoter),
		//					promoterToRepressors.get(promoter), moduleDef, sbolDoc, targetModel);
		//		}

		for (Module subModule : moduleDef.getModules()) {
			ModuleDefinition subModuleDef = sbolDoc.getModuleDefinition(subModule.getDefinitionURI());
			BioModel subTargetModel = new BioModel(projectDirectory);
			if (subTargetModel.load(projectDirectory + File.separator + getDisplayID(subModuleDef) + ".xml")) {
				generateSubModel(projectDirectory, subModule, moduleDef, sbolDoc, subTargetModel, targetModel);
			} else {
				List<BioModel> subModels = generateSubModel(projectDirectory, subModule, moduleDef, sbolDoc, targetModel);
				models.addAll(subModels);
			}
		}
		models.add(targetModel);
		return models;
	}

	private static void generateSubModel(String projectDirectory, Module subModule, ModuleDefinition moduleDef, SBOLDocument sbolDoc, 
			BioModel subTargetModel, BioModel targetModel) {
		ModuleDefinition subModuleDef = sbolDoc.getModuleDefinition(subModule.getDefinitionURI());
		String md5 = Utility.MD5(subTargetModel.getSBMLDocument());
		targetModel.addComponent(getDisplayID(subModule), getDisplayID(subModuleDef) + ".xml", 
				subTargetModel.IsWithinCompartment(), subTargetModel.getCompartmentPorts(), 
				-1, -1, 0, 0, md5);
		annotateSubModel(targetModel.getSBMLCompModel().getSubmodel(getDisplayID(subModule)), subModule);
		for (MapsTo mapping : subModule.getMapsTos()) 
			if (isIOMapping(mapping, subModule, sbolDoc)) {
				RefinementType refinement = mapping.getRefinement();
				if (refinement == RefinementType.VERIFYIDENTICAL || refinement == RefinementType.MERGE
						|| refinement == RefinementType.USELOCAL) {
					generateReplacement(mapping, subModule, moduleDef, sbolDoc, subTargetModel, targetModel);
				} else if (refinement == RefinementType.USEREMOTE) {
					generateReplacedBy(mapping, subModule, moduleDef, sbolDoc, subTargetModel, targetModel);
				}
			}
	}

	private static List<BioModel> generateSubModel(String projectDirectory, Module subModule, ModuleDefinition moduleDef, SBOLDocument sbolDoc, 
			BioModel targetModel) throws XMLStreamException, IOException {
		ModuleDefinition subModuleDef = sbolDoc.getModuleDefinition(subModule.getDefinitionURI());
		List<BioModel> subModels = generateModel(projectDirectory, subModuleDef, sbolDoc);
		BioModel subTargetModel = subModels.get(subModels.size()-1);
		generateSubModel(projectDirectory, subModule, moduleDef, sbolDoc, subTargetModel, targetModel);
		return subModels;
	}

	private static void generateReplacement(MapsTo mapping, Module subModule, ModuleDefinition moduleDef, 
			SBOLDocument sbolDoc, BioModel subTargetModel, BioModel targetModel) {
		ModuleDefinition subModuleDef = sbolDoc.getModuleDefinition(subModule.getDefinitionURI()); 
		FunctionalComponent remoteSpecies = subModuleDef.getFunctionalComponent(mapping.getRemoteURI());
		FunctionalComponent localSpecies = moduleDef.getFunctionalComponent(mapping.getLocalURI());

		//System.out.println(mapping.getRemoteURI()+" <-> " + mapping.getLocalURI());
		Species localSBMLSpecies = targetModel.getSBMLDocument().getModel().getSpecies(getDisplayID(localSpecies));
		Port port = subTargetModel.getPortByIdRef(getDisplayID(remoteSpecies));
		if (port==null) {
			System.out.println("Cannot find "+getDisplayID(remoteSpecies));
			//return;
		}

		Submodel subModel = targetModel.getSBMLCompModel().getSubmodel(getDisplayID(subModule));
		SBMLutilities.addReplacement(localSBMLSpecies, subModel, getDisplayID(subModule), port.getId(), "(none)", 
				new String[]{""}, new String[]{""}, new String[]{""}, new String[]{""}, false);

		// Annotate SBML replacment with SBOL maps-to
		CompSBasePlugin compSBML = SBMLutilities.getCompSBasePlugin(localSBMLSpecies);
		SBMLutilities.setDefaultMetaID(targetModel.getSBMLDocument(), compSBML.getReplacedElement(compSBML.getNumReplacedElements() - 1), 1);
		annotateReplacement(compSBML.getReplacedElement(compSBML.getNumReplacedElements() - 1), mapping);
	}

	private static void generateReplacedBy(MapsTo mapping, Module subModule, ModuleDefinition moduleDef, 
			SBOLDocument sbolDoc, BioModel subTargetModel, BioModel targetModel) {
		ModuleDefinition subModuleDef = sbolDoc.getModuleDefinition(subModule.getDefinitionURI());
		FunctionalComponent remoteSpecies = subModuleDef.getFunctionalComponent(mapping.getRemoteURI());
		FunctionalComponent localSpecies = moduleDef.getFunctionalComponent(mapping.getLocalURI());

		Species localSBMLSpecies = targetModel.getSBMLDocument().getModel().getSpecies(getDisplayID(localSpecies));
		Port port = subTargetModel.getPortByIdRef(getDisplayID(remoteSpecies));
		SBMLutilities.addReplacedBy(localSBMLSpecies, getDisplayID(subModule), port.getId(), new String[]{""}, 
				new String[]{""}, new String[]{""}, new String[]{""});

		// Annotate SBML replaced-by with SBOL maps-to
		CompSBasePlugin compSBML = SBMLutilities.getCompSBasePlugin(localSBMLSpecies);
		SBMLutilities.setDefaultMetaID(targetModel.getSBMLDocument(), compSBML.getReplacedBy(), 1);
		annotateReplacedBy(compSBML.getReplacedBy(), mapping);
	}

	private static void generateInputPort(FunctionalComponent species, BioModel targetModel) {
		targetModel.createDirPort(getDisplayID(species), GlobalConstants.INPUT);
	}

	private static void generateOutputPort(FunctionalComponent species, BioModel targetModel) {
		targetModel.createDirPort(getDisplayID(species), GlobalConstants.OUTPUT);
	}

	private static void generateSpecies(FunctionalComponent species, SBOLDocument sbolDoc, BioModel targetModel) {
		targetModel.createSpecies(getDisplayID(species), -1, -1);
		Species sbmlSpecies = targetModel.getSBMLDocument().getModel().getSpecies(getDisplayID(species));
		sbmlSpecies.setBoundaryCondition(species.getDirection().equals(DirectionType.IN));
		if (isDNAComponent(species,sbolDoc)) {
			sbmlSpecies.setSBOTerm(GlobalConstants.SBO_DNA_SEGMENT);
		} else if (isRNAComponent(species,sbolDoc)) {
			sbmlSpecies.setSBOTerm(GlobalConstants.SBO_RNA_SEGMENT);
		} else if (isProteinComponent(species,sbolDoc)) {
			sbmlSpecies.setSBOTerm(GlobalConstants.SBO_PROTEIN);
		} else if (isComplexComponent(species,sbolDoc)) {
			sbmlSpecies.setSBOTerm(GlobalConstants.SBO_NONCOVALENT_COMPLEX);
		} else if (isSmallMoleculeComponent(species,sbolDoc)) {
			sbmlSpecies.setSBOTerm(GlobalConstants.SBO_SIMPLE_CHEMICAL);
		}
		// Annotate SBML species with SBOL component and component definition
		annotateSpecies(sbmlSpecies, species, sbolDoc);	
	}

	private static void generatePromoterSpecies(FunctionalComponent promoter, SBOLDocument sbolDoc, BioModel targetModel) {
		targetModel.createPromoter(getDisplayID(promoter), -1, -1, true, false, null);
		Species sbmlPromoter = targetModel.getSBMLDocument().getModel().getSpecies(getDisplayID(promoter));

		// Annotate SBML promoter species with SBOL component and component definition
		ComponentDefinition compDef = sbolDoc.getComponentDefinition(promoter.getDefinitionURI());
		if (compDef!=null) {
			annotateSpecies(sbmlPromoter, promoter, compDef, sbolDoc);
		}
	}

	private static void generateBiochemicalRxn(Interaction interaction, ModuleDefinition moduleDef, BioModel targetModel) {
		SystemsBiologyOntology sbo = new SystemsBiologyOntology();
		String SBOid = "";
		for (URI type : interaction.getTypes()) {
			if (sbo.getId(type)!=null) {
				SBOid = sbo.getId(type);
			}
		}
		HashMap<String,String> reactants = new HashMap<String,String>();
		HashMap<String,String> modifiers = new HashMap<String,String>();
		HashMap<String,String> products = new HashMap<String,String>();
		Set<URI> reactantSBO = sbo.getDescendantURIsOf(SystemsBiologyOntology.REACTANT);
		reactantSBO.add(SystemsBiologyOntology.REACTANT);
		Set<URI> modifierSBO = sbo.getDescendantURIsOf(SystemsBiologyOntology.MODIFIER);
		modifierSBO.add(SystemsBiologyOntology.MODIFIER);
		modifierSBO.add(SystemsBiologyOntology.FUNCTIONAL_COMPARTMENT);
		modifierSBO.add(SystemsBiologyOntology.NEUTRAL_PARTICIPANT);
		modifierSBO.add(SystemsBiologyOntology.PROMOTER);
		Set<URI> productSBO = sbo.getDescendantURIsOf(SystemsBiologyOntology.PRODUCT);
		productSBO.add(SystemsBiologyOntology.PRODUCT);

		for (Participation participation : interaction.getParticipations()) {
			for (URI role : participation.getRoles()) {
				String id = getDisplayID(moduleDef.getFunctionalComponent(participation.getParticipantURI())); 
				if (reactantSBO.contains(role)) {
					reactants.put(id,sbo.getId(role));
					break;
				} else if (modifierSBO.contains(role)) {
					modifiers.put(id,sbo.getId(role));
					break;
				} else if (productSBO.contains(role)) {
					products.put(id,sbo.getId(role));
					break;
				}
			}
		}
		targetModel.createBiochemicalReaction(interaction.getDisplayId(),SBOid,reactants,modifiers,products);
	}

	private static void generateDegradationRxn(Interaction degradation, ModuleDefinition moduleDef, BioModel targetModel) {
		//		Participation degraded = degradation.getParticipations().get(0); //OLD VERSION
		Participation degraded = null;
		for(Participation part : degradation.getParticipations())
		{
			degraded = part;
			break;
		}
		FunctionalComponent species = moduleDef.getFunctionalComponent(degraded.getParticipantURI());
		boolean onPort = (species.getDirection().equals(DirectionType.IN) 
				|| species.getDirection().equals(DirectionType.OUT));
		Reaction degradationRxn = targetModel.createDegradationReaction(getDisplayID(species), -1, null, onPort, null);
		degradationRxn.setId(getDisplayID(degradation));

		// Annotate SBML degradation reaction with SBOL interaction
		annotateRxn(degradationRxn, degradation);

		// Annotate SBML degraded reactant with SBOL participation
		SBMLutilities.setDefaultMetaID(targetModel.getSBMLDocument(), degradationRxn.getReactant(0), 1);
		annotateSpeciesReference(degradationRxn.getReactant(0), degraded);
	}

	private static void generateComplexFormationRxn(Interaction complexFormation, Participation complex,
			List<Participation> ligands, ModuleDefinition moduleDef, BioModel targetModel) {
		FunctionalComponent complexSpecies = moduleDef.getFunctionalComponent(complex.getParticipantURI());
		boolean onPort = (complexSpecies.getDirection().equals(DirectionType.IN) 
				|| complexSpecies.getDirection().equals(DirectionType.OUT));
		Reaction complexFormationRxn = targetModel.createComplexReaction(getDisplayID(complexSpecies), null, onPort);
		complexFormationRxn.setId(getDisplayID(complexFormation));
		SBMLutilities.setDefaultMetaID(targetModel.getSBMLDocument(), complexFormationRxn, 1);

		// Annotate SBML complex formation reaction with SBOL interaction
		annotateRxn(complexFormationRxn, complexFormation);

		// Annotate SBML complex product with SBOL participation
		SimpleSpeciesReference complexRef = complexFormationRxn.getProductForSpecies(getDisplayID(complexSpecies));
		SBMLutilities.setDefaultMetaID(targetModel.getSBMLDocument(), complexRef, 1);
		annotateSpeciesReference(complexRef, complex);

		for (Participation ligand : ligands) {
			FunctionalComponent ligandSpecies = moduleDef.getFunctionalComponent(ligand.getParticipantURI());
			targetModel.addReactantToComplexReaction(getDisplayID(ligandSpecies), getDisplayID(complexSpecies), 
					null, null, complexFormationRxn);

			// Annotate SBML ligand reactant with SBOL participation
			SimpleSpeciesReference ligandRef = complexFormationRxn.getReactantForSpecies(getDisplayID(ligandSpecies));
			SBMLutilities.setDefaultMetaID(targetModel.getSBMLDocument(), ligandRef, 1);
			annotateSpeciesReference(ligandRef, ligand);
		}
	}

	private static void generateProductionRxn(FunctionalComponent promoter, List<Participation> partici, List<Interaction> productions,
			List<Interaction> activations, List<Interaction> repressions,
			List<Participation> products, List<Participation> transcribed, List<Participation> activators, 
			List<Participation> repressors, ModuleDefinition moduleDef, SBOLDocument sbolDoc, BioModel targetModel) {

		String rxnID = "";
		if (productions!=null) {
			for (Interaction production : productions)
				rxnID = rxnID + "_" + getDisplayID(production);
		} else {
			rxnID = promoter.getDisplayId() + "_Production";
		}
		//rxnID = rxnID.substring(1);
		Reaction productionRxn = targetModel.createProductionReaction(getDisplayID(promoter), rxnID, null, null, null, null, 
				null, null, false, null);

		// Annotate SBML production reaction with SBOL production interactions
		List<Interaction> productionsRegulations = new LinkedList<Interaction>();
		if (productions!=null) productionsRegulations.addAll(productions);
		productionsRegulations.addAll(activations);
		productionsRegulations.addAll(repressions);
		if (!productionsRegulations.isEmpty())
			annotateRxn(productionRxn, productionsRegulations);
		if (!partici.isEmpty()) 
			annotateSpeciesReference(productionRxn.getModifier(0), partici);

		for (Participation activator : activators)
			generateActivatorReference(activator, promoter, moduleDef, productionRxn, targetModel);

		for (Participation repressor : repressors)
			generateRepressorReference(repressor, promoter, moduleDef, productionRxn, targetModel);

		for (Participation product : products)
			generateProductReference(product, promoter, moduleDef, productionRxn, targetModel);

		for (int i = 0; i < transcribed.size(); i++) {
			FunctionalComponent gene = moduleDef.getFunctionalComponent(transcribed.get(i).getParticipantURI());
			FunctionalComponent protein = moduleDef.getFunctionalComponent(products.get(i).getParticipantURI());

			ComponentDefinition compDef = sbolDoc.getComponentDefinition(gene.getDefinitionURI());
			if (compDef!=null) {
				annotateSpecies(targetModel.getSBMLDocument().getModel().getSpecies(getDisplayID(protein)), compDef);
			}
		}
	}

	private static void generateActivatorReference(Participation activator, FunctionalComponent promoter, 
			ModuleDefinition moduleDef, Reaction productionRxn, BioModel targetModel) {
		FunctionalComponent tf = moduleDef.getFunctionalComponent(activator.getParticipantURI());

		targetModel.addActivatorToProductionReaction(getDisplayID(promoter),  
				getDisplayID(tf), "none", productionRxn, null, null, null);

		// Annotate SBML activator species reference with SBOL activator participation
		ModifierSpeciesReference activatorRef = productionRxn.getModifierForSpecies(getDisplayID(tf));
		SBMLutilities.setDefaultMetaID(targetModel.getSBMLDocument(), activatorRef, 1);
		annotateSpeciesReference(activatorRef, activator);
	}

	private static void generateRepressorReference(Participation repressor, FunctionalComponent promoter, 
			ModuleDefinition moduleDef, Reaction productionRxn, BioModel targetModel) {
		FunctionalComponent tf = moduleDef.getFunctionalComponent(repressor.getParticipantURI());
		targetModel.addRepressorToProductionReaction(getDisplayID(promoter),  
				getDisplayID(tf), "none", productionRxn, null, null, null);

		// Annotate SBML repressor species reference with SBOL repressor participation
		ModifierSpeciesReference repressorRef = productionRxn.getModifierForSpecies(getDisplayID(tf));
		SBMLutilities.setDefaultMetaID(targetModel.getSBMLDocument(), repressorRef, 1);
		annotateSpeciesReference(repressorRef, repressor);
	}

	private static void generateProductReference(Participation product, FunctionalComponent promoter, 
			ModuleDefinition moduleDef, Reaction productionRxn, BioModel targetModel) {
		FunctionalComponent protein = moduleDef.getFunctionalComponent(product.getParticipantURI());
		targetModel.addActivatorToProductionReaction(getDisplayID(promoter),  
				"none", getDisplayID(protein), productionRxn, null, null, null);

		// Annotate SBML product species reference with SBOL product participation
		SpeciesReference productRef = productionRxn.getProductForSpecies(getDisplayID(protein));
		SBMLutilities.setDefaultMetaID(targetModel.getSBMLDocument(), productRef, 1);
		annotateSpeciesReference(productRef, product);
	}

	private static void annotateSpecies(Species species, FunctionalComponent comp, ComponentDefinition compDef, 
			SBOLDocument sbolDoc) {
		SBOLAnnotation speciesAnno = new SBOLAnnotation(species.getMetaId(), compDef.getIdentity());
		speciesAnno.createSBOLElementsDescription(comp.getClass().getSimpleName(), 
				comp.getDefinitionURI()); 
		speciesAnno.createSBOLElementsDescription(compDef.getClass().getSimpleName(), 
				compDef.getIdentity());
		AnnotationUtility.setSBOLAnnotation(species, speciesAnno);	
	}

	// Annotate SBML species with SBOL component, component definition, and any existing, annotating
	// DNA components or strand sign
	private static void annotateSpecies(Species species, FunctionalComponent comp, SBOLDocument sbolDoc) {
		SBOLAnnotation speciesAnno;
		List<URI> dnaCompIdentities = new LinkedList<URI>();
		String strand = AnnotationUtility.parseSBOLAnnotation(species, dnaCompIdentities);
		if (strand != null && dnaCompIdentities.size() > 0) {
			List<URI> sbolElementIdentities = new LinkedList<URI>();
			sbolElementIdentities.add(comp.getDefinitionURI()); 
			speciesAnno = new SBOLAnnotation(species.getMetaId(), comp.getClass().getSimpleName(), 
					sbolElementIdentities, dnaCompIdentities, strand);
		} else {
			speciesAnno = new SBOLAnnotation(species.getMetaId(), comp.getClass().getSimpleName(), 
					comp.getDefinitionURI());
		}
		ComponentDefinition compDef = sbolDoc.getComponentDefinition(comp.getDefinitionURI());
		if (compDef!=null) {
			speciesAnno.createSBOLElementsDescription(compDef.getClass().getSimpleName(), 
					compDef.getIdentity());
			AnnotationUtility.setSBOLAnnotation(species, speciesAnno);	
		}
	}

	// Annotate SBML species with SBOL DNA component and any existing, annotating SBOL elements
	private static void annotateSpecies(Species species, ComponentDefinition compDef) {
		SBOLAnnotation speciesAnno = new SBOLAnnotation(species.getMetaId(), compDef.getIdentity());
		HashMap<String, List<URI>> sbolElementIdentities = new HashMap<String, List<URI>>();
		AnnotationUtility.parseSBOLAnnotation(species, sbolElementIdentities);
		for (String className : sbolElementIdentities.keySet()) {
			speciesAnno.createSBOLElementsDescription(className, sbolElementIdentities.get(className));
		}
		AnnotationUtility.setSBOLAnnotation(species, speciesAnno);	
	}

	// Annotate SBML reaction with SBOL interactions
	private static void annotateRxn(Reaction rxn, List<Interaction> interacts) {
		List<URI> interactIdentities = new LinkedList<URI>();
		for (Interaction interact : interacts)
			interactIdentities.add(interact.getIdentity());
		SBOLAnnotation rxnAnno = new SBOLAnnotation(rxn.getMetaId(), 
				interacts.get(0).getClass().getSimpleName(), interactIdentities);
		AnnotationUtility.setSBOLAnnotation(rxn, rxnAnno);
	}

	// Annotate SBML reaction with SBOL interaction
	private static void annotateRxn(Reaction rxn, Interaction interact) {
		SBOLAnnotation rxnAnno = new SBOLAnnotation(rxn.getMetaId(), 
				interact.getClass().getSimpleName(), interact.getIdentity());
		AnnotationUtility.setSBOLAnnotation(rxn, rxnAnno);
	}

	// Annotate SBML species reference with SBOL participation
	private static void annotateSpeciesReference(SimpleSpeciesReference speciesRef, Participation partici) {
		SBOLAnnotation speciesRefAnno = new SBOLAnnotation(speciesRef.getMetaId(),
				partici.getClass().getSimpleName(), partici.getParticipantURI());
		AnnotationUtility.setSBOLAnnotation(speciesRef, speciesRefAnno);
	}

	// Annotate SBML species reference with SBOL participations
	private static void annotateSpeciesReference(SimpleSpeciesReference speciesRef, List<Participation> partici) {
		List<URI> particiIdentities = new LinkedList<URI>();
		for (Participation p : partici) {
			particiIdentities.add(p.getIdentity());
		}
		SBOLAnnotation speciesRefAnno = new SBOLAnnotation(speciesRef.getMetaId(),
				partici.get(0).getClass().getSimpleName(), particiIdentities);
		AnnotationUtility.setSBOLAnnotation(speciesRef, speciesRefAnno);
	}


	private static void annotateReplacedBy(ReplacedBy replacedBy, MapsTo mapping) {
		SBOLAnnotation replacedByAnno = new SBOLAnnotation(replacedBy.getMetaId(),
				mapping.getClass().getSimpleName(), mapping.getIdentity());
		AnnotationUtility.setSBOLAnnotation(replacedBy, replacedByAnno);
	}

	private static void annotateReplacement(ReplacedElement replacement, MapsTo mapping) {
		SBOLAnnotation replacementAnno = new SBOLAnnotation(replacement.getMetaId(),
				mapping.getClass().getSimpleName(), mapping.getIdentity()); 
		AnnotationUtility.setSBOLAnnotation(replacement, replacementAnno);
	}

	private static void annotateSubModel(Submodel subModel, Module subModule) {
		SBOLAnnotation subModelAnno = new SBOLAnnotation(subModel.getMetaId(),
				subModule.getClass().getSimpleName(), subModule.getDefinitionURI()); 
		AnnotationUtility.setSBOLAnnotation(subModel, subModelAnno);
	}

	private static boolean isIOMapping(MapsTo mapping, Module subModule, SBOLDocument sbolDoc) {
		ModuleDefinition subModuleDef = sbolDoc.getModuleDefinition(subModule.getDefinitionURI());
		FunctionalComponent remoteComp = subModuleDef.getFunctionalComponent(mapping.getRemoteURI());
		return isInputComponent(remoteComp) || isOutputComponent(remoteComp);
	}

	private static boolean isTopDownMapping(MapsTo mapping) { 
		RefinementType refinement = mapping.getRefinement();
		return refinement == RefinementType.VERIFYIDENTICAL || refinement == RefinementType.MERGE
				|| refinement == RefinementType.USELOCAL;
	}

	private static boolean isInputComponent(FunctionalComponent comp) {
		return comp.getDirection().equals(DirectionType.IN);
	}

	private static boolean isOutputComponent(FunctionalComponent comp) {
		return comp.getDirection().equals(DirectionType.OUT) || comp.getDirection().equals(DirectionType.INOUT);
	}

	private static boolean isDNAComponent(FunctionalComponent comp, SBOLDocument sbolDoc) {
		ComponentDefinition compDef = sbolDoc.getComponentDefinition(comp.getDefinitionURI());
		if (compDef==null) return false;
		return isDNADefinition(compDef);
	}

	private static boolean isDNADefinition(ComponentDefinition compDef) {
		return /*compDef.containsType(ChEBI.DNA) ||*/
				compDef.containsType(ComponentDefinition.DNA);
	}

	private static boolean isProteinComponent(FunctionalComponent comp, SBOLDocument sbolDoc) {
		ComponentDefinition compDef = sbolDoc.getComponentDefinition(comp.getDefinitionURI());
		if (compDef==null) return false;
		return isProteinDefinition(compDef);
	}

	private static boolean isProteinDefinition(ComponentDefinition compDef) {
		return /*compDef.containsType(ChEBI.PROTEIN) ||*/
				compDef.containsType(ComponentDefinition.PROTEIN);
	}

	private static boolean isRNAComponent(FunctionalComponent comp, SBOLDocument sbolDoc) {
		ComponentDefinition compDef = sbolDoc.getComponentDefinition(comp.getDefinitionURI());
		if (compDef==null) return false;
		return isRNADefinition(compDef);
	}

	/**
	 * Determine if the given SBOL ComponentDefinition is a valid RNA species.
	 * 
	 * @param compDef - The SBOL ComponentDefinition to check if it is a valid RNA species.
	 * @return True if the given ComponentDefinition is a valid RNA species
	 */
	private static boolean isRNADefinition(ComponentDefinition compDef) {
		return compDef.containsType(ComponentDefinition.RNA);
	}

	/**
	 *  Determine if the given SBOL FunctionalComponent is a valid promoter.
	 *  
	 * @param comp - The SBOL FunctionalComponent to check if it is contained within the given SBOLDocument.
	 * @param sbolDoc  - The SBOL Document to check if the given FunctionalComponent exist.
	 * @return True if the given FunctionalComponent is a valid promoter. False otherwise.
	 */
	private static boolean isPromoterComponent(FunctionalComponent comp, SBOLDocument sbolDoc) {
		ComponentDefinition compDef = sbolDoc.getComponentDefinition(comp.getDefinitionURI());
		if (compDef==null) return false;
		return isPromoterDefinition(compDef);
	}

	/**
	 * Determine if the given SBOL ComponentDefinition is a valid promoter.
	 * 
	 * @param compDef - The SBOL ComponentDefinition to check if it is a valid promoter.
	 * @return True if the given ComponentDefinition is a valid promoter.
	 */
	private static boolean isPromoterDefinition(ComponentDefinition compDef) {
		// TODO: need to figure out better if the CD is a promoter, need to look carefully at its subComponents
		return isDNADefinition(compDef) /*
				&& (compDef.containsRole(SequenceOntology.PROMOTER) ||
						isGeneDefinition(compDef))*/;
	}

	/**
	 * Determine if the given SBOL FunctionalComponent is Gene species.
	 * 
	 * @param comp - The SBOL FunctionalComponent to check if it is contained within the given SBOLDocument.
	 * @param sbolDoc - The SBOL Document to check if the given FunctionalComponent exist 
	 * @return True if the given FunctionalComponent could be converted to an SBML species. False otherwise.
	 */
	private static boolean isGeneComponent(FunctionalComponent comp, SBOLDocument sbolDoc) {
		ComponentDefinition compDef = sbolDoc.getComponentDefinition(comp.getDefinitionURI());
		if (compDef==null) return false;
		return isGeneDefinition(compDef);
	}

	/**
	 * Determine if the given SBOL ComponentDefinition is Gene species.
	 * 
	 * @param compDef - The SBOL ComponentDefinition to check if it is a valid Gene species
	 * @return True if the given ComponentDefinition is a valid Gene species
	 */
	private static boolean isGeneDefinition(ComponentDefinition compDef) {
		SequenceOntology so = new SequenceOntology();
		boolean isGene = false;
		for (URI role : compDef.getRoles()) {
			if (role.equals(SequenceOntology.GENE) || so.isDescendantOf(role, SequenceOntology.GENE)) {
				isGene = true;
				break;
			}
		}
		return isDNADefinition(compDef) && isGene;
	}

	/**
	 * Determine if the given SBOL FunctionComponent could be converted to an SBML species.
	 * 
	 * @param comp - The SBOL FunctionalComponent to check if it is contained within the given SBOLDocument.
	 * @param sbolDoc - The SBOL Document to check if the given FunctionalComponent exist 
	 * @return True if the given FunctionalComponent could be converted to an SBML species. False otherwise.
	 * the FunctionalComponent is a valid SBML species type.
	 */
	private static boolean isSpeciesComponent(FunctionalComponent comp, SBOLDocument sbolDoc) {
		ComponentDefinition compDef = sbolDoc.getComponentDefinition(comp.getDefinitionURI());
		if (compDef==null) return true;
		return isSpeciesDefinition(compDef);
	}

	/**
	 * Determine if the given SBOL ComponentDefinition is a valid SBML species.
	 * 
	 * @param compDef - The SBOL ComponentDefinition to check if it is a valid SBML species
	 * @return True if the given ComponentDefinition is a valid SBML species
	 */
	private static boolean isSpeciesDefinition(ComponentDefinition compDef) {
		return isComplexDefinition(compDef)
				|| isProteinDefinition(compDef)
				|| isRNADefinition(compDef)
				//				|| isTFDefinition(compDef)
				|| isSmallMoleculeDefinition(compDef)
				|| compDef.containsType(ComponentDefinition.EFFECTOR);
	}

	private static boolean isComplexComponent(FunctionalComponent comp, SBOLDocument sbolDoc) {
		ComponentDefinition compDef = sbolDoc.getComponentDefinition(comp.getDefinitionURI());
		if (compDef==null) return false;
		return isComplexDefinition(compDef);
	}

	/**
	 * Determine if the given SBOL FunctionalComponent is Complex SBML species.
	 * 
	 * @param comp - The FunctionalComponent to determine if it is a Complex SBML species.
	 * @param sbolDoc - The SBOL FunctionalComponent to check if it is a valid SBML species
	 * @return True if the given FunctionalComponent is Complex SBML species. False otherwise. 
	 */
	private static boolean isSmallMoleculeComponent(FunctionalComponent comp, SBOLDocument sbolDoc) {
		ComponentDefinition compDef = sbolDoc.getComponentDefinition(comp.getDefinitionURI());
		if (compDef==null) return false;
		return isSmallMoleculeDefinition(compDef);
	}

	/**
	 * Determine if the given SBOL ComponentDefinition is Complex SBML species.
	 * 
	 * @param compDef - The ComponentDefinition to determine if it is a Complex SBML species.
	 * @return True if the given ComponentDefinition is Complex SBML species. False otherwise. 
	 */
	private static boolean isComplexDefinition(ComponentDefinition compDef) {
		return /*compDef.containsType(ChEBI.NON_COVALENTLY_BOUND_MOLECULAR_ENTITY) ||*/
				compDef.containsType(ComponentDefinition.COMPLEX);
	}

	/**
	 * Determine if the given SBOL ComponentDefinition is Small Molecule SBML species.
	 * 
	 * @param compDef - The ComponentDefinition to determine if it is a Small Molecule SBML species.
	 * @return True if the given ComponentDefinition is Small Molecule SBML species. False otherwise. 
	 */
	private static boolean isSmallMoleculeDefinition(ComponentDefinition compDef) {
		return compDef.containsType(ComponentDefinition.SMALL_MOLECULE);
	}

	//	public static boolean isTFComponent(FunctionalComponent comp, SBOLDocument sbolDoc) {
	//		ComponentDefinition compDef = sbolDoc.getComponentDefinition(comp.getDefinitionURI());
	//		if (compDef==null) return false;
	//		return isTFDefinition(compDef);
	//	}
	//	
	//	public static boolean isTFDefinition(ComponentDefinition compDef) {
	//		return (isProteinDefinition(compDef) || isComplexDefinition(compDef))
	//				&& compDef.containsRole(MyersOntology.TF);
	//	}

	private static boolean isDegradationInteraction(Interaction interact, ModuleDefinition moduleDef, 
			SBOLDocument sbolDoc) {
		if (interact.containsType(SystemsBiologyOntology.DEGRADATION) && interact.getParticipations().size() == 1) {
			Participation partici = null;
			for(Participation part : interact.getParticipations())
			{
				partici = part;
				break;
			}
			if (partici.containsRole(SystemsBiologyOntology.REACTANT)) {
				FunctionalComponent comp = moduleDef.getFunctionalComponent(partici.getParticipantURI());
				if (isSpeciesComponent(comp, sbolDoc))
					return true;
			}
		}
		return false;
	}

	private static boolean isComplexFormationInteraction(Interaction interact, ModuleDefinition moduleDef, 
			SBOLDocument sbolDoc) {
		if (interact.containsType(SystemsBiologyOntology.NON_COVALENT_BINDING)||
				interact.containsType(URI.create("http://www.biopax.org/release/biopax-level3.owl#Complex"))) {
			int complexCount = 0;
			int ligandCount = 0;
			for (Participation partici: interact.getParticipations()) {
				FunctionalComponent comp = moduleDef.getFunctionalComponent(partici.getParticipantURI());
				if ((partici.containsRole(SystemsBiologyOntology.PRODUCT) ||
						partici.containsRole(URI.create("http://identifiers.org/biomodels.sbo/SBO:0000253"))) && 
						(isComplexComponent(comp, sbolDoc)||isSpeciesComponent(comp, sbolDoc))) 
					complexCount++;
				else if ((partici.containsRole(SystemsBiologyOntology.REACTANT) ||
						partici.containsRole(URI.create("http://identifiers.org/biomodels.sbo/SBO:0000280"))) && 
						isSpeciesComponent(comp, sbolDoc))
					ligandCount++;
				else
					return false;
			}
			if (complexCount == 1 && ligandCount > 0)
				return true;
		}
		return false;
	}

	private static boolean isProductionInteraction(Interaction interact, ModuleDefinition moduleDef,
			SBOLDocument sbolDoc) {
		if (interact.containsType(SystemsBiologyOntology.GENETIC_PRODUCTION) && interact.getParticipations().size() == 2/*3*/) {
			boolean hasPromoter = false;
			boolean hasProduct = false;
			//boolean hasTranscribed = false;
			for (Participation partici : interact.getParticipations()) {
				FunctionalComponent comp = moduleDef.getFunctionalComponent(partici.getParticipantURI());
				if ((partici.containsRole(SystemsBiologyOntology.PROMOTER)||
						partici.containsRole(SystemsBiologyOntology.TEMPLATE)) && isPromoterComponent(comp, sbolDoc))
					hasPromoter = true;
				else if (partici.containsRole(SystemsBiologyOntology.PRODUCT) && 
						(isProteinComponent(comp, sbolDoc)||isRNAComponent(comp, sbolDoc)))
					hasProduct = true;
				// TRANSCRIBED
				else if (partici.containsRole(SystemsBiologyOntology.PROMOTER) && isGeneComponent(comp, sbolDoc))
					;//hasTranscribed = true;
			}
			if (hasPromoter && hasProduct /*&& hasTranscribed*/)
				return true;
		}
		return false;
	}

	private static boolean isActivationInteraction(Interaction interact, ModuleDefinition moduleDef,
			SBOLDocument sbolDoc) {
		if ((interact.containsType(SystemsBiologyOntology.GENETIC_ENHANCEMENT) ||
				interact.containsType(SystemsBiologyOntology.STIMULATION)) 
				&& interact.getParticipations().size() == 2) {
			boolean hasActivated = false;
			boolean hasActivator = false;
			for (Participation partici : interact.getParticipations()) {
				FunctionalComponent comp = moduleDef.getFunctionalComponent(partici.getParticipantURI());
				if ((partici.containsRole(SystemsBiologyOntology.PROMOTER)||
						partici.containsRole(SystemsBiologyOntology.STIMULATED)) && isPromoterComponent(comp, sbolDoc))
					hasActivated = true;
				else if (partici.containsRole(SystemsBiologyOntology.STIMULATOR) /*&& isTFComponent(comp, sbolDoc)*/)
					hasActivator = true;
			}
			if (hasActivated && hasActivator)
				return true;
		}
		return false;
	}

	private static boolean isRepressionInteraction(Interaction interact, ModuleDefinition moduleDef,
			SBOLDocument sbolDoc) {
		if ((interact.containsType(SystemsBiologyOntology.GENETIC_SUPPRESSION) ||
				interact.containsType(SystemsBiologyOntology.INHIBITION))
				&& interact.getParticipations().size() == 2) {
			boolean hasRepressed = false;
			boolean hasRepressor = false;
			for (Participation partici : interact.getParticipations()) {
				FunctionalComponent comp = moduleDef.getFunctionalComponent(partici.getParticipantURI());
				if ((partici.containsRole(SystemsBiologyOntology.PROMOTER) ||
						partici.containsRole(SystemsBiologyOntology.INHIBITED)) && isPromoterComponent(comp, sbolDoc))
					hasRepressed = true;
				else if (partici.containsRole(SystemsBiologyOntology.INHIBITOR) /*&& isTFComponent(comp, sbolDoc)*/)
					hasRepressor = true;
			}
			if (hasRepressed && hasRepressor)
				return true;
		}
		return false;
	}

	private static void usage() {
		System.err.println("SBOL2SBML");
		System.err.println("Description: converts SBOL into SBML.");
		System.err.println();
		System.err.println("Usage:");
		System.err.println("\tjava --jar SBOL2SBML.jar [options] <inputFile> [-o <outputLocation>]");
		System.err.println();
		System.err.println("Options:");
		System.err.println("\t-u  URI of ModuleDefinition to convert (optional)");
		System.exit(1);
	}


	public static void main(String[] args) {
		String inputName = null;
		String outputName = null;
		String uri = null;
		String outputDir = null;
		String inputDir = null; 

		File fileFullPath;
		//GOAL: inputFile -o outputLocation -u optionalURI

		if(args.length == 0){
			usage();
		}


		if(args[0].equals("-h")){
			usage();	
		}
		else{
			if (args[0] == null) {
				usage();
			}
			else{
				fileFullPath = new File(args[0]);
				String absPath = fileFullPath.getAbsolutePath();
				inputDir = absPath.substring(0, absPath.lastIndexOf(File.separator)+1);
				inputName = absPath.substring(absPath.lastIndexOf(File.separator)+1);
			}
			for(int i = 1; i< args.length-1; i=i+2){
				String flag = args[i];
				String value = args[i+1];
				switch(flag)
				{
				case "-o":
					fileFullPath = new File(value);
					String absPath = fileFullPath.getAbsolutePath();
					outputDir = absPath.substring(0, absPath.lastIndexOf(File.separator)+1);
					outputName = absPath.substring(absPath.lastIndexOf(File.separator)+1);
					break;
				case "-u":
					uri = value;
					break;
				default:
					usage();
					return;
				}

			}

			SBOLDocument sbolDoc;
			try {
				sbolDoc = SBOLReader.read(new FileInputStream(inputDir + inputName));
				String projectDirectory = ".";
				if (outputName!=null) 
					projectDirectory = outputName;

				if(uri!=null){
					ModuleDefinition topModuleDef= sbolDoc.getModuleDefinition(URI.create(uri));
					List<BioModel> models = SBOL2SBML.generateModel(outputDir, topModuleDef, sbolDoc);
					for (BioModel model : models)
					{
						model.save(outputDir + File.separator + model.getSBMLDocument().getModel().getId() + ".xml");
					}
				}
				else{
					//No ModuleDefinition URI provided so loop over all rootModuleDefinition
					for (ModuleDefinition moduleDef : sbolDoc.getRootModuleDefinitions())
					{
						List<BioModel> models = SBOL2SBML.generateModel(outputDir, moduleDef, sbolDoc);
						for (BioModel model : models)
						{
							model.save(outputDir + File.separator + model.getSBMLDocument().getModel().getId() + ".xml");
						}
					}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SBOLValidationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SBOLConversionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

}
