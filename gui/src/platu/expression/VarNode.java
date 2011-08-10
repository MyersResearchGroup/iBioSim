package platu.expression;

import java.util.HashMap;
import java.util.HashSet;
import platu.lpn.VarType;

import platu.stategraph.state.State;

public class VarNode implements ExpressionNode {
	protected String name;		 // if input, associated output var name
	protected String alias = ""; 	 // if input, original var name
	protected VarType type = VarType.INTERNAL;
	protected int value = 0;
	protected int index = -1;
	
	public VarNode(String name){
		this.name = name;
	}
	
	public VarNode(String name, int index){
		this.name = name;
		this.index = index;
	}
	
	public void setName(String name){
		this.name = name;
	}

	public void setType(VarType type){
		this.type = type;
	}
	
	public VarType getType(){
		return this.type;
	}
	
	public void setAlias(String alias){
		this.alias = alias;
	}
	
	public String getAlias(){
		return this.alias;
	}
	
	public String getName(){
		return this.name;
	}
	
	public int evaluate(){
		return this.value;
	}
	
	public int evaluate(State currentState){
		int[] currentVector = currentState.getVector();
		
		if(this.index < 0){
			Integer i = currentState.getLpn().getVarIndexMap().getValue(name);
			if(i == null){
				System.err.println("error: variable " + name + " does not exist in " + currentState.getLpn().getLabel());
				System.exit(1);
			}
			
			this.index = i;
		}
		
		return currentVector[this.index];
	}
	
	public void setIndex(int index){
		this.index = index;
	}
	
	public int getIndex(State currentState){
		return this.index;
	}
	
	public void getVariables(HashSet<VarNode> variables){
		variables.add(this);
	}
	
	@Override
	public String toString(){
		return name;
	}
	
	@Override
	public VarNode clone(){
		VarNode cloneNode = new VarNode(this.name, this.index);
		cloneNode.setAlias(this.alias);
		cloneNode.setType(this.type);
		return cloneNode;
	}
	
	public ExpressionNode copy(HashMap<String, VarNode> variables){
		VarNode node = variables.get(this.name);
		if(node == null){
			node = this.clone();
		}
		
		return node;
	}
}
