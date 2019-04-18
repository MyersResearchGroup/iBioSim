package edu.utah.ece.async.ibiosim.synthesis.GeneticGates;

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
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SequenceOntology;
import org.sbolstandard.core2.SystemsBiologyOntology;

import edu.utah.ece.async.ibiosim.synthesis.GateGenerator.GateGenerationExeception;

/**
 * Class that examines an SBOL ModuleDefinition enriched by VPR to identify the type of genetic logic gate. 
 * 
 * @author Tramy Nguyen
 */
public class GateIdentifier {
	
	private List<ComponentParticipation> promoters, cds;
	private Map<URI, ComponentParticipation> fcToInteractionMap;

	private SBOLDocument gateDocument;
	private ModuleDefinition gateMD;

	public GateIdentifier(SBOLDocument doc, ModuleDefinition md) {
		this.fcToInteractionMap = new HashMap<>();
		this.promoters = new ArrayList<>();
		this.cds = new ArrayList<>();
		
		this.gateDocument = doc;
		this.gateMD = md;
	}

	public GeneticGate createGate() throws GateGenerationExeception, SBOLValidationException {
		//collect DNA parts where input and output molecules are attached
		for(FunctionalComponent functionalComponent : gateMD.getFunctionalComponents()) {
			ComponentDefinition cd = functionalComponent.getDefinition();
			ComponentParticipation component = new ComponentParticipation(functionalComponent);
			fcToInteractionMap.put(functionalComponent.getIdentity(), component);

			if(componentDefinitionHasRole(cd, SequenceOntology.PROMOTER)) {
				promoters.add(component);
			}
			if(componentDefinitionHasRole(cd, SequenceOntology.CDS)) {
				cds.add(component);
			}
			
		}

		for(Module module : gateMD.getModules()) {
			addGateInteraction(module);
		}
		return getGateType();
	}
	

	private GeneticGate getGateType() {
		if(isNOTGate()) {
			return createNOTGate();
		}
		if(isNORGate()) {
			return createNORGate();
		}
		if(isORGate()) {
			return createORGate();
		}
		if(isNANDGate()) {
			return createNANDGate();
		}
		if(isANDGate()) {
			return createANDGate();
		}
		return new NOTSUPPORTEDGate(gateDocument, gateMD);
	}
	
	private NOTGate createNOTGate() {
		NOTGate gate = new NOTGate(gateDocument, gateMD);
		
		InteractsWith inputInteraction= promoters.get(0).outputOfInteraction.get(0);
		FunctionalComponent input = inputInteraction.components.get(0).component;
		gate.addInputMolecule(input);

		InteractsWith outputInteraction = cds.get(0).inputOfInteractions.get(0);
		FunctionalComponent output = outputInteraction.components.get(0).component;
		gate.addOutputMolecule(output);
		
		return gate;
	}
	
	private NORGate createNORGate() {
		NORGate gate = new NORGate(gateDocument, gateMD);
		
		InteractsWith input1Interaction = promoters.get(0).outputOfInteraction.get(0);
		FunctionalComponent input1 = input1Interaction.components.get(0).component;
		gate.addInputMolecule(input1);
		
		InteractsWith outputInteraction = cds.get(0).inputOfInteractions.get(0);
		FunctionalComponent output = outputInteraction.components.get(0).component;
		gate.addOutputMolecule(output);
		
		int numOfPromoterRepression = promoters.get(0).outputOfInteraction.size();
		if(numOfPromoterRepression == 1) {
			InteractsWith outputProteinInteraction = outputInteraction.components.get(0).inputOfInteractions.get(0);
			
			InteractsWith complexProteinInteraction = outputProteinInteraction.components.get(0).inputOfInteractions.get(0);
			FunctionalComponent temp1 = complexProteinInteraction.components.get(0).component;
			FunctionalComponent temp2 = complexProteinInteraction.components.get(1).component;
			if(temp1 == output) {
				gate.addInputMolecule(temp2);
			}
			else if(temp2 == output) {
				gate.addInputMolecule(temp1);
			}
			
		}
		else if(numOfPromoterRepression == 2) {
			InteractsWith input2Interaction = promoters.get(0).outputOfInteraction.get(1);
			FunctionalComponent input2 = input2Interaction.components.get(0).component;
			gate.addInputMolecule(input2);
		}
		
		return gate;
	}
	
	private ORGate createORGate() {
		ORGate gate = new ORGate(gateDocument, gateMD);
		
		InteractsWith input1Interaction = promoters.get(0).outputOfInteraction.get(0);
		FunctionalComponent input1 = input1Interaction.components.get(0).component;
		gate.addInputMolecule(input1);
		
		InteractsWith input2Interaction = promoters.get(0).outputOfInteraction.get(1);
		FunctionalComponent input2 = input2Interaction.components.get(0).component;
		gate.addInputMolecule(input2);
		
		InteractsWith outputInteraction = cds.get(0).inputOfInteractions.get(0);
		FunctionalComponent output = outputInteraction.components.get(0).component;
		gate.addOutputMolecule(output);
		
		return gate;
	}
	
	private NANDGate createNANDGate() {
		NANDGate gate = new NANDGate(gateDocument, gateMD);
		
		InteractsWith input1Interaction = promoters.get(0).outputOfInteraction.get(0);
		FunctionalComponent input1 = input1Interaction.components.get(0).component;
		gate.addInputMolecule(input1);
		
		InteractsWith input2Interaction = promoters.get(1).outputOfInteraction.get(0);
		FunctionalComponent input2 = input2Interaction.components.get(0).component;
		gate.addInputMolecule(input2);
		
		InteractsWith outputInteraction = cds.get(0).inputOfInteractions.get(0);
		FunctionalComponent output = outputInteraction.components.get(0).component;
		gate.addOutputMolecule(output);
		
		return gate;
	}
	
	private ANDGate createANDGate() {
		ANDGate gate = new ANDGate(gateDocument, gateMD);
		
		InteractsWith promoterInteraction = promoters.get(0).outputOfInteraction.get(0);
		InteractsWith complexInteraction = promoterInteraction.components.get(0).outputOfInteraction.get(0);
		FunctionalComponent input1 = complexInteraction.components.get(0).component;
		FunctionalComponent input2 = complexInteraction.components.get(1).component;
		gate.addInputMolecule(input1);
		gate.addInputMolecule(input2);
		
		InteractsWith outputInteraction = cds.get(0).inputOfInteractions.get(0);
		FunctionalComponent output = outputInteraction.components.get(0).component;
		gate.addOutputMolecule(output);
		
		return gate;
	}


	private boolean isNOTGate() {
		if(promoters.size() != 1 || cds.size() != 1) {
			return false;
		}
		if(promoters.get(0).outputOfInteraction.size() != 1 || cds.get(0).inputOfInteractions.size() != 1) {
			return false;
		}

		InteractsWith reprInteraction= promoters.get(0).outputOfInteraction.get(0);
		InteractsWith prodInteraction = cds.get(0).inputOfInteractions.get(0);
		if (prodInteraction.type !=  InteractionType.PRODUCTION || reprInteraction.type !=  InteractionType.REPRESSION) {
			return false;
		}
		if (reprInteraction.components.size() != 1 || prodInteraction.components.size() != 1) {
			return false;
		}
		if(checkIfInputIsComplex() || checkIfOutputIsComplex()) {
			return false;
		}
		return reprInteraction.components.get(0).outputOfInteraction.size() == 0 && prodInteraction.components.get(0).inputOfInteractions.size() == 0;
	}

	private boolean isNORGate() {
		if(promoters.size() != 1 || cds.size() != 1) {
			return false;
		}
		if(promoters.get(0).outputOfInteraction.size() < 1 || promoters.get(0).outputOfInteraction.size() > 2 || cds.get(0).inputOfInteractions.size() != 1) {
			return false;
		}

		InteractsWith prodInteraction = cds.get(0).inputOfInteractions.get(0);
		if(promoters.get(0).outputOfInteraction.size() == 1) {
			InteractsWith reprInteraction = promoters.get(0).outputOfInteraction.get(0);
			if (prodInteraction.type !=  InteractionType.PRODUCTION || reprInteraction.type !=  InteractionType.REPRESSION) {
				return false;
			}
			if (reprInteraction.components.size() != 1 || prodInteraction.components.size() != 1) {
				return false;
			}
			return checkIfOutputIsComplex();
		}

		InteractsWith reprInteraction = promoters.get(0).outputOfInteraction.get(0);
		InteractsWith reprInteraction2 = promoters.get(0).outputOfInteraction.get(1);
		if (prodInteraction.type !=  InteractionType.PRODUCTION 
				|| reprInteraction.type !=  InteractionType.REPRESSION
				|| reprInteraction2.type !=  InteractionType.REPRESSION ) {
			return false;
		}
		return (reprInteraction.components.size() == 1 && reprInteraction2.components.size() == 1 && prodInteraction.components.size() == 1);
	}

	private boolean checkIfOutputIsComplex() {
		if( cds.size() != 1) {
			return false;	
		}
		if(cds.get(0).inputOfInteractions.size() != 1 || cds.get(0).outputOfInteraction.size() != 0) {
			return false;
		}
		InteractsWith prodInteraction = cds.get(0).inputOfInteractions.get(0);
		if (prodInteraction.type !=  InteractionType.PRODUCTION) {
			return false;
		}

		return prodInteraction.components.get(0).inputOfInteractions.size() == 1 
				&& prodInteraction.components.get(0).inputOfInteractions.get(0).type == InteractionType.COMPLEX_FORMATION;
	}

	private boolean checkIfInputIsComplex() {
		if(promoters.size() != 1) {
			return false;	
		}
		if(promoters.get(0).outputOfInteraction.size() != 1 || promoters.get(0).inputOfInteractions.size() != 0) {
			return false;
		}
		InteractsWith activInteraction = promoters.get(0).outputOfInteraction.get(0);
		if (activInteraction.type !=  InteractionType.ACTIVATION) {
			return false;
		}
		if(activInteraction.components.size() != 1) {
			return false;
		}
		InteractsWith complexInteraction = activInteraction.components.get(0).outputOfInteraction.get(0);
		return complexInteraction.type == InteractionType.COMPLEX_FORMATION 
				&& complexInteraction.components.size() == 2
				&& complexInteraction.components.get(0).outputOfInteraction.size() == 0
				&& complexInteraction.components.get(1).outputOfInteraction.size() == 0;
	}
	
	private boolean isORGate() {
		if(promoters.size() != 1 || cds.size() != 1) {
			return false;
		}
		if(promoters.get(0).outputOfInteraction.size() != 2 || cds.get(0).inputOfInteractions.size() != 1) {
			return false;
		}

		InteractsWith activInteraction1 = promoters.get(0).outputOfInteraction.get(0);
		InteractsWith activInteraction2 = promoters.get(0).outputOfInteraction.get(1);
		InteractsWith prodInteraction = cds.get(0).inputOfInteractions.get(0);
		if (prodInteraction.type !=  InteractionType.PRODUCTION || activInteraction1.type !=  InteractionType.ACTIVATION || activInteraction2.type !=  InteractionType.ACTIVATION) {
			return false;
		}
		if (activInteraction1.components.size() != 1 || activInteraction1.components.size() != 1 || prodInteraction.components.size() != 1) {
			return false;
		}

		return activInteraction1.components.get(0).outputOfInteraction.size() == 0 
				&& activInteraction2.components.get(0).outputOfInteraction.size() == 0 
				&& prodInteraction.components.get(0).inputOfInteractions.size() == 0;

	}

	private boolean isNANDGate() {
		if(promoters.size() != 2 || cds.size() != 1) {
			return false;
		}
		if(promoters.get(0).outputOfInteraction.size() != 1 || promoters.get(1).outputOfInteraction.size() != 1 
				|| cds.get(0).inputOfInteractions.size() != 1) {
			return false;
		}

		InteractsWith inhibInteraction1= promoters.get(0).outputOfInteraction.get(0);
		InteractsWith inhibInteraction2= promoters.get(1).outputOfInteraction.get(0);
		InteractsWith prodInteraction = cds.get(0).inputOfInteractions.get(0);
		if (prodInteraction.type !=  InteractionType.PRODUCTION || inhibInteraction1.type !=  InteractionType.REPRESSION) {
			return false;
		}
		if (inhibInteraction1.components.size() != 1 || inhibInteraction2.components.size() != 1 
				|| prodInteraction.components.size() != 1) {
			return false;
		}

		return inhibInteraction1.components.get(0).outputOfInteraction.size() == 0 
				&& inhibInteraction2.components.get(0).outputOfInteraction.size() == 0 
				&& prodInteraction.components.get(0).inputOfInteractions.size() == 0;
	}

	private boolean isANDGate() {
		if(cds.size() != 1) {
			return false;	
		}
		if(cds.get(0).inputOfInteractions.size() != 1) {
			return false;
		}
		InteractsWith prodInteraction = cds.get(0).inputOfInteractions.get(0);
		if (prodInteraction.type !=  InteractionType.PRODUCTION) {
			return false;
		}
		if (prodInteraction.components.size() != 1) {
			return false;
		}
		return checkIfInputIsComplex();

	}

	private void addGateInteraction(Module module) throws GateGenerationExeception {
		HashMap<FunctionalComponent, MapsTo> remoteMappings = getRemoteWithMapsTo(module.getMapsTos());

		ModuleDefinition referencedMD = module.getDefinition();
		for(Interaction interaction : referencedMD.getInteractions()) {
			List<ComponentParticipation> outputs =  new ArrayList<>();
			List<ComponentParticipation> inputs = new ArrayList<>();	

			for(Participation participation : interaction.getParticipations()) {
				FunctionalComponent participant = participation.getParticipant();

				if(remoteMappings.containsKey(participant)) {
					MapsTo mapsToObj = remoteMappings.get(participant);
					assert(mapsToObj.getRefinement().equals(RefinementType.VERIFYIDENTICAL));
					FunctionalComponent localFC = (FunctionalComponent) mapsToObj.getLocal();

					if(participationHasRole(participation, SystemsBiologyOntology.INHIBITED)
							|| participationHasRole(participation, SystemsBiologyOntology.STIMULATED) 
							|| participationHasRole(participation, SystemsBiologyOntology.PRODUCT)) {

						outputs.add(fcToInteractionMap.get(localFC.getIdentity()));
					}
					else if(participationHasRole(participation, SystemsBiologyOntology.INHIBITOR)
							|| participationHasRole(participation, SystemsBiologyOntology.STIMULATOR) 
							|| participationHasRole(participation, SystemsBiologyOntology.TEMPLATE)
							|| participationHasRole(participation, SystemsBiologyOntology.REACTANT)) {
						inputs.add(fcToInteractionMap.get(localFC.getIdentity()));
					}
				}

			}
			InteractionType interType = getInteractionType(interaction);
			if(interType != InteractionType.UNKNOWN) {
				for (ComponentParticipation output: outputs) {
					output.outputOfInteraction.add(new InteractsWith(interType, inputs));
				}
				for (ComponentParticipation input : inputs) {
					input.inputOfInteractions.add(new InteractsWith(interType, outputs));
				}
			}
		}
	}


	private InteractionType getInteractionType(Interaction interaction) {
		if(interaction.containsType(SystemsBiologyOntology.INHIBITION)) {
			return InteractionType.REPRESSION;
		}
		if(interaction.containsType(SystemsBiologyOntology.STIMULATION)) {
			return InteractionType.ACTIVATION;
		}
		if(interaction.containsType(SystemsBiologyOntology.GENETIC_PRODUCTION)) {
			return InteractionType.PRODUCTION;
		}
		if(interaction.containsType(SystemsBiologyOntology.NON_COVALENT_BINDING)) {
			return InteractionType.COMPLEX_FORMATION;
		}
		return InteractionType.UNKNOWN;
	}

	private boolean componentDefinitionHasRole(ComponentDefinition cd, URI role) {
		return cd.getRoles().contains(role);
	}

	private boolean participationHasRole(Participation participation, URI role) {
		return participation.getRoles().contains(role); 
	}

	private HashMap<FunctionalComponent, MapsTo> getRemoteWithMapsTo(Set<MapsTo> mapsToList) {
		HashMap<FunctionalComponent, MapsTo> remoteMapping = new HashMap<>();
		for(MapsTo mt : mapsToList) {
			ComponentInstance mappedObj = mt.getRemote();
			if(mappedObj instanceof FunctionalComponent) {
				remoteMapping.put((FunctionalComponent) mappedObj, mt);
			}
		}
		return remoteMapping;
	}

	
	private enum InteractionType {
		REPRESSION,
		COMPLEX_FORMATION,
		ACTIVATION,
		PRODUCTION, 
		UNKNOWN
	}; 

	private class ComponentParticipation {
		FunctionalComponent component;
		List<InteractsWith> inputOfInteractions;
		List<InteractsWith> outputOfInteraction;

		ComponentParticipation(FunctionalComponent fc) {
			inputOfInteractions = new ArrayList<>();
			outputOfInteraction = new ArrayList<>();
			component = fc;
		}
	}

	private class InteractsWith {
		InteractionType type;
		List<ComponentParticipation> components;

		InteractsWith(InteractionType type, List<ComponentParticipation> componentList) {
			this.type = type;
			components = componentList;
		}
	}

}
