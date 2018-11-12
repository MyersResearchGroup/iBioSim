package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.ext.comp.Submodel;
import org.sbml.jsbml.text.parser.ParseException;

import VerilogConstructs.AbstractVerilogConstruct;
import VerilogConstructs.VerilogAlwaysBlock;
import VerilogConstructs.VerilogAssignment;
import VerilogConstructs.VerilogBlock;
import VerilogConstructs.VerilogConditional;
import VerilogConstructs.VerilogDelay;
import VerilogConstructs.VerilogInitialBlock;
import VerilogConstructs.VerilogModule;
import VerilogConstructs.VerilogModuleInstance;
import VerilogConstructs.VerilogWait;


/**
 * Converts a structured Verilog module to SBML.
 * 
 * @author Tramy Nguyen
 */
public class VerilogToSBML {

	/**
	 * Converts a {@link VerilogModule} to an SBML model representing an LPN model.
	 * 
	 * @param module: The Verilog module to convert to SBML
	 * @return An instance of an SBMLWrapper where the SBML model that was converted from the {@link VerilogModule} is contained within.
	 * @throws ParseException: An SBML exception that has occurred when converting information from Verilog to SBML.
	 */
	public static WrappedSBML convertVerilogToSBML(VerilogModule module) throws ParseException {
		WrappedSBML sbmlWrapper = new WrappedSBML();

		sbmlWrapper.setModuleID(module.getModuleId());
		convertVerilogWires(sbmlWrapper, module.getWirePorts());
		convertVerilogInputPorts(sbmlWrapper, module.getInputPorts());
		convertVerilogOutputPorts(sbmlWrapper, module.getOutputPorts());
		convertVerilogRegisters(sbmlWrapper, module.getRegisters());
		convertVerilogContinuousAssignment(sbmlWrapper, module.getContinousAssignments());
		convertVerilogSubmodule(sbmlWrapper,  module.getSubmodules());
		convertVerilogInitialBlock(sbmlWrapper, module.getInitialBlockList());
		convertVerilogAlwaysBlock(sbmlWrapper, module.getAlwaysBlockList());

		return sbmlWrapper;
	}
	
	private static void convertVerilogWires(WrappedSBML sbmlWrapper, List<String> wires) {
		for(String w : wires) {
			sbmlWrapper.addBoolean(w);
		}
	}

	private static void convertVerilogInputPorts(WrappedSBML sbmlWrapper, List<String> inputPorts) {
		for(String currentInput : inputPorts) {
			sbmlWrapper.addInput(currentInput);
		}
	}

	private static void convertVerilogOutputPorts(WrappedSBML sbmlWrapper, List<String> outputPorts) {
		for(String currentOutput : outputPorts) {
			sbmlWrapper.addOutput(currentOutput);
		}
	}

	private static void convertVerilogRegisters(WrappedSBML sbmlWrapper, List<String> registers) {
		for(String currentRegister : registers) {
			sbmlWrapper.addBoolean(currentRegister);
		}
	}
	
	private static void convertVerilogContinuousAssignment(WrappedSBML sbmlWrapper, List<VerilogAssignment> contAssigns) throws ParseException {
		for(VerilogAssignment a : contAssigns) {
			ASTNode expression = ASTNode.parseFormula(a.getExpression());
			sbmlWrapper.addAssignmentRule(a.getVariable(), expression);
		}
	}
	
	private static void convertVerilogSubmodule(WrappedSBML sbmlWrapper, List<VerilogModuleInstance> submoduleList) {
		HashSet<String> moduleReferences = new HashSet<>();
		for(VerilogModuleInstance submodule : submoduleList) {
			
			if(!moduleReferences.contains(submodule.getModuleReference())) {
				sbmlWrapper.addExternalModuleDefinition(submodule.getModuleReference());
				moduleReferences.add(submodule.getModuleReference());
			}
			
			Submodel submodel = sbmlWrapper.addSubmodel(submodule.getModuleReference(), submodule.getSubmoduleId());
			
			Map<String, String> portNamedConnections = submodule.getNamedConnections();
			if(portNamedConnections.size() > 0) {
				for(String wire : portNamedConnections.keySet()) {
					String varName = submodule.getNamedConnections().get(wire);
					sbmlWrapper.addReplacement(wire, submodel.getModelRef(), submodel.getId(), varName);
				}
			}
		}

	}

	/**
	 * 
	 * @param sbmlWrapper
	 * @param initialBlocks
	 * @throws ParseException - Unable to parse expression to set the SBML ASTNode
	 */
	private static void convertVerilogInitialBlock(WrappedSBML sbmlWrapper, List<VerilogInitialBlock> initialBlocks) throws ParseException {
		for(VerilogInitialBlock currentInitialBlock : initialBlocks) {
			VerilogBlock block = (VerilogBlock) currentInitialBlock.getBlock();
			for(AbstractVerilogConstruct currentConstruct : block.getBlockConstructs()) {
				if (currentConstruct instanceof VerilogAssignment) {
					VerilogAssignment blockingAssignment = (VerilogAssignment) currentConstruct;
					ASTNode expression = ASTNode.parseFormula(blockingAssignment.getExpression());
					sbmlWrapper.addInitialAssignment(blockingAssignment.getVariable(), expression);
				}
			}
		}
	}

	private static void convertVerilogAlwaysBlock(WrappedSBML sbmlWrapper, List<VerilogAlwaysBlock> alwaysBlocks) throws ParseException {
		
		for(VerilogAlwaysBlock currentAlwaysBlock : alwaysBlocks) {
			// Start the petri net from the alway block by creating a place
			String startPlace = sbmlWrapper.addPlace(true);
			HashSet<String> preset = new HashSet<String>();
			preset.add(startPlace);
			
			// Convert all constructs found in the always block
			VerilogBlock block = (VerilogBlock) currentAlwaysBlock.getBlock();
			for(AbstractVerilogConstruct currentConstruct : block.getBlockConstructs()) {
				
				if (currentConstruct instanceof VerilogAssignment) {
					VerilogAssignment blockingAssignment = (VerilogAssignment) currentConstruct;
					convertVerilogAssignStatement(sbmlWrapper, blockingAssignment, preset);
				}
				else if(currentConstruct instanceof VerilogConditional) {
					VerilogConditional conditionalStatement = (VerilogConditional) currentConstruct;
					convertVerilogCondition(sbmlWrapper, conditionalStatement, preset);
					
				}
				else if(currentConstruct instanceof VerilogWait) {
					VerilogWait waitStatement = (VerilogWait) currentConstruct;
					convertVerilogWaitStatement(sbmlWrapper, waitStatement, preset);
				}
				else if(currentConstruct instanceof VerilogDelay) {
					VerilogDelay delayConstruct = (VerilogDelay) currentConstruct;
					convertVerilogDelay(sbmlWrapper, delayConstruct, preset);
				}
			}
			
			// End the petri net by point the last transition back to the start of the petri net
			Event endTransition = sbmlWrapper.closeNet();
			sbmlWrapper.addMovementToTransition(preset, endTransition, sbmlWrapper.getTrueNode());
			sbmlWrapper.addMovementToPlace(endTransition, startPlace);
		}
	}
	
	private static void convertVerilogCondition(WrappedSBML sbmlWrapper, VerilogConditional conditionalStatement, HashSet<String> currentPostSet) throws ParseException {
		// Add to the queue the first if condition to perform conversion
		boolean isFirst = true, processedElse = false;
		
		String endPlace = sbmlWrapper.addPlace(false);
		
		LinkedList<AbstractVerilogConstruct> queue = new LinkedList<>();
		queue.add(conditionalStatement);
		
		// Node to AND each inverted if/elseif/else conditions
		ASTNode exclude1 = null;
		
		while(!queue.isEmpty()) {
			AbstractVerilogConstruct currentConstruct = queue.remove();
			if(currentConstruct instanceof VerilogConditional) {
				VerilogConditional currentConditional = (VerilogConditional) currentConstruct;

				if(currentConditional.getIfCondition() == null) {
					new VerilogCompilerException("A null if statement occurred when parsing from an a VerilogConditional.");
				}
				
				ASTNode currentNode = ASTNode.parseFormula(currentConditional.getIfCondition());
				if(isFirst) {
					isFirst = false;
					createVerilogConditionalTransition(sbmlWrapper, currentNode, currentConditional, endPlace, currentPostSet);
					ASTNode notNode = new ASTNode(ASTNode.Type.LOGICAL_NOT);
					notNode.addChild(currentNode);
					
					exclude1 = notNode;
				} 
				else {
					ASTNode andNode = new ASTNode(ASTNode.Type.LOGICAL_AND);
					andNode.addChild(currentNode);
					andNode.addChild(exclude1);
					createVerilogConditionalTransition(sbmlWrapper, andNode, currentConditional, endPlace, currentPostSet);
				
					ASTNode notNode = new ASTNode(ASTNode.Type.LOGICAL_NOT);
					notNode.addChild(currentNode);
					andNode = new ASTNode(ASTNode.Type.LOGICAL_AND);
					andNode.addChild(notNode);
					andNode.addChild(exclude1);
					
					exclude1 = andNode;
				}
				
				// Check if there are any more else if conditions and continue creating the inverted place+transitions
				if(currentConditional.getElseBlock() != null) {
					queue.add(currentConditional.getElseBlock());
				}
			}
			else {
				processedElse = true;
				String currentPlace = sbmlWrapper.addPlace(false);
				Event elseEvent = sbmlWrapper.createConditionalTransition();
				HashSet<String> preset = new HashSet<>();
				preset.add(currentPlace);
				sbmlWrapper.addMovementToTransition(currentPostSet, elseEvent, exclude1);
				sbmlWrapper.addMovementToPlace(elseEvent, currentPlace);
				convertVerilogConditionalBlock(sbmlWrapper, elseEvent, currentConstruct, preset);

				Event dummyEvent = sbmlWrapper.closeNet();
				sbmlWrapper.addMovementToTransition(preset, dummyEvent, sbmlWrapper.getTrueNode());
				sbmlWrapper.addMovementToPlace(dummyEvent, endPlace);
			}
		}
		
		if(!processedElse) {
			// Handle the next line of Verilog code when there is an if statement that does not have an else condition
			Event dummyEvent = sbmlWrapper.closeNet();
			sbmlWrapper.addMovementToTransition(currentPostSet, dummyEvent, exclude1);
			sbmlWrapper.addMovementToPlace(dummyEvent, endPlace);
		}
		
		currentPostSet.clear();
		currentPostSet.add(endPlace);
	}
	
	private static void createVerilogConditionalTransition(WrappedSBML sbmlWrapper, ASTNode nodeConditional, VerilogConditional currentConditional, String endPlace, HashSet<String> currentPostSet) throws ParseException {
		String newPlace = sbmlWrapper.addPlace(false);
		Event ifEvent = sbmlWrapper.createConditionalTransition();
		sbmlWrapper.addMovementToTransition(currentPostSet, ifEvent, nodeConditional);
		sbmlWrapper.addMovementToPlace(ifEvent, newPlace);
		HashSet<String> newPostSet = new HashSet<String>();
		newPostSet.add(newPlace);
		convertVerilogConditionalBlock(sbmlWrapper, ifEvent, currentConditional.getIfBlock(), newPostSet);
		
		Event dummyEvent = sbmlWrapper.closeNet();
		sbmlWrapper.addMovementToTransition(newPostSet, dummyEvent, sbmlWrapper.getTrueNode());
		sbmlWrapper.addMovementToPlace(dummyEvent, endPlace);
	}
	
	private static void convertVerilogConditionalBlock(WrappedSBML sbmlWrapper, Event transitionEvent, AbstractVerilogConstruct block, HashSet<String> currentPostSet) throws ParseException {
		if(block instanceof VerilogBlock) {
			VerilogBlock blockConstruct = (VerilogBlock) block;
			for(AbstractVerilogConstruct construct : blockConstruct.getBlockConstructs()) {
				convertVerilogConditionalBlock(sbmlWrapper, transitionEvent, construct, currentPostSet);
			}
		}
		else if(block instanceof VerilogAssignment) {
			VerilogAssignment assignment = (VerilogAssignment) block;
			convertVerilogAssignStatement(sbmlWrapper, assignment, currentPostSet);
		}
		else if(block instanceof VerilogWait) {
			VerilogWait waitStatement = (VerilogWait) block;
			convertVerilogWaitStatement(sbmlWrapper, waitStatement, currentPostSet);
		}
		else if(block instanceof VerilogConditional) {
			VerilogConditional nestedCondition = (VerilogConditional) block;
			convertVerilogCondition(sbmlWrapper, nestedCondition, currentPostSet);
		}
		else if(block instanceof VerilogDelay) {
			VerilogDelay delayConstruct = (VerilogDelay) block;
			convertVerilogDelay(sbmlWrapper, delayConstruct, currentPostSet);
		}
	}
	
	private static void convertVerilogAssignStatement(WrappedSBML sbmlWrapper, VerilogAssignment blockingAssignment, HashSet<String> preset) {
		// Create place and transition for this assignment 
		String currentPlace = sbmlWrapper.addPlace(false);
		Event currentTransition = sbmlWrapper.createAssignmentTransition();
		
		// Attach this assignment with the previous places
		sbmlWrapper.addMovementToTransition(preset, currentTransition, sbmlWrapper.getTrueNode());
		sbmlWrapper.addMovementToPlace(currentTransition, currentPlace);
		sbmlWrapper.addAssignmentToTransition(currentTransition, blockingAssignment.getVariable(), blockingAssignment.getExpression());
		// Clear out the post place name for the next Verilog syntax
		preset.clear();
		preset.add(currentPlace);
				
		AbstractVerilogConstruct waitConstruct = blockingAssignment.getDelayConstruct();
		if(waitConstruct != null && waitConstruct instanceof VerilogDelay) {
			VerilogDelay delayConstruct = (VerilogDelay) waitConstruct;
			convertVerilogDelay(sbmlWrapper, delayConstruct, preset);
		}
	}
	
	private static void convertVerilogDelay(WrappedSBML sbmlWrapper, VerilogDelay delayConstruct, HashSet<String> preset) {
		String currentPlace = sbmlWrapper.addPlace(false);
		Event delayEvent = sbmlWrapper.createDelay(delayConstruct.getDelayValue());
		
		// Attach this delay with the previous places
		sbmlWrapper.addMovementToTransition(preset, delayEvent, sbmlWrapper.getTrueNode());
		sbmlWrapper.addMovementToPlace(delayEvent, currentPlace);
		
		// Clear out the post place name for the next Verilog syntax
		preset.clear();
		preset.add(currentPlace);
	}
	
	private static void convertVerilogWaitStatement(WrappedSBML sbmlWrapper, VerilogWait waitStatement, HashSet<String> currentPostSet) throws ParseException {

		String currentPlace = sbmlWrapper.addPlace(false);
		Event waitEvent = sbmlWrapper.createWaitTransition();
		ASTNode waitExpression = ASTNode.parseFormula(waitStatement.getWaitExpression());
		sbmlWrapper.addMovementToTransition(currentPostSet, waitEvent, waitExpression);
		sbmlWrapper.addMovementToPlace(waitEvent, currentPlace);

		// Clear out the post place name for the next Verilog syntax
		currentPostSet.clear();
		currentPostSet.add(currentPlace);

		AbstractVerilogConstruct waitConstruct = waitStatement.getDelayConstruct();
		if(waitConstruct != null && waitConstruct instanceof VerilogDelay) {
			VerilogDelay delayConstruct = (VerilogDelay) waitConstruct;
			convertVerilogDelay(sbmlWrapper, delayConstruct, currentPostSet);
		}
	}


}