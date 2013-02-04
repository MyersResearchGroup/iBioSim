package verification.platu.logicAnalysis;

import lpn.parser.Transition;

public class CompositeStateTran {
	private int nextState = 0;
	private int currentState = 0;
	private Transition lpnTransition = null;
	private boolean visible = false;
	
	public CompositeStateTran(CompositeState currentState, CompositeState nextState, Transition lpnTran){
		this.currentState = currentState.getIndex();
		this.nextState = nextState.getIndex();
		this.lpnTransition = lpnTran;
	}
	
	public CompositeStateTran(int currentState, int nextState, Transition lpnTransition){
		this.currentState = currentState;
		this.nextState = nextState;
		this.lpnTransition = lpnTransition;
	}
	
	public void setVisibility(){
		this.visible = true;
	}
	
	public boolean visible(){
		return this.visible;
	}
	
	public void setCurrentState(int currState){
		this.currentState = currState;
	}
	
	public void setNextState(int nxtState){
		this.nextState = nxtState;
	}
	
	public void setLpnTran(Transition lpnTran){
		this.lpnTransition = lpnTran;
	}
	
	public int getCurrentState(){
		return this.currentState;
	}
	
	public int getNextState(){
		return this.nextState;
	}
	
	public Transition getLPNTran(){
		return this.lpnTransition;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.nextState;
		result = prime * result + this.lpnTransition.hashCode();
		result = prime * result + this.currentState;
		
		return result;
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
		if (this.nextState != other.nextState)
			return false;
		
		if (this.lpnTransition != other.lpnTransition)
			return false;
		
		if (this.currentState != other.currentState)
			return false;
		
		return true;
	}
	
	@Override
	public String toString(){
		return (this.currentState + " --" + this.lpnTransition.getFullLabel() + "--> " + this.nextState);
	}
}
