package platu.expression;

import java.util.HashMap;
import java.util.HashSet;

import platu.stategraph.state.State;

public class SubNode implements ExpressionNode {
	ExpressionNode LeftOperand = null;
	ExpressionNode RightOperand = null;
	
	public SubNode(ExpressionNode leftOperand, ExpressionNode rightOperand){
		this.LeftOperand = leftOperand;
		this.RightOperand = rightOperand;
	}
	
	public int evaluate(State statevector){
		return LeftOperand.evaluate(statevector) - RightOperand.evaluate(statevector);
	}
	
	public void getVariables(HashSet<VarNode> variables){
		LeftOperand.getVariables(variables);
		RightOperand.getVariables(variables);
	}
	
	@Override
	public String toString(){
		return LeftOperand.toString() + "-" + RightOperand.toString();
	}
	
	public ExpressionNode copy(HashMap<String, VarNode> variables){
		return new SubNode(this.LeftOperand.copy(variables), this.RightOperand.copy(variables));
	}
}
