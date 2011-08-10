package platu.logicAnalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import platu.expression.VarNode;
import platu.lpn.DualHashMap;
import platu.lpn.LPN;
import platu.lpn.LPNTran;
import platu.stategraph.state.State;

public class Constraint{
	private LPN lpn;
	final private int[] interfaceValues;
	final private LPNTran lpnTransition;
	final private int[] vector;
	List<VarNode> variableList = new ArrayList<VarNode>(1);
	List<Integer> valueList = new ArrayList<Integer>(1);
	private int hashVal = -1;

	public Constraint(State start, State end, LPNTran tran, LPN dstLpn) {
	    this.lpnTransition = tran;
		this.lpn = tran.getLpn();
		this.vector = start.getVector();

		int[] endVector = end.getVector();
//		int index = dstLpn.getInterfaceIndex(this.lpn.getLabel());
		int[] thisIndex = dstLpn.getOtherIndexArray(this.lpn.ID-1);
		DualHashMap<String, Integer> varIndexMap = this.lpn.getVarIndexMap();

		this.interfaceValues = new int[thisIndex.length];
		for(int i = 0; i < thisIndex.length; i++){
			int varIndex = thisIndex[i];
			this.interfaceValues[i] = this.vector[varIndex];
			if(this.vector[varIndex] != endVector[varIndex]){
				String variable = varIndexMap.getKey(varIndex);
				this.valueList.add(endVector[varIndex]);
				this.variableList.add(dstLpn.getVarNodeMap().get(variable));
			}
		}

//		if(this.variableList.size() == 0){
//			System.out.println(this.lpnTransition.getFullLabel());
//			System.err.println("error: invalid constraint");
//			System.exit(1);
//		}
	}
	
	/**
     * @return List of modified variables.
     */
	public List<VarNode> getVariableList(){
		return this.variableList;
	}
	
	/**
     * @return List of new variable values.
     */
	public List<Integer> getValueList(){
		return this.valueList;
	}
	
	/**
     * @return State vector.
     */
	public int[] getVector(){
		return this.vector;
	}
	
	/**
     * @return LPN where the constraint was generated.
     */
	public LPN getLpn(){
		return this.lpn;
	}

	/**
     * @return Values of the interface variables.
     */
	public int[] getInterfaceValue(){
		return this.interfaceValues;
	}
	
	/**
     * @return LPNTran applied.
     */
    public LPNTran getLpnTransition(){
    	return this.lpnTransition;
    }

	@Override
	public int hashCode() {
		if(this.hashVal == -1){
			final int prime = 31;
			this.hashVal = 1;
			this.hashVal = prime * this.hashVal + Arrays.hashCode(interfaceValues);
			this.hashVal = prime * this.hashVal + ((this.lpn == null) ? 0 : this.lpn.getLabel().hashCode());
			this.hashVal = prime * this.hashVal + ((this.lpnTransition == null) ? 0 : this.lpnTransition.hashCode());
		}
		
		return this.hashVal;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (getClass() != obj.getClass())
			return false;
		
		Constraint other = (Constraint) obj;
		if (!Arrays.equals(this.interfaceValues, other.interfaceValues))
			return false;
		
		if (this.lpn == null) {
			if (other.lpn != null)
				return false;
		} 
		else if (!this.lpn.equals(other.lpn))
			return false;
		
		if (this.lpnTransition == null) {
			if (other.lpnTransition != null)
				return false;
		} 
		else if (!this.lpnTransition.equals(other.lpnTransition))
			return false;
		
		return true;
	}    
}
