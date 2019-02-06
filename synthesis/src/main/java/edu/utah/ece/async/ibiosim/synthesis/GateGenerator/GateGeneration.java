package edu.utah.ece.async.ibiosim.synthesis.GateGenerator;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.ComponentInstance;
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
import org.sbolstandard.core2.SBOLWriter;
import org.sbolstandard.core2.SequenceOntology;
import org.sbolstandard.core2.SystemsBiologyOntology;
import org.virtualparts.VPRException;
import org.virtualparts.VPRTripleStoreException;

import edu.utah.ece.async.ibiosim.conversion.VPRModelGenerator;
import edu.utah.ece.async.ibiosim.dataModels.sbol.SBOLUtility;
import edu.utah.ece.async.ibiosim.synthesis.GateGenerator.GeneticGate.GateType;

/**
 * Class to build different variation of logic gates.
 * Designs provided to this class must be transcription unit(s) annotated with parts. 
 * The transcription unit will call VPR model generation to build gate interactions and are then analyzed to create supported logic gates.
 * @author Tramy Nguyen
 */
public class GateGeneration {
	
	private SBOLUtility sbolUtility;
	private List<GeneticGate> gates;
	
	public GateGeneration() {
		this.sbolUtility = SBOLUtility.getInstance();
		this.gates = new ArrayList<>();
	}

	public List<SBOLDocument> enrichedTU(List<SBOLDocument> transcriptionalUnitList, String SynBioHubRepository) throws SBOLValidationException, IOException, SBOLConversionException, VPRException, VPRTripleStoreException {
		List<SBOLDocument> enrichedTU_list = new ArrayList<>();
		for(SBOLDocument document : transcriptionalUnitList) {
			for(ComponentDefinition rootCD : document.getRootComponentDefinitions()) {
				SBOLDocument tuDesign = sbolUtility.createSBOLDocument();
				document.createRecursiveCopy(tuDesign, rootCD);
				SBOLDocument enrichedTU = VPRModelGenerator.generateModel(SynBioHubRepository, tuDesign, "");
				enrichedTU_list.add(enrichedTU);
			}
		}
		return enrichedTU_list;
	}
	
	public void sortEnrichedTUList(List<SBOLDocument> enrichedTU_list) throws GateGenerationExeception {
		for(SBOLDocument enrichedTU : enrichedTU_list) {
			for(ModuleDefinition mdGate : enrichedTU.getRootModuleDefinitions()) {
				//collect interaction information
				Map<Interaction, FunctionalComponent> gateInteractions = getGateInteractions(mdGate.getModules());
				
				//find tu design and verify connection
				for(FunctionalComponent gatePart : mdGate.getFunctionalComponents()) {
					int numOfRepressions = 0;
					int numOfActivations = 0;
					int numOfProductions = 0;
					boolean promoterHasDoubleInteraction = false;
					for(MapsTo fc_mp : gatePart.getMapsTos()) {
						ComponentInstance mpObj = getMapsTo(fc_mp);
						if(mpObj instanceof FunctionalComponent) {
							FunctionalComponent fc = (FunctionalComponent) mpObj;
							ComponentDefinition fc_cd = fc.getDefinition();
							if(fc_cd.getRoles().contains(SequenceOntology.PROMOTER)) {
								List<Interaction> interList = getInteractions(gateInteractions, fc);
								if(interList.size() == 2) {
									promoterHasDoubleInteraction = true;
								}
								else { 
									//throw error
								}
								for(Interaction inter : interList) {
									if(inter.getTypes().contains(SystemsBiologyOntology.INHIBITION)) {
										numOfRepressions++;
									}
									else if(inter.getTypes().contains(SystemsBiologyOntology.STIMULATION)){
										numOfActivations++;
									}
									
								}
							}
							else if(fc_cd.getRoles().contains(SequenceOntology.CDS) || fc_cd.getRoles().contains(SequenceOntology.ENGINEERED_REGION)) {
								FunctionalComponent gateCDS = fc;
								if(fc_cd.getRoles().contains(SequenceOntology.ENGINEERED_REGION)) {
									for(MapsTo er_mp : fc.getMapsTos()) {
										ComponentInstance erComponent = getMapsTo(er_mp);
										if(erComponent instanceof FunctionalComponent) {
											FunctionalComponent erDNA = (FunctionalComponent) erComponent;
											if(erDNA.getDefinition().getRoles().contains(SequenceOntology.CDS)) {
												gateCDS = erDNA;
											}
										}
									}
								}
								List<Interaction> interList = getInteractions(gateInteractions, gateCDS);
								for(Interaction inter : interList) {
									if(inter.getTypes().contains(SystemsBiologyOntology.GENETIC_PRODUCTION)) {
										numOfProductions++;
									}
								}
							}
						}
					}
					if(numOfRepressions == 1 && numOfProductions == 1 && !promoterHasDoubleInteraction) {
						NOTGate identifiedGate = new NOTGate(enrichedTU);
						gates.add(identifiedGate);
					}
					else if(numOfRepressions == 2 && numOfProductions == 1 && !promoterHasDoubleInteraction) {
						NORGate identifiedGate = new NORGate(enrichedTU);
						gates.add(identifiedGate);
					}
					else if(numOfActivations == 2 && numOfProductions == 1 && !promoterHasDoubleInteraction) {
						ORGate identifiedGate = new ORGate(enrichedTU);
						gates.add(identifiedGate);
					}
//					else {
//						NotSupportedGate identifiedGate = new NotSupportedGate(enrichedTU);
//						gates.add(identifiedGate);
//					}
				}
			}
		}
	}
	
	
	private List<Interaction> getInteractions(Map<Interaction, FunctionalComponent> interList, FunctionalComponent fc) {
		List<Interaction> fcList = new ArrayList<>();
		for(Map.Entry<Interaction, FunctionalComponent> interFCMap : interList.entrySet()) {
			if(interFCMap.getValue().equals(fc)) {
				fcList.add(interFCMap.getKey());
			}
		}
		return fcList;
	}
	
	private Map<Interaction, FunctionalComponent> getGateInteractions(Set<Module> modules) throws GateGenerationExeception{
		Map<Interaction, FunctionalComponent> gateInteractions = new HashMap<>();
		for(Module module : modules) {
			ModuleDefinition referencedMD = module.getDefinition();
			if(mdHasInteractionType(referencedMD, SystemsBiologyOntology.INHIBITION)) {
				Interaction inhibitionInter = getInteraction(referencedMD, SystemsBiologyOntology.INHIBITION);
				FunctionalComponent promoter = getParticipant(module, inhibitionInter, SequenceOntology.PROMOTER);
				gateInteractions.put(inhibitionInter, promoter);
			}
			else if(mdHasInteractionType(referencedMD, SystemsBiologyOntology.STIMULATION)) {
				Interaction activationInter = getInteraction(referencedMD, SystemsBiologyOntology.STIMULATION);
				FunctionalComponent promoter = getParticipant(module, activationInter, SequenceOntology.PROMOTER);
				gateInteractions.put(activationInter, promoter);
			}
			else if(mdHasInteractionType(referencedMD, SystemsBiologyOntology.GENETIC_PRODUCTION)){
				Interaction productionInter = getInteraction(referencedMD, SystemsBiologyOntology.GENETIC_PRODUCTION);
				FunctionalComponent cds = getParticipant(module, productionInter, SequenceOntology.CDS);
				gateInteractions.put(productionInter, cds);
			}
		}
		return gateInteractions;
	}
	
	private Interaction getInteraction(ModuleDefinition md, URI interactionType) throws GateGenerationExeception {
		List<Interaction> interWithType = new ArrayList<>();
		for(Interaction inter : md.getInteractions()) {
			if(inter.getTypes().contains(interactionType)) {
				interWithType.add(inter);
			}
		}
		
		if(interWithType.size() != 1) {
			throw new GateGenerationExeception("After VPR is performed, one Interaction is allowed to exist within a ModuleDefinition. "
					+ "However, this ModuleDefinition " + md.getDisplayId() + " has " + interWithType.size() + " with interaction type: " + interactionType);
		}
		return interWithType.get(0);
	}
	
	/**
	 * Find the Participant with given participant role from the given Interaction. 
	 * Before the Participant is returned, verify that the desired Participant and the Module where the given Interaction is referenced in is pointing to the same ComponentDefinition.
	 * @param module: The module that references the interaction.
	 * @param interaction: The interaction to locate the given Participant role
	 * @param participantRole: The role of the participant.
	 * @return A FunctionalComponent that represents the desired Participant expected to be found within the given Interaction.
	 * @throws GateGenerationExeception
	 */
	private FunctionalComponent getParticipant(Module module, Interaction interaction, URI participantRole) throws GateGenerationExeception {
		List<FunctionalComponent> participantList = new ArrayList<>();
		for(Participation p : interaction.getParticipations()) {
			FunctionalComponent participant = p.getParticipant();
			if(participant.getDefinition().getRoles().contains(participantRole)) {
				participantList.add(participant);
			}
		}
		if(participantList.size() != 1) {
			throw new GateGenerationExeception("Expected to find one participant with " + participantRole + ". However, " +
		participantList.size() + " participant(s) was found with the the given role " + participantRole + " in this Interaction " + interaction.getDisplayId());
		}
	
		FunctionalComponent expectedFC = null;
		ComponentDefinition participantCD = participantList.get(0).getDefinition();
		for(MapsTo mp : module.getMapsTos()) {
			ComponentInstance mapsToObj = getMapsTo(mp);
			if((mapsToObj instanceof FunctionalComponent)) {
				FunctionalComponent fc = (FunctionalComponent) mapsToObj;
				ComponentDefinition fc_cd = fc.getDefinition();
				if(fc_cd.containsRole(participantRole) && fc_cd.equals(participantCD)) {
					expectedFC = fc;
				}
			}
		}
		
		if(expectedFC == null) {
			throw new GateGenerationExeception("The participant found in this interaction " + interaction.getIdentity() + " does not reference to the same ComponentDefinition from this Module " + module.getIdentity());
		}
		return expectedFC;
	}
	
	private ComponentInstance getMapsTo(MapsTo mapsTo) throws GateGenerationExeception {
		RefinementType type = mapsTo.getRefinement();
		if(type.equals(RefinementType.MERGE)) {
			throw new GateGenerationExeception("Gate generation does not support MapsTo with " + RefinementType.MERGE + " when sorting gate types. This type is contained within the following MapsTo object: " + mapsTo.getDisplayId());
		}
		
		ComponentInstance local = mapsTo.getLocal();
		ComponentInstance remote = mapsTo.getRemote();
		if(type.equals(RefinementType.USELOCAL)){
			return local;
		}
		else if(type.equals(RefinementType.USEREMOTE)) {
			return remote;
		}
		assert(mapsTo.getRefinement().equals(RefinementType.VERIFYIDENTICAL));
		return local;
	}
	
	/**
	 * Check if the given ModuleDefinition has an interaction with the given type. 
	 * @param md: ModuleDefinition that contains the interaction
	 * @param interactionType: The interaction type
	 * @return True if the given ModuleDefinition has an interaction with the given type. Otherwise, false is returned.
	 */
	private boolean mdHasInteractionType(ModuleDefinition md, URI interactionType) {
		Set<Interaction> interList = md.getInteractions();
		if(interList.size() < 1) {
			return false;
		}
		for(Interaction inter : interList) {
			if(inter.getTypes().contains(interactionType)) {
				return true;
			}
		}
		return false;
	}
	
	public SBOLDocument getNOTLibrary() throws SBOLValidationException {
		SBOLDocument notLibrary = sbolUtility.createSBOLDocument();
		for(GeneticGate gate : this.gates) {
			if(gate.getType().equals(GateType.NOT)) {
				notLibrary.createCopy(gate.getSBOLDocument());
			}
		}
		return notLibrary;
	}
	
	public SBOLDocument getNORLibrary() throws SBOLValidationException {
		SBOLDocument norLibary = sbolUtility.createSBOLDocument();
		for(GeneticGate gate : this.gates) {
			if(gate.getType().equals(GateType.NOR)) {
				norLibary.createCopy(gate.getSBOLDocument());
			}
		}
		return norLibary;
	}
	
	public SBOLDocument getANDLibrary() throws SBOLValidationException {
		SBOLDocument andLibary = sbolUtility.createSBOLDocument();
		for(GeneticGate gate : this.gates) {
			if(gate.getType().equals(GateType.AND)) {
				andLibary.createCopy(gate.getSBOLDocument());
			}
		}
		return andLibary;
	}
	
	public List<GeneticGate> getGeneticGateList(){
		return this.gates;
	}
	
	public void exportLibrary(SBOLDocument libraryDocument, String fullPath) throws IOException, SBOLConversionException {
		SBOLWriter.write(libraryDocument, fullPath);
	}
	
}
