package platu.logicAnalysis;

import platu.lpn.LPNTran;

public class CompositeStateTran {
	int hashCode = 0;
	private CompositeState headState = null;
	private CompositeState tailState = null;
	private LPNTran lpnTransition = null;
	
	public CompositeStateTran(CompositeState tailState, CompositeState headState, LPNTran lpnTransition){
		this.tailState = tailState;
		this.headState = headState;
		this.lpnTransition = lpnTransition;
	}
	
	public CompositeState getHeadState(){
		return this.headState;
	}
	
	public CompositeState getTailState(){
		return this.tailState;
	}
	
	public LPNTran getLPNTran(){
		return this.lpnTransition;
	}

	@Override
	public int hashCode() {
		if(hashCode == 0){
			final int prime = 31;
			int result = 1;
			result = prime * result + ((headState == null) ? 0 : headState.hashCode());
			result = prime * result+ ((lpnTransition == null) ? 0 : lpnTransition.hashCode());
			result = prime * result+ ((tailState == null) ? 0 : tailState.hashCode());
			hashCode = result;
		}
		
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (getClass() != obj.getClass())
			return false;
		
		CompositeStateTran other = (CompositeStateTran) obj;
		if (headState == null) {
			if (other.headState != null)
				return false;
		} 
		else if (!headState.equals(other.headState))
			return false;
		
		if (lpnTransition == null) {
			if (other.lpnTransition != null)
				return false;
		} 
		else if (!lpnTransition.equals(other.lpnTransition))
			return false;
		
		if (tailState == null) {
			if (other.tailState != null)
				return false;
		} 
		else if (!tailState.equals(other.tailState))
			return false;
		
		return true;
	}
	
	
}
