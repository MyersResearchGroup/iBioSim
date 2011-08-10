package platu.expression;

import java.util.HashMap;
import java.util.HashSet;

import platu.stategraph.state.State;

public class TernaryNode implements ExpressionNode {
	ExpressionNode Condition = null;
	ExpressionNode LeftOperand = null;
	ExpressionNode RightOperand = null;
	
	public TernaryNode(ExpressionNode condition, ExpressionNode leftOperand, ExpressionNode rightOperand){
		this.Condition = condition;
		this.LeftOperand = leftOperand;
		this.RightOperand = rightOperand;
	}
	
	public int evaluate(State statevector){
		if(this.Condition.evaluate(statevector) != 0)
			return LeftOperand.evaluate(statevector);
		
		return RightOperand.evaluate(statevector);
	}
	
	public void getVariables(HashSet<VarNode> variables){
		LeftOperand.getVariables(variables);
		RightOperand.getVariables(variables);
	}
	
	@Override
	public String toString(){
		return Condition + "?" + LeftOperand.toString() + ":" + RightOperand.toString();
	}
	
	public ExpressionNode copy(HashMap<String, VarNode> variables){
		return new TernaryNode(this.Condition.copy(variables), this.LeftOperand.copy(variables), this.RightOperand.copy(variables));
	}
}
