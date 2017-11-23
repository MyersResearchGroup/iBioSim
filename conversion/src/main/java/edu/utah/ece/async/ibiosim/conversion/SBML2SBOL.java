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
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.CompSBMLDocumentPlugin;
import org.sbml.jsbml.ext.comp.CompSBasePlugin;
import org.sbml.jsbml.ext.comp.ExternalModelDefinition;
import org.sbml.jsbml.ext.comp.ReplacedBy;
import org.sbml.jsbml.ext.comp.ReplacedElement;
import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.EDAMOntology;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.Module;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.RefinementType;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.Sequence;
import org.sbolstandard.core2.SequenceOntology;
import org.sbolstandard.core2.SystemsBiologyOntology;

import edu.utah.ece.async.ibiosim.dataModels.biomodel.annotation.AnnotationUtility;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.parser.BioModel;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.util.SBMLutilities;
import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.ibiosim.dataModels.util.IBioSimPreferences;

/**
 * Perform conversion from SBML to SBOL. 
 *
 * @author Tramy Nguyen
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SBML2SBOL {

	private static String VERSION = "1";

	
	/**
	 * Load the given set of SBOL files for the converter to reference an existing object taken 
	 * from the set of SBOL files rather than constructing a new SBOL object. 
	 * 
	 * @param sbol_Library - The global SBOL document that contains all SBOL objects referenced in conversion. 
	 * @param sbolFilePaths - The set of SBOL files to use for the conversion to reference from.
	 * @param sbolURIPrefix - The SBOL URI prefix that the global SBOLDocument will contain
	 * @return True if all referenced SBOL files were loaded successfully. False otherwise.
	 * @throws FileNotFoundException - Unable to find referenced SBOL files. 
	 * @throws SBOLValidationException - SBOL Exception occurred when loading referenced SBOL files
	 * @throws IOException - Input Exception occurred. 
	 * @throws SBOLConversionException - SBOL conversion exception occurred when reading in the referenced SBOL files.
	 */
	private static boolean loadSBOLFiles(SBOLDocument sbol_Library, HashSet<String> sbolFilePaths, String sbolURIPrefix) throws FileNotFoundException, SBOLValidationException, IOException, SBOLConversionException 
	{
		for (String filePath : sbolFilePaths) 
		{
			SBOLReader.setURIPrefix(sbolURIPrefix);
			SBOLDocument sbolDoc = SBOLReader.read(new FileInputStream(filePath));

			if (sbolDoc != null) 
			{
				for(ComponentDefinition c : sbolDoc.getComponentDefinitions())
				{
					if(sbol_Library.getComponentDefinition(c.getIdentity()) == null) 
					{
						sbol_Library.createCopy(c);
					}
				}
				for(Sequence c : sbolDoc.getSequences())
				{
					if(sbol_Library.getSequence(c.getIdentity()) == null) 
					{
						sbol_Library.createCopy(c);
						
					}
				}
			} 
			else{
				return false;
			}
		}
		return true;
	}

	
	/**
	 * Convert the given SBML Document to an SBOL Document.
	 * 
	 * @param sbolDoc - The SBOLDocument to store the converted SBML content to.
	 * @param externalSBMLPath - The full path of external SBML files to be referenced in the SBML2SBOL conversion
	 * @param sbmlDoc - The SBML document to be converted.
	 * @param fileName - The name of the input SBML file
	 * @param sbolFiles - The set of SBOL files to use for the conversion to reference from. If no ref_sbolInputfilePath is given, then null should be set to indicate no referencing SBOL files are given for conversion.
	 * @param sbolURIPrefix - The URI prefix to be set on the SBOLDocument.
	 * @throws SBOLValidationException - SBOL Exception occurred when loading referenced SBOL files
	 * @throws XMLStreamException - Invalid XML file.
	 * @throws IOException - Input Exception occurred. 
	 * @throws SBOLConversionException - SBOL conversion exception occurred when reading in the referenced SBOL files.
	 */
	public static void convert_SBML2SBOL(SBOLDocument sbolDoc, String externalSBMLPath, SBMLDocument sbmlDoc, String fileName, HashSet<String> sbolFiles, String sbolURIPrefix) throws SBOLValidationException, XMLStreamException, IOException, SBOLConversionException {

		//Load all SBOL files given from user to store reference SBOL objects
		SBOLDocument sbol_Library = new SBOLDocument();
		sbol_Library.setDefaultURIprefix(sbolURIPrefix);
		if(sbolFiles != null && !sbolFiles.isEmpty()){
			SBML2SBOL.loadSBOLFiles(sbol_Library, sbolFiles, sbolURIPrefix);
		}

		sbolDoc.setDefaultURIprefix(sbolURIPrefix);
		sbolDoc.setComplete(false);

		parseSBMLModel("file:" + fileName, externalSBMLPath, sbmlDoc, sbmlDoc.getModel(), sbolDoc, sbol_Library); 
	}
	
	/**
	 * Convert the specified SBML model to its equivalent SBOL ModuleDefinition.
	 * All SBML SBase referencing this SBML model will be converted to SBOL data object.
	 * In this case, all SBML species will be converted to SBOL ComponentDefinition and FunctionalComponent.
	 * All SBML reactions will be converted to SBOL Interactions. 
	 * All SBML replacements will be converted to SBOL MapsTo.
	 * 
	 * @param source - reference to the source file for the given SBML model
	 * @param externalSBMLPath - The full path of external SBML files to be referenced in the SBML2SBOL conversion
	 * @param sbmlDoc - The SBML Document to be converted to SBOL
	 * @param model - The SBML model to be converted to SBOL ModuleDefinition
	 * @param sbolDoc - The SBOL document to store all SBOL objects converted from the SBML Document
	 * @param sbol_Library - The global SBOL document that contains all SBOL objects referenced in conversion. 
	 * @throws SBOLValidationException - SBOL Exception occurred when loading referenced SBOL files
	 * @throws XMLStreamException - Invalid XML file.
	 * @throws IOException - Input Exception occurred. 
	 */
	private static void parseSBMLModel(String source, String externalSBMLPath, SBMLDocument sbmlDoc, Model model, SBOLDocument sbolDoc, SBOLDocument sbol_Library) throws SBOLValidationException, XMLStreamException, IOException 
	{
		//String modelId = model.getId();
		URI sourceURI  = URI.create(source);
		URI LANGUAGE  = EDAMOntology.SBML;
		URI FRAMEWORK = SystemsBiologyOntology.DISCRETE_FRAMEWORK;

		org.sbolstandard.core2.Model sbolModel = sbolDoc.getModel(model.getId() + "_model", VERSION);
		if (sbolModel!=null) {
			sbolDoc.removeModel(sbolModel);
		}
		
		sbolModel = sbolDoc.createModel(model.getId() + "_model", VERSION, sourceURI, LANGUAGE, FRAMEWORK);

		String identityStr  = model.getId() + "_md";
		ModuleDefinition moduleDef = sbolDoc.getModuleDefinition(identityStr, VERSION);
		if (moduleDef!=null) {
			sbolDoc.removeModuleDefinition(moduleDef);
		}
		moduleDef = sbolDoc.createModuleDefinition(identityStr, VERSION);
		moduleDef.addModel(sbolModel);

		for (int i = 0; i < model.getSpeciesCount(); i++) 
		{
			Species species = model.getSpecies(i);
			ComponentDefinition compDef = setComponentDefinition(sbol_Library, sbolDoc, model, species);
			setFunctionalComponent(sbmlDoc, model, moduleDef, compDef, species);
		}

		for (int i = 0; i < model.getReactionCount(); i++) 
		{
			Reaction reaction = model.getReaction(i);

			if(BioModel.isProductionReaction(reaction))
			{
				// if production reaction, then you want to examine the modifiers, and create interactions for 
				// each modifier that is a repressor from this species to the promoter
				parseProductionReaction(moduleDef, reaction);
			}
			else if(BioModel.isComplexReaction(reaction))
			{	
				// if complex reaction, then create on interaction
				parseComplexReaction(moduleDef, reaction);
			}
			else if(BioModel.isDegradationReaction(reaction))
			{
				// if degradation reaction, then create an interaction
				parseDegradationReaction(moduleDef, reaction);
			}
			else 
			{
				parseBiochemicalReaction(moduleDef, reaction);
			}
		}

		extractSubModels(source, externalSBMLPath, sbmlDoc, sbol_Library, sbolDoc, moduleDef, model);
	}
	

	/**
	 * Convert the given SBML Biochemical Reaction to its equivalent SBOL Interaction and participants.
	 * 
	 * @param moduleDef - The SBOL ModuleDefinition the converted SBOL Interaction will be stored in.
	 * @param reaction - The SBML reaction to be converted into SBOL Interaction.
	 * @throws SBOLValidationException - SBOL validation exception while creating an SBOL object for SBML2SBOL conversion.
	 */
	private static void parseBiochemicalReaction(ModuleDefinition moduleDef, Reaction reaction) throws SBOLValidationException{
		
		Set<URI> types = new HashSet<URI>();
		types.add(SystemsBiologyOntology.BIOCHEMICAL_REACTION);

		Interaction inter = moduleDef.createInteraction(reaction.getId(), types);

		int j = 0;
		for(SpeciesReference reactant : reaction.getListOfReactants())
		{
			Set<URI> roles_r = new HashSet<URI>();
			roles_r.add(SystemsBiologyOntology.REACTANT);
			inter.createParticipation(reactant.getSpecies()+"_r"+j, reactant.getSpecies(),roles_r);
			j++;
		}
		j = 0;
		for(ModifierSpeciesReference modifier : reaction.getListOfModifiers())
		{
			Set<URI> roles_r = new HashSet<URI>();
			roles_r.add(SystemsBiologyOntology.MODIFIER);
			inter.createParticipation(modifier.getSpecies()+"_m"+j, modifier.getSpecies(),roles_r);
			j++;
		}
		j = 0;
		for(SpeciesReference product : reaction.getListOfProducts())
		{
			// create participation from product.getSpecies() as type product
			Set<URI> roles_p = new HashSet<URI>();
			roles_p.add(SystemsBiologyOntology.PRODUCT);
			inter.createParticipation(product.getSpecies()+"_p"+j, product.getSpecies(),roles_p);
			j++;
		}
	}

	/**
	 * Creates a copy of an SBOL ComponentDefintion from the given ComponentDefinition's Component if it does not already exist within the given SBOL Document.
	 * 
	 * @param sbolDoc - The SBOL Document that will store the created ComponentDefintion.
	 * @param cd - The ComponentDefinition's Component to be created.
	 * @throws SBOLValidationException - SBOL validation exception while creating an SBOL object for SBML2SBOL conversion.
	 */
	private static void recurseComponentDefinition(SBOLDocument sbolDoc, ComponentDefinition cd) throws SBOLValidationException {
		for (org.sbolstandard.core2.Component comp : cd.getComponents()) {
			if (sbolDoc.getComponentDefinition(comp.getDefinitionURI()) == null) {
				ComponentDefinition compDef = comp.getDefinition();
				sbolDoc.createCopy(compDef);

				for (Sequence sequence : compDef.getSequences()) {
					if (sbolDoc.getSequence(sequence.getIdentity()) == null) {
						sbolDoc.createCopy(sequence);

					}
				}
				recurseComponentDefinition(sbolDoc,compDef);
			}
		}
	}


	/**
	 * Convert an SBOL ComponentDefinition from SBML Species.
	 * 
	 * @param sbol_Library - The global SBOL document that contains all SBOL objects referenced in conversion. 
	 * @param sbolDoc - The SBOL Document that will store the ComponentDefinition.
	 * @param model - The SBML model that contains the SBML species.
	 * @param species - The SBML Species to be converted to SBOL ComponentDefinition.
	 * @return The ComponentDefintion that was created.
	 * @throws SBOLValidationException - SBOL validation exception while creating an SBOL object for SBML2SBOL conversion.
	 */
	private static ComponentDefinition setComponentDefinition(SBOLDocument sbol_Library, SBOLDocument sbolDoc, Model model, Species species) throws SBOLValidationException
	{
		String compDef_identity =  model.getId() + "__" + species.getId();

		Set<URI> compDef_type = new HashSet<URI>();
		Set<URI> compDef_role = new HashSet<URI>();
		ComponentDefinition compDef = null;

		if (BioModel.isPromoterSpecies(species)) 
		{
			List<URI> sbolURIs = new LinkedList<URI>();
			//String sbolStrand = 
			AnnotationUtility.parseSBOLAnnotation(species, sbolURIs);
			if (sbolURIs.size()>0) {
				// TODO: need to figure out what to do when size is greater than 1
				compDef = sbol_Library.getComponentDefinition(sbolURIs.get(0));
				if (compDef != null) {
					if (sbolDoc.getComponentDefinition(compDef.getIdentity())==null) {
						sbolDoc.createCopy(compDef);
					}

					for (Sequence sequence : compDef.getSequences()) {
						if (sbolDoc.getSequence(sequence.getIdentity())==null) {
							sbolDoc.createCopy(sequence);
						}
					}
					recurseComponentDefinition(sbolDoc,compDef);
					return compDef;
				}
			}
			Reaction production = BioModel.getProductionReaction(species.getId(),model);
			if (production!=null) {
				sbolURIs = new LinkedList<URI>();
				//sbolStrand = 
				AnnotationUtility.parseSBOLAnnotation(production, sbolURIs);
				if (sbolURIs.size()>0) {
					compDef = sbol_Library.getComponentDefinition(sbolURIs.get(0));
					if (compDef != null) {
						if (sbolDoc.getComponentDefinition(compDef.getIdentity())==null) {
							sbolDoc.createCopy(compDef);
						}

						for (Sequence sequence : compDef.getSequences()) {
							if (sbolDoc.getSequence(sequence.getIdentity())==null) {
								sbolDoc.createCopy(sequence);

							}
						}
						recurseComponentDefinition(sbolDoc,compDef);
						return compDef;
					}
				}
			}
			compDef_type.add(ComponentDefinition.DNA);
			compDef_role.add(SequenceOntology.PROMOTER);
		} 
		else 
		{
			List<URI> sbolURIs = new LinkedList<URI>();

			AnnotationUtility.parseSBOLAnnotation(species, sbolURIs);
			if (sbolURIs.size()>0) {

				compDef = sbol_Library.getComponentDefinition(sbolURIs.get(0));
				if (compDef != null) {
					if (sbolDoc.getComponentDefinition(compDef.getIdentity())==null) {
						sbolDoc.createCopy(compDef);
					}

					for (Sequence sequence : compDef.getSequences()) {
						if (sbolDoc.getSequence(sequence.getIdentity())==null) {
							sbolDoc.createCopy(sequence);

						}
					}
					recurseComponentDefinition(sbolDoc,compDef);
					return compDef;
				}
			}
			if (species.isSetSBOTerm()) {
				if (species.getSBOTermID().equals(GlobalConstants.SBO_DNA) ||
						species.getSBOTermID().equals(GlobalConstants.SBO_DNA_SEGMENT)) {
					compDef_type.add(ComponentDefinition.DNA);
				} else if (species.getSBOTermID().equals(GlobalConstants.SBO_RNA) ||
						species.getSBOTermID().equals(GlobalConstants.SBO_RNA_SEGMENT)) {
					compDef_type.add(ComponentDefinition.RNA);
				} else if (species.getSBOTermID().equals(GlobalConstants.SBO_PROTEIN)) {
					compDef_type.add(ComponentDefinition.PROTEIN);
				} else if (species.getSBOTermID().equals(GlobalConstants.SBO_NONCOVALENT_COMPLEX) ||
						SBMLutilities.sbo.isDescendantOf(species.getSBOTermID(), GlobalConstants.SBO_NONCOVALENT_COMPLEX)) {
					compDef_type.add(ComponentDefinition.COMPLEX);
				} else if (species.getSBOTermID().equals(GlobalConstants.SBO_SIMPLE_CHEMICAL) ||
						SBMLutilities.sbo.isDescendantOf(species.getSBOTermID(), GlobalConstants.SBO_SIMPLE_CHEMICAL)) {
					compDef_type.add(ComponentDefinition.SMALL_MOLECULE);
				} else {
					compDef_type.add(ComponentDefinition.PROTEIN);
				}
			} else {
				compDef_type.add(ComponentDefinition.PROTEIN);
			}
		}
		compDef = sbolDoc.getComponentDefinition(compDef_identity, VERSION);
		if (compDef==null) {
			compDef = sbolDoc.createComponentDefinition(compDef_identity, VERSION, compDef_type);
			compDef.setName(species.getId());
			compDef.setRoles(compDef_role);
		} else if (!compDef.getTypes().containsAll(compDef_type)) {
			sbolDoc.removeComponentDefinition(compDef);
			compDef = sbolDoc.createComponentDefinition(compDef_identity, VERSION, compDef_type);
			compDef.setName(species.getId());
			compDef.setRoles(compDef_role);
		}
		return compDef; 
	}


	/**
	 * For each species that exist in an SBML model, this function will create a FunctionalComponent instance for the corresponding 
	 * ComponentDefinition existing in the specified ModuleDefinition.
	 * 
	 * @param sbmlDoc - The SBML Document that contains the SBML species to be converted to SBOL FunctionalComponent. 
	 * @param moduleDef - The SBOL ModuleDedfinition that the converted FunctionalComponent will be stored in.
	 * @param compDef - The ComponentDefinition that the FunctionalComponent will reference from.
	 * @param species - The SBML Species that will be converted to SBOL FunctionalComponent.
	 * @return The FunctionalComponent that was created. 
	 * @throws SBOLValidationException - SBOL validation exception while creating an SBOL object for SBML2SBOL conversion.
	 */
	private static FunctionalComponent setFunctionalComponent(SBMLDocument sbmlDoc, Model model, 
			ModuleDefinition moduleDef, ComponentDefinition compDef, Species species) throws SBOLValidationException
	{
		AccessType access; 
		DirectionType direction;
		// create FunctionalComponents for these within the module
		String funcComp_identity =  species.getId();

		if (SBMLutilities.isInput(model,species.getId())) 
		{
			access    = AccessType.PUBLIC;
			direction = DirectionType.IN;
		} 
		else if (SBMLutilities.isOutput(model,species.getId())) 
		{
			access    = AccessType.PUBLIC;
			direction = DirectionType.OUT;
		} 
		else if (SBMLutilities.isOnPort(model,species.getId())) 
		{
			access    = AccessType.PUBLIC;
			direction = DirectionType.NONE;
		} 
		else 
		{
			access    = AccessType.PRIVATE; 
			direction = DirectionType.NONE;
		}
		FunctionalComponent fc = moduleDef.createFunctionalComponent(funcComp_identity, access, compDef.getIdentity(), direction);
		fc.setName(species.getId());
		return fc;
	}

	/**
	 * Convert SBOL Interaction from SBML production reactions.
	 * 
	 * @param moduleDef - The SBOL ModuleDefinition that the Interaction occurs in.
	 * @param reaction - The SBML reaction to be converted to its corresponding SBOL Interaction.
	 * @throws SBOLValidationException - SBOL validation exception while creating an SBOL object for SBML2SBOL conversion.
	 */
	private static void parseProductionReaction(ModuleDefinition moduleDef, Reaction reaction) throws SBOLValidationException
	{
		List<ModifierSpeciesReference> repressors = new ArrayList<ModifierSpeciesReference>();
		List<ModifierSpeciesReference> activators = new ArrayList<ModifierSpeciesReference>(); 
		String promoterId = "";
		for(ModifierSpeciesReference modifier : reaction.getListOfModifiers())
		{

			if (BioModel.isPromoter(modifier)) 
			{
				promoterId = modifier.getSpecies(); 
			} 
			else if (BioModel.isRepressor(modifier)) 
			{
				repressors.add(modifier);
			} 
			else if (BioModel.isActivator(modifier)) 
			{
				activators.add(modifier);
			} 
			else if (BioModel.isRegulator(modifier)) 
			{
				repressors.add(modifier);
				activators.add(modifier);
			}
		}

		for(ModifierSpeciesReference r : repressors)
		{
			String inter_id = r.getSpecies() + "_rep_" + promoterId;

			Set<URI> types = new HashSet<URI>();
			types.add(SystemsBiologyOntology.INHIBITION);

			Interaction interaction = moduleDef.createInteraction(inter_id, types);

			interaction.createParticipation(promoterId, promoterId,SystemsBiologyOntology.INHIBITED);
			interaction.createParticipation(r.getSpecies(), r.getSpecies(),SystemsBiologyOntology.INHIBITOR);
		}

		// Repeat same steps for the list of activators
		for(ModifierSpeciesReference a : activators)
		{
			String inter_id = a.getSpecies() + "_act_" + promoterId;

			Set<URI> types = new HashSet<URI>();
			types.add(SystemsBiologyOntology.STIMULATION); 

			Interaction interaction = moduleDef.createInteraction(inter_id, types);

			interaction.createParticipation(promoterId, promoterId,SystemsBiologyOntology.STIMULATED);
			interaction.createParticipation(a.getSpecies(), a.getSpecies(),SystemsBiologyOntology.STIMULATOR);
		}

		for(SpeciesReference product : reaction.getListOfProducts())
		{
			String i_id = promoterId + "_prod_" + product.getSpecies();

			Set<URI> type = new HashSet<URI>();
			type.add(SystemsBiologyOntology.GENETIC_PRODUCTION);

			Interaction interaction = moduleDef.createInteraction(i_id, type);
			interaction.createParticipation(promoterId, promoterId,SystemsBiologyOntology.TEMPLATE);
			interaction.createParticipation(product.getSpecies(), product.getSpecies(),SystemsBiologyOntology.PRODUCT);
		}
	}


	/**
	 * Convert SBOL Interaction from SBML complex reactions.
	 * 
	 * @param moduleDef - The SBOL ModuleDefinition that the Interaction occurs in.
	 * @param reaction - The SBML reaction to be converted to SBOL Interaction
	 * @throws SBOLValidationException - SBOL validation exception while creating an SBOL object for SBML2SBOL conversion.
	 */
	private static void parseComplexReaction(ModuleDefinition moduleDef, Reaction reaction) throws SBOLValidationException
	{
		Set<URI> type = new HashSet<URI>();
		type.add(SystemsBiologyOntology.NON_COVALENT_BINDING);

		Interaction inter = moduleDef.createInteraction(reaction.getId(), type);

		for(SpeciesReference reactant : reaction.getListOfReactants())
		{
			inter.createParticipation(reactant.getSpecies(), reactant.getSpecies(),SystemsBiologyOntology.REACTANT);
		}
		for(SpeciesReference product : reaction.getListOfProducts())
		{
			inter.createParticipation(product.getSpecies(), product.getSpecies(),SystemsBiologyOntology.PRODUCT);
		}
	}

	/**
	 * Convert SBOL Interaction from SBML degradation reactions.
	 * 
	 * @param moduleDef - The SBOL ModuleDefinition that the Interaction occurs in.
	 * @param reaction - The SBML reaction to be converted to SBOL Interaction
	 * @throws SBOLValidationException - SBOL validation exception while creating an SBOL object for SBML2SBOL conversion.
	 */
	private static void parseDegradationReaction(ModuleDefinition moduleDef, Reaction reaction) throws SBOLValidationException
	{
		Set<URI> types = new HashSet<URI>();
		types.add(SystemsBiologyOntology.DEGRADATION);

		Interaction inter = moduleDef.createInteraction(reaction.getId(), types);

		for(SpeciesReference sp : reaction.getListOfReactants())
		{
			String p_id = sp.getSpecies();
			inter.createParticipation(p_id, sp.getSpecies(),SystemsBiologyOntology.REACTANT);
		}
	}

	
	/**
	 * Converts each SBML submodels contained within the given SBML Document to SBOL Modules.
	 * Any replacements connected to each SBML submodel will be converted to SBOL MapsTo object.
	 * 
	 * @param source - Reference to the source file for the given SBML model
	 * @param externalSBMLPath - The full path of external SBML files to be referenced in the SBML2SBOL conversion
	 * @param sbmlDoc - The SBML Document that contains the SBML submodels to be converted to SBOL Module. 
	 * @param sbol_Library - The global SBOL document that contains all SBOL objects referenced in conversion. 
	 * @param sbolDoc - The SBOL Document that will store the ModuleDefinition.
	 * @param moduleDef - The SBOL ModuleDefinition to reference the converted SBOL Module.
	 * @param model - The SBML Model external model to convert to SBML Module.
	 * @throws SBOLValidationException - SBOL validation exception while creating an SBOL object for SBML2SBOL conversion.
	 * @throws XMLStreamException - Invalid XML occurred when loading SBML file
	 * @throws IOException - Unable to read SBML file
	 */
	private static void extractSubModels(String source, String externalSBMLPath, SBMLDocument sbmlDoc, SBOLDocument sbol_Library, SBOLDocument sbolDoc, ModuleDefinition moduleDef, Model model) throws SBOLValidationException, XMLStreamException, IOException
	{
		ArrayList<String> comps = new ArrayList<String>();
		CompSBMLDocumentPlugin sbmlComp = SBMLutilities.getCompSBMLDocumentPlugin(sbmlDoc);
		CompModelPlugin sbmlCompModel = SBMLutilities.getCompModelPlugin(model);

		if (sbmlCompModel.getListOfSubmodels().size()>0) 
		{
			for (int i = 0; i < sbmlCompModel.getListOfSubmodels().size(); i++) {
				String subModelId = sbmlCompModel.getListOfSubmodels().get(i).getId();
				String modelRef = sbmlCompModel.getListOfSubmodels().get(subModelId).getModelRef();
				ExternalModelDefinition extModelRef = sbmlComp.getListOfExternalModelDefinitions().get(modelRef);
				Model subModel = null;
				if (extModelRef!=null) {
					String extModel = extModelRef.getSource().replace("file://","").replace("file:","").replace(".gcm",".xml");
					SBMLDocument subDocument = SBMLutilities.readSBML(externalSBMLPath + File.separator + extModel);
					subModel = subDocument.getModel();
					if (!comps.contains(modelRef)) {
						comps.add(modelRef);
						parseSBMLModel("file:"+extModelRef.getSource(), externalSBMLPath, subDocument, subModel, sbolDoc, sbol_Library);
					}
				} else {
					subModel = sbmlComp.getListOfModelDefinitions().get(modelRef);
					if (subModel==null) {
						System.out.println("Cannot find "+modelRef);
					} else {
						if (!comps.contains(modelRef)) {
							comps.add(modelRef);
							parseSBMLModel("file:"+source, externalSBMLPath, sbmlDoc,subModel,sbolDoc, sbol_Library);
						}
					}	
				}	
				
				Module m = moduleDef.createModule(subModelId, subModel.getId()+"_md", VERSION);

				for (int j = 0; j < model.getSpeciesCount(); j++) 
				{
					CompSBasePlugin sbmlSBase = SBMLutilities.getCompSBasePlugin(model.getSpecies(j));
					for (int k = 0; k < sbmlSBase.getListOfReplacedElements().size(); k++) 
					{
						ReplacedElement replacement = sbmlSBase.getListOfReplacedElements().get(k);
						if (replacement.getSubmodelRef().equals(subModelId)) 
						{
							if (replacement.isSetPortRef()) 
							{
								String mapId = model.getSpecies(j).getId();
								RefinementType refinement = RefinementType.USELOCAL;
								CompModelPlugin subCompModel = SBMLutilities.getCompModelPlugin(subModel);
								String remoteId = subCompModel.getListOfPorts().get(replacement.getPortRef()).getIdRef();
								String localId = model.getSpecies(j).getId();

								m.createMapsTo(mapId, refinement, localId, remoteId);
							}
						}
					}
					if (sbmlSBase.isSetReplacedBy()) 
					{
						ReplacedBy replacement = sbmlSBase.getReplacedBy();
						if (replacement.getSubmodelRef().equals(subModelId)) 
						{
							if (replacement.isSetPortRef()) 
							{
								String mapId = model.getSpecies(j).getId(); 
								RefinementType refinement = RefinementType.USEREMOTE;
								CompModelPlugin subCompModel = SBMLutilities.getCompModelPlugin(subModel);
								String remoteId = subCompModel.getListOfPorts().get(replacement.getPortRef()).getIdRef();
								String localId = model.getSpecies(j).getId();
								
								m.createMapsTo(mapId, refinement, localId, remoteId);
							} 
						}
					}
				}
			}
		}
	}
	

	/**
	 * Conversion command features
	 */
	private static void usage() {
		System.err.println("SBML2SBOL");
		System.err.println("Description: converts SBML into SBOL.");
		System.err.println();
		System.err.println("Usage:");
		System.err.println("\tjava --jar SBML2SBOL.jar [options] <inputFile> [-u sbolURIPre] [-o <outputFile>]");
		System.err.println();
		System.err.println("Options:");
		System.err.println("\t-I  include path for SBML files (optional)");
		System.err.println("\t-s  SBOL library file (optional)");
		System.exit(1);
	}


	public static void main(String[] args) {
	
		String inputFilePath = null;
		String outputName = null;

		String includeSBMLPath = null;
		String includeSBOLPath = null;

		String sbolURIPre = null;

		HashSet<String> ref_sbolInputFilePath = new HashSet<String>();

		if(args.length == 0 || args[0].equals("-h") ||args[0] == null){
			usage();
		}
		else{
			//Note: first argument must always be the input file name. Assume user is giving full path
			inputFilePath = args[0];

			for(int i = 1; i< args.length-1; i=i+2){
				String flag = args[i];
				String value = args[i+1];

				switch(flag)
				{
				case "-I":
					includeSBMLPath = value;
					break;
				case "-o":
					outputName = value;
					break;
				case "-s":
					includeSBOLPath = value;
					break;
				case "-u":
					sbolURIPre = value;
					break;
				default:
					usage();
				}

			}

			if(includeSBOLPath != null && !includeSBOLPath.isEmpty()){
				//Note: this is an optional field. User provided sbol path to read in
				File fileDir = new File(includeSBOLPath);
				File[] sbolFiles =  fileDir.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return (name.toLowerCase().endsWith(".rdf") || name.toLowerCase().endsWith(".sbol"));
					}
				});

				for(File f : sbolFiles){
					ref_sbolInputFilePath.add(f.getAbsolutePath());
				}
			}

//			if(sbolURIPre == null || sbolURIPre.isEmpty()){
//				//SBOL Default URI is a required field. Set SBOL Document to the given SBOL Prefix if the user did not provide one.
//				Preferences biosimrc = Preferences.userRoot();
//				sbolURIPre = IBioSimPreferences.INSTANCE.getUserInfo().getURI().toString();
//			}

			SBMLDocument sbmlDoc = null;
			try {
				if(inputFilePath != null){
					if (includeSBMLPath == null) {
						//SBML file is relative. No external path was given for the input SBML file. 
						sbmlDoc = SBMLutilities.readSBML(inputFilePath);
					} 
					else {
						sbmlDoc = SBMLutilities.readSBML(includeSBMLPath + File.separator + inputFilePath);
					}
					SBOLDocument sbolDoc = new SBOLDocument();
					SBML2SBOL.convert_SBML2SBOL(sbolDoc,includeSBMLPath, sbmlDoc, inputFilePath, ref_sbolInputFilePath, sbolURIPre);
					sbolDoc.write(outputName, SBOLDocument.RDF);
				} 
				else {
					usage();
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
		}

	}
}
