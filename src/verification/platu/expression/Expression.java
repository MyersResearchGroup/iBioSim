package verification.platu.expression;

import java.util.HashMap;
import java.util.HashSet;

public class Expression {
	String ExprString = null;
	ExpressionNode ExprNode = null;
	HashSet<VarNode> Variables = null;

	public Expression(ExpressionNode expression){
		this.ExprNode = expression;
	}
	
	public Expression(ExpressionNode expression, HashSet<VarNode> variables){
		this.Variables = variables;
		this.ExprNode = expression;
	}
	
	public int evaluate(int[] stateVector){
		return ExprNode.evaluate(stateVector);
	}
	
	public HashSet<VarNode> getVariables(){
		if(Variables == null){
			Variables = new HashSet<VarNode>();
			this.ExprNode.getVariables(Variables);
		}
		
		return Variables;
	}

	@Override
	public String toString(){
		if(ExprString == null){
			ExprString = ExprNode.toString();
		}
		
		return ExprString;
	}
	
	/**
     * Returns a copy of the expression and all subsequent nodes.
     * Variable nodes are replaced with the VarNode indexed at it's name
     * in the variables HashMap, otherwise a new object is created with
     * the same attributes.  Constant nodes are not copied.
     * @param variables - HashMap of variable nodes keyed with their name
     * @return ExpressionNode - New ExpressionNode object
     */
	public Expression copy(HashMap<String, VarNode> variables){
		return new Expression(this.ExprNode.copy(variables));
	}
}
