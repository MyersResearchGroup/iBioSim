package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.ext.comp.Submodel;
import org.sbml.jsbml.text.parser.ParseException;

import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.AbstractVerilogConstruct;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogAlwaysBlock;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogAssignment;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogBlock;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogConditional;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogDelay;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogInitialBlock;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogModule;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogModuleInstance;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogWait;


/**
 * Converts a structured Verilog module to SBML.
 * 
 * @author Tramy Nguyen
 */
public class VerilogToSBML {

	private WrappedSBML sbmlWrapper;
	private Map<String, VerilogModule> referredModules;
	
	VerilogToSBML() {
		this.sbmlWrapper = new WrappedSBML();
	}
	
	VerilogToSBML(Map<String, VerilogModule> referredModules) {
		this();
		this.referredModules = referredModules;
	}

	/**
	 * Converts a {@link VerilogModule} to an SBML model representing an LPN model.
	 * 
	 * @param module: The Verilog module to convert to SBML
	 * @return An instance of an SBMLWrapper where the SBML model that was converted from the {@link VerilogModule} is contained within.
	 * @throws ParseException: An SBML exception that has occurred when converting information from Verilog to SBML.
	 */
	public WrappedSBML convertVerilogToSBML(VerilogModule module) throws ParseException {

		sbmlWrapper.setModuleID(module.getModuleId());
		convertVerilogWires(module.getWirePorts());
		convertVerilogInputPorts(module.getInputPorts());
		convertVerilogOutputPorts(module.getOutputPorts());
		convertVerilogRegisters(module.getRegisters());
		convertVerilogSubmodule(module.getSubmodules());
		convertVerilogInitialBlock(module.getInitialBlockList());
		convertVerilogAlwaysBlock(module.getAlwaysBlockList());

		return sbmlWrapper;
	}
	
	private void convertVerilogWires(List<String> wires) {
		for(String w : wires) {
			sbmlWrapper.addBoolean(w);
		}
	}

	private void convertVerilogInputPorts(List<String> inputPorts) {
		for(String currentInput : inputPorts) {
			sbmlWrapper.addInput(currentInput);
		}
	}

	private void convertVerilogOutputPorts(List<String> outputPorts) {
		for(String currentOutput : outputPorts) {
			sbmlWrapper.addOutput(currentOutput);
		}
	}

	private void convertVerilogRegisters(List<String> registers) {
		for(String currentRegister : registers) {
			sbmlWrapper.addBoolean(currentRegister);
		}
	}
	
	private void convertVerilogSubmodule(List<VerilogModuleInstance> submoduleList) {
		HashSet<String> moduleReferences = new HashSet<>();
		for(VerilogModuleInstance submodule : submoduleList) {
			
			if(!moduleReferences.contains(submodule.getModuleReference())) {
				sbmlWrapper.addExternalModuleDefinition(submodule.getModuleReference());
				moduleReferences.add(submodule.getModuleReference());
			}
			
			String referredModuleId = submodule.getModuleReference();
			VerilogModule referredModule = this.referredModules.get(referredModuleId);
			Submodel submodel = sbmlWrapper.addSubmodel(referredModuleId, submodule.getSubmoduleId());
			
			Map<String, String> portNamedConnections = submodule.getNamedConnections();
			if(portNamedConnections.size() > 0) {
				for(String wire : portNamedConnections.keySet()) {
					String varName = submodule.getNamedConnections().get(wire);
					if(referredModule.getInputPorts().contains(varName)){
						sbmlWrapper.addReplacement(wire, submodel.getModelRef(), submodel.getId(), varName);
					}
					else if(referredModule.getOutputPorts().contains(varName)) {
						sbmlWrapper.addReplacedBy(wire, submodel.getModelRef(), submodel.getId(), varName);
					}
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
	private void convertVerilogInitialBlock(List<VerilogInitialBlock> initialBlocks) throws ParseException {
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

	private void convertVerilogAlwaysBlock(List<VerilogAlwaysBlock> alwaysBlocks) throws ParseException {
		
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
					convertVerilogAssignStatement(blockingAssignment, preset);
				}
				else if(currentConstruct instanceof VerilogConditional) {
					VerilogConditional conditionalStatement = (VerilogConditional) currentConstruct;
					convertVerilogCondition(conditionalStatement, preset);
					
				}
				else if(currentConstruct instanceof VerilogWait) {
					VerilogWait waitStatement = (VerilogWait) currentConstruct;
					convertVerilogWaitStatement(waitStatement, preset);
				}
				else if(currentConstruct instanceof VerilogDelay) {
					VerilogDelay delayConstruct = (VerilogDelay) currentConstruct;
					convertVerilogDelay(delayConstruct, preset);
				}
			}
			
			// End the petri net by point the last transition back to the start of the petri net
			Event endTransition = sbmlWrapper.closeNet();
			sbmlWrapper.addMovementToTransition(preset, endTransition, sbmlWrapper.getTrueNode());
			sbmlWrapper.addMovementToPlace(endTransition, startPlace);
		}
	}
	
	private void convertVerilogCondition(VerilogConditional conditionalStatement, HashSet<String> currentPostSet) throws ParseException {
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
					createVerilogConditionalTransition(currentNode, currentConditional, endPlace, currentPostSet);
					ASTNode notNode = new ASTNode(ASTNode.Type.LOGICAL_NOT);
					notNode.addChild(currentNode);
					
					exclude1 = notNode;
				} 
				else {
					ASTNode andNode = new ASTNode(ASTNode.Type.LOGICAL_AND);
					andNode.addChild(currentNode);
					andNode.addChild(exclude1);
					createVerilogConditionalTransition(andNode, currentConditional, endPlace, currentPostSet);
				
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
				convertVerilogConditionalBlock(elseEvent, currentConstruct, preset);

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
	
	private void createVerilogConditionalTransition(ASTNode nodeConditional, VerilogConditional currentConditional, String endPlace, HashSet<String> currentPostSet) throws ParseException {
		String newPlace = sbmlWrapper.addPlace(false);
		Event ifEvent = sbmlWrapper.createConditionalTransition();
		sbmlWrapper.addMovementToTransition(currentPostSet, ifEvent, nodeConditional);
		sbmlWrapper.addMovementToPlace(ifEvent, newPlace);
		HashSet<String> newPostSet = new HashSet<String>();
		newPostSet.add(newPlace);
		convertVerilogConditionalBlock(ifEvent, currentConditional.getIfBlock(), newPostSet);
		
		Event dummyEvent = sbmlWrapper.closeNet();
		sbmlWrapper.addMovementToTransition(newPostSet, dummyEvent, sbmlWrapper.getTrueNode());
		sbmlWrapper.addMovementToPlace(dummyEvent, endPlace);
	}
	
	private void convertVerilogConditionalBlock(Event transitionEvent, AbstractVerilogConstruct block, HashSet<String> currentPostSet) throws ParseException {
		if(block instanceof VerilogBlock) {
			VerilogBlock blockConstruct = (VerilogBlock) block;
			for(AbstractVerilogConstruct construct : blockConstruct.getBlockConstructs()) {
				convertVerilogConditionalBlock(transitionEvent, construct, currentPostSet);
			}
		}
		else if(block instanceof VerilogAssignment) {
			VerilogAssignment assignment = (VerilogAssignment) block;
			convertVerilogAssignStatement(assignment, currentPostSet);
		}
		else if(block instanceof VerilogWait) {
			VerilogWait waitStatement = (VerilogWait) block;
			convertVerilogWaitStatement(waitStatement, currentPostSet);
		}
		else if(block instanceof VerilogConditional) {
			VerilogConditional nestedCondition = (VerilogConditional) block;
			convertVerilogCondition(nestedCondition, currentPostSet);
		}
		else if(block instanceof VerilogDelay) {
			VerilogDelay delayConstruct = (VerilogDelay) block;
			convertVerilogDelay(delayConstruct, currentPostSet);
		}
	}
	
	private void convertVerilogAssignStatement(VerilogAssignment blockingAssignment, HashSet<String> preset) {
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
			convertVerilogDelay(delayConstruct, preset);
		}
	}
	
	private void convertVerilogDelay(VerilogDelay delayConstruct, HashSet<String> preset) {
		String currentPlace = sbmlWrapper.addPlace(false);
		Event delayEvent = sbmlWrapper.createDelay(delayConstruct.getDelayValue());
		
		// Attach this delay with the previous places
		sbmlWrapper.addMovementToTransition(preset, delayEvent, sbmlWrapper.getTrueNode());
		sbmlWrapper.addMovementToPlace(delayEvent, currentPlace);
		
		// Clear out the post place name for the next Verilog syntax
		preset.clear();
		preset.add(currentPlace);
	}
	
	private void convertVerilogWaitStatement(VerilogWait waitStatement, HashSet<String> currentPostSet) throws ParseException {

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
			convertVerilogDelay(delayConstruct, currentPostSet);
		}
	}


}