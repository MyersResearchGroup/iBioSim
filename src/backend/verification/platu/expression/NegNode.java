package backend.verification.platu.expression;

import java.util.HashMap;
import java.util.HashSet;

public class NegNode implements ExpressionNode {
	ExpressionNode RightOperand = null;
	
	public NegNode(ExpressionNode rightOperand){
		this.RightOperand = rightOperand;
	}
	
	@Override
	public int evaluate(int[] stateVector){
		if(RightOperand.evaluate(stateVector) == 0)
			return 1;
		
		return 0;
	}
	
	@Override
	public void getVariables(HashSet<VarNode> variables){
		RightOperand.getVariables(variables);
	}
	
	@Override
	public String toString(){
		return "!" + RightOperand.toString();
	}
	
	@Override
	public ExpressionNode copy(HashMap<String, VarNode> variables){
		return new NegNode(this.RightOperand.copy(variables));
	}
}
