package platu.logicAnalysis;

public class CompositeStatePair {
	private CompositeState state1;
	private CompositeState state2;
	private int hashCode = 0;
	
	public CompositeStatePair(CompositeState st1, CompositeState st2){
		this.state1 = st1;
		this.state2 = st2;
	}

	public CompositeState getState1(){
		return this.state1;
	}
	
	public CompositeState getState2(){
		return this.state2;
	}
	
	@Override
	public int hashCode() {
		if(this.hashCode == 0){
			final int prime = 31;
			this.hashCode = 1;
			this.hashCode = prime * this.hashCode + ((this.state1 == null) ? 0 : this.state1.hashCode());
			this.hashCode = prime * this.hashCode + ((this.state2 == null) ? 0 : this.state2.hashCode());
		}
		
		return this.hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (getClass() != obj.getClass())
			return false;
		
		CompositeStatePair other = (CompositeStatePair) obj;
		if (state1 == null) {
			if (other.state1 != null)
				return false;
		} 
		else if (!state1.equals(other.state1))
			return false;
		
		if (state2 == null) {
			if (other.state2 != null)
				return false;
		} 
		else if (!state2.equals(other.state2))
			return false;
		
		return true;
	}
}
