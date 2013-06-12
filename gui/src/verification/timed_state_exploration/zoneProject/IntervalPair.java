package verification.timed_state_exploration.zoneProject;

public class IntervalPair {
	
	private int _lowerBound;
	private int _upperBound;
	
	public IntervalPair(int lowerBound, int upperBound) {
		this._lowerBound = lowerBound;
		this._upperBound = upperBound;
	}
	
	public IntervalPair(){
		this._lowerBound = 0;
		this._upperBound = 0;
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
	
	/**
	 * Determines whether this IntervalPair represents a single value.
	 * @return
	 * 		True if lower bound is equal to upper bound.
	 */
	public boolean singleValue(){
		return _lowerBound == _upperBound;
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
	
	/**
	 * Returns the rate in the range with the smallest absolute value. 
	 * @return
	 * 		Let [a,b] be the range of rates. Returns 0 if a<0<b, a if
	 * 		0<a<b and b if a<b<0.
	 */
	public int getSmallestRate(){
		
		if(_lowerBound < 0 && _upperBound >0){
			return 0;
		}
		
		return Math.abs(_lowerBound)<Math.abs(_upperBound) ?
				_lowerBound : _upperBound;
	}
	
	/**
	 * Returns the rate in the range with the largest absolute value.
	 * @return
	 * 		Let [a,b] be the rage of rates.
	 */		
	public int getLargestRate(){
		return Math.abs(_lowerBound)>Math.abs(_upperBound)?
				_lowerBound : _upperBound;
	}
}
