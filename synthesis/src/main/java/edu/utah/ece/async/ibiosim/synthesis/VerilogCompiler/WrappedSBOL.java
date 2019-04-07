package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.Module;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.RefinementType;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SequenceOntology;
import org.sbolstandard.core2.SystemsBiologyOntology;

import edu.utah.ece.async.ibiosim.dataModels.sbol.SBOLUtility;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate;


/**
 * An SBOL utility class created for the VerilogToSBOL converter. 
 * @author Tramy Nguyen
 *
 */
public class WrappedSBOL {

	private SBOLDocument sbolDoc;
	private final String sbolVersion = "1.0";
	
	private Map<String, String> proteinMapping; //map Verilog name to SBOL FunctionalComponent displayId 
	private Map<String, GeneticGate> gateMapping;
	
	//use to generate unique URIs
	private int interCounter, partiCounter, cdCounter, fcCounter, cCounter, moduleCounter, mapsToCounter;

	public WrappedSBOL() {
		this.sbolDoc = SBOLUtility.getInstance().createSBOLDocument();
		this.proteinMapping = new HashMap<>();
		this.gateMapping = new HashMap<>();
	}
	
	public ModuleDefinition createCircuit(String id) throws SBOLValidationException {
		return this.sbolDoc.createModuleDefinition(id, sbolVersion);
	}
	
	private ComponentDefinition addComponentDefinition(String cd_id, URI compDefType) throws SBOLValidationException {
		return sbolDoc.createComponentDefinition(cd_id, sbolVersion, compDefType);
	}
	
	public FunctionalComponent addFunctionalComponent(ModuleDefinition circuit, String fc_id, AccessType accessType, URI compDefId, DirectionType dirType) throws SBOLValidationException {
		return circuit.createFunctionalComponent(fc_id, accessType, compDefId, dirType);
	}
	
	private Interaction addInteraction(ModuleDefinition circuit, String interactionId, URI interactionType) throws SBOLValidationException {
		return circuit.createInteraction(interactionId, interactionType);
	}
	
	private void createInteractionParticipation(Interaction interaction, URI participationRole, URI participantId) throws SBOLValidationException {
		interaction.createParticipation(getParticipationId(), participantId, participationRole);
	}
	
	public Interaction createInhibitionInteraction(ModuleDefinition circuit, FunctionalComponent inhibitor, FunctionalComponent inhibited) throws SBOLValidationException {
		Interaction inter = addInteraction(circuit, getInteractionId() + "_Inhib", SystemsBiologyOntology.INHIBITION);
		createInteractionParticipation(inter, SystemsBiologyOntology.INHIBITOR, inhibitor.getIdentity());
		createInteractionParticipation(inter, SystemsBiologyOntology.INHIBITED, inhibited.getIdentity());
		return inter;
	}
	
	public Interaction createActivationInteraction(ModuleDefinition circuit, FunctionalComponent stimulator, FunctionalComponent stimulated) throws SBOLValidationException {
		Interaction inter = addInteraction(circuit, getInteractionId() + "_Stim", SystemsBiologyOntology.STIMULATION);
		createInteractionParticipation(inter, SystemsBiologyOntology.STIMULATOR, stimulator.getIdentity());
		createInteractionParticipation(inter, SystemsBiologyOntology.STIMULATED, stimulated.getIdentity());
		return inter;
	}
	
	public Interaction createProductionInteraction(ModuleDefinition circuit, FunctionalComponent template, FunctionalComponent product) throws SBOLValidationException {
		Interaction inter = addInteraction(circuit, getInteractionId() + "_Prod", SystemsBiologyOntology.GENETIC_PRODUCTION);
		createInteractionParticipation(inter, SystemsBiologyOntology.TEMPLATE, template.getIdentity());
		createInteractionParticipation(inter, SystemsBiologyOntology.PRODUCT, product.getIdentity());
		return inter;
	}

	public FunctionalComponent createTranscriptionalUnit(ModuleDefinition circuit, String tu_id) throws SBOLValidationException {
		String id = "_" + tu_id;
		ComponentDefinition tu = createEngineeredRegion();
		ComponentDefinition pro1 = createPromoter();
		ComponentDefinition cds = createCDS();
		ComponentDefinition terminator = createTerminator();
		
		tu.createComponent(getComponentId() + "_part", AccessType.PRIVATE, pro1.getIdentity());
		tu.createComponent(getComponentId() + "_part", AccessType.PRIVATE, cds.getIdentity());
		tu.createComponent(getComponentId() + "_part", AccessType.PRIVATE, terminator.getIdentity());
		FunctionalComponent tu_instance = addFunctionalComponent(circuit, getFunctionalComponentId() + id, AccessType.PUBLIC, tu.getIdentity(), DirectionType.NONE);
		return tu_instance;
	}
	
	public ComponentDefinition createEngineeredRegion() throws SBOLValidationException {
		ComponentDefinition er = addComponentDefinition(getComponentDefinitionId() + "_er", ComponentDefinition.DNA_REGION);
		er.addRole(SequenceOntology.ENGINEERED_REGION);
		return er;
	}
	
	public ComponentDefinition createPromoter() throws SBOLValidationException{
		ComponentDefinition part = addComponentDefinition(getComponentDefinitionId() + "_promoter", ComponentDefinition.DNA_REGION);
		part.addRole(SequenceOntology.PROMOTER);
		return part;
	}
	
	public ComponentDefinition createRibosome() throws SBOLValidationException{
		ComponentDefinition part = addComponentDefinition(getComponentDefinitionId() + "_ribosome", ComponentDefinition.DNA_REGION);
		part.addRole(SequenceOntology.RIBOSOME_ENTRY_SITE);
		return part;
	}
	
	public ComponentDefinition createCDS() throws SBOLValidationException{
		ComponentDefinition part = addComponentDefinition(getComponentDefinitionId() + "_cds", ComponentDefinition.DNA_REGION);
		part.addRole(SequenceOntology.CDS);
		return part;
	}
	
	public ComponentDefinition createTerminator() throws SBOLValidationException{
		ComponentDefinition part = addComponentDefinition(getComponentDefinitionId() + "_terminator", ComponentDefinition.DNA_REGION);
		part.addRole(SequenceOntology.TERMINATOR);
		return part;
	}
	
	public FunctionalComponent addProtein(ModuleDefinition circuit, String proteinId, DirectionType proteinDirection) throws SBOLValidationException, SBOLException {
		String id = "_" + proteinId; 
		
		ComponentDefinition protein = addComponentDefinition(getComponentDefinitionId() + id, ComponentDefinition.PROTEIN);
		FunctionalComponent protein_fc = addFunctionalComponent(circuit, getFunctionalComponentId() + id, AccessType.PUBLIC, protein.getIdentity(), proteinDirection);
		proteinMapping.put(proteinId, protein_fc.getDisplayId());
		return protein_fc;
	}
	
	public ArrayList<ComponentDefinition> generatePromoters(int numofPromoters) throws SBOLValidationException{
		ArrayList<ComponentDefinition> promoters = new ArrayList<ComponentDefinition>();
		for(int i = 0; i < numofPromoters; i++) {
			ComponentDefinition promoter = createPromoter();
			promoters.add(promoter);
		}
		return promoters;
	}
	
	public Module addSubCircuit(ModuleDefinition fullCircuit, ModuleDefinition subCircuit) throws SBOLValidationException {
		return fullCircuit.createModule(getModuleId(), subCircuit.getDisplayId());
	}
	
	public void createMapsTo(RefinementType refinementType, Module circuit, FunctionalComponent fullCircuitProtein, FunctionalComponent subCircuitProtein) throws SBOLValidationException {
		circuit.createMapsTo(getMapsToId(), refinementType, fullCircuitProtein.getDisplayId(), subCircuitProtein.getDisplayId());
	}
	
	public FunctionalComponent getFunctionalComponent(ModuleDefinition circuit, String fc_id) throws SBOLException {
		if(circuit.getFunctionalComponent(fc_id) == null) {
			throw new SBOLException("A FunctionalComponent with a displayId: " + fc_id + " does not exist in the given ModuleDefinition with this displayId : " + circuit.getDisplayId(),
					"SBOL object not found");
		}
		return circuit.getFunctionalComponent(fc_id);
	}
	
	public void addGateMapping(String gateId, GeneticGate logicGate) {
		this.gateMapping.put(gateId, logicGate);
	}
	
	/**
	 * Get the displayId of the ComponentDefinition that maps to the given Verilog port name. 
	 * If the ComponentDefinition does not exist for the given Verilog port name, a null is returned. 
	 * @param verilogPortName : Name of the Verilog port name
	 * @return The displayId of the ComponentDefinition that maps to the Verilog port name. Null is returned if the componentDefinition does not exist.
	 */
	public String getProteinMapping(String verilogPortName) {
		return this.proteinMapping.get(verilogPortName);
	}

	public GeneticGate getGateMapping(String gateName) {
		return this.gateMapping.get(gateName);
	}
	
	private String getComponentDefinitionId() {
		return "CD" + this.cdCounter++;
	}
	
	private String getComponentId() {
		return "C" + this.cCounter++;
	}
	
	public String getFunctionalComponentId() {
		return "FC" + fcCounter++;
	}
	
	private String getInteractionId() {
		return "I" + this.interCounter++;
	}
	
	private String getParticipationId() {
		return "P" + this.partiCounter++;
	}

	private String getModuleId() {
		return "M" + this.moduleCounter++;
	}
	
	private String getMapsToId() {
		return "MT" + this.mapsToCounter++;
	}
	
	public SBOLDocument getSBOLDocument(){
		return this.sbolDoc;
	}
}