package verification.platu.logicAnalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lpn.parser.LhpnFile;
import lpn.parser.Transition;

import verification.platu.expression.VarNode;
import verification.platu.lpn.DualHashMap;
import verification.platu.stategraph.State;

public class Constraint{
	private LhpnFile lpn;
	final private int[] interfaceValues;
	final private Transition lpnTransition;
	final private int[] vector;
	List<VarNode> variableList = new ArrayList<VarNode>(1);
	List<Integer> valueList = new ArrayList<Integer>(1);
	private int hashVal = -1;

	public Constraint(State start, State end, Transition firedTran, Transition lpn2) {
	    this.lpnTransition = firedTran;
	    this.lpn = firedTran.getLpn();
		//this.lpn = firedTran.getLpn();
		this.vector = start.getVector();

		int[] endVector = end.getVector();
//		int index = dstLpn.getInterfaceIndex(this.lpn.getLabel());
		// TODO: (temp) Need to find out what ID is.
		int[] thisIndex = null; // lpn2.getOtherIndexArray(this.lpn.ID-1);
		DualHashMap<String, Integer> varIndexMap = this.lpn.getVarIndexMap();

		this.interfaceValues = new int[thisIndex.length];
		for(int i = 0; i < thisIndex.length; i++){
			int varIndex = thisIndex[i];
			this.interfaceValues[i] = this.vector[varIndex];
			if(this.vector[varIndex] != endVector[varIndex]){
				String variable = varIndexMap.getKey(varIndex);
				this.valueList.add(endVector[varIndex]);
				// TODO: (temp) need to replace getVarNodeMap.
				this.variableList.add(null);
				//this.variableList.add(lpn2.getVarNodeMap().get(variable));
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
	public LhpnFile getLpn(){
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
    public Transition getLpnTransition(){
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
