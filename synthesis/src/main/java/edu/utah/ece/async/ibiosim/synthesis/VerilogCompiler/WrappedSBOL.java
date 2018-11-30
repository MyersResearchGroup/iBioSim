package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.ModuleDefinition;
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
	private ModuleDefinition design;
	private final String sbolVersion = "1.0";
	
	private Map<String, String> portMapping; //map verilog inputs to sbol IDs
	
	private int interCounter, partiCounter, cdCounter, fcCounter, cCounter; //use to generate unique URIs

	public WrappedSBOL() {
		this.sbolDoc = new SBOLDocument();
		this.sbolDoc.setDefaultURIprefix("http://async.ece.utah.edu/VerilogCompiler/"); 
		this.sbolDoc.setComplete(true);
		this.portMapping = new HashMap<String, String>();
	}
	
	public void setModuleDefinition(String id) throws SBOLValidationException {
		this.design = this.sbolDoc.createModuleDefinition(id, sbolVersion);
	}

	public void addInput(String inName) throws SBOLValidationException{
		FunctionalComponent protein = createProtein(inName, DirectionType.IN);
		portMapping.put(inName, protein.getDisplayId());
	}
	
	public void addOutput(String outName) throws SBOLValidationException{
		FunctionalComponent protein = createProtein(outName, DirectionType.OUT);
		portMapping.put(outName, protein.getDisplayId());
	}
	
	public void addRegister(String regName) throws SBOLValidationException {
		FunctionalComponent protein = createProtein(regName, DirectionType.INOUT);
		portMapping.put(regName, protein.getDisplayId());
	}
	
	private ComponentDefinition addComponentDefinition(String compDefId, URI compDefType) throws SBOLValidationException {
		return sbolDoc.createComponentDefinition(compDefId, sbolVersion, compDefType);
	}
	
	private FunctionalComponent addFunctionalComponent(String funcCompId, AccessType accessType, URI compDefId, DirectionType dirType) throws SBOLValidationException {
		return design.createFunctionalComponent(funcCompId, accessType, compDefId, dirType);
	}
	
	private Interaction addInteraction(String interactionId, URI interactionType) throws SBOLValidationException {
		return this.design.createInteraction(interactionId, interactionType);
	}
	
	private void addInteractionParticipation(Interaction interaction, URI participationRole, URI participantId) throws SBOLValidationException {
		interaction.createParticipation(getParticipationId(), participantId, participationRole);
	}
	
	private void addInhibitionInteraction(FunctionalComponent inhibitor, FunctionalComponent inhibited) throws SBOLValidationException {
		Interaction inter = addInteraction(getInteractionId(), SystemsBiologyOntology.INHIBITION);
		addInteractionParticipation(inter, SystemsBiologyOntology.INHIBITOR, inhibitor.getIdentity());
		addInteractionParticipation(inter, SystemsBiologyOntology.INHIBITED, inhibited.getIdentity());
	}
	
	private void addProductionInteraction(FunctionalComponent transcriptionalUnit, FunctionalComponent product) throws SBOLValidationException {
		Interaction inter = addInteraction(getInteractionId(), SystemsBiologyOntology.GENETIC_PRODUCTION);
		addInteractionParticipation(inter, SystemsBiologyOntology.PROMOTER, transcriptionalUnit.getIdentity()); //TODO: confirm
		addInteractionParticipation(inter, SystemsBiologyOntology.PRODUCT, product.getIdentity());
	}
	
	private FunctionalComponent createTranscriptionalUnit(String tu_id) throws SBOLValidationException {
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
		
		FunctionalComponent tu_instance = addFunctionalComponent(getFunctionalComponentId() + id, AccessType.PUBLIC, tu.getIdentity(), DirectionType.NONE);
		return tu_instance;
	} 
	
	public ComponentDefinition createPromoter() throws SBOLValidationException{
		String id = "_promoter";
		ComponentDefinition part = addComponentDefinition(getComponentDefinitionId() + id, ComponentDefinition.DNA);
		part.addRole(SequenceOntology.PROMOTER);
		return part;
	}
	
	public ComponentDefinition createRibosome() throws SBOLValidationException{
		String id = "_ribosome";
		ComponentDefinition part = addComponentDefinition(getComponentDefinitionId() + id, ComponentDefinition.DNA);
		part.addRole(SequenceOntology.RIBOSOME_ENTRY_SITE	);
		return part;
	}
	
	public ComponentDefinition createCDS() throws SBOLValidationException{
		String id = "_cds";
		ComponentDefinition part = addComponentDefinition(getComponentDefinitionId() + id, ComponentDefinition.DNA);
		part.addRole(SequenceOntology.CDS);
		return part;
	}
	
	public ComponentDefinition createTerminator() throws SBOLValidationException{
		String id = "_terminator";
		ComponentDefinition part = addComponentDefinition(getComponentDefinitionId() + id, ComponentDefinition.DNA);
		part.addRole(SequenceOntology.TERMINATOR	);
		return part;
	}
	
	public FunctionalComponent createProtein(String proteinId, DirectionType proteinDirection) throws SBOLValidationException {
		String id =  "_" + proteinId;
		ComponentDefinition protein = addComponentDefinition(getComponentDefinitionId() + id, ComponentDefinition.PROTEIN);
		FunctionalComponent protein_fc = addFunctionalComponent(getFunctionalComponentId() + id, AccessType.PUBLIC, protein.getIdentity(), proteinDirection);
		portMapping.put(proteinId, protein_fc.getDisplayId());
		return protein_fc;
	}

	public void addNOTGate(FunctionalComponent input, FunctionalComponent output) throws SBOLValidationException {
		FunctionalComponent gate = createTranscriptionalUnit("notGate");
	
		addInhibitionInteraction(input, gate);
		addProductionInteraction(gate, output);
	}

	public void addNORGate(FunctionalComponent input1, FunctionalComponent input2, FunctionalComponent output) throws SBOLValidationException {
		
		FunctionalComponent gate = createTranscriptionalUnit("norGate");
		addInhibitionInteraction(input1, gate);
		addInhibitionInteraction(input2, gate);
		addProductionInteraction(gate, output);
	}
	
	
	public void addNOTGate(LinkedList<FunctionalComponent> inputs, LinkedList<FunctionalComponent> outputs) throws SBOLValidationException {
		FunctionalComponent gate = createTranscriptionalUnit("notGate");
		FunctionalComponent gateInput = inputs.removeFirst();
		FunctionalComponent gateOutput = outputs.removeFirst();
		
		addInhibitionInteraction(gateInput, gate);
		addProductionInteraction(gate, gateOutput);
		
		outputs.add(gateInput);
	}
	
	public void addNORGate(LinkedList<FunctionalComponent> inputs, LinkedList<FunctionalComponent> outputs) throws SBOLValidationException {
		FunctionalComponent gate = createTranscriptionalUnit("norGate");
		FunctionalComponent gateInput1 = inputs.removeFirst();
		FunctionalComponent gateInput2 = inputs.removeFirst();
		FunctionalComponent gateOutput = outputs.removeFirst();
		
		addInhibitionInteraction(gateInput1, gate);
		addInhibitionInteraction(gateInput2, gate);
		addProductionInteraction(gate, gateOutput);
		
		outputs.add(gateInput1);
		outputs.add(gateInput2);
	}
	
	public FunctionalComponent getFunctionalComponent(String fc_id) {
		return this.design.getFunctionalComponent(fc_id);
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

	public SBOLDocument getSBOLDocument(){
		return this.sbolDoc;
	}
}