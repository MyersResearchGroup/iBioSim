package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.util.LinkedList;

import org.sbml.jsbml.ASTNode;

/**
 * Decompose a given expression to NOT and OR logic functions.
 * @author Tramy Nguyen
 */
public class VerilogSynthesizer{
	
	public static ASTNode synthesize(ASTNode logicFunction) throws VerilogCompilerException {
		
		LinkedList<ASTNode> operands = new LinkedList<ASTNode>();
		return synthesize_recurs(logicFunction.clone(), operands);
	}
	
	
	private static ASTNode synthesize_recurs(ASTNode logicFunction, LinkedList<ASTNode> operands) throws VerilogCompilerException{
		//locate the base operand 
		if(logicFunction.getChildCount() > 2){
			throw new VerilogCompilerException("Synthesizer does not support more than 2 input logic functions");
		}
		if(logicFunction.getChildCount() < 1) {
			return logicFunction;
		}
		
		//append children to parent node and build expression bottom-up
		ASTNode result = null;
		
		for(int i = 0; i < logicFunction.getChildCount(); i++){
			ASTNode temp = synthesize_recurs(logicFunction.getChild(i), operands);
			operands.add(temp);
		}
		
		if(logicFunction.getType() == ASTNode.Type.LOGICAL_NOT) {
			ASTNode notNode = new ASTNode(ASTNode.Type.LOGICAL_NOT);
			notNode.addChild(operands.removeLast());
			result = notNode;

		}
		else if(logicFunction.getType() == ASTNode.Type.LOGICAL_AND){
			result = convertAndToNor(operands);
		}
		else if(logicFunction.getType() == ASTNode.Type.LOGICAL_OR) {
			result = convertOrToNor(operands);
			
		}
		else{
			throw new VerilogCompilerException("The following expression contains a logic function that isn't supported during synthesis: " + logicFunction.getName());
		}
		
		return result;
	}
	
	private static ASTNode invertNode(ASTNode node) {
		ASTNode not = new ASTNode(ASTNode.Type.LOGICAL_NOT);
		not.addChild(node);
		
		return not;
	}
	
	
	private static ASTNode convertAndToNor(LinkedList<ASTNode> operands) {
		ASTNode or = new ASTNode(ASTNode.Type.LOGICAL_OR);
		
		ASTNode righOp = invertNode(operands.removeLast());
		ASTNode leftOP = invertNode(operands.removeLast());
		or.addChild(leftOP);
		or.addChild(righOp);
		
		return invertNode(or);
	}
	
	private static ASTNode convertOrToNor(LinkedList<ASTNode> operands) {
		return invertNode(invertNode(createOR(operands)));
	}
	
	private static ASTNode createOR(LinkedList<ASTNode> operands) {
		ASTNode or = new ASTNode(ASTNode.Type.LOGICAL_OR);
		
		ASTNode righOp = operands.removeLast();
		ASTNode leftOp = operands.removeLast();
		or.addChild(leftOp);
		or.addChild(righOp);
		
		return or;
	}

}