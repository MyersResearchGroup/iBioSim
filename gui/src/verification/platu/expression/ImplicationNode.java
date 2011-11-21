package verification.platu.expression;

import java.util.HashMap;
import java.util.HashSet;

public class ImplicationNode implements ExpressionNode {
	ExpressionNode LeftOperand = null;
	ExpressionNode RightOperand = null;
	
	public ImplicationNode(ExpressionNode leftOperand, ExpressionNode rightOperand){
		this.LeftOperand = leftOperand;
		this.RightOperand = rightOperand;
	}

	public int evaluate(int[] stateVector){
		if(LeftOperand.evaluate(stateVector) == 0 || RightOperand.evaluate(stateVector) != 0)
			return 1;
		
		return 0;
	}
	
	public void getVariables(HashSet<VarNode> variables){
		LeftOperand.getVariables(variables);
		RightOperand.getVariables(variables);
	}
	
	@Override
	public String toString(){
		return LeftOperand.toString() + "->" + RightOperand.toString();
	}
	
	public ExpressionNode copy(HashMap<String, VarNode> variables){
		return new ImplicationNode(this.LeftOperand.copy(variables), this.RightOperand.copy(variables));
	}
}
