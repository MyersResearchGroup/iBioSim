package backend.verification.platu.expression;

import java.util.HashMap;
import java.util.HashSet;

public class TernaryNode implements ExpressionNode {
	ExpressionNode Condition = null;
	ExpressionNode LeftOperand = null;
	ExpressionNode RightOperand = null;
	
	public TernaryNode(ExpressionNode condition, ExpressionNode leftOperand, ExpressionNode rightOperand){
		this.Condition = condition;
		this.LeftOperand = leftOperand;
		this.RightOperand = rightOperand;
	}
	
	@Override
	public int evaluate(int[] stateVector){
		if(this.Condition.evaluate(stateVector) != 0)
			return LeftOperand.evaluate(stateVector);
		
		return RightOperand.evaluate(stateVector);
	}
	
	@Override
	public void getVariables(HashSet<VarNode> variables){
		LeftOperand.getVariables(variables);
		RightOperand.getVariables(variables);
	}
	
	@Override
	public String toString(){
		return Condition + "?" + LeftOperand.toString() + ":" + RightOperand.toString();
	}
	
	@Override
	public ExpressionNode copy(HashMap<String, VarNode> variables){
		return new TernaryNode(this.Condition.copy(variables), this.LeftOperand.copy(variables), this.RightOperand.copy(variables));
	}
}
