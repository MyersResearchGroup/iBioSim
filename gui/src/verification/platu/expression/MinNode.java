package verification.platu.expression;

import java.util.HashMap;
import java.util.HashSet;

public class MinNode implements ExpressionNode {
	ExpressionNode RightOperand = null;
	
	public MinNode(ExpressionNode rightOperand){
		this.RightOperand = rightOperand;
	}

	public int evaluate(int[] stateVector){
		return -RightOperand.evaluate(stateVector);
	}
	
	public void getVariables(HashSet<VarNode> variables){
		RightOperand.getVariables(variables);
	}
	
	@Override
	public String toString(){
		return "-" + RightOperand.toString();
	}
	
	public ExpressionNode copy(HashMap<String, VarNode> variables){
		return new MinNode(this.RightOperand.copy(variables));
	}
}
