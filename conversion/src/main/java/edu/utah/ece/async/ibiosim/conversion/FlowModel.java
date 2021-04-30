/*
 * @author Pedro Fontanarrosa
 */
package edu.utah.ece.async.ibiosim.conversion;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.Variable;
import org.sbml.jsbml.ext.comp.CompSBasePlugin;
import org.sbml.jsbml.ext.comp.Port;
import org.sbml.jsbml.ext.comp.Submodel;
import org.sbml.libsbml.Rule;
import org.sbolstandard.core2.Annotation;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.EDAMOntology;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.MapsTo;
import org.sbolstandard.core2.Module;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.Participation;
import org.sbolstandard.core2.RefinementType;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SequenceConstraint;
import org.sbolstandard.core2.SequenceOntology;
import org.sbolstandard.core2.SystemsBiologyOntology;
import org.sbolstandard.core2.Measure;
import org.synbiohub.frontend.SynBioHubException;

import edu.utah.ece.async.ibiosim.dataModels.biomodel.annotation.AnnotationUtility;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.annotation.SBOLAnnotation;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.parser.BioModel;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.util.SBMLutilities;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.util.Utility;
import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;

public class FlowModel {
	
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
	 * @throws SBOLValidationException - thrown when there is a SBOL validation error
	 */
    
	public static HashMap<String,BioModel> generateModel(String projectDirectory, ModuleDefinition moduleDef, SBOLDocument sbolDoc) throws XMLStreamException, IOException, BioSimException, SBOLValidationException {
				
		HashMap<String,BioModel> models = new HashMap<String,BioModel>();

		BioModel targetModel = new BioModel(projectDirectory);
		targetModel.createSBMLDocument(SBOL2SBML.getDisplayID(moduleDef), false, false);
		if (sbolDoc.getModel(SBOL2SBML.getDisplayID(moduleDef)+"_model", "1")==null) {
			org.sbolstandard.core2.Model sbolModel = sbolDoc.createModel(SBOL2SBML.getDisplayID(moduleDef)+"_model", "1", 
					URI.create("file:" + SBOL2SBML.getDisplayID(moduleDef) + ".xml"), 
					EDAMOntology.SBML, SystemsBiologyOntology.DISCRETE_FRAMEWORK);
			moduleDef.addModel(sbolModel);
		}
		
		// Annotate SBML model with SBOL module definition
		Model sbmlModel = targetModel.getSBMLDocument().getModel();
		SBOLAnnotation modelAnno = new SBOLAnnotation(sbmlModel.getMetaId(), 
				moduleDef.getClass().getSimpleName(), moduleDef.getIdentity()); 
		AnnotationUtility.setSBOLAnnotation(sbmlModel, modelAnno);
		
		//Determine the relationship between Sensor Molecules and their target. This method will return a list with Sensor Proteins/Complexes related
		//the ligand they interact with.
		HashMap<String, String> sensorMolecules = sensorMolecules(sbolDoc);
		
		//This maps ligands, small molecules and their complex formation molecule to use in replacements later in the model
		HashMap<String, HashMap <String, String>> complex2sensor2ligand = mapSensorToLigand(sbolDoc);
		
		//This method returns a map with each protein, and it's associated Cello parameters.
		HashMap<String, List<String>> Prot_2_Param = productionInteractions(sbolDoc);
		
		//Determine the activations/repressions of each promoter in the SBOLDocument. Returns a map that relates each promoter, if it is activated/repressed
		//with the protein/ligand responsible for it's activation/repression and the Cello parameters associated with the interaction.
		HashMap<String, HashMap <String, String>> promoterInteractions = promoterInteractions(targetModel, sbolDoc, Prot_2_Param, complex2sensor2ligand, sensorMolecules);
		
		// Flatten ModuleDefinition. Combine all parts of a Transcriptional Unit into a single TU. 
		ModuleDefinition resultMD = SBOL2SBML.MDFlattener(sbolDoc, moduleDef);
		
		//Removes the complex formation interaction (from sensor protein and ligand) from the document, so that no species is created for these
		//removeSensorInteractios(resultMD, sensorMolecules);
		

		// Generate SBML Species for each part in the model
		for (FunctionalComponent comp : resultMD.getFunctionalComponents()) {
			if (SBOL2SBML.isSpeciesComponent(comp, sbolDoc)) {
				if (SBOL2SBML.isSmallMoleculeComponent(comp, sbolDoc)){
					SBOL2SBML.generateSpecies(comp, sbolDoc, targetModel);
					if (SBOL2SBML.isInputComponent(comp)) {
						SBOL2SBML.generateInputPort(comp, targetModel);
					} else if (SBOL2SBML.isOutputComponent(comp)){
						SBOL2SBML.generateOutputPort(comp, targetModel);
					}
				} else {
					continue;
				}
			} else if (SBOL2SBML.isPromoterComponent(resultMD, comp, sbolDoc)) {
				generateTUSpecies(comp, sbolDoc, targetModel);
				if (SBOL2SBML.isInputComponent(comp)) {
					SBOL2SBML.generateInputPort(comp, targetModel);
				} else if (SBOL2SBML.isOutputComponent(comp)){
					SBOL2SBML.generateOutputPort(comp, targetModel);
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
		
		for (Interaction interact : resultMD.getInteractions()) {
			if (SBOL2SBML.isDegradationInteraction(interact, resultMD, sbolDoc)) {				
			} else if (SBOL2SBML.isComplexFormationInteraction(interact, resultMD, sbolDoc)) {
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
				//SBOL2SBML.generateComplexFormationRxn(interact, complex, ligands, resultMD, targetModel);
			} else if (SBOL2SBML.isProductionInteraction(interact, resultMD, sbolDoc)) {
				FunctionalComponent promoter = null;
				for (Participation partici : interact.getParticipations())
					if (partici.containsRole(SystemsBiologyOntology.PROMOTER)||
							partici.containsRole(SystemsBiologyOntology.TEMPLATE)) {
						promoter = resultMD.getFunctionalComponent(partici.getParticipantURI());
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
			} else if (SBOL2SBML.isActivationInteraction(interact, resultMD, sbolDoc)) {
				FunctionalComponent promoter = null;
				for (Participation partici : interact.getParticipations())
					if (partici.containsRole(SystemsBiologyOntology.PROMOTER)||
							partici.containsRole(SystemsBiologyOntology.STIMULATED)) {
						promoter = resultMD.getFunctionalComponent(partici.getParticipantURI());
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
			} else if (SBOL2SBML.isRepressionInteraction(interact, resultMD, sbolDoc)) {
				FunctionalComponent promoter = null;
				for (Participation partici : interact.getParticipations())
					if (partici.containsRole(SystemsBiologyOntology.PROMOTER)||
							partici.containsRole(SystemsBiologyOntology.INHIBITED)) {
						promoter = resultMD.getFunctionalComponent(partici.getParticipantURI());
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
				SBOL2SBML.generateBiochemicalRxn(interact, resultMD, targetModel);
			}
		}
		
		for (FunctionalComponent promoter : resultMD.getFunctionalComponents()) { 
			if (SBOL2SBML.isPromoterComponent(resultMD, promoter, sbolDoc)) {
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

				generateFlowProductionRxns(promoter, promoterToPartici.get(promoter), promoterToProductions.get(promoter), 
						promoterToActivations.get(promoter), promoterToRepressions.get(promoter), promoterToProducts.get(promoter),
						promoterToTranscribed.get(promoter), promoterToActivators.get(promoter),
						promoterToRepressors.get(promoter), resultMD, sbolDoc, targetModel, Prot_2_Param, promoterInteractions, complex2sensor2ligand);
			}
		}
				
		//option 1
		for (Module subModule : resultMD.getModules()) {
			//Remove MapsTo's from the submodules that reference the sensorMolecules that we are not using in this model
			for (MapsTo subMaps : subModule.getMapsTos()) {
				for (String toRemove : sensorMolecules.keySet()) {
					if (subMaps.getRemoteDefinition().getDisplayId().equals(toRemove)) {
						subModule.removeMapsTo(subMaps);
					}
				}
			}
			ModuleDefinition subModuleDef = sbolDoc.getModuleDefinition(subModule.getDefinitionURI());
			ModuleDefinition subModuleDefFlatt = SBOL2SBML.MDFlattener(sbolDoc, subModuleDef);
/*			if (SensorGateModule(subModuleDefFlatt, sensorMolecules, sbolDoc)) {
				continue;
			}*/
			BioModel subTargetModel = new BioModel(projectDirectory);
			if (subTargetModel.load(projectDirectory + File.separator + SBOL2SBML.getDisplayID(subModuleDefFlatt) + ".xml")) {
				generateSubModel(projectDirectory, subModule, resultMD, sbolDoc, subTargetModel, targetModel);
			} if ((subTargetModel=models.get(SBOL2SBML.getDisplayID(subModuleDefFlatt)))!=null) {
				generateSubModel(projectDirectory, subModule, resultMD, sbolDoc, subTargetModel, targetModel);
			} else {
				HashMap<String,BioModel> subModels = generateSubModel(projectDirectory, subModule, resultMD, sbolDoc, targetModel);
				for (String key : subModels.keySet()) {
					models.put(key,subModels.get(key));
				}
			}
		}
		models.put(SBOL2SBML.getDisplayID(resultMD),targetModel);
		
		return models;
	}
	
	
	/**
	 * Convert the given SBOL ModuleDefinition and its submodule to equivalent SBML models. 
	 * SBML replacement and replacedBy objects will be created for each SBOL MapsTo that occur in the given SBML submodule.
	 * Annotation will be performed on SBML the given SBML submodule for any SBOL subModule information that can't be mapped directly.
	 * NOTE: This method emulates almost exactly the same method in SBOL2SBML.java class
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
		targetModel.addComponent(SBOL2SBML.getDisplayID(subModule), SBOL2SBML.getDisplayID(subModuleDef) + ".xml", 
				subTargetModel.IsWithinCompartment(), subTargetModel.getCompartmentPorts(), 
				-1, -1, 0, 0, md5);
		SBOL2SBML.annotateSubModel(targetModel.getSBMLCompModel().getSubmodel(SBOL2SBML.getDisplayID(subModule)), subModule);
		
		// This portion of the code creates replacements for input molecules
		for (FunctionalComponent fc : moduleDef.getFunctionalComponents()) {
			ComponentDefinition ligand = fc.getDefinition();
			if (SBOL2SBML.isSmallMoleculeDefinition(ligand)) {
				generateReplacement(fc, subModule, moduleDef, sbolDoc, subTargetModel, targetModel);
			}
		}
		
		// This portion of the method creates SBML species for promoter fluxes (for example "Y_PhlF"). This is because they
		// are not created for the top level model. It then creates the appropriate replacements.
		for (FunctionalComponent fc : moduleDef.getFunctionalComponents()) {
			String spe = fc.getDisplayId();
			ComponentDefinition CD = fc.getDefinition();
			if (CD.containsType(URI.create("http://www.biopax.org/release/biopax-level3.owl#Complex"))) {
				continue;				
			}
			if (spe.contains("_protein")) {
				spe = spe.replace("_protein", "");
				
				//TODO PEDRO ---- TEMPORARY HACK TO SOLVE YFP OUTPUT PROBLEM
				if (!spe.contains("YFP")) {
					spe = "Y_" + spe;
				} 				
				Species pe = targetModel.getSBMLDocument().getModel().getSpecies(spe);
				if (pe == null) {
					targetModel.createSpecies(spe, -1, -1);
					pe = targetModel.getSBMLDocument().getModel().getSpecies(spe);
					targetModel.createDirPort(spe, GlobalConstants.OUTPUT);
					//Species sbmlSpecies = targetModel.getSBMLDocument().getModel().getSpecies(spe);
					pe.setBoundaryCondition(false);
					pe.setSBOTerm(GlobalConstants.SBO_FLUX_BALANCE);
					//generateReplacementForFlow(pe, subModule, moduleDef, sbolDoc, subTargetModel, targetModel);
				}
				generateReplacementForFlow(pe, subModule, moduleDef, sbolDoc, subTargetModel, targetModel);
			}
		}
		
		for (MapsTo mapping : subModule.getMapsTos()) 
			if (SBOL2SBML.isIOMapping(mapping, subModule, sbolDoc)) {
				FunctionalComponent remoteSpecies = subModuleDef.getFunctionalComponent(mapping.getRemoteIdentity());
				FunctionalComponent localSpecies = moduleDef.getFunctionalComponent(mapping.getLocalURI());
				if(remoteSpecies.getDefinition().containsType(ComponentDefinition.PROTEIN) || localSpecies.getDefinition().containsType(ComponentDefinition.PROTEIN)) {
					continue;
				} else {
					RefinementType refinement = mapping.getRefinement();
					if (refinement == RefinementType.VERIFYIDENTICAL || refinement == RefinementType.MERGE
							|| refinement == RefinementType.USELOCAL) {
						SBOL2SBML.generateReplacement(mapping, subModule, moduleDef, sbolDoc, subTargetModel, targetModel);
					} else if (refinement == RefinementType.USEREMOTE) {
						SBOL2SBML.generateReplacedBy(mapping, subModule, moduleDef, sbolDoc, subTargetModel, targetModel);
					}
				}
			}
	}

	/**
	 * Perform conversion on the given SBOL Module and ModuleDefinition into SBML model. 
	 * Each SBML model will be generated into its own .xml file that will be stored into the given project directory.
	 * To retain the the submodels that are referenced in the given SBOL ModuleDefinition, SBML replacement and replacedBy
	 * will handle each submodels that are referenced from the "top level" moduleDefinition.
	 * NOTE: This method emulates almost exactly the same method in SBOL2SBML.java class
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
	 * @throws SBOLValidationException 
	 * @throws SynBioHubException 
	 * @throws SBOLConversionException 
	 */
	private static HashMap<String,BioModel> generateSubModel(String projectDirectory, Module subModule, ModuleDefinition moduleDef, SBOLDocument sbolDoc, 
			BioModel targetModel) throws XMLStreamException, IOException, BioSimException, SBOLValidationException {
		ModuleDefinition subModuleDef = sbolDoc.getModuleDefinition(subModule.getDefinitionURI());
		//convert each submodules into its own SBML model stored in their own .xml file.
		HashMap<String,BioModel> subModels = generateModel(projectDirectory, subModuleDef, sbolDoc);
		BioModel subTargetModel = subModels.get(SBOL2SBML.getDisplayID(subModuleDef));
		
		//Perform replacement and replacedBy with each subModules to its referenced ModuleDefinition.
		generateSubModel(projectDirectory, subModule, moduleDef, sbolDoc, subTargetModel, targetModel);
		return subModels;
	}
	
	/**
	 * This method is used when the model needs one SBML species per Transcriptional Unit (TU) rather than multiple promoter
	 * species (per promoter sequence present in the TU) per TU. 
	 * 
	 * @author Pedro Fontanarrosa
	 * @param promoter the TU the model needs to create a species from
	 * @param sbolDoc the SBOLDocument being worked on
	 * @param targetModel is the target model being created
	 * 
	 */
	private static void generateTUSpecies(FunctionalComponent promoter, SBOLDocument sbolDoc, BioModel targetModel) {
					
			String TU = promoter.getDisplayId();
			if (targetModel.getSBMLDocument().getModel().getSpecies(TU)==null) {
				targetModel.createPromoter(TU, -1, -1, true, false, null);
			}
			Species sbmlPromoter = targetModel.getSBMLDocument().getModel().getSpecies(TU);
			
			// Annotate SBML promoter species with SBOL component and component definition
			ComponentDefinition compDef = sbolDoc.getComponentDefinition(promoter.getDefinitionURI());
			if (compDef!=null) {
				SBOL2SBML.annotateSpecies(sbmlPromoter, promoter, compDef, sbolDoc);
			}	
	}
	
	/**
	 * This method will return true, if the MD input is a SensorGate production Module Definition, in which the model should skip the production of 
	 * a model of this. If it is, it returns true and the model generator doesn't generate a subTargetModel for it
	 *
	 * @param subModule the sub module being analyzed
	 * @param sensorMolecules a HashMap where sensor proteins are part of the key set.
	 * @param sbolDoc the sbol doc
	 * @return true, if this is a subModuleDefinition for the production of sensor proteins from a sensor gate
	 */
	private static boolean SensorGateModule(ModuleDefinition subModule, HashMap<String, String> sensorMolecules, SBOLDocument sbolDoc){
		
		boolean SensorGate = false;
		
		//ModuleDefinition subModuleDef = subModule.getDefinition();
		for (Interaction interact : subModule.getInteractions()) {
			if (SBOL2SBML.isProductionInteraction(interact, subModule, sbolDoc)) {
				for (Participation partici : interact.getParticipations()) {
					if (partici.containsRole(SystemsBiologyOntology.PRODUCT)) {
						String product = partici.getParticipant().getDisplayId();
						if (sensorMolecules.keySet().contains(product)) {
							SensorGate = true;
						}
					}
				}
			}
		}
		return SensorGate;
	}
	
	/**
	 * This method returns a list of complex molecules formed, and their associated ligand. This is going to be used when
	 * determining the sensor promoters, and who represses/activates them (since for Cello, the presence of input ligand
	 * activates the promoters), and do the appropriate species replacements.
	 *
	 * @author Pedro Fontanarrosa
	 * @param sbolDoc the SBOL document in use
	 * @return the hash map where "keys" are the complex molecules and "values" are the ligand associated
	 */
	private static HashMap<String, String> sensorMolecules(SBOLDocument sbolDoc){

		HashMap<String, String> sensorMolecules = new HashMap<String, String>();
				
		for (ModuleDefinition moduleDef : sbolDoc.getModuleDefinitions()) {
			for (Interaction interact : moduleDef.getInteractions()) {
				if (SBOL2SBML.isComplexFormationInteraction(interact, moduleDef, sbolDoc)) {
					ComponentDefinition ligand = null;
					ComponentDefinition complex = null;
					ComponentDefinition prot = null;
					for (Participation partici : interact.getParticipations()) {
						if (partici.containsRole(SystemsBiologyOntology.PRODUCT)) {
							complex = partici.getParticipantDefinition();
							if (!sensorMolecules.containsKey(complex)) {
								//promoterActivations.put(promoter.getDisplayId(), null);
								sensorMolecules.put(complex.getDisplayId(),"");
							}
						}
					}
					for (Participation partici : interact.getParticipations()) {
						if (partici.containsRole(SystemsBiologyOntology.REACTANT)) {
							prot = partici.getParticipantDefinition();
							if(SBOL2SBML.isProteinDefinition(prot)) {
								if (!sensorMolecules.containsKey(prot)) {
									//promoterActivations.put(promoter.getDisplayId(), null);
									sensorMolecules.put(prot.getDisplayId(),"");
									break;
								}
							}
						}
					}
					for (Participation partici : interact.getParticipations()) {
						if (partici.containsRole(SystemsBiologyOntology.REACTANT)) {
							FunctionalComponent comp = moduleDef.getFunctionalComponent(partici.getParticipantURI());
							ligand = partici.getParticipantDefinition();
							ligand = comp.getDefinition();
							if (SBOL2SBML.isSmallMoleculeDefinition(ligand)) {
								sensorMolecules.replace(complex.getDisplayId(), ligand.getDisplayId());
								sensorMolecules.replace(prot.getDisplayId(), ligand.getDisplayId());
								//.get(complex.getDisplayId())
								//.put(partici.getParticipantDefinition().getDisplayId());
							}
						}
					}
				}
			}
		}
		return sensorMolecules;
	}
	
	
	/**
	 * This method searches all activation and repression interactions and maps it to individual promoters (not TUs). This information
	 * will be later used when creating the mathematical model using Hamid's paper for dynamic modeling using Cello Parameters.
	 * 
	 * @author Pedro Fontanarrosa
	 * @param sbolDoc the SBOLDocument
	 * @return the hash map with all the interactions per promoter
	 */
	private static HashMap<String, HashMap <String, String>> promoterInteractions(BioModel targetModel, SBOLDocument sbolDoc, HashMap<String, List<String>> Prot_2_Param, HashMap<String, HashMap <String, String>> complex2sensor2ligand, HashMap<String, String> sensorMolecules){

		HashMap<String, HashMap <String, String>> promoterInteractions = new HashMap<String, HashMap <String, String>>();
		
		boolean SensorPromoterParametersinInteraction = false;
		
		for (ModuleDefinition moduleDef : sbolDoc.getModuleDefinitions()) {
			for (Interaction interact : moduleDef.getInteractions()) {
				if (SBOL2SBML.isActivationInteraction(interact, moduleDef, sbolDoc)) {
					List<Annotation> Annot = interact.getAnnotations();
					String ymax = "";
					String ymin = "";
					String alpha = "";
					String beta = "";
					
					for (int i = 0; i < Annot.size(); i++) {
						if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}ymax"))) {
							ymax = Annot.get(i).getStringValue();
						}
						if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}ymin"))) {
							ymin = Annot.get(i).getStringValue();
						}
						if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}alpha"))) {
							alpha = Annot.get(i).getStringValue();
						}
						if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}beta"))) {
							beta = Annot.get(i).getStringValue();
						}
					}
					Boolean sensor = false;
					if (!ymax.isEmpty() & !ymin.isEmpty()) {
						sensor = true;
						SensorPromoterParametersinInteraction = true;
					}
					ComponentDefinition promoter = null;
					for (Participation partici : interact.getParticipations()) {
						if (partici.containsRole(SystemsBiologyOntology.PROMOTER)|| partici.containsRole(SystemsBiologyOntology.STIMULATED)) {
							if (partici.getParticipantDefinition().getComponents().isEmpty()) {
								promoter = partici.getParticipantDefinition();
								if (!promoterInteractions.containsKey(promoter))
									//promoterActivations.put(promoter.getDisplayId(), null);
									promoterInteractions.put(promoter.getDisplayId(), new HashMap <String, String>());
							}
						}  else {
							for (Component comp : partici.getParticipantDefinition().getComponents()) {
								if (comp.getDefinition().containsRole(SystemsBiologyOntology.PROMOTER) || comp.getDefinition().containsRole(SystemsBiologyOntology.STIMULATED)) {
									promoter = comp.getDefinition();
									if (!promoterInteractions.containsKey(promoter)) {
										promoterInteractions.put(promoter.getDisplayId(), new HashMap <String, String>());
									}
								}
							}
						}
					}
					for (Participation partici : interact.getParticipations()) {
						if (partici.containsRole(SystemsBiologyOntology.STIMULATOR)) {
							//promoterActivations.put(promoter.getDisplayId(), partici.getParticipantDefinition());
							if (sensor) {
								String protein = "";
								if (complex2sensor2ligand.keySet().contains(partici.getParticipantDefinition().getDisplayId())) {
									HashMap<String, String> protein2ligand = complex2sensor2ligand.get(partici.getParticipantDefinition().getDisplayId());
									for (String activator : protein2ligand.keySet()) {
										protein = activator;
									}
								} else {
									protein = partici.getParticipantDefinition().getDisplayId();
								}
								promoterInteractions.get(promoter.getDisplayId()).put("sensor", protein);
								Prot_2_Param.put(protein, Arrays.asList("", "", ymax, ymin, alpha, beta, "", ""));
							} else {
								boolean isInputPromoter = false;
								//get all the annotations of the part, which is where the cello parameters are stored as from (09/05/18). Eventually
								//the location of these parameters can change
								List<Annotation> Annot2 = promoter.getAnnotations();

								for (int j = 0; j < Annot2.size(); j++) {
									if (Annot2.get(j).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}gateType"))) {
										if (Annot2.get(j).getStringValue().equals("input_sensor")) {
											isInputPromoter = true;
										}	
									}
								}
								if (isInputPromoter) {
									sensorPromoterParametrizationT2Collection2(targetModel, promoterInteractions, sbolDoc, Prot_2_Param, complex2sensor2ligand, sensorMolecules, promoter, partici.getParticipantDefinition());
								} else {
									promoterInteractions.get(promoter.getDisplayId()).put("activation", partici.getParticipantDefinition().getDisplayId());
								}
								//promoterInteractions.get(promoter.getDisplayId()).put("activation", partici.getParticipantDefinition().getDisplayId());
							}	
						}
					}
				} else if (SBOL2SBML.isRepressionInteraction(interact, moduleDef, sbolDoc)) {
					List<Annotation> Annot = interact.getAnnotations();
					String ymax = "";
					String ymin = "";
					String alpha = "";
					String beta = "";
					
					for (int i = 0; i < Annot.size(); i++) {
						if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}ymax"))) {
							ymax = Annot.get(i).getStringValue();
						}
						if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}ymin"))) {
							ymin = Annot.get(i).getStringValue();
						}
						if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}alpha"))) {
							alpha = Annot.get(i).getStringValue();
						}
						if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}beta"))) {
							beta = Annot.get(i).getStringValue();
						}
					}
					Boolean sensor = false;
					if (!ymax.isEmpty() & !ymin.isEmpty()) {
						sensor = true;
						SensorPromoterParametersinInteraction = true;
					}
					ComponentDefinition promoter = null;
					for (Participation partici : interact.getParticipations()) {
						if (partici.containsRole(SystemsBiologyOntology.PROMOTER)|| partici.containsRole(SystemsBiologyOntology.INHIBITED)) {
							if (partici.getParticipantDefinition().getComponents().isEmpty()) {
								promoter = partici.getParticipantDefinition();
								if (!promoterInteractions.containsKey(promoter)) {
									promoterInteractions.put(promoter.getDisplayId(), new HashMap <String, String>());
								}
							} else {
								for (Component comp : partici.getParticipantDefinition().getComponents()) {
									if (comp.getDefinition().containsRole(SystemsBiologyOntology.PROMOTER) || comp.getDefinition().containsRole(SystemsBiologyOntology.INHIBITED)) {
										promoter = comp.getDefinition();
										if (!promoterInteractions.containsKey(promoter)) {
											promoterInteractions.put(promoter.getDisplayId(), new HashMap <String, String>());
										}
									}
								}
							}
						}
					}
					for (Participation partici : interact.getParticipations()) {
						if (partici.containsRole(SystemsBiologyOntology.INHIBITOR)) {
							//promoterActivations.put(promoter.getDisplayId(), partici.getParticipantDefinition());
							if (sensor) {
								promoterInteractions.get(promoter.getDisplayId()).put("sensor", partici.getParticipantDefinition().getDisplayId());
								Prot_2_Param.put(partici.getParticipantDefinition().getDisplayId(), Arrays.asList("", "", ymax, ymin, alpha, beta, "", ""));
							} else {
								boolean isInputPromoter = false;
								//get all the annotations of the part, which is where the cello parameters are stored as from (09/05/18). Eventually
								//the location of these parameters can change
								List<Annotation> Annot2 = promoter.getAnnotations();

								for (int j = 0; j < Annot2.size(); j++) {
									if (Annot2.get(j).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}gateType"))) {
										if (Annot2.get(j).getStringValue().equals("input_sensor")) {
											isInputPromoter = true;
										}	
									}
								}
								if (isInputPromoter) {
									sensorPromoterParametrizationT2Collection2(targetModel, promoterInteractions, sbolDoc, Prot_2_Param, complex2sensor2ligand, sensorMolecules, promoter, partici.getParticipantDefinition());
								} else {
									promoterInteractions.get(promoter.getDisplayId()).put("repression", partici.getParticipantDefinition().getDisplayId());
								}
								//promoterInteractions.get(promoter.getDisplayId()).put("repression", partici.getParticipantDefinition().getDisplayId());
							}
						}
					}
				}
			}
		}
		
		if (!SensorPromoterParametersinInteraction) {
			//promoterInteractions = sensorPromoterParametrizationT2Collection(promoterInteractions, sbolDoc, Prot_2_Param, complex2sensor2ligand, sensorMolecules);
		}

		return promoterInteractions;
	}

	
	/**
	 * This method returns a HashMap with a activator/repressor, and Cello Parameters associated with it if it has any. This
	 * will be used later to populate the kinetic model parameters generated for each Transcriptional Unit TU.
	 *
	 * @author Pedro Fontanarrosa
	 * @param sbolDoc the SBOL document being used
	 * @return the hash map where maps Promoters, to Proteins (or TF) and Cello Parameters
	 */
	private static HashMap<String, HashMap <String, String>> sensorPromoterParametrizationT2Collection2(BioModel targetModel, HashMap<String, HashMap <String, String>> promoterInteractions, SBOLDocument sbolDoc, HashMap<String, List<String>> Prot_2_Param, HashMap<String, HashMap <String, String>> complex2sensor2ligand, HashMap<String, String> sensorMolecules, ComponentDefinition promoter, ComponentDefinition input_molecule){

		String tau_on = "";
		String tau_off = "";
		String ymax = "";
		String ymin = "";
		String alpha = "";
		String beta = "";
		List<Annotation> Annot = promoter.getAnnotations();
		//circle through all annotations looking for Cello parameters
		for (int i = 0; i < Annot.size(); i++) {
			if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}tau_on"))) {
				tau_on = Annot.get(i).getStringValue();	
			}
			if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}tau_off"))) {
				tau_off = Annot.get(i).getStringValue();
			}
			if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}ymax"))) {
				ymax = Annot.get(i).getStringValue();
			}
			if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}ymin"))) {
				ymin = Annot.get(i).getStringValue();
			}
			if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}alpha"))) {
				alpha = Annot.get(i).getStringValue();
			}
			if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}beta"))) {
				beta = Annot.get(i).getStringValue();
			}
		}
		
		promoterInteractions.put(promoter.getDisplayId(), new HashMap <String, String>());
		//promoterInteractions.get(promoter.getDisplayId()).put("sensor", input_molecule.getDisplayId());
		promoterInteractions.get(promoter.getDisplayId()).put("sensor", "Y_" + promoter.getDisplayId().replace("p", ""));
		
		Species outputFlux = targetModel.getSBMLDocument().getModel().createSpecies();
		outputFlux.setId("Y_" + promoter.getDisplayId().replace("p", ""));
		outputFlux.setInitialAmount(0.0);
		outputFlux.setBoundaryCondition(false);
		outputFlux.setConstant(false);
		outputFlux.setHasOnlySubstanceUnits(true);
		outputFlux.setCompartment(targetModel.getSBMLDocument().getModel().getCompartment(0).getId());
		
		//targetModel.createDirPort(outputFlux.getId(), GlobalConstants.OUTPUT);
		//moduleDef.createFunctionalComponent("Y_" + promoter.getDisplayId().replace("p", ""), URI.create("http://sbols.org/v2#public"), "", GlobalConstants.OUTPUT);
		
		
		Prot_2_Param.put(input_molecule.getDisplayId(), Arrays.asList("", "", ymax, ymin, alpha, beta, tau_on, tau_off));

		complex2sensor2ligand.put(input_molecule.getDisplayId(), new HashMap <String, String>());
		complex2sensor2ligand.get(input_molecule.getDisplayId()).put("Y_" + promoter.getDisplayId().replace("p", ""),input_molecule.getDisplayId());

		sensorMolecules.put("Y_" + promoter.getDisplayId().replace("p", ""), input_molecule.getDisplayId());

		return promoterInteractions;	
	}
	
	/**
	 * This method returns a HashMap with a activator/repressor, and Cello Parameters associated with it if it has any. This
	 * will be used later to populate the kinetic model parameters generated for each Transcriptional Unit TU.
	 *
	 * @author Pedro Fontanarrosa
	 * @param sbolDoc the SBOL document being used
	 * @return the hash map where maps Promoters, to Proteins (or TF) and Cello Parameters
	 */
	private static HashMap<String, HashMap <String, String>> sensorPromoterParametrizationT2Collection(HashMap<String, HashMap <String, String>> promoterInteractions, SBOLDocument sbolDoc, HashMap<String, List<String>> Prot_2_Param, HashMap<String, HashMap <String, String>> complex2sensor2ligand, HashMap<String, String> sensorMolecules){


		for (ComponentDefinition CD: sbolDoc.getComponentDefinitions()) {
			for (Component CDcomponent : CD.getComponents()) {
				ComponentDefinition promoter = CDcomponent.getDefinition();
				if (promoter.getRoles().contains(SequenceOntology.ENGINEERED_REGION)) {

					boolean isInputPromoter = false;
					//get all the annotations of the part, which is where the cello parameters are stored as from (09/05/18). Eventually
					//the location of these parameters can change
					List<Annotation> Annot = promoter.getAnnotations();

					for (int j = 0; j < Annot.size(); j++) {
						if (Annot.get(j).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}gateType"))) {
							if (Annot.get(j).getStringValue().equals("input_sensor")) {
								isInputPromoter = true;
							}	
						}
					}
					if (isInputPromoter) {
						String tau_on = "";
						String tau_off = "";
						String ymax = "";
						String ymin = "";
						String alpha = "";
						String beta = "";
						//circle through all annotations looking for Cello parameters
						for (int i = 0; i < Annot.size(); i++) {
							if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}tau_on"))) {
								tau_on = Annot.get(i).getStringValue();	
							}
							if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}tau_off"))) {
								tau_off = Annot.get(i).getStringValue();
							}
							if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}ymax"))) {
								ymax = Annot.get(i).getStringValue();
							}
							if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}ymin"))) {
								ymin = Annot.get(i).getStringValue();
							}
							if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}alpha"))) {
								alpha = Annot.get(i).getStringValue();
							}
							if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}beta"))) {
								beta = Annot.get(i).getStringValue();
							}
						}

						for (Component part : promoter.getComponents()) {
							ComponentDefinition promoter2 = part.getDefinition();
							if (promoter2.getRoles().contains(SequenceOntology.PROMOTER)) {
								if (!promoterInteractions.containsKey(promoter2.getDisplayId())) {
									promoterInteractions.put(promoter2.getDisplayId(), new HashMap <String, String>());
									promoterInteractions.get(promoter2.getDisplayId()).put("sensor", promoter2.getDisplayId());

								} else {
									//HashMap<String, String> Sensor = new HashMap <String, String>();
									//Sensor.put("sensor", promoter2.getDisplayId());
									promoterInteractions.get(promoter2.getDisplayId()).clear();
									promoterInteractions.get(promoter2.getDisplayId()).put("sensor", promoter2.getDisplayId());
								}

								Prot_2_Param.put(promoter2.getDisplayId(), Arrays.asList("", "", ymax, ymin, alpha, beta, tau_on, tau_off));

								complex2sensor2ligand.put(promoter2.getDisplayId(), new HashMap <String, String>());
								complex2sensor2ligand.get(promoter2.getDisplayId()).put(promoter2.getDisplayId(),promoter2.getDisplayId());

								sensorMolecules.put(promoter2.getDisplayId(), promoter2.getDisplayId());
							}
						}
					}
				}
			}
		}
		return promoterInteractions;	
	}
	
	/**
	 * This method returns a HashMap with a activator/repressor, and Cello Parameters associated with it if it has any. This
	 * will be used later to populate the kinetic model parameters generated for each Transcriptional Unit TU.
	 *
	 * @author Pedro Fontanarrosa
	 * @param sbolDoc the SBOL document being used
	 * @return the hash map where maps Promoters, to Proteins (or TF) and Cello Parameters
	 */
	private static HashMap<String, List<String>> productionInteractions(SBOLDocument sbolDoc){
		
		//Since it is not possible to determine in which production reactions each Engineered Region participates in, then we first have to look through all the ER, then through 
		//all production reactions, and match them
		
		HashMap<String, List<String>> Prot_2_Param = new HashMap <String, List<String>>();
		for (ComponentDefinition CD : sbolDoc.getComponentDefinitions()) {
			if (CD.containsRole(SequenceOntology.ENGINEERED_REGION)) {
				for (Component comp : CD.getComponents()) {
					if (comp.getDefinition().containsRole(SequenceOntology.CDS)) {
						for (ModuleDefinition moduleDef : sbolDoc.getModuleDefinitions()) {
							for (Interaction interaction : moduleDef.getInteractions()) {
								if (SBOL2SBML.isProductionInteraction(interaction, moduleDef, sbolDoc)) {
									for (Participation participator : interaction.getParticipations()) {
										// use .equals() rather than == in the If statement, because .equals() compares objects and == only compares int.
										if (participator.getParticipant().getDisplayId().equals(comp.getDefinition().getDisplayId())) {
											for (Participation participator2 : interaction.getParticipations()) {
												if (participator2.containsRole(SystemsBiologyOntology.PRODUCT)) {
													String protein1 = participator2.getParticipant().getDisplayId();
													if (!Prot_2_Param.containsKey(protein1)) {
														Prot_2_Param.put(protein1, hasCelloParameters2(CD));
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}

		return Prot_2_Param;
	}

	/**
	 * Checks for Cello parameters. This method searches annotations of parts to see if there are stored any Cello parameters. 
	 *
	 * @author Pedro Fontanarrosa
	 * @param promoter The TranscriptionalUnit (TU) where, if present, Cello parameters are stored
	 * @return the hash map with all the parameters found. If there are no Cello parameters, it will return empty "" strings
	 */
	
	private static HashMap<String, String> hasCelloParameters(FunctionalComponent promoter){
		
		//Initialize the parameters we are looking for
		String n = "";
		String K = "";
		String ymax = "";
		String ymin = "";
		
		if (promoter.getDefinition() != null) {
			ComponentDefinition tuCD = promoter.getDefinition();
			for (Component comp : tuCD.getComponents()) {
				if (comp.getDefinition() != null) {
					if (comp.getDefinition().getRoles().contains(SequenceOntology.ENGINEERED_REGION)) {
						ComponentDefinition Part = comp.getDefinition();					
						//get all the annotations of the part, which is where the cello parameters are stored as from (09/05/18). Eventually
						//the location of these parameters can change
						List<Annotation> Annot = Part.getAnnotations();
						//circle through all annotations looking for Cello parameters
						for (int i = 0; i < Annot.size(); i++) {
							if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}K"))) {
								K = Annot.get(i).getStringValue();
							}
							if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}n"))) {
								n = Annot.get(i).getStringValue();
							}
							if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}ymax"))) {
								ymax = Annot.get(i).getStringValue();
							}
							if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}ymin"))) {
								ymin = Annot.get(i).getStringValue();
							}
						}
						
						for (Component comp2 : Part.getComponents()) {

							if (comp2.getDisplayId().equals("YFP")) {
								n = "Reporter";
								K = "Reporter";
								ymax = "Reporter";
								ymin = "Reporter";
							}
						}
					}
				}
			}
		}
		//Create HashMap where to store all parameters. It will return empty "" string values if it hasn't found any Cello parameters.
		HashMap<String, String> celloParameters = new HashMap<String, String>();
				
		celloParameters.put("n", n);
		celloParameters.put("K", K);
		celloParameters.put("ymax", ymax);
		celloParameters.put("ymin", ymin);
		return celloParameters;
	}
	
	private static void CreateMeasureClasses(SBOLDocument sbolDoc) throws SBOLValidationException{
		
		//Initialize the parameters we are looking for
		String n = "";
		String K = "";
		String ymax = "";
		String ymin = "";
		for(ModuleDefinition topModule : sbolDoc.getModuleDefinitions()) {
			for (FunctionalComponent promoter : topModule.getFunctionalComponents()) {
				if (promoter.getDefinition() != null) {
					ComponentDefinition tuCD = promoter.getDefinition();
					for (Component comp: tuCD.getComponents()) {
						ComponentDefinition tuCDcomp = comp.getDefinition();
						if (tuCDcomp != null) {
							if (tuCDcomp.getRoles().contains(SequenceOntology.ENGINEERED_REGION)) {
								//get all the annotations of the part, which is where the cello parameters are stored as from (09/05/18). Eventually
								//the location of these parameters can change
								List<Annotation> Annot = tuCDcomp.getAnnotations();
								//circle through all annotations looking for Cello parameters
								for (int i = 0; i < Annot.size(); i++) {
									if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}K"))) {
										K = Annot.get(i).getStringValue();
										promoter.createMeasure("K", Annot.get(i).getDoubleValue(), URI.create("http://www.ontology-of-units-of-measure.org/resource/om-2/Measure"));
									}
									if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}n"))) {
										n = Annot.get(i).getStringValue();
										promoter.createMeasure("n", Annot.get(i).getDoubleValue(), URI.create("http://www.ontology-of-units-of-measure.org/resource/om-2/Measure"));
									}
									if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}ymax"))) {
										ymax = Annot.get(i).getStringValue();
										promoter.createMeasure("ymax", Annot.get(i).getDoubleValue(), URI.create("http://www.ontology-of-units-of-measure.org/resource/om-2/Measure"));
									}
									if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}ymin"))) {
										ymin = Annot.get(i).getStringValue();
										promoter.createMeasure("ymin", Annot.get(i).getDoubleValue(), URI.create("http://www.ontology-of-units-of-measure.org/resource/om-2/Measure"));
										//promoter.createMeasure(displayId, numericalValue, unit);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	
	/**
	 * Checks for cello parameters 2.
	 *
	 * @author Pedro Fontanarrosa
	 * @param promoter - The promoter or TU that we want to check if it has cello parameters
	 * @return the array list with the parameters found, if any
	 */
	private static ArrayList<String> hasCelloParameters2(ComponentDefinition promoter){
		
		//Initialize the parameters we are looking for
		String n = "";
		String K = "";
		String ymax = "";
		String ymin = "";
		String alpha = "";
		String beta = "";
		String TauON = "";
		String TauOFF = "";
		
		if (promoter != null) {
			if (promoter.getRoles().contains(SequenceOntology.ENGINEERED_REGION)) {
				
				//get all the annotations of the part, which is where the cello parameters are stored as from (09/05/18). Eventually
				//the location of these parameters can change
				List<Annotation> Annot = promoter.getAnnotations();
					//circle through all annotations looking for Cello parameters
					for (int i = 0; i < Annot.size(); i++) {
						if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}K"))) {
							K = Annot.get(i).getStringValue();
						}
						if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}n"))) {
							n = Annot.get(i).getStringValue();
						}
						if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}ymax"))) {
							ymax = Annot.get(i).getStringValue();
						}
						if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}ymin"))) {
							ymin = Annot.get(i).getStringValue();
						}
						if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}alpha"))) {
							alpha = Annot.get(i).getStringValue();
						}
						if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}beta"))) {
							beta = Annot.get(i).getStringValue();
						}
						if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}tau_on"))) {
							TauON = Annot.get(i).getStringValue();
						}
						//TODO PEDRO CHECK IF ANNOTATION IS REALLY TAUON OR HOW.
						if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}tau_off"))) {
							TauOFF = Annot.get(i).getStringValue();
						}
					}
			}
		}
		//Create list where to store all parameters. It will return empty "" string values if it hasn't found any Cello parameters.
		ArrayList<String> CelloParameters2 = new ArrayList<String>();
		CelloParameters2.add(n);
		CelloParameters2.add(K);
		CelloParameters2.add(ymax);
		CelloParameters2.add(ymin);
		CelloParameters2.add(alpha);
		CelloParameters2.add(beta);
		CelloParameters2.add(TauON);
		CelloParameters2.add(TauOFF);
		
		return CelloParameters2;
	}
	
	/**
	 * Removes the complex formation interaction (from sensor protein and ligand) from the document. This will remove the need to create these interactions for the SBML document
	 *
	 * @author Pedro Fontanarrosa
	 * @param moduleDef the module def
	 * @throws SBOLValidationException the SBOL validation exception
	 */
	private static HashMap<String, HashMap <String, String>> mapSensorToLigand(SBOLDocument sbolDoc) throws SBOLValidationException{
		
		HashMap<String, HashMap <String, String>> complex2ligand2sensor = new HashMap<String, HashMap <String, String>>();
		//HashMap<String, List<String>> Prot_2_Param = new HashMap <String, List<String>>();		
		for (ModuleDefinition resultMD : sbolDoc.getModuleDefinitions()) {
			for (Interaction interact : resultMD.getInteractions()) {
				if (SBOL2SBML.isComplexFormationInteraction(interact, resultMD, sbolDoc)) {
					ComponentDefinition complex = null;
					ComponentDefinition ligand = null;
					ComponentDefinition sensor = null;
					List<Participation> ligands = new LinkedList<Participation>();
					HashMap <String, String> ligand2sensor = new HashMap <String, String>();
					for (Participation partici: interact.getParticipations()) {
						// COMPLEX
						if (partici.containsRole(SystemsBiologyOntology.PRODUCT) ||
								partici.containsRole(URI.create("http://identifiers.org/biomodels.sbo/SBO:0000253"))) {
							complex = partici.getParticipantDefinition();
						} else if (partici.containsRole(SystemsBiologyOntology.REACTANT) ||
								partici.containsRole(URI.create("http://identifiers.org/biomodels.sbo/SBO:0000280"))) {
							if (SBOL2SBML.isSmallMoleculeDefinition(partici.getParticipantDefinition())) {
								sensor = partici.getParticipantDefinition();
							} else {
								ligand = partici.getParticipantDefinition();
							}
						}
					}
					ligand2sensor.put(ligand.getDisplayId(), sensor.getDisplayId());
					complex2ligand2sensor.put(complex.getDisplayId(), ligand2sensor);
				}
			}
		}
		return complex2ligand2sensor;
	}

	
	
	/**
	 * Removes the complex formation interaction (from sensor protein and ligand) from the document. This will remove the need to create these interactions for the SBML document
	 *
	 * @author Pedro Fontanarrosa
	 * @param moduleDef the module def
	 * @param sensorMolecules the sensor molecules
	 * @throws SBOLValidationException the SBOL validation exception
	 */
	private static void removeSensorInteractios(ModuleDefinition moduleDef, HashMap<String, String> sensorMolecules) throws SBOLValidationException{
		

		for (Interaction interact : moduleDef.getInteractions()) {
			for (Participation part :interact.getParticipations()){
				for (String com_prot : sensorMolecules.keySet()) {
					if (part.getDisplayId().equals(com_prot)) {
						moduleDef.removeInteraction(interact);
						break;
					}
				}
			}
		}
	}

	
	/**
	 * Convert the given SBOL MapsTo object into SBML replacement for the given remote and local SBOL ModuleDefinition.
	 * Annotation will be performed on SBML replacement for any SBOL MapsTo information that can't be mapped directly.
	 * 
	 * @author Pedro Fontanarrosa
	 * @param mapping - The SBOL MapsTo to be converted to SBML replacement.  
	 * @param subModule - The SBOL Module that is considered to be the remote model in the replacement object.
	 * @param moduleDef - The SBOL ModuleDefinition that is considered to be the local model in the replacement object.
	 * @param sbolDoc - The SBOL Document that contains the SBOL objects to convert to SBML replacement.
	 * @param subTargetModel - The SBML remote model that contain the SBML replacement.
	 * @param targetModel - The SBML local model that contain the SBML replacement.
	 */
	private static void generateReplacement(FunctionalComponent ligand, Module subModule, ModuleDefinition moduleDef, 
			SBOLDocument sbolDoc, BioModel subTargetModel, BioModel targetModel) {
		ModuleDefinition subModuleDef = sbolDoc.getModuleDefinition(subModule.getDefinitionURI()); 
		//FunctionalComponent remoteSpecies = subModuleDef.getFunctionalComponent(ligand.getDisplayId());
		FunctionalComponent localSpecies = moduleDef.getFunctionalComponent(ligand.getDisplayId());

		Species localSBMLSpecies = targetModel.getSBMLDocument().getModel().getSpecies(SBOL2SBML.getDisplayID(localSpecies));
		Species remoteSBMLSpecies = targetModel.getSBMLCompModel().getSubmodel(SBOL2SBML.getDisplayID(subModule)).getModel().getSpecies(SBOL2SBML.getDisplayID(localSpecies));
		//Species remoteSBMLSpecies = subTargetModel.getSBMLDocument().getModel().getSpecies(SBOL2SBML.getDisplayID(localSpecies));
		//Species remoteSBMLSpecies = targetModel.getSBMLCompModel().getSubmodel(SBOL2SBML.getDisplayID(subModule)).get
		
/*		Port port = subTargetModel.getPortByIdRef(SBOL2SBML.getDisplayID(localSpecies));
		if (port==null) {
			System.err.println("Cannot find "+ SBOL2SBML.getDisplayID(localSpecies));
			//return;
		}*/
		
		String port = "input__" + SBOL2SBML.getDisplayID(localSpecies);
		
		Submodel subModel = targetModel.getSBMLCompModel().getSubmodel(SBOL2SBML.getDisplayID(subModule));
		
		SBMLutilities.addReplacement(localSBMLSpecies, subModel, SBOL2SBML.getDisplayID(subModule), port, "(none)", 
				new String[]{""}, new String[]{""}, new String[]{""}, new String[]{""}, false);

		// Annotate SBML replacement with SBOL maps-to
		CompSBasePlugin compSBML = SBMLutilities.getCompSBasePlugin(localSBMLSpecies);
		SBMLutilities.setDefaultMetaID(targetModel.getSBMLDocument(), compSBML.getReplacedElement(compSBML.getNumReplacedElements() - 1), 1);
		//SBOL2SBML.annotateReplacement(compSBML.getReplacedElement(compSBML.getNumReplacedElements() - 1), mapping);
	}

	/**
	 * Creates the appropriate replacements for promoter fluxes in the new Flow Model of Cello.
	 * 
	 * @author Pedro Fontanarrosa
	 * @param mapping - The SBOL MapsTo to be converted to SBML replacement.  
	 * @param subModule - The SBOL Module that is considered to be the remote model in the replacement object.
	 * @param moduleDef - The SBOL ModuleDefinition that is considered to be the local model in the replacement object.
	 * @param sbolDoc - The SBOL Document that contains the SBOL objects to convert to SBML replacement.
	 * @param subTargetModel - The SBML remote model that contain the SBML replacement.
	 * @param targetModel - The SBML local model that contain the SBML replacement.
	 */
	private static void generateReplacementForFlow(Species ligand, Module subModule, ModuleDefinition moduleDef, 
			SBOLDocument sbolDoc, BioModel subTargetModel, BioModel targetModel) {
		ModuleDefinition subModuleDef = sbolDoc.getModuleDefinition(subModule.getDefinitionURI()); 
		//FunctionalComponent remoteSpecies = subModuleDef.getFunctionalComponent(ligand.getDisplayId());
		//FunctionalComponent localSpecies = moduleDef.getFunctionalComponent(ligand.getDisplayId());
		
		
		Species localSBMLSpecies = targetModel.getSBMLDocument().getModel().getSpecies(ligand.getId());
		Species remoteSBMLSpecies = targetModel.getSBMLCompModel().getSubmodel(SBOL2SBML.getDisplayID(subModule)).getModel().getSpecies(ligand.getId());
		//Species remoteSBMLSpecies = subTargetModel.getSBMLDocument().getModel().getSpecies(SBOL2SBML.getDisplayID(localSpecies));
		//Species remoteSBMLSpecies = targetModel.getSBMLCompModel().getSubmodel(SBOL2SBML.getDisplayID(subModule)).get
		
		if (!localSBMLSpecies.getId().equals("aTc") && !localSBMLSpecies.getId().equals("IPTG") && !localSBMLSpecies.getId().equals("Ara")) {
		
		//String port = "input__" + SBOL2SBML.getDisplayID(localSpecies);
		//Port port = targetModel.getPortByIdRef(remoteSBMLSpecies.getId());
		Port port = subTargetModel.getPortByIdRef(remoteSBMLSpecies.getId());
		
		if (port != null) {
			Submodel subModel = targetModel.getSBMLCompModel().getSubmodel(SBOL2SBML.getDisplayID(subModule));
			
			if (port.getId().contains("input__")) {
				SBMLutilities.addReplacement(localSBMLSpecies, subModel, SBOL2SBML.getDisplayID(subModule), port.getId(), "(none)", 
						new String[]{""}, new String[]{""}, new String[]{""}, new String[]{""}, false);
			}
			if (port.getId().contains("output__")) {
				SBMLutilities.addReplacedBy(localSBMLSpecies, SBOL2SBML.getDisplayID(subModule), port.getId(), new String[]{""}, 
						new String[]{""}, new String[]{""}, new String[]{""});
			}
		}

//		SBMLutilities.addReplacement(localSBMLSpecies, subModel, SBOL2SBML.getDisplayID(subModule), port.getId(), "(none)", 
//				new String[]{""}, new String[]{""}, new String[]{""}, new String[]{""}, false);
//		SBMLutilities.addReplacedBy(localSBMLSpecies, getDisplayID(subModule), port.getId(), new String[]{""}, 
//				new String[]{""}, new String[]{""}, new String[]{""});
		}

		// Annotate SBML replacement with SBOL maps-to
		//CompSBasePlugin compSBML = SBMLutilities.getCompSBasePlugin(localSBMLSpecies);
		//SBMLutilities.setDefaultMetaID(targetModel.getSBMLDocument(), compSBML.getReplacedElement(compSBML.getNumReplacedElements() - 1), 1);
		//SBOL2SBML.annotateReplacement(compSBML.getReplacedElement(compSBML.getNumReplacedElements() - 1), mapping);
	}


	/**
	 * Generate flow production rxns.
	 * @author Pedro Fontanarrosa
	 *
	 * @param promoter the promoter
	 * @param partici the partici
	 * @param productions the productions
	 * @param activations the activations
	 * @param repressions the repressions
	 * @param products the products
	 * @param transcribed the transcribed
	 * @param activators the activators
	 * @param repressors the repressors
	 * @param moduleDef the module def
	 * @param sbolDoc the sbol doc
	 * @param targetModel the target model
	 * @param celloParameters the cello parameters
	 * @param promoterInteractions the promoter interactions
	 * @throws BioSimException the bio sim exception
	 */
	private static void generateFlowProductionRxns(FunctionalComponent promoter, List<Participation> partici, List<Interaction> productions,
			List<Interaction> activations, List<Interaction> repressions,
			List<Participation> products, List<Participation> transcribed, List<Participation> activators, 
			List<Participation> repressors, ModuleDefinition moduleDef, SBOLDocument sbolDoc, BioModel targetModel, HashMap<String, List<String>> celloParameters, HashMap<String, HashMap <String, String>> promoterInteractions, HashMap<String, HashMap <String, String>> complex2sensor2ligand) throws BioSimException {
		
		//This method should create a mRNA species for each promoter, since this species are not present in the SBOLdocument returned by VPR
		// collect data, create mRNA species, mRNA degradation reaction, mRNA Production reaction, TF production reaction
		
		boolean sensor_gate = false;
		boolean reporter_gate = false;
		ComponentDefinition TUCD = promoter.getDefinition();
		for (Component comp : TUCD.getComponents()) {
			if (comp.getDefinition().getRoles().contains(SequenceOntology.ENGINEERED_REGION)) {
				ComponentDefinition Part = comp.getDefinition();
				List<Annotation> Annot = Part.getAnnotations();
				String type = "";
				for (int i = 0; i < Annot.size(); i++) {
					if (Annot.get(i).getQName().toString().equals(new String("{http://cellocad.org/Terms/cello#}gateType"))) {
						type = Annot.get(i).getStringValue();
						if (type.equals("output_reporter")) {
							reporter_gate = true;
						} else if(type.equals("input_sensor")) {
							sensor_gate = true;
						}
					}
				}
			}
		}	
		
	
		// Create reaction ID string using all the productions listed with this Transcriptional Unit (TU).
		String rxnID = "";
		String product = "";
		
		if (productions == null && sensor_gate) {
			if (activations != null) {
				for (Interaction activation : activations) {
					for (Participation parti : activation.getParticipations()){
						ComponentDefinition part = parti.getParticipantDefinition();
							if (complex2sensor2ligand.containsKey(part.getDisplayId())) {
									HashMap<String, String> protein2ligand = complex2sensor2ligand.get(part.getDisplayId());
									for (String repressor : protein2ligand.keySet()) {
										//repressor = repressor.replace("Y_", "");
										rxnID = repressor;
										product = repressor;
									}

							}
						}
					}
				} else if (repressions != null) {
					
				} 
			}
			
		if (products!=null) {
			for (Participation production : products) {
				if (rxnID.equals("")) {
					rxnID = SBOL2SBML.getDisplayID(production);
					product = rxnID;
					rxnID = rxnID.replace("_protein", "");

				} else {
					rxnID = rxnID + "_" + SBOL2SBML.getDisplayID(production);
				}
			}
		} else {
			throw new BioSimException("The Transcriptional Unit" + promoter.getDisplayId() + "you are trying to model doesn't have any products", "Error while generating model");
			}

		
		//create reaction ID for the output production flow for each gate,
		//which is the display ID's of the products separated by underscores, 
		//check if it's unique using SMBLUtilities.getUniqueSBMLId()
		String rxnIDSD = "";
		if (reporter_gate) {
			rxnIDSD = rxnID;
			rxnIDSD = SBMLutilities.getUniqueSBMLId(rxnIDSD, targetModel);
		} else {
			rxnIDSD = "dy_" + rxnID;
			rxnIDSD = SBMLutilities.getUniqueSBMLId(rxnIDSD, targetModel);
		}

		
		// Count promoters
		int promoterCnt = 0;
		if (promoter.getDefinition() != null) {
			ComponentDefinition tuCD = promoter.getDefinition();
			for (Component comp : tuCD.getComponents()) {
				if (comp.getDefinition() != null) {
					if (comp.getDefinition().getRoles().contains(SequenceOntology.PROMOTER)||
							comp.getDefinition().getRoles().contains(SequenceOntology.OPERATOR)) {
						promoterCnt++;
					}
				}
			}
		}
		
		//Check if there are two tandem promoters. If there are, then list them in order to have the method createCelloSDProductionReactions
		//create roadblocking effects for it. 
		List<String> ordered_promoters = new ArrayList<>();
		if (promoterCnt == 2) {
			if (promoter.getDefinition() != null) {
				ComponentDefinition tuCD = promoter.getDefinition();
				for (SequenceConstraint SC : tuCD.getSequenceConstraints()) {
					if (SC.getRestriction().toString().equals("precedes")) {
						ComponentDefinition object = SC.getObjectDefinition();
						ComponentDefinition subject = SC.getSubjectDefinition();
						if (object.getRoles().contains(SequenceOntology.PROMOTER) || object.getRoles().contains(SequenceOntology.OPERATOR) && subject.getRoles().contains(SequenceOntology.PROMOTER) || subject.getRoles().contains(SequenceOntology.OPERATOR)){
							ordered_promoters.add(SBOL2SBML.getDisplayID(subject));
							ordered_promoters.add(SBOL2SBML.getDisplayID(object));
						}
					}
				}
			}
		}
		
		// each promoter will have a set of interactions
		List<String> promoters = new ArrayList<>();
		for (int i = 0; i < promoterCnt; i++) {
			
			// if only one promoter in TU, then promoterId will be the name of the TU
			String promoterId = SBOL2SBML.getDisplayID(promoter);
			
			// Use the id of the actual promoter
			if (promoterCnt >= 1) {
				if (promoter.getDefinition() != null) {
					ComponentDefinition tuCD = promoter.getDefinition();
					int j = 0;
					for (Component comp : tuCD.getComponents()) {
						if (comp.getDefinition() != null) {
							if (comp.getDefinition().getRoles().contains(SequenceOntology.PROMOTER)) {
								if (i==j) {
									promoterId = SBOL2SBML.getDisplayID(comp.getDefinition());
									promoters.add(promoterId);
									break;
								}
								j++;
							}
						}
					}
				}
			}
		}
				
		//Create the gate flow production reaction species for each TU.
		Species gate_flow = targetModel.getSBMLDocument().getModel().createSpecies();
		if(reporter_gate) {
			gate_flow.setId(rxnID);
		} else if (sensor_gate) {
			gate_flow.setId(rxnID);
		} else {
			gate_flow.setId("Y_" + rxnID);
		}
		
		
		
		//This parameter will store the value of the difference between the steady-state and the current state of the gate flow
		Parameter gateSS = targetModel.getSBMLDocument().getModel().createParameter();
		gateSS.setId(rxnID + "_SS");
		gateSS.setValue(0.0);
		gateSS.setConstant(false);
		
		Reaction gateDynamics = targetModel.createFlowProductionReactions(gate_flow, rxnIDSD, product, promoter.getDisplayId(), reporter_gate, sensor_gate, complex2sensor2ligand, false, null, targetModel, promoters, promoterInteractions);

		AssignmentRule steadyState = targetModel.createFlowSteadyStateRule(gateSS, rxnIDSD, promoter.getDisplayId(), false, null, targetModel, promoters, promoterInteractions);
		ASTNode math = targetModel.createFlowSteadyState(gateDynamics, product, reporter_gate, sensor_gate, targetModel, celloParameters, promoterInteractions, promoters, ordered_promoters, complex2sensor2ligand);
		steadyState.setMath(math);

		
		//TODO PEDRO: it would be nice to fix sbml annotations to link it to the sbol document
		
		//Update the Kinetic Law using Sins's Paper for dynamic modeling using Cello Parameters. 
		gateDynamics.getKineticLaw().setMath(SBMLutilities.myParseFormula(BioModel.createFlowDynamic(gateDynamics, gateSS, product, reporter_gate, gate_flow, celloParameters, promoterInteractions, targetModel, promoters, ordered_promoters)));		
	}
}
