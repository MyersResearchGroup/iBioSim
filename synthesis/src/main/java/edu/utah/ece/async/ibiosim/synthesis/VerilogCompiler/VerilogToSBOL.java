package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;


import java.util.List;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.text.parser.ParseException;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLValidationException;

import VerilogConstructs.VerilogAssignment;
import VerilogConstructs.VerilogModule;


/**
 * Convert Verilog to SBOL. This conversion is limited to converting Verilog continuous assignments.
 * @author Tramy Nguyen
 *
 */
public class VerilogToSBOL {

	public static WrappedSBOL convertVerilog2SBOL(VerilogModule module, boolean generateflatModel) throws SBOLValidationException, ParseException, VerilogCompilerException {
		WrappedSBOL sbolWrapper = new WrappedSBOL();
		
		ModuleDefinition fullCircuit = sbolWrapper.addCircuit(module.getModuleId());
		convertVerilogInputPorts(fullCircuit, sbolWrapper, module.getInputPorts());
		convertVerilogOutputPorts(fullCircuit, sbolWrapper, module.getOutputPorts());
		convertVerilogRegisters(fullCircuit, sbolWrapper, module.getRegisters());
		convertVerilogContinousAssignments(fullCircuit, generateflatModel, sbolWrapper, module.getContinousAssignments());
		return sbolWrapper;
	}
	
	private static void convertVerilogInputPorts(ModuleDefinition circuit, WrappedSBOL sbolWrapper, List<String> inputPorts) throws SBOLValidationException {
		for(String input : inputPorts) {
			sbolWrapper.createProtein(circuit, input, DirectionType.IN);
		}
	}
	
	private static void convertVerilogOutputPorts(ModuleDefinition circuit, WrappedSBOL sbolWrapper, List<String> outputPorts) throws SBOLValidationException {
		for(String output : outputPorts) {
			sbolWrapper.createProtein(circuit, output, DirectionType.OUT);
		}
	}
	
	private static void convertVerilogRegisters(ModuleDefinition circuit, WrappedSBOL sbolWrapper, List<String> registers) throws SBOLValidationException {
		for(String reg : registers) {
			sbolWrapper.createProtein(circuit, reg, DirectionType.INOUT);
		}
	}
	
	private static void convertVerilogContinousAssignments(ModuleDefinition circuit, boolean generateflatModel, WrappedSBOL sbolWrapper, List<VerilogAssignment> contAssigns) throws SBOLValidationException, ParseException, VerilogCompilerException {
		
		ModuleDefinition currentCircuit = circuit;
		for(VerilogAssignment assign : contAssigns) {
			if(generateflatModel) {
				currentCircuit = sbolWrapper.addCircuit(assign.getVariable());
			}
			convertAssignment(currentCircuit, sbolWrapper, assign);

		}
	}

	private static void convertAssignment(ModuleDefinition circuit, WrappedSBOL sbolWrapper, VerilogAssignment assign) throws SBOLValidationException, ParseException, VerilogCompilerException {

		String var = assign.getVariable();
		ASTNode expression = ASTNode.parseFormula(assign.getExpression());
		ASTNode synthExpression = VerilogSynthesizer.synthesize(expression);

		FunctionalComponent design_output = sbolWrapper.getFunctionalComponent(circuit, sbolWrapper.getPortMapping(var));
		buildSBOLExpression(circuit, sbolWrapper, synthExpression, design_output);
	}
	
	private static void buildSBOLExpression(ModuleDefinition circuit, WrappedSBOL sbolWrapper, ASTNode node, FunctionalComponent outputProtein) throws SBOLValidationException {
		//no more expression to build SBOL on
		if(node.getChildCount() < 1){
			return;
		}
		
		//convert logic gates
		if(node.getType() == ASTNode.Type.LOGICAL_NOT){
			assert(node.getNumChildren() == 1);
			ASTNode notOperand = node.getChild(0);
				
			if(notOperand.getType() == ASTNode.Type.LOGICAL_OR) {
				//a NOR gate was found
				assert(notOperand.getNumChildren() == 2);
				FunctionalComponent inputProtein1 = getInputNode(circuit, notOperand.getLeftChild(), sbolWrapper);
				FunctionalComponent inputProtein2 = getInputNode(circuit, notOperand.getRightChild(), sbolWrapper);
				sbolWrapper.addNORGate(circuit, inputProtein1, inputProtein2, outputProtein);
			
				buildSBOLExpression(circuit, sbolWrapper, notOperand.getLeftChild(), inputProtein1);
				buildSBOLExpression(circuit, sbolWrapper, notOperand.getRightChild(), inputProtein2);
			}
			else {
				FunctionalComponent inputProtein = getInputNode(circuit, notOperand, sbolWrapper);
				sbolWrapper.addNOTGate(circuit, inputProtein, outputProtein);
				buildSBOLExpression(circuit, sbolWrapper, notOperand, inputProtein);
			}
		}
	}

	/**
	 * Returns the equivalent SBOL FunctionalComponent of the given ASTNode if it already exists in the WrappedSBOL. 
	 * If the node does not exist, then a new FunctionalComponent is created and returned from this method.
	 * @param node: The ASTNode to find the equivalent SBOL FunctionalComponent.
	 * @param sbolWrapper: A container for the SBOLDocument that will be accessed to retrieve the FunctionalComponent.
	 * @return An SBOL FunctionalComponent
	 * @throws SBOLValidationException
	 */
	private static FunctionalComponent getInputNode(ModuleDefinition circuit, ASTNode node, WrappedSBOL sbolWrapper) throws SBOLValidationException {
		String inputNode = sbolWrapper.getPortMapping(node.getName());
		if(inputNode != null) {
			return sbolWrapper.getFunctionalComponent(circuit, inputNode);
		}
		
		return sbolWrapper.createProtein(circuit, "wiredProtein", DirectionType.NONE);
	}
	
}