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
package edu.utah.ece.async.ibiosim.conversion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
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

import edu.utah.ece.async.ibiosim.dataModels.biomodel.annotation.AnnotationUtility;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.annotation.SBOLAnnotation;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.parser.BioModel;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.util.SBMLutilities;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.util.Utility;
import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;


/**
 * Perform conversion from SBOL to SBML. 
 * 
 * @author Nicholas Roehner 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SBOL2SBML {

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
	 * @throws XMLStreamException - Invalid XML file occurred
	 * @throws IOException - Unable to read/write file for SBOL2SBML converter.
	 * @throws BioSimException - if something is wrong with the SBML model.
	 */
	public static List<BioModel> generateModel(String projectDirectory, ModuleDefinition moduleDef, SBOLDocument sbolDoc) throws XMLStreamException, IOException, BioSimException {

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

		//option 1
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

	/**
	 * Convert the given SBOL ModuleDefinition and its submodule to equivalent SBML models. 
	 * SBML replacement and replacedBy objects will be created for each SBOL MapsTo that occur in the given SBML submodule.
	 * Annotation will be performed on SBML the given SBML submodule for any SBOL subModule information that can't be mapped directly.
	 * 
	 * @param projectDirectory - The location to generate the SBML model. 
	 * @param subModule - The SBOL subModule that is referenced within the given ModuleDefinition to be converted into SBML model.
	 * @param moduleDef - The given ModuleDefinition that contains the submodule to be converted into SBML model.
	 * @param sbolDoc - The SBOL Document that contains the SBOL objects to convert to SBML model.
	 * @param subTargetModel - The SBML submodel that the converted SBOL subModule will be converted to.
	 * @param targetModel - The SBML local model.
	 */
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

	/**
	 * Perform conversion on the given SBOL Module and ModuleDefinition into SBML model. 
	 * Each SBML model will be generated into its own .xml file that will be stored into the given project directory.
	 * To retain the the submodels that are referenced in the given SBOL ModuleDefinition, SBML replacement and replacedBy
	 * will handle each submodels that are referenced from the "top level" moduleDefinition.
	 * 
	 * @param projectDirectory - The location to generate the SBML model. 
	 * @param subModule - The SBOL submodule referenced by the given SBOL ModuleDefinition.
	 * @param moduleDef - The SBOL ModuleDefinition that contains the referenced submodules.
	 * @param sbolDoc - The SBOL Document that contains the SBOL objects to convert to SBML model.
	 * @param targetModel - The SBML "top level" model that will referenced all converted SBML submodels. 
	 * @return
	 * @throws XMLStreamException - Invalid XML file.
	 * @throws IOException - Unable to read/write file for SBOL2SBML converter.
	 * @throws BioSimException - if something is wrong the with SBML model.
	 */
	private static List<BioModel> generateSubModel(String projectDirectory, Module subModule, ModuleDefinition moduleDef, SBOLDocument sbolDoc, 
			BioModel targetModel) throws XMLStreamException, IOException, BioSimException {
		ModuleDefinition subModuleDef = sbolDoc.getModuleDefinition(subModule.getDefinitionURI());
		//convert each submodules into its own SBML model stored in their own .xml file.
		List<BioModel> subModels = generateModel(projectDirectory, subModuleDef, sbolDoc);
		BioModel subTargetModel = subModels.get(subModels.size()-1);
		
		//Perform replacement and replacedBy with each subModules to its referenced ModuleDefinition.
		generateSubModel(projectDirectory, subModule, moduleDef, sbolDoc, subTargetModel, targetModel);
		return subModels;
	}

	/**
	 * Convert the given SBOL MapsTo object into SBML replacement for the given remote and local SBOL ModuleDefinition.
	 * Annotation will be performed on SBML replacement for any SBOL MapsTo information that can't be mapped directly.
	 * 
	 * @param mapping - The SBOL MapsTo to be converted to SBML replacement.  
	 * @param subModule - The SBOL Module that is considered to be the remote model in the replacement object.
	 * @param moduleDef - The SBOL ModuleDefinition that is considered to be the local model in the replacement object.
	 * @param sbolDoc - The SBOL Document that contains the SBOL objects to convert to SBML replacement.
	 * @param subTargetModel - The SBML remote model that contain the SBML replacement.
	 * @param targetModel - The SBML local model that contain the SBML replacement.
	 */
	private static void generateReplacement(MapsTo mapping, Module subModule, ModuleDefinition moduleDef, 
			SBOLDocument sbolDoc, BioModel subTargetModel, BioModel targetModel) {
		ModuleDefinition subModuleDef = sbolDoc.getModuleDefinition(subModule.getDefinitionURI()); 
		FunctionalComponent remoteSpecies = subModuleDef.getFunctionalComponent(mapping.getRemoteURI());
		FunctionalComponent localSpecies = moduleDef.getFunctionalComponent(mapping.getLocalURI());

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

	/**
	 * Convert the given SBOL MapsTo object into SBML replacedBy for the given remote and local SBOL ModuleDefinition.
	 * Annotation will be performed on SBML replacedBy for any SBOL MapsTo information that can't be mapped directly.
	 * 
	 * @param mapping - The SBOL MapsTo to be converted to SBML replacedBy.  
	 * @param subModule - The SBOL Module that is considered to be the remote model in the replacedBy object.
	 * @param moduleDef - The SBOL ModuleDefinition that is considered to be the local model in the replacedBy object.
	 * @param sbolDoc - The SBOL Document that contains the SBOL objects to convert to SBML replacedBy.
	 * @param subTargetModel - The SBML remote model that contain the SBML replacedBy.
	 * @param targetModel - The SBML local model that contain the SBML replacedBy.
	 */
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

	/**
	 * Set the SBML port of the given FunctionalComponent in the given SBML model as an input species.
	 * 
	 * @param species - The SBOL FunctionalComponent to set as an input species in its equivalent SBML model.
	 * @param targetModel - The SBML model that contain the input port for the given species.
	 */
	private static void generateInputPort(FunctionalComponent species, BioModel targetModel) {
		targetModel.createDirPort(getDisplayID(species), GlobalConstants.INPUT);
	}

	/**
	 * Set the SBML port of the given FunctionalComponent in the given SBML model as an output species.
	 * 
	 * @param species - The SBOL FunctionalComponent to set as an output species in its equivalent SBML model.
	 * @param targetModel - The SBML model that contain the output port for the given species.
	 */
	private static void generateOutputPort(FunctionalComponent species, BioModel targetModel) {
		targetModel.createDirPort(getDisplayID(species), GlobalConstants.OUTPUT);
	}

	/**
	 * Convert the given SBOL FunctionalComponent to  its equivalent SBML species. 
	 * SBO terms will be assigned to the generated SBML species to retain the type of SBOL FunctionalComponent.
	 * Annotation will be performed on the species for any FunctionalComponent information that can't be mapped directly to the SBML species. 
	 * 
	 * @param species - The SBOL FunctionalComponent to convert to SBML species.
	 * @param sbolDoc - The SBOL Document that contains the SBOL objects to convert to SBML promoter species.
	 * @param targetModel - The SBML model to store the SBML promoter species created from the conversion.
	 */
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

	/**
	 *  Convert the given SBOL FunctionalComponent that plays the role of promoter to its equivalent SBML species.
	 *  Annotation will be performed on the species for any FunctionalComponent information that can't be mapped directly to the SBML species. 
	 *  
	 * @param promoter - The SBOL FunctionalComponent to convert to SBML species.
	 * @param sbolDoc - The SBOL Document that contains the SBOL objects to convert to SBML promoter species.
	 * @param targetModel - The SBML model to store the SBML promoter species created from the conversion.
	 */
	private static void generatePromoterSpecies(FunctionalComponent promoter, SBOLDocument sbolDoc, BioModel targetModel) {
		targetModel.createPromoter(getDisplayID(promoter), -1, -1, true, false, null);
		Species sbmlPromoter = targetModel.getSBMLDocument().getModel().getSpecies(getDisplayID(promoter));

		// Annotate SBML promoter species with SBOL component and component definition
		ComponentDefinition compDef = sbolDoc.getComponentDefinition(promoter.getDefinitionURI());
		if (compDef!=null) {
			annotateSpecies(sbmlPromoter, promoter, compDef, sbolDoc);
		}
	}

	/**
	 * Convert the given SBOL biochemical reaction interaction to its equivalent SBML reaction with its corresponding SpeciesReference.
	 * Each SBOL participation that takes place in the biochemical reaction will be given SBO terms to retain the role of the participation 
	 * that occurred. 
	 * 
	 * @param interaction - The SBOL Interaction that has the role of reaction of the biochemical reaction.
	 * @param moduleDef - The SBOL ModuleDefinition that contain the SBOL biochemical reaction objects to convert to SBML biochemical reaction.
	 * @param targetModel - The SBML model to store the SBML Reaction and SpeciesReference created from the conversion.
	 */
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

	/**
	 * Convert the given SBOL degradation interaction to its equivalent SBML reaction with its corresponding SpeciesReference.
	 * 
	 * @param degradation - The SBOL Interaction that has the role of reaction of the degradation reaction.
	 * @param moduleDef - The SBOL ModuleDefinition that contain the SBOL degradation objects to convert to SBML degradation reaction.
	 * @param targetModel - The SBML model to store the SBML Reaction and SpeciesReference created from the conversion.
	 */
	private static void generateDegradationRxn(Interaction degradation, ModuleDefinition moduleDef, BioModel targetModel) {
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

	/**
	 * Convert the given list of SBOL complex formation interaction to its equivalent SBML reaction with its corresponding SpeciesReference.
	 * 
	 * @param complexFormation - The SBOL Interaction that has the role of reaction of the complex formation reaction.
	 * @param complex - The SBOL Participation that has the role of product of the complex formation reaction
	 * @param ligands - The list of SBOL Participations that has the role of reactant of the complex formation reaction
	 * @param moduleDef - The SBOL ModuleDefinition that contain the SBOL complex formation objects to convert to SBML complex formation reaction.
	 * @param targetModel - The SBML model to store the SBML Reaction and SpeciesReference created from the conversion.
	 */
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

	/**
	 * Convert the given list of SBOL production interaction and its related components to its equivalent SBML reaction with its corresponding SpeciesReference.
	 * Annotation will be performed on the SBML species of the resulting production reaction from the SBOL object.
	 * 
	 * @param promoter - The SBOL FunctionalComponent promoter that takes part in the production reaction.
	 * @param partici  - The SBOL Interaction that takes part in the production reaction.
	 * @param productions - The SBOL Interaction production that takes part in the production reaction.
	 * @param activations - The SBOL Interaction activation that takes part in the production reaction.
	 * @param repressions - The SBOL Interaction repressions that takes part in the production reaction.
	 * @param products - The SBOL Participation products that takes part in the production reaction.
	 * @param transcribed - The SBOL Participation transcribed that takes part in the production reaction.
	 * @param activators - The SBOL Participation activators that takes part in the production reaction.
	 * @param repressors - The SBOL Participation repressors that takes part in the production reaction.
	 * @param moduleDef - The SBOL ModuleDefinition that contain the SBOL production objects to convert to SBML production reaction.
	 * @param sbolDoc - The SBOL Document that contains the SBOL objects to convert to SBML production reaction.
	 * @param targetModel - The SBML model to store the SBML Reaction and SpeciesReference created from the conversion.
	 */
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

		//Note: find the resulting ComponentDefinition that creates or result in the production reaction to annotate in its equivalent
		// SBML species. 
		for (int i = 0; i < transcribed.size(); i++) {
			FunctionalComponent gene = moduleDef.getFunctionalComponent(transcribed.get(i).getParticipantURI());
			FunctionalComponent protein = moduleDef.getFunctionalComponent(products.get(i).getParticipantURI());

			ComponentDefinition compDef = sbolDoc.getComponentDefinition(gene.getDefinitionURI());
			if (compDef!=null) {
				annotateSpecies(targetModel.getSBMLDocument().getModel().getSpecies(getDisplayID(protein)), compDef);
			}
		}
	}

	/**
	 * Convert SBOL participation that takes on the role of activator into its equivalent SBML SpeciesReference.
	 * 
	 * @param activator - The SBOL participation to be annotated as SBML SpeciesReference.
	 * @param promoter - The SBOL FunctionalComponent promoter of the activator.
	 * @param moduleDef - The SBOL ModuleDefinition that contain the SBOL Participation.
	 * @param productionRxn - The SBML reaction that will store the SpeciesReference created from the converted activator SBOL Participation.
	 * @param targetModel - The SBML model to store the SBML Reaction and SpeciesReference created from the conversion.
	 */
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

	/**
	 * Convert SBOL participation that takes on the role of repressor into its equivalent SBML SpeciesReference.
	 * 
	 * @param repressor - The SBOL participation to be annotated as SBML SpeciesReference.
	 * @param promoter - The SBOL FunctionalComponent promoter of the repressor.
	 * @param moduleDef - The SBOL ModuleDefinition that contain the SBOL Participation.
	 * @param productionRxn - The SBML reaction that will store the SpeciesReference created from the converted repressor SBOL Participation.
	 * @param targetModel - The SBML model to store the SBML Reaction and SpeciesReference created from the conversion.
	 */
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

	/**
	 * Convert SBOL participation that takes on the role of product into its equivalent SBML SpeciesReference.
	 * 
	 * @param product - The SBOL participation to be annotated as SBML SpeciesReference.
	 * @param promoter - The SBOL FunctionalComponent promoter of the product.
	 * @param moduleDef - The SBOL ModuleDefinition that contain the SBOL Participation.
	 * @param productionRxn - The SBML reaction that will store the SpeciesReference created from the converted activation/product SBOL Participation.
	 * @param targetModel - The SBML model to store the SBML Reaction and SpeciesReference created from the conversion.
	 */
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

	/**
	 * Annotate SBML species with the given SBOL FunctionalComponent and ComponentDefinition.
	 * 
	 * @param species - The SBML species to be annotated with SBOL FunctionalComponent and ComponentDefinition.
	 * @param comp - The SBOL FunctionalComponent to be annotated into SBML species.
	 * @param compDef - The SBOL ComponentDefinition to be annotated into SBML species.
	 * @param sbolDoc - The SBOL Document that contains the SBOL FunctionalComponent and ComponentDefinition to parse for annotation.
	 */
	private static void annotateSpecies(Species species, FunctionalComponent comp, ComponentDefinition compDef, 
			SBOLDocument sbolDoc) {
		SBOLAnnotation speciesAnno = new SBOLAnnotation(species.getMetaId(), compDef.getIdentity());
		speciesAnno.createSBOLElementsDescription(comp.getClass().getSimpleName(), 
				comp.getDefinitionURI()); 
		speciesAnno.createSBOLElementsDescription(compDef.getClass().getSimpleName(), 
				compDef.getIdentity());
		AnnotationUtility.setSBOLAnnotation(species, speciesAnno);	
	}

	
	/**
	 * Annotate SBML species with SBOL component, component definition, and any existing, annotating
	 * DNA components or strand sign.
	 * 
	 * @param species - The SBML species to be annotated with SBOL FunctionalComponent 
	 * @param comp - The SBOL FunctionalComponent to be annotated into SBML species.
	 * @param sbolDoc - The SBOL Document that contains the SBOL FunctionalComponent to parse for annotation.
	 */
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

	/**
	 * Annotate SBML species with SBOL DNA ComponentDefinition and any existing, annotating SBOL elements.
	 * 
	 * @param species - The SBML species to be annotated with SBOL ComponentDefinition
	 * @param compDef - The SBOL ComponentDefinition to be annotated into SBML species.
	 */
	private static void annotateSpecies(Species species, ComponentDefinition compDef) {
		SBOLAnnotation speciesAnno = new SBOLAnnotation(species.getMetaId(), compDef.getIdentity());
		HashMap<String, List<URI>> sbolElementIdentities = new HashMap<String, List<URI>>();
		AnnotationUtility.parseSBOLAnnotation(species, sbolElementIdentities);
		for (String className : sbolElementIdentities.keySet()) {
			speciesAnno.createSBOLElementsDescription(className, sbolElementIdentities.get(className));
		}
		AnnotationUtility.setSBOLAnnotation(species, speciesAnno);	
	}

	
	/**
	 * Annotate SBML reaction with a list of SBOL interactions.
	 * 
	 * @param rxn - The SBML reaction to be annotated with SBOL interactions
	 * @param interacts - The SBOL Interactions to be annotated into SBML reaction.
	 */
	private static void annotateRxn(Reaction rxn, List<Interaction> interacts) {
		List<URI> interactIdentities = new LinkedList<URI>();
		for (Interaction interact : interacts)
			interactIdentities.add(interact.getIdentity());
		SBOLAnnotation rxnAnno = new SBOLAnnotation(rxn.getMetaId(), 
				interacts.get(0).getClass().getSimpleName(), interactIdentities);
		AnnotationUtility.setSBOLAnnotation(rxn, rxnAnno);
	}

	
	/**
	 * Annotate SBML reaction with SBOL interaction.
	 * 
	 * @param rxn - The SBML reaction to be annotated with SBOL interaction
	 * @param interact - The SBOL Interaction to be annotated into SBML reaction.
	 */
	private static void annotateRxn(Reaction rxn, Interaction interact) {
		SBOLAnnotation rxnAnno = new SBOLAnnotation(rxn.getMetaId(), 
				interact.getClass().getSimpleName(), interact.getIdentity());
		AnnotationUtility.setSBOLAnnotation(rxn, rxnAnno);
	}

	
	/**
	 * Annotate SBML species reference with SBOL participation.
	 * 
	 * @param speciesRef - The SBML SimpleSpeciesReference to be annotated with SBOL participations
	 * @param partici - The SBOL Participation to be annotated into SBML SimpleSpeciesReference.
	 */
	private static void annotateSpeciesReference(SimpleSpeciesReference speciesRef, Participation partici) {
		SBOLAnnotation speciesRefAnno = new SBOLAnnotation(speciesRef.getMetaId(),
				partici.getClass().getSimpleName(), partici.getParticipantURI());
		AnnotationUtility.setSBOLAnnotation(speciesRef, speciesRefAnno);
	}

	
	/**
	 * Annotate SBML SpeciesReference with SBOL Participation
	 * 
	 * @param speciesRef - The SBML SpeciesReference to be annotated with SBOL participations
	 * @param partici - The SBOL Participation to be annotated into SBML SpeciesReference.
	 */
	private static void annotateSpeciesReference(SimpleSpeciesReference speciesRef, List<Participation> partici) {
		List<URI> particiIdentities = new LinkedList<URI>();
		for (Participation p : partici) {
			particiIdentities.add(p.getIdentity());
		}
		SBOLAnnotation speciesRefAnno = new SBOLAnnotation(speciesRef.getMetaId(),
				partici.get(0).getClass().getSimpleName(), particiIdentities);
		AnnotationUtility.setSBOLAnnotation(speciesRef, speciesRefAnno);
	}

	/**
	 * Annotate the given SBOL MapsTo to SBML ReplacedBy element.
	 * 
	 * @param replacedBy - The SBML ReplacedBy object that stores the annotated SBOL MapsTo.
	 * @param mapping - The SBOL MapsTo object to annotate into SBML ReplacedBy.
	 */
	private static void annotateReplacedBy(ReplacedBy replacedBy, MapsTo mapping) {
		SBOLAnnotation replacedByAnno = new SBOLAnnotation(replacedBy.getMetaId(),
				mapping.getClass().getSimpleName(), mapping.getIdentity());
		AnnotationUtility.setSBOLAnnotation(replacedBy, replacedByAnno);
	}

	/**
	 * Annotate the given SBOL MapsTo to SBML replacements.
	 * 
	 * @param replacement - The SBML replacement object that stores the annotated SBOL MapsTo.
	 * @param mapping - The SBOL MapsTo object to annotate into SBML replacement.
	 */
	private static void annotateReplacement(ReplacedElement replacement, MapsTo mapping) {
		SBOLAnnotation replacementAnno = new SBOLAnnotation(replacement.getMetaId(),
				mapping.getClass().getSimpleName(), mapping.getIdentity()); 
		AnnotationUtility.setSBOLAnnotation(replacement, replacementAnno);
	}

	/**
	 * Annotate the given SBOL module to SBML models.
	 * 
	 * @param subModel - The SBML model that stores the annotated SBOL module.
	 * @param subModule - The SBOL model to annotate into SBML model
	 */
	private static void annotateSubModel(Submodel subModel, Module subModule) {
		SBOLAnnotation subModelAnno = new SBOLAnnotation(subModel.getMetaId(),
				subModule.getClass().getSimpleName(), subModule.getDefinitionURI()); 
		AnnotationUtility.setSBOLAnnotation(subModel, subModelAnno);
	}

	/**
	 * Determine if SBOL MapsTo object has inputs or outputs to be mapped in SBML.
	 * 
	 * @param mapping - The SBOL MapsTo object to determine if it can be mapped to SBML input or output components.
	 * @param subModule - The SBOL Module that the MapsTo object is referencing.
	 * @param sbolDoc - The SBOL Document that contains the module being referenced and the MapsTo object is contained in.
	 * @return True if the given SBOL MapsTo object has components that can be mapped to input and output component. False otherwise. 
	 */
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

	/**
	 * Determine if the given SBOL FunctionalComponent is a valid input SBML component.
	 * 
	 * @param comp - The SBOL FunctionalComponent to check if it is a valid input SBML component.
	 * @return True if the given FunctionalComponent is a valid input SBML component. False otherwise.
	 */
	private static boolean isInputComponent(FunctionalComponent comp) {
		return comp.getDirection().equals(DirectionType.IN);
	}

	
	/**
	 * Determine if the given SBOL FunctionalComponent is a valid output SBML component.
	 * 
	 * @param comp - The SBOL FunctionalComponent to check if it is a valid input SBML component.
	 * @return True if the given FunctionalComponent is a valid output SBML component. False otherwise.
	 */
	private static boolean isOutputComponent(FunctionalComponent comp) {
		return comp.getDirection().equals(DirectionType.OUT) || comp.getDirection().equals(DirectionType.INOUT);
	}

	/**
	 * Determine if the given SBOL FunctionalComponent is a valid DNA species.
	 * 
	 * @param comp - The SBOL FunctionalComponent to check if it is contained within the given SBOLDocument.
	 * @param sbolDoc - The SBOL Document to check if the given FunctionalComponent exist.
	 * @return True if the given FunctionalComponent is a valid DNA. False otherwise.
	 */
	private static boolean isDNAComponent(FunctionalComponent comp, SBOLDocument sbolDoc) {
		ComponentDefinition compDef = sbolDoc.getComponentDefinition(comp.getDefinitionURI());
		if (compDef==null) return false;
		return isDNADefinition(compDef);
	}

	/**
	 * Determine if the given SBOL ComponentDefinition is a valid DNA species.
	 * 
	 * @param compDef - The SBOL ComponentDefinition to check if it is a valid DNA species.
	 * @return True if the given ComponentDefinition is a valid DNA species
	 */
	private static boolean isDNADefinition(ComponentDefinition compDef) {
		return compDef.containsType(ComponentDefinition.DNA);
	}

	/**
	 * Determine if the given SBOL FunctionalComponent is a valid protein species.
	 * 
	 * @param comp - The SBOL FunctionalComponent to check if it is contained within the given SBOLDocument.
	 * @param sbolDoc - The SBOL Document to check if the given FunctionalComponent exist.
	 * @return True if the given FunctionalComponent is a valid protein. False otherwise.
	 */
	private static boolean isProteinComponent(FunctionalComponent comp, SBOLDocument sbolDoc) {
		ComponentDefinition compDef = sbolDoc.getComponentDefinition(comp.getDefinitionURI());
		if (compDef==null) return false;
		return isProteinDefinition(compDef);
	}

	/**
	 * Determine if the given SBOL ComponentDefinition is a valid protein species.
	 * 
	 * @param compDef - The SBOL ComponentDefinition to check if it is a valid protein species.
	 * @return True if the given ComponentDefinition is a valid protein species
	 */
	private static boolean isProteinDefinition(ComponentDefinition compDef) {
		return compDef.containsType(ComponentDefinition.PROTEIN);
	}

	/**
	 * Determine if the given SBOL FunctionalComponent is a valid RNA species.
	 * 
	 * @param comp - The SBOL FunctionalComponent to check if it is contained within the given SBOLDocument.
	 * @param sbolDoc - The SBOL Document to check if the given FunctionalComponent exist.
	 * @return True if the given FunctionalComponent is a valid RNA. False otherwise.
	 */
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
		return isDNADefinition(compDef) ;
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

	/**
	 * Determine if the given SBOL FunctionalComponent is a valid Complex SBML species.
	 * 
	 * @param comp - The FunctionalComponent to determine if it is a Complex SBML species.
	 * @param sbolDoc - The SBOL FunctionalComponent to check if it is a valid SBML species
	 * @return True if the given FunctionalComponent is Complex SBML species. False otherwise.
	 */
	private static boolean isComplexComponent(FunctionalComponent comp, SBOLDocument sbolDoc) {
		ComponentDefinition compDef = sbolDoc.getComponentDefinition(comp.getDefinitionURI());
		if (compDef==null) return false;
		return isComplexDefinition(compDef);
	}

	/**
	 * Determine if the given SBOL FunctionalComponent is Small Molecule SBML species.
	 * 
	 * @param comp - The FunctionalComponent to determine if it is a Small Molecule SBML species.
	 * @param sbolDoc - The SBOL FunctionalComponent to check if it is a valid SBML species
	 * @return True if the given FunctionalComponent is Small Molecule SBML species. False otherwise. 
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
		return compDef.containsType(ComponentDefinition.COMPLEX);
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


	/**
	 * Determine if the given SBOL Interaction is a valid degradation interaction.
	 *  
	 * @param interact - The SBOL Interaction to be determined if it is a valid degradation interaction.
	 * @param moduleDef - The SBOL ModuleDefinition that contains the given SBOL Interaction.
	 * @param sbolDoc - The SBOL Document that contains both the SBOL ModuleDefinition and SBOL Interaction.
	 * @return True if the given SBOL Interaction is a valid degradation interaction. False otherwise.
	 */
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

	/**
	 * Determine if the given SBOL Interaction is a valid complex formation interaction.
	 * 
	 * @param interact - The SBOL Interaction to be determined if it is a valid complex formation interaction.
	 * @param moduleDef - The SBOL ModuleDefinition that contains the given SBOL Interaction.
	 * @param sbolDoc - The SBOL Document that contains both the SBOL ModuleDefinition and SBOL Interaction.
	 * @return True if the given SBOL Interaction is a valid complex formation interaction. False otherwise.
	 */
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

	/**
	 * Determine if the given SBOL Interaction is a valid production interaction.
	 * 
	 * @param interact - The SBOL Interaction to be determined if it is a valid production interaction.
	 * @param moduleDef - The SBOL ModuleDefinition that contains the given SBOL Interaction.
	 * @param sbolDoc - The SBOL Document that contains both the SBOL ModuleDefinition and SBOL Interaction.
	 * @return True if the given SBOL Interaction is a valid production interaction. False otherwise.
	 */
	private static boolean isProductionInteraction(Interaction interact, ModuleDefinition moduleDef,
			SBOLDocument sbolDoc) {
		if (interact.containsType(SystemsBiologyOntology.GENETIC_PRODUCTION) && interact.getParticipations().size() == 2/*3*/) {
			boolean hasPromoter = false;
			boolean hasProduct = false;
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
					;
			}
			if (hasPromoter && hasProduct)
				return true;
		}
		return false;
	}

	/**
	 * Determine if the given SBOL Interaction is a valid activation interaction.
	 * 
	 * @param interact - The SBOL Interaction to be determined if it is a valid activation interaction.
	 * @param moduleDef - The SBOL ModuleDefinition that contains the given SBOL Interaction.
	 * @param sbolDoc - The SBOL Document that contains both the SBOL ModuleDefinition and SBOL Interaction.
	 * @return True if the given SBOL Interaction is a valid activation interaction. False otherwise.
	 */
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

	/**
	 * Determine if the given SBOL Interaction is a valid repression interaction.
	 * 
	 * @param interact - The SBOL Interaction to be determined if it is a valid repression interaction.
	 * @param moduleDef - The SBOL ModuleDefinition that contains the given SBOL Interaction.
	 * @param sbolDoc - The SBOL Document that contains both the SBOL ModuleDefinition and SBOL Interaction.
	 * @return True if the given SBOL Interaction is a valid repression interaction. False otherwise.
	 */
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
				else if (partici.containsRole(SystemsBiologyOntology.INHIBITOR) )
					hasRepressor = true;
			}
			if (hasRepressed && hasRepressor)
				return true;
		}
		return false;
	}

	/**
	 * Print to console the command options and requirements to execute this converter.
	 */
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
				e.printStackTrace();
			} catch (SBOLValidationException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SBOLConversionException e) {
				e.printStackTrace();
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
			catch(BioSimException e)
			{
			  e.printStackTrace();
			}

		}
	}

}
