package verification.platu.expression;

import java.util.HashMap;
import java.util.HashSet;

public class BitAndNode implements ExpressionNode {
	ExpressionNode LeftOperand = null;
	ExpressionNode RightOperand = null;
	
	public BitAndNode(ExpressionNode leftOperand, ExpressionNode rightOperand){
		this.LeftOperand = leftOperand;
		this.RightOperand = rightOperand;
	}
	
	@Override
	public int evaluate(int[] stateVector){
		return LeftOperand.evaluate(stateVector) & RightOperand.evaluate(stateVector);
	}
	
	@Override
	public void getVariables(HashSet<VarNode> variables){
		LeftOperand.getVariables(variables);
		RightOperand.getVariables(variables);
	}
	
	@Override
	public String toString(){
		return LeftOperand.toString() + "&" + RightOperand.toString();
	}
	
	@Override
	public ExpressionNode copy(HashMap<String, VarNode> variables){
		return new BitAndNode(this.LeftOperand.copy(variables), this.RightOperand.copy(variables));
	}
}
