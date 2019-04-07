package edu.utah.ece.async.ibiosim.synthesis.GateGenerator;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sbolstandard.core2.Component;
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

import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.ANDGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.NANDGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.NORGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.NOTGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.NotSupportedGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.ORGate;

/**
 * Class that examines an SBOL ModuleDefinition enriched by VPR to identify the type of genetic logic gate. 
 * 
 * @author Tramy Nguyen
 */
public class GateIdentifier {
	
	private Map<Interaction, FunctionalComponent> preInteractions;
	private Map<Interaction, FunctionalComponent> postInteractions;
	private List<ComponentDefinition> tu_parts; 
	private List<FunctionalComponent> primaryInputs, primaryOutputs;
	private SBOLDocument gateDocument;
	private ModuleDefinition gateMD;
	
	public GateIdentifier(SBOLDocument doc, ModuleDefinition md) {
		this.preInteractions = new HashMap<>();
		this.postInteractions = new HashMap<>();
		this.tu_parts = new ArrayList<>(); 
		this.primaryInputs = new ArrayList<>();
		this.primaryOutputs = new ArrayList<>();
		this.gateDocument = doc;
		this.gateMD = md;
	}
	
	public String createGate() throws GateGenerationExeception, SBOLValidationException {
		for(Module module : gateMD.getModules()) {
			addGateInteraction(module);
		}
		String gatePattern = "";
		for(FunctionalComponent functionalComponent : gateMD.getFunctionalComponents()) {
			ComponentDefinition tu_cd = functionalComponent.getDefinition();
			if(isFCTranscriptionalUnit(tu_cd)) {
				addTranscriptionalUnitParts(tu_cd);
				HashMap<ComponentDefinition, MapsTo> remoteCDwithMapsTo = getRemoteCDWithMapsTo(functionalComponent.getMapsTos());
				gatePattern = getGatePattern(remoteCDwithMapsTo);
			}
		}
		return gatePattern;
	}
	
	public GeneticGate getGate(String gatePattern) {
		GeneticGate result = null;
		switch(gatePattern) {
		case "prominhibproteincdsprodprotein":
			NOTGate notGate = new NOTGate(gateDocument, gateMD);
			for(FunctionalComponent in : primaryInputs) {
				notGate.addInputMolecule(in);
			}
			for(FunctionalComponent out : primaryOutputs) {
				notGate.addOutputMolecule(out);
			}
			result = notGate;
			break;
		case "prominhibproteininhibproteincdsprodprotein":
		case "prominhibproteincdsprodproteinstimnoncovbstimsmallMol":
		case "prominhibproteincdsprodproteinstimnoncovbstimprotein":
			
			NORGate norGate = new NORGate(gateDocument, gateMD);
			for(FunctionalComponent in : primaryInputs) {
				norGate.addInputMolecule(in);
			}
			for(FunctionalComponent out : primaryOutputs) {
				norGate.addOutputMolecule(out);
			}
			result = norGate;
			break;
		case "promstimproteinstimproteincdsprodprotein":
			ORGate orGate = new ORGate(gateDocument, gateMD);
			for(FunctionalComponent in : primaryInputs) {
				orGate.addInputMolecule(in);
			}
			for(FunctionalComponent out : primaryOutputs) {
				orGate.addOutputMolecule(out);
			}
			result = orGate;
			break;
		case "prominhibproteinprominhibproteincdsprodprotein":
			NANDGate nandGate = new NANDGate(gateDocument, gateMD);
			for(FunctionalComponent in : primaryInputs) {
				nandGate.addInputMolecule(in);
			}
			for(FunctionalComponent out : primaryOutputs) {
				nandGate.addOutputMolecule(out);
			}
			result = nandGate;
			break;
		case "promstimcomplexstimproteinstimproteincdsprodprotein":
			ANDGate andGate = new ANDGate(gateDocument, gateMD);
			for(FunctionalComponent in : primaryInputs) {
				andGate.addInputMolecule(in);
			}
			for(FunctionalComponent out : primaryOutputs) {
				andGate.addOutputMolecule(out);
			}
			result = andGate;
			break;
		default:
			result = new NotSupportedGate(gateDocument, gateMD);
		}
		return result;
	}
	
	private String getGatePattern(HashMap<ComponentDefinition, MapsTo> remoteCDwithMapsTo) throws GateGenerationExeception {
		String gatePattern = "";
		for(ComponentDefinition dnaPart : tu_parts) {
			if(remoteCDwithMapsTo.containsKey(dnaPart)) {
				MapsTo connection = remoteCDwithMapsTo.get(dnaPart);
				List<FunctionalComponent> localFCList = getDNAPartMapsTo(connection);
				for(FunctionalComponent localFC : localFCList) {
					String dnaPattern = getCDRoleAbbreviation(localFC.getDefinition());
					String interactionPattern = getInteractionPattern(localFC);
					FunctionalComponent primaryMolecule = getLastPreInteractionMolecule(localFC);
					addPrimaryInputOutputMolecule(primaryMolecule);
					gatePattern = gatePattern.concat(dnaPattern + interactionPattern);
				}
			}
		}
		return gatePattern;
	}
	
	private List<FunctionalComponent> getDNAPartMapsTo(MapsTo connection) {
		List<FunctionalComponent> localFCList = new ArrayList<>();
		assert(connection.getRefinement().equals(RefinementType.USELOCAL));
		FunctionalComponent localFC = (FunctionalComponent) connection.getLocal();
		ComponentDefinition localCD = localFC.getDefinition();
		
		if(componentDefinitionHasRole(localCD, SequenceOntology.ENGINEERED_REGION)) {
			
			List<FunctionalComponent> nestedMapsToList = getLocalFromMapsTo(localFC.getMapsTos());
			localFCList.addAll(nestedMapsToList);
		}
		else {
			localFCList.add(localFC);
		}
		return localFCList;
	}
	
	private void addPrimaryInputOutputMolecule(FunctionalComponent primaryMolecule) throws GateGenerationExeception {
		FunctionalComponent postFC = getPostInteractionMolecule(primaryMolecule);
		ComponentDefinition postCD = postFC.getDefinition();
		if(componentDefinitionHasType(postCD, ComponentDefinition.DNA_REGION)) {
			if(componentDefinitionHasRole(postCD, SequenceOntology.PROMOTER)) {
				primaryInputs.add(primaryMolecule);
			}
			else if(componentDefinitionHasRole(postCD, SequenceOntology.CDS)) {
				primaryOutputs.add(primaryMolecule);
			}
		}
		else if(componentDefinitionHasType(postCD, ComponentDefinition.COMPLEX)) {
			primaryInputs.add(primaryMolecule);
		}
		
	}
	
	private FunctionalComponent getPostInteractionMolecule(FunctionalComponent fc) throws GateGenerationExeception {
		if(!preInteractions.containsValue(fc)) {
			throw new GateGenerationExeception("No Interactions were found for this functionalComponent: " + fc.getIdentity());
		}
		FunctionalComponent result = null;
		for(Interaction interaction : preInteractions.keySet()) {
			FunctionalComponent preFC = preInteractions.get(interaction);
			if(preFC.equals(fc)) {
				FunctionalComponent postFC = postInteractions.get(interaction);
				if(postFC == null) {
					throw new GateGenerationExeception("Unable to locate inhibited/stimulated/template for this Interaction: " + interaction.getIdentity());
				}
				result = postFC;
			}
		}
		return result;
	}
	
	private FunctionalComponent getLastPreInteractionMolecule(FunctionalComponent fc) throws GateGenerationExeception {
		if(!postInteractions.containsValue(fc)) {
			return fc;
		}
		
		for(Interaction interaction : postInteractions.keySet()) {
			FunctionalComponent postFC = postInteractions.get(interaction);
			if(postFC.equals(fc)) {
				FunctionalComponent preFC = preInteractions.get(interaction);
				if(preFC == null) {
					throw new GateGenerationExeception("Unable to locate inhibitor/stimulator/product for this Interaction: " + interaction.getIdentity());
				}
				fc = getLastPreInteractionMolecule(preFC);
			}
		}

		return fc;
	}

	private String getInteractionPattern(FunctionalComponent fc) throws GateGenerationExeception {
		String interactionPattern = "";
		for(Interaction interaction : postInteractions.keySet()) {
			FunctionalComponent postFC = postInteractions.get(interaction);
			if(postFC.equals(fc)) {
				FunctionalComponent preFC = preInteractions.get(interaction);
				if(preFC == null) {
					throw new GateGenerationExeception("Unable to locate inhibitor/stimulator/product for this Interaction: " + interaction.getIdentity());
				}
				String componentType = getCDTypeAbbreviation(preFC.getDefinition());
				String interactionType = getInteractionTypeAbbreviation(interaction);
				interactionPattern = interactionPattern.concat(interactionType + componentType);
				interactionPattern = interactionPattern + getInteractionPattern(preFC);
			}
		}

		return interactionPattern;
	}
	
	private String getInteractionTypeAbbreviation(Interaction interaction) {
		String result = "";
		if(interactionHasType(interaction, SystemsBiologyOntology.INHIBITION)) {
			result = "inhib";
		}
		else if(interactionHasType(interaction, SystemsBiologyOntology.STIMULATION)) {
			result = "stim";
		}
		else if(interactionHasType(interaction, SystemsBiologyOntology.GENETIC_PRODUCTION)) {
			result = "prod";
		}
		else if(interactionHasType(interaction, SystemsBiologyOntology.NON_COVALENT_BINDING)) {
			result = "noncovb";
		}
		return result;
	}
	
	private String getCDTypeAbbreviation(ComponentDefinition cd) {
		String result = "";
		if(componentDefinitionHasType(cd, ComponentDefinition.PROTEIN)) {
			result = "protein";
		}
		else if(componentDefinitionHasType(cd, ComponentDefinition.COMPLEX)) {
			result = "complex";
		}
		else if(componentDefinitionHasType(cd, ComponentDefinition.SMALL_MOLECULE)) {
			result = "smallMol";
		}
		return result;
	}
	
	private String getCDRoleAbbreviation(ComponentDefinition cd) {
		String result = "";
		if(componentDefinitionHasRole(cd, SequenceOntology.PROMOTER)) {
			result = "prom";
		}
		else if(componentDefinitionHasRole(cd, SequenceOntology.CDS)) {
			result = "cds";
		}
		return result;
	}
	
	private boolean isFCTranscriptionalUnit(ComponentDefinition tu_cd) throws SBOLValidationException {
		List<Component> parts = tu_cd.getSortedComponents(); 
		if(parts.size() == 0) {
			return false;
		}
		boolean beginsWithPromoter = componentDefinitionHasRole(parts.get(0).getDefinition(), SequenceOntology.PROMOTER);
		boolean endsWithTerminator = doesTUEndWithTerminator(parts.get(parts.size() - 1).getDefinition());
		
		return beginsWithPromoter && endsWithTerminator;
	}
	
	private boolean doesTUEndWithTerminator(ComponentDefinition tu) throws SBOLValidationException {
		if(componentDefinitionHasRole(tu, SequenceOntology.TERMINATOR)) {
			return true;
		}
		if(componentDefinitionHasRole(tu, SequenceOntology.ENGINEERED_REGION)) {
			List<Component> dnaParts = tu.getSortedComponents();
			
			ComponentDefinition endCD = dnaParts.get(dnaParts.size() - 1).getDefinition();
			while(componentDefinitionHasRole(endCD, SequenceOntology.ENGINEERED_REGION)) {
				List<Component> nestedParts = endCD.getSortedComponents();
				endCD = nestedParts.get(nestedParts.size() - 1).getDefinition();
				if(componentDefinitionHasRole(endCD, SequenceOntology.TERMINATOR)) {
					return true;
				}
			}
			if(componentDefinitionHasRole(endCD, SequenceOntology.TERMINATOR)) {
				return true;
			}
			//return doesTUEndWithTerminator(dnaParts.get(size-1).getDefinition());
		}
		
		return false;
	}
	
	private void addTranscriptionalUnitParts(ComponentDefinition tu_cd) throws SBOLValidationException{
		for(Component cd_component : tu_cd.getSortedComponents()) {
			ComponentDefinition cd_part = cd_component.getDefinition();
			tu_parts.add(cd_part);
		}
	}
	
	
	
	private void addGateInteraction(Module module) throws GateGenerationExeception {
		HashMap<FunctionalComponent, MapsTo> remoteMappings = getRemoteWithMapsTo(module.getMapsTos());
		
		ModuleDefinition referencedMD = module.getDefinition();
		for(Interaction interaction : referencedMD.getInteractions()) {
			for(Participation participation : interaction.getParticipations()) {
				FunctionalComponent participant = participation.getParticipant();
				
				if(remoteMappings.containsKey(participant)) {
					MapsTo mapsToObj = remoteMappings.get(participant);
					assert(mapsToObj.getRefinement().equals(RefinementType.VERIFYIDENTICAL));
					FunctionalComponent localFC = (FunctionalComponent) mapsToObj.getLocal();
					if(participationHasRole(participation, SystemsBiologyOntology.INHIBITED)
							 || participationHasRole(participation, SystemsBiologyOntology.STIMULATED) 
							 || participationHasRole(participation, SystemsBiologyOntology.TEMPLATE)) {
						postInteractions.put(interaction, localFC);
					}
					else if(participationHasRole(participation, SystemsBiologyOntology.INHIBITOR)
							 || participationHasRole(participation, SystemsBiologyOntology.STIMULATOR) 
							 || participationHasRole(participation, SystemsBiologyOntology.PRODUCT)) {
						preInteractions.put(interaction, localFC);
					}
				}
				
			}
		}
	}
	
	private boolean componentDefinitionHasRole(ComponentDefinition cd, URI role) {
		return cd.getRoles().contains(role);
	}
	
	private boolean interactionHasType(Interaction interaction, URI type) {
		return interaction.containsType(type);
	}
	
	private boolean componentDefinitionHasType(ComponentDefinition cd, URI type) {
		return cd.getTypes().contains(type);
	}
	
	private boolean participationHasRole(Participation participation, URI role) {
		return participation.getRoles().contains(role); 
	}
	
	private List<FunctionalComponent> getLocalFromMapsTo(Set<MapsTo> mapsToList){
		List<FunctionalComponent> list = new ArrayList<>();
		for(MapsTo m : mapsToList) {
			ComponentInstance localInstance = m.getLocal();
			if(localInstance instanceof FunctionalComponent) {
				list.add((FunctionalComponent) localInstance);
			}
		}
		return list;
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

	private HashMap<ComponentDefinition, MapsTo> getRemoteCDWithMapsTo(Set<MapsTo> mapsToList) {
		HashMap<ComponentDefinition, MapsTo> remoteCDwithMapsTo = new HashMap<>();
		for(MapsTo mtObj : mapsToList) {
			ComponentInstance remoteComp = mtObj.getRemote();
			if(remoteComp instanceof Component)  {
				Component remoteInstance = (Component) remoteComp;
				ComponentDefinition remoteCD = remoteInstance.getDefinition();
				remoteCDwithMapsTo.put(remoteCD, mtObj);
			}
		}
		return remoteCDwithMapsTo;
	}
	
	
}
