package sbol.assembly;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

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
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.Identified;
import org.sbolstandard.core2.SystemsBiologyOntology;

import sbol.util.ChEBI;
import sbol.util.MyersOntology;
import biomodel.annotation.AnnotationUtility;
import biomodel.annotation.SBOLAnnotation;
import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;
import biomodel.util.SBMLutilities;
import biomodel.util.Utility;

public class ModelGenerator {
	
	public ModelGenerator() {
	}
	
	public static String getDisplayID(Identified sbolElement) { 
		if (sbolElement.isSetDisplayId()) {
			return sbolElement.getDisplayId();
		}
		String identity = sbolElement.getIdentity().toString();
		return identity.substring(identity.lastIndexOf("/") + 1);
	}
	
	public static List<BioModel> generateModel(String projectDirectory, ModuleDefinition moduleDef, SBOLDocument sbolDoc) {
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
			}
			if (isPromoterComponent(comp, sbolDoc)) {
				generatePromoterSpecies(comp, sbolDoc, targetModel);
				if (isInputComponent(comp)) {
					generateInputPort(comp, targetModel);
				} else if (isOutputComponent(comp)){
					generateOutputPort(comp, targetModel);
				}
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
					if (partici.containsRole(SystemsBiologyOntology.PROMOTER)) {
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
					} else if (partici.containsRole(SystemsBiologyOntology.PROMOTER)) {
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
					if (partici.containsRole(SystemsBiologyOntology.PROMOTER)) {
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
					if (partici.containsRole(SystemsBiologyOntology.PROMOTER)) {
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
	
	public static void generateSubModel(String projectDirectory, Module subModule, ModuleDefinition moduleDef, SBOLDocument sbolDoc, 
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
	
	public static List<BioModel> generateSubModel(String projectDirectory, Module subModule, ModuleDefinition moduleDef, SBOLDocument sbolDoc, 
			BioModel targetModel) {
		ModuleDefinition subModuleDef = sbolDoc.getModuleDefinition(subModule.getDefinitionURI());
		List<BioModel> subModels = generateModel(projectDirectory, subModuleDef, sbolDoc);
		BioModel subTargetModel = subModels.get(0);
		generateSubModel(projectDirectory, subModule, moduleDef, sbolDoc, subTargetModel, targetModel);
		return subModels;
	}
	
	public static void generateReplacement(MapsTo mapping, Module subModule, ModuleDefinition moduleDef, 
			SBOLDocument sbolDoc, BioModel subTargetModel, BioModel targetModel) {
		ModuleDefinition subModuleDef = sbolDoc.getModuleDefinition(subModule.getDefinitionURI()); 
		FunctionalComponent remoteSpecies = subModuleDef.getFunctionalComponent(mapping.getRemoteURI());
		FunctionalComponent localSpecies = moduleDef.getFunctionalComponent(mapping.getLocalURI());
		
		Species localSBMLSpecies = targetModel.getSBMLDocument().getModel().getSpecies(getDisplayID(localSpecies));
		Port port = subTargetModel.getPortByIdRef(getDisplayID(remoteSpecies));
		if (port==null) System.out.println("Cannot find "+getDisplayID(remoteSpecies));
		
		Submodel subModel = targetModel.getSBMLCompModel().getSubmodel(getDisplayID(subModule));
		SBMLutilities.addReplacement(localSBMLSpecies, subModel, getDisplayID(subModule), port.getId(), "(none)", 
				new String[]{""}, new String[]{""}, new String[]{""}, new String[]{""}, false);
		
		// Annotate SBML replacment with SBOL maps-to
		CompSBasePlugin compSBML = SBMLutilities.getCompSBasePlugin(localSBMLSpecies);
		SBMLutilities.setDefaultMetaID(targetModel.getSBMLDocument(), compSBML.getReplacedElement(compSBML.getNumReplacedElements() - 1), 1);
		annotateReplacement(compSBML.getReplacedElement(compSBML.getNumReplacedElements() - 1), mapping);
	}
	
	public static void generateReplacedBy(MapsTo mapping, Module subModule, ModuleDefinition moduleDef, 
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
	
	public static void generateInputPort(FunctionalComponent species, BioModel targetModel) {
		targetModel.createDirPort(getDisplayID(species), GlobalConstants.INPUT);
	}
	
	public static void generateOutputPort(FunctionalComponent species, BioModel targetModel) {
		targetModel.createDirPort(getDisplayID(species), GlobalConstants.OUTPUT);
	}

	public static void generateSpecies(FunctionalComponent species, SBOLDocument sbolDoc, BioModel targetModel) {
		targetModel.createSpecies(getDisplayID(species), -1, -1);
		Species sbmlSpecies = targetModel.getSBMLDocument().getModel().getSpecies(getDisplayID(species));
		sbmlSpecies.setBoundaryCondition(species.getDirection().equals(DirectionType.IN));
		// Annotate SBML species with SBOL component and component definition
		annotateSpecies(sbmlSpecies, species, sbolDoc);	
	}
	
	public static void generatePromoterSpecies(FunctionalComponent promoter, SBOLDocument sbolDoc, BioModel targetModel) {
		targetModel.createPromoter(getDisplayID(promoter), -1, -1, true, false, null);
		Species sbmlPromoter = targetModel.getSBMLDocument().getModel().getSpecies(getDisplayID(promoter));
		
		// Annotate SBML promoter species with SBOL component and component definition
		ComponentDefinition compDef = sbolDoc.getComponentDefinition(promoter.getDefinitionURI());
		if (compDef!=null) {
			annotateSpecies(sbmlPromoter, promoter, compDef, sbolDoc);
		}
	}

	public static void generateBiochemicalRxn(Interaction interaction, ModuleDefinition moduleDef, BioModel targetModel) {
		SystemsBiologyOntology sbo = new SystemsBiologyOntology();
		String SBOid = "";
		for (URI type : interaction.getTypes()) {
			if (sbo.getId(type)!=null) {
				SBOid = sbo.getId(type);
			}
		}
		ArrayList<String> reactants = new ArrayList<String>();
		ArrayList<String> modifiers = new ArrayList<String>();
		ArrayList<String> products = new ArrayList<String>();
		for (Participation participation : interaction.getParticipations()) {
			if (participation.containsRole(SystemsBiologyOntology.REACTANT)) {
				reactants.add(participation.getDisplayId());
			}
			if (participation.containsRole(SystemsBiologyOntology.MODIFIER)) {
				modifiers.add(participation.getDisplayId());
			}
			if (participation.containsRole(SystemsBiologyOntology.PRODUCT)) {
				products.add(participation.getDisplayId());
			}
		}
		targetModel.createBiochemicalReaction(interaction.getDisplayId(),SBOid,reactants,modifiers,products);
	}

	public static void generateDegradationRxn(Interaction degradation, ModuleDefinition moduleDef, BioModel targetModel) {
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
	
	public static void generateComplexFormationRxn(Interaction complexFormation, Participation complex,
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
	
	public static void generateProductionRxn(FunctionalComponent promoter, List<Participation> partici, List<Interaction> productions,
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
		rxnID = rxnID.substring(1);
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
	
	public static void generateActivatorReference(Participation activator, FunctionalComponent promoter, 
			ModuleDefinition moduleDef, Reaction productionRxn, BioModel targetModel) {
		FunctionalComponent tf = moduleDef.getFunctionalComponent(activator.getParticipantURI());

		targetModel.addActivatorToProductionReaction(getDisplayID(promoter),  
				getDisplayID(tf), "none", productionRxn, null, null, null);
		
		// Annotate SBML activator species reference with SBOL activator participation
		ModifierSpeciesReference activatorRef = productionRxn.getModifierForSpecies(getDisplayID(tf));
		SBMLutilities.setDefaultMetaID(targetModel.getSBMLDocument(), activatorRef, 1);
		annotateSpeciesReference(activatorRef, activator);
	}
	
	public static void generateRepressorReference(Participation repressor, FunctionalComponent promoter, 
			ModuleDefinition moduleDef, Reaction productionRxn, BioModel targetModel) {
		FunctionalComponent tf = moduleDef.getFunctionalComponent(repressor.getParticipantURI());
		targetModel.addRepressorToProductionReaction(getDisplayID(promoter),  
				getDisplayID(tf), "none", productionRxn, null, null, null);
		
		// Annotate SBML repressor species reference with SBOL repressor participation
		ModifierSpeciesReference repressorRef = productionRxn.getModifierForSpecies(getDisplayID(tf));
		SBMLutilities.setDefaultMetaID(targetModel.getSBMLDocument(), repressorRef, 1);
		annotateSpeciesReference(repressorRef, repressor);
	}
	
	public static void generateProductReference(Participation product, FunctionalComponent promoter, 
			ModuleDefinition moduleDef, Reaction productionRxn, BioModel targetModel) {
		FunctionalComponent protein = moduleDef.getFunctionalComponent(product.getParticipantURI());
		targetModel.addActivatorToProductionReaction(getDisplayID(promoter),  
				"none", getDisplayID(protein), productionRxn, null, null, null);
		
		// Annotate SBML product species reference with SBOL product participation
		SpeciesReference productRef = productionRxn.getProductForSpecies(getDisplayID(protein));
		SBMLutilities.setDefaultMetaID(targetModel.getSBMLDocument(), productRef, 1);
		annotateSpeciesReference(productRef, product);
	}
	
	public static void annotateSpecies(Species species, FunctionalComponent comp, ComponentDefinition compDef, 
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
	public static void annotateSpecies(Species species, FunctionalComponent comp, SBOLDocument sbolDoc) {
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
	public static void annotateSpecies(Species species, ComponentDefinition compDef) {
		SBOLAnnotation speciesAnno = new SBOLAnnotation(species.getMetaId(), compDef.getIdentity());
		HashMap<String, List<URI>> sbolElementIdentities = new HashMap<String, List<URI>>();
		AnnotationUtility.parseSBOLAnnotation(species, sbolElementIdentities);
		for (String className : sbolElementIdentities.keySet()) {
			speciesAnno.createSBOLElementsDescription(className, sbolElementIdentities.get(className));
		}
		AnnotationUtility.setSBOLAnnotation(species, speciesAnno);	
	}
	
	// Annotate SBML reaction with SBOL interactions
	public static void annotateRxn(Reaction rxn, List<Interaction> interacts) {
		List<URI> interactIdentities = new LinkedList<URI>();
		for (Interaction interact : interacts)
			interactIdentities.add(interact.getIdentity());
		SBOLAnnotation rxnAnno = new SBOLAnnotation(rxn.getMetaId(), 
				interacts.get(0).getClass().getSimpleName(), interactIdentities);
		AnnotationUtility.setSBOLAnnotation(rxn, rxnAnno);
	}
	
	// Annotate SBML reaction with SBOL interaction
	public static void annotateRxn(Reaction rxn, Interaction interact) {
		SBOLAnnotation rxnAnno = new SBOLAnnotation(rxn.getMetaId(), 
				interact.getClass().getSimpleName(), interact.getIdentity());
		AnnotationUtility.setSBOLAnnotation(rxn, rxnAnno);
	}
	
	// Annotate SBML species reference with SBOL participation
	public static void annotateSpeciesReference(SimpleSpeciesReference speciesRef, Participation partici) {
		SBOLAnnotation speciesRefAnno = new SBOLAnnotation(speciesRef.getMetaId(),
				partici.getClass().getSimpleName(), partici.getParticipantURI());
		AnnotationUtility.setSBOLAnnotation(speciesRef, speciesRefAnno);
	}
	
	// Annotate SBML species reference with SBOL participations
	public static void annotateSpeciesReference(SimpleSpeciesReference speciesRef, List<Participation> partici) {
		List<URI> particiIdentities = new LinkedList<URI>();
 		for (Participation p : partici) {
			particiIdentities.add(p.getIdentity());
		}
		SBOLAnnotation speciesRefAnno = new SBOLAnnotation(speciesRef.getMetaId(),
				partici.get(0).getClass().getSimpleName(), particiIdentities);
		AnnotationUtility.setSBOLAnnotation(speciesRef, speciesRefAnno);
	}
		
	
	public static void annotateReplacedBy(ReplacedBy replacedBy, MapsTo mapping) {
		SBOLAnnotation replacedByAnno = new SBOLAnnotation(replacedBy.getMetaId(),
				mapping.getClass().getSimpleName(), mapping.getIdentity());
		AnnotationUtility.setSBOLAnnotation(replacedBy, replacedByAnno);
	}
	
	public static void annotateReplacement(ReplacedElement replacement, MapsTo mapping) {
		SBOLAnnotation replacementAnno = new SBOLAnnotation(replacement.getMetaId(),
				mapping.getClass().getSimpleName(), mapping.getIdentity()); 
		AnnotationUtility.setSBOLAnnotation(replacement, replacementAnno);
	}
	
	public static void annotateSubModel(Submodel subModel, Module subModule) {
		SBOLAnnotation subModelAnno = new SBOLAnnotation(subModel.getMetaId(),
				subModule.getClass().getSimpleName(), subModule.getDefinitionURI()); 
		AnnotationUtility.setSBOLAnnotation(subModel, subModelAnno);
	}
	
	public static boolean isIOMapping(MapsTo mapping, Module subModule, SBOLDocument sbolDoc) {
		ModuleDefinition subModuleDef = sbolDoc.getModuleDefinition(subModule.getDefinitionURI());
		FunctionalComponent remoteComp = subModuleDef.getFunctionalComponent(mapping.getRemoteURI());
		return isInputComponent(remoteComp) || isOutputComponent(remoteComp);
	}
	
	public static boolean isTopDownMapping(MapsTo mapping) { 
		RefinementType refinement = mapping.getRefinement();
		return refinement == RefinementType.VERIFYIDENTICAL || refinement == RefinementType.MERGE
				|| refinement == RefinementType.USELOCAL;
	}
	
	public static boolean isInputComponent(FunctionalComponent comp) {
		return comp.getDirection().equals(DirectionType.IN);
	}
	
	public static boolean isOutputComponent(FunctionalComponent comp) {
		return comp.getDirection().equals(DirectionType.OUT) || comp.getDirection().equals(DirectionType.INOUT);
	}
	
	public static boolean isDNAComponent(FunctionalComponent comp, SBOLDocument sbolDoc) {
		ComponentDefinition compDef = sbolDoc.getComponentDefinition(comp.getDefinitionURI());
		if (compDef==null) return false;
		return isDNADefinition(compDef);
	}
	
	public static boolean isDNADefinition(ComponentDefinition compDef) {
		return compDef.containsType(ChEBI.DNA) ||
				compDef.containsType(ComponentDefinition.DNA);
	}
	
	public static boolean isProteinComponent(FunctionalComponent comp, SBOLDocument sbolDoc) {
		ComponentDefinition compDef = sbolDoc.getComponentDefinition(comp.getDefinitionURI());
		if (compDef==null) return false;
		return isProteinDefinition(compDef);
	}
	
	public static boolean isProteinDefinition(ComponentDefinition compDef) {
		return compDef.containsType(ChEBI.PROTEIN) ||
				compDef.containsType(ComponentDefinition.PROTEIN);
	}
		
	public static boolean isRNAComponent(FunctionalComponent comp, SBOLDocument sbolDoc) {
		ComponentDefinition compDef = sbolDoc.getComponentDefinition(comp.getDefinitionURI());
		if (compDef==null) return false;
		return isRNADefinition(compDef);
	}
	
	public static boolean isRNADefinition(ComponentDefinition compDef) {
		return compDef.containsType(ComponentDefinition.RNA);
	}
	
	public static boolean isPromoterComponent(FunctionalComponent comp, SBOLDocument sbolDoc) {
		ComponentDefinition compDef = sbolDoc.getComponentDefinition(comp.getDefinitionURI());
		if (compDef==null) return false;
		return isPromoterDefinition(compDef);
	}
	
	// TODO: need to figure out better if the CD is a promoter, need to look carefully at its subComponents
	public static boolean isPromoterDefinition(ComponentDefinition compDef) {
		return isDNADefinition(compDef) /*
				&& (compDef.containsRole(SequenceOntology.PROMOTER)||
						(compDef.containsRole(SequenceOntology.PROMOTER)))*/;
	}
	
	public static boolean isGeneComponent(FunctionalComponent comp, SBOLDocument sbolDoc) {
		ComponentDefinition compDef = sbolDoc.getComponentDefinition(comp.getDefinitionURI());
		if (compDef==null) return false;
		return isGeneDefinition(compDef);
	}
	
	public static boolean isGeneDefinition(ComponentDefinition compDef) {
		return isDNADefinition(compDef) 
				&& compDef.containsRole(MyersOntology.GENE);
	}
	
	public static boolean isSpeciesComponent(FunctionalComponent comp, SBOLDocument sbolDoc) {
		ComponentDefinition compDef = sbolDoc.getComponentDefinition(comp.getDefinitionURI());
		if (compDef==null) return true;
		return isSpeciesDefinition(compDef);
	}
	
	public static boolean isSpeciesDefinition(ComponentDefinition compDef) {
		return isComplexDefinition(compDef)
				|| isProteinDefinition(compDef)
				|| isRNADefinition(compDef)
				|| isTFDefinition(compDef)
				|| isSmallMoleculeDefinition(compDef)
				|| compDef.containsType(ChEBI.EFFECTOR);
	}
	
	public static boolean isComplexComponent(FunctionalComponent comp, SBOLDocument sbolDoc) {
		ComponentDefinition compDef = sbolDoc.getComponentDefinition(comp.getDefinitionURI());
		if (compDef==null) return false;
		return isComplexDefinition(compDef);
	}
	
	public static boolean isComplexDefinition(ComponentDefinition compDef) {
		return compDef.containsType(ChEBI.NON_COVALENTLY_BOUND_MOLECULAR_ENTITY) ||
				compDef.containsType(ComponentDefinition.COMPLEX);
	}
	public static boolean isSmallMoleculeDefinition(ComponentDefinition compDef) {
		return compDef.containsType(ComponentDefinition.SMALL_MOLECULE);
	}
	
	public static boolean isTFComponent(FunctionalComponent comp, SBOLDocument sbolDoc) {
		ComponentDefinition compDef = sbolDoc.getComponentDefinition(comp.getDefinitionURI());
		if (compDef==null) return false;
		return isTFDefinition(compDef);
	}
	
	public static boolean isTFDefinition(ComponentDefinition compDef) {
		return (isProteinDefinition(compDef) || isComplexDefinition(compDef))
				&& compDef.containsRole(MyersOntology.TF);
	}
	
	public static boolean isDegradationInteraction(Interaction interact, ModuleDefinition moduleDef, 
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
	
	public static boolean isComplexFormationInteraction(Interaction interact, ModuleDefinition moduleDef, 
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
	
	public static boolean isProductionInteraction(Interaction interact, ModuleDefinition moduleDef,
			SBOLDocument sbolDoc) {
		if (interact.containsType(SystemsBiologyOntology.GENETIC_PRODUCTION) && interact.getParticipations().size() == 2/*3*/) {
			boolean hasPromoter = false;
			boolean hasProduct = false;
			//boolean hasTranscribed = false;
			for (Participation partici : interact.getParticipations()) {
				FunctionalComponent comp = moduleDef.getFunctionalComponent(partici.getParticipantURI());
				if (partici.containsRole(SystemsBiologyOntology.PROMOTER) && isPromoterComponent(comp, sbolDoc))
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
	
	public static boolean isActivationInteraction(Interaction interact, ModuleDefinition moduleDef,
			SBOLDocument sbolDoc) {
		if ((interact.containsType(SystemsBiologyOntology.GENETIC_ENHANCEMENT) ||
				interact.containsType(SystemsBiologyOntology.STIMULATION)) 
				&& interact.getParticipations().size() == 2) {
			boolean hasActivated = false;
			boolean hasActivator = false;
			for (Participation partici : interact.getParticipations()) {
				FunctionalComponent comp = moduleDef.getFunctionalComponent(partici.getParticipantURI());
				if (partici.containsRole(SystemsBiologyOntology.PROMOTER) && isPromoterComponent(comp, sbolDoc))
					hasActivated = true;
				else if (partici.containsRole(SystemsBiologyOntology.STIMULATOR) /*&& isTFComponent(comp, sbolDoc)*/)
					hasActivator = true;
			}
			if (hasActivated && hasActivator)
				return true;
		}
		return false;
	}
	
	public static boolean isRepressionInteraction(Interaction interact, ModuleDefinition moduleDef,
			SBOLDocument sbolDoc) {
		if ((interact.containsType(SystemsBiologyOntology.GENETIC_SUPPRESSION) ||
				interact.containsType(SystemsBiologyOntology.INHIBITION))
				&& interact.getParticipations().size() == 2) {
			boolean hasRepressed = false;
			boolean hasRepressor = false;
			for (Participation partici : interact.getParticipations()) {
				FunctionalComponent comp = moduleDef.getFunctionalComponent(partici.getParticipantURI());
				if (partici.containsRole(SystemsBiologyOntology.PROMOTER) && isPromoterComponent(comp, sbolDoc))
					hasRepressed = true;
				else if (partici.containsRole(SystemsBiologyOntology.INHIBITOR) /*&& isTFComponent(comp, sbolDoc)*/)
					hasRepressor = true;
			}
			if (hasRepressed && hasRepressor)
				return true;
		}
		return false;
	}

}
