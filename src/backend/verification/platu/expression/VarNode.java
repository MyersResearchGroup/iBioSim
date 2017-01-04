package backend.verification.platu.expression;

import java.util.HashMap;
import java.util.HashSet;

import backend.verification.platu.lpn.VarType;

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
	
	@Override
	public int evaluate(int[] stateVector){
		return stateVector[this.index];
	}
	
	public void setIndex(int index){
		this.index = index;
	}
	
	// TODO: No need to pass in the stateVector for this method.
	public int getIndex(int[] stateVector){
		return this.index;
	}
	
	@Override
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
	
	@Override
	public ExpressionNode copy(HashMap<String, VarNode> variables){
		VarNode node = variables.get(this.name);
		if(node == null){
			node = this.clone();
		}
		
		return node;
	}
}
