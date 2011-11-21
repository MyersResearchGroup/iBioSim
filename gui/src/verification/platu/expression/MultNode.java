package verification.platu.expression;

import java.util.HashMap;
import java.util.HashSet;

public class MultNode implements ExpressionNode {
	ExpressionNode LeftOperand = null;
	ExpressionNode RightOperand = null;
	
	public MultNode(ExpressionNode leftOperand, ExpressionNode rightOperand){
		this.LeftOperand = leftOperand;
		this.RightOperand = rightOperand;
	}
	
	public int evaluate(int[] stateVector){
		return LeftOperand.evaluate(stateVector) * RightOperand.evaluate(stateVector);
	}
	
	public void getVariables(HashSet<VarNode> variables){
		LeftOperand.getVariables(variables);
		RightOperand.getVariables(variables);
	}
	
	@Override
	public String toString(){
		return LeftOperand.toString() + "*" + RightOperand.toString();
	}
	
	public ExpressionNode copy(HashMap<String, VarNode> variables){
		return new MultNode(this.LeftOperand.copy(variables), this.RightOperand.copy(variables));
	}
}
