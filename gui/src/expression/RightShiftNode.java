package platu.expression;

import java.util.HashMap;
import java.util.HashSet;

public class RightShiftNode implements ExpressionNode {
	ExpressionNode LeftOperand = null;
	ExpressionNode RightOperand = null;
	
	public RightShiftNode(ExpressionNode leftOperand, ExpressionNode rightOperand){
		this.LeftOperand = leftOperand;
		this.RightOperand = rightOperand;
	}
	
	public int evaluate(int[] stateVector){
		return LeftOperand.evaluate(stateVector) >> RightOperand.evaluate(stateVector);
	}
	
	public void getVariables(HashSet<VarNode> variables){
		LeftOperand.getVariables(variables);
		RightOperand.getVariables(variables);
	}
	
	@Override
	public String toString(){
		return LeftOperand.toString() + ">>" + RightOperand.toString();
	}
	
	public ExpressionNode copy(HashMap<String, VarNode> variables){
		return new RightShiftNode(this.LeftOperand.copy(variables), this.RightOperand.copy(variables));
	}
}
