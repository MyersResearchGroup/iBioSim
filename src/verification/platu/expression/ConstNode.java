package verification.platu.expression;

import java.util.HashMap;
import java.util.HashSet;

public class ConstNode implements ExpressionNode {
	int Value = 0;
	String Name;
	
	public ConstNode(String name, int value){
		this.Name = name;
		this.Value = value; 
	}

	@Override
	public int evaluate(int[] stateVector){
		return this.Value;
	}
	
	@Override
	public void getVariables(HashSet<VarNode> variables){

	}
	
	@Override
	public String toString(){
		return "" + Value;
	}
	
	@Override
	public ExpressionNode copy(HashMap<String, VarNode> variables){
		return this;
	}
}
