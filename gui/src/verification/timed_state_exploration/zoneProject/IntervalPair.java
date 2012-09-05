package verification.timed_state_exploration.zoneProject;

public class IntervalPair {
	
	private int _lowerBound;
	private int _upperBound;
	
	public IntervalPair(int lowerBound, int upperBound) {
		this._lowerBound = lowerBound;
		this._upperBound = upperBound;
	}
	
	public int get_LowerBound() {
		return _lowerBound;
	}
	public void set_LowerBound(int lowerBound) {
		this._lowerBound = lowerBound;
	}
	public int get_UpperBound() {
		return _upperBound;
	}
	public void set_UpperBound(int upperBound) {
		this._upperBound = upperBound;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _lowerBound;
		result = prime * result + _upperBound;
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
		IntervalPair other = (IntervalPair) obj;
		if (_lowerBound != other._lowerBound)
			return false;
		if (_upperBound != other._upperBound)
			return false;
		return true;
	}
	
	public String toString(){
		return "[" + _lowerBound + "," + _upperBound + "]";
	}
}
