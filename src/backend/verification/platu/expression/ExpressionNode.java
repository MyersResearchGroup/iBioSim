package backend.verification.platu.expression;

import java.util.HashMap;
import java.util.HashSet;

public interface ExpressionNode {
    public int evaluate(int[] stateVector);
	public void getVariables(HashSet<VarNode> variables);
	@Override
	public String toString();
	
	/**
     * Returns a copy of the top level node and all subsequent nodes.
     * Variable nodes are replaced with the VarNode indexed at it's name
     * in the variables HashMap, otherwise a new object is created with
     * the same attributes.  Constant nodes are not copied.
     * @param variables - HashMap of variable nodes keyed with their name
     * @return ExpressionNode - New ExpressionNode object
     */
	public ExpressionNode copy(HashMap<String, VarNode> variables);
}

