package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.net.URI;
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


/**
 * An SBOL utility class created for the VerilogToSBOL converter. 
 * @author Tramy Nguyen
 *
 */
public class WrappedSBOL {

	private SBOLDocument sbolDoc;
	private final String sbolVersion = "1.0";
	
	private Map<String, String> portMapping; //map Verilog port names to SBOL FunctionalComponent displayId 
	private Map<String, String> proteinMapping; //map subcircuit proteins with top level circuit proteins using FunctionalComponent dispalyIds
	
	private int interCounter, partiCounter, cdCounter, fcCounter, cCounter, moduleCounter, mdCounter, mapsToCounter; //use to generate unique URIs

	public WrappedSBOL() {
		this.sbolDoc = new SBOLDocument();
		this.sbolDoc.setDefaultURIprefix("http://async.ece.utah.edu/VerilogCompiler/"); 
		this.sbolDoc.setComplete(true);
		this.portMapping = new HashMap<String, String>();
		this.proteinMapping = new HashMap<String, String>();
	}
	
	public ModuleDefinition addCircuit(String id) throws SBOLValidationException {
		return this.sbolDoc.createModuleDefinition(getModuleDefinitionId() + "_" + id, sbolVersion);
	}
	
	public void addInput(ModuleDefinition circuit, String inName) throws SBOLValidationException{
		FunctionalComponent protein = createProtein(circuit, inName, DirectionType.IN);
		portMapping.put(inName, protein.getDisplayId());
	}
	
	public void addOutput(ModuleDefinition circuit, String outName) throws SBOLValidationException{
		FunctionalComponent protein = createProtein(circuit, outName, DirectionType.OUT);
		portMapping.put(outName, protein.getDisplayId());
	}
	
	public void addRegister(ModuleDefinition circuit, String regName) throws SBOLValidationException {
		FunctionalComponent protein = createProtein(circuit, regName, DirectionType.INOUT);
		portMapping.put(regName, protein.getDisplayId());
	}
	
	private ComponentDefinition addComponentDefinition(String compDefId, URI compDefType) throws SBOLValidationException {
		return sbolDoc.createComponentDefinition(compDefId, sbolVersion, compDefType);
	}
	
	private FunctionalComponent addFunctionalComponent(ModuleDefinition circuit, String funcCompId, AccessType accessType, URI compDefId, DirectionType dirType) throws SBOLValidationException {
		return circuit.createFunctionalComponent(funcCompId, accessType, compDefId, dirType);
	}
	
	private Interaction addInteraction(ModuleDefinition circuit, String interactionId, URI interactionType) throws SBOLValidationException {
		return circuit.createInteraction(interactionId, interactionType);
	}
	
	private void addInteractionParticipation(Interaction interaction, URI participationRole, URI participantId) throws SBOLValidationException {
		interaction.createParticipation(getParticipationId(), participantId, participationRole);
	}
	
	private void addInhibitionInteraction(ModuleDefinition circuit, FunctionalComponent inhibitor, FunctionalComponent inhibited) throws SBOLValidationException {
		Interaction inter = addInteraction(circuit, getInteractionId(), SystemsBiologyOntology.INHIBITION);
		addInteractionParticipation(inter, SystemsBiologyOntology.INHIBITOR, inhibitor.getIdentity());
		addInteractionParticipation(inter, SystemsBiologyOntology.INHIBITED, inhibited.getIdentity());
	}
	
	private void addProductionInteraction(ModuleDefinition circuit, FunctionalComponent transcriptionalUnit, FunctionalComponent product) throws SBOLValidationException {
		Interaction inter = addInteraction(circuit, getInteractionId(), SystemsBiologyOntology.GENETIC_PRODUCTION);
		addInteractionParticipation(inter, SystemsBiologyOntology.PROMOTER, transcriptionalUnit.getIdentity()); //TODO: confirm
		addInteractionParticipation(inter, SystemsBiologyOntology.PRODUCT, product.getIdentity());
	}
	
	private FunctionalComponent createTranscriptionalUnit(ModuleDefinition circuit, String tu_id) throws SBOLValidationException {
		String id = "_" + tu_id;
		ComponentDefinition tu = addComponentDefinition(getComponentDefinitionId() + id, ComponentDefinition.DNA);
		tu.addRole(SequenceOntology.ENGINEERED_REGION);
		
		ComponentDefinition promoter = createPromoter();
		ComponentDefinition ribosome = createRibosome();
		ComponentDefinition cds = createCDS();
		ComponentDefinition terminator = createTerminator();
		
		tu.createComponent(getComponentId() + "_part", AccessType.PRIVATE, promoter.getIdentity());
		tu.createComponent(getComponentId() + "_part", AccessType.PRIVATE, ribosome.getIdentity());
		tu.createComponent(getComponentId() + "_part", AccessType.PRIVATE, cds.getIdentity());
		tu.createComponent(getComponentId() + "_part", AccessType.PRIVATE, terminator.getIdentity());
		
		FunctionalComponent tu_instance = addFunctionalComponent(circuit, getFunctionalComponentId() + id, AccessType.PUBLIC, tu.getIdentity(), DirectionType.NONE);
		return tu_instance;
	} 
	
	private ComponentDefinition createPromoter() throws SBOLValidationException{
		String id = "_promoter";
		ComponentDefinition part = addComponentDefinition(getComponentDefinitionId() + id, ComponentDefinition.DNA);
		part.addRole(SequenceOntology.PROMOTER);
		return part;
	}
	
	private ComponentDefinition createRibosome() throws SBOLValidationException{
		String id = "_ribosome";
		ComponentDefinition part = addComponentDefinition(getComponentDefinitionId() + id, ComponentDefinition.DNA);
		part.addRole(SequenceOntology.RIBOSOME_ENTRY_SITE	);
		return part;
	}
	
	private ComponentDefinition createCDS() throws SBOLValidationException{
		String id = "_cds";
		ComponentDefinition part = addComponentDefinition(getComponentDefinitionId() + id, ComponentDefinition.DNA);
		part.addRole(SequenceOntology.CDS);
		return part;
	}
	
	private ComponentDefinition createTerminator() throws SBOLValidationException{
		String id = "_terminator";
		ComponentDefinition part = addComponentDefinition(getComponentDefinitionId() + id, ComponentDefinition.DNA);
		part.addRole(SequenceOntology.TERMINATOR	);
		return part;
	}
	
	public FunctionalComponent createProtein(ModuleDefinition circuit, String proteinId, DirectionType proteinDirection) throws SBOLValidationException {
		String id =  "_" + proteinId;
		ComponentDefinition protein = addComponentDefinition(getComponentDefinitionId() + id, ComponentDefinition.PROTEIN);
		FunctionalComponent protein_fc = addFunctionalComponent(circuit, getFunctionalComponentId() + id, AccessType.PUBLIC, protein.getIdentity(), proteinDirection);
		portMapping.put(proteinId, protein_fc.getDisplayId());
		return protein_fc;
	}

	public void addNOTGate(ModuleDefinition circuit,FunctionalComponent input, FunctionalComponent output) throws SBOLValidationException {
		FunctionalComponent gate = createTranscriptionalUnit(circuit, "notGate");
		addInhibitionInteraction(circuit, input, gate);
		addProductionInteraction(circuit, gate, output);
	}

	public void addNORGate(ModuleDefinition circuit, FunctionalComponent input1, FunctionalComponent input2, FunctionalComponent output) throws SBOLValidationException {
		FunctionalComponent gate = createTranscriptionalUnit(circuit, "norGate");
		addInhibitionInteraction(circuit, input1, gate);
		addInhibitionInteraction(circuit, input2, gate);
		addProductionInteraction(circuit, gate, output);
	}
	
	public void createMapsTo(ModuleDefinition circuit, FunctionalComponent fullCircuitProtein, FunctionalComponent subCircuitProtein) throws SBOLValidationException {
		//fullCircuitProtein.createMapsTo("", RefinementType.USELOCAL, fullCircuitProtein.getDisplayId(), subCircuitProtein.getDisplayId());
		Module circuit_instance = circuit.createModule(getModuleId(), circuit.getDisplayId());
		circuit_instance.createMapsTo(getMapsToId(), RefinementType.USELOCAL, fullCircuitProtein.getDisplayId(), subCircuitProtein.getDisplayId());
	}
	
	public FunctionalComponent getFunctionalComponent(ModuleDefinition circuit, String fc_id) {
		return circuit.getFunctionalComponent(fc_id);
	}

	public String getPortMapping(String verilogPortName) {
		return this.portMapping.get(verilogPortName);
	}
	
	private String getComponentDefinitionId() {
		return "CD" + this.cdCounter++;
	}
	
	private String getComponentId() {
		return "C" + this.cCounter++;
	}
	
	private String getFunctionalComponentId() {
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
	
	private String getModuleDefinitionId() {
		return "MD" + this.mdCounter++;
	}
	
	public SBOLDocument getSBOLDocument(){
		return this.sbolDoc;
	}
}