package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;


import java.util.LinkedList;
import java.util.List;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.text.parser.ParseException;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.SBOLValidationException;

import VerilogConstructs.AbstractVerilogConstruct;
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
  			buildSBOLExpression(sbolWrapper, var, synthExpression);
		}
	}
	
	private static void buildSBOLExpression(WrappedSBOL sbolWrapper, String variable, ASTNode expression) throws SBOLValidationException {
		ASTNode design = expression.clone();
		
		LinkedList<FunctionalComponent> inputs = new LinkedList<FunctionalComponent>();
		LinkedList<FunctionalComponent> outputs = new LinkedList<FunctionalComponent>();
		FunctionalComponent design_output = sbolWrapper.getFunctionalComponent(sbolWrapper.getPortMapping(variable));
		outputs.add(design_output);
		
		temp(sbolWrapper, design, design_output);
		//convertLogicFunctions(sbolWrapper, design, inputs, outputs);
	}
	
	private static void temp(WrappedSBOL sbolWrapper, ASTNode node, FunctionalComponent outputProtein) throws SBOLValidationException {
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
			
				temp(sbolWrapper, notOperand.getLeftChild(), inputProtein1);
				temp(sbolWrapper, notOperand.getRightChild(), inputProtein2);
			}
			else {
				FunctionalComponent inputProtein = getInputNode(node, sbolWrapper);
				sbolWrapper.addNOTGate(inputProtein, outputProtein);
				temp(sbolWrapper, notOperand, inputProtein);
			}
		}
	}
	
	private static void convertLogicFunctions(WrappedSBOL sbolWrapper, ASTNode node, LinkedList<FunctionalComponent> inputs, LinkedList<FunctionalComponent> outputs) throws SBOLValidationException{
		//no more expression to build SBOL on
		if(node.getChildCount() < 1){
			return;
		}

		if(node.getType() == ASTNode.Type.LOGICAL_NOT){
			if(node.getChildCount() == 1) {
				ASTNode childNode = node.getChild(0);
				//a NOR gate was found
				if(childNode.getType() == ASTNode.Type.LOGICAL_OR) {
					createInputNodes(sbolWrapper, childNode, inputs);
					sbolWrapper.addNORGate(inputs, outputs);

					convertLogicFunctions(sbolWrapper, childNode.getChild(0), inputs, outputs);
					convertLogicFunctions(sbolWrapper, childNode.getChild(1), inputs, outputs);
				}
				else{
					createInputNodes(sbolWrapper, node, inputs);
					sbolWrapper.addNOTGate(inputs, outputs);
					convertLogicFunctions(sbolWrapper, childNode, inputs, outputs);
				}
			}

		}

	}

	private static FunctionalComponent getInputNode(ASTNode node, WrappedSBOL sbolWrapper) throws SBOLValidationException {
		String inputNode = sbolWrapper.getPortMapping(node.getName());
		if(inputNode != null) {
			return sbolWrapper.getFunctionalComponent(inputNode);
		}
		
		return sbolWrapper.createProtein("wiredProtein", DirectionType.NONE);
	}
	
	private static void createInputNodes(WrappedSBOL sbolWrapper, ASTNode node, LinkedList<FunctionalComponent> inputs) throws SBOLValidationException{
		for(int i = node.getChildCount() - 1; i >= 0; i--) {
			ASTNode child = node.getChild(i);
			String design_port = sbolWrapper.getPortMapping(child.getName());
			
			if(design_port != null) {
				//the variable already exist. 
				inputs.add(sbolWrapper.getFunctionalComponent(design_port));
			}
			else 
			{
				inputs.add(sbolWrapper.createProtein("wiredProtein", DirectionType.NONE));
				
			}
		}
	}

}