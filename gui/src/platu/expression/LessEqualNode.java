package platu.expression;

import java.util.HashMap;
import java.util.HashSet;

import platu.stategraph.state.State;

public class LessEqualNode implements ExpressionNode {
	ExpressionNode LeftOperand = null;
	ExpressionNode RightOperand = null;
	
	public LessEqualNode(ExpressionNode leftOperand, ExpressionNode rightOperand){
		this.LeftOperand = leftOperand;
		this.RightOperand = rightOperand;
	}
	
	public int evaluate(State statevector){
		if(LeftOperand.evaluate(statevector) <= RightOperand.evaluate(statevector))
			return 1;
		
		return 0;
	}
	
	public void getVariables(HashSet<VarNode> variables){
		LeftOperand.getVariables(variables);
		RightOperand.getVariables(variables);
	}
	
	@Override
	public String toString(){
		return LeftOperand.toString() + "<=" + RightOperand.toString();
	}
	
	public ExpressionNode copy(HashMap<String, VarNode> variables){
		return new LessEqualNode(this.LeftOperand.copy(variables), this.RightOperand.copy(variables));
	}
}
