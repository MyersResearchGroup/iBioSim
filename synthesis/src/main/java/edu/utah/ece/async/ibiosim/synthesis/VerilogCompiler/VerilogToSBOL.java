package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;


import java.util.List;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.text.parser.ParseException;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.SBOLValidationException;

import VerilogConstructs.VerilogAssignment;
import VerilogConstructs.VerilogModule;


/**
 * Convert Verilog to SBOL. This conversion is limited to converting Verilog continuous assignments.
 * @author Tramy Nguyen
 *
 */
public class VerilogToSBOL {

	public static WrappedSBOL convertVerilog2SBOL(VerilogModule module) throws SBOLValidationException, ParseException, VerilogCompilerException {
		WrappedSBOL sbolWrapper = new WrappedSBOL();
		
		//a circuit design in SBOL is represented as a ModuleDefinition
		sbolWrapper.setModuleDefinition(module.getModuleId()); 
		
		convertVerilogInputPorts(sbolWrapper, module.getInputPorts());
		convertVerilogOutputPorts(sbolWrapper, module.getOutputPorts());
		convertVerilogRegisters(sbolWrapper, module.getRegisters());
		convertVerilogContinousAssignments(sbolWrapper, module.getContinousAssignments());
		return sbolWrapper;
	}
	
	private static void convertVerilogInputPorts(WrappedSBOL sbolWrapper, List<String> inputPorts) throws SBOLValidationException {
		for(String input : inputPorts) {
			sbolWrapper.addInput(input);
		}
	}
	
	private static void convertVerilogOutputPorts(WrappedSBOL sbolWrapper, List<String> outputPorts) throws SBOLValidationException {
		for(String output : outputPorts) {
			sbolWrapper.addOutput(output);
		}
	}
	
	private static void convertVerilogRegisters(WrappedSBOL sbolWrapper, List<String> registers) throws SBOLValidationException {
		for(String reg : registers) {
			sbolWrapper.addRegister(reg);
		}
	}
	
	private static void convertVerilogContinousAssignments(WrappedSBOL sbolWrapper, List<VerilogAssignment> contAssigns) throws SBOLValidationException, ParseException, VerilogCompilerException {
		for(VerilogAssignment assign : contAssigns) {
			String var = assign.getVariable();
			ASTNode expression = ASTNode.parseFormula(assign.getExpression());
			ASTNode synthExpression = VerilogSynthesizer.synthesize(expression);
  			
  			FunctionalComponent design_output = sbolWrapper.getFunctionalComponent(sbolWrapper.getPortMapping(var));
  			buildSBOLExpression(sbolWrapper, synthExpression, design_output);
		}
	}
	
	private static void buildSBOLExpression(WrappedSBOL sbolWrapper, ASTNode node, FunctionalComponent outputProtein) throws SBOLValidationException {
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
				FunctionalComponent inputProtein1 = getInputNode(notOperand.getLeftChild(), sbolWrapper);
				FunctionalComponent inputProtein2 = getInputNode(notOperand.getRightChild(), sbolWrapper);
				sbolWrapper.addNORGate(inputProtein1, inputProtein2, outputProtein);
			
				buildSBOLExpression(sbolWrapper, notOperand.getLeftChild(), inputProtein1);
				buildSBOLExpression(sbolWrapper, notOperand.getRightChild(), inputProtein2);
			}
			else {
				FunctionalComponent inputProtein = getInputNode(node, sbolWrapper);
				sbolWrapper.addNOTGate(inputProtein, outputProtein);
				buildSBOLExpression(sbolWrapper, notOperand, inputProtein);
			}
		}
	}

	/**
	 * Returns the equivalent SBOL FunctionalComponent of the given ASTNode if it already exists in the WrappedSBOL. 
	 * If the node does not exist, then a new FunctionalComponent is created and returned from this method.
	 * @param node: The ASTNode to find the equivalent SBOL FunctionalComponent.
	 * @param sbolWrapper: The container for the SBOLDocument that will be accessed to retrieve the FunctionalComponent.
	 * @return An SBOL FunctionalComponent
	 * @throws SBOLValidationException
	 */
	private static FunctionalComponent getInputNode(ASTNode node, WrappedSBOL sbolWrapper) throws SBOLValidationException {
		String inputNode = sbolWrapper.getPortMapping(node.getName());
		if(inputNode != null) {
			return sbolWrapper.getFunctionalComponent(inputNode);
		}
		
		return sbolWrapper.createProtein("wiredProtein", DirectionType.NONE);
	}
	
}