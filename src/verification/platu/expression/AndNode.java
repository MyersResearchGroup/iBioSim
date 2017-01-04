package verification.platu.expression;

import java.util.HashMap;
import java.util.HashSet;

public class AndNode implements ExpressionNode {
	ExpressionNode LeftOperand = null;
	ExpressionNode RightOperand = null;
	
	public AndNode(ExpressionNode leftOperand, ExpressionNode rightOperand){
		this.LeftOperand = leftOperand;
		this.RightOperand = rightOperand;
	}
	
	@Override
	public int evaluate(int[] statevector){
		if(LeftOperand.evaluate(statevector) == 0 || RightOperand.evaluate(statevector) == 0)
			return 0;
		
		return 1;
	}
	
	@Override
	public void getVariables(HashSet<VarNode> variables){
		LeftOperand.getVariables(variables);
		RightOperand.getVariables(variables);
	}
	
	@Override
	public String toString(){
		return LeftOperand.toString() + "&&" + RightOperand.toString();
	}
	
	@Override
	public ExpressionNode copy(HashMap<String, VarNode> variables){
		return new AndNode(this.LeftOperand.copy(variables), this.RightOperand.copy(variables));
	}
}
