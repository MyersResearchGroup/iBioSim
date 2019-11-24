package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.util.HashMap;
import java.util.List;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.text.parser.ParseException;
import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Module;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.RefinementType;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.NORGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.NOTGate;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogAssignment;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogModule;


/**
 * Convert Verilog to SBOL. This conversion is limited to converting Verilog continuous assignments.
 * 
 * @author Tramy Nguyen
 */
public class VerilogToSBOL {

	private final boolean isFlatModel;
	private WrappedSBOL sbolWrapper;
	private int dummyGateCounter, dummyInputCounter;
	
	private HashMap<String, FunctionalComponent> primaryProteins; //map verilog port names to its FunctionalComponent displayId

	public VerilogToSBOL(boolean generateFlatModel) {
		this.isFlatModel = generateFlatModel;
		this.sbolWrapper = new WrappedSBOL();
		this.primaryProteins = new HashMap<>();
	}
	
	public WrappedSBOL convertVerilog2SBOL(VerilogModule module) throws SBOLValidationException, ParseException, VerilogCompilerException, SBOLException {
		
		ModuleDefinition fullCircuit = sbolWrapper.createModuleDefinition(module.getModuleId());
		
		convertVerilogPorts(fullCircuit, module.getInputPorts(), DirectionType.IN);
		convertVerilogPorts(fullCircuit, module.getOutputPorts(), DirectionType.OUT);
		convertVerilogPorts(fullCircuit, module.getRegisters(), DirectionType.INOUT);
		convertVerilogPorts(fullCircuit, module.getWirePorts(), DirectionType.INOUT);
		
		convertVerilogContinousAssignments(fullCircuit, module.getContinousAssignments());
		return sbolWrapper;
	}

	private void convertVerilogPorts(ModuleDefinition circuit, List<String> verilogPorts, DirectionType portType) throws SBOLValidationException, SBOLException {
		for(String port : verilogPorts) {
			FunctionalComponent portMolecule = sbolWrapper.createProtein(circuit, port, portType);
			portMolecule.setName(port); 
			primaryProteins.put(port, portMolecule);
		}
	}
	
	private void convertVerilogContinousAssignments(ModuleDefinition circuit, List<VerilogAssignment> contAssigns) throws SBOLValidationException, ParseException, VerilogCompilerException, SBOLException {
		for(VerilogAssignment assign : contAssigns) {
			String var = assign.getVariable();
			ASTNode expression = ASTNode.parseFormula(assign.getExpression());
			
			FunctionalComponent fullCircuit_outputProtein = primaryProteins.get(var);
			HashMap<FunctionalComponent, FunctionalComponent> primaryProteins = new HashMap<>();
			
			if(!isFlatModel) {
				ModuleDefinition subCircuit = sbolWrapper.createModuleDefinition("circuit_" + var);
				Module subCircuit_instance = sbolWrapper.createModule(circuit, subCircuit);
				
				FunctionalComponent subCircuit_outputProtein = sbolWrapper.addFunctionalComponent(subCircuit, sbolWrapper.getFunctionalComponentId() + "_" + var, AccessType.PUBLIC, fullCircuit_outputProtein.getDefinition().getIdentity(), DirectionType.OUT);
				addPrimaryProteinMapping(var, primaryProteins, subCircuit_outputProtein);
				
				buildSBOLExpression(subCircuit, expression, subCircuit_outputProtein, primaryProteins);
		
				//Connect primary input and output proteins for full circuit and subcircuit.
				for(FunctionalComponent subcircuit_InputProtein : primaryProteins.keySet()) {
					FunctionalComponent portProtein = primaryProteins.get(subcircuit_InputProtein);
					if(portProtein.getDirection().equals(DirectionType.IN)) {
						sbolWrapper.createMapsTo(RefinementType.VERIFYIDENTICAL, subCircuit_instance, portProtein, subcircuit_InputProtein);
					}
					else if(portProtein.getDirection().equals(DirectionType.OUT)) {
						sbolWrapper.createMapsTo(RefinementType.VERIFYIDENTICAL, subCircuit_instance, portProtein, subcircuit_InputProtein);
					}
				}
			}
			else {
				buildSBOLExpression(circuit, expression, fullCircuit_outputProtein, primaryProteins);
			}
		}
	}

	private void buildSBOLExpression(ModuleDefinition circuit, ASTNode logicNode, FunctionalComponent outProtein, HashMap<FunctionalComponent, FunctionalComponent> mapped_primaryProteins) throws SBOLValidationException, SBOLException, VerilogCompilerException {
		//no more expression to build SBOL on
		if(logicNode.getChildCount() < 1){
			return;
		}

		//convert logic gates
		if(logicNode.getType() == ASTNode.Type.LOGICAL_NOT){
			assert(logicNode.getNumChildren() == 1);
			ASTNode notOperand = logicNode.getChild(0);
			String gateId = logicNode.toString();
			
			if(notOperand.getType() == ASTNode.Type.LOGICAL_OR) {
				//a NOR gate was found
				assert(notOperand.getNumChildren() == 2);
				NORGate norGate = createNORGate(circuit, gateId);
				
				ASTNode in1Node = notOperand.getLeftChild();
				ASTNode in2Node = notOperand.getRightChild();
				FunctionalComponent gateTU = norGate.getTranscriptionalUnit();
				FunctionalComponent in1Protein = addInput(circuit, in1Node.toString(), norGate, gateTU, mapped_primaryProteins);
				FunctionalComponent in2Protein = addInput(circuit, in2Node.toString(), norGate, gateTU, mapped_primaryProteins);
				addOutput(circuit, norGate, outProtein, gateTU);
				sbolWrapper.addGateMapping(gateId, norGate);

				buildSBOLExpression(circuit, in1Node, in1Protein, mapped_primaryProteins);
				buildSBOLExpression(circuit, in2Node, in2Protein, mapped_primaryProteins);

			}
			else if(notOperand.getType() == ASTNode.Type.LOGICAL_AND) {
				//a NAND gate was found
				assert(notOperand.getNumChildren() == 2);
				ASTNode in1Node = notOperand.getLeftChild();
				ASTNode in2Node = notOperand.getRightChild();
						
				String not1GateId = getDummyGateId();
				NOTGate not1Gate = createNOTGate(circuit, not1GateId);
				FunctionalComponent not1Tu = not1Gate.getTranscriptionalUnit(); 
				FunctionalComponent notIn1Protein= addInput(circuit, getDummyInputId(), not1Gate, not1Tu, mapped_primaryProteins); 
				addOutput(circuit, not1Gate, outProtein, not1Tu);
				sbolWrapper.addGateMapping(not1GateId, not1Gate);
				
				String norGateId = getDummyGateId();
				NORGate norGate = createNORGate(circuit, norGateId);
				FunctionalComponent norTu = norGate.getTranscriptionalUnit();
				FunctionalComponent norIn1Protein = addInput(circuit, getDummyInputId(), norGate, norTu, mapped_primaryProteins);
				FunctionalComponent norIn2Protein = addInput(circuit, getDummyInputId(), norGate, norTu, mapped_primaryProteins);
				addOutput(circuit, norGate, notIn1Protein, norTu);
				sbolWrapper.addGateMapping(norGateId, norGate);
				
				String not2GateId = getDummyGateId();
				NOTGate not2Gate = createNOTGate(circuit, not2GateId);
				FunctionalComponent not2Tu = not2Gate.getTranscriptionalUnit(); 
				FunctionalComponent notIn2Protein= addInput(circuit, in1Node.toString(), not2Gate, not2Tu, mapped_primaryProteins); 
				addOutput(circuit, not2Gate, norIn1Protein, not2Tu);
				sbolWrapper.addGateMapping(not2GateId, not2Gate);
				
				String not3GateId = getDummyGateId();
				NOTGate not3Gate = createNOTGate(circuit, not3GateId);
				FunctionalComponent not3Tu = not3Gate.getTranscriptionalUnit(); 
				FunctionalComponent notIn3Protein= addInput(circuit, in2Node.toString(), not3Gate, not3Tu, mapped_primaryProteins); 
				addOutput(circuit, not3Gate, norIn2Protein, not3Tu);
				sbolWrapper.addGateMapping(not3GateId, not2Gate);

				buildSBOLExpression(circuit, in1Node, notIn2Protein, mapped_primaryProteins);
				buildSBOLExpression(circuit, in2Node, notIn3Protein, mapped_primaryProteins);
			}
			else {
				//a NOT gate was found
				NOTGate notGate = createNOTGate(circuit, gateId);
				FunctionalComponent gateTU = notGate.getTranscriptionalUnit(); 
				FunctionalComponent inProtein = addInput(circuit, notOperand.toString(), notGate, gateTU, mapped_primaryProteins); 
				addOutput(circuit, notGate, outProtein, gateTU);
				sbolWrapper.addGateMapping(gateId, notGate);
				buildSBOLExpression(circuit, notOperand, inProtein, mapped_primaryProteins);
			}
		}
	}
	
	/**
	 * Retrieve the desired protein from the given circuit with the given verilog variable name.
	 * If a protein does not exist with the given verilog variable name a new protein is created and returned. 
	 * @param portName: The gate's input name to find the equivalent SBOL FunctionalComponent.
	 * @return The desired protein represented as a FunctionalComponent 
	 * @throws SBOLValidationException
	 * @throws SBOLException
	 */
	private FunctionalComponent getProtein(ModuleDefinition circuit, String portName) throws SBOLValidationException, SBOLException {
		String proteinId = sbolWrapper.getProteinMapping(portName);
		if(proteinId != null) {	
			if(!this.isFlatModel) {
				if(primaryProteins.containsKey(portName)) {
					//duplicate the protein for a subcircuit and connect to full circuit 
					FunctionalComponent fullCircuit_Protein = primaryProteins.get(portName);
					
					//handle feedback by changing output proteins as input for subcircuit
					DirectionType mappedDirection = fullCircuit_Protein.getDirection().equals(DirectionType.OUT) ? DirectionType.IN : fullCircuit_Protein.getDirection();
					
					return sbolWrapper.addFunctionalComponent(circuit, sbolWrapper.getFunctionalComponentId() + "_" + portName, AccessType.PUBLIC, fullCircuit_Protein.getDefinitionIdentity(), mappedDirection);
				}
			}
			return sbolWrapper.getFunctionalComponent(circuit, proteinId);
		}
		
		return sbolWrapper.createProtein(circuit, "wiredProtein", DirectionType.INOUT);
	}

	private FunctionalComponent addInput(ModuleDefinition circuit, String nodeId, GeneticGate logicGate, FunctionalComponent tu, HashMap<FunctionalComponent, FunctionalComponent> primaryProteinList) throws SBOLValidationException, VerilogCompilerException, SBOLException {
		FunctionalComponent inputProtein = null;
		GeneticGate inputGate = sbolWrapper.getGateMapping(nodeId);
		if(inputGate != null) {
			inputProtein = getOutputProteinForGate(inputGate);
			logicGate.addInputMolecule(inputProtein);
			sbolWrapper.createInhibitionInteraction(circuit, inputProtein, tu);
		}
		else {
			inputProtein = getProtein(circuit, nodeId);
			//inputProtein already is an input on the current gate. Don't create interaction and continue. This will solve output proteins with multiple fan-outs
			if(!checkIfProteinIsInput(inputProtein, logicGate)) {
				logicGate.addInputMolecule(inputProtein);
				sbolWrapper.createInhibitionInteraction(circuit, inputProtein, tu);;
			}
		}
		addPrimaryProteinMapping(nodeId, primaryProteinList, inputProtein);
		
		return inputProtein;
	}
	
	private FunctionalComponent getOutputProteinForGate(GeneticGate gate) {
		List<FunctionalComponent> gateOutputs = gate.getListOfOutputs();
		assert(gateOutputs.size() == 1);
		return gateOutputs.get(0);
	}
	
	private void addPrimaryProteinMapping(String logicId, HashMap<FunctionalComponent, FunctionalComponent> proteinList, FunctionalComponent protein) {
		if(primaryProteins.containsKey(logicId)){
			FunctionalComponent fullCircuit_inputProtein = primaryProteins.get(logicId);
			proteinList.put(protein, fullCircuit_inputProtein);
		}
	}
	
	private void addOutput(ModuleDefinition circuit, GeneticGate logicGate, FunctionalComponent outputProtein, FunctionalComponent tu) throws SBOLValidationException {
		//outputProtein is already set as an output onto the current gate. Don't create interaction and continue.
		if(!checkIfProteinIsOutput(outputProtein, logicGate)) {
			logicGate.addOutputMolecule(outputProtein);
			sbolWrapper.createProductionInteraction(circuit, tu, outputProtein);
		}
	}
	
	private boolean checkIfProteinIsInput(FunctionalComponent inputProtein, GeneticGate logicGate) {
		if(logicGate instanceof NOTGate) {
			NOTGate gate = (NOTGate)logicGate;
			return gate.containsInput(inputProtein);
		}
		else if(logicGate instanceof NORGate) {
			NORGate gate = (NORGate)logicGate;
			for(FunctionalComponent fc : gate.getListOfInputs()) {
				if(fc.equals(inputProtein)) {
					return true;
				}
			}
			return false;
		}
		return false;
	}
	
	private boolean checkIfProteinIsOutput(FunctionalComponent outputProtein, GeneticGate logicGate) {
		return logicGate.containsOutput(outputProtein);
	}
	
	/**
	 * Retrieve the NORGate mapped from the given gateId. A new NORGate will be created and returned if an existing NORGate was not found from the given gateId.
	 * @param circuit: The circuit where the expected NORGate should be contained in.
	 * @param gateId: The gateId used to look up the expected NORGate.
	 * @return The NORGate that was added to the given circuit.
	 * @throws SBOLValidationException
	 * @throws VerilogCompilerException
	 */
	private NORGate createNORGate(ModuleDefinition circuit, String gateId) throws SBOLValidationException, VerilogCompilerException {
		GeneticGate currentGate = sbolWrapper.getGateMapping(gateId);
		NORGate sbolNOR = null;
		if(currentGate == null) {
			//this NOR gate does not exist. Create one. 
			FunctionalComponent norTU = sbolWrapper.createTranscriptionalUnit(circuit, "norTU");
			sbolNOR = new NORGate(sbolWrapper.getSBOLDocument(), circuit);
			sbolNOR.setTranscriptionalUnit(norTU);
		}
		else if(currentGate instanceof NORGate){
			sbolNOR = (NORGate) currentGate; 
		}
		else {
			throw new VerilogCompilerException("Expected " + gateId + " to be of type NORGate but is actually " + currentGate.getClass());
		}
		
		return sbolNOR;
	}
	
	/**
	 * Retrieve the NOTGate mapped from the given gateId. A new NOTGate will be created and returned if an existing NOTGate was not found from the given gateId.
	 * @param circuit: The circuit where the expected NOTGate should be contained in.
	 * @param gateId: The gateId used to look up the expected NOTGate.
	 * @return The NOTGate that was added to the given circuit.
	 * @throws SBOLValidationException
	 * @throws VerilogCompilerException
	 */
	private NOTGate createNOTGate(ModuleDefinition circuit, String gateId) throws SBOLValidationException, VerilogCompilerException {
		GeneticGate currentGate = sbolWrapper.getGateMapping(gateId);
		NOTGate sbolNOT = null;
		if(currentGate == null) {
			//this NOT gate does not exist. Create one. 
			FunctionalComponent notTU = sbolWrapper.createTranscriptionalUnit(circuit, "notTU");
			sbolNOT = new NOTGate(sbolWrapper.getSBOLDocument(), circuit);
			sbolNOT.setTranscriptionalUnit(notTU);
		}
		else if(currentGate instanceof NOTGate){
			sbolNOT = (NOTGate) currentGate;
		}
		else {
			throw new VerilogCompilerException("Expected " + gateId + " to be of type NOTGate but is actually " + currentGate.getClass());
		}
		
		return sbolNOT;
	}
	
	private String getDummyGateId() {
		return "dummyG" + this.dummyGateCounter++;
	}
	
	private String getDummyInputId() {
		return "dummyIn" + this.dummyInputCounter++;
	}
}