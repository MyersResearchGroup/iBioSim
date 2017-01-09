package backend.verification.platu.expression;

import java.util.HashMap;
import java.util.HashSet;

public class AddNode implements ExpressionNode{
	ExpressionNode LeftOperand = null;
	ExpressionNode RightOperand = null;
	
	public AddNode(ExpressionNode leftOperand, ExpressionNode rightOperand){
		this.LeftOperand = leftOperand;
		this.RightOperand = rightOperand;
	}
	
	@Override
	public int evaluate(int[] statevector){
		return LeftOperand.evaluate(statevector) + RightOperand.evaluate(statevector);
	}
	
	@Override
	public void getVariables(HashSet<VarNode> variables){
		LeftOperand.getVariables(variables);
		RightOperand.getVariables(variables);
	}
	
	@Override
	public String toString(){
		return LeftOperand.toString() + "+" + RightOperand.toString();
	}
	
	@Override
	public ExpressionNode copy(HashMap<String, VarNode> variables){
		return new AddNode(this.LeftOperand.copy(variables), this.RightOperand.copy(variables));
	}
}
