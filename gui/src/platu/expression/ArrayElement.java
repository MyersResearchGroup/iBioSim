package platu.expression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import platu.stategraph.state.State;

public class ArrayElement extends VarNode{
	List<ExpressionNode> indexVariables = null;
	ArrayNode array = null;
	
	public ArrayElement(ArrayNode array, List<ExpressionNode> indexVars){
		super(array.getName());
		this.array = array;
		this.indexVariables = indexVars;
	}
	
	public ArrayNode getArray(){
		return this.array;
	}
	
	public int evaluate(State statevector){
		List<Integer> indexValues = new ArrayList<Integer>(this.indexVariables.size());
		for(ExpressionNode n : this.indexVariables){
			indexValues.add(n.evaluate(statevector));
		}
		
		return this.array.getElement(indexValues).evaluate(statevector);
	}
	
	public void getVariables(HashSet<VarNode> variables){
		variables.add(this.array);
	}
	
	@Override
	public String toString(){
		String s = array.getName();
		for(ExpressionNode n : this.indexVariables){
			s += "[" + n.toString() + "]";
		}
		
		return s;
	}
	
	@Override
	public int getIndex(State currentState){
		List<Integer> indexValues = new ArrayList<Integer>(this.indexVariables.size());
		for(ExpressionNode n : this.indexVariables){
			indexValues.add(n.evaluate(currentState));
		}
		
		return this.array.getElement(indexValues).getIndex(currentState);
	}
	
	public ExpressionNode copy(HashMap<String, VarNode> variables){
		List<ExpressionNode> copyList = new ArrayList<ExpressionNode>(this.indexVariables.size());
		for(ExpressionNode n : this.indexVariables){
			copyList.add(n.copy(variables));
		}
		
		return new ArrayElement((ArrayNode) this.array.copy(variables), copyList);
	}
}
